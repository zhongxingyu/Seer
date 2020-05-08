 /*
  * Copyright 2012 C24 Technologies.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *			http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package biz.c24.io.spring.batch.reader;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.regex.Pattern;
 
 import javax.annotation.PostConstruct;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.batch.core.StepExecution;
 import org.springframework.batch.core.annotation.AfterStep;
 import org.springframework.batch.core.annotation.BeforeStep;
 import org.springframework.batch.item.ItemReader;
 import org.springframework.batch.item.NonTransientResourceException;
 import org.springframework.batch.item.ParseException;
 import org.springframework.batch.item.UnexpectedInputException;
 import org.springframework.util.Assert;
 
 import biz.c24.io.api.data.ComplexDataObject;
 import biz.c24.io.api.data.Element;
 import biz.c24.io.api.data.ValidationException;
 import biz.c24.io.api.data.ValidationManager;
 import biz.c24.io.api.presentation.Source;
 import biz.c24.io.api.presentation.TextualSource;
 import biz.c24.io.api.presentation.XMLSource;
 import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
 import biz.c24.io.spring.core.C24Model;
 import biz.c24.io.spring.source.SourceFactory;
 import biz.c24.io.spring.source.TextualSourceFactory;
 
 /**
  * ItemReader that reads ComplexDataObjects from a BufferedReaderSource.
  * Optionally supports the ability to split the incoming data stream into entities by use of a
  * regular expression to detect the start of a new entity; this allows the more expensive parsing 
  * to be performed in parallel.
  * 
  * The optional splitting process currently assumes that each line:
  * a) Is terminated with a platform specific CRLF (or equivalent)
  * b) Belongs to at most one entity
  * 
  * In all cases the optional validation takes place in parallel if multiple threads are used.
  * 
  * @author Andrew Elmore
  */
 public class C24ItemReader implements ItemReader<ComplexDataObject> {
 	
 	private static Logger LOG = LoggerFactory.getLogger(C24ItemReader.class);
 	
 	/**
 	 * SourceFactory to use to generate our IO Sources
 	 */
 	private SourceFactory ioSourceFactory = null;
 	
 	/**
 	 * IO Source to use where we do not have an elementStartPattern
 	 */
 	private volatile Source ioSource = null;
 	/**
	 * Cache for IO sources where we can parallelise parsing
 	 */
 	private ThreadLocal<Source> threadedIOSource = new ThreadLocal<Source>();
 	
 	/**
 	 * The type of CDO that we will parse from the source
 	 */
 	private Element elementType;
 	
 	/**
 	 * An optional pattern to use to quickly split the readerSource so we can perform more heavyweight
 	 * parsing in parallel
 	 */
 	private Pattern elementStartPattern = null;
 	
 	/**
 	 * The source from which we'll read the data
 	 */
 	private BufferedReaderSource source;
 
 	/**
 	 * The lineTerminator we use to join lines from a message back together.
 	 * Determined once when we start processing files.
 	 */
 	private String lineTerminator = null;
 
 	/**
 	 * Control whether or not we validate the parsed CDOs
 	 */
 	private boolean validate = false;
 	private ThreadLocal<ValidationManager> validator = new ThreadLocal<ValidationManager>();
 	
 	public C24ItemReader() {
 
 	}
 	
 	/**
 	 * Asserts that we have been properly configured
 	 */
 	@PostConstruct
 	public void validateConfiguration() {
 		Assert.notNull(elementType, "Element type must be set, either explicitly or by setting the model");
 		Assert.notNull(source, "Source must be set");
 	}
 	
 	/**
 	 * Returns the element type that we will attempt to parse from the source
 	 */
 	public Element getElementType() {
 		return elementType;
 	}
 
 	/**
 	 * Set the type of element that we will attempt to parse from the source
 	 * 
 	 * @param elementType The type of element that we want to parse from the source
 	 */
 	public void setElementType(Element elementType) {
 		this.elementType = elementType;
 	}
 	
 	/**
 	 * Allows setting of element type via the supplied model
 	 * 
 	 * @param model The model of the type we wish to parse
 	 */
 	public void setModel(C24Model model) {
 		elementType = model.getRootElement();
 	}
 	
 	/**
 	 * Returns the regular expression that we're using to split up in the incoming data.
 	 * Null if not set.
 	 */
 	public String getElementStartPattern() {
 		return elementStartPattern != null? elementStartPattern.pattern() : null;
 	}
 
 	/**
 	 * Sets the regular expression used to quickly split up the source into individual entities for parsing
 	 * 
 	 * @param elementStartRegEx The regular expression to identify the start of a new entity in the source
 	 */
 	public void setElementStartPattern(String elementStartRegEx) {
 		this.elementStartPattern = Pattern.compile(elementStartRegEx);
 	}
 	
 	/**
 	 * Set whether or not you want validation to be performed on the parsed CDOs. 
 	 * An exception will be thrown for any entity which fails validation.
 	 * 
 	 * @param validate Whether or not to validate parsed CDOs
 	 */
 	public void setValidate(boolean validate) {
 		this.validate = validate;
 	}
 	
 	/**
 	 * Query whether or not this ItemReader will validate parsed CDOs
 	 * 
 	 * @return True iff this ItemReader will automtically validate read CDOs
 	 */
 	public boolean isValidating() {
 		return validate;
 	}
 	
 	/**
 	 * Gets the BufferedReaderSource from which CDOs are being parsed
 	 * 
 	 * @return This reader's BufferedReaderSource
 	 */
 	public BufferedReaderSource getSource() {
 		return source;
 	}
 
 	/**
 	 * Sets the source that this reader will read from
 	 * 
 	 * @param source The BufferedReaderSource to read data from
 	 */
 	public void setSource(BufferedReaderSource source) {
 		this.source = source;
 	}
 	
 	/**
 	 * Sets the iO source factory to use
 	 * 
 	 * @param ioSourceFactory
 	 */
 	public void setSourceFactory(SourceFactory ioSourceFactory) {
 		this.ioSourceFactory = ioSourceFactory;
 	}
 	
 	public SourceFactory getSourceFactory() {
 		return this.ioSourceFactory;
 	}
 	
 	/**
 	 * Initialise our context
 	 * 
 	 * @param stepExecution The step execution context
 	 */
 	@BeforeStep
 	public void setup(StepExecution stepExecution) {		
 		source.initialise(stepExecution);
 	}
 	
 	/**
 	 * Clean up and resources we're consuming
 	 */
 	@AfterStep
 	public void cleanup() {
 		source.close();
 	}
 	
 	/**
 	 * In the parallel/splitting case, when we detect the start of the next message we will effectively
 	 * consume the first line of the next entity's data. For now we simplistically rewind the buffer to the
 	 * start of the line.
 	 * This requires us to mark the buffer pre-read and to tell it what are the maximum number of bytes we might 
 	 * read and still rewind.
 	 * 
 	 * TODO Currently hardcoded, this value should either be made configurable or the read data cached (by reader) 
 	 * rather than rewinding the BufferedReader
 	 * 
 	 */
 	private static final int MAX_MESSAGE_SIZE = 1000000;
 	
 	/**
 	 * Extracts the textual data for an element from the BufferedReader using the elementStartPattern to split
 	 * up the data. If this instance has not yet determined the lineTerminator being used, it will read the reader
 	 * character by character until it finds one of the following line terminators:
 	 * \r\n
 	 * \r
 	 * \n
 	 * 
 	 * Once the line terminator has been determined, it will be used for all subsequent calls to readElement; 
 	 * even if the BufferedReaderSource is changed.
 	 * 
 	 * @param reader The BufferedReader to extract the element from
 	 */
 	private String readElement(BufferedReader reader) {
 
 		StringBuffer elementCache = new StringBuffer();
 		boolean inElement = false;		
 		
 		synchronized(reader) {
 			try {
 				while(reader.ready()) {
 					// Mark the stream in case we need to rewind (ie if we read the start line for the next element)
 					reader.mark(MAX_MESSAGE_SIZE);
 					String line = null;
 					if(lineTerminator != null) {
 						line = reader.readLine();
 					} else {
 						// We need to parse the file to determine the line terminator
 						// We support \n, \r and \r\n
 						StringBuffer buffer = new StringBuffer();
 						int curr;
 						while(lineTerminator == null) {
 							curr = reader.read();
 							if(curr == -1) {
 								// EOF - we don't know if this is the terminator or not. Assume not
 								break;
 							} else if(curr == '\n') {
 								lineTerminator = "\n";
 								LOG.debug("Determined line terminator is \\n");
 							} else if(curr == '\r') {
 								// Need to see if we're \r or \r\n
 								// We can safely mark; we're the first line hence no danger of being asked to reset later on
 								reader.mark(1);
 								curr = reader.read();
 								if(curr == '\n') {
 									lineTerminator = "\r\n";
 									LOG.debug("Determined line terminator is \\r\\n");
 								} else {
 									lineTerminator = "\r";
 									LOG.debug("Determined line terminator is \\r");
 									reader.reset();
 								}
 							} else {
 								buffer.append((char)curr);
 							}
 						}
 						
 						line = buffer.toString();
 					}
 				
 					if(line != null) {					
 						if(elementStartPattern.matcher(line).matches()) {
 							// We've encountered the start of a new element
 							String message = elementCache.toString();
 							if(message.trim().length() > 0) {
 								// We were already parsing an element; thus we've finished extracting our element
 								// Rewind the stream...
 								reader.reset();
 								// ...and return what we have already extracted
 								return message;
 							} else {
 								// This is the start of our element. Add it to our elementCache.
 								elementCache.append(line);
 								if(lineTerminator != null) {
 									elementCache.append(lineTerminator);
 								}
 								inElement = true;
 							}
 						} else if(inElement) {
 							// More data for our current element
 							elementCache.append(line);
 							if(lineTerminator != null) {
 								elementCache.append(lineTerminator);
 							}
 						}
 					}
 				}
 			} catch(IOException ioEx) {
 				throw new NonTransientResourceException("Failed to extract entity", ioEx);
 			}
 		}
 		
 		return elementCache.toString();
 	}
 	
 	/**
 	 * Called once a thread determines it has exhausted the current iO source (more accurately, the underlying Reader).
 	 * Triggers creation of an appropriate new Source next time getParser is called.
 	 * 
 	 * @param parser The source that has been exhausted.
 	 */
 	private void discardParser(Source parser) {
 		// If there's no splitting pattern, we have to ensure that we discard the underlying reader too
 		if(elementStartPattern == null) {
 			source.discard(parser.getReader());
 		}
 		if(this.elementStartPattern == null && source.useMultipleThreadsPerReader()) {
 			synchronized(this) {
 				if(ioSource == parser) {
 					ioSource = null;
 				}
 			}
 		} else {
 			threadedIOSource.set(null);
 		}
 	}
 	
 	/**
 	 * Gets the appropriate iO Source to use to read the message.
 	 * If ioSourceFactory is not set, it defaults to the model's default source.
 	 * 
 	 * @param An optional BufferedReader to pass to the source's setReader method
 	 * 
 	 * @return A configured iO source
 	 */
 	private Source getIoSource(BufferedReader reader) {
 		Source source = null;
 		
 		if(ioSourceFactory == null) {
 			// Use the default
 			source = elementType.getModel().source();
 			if(reader != null) {
 				source.setReader(reader);
 			}
 		} else {
 			// If the reader is null, we have to give the factory a dummy one
 			source = ioSourceFactory.getSource(reader != null? reader : new StringReader(""));
 		}
 		
 		if(source instanceof TextualSource) {
 			((TextualSource)source).setEndOfDataRequired(false);
 		}
 		
 		return source;
 	}
 	
 	/**
 	 * Gets a configured iO source for this thread to use to parse messages.
 	 * Depending on configuration, threads may or may not share the source.
 	 * 
 	 * @return The iO source this thread should use to parse messages.
 	 */
 	private Source getParser() {
 		
 		Source returnSource = null;
 		
 		// We operate in one of 3 modes
 		// 1. We have no splitter pattern and the ReaderSource advises us to share the Reader between threads
 		// In this case all threads must share the same ioSource
 		if(this.elementStartPattern == null && source.useMultipleThreadsPerReader()) {
 			returnSource = ioSource;
 			if(returnSource == null) {
 				synchronized(this) {
 					if(ioSource == null) {
 						BufferedReader reader = source.getReader();
 						if(reader != null) {
 							returnSource = getIoSource(reader);
 							ioSource = returnSource;							
 						}
 					}
 				}
 			}
 		}
 		
 		// 2. The ReaderSource advises us not to share the reader between threads
 		// In this case, each thread will have its own ioSource and we need to ask for a new Reader each time we create one
 		else if(!source.useMultipleThreadsPerReader()) {
 			returnSource = threadedIOSource.get();
 
 			boolean needNewReader = returnSource == null;
 			if(!needNewReader) {
 				try {
 					needNewReader = !returnSource.getReader().ready();
 				} catch (IOException ex) {
 					// Unhelpfully if the stream has been closed beneath our feet this is how we find out about it
 					// Even more unhelpfully, it appears as though the SAXParser does exactly that when it's finished parsing
 					needNewReader = true;
 				}
 			}
 			
 			if(needNewReader) {
 				BufferedReader reader = source.getNextReader();
 				if(reader != null) {
 					returnSource = getIoSource(reader);					
 					threadedIOSource.set(returnSource);
 				}
 			}
 
 			
 		}
 		// 3. We have a splitter pattern and the Reader source advises us to share the Reader between threads
 		// In this case each thread will have its own ioSource but we'll share a reader and keep using it until it runs out
 		else {
 			returnSource = threadedIOSource.get();
 			if(returnSource == null) {
 				BufferedReader reader = source.getReader();
 				if(reader != null) {
 					returnSource = getIoSource(null);					
 					threadedIOSource.set(returnSource);
 				}
 			}			
 		}
 		
 		return returnSource;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.springframework.batch.item.ItemReader#read()
 	 */
 	@Override
 	public ComplexDataObject read() throws UnexpectedInputException,
 			ParseException, NonTransientResourceException {
 		
 		ComplexDataObject result = null;
 		Source parser = null;
 		
 		// Keep trying to parse an entity until either we get one (result != null) or we run out of data to read (parser == null)
 		// BufferedReaderSources such as the ZipFileSource can return multiple BufferedReaders; when our current one is exhausted it
 		// will return another one
 		while(result == null && (parser = getParser()) != null) {
 			
 			if(elementStartPattern != null && source.useMultipleThreadsPerReader()) {
 				
 				// We're sharing a BufferedReader with other threads. Get our data out of it as quickly as we can to reduce
 				// the amount of time we spend blocking others
 				
 				BufferedReader reader = source.getReader();
 				if(reader == null) {
 					// There's nothing left to read
 					break;
 				}
 				
 				// Get the textual source for an element from the reader
 				String element = readElement(reader);
 				
 				// If we got something then parse it
 				if(element != null && element.trim().length() > 0) {
 					
 					StringReader stringReader = new StringReader(element);
 
 					parser.setReader(stringReader);
 				
 					try {
 						result = parser.readObject(elementType);
 					} catch(IOException ioEx) {
 						throw new ParseException("Failed to parse CDO from " + source.getName() + ". Message: " + element, ioEx);
 					}
 				} else {
 					// This parser has been exhausted
 					discardParser(parser);
 				}
 				
 			} else {
 				// We'll parse CDOs from the parser in serial
 				synchronized(parser) {
 					try {
 						result = parser.readObject(elementType);
 					} catch(IOException ioEx) {
 						
 						// If we're using the XML source, the underlying SAXParser can helpfully close the stream
 						// when it finished parsing the previous element, presumably because it assumes the document 
 						// is well-formed (ie only one per file)
 						if(parser instanceof XMLSource) {
 							// Find the root cause
 							Throwable ex = ioEx;
 							while(ex.getCause() != null) {
 								ex = ex.getCause();
 							}
 							if(ex instanceof IOException && ex.getMessage() == "Stream closed") {
 								// Sigh. That looks like that's what's happened.
 								result = null;
 							} else {
 								throw new ParseException("Failed to parse CDO from " + source.getName(), ioEx);
 							}
 						} else {
 							throw new ParseException("Failed to parse CDO from " + source.getName(), ioEx);
 						}
 					} finally {
 						if(result != null && (result.getTotalAttrCount() + result.getTotalElementCount() == 0)) {
 							// We didn't manage to read anything
 							result = null;
 						}
 						if(result == null) {
 							// We've exhausted this reader
 							// In the event of an exception being thrown there might still be data left in the reader 
 							// but as we have no way to skip to the next message, we have to abandon it
 							discardParser(parser);
 						}
 					}
 				}
 			}
 		}
 		
 		if(validate && result != null) {
 			try {
 				ValidationManager mgr = validator.get();
 				if(mgr == null) {
 					mgr = new ValidationManager();
 					validator.set(mgr);
 				}
 				mgr.validateByException(result);
 			} catch(ValidationException vEx) {
 				throw new C24ValidationException("Failed to validate message: " + vEx.getLocalizedMessage() + " [" + source.getName() + "]", result, vEx);
 			}
 		}
 		
 		return result;
 		
 	}
 	
 
 }
 

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
 import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
 import biz.c24.io.spring.core.C24Model;
 import biz.c24.io.spring.source.SourceFactory;
 import biz.c24.io.spring.source.TextualSourceFactory;
 
 /*
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
 	
 	/*
 	 * SourceFactory to use to generate our IO Sources
 	 */
 	private SourceFactory ioSourceFactory;
 	
 	/*
 	 * IO Source to use where we do not have an elementStartPattern
 	 */
 	private Source ioSource = null;
 	/*
 	 * Cache for IO sources where we have an elementStartPattern and can parallelise parsing
 	 */
 	private ThreadLocal<Source> threadedIOSource = new ThreadLocal<Source>();
 	
 	/*
 	 * The type of CDO that we will parse from the source
 	 */
 	private Element elementType;
 	
 	/*
 	 * An optional pattern to use to quickly split the readerSource so we can perform more heavyweight
 	 * parsing in parallel
 	 */
 	private Pattern elementStartPattern = null;
 	
 	/*
 	 * The source from which we'll read the data
 	 */
 	private BufferedReaderSource source;
 
 	private static String lineTerminator = System.getProperty("line.separator");
 
 	/*
 	 * Control whether or not we validate the parsed CDOs
 	 */
 	private boolean validate = false;
	private ValidationManager validator = new ValidationManager();
 	
 	public C24ItemReader() {
 		// Default to a textual source factory
 		TextualSourceFactory factory = new TextualSourceFactory();
 		factory.setEndOfDataRequired(false);
 		ioSourceFactory = factory;
 	}
 	
 	@PostConstruct
 	public void validateConfiguration() {
 		Assert.notNull(elementType, "Element type must be set, either explicitly or by setting the model");
 		Assert.notNull(source, "Source must be set");
 	}
 	
 	/*
 	 * Returns the element type that we will attempt to parse from the source
 	 */
 	public Element getElementType() {
 		return elementType;
 	}
 
 	/*
 	 * Set the type of element that we will attempt to parse from the source
 	 * 
 	 * @param elementType The type of element that we want to parse from the source
 	 */
 	public void setElementType(Element elementType) {
 		this.elementType = elementType;
 	}
 	
 	/*
 	 * Allows setting of element type via the supplied model
 	 * 
 	 * @param model The model of the type we wish to parse
 	 */
 	public void setModel(C24Model model) {
 		elementType = model.getRootElement();
 	}
 	
 	/*
 	 * Returns the regular expression that we're using to split up in the incoming data.
 	 * Null if not set.
 	 */
 	public String getElementStartPattern() {
 		return elementStartPattern != null? elementStartPattern.pattern() : null;
 	}
 
 	/*
 	 * Sets the regular expression used to quickly split up the source into individual entities for parsing
 	 * 
 	 * @param elementStartRegEx The regular expression to identify the start of a new entity in the source
 	 */
 	public void setElementStartPattern(String elementStartRegEx) {
 		this.elementStartPattern = Pattern.compile(elementStartRegEx);
 	}
 	
 	/*
 	 * Set whether or not you want validation to be performed on the parsed CDOs. 
 	 * An exception will be thrown for any entity which fails validation.
 	 * 
 	 * @param validate Whether or not to validate parsed CDOs
 	 */
 	public void setValidate(boolean validate) {
 		this.validate = validate;
 	}
 	
 	/*
 	 * Query whether or not this ItemReader will validate parsed CDOs
 	 * 
 	 * @return True iff this ItemReader will automtically validate read CDOs
 	 */
 	public boolean isValidating() {
 		return validate;
 	}
 	
 	/*
 	 * Gets the BufferedReaderSource from which CDOs are being parsed
 	 * 
 	 * @return This reader's BufferedReaderSource
 	 */
 	public BufferedReaderSource getSource() {
 		return source;
 	}
 
 	/*
 	 * Sets the source that this reader will read from
 	 * 
 	 * @param source The BufferedReaderSource to read data from
 	 */
 	public void setSource(BufferedReaderSource source) {
 		this.source = source;
 	}
 	
 	/*
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
 	
 	/*
 	 * Initialise our context
 	 * 
 	 * @param stepExecution The step execution context
 	 */
 	@BeforeStep
 	public void setup(StepExecution stepExecution) {		
 		source.initialise(stepExecution);
 	}
 	
 	/*
 	 * Clean up and resources we're consuming
 	 */
 	@AfterStep
 	public void cleanup() {
 		source.close();
 	}
 	
 	/*
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
 	
 	/*
 	 * Extracts the textual data for an element from the BufferedReader using the elementStartPattern to split
 	 * up the data
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
 					String line = reader.readLine();
 					
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
 							elementCache.append(lineTerminator);
 							inElement = true;
 						}
 					} else if(inElement) {
 						// More data for our current element
 						elementCache.append(line);
 						elementCache.append(lineTerminator);
 					}
 				}
 			} catch(IOException ioEx) {
 				throw new NonTransientResourceException("Failed to extract entity", ioEx);
 			}
 		}
 		
 		return elementCache.toString();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.springframework.batch.item.ItemReader#read()
 	 */
 	@Override
 	public ComplexDataObject read() throws UnexpectedInputException,
 			ParseException, NonTransientResourceException {
 		
 		ComplexDataObject result = null;
 		BufferedReader reader = null;
 		
 		// Keep trying to parse an entity until either we get one (result != null) or we run out of data to read (reader == null)
 		// BufferedReaderSources such as the ZipFileSource can return multiple BufferedReaders; when our current one is exhausted it
 		// will return another one
 		while(((ioSource != null) || (reader = source.getReader()) != null) && result == null) {
 			
 			if(elementStartPattern != null) {
 				// Get the textual source from an element
 				String element = readElement(reader);
 				
 				// If we got something then parse it
 				if(element != null && element.trim().length() > 0) {
 					
 					StringReader stringReader = new StringReader(element);
 					
 					Source parser = threadedIOSource.get();
 					if(parser == null) {
 						parser = ioSourceFactory.getSource(stringReader);
 						threadedIOSource.set(parser);
 					} else {
 						parser.setReader(stringReader);
 					}
 				
 					try {
 						result = parser.readObject(elementType);
 					} catch(IOException ioEx) {
 						throw new UnexpectedInputException("Failed to parse CDO from source: " + element, ioEx);
 					}
 				}
 				
 			} else {
 				// As we don't have an elementSplitPattern to use, we'll have to parse CDOs from the BufferedReader in serial
 				synchronized(this) {
 					if(ioSource == null) {
 						ioSource = ioSourceFactory.getSource(reader);
 					}
 					
 					try {
 						result = ioSource.readObject(elementType);
 					} catch(IOException ioEx) {
 						throw new UnexpectedInputException("Failed to parse CDO", ioEx);
 					}
 					
 					if(result != null && (result.getTotalAttrCount() + result.getTotalElementCount() == 0)) {
 						// We didn't manage to read anything
 						result = null;
 					}
 					if(result == null) {
 						// We've exhausted this reader
 						ioSource = null;
 					}
 				}
 			}
 		}
 		
 		if(validate && result != null) {
 			try {
				validator.validateByException(result);
 			} catch(ValidationException vEx) {
 				throw new ParseException("Failed to validate CDO", vEx);
 			}
 		}
 		
 		return result;
 	}
 	
 
 }
 

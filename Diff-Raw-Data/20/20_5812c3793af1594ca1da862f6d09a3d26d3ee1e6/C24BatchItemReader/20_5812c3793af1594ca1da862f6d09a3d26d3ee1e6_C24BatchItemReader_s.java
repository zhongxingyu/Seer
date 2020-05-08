 package biz.c24.io.spring.batch.reader;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.springframework.batch.core.StepExecution;
 import org.springframework.batch.core.annotation.AfterStep;
 import org.springframework.batch.core.annotation.BeforeStep;
 import org.springframework.batch.item.ItemReader;
 import org.springframework.batch.item.NonTransientResourceException;
 import org.springframework.batch.item.ParseException;
 import org.springframework.batch.item.UnexpectedInputException;
 
 import biz.c24.io.api.ParserException;
 import biz.c24.io.api.data.ComplexDataObject;
 import biz.c24.io.api.data.ComplexDataType;
 import biz.c24.io.api.data.Element;
 import biz.c24.io.api.data.ValidationException;
 import biz.c24.io.api.data.ValidationManager;
 import biz.c24.io.api.presentation.ParseListener;
 import biz.c24.io.api.presentation.Source;
 import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
 import biz.c24.io.spring.core.C24Model;
 
 public class C24BatchItemReader implements ItemReader<ComplexDataObject> {
 	
 	private Element element;
 	/**
 	 * The source from which we'll read the data
 	 */
 	private BufferedReaderSource source;
 
 	private boolean validate = false;
 	
 	private volatile Thread parsingThread = null;
 	/**
 	 * Store this separately to make sure we report a job abort once only
 	 */
 	private volatile Throwable abortJobException = null;
 	private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(128);
 	
 	private ThreadLocal<ValidationManager> validator = new ThreadLocal<ValidationManager>();
 	
 	
 	public void setModel(C24Model model) {
 		element = model.getRootElement();
		((ComplexDataType) element.getType()).setProcessAsBatch(true);
 	}
 
 	/**
 	 * Initialise our context
 	 * 
 	 * @param stepExecution The step execution context
 	 */
 	@BeforeStep
 	public void setup(StepExecution stepExecution) {		
 		source.initialise(stepExecution);
 		startParsing();
 	}
 	
 	private void queueObject(ComplexDataObject obj) throws TimeoutException, InterruptedException {
 
 		if(!queue.offer(obj, 10, TimeUnit.SECONDS)) {
 			// TODO: Come up with a better way to propagating this up. The problem is we can't throw a checked type from the ParseListener callback
 			TimeoutException ex = new TimeoutException("Timed out waiting for parsed elements to be processed. Aborting.");
 			throw ex;
 		}
 	}
 	
 	private void queueObject(ParserException obj) throws TimeoutException, InterruptedException {
 
 		if(!queue.offer(obj, 10, TimeUnit.SECONDS)) {
 			// TODO: Come up with a better way to propagating this up. The problem is we can't throw a checked type from the ParseListener callback
 			TimeoutException ex = new TimeoutException("Timed out waiting for parsed elements to be processed. Aborting.");
 			throw ex;
 		}
 	}
 	
 	private void setParsingComplete() {
 		parsingThread = null;
 	}
 	
 	/**
 	 * Clean up and resources we're consuming
 	 */
 	@AfterStep
 	public void cleanup() {
 		source.close();
 	}
 	
 	private void startParsing() {
 		parsingThread = new Thread(new IoParser());
 		parsingThread.start();
 	}
 	
 	private Element getElement() {
 		return element;
 	}
 	
 	public BufferedReaderSource getSource() {
 		return source;
 	}
 
 	public void setSource(BufferedReaderSource source) {
 		this.source = source;
 	}
 	
 	private boolean stillParsing() {
 		return parsingThread != null;
 	}
 	
 	@Override
 	public ComplexDataObject read() throws Exception, UnexpectedInputException,
 			ParseException, NonTransientResourceException {
 		
 		ComplexDataObject cdo = null;
 		
 		while(cdo == null && (!queue.isEmpty() || stillParsing())) {
 			try {
 				Object obj = queue.poll(1, TimeUnit.SECONDS);
 				if(obj != null) {
 					if(obj instanceof ParserException) {
 						throw new ParseException("Failed to parse file", (Throwable)obj);
 					} else if(obj instanceof ComplexDataObject) {
 						cdo = (ComplexDataObject)obj;
 					} else {
 						throw new ParseException("Unexpected type of object parsed: " + obj.getClass().getName());
 					}
 				}
 			} catch(InterruptedException ioEx) {
 				throw new ParseException("Interrupted while parsing", ioEx);
 			}
 		}
 		
 		if(cdo == null && abortJobException != null) {
 			synchronized(this) {
 				if(abortJobException != null) {
 					Throwable ex = abortJobException;
 					abortJobException = null;
 					ParseException rethrow = ex instanceof ParseException? (ParseException)ex : new ParseException("Failure during parsing", ex);
 					throw rethrow;
 				}
 			}
 		} else if(cdo != null && validate) {
 			try {
 				ValidationManager mgr = validator.get();
 				if(mgr == null) {
 					mgr = new ValidationManager();
 					validator.set(mgr);
 				}
 				mgr.validateByException(cdo);
 			} catch(ValidationException vEx) {
 				throw new C24ValidationException("Failed to validate message: " + vEx.getLocalizedMessage() + " [" + source.getName() + "]", cdo, vEx);
 			}
 		}
 		
 		return cdo;
 	}
 	
 	public boolean isValidate() {
 		return validate;
 	}
 
 	public void setValidate(boolean validate) {
 		this.validate = validate;
 	}
 
 	private class IoParser implements ParseListener, Runnable {
 		
 		public void run() {
 			try {
 				Source iOSource = getElement().getModel().source();
 				iOSource.setParseListener(this);
 				
 				BufferedReader reader = null;
 				
 				while(abortJobException == null) {
 					
 					try {
 						reader = source.getReader();
 						if(reader != null && !reader.ready()) {
 							continue;
 						}
 					} catch (IOException ex) {
 						// Unhelpfully if the stream has been closed beneath our feet this is how we find out about it
 						// Even more unhelpfully, it appears as though the SAXParser does exactly that when it's finished parsing
 						break;
 					}
 					if(reader == null) {
 						break;
 					}
 					
 					iOSource.setReader(reader);
 					iOSource.readObject(getElement());
 				}
 				
 			} catch(Throwable ex) {
 				abortJobException = ex;
 			} finally {
 				setParsingComplete();
 			}
 		}
 
 		@Override
 		public void onStartBatch(Element element) throws ParserException {
 			// Add callback logic here
 			
 		}
 
 		@Override
 		public void onEndBatch(Element element) throws ParserException {
 			// Add callback logic here
 			
 		}
 
 		@Override
 		public Object onBatchEntryParsed(Object object) throws ParserException {
 			try {
 				if(object instanceof ComplexDataObject) {
 					queueObject((ComplexDataObject)object);
 					return null;
 				} else {
 					return object;
 				}
 			} catch(Exception ex) {
 				throw new ParserException(ex, ((ComplexDataObject)object).getName());
 			}
 		}
 
 		@Override
 		public void onBatchEntryFailed(Object object, ParserException failure)
 				throws ParserException {
 			try {
 				queueObject(failure);
 				// We can't read anything further from this reader
 				source.discard(source.getReader());
			} catch(Exception ex) {
 				throw new ParserException(ex, ((ComplexDataObject)object).getName());
			}
 			
 		}
 
 		@Override
 		public String generateFailedName(String original) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public boolean isAdditionalBatch(Element element) {
 			return false;
 		}
 		
 	}	
 }

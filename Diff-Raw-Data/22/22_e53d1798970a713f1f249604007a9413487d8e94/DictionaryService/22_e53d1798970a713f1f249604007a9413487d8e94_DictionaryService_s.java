 package cz.havlena.dictionary;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 public class DictionaryService {
 	
 	private static int 					UNCOMPARABLE_CHARS = 0x12345678;
 	private static final String 		DEFAULT_ENCODING = "UTF8";
 	private static final String 		ELEMENT_START = "<entry key=\"";
 	private static final String 		ELEMENT_END = "</entry>";
 	
 	public static final int 			FORMAT_HTML = 1;
 	public static final int 			FORMAT_ELEMENT = 2;
 	
 	public static final int 			STATE_DONE = 1;
 	public static final int 			STATE_SEARCHING = 2;
 	public static final int 			STATE_SEARCHING_COMPLETED = 4;
 	public static final int 			STATE_STOP = 3;
 
 	public static final int 			PHASE_FINDING_SOME_EXPRESSION = 1;
 	public static final int 			PHASE_GOING_FORWARD_FOR_FIRST = 2;
 	public static final int 			PHASE_APPENDING_TEXT_INSIDE = 3;
 	public static final int 			PHASE_SKIPPING_TEXT_OUTSIDE = 4;
 	
 	
 	private File 						mFile;
 	private FileInputStream 			mStream;
 	private DictionaryXmlParser			mXmlParser;
 	private long 						mActualPosition;
 	private IDictionaryService			mListener;
 	private Thread						mThread;
 	private int							mState = STATE_DONE;
 	
 	public void init(File file) throws FileNotFoundException {
 		if(!file.exists()) {
 			throw new FileNotFoundException("File: " + file.getName() + " isn't exist");
 		}
 		mFile = file;
 	}
 	
 	public void setListener(IDictionaryService listener) {
 		mListener = listener;
 	}
 	
 	private void initStream() throws FileNotFoundException {
 		mStream = new FileInputStream(mFile);
 	}
 	
 	private String readLine() throws IOException {
 		byte[] buff = new byte[50];
 		String data = "";
 		for(;;)
 		{
 			int count = 0;
 			while(count < buff.length)
 				count += mStream.read(buff, count, buff.length - count);
 			
 			data += new String(buff, DEFAULT_ENCODING);
 			int line = data.indexOf("\n");
 			if(line != -1) {
 				moveTo(mActualPosition + line + 1);
 				return data.substring(0, line + 1);
 			}
 		}
 	}
 	
 	private void moveTo(long position) throws IOException {
 		FileChannel ch = mStream.getChannel();
 		mActualPosition = position;
 		ch.position(position);
 	}
 	
 	/**
 	 * Compare current key and searched expression.
 	 * @param expr
 	 * @param key
 	 * @return
 	 */
 	private int compareExprKey(String expr, String key) {
 	    for(int i = 0; i < expr.length(); i++) {
 	        if(i >= key.length()) {
 	            return 1;       // expression is bigger
 	        }
 	        char ech = Character.toUpperCase(expr.charAt(i));
 	        char kch = Character.toUpperCase(key.charAt(i));
 	        if(ech == kch) {
 	            continue;
 	        }
 	        if(!Character.isLetterOrDigit(kch) || kch > 128) {
 	            return UNCOMPARABLE_CHARS;
 	        }
 	        return ech - kch;
 	    }
 	    return 0;
 	}
 
 	public void searchAsync(final String key, final int maxResults, final int format) {
 		stopSearching();
 		
 		mThread = new Thread() {
 			public void run() {
 				try {
 					search(key, maxResults, format);
 				} catch (IOException e) {
 					if(mListener != null) mListener.onError(e);
 				}
 			};
 		};
 		mThread.start();
 	}
 	
 	private static final int MAX_WAIT = 3000;
 	public void stopSearching() {
 		if(mState == STATE_DONE) {
 			return;
 		}
 		
 		mState = STATE_STOP;
 		while(mState != STATE_DONE) {
 			try {
 				mThread.join(MAX_WAIT);
 			} catch (InterruptedException e) {
 				if(mListener != null) 
 					mListener.onError(e);
 			}
 		}
 	}
 
 	private void handleFoundElement(String element, int format) throws IOException {
 		if(mXmlParser == null) {
 			mXmlParser = new DictionaryXmlParser();
 			mXmlParser.init();
 		}
 		
 		Object el = null;
 		if(format == DictionaryService.FORMAT_HTML) {
 			try {
 				el = mXmlParser.parseToHtml(element);
 			} catch (ParserConfigurationException e) {
 				if(mListener != null) mListener.onError(e);
 			} catch (SAXException e) {
 				if(mListener != null) mListener.onError(e);
 			} catch (IOException e) {
 				if(mListener != null) mListener.onError(e);
 			}
 		}else if(format == DictionaryService.FORMAT_ELEMENT) {
 			try {
 				el = mXmlParser.parseToElement(element);
 			} catch (ParserConfigurationException e) {
 				if(mListener != null) mListener.onError(e);
 			} catch (SAXException e) {
 				if(mListener != null) mListener.onError(e);
 			} catch (IOException e) {
 				if(mListener != null) mListener.onError(e);
 			}
 		} else {
 			throw new IOException("Unsupported encoding format");
 		}
 		
 		if(mListener != null) {
         	mListener.onFoundElement(el);
         }
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @param maxResults
 	 * @return
 	 * @throws IOException
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public void search(String key, int maxResults, int format) throws IOException {
 		int phase = 0, left = 0, right = (int) mFile.length() - 4096;
 		initStream();
 	    moveTo((left + right) / 2);
 		mState = STATE_SEARCHING;
 		if(mListener != null) {
 			mListener.onSearchStart();
 		}
 		
 	    String exprString = ELEMENT_START + key;   
 	    String element = "";
 	    int numResults = 0;
 	    while(mState == STATE_SEARCHING)
 	    {
 	    	String line = readLine();
 	        if(line == null) {
 	        	throw new IOException("I am on the end of the file");
 	        }
 	        else if(line != null && line.length() == 0) {
 	            continue;   // empty line
 	        }
 
 	        if(phase == 2) {
 	            int entryEnd = line.indexOf(ELEMENT_END);
 	            if(entryEnd == -1) {
 	                element += line;
 	                continue;
 	            }
 	            
 	            element += line.substring(entryEnd);
 	            handleFoundElement(element, format);
 	            element = "";
 	            numResults++;
 	            if(numResults >= maxResults) {
 	            	mState = STATE_SEARCHING_COMPLETED;
 	            }
 	            phase = 3;
 	            continue;
 	        }
 	        
 	        String expr = findExpression(line);
 	        if(expr == null) {
 	        	continue;
 	        }
 	        int cmp = compareExprKey(key, expr);
 	        if(cmp == UNCOMPARABLE_CHARS) {
 	            continue;        // skip uncomparable words
 	        }
 	        
 	        if(phase == 0) {
 	            boolean changed = true;
 	            if(cmp > 0) {  // expression is bigger then key
 	                left = (int) mActualPosition;
 	            }
 	            else {           // expression is smaller or matches
 	                changed = (right != (int) mActualPosition);    // comparing twice same word
 	                right = (int) mActualPosition;
 	            }
 	            
 	            if(changed && (right - left > 4096)) {
 	                moveTo((left + right) / 2);
 	                continue;
 	            }
 	            
 	            phase = 1;
 	            moveTo(left);
 	            continue;
 	        }
 	        
 	        if(phase == 1) {
 	            if(cmp > 0) {
 	                continue;           // first match still not found
 	            }
 	            else if(cmp < 0) {
 	                break;              // all matching words passed
 	            }
 	            phase = 2;
 	        }
 	        
 	        if(phase == 2 || phase == 3) { // we are not accepting case sensitive
 	        	String l = line.toLowerCase();
 	        	String exp = exprString.toLowerCase();
 	            int entryStart = l.indexOf(exp);
 	            if(entryStart == -1) {
 	                mState = STATE_SEARCHING_COMPLETED;      // first non matching entry was hit
 	            }
 	            
 	            element += line.substring(line.indexOf(ELEMENT_START));
 	            phase = 2;
 	        }
 	    }
 	    
 	    mStream.close();
 	    mState = STATE_DONE;
	    if(mListener != null) {
	    	mListener.onSearchStop();
	    }
 	}
 	
 	private String findExpression(String line) {
 		int keyStart = line.indexOf(ELEMENT_START);
 		if(keyStart == -1)
         {
             return null;
         }
         keyStart += ELEMENT_START.length();
         int keyEnd = line.indexOf('"', keyStart);
         return line.substring(keyStart, keyEnd);
 	}
 	
 }

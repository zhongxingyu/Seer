 /**
  * 
  */
 package cc.warlock.network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StormFrontStream extends InputStream {
 	
 	private StormFrontConnection connection;
 	private boolean inHeader = true;
 	private boolean started = false;
 	private int notDone = 0;
 	private String headerString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><document>";
 	private String footerString = "</document>"; 
 	private InputStream inputStream;
 	private static Replacer[] invalidPatterns = {
 			new Replacer(Pattern.compile("&(?= )"), "&amp;"),
 			new Replacer(Pattern.compile("\034GSw.*$"), ""),
 			new Replacer(Pattern.compile("\034"), "\\\\\\^"),
 			new Replacer(Pattern.compile("<(?=[^\\w/])"), "&lt;"),
 			//new Replacer(Pattern.compile("(?<=^[^<]+)>"), "&gt;"),
			new Replacer(Pattern.compile("&(?![a-zA-z]+;)"), "&amp;"),
 			new Replacer(Pattern.compile("\07"), ""),
 			new Replacer(Pattern.compile("(?<!/)>(\\n|\\r\\n)"), ">"),
 			new Replacer(Pattern.compile("\\w+=\"[^\"]*(\")[^=]*\"(\\s+\\w+=|\\s*/>)"), "&quot;", 1),
 			};
 	private CharSequence buffer = null;
 	
 	public StormFrontStream (StormFrontConnection connection, InputStream stream)
 	{
 		this.connection = connection;
 		this.inputStream = stream;
 	}
 	
 	private static class Replacer {
 		private Pattern pattern;
 		private String text;
 		private int group;
 		
 		public Replacer(Pattern pattern, String text) {
 			this(pattern, text, 0);
 		}
 		
 		public Replacer(Pattern pattern, String text, int group) {
 			this.pattern = pattern;
 			this.text = text;
 			this.group = group;
 		}
 		
 		public CharSequence replace(CharSequence input) {
 			CharSequence result = null;
 			Matcher matcher = pattern.matcher(input);
 			if(group == 0) {
 				StringBuffer buffer = null;
 				
 				while(matcher.find()) {
 					if(buffer == null)
 						buffer = new StringBuffer();
 					matcher.appendReplacement(buffer, text);
 				}
 				if(buffer != null) {
 					matcher.appendTail(buffer);
 				}
 				result = buffer;
 			} else {
 				result = input;
 				while(matcher.find()) {
 					result = result.subSequence(0, matcher.start(group)) + text
 					+ result.subSequence(matcher.end(group), result.length());
 					System.out.println("Result: " + result);
 					matcher = pattern.matcher(result);
 				}
 				// if we didn't change the result, then set it back to null
 				if(result == input) result = null;
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * This function doesn't actually work in all situations
 	 * if it starts getting called for some reason, make it more
 	 * robust
 	 * @return next value in the string
 	 */
 	public int read() throws IOException {
 		int rv;
 		
 		if(inHeader) {
 			if(buffer != null) {
 				buffer = buffer + headerString;
 			} else {
 				buffer = headerString;
 			}
 			inHeader = false;
 		}
 		
 		if(buffer != null) {
 			rv = buffer.charAt(0);
 			buffer = buffer.subSequence(1, buffer.length());
 		} else {
 			rv = inputStream.read();
 		}
 		System.out.write(rv);
 		return rv;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#available()
 	 */
 	public int available() throws IOException {
 		if(buffer != null) {
 			return buffer.length() + inputStream.available();
 		} else {
 			return inputStream.available();
 		}
 	}
 	/* (non-Javadoc)
 	 * @see java.io.Closeable#close()
 	 */
 	public void close() throws IOException {
 		inputStream.close();
 	}
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#mark(int)
 	 */
 	public synchronized void mark(int readlimit) {
 		inputStream.mark(readlimit);
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#markSupported()
 	 */
 	public boolean markSupported() {
 		return inputStream.markSupported();
 	}
 	
 	/**
 	 * @see java.io.InputStream#read(byte[], int, int)
 	 * shim the stream into well-formed XML
 	 */
 	public int read(byte[] b, int off, int len) throws IOException {
 		// if we're in the header
 		if(inHeader) {
 			if(buffer != null) {
 				buffer = buffer + headerString;
 			} else {
 				buffer = headerString;
 			}
 			inHeader = false;
 		}
 		
 		// do the reading
 		int bytesRead;
 		int toRead;
 		if(buffer != null) {
 			toRead = Math.max(len - buffer.length(), 256);
 		} else {
 			toRead = len;
 		}
 		
 		if(notDone >= 0) {
 			bytesRead = inputStream.read(b, off, toRead);
 		
 //			System.out.println("Original bytesRead: " + bytesRead + ", toRead: " + toRead + ", len: " + len);
 			
 			// do some debug outputting
 			if (bytesRead != -1)
 				connection.dataReady(new String(b, off, bytesRead));
 			
 //				System.out.write(b, off, bytesRead);
 		} else {
 			if(buffer != null) {
 				bytesRead = 0;
 			} else {
 				bytesRead = notDone;
 			}
 		}
 		
 		// if no bytes were read, use the buffer if we have one
 		if(bytesRead < 0) {
 			if(notDone >= 0) {
 				if(buffer == null) {
 					buffer = footerString;
 				} else {
 					buffer = buffer + footerString;
 				}
 			}
 			if(buffer == null) {
 				return bytesRead;
 			}
 			notDone = bytesRead;
 			bytesRead = 0;
 		}
 		
 		// setup for replacing
 		CharSequence result;
 		if(buffer != null)
 			result = buffer + new String(b, off, bytesRead);
 		else
 			result = new String(b, off, bytesRead);
 
 		// Make sure the last character is a newline.
 		int newlinePos;
 		while((newlinePos = result.toString().lastIndexOf('\n')) < 0 && newlinePos >= toRead) {
 //			System.out.println("More input");
 			byte[] tmp = new byte[2048];
 			int numBytes = inputStream.read(tmp, 0, 2048);
 			result = result + new String(tmp, 0, numBytes);
 			System.out.write(tmp, 0, numBytes);
 			bytesRead += numBytes;
 		}
 		CharSequence afterNewline = null;
 		if(newlinePos + 1 < result.length())
 			afterNewline = result.subSequence(newlinePos + 1, result.length());
 		result = result.subSequence(0, newlinePos + 1);
 		
 		// do bad behavior replacements
 		boolean didReplacement = false;
 		for(Replacer replacer : invalidPatterns) {
 			CharSequence tmp = replacer.replace(result);
 			if(tmp != null) {
 				result = tmp;
 				didReplacement = true;
 				if(!started) {
 					/*
 					 * TODO change the replacement class to allow
 					 * executing some action, and move this there
 					 */
 					connection.send("GOOD\n");
 					started = true;
 				}
 			}
 		}
 		
 		// put the result back into the string
 		if(buffer != null || didReplacement) {
 			if(result.length() > len) {
 				buffer = result.subSequence(len, result.length());
 				if(afterNewline != null)
 					buffer = buffer.toString() + afterNewline;
 				result = result.subSequence(0, len);
 			} else {
 				buffer = afterNewline;
 			}
 			byte[] bytes = result.toString().getBytes();
 			bytesRead = bytes.length;
 			System.arraycopy(bytes, 0, b, off, bytesRead);
 		}
 		
 //		System.out.println("Final bytesRead: " + bytesRead);
 		return bytesRead;
 	}
 	
 	public synchronized void reset() throws IOException {
 		inputStream.reset();
 	}
 	
 	public long skip(long n) throws IOException {
 		if(inHeader) {
 			return super.skip(n);
 		} else {
 			return inputStream.skip(n);
 		}
 	}
 }

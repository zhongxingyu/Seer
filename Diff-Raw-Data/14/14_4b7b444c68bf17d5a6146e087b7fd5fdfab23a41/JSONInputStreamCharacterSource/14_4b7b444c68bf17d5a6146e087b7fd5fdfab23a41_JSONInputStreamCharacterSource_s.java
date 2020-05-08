 package com.sis.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 import org.svenson.tokenize.JSONCharacterSource;
 
 /**
  * to be used with {@link org.svenson.JSONParser} to avoid full buffering of stream
  * 
  * JSONParser.defaultJSONParser().parse(new JSONInputStreamCharacterSource(inputStream));
  * 
  * @author CR
  *
  */
 public class JSONInputStreamCharacterSource implements JSONCharacterSource {
 	private static final Logger logger = Logger.getLogger(JSONInputStreamCharacterSource.class);
 	private final InputStream inputStream;
 	
 	private int index = 0;
 	
 	private int maxLength = 0;
 	
 	public JSONInputStreamCharacterSource(InputStream is) {
 		inputStream = is;
 	}
 	
 	public JSONInputStreamCharacterSource(InputStream is, int maxLen) {
 		this(is);
 		setMaxLength(maxLen);
 	}
 	
 	public void setMaxLength(int maxLen) {
 		maxLength = maxLen;
 	}
 	
 	@Override
 	public void destroy() {
 		try {
 			inputStream.close();
 		} catch (IOException e) {
 			logger.error("could not close stream!", e);
 		}
 	}
 
 	@Override
 	public int getIndex() {
 		return index;
 	}
 
 	@Override
 	public int nextChar() {
 		if (maxLength > 0 && getIndex() > maxLength) {
 			return 0;
 		}
 		
 		try {
			if (inputStream.available() > 0) {
				return inputStream.read();
			}
 			
 			index++;
 		} catch (IOException e) {
 			logger.error("could not read stream!", e);
 		}
 		
 		return 0;
 	}
 
 }

 package io.bufferreaders;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.Reader;
 
 import removecomments.RemoveComments;
 
 public class UncommenterBufferedReader extends BufferedReader {
 
 	public UncommenterBufferedReader(Reader in) {
 		super(in);
 	}
 
 	@Override
 	public String readLine() throws IOException {
 		
 		String currentLine = super.readLine();

 		if (currentLine!=null) {
 		
 			StringBuffer sb = new StringBuffer(currentLine);
 		
			String result = (new RemoveComments()).uncomment(new ByteArrayInputStream(sb.toString().getBytes()));
 		
 			return result.trim();
 			
 		} else {
 			
 			return null;
 		}
 		
 	}
 	
 	
 	
 	
 
 }

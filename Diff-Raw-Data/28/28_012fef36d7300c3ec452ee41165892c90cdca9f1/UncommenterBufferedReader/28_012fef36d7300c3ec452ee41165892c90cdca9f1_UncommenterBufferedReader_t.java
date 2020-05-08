 package io.bufferreaders;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.Reader;
 
 import removecomments.RemoveComments;
 
 public class UncommenterBufferedReader extends BufferedReader {
 
	private RemoveComments remover; 
	
 	public UncommenterBufferedReader(Reader in) {
		
 		super(in);
		
		remover = new RemoveComments();
		
 	}
 
 	@Override
 	public String readLine() throws IOException {
 		
		String currentLine = null; 
				
		while ((currentLine = this.getNextLine()) != null && currentLine.equals(""));
				
		return currentLine;
			
	}
	
	
	public String getNextLine() throws IOException {
		
 		String currentLine = super.readLine();
		
 		if (currentLine!=null) {
 		
 			StringBuffer sb = new StringBuffer(currentLine);
 		
			String result = remover.uncomment(new ByteArrayInputStream(sb.toString().getBytes()));
 		
 			return result.trim();
 			
 		} else {
 			
 			return null;
			
 		}
 		
 	}
 	
 	
 	
 	
 
 }

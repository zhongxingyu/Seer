package mybencode;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class StringScanner implements IScanner{
 	
 	private IScanner parent ;
 	InputStream is ;
 	private long value ;
 	private boolean isData ;
 	private int length ;
 	
 	public StringScanner(IScanner parent, InputStream is) {
 		this.parent = parent ;
 		this.is  = is ;
 		this.isData = false;
 		this.length = 0 ;
 		
 	}
 	
 	public void scan() throws IOException {
 		int i ;
 		while( ((i = this.is.read()) >= 0) && !this.isData ) {
 			getNumeric(i);
 		}
 		
 		byte[] bytes = new byte[this.length];
 		is.read(bytes);
 		
 		if(parent != null)
 			parent.ping(this);
 	}
 	
 	private void getNumeric(int i) throws IOException {
 		int digit ;
 		byte b = (byte) i ;
 		
 		switch(b) {
 			case ':' :
 				if(this.length == 0 ) {
 					throw new IOException("string length is unspecified") ;
 				}
 				
 				this.isData = true ;
 				break ;
 			case '0' :
 			case '1' :
 			case '2' :
 			case '3' :
 			case '4' :
 			case '5' :
 			case '6' :
 			case '7' :
 			case '8' :
 			case '9' :
 				digit = b - '0';
 				this.length = (this.length * 10) + digit;
 				break ;
 		}
 		
 	}
 
 	public long getValue() {
 		return this.value;
 	}
 
 	@Override
 	public void ping(IScanner child) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }

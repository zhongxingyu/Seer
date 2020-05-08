package com.yuktix.bencode;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class Scanner implements IScanner{
 	
 	
 	public Scanner() {
 		
 	}
 	
 	public void scan(InputStream is) throws IOException {
 		IntegerScanner scanner = new IntegerScanner(this,is);
 		scanner.scan();
 		
 	}
 
 	@Override
 	public void ping(IScanner child) {
 		System.out.println("value => " + child.getValue());
 		System.out.println("back to main scanner");
 		
 	}
 
 	@Override
 	public long getValue() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }

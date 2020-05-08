 package com.mcode.mamoi.mcc;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 
 public class mcc {
     public static void main( String[] args ) throws Exception {
     	// modes
     	boolean padzeros = false;
     	int radix = 10;
     	
     	String sourceFile = args[0];
     	String binaryFile = args[1];
 		OutputStream fos = new FileOutputStream(binaryFile);
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
 		
 		String line = br.readLine();
 		int bytesWritten = 0;
 		while ( line != null ) {
 			line = line.replaceAll(";.*", ""); // remove comments from code
 			if(!line.trim().isEmpty()) {
 				String[] elements = line.split( " " );
 				for(int i = 0; i < elements.length; i++) {
 					String element = elements[i];
					if(element.trim().isEmpty()) {
						continue;
					}
 					// Check for modes
 					if(element.equals("padzeros")) {
 						padzeros = true;
 						continue;
 					} else if ( element.equals("hex") ) {
 						radix = 16;
 						continue;
 					} else if ( element.equals("decimal")) {
 						radix = 10;
 						continue;
 					} else if ( element.equals("binary")) {
 						radix = 2;
 						continue;
 					}
 					
 					if(padzeros) { // pad zeros until byte number specified
 						while( bytesWritten < Integer.parseInt(element, radix) ) {
 							fos.write(0);
 							bytesWritten ++;
 						}
 						padzeros = false;
 					} else { // normal mode
 						fos.write(Integer.parseInt(element, radix));
 						bytesWritten ++;
 					}
 				}
 			}
 
 			line = br.readLine();
 		}
 		fos.close();
 		br.close();
 		System.out.println("Bytes written: " + bytesWritten);
     }
 }

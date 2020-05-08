 package com.migrenus.dummies;
 
 //import static java.lang.System.in;
 //import static java.lang.System.out;
 
 import java.io.IOException;
 
 
@SuppressWarnings("unused")
 public class KeyChecker {
 	
 	public boolean checkKey(String keyToCheck) throws IOException {
 		
 		boolean keyReal = true;
 		
 		FileHandler input = new FileHandler();
 		input.fileInit("keygen.txt");
 		
 		for (int i = 0; i < input.getLineAmmount(); i++) {
 			
 			if (input.getFileContents(i).equals(keyToCheck)) {
 				keyReal = true;
 				break;
 			} else { 
 				keyReal = false;
				break;
 			}
 			
 		} 
 		
 		return keyReal;
 	}
 	
 }

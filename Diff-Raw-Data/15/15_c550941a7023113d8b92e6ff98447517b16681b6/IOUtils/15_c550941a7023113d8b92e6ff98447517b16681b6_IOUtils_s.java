 package com.kontagent;
 
 import java.io.InputStream;
 import java.util.Scanner;
 
public final class InputUtils {
 	
 	public static String getInput(String fileName){		
		InputStream fileInput = InputUtils.class.getResourceAsStream(fileName);
 		return new Scanner(fileInput).useDelimiter("\\A").next();
 	}
 	
 	public static void sendOutput(String csvString){
 		System.out.println(csvString);
 	}
 }

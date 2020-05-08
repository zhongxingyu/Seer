package com.acm.problemhelper;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.io.File;
 import java.io.FileNotFoundException;
 
 public class InputHelper {
 	private static final String INPUT_FOLDER = "../../InputOutput/Input/";
 
 	public static String[] getInputOptions() {
 		File inputDirectory = new File(INPUT_FOLDER);
 		String[] inputOptions;
 		if(false == inputDirectory.exists()) {
 			System.out.println("Input directory not found. Please consult documentation to set up an input folder and inputs.");
 			inputOptions = new String[0];
 		} else {
 			File[] inputFiles = inputDirectory.listFiles();
 			inputOptions = new String[inputFiles.length];
 			for(int i = 0; i < inputFiles.length; i++) {
 				inputOptions[i] = inputFiles[i].getName();
 			}
 		}
 
 		return inputOptions;
 	}
 
 	public static Scanner getInput(String[] args) {
 		String inputFile = "";
 		if(0 != args.length) {
 			inputFile = args[0];
 		}
 		String[] inputOptions = InputHelper.getInputOptions();
 		return new Scanner(System.in);
 	}
 }

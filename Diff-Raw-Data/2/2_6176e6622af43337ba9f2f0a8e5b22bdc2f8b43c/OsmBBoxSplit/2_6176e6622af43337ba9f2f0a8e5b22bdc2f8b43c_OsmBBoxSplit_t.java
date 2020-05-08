 /*
  * Copyright (C) 2012 The Serval Project
  *
  * This file is part of the Serval Maps OSM Bounding Box Split Software
  *
  * Serval Maps OSM PBF Metadata Reader Software is free software; you can 
  * redistribute it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software Foundation; either 
  * version 3 of the License, or (at your option) any later version.
  *
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.servalproject.maps.osmbboxsplit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.servalproject.maps.osmbboxsplit.utils.FileUtils;
 import org.servalproject.maps.osmbboxsplit.utils.StringUtils;
 
 /**
  * main class for the application
  */
 public class OsmBBoxSplit {
 	
 	/**
 	 * name of the app
 	 */
 	public static final String APP_NAME    = "Serval Maps OSM Bounding Box Split";
 
 	/**
 	 * version of the app
 	 */
 	public static final String APP_VERSION = "1.0";
 
 	/**
 	 * url for more information about the app
 	 */
 	public static final String MORE_INFO   = "http://developer.servalproject.org/dokuwiki/doku.php?id=content:servalmaps:main_page";
 
 	/**
 	 * url for the license info
 	 */
 	public static final String LICENSE_INFO = "http://www.gnu.org/copyleft/gpl.html";
 	
 	/**
 	 * minimum file size, in MB, of files to split
 	 */
 	public static final int MIN_FILE_SIZE = 100;
 	
 	/**
 	 * a list of files that should be ignored
 	 */
 	public static ArrayList<String> ignoreList;
 	
 	/**
 	 * main method of the main class of the application
 	 * 
 	 * @param args an array of command line arguments
 	 */
 	public static void main(String[] args) {
 		
 		// parse the command line options
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = null;
 		try {
 			cmd = parser.parse(createOptions(), args);
 		}catch(org.apache.commons.cli.ParseException e) {
 			// something bad happened so output help message
 			printCliHelp("Error in parsing arguments:\n" + e.getMessage());
 		}
 		
 		/*
 		 * get and test the command line arguments
 		 */
 
 		// input path
 		String inputPath = cmd.getOptionValue("input");
 
 		if(StringUtils.isEmpty(inputPath)) {
 			printCliHelp("Error: the path to the input file / directory is required");
 		}
 
 		if(FileUtils.isFileAccessible(inputPath) == false && FileUtils.isDirectoryAccessible(inputPath) == false) {
 			printCliHelp("Error: the input file / directory is not accessible");
 		}
 
 		File inputFile = new File(inputPath);
 		
 		// minimum file size
 		String inputSize = cmd.getOptionValue("minsize");
 		int minFileSize = MIN_FILE_SIZE;
 		
 		if(StringUtils.isEmpty(inputSize) == false) {
 			try {
 				minFileSize = Integer.parseInt(inputSize);
 			} catch(NumberFormatException e) {
 				printCliHelp("Error: the minimum file size must be a valid integer");
 			}
 		}
 		
 		// arbitrary file size in attempt to stop invalid data and 
 		// unnecessarily processing small files
 		if(minFileSize < 10) {
 			printCliHelp("Error: the minimum file size must be greater than 10");
 		}
 		
 		// output path && template file
 		
 		File outputDir = null;
 		String scriptContents = null;
 		
 		String outputPath = cmd.getOptionValue("output");
 		String templatePath = cmd.getOptionValue("template");
 		
 		if(StringUtils.isEmpty(outputPath) == false) {
 			if(FileUtils.isDirectoryAccessible(outputPath) == false) {
 				printCliHelp("Error: the output directory is not accessible");
 			}
 			
 			if(StringUtils.isEmpty(templatePath)) {
 				printCliHelp("Error: the template must be specified when the output pararameter used");
 			}
 			
 			if(FileUtils.isFileAccessible(templatePath) == false) {
 				printCliHelp("Error: the osmosis script template file is not accesible");
 			}
 			
 			outputDir = new File(outputPath);
 			
 			// read the contents of the template file
 			try {
 				scriptContents = org.apache.commons.io.FileUtils.readFileToString(new File(templatePath));
 			} catch (IOException e1) {
 				System.err.println("ERROR: unable to read the template file");
 				System.exit(-1);
 			}
 		}
 		
 		// ignore list path
 		ignoreList = new ArrayList<String>();
 		
 		String ignorePath = cmd.getOptionValue("ignore");
 		
 		if(StringUtils.isEmpty(ignorePath) == false) {
 			
 			if(FileUtils.isFileAccessible(ignorePath) == false) {
 				printCliHelp("Error: the ignore list file is not accessible");
 			}
 			
 			// read the contents of the ignore list file
 			try {
 				ignoreList = (ArrayList<String>) org.apache.commons.io.FileUtils.readLines(new File(ignorePath));
 				
 				// strip out any comment lines
 				for (int i = 0; i < ignoreList.size(); i++) {
 					if(ignoreList.get(i).startsWith("#")) {
 						ignoreList.remove(i);
 						i--;
 					}
 				}
 			} catch (IOException e) {
 				System.err.println("ERROR: unable to read the ignore list file");
 				System.exit(-1);
 			}
 			
 		}
 		
 		/*
 		 * output some text
 		 */
 		System.out.println(APP_NAME);
 		System.out.println("Version: " + APP_VERSION);
 		System.out.println("More info: " + MORE_INFO);
 		System.out.println("License info: " + LICENSE_INFO + "\n");
 		
 		// inform user of resources we'll be working on
 		try {
 			if(inputFile.isDirectory()) {
 				System.out.println("Processing OSM PBF files in directory: " + inputFile.getCanonicalPath());
 				
 				// test the files in the directory
 				BBoxSplit.readFilesInDir(inputFile, outputDir, scriptContents, minFileSize);
 				
 			} else {
 				
 				// test the file
 				BBoxSplit.readFile(inputFile, outputDir, scriptContents, minFileSize);
 			}
 		} catch (IOException e) {
 			System.err.println("Unable to access file system resources.\n" + e.toString());
 			System.exit(-1);
 		}
 	}
 	
 	/*
 	 * output the command line options help
 	 */
 	private static void printCliHelp(String message) {
 		System.out.println(message);
 		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar OsmBBoxSplit.jar", createOptions());
 		System.exit(-1);
 	}
 	
 	/*
 	 * create the command line options used by the app
 	 */
 	private static Options createOptions() {
 
 		Options options = new Options();
 		
 		OptionBuilder.withArgName("path");
 		OptionBuilder.hasArg(true);
 		OptionBuilder.withDescription("path to the input file / directory");
 		OptionBuilder.isRequired(true);
 		options.addOption(OptionBuilder.create("input"));
 		
 		OptionBuilder.withArgName("int");
 		OptionBuilder.hasArg(true);
 		OptionBuilder.withDescription("minimum size of file in MB to split (Default: " + MIN_FILE_SIZE + " MB)");
 		OptionBuilder.isRequired(false);
 		options.addOption(OptionBuilder.create("minsize"));
 		
 		OptionBuilder.withArgName("path");
 		OptionBuilder.hasArg(true);
 		OptionBuilder.withDescription("path to the ignore list file");
 		OptionBuilder.isRequired(false);
 		options.addOption(OptionBuilder.create("ignore"));
 		
 		OptionBuilder.withArgName("path");
 		OptionBuilder.hasArg(true);
 		OptionBuilder.withDescription("path to the output directory");
 		OptionBuilder.isRequired(false);
 		options.addOption(OptionBuilder.create("output"));
 		
 		OptionBuilder.withArgName("path");
 		OptionBuilder.hasArg(true);
 		OptionBuilder.withDescription("path to the osmosis script template");
 		OptionBuilder.isRequired(false);
 		options.addOption(OptionBuilder.create("template"));
 		
 		return options;
 		
 	}
 
 }

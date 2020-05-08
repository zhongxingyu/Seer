 /**
  *  Copyright 2012 Zhijian Zeng, All Rights Reserved.
  *  Email: me@kanezeng.com
  *	This file is created at 2012-2-15
  */
 package com.kanezeng.Translation.FileProcessing;
 
 import java.util.ArrayList;
 import java.io.*;
 import java.util.regex.*;
 
 import com.kanezeng.Translation.EngObjects.ParsingRule;
 import com.kanezeng.Translation.EngObjects.ExcludeRule;
 import com.kanezeng.Translation.EngObjects.TUnit;
 import com.kanezeng.Translation.Utils.FileIO;
 
 /**
  * This class will extract translatable strings from the given file following the specified parsing rules.
  */
 public class ExtractTranslatableStrings {
 	
 	public static ArrayList<TUnit> doExtract(String fileNameAndPath, String fileEncoding, ArrayList<ParsingRule> allParsingRule) {
 		ArrayList<TUnit> result = new ArrayList<TUnit>();
 		ArrayList<ExcludeRule> emptyExclude = new ArrayList<ExcludeRule>();
 		result = doExtract(fileNameAndPath, fileEncoding, emptyExclude, allParsingRule);		
 		return result;
 	}
 
 
 	/**
  	* This class will exclude untranslatable/unlocalizable strings first.
  	* Then it will extract translatable strings from the given file following the specified parsing rules.
  	*/
 	public static ArrayList<TUnit> doExtract(String fileNameAndPath, String fileEncoding, ArrayList<ExcludeRule> allExcludeRule, ArrayList<ParsingRule> allParsingRule) {
 		ArrayList<TUnit> result = new ArrayList<TUnit>();
 		//Check if file is existed, return empty result if the file is not existed.
 		File inputfile = new File(fileNameAndPath);
 		if(!inputfile.exists()) return result;
 		
 		try {
 			String filecontent = FileIO.readFileToString(fileNameAndPath, fileEncoding);
 
 			// Remove strings matched by Exclude rules.
 			for (ExcludeRule currentExclude:allExcludeRule) {
				filecontent = replaceString(filecontent, "(?m)"+currentExclude.rule, "");
 			}
 
 			for (ParsingRule currentRule:allParsingRule) {
 				// Note from Kane: You need (?m) so Java will consider the line feeds in the string. Otherwise, it will consider it as a single line string.
 				Pattern tempPattern = Pattern.compile("(?m)"+currentRule.rule);
 				Matcher tempMatcher = tempPattern.matcher(filecontent);
 				while(tempMatcher.find()) {
 					result.add(new TUnit(fileNameAndPath,tempMatcher.group(currentRule.indexOfID),tempMatcher.group(currentRule.indexOfString)));
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 	private static String replaceString(String src, String regEx, String rep) {
 		Pattern pat = Pattern.compile(regEx);
 		Matcher matcher = pat.matcher(src);
		String result = src;

		while(matcher.find()) {
			result = result.replace(matcher.group(0),"");
 		}

		return result;
 	}
 }

 /**
  *  Copyright 2012 Zhijian Zeng, All Rights Reserved.
  *  Email: me@kanezeng.com
  *	This file is created at Mar 13, 2012
  */
 package com.kanezeng.Translation.FileProcessing;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.kanezeng.Translation.EngObjects.TUnit;
 import com.kanezeng.Translation.Languages.LanguageObject;
 import com.kanezeng.Translation.Languages.Languages;
 
 /**
  * 
  */
 public class XLIFFFile {
 
 	private LanguageObject sourceLanguage = Languages.defaultLanguage();
 	private FileOutputStream output;
 
 	/**
 	 * @param translationUnits
 	 *            an ArrayList contains all translatable units.
 	 * @param inputFile
 	 *            The file name with full path for the input resource file
 	 * @param outputFile
 	 *            The file name with full path for the output XLIFF file
 	 *            skeletonFile removed temperately The file name with full path
 	 *            for the skeleton file. The skeleton file is a template that
 	 *            can be used in recreating the original file, from the <source>
 	 *            content, or the translated file, from the <target> content.
 	 */
	public void create(ArrayList<TUnit> translationUnits, String outputFile,
			String sourceLanguage) {
 
 		Collections.sort(translationUnits);
 
 		try {
 			output = new FileOutputStream(outputFile);
 			String currentFile = "";
 			this.sourceLanguage = Languages.getLanguage(sourceLanguage);
 			// segId = 0;
 			writeXLIFFHead();
 			for (TUnit tempUnit : translationUnits) {
 				if (!tempUnit.originalFile.equalsIgnoreCase(currentFile)) {
 					if (!currentFile.isEmpty()) {
 						endFile();
 					}
 					currentFile = tempUnit.originalFile;
 					startFile(currentFile);
 				}
 				writeString("\t\t\t<trans-unit id='" + cleanString(tempUnit.ID)
 						+ "'>\n");
 				writeString("\t\t\t\t<source>"
 						+ cleanString(preTranslate(tempUnit.originalString))
 						+ "</source>\n");
 				writeString("\t\t\t</trans-unit>\n");
 			}
 			writeXLIFFTrail();
 			output.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void writeXLIFFTrail() throws IOException {
 		endFile();
 		writeString("</xliff>");
 	}
 
 	private void endFile() throws UnsupportedEncodingException, IOException {
 		writeString("\t\t</body>\n");
 		writeString("\t</file>\n");
 	}
 
 	private void writeXLIFFHead() throws UnsupportedEncodingException,
 			IOException {
 
 		writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		writeString("<xliff version='1.2' xmlns='urn:oasis:names:tc:xliff:document:1.2'>\n");
 	}
 
 	private void startFile(String filename)
 			throws UnsupportedEncodingException, IOException {
 		writeString("\t<file original=\"" + filename + "\" source-language=\""
 				+ sourceLanguage.languageCountryCode + "\">\n");
 		writeString("\t\t<body>\n");
 	}
 
 	private void writeString(String string)
 			throws UnsupportedEncodingException, IOException {
 		output.write(string.getBytes("UTF-8"));
 	}
 
 	private String preTranslate(String segment) {
 		//
 		// Search for translations in a TM database and
 		// return <trans-unit> elements if matches exist
 		//
 		return segment;
 	}
 
 	private String cleanString(String s) {
 		int control = s.indexOf("&");
 		while (control != -1) {
 			s = s.substring(0, control) + "&amp;" + s.substring(control + 1);
 			if (control < s.length()) {
 				control++;
 			}
 			control = s.indexOf("&", control);
 		}
 
 		control = s.indexOf("<");
 		while (control != -1) {
 			s = s.substring(0, control) + "&lt;" + s.substring(control + 1);
 			if (control < s.length()) {
 				control++;
 			}
 			control = s.indexOf("<", control);
 		}
 
 		control = s.indexOf(">");
 		while (control != -1) {
 			s = s.substring(0, control) + "&gt;" + s.substring(control + 1);
 			if (control < s.length()) {
 				control++;
 			}
 			control = s.indexOf(">", control);
 		}
 		return s;
 	}
 
 }

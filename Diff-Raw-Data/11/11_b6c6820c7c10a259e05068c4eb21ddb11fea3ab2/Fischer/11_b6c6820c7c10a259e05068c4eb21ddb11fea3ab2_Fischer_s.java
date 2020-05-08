 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.toms_cz.tconvertor.business;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.toms_cz.tconvertor.dao.RowData;
 
 import cz.toms_cz.com.tconvertor.util.TConvertorConstants;
 import cz.toms_cz.com.tconvertor.util.readers.PDFReader;
 
 /**
  *
  * @author Tom
  */
 public class Fischer extends Template {
 
	
 	/**
 	 * Constructor for this class it set default reader which will be used to read file and
 	 * file type to read.
 	 */
     public Fischer() {
     	reader = new PDFReader();
         setFileDescription(TConvertorConstants.PDF_TYPE_DESCRIPTOR);
         setFileType(TConvertorConstants.PDF_TYPE_LABEL);
     }
 
     @Override
     public ArrayList<RowData> parsedData(String stringToParse) {
         String parsed = null;
         int startPosition = 0;
         int stopPosition = 0;
         int numberOfPages = 0;
         Pattern pattern = Pattern.compile("FAKTURA");
         Matcher matcher = pattern.matcher(stringToParse);
         while (matcher.find()) {
             numberOfPages++;
         }
 
 
         if (numberOfPages > 1) {
             String pages[];
             pages = pagesParsed(stringToParse, numberOfPages);
             parsed = pages[0];
             for (int j = 1; j < numberOfPages; j++) {
                 pattern = Pattern.compile("\r\n");
                 matcher = pattern.matcher(pages[j]);
                 if (matcher.find()) {
                     startPosition = matcher.end();
                     pages[j] = pages[j].substring(startPosition);
                 }
                 parsed = parsed.concat(pages[j]);
             }
 
         } else {
             pattern = Pattern.compile("Měna");
             matcher = pattern.matcher(stringToParse);
             if (matcher.find()) {
                 startPosition = matcher.end();
                 stringToParse = stringToParse.substring(startPosition);
             }
             pattern = Pattern.compile("Diskont");
             matcher = pattern.matcher(stringToParse);
             if (matcher.find()) {
                 stopPosition = matcher.start();
                 parsed = stringToParse.substring(0, stopPosition);
             }
         }
        parsed = parsed.replaceAll("železobeton", "");
         parsed = parsed.replaceAll("abrazivo", "");
         parsed = parsed.replaceAll("keramika celoob.", "");
         parsed = parsed.replaceAll("turbo", "");
         parsed = parsed.replaceAll(",", ".");
         String[] parsedArray = parsed.split("\\r?\\n");
         int numberOfGoodRow = 0;
         for (int j = 0; j < parsedArray.length; j++) {
             if (!(parsedArray[j].equals(""))) {
                 numberOfGoodRow++;
             }
         }
         String[] helpArray=new String[numberOfGoodRow];
         int helpArrayCounter=0;
         for (int j = 0; j < parsedArray.length; j++) {
             if (!(parsedArray[j].equals(""))) {
                 helpArray[helpArrayCounter]=parsedArray[j];
                 helpArrayCounter++;
             }
         }
         
         parsedArray=helpArray;
         ArrayList<RowData> records = new ArrayList<>();
         for (int j = 0; j < parsedArray.length; j = j + 3) {
             boolean aditionalPrice;
             boolean aditionalPieces;
             String[] firstLine = parsedArray[j].split(" ");
             int secondLinePosition = j + 2;
             String[] secondLine = parsedArray[secondLinePosition].split(" ");
             RowData nowLine = new RowData();
             nowLine.setItemCode(firstLine[0]);
             try {
                 int testPieces = Integer.parseInt(secondLine[0]);
                 aditionalPieces = true;
             } catch (NumberFormatException e) {
                 aditionalPieces = false;
             }
             String adPieces = null;
             if (aditionalPieces == true) {
                 adPieces = secondLine[0];
                 adPieces = adPieces.concat(secondLine[1]);
             } else {
                 adPieces = secondLine[0];
             }
             adPieces = transformToOneDot(adPieces);
             nowLine.setNumberOfItems(Double.parseDouble(adPieces));
             nowLine.setTaxRate(Double.parseDouble(secondLine[secondLine.length - 2]));
             try {
                 Integer.parseInt(secondLine[secondLine.length - 4]);
                 aditionalPrice = true;
             } catch (NumberFormatException e) {
                 aditionalPrice = false;
             }
             String price = null;
             if (aditionalPrice == true) {
                 price = secondLine[secondLine.length - 4];
                 price = price.concat(secondLine[secondLine.length - 3]);
             } else {
                 price = secondLine[secondLine.length - 3];
             }
             price = transformToOneDot(price);
             nowLine.setPriceWithoutTaxes(Double.parseDouble(price));
             records.add(nowLine);
         }
         return records;
 
     }
     
     private String transformToOneDot(String input){
     	char [] tempPrice = input.toCharArray();
         StringBuilder st = new StringBuilder();
         boolean founded = false;
         for(int i=0;i<tempPrice.length;i++){
         	if((tempPrice[i]=='.' && !founded)){
         		founded = true;
         	}
         	else{
         		st.append(tempPrice[i]);
         	}
 
         }
     	String tempPriceStr = st.toString();
     	if(tempPriceStr.contains(".")){
     		input = tempPriceStr;
     	}
     	return input;
     }
 
     private String[] pagesParsed(String document, int numberOfPages) {
         Pattern pattern;
         Matcher matcher;
         String[] pages = new String[numberOfPages];
         for (int j = 0; j < numberOfPages - 1; j++) {
             String part = null;
             pattern = Pattern.compile("Měna");
             matcher = pattern.matcher(document);
             if (matcher.find()) {
                 int startPosition = matcher.end();
                 document = document.substring(startPosition);
             }
             pattern = Pattern.compile("Vystavil/a");
             matcher = pattern.matcher(document);
             if (matcher.find()) {
                 int startPosition = matcher.start();
                 part = document.substring(0, startPosition);
                 document = document.substring(startPosition);
             }
             pages[j] = part;
         }
         pattern = Pattern.compile("Měna");
         matcher = pattern.matcher(document);
         if (matcher.find()) {
             int startPosition = matcher.end();
             document = document.substring(startPosition);
         }
         pattern = Pattern.compile("Diskont");
         matcher = pattern.matcher(document);
         if (matcher.find()) {
             int stopPosition = matcher.start();
             document = document.substring(0, stopPosition);
             pages[pages.length - 1] = document;
         } else {
             String prevPage = pages[pages.length - 2];
             matcher = pattern.matcher(prevPage);
             if (matcher.find()) {
                 int stopPosition = matcher.start();
                 prevPage = prevPage.substring(0, stopPosition);
                 pages[pages.length - 2] = prevPage;
             }
 
             pages[pages.length - 1] = "";
         }
         return pages;
     }
 
     @Override
     public ArrayList<RowData> parsedData(File fileToParse) {
         return null;
     }
 }

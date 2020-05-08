 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * <p>
  * Utilits methods for loaders
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class LoaderUtils {
     /**
      * Convert dBm values to milliwatts
      * 
      * @param dbm
      * @return milliwatts
      */
     public static final double dbm2mw(int dbm) {
         return Math.pow(10.0, ((dbm) / 10.0));
     }
 
     /**
      * Convert milliwatss values to dBm
      * 
      * @param milliwatts
      * @return dBm
      */
     public static final float mw2dbm(double mw) {
         return (float)(10.0 * Math.log10(mw));
     }
 
     /**
      * Calculates list of files
      * 
      * @param directoryName directory to import
      * @param filter - filter (if filter teturn true for directory this directory will be handled
      *        also )
      * @return list of files to import
      */
     public static List<File> getAllFiles(String directoryName, FileFilter filter) {
         File directory = new File(directoryName);
         return getAllFiles(directory, filter);
     }
 
     /**
      * Calculates list of files
      * 
      * @param directory - directory to import
      * @param filter - filter (if filter teturn true for directory this directory will be handled
      *        also )
      * @return list of files to import
      */
     public static List<File> getAllFiles(File directory, FileFilter filter) {
         LinkedList<File> result = new LinkedList<File>();
         for (File childFile : directory.listFiles(filter)) {
             if (childFile.isDirectory()) {
                 result.addAll(getAllFiles(childFile, filter));
             } else {
                 result.add(childFile);
             }
         }
         return result;
     }
 
     /**
      * get file extension
      * 
      * @param fileName - file name
      * @return file extension
      */
     public static String getFileExtension(String fileName) {
         return getFileExtension(fileName,true);
     }
     /**
      * get file extension
      * 
      * @param fileName - file name
      * @return file extension
      */
     public static String getFileExtension(String fileName,boolean returnDot) {
         int idx = fileName.lastIndexOf(".");
         if (idx<0){
             return "";
         }
         if (returnDot){
            return fileName.substring(idx);
         }
         idx++;
         return idx<fileName.length()?fileName.substring(idx):"";
     }
     public static File getFirstFile(String dirName) {
         File file = new File(dirName);
         if (file.isFile()) {
             return file;
         }
         File[] list = file.listFiles();
         if (list.length > 0) {
             return list[0];
         } else {
             // TODO optimize
             List<File> all = getAllFiles(dirName, new FileFilter() {
 
                 @Override
                 public boolean accept(File pathname) {
                     return true;
                 }
             });
             if (all.isEmpty()) {
                 return null;
             } else {
                 return all.iterator().next();
             }
         }
     }
 
     /**
      * Define delimeters.
      * 
      * @param file the file
      * @param minSize the min size
      * @param possibleFieldSepRegexes the possible field sep regexes
      * @return the string
      */
     public static String defineDelimeters(File file, int minSize, String[] possibleFieldSepRegexes) {
         String fieldSepRegex = "\t";
         BufferedReader read = null;
         String line;
         try {
             read = new BufferedReader(new FileReader(file));
             while ((line = read.readLine()) != null) {
                 int maxMatch = 0;
                 for (String regex : possibleFieldSepRegexes) {
                     String[] fields = line.split(regex);
                     if (fields.length > maxMatch) {
                         maxMatch = fields.length;
                         fieldSepRegex = regex;
                     }
                 }
                 if (maxMatch >= minSize) {
                     break;
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 read.close();
             } catch (IOException e) {
                 e.printStackTrace();
             };
         }
         return fieldSepRegex;
     }
 
     /**
      * Gets the CSV row.
      * 
      * @param file the file
      * @param minSize the min size
      * @param rowNum the row number
      * @param delimeter the delimeter
      * @return the cSV row
      */
     public static String[] getCSVRow(File file, int minSize, int rowNum, char delimeter) {
         CSVReader reader = null;
         try {
             CountingFileInputStream is = new CountingFileInputStream(file);
             reader = new CSVReader(new InputStreamReader(is), delimeter);
             String[] nextLine;
             long line = 0;
             while ((nextLine = reader.readNext()) != null) {
                 if (nextLine.length < minSize) {
                     continue;
                 }
                 line++;
                 if (line >= rowNum) {
                     return nextLine;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
 
     /**
      * Find header id.
      *
      * @param header the header
      * @param possibleHeaders the possible headers
      * @param firstElem the first elem
      * @return the int
      */
     public static int findHeaderId(String[] header, String[] possibleHeaders,int firstElem) {
         if (possibleHeaders == null||possibleHeaders.length==0) {
             return -1;
         }
         for (int i=firstElem;i<header.length;i++) {
             for (String headerRegExp : possibleHeaders) {
                 Pattern pat=Pattern.compile(headerRegExp,Pattern.CASE_INSENSITIVE);
                 Matcher match = pat.matcher(header[i]);
                 if (match.matches()) {
                     return i;
                 }
             }
         }
         return -1;
     }
 }

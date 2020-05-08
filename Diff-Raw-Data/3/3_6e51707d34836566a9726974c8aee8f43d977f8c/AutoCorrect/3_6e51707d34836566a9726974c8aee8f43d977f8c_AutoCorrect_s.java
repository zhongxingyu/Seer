 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.brown.cs32.atian.crassus.backend.AutoComplete;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Class to wrap UseTrie and adapt it to map project
  */
 public class AutoCorrect {
 
     //String _dictFilePath;
     UseTrie _useTrie;
     int _edit;
     /**
      * constructor
      */
     public AutoCorrect() {
         _edit = 2;
         
         boolean prefix = true;
         boolean led = true;
         boolean whitespace = false;
         boolean smart = true;    
         _useTrie = new UseTrie(prefix, led, whitespace, smart, false);
     }
 
     /**
      * Read way names in index file to list of string
      * @param indexFilePath index file
      * @return all way names in a list
      */
     private List<String> readStreetName(String indexFilePath, String key) {
         List<String>  allStreetNames = new ArrayList<String>();
         try {
             BufferedReader wordReader = new BufferedReader(new FileReader(indexFilePath));
             //BufferedWriter wordWriter = new BufferedWriter(new FileWriter(_dictFilePath));
             String input;
             String header = wordReader.readLine();
 
             // get header strings
             String[] headerStrs = header.split("\t");
             //String key = "Name";
             int keyIndex = -1;
             for (int i = 0; i < headerStrs.length; i++) {
                 if (key.equals(headerStrs[i])) {
                     keyIndex = i;
                     break;
                 }
             }
             if (keyIndex == -1) {
                 System.err.println("ERROR: Cannot find key " + key + " in file " + indexFilePath);
                 System.exit(1);
             }
 
             while ((input = wordReader.readLine()) != null) {
                 input = input.trim();
                 String[] words = input.split("\t");
                 if (words.length >= keyIndex + 1) {
                     String streetName = words[keyIndex];
                     allStreetNames.add(streetName);
                     //wordWriter.write(streetName + "\n");
                 }
             }
 
         } catch (FileNotFoundException e) {
 
             System.out.println("ERROR: Need to enter a valid input file");
             System.exit(0);
         } catch (IOException e) {
             System.out.println("ERROR: IO exception");
             System.exit(1);
         }
         
         return allStreetNames;
     }
 
     public void initializer(String indexFilePath, String key) {
         List<String>  allStreetNames = readStreetName(indexFilePath, key);      
         _useTrie.initializerFromString(_edit, allStreetNames);
     }
 
     /**
      * main method, return the suggestion for autocomplete for a given string
      * @param userSearch input string
      * @return suggestions of autocompletion, seperated by ','
      */
     public List<String> Searcher(String userSearch) {
         List<String> result = new ArrayList<String>();
         List<String> search = _useTrie.Searcher(userSearch, _edit);
         
         for(String s : search) {
             result.add(s.toUpperCase());
         }
         return result;
     }
 }

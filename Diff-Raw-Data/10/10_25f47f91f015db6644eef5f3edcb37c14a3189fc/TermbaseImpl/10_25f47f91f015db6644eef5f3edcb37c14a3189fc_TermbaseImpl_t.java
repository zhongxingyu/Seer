 package eu.monnetproject.nlp.stl.impl;
 
 import eu.monnetproject.nlp.stl.Termbase;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.zip.GZIPInputStream;
 
 /**
  * A termbase implementation to lookup terms
  *
  * @author Tobias Wunner
  */
 public class TermbaseImpl extends HashSet<String> implements Termbase {
 
     final String language;
 
     public TermbaseImpl(String language) {
         this.language = language;
     }
 
     @Override
     public boolean add(String term) {
         super.add(term);
         return true;
     }
 
     @Override
     public boolean lookup(String term) {
         return super.contains(term);
     }
 
     @Override
     public String getLanguage() {
         return language;
     }
 
     @Override
     public int size() {
         return super.size();
     }
 
     public static void main(String[] args) {
         TermbaseImpl termbaseImpl = new TermbaseImpl("en");
         termbaseImpl.add("John");
         termbaseImpl.add("Peter");
         termbaseImpl.add("Mary");
         TermbaseImpl termbase = termbaseImpl;
         System.out.println(termbaseImpl.size());
         for (String term : termbaseImpl) {
             System.out.println(term + " -> " + termbaseImpl.lookup(term));
         }
         System.out.println(termbaseImpl);
     }
     
     public static TermbaseImpl fromFile(File file, String language) throws IOException {
         final Scanner scanner;
         if(file.getName().endsWith(".gz")) {
             scanner = new Scanner(new GZIPInputStream(new FileInputStream(file)));
         } else {
             scanner = new Scanner(file);
         }
         final TermbaseImpl termbaseImpl = new TermbaseImpl(language);
         while(scanner.hasNextLine()) {
             final String term =  scanner.nextLine();
             if(!term.matches("\\s*")) {
                termbaseImpl.add(term.trim());
             }
         }
         return termbaseImpl;
     }
 }

 package net.kurochenko.ispub.upload;
 
 import au.com.bytecode.opencsv.CSVReader;
 import net.kurochenko.ispub.author.form.Author;
 import net.kurochenko.ispub.department.form.Department;
 import net.kurochenko.ispub.source.form.Source;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * User: kurochenko
  * Date: 7/9/11
  * Time: 8:12 PM
  */
 public class AuthorParserCSV {
 
     public static Collection<Author> parse(InputStream fileIS) throws IOException {
         if (fileIS == null) {
             throw new IllegalArgumentException("Input Stream is null");
         }
 
         List<Author> authors = new ArrayList<Author>();
 
 
         CSVReader reader = new CSVReader(new InputStreamReader(fileIS, "UTF-8"), ',', '"');
 
         String [] line;
         Author author = null;
         String department = null;
         List<String> sourceNames = null;
 
         while ((line = reader.readNext()) != null) {
 
             /* Line doesn't contain required values */
             if (line.length < AuthorCSVColumnNumbers.SOURCES.getColumnNumber()) {
                 continue;
             }
 
             author = new Author();
             author.setName(getValue(line, AuthorCSVColumnNumbers.NAME.getColumnNumber()));
             author.setSurname(getValue(line, AuthorCSVColumnNumbers.SURNAME.getColumnNumber()));
 
             department = getValue(line, AuthorCSVColumnNumbers.DEPARTMENT.getColumnNumber());
             if (department != null) {
                 author.setDepartment(new Department(department));
             }
 
             sourceNames = getValues(line, AuthorCSVColumnNumbers.SOURCES.getColumnNumbers());
 
             if (!sourceNames.isEmpty()) {
                Set<Source> sources = new HashSet<Source>();
 
                 for (int i = 0; i < sourceNames.size(); i++) {
                    sources.add(new Source(sourceNames.get(i)));
                 }
                author.setSources(sources);
             }
 
             author.setNote(getValue(line, AuthorCSVColumnNumbers.NOTE.getColumnNumber()));
 
             authors.add(author);
         }
 
         return authors;
     }
 
     private static List<String> getValues(String [] line, int [] colNumber) {
 
         List<String> result = new ArrayList<String>();
 
         for (int number : colNumber) {
             if (getValue(line, number) == null) {
                 continue;
             }
             result.add(getValue(line, number));
         }
 
         return result;
     }
 
     private static String getValue(String [] line, int number) {
         if (number >= line.length) {
             return null;
         }
 
         return (line[number].trim().isEmpty()) ? null : line[number].trim();
     }
 }

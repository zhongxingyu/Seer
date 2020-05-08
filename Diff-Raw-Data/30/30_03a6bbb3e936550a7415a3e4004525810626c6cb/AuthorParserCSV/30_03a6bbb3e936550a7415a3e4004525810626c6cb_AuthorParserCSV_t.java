 package net.kurochenko.ispub.upload;
 
 import au.com.bytecode.opencsv.CSVReader;
 import net.kurochenko.ispub.author.form.Author;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
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
 
         while ((line = reader.readNext()) != null) {
             author = new Author();
             author.setName(line[AuthorCSVColumnNumbers.NAME.getColumnNumber()]);
             author.setSurname(line[AuthorCSVColumnNumbers.SURNAME.getColumnNumber()]);
 
             authors.add(author);
         }
 
         return authors;
     }
 }

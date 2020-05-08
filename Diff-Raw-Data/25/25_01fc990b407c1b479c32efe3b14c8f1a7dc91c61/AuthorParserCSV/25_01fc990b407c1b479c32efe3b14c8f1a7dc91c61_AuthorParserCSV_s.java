 package net.kurochenko.ispub.upload;
 
 import au.com.bytecode.opencsv.CSVReader;
 import net.kurochenko.ispub.author.form.Author;
 
import java.io.FileReader;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: kurochenko
  * Date: 7/9/11
  * Time: 8:12 PM
  */
 public class AuthorParserCSV implements AuthorParser {
 
     @Override
    public Collection<Author> parse(String file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is null");
         }
 
         List<Author> authors = new ArrayList<Author>();
 
        CSVReader reader = new CSVReader(new FileReader(file), ',', '"');
 
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

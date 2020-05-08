 package net.kurochenko.ispub.upload;
 
 import net.kurochenko.ispub.author.form.Author;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Collection;
 
 /**
  * User: kurochenko
  * Date: 7/9/11
  * Time: 8:09 PM
  */
 public interface AuthorParser {
 
    public Collection<Author> parse(InputStream file) throws IOException;
 
 }

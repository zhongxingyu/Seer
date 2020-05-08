 package ohtu.refero.bibtex;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import ohtu.refero.models.Article;
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 
 public class FieldsTest {
 
     @Test
     public void getAllDeclaredFieldsOfArticle() {
         
         List<String> expected = new ArrayList<String>();
         
         expected.add("id");
        expected.add("author");
         expected.add("title");
         expected.add("releaseYear");
         expected.add("publisher");
         expected.add("address");
         expected.add("referenceID");
         expected.add("journal");
         expected.add("volume");
         expected.add("number");
         expected.add("pages");
         
         List<String> actual = new ArrayList<String>();
         
         for (Field field : Fields.getAllDeclaredFields(Article.class)) {
             actual.add(field.getName());
         }
         
         Collections.sort(expected);
         Collections.sort(actual);
         
         assertEquals(expected, actual);
     }
 }

 package com.thoughtworks.mvc.parameter.transformers;
 
 import bean.BookCategory;
 import org.junit.Test;
 
 import java.util.HashMap;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class EnumTransformerTest {
     @Test
     public void testTransform() throws Exception {
         HashMap<String, String[]> map = new HashMap<>();
         map.put("category", new String[]{"Computer"});
        EnumTransformer transformer = new EnumTransformer();
 
         BookCategory category = (BookCategory) transformer.transform(BookCategory.class, "category", map);
 
         assertThat(category, is(BookCategory.Computer));
     }
 }

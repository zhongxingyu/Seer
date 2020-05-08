 package com.wedlum.styleprofile.domain.photo;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TagAutocompleteTest {
 
 	private PhotoSourceMock photoSourceMock;
 	private TagAutocomplete subject;
 
 	@Before
 	public void init() {
 		photoSourceMock = new PhotoSourceMock();
 		subject = new TagAutocompleteImpl(photoSourceMock);		
 	}
 
     @Test
     public void testAutocomplete(){
         photoSourceMock.setMetadataWithoutValidation(
                 "42.png",
                "Tag:\n" +
                        "   Sub-Tag: \n" +
                        "      - Tag Value 1\n" +
                        "      - Tag Value 2");
 
         Map<String, Set<String>> suggestMap = subject.getSuggestions();
 
         Assert.assertEquals(
                 "Root/Tag/Sub-Tag [Tag Value 1, Tag Value 2]\n" +
                 "Root/Tag [Sub-Tag]\n" +
                 "Root [Tag]",
                 toString(suggestMap)
         );
     }
 
     @Test
     public void testEmptyInput(){
         photoSourceMock.setMetadataWithoutValidation("42.png", "");
 
         Map<String, Set<String>> suggestMap = subject.getSuggestions();
         Assert.assertEquals("", toString(suggestMap)
         );
     }
 
     @Test
     public void testDuplicateOutuputEntries() {
         photoSourceMock.setMetadataWithoutValidation("42.png", "Tag:");
         photoSourceMock.setMetadataWithoutValidation("43.png", "Tag:");
 
          Map<String, Set<String>> suggestMap = subject.getSuggestions();
          Assert.assertEquals("Root [Tag]", toString(suggestMap));
 	}
 
     private String toString(Map<String, Set<String>> suggestMap) {
         String result = "";
         for(Map.Entry<String, Set<String>> entry : suggestMap.entrySet())
             result += entry.getKey() + " " + Arrays.toString(entry.getValue().toArray()) + "\n" ;
         return result.trim();
     }
 }

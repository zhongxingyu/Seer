 /** 
  *  Copyright 2007 Applied Research in Patacriticism and the University of Virginia
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  **/
 package org.nines;
 
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ValidationUtilityTest extends TestCase {
  
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   public void testGenre() {
     assertTrue(ValidationUtility.validateGenreInList("Poetry"));
     assertFalse(ValidationUtility.validateGenreInList("asdf"));
   }
 
   public void testGenreErrorList() {
     assertTrue(ValidationUtility.validateGenreInList("Poetry"));
     assertFalse(ValidationUtility.validateGenreInList("asdf"));
   }
 
   public void testFilterNonXMLCharacters() {
 	  char[] containsBadChar = new char[5];
 	  containsBadChar[0] = 'H';
 	  containsBadChar[1] = 'i';
 	  containsBadChar[2] = '!';
 	  containsBadChar[3] = 0x18;
 	  containsBadChar[4] = '&';
 	  
 	  String badString = new String(containsBadChar);  
 	  String goodString = ValidationUtility.filterNonXMLCharacters(badString);
 	  
 	  assertTrue( goodString.charAt(3) != 0x18 );
 	  assertTrue( goodString.compareTo("Hi!&") == 0 );
   }
 
   public void testValidateRequired() {
     // "archive","title","agent","genre","date_label"
     HashMap<String, ArrayList<String>> testMap = new HashMap<String, ArrayList<String>>();
 
     ArrayList<String> genreVals = new ArrayList<String>();
     testMap.put("genre", genreVals);
     testMap.put("archive", genreVals);
     testMap.put("year", genreVals);
 
    ArrayList<Message> messages = ValidationUtility.validateRequired(testMap);
 
     assertEquals(2, messages.size());
 
     testMap.put("title", genreVals);
     testMap.put("role_AUT", genreVals);
 
     messages = ValidationUtility.validateRequired(testMap);
 
     assertEquals(0, messages.size());
   }
 
   public void testValidateFreeCulture() {
     assertTrue(ValidationUtility.validateFreeculture("true"));
     assertTrue(ValidationUtility.validateFreeculture("false"));
     assertTrue(ValidationUtility.validateFreeculture(null));
     assertFalse(ValidationUtility.validateFreeculture("yes"));
   }
 
   public void testValidateRole() {
     HashMap<String, ArrayList<String>> object = new HashMap<String, ArrayList<String>>();
     // ART, AUT, EDT, PBL, and TRL are all we currently support
     object.put("role_ART", null);
     object.put("role_AUT", null);
     object.put("role_EDT", null);
     object.put("role_PBL", null);
     object.put("role_TRL", null);
     object.put("something_else", null);
 
     assertEquals(0, ValidationUtility.validateRole(object).size());
 
     object.put("role_XXX", null);
     assertEquals(1, ValidationUtility.validateRole(object).size());
 
   }
 }

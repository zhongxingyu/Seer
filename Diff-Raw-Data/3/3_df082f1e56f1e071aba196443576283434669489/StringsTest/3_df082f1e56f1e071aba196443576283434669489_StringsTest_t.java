 /*
  * Copyright 2010 CodeGist.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * ===================================================================
  *
  * More information at http://www.codegist.org.
  */
 
 package org.codegist.common.lang;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class StringsTest {
 
     @Test
     public void testIsBlank(){
         assertTrue(Strings.isBlank("\n\t \n"));
         assertTrue(Strings.isBlank(""));
         assertTrue(Strings.isBlank(null));
         assertTrue(Strings.isBlank(" "));
         assertTrue(Strings.isBlank("    "));
     }
     @Test
     public void testIsNotBlank(){
         assertTrue(Strings.isNotBlank("\n d\n"));
         assertTrue(Strings.isNotBlank("d"));
         assertTrue(Strings.isNotBlank(" ."));
         assertTrue(Strings.isNotBlank("  .  "));
     }
     @Test
     public void testDefaultIfBlank(){
         assertEquals("def", Strings.defaultIfBlank("\n\t \n", "def"));
         assertEquals("def", Strings.defaultIfBlank("", "def"));
         assertEquals("def", Strings.defaultIfBlank(null, "def"));
         assertEquals("def", Strings.defaultIfBlank(" ", "def"));
         assertEquals("def", Strings.defaultIfBlank("    ", "def"));
         assertEquals("\n d\n", Strings.defaultIfBlank("\n d\n", "def"));
         assertEquals("d", Strings.defaultIfBlank("d", "def"));
         assertEquals(" .", Strings.defaultIfBlank(" .", "def"));
         assertEquals("  .  ", Strings.defaultIfBlank("  .  ", "def"));
     }
 
     @Test
     public void testExtractGroups(){
 
         Strings.extractGroups("([^;]+)?;?(?:charset=(.*))?", "charset=sdfsdfdsf");
 
         assertArrayEquals(new String[]{"abbabc", "ab", "b", "c"}, Strings.extractGroups("(a(b*))+(c*)", "abbabcd"));
         assertArrayEquals(new String[]{"abbabc", "ab", "c"}, Strings.extractGroups("(a(?:b*))+(c*)", "abbabcd"));
         assertArrayEquals(new String[]{"abbabc", "b", "c"}, Strings.extractGroups("(?:a(b*))+(c*)", "abbabcd"));
         assertArrayEquals(new String[]{"abbabc", "c"}, Strings.extractGroups("(?:a(?:b*))+(c*)", "abbabcd"));
         assertArrayEquals(new String[]{"abbabc"}, Strings.extractGroups("(?:a(?:b*))+(?:c*)", "abbabcd"));
         assertArrayEquals(new String[]{}, Strings.extractGroups("(?:a(?:b*))+(?:c*)", "ghjgj"));
 
         String headerPattern = "^(?:([^;=]+)?;?charset=(.*+))|(?:([^;=]+);?)$";
 
         assertArrayEquals(new String[]{"application/vnd.oasis.opendocument.spreadsheet;charset=utf-8", "application/vnd.oasis.opendocument.spreadsheet", "utf-8", null},
                 Strings.extractGroups(headerPattern, "application/vnd.oasis.opendocument.spreadsheet;charset=utf-8"));
         assertArrayEquals(new String[]{"application/vnd.oasis.opendocument.spreadsheet", null,null,"application/vnd.oasis.opendocument.spreadsheet"},
                 Strings.extractGroups(headerPattern, "application/vnd.oasis.opendocument.spreadsheet"));
         assertArrayEquals(new String[]{"application/vnd.oasis.opendocument.spreadsheet;", null,null,"application/vnd.oasis.opendocument.spreadsheet"},
                 Strings.extractGroups(headerPattern, "application/vnd.oasis.opendocument.spreadsheet;"));
         assertArrayEquals(new String[]{"charset=utf-8", null, "utf-8", null},
                 Strings.extractGroups(headerPattern, "charset=utf-8"));
         assertArrayEquals(new String[]{";charset=utf-8", null, "utf-8", null},
                 Strings.extractGroups(headerPattern, ";charset=utf-8"));
 
        assertArrayEquals(new String[]{}, Strings.extractGroups(headerPattern, ""));
     }
 
 }

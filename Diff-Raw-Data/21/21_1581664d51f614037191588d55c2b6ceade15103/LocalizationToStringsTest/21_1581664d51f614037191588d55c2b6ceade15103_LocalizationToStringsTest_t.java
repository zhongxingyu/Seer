 /**
  * Copyright (C) 2011 Jesse Wilson
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
  */
 
 package com.publicobject.ss2s;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import junit.framework.TestCase;
 import org.xmlpull.v1.XmlPullParserException;
 
 public final class LocalizationToStringsTest extends TestCase {
 
   public void test() throws IOException, XmlPullParserException {
     Localization en = new Localization("en");
     en.add("title", "Shush! Ringer Restorer");
     en.add("okay", "Sweet!");
 
     StringWriter writer = new StringWriter();
     new LocalizationToStrings().localizationToStrings(en, writer);
     String xml = writer.toString();
 
     // TODO: use \n instead of \r\n
     assertEquals("<?xml version='1.0' ?>\r\n"
         + "<resources>\r\n"
         + "  <string name=\"title\">Shush! Ringer Restorer</string>\r\n"
         + "  <string name=\"okay\">Sweet!</string>\r\n"
         + "</resources>", xml);
   }
 
   public void testApostrophes() {
     LocalizationToStrings localizationToStrings = new LocalizationToStrings();
    assertEquals("\\\\", localizationToStrings.resourceEscape("\\"));
     assertEquals("\\\"", localizationToStrings.resourceEscape("\""));
     assertEquals("\\'", localizationToStrings.resourceEscape("'"));
    assertEquals("foo\\\\\\\'bar\\\\", localizationToStrings.resourceEscape("foo\\'bar\\"));
   }
 }

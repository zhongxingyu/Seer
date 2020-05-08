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
 import java.io.Writer;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 import org.xmlpull.v1.XmlSerializer;
 
 /**
  * Creates an XML file of this form.
  *
  * <resources>
  * <string name="title">Shush! Ringer Restorer</string>
  * <string name="okay">Sweet!</string>
  * </resources>
  */
 public final class LocalizationToStrings {
 
   public void localizationToStrings(Localization localization, Writer xmlWriter)
       throws XmlPullParserException, IOException {
     String ns = null;
 
     XmlSerializer out = XmlPullParserFactory.newInstance().newSerializer();
     out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
     out.setOutput(xmlWriter);
 
     out.startDocument(null, null);
     out.startTag(ns, "resources");
     for (int i = 0; i < localization.size(); i++) {
       out.startTag(ns, "string");
       out.attribute(ns, "name", localization.getName(i));
       out.text(resourceEscape(localization.getValue(i)));
       out.endTag(ns, "string");
     }
     out.endTag(ns, "resources");
     out.endDocument();
   }
 
   public String resourceEscape(String s) {
    StringBuilder result = new StringBuilder();
    for (char c : s.toCharArray()) {
      switch (c) {
      case '"':
      case '\\':
      case '\'':
        result.append('\\');
        break;
      }
      result.append(c);
    }
    return result.toString();
   }
 }

 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.xmlimport;
 
 import com.google.code.geobeagle.GeocacheFactory.Source;
 
 import java.io.IOException;
 
 enum PathType {
     CACHE {
         @Override
         public void startTag(XmlPullParserWrapper xmlPullParser,
                 ICachePersisterFacade cachePersisterFacade) {
             cachePersisterFacade.available(xmlPullParser.getAttributeValue(null, "available"));
             cachePersisterFacade.archived(xmlPullParser.getAttributeValue(null, "archived"));
         }
 
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             return true;
         }
     },
     CACHE_TYPE {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.cacheType(text);
             return true;
         }
     },
     CONTAINER {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.container(text);
             return true;
         }
     },
     DESC {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.wptDesc(text);
             return true;
         }
     },
     DIFFICULTY {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.difficulty(text);
             return true;
         }
     },
     GPX_TIME {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             return cachePersisterFacade.gpxTime(text);
         }
     },
     HINT {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             if (!text.equals(""))
                 cachePersisterFacade.hint(text);
             return true;
         }
     },
     LAST_MODIFIED {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             return true;
         }
     },
     LINE {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.line(text);
             return true;
         }
     },
     LOC_COORD {
         @Override
         public void startTag(XmlPullParserWrapper xmlPullParser,
                 ICachePersisterFacade cachePersisterFacade) {
             cachePersisterFacade.wpt(xmlPullParser.getAttributeValue(null, "lat"),
                     xmlPullParser.getAttributeValue(null, "lon"));
         }
     },
     LOC_WPT {
         @Override
         public void endTag(ICachePersisterFacade cachePersisterFacade) throws IOException {
             cachePersisterFacade.endCache(Source.LOC);
         }
     },
     LOC_WPTNAME {
         @Override
         public void startTag(XmlPullParserWrapper xmlPullParser,
                 ICachePersisterFacade cachePersisterFacade) throws IOException {
             cachePersisterFacade.startCache();
             cachePersisterFacade.wptName(xmlPullParser.getAttributeValue(null, "id"));
         }
 
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.groundspeakName(text.trim());
             return true;
         }
     },
     LOG_DATE {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.logDate(text);
             return true;
         }
     },
     LOG_TEXT {
         @Override
         public void endTag(ICachePersisterFacade cachePersisterFacade) throws IOException {
             cachePersisterFacade.setEncrypted(false);
         }
 
         @Override
         public void startTag(XmlPullParserWrapper xmlPullParser,
                 ICachePersisterFacade cachePersisterFacade) {
             cachePersisterFacade.setEncrypted("true".equalsIgnoreCase(xmlPullParser
                     .getAttributeValue(null, "encoded")));
         }
 
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.logText(text);
             return true;
         }
     },
     LOG_TYPE {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.logType(text);
             return true;
         }
     },
     LONG_DESCRIPTION {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.longDescription(text);
             return true;
         }
     },
     NAME {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.groundspeakName(text);
             return true;
         }
     },
     NOP {
         @Override
         public void startTag(XmlPullParserWrapper xmlPullParser,
                 ICachePersisterFacade cachePersisterFacade) {
         }
 
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             return true;
         }
     },
     PLACED_BY {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.placedBy(text);
             return true;
         }
     },
     SHORT_DESCRIPTION {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.shortDescription(text);
             return true;
         }
     },
     SYMBOL {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.symbol(text);
             return true;
         }
     },
     TERRAIN {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.terrain(text);
             return true;
         }
     },
     WPT {
         @Override
         public void endTag(ICachePersisterFacade cachePersisterFacade) throws IOException {
             cachePersisterFacade.endCache(Source.GPX);
         }
 
         @Override
         public void startTag(XmlPullParserWrapper xmlPullParser,
                 ICachePersisterFacade cachePersisterFacade) {
             cachePersisterFacade.startCache();
             cachePersisterFacade.wpt(xmlPullParser.getAttributeValue(null, "lat"),
                     xmlPullParser.getAttributeValue(null, "lon"));
         }
     },
     WPT_NAME {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.wptName(text);
             return true;
         }
     },
     WPT_TIME {
         @Override
         public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException {
             cachePersisterFacade.wptTime(text);
             return true;
         }
     };
 
     @SuppressWarnings("unused")
     public void endTag(ICachePersisterFacade cachePersisterFacade) throws IOException {
     }
 
     @SuppressWarnings("unused")
     public void startTag(XmlPullParserWrapper xmlPullParser,
             ICachePersisterFacade cachePersisterFacade) throws IOException {
     }
 
     @SuppressWarnings("unused")
     public boolean text(String text, ICachePersisterFacade cachePersisterFacade) throws IOException {
         return true;
     }
 }

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
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum GpxPath {
     XPATH_CACHE_CONTAINER("/gpx/wpt/groundspeak:cache/groundspeak:container", PathType.CONTAINER),
     XPATH_CACHE_DIFFICULTY("/gpx/wpt/groundspeak:cache/groundspeak:difficulty", PathType.DIFFICULTY),
     XPATH_CACHE_TERRAIN("/gpx/wpt/groundspeak:cache/groundspeak:terrain", PathType.TERRAIN),
     XPATH_EXT_LONGDESC("/gpx/wpt/extensions/cache/long_description", PathType.LONG_DESCRIPTION),
     XPATH_EXT_SHORTDESC("/gpx/wpt/extensions/cache/short_description", PathType.SHORT_DESCRIPTION),
     XPATH_GEOCACHE_CONTAINER("/gpx/wpt/geocache/container", PathType.CONTAINER),
     XPATH_GEOCACHE_DIFFICULTY("/gpx/wpt/geocache/difficulty", PathType.DIFFICULTY),
     XPATH_GEOCACHE_EXT_DIFFICULTY("/gpx/wpt/extensions/cache/difficulty", PathType.DIFFICULTY),
     XPATH_GEOCACHE_EXT_TERRAIN("/gpx/wpt/extensions/cache/terrain", PathType.TERRAIN),
     XPATH_GEOCACHE_TERRAIN("/gpx/wpt/geocache/terrain", PathType.TERRAIN),
     XPATH_GEOCACHE_TYPE("/gpx/wpt/geocache/type", PathType.CACHE_TYPE),
     XPATH_GEOCACHEHINT("/gpx/wpt/geocache/hints", PathType.HINT),
     XPATH_GEOCACHELOGDATE("/gpx/wpt/geocache/logs/log/time", PathType.LOG_DATE),
     XPATH_GEOCACHENAME("/gpx/wpt/geocache/name", PathType.NAME),
     XPATH_GPXTIME("/gpx/time", PathType.GPX_TIME),
     XPATH_GROUNDSPEAKNAME("/gpx/wpt/groundspeak:cache/groundspeak:name", PathType.NAME),
     XPATH_HINT("/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints", PathType.HINT),
     XPATH_LAST_MODIFIED("/gpx/wpt/bcaching:cache/bcaching:lastModified", PathType.LAST_MODIFIED),
     XPATH_LOGDATE("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date",
             PathType.LOG_DATE),
     XPATH_LOGTYPE("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type",
             PathType.LOG_TYPE),
     XPATH_LONGDESC("/gpx/wpt/groundspeak:cache/groundspeak:long_description",
             PathType.LONG_DESCRIPTION),
     XPATH_PLACEDBY("/gpx/wpt/groundspeak:cache/groundspeak:placed_by", PathType.PLACED_BY),
     XPATH_SHORTDESC("/gpx/wpt/groundspeak:cache/groundspeak:short_description",
             PathType.SHORT_DESCRIPTION),
     XPATH_SYM("/gpx/wpt/sym", PathType.SYMBOL),
     XPATH_TERRACACHINGGPXTIME("/gpx/metadata/time", PathType.GPX_TIME),
     XPATH_WAYPOINT_TYPE("/gpx/wpt/type", PathType.CACHE_TYPE),
     XPATH_WPTDESC("/gpx/wpt/desc", PathType.DESC),
     XPATH_WPTNAME("/gpx/wpt/name", PathType.WPT_NAME);
 
     private enum PathType {
         CACHE_TYPE {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.cacheType(text);
                 return false;
             }
         },
         CONTAINER {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
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
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.difficulty(text);
                 return true;
             }
         },
         GPX_TIME {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
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
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 return true;
             }
         },
         LOG_DATE {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.logDate(text);
                 return true;
             }
         },
         LOG_TYPE {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.logType(text);
                 return true;
             }
         },
         LONG_DESCRIPTION {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.longDescription(text);
                 return true;
             }
         },
         NAME {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.groundspeakName(text);
                 return true;
             }
         },
         PLACED_BY {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.placedBy(text);
                 return true;
             }
         },
         SHORT_DESCRIPTION {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.shortDescription(text);
                 return true;
             }
         },
         SYMBOL {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.symbol(text);
                 return true;
             }
         },
         TERRAIN {
             @Override
             boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.terrain(text);
                 return true;
             }
         },
         WPT_NAME {
             @Override
             public boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                     throws IOException {
                 cachePersisterFacade.wptName(text);
                 return true;
             }
         };
         abstract boolean text(String text, ICachePersisterFacade cachePersisterFacade)
                 throws IOException;
     }
 
     private static final Map<String, GpxPath> stringToEnum = new HashMap<String, GpxPath>();
 
     static {
         for (GpxPath gpxPath : values())
             stringToEnum.put(gpxPath.getPath(), gpxPath);
     }
 
     public static GpxPath fromString(String symbol) {
         return stringToEnum.get(symbol);
     }
 
     private final String path;
     private final PathType pathType;
 
     GpxPath(String path, PathType pathType) {
         this.path = path;
         this.pathType = pathType;
     }
 
     public String getPath() {
         return path;
     }
 
    public boolean text(String text, ICachePersisterFacade cachePersisterFacade) throws IOException {
        return pathType.text(text, cachePersisterFacade);
     }
 }

 /**
  * JsTest Maven Plugin
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.awired.jstest.resource;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.plugin.logging.Log;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Maps;
 
 public class ResourceResolver {
 
 	public static final String SRC_YUI_RESOURCE_PREFIX = "yui";
     public static final String SRC_RESOURCE_PREFIX = "/src/";
     public static final String LIB_RESOURCE_PREFIX = "/src/lib/";
     public static final String TEST_RESOURCE_PREFIX = "/test/";
 
     private ResourceDirectoryScanner directoryScanner = new ResourceDirectoryScanner();
 
     private final Log log;
     private final ResourceDirectory source;
     private final ResourceDirectory test;
     private final List<ResourceDirectory> sourceOverlays;
     private final List<ResourceDirectory> preloadOverlayDirs;
 
     private final Map<String, File> resources = new LinkedHashMap<String, File>();
     
     private final Map<String, File> srcMap = new LinkedHashMap<String, File>();
 
     public ResourceResolver(Log log, ResourceDirectory source, ResourceDirectory test,
             List<ResourceDirectory> overlays, List<ResourceDirectory> preloadOverlayDirs) {
         this.log = log;
         this.source = source;
         this.test = test;
         this.sourceOverlays = overlays;
         this.preloadOverlayDirs = preloadOverlayDirs;
 
         for (ResourceDirectory overlay : overlays) {
             String path = overlay.getDirectory().getPath();
             log.debug("Resource dir path " + path);
             registerResourcesToMap(LIB_RESOURCE_PREFIX, directoryScanner.scan(overlay), overlay.getDirectory(), true);
            //registerResourcesToMap(SRC_RESOURCE_PREFIX, directoryScanner.scan(overlay), overlay.getDirectory(), true);
         }
         
         for (ResourceDirectory overlayPreload : preloadOverlayDirs) {
             String path = overlayPreload.getDirectory().getPath();
             log.debug("OverlayPreload dir path " + path);
             registerResourcesToMap(SRC_RESOURCE_PREFIX, directoryScanner.scan(overlayPreload),
                     overlayPreload.getDirectory(), false);
         }
         
         log.debug("test.getDirectory  " + test.getDirectory().getPath());
         log.debug("source.getDirectory  " + source.getDirectory().getPath());
         registerResourcesToMap(TEST_RESOURCE_PREFIX, directoryScanner.scan(test), test.getDirectory(), true);
         registerSrcResourcesToMap(SRC_RESOURCE_PREFIX, directoryScanner.scan(source), source.getDirectory(), true);
 
         if (log.isDebugEnabled()) {
             log.debug("Resources resolved by the server : ");
             for (String resourcePath : resources.keySet()) {
                 log.debug("* " + resourcePath + " to " + resources.get(resourcePath));
             }
         }
     }
     
     private void registerSrcResourcesToMap(String prefix, List<String> founds, File path, boolean logOnConflict) {
     	for (String found : founds) {
             File fullPath = new File(path, found);
             File alreadyRegistered = resources.get(found);
             if (alreadyRegistered != null) {
                 log.warn("Resource conflics for : " + found + ". Found in " + alreadyRegistered + " and in "
                         + fullPath);
             } else {
                 String foundWithSlashes = found.replaceAll("\\\\", "/");
                 resources.put(prefix + foundWithSlashes, fullPath);
                 srcMap.put(prefix + foundWithSlashes, fullPath);
             }
         }
     }
 
     private void registerResourcesToMap(String prefix, List<String> founds, File path, boolean logOnConflict) {
         for (String found : founds) {
             File fullPath = new File(path, found);
             File alreadyRegistered = resources.get(found);
             if (alreadyRegistered != null) {
                 log.warn("Resource conflics for : " + found + ". Found in " + alreadyRegistered + " and in "
                         + fullPath);
             } else {
                 String foundWithSlashes = found.replaceAll("\\\\", "/");
                String resourcePrefix = prefix + foundWithSlashes;
                log.debug("ResourcePrefix " + resourcePrefix);
                resources.put(resourcePrefix, fullPath);
                String sourcePrefix = prefix + foundWithSlashes;
                log.debug("SourcePrefix " + sourcePrefix);
                srcMap.put(sourcePrefix, fullPath);
             }
         }
     }
 
     public File getResource(String path) {
         return resources.get(path);
     }
 
     public void updateChangeableDirectories() {
         cleanNotExists();
         if (source.isUpdatable()) {
             log.debug("Updating directory files for " + source.getDirectory());
             registerResourcesToMap(SRC_RESOURCE_PREFIX, directoryScanner.scan(source), source.getDirectory(), false);
         }
         if (test.isUpdatable()) {
             log.debug("Updating directory files for " + test.getDirectory());
             registerResourcesToMap(TEST_RESOURCE_PREFIX, directoryScanner.scan(test), test.getDirectory(), false);
         }
         for (ResourceDirectory overlayPreload : preloadOverlayDirs) {
             log.debug("Updating directory files for " + overlayPreload.getDirectory());
             registerResourcesToMap(SRC_RESOURCE_PREFIX, directoryScanner.scan(overlayPreload),
                     overlayPreload.getDirectory(), false);
         }
     }
 
     public Map<String, File> FilterSourcesKeysAlone() {
     	return Maps.filterKeys(srcMap, new Predicate<String>() {
             @Override
             public boolean apply(String input) {
                 return input.startsWith(SRC_RESOURCE_PREFIX);
             }
         });
     }
     
     public Map<String, File> FilterSourcesKeys() {
         return Maps.filterKeys(resources, new Predicate<String>() {
             @Override
             public boolean apply(String input) {
                 return input.startsWith(SRC_RESOURCE_PREFIX);
             }
         });
     }
     
     public Map<String, File> FilterNonYUISourcesKeys() {
         return Maps.filterKeys(resources, new Predicate<String>() {
             @Override
             public boolean apply(String input) {
                 return input.startsWith(SRC_RESOURCE_PREFIX) && !input.contains(SRC_YUI_RESOURCE_PREFIX);
             }
         });
     }
 
     public Map<String, File> FilterTestsKeys() {
         return Maps.filterKeys(resources, new Predicate<String>() {
             @Override
             public boolean apply(String input) {
                 return input.startsWith(TEST_RESOURCE_PREFIX);
             }
         });
     }
 
     private void cleanNotExists() {
         Iterator<String> iterator = resources.keySet().iterator();
         while (iterator.hasNext()) {
             String key = iterator.next();
             File val = resources.get(key);
             if (!val.exists()) {
                 iterator.remove();
             }
         }
     }
 
 }

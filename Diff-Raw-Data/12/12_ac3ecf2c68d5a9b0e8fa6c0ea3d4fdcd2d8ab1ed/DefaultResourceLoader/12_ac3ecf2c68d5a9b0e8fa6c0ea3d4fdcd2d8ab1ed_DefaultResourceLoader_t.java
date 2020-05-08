 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.resourceloaders;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.histone.evaluator.nodes.Node;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.net.URI;
 
 /**
  * Histone default resource loader<br/>
  * Supports file and http protocols
  */
 public class DefaultResourceLoader implements ResourceLoader {
     private static final Logger log = LoggerFactory.getLogger(DefaultResourceLoader.class);
 
     @Override
     public String resolveFullPath(String location, String baseLocation) throws ResourceLoadException {
         log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});
 
         URI fullLocation = makeFullLocation(location, baseLocation);
 
         return fullLocation.toString();
     }
 
     @Override
     public Resource load(String location, String baseLocation, Node... args) throws ResourceLoadException {
         log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});
 
         URI fullLocation = makeFullLocation(location, baseLocation);
 
         Resource resource = null;
 
         if (fullLocation.getScheme().equals("file")) {
             resource = loadFileResource(fullLocation);
         } else if (fullLocation.getScheme().equals("http")) {
             resource = loadHttpResource(fullLocation, args);
         } else {
             throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", fullLocation.getScheme()));
         }
 
 
         return resource;
     }
 
     private Resource loadFileResource(URI location) {
         InputStream stream = null;
 
         File file = new File(location);
         if (file.exists() && file.isFile() && file.canRead()) {
             try {
                 stream = new FileInputStream(file);
             } catch (FileNotFoundException e) {
                 throw new ResourceLoadException("File not found", e);
             }
         } else {
             throw new ResourceLoadException(String.format("Can't read file '%s'", location.toString()));
         }
 
         return new StreamResource(stream, location.toString());
     }
 
     private Resource loadHttpResource(URI location, Node[] args) {
         return null;  //To change body of created methods use File | Settings | File Templates.
     }
 
     public URI makeFullLocation(String location, String baseLocation) {
         if (location == null) {
             throw new ResourceLoadException("Resource location is undefined!");
         }
 
         URI locationURI = URI.create(location);
 
         if (baseLocation == null && !locationURI.isAbsolute()) {
             throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
         }
 
		if (baseLocation != null) {
			baseLocation = baseLocation.replace("\\", "/");
			baseLocation = baseLocation.replace("file://", "file:/");
		}
         URI baseLocationURI = (baseLocation != null) ? URI.create(baseLocation) : null;
 
         if (!locationURI.isAbsolute() && baseLocation != null) {
             locationURI = baseLocationURI.resolve(locationURI.normalize());
         }
 
         if (!locationURI.isAbsolute()) {
             throw new ResourceLoadException("Resource location is not absolute!");
         }
 
         return locationURI;
     }
 }

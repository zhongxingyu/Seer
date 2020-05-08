 /**
  *
  * Copyright 2013 Dennis Reedy
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.boot.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sorcer.resolver.Resolver;
 
 /**
  * @author Dennis Reedy
  */
 public class JarClassPathHelper {
 	final private static Logger log = LoggerFactory.getLogger(JarClassPathHelper.class);
 
 	// short file name (np path) => File with full path
 	private Map<String, File> cache = new HashMap<String, File>();
 
 	public Collection<String> getClassPathFromJar(File f) {
 		Set<String> result = new HashSet<String>();
 		log.debug("Creating jar file path from [{}]", f.getPath());
 		try {
 			JarFile jar = new JarFile(f);
 			Manifest man = jar.getManifest();
 			if (man == null) {
 				return result;
 			}
 			Attributes attributes = man.getMainAttributes();
 			if (attributes == null) {
 				return result;
 			}
 			String values = (String) attributes.get(new Attributes.Name("Class-Path"));
 			if (values != null) {
 				for (String v : values.split(" ")) {
 					File add = findFile(v);
 					if (add != null) {
 						result.add(add.getCanonicalPath());
 					} else {
 						log.warn("Could not find {} on the search path", v);
 					}
 				}
 			}
 			return result;
 		} catch (IOException e) {
 			throw new IllegalArgumentException("Error while reading classpath", e);
 		}
 	}
 
 	//do not call recursively
 	private File findFile(String name) {
 		if (cache.containsKey(name)) {
 			return cache.get(name);
 		}
 		File result = Resolver.resolveSimpleName(name);
        if (result != null) {
            cache.put(name, result);
        }
        return result;
 	}
 }

 /**
  *
  * Copyright 2013 the original author or authors.
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
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sorcer.core.SorcerEnv;
 import sorcer.resolver.Resolver;
 
 /**
  * @author Rafał Krupiński
  */
 public class JarClassPathHelper {
 	final private static Logger log = LoggerFactory.getLogger(JarClassPathHelper.class);
 	
 	// short file name (np path) => File with full path
 	private Map<String, File> cache = new HashMap<String, File>();
 	final private static File[] jarRoots;
 
 	static {
         if (Resolver.isMaven()) {
             String repo = SorcerEnv.getRepoDir();
             jarRoots = new File[] {
                     new File(repo, "org/apache/river"),
                     new File(repo, "net/jini"),
                     new File(repo, "org/sorcersoft"),
                     new File(SorcerEnv.getHomeDir(), "lib") };
         } else {
             jarRoots = new File[] {
                     new File(Resolver.getRootDir()) };
         }
 	}
 
 	public void getClassPathFromJar(List<String> buff, File f) {
 		try {
 
 			log.debug("Creating jar file path from [{}]", f.getCanonicalPath());
            if (f.getAbsolutePath().contains(" "))
                f = new File(f.getCanonicalPath());
            log.info("File to read manifest: " + f.toString());
             JarFile jar = new JarFile(f);
 			Manifest man = jar.getManifest();
 			if (man == null) {
 				return;
 			}
 			Attributes attributes = man.getMainAttributes();
 			if (attributes == null) {
 				return;
 			}
 			String values = (String) attributes.get(new Attributes.Name("Class-Path"));
 			if (values != null) {
 				for (String v : values.split(" ")) {
 					File add = findFile(v);
 					if (add != null) {
 						buff.add(add.getCanonicalPath());
 					} else {
 						log.warn("Could not find {} on the search path",v);
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new IllegalArgumentException("Error while reading classpath", e);
 		}
 	}
 
 	//do not call recursively
 	private File findFile(String name) {
 		if (cache.containsKey(name)) {
 			return cache.get(name);
 		}
 		File file = new File(name);
 		if (file.exists()) {
 			return file;
 		}
 		File result = findFile(name, jarRoots);
 		cache.put(name, result);
 		return result;
 	}
 
 	private static File findFile(String name, File[] roots) {
 		for (File root : roots) {
 			File file = findJar(root, name);
 			if (file != null) {
 				return file;
 			}
 		}
 		return null;
 	}
 
 	private static File findJar(File root, String name) {
 		File result = new File(root, name);
 		if (result.exists()) {
 			return result;
 		}
 		File[] files = root.listFiles(new DirectoryFilter());
 		if (files == null || files.length == 0) {
 			return null;
 		}
 		return findFile(name, files);
 	}
 
 	private static class DirectoryFilter implements FileFilter {
 		@Override
 		public boolean accept(File pathname) {
 			return pathname.isDirectory();
 		}
 	}
 
 }

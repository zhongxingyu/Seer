 /*
  * junitflood - An automatic junit test generator
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.junitflood.classpath;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.objectweb.asm.ClassReader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mebigfatguy.junitflood.Configuration;
 import com.mebigfatguy.junitflood.util.Closer;
 
 public class ClassLookup {
 
 	private static final  Logger logger = LoggerFactory.getLogger(ClassLookup.class);
 
 	private final URLClassLoader classLoader;
 	private final Map<String, Map<LookupType, Map<String, Access>>> classDetails = new HashMap<String, Map<LookupType, Map<String, Access>>>();
 
 	public ClassLookup(Configuration config) throws MalformedURLException {
 		Set<File> roots = new HashSet<File>();
 		roots.addAll(config.getScanClassPath());
 		roots.addAll(config.getAuxClassPath());
 
		URL[] urls = new URL[roots.size()];
 
 		int i = 0;
 		for (File root : roots) {
 			urls[i++] = new URL("file://" + root.getAbsolutePath());
 		}
 
		classLoader = new URLClassLoader(urls);
 	}
 
 	public Set<String> getConstructors(String clsName, String fromClass) {
 		Map<LookupType, Map<String, Access>> classInfo = classDetails.get(clsName);
 		if (classInfo == null) {
 			classInfo = loadClassDetails(clsName);
 		}
 
 		Map<String, Access> ctors = classInfo.get(LookupType.CONSTRUCTOR);
 		if (ctors != null) {
 			return ctors.keySet(); //for now
 		} else {
 			return Collections.<String>emptySet();
 		}
 	}
 
 	private Map<LookupType, Map<String, Access>> loadClassDetails(String clsName) {
 
 		Map<LookupType, Map<String, Access>> details = new EnumMap<LookupType, Map<String, Access>>(LookupType.class);
 		InputStream is = null;
 		try {
 			is = classLoader.getResourceAsStream(clsName + ".class");
 			ClassReader cr = new ClassReader(is);
 			ClassInfoCollectingVisitor cicv = new ClassInfoCollectingVisitor(details);
 			cr.accept(cicv, ClassReader.SKIP_DEBUG|ClassReader.SKIP_CODE);
 			classDetails.put(clsName, details);
 		} catch (IOException ioe) {
 			logger.error("Failed parsing class " + clsName, ioe);
 		} finally {
 			Closer.closeQuietly(is);
 		}
 		return details;
 	}
 }

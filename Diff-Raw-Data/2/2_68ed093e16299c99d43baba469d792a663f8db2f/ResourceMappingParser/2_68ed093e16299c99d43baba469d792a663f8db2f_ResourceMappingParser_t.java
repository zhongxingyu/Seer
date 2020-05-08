 /*******************************************************************************
  * Copyright (c) Jun 9, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.mapping;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.zend.sdklib.mapping.IMapping;
 import org.zend.sdklib.mapping.IResourceMapping;
 
 public class ResourceMappingParser {
 
	private static final String MAPPING_DEFAULT = "tools/conf/excludes.default";
 	private static final String CONTENT = "/*";
 	private static final String SEPARATOR = ",";
 	private static final String GLOBAL = "**/";
 	private static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
 	private static final String INCLUDES = ".includes";
 	private static final String EXCLUDES = ".excludes";
 	private File container;
 
 	public IResourceMapping load(File file) throws IOException {
 		ResourceMapping mapping = new ResourceMapping();
 		if (file == null || !file.exists()) {
 			return null;
 		}
 		this.container = file;
 		Properties props = loadProperties(new FileInputStream(new File(file,
 				DEPLOYMENT_PROPERTIES)));
 		mapping.setInclusion(getMapping(props, INCLUDES));
 		mapping.setExclusion(getMapping(props, EXCLUDES));
 		mapping.setDefaultExclusion(getDefaultExclusion());
 		return mapping;
 	}
 
 	public Set<IMapping> getMappings(String[] result) throws IOException {
 		Set<IMapping> mappings = new HashSet<IMapping>();
 		for (int i = 0; i < result.length; i++) {
 			String file = result[i].trim();
 			boolean isContent = file.endsWith(CONTENT);
 			if (isContent) {
 				file = file.substring(0, file.length() - SEPARATOR.length());
 			}
 			boolean isGlobal = file.startsWith(GLOBAL);
 			if (isGlobal) {
 				file = file.substring(GLOBAL.length());
 			} else {
 				file = new File(container, file).getCanonicalPath();
 			}
 			mappings.add(new Mapping(file, isContent, isGlobal));
 		}
 		return mappings;
 	}
 
 	private Map<String, Set<IMapping>> getMapping(Properties props, String kind)
 			throws IOException {
 		Map<String, Set<IMapping>> result = new HashMap<String, Set<IMapping>>();
 		Enumeration<?> e = props.propertyNames();
 		while (e.hasMoreElements()) {
 			String folderName = (String) e.nextElement();
 			if (folderName.endsWith(kind)) {
 				String[] files = ((String) props.getProperty(folderName))
 						.split(SEPARATOR);
 				Set<IMapping> mappings = getMappings(files);
 				folderName = folderName.substring(0, folderName.indexOf("."));
 				result.put(folderName, mappings);
 			}
 		}
 		return result;
 	}
 
 	private Properties loadProperties(InputStream stream) throws IOException {
 		Properties props = new Properties();
 		props.load(stream);
 		return props;
 	}
 
 	private Set<IMapping> getDefaultExclusion() throws IOException {
 		final InputStream stream = getDefaultExclusionStream();
 		if (stream != null) {
 			Properties props = loadProperties(stream);
 			String excludes = (String) props.get(EXCLUDES);
 			if (excludes != null) {
 				String[] result = excludes.split(SEPARATOR);
 				return getMappings(result);
 			}
 		}
 		return Collections.emptySet();
 	}
 
 	private InputStream getDefaultExclusionStream()
 			throws FileNotFoundException {
 		File zendSDKJarFile = new File(getClass().getProtectionDomain()
 				.getCodeSource().getLocation().getPath());
 
 		File zendSDKroot = zendSDKJarFile.getParentFile().getParentFile();
 		File mapping = new File(zendSDKroot, MAPPING_DEFAULT);
 
 		// in development-time scenario, classes are in "sdklib", instead of
 		// "lib/zend_sdk.jar"
 		if (!mapping.exists()) {
 			zendSDKroot = zendSDKJarFile.getParentFile();
 			mapping = new File(zendSDKroot, MAPPING_DEFAULT);
 		}
 		return mapping.exists() ? new FileInputStream(mapping) : null;
 	}
 
 }

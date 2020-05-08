 package com.adaptiweb.utils.ci;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 import com.adaptiweb.utils.ci.LiveFile.FileLoader;
 import com.adaptiweb.utils.commons.InicializableVariableSource;
 import com.adaptiweb.utils.commons.Properties;
 import com.adaptiweb.utils.commons.VariableResolver;
 
 public class PropertyFileVariableSource implements InicializableVariableSource, FileLoader {
 	
 	private final AtomicReference<Properties> properties = new AtomicReference<Properties>();
 	private final LiveFileHandler fileHandler = new LiveFileHandler();
 	
 	public void setPropertyFileName(String fileName) {
 		fileHandler.setPropertyFile(fileName);
 	}
 	
 	public void setSystemResourceAsTemplate(String template) {
 		fileHandler.setTemplateResource(template);
 	}
 	
 	@Override
 	public String getRawValue(String variableName) throws NullPointerException {
 		fileHandler.checkChanges(this);
 		return properties.get().getProperty(variableName);
 	}
 	
 	@Override
 	public void loadFile(File file) throws IOException {
 		System.out.println("Loading configuration from " + file);
 		properties.set(new Properties(file));
 	}
 
 	@Override
 	public void initSource(VariableResolver variables) throws IOException {
 		fileHandler.setVariables(variables);
 		fileHandler.checkChanges(this);
 	}
 	
 	public Properties getProperties() {
 		return properties.get();
 	}
 	
 	public Map<String,String> extractDirectChildrenProperties(String prefix) {
 		Map<String,String> result = new HashMap<String, String>();
 		
 		Properties subProperties = properties.get().select(prefix);
 		for (String child : subProperties) {
 			if (child.indexOf('.') == -1) {
 				result.put(child, subProperties.getProperty(child));
 			}
 		}
 		return result;
 	}
 
 }

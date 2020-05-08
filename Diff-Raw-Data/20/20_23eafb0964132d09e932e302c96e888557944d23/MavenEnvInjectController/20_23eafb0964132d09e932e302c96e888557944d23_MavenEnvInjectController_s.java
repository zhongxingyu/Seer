 package com.kik.inject.json;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.NameNotFoundException;
 
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Base64;
 import org.json.simple.JSONObject;
 
 import com.kik.inject.iface.ParamProcessor;
 
 public class MavenEnvInjectController implements ParamProcessor {
 	private final Map<String, String> systemProperties;
 	private final Log _log;
 	
 	public MavenEnvInjectController(MavenProject project,Log log)
 	{		
 		_log = log;
 		_log.debug("LOG!");
 		systemProperties = addPropertiesFromProperties(project.getProperties());
 	}
 	
 	protected Map<String, String> addPropertiesFromProperties(
 			Properties properties) 
 	{
 		final Map<String, String> systemParams = new HashMap<String, String>();
 		Set<Object> propertyKeys = properties.keySet();
 
 		for (Object propertyKey : propertyKeys) {
 			if ( propertyKey instanceof String ){ 
 				String propertyKeyString = (String) propertyKey;
 				if (propertyKeyString != null ) {
 					String value = properties.getProperty(propertyKeyString,"");
 					
 					_log.debug("KEY: "  + propertyKeyString);
 					_log.debug("VALUE: "+value);
 					
 					systemParams.put(propertyKeyString,value);
 				}
 			}
 		}
 		return systemParams;
 	}
 	
 	@Override
 	public boolean outputVarContentsToStream(OutputStream os, String varName)
 			throws NameNotFoundException, IOException {
 		boolean isBase64 = false;
 		boolean isEscaped = false;
 		boolean canHandle = false;
 		final String cleanParamName;
 		if ( varName.contains("|") ) {
 			int index = varName.lastIndexOf("|");
 			String options = varName.substring(index,varName.length());
 			cleanParamName = varName.substring(0, index);
 			
 			if ( options.contains("b") ) {
 				isBase64 = true;
 			} 
 			
 			if ( options.contains("e") ) {
 				isEscaped = true;
 			}
 			
 		} else {
 			cleanParamName = varName;
 		}
 		
 		if ( systemProperties.containsKey(cleanParamName)) {
			
			String value = systemProperties.get(varName);
			final byte[] writtenValue;
			if ( isBase64 ) {
				writtenValue = Base64.encodeBase64(value.getBytes(Charset.forName("UTF-8")));
			} else {
				writtenValue = value.getBytes(Charset.forName("UTF-8"));
			}
			
 			if ( isEscaped ) {
				writeJSONEscapedStringToStream(os, writtenValue);
 			} else {
				os.write(systemProperties.get(varName).getBytes(Charset.forName("UTF-8")));
 			}
 			canHandle = true;
 		}
 		
 		return canHandle;
 	}
 	
 	protected void writeJSONEscapedStringToStream(OutputStream os,
 			String toWrite) throws IOException {
		String tmpString = JSONObject.escape(toWrite);
 		os.write(tmpString.getBytes(Charset.forName("UTF-8")));
 	}
 	
 	@Override
 	public Set<String> getKeySet() {
 		Set<String> mavenEnvVars = new HashSet<String>();
 		mavenEnvVars.addAll(systemProperties.keySet());
 		return mavenEnvVars;
 	}
 
 }

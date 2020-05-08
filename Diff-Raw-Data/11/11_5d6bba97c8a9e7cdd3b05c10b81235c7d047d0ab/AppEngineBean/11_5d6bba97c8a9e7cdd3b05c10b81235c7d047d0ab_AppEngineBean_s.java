 package org.gwtapp.startapp.server;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.appengine.api.utils.SystemProperty;
 
 public class AppEngineBean {
 
 	public String getDeployVersion() {
 		String version = SystemProperty.applicationVersion.get();
 		if (StringUtils.isEmpty(version)) {
 			return "R" + Math.random();
 		} else {
 			return version;
 		}
 	}
 }

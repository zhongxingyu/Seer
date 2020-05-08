 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.tamarillo.addressws.controller;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.commons.lang3.StringUtils;
 import org.tamarillo.addressws.util.JsfUtil;
 
 import org.slf4j.Logger;
 
 /**
  * Get information from Maven to inject on our java project.
  * 
  * (based on Oleg Varaksin example)
  * 
  * http://ovaraksin.blogspot.com/2012/02/advanced-injection-of-maven-properties.html
  * 
  */
 @ApplicationScoped
 @Named(value = "technicalInfo")
 public class TechnicalInfo implements Serializable{
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = -8138656826572480010L;
 
 	/** The log. */
 	@Inject
 	private Logger log;
 	
 	/** The revision. */
 	private String revision;
 	
 	/** The build time. */
 	private String buildTime;
 	
 	/** The version. */
 	private String version;
 
 	/**
 	 * Initialize.
 	 */
 	@PostConstruct
 	protected void initialize() {
 		ResourceBundle rb;
 		try {
 			rb = ResourceBundle.getBundle("addressws");
 			
 			//TODO: fetch git info
 			
 			// fetch the application properties
 			String strAppProps = rb.getString("application.properties");
 			log.info("application.properties = " + strAppProps);
 
 			// remove unwanted maven stuff
 			int lastBrace = strAppProps.lastIndexOf("}");
 			
 			strAppProps = strAppProps.substring(1, lastBrace);
 
 			Map<String, String> appProperties = new HashMap<String, String>();
 			String[] appProps = strAppProps.split("[\\s,]+");
 			for (String appProp : appProps) {
 				String[] keyValue = appProp.split("=");
 				if (keyValue != null && keyValue.length > 1) {
 					appProperties.put(keyValue[0], keyValue[1]);
 				}
 			}
 //			revision = appProperties.get("git.revision");
 
 			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			Calendar calendar = Calendar.getInstance();
 			if (appProperties.get("timestamp") != null) {
 				calendar.setTimeInMillis(Long.valueOf(appProperties.get("timestamp")));
 				buildTime = formatter.format(calendar.getTime());
 			}
 			version = appProperties.get("addressws.version");
 
 		} catch (MissingResourceException e) {
 			log.warn("Resource bundle 'addressws' was not found!", e);
		} catch (Exception e) {
			log.warn("Runtime Exception!", e);
 		}
 	}
 
 	/**
 	 * Gets the revision.
 	 *
 	 * @return the revision
 	 */
 	public String getRevision() {
 		return revision;
 	}
 
 	/**
 	 * Gets the builds the time.
 	 *
 	 * @return the buildTime
 	 */
 	public String getBuildTime() {
 		return buildTime;
 	}
 
 	/**
 	 * Gets the version.
 	 *
 	 * @return the version
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * Gets the technical info.
 	 * 
 	 * @return the technical info
 	 */
 	public String getTechnicalInfo() {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		// "git-Revision: "
 		StringBuffer sb = new StringBuffer();
 		if (StringUtils.isNotBlank(getVersion())) {
 			sb.append(JsfUtil.getResourceText("technical.info.version", ctx));
 			sb.append(": ");
 			sb.append(getVersion());
 		}
 		if (StringUtils.isNotBlank(getRevision())) {
 			sb.append(", ");
 			sb.append(JsfUtil
 					.getResourceText("technical.info.version.git", ctx));
 			sb.append(": ");
 			sb.append(getRevision());
 		}
 		if (StringUtils.isNotBlank(getBuildTime())) {
 			sb.append(", ");
 			sb.append(JsfUtil.getResourceText("technical.info.build.time", ctx));
 			sb.append(": ");
 			sb.append(getBuildTime());
 		}
 		return sb.toString();
 	}
 
 }

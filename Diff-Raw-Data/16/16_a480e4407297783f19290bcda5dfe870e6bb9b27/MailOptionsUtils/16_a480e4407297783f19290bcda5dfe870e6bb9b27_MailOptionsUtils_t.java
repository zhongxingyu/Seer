 package org.notificationengine.mail;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.notificationengine.constants.Constants;
import org.notificationengine.spring.SpringUtils;
 
 public class MailOptionsUtils {
 
 	/**
 	 * Builds the mail options from context.
 	 *
 	 * @param context the context
 	 * @return the map
 	 */
 	public static Map<String, String> buildMailOptionsFromContext(Map<String, Object> context) {
 		
 		Map<String, String> options = new HashMap<>();
 		
 		Object fromAsObject = context.get(Constants.FROM);
 		
 		if (fromAsObject != null) {
 			
 			if (fromAsObject instanceof String) {
 				
 				String from = (String)fromAsObject;
 				
 				if (!StringUtils.isEmpty(from)) {
 					
 					options.put(Constants.FROM, from);
 				}
 			}
 		}
 		
 		Object subjectAsObject = context.get(Constants.SUBJECT);
 		
 		if (subjectAsObject != null) {
 			
 			if (subjectAsObject instanceof String) {
 				
 				String subject = (String)subjectAsObject;
 				
 				if (!StringUtils.isEmpty(subject)) {
 					
 					options.put(Constants.SUBJECT, subject);
 				}
 			}
 		}
 		
 		return options;
 	}
 
 	public static Map<String, String> buildMailOptionsFromContexts(
 			Collection<Map<String, Object>> contexts) {
 		
 		Map<String, String> options = new HashMap<>();
 		
 		// get from
 		String from = null;
 		
 		for (Map<String, Object> context : contexts) {
 			
 			Map<String, String> contextOptions = buildMailOptionsFromContext(context);
 			
 			String contextFrom = contextOptions.get(Constants.FROM);
 			
 			if (!StringUtils.isEmpty(contextFrom)) {
 
 				if (StringUtils.isEmpty(from)) {
 						
 					from = contextFrom;
 				}
 				else {
 					if (!StringUtils.equals(from, contextFrom)) {
 						
 						from = null;
 						
 						break;
 					}
 				}
 			}
 		}
 		
 		// get subject
 		String subject = null;
 		
 		for (Map<String, Object> context : contexts) {
 			
 			Map<String, String> contextOptions = buildMailOptionsFromContext(context);
 			
 			String contextSubject = contextOptions.get(Constants.SUBJECT);
 			
 			if (!StringUtils.isEmpty(contextSubject)) {
 
 				if (StringUtils.isEmpty(subject)) {
 						
 					subject = contextSubject;
 				}
 				else {
 					if (!StringUtils.equals(subject, contextSubject)) {

                        Properties localSettingsProperties = (Properties) SpringUtils.getBean(Constants.LOCAL_SETTINGS_PROPERTIES);
 						
						subject = localSettingsProperties.getProperty(Constants.DEFAULT_SUBJECT);
 						
 						break;
 					}
 				}
 			}
 		}
 		
 		// build options		
 		if (!StringUtils.isEmpty(from)) {
 		
 			options.put(Constants.FROM, from);
 		}
 		if (!StringUtils.isEmpty(subject)) {
 			
 			options.put(Constants.SUBJECT, subject);
 		}
 		
 		return options;
 	}
 }

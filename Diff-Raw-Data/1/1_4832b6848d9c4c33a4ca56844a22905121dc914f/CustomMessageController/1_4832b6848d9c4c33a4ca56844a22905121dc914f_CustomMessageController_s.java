 package org.openmrs.module.custommessage.web.controller;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.openmrs.api.context.Context;
 import org.openmrs.messagesource.MutableMessageSource;
 import org.openmrs.messagesource.PresentationMessage;
 import org.openmrs.messagesource.PresentationMessageMap;
 import org.openmrs.module.custommessage.CustomMessage;
 import org.openmrs.module.custommessage.CustomMessageConstants;
 import org.openmrs.module.custommessage.CustomMessageSource;
 import org.openmrs.module.custommessage.MessagesLocation;
 import org.openmrs.module.custommessage.service.CustomMessageService;
 import org.openmrs.module.custommessage.util.InMemoryZipStream;
 import org.openmrs.util.LocaleUtility;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class CustomMessageController {
 	
 	@RequestMapping("/module/custommessage/index.form")
 	public void viewIndex(ModelMap model, @RequestParam(value = "missingInLocale", required = false) Locale missingInLocale,
 	        @RequestParam(value = "matchingText", required = false) String matchingText) throws Exception {
 		
 		MutableMessageSource mms = Context.getMessageSourceService().getActiveMessageSource();
 		CustomMessageSource cms = (CustomMessageSource) mms;
 		cms.refreshCache();
 		
 		List<String> codes = new ArrayList<String>();
 		Map<String, Map<Locale, PresentationMessage>> messagesByLocale = cms.getAllMessagesByCode();
 		if (missingInLocale == null && StringUtils.isBlank(matchingText)) {
 			codes.addAll(messagesByLocale.keySet());
 		} else {
 			for (String code : messagesByLocale.keySet()) {
 				Map<Locale, PresentationMessage> forCode = messagesByLocale.get(code);
 				boolean codeMatches = StringUtils.isBlank(matchingText) || code.contains(matchingText);
 				boolean localeMatches = missingInLocale == null || forCode.get(missingInLocale) == null;
 				if (codeMatches && localeMatches) {
 					codes.add(code);
 				}
 			}
 		}
 		model.addAttribute("codes", codes);
 		model.addAttribute("missingInLocale", missingInLocale);
 		model.addAttribute("matchingText", matchingText);
 		
 		Map<String, Locale> localeMap = new TreeMap<String, Locale>();
 		for (Locale l : Context.getAdministrationService().getPresentationLocales()) {
 			localeMap.put(l.getDisplayName(), l);
 		}
 		model.addAttribute("supportedLocales", localeMap);
 	}
 	
 	@RequestMapping("/module/custommessage/getMessagesForCode.form")
 	public void getMessagesForCode(HttpServletResponse response, @RequestParam("code") String code) throws Exception {
 		response.setContentType("text/json");
 		response.setCharacterEncoding("UTF-8");
 		Map<String, Object> ret = new HashMap<String, Object>();
 		
 		MutableMessageSource mms = Context.getMessageSourceService().getActiveMessageSource();
 		CustomMessageSource cms = (CustomMessageSource) mms;
 		
 		Map<String, String> defaults = new HashMap<String, String>();
 		for (PresentationMessage pm : cms.getMutableParentSource().getPresentations()) {
 			if (code.equals(pm.getCode())) {
 				defaults.put(pm.getLocale().toString(), pm.getMessage());
 			}
 		}
 		ret.put("defaults", defaults);
 		
 		Map<String, Map<String, String>> customs = new HashMap<String, Map<String, String>>();
 		for (CustomMessage cm : Context.getService(CustomMessageService.class).getCustomMessagesForCode(code)) {
 			Map<String, String> custom = new HashMap<String, String>();
 			custom.put("id", cm.getId().toString());
 			custom.put("message", cm.getMessage());
 			customs.put(cm.getLocale().toString(), custom);
 		}
 		ret.put("customs", customs);
 		
 		response.getWriter().write(new ObjectMapper().writeValueAsString(ret));
 	}
 	
 	@RequestMapping("/module/custommessage/saveMessagesForCode.form")
 	public void saveMessagesForCode(HttpServletRequest request, HttpServletResponse response,
 	        @RequestParam("code") String code) {
 		CustomMessageService cms = Context.getService(CustomMessageService.class);
 		boolean hasChanged = false;
 		for (Locale l : Context.getAdministrationService().getPresentationLocales()) {
 			String id = request.getParameter("id" + l.toString());
 			String message = request.getParameter("message" + l.toString());
 			if (StringUtils.isNotBlank(id)) {
 				CustomMessage msg = cms.getCustomMessage(Integer.parseInt(id));
 				if (StringUtils.isNotBlank(message)) { // Update existing message
 					msg.setMessage(message);
 					cms.saveCustomMessage(msg);
 					hasChanged = true;
 				} else { // Delete existing message
 					cms.deleteCustomMessage(msg);
 					hasChanged = true;
 				}
 			} else {
 				if (StringUtils.isNotBlank(message)) { // Insert new message
 					CustomMessage msg = new CustomMessage();
 					msg.setCode(code);
 					msg.setLocale(l);
 					msg.setMessage(message);
 					cms.saveCustomMessage(msg);
 					hasChanged = true;
 				}
 			}
 		}
 		if (hasChanged) {
 			((CustomMessageSource) Context.getMessageSourceService().getActiveMessageSource()).refreshCache();
 		}
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/module/custommessage/export.form")
 	public void exportMessagePage(ModelMap model) {
 		// add map of supported locale to model to be used on export page 
 		Map<String, Locale> localeMap = new TreeMap<String, Locale>();
 		for (Locale l : Context.getAdministrationService().getPresentationLocales()) {
 			localeMap.put(l.getDisplayName(), l);
 		}
 		model.addAttribute("supportedLocales", localeMap);
 		// add map of locations to model to be used on export page
 		model.addAttribute("messagesLocations", Context.getService(CustomMessageService.class)
 		        .getAvailableMessagesLocationsMap());
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, value = "/module/custommessage/export.form")
 	public void exportMessageSubmit(HttpServletResponse response, @RequestParam("locale") String[] locales,
 	        @RequestParam("location") String[] locationIds,
 	        @RequestParam(value="onlyExportCustomizedMessages", required=false) Boolean onlyExportCustomizedMessages) throws Exception {
 		// decide if it is need to export only custom messages or all messages available for given locale and location
 		if (onlyExportCustomizedMessages != null) {
 			// if single file export is possible, then export customized messages into file and send it back via response output stream
 			if (locales.length == 1 && locationIds.length == 1) {
 				exportSingleFileWithOnlyCustomizedMessages(response, locales[0], locationIds[0]);
 			} else {
 				exportZipWithOnlyCustomizedMessages(response, locales, locationIds);
 			}
 		} else {
 			// if single file export is possible, then export customized messages into file and send it back via response output stream
 			if (locales.length == 1 && locationIds.length == 1) {
 				exportSingleFileWithAllMessages(response, locales[0], locationIds[0]);
 			} else {
 				exportZipWithAllMessages(response, locales, locationIds);
 			}
 		}
 	}
 	
 	/**
 	 * Writes into output a single file with both customized messages and messages from property
 	 * file corresponding to given locale and location
 	 * 
 	 * @param response the response instance to export file with
 	 * @param locale the locale of messages to be exported
 	 * @param locationId the location id of messages (i.e. name of project from where messages come)
 	 * @throws IOException if any I/O related error occurs
 	 */
 	private void exportSingleFileWithAllMessages(HttpServletResponse response, String locale, String locationId)
 	        throws IOException {
 		CustomMessageSource cms = (CustomMessageSource) Context.getMessageSourceService().getActiveMessageSource();
 		cms.refreshCache();
 		PresentationMessageMap cachedMessages = cms.getCachedMessages().get(LocaleUtility.fromSpecification(locale));
 		if (cachedMessages != null) {
 			Map<String, PresentationMessage> messageMap = new TreeMap<String, PresentationMessage>(cachedMessages);
 			Map<String, String> availableLocations = Context.getService(CustomMessageService.class)
 			        .getAvailableMessagesLocationsMap();
 			response.setContentType("text/plain");
 			response.addHeader("Content-disposition",
 			    String.format("attachment; filename=%s_messages_%s.properties", locationId, locale));
 			for (String code : messageMap.keySet()) {
 				String prefix = StringUtils.substringBefore(code, ".");
 				// when passed in location is core
 				if (StringUtils.equals(locationId, CustomMessageConstants.CUSTOM_MESSAGES_LOCATION_DEFAULT_ID)) {
 					// if message code prefix is not listed as key of available messages 
 					// locations this means that this message is related to core location
 					if (!availableLocations.containsKey(prefix)) {
 						PresentationMessage pm = messageMap.get(code);
 						if (pm != null && StringUtils.isNotBlank(pm.getMessage())) {
 							response.getWriter().write(code + " = " + pm.getMessage() + System.getProperty("line.separator"));
 						}
 					}
 				} else {
 					// if message code prefix is equal to passed in location id
 					if (StringUtils.equals(locationId, prefix)) {
 						PresentationMessage pm = messageMap.get(code);
 						if (pm != null && StringUtils.isNotBlank(pm.getMessage())) {
 							response.getWriter().write(
 							    String.format("%s = %s%s", code, pm.getMessage(), System.getProperty("line.separator")));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Writes into output a single file with only customized messages corresponding to given locale
 	 * and location
 	 * 
 	 * @param response the response instance to export file with
 	 * @param locale the locale of messages to be exported
 	 * @param locationId the identifier of location of messages to be exported (i.e. name of project
 	 *            from where messages come)
 	 * @throws IOException if any I/O related error occurs
 	 */
 	private void exportSingleFileWithOnlyCustomizedMessages(HttpServletResponse response, String locale, String locationId)
 	        throws IOException {
 		MessagesLocation messagesLocation = Context.getService(CustomMessageService.class).getMessagesLocation(locationId);
 		if (messagesLocation != null) {
 			Collection<CustomMessage> customMessages = messagesLocation.getCustomMessages(LocaleUtility
 			        .fromSpecification(locale));
 			if (customMessages != null) {
 				response.setContentType("text/plain");
 				response.addHeader("Content-disposition",
 				    String.format("attachment; filename=%s_messages_%s.properties", locationId, locale));
 				for (CustomMessage customMessage : customMessages) {
 					if (StringUtils.isNotBlank(customMessage.getMessage())) {
 						response.getWriter().write(
 						    String.format("%s = %s%s", customMessage.getCode(), customMessage.getMessage(),
 						        System.getProperty("line.separator")));
 					}
 				}
 			}
 		} else {
 			throw new IllegalArgumentException("Unable to export customized messages by unknown location");
 		}
 	}
 	
 	/**
 	 * Exports multiple properties files into a ZIP file with each location-specific property file
 	 * under a folder with location ID as name for each specified locale. Both messages from
 	 * properties file and from database will be exported with this method
 	 * 
 	 * @param response the response instance to export ZIP file with
 	 * @param locales an array of locales to export messages for
 	 * @param locationIds an array of messages locations to export messages into
 	 * @throws IOException IOException if any I/O related error occurs
 	 */
 	private void exportZipWithAllMessages(HttpServletResponse response, String[] locales, String[] locationIds)
 	        throws IOException {
 		// prepare data model to be exported
 		List<LocationEntry> model = new ArrayList<CustomMessageController.LocationEntry>(locationIds.length);
 		CustomMessageService customMessageService = Context.getService(CustomMessageService.class);
 		Map<String, String> availableLocations = customMessageService.getAvailableMessagesLocationsMap();
 		for (int i = 0; i < locationIds.length; i++) {
 			LocationEntry locationEntry = new LocationEntry(locationIds[i]);
 			// create list of messages by locale for current location
 			List<MessagesByLanguage> messagesByLanguages = new ArrayList<CustomMessageController.MessagesByLanguage>();
 			for (int j = 0; j < locales.length; j++) {
 				MessagesByLanguage messagesByLanguage = new MessagesByLanguage(locales[j]);
 				CustomMessageSource cms = (CustomMessageSource) Context.getMessageSourceService().getActiveMessageSource();
 				cms.refreshCache();
 				PresentationMessageMap cacheMessages = cms.getCachedMessages().get(LocaleUtility.fromSpecification(messagesByLanguage.getLanguage()));
 				if (cacheMessages != null) {
 					Map<String, PresentationMessage> messageMap = new TreeMap<String, PresentationMessage>(cacheMessages);
 					for (String code : messageMap.keySet()) {
 						String prefix = StringUtils.substringBefore(code, ".");
 						// when passed in location is core
 						if (StringUtils.equals(locationEntry.getCode(), CustomMessageConstants.CUSTOM_MESSAGES_LOCATION_DEFAULT_ID)) {
 							// if message code prefix is not listed as key of available messages 
 							// locations this means that this message is related to core location
 							if (!availableLocations.containsKey(prefix)) {
 								PresentationMessage pm = messageMap.get(code);
 								if (pm != null && StringUtils.isNotBlank(pm.getMessage())) {
 									messagesByLanguage.getKeyValuePairs().put(code, pm.getMessage());
 								}
 							}
 						} else {
 							// if message code prefix is equal to passed in location id
 							if (StringUtils.equals(locationEntry.getCode(), prefix)) {
 								PresentationMessage pm = messageMap.get(code);
 								if (pm != null && StringUtils.isNotBlank(pm.getMessage())) {
 									messagesByLanguage.getKeyValuePairs().put(code, pm.getMessage());
 								}
 							}
 						}
 					}
 				}
 				messagesByLanguages.add(messagesByLanguage);
 			}
 			// set list of messages by languages into location entry
 			locationEntry.setMessagesByLanguage(messagesByLanguages);
 			// add current location entry into list
 			model.add(locationEntry);
 		}
 		// export this model as zip file
 		exportModelToZip(response, model);
 	}
 	
 	/**
 	 * Exports multiple properties files into a ZIP file with each location-specific property file
 	 * under a folder with location ID as name for each specified locale. Only customized messages
 	 * will be exported with this method
 	 * 
 	 * @param response the response instance to export ZIP file with
 	 * @param locales an array of locales to export messages for
 	 * @param locationIds an array of messages locations to export messages into
 	 * @throws IOException IOException if any I/O related error occurs
 	 */
 	private void exportZipWithOnlyCustomizedMessages(HttpServletResponse response, String[] locales, String[] locationIds)
 	        throws IOException {
 		// prepare data model to be exported
 		List<LocationEntry> model = new ArrayList<CustomMessageController.LocationEntry>(locationIds.length);
 		for (int i = 0; i < locationIds.length; i++) {
 			MessagesLocation messagesLocation = Context.getService(CustomMessageService.class).getMessagesLocation(
 				locationIds[i]);
 			if (messagesLocation != null) {
 				LocationEntry locationEntry = new LocationEntry(locationIds[i]);
 				// create list of messages by locale for current location
 				List<MessagesByLanguage> messagesByLanguages = new ArrayList<CustomMessageController.MessagesByLanguage>();
 				for (int j = 0; j < locales.length; j++) {
 					MessagesByLanguage messagesByLanguage = new MessagesByLanguage(locales[j]);
 					Collection<CustomMessage> customMessages = messagesLocation.getCustomMessages(LocaleUtility
 				        .fromSpecification(messagesByLanguage.getLanguage()));
 					for (CustomMessage customMessage : customMessages) {
 						if (StringUtils.isNotBlank(customMessage.getMessage())) {
 							messagesByLanguage.getKeyValuePairs().put(customMessage.getCode(), customMessage.getMessage());
 						}
 					}
 					messagesByLanguages.add(messagesByLanguage);
 				}
 				// set list of messages by languages into location entry
 				locationEntry.setMessagesByLanguage(messagesByLanguages);
 				// add current location entry into list
 				model.add(locationEntry);
 			} else {
 				throw new IllegalArgumentException("Unable to export customized messages by unknown location");
 			}
 		}
 		// export this model as zip file
 		exportModelToZip(response, model);
 	}
 	
 	/**
 	 * Exports given messages model to ZIP file with each location-specific property file under a
 	 * folder with location ID as name for each specified locale.
 	 * 
 	 * @param response the response object to be used for exporting zip file with
 	 * @param model the model to be exported
 	 * @throws IOException if any I/O related error occurs
 	 */
 	private void exportModelToZip(HttpServletResponse response, List<LocationEntry> model) throws IOException {
 		InMemoryZipStream zipStream = new InMemoryZipStream();
 		for (LocationEntry locationEntry : model) {
 			// create zip entry for each location id
 			zipStream.enterFolder(locationEntry.getCode());
 			// write properties files for each language
 			for (MessagesByLanguage languageEntry : locationEntry.getMessagesByLanguage()) {
 				// prepare bytes data to be added to zip stream
 				StringBuilder messagesData = new StringBuilder();
 				for (Map.Entry<String, String> messageEntry : languageEntry.getKeyValuePairs().entrySet()) {
 					messagesData.append(String.format("%s = %s%s", messageEntry.getKey(), messageEntry.getValue(),
 					    System.getProperty("line.separator")));
 				}
 				// write bytes of each messages property file for corresponding locale
 				zipStream.add(
 				    String.format("%s/messages_%s.properties", locationEntry.getCode(), languageEntry.getLanguage()),
 				    messagesData.toString().getBytes());
 			}
 			// close folder for current location id
 			zipStream.closeFolder();
 		}
 		// do not forget to close zip stream to avoid memory leak
 		zipStream.close();
 		// send the response back to the user / browser. The content for zip file type is "application/zip". 
 		// Also set the content disposition as attachment for the browser to show a dialog that will let user 
 		// choose what action will he do to the sent content.
 		ServletOutputStream sos = response.getOutputStream();
 		response.setContentType("application/zip");
 		response.addHeader(
 		    "Content-disposition",
 		    String.format("attachment; filename=messages-%s.zip",
 		        new SimpleDateFormat("yyyy-MM-dd_hh:mm").format(new Date())));
 		sos.write(zipStream.toByteArray());
 		sos.flush();
 	}
 	
 	/**
 	 * Inner class to be used for exporting complex model of messages
 	 */
 	class LocationEntry {
 		
 		/** The code of location entry to be used as folder name inside exported zip messages */
 		String code;
 		
 		/** List of message key/value pairs by language */
 		List<MessagesByLanguage> messagesByLanguage;
 		
 		/**
 		 * Creates new location entry using given fields
 		 * @param code the string to be used as code of new location entry
 		 */
         public LocationEntry(String code) {
 	        this.code = code;
         }
 		
 		/**
 		 * @return the code
 		 */
 		public String getCode() {
 			return code;
 		}
 		
 		/**
 		 * @param code the code to set
 		 */
 		public void setCode(String code) {
 			this.code = code;
 		}
 		
 		/**
 		 * @return the messagesByLanguage
 		 */
 		public List<MessagesByLanguage> getMessagesByLanguage() {
 			if (messagesByLanguage == null) {
 				messagesByLanguage = new ArrayList<CustomMessageController.MessagesByLanguage>();
 			}
 			return messagesByLanguage;
 		}
 		
 		/**
 		 * @param messagesByLanguage the messagesByLanguage to set
 		 */
 		public void setMessagesByLanguage(List<MessagesByLanguage> messagesByLanguage) {
 			this.messagesByLanguage = messagesByLanguage;
 		}
 		
 	}
 	
 	/**
 	 * Represents key/value pair of messages for certain locale
 	 */
 	class MessagesByLanguage {
 		
 		/** The locale of messages aggregated by this class */
 		String language;
 		
 		/** Messages key value pairs for locale set in this class field */
 		Map<String, String> keyValuePairs;
 		
 		/**
 		 * Creates new instance of this class with given language
 		 * @param language the language to be used as value for corresponding class field
 		 */
         public MessagesByLanguage(String language) {
         	this.language = language;
         }
 		
 		/**
 		 * @return the language
 		 */
 		public String getLanguage() {
 			return language;
 		}
 		
 		/**
 		 * @param language the language to set
 		 */
 		public void setLanguage(String language) {
 			this.language = language;
 		}
 		
 		/**
 		 * @return the keyValuePair
 		 */
 		public Map<String, String> getKeyValuePairs() {
 			if (keyValuePairs == null) {
 				keyValuePairs = new HashMap<String, String>();
 			}
 			return keyValuePairs;
 		}
 		
 		/**
 		 * @param keyValuePair the keyValuePair to set
 		 */
 		public void setKeyValuePairs(Map<String, String> keyValuePairs) {
 			this.keyValuePairs = keyValuePairs;
 		}
 		
 	}
 	
 }

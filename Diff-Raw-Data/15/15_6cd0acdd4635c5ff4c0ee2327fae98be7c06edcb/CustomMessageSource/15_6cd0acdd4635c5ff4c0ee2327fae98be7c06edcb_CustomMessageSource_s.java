 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.custommessage;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.GlobalPropertyListener;
 import org.openmrs.api.context.Context;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.messagesource.MutableMessageSource;
 import org.openmrs.messagesource.PresentationMessage;
 import org.openmrs.messagesource.PresentationMessageMap;
 import org.openmrs.module.custommessage.service.CustomMessageService;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.MessageSource;
 import org.springframework.context.support.AbstractMessageSource;
 
 /**
  * Registers the custom message source service
  */
 public class CustomMessageSource extends AbstractMessageSource implements MutableMessageSource, ApplicationContextAware, GlobalPropertyListener {
 	
 	protected static final Log log = LogFactory.getLog(CustomMessageSource.class);
 	private Map<Locale, PresentationMessageMap> cache = null;
 	private boolean showMessageCode = false;
 	
 	public static final String GLOBAL_PROPERTY_SHOW_MESSAGE_CODES = "custommessage.showMessageCodes";
 	
 	/**
 	 * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
 	 */
 	@Override
 	public void setApplicationContext(ApplicationContext context) throws BeansException {
 		MessageSourceService svc = (MessageSourceService)context.getBean("messageSourceServiceTarget");
 		MessageSource activeSource = svc.getActiveMessageSource();
 		setParentMessageSource(activeSource);
 		svc.setActiveMessageSource(this);
 	}
 	
 	/**
 	 * @return the cached messages, merged from the custom source and the parent source
 	 */
 	public synchronized Map<Locale, PresentationMessageMap> getCachedMessages() {
 		if (cache == null) {
 			refreshCache();
 		}
 		return cache;
 	}
 	
 	/**
 	 * @return all message codes defined in the system
 	 */
 	public Set<String> getAllMessageCodes() {
 		return getAllMessagesByCode().keySet();
 	}
 	
 	/**
 	 * @return a Map from code to Map of Locale string to message
 	 */
 	public Map<String, Map<Locale, PresentationMessage>> getAllMessagesByCode() {
 		Map<String, Map<Locale, PresentationMessage>> ret = new TreeMap<String, Map<Locale, PresentationMessage>>();
 		Map<Locale, PresentationMessageMap> m = getCachedMessages();
 		for (Locale locale : m.keySet()) {
 			PresentationMessageMap pmm = m.get(locale);
 			for (String code : pmm.keySet()) {
 				Map<Locale, PresentationMessage> messagesForCode = ret.get(code);
 				if (messagesForCode == null) {
 					messagesForCode = new LinkedHashMap<Locale, PresentationMessage>();
 					ret.put(code, messagesForCode);
 				}
 				messagesForCode.put(locale, pmm.get(code));
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	 * @param pm the presentation message to add to the cache
 	 * @param override if true, should override any existing message
 	 */
 	public void addPresentationMessageToCache(PresentationMessage pm, boolean override) {
 		PresentationMessageMap pmm = getCachedMessages().get(pm.getLocale());
 		if (pmm == null) {
 			pmm = new PresentationMessageMap(pm.getLocale());
 			getCachedMessages().put(pm.getLocale(), pmm);
 		}
 		if (pmm.get(pm.getCode()) == null || override) {
 			pmm.put(pm.getCode(), pm);
 		}
 	}
 	
 	/**
 	 * Refreshes the cache, merged from the custom source and the parent source
 	 */
 	public synchronized void refreshCache() {
 		cache = getCustomMessageService().getPresentationMessagesByLocale();
 		for (PresentationMessage pm : getMutableParentSource().getPresentations()) {
 			if (!pm.getCode().contains("<!--")) {
 				addPresentationMessageToCache(pm, false);
 			}
 		}
 		updateShowMessageCode();
 	}
 	
 	/**
 	 * Updates the showMessageCode variable based on the global property configuration
 	 */
 	public void updateShowMessageCode() {
 		showMessageCode = "true".equals(Context.getAdministrationService().getGlobalProperty(GLOBAL_PROPERTY_SHOW_MESSAGE_CODES, "false"));
 	}
 
 	/**
 	 * @see MutableMessageSource#getLocales()
 	 */
 	@Override
 	public Collection<Locale> getLocales() {
 		MutableMessageSource m = getMutableParentSource();
 		Set<Locale> s = new HashSet<Locale>(m.getLocales());
 		s.addAll(cache.keySet());
 		return s;
 	}
 
 	/**
 	 * @see MutableMessageSource#publishProperties(Properties, String, String, String, String)
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void publishProperties(Properties props, String locale, String namespace, String name, String version) {
 		getMutableParentSource().publishProperties(props, locale, namespace, name, version);
 	}
 
 	/**
 	 * @see MutableMessageSource#getPresentations()
 	 */
 	@Override
 	public Collection<PresentationMessage> getPresentations() {
 		Collection<PresentationMessage> ret = new ArrayList<PresentationMessage>();
 		for (PresentationMessageMap pmm : getCachedMessages().values()) {
 			ret.addAll(pmm.values());
 		}
 		return ret;
 	}
 
 	/**
 	 * @see MutableMessageSource#getPresentationsInLocale(Locale)
 	 */
 	@Override
 	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
 		PresentationMessageMap pmm = getCachedMessages().get(locale);
 		if (pmm == null) {
 			return new HashSet<PresentationMessage>();
 		}
 		return pmm.values();
 	}
 
 	/**
 	 * @see MutableMessageSource#addPresentation(PresentationMessage)
 	 */
 	@Override
 	public void addPresentation(PresentationMessage message) {
 		addPresentationMessageToCache(message, true);
 	}
 
 	/**
 	 * @see MutableMessageSource#getPresentation(String, Locale)
 	 */
 	@Override
 	public PresentationMessage getPresentation(String code, Locale locale) {
 		PresentationMessageMap pmm = getCachedMessages().get(locale);
 		if (pmm == null) {
 			return null;
 		}
 		return pmm.get(code);
 	}
 
 	/**
 	 * @see MutableMessageSource#removePresentation(PresentationMessage)
 	 */
 	@Override
 	public void removePresentation(PresentationMessage message) {
 		PresentationMessageMap pmm = getCachedMessages().get(message.getLocale());
 		if (pmm != null) {
 			pmm.remove(message.getCode());
 		}
 		getMutableParentSource().removePresentation(message);
 	}
 
 	/**
 	 * @see MutableMessageSource#merge(MutableMessageSource, boolean)
 	 */
 	@Override
 	public void merge(MutableMessageSource fromSource, boolean overwrite) {
 		getMutableParentSource().merge(fromSource, overwrite);
 	}
 
 	/**
 	 * @see AbstractMessageSource#resolveCode(String, Locale)
 	 */
 	@Override
 	protected MessageFormat resolveCode(String code, Locale locale) {
 		if (showMessageCode) {
 			return new MessageFormat(code);
 		}
 		PresentationMessage pm = getPresentation(code, locale); // Check exact match
 		if (pm == null) {
 			if (locale.getVariant() != null) {
 				pm = getPresentation(code, new Locale(locale.getLanguage(), locale.getCountry())); // Try to match language and country
 				if (pm == null) {
 					pm = getPresentation(code, new Locale(locale.getLanguage())); // Try to match language only
 				}
 			}
 		}
 		if (pm != null) {
 			return new MessageFormat(pm.getMessage());
 		}
 		return null;
 	}
	
 	/**
 	 * @see GlobalPropertyListener#supportsPropertyName(String)
 	 */
     public boolean supportsPropertyName(String property) {   	
 	    return property != null && property.equals(CustomMessageSource.GLOBAL_PROPERTY_SHOW_MESSAGE_CODES);
     }
 	
 	/**
 	 * @see GlobalPropertyListener#globalPropertyChanged(GlobalProperty)
 	 */
 	public void globalPropertyChanged(GlobalProperty property) {
 		if (property.getProperty() != null) {
 			if (property.getProperty().equals(CustomMessageSource.GLOBAL_PROPERTY_SHOW_MESSAGE_CODES)) {
 				updateShowMessageCode();
 			}
 		}
 	    
     }
 
 	/**
 	 * @see GlobalPropertyListener#globalPropertyDeleted(String)
 	 */
     public void globalPropertyDeleted(String property) {
     	// Do nothing
     }
 	
 	/**
 	 * Convenience method to get the parent message source as a MutableMessageSource
 	 */
 	public MutableMessageSource getMutableParentSource() {
 		return (MutableMessageSource) getParentMessageSource();
 	}
 	
 	/**
 	 * Convenience method to get the Custom Message Service
 	 */
 	public CustomMessageService getCustomMessageService() {
 		return Context.getService(CustomMessageService.class);
 	}
 }

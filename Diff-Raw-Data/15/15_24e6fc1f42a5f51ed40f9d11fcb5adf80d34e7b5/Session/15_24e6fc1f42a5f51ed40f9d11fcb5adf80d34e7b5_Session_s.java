 package de.cosmocode.palava;
 /*
 palava - a java-php-bridge
 Copyright (C) 2007  CosmoCode GmbH
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 import java.text.Collator;
 import java.text.NumberFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 public class Session implements Convertible {
 	
     public static final Logger log = Logger.getLogger(Session.class);
     
     private static final String LANGUAGE_KEY = "lang";
     private static final String COUNTRY_KEY = "country";
 
     private String id;
 
     private Date accessTime;
     private boolean valid = true;
     private ClientData clientData;
 
 	private Map<String,Object> data = new HashMap<String,Object>();
 	
 	private Locale locale;
 	private NumberFormat format;
 	private Collator collator;
 	
 	public Session(String id) {
 		this.id = id;
 	}
 
     public boolean isValid() {
         return valid;
     }
     
     public ClientData getClientData() {
         return clientData;
     }
     
     public void setClientData( ClientData data ) {
         clientData = data;
     }
     
     public boolean hasLocale() {
         return data.containsKey(LANGUAGE_KEY);
     }
     
     public Locale getLocale() {
         final Object langValue = data.get(LANGUAGE_KEY);
         if (locale == null || !locale.getLanguage().equals(langValue)) {
             
             format = null;
             collator = null;
             
             final Object countryValue = data.get(COUNTRY_KEY);
             
             if (langValue instanceof String && StringUtils.isNotBlank(String.class.cast(langValue))) {
                 if (countryValue instanceof String && StringUtils.isNotBlank(String.class.cast(countryValue))) {
                     locale = new Locale(String.class.cast(langValue), String.class.cast(countryValue)); 
                 } else {
                     locale = new Locale(String.class.cast(langValue));
                 }
             } else {
                 throw new IllegalStateException("No language found in session");
             }
         }
         return locale;
 	}
     
     public NumberFormat getNumberFormat() {
         final Locale locale = getLocale();
         return format == null ? format = NumberFormat.getInstance(locale) : format;
     }
     
     public Collator getCollator() {
         final Locale locale = getLocale();
         return collator == null ? collator = Collator.getInstance(locale) : collator;
     }
 
 	public void invalidate () {
         if (valid) {
             valid = false;
             for (Map.Entry<String, Object> entry : data.entrySet()) {
             	if (entry.getValue() instanceof Destroyable) {
             		try {
 						Destroyable.class.cast(entry.getValue()).destroy();
 					} catch (Exception e) {
                        log.error("cannot destroy session data " + entry.getKey());
 					}
             	}
             }
             data.clear();
             data = null;
         }
     }
 
 
     public void updateAccessTime() {
         accessTime = new Date();
     }
 
     public Date getAccessTime() {
         return accessTime;
     }
 
 	public String getSessionID() {
 		return id;
 	}
 
 	public void set(String key, Object value) {
 		data.put(key, value);
 	}
 	
 	public void putAll(Map<String,Object> map) {
 		data.putAll(map);
 	}
 
 	public Object get(String key) {
 		return data.get(key);
 	}
 
 	public Object remove(String key) {
 		return data.remove(key);
 	}
 
     public void convert( StringBuffer buf, ContentConverter converter ) throws ConversionException {
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("id", id);
         data.put("accesstime", accessTime);
         data.put("data", this.data);
         converter.convert(buf, data);
     }
     
     public static boolean hasCurrentSession() {
         return Worker.SESSION.get() != null;
     }
     
     public static Session getCurrentSession() {
     	final Session session = Worker.SESSION.get();
     	if (session == null) throw new IllegalStateException("no session found");
     	return session;
     }
 
 }

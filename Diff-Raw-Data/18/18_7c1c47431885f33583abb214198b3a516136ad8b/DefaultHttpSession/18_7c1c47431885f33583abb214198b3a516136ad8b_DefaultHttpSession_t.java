 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.bridge.session;
 
 import java.text.Collator;
 import java.text.NumberFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
 
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.palava.bridge.scope.Destroyable;
 
 /**
  * Default implementation of the {@link HttpSession} interface.
  *
  * @author Willi Schoenborn
  */
 final class DefaultHttpSession implements HttpSession {
     
     public static final Logger LOG = LoggerFactory.getLogger(DefaultHttpSession.class);
 
     private String sessionId;
 
     private long accessTime;
 
     private final Map<Object, Object> context = Maps.newHashMap();
     
     private Locale locale;
     
     private NumberFormat format;
     
     private Collator collator;
     
     public DefaultHttpSession(String sessionId) {
         this.sessionId = Preconditions.checkNotNull(sessionId, "SessionId");
     }
 
     @Override
     public Locale getLocale() {
         final Object langValue = context.get(LANGUAGE);
         if (locale == null || !locale.getLanguage().equals(langValue)) {
             
             format = null;
             collator = null;
             
             final Object countryValue = context.get(COUNTRY);
             
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
 
     @Override
     public NumberFormat getNumberFormat() {
         if (format == null) {
             format = NumberFormat.getInstance(getLocale());
         }
         return format;
     }
 
     @Override
     public Collator getCollator() {
         if (collator == null) {
             collator = Collator.getInstance(getLocale());
         }
         return collator;
     }
 
     @Override
     public void updateAccessTime() {
         accessTime = System.currentTimeMillis();
     }
 
     @Override
     public Date getAccessTime() {
         return new Date(accessTime);
     }
 
     @Override
     public String getSessionId() {
         return sessionId;
     }
 
     @Override
     public <K> boolean contains(K key) {
         return context.containsKey(key);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <K, V> V get(K key) {
         return (V) context.get(key);
     }
 
     @Override
     public <K, V> void putAll(Map<? extends K, ? extends V> map) {
         context.putAll(map);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <K, V> V remove(K key) {
         return (V) context.remove(key);
     }
 
     @Override
     public <K, V> void set(K key, V value) {
         context.put(key, value);
     }
 
     @Override
    public UnmodifiableIterator<Entry<Object, Object>> iterator() {
        return Iterators.unmodifiableIterator(context.entrySet().iterator());
    }
    
    @Override
     public void destroy() {
         final Iterable<Destroyable> destroyables = Iterables.filter(context.values(), Destroyable.class);
         for (Destroyable destroyable : destroyables) {
             LOG.debug("Destryoing {} in session", destroyable);
             destroyable.destroy(); 
         }
         context.clear();
     }
 
     @Override
     public JSONRenderer renderAsMap(JSONRenderer renderer) {
         return renderer.
             key("id").value(sessionId).
             key("accesstime").value(accessTime).
             key("data").object(context);
     }
 
 }

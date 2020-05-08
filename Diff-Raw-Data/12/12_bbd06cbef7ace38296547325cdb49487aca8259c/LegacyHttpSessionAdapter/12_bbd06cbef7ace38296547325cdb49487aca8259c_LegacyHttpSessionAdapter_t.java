 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.ipc.legacy;
 
 import java.text.Collator;
 import java.text.NumberFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.base.Preconditions;
 
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.palava.bridge.session.HttpSession;
 import de.cosmocode.palava.ipc.IpcSession;
 
 /**
  * Adapter for {@link IpcSession} to {@link HttpSession}.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 final class LegacyHttpSessionAdapter implements HttpSession {
 
     private final IpcSession session;
 
     private transient Locale locale;
 
     private transient NumberFormat format;
 
     private transient Collator collator;
     
     public LegacyHttpSessionAdapter(IpcSession session) {
         this.session = Preconditions.checkNotNull(session, "Session");
     }
     
     @Override
     public String getIdentifier() {
         return session.getIdentifier();
     }
 
     @Override
     public String getSessionId() {
         return session.getSessionId();
     }
 
     @Override
     public long getTimeout(TimeUnit unit) {
         return session.getTimeout(unit);
     }
 
     @Override
     public boolean isExpired() {
         return session.isExpired();
     }
 
     @Override
     public Date lastAccessTime() {
         return session.lastAccessTime();
     }
 
     @Override
     public void setTimeout(long timeout, TimeUnit unit) {
         session.setTimeout(timeout, unit);
     }
 
     @Override
     public Date startedAt() {
         return session.startedAt();
     }
 
     @Override
     public void touch() {
         session.touch();
     }
 
     @Override
     public Date getAccessTime() {
         return lastAccessTime();
     }
 
     @Override
     public Locale getLocale() {
         touch();
         final Object langValue = get(LANGUAGE);
         if (locale == null || !locale.getLanguage().equals(langValue)) {
             
             format = null;
             collator = null;
             
             final Object countryValue = get(COUNTRY);
             
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
         touch();
         if (format == null) {
             format = NumberFormat.getInstance(getLocale());
         }
         return format;
     }
 
     @Override
     public Collator getCollator() {
         touch();
         if (collator == null) {
             collator = Collator.getInstance(getLocale());
         }
         return collator;
     }
 
     @Override
     public void updateAccessTime() {
         session.touch();
     }
 
     @Override
     public void clear() {
         session.clear();
     }
 
     @Override
     public <K> boolean contains(K key) {
         return session.contains(key);
     }
 
     @Override
     public <K, V> V get(K key) {
        return session.<K, V>get(key);
     }
 
     @Override
     public Iterator<Entry<Object, Object>> iterator() {
         return session.iterator();
     }
 
     @Override
     public <K, V> void putAll(Map<? extends K, ? extends V> map) {
         session.putAll(map);
     }
 
     @Override
     public <K, V> V remove(K key) {
        return session.<K, V>remove(key);
     }
 
     @Override
     public <K, V> void set(K key, V value) {
         session.set(key, value);
     }
 
     @Override
     public JSONRenderer renderAsMap(JSONRenderer renderer) {
         renderer.
             key("id").value(getSessionId()).
             key("accesstime").value(lastAccessTime()).
             key("data").object();
         
         for (Entry<Object, Object> entry : session) {
             renderer.key(entry.getKey()).value(entry.getValue());
         }
         
         renderer.
             endObject();
         
         return renderer;
     }
 
 }

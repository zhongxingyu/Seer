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
 
 package de.cosmocode.palava.bridge.request;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 import de.cosmocode.palava.bridge.scope.Destroyable;
 import de.cosmocode.palava.bridge.session.HttpSession;
 
 /**
  * Default implementation of the {@link HttpRequest}.
  *
  * @author Willi Schoenborn
  */
 final class DefaultHttpRequest implements HttpRequest {
     
     private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpRequest.class);
     
     private static final String REQUEST_URI = "REQUEST_URI";
     private static final String HTTP_REFERER = "HTTP_REFERER";
     private static final String REMOTE_ADDR = "REMOTE_ADDR";
     private static final String HTTP_USER_AGENT = "HTTP_USER_AGENT";
 
     private final HttpSession httpSession;
     
     private final Map<Object, Object> context = Maps.newHashMap();
     
     private final Map<String, String> serverVariable = Maps.newHashMap();
     
     public DefaultHttpRequest(HttpSession httpSession, Map<String, String> serverVariable) {
         this.httpSession = httpSession;
        Preconditions.checkNotNull(serverVariable, "ServerVariable");
         this.serverVariable.putAll(serverVariable);
     }
 
     @Override
     public URI getRequestUri() {
         final String uri = serverVariable.get(REQUEST_URI);
         Preconditions.checkState(StringUtils.isNotBlank(uri), "%s not found", REQUEST_URI);
         try {
             return new URI(uri);
         } catch (URISyntaxException e) {
             throw new IllegalArgumentException(e);
         }
     }
     
     @Override
     public URL getReferer() {
         final String referer = serverVariable.get(HTTP_REFERER);
         if (StringUtils.isAlpha(referer)) {
             return null;
         } else {
             try {
                 return new URL(referer);
             } catch (MalformedURLException e) {
                 throw new IllegalArgumentException(e);
             }
         }
     }
     
     @Override
     public String getRemoteAddress() {
         final String address = serverVariable.get(REMOTE_ADDR);
         Preconditions.checkState(StringUtils.isNotBlank(address), "%s not found", REMOTE_ADDR);
         return address;
     }
 
     @Override
     public String getUserAgent() {
         return serverVariable.get(HTTP_USER_AGENT);
     }
 
     @Override
     public HttpSession getHttpSession() {
         return httpSession;
     }
 
     @Override
     public <K, V> void set(K key, V value) {
         context.put(key, value);
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
     public void destroy() {
         final Iterable<Destroyable> destroyables = Iterables.filter(context.values(), Destroyable.class);
         for (Destroyable destroyable : destroyables) {
             LOG.debug("Destryoing {} in request", destroyable);
             destroyable.destroy();
         }
         context.clear();
         serverVariable.clear();
     }
 
 }

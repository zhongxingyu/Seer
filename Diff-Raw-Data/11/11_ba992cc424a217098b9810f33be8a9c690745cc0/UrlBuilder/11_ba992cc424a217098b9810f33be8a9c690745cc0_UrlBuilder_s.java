 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.render.html.support;
 
 import java.net.URI;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * @author manhole
  */
 public class UrlBuilder {
 
     private String base;
 
     private final Map urlParameters = new LinkedHashMap() {
 
         private static final long serialVersionUID = 1L;
 
         public Object get(Object key) {
             Object value = super.get(key);
             if (value != null) {
                 return value;
             }
             UrlParameter p = new UrlParameter();
             p.setKey((String) key);
             put(key, p);
             return p;
         }
     };
 
     public void setBase(final String base) {
         this.base = base;
     }
 
     public String build() {
         final StringBuffer sb = new StringBuffer(100);
         final URI uri = URI.create(base);
         if (uri.getScheme() != null) {
             sb.append(uri.getScheme());
            sb.append(":");
            if (-1 < uri.getPort()) {
                sb.append(uri.getPort());
            }
            sb.append("//");
         }
         if (uri.getHost() != null) {
             sb.append(uri.getHost());
         }
 
         if (uri.getPath() != null) {
             sb.append(uri.getPath());
         }
         boolean questionAppeared = false;
         if (uri.getQuery() != null) {
             questionAppeared = true;
             sb.append('?');
             sb.append(uri.getQuery());
         }
         for (final Iterator it = urlParameters.entrySet().iterator(); it
                 .hasNext();) {
             final Map.Entry entry = (Map.Entry) it.next();
             final String key = (String) entry.getKey();
             final UrlParameter parameter = (UrlParameter) entry.getValue();
             final String[] values = parameter.getValues();
             for (int i = 0; i < values.length; i++) {
                 final String value = values[i];
                 if (questionAppeared) {
                     sb.append('&');
                 } else {
                     sb.append('?');
                     questionAppeared = true;
                 }
                 sb.append(key);
                 sb.append('=');
                 sb.append(value);
             }
         }
         if (uri.getFragment() != null) {
             sb.append('#');
             sb.append(uri.getFragment());
         }
         return new String(sb);
     }
 
     public void add(final String key, final String value) {
         final UrlParameter p = (UrlParameter) urlParameters.get(key);
         p.addValue(value);
     }
 
 }

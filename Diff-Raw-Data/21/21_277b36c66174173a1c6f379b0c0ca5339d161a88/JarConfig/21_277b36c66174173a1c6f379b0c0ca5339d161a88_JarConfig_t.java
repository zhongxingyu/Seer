 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
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
 package net.oneandone.lavender.modules;
 
 import net.oneandone.sushi.xml.Selector;
 import net.oneandone.sushi.xml.Xml;
 import net.oneandone.sushi.xml.XmlException;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.FilterInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * META-INF/pustefix-module.xml.
  */
 public class JarConfig {
     private static final String MODULES = "modules/";
     private static final String PUSTEFIX_INF = "PUSTEFIX-INF/";
 
     private final WarConfig global;
     private final String name;
 
     /** trimmed, without heading slash, with tailing slash */
     private final List<String> statics;
 
     public static JarConfig load(Xml xml, WarConfig parent, InputStream src) throws IOException, SAXException, XmlException {
         String path;
         Element root;
         Selector selector;
         String name;
         List<String> statics;
 
         root = xml.getBuilder().parse(doNotClose(src)).getDocumentElement();
         selector = xml.getSelector();
         name = selector.string(root, "module-name");
         statics = new ArrayList<>();
         for (Element element : selector.elements(root, "static/path")) {
             path = element.getTextContent();
             path = path.trim();
             if (path.isEmpty() || path.startsWith("/")) {
                 throw new IllegalStateException(path);
             }
             if (!path.endsWith("/")) {
                 path = path + "/";
             }
             statics.add(path);
         }
         return new JarConfig(parent, name, statics);
     }
 
     public JarConfig(WarConfig global, String name, List<String> statics) {
         this.global = global;
         this.name = name;
         this.statics = statics;
     }
 
     public String getModuleName() {
         return name;
     }
 
     public List<String> getStatics() {
         return statics;
     }
 
     public String getResourcePathPrefix() {
         return MODULES + getModuleName() + "/";
     }
 
     /* @return null if not public */
    public String getPath(String resourceName) {
        if (resourceName.startsWith("/")) {
            throw new IllegalArgumentException(resourceName);
         }
        if (resourceName.startsWith(PUSTEFIX_INF)) {
            resourceName = resourceName.substring(PUSTEFIX_INF.length());

            // statics is relative to PUSTEFIX-INF - so it's not checked, when the resource name does not start with it:
             for (String path : statics) {
                 if (resourceName.startsWith(path)) {
                     return resourceName;
                 }
             }
         }
        // note that PUSTEFIX-INF has been stripped from the beginning
        if (global.isPublicResource(resourceName)) {
            return resourceName;
         }
         return null;
     }
 
     //--
 
     public static InputStream doNotClose(InputStream dest) {
         return new FilterInputStream(dest) {
             @Override
             public void close() {
                 // do nothing
             }
         };
     }
 }

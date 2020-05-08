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
 package net.oneandone.maven.plugins.prerelease.util;
 
import net.oneandone.sushi.fs.FileNotFoundException;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.xml.Selector;
 import net.oneandone.sushi.xml.XmlException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import javax.xml.namespace.NamespaceContext;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 
 /** Represents the changes.xml file. */
 
 public class ChangesXml {
     private static final String NAMESPACE_URI = "http://maven.apache.org/changes/1.0.0";
     public static final String PATH = "src/changes/changes.xml";
 
     private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
    public static ChangesXml load(FileNode basedir) throws IOException {
        FileNode file;
 
        file = basedir.join(PATH);
        try {
            return new ChangesXml(file, file.readXml());
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException | SAXException e) {
            throw new IOException(file.getPath() + ": cannot load changes.xml: " + e.getMessage(), e);
        }
     }
 
     private final FileNode dest;
     private final Document doc;
     private final Selector selector;
 
     public ChangesXml(FileNode dest, Document doc) {
         this.dest = dest;
         this.doc = doc;
         this.selector = dest.getWorld().getXml().getSelector();
         this.selector.setNamespaceContext(new NamespaceContext() {
             @Override
             public String getNamespaceURI(String prefix) {
                 if ("a".equals(prefix)) {
                     return NAMESPACE_URI;
                 }
                 return null;
             }
 
             @Override
             public String getPrefix(String namespaceURI) {
                 throw new UnsupportedOperationException();
             }
 
             @Override
             public Iterator getPrefixes(String namespaceURI) {
                 throw new UnsupportedOperationException();
             }
         });
     }
 
     public void releaseDate(String version, Date date) throws XmlException {
         Element release;
         Element body;
 
         release = selector.elementOpt(doc, "/a:document/a:body/a:release[@version='" + version + "']");
         if (release == null) {
             release = doc.createElementNS(NAMESPACE_URI, "release");
             release.setAttribute("version", version);
             body = selector.element(doc, "/a:document/a:body");
             body.insertBefore(release, body.getFirstChild());
         }
         release.setAttribute("date", FORMAT.format(date));
     }
 
     public void save() throws IOException {
         dest.writeXml(doc);
     }
 }

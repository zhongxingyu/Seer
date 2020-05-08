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
 
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.xml.Selector;
 import net.oneandone.sushi.xml.XmlException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.SAXException;
 
 import javax.xml.namespace.NamespaceContext;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Iterator;
 
 /** Transforms via DOM - transforming the model would loose all formatting. Maven's Release Plugin follows the same approach. */
 public final class Transform {
     /** @param tag null to skip scm transformation */
     public static void adjustPom(FileNode pom, String expectedVersion, String nextVersion, String svnurl, String tag)
             throws IOException, MojoExecutionException, SAXException, XmlException {
         Document document;
         Element element;
         Selector selector;
 
         document = pom.readXml();
         selector = new Selector();
         selector.setNamespaceContext(new NamespaceContext() {
             @Override
             public String getNamespaceURI(String prefix) {
                 if (!prefix.equals("M")) {
                     throw new IllegalStateException("prefix: " + prefix);
                 }
                 return "http://maven.apache.org/POM/4.0.0";
             }
 
             @Override
             public String getPrefix(String namespaceURI) {
                 throw new IllegalStateException();
             }
 
             @Override
             public Iterator getPrefixes(String namespaceURI) {
                 throw new IllegalStateException();
             }
         });
         element = selector.element(document, "/M:project/M:version");
         if (!expectedVersion.equals(element.getTextContent().trim())) {
             throw new MojoExecutionException("expected version " + expectedVersion + ", got " + element.getTextContent().trim());
         }
         element.setTextContent(nextVersion);
         if (tag != null) {
             scm(selector.elementOpt(document, "/M:project/M:scm/M:connection"), svnurl, tag);
             scm(selector.elementOpt(document, "/M:project/M:scm/M:developerConnection"), svnurl, tag);
             scm(selector.elementOpt(document, "/M:project/M:scm/M:url"), svnurl, tag);
         }
         writeRaw(document, pom);
     }
 
     public static void writeRaw(Document document, FileNode file) throws IOException {
         DOMImplementationLS ls;
         LSSerializer serializer;
         LSOutput output;
         OutputStream stream;
 
         ls = (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");
         serializer = ls.createLSSerializer();
         output = ls.createLSOutput();
         stream = file.createOutputStream();
         output.setByteStream(stream);
         serializer.write(document, output);
         stream.write('\n');
         stream.close();
     }
 
     private static void scm(Element element, String svnurl, String tag) throws MojoExecutionException {
         String old;
 
         if (element == null) {
             return;
         }
         old = element.getTextContent();
         element.setTextContent(adjustScm(old, svnurl, tag));
     }
 
     public static String adjustScm(String origStr, String svnurl, String tag) throws MojoExecutionException {
         String str;
 
        if ("scm:svn:${project.svn.url}".equals(origStr)) {
             // do not touch - implied by scm url.
             return origStr;
         }
         str = origStr;
         if (str.endsWith("/")) {
             str = str.substring(0, str.length() - 1);
         }
         str = str.trim();
         if (!str.endsWith(svnurl)) {
             throw new MojoExecutionException("unexpected scm entry: " + origStr);
         }
         return str.substring(0, str.length() - svnurl.length()) + tag;
     }
 
     private Transform() {
     }
 }

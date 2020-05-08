 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.richfaces.build.shade.resource;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 
 import org.apache.maven.plugins.shade.resource.ResourceTransformer;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.jdom.xpath.XPath;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * @author Nick Belaevski
  * 
  */
 public abstract class BaseFacesResourceTransformer implements ResourceTransformer {
 
     protected static final String META_INF_PATH = "META-INF/";
 
     protected static final String JAVAEE_PREFIX = "javaee";
 
     protected static final String JAVAEE_URI = "http://java.sun.com/xml/ns/javaee";
 
     private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
     
     private static final String XSI_PREFIX = "xsi";
     
     protected NamespacesTracker namespacesFactory = new NamespacesTracker();
     
     protected static XPath createXPath(String path) throws JDOMException {
         XPath xPath = XPath.newInstance(path);
         xPath.addNamespace(Namespace.getNamespace(JAVAEE_PREFIX, JAVAEE_URI));
         xPath.addNamespace(Namespace.getNamespace(XSI_PREFIX, XSI_URI));
 
         return xPath;
     }
 
     protected Namespace getJavaEENamespace() {
         return namespacesFactory.getNamespace(JAVAEE_URI, null);
     }
     
     protected void addSchemaLocation(Element element, String schemaLocation) {
         if (schemaLocation != null && schemaLocation.length() != 0) {
             Namespace xsiNamespace = namespacesFactory.getNamespace(XSI_URI, XSI_PREFIX);
             element.setAttribute("schemaLocation", JAVAEE_URI + " " + schemaLocation, xsiNamespace);
         }
     }
     
     private void updateNamespaceRecursively(Object object) {
         if (object instanceof Element) {
             Element element = (Element) object;
 
             element.setNamespace(namespacesFactory.getNamespace(element.getNamespace()));
 
             for (Object attributeObject : element.getAttributes()) {
                 Attribute attribute = (Attribute) attributeObject;
 
                 if (!Namespace.NO_NAMESPACE.equals(attribute.getNamespace())) {
                     attribute.setNamespace(namespacesFactory.getNamespace(attribute.getNamespace()));
                 }
             }
 
             for (Object child : element.getChildren()) {
                 updateNamespaceRecursively(child);
             }
         }
     }
 
     protected boolean isJavaEEOrDefaultNamespace(Element element) {
         String namespaceURI = element.getNamespaceURI();
         if (namespaceURI == null || namespaceURI.trim().length() == 0) {
             return true;
         }
         
         return JAVAEE_URI.equals(namespaceURI);
     }
     
     protected Element cloneAndImportElement(Element element) {
         Element clonedElement = (Element) element.clone();
         updateNamespaceRecursively(clonedElement);
         return clonedElement;
     }
 
     protected List<Element> cloneAndImportElements(List<Element> elements) {
         List<Element> result = new ArrayList<Element>(elements.size());
         for (Element element : elements) {
             result.add(cloneAndImportElement(element));
         }
         
         return result;
     }
     
     protected void appendToStream(String resourceName, Document document, JarOutputStream jos) throws IOException {
         jos.putNextEntry(new JarEntry(resourceName));
         Format prettyFormat = Format.getPrettyFormat();
         prettyFormat.setIndent("    ");
 
         Element rootElement = document.getRootElement();
         Collection<Namespace> namespaces = namespacesFactory.getNamespaces();
         for (Namespace namespace : namespaces) {
             if (namespace.getPrefix().length() == 0) {
                 continue;
             }
            
             rootElement.addNamespaceDeclaration(namespace);
         }
        
         new XMLOutputter(prettyFormat).output(document, jos);
     }
 
     protected abstract void processDocument(String resource, Document document, List relocators) throws JDOMException;
 
     protected void resetTransformer() {
         namespacesFactory = new NamespacesTracker();
     }
    
     protected String getMetaInfResourceName(String resource) {
         if (!resource.startsWith(META_INF_PATH)) {
             return null;
         }
         
         String subPath = resource.substring(META_INF_PATH.length());
         if (subPath.contains("/")) {
             return null;
         }
         
         return subPath;
     }
     
     @SuppressWarnings("unchecked")
     protected <T> List<T> checkedList(List<?> list, Class<T> clazz) {
         for (Object o: list) {
             if (!clazz.isInstance(o)) {
                 throw new ClassCastException(o.toString());
             }
         }
         
         return (List<T>) list;
     }
     
     public void processResource(String resource, InputStream is, List relocators) throws IOException {
         try {
             SAXBuilder builder = new SAXBuilder(false);
             builder.setExpandEntities(false);
             //TODO nick - namespace aware?
             builder.setEntityResolver(new EntityResolver() {
 
                 public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                     return new InputSource(new StringReader(""));
                 }
             });
             Document document = builder.build(is);
             processDocument(resource, document, relocators);
         } catch (JDOMException e) {
             throw new RuntimeException(e.getMessage(), e);
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 // TODO: handle exception
             }
         }
     }
 
 }

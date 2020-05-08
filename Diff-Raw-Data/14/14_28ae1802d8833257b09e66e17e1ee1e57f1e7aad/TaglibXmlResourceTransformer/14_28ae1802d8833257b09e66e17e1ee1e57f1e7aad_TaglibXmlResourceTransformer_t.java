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
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.components.io.fileselectors.FileInfo;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.filter.ElementFilter;
 import org.jdom.filter.Filter;
 import org.jdom.xpath.XPath;
 
 /**
  * @author Nick Belaevski
  */
 public class TaglibXmlResourceTransformer extends BaseFacesResourceTransformer {
 
     private static final String ID = "id";
 
     private static final String CURRENT_VERSION = "2.0";
 
     private static final String VERSION = "version";
 
     private static final String NAMESPACE = "namespace";
 
     private static final String FUNCTION = "function";
 
     private static final String TAG = "tag";
 
     private static final String FUNCTION_NAME = "function-name";
 
     private static final String TAG_NAME = "tag-name";
 
     private static final String FACELET_TAGLIB = "facelet-taglib";
 
     private static final String TAGLIB_XML_FILE_EXTENSION = ".taglib.xml";
 
     private static final String NAMESPACE_EXPRESSION = MessageFormat.format("/{0}:{1}/{0}:{2}|/{1}/{2}", JAVAEE_PREFIX,
             FACELET_TAGLIB, NAMESPACE);
 
     private final Map<String, List<Document>> tagLibraries = new HashMap<String, List<Document>>();
    
    // ensures that each library will be processed only once
    private final Set<String> tagLibrariesProcessed = new HashSet<String>();
 
     private final Map<String, Document> passThroughLibraries = new HashMap<String, Document>();
 
     private Taglib[] taglibs = new Taglib[0];
 
     private Comparator<Element> createElementsComparator() throws JDOMException {
         List<String> elements = Arrays.asList("description", "display-name", "icon", "library-class", NAMESPACE,
                 "composite-library-name", TAG, FUNCTION, "taglib-extension");
 
         Map<String, XPath> elementNameExpressions = new HashMap<String, XPath>();
         String tagPathExpr = MessageFormat.format("./{0}:{1}|./{1}", JAVAEE_PREFIX, TAG_NAME);
         elementNameExpressions.put(TAG, createXPath(tagPathExpr));
 
         String fnPathExpr = MessageFormat.format("./{0}:{1}|./{1}", JAVAEE_PREFIX, FUNCTION_NAME);
         elementNameExpressions.put(FUNCTION, createXPath(fnPathExpr));
 
         return new ElementsComparator(JAVAEE_URI, elements, elementNameExpressions);
     }
 
     private String getShortName(final String namespaceUri) {
         int idx = namespaceUri.lastIndexOf('/');
         if (idx < 0) {
             return namespaceUri;
         } else {
             return namespaceUri.substring(idx + 1);
         }
     }
 
     private String getFileName(final String shortName) {
         return META_INF_PATH + shortName + TAGLIB_XML_FILE_EXTENSION;
     }
 
     private void checkRootElement(final Element element) {
         if (!FACELET_TAGLIB.equals(element.getName())) {
             throw new IllegalArgumentException("Root element name: " + element.getName());
         }
 
         if (!isJavaEEOrDefaultNamespace(element)) {
             throw new IllegalArgumentException("Root element namespace: " + element.getNamespaceURI());
         }
     }
 
     public boolean hasTransformedResource() {
         return !tagLibraries.isEmpty() || !passThroughLibraries.isEmpty();
     }
 
     @Override
     protected void writeMergedConfigFiles(final Archiver archiver) throws ArchiverException {
         try {
             for (Map.Entry<String, Document> entry : passThroughLibraries.entrySet()) {
                 String resourceName = entry.getKey();
                 Document document = entry.getValue();
 
                 addToArchive(resourceName, document, archiver);
             }
 
             if (!tagLibraries.isEmpty()) {
                 Comparator<Element> elementsComparator;
                 try {
                     elementsComparator = createElementsComparator();
                 } catch (JDOMException e) {
                     throw new RuntimeException(e.getMessage(), e);
                 }
 
                 Namespace javaEENamespace = getJavaEENamespace();
                 Filter filter = new ElementFilter().and(new ElementFilter(NAMESPACE, javaEENamespace).negate());
 
                 for (Map.Entry<String, List<Document>> entry : tagLibraries.entrySet()) {
                     String namespaceUri = entry.getKey();
                     String shortName = getShortName(namespaceUri);
                     List<Document> sourceDocuments = entry.getValue();
 
                     Document document = new org.jdom.Document();
 
                     Element rootElement = new Element(FACELET_TAGLIB, javaEENamespace);
                     rootElement.setAttribute(VERSION, CURRENT_VERSION);
                     addSchemaLocation(rootElement, "http://java.sun.com/xml/ns/javaee/web-facelettaglibrary_2_0.xsd");
                     rootElement.setAttribute(ID, shortName);
 
                     document.addContent(rootElement);
 
                     List<Element> elements = new ArrayList<Element>();
 
                     Element nsElement = new Element(NAMESPACE, javaEENamespace);
                     nsElement.setText(namespaceUri);
                     elements.add(nsElement);
 
                     for (Document sourceDocument : sourceDocuments) {
                         Element sourceRootElement = sourceDocument.getRootElement();
                         checkRootElement(sourceRootElement);
 
                         List<Element> tagsContent = checkedList(sourceRootElement.getContent(filter), Element.class);
                         for (Element tagElement : tagsContent) {
                             Element clonedElement = cloneAndImportElement(tagElement);
                             elements.add(clonedElement);
                         }
                     }
 
                     Collections.sort(elements, elementsComparator);
                     rootElement.addContent(elements);
 
                     String fileName = getFileName(shortName);
                     addToArchive(fileName, document, archiver);
                 }
             }
         } finally {
             resetTransformer();
         }
     }
 
     @Override
     protected void resetTransformer() {
         super.resetTransformer();
         passThroughLibraries.clear();
         tagLibraries.clear();
     }
 
     @Override
     protected void processDocument(final String resource, final Document document) throws JDOMException {
         String namespaceUri = createXPath(NAMESPACE_EXPRESSION).valueOf(document);
         if (namespaceUri == null || namespaceUri.length() == 0) {
             passThroughLibraries.put(resource, document);
         } else {
            if (tagLibrariesProcessed.contains(namespaceUri)) {
                return;
            } else {
                tagLibrariesProcessed.add(namespaceUri);
            }
            
             for (Taglib taglib : taglibs) {
                 if (taglib.matches(namespaceUri)) {
                     namespaceUri = taglib.getTargetNamespace();
                     break;
                 }
             }
 
             List<Document> documents = tagLibraries.get(namespaceUri);
             if (documents == null) {
                 documents = new ArrayList<Document>();
                 tagLibraries.put(namespaceUri, documents);
             }
 
             documents.add(document);
         }
     }
 
     public Taglib[] getTaglibs() {
         return taglibs;
     }
 
     public void setTaglibs(final Taglib[] taglibs) {
         this.taglibs = taglibs;
     }
 
     @SuppressWarnings("rawtypes")
     @Override
     public List getVirtualFiles() {
         List<String> result = new ArrayList<String>();
 
         for (Map.Entry<String, Document> entry : passThroughLibraries.entrySet()) {
             String resourceName = entry.getKey();
             result.add(resourceName);
         }
 
         if (!tagLibraries.isEmpty()) {
             for (Map.Entry<String, List<Document>> entry : tagLibraries.entrySet()) {
                 String namespaceUri = entry.getKey();
                 String shortName = getShortName(namespaceUri);
                 result.add(getFileName(shortName));
             }
         }
 
         return result;
     }
 
     @Override
     protected boolean isHandled(final FileInfo fileInfo) {
         if (!fileInfo.isFile()) {
             return false;
         }
 
         String name = getMetaInfResourceName(fileInfo.getName());
         return name != null && name.endsWith(TAGLIB_XML_FILE_EXTENSION);
     }
 }

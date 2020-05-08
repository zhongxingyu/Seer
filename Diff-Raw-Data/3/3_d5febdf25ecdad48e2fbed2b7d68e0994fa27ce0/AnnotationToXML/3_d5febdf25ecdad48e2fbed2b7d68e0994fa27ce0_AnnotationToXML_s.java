 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.tools.text.transform;
 
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.components.text.datatype.corpora.Annotation;
 import org.seasr.components.text.datatype.corpora.AnnotationConstants;
 import org.seasr.components.text.datatype.corpora.AnnotationSet;
 import org.seasr.components.text.datatype.corpora.Document;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.io.DOMUtils;
 import org.seasr.meandre.support.text.XMLUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Lily Dong
  * @author Loretta Auvil
  * @author Boris Capitanu
  *
  */
 
 @Component(
         creator = "Lily Dong",
 		description = "<p>Overview: <br> This component extracts the " +
 		              "annotations from an annotated text document and outputs them " +
 		              "as xml document. Only those entity types specified in this component's " +
 		              "properties will be included in the output XML doucment.</p>",
         name = "Annotation To XML",
         tags = "text, document, annotation",
         rights = Licenses.UofINCSA,
         baseURL="meandre://seasr.org/components/tools/",
         dependency = {"protobuf-java-2.0.3.jar"}
 )
 
 public class AnnotationToXML extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 	        description = "Input document to be read.",
 	        name = Names.PORT_DOCUMENT
 	)
 	protected static final String IN_DOCUMENT = Names.PORT_DOCUMENT;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 	        description = "Extracted annotations as XML document." +
 	                      "<br>TYPE: org.w3c.dom.Document",
 	        name = Names.PORT_XML
 	)
 	protected static final String OUT_XML_ANNOTATIONS = Names.PORT_XML;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 	        description = "Entity types (comma delimited list).",
             name = Names.PROP_ENTITIES,
             defaultValue =  "person,organization,location,time,money,percentage,date"
 	)
 	protected static final String PROP_ENTITIES = Names.PROP_ENTITIES;
 
     //--------------------------------------------------------------------------------------------
 
 
 	private String _entities;
 	private DocumentBuilder _docBuilder;
 	private Properties _xmlProperties;
 	private Vector<org.w3c.dom.Document> _simileDocs = new Vector<org.w3c.dom.Document>();
 	private boolean _gotInitiator;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _entities = ccp.getProperty(PROP_ENTITIES);
 
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         dbfac.setNamespaceAware(true);
         _docBuilder = dbfac.newDocumentBuilder();
 
         _xmlProperties = new Properties();
         _xmlProperties.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
         _xmlProperties.put(OutputKeys.INDENT, "yes");
         _xmlProperties.put(OutputKeys.ENCODING, "UTF-8");
 
         _gotInitiator = false;
     }
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		Document doc_in = (Document) cc.getDataComponentFromInput(IN_DOCUMENT);
 
 		_simileDocs.add(annotationToXml(doc_in, _entities));
 
 		if (!_gotInitiator) {
 		    String xmlString = DOMUtils.getString(_simileDocs.get(0), _xmlProperties);
 
 		    xmlString = XMLUtils.stripNonValidXMLCharacters(xmlString);
 
 		    cc.pushDataComponentToOutput(OUT_XML_ANNOTATIONS, BasicDataTypesTools.stringToStrings(xmlString));
 		    _simileDocs.clear();
 		}
 	}
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
 	protected void handleStreamInitiators() throws Exception {
         if (_gotInitiator)
             throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");
 
         _simileDocs = new Vector<org.w3c.dom.Document>();
         _gotInitiator = true;
 	}
 
 	@Override
     protected void handleStreamTerminators() throws Exception {
         if (!_gotInitiator)
             throw new Exception("Received StreamTerminator without receiving StreamInitiator");
 
         String xmlString = DOMUtils.getString(mergeXmlDocuments(), _xmlProperties);
         componentContext.pushDataComponentToOutput(OUT_XML_ANNOTATIONS, new StreamInitiator());
         componentContext.pushDataComponentToOutput(OUT_XML_ANNOTATIONS, BasicDataTypesTools.stringToStrings(xmlString));
         componentContext.pushDataComponentToOutput(OUT_XML_ANNOTATIONS, new StreamTerminator());
 
         _gotInitiator = false;
         _simileDocs.clear();
     }
 
     //--------------------------------------------------------------------------------------------
 
     private org.w3c.dom.Document annotationToXml(Document doc_in, String entities)
         throws Exception {
 
         String docId = doc_in.getDocID();
         String docTitle = doc_in.getTitle();
 
         org.w3c.dom.Document doc_out = _docBuilder.newDocument();
 
         AnnotationSet as = doc_in.getAnnotations(AnnotationConstants.ANNOTATION_SET_ENTITIES);
         console.info("Number of entities in the input: " + as.size());
 
         Iterator<Annotation> itty = as.iterator();
 
         AnnotationSet as2 = doc_in.getAnnotations(AnnotationConstants.ANNOTATION_SET_SENTENCES);
 
         Element root = doc_out.createElement("root");
         doc_out.appendChild(root);
         root.setAttribute("docID", docId);
         if (docTitle != null)
             root.setAttribute("docTitle", docTitle);
 
         console.fine("docID: " + docId);
         console.fine("docTitle: " + docTitle);
 
         while (itty.hasNext()) {
             Annotation ann = itty.next();
             if(entities.indexOf(ann.getType()) != -1) {
                 AnnotationSet subSet = as2.get(ann.getStartNodeOffset(), ann.getEndNodeOffset());
                 Iterator<Annotation> itty2 = subSet.iterator();
                 StringBuffer sentenceBuffer = new StringBuffer();
                 while(itty2.hasNext()) {
                     Annotation item = itty2.next();
                     sentenceBuffer.append(item.getContent(doc_in).trim());
                 }
 
                 String sentence = sentenceBuffer.toString();
                 Element elSentence = createSentenceNode(doc_out, sentence, docId, docTitle);
 
                 String entityValue = ann.getContent(doc_in).trim().toLowerCase();
                 console.fine("Entity: " + entityValue + " :" + ann.getType());
 
                 Element elEntity = doc_out.getElementById(ann.getType() + ":" + entityValue);
 
                 if (elEntity == null) {
                     elEntity = doc_out.createElement(ann.getType());
                     elEntity.setAttribute("value", entityValue);
                     elEntity.setAttribute("id", ann.getType()+":"+entityValue);
                     elEntity.setIdAttribute("id", true);
                     root.appendChild(elEntity);
                 }
 
                 elEntity.appendChild(elSentence);
             }
         }
 
         return doc_out;
     }
 
     private Element createSentenceNode(org.w3c.dom.Document doc_out, String sentence, String docId, String docTitle) {
         Element elSentence = doc_out.createElement("sentence");
 
         if (docId != null)
             elSentence.setAttribute("docId", docId);
 
         if (docTitle != null)
             elSentence.setAttribute("docTitle", docTitle);
 
         elSentence.setTextContent(sentence);
 
         return elSentence;
     }
 
     private org.w3c.dom.Document mergeXmlDocuments() {
         if (_simileDocs.size() == 0) return null;
         if (_simileDocs.size() == 1) return _simileDocs.get(0);
 
         org.w3c.dom.Document doc = _docBuilder.newDocument();
         Element root = doc.createElement("root");
         doc.appendChild(root);
 
         for (org.w3c.dom.Document d : _simileDocs) {
             NodeList nodes = d.getDocumentElement().getChildNodes();
             for (int i = 0, iMax = nodes.getLength(); i < iMax; i++) {
                 Element elEntity = (Element)nodes.item(i);
                 String entityId = elEntity.getAttribute("id");
 
                 Element element = doc.getElementById(entityId);
                 if (element == null) {
                     element = doc.createElement(elEntity.getNodeName());
                     element.setAttribute("value", elEntity.getAttribute("value"));
                     element.setAttribute("id", elEntity.getAttribute("id"));
                     element.setIdAttribute("id", true);
                     root.appendChild(element);
                 }
 
                 NodeList entityChildren = elEntity.getElementsByTagName("sentence");
                 for (int j = 0, jMax = entityChildren.getLength(); j < jMax; j++) {
                     Element child = (Element)entityChildren.item(j);
                     String docId = child.getAttribute("docId");
                     String docTitle = child.getAttribute("docTitle");
                     if (docId == null || docId.length() == 0) docId = null;
                     if (docTitle == null || docTitle.length() == 0) docTitle = null;
                     Element elSentence = createSentenceNode(doc, child.getTextContent(), docId, docTitle);
                     element.appendChild(elSentence);
                 }
             }
         }
 
         return doc;
     }
 }

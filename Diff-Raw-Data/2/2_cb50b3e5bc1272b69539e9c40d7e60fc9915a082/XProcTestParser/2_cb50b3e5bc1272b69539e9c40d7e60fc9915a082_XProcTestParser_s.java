 /*
  * Copyright (C) 2010 Romain Deltour
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xproc;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import net.sf.saxon.s9api.*;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.Saxon;
 import org.trancecode.xml.saxon.SaxonAxis;
 import org.trancecode.xml.saxon.SaxonPredicates;
 import org.xml.sax.InputSource;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamSource;
 import java.net.URI;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Parses a {http://xproc.org/ns/testsuite}test element into an
  * {@link XProcTestCase}.
  * 
  * @author Romain Deltour
  * @authro Herve Quiroz
  */
 public class XProcTestParser
 {
 
     private static final Logger LOG = Logger.getLogger(XProcTestParser.class);
 
     private final Processor processor;
     private final Source source;
     private final String testSuite;
     private final URL url;
 
     private String title;
     private XdmNode description;
     private boolean ignoreWhitespace = true;
     private XdmNode pipeline;
     private final Map<String, List<XdmNode>> inputs = Maps.newHashMap();
     private final Map<QName, String> options = Maps.newHashMap();
     private final Map<String, Map<QName, String>> parameters = Maps.newHashMap();
     private QName error;
     private final Map<String, List<XdmNode>> outputs = Maps.newHashMap();
     private XdmNode comparePipeline;
 
     public XProcTestParser(final Processor processor, final URL url, final String testSuite)
     {
         this.processor = Preconditions.checkNotNull(processor);
         this.url = url;
         this.testSuite = testSuite;
         this.source = new StreamSource(url.toString());
     }
 
     public void parse()
     {
         try
         {
             final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
             final XdmNode pipelineDocument = documentBuilder.build(source);
             final XdmNode rootNode = SaxonAxis.childElement(pipelineDocument);
             parseTest(rootNode);
         }
         catch (final SaxonApiException e)
         {
             throw new PipelineException(e);
         }
     }
 
     private void parseTest(final XdmNode node)
     {
         if (!node.getNodeName().equals(XProcTestSuiteXmlModel.ELEMENT_TEST))
         {
             unsupportedElement(node);
         }
         parseTitle(SaxonAxis.childElement(node, XProcTestSuiteXmlModel.ELEMENT_TITLE));
         parseIgnoreWhitespace(node);
         parseError(node);
         parseDescription(SaxonAxis.childElements(node, XProcTestSuiteXmlModel.ELEMENT_DESCRIPTION));
         parseInputs(SaxonAxis.childElements(node, XProcTestSuiteXmlModel.ELEMENT_INPUT));
         parseOptions(SaxonAxis.childElements(node, XProcTestSuiteXmlModel.ELEMENT_OPTION));
         parseParameters(SaxonAxis.childElements(node, XProcTestSuiteXmlModel.ELEMENT_PARAMETER));
         parsePipeline(SaxonAxis.childElement(node, XProcTestSuiteXmlModel.ELEMENT_PIPELINE));
         if (error == null)
         {
             parseComparePipeline(SaxonAxis.childElements(node, XProcTestSuiteXmlModel.ELEMENT_COMPARE_PIPELINE));
             parseOutputs(SaxonAxis.childElements(node, XProcTestSuiteXmlModel.ELEMENT_OUTPUT));
         }
 
     }
 
     private void parseTitle(final XdmNode node)
     {
         title = node.getStringValue();
         LOG.trace("== Test: {} ==", title);
     }
 
     private void parseIgnoreWhitespace(final XdmNode node)
     {
         final String ignoreWhitespaceValue = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_IGNORE_WHITESPACE);
         if (ignoreWhitespaceValue != null)
         {
             LOG.trace("Ignore whitespace: {}", ignoreWhitespaceValue);
             ignoreWhitespace = !"false".equals(ignoreWhitespaceValue);
         }
     }
 
     private void parseError(final XdmNode node)
     {
         final String errorName = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_ERROR);
         error = (errorName != null) ? new QName(errorName, node) : null;
         if (error != null)
         {
             LOG.trace("Expected error: {}", error);
         }
     }
 
     private void parseDescription(final Iterable<XdmNode> nodes)
     {
         final Iterator<XdmNode> iter = nodes.iterator();
         if (iter.hasNext())
         {
             description = Saxon.asDocumentNode(iter.next(), processor);
             LOG.trace("Description: parsed");
         }
         if (iter.hasNext())
         {
             LOG.warn("More than one description was found");
         }
     }
 
     private void parseInputs(final Iterable<XdmNode> nodes)
     {
         for (final XdmNode node : nodes)
         {
             final String port = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
             if (port == null)
             {
                 LOG.warn("Input with no port");
             }
             else
             {
                 LOG.trace("Input for port {}", port);
                 inputs.put(port, extractDocuments(node));
             }
         }
     }
 
     private void parseOptions(final Iterable<XdmNode> nodes)
     {
         for (final XdmNode node : nodes)
         {
             final String name = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_NAME);
             final String value = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_VALUE);
             if (name == null || value == null)
             {
                 LOG.warn("Invalid option: {}={}", name, value);
             }
             else
             {
                 LOG.trace("Option: {}={}", name, value);
                 options.put(new QName(name, node), value);
             }
         }
     }
 
     private void parseParameters(final Iterable<XdmNode> nodes)
     {
         for (final XdmNode node : nodes)
         {
             final String port = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
             final String name = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_NAME);
             final String value = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_VALUE);
             if (name == null || value == null)
             {
                 LOG.warn("Involid parameter on port '{}': {}={}", port, name, value);
             }
             else
             {
                 LOG.trace("Parameter: [{}] {}={}", port, name, value);
                 final Map<QName, String> portParams = (parameters.containsKey(port)) ? parameters.get(port)
                          : new HashMap<QName, String>();
                 portParams.put(new QName(name, node), value);
                 if (port != null)
                 {
                     parameters.put(port, portParams);
                 }
                 else
                 {
                     parameters.put("", portParams);
                 }
             }
         }
     }
 
     private void parsePipeline(final XdmNode node)
     {
         pipeline = extractPipeline(node);
         LOG.trace("Parsed pipeline");
     }
 
     private void parseComparePipeline(final Iterable<XdmNode> nodes)
     {
         final Iterator<XdmNode> iter = nodes.iterator();
         if (iter.hasNext())
         {
             comparePipeline = extractPipeline(iter.next());
             LOG.trace("Parsed compare-pipeline");
         }
         if (iter.hasNext())
         {
             LOG.warn("More than one compare-pipeline was found");
         }
     }
 
     private void parseOutputs(final Iterable<XdmNode> nodes)
     {
         for (final XdmNode node : nodes)
         {
             final String port = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
             if (port == null)
             {
                 LOG.warn("Output with no port");
             }
             else
             {
                 LOG.trace("Output for port {}", port);
                 outputs.put(port, extractDocuments(node));
             }
         }
     }
 
     private List<XdmNode> extractDocuments(final XdmNode node)
     {
         final List<XdmNode> documents = Lists.newLinkedList();
         final String href = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
         if (href != null)
         {
             documents.add(loadExternalDocument(href, node));
             LOG.trace("New external document");
         }
         else
         {
             final Iterable<XdmNode> documentElements = SaxonAxis.childElements(node,
                     XProcTestSuiteXmlModel.ELEMENT_DOCUMENT);
             if (Iterables.isEmpty(documentElements))
             {
                 final Iterable<XdmNode> nodes = Iterables.filter(SaxonAxis.childElements(node),
                         Predicates.not(SaxonPredicates.isAttribute()));
                 if (Iterables.isEmpty(nodes))
                 {
                     LOG.trace("New empty document");
                     documents.add(Saxon.asDocumentNode(processor, nodes));
                 }
                 else
                 {
                     LOG.trace("New inline document");
                     documents.add(Saxon.asDocumentNode(processor, nodes));
                 }
             }
             else
             {
                 for (final XdmNode documentNode : documentElements)
                 {
                     final String documentHref = documentNode.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
                    if (href != null)
                     {
                         LOG.trace("New external document: {}", documentHref);
                         documents.add(loadExternalDocument(documentHref, documentNode));
                     }
                     else
                     {
                         LOG.trace("New wrapped document");
                         final Iterable<XdmNode> nodesWithoutAttributes = Iterables.filter(
                                 SaxonAxis.childNodes(documentNode), Predicates.not(SaxonPredicates.isAttribute()));
                         documents.add(Saxon.asDocumentNode(processor, nodesWithoutAttributes));
                     }
                 }
             }
         }
         return documents;
     }
 
     private XdmNode extractPipeline(final XdmNode node)
     {
         final String href = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
         if (href != null)
         {
             LOG.trace("New external document");
             return loadExternalDocument(href, node);
         }
         else
         {
             return Saxon.asDocumentNode(SaxonAxis.childElement(node), processor);
         }
     }
 
     private XdmNode loadExternalDocument(final String href, final XdmNode node)
     {
 
         final DocumentBuilder builder = processor.newDocumentBuilder();
         final URI uri = node.getBaseURI().resolve(href);
         final SAXSource source = new SAXSource(new InputSource(uri.toString()));
         try
         {
             return builder.build(source);
         }
         catch (final SaxonApiException e)
         {
             throw new IllegalStateException("Couldn't load document at " + uri);
         }
     }
 
     public XProcTestCase getTest()
     {
         return new XProcTestCase(url, testSuite, title, description, ignoreWhitespace, pipeline, inputs, options,
                 parameters, error, outputs, comparePipeline);
     }
 
     private void unsupportedElement(final XdmNode node)
     {
         throw new IllegalStateException("Unsupported element " + node.getNodeName().toString());
     }
 }

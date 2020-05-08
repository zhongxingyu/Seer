 /*
  * Copyright (C) 2011 Emmanuel Tourdot
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
  *
  * $Id$
  */
 package org.trancecode.xproc.step;
 
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.EnumSet;
 import javax.mail.internet.ContentType;
 import javax.mail.internet.ParseException;
 import javax.xml.transform.sax.SAXSource;
 import net.sf.saxon.s9api.DocumentBuilder;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.ccil.cowan.tagsoup.Parser;
 import org.trancecode.io.MediaTypes;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.Saxon;
 import org.trancecode.xml.saxon.SaxonAxis;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xml.saxon.SaxonProcessor;
 import org.trancecode.xml.saxon.SaxonProcessorDelegate;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 import org.xml.sax.InputSource;
 
 /**
  * @author Emmanuel Tourdot
  */
 public final class UnEscapeMarkupStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(UnEscapeMarkupStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.UNESCAPE_MARKUP;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
 
         final String namespaceOption = input.getOptionValue(XProcOptions.NAMESPACE, null);
         final URI namespaceURI = getUri(namespaceOption);
         final String contentTypeOption = input.getOptionValue(XProcOptions.CONTENT_TYPE, MediaTypes.MEDIA_XML);
         final String encodingOption = input.getOptionValue(XProcOptions.ENCODING, null);

         final ContentType contentType = getContentType(contentTypeOption, input.getStep());
         final String charsetOption = input.getOptionValue(XProcOptions.CHARSET, null);
         final String charset = (charsetOption == null) ? contentType.getParameter("charset") : charsetOption;
 
         if (StepUtils.ENCODING_BASE64.equals(encodingOption))
         {
             if (charset == null)
             {
                 throw XProcExceptions.xc0010(input.getStep().getNode());
             }
             else
             {
                 final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                     .getUnderlyingConfiguration());
                 builder.startDocument();
                 final Iterable<XdmNode> childNodes = SaxonAxis.childElements(sourceDocument);
                 for (XdmNode aNode : childNodes)
                 {
                     if (XdmNodeKind.ELEMENT.equals(aNode.getNodeKind()))
                     {
                         final String unEscapeContent = getUnEscapeContent(aNode.getStringValue(), encodingOption, contentType, charset);
                         builder.startElement(aNode.getNodeName(), aNode);
                         for (final XdmNode attribute : SaxonAxis.attributes(aNode))
                         {
                             LOG.trace("copy existing attribute: {}", attribute);
                             builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                         }
                         if (MediaTypes.MEDIA_TYPE_HTML.equals(contentType.getBaseType()))
                         {
                             writeHtmlNodes(unEscapeContent, namespaceOption, input.getPipelineContext().getProcessor(), builder);
                         }
                         else
                         {
                             writeXmlNodes(unEscapeContent, namespaceOption, input.getPipelineContext().getProcessor(), builder);
                         }
                         builder.endElement();
                     }
                     else
                     {
                         builder.nodes(aNode);
                     }
                 }
                 builder.endDocument();
                 output.writeNodes(XProcPorts.RESULT, builder.getNode());
             }
         }
         else
         {
             final SaxonProcessorDelegate escapeDelegate = new CopyingSaxonProcessorDelegate()
             {
                 @Override
                 public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
                 {
                     builder.startElement(node.getNodeName(), node);
                     return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
                 }
 
                 @Override
                 public void text(final XdmNode node, final SaxonBuilder builder)
                 {
                     final String unEscapeContent = getUnEscapeContent(node.getStringValue(), encodingOption, contentType, charset);
                     if (MediaTypes.MEDIA_TYPE_HTML.equals(contentType.getBaseType()))
                     {
                         writeHtmlNodes(unEscapeContent, namespaceOption, input.getPipelineContext().getProcessor(), builder);
                     }
                     else
                     {
                         writeXmlNodes(unEscapeContent, namespaceOption, input.getPipelineContext().getProcessor(), builder);                            
                     }
                 }
             };
             final SaxonProcessor escapeProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(), escapeDelegate);
 
             final XdmNode result = escapeProcessor.apply(sourceDocument);
             output.writeNodes(XProcPorts.RESULT, result);
         }
     }
 
     private static void writeXmlNodes(final String unEscapeContent, final String namespaceOption, final Processor processor, final SaxonBuilder builder)
     {
         final XdmNode parsedNode = Saxon.parse("<z>" + unEscapeContent + "</z>", processor);
         final Iterable<XdmNode> childNodes = SaxonAxis.childNodes(SaxonAxis.childElement(parsedNode));
         for (final XdmNode aNode : childNodes)
         {
             if (XdmNodeKind.ELEMENT.equals(aNode.getNodeKind()))
             {
                 final QName nameNode = new QName("", namespaceOption, aNode.getNodeName().getLocalName());
                 builder.startElement(nameNode, aNode);
                 builder.nodes(SaxonAxis.childNodes(aNode));
                 builder.endElement();
             }
             else
             {
                 builder.nodes(aNode);
             }
         }
     }
 
     private static void writeHtmlNodes(final String unEscapeContent, final String namespaceOption, final Processor processor, final SaxonBuilder builder)
     {
         try
         {
             final StringReader inputStream = new StringReader(unEscapeContent);
             final InputSource source = new InputSource(inputStream);
             final Parser parser = new Parser();
             final SAXSource saxSource = new SAXSource(parser, source);
             final DocumentBuilder docBuilder = processor.newDocumentBuilder();
             final XdmNode aNode = docBuilder.build(saxSource);
             builder.namespace("", namespaceOption);
             builder.nodes(aNode);
         }
         catch (SaxonApiException e)
         {
             throw XProcExceptions.xc0051(null) ;
         }
     }
 
     private static URI getUri(final String namespace)
     {
         if (namespace == null)
         {
             return null;
         }
         try
         {
             final URI uri = new URI(namespace);
             if (!uri.isAbsolute())
             {
                 return null;
             }
             else
             {
                 return uri;
             }
         }
         catch (URISyntaxException e)
         {
             return null;
         }
     }
 
     private static ContentType getContentType(final String content_type, final Step inputStep)
     {
         try
         {
             final ContentType ct = new ContentType(content_type);
             if (!StepUtils.SUPPORTED_CONTENTTYPE.contains(ct.getBaseType()))
             {
                 throw XProcExceptions.xc0051(inputStep.getLocation());
             }
             return ct;
         }
         catch (ParseException e)
         {
             throw XProcExceptions.xc0051(inputStep.getLocation());
         }
     }
 
     private static String getUnEscapeContent(final String content, final String encoding, final ContentType contentType, final String charset)
     {
         if (StepUtils.ENCODING_BASE64.equals(encoding))
         {
             return StepUtils.getBase64Content(content, contentType, charset);
         }
         else if (MediaTypes.MEDIA_XML.equals(contentType.getBaseType()))
         {
             return StringEscapeUtils.unescapeHtml(content);
         }
         else if (MediaTypes.MEDIA_TYPE_HTML.equals(contentType.getBaseType()))
         {
             return StringEscapeUtils.unescapeXml(content);
         }
         return "";
     }
 }

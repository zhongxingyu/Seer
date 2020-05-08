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
 
 import com.google.common.io.Closeables;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import javax.xml.transform.stream.StreamSource;
 import net.sf.saxon.Configuration;
 import net.sf.saxon.lib.AugmentedSource;
 import net.sf.saxon.om.DocumentInfo;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonLocation;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * {@code p:xinclude}.
  * 
  * @author Emmanuel Tourdot
  * @see <a href="http://www.w3.org/TR/xproc/#c.xinclude">p:xinclude</a>
  */
 @ExternalResources(read = false, write = false)
 public final class XIncludeStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(XIncludeStepProcessor.class);
     private static final String XINCLUDE_FEATURE_ID = "http://apache.org/xml/features/xinclude";
     private static final String XINCLUDE_FIXUP_BASE_URIS_FEATURE_ID = "http://apache.org/xml/features/xinclude/fixup-base-uris";
     private static final String XINCLUDE_FIXUP_LANGUAGE_FEATURE_ID = "http://apache.org/xml/features/xinclude/fixup-language";
     private static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.XINCLUDE;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final XdmNode node = input.readNode(XProcPorts.SOURCE);
         assert node != null;
         final boolean xmlBase = Boolean.parseBoolean(input.getOptionValue(XProcOptions.FIXUP_XML_BASE, "false"));
         LOG.trace("xmlBase = {}", xmlBase);
         final boolean xmlLang = Boolean.parseBoolean(input.getOptionValue(XProcOptions.FIXUP_XML_LANG, "false"));
         LOG.trace("xmlLang = {}", xmlLang);
 
         final Processor processor = input.getPipelineContext().getProcessor();
         final Configuration configuration = processor.getUnderlyingConfiguration();
         final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             final Serializer serializer = processor.newSerializer(baos);
             serializer.serializeNode(node);
             final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
             final AugmentedSource source = AugmentedSource.makeAugmentedSource(new StreamSource(bais));
             source.setXIncludeAware(true);
             source.setPleaseCloseAfterUse(true);
            source.setSystemId(node.getBaseURI().toASCIIString());
             final XMLReader xmlReader = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
             xmlReader.setFeature(XINCLUDE_FEATURE_ID, true);
             xmlReader.setFeature(XINCLUDE_FIXUP_BASE_URIS_FEATURE_ID, xmlBase);
             xmlReader.setFeature(XINCLUDE_FIXUP_LANGUAGE_FEATURE_ID, xmlLang);
             source.setXMLReader(xmlReader);
             final DocumentInfo doc = configuration.buildDocument(source);
             Closeables.closeQuietly(bais);
             output.writeNodes(XProcPorts.RESULT, new XdmNode(doc));
         }
         catch (Exception e)
         {
             e.printStackTrace();
             throw XProcExceptions.xc0029(SaxonLocation.of(node));
         }
         finally
         {
             Closeables.closeQuietly(baos);
         }
     }
 }

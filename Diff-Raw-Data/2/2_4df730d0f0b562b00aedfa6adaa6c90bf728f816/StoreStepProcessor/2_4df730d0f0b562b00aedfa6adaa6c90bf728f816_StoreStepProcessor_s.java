 /*
  * Copyright (C) 2008 TranceCode Software
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
 
 import java.io.OutputStream;
 import java.net.URI;
 
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.Serializer.Property;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.io.IOUtil;
 import org.trancecode.io.MediaTypes;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.PipelineException;
 import org.trancecode.xproc.Step;
 import org.trancecode.xproc.XProcOptions;
 import org.trancecode.xproc.XProcPorts;
 import org.trancecode.xproc.parser.XProcElements;
 
 /**
  * @author Herve Quiroz
  */
 public class StoreStepProcessor extends AbstractStepProcessor
 {
     public static final String DEFAULT_ENCODING = "UTF-8";
 
     public static final String DEFAULT_OMIT_XML_DECLARATION = "no";
 
     public static final String DEFAULT_DOCTYPE_PUBLIC = null;
 
     public static final String DEFAULT_DOCTYPE_SYSTEM = null;
 
     public static final String DEFAULT_METHOD = null;
 
     public static final String DEFAULT_MIMETYPE = MediaTypes.MEDIA_TYPE_XML;
 
     public static final StoreStepProcessor INSTANCE = new StoreStepProcessor();
 
     private static final Logger LOG = Logger.getLogger(StoreStepProcessor.class);
 
     @Override
     protected Environment doRun(final Step step, final Environment environment)
     {
         final XdmNode node = environment.readNode(step.getPortReference(XProcPorts.SOURCE));
         assert node != null;
 
         final URI baseUri = environment.getBaseUri();
         final String providedHref = environment.getVariable(XProcOptions.HREF);
         final String href;
         if (providedHref != null)
         {
             href = providedHref;
         }
         else
         {
             href = node.getUnderlyingNode().getSystemId();
         }
 
         final URI outputUri = baseUri.resolve(href);
 
         final String mimeType = environment.getVariable(XProcOptions.MEDIA_TYPE, DEFAULT_MIMETYPE);
 
         final String encoding = environment.getVariable(XProcOptions.ENCODING, DEFAULT_ENCODING);
 
         final String omitXmlDeclaration = environment.getVariable(XProcOptions.OMIT_XML_DECLARATION,
                 DEFAULT_OMIT_XML_DECLARATION);
 
         final String doctypePublicId = environment.getVariable(XProcOptions.DOCTYPE_PUBLIC, DEFAULT_DOCTYPE_PUBLIC);
 
         final String doctypeSystemId = environment.getVariable(XProcOptions.DOCTYPE_SYSTEM, DEFAULT_DOCTYPE_SYSTEM);
 
         final String method = environment.getVariable(XProcOptions.METHOD, DEFAULT_METHOD);
 
         final boolean indent = Boolean.parseBoolean(environment.getVariable(XProcOptions.INDENT));
 
         LOG.debug("Storing document to: {} ; mime-type: {} ; encoding: {} ; doctype-public = {} ; doctype-system = {}",
                new Object[] { href, mimeType, encoding, doctypePublicId, doctypeSystemId });
 
         assert environment.getConfiguration().getOutputResolver() != null;
         final OutputStream targetOutputStream = environment.getConfiguration().getOutputResolver()
                 .resolveOutputStream(href, environment.getBaseUri().toString());
 
         final Serializer serializer = new Serializer();
         serializer.setOutputStream(targetOutputStream);
         if (doctypePublicId != null)
         {
             serializer.setOutputProperty(Property.DOCTYPE_PUBLIC, doctypePublicId);
         }
         if (doctypeSystemId != null)
         {
             serializer.setOutputProperty(Property.DOCTYPE_SYSTEM, doctypeSystemId);
         }
         serializer.setOutputProperty(Property.DOCTYPE_SYSTEM, doctypeSystemId);
         if (method != null)
         {
             LOG.debug("method = {}", method);
             serializer.setOutputProperty(Property.METHOD, method);
         }
         serializer.setOutputProperty(Property.ENCODING, encoding);
         serializer.setOutputProperty(Property.MEDIA_TYPE, mimeType);
         serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, omitXmlDeclaration);
         serializer.setOutputProperty(Property.INDENT, (indent ? "yes" : "no"));
 
         try
         {
             environment.getConfiguration().getProcessor().writeXdmValue(node, serializer);
             targetOutputStream.close();
         }
         catch (final Exception e)
         {
             throw new PipelineException("Error while trying to write document ; output-base-uri = %s", e, outputUri);
         }
         finally
         {
             IOUtil.closeQuietly(targetOutputStream);
         }
 
         final XdmNode resultNode = XProcElements.newResultElement(outputUri.toString(), environment.getConfiguration()
                 .getProcessor());
         return environment.writeNodes(step.getPortReference(XProcPorts.RESULT), resultNode);
     }
 }

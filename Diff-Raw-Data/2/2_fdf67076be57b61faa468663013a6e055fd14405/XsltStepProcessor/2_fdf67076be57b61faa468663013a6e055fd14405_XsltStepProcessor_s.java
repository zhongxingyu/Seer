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
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 
 import java.net.URI;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.TransformerException;
 
 import net.sf.saxon.OutputURIResolver;
 import net.sf.saxon.event.Receiver;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XdmAtomicValue;
 import net.sf.saxon.s9api.XdmDestination;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XsltTransformer;
 import org.trancecode.collection.TcMaps;
 import org.trancecode.io.Uris;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.PipelineException;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * @author Herve Quiroz
  */
 public final class XsltStepProcessor extends AbstractStepProcessor
 {
     private static final String DEFAULT_VERSION = "2.0";
     private static final Set<String> SUPPORTED_VERSIONS = ImmutableSet.of("2.0");
 
     private static final Logger LOG = Logger.getLogger(XsltStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.XSLT;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
         assert sourceDocument != null;
 
         final String providedOutputBaseUri = input.getOptionValue(XProcOptions.OUTPUT_BASE_URI);
         final URI outputBaseUri;
         if (providedOutputBaseUri != null && providedOutputBaseUri.length() > 0)
         {
             outputBaseUri = URI.create(providedOutputBaseUri);
         }
        else if (sourceDocument.getBaseURI() != null)
         {
             outputBaseUri = sourceDocument.getBaseURI();
         }
         else
         {
             outputBaseUri = input.getBaseUri();
         }
         assert outputBaseUri != null;
         LOG.trace("outputBaseUri = {}", outputBaseUri);
 
         final String version = input.getOptionValue(XProcOptions.VERSION, DEFAULT_VERSION);
 
         if (!SUPPORTED_VERSIONS.contains(version))
         {
             throw XProcExceptions.xc0038(input.getStep().getLocation(), version);
         }
         final XdmNode stylesheet = input.readNode(XProcPorts.STYLESHEET);
         assert stylesheet != null;
 
         final Processor processor = input.getPipelineContext().getProcessor();
 
         // TODO pipeline logging
         final XsltTransformer transformer;
         try
         {
             transformer = processor.newXsltCompiler().compile(stylesheet.asSource()).load();
             transformer.setSource(sourceDocument.asSource());
         }
         catch (final SaxonApiException e)
         {
             throw new PipelineException(e);
         }
 
         // TODO transformer.setMessageListener();
         final XdmDestination result = new XdmDestination();
         result.setBaseURI(outputBaseUri);
         transformer.setDestination(result);
         transformer.getUnderlyingController().setBaseOutputURI(outputBaseUri.toString());
 
         final List<XdmNode> secondaryPortNodes = Lists.newArrayList();
         transformer.getUnderlyingController().setOutputURIResolver(new OutputURIResolver()
         {
             final Map<URI, XdmDestination> destinations = TcMaps.newSmallWriteOnceMap();
 
             public void close(final Result result) throws TransformerException
             {
                 final URI uri = URI.create(result.getSystemId());
                 assert destinations.containsKey(uri);
                 final XdmDestination xdmResult = destinations.get(uri);
                 LOG.trace("result base URI = {}", xdmResult.getXdmNode().getBaseURI());
                 secondaryPortNodes.add(xdmResult.getXdmNode());
             }
 
             public Result resolve(final String href, final String base) throws TransformerException
             {
                 final URI uri = Uris.resolve(href, base);
                 assert uri != null;
                 LOG.debug("new result document: {}", uri);
 
                 try
                 {
                     final XdmDestination xdmResult = new XdmDestination();
                     xdmResult.setBaseURI(uri);
                     destinations.put(uri, xdmResult);
                     final Receiver receiver = xdmResult.getReceiver(processor.getUnderlyingConfiguration());
                     receiver.setSystemId(uri.toString());
 
                     return receiver;
                 }
                 catch (final SaxonApiException e)
                 {
                     throw new TransformerException(e);
                 }
             }
         });
 
         final String initialMode = input.getOptionValue(XProcOptions.INITIAL_MODE, null);
         if (initialMode != null)
         {
             // FIXME does not handle namespaces
             transformer.setInitialMode(new QName(initialMode));
         }
 
         final Map<QName, String> parameters = input.getParameters(XProcPorts.PARAMETERS);
         LOG.debug("parameters = {}", parameters);
         for (final Map.Entry<QName, String> parameter : parameters.entrySet())
         {
             transformer.setParameter(parameter.getKey(), new XdmAtomicValue(parameter.getValue()));
         }
 
         try
         {
             transformer.transform();
         }
         catch (final SaxonApiException e)
         {
             // TODO XProcException?
             throw new PipelineException(e);
         }
 
         output.writeNodes(XProcPorts.SECONDARY, secondaryPortNodes);
         output.writeNodes(XProcPorts.RESULT, result.getXdmNode());
     }
 }

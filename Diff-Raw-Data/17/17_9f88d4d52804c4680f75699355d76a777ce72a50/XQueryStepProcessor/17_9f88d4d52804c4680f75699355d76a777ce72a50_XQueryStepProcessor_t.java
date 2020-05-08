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
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;

 import java.util.Iterator;
 import java.util.Map;

 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.lib.CollectionURIResolver;
 import net.sf.saxon.om.Item;
 import net.sf.saxon.om.SequenceIterator;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XQueryCompiler;
 import net.sf.saxon.s9api.XQueryEvaluator;
 import net.sf.saxon.s9api.XdmAtomicValue;
 import net.sf.saxon.s9api.XdmItem;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.NodeListIterator;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonLocation;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.Variable;
 
 /**
  * {@code p:xquery}.
  * 
  * @author Emmanuel Tourdot
  * @see <a href="http://www.w3.org/TR/xproc/#c.xquery">p:xquery</a>
  */
 @ExternalResources(read = false, write = false)
 public final class XQueryStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(XQueryStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.XQUERY;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final Iterable<XdmNode> sourcesDoc = readSequencePort(input, XProcPorts.SOURCE);
         final XdmNode queryNode = input.readNode(XProcPorts.QUERY);
         LOG.trace("query = {}", queryNode.getStringValue());
 
         final Processor processor = input.getPipelineContext().getProcessor();
         final CollectionURIResolver oldCollResolver = processor.getUnderlyingConfiguration().getCollectionURIResolver();
         final XQCollectionResolver collResolver = new XQCollectionResolver(oldCollResolver);
         collResolver.addToCollection(sourcesDoc);
         processor.getUnderlyingConfiguration().setCollectionURIResolver(collResolver);
         final XQueryCompiler xQueryCompiler = processor.newXQueryCompiler();
         try
         {
             final XQueryEvaluator xQueryEvaluator = xQueryCompiler.compile(queryNode.getStringValue()).load();
             xQueryEvaluator.setContextItem(Iterables.getFirst(sourcesDoc, null));
             final Map<QName, String> params = getParameters(input);
             for (final Map.Entry<QName, String> param : params.entrySet())
             {
                 xQueryEvaluator.setExternalVariable(param.getKey(), new XdmAtomicValue(param.getValue()));
             }
             final Iterator<XdmItem> iterator = xQueryEvaluator.iterator();
             while (iterator.hasNext())
             {
                 final XdmItem item = iterator.next();
                 if (item.isAtomicValue())
                 {
                     throw XProcExceptions.xc0057(SaxonLocation.of(queryNode));
                 }
                 output.writeNodes(XProcPorts.RESULT, (XdmNode) item);
             }
         }
        catch (final SaxonApiException e)
         {
             e.printStackTrace();
         }
         finally
         {
             processor.getUnderlyingConfiguration().setCollectionURIResolver(oldCollResolver);
         }
     }
 
     private Map<QName, String> getParameters(final StepInput input)
     {
         final ImmutableMap.Builder<QName, String> builder = new ImmutableMap.Builder<QName, String>();
         final Map<QName, Variable> stepParams = Environment.getCurrentEnvironment().getPipeline().getParameters();
         for (final Map.Entry<QName, Variable> entry : stepParams.entrySet())
         {
             builder.put(entry.getKey(), entry.getValue().getValue());
         }
         final Map<QName, String> inputParams = input.getParameters(XProcSteps.PARAMETERS.getLocalName());
         builder.putAll(inputParams);
         return builder.build();
     }
 
     private Iterable<XdmNode> readSequencePort(final StepInput input, final String portName)
     {
         final Iterable<XdmNode> source = input.readNodes(portName);
         final Iterator<XdmNode> iterator = source.iterator();
        final Builder<XdmNode> builder = ImmutableList.builder();
         while (iterator.hasNext())
         {
             final XdmNode node = iterator.next();
             builder.add(node);
         }
         return builder.build();
     }
 
     private final class XQCollectionResolver implements CollectionURIResolver
     {
         private final CollectionURIResolver oldCollResolver;
        private final Builder<Item> collectionBuilder;
 
         private XQCollectionResolver(final CollectionURIResolver oldCollResolver)
         {
             this.oldCollResolver = oldCollResolver;
            collectionBuilder = ImmutableList.builder();
         }
 
         public void addToCollection(final Iterable<XdmNode> nodes)
         {
             for (final XdmNode node : nodes)
             {
                 collectionBuilder.add(node.getUnderlyingNode());
             }
         }
 
         @Override
         public SequenceIterator resolve(final String href, final String base, final XPathContext context)
                 throws XPathException
         {
             if (Strings.isNullOrEmpty(href))
             {
                 return new NodeListIterator(collectionBuilder.build());
             }
             return oldCollResolver.resolve(href, base, context);
         }
     }
 }

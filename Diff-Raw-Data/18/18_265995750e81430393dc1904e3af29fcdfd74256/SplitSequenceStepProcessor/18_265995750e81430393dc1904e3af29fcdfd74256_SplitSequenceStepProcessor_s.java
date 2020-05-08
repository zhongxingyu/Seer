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
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 import net.sf.saxon.om.Item;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XPathCompiler;
 import net.sf.saxon.s9api.XPathExecutable;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.sxpath.XPathDynamicContext;
 import net.sf.saxon.sxpath.XPathExpression;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.NodeListIterator;
 import net.sf.saxon.value.BooleanValue;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.Saxon;
 import org.trancecode.xml.saxon.SaxonNamespaces;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * {@code p:split-sequence}.
  * 
  * @author Emmanuel Tourdot
  * @see <a href="http://www.w3.org/TR/xproc/#c.split-sequence">p:split-sequence</a>
  */
 @ExternalResources(read = false, write = false)
 public final class SplitSequenceStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(SplitSequenceStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.SPLIT_SEQUENCE;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final Iterable<XdmNode> nodes = input.readNodes(XProcPorts.SOURCE);
         LOG.trace("nodes = {}", nodes);
         final String test = input.getOptionValue(XProcOptions.TEST);
         assert test != null;
         final Processor processor = input.getPipelineContext().getProcessor();
         final boolean initialOnly = Boolean.parseBoolean(input.getOptionValue(XProcOptions.INITIAL_ONLY, "false"));
 
         // Transform element's node to document's node needed by xpath test
         final Function<XdmNode, NodeInfo> eltToDoc = new Function<XdmNode, NodeInfo>()
         {
             @Override
             public NodeInfo apply(final XdmNode node)
             {
                 return Saxon.asDocumentNode(node, processor).getUnderlyingNode();
             }
         };
         final List<NodeInfo> docs = ImmutableList.copyOf(Iterables.transform(nodes, eltToDoc));
 
         try
         {
             final XPathCompiler xpathCompiler = processor.newXPathCompiler();
             for (Map.Entry<String, String> namespace : SaxonNamespaces.namespaceSequence(input.getStep().getNode()))
             {
                 xpathCompiler.declareNamespace(namespace.getKey(), namespace.getValue());
             }
             final XPathExecutable xpathExecutable = xpathCompiler.compile(test);
             final NodeListIterator nodeIterator = new NodeListIterator(docs);
             final AtomicBoolean reached = new AtomicBoolean(false);
             while (nodeIterator.hasNext())
             {
                 final NodeInfo doc = (NodeInfo) nodeIterator.next();
 
                 final AtomicBoolean pass = new AtomicBoolean(false);
                 final XPathExpression xpathExpression = xpathExecutable.getUnderlyingExpression();
                 final XPathDynamicContext xpathDynamicContext = xpathExpression.createDynamicContext(doc);
                 xpathDynamicContext.getXPathContextObject().setCurrentIterator(nodeIterator);
                 final List<Item> results = xpathExpression.evaluate(xpathDynamicContext);
                 if (results.isEmpty())
                 {
                     pass.set(false);
                    reached.set(true && initialOnly);
                 }
                 else
                 {
                     final Item item = results.get(0);
                     if (item instanceof BooleanValue)
                     {
                         pass.set(((BooleanValue) item).getBooleanValue());
                     }
                     else
                     {
                         pass.set(item != null);
                     }
                 }
                if (pass.get() && !reached.get()) {
                     output.writeNodes(XProcPorts.MATCHED, new XdmNode(doc));
                } else {
                     output.writeNodes(XProcPorts.NOT_MATCHED, new XdmNode(doc));
                 }
             }
         }
         catch (final XPathException xpe)
         {
             throw XProcExceptions.xd0023(input.getLocation(), test, xpe.getMessage());
         }
         catch (final SaxonApiException sae)
         {
             throw XProcExceptions.xd0023(input.getLocation(), test, sae.getMessage());
         }
     }
 }

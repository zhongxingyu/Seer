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
 
 import com.google.common.collect.Iterables;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XPathCompiler;
 import net.sf.saxon.s9api.XPathSelector;
 import net.sf.saxon.s9api.XdmAtomicValue;
 import net.sf.saxon.s9api.XdmItem;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmValue;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * {@code p:compare}.
  * 
  * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.count">p:count</a>
  */
 public final class CompareStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(CompareStepProcessor.class);
 
     @Override
     public QName stepType()
     {
         return XProcSteps.COMPARE;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final XdmNode sourceNode = input.readNode(XProcPorts.SOURCE);
         final XdmNode alternateNode = input.readNode(XProcPorts.ALTERNATE);
         final boolean result = compare(input.pipelineContext().getProcessor(), sourceNode, alternateNode);
         LOG.trace("  result = {}", result);
         final boolean failIfNotEqual = Boolean.parseBoolean(input.getOptionValue(XProcOptions.FAIL_IF_NOT_EQUAL));
         LOG.trace("  failIfNotEqual = {}", failIfNotEqual);
         if (!result && failIfNotEqual)
         {
             throw XProcExceptions.xc0019(input.step());
         }
 
         output.writeNodes(XProcPorts.RESULT, input.newResultElement(Boolean.toString(result)));
     }
 
     private static boolean compare(final Processor processor, final XdmNode sourceNode, final XdmNode alternateNode)
     {
         try
         {
             final XPathCompiler xpathCompiler = processor.newXPathCompiler();
             final QName node1 = new QName("node1");
             final QName node2 = new QName("node2");
             xpathCompiler.declareVariable(node1);
             xpathCompiler.declareVariable(node2);
 
             final XPathSelector selector = xpathCompiler.compile("deep-equal($node1, $node2)").load();
             selector.setVariable(node1, sourceNode);
             selector.setVariable(node2, sourceNode);
 
             final XdmValue result = selector.evaluate();
             final XdmItem resultNode = Iterables.getOnlyElement(result);
             assert resultNode.isAtomicValue() : resultNode;
             return ((XdmAtomicValue) resultNode).getBooleanValue();
         }
         catch (final SaxonApiException e)
         {
             throw new IllegalStateException(e);
         }
     }
 }

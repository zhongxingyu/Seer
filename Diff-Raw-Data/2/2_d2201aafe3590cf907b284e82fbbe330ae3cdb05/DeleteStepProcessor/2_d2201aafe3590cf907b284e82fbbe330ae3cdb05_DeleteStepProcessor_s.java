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
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.XmlnsNamespace;
 import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.DeleteSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.MatchSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xml.saxon.SaxonProcessor;
 import org.trancecode.xml.saxon.SaxonProcessorDelegate;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * {@code p:delete}.
  * 
  * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.delte">p:delete</a>
  */
 public final class DeleteStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(DeleteStepProcessor.class);
 
     @Override
     public QName stepType()
     {
         return XProcSteps.DELETE;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final XdmNode source = input.readNode(XProcPorts.SOURCE);
         final String match = input.getOptionValue(XProcOptions.MATCH);
         LOG.trace("match = {}", match);
 
         final SaxonProcessorDelegate delete = new DeleteSaxonProcessorDelegate()
         {
             @Override
             public void attribute(final XdmNode node, final SaxonBuilder builder)
             {
                 if (XmlnsNamespace.instance().uri().equals(node.getNodeName().getNamespaceURI()))
                 {
                     throw XProcExceptions.xc0062(input.getLocation(), node);
                 }
 
                 super.attribute(node, builder);
             }
         };
         final SaxonProcessor matchProcessor = new SaxonProcessor(input.pipelineContext().getProcessor(),
                 new MatchSaxonProcessorDelegate(input.pipelineContext().getProcessor(), match, input.step().getNode(),
                         delete, new CopyingSaxonProcessorDelegate()));
 
         final XdmNode result = matchProcessor.apply(source);
         output.writeNodes(XProcPorts.RESULT, result);
     }
 }

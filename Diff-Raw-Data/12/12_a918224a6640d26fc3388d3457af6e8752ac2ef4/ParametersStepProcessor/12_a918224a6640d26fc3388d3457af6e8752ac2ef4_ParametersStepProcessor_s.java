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
 
 import com.google.common.collect.ImmutableSet;
 import java.util.Map;
 import java.util.Set;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.XProcXmlModel;
 import org.trancecode.xproc.port.XProcPorts;
 
 /**
  * {@code p:parameters}.
  * 
  * @author Emmanuel Tourdot
  * @see <a href="http://www.w3.org/TR/xproc/#c.parameters">p:parameters</a>
  */
 @ExternalResources(read = false, write = false)
 public final class ParametersStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(ParametersStepProcessor.class);
     private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.ELEMENT, XdmNodeKind.ATTRIBUTE,
             XdmNodeKind.PROCESSING_INSTRUCTION);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.PARAMETERS;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final Map<QName, String> parameters = input.getParameters(XProcPorts.PARAMETERS);
         LOG.debug("parameters = {}", parameters);
         final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                 .getUnderlyingConfiguration());
         builder.startDocument();
         builder.startElement(XProcXmlModel.Elements.PARAM_SET);
         for (final Map.Entry<QName, String> entry : parameters.entrySet())
         {
             builder.startElement(XProcXmlModel.Elements.PARAM);
            builder.attribute(entry.getKey(), entry.getValue());
             builder.endElement();
         }
         builder.endElement();
         builder.endDocument();
         output.writeNodes(XProcPorts.RESULT, builder.getNode());
 
     }
 }

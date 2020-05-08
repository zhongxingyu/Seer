 /*
  * Copyright (C) 2010 Herve Quiroz
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
 package org.trancecode.xproc.step;
 
 import net.sf.saxon.s9api.QName;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * {@code p:wrap-sequence}.
  * 
  * @author Herve Quiroz
  * @see <a
  *      href="http://www.w3.org/TR/xproc/#c.wrap-sequence">p:wrap-sequence</a>
  */
 @ExternalResources(read = false, write = false)
 public final class WrapSequenceStepProcessor extends AbstractStepProcessor
 {
     @Override
     public QName getStepType()
     {
         return XProcSteps.WRAP_SEQUENCE;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                 .getUnderlyingConfiguration());
 
         final String wrapperLocalName = input.getOptionValue(XProcOptions.WRAPPER);
         final String wrapperPrefix = input.getOptionValue(XProcOptions.WRAPPER_PREFIX, null);
         final String wrapperNamespaceUri = input.getOptionValue(XProcOptions.WRAPPER_NAMESPACE, null);
        final QName wrapper;
        if (wrapperPrefix == null || wrapperNamespaceUri == null)
        {
            wrapper = new QName(wrapperLocalName, input.getStep().getNode());
        }
        else
        {
            wrapper = new QName(wrapperPrefix, wrapperNamespaceUri, wrapperLocalName);
        }

         // TODO handle 'group-adjacent' option
 
         builder.startDocument();
         builder.startElement(wrapper, input.getStep().getNode());

        /*
         * for (final XdmNode inputDocument :
         * input.readNodes(XProcPorts.SOURCE)) {
         * builder.nodes(SaxonAxis.childElement(inputDocument)); }
         */
         builder.nodes(input.readNodes(XProcPorts.SOURCE));

         builder.endElement();
         builder.endDocument();
 
         output.writeNodes(XProcPorts.RESULT, builder.getNode());
     }
 }

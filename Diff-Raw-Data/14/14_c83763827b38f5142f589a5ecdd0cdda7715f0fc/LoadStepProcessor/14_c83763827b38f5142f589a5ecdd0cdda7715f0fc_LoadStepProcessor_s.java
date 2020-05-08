 /*
  * Copyright (C) 2008 Herve Quiroz
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
 
 import javax.xml.transform.Source;
 
 import net.sf.saxon.s9api.DocumentBuilder;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.Jaxp;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * @author Herve Quiroz
  */
 public final class LoadStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(LoadStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.LOAD;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final String href = input.getOptionValue(XProcOptions.HREF);
         assert href != null;
         LOG.trace("href = {}", href);
 
         final boolean validate = Boolean.parseBoolean(input.getOptionValue(XProcOptions.DTD_VALIDATE));
         LOG.trace("dtd-validate = {}", validate);
 
         final XdmNode document;
         try
         {
             final Source source = input.getPipelineContext().getUriResolver()
                     .resolve(href, input.getBaseUri().toString());
             try
             {
                 final DocumentBuilder documentBuilder = input.getPipelineContext().getProcessor().newDocumentBuilder();
                 documentBuilder.setDTDValidation(validate);
                 document = documentBuilder.build(source);
             }
             finally
             {
                 Jaxp.closeQuietly(source, LOG);
             }
         }
         catch (final Exception e)
         {
            throw XProcExceptions.xd0011(input.getLocation(), href, e);
         }
 
         output.writeNodes(XProcPorts.RESULT, document);
     }
 }

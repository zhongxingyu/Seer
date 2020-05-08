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
 
 import com.fasterxml.uuid.Generators;
 import com.fasterxml.uuid.impl.NameBasedGenerator;
 import com.fasterxml.uuid.impl.RandomBasedGenerator;
 import com.fasterxml.uuid.impl.TimeBasedGenerator;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
 import java.util.EnumSet;
 import java.util.UUID;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xml.saxon.SaxonProcessor;
 import org.trancecode.xml.saxon.SaxonProcessorDelegate;
 import org.trancecode.xml.saxon.SaxonProcessorDelegates;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * @author Emmanuel Tourdot
  */
 @ExternalResources(read = false, write = false)
 public final class UuidStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(UuidStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.UUID;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
         final String match = input.getOptionValue(XProcOptions.MATCH);
         assert match != null;
         LOG.trace("match = {}", match);
         final String version = input.getOptionValue(XProcOptions.VERSION, "4");
         LOG.trace("version = {}", version);
 
         final UUID uuid = getUuid(version, input.getStep());
         LOG.trace("uuid = {}", uuid.toString());
         final String textUuid = uuid.toString();
 
         final SaxonProcessorDelegate uuidReplace = new CopyingSaxonProcessorDelegate()
         {
             private void replace(final XdmNode node, final SaxonBuilder builder)
             {
                 LOG.trace("{@method} node = {}", node.getNodeName());
                 if (node.getNodeKind() == XdmNodeKind.ATTRIBUTE)
                 {
                     builder.attribute(node.getNodeName(), textUuid);
                 }
                 else
                 {
                     builder.text(textUuid);
                 }
             }
 
             @Override
             public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
             {
                 replace(node, builder);
                 return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
             }
 
             @Override
             public void text(final XdmNode node, final SaxonBuilder builder)
             {
                 replace(node, builder);
             }
 
             @Override
             public void comment(final XdmNode node, final SaxonBuilder builder)
             {
                 replace(node, builder);
             }
 
             @Override
             public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
             {
                 replace(node, builder);
             }
 
             @Override
             public void attribute(final XdmNode node, final SaxonBuilder builder)
             {
                 replace(node, builder);
             }
         };
         final SaxonProcessor matchProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                 SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                         .getStep().getNode(), uuidReplace, new CopyingSaxonProcessorDelegate()));
 
         final XdmNode result = matchProcessor.apply(sourceDocument);
         output.writeNodes(XProcPorts.RESULT, result);
     }
 
     private static UUID getUuid(final String version, final Step inputStep)
     {
         try
         {
             switch (Integer.parseInt(version))
             {
                 case 1:
                     final TimeBasedGenerator tUuidGen = Generators.timeBasedGenerator();
                     return tUuidGen.generate();
                 case 3:
                    final NameBasedGenerator nUuidGen = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL,
                                 MessageDigest.getInstance("MD5"));
                     return nUuidGen.generate("tubular_uuid");
                 case 4:
                    final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
                    final RandomBasedGenerator rUuidGen = Generators.randomBasedGenerator(sr);
                     return rUuidGen.generate();
                 default:
                     throw XProcExceptions.xc0060(inputStep.getLocation());
             }
         }
         catch (final NumberFormatException e)
         {
             throw XProcExceptions.xc0060(inputStep.getLocation());
         }
        catch (NoSuchAlgorithmException e)
        {
            throw XProcExceptions.xc0060(inputStep.getLocation());
        }
        catch (NoSuchProviderException e)
        {
            throw XProcExceptions.xc0060(inputStep.getLocation());
        }
     }
 }

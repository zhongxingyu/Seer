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
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ListMultimap;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.collection.TcMaps;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.PipelineContext;
 import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.port.PortReference;
 
 /**
  * @author Herve Quiroz
  */
 public abstract class AbstractStepProcessor implements StepProcessor
 {
     private static final Logger LOG = Logger.getLogger(AbstractStepProcessor.class);
 
     @Override
     public final Environment run(final Step step, final Environment environment)
     {
         LOG.trace("{@method} step = {}", step.getName());
         assert stepType().equals(step.getType()) || stepType().equals(XProcSteps.ANY);
 
         try
         {
             final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
 
            final StepInput input = new StepInput(step, environment);
             final StepOutput output = new StepOutput(step);
             execute(input, output);
 
             Environment resultEnvironment = stepEnvironment;
             for (final Entry<String, Collection<XdmNode>> port : output.ports.asMap().entrySet())
             {
                 resultEnvironment = resultEnvironment.writeNodes(step.getPortReference(port.getKey()), port.getValue());
 
             }
 
             return resultEnvironment.setupOutputPorts(step);
         }
         catch (final XProcException e)
         {
             throw (XProcException) e.fillInStackTrace();
         }
         catch (final Exception e)
         {
             throw new IllegalStateException(String.format("Error while executing step %s", step.getName()), e);
         }
     }
 
     protected static final class StepInput
     {
         private final Environment environment;
         private Map<QName, String> parameters;
         private final Step step;
 
         public StepInput(final Step step, final Environment environment)
         {
             this.step = step;
             this.environment = environment;
         }
 
         public PipelineContext pipelineContext()
         {
             return environment.getPipelineContext();
         }
 
         public URI baseUri()
         {
             return environment.getBaseUri();
         }
 
         public XdmNode newResultElement(final String value)
         {
             return environment.newResultElement(value);
         }
 
         public Step step()
         {
             return step;
         }
 
         public XdmNode readNode(final String portName)
         {
            return environment.readNode(PortReference.newReference(step.getName(), portName));
         }
 
         public Iterable<XdmNode> readNodes(final String portName)
         {
            return environment.readNodes(PortReference.newReference(step.getName(), portName));
         }
 
         public String getOptionValue(final QName name)
         {
             return getOptionValue(name, null);
         }
 
         public String getOptionValue(final QName name, final String defaultValue)
         {
             Preconditions.checkArgument(step.hasOptionDeclared(name), "no such option %s on step %s", name,
                     step.getType());
             return environment.getVariable(name, defaultValue);
         }
 
         public Map<QName, String> parameters(final String portName)
         {
             if (parameters == null)
             {
                 parameters = ImmutableMap.copyOf(environment.readParameters(step.getPortReference(portName)));
             }
 
             return parameters;
         }
 
         public String getParameterValue(final QName name)
         {
             return getParameterValue(name, null);
         }
 
         public String getParameterValue(final QName name, final String defaultValue)
         {
             return TcMaps.get(parameters, name, defaultValue);
         }
     }
 
     protected static final class StepOutput
     {
         private final ListMultimap<String, XdmNode> ports = ArrayListMultimap.create();
         private final Step step;
 
         public StepOutput(final Step step)
         {
             this.step = step;
         }
 
         public void writeNodes(final String portName, final XdmNode... nodes)
         {
             writeNodes(portName, ImmutableList.copyOf(nodes));
         }
 
         public void writeNodes(final String portName, final Iterable<XdmNode> nodes)
         {
             Preconditions.checkArgument(step.hasPortDeclared(portName), "no such port %s on step %s", portName,
                     step.getType());
             ports.putAll(portName, nodes);
         }
     }
 
     protected abstract void execute(final StepInput input, final StepOutput output) throws Exception;
 }

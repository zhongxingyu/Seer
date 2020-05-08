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
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ListMultimap;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmValue;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.HasLocation;
 import org.trancecode.xml.Location;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.PipelineContext;
 import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.event.AfterExecuteStepEvent;
import org.trancecode.xproc.event.BeforeExecuteStepEvent;
 
 /**
  * Base class for {@link StepProcessor} implementations.
  * 
  * @author Herve Quiroz
  */
 public abstract class AbstractStepProcessor implements StepProcessor
 {
     private static final Logger LOG = Logger.getLogger(AbstractStepProcessor.class);
 
     @Override
     public final Environment run(final Step step, final Environment environment)
     {
         LOG.trace("{@method} step = {} ; type = {}", step.getName(), step.getType());
         assert getStepType().equals(step.getType()) || getStepType().equals(XProcSteps.ANY);
 
        environment.getPipelineContext().getEventDispatcher()
                .notify(new BeforeExecuteStepEvent(environment.getPipeline(), step, environment));

         try
         {
             final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
 
             final StepInput input = new StepInput(step, stepEnvironment);
             final StepOutput output = new StepOutput(step);
             execute(input, output);
 
             Environment resultEnvironment = stepEnvironment;
             for (final Entry<String, Collection<XdmNode>> port : output.ports.asMap().entrySet())
             {
                 resultEnvironment = resultEnvironment.writeNodes(step.getPortReference(port.getKey()), port.getValue());
             }
 
             resultEnvironment = resultEnvironment.setupOutputPorts(step);
             Steps.writeLogs(step, resultEnvironment);
 
            environment.getPipelineContext().getEventDispatcher()
                    .notify(new AfterExecuteStepEvent(environment.getPipeline(), step, environment, resultEnvironment));

             return resultEnvironment;
         }
         catch (final XProcException e)
         {
             throw e;
         }
         catch (final Exception e)
         {
             throw new IllegalStateException(String.format("Error while executing step %s", step.getName()), e);
         }
     }
 
     /**
      * Input for a step execution.
      */
     protected static final class StepInput implements HasLocation
     {
         private final Environment environment;
         private final Step step;
 
         private StepInput(final Step step, final Environment environment)
         {
             this.step = step;
             this.environment = environment;
         }
 
         /**
          * Returns the {@link Location} of the current step in the pipeline.
          */
         @Override
         public Location getLocation()
         {
             return step.getLocation();
         }
 
         /**
          * Returns the pipeline context, from which extra facilities can be
          * retrieved.
          */
         public PipelineContext getPipelineContext()
         {
             return environment.getPipelineContext();
         }
 
         /**
          * Returns the base URI of the pipeline being executed.
          */
         public URI getBaseUri()
         {
             return environment.getBaseUri();
         }
 
         /**
          * Creates a {@code c:result} element to be used as the result of step
          * execution.
          * 
          * @see <A href="http://www.w3.org/TR/xproc/#cv.result">c:result</a>
          */
         public XdmNode newResultElement(final String value)
         {
             return environment.newResultElement(value);
         }
 
         /**
          * Returns the step being executed.
          */
         public Step getStep()
         {
             return step;
         }
 
         /**
          * Reads a single node from the specified port.
          * <p>
          * The port is necessarily a declared one from the current step.
          */
         public XdmNode readNode(final String portName)
         {
             return environment.readNode(step.getPortReference(portName));
         }
 
         /**
          * Reads nodes from the specified port.
          * <p>
          * The port is necessarily a declared one from the current step.
          */
         public Iterable<XdmNode> readNodes(final String portName)
         {
             return environment.readNodes(step.getPortReference(portName));
         }
 
         /**
          * Returns the value of an option from the current step.
          */
         public String getOptionValue(final QName name)
         {
             return getOptionValue(name, null);
         }
 
         /**
          * Returns the value of an option from the current step or the specified
          * default value if there is no value for this option on the current
          * step.
          */
         public String getOptionValue(final QName name, final String defaultValue)
         {
             Preconditions.checkArgument(step.hasOptionDeclared(name), "no such option %s on step %s", name,
                     step.getType());
             return environment.getVariable(name, defaultValue);
         }
 
         /**
          * Returns a map of parameters for the specified port.
          */
         public Map<QName, String> getParameters(final String portName)
         {
             return environment.readParameters(step.getPortReference(portName));
         }
 
         /**
          * Evaluates an XPath query.
          */
         public XdmValue evaluateXPath(final String select)
         {
             return environment.evaluateXPath(select);
         }
 
         /**
          * Evaluates an XPath query with the specified node as a context.
          */
         public XdmValue evaluateXPath(final String select, final XdmNode xpathContextNode)
         {
             return environment.evaluateXPath(select, xpathContextNode);
         }
     }
 
     /**
      * Input of a step execution.
      */
     protected static final class StepOutput
     {
         private final ListMultimap<String, XdmNode> ports = ArrayListMultimap.create();
         private final Step step;
 
         private StepOutput(final Step step)
         {
             this.step = step;
         }
 
         /**
          * Writes the specified nodes to a given port.
          */
         public void writeNodes(final String portName, final XdmNode... nodes)
         {
             writeNodes(portName, ImmutableList.copyOf(nodes));
         }
 
         /**
          * Writes the specified nodes to a given port.
          */
         public void writeNodes(final String portName, final Iterable<XdmNode> nodes)
         {
             Preconditions.checkArgument(step.hasPortDeclared(portName), "no such port %s on step %s", portName,
                     step.getType());
             ports.putAll(portName, nodes);
         }
     }
 
     protected abstract void execute(final StepInput input, final StepOutput output) throws Exception;
 }

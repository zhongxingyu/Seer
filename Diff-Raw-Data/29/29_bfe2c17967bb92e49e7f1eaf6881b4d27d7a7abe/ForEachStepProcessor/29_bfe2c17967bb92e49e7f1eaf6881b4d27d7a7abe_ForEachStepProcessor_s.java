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
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.functions.ExtensionFunctionCall;
 import net.sf.saxon.functions.ExtensionFunctionDefinition;
 import net.sf.saxon.om.SequenceIterator;
 import net.sf.saxon.om.SingletonIterator;
 import net.sf.saxon.om.StructuredQName;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.value.Int64Value;
 import net.sf.saxon.value.SequenceType;
 import org.trancecode.concurrent.TcFutures;
 import org.trancecode.core.TcThreads;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.XProcXmlModel;
 import org.trancecode.xproc.binding.InlinePortBinding;
 import org.trancecode.xproc.port.EnvironmentPort;
 import org.trancecode.xproc.port.Port;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.xpath.AbstractXPathExtensionFunction;
 
 /**
  * @author Herve Quiroz
  */
 public final class ForEachStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(ForEachStepProcessor.class);
 
     private static final ThreadLocal<Integer> iterationPosition = new ThreadLocal<Integer>();
     private static final ThreadLocal<Integer> iterationSize = new ThreadLocal<Integer>();
 
     public static final class IterationPositionXPathExtensionFunction extends AbstractXPathExtensionFunction
     {
         @Override
         public ExtensionFunctionDefinition getExtensionFunctionDefinition()
         {
             return new ExtensionFunctionDefinition()
             {
                 private static final long serialVersionUID = -2376250179411225176L;
 
                 @Override
                 public StructuredQName getFunctionQName()
                 {
                     return XProcXmlModel.xprocNamespace().newStructuredQName("iteration-position");
                 }
 
                 @Override
                 public int getMinimumNumberOfArguments()
                 {
                     return 0;
                 }
 
                 @Override
                 public SequenceType[] getArgumentTypes()
                 {
                     return new SequenceType[0];
                 }
 
                 @Override
                 public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
                 {
                     return SequenceType.SINGLE_INTEGER;
                 }
 
                 @Override
                 public ExtensionFunctionCall makeCallExpression()
                 {
                     return new ExtensionFunctionCall()
                     {
                         private static final long serialVersionUID = -8363336682570398286L;
 
                         @Override
                         public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                                 throws XPathException
                         {
                             return SingletonIterator.makeIterator(Int64Value.makeIntegerValue(iterationPosition.get()));
                         }
                     };
                 }
             };
         }
     }
 
     public static final class IterationSizeXPathExtensionFunction extends AbstractXPathExtensionFunction
     {
         @Override
         public ExtensionFunctionDefinition getExtensionFunctionDefinition()
         {
             return new ExtensionFunctionDefinition()
             {
                 private static final long serialVersionUID = -2376250179411225176L;
 
                 @Override
                 public StructuredQName getFunctionQName()
                 {
                     return XProcXmlModel.xprocNamespace().newStructuredQName("iteration-size");
                 }
 
                 @Override
                 public int getMinimumNumberOfArguments()
                 {
                     return 0;
                 }
 
                 @Override
                 public SequenceType[] getArgumentTypes()
                 {
                     return new SequenceType[0];
                 }
 
                 @Override
                 public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
                 {
                     return SequenceType.SINGLE_INTEGER;
                 }
 
                 @Override
                 public ExtensionFunctionCall makeCallExpression()
                 {
                     return new ExtensionFunctionCall()
                     {
                         private static final long serialVersionUID = -8363336682570398286L;
 
                         @Override
                         public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                                 throws XPathException
                         {
                             return SingletonIterator.makeIterator(Int64Value.makeIntegerValue(iterationSize.get()));
                         }
                     };
                 }
             };
         }
     }
 
     @Override
     public Step stepDeclaration()
     {
         final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.ITERATION_SOURCE).setSequence(true),
                 Port.newOutputPort(XProcPorts.RESULT).setSequence(true));
         return Step.newStep(XProcSteps.FOR_EACH, this, true).declarePorts(ports);
     }
 
     @Override
     public QName stepType()
     {
         return XProcSteps.FOR_EACH;
     }
 
     private Port newIterationPort(final Step step, final XdmNode node)
     {
         return Port.newInputPort(step.getName(), XProcPorts.CURRENT, step.getLocation()).setPrimary(false)
                 .setSequence(false).setPortBindings(new InlinePortBinding(node, step.getLocation()));
     }
 
     @Override
     public Environment run(final Step step, final Environment environment)
     {
         LOG.trace("step = {}", step.getName());
         assert step.getType().equals(XProcSteps.FOR_EACH);
 
         final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
 
         final List<XdmNode> inputNodes = ImmutableList.copyOf(stepEnvironment.readNodes(step
                 .getPortReference(XProcPorts.ITERATION_SOURCE)));
         final int iterationSize = inputNodes.size();
         final List<Callable<Iterable<XdmNode>>> tasks = Lists.newArrayListWithCapacity(inputNodes.size());
         for (int i = 0; i < inputNodes.size(); i++)
         {
             final int iterationPosition = i + 1;
             final XdmNode inputNode = inputNodes.get(i);
             tasks.add(new Callable<Iterable<XdmNode>>()
             {
                 @Override
                 public Iterable<XdmNode> call()
                 {
                     LOG.trace("iteration {}/{}: {}", iterationPosition, iterationSize, inputNode);
 
                     final int oldIterationPosition = TcThreads.set(ForEachStepProcessor.iterationPosition,
                             iterationPosition, -1);
                     final int oldIterationSize = TcThreads.set(ForEachStepProcessor.iterationSize, iterationSize, -1);
                     try
                     {
                         final Port iterationPort = newIterationPort(step, inputNode);
                         final Environment iterationEnvironment = environment.newChildStepEnvironment()
                                 .addPorts(EnvironmentPort.newEnvironmentPort(iterationPort, environment))
                                 .setDefaultReadablePort(step.getPortReference(XProcPorts.CURRENT));
                         final Environment resultEnvironment = runSteps(step.getSubpipeline(), iterationEnvironment);
                         return resultEnvironment.getDefaultReadablePort().readNodes();
                     }
                     finally
                     {
                         TcThreads.set(ForEachStepProcessor.iterationPosition, oldIterationPosition);
                         TcThreads.set(ForEachStepProcessor.iterationSize, oldIterationSize);
                     }
                 }
             });
         }
         final Iterable<Future<Iterable<XdmNode>>> futureResultNodes = TcFutures.submit(environment.getPipelineContext()
                 .getExecutor(), tasks);
         final Iterable<XdmNode> resultNodes;
         try
         {
            resultNodes = Iterables.concat(TcFutures.get(futureResultNodes));
         }
         catch (final ExecutionException e)
         {
            TcFutures.cancel(futureResultNodes);
            throw Throwables.propagate(e);
         }
         catch (final InterruptedException e)
         {
             throw new IllegalStateException(e);
         }
 
         return stepEnvironment.writeNodes(step.getPortReference(XProcPorts.RESULT), resultNodes).setupOutputPorts(step);
     }
 }

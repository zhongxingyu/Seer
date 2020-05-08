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
 import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.trancecode.concurrent.TcFutures;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Environment;
 
 /**
  * @author Herve Quiroz
  */
 public abstract class AbstractCompoundStepProcessor implements StepProcessor
 {
     private static final Logger LOG = Logger.getLogger(AbstractCompoundStepProcessor.class);
 
     @Override
     public Environment run(final Step step, final Environment environment)
     {
         LOG.trace("step = {}", step.getName());
         assert step.isCompoundStep();
 
         environment.setCurrentEnvironment();
 
         final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
         final Environment resultEnvironment = runSteps(step.getSubpipeline(), stepEnvironment);
 
         return stepEnvironment.setupOutputPorts(step, resultEnvironment);
     }
 
     protected Environment runSteps(final Iterable<Step> steps, final Environment environment)
     {
         LOG.trace("steps = {}", steps);
 
         final Environment initialEnvironment = environment.newChildStepEnvironment();
 
         final Map<Step, Step> stepDependencies = Step.getSubpipelineStepDependencies(steps);
         final Map<Step, Future<Environment>> stepResults = new ConcurrentHashMap<Step, Future<Environment>>();
         final List<Future<Environment>> results = Lists.newArrayList();
         final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
         for (final Step step : steps)
         {
             final Future<Environment> result = environment.getPipelineContext().getExecutor()
                     .submit(new Callable<Environment>()
                     {
                         @Override
                         public Environment call() throws Exception
                         {
                             // shortcut in case an error was reported by another
                             // task
                             if (error.get() != null)
                             {
                                 throw new IllegalStateException(error.get());
                             }
 
                             final Step dependency = stepDependencies.get(step);
                             final Environment inputEnvironment;
                             if (dependency != null)
                             {
                                 try
                                 {
                                     inputEnvironment = stepResults.get(dependency).get();
                                 }
                                 catch (final ExecutionException e)
                                 {
                                     throw Throwables.propagate(e.getCause());
                                 }
                             }
                             else
                             {
                                 inputEnvironment = initialEnvironment;
                             }
 
                             Environment.setCurrentNamespaceContext(step.getNode());
                             inputEnvironment.setCurrentEnvironment();
                             return step.run(inputEnvironment);
                         }
                     });
             stepResults.put(step, result);
             results.add(result);
         }
 
         final Iterable<Environment> resultEnvironments;
         try
         {
             resultEnvironments = TcFutures.get(results);
         }
         catch (final ExecutionException e)
         {
             throw Throwables.propagate(e.getCause());
         }
         catch (final InterruptedException e)
         {
             throw new IllegalStateException(e);
         }
 
         return Iterables.getLast(resultEnvironments);
     }
 }

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
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.XProcException;
 import org.trancecode.xproc.port.EnvironmentPort;
 import org.trancecode.xproc.port.Port;
 
 /**
  * @author Herve Quiroz
  */
 public final class TryStepProcessor extends AbstractCompoundStepProcessor
 {
     public static final TryStepProcessor INSTANCE = new TryStepProcessor();
    public static final Step STEP = Step.newStep(XProcSteps.TRY, INSTANCE, true);
 
     private static final Logger LOG = Logger.getLogger(TryStepProcessor.class);
 
     private TryStepProcessor()
     {
         // single instance
     }
 
     @Override
     public QName stepType()
     {
         return XProcSteps.TRY;
     }
 
     @Override
     public Environment run(final Step step, final Environment environment)
     {
         LOG.trace("step = {}", step.getName());
         assert step.getSubpipeline().size() == 2;
 
         try
         {
             final Step groupStep = step.getSubpipeline().get(0);
             assert groupStep.getType().equals(XProcSteps.GROUP);
             final Environment resultEnvironment = groupStep.run(environment);
             return buildResultEnvironment(groupStep, resultEnvironment);
         }
         catch (final XProcException e)
         {
             final Step catchStep = step.getSubpipeline().get(1);
             assert catchStep.getType().equals(XProcSteps.CATCH);
             final Environment resultEnvironment = catchStep.run(environment);
             return buildResultEnvironment(catchStep, resultEnvironment);
         }
     }
 
     private Environment buildResultEnvironment(final Step step, final Environment environment)
     {
         Environment resultEnvironment = environment;
         for (final Port port : step.getOutputPorts())
         {
             EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port.setStepName(step.getName()),
                     environment);
             environmentPort = environmentPort.pipe(environment.getEnvironmentPort(port));
             resultEnvironment = resultEnvironment.addPorts(environmentPort);
         }
 
         final Port primaryOutputPort = step.getPrimaryOutputPort();
         if (primaryOutputPort != null)
         {
             resultEnvironment = resultEnvironment.setDefaultReadablePort(step.getPortReference(primaryOutputPort
                     .getPortName()));
         }
 
         return resultEnvironment;
     }
 }

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
 
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.EnvironmentPort;
 import org.trancecode.xproc.Port;
 import org.trancecode.xproc.Step;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.XProcPorts;
 import org.trancecode.xproc.XProcSteps;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author Herve Quiroz
  * @version $Revision$
  */
 public class Choose extends AbstractCompoundStepProcessor
 {
 	public static final Choose INSTANCE = new Choose();
 
 	private static final Iterable<Port> PORTS =
 		ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT).setSequence(false).setPrimary(false));
 	public static final Step STEP = Step.newStep(XProcSteps.CHOOSE, INSTANCE, true).declarePorts(PORTS);
 
 	private static final Logger LOG = LoggerFactory.getLogger(Choose.class);
 
 
 	private Choose()
 	{
 		// single instance
 	}
 
 
 	@Override
 	public Environment run(final Step step, final Environment environment)
 	{
 		LOG.trace("step = {}", step.getName());
 		assert step.getType().equals(XProcSteps.CHOOSE);
 		assert step.isCompoundStep();
 
 		final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
 		for (final Step whenStep : step.getSteps())
 		{
 			assert XProcSteps.WHEN_STEPS.contains(whenStep.getType());
			final Environment resultEnvironment = runSteps(Collections.singleton(whenStep), stepEnvironment);
 
 			if (resultEnvironment != null)
 			{
 				final List<EnvironmentPort> newPorts = Lists.newArrayList();
 
 				for (final Port port : whenStep.getOutputPorts())
 				{
 					final EnvironmentPort environmentPort =
 						EnvironmentPort.newEnvironmentPort(port.setStepName(step.getName()), stepEnvironment);
 					newPorts.add(environmentPort.pipe(resultEnvironment.getEnvironmentPort(port)));
 				}
 
 				final Port primaryOutputPort = whenStep.getPrimaryOutputPort();
 
				return resultEnvironment.addPorts(newPorts).setDefaultReadablePort(
					step.getName(), primaryOutputPort.getPortName());
 			}
 		}
 
 		// TODO map output ports from sub-pipeline
 
 		throw XProcExceptions.xd0004(step.getLocation());
 	}
 }

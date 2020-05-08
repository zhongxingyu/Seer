 // STEMExecutionCommandHandler.java
 package org.eclipse.stem.ui.handlers;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.stem.jobs.execution.IExecutable;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.perspectives.Simulation;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * This class is the top-level {@link IHandler} for the STEM "execution" command
  * handlers that begin and control the execution of
  * {@link org.eclipse.stem.core.experiment.Experiment}s and
  * {@link org.eclipse.stem.core.scenario.Scenario}s.
  * 
  * @see org.eclipse.stem.jobs.batch.BatchManager
  * @see org.eclipse.stem.jobs.simulation.SimulationManager
  */
 abstract public class STEMExecutionCommandHandler extends AbstractHandler
 		implements IHandler {
 	
 	/**
 	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent executionEvent)
 			throws ExecutionException {
 
 		boolean switchPerspective = false;
 		
		String name = executionEvent.getApplicationContext().getClass().getSimpleName();
		System.out.println(" execution event name = "+name);
 		
 		final ISelection selection = HandlerUtil
 				.getCurrentSelectionChecked(executionEvent);
 		
 		boolean useSimulationPerspective = true;
 
 		// Structured Selection?
 		if (selection instanceof StructuredSelection) {
 			// Yes
 			// Iterate through everything that's in the selection and put each
 			// object into the appropriate collection.
 			for (final Object obj : ((StructuredSelection) selection).toList()) {
 				// IExecutable executable = (IExecutable) ExecutableAdapterFactory.INSTANCE
 				// .adapt(obj, IExecutable.class);
 				IExecutable executable = (IExecutable) Platform
 						.getAdapterManager().getAdapter(obj, IExecutable.class); 
 				
 				// Only switch to the Simulation Persepctive when the executable is a standard
 				// run (scenario). For other executables we may want to stay in other special
 				// perspectives
				if( !(obj instanceof org.eclipse.stem.core.scenario.impl.ScenarioImpl )) {
					useSimulationPerspective = false;
				}
 				
 				// Were we successful in adapting?
 				if (executable != null) {
 					// Yes
					switchPerspective = true;
 					doit(executable);
 				} // if
 				else {
 					// Internal error
 					Activator.logError(
 							"STEM Internal error execution command applied to \""
 									+ obj.getClass().getName() + "\"", null);
 				}
 			} // for each selection
 		} // if StructuredSelection
 
 		// Flip to Simulation Perspective?
 		if (switchPerspective) {
 			// Yes
 			if(useSimulationPerspective) {
 				Activator.switchToPerspective(Simulation.ID_STEM_SIMULATION_PERSPECTIVE);
 			}else {
 				// Nothing
 				// Just don't switch Perspectives
 				// so if running, e.g., in the automated experiment perspective just stay there
 			}
 			
 		} // if flip
 
 		return null;
 	} // execute
 
 	/**
 	 * This method is implemented by subclasses to invoke the specific method of
 	 * the {@link IExecutable} that implements their command for the
 	 * {@link IExecutable}.
 	 * 
 	 * @param executable
 	 *            the {@link IExecutable}
 	 */
 	abstract protected void doit(IExecutable executable);
 
 	/**
 	 * This class is a {@link STEMExecutionCommandHandler} that invokes
 	 * {@link IExecutable#run()}.
 	 */
 	public static class RunCommandHandler extends STEMExecutionCommandHandler {
 
 		/**
 		 * @see org.eclipse.stem.ui.handlers.STEMExecutionCommandHandler#doit(org.eclipse.stem.jobs.execution.IExecutable)
 		 */
 		@Override
 		protected void doit(IExecutable executable) {
 			executable.run();
 		}
 	} // RunCommandHandler
 
 	/**
 	 * This class is a {@link STEMExecutionCommandHandler} that invokes
 	 * {@link IExecutable#pause()}.
 	 */
 	public static class PauseCommandHandler extends STEMExecutionCommandHandler {
 
 		/**
 		 * @see org.eclipse.stem.ui.handlers.STEMExecutionCommandHandler#doit(org.eclipse.stem.jobs.execution.IExecutable)
 		 */
 		@Override
 		protected void doit(IExecutable executable) {
 			executable.pause();
 		}
 	} // PauseCommandHandler
 
 	/**
 	 * This class is a {@link STEMExecutionCommandHandler} that invokes
 	 * {@link IExecutable#step()}.
 	 */
 	public static class StepCommandHandler extends STEMExecutionCommandHandler {
 
 		/**
 		 * @see org.eclipse.stem.ui.handlers.STEMExecutionCommandHandler#doit(org.eclipse.stem.jobs.execution.IExecutable)
 		 */
 		@Override
 		protected void doit(IExecutable executable) {
 			executable.step();
 		}
 	} // StepCommandHandler
 
 	/**
 	 * This class is a {@link STEMExecutionCommandHandler} that invokes
 	 * {@link IExecutable#reset()}.
 	 */
 	public static class ResetCommandHandler extends STEMExecutionCommandHandler {
 
 		/**
 		 * @see org.eclipse.stem.ui.handlers.STEMExecutionCommandHandler#doit(org.eclipse.stem.jobs.execution.IExecutable)
 		 */
 		@Override
 		protected void doit(IExecutable executable) {
 			executable.reset();
 		}
 	} // ResetCommandHandler
 
 	/**
 	 * This class is a {@link STEMExecutionCommandHandler} that invokes
 	 * {@link IExecutable#stop()}.
 	 */
 	public static class StopCommandHandler extends STEMExecutionCommandHandler {
 
 		/**
 		 * @see org.eclipse.stem.ui.handlers.STEMExecutionCommandHandler#doit(org.eclipse.stem.jobs.execution.IExecutable)
 		 */
 		@Override
 		protected void doit(IExecutable executable) {
 			executable.stop();
 		}
 	} // StopCommandHandler
 
 } // STEMExecutionCommandHandler

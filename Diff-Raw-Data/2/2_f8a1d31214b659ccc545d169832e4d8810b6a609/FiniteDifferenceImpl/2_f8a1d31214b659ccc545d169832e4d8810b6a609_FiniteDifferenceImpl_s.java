 package org.eclipse.stem.solvers.fd.impl;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.IntegrationLabel;
 import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.core.graph.SimpleDataExchangeLabelValue;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.IntegrationDecorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.core.solver.impl.SolverImpl;
 import org.eclipse.stem.core.trigger.Trigger;
 
 import org.eclipse.stem.solvers.fd.FdPackage;
 import org.eclipse.stem.solvers.fd.FiniteDifference;
 import org.eclipse.stem.ui.Activator;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Finite Difference</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * </p>
  *
  * @generated
  */
 public class FiniteDifferenceImpl extends SolverImpl implements FiniteDifference {
 	
 	// The worker jobs
 	private FdJob [] jobs;
 	// Number of threads
 	private short num_threads;
 	
 	private final static int MAX_PROGRESS_REPORTS = 5;
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public FiniteDifferenceImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public void step(STEMTime time, long timeDelta, int cycle) {
 		
 		if(!this.isInitialized()) 
 			initialize(time);
 		Activator act = org.eclipse.stem.ui.Activator.getDefault();
 		if(act != null) {
 			final Preferences preferences = act.getPluginPreferences();
 			num_threads = (short)preferences.getInt(org.eclipse.stem.ui.preferences.PreferenceConstants.SIMULATION_THREADS);
 		} else num_threads = 2; // Just so we can run inside junit test
 		
 		// Find triggers and make sure they are invoked
 		for(Decorator decorator:this.getDecorators()) {
 			if(decorator instanceof Trigger) {
 				decorator.updateLabels(time, timeDelta, cycle);
 			}
 		}		
 		
 		// First initialize the Y and temp label values from the current
 		// label values.
 		
 		for(Decorator decorator:this.getDecorators()) {
 			EList<DynamicLabel>allLabels = decorator.getLabelsToUpdate();
 			for (final Iterator<DynamicLabel> currentStateLabelIter = allLabels
 					.iterator(); currentStateLabelIter.hasNext();) {
 				if(decorator instanceof IntegrationDecorator) {
 					// It's a standard disease model with a standard disease model label
 					final IntegrationLabel iLabel = (IntegrationLabel) currentStateLabelIter.next();
 					((IntegrationLabelValue)iLabel.getProbeValue()).set((IntegrationLabelValue)iLabel.getCurrentValue());
 					((IntegrationLabelValue)iLabel.getTempValue()).set((IntegrationLabelValue)iLabel.getCurrentValue());
				}
 			}
 		}
 			
 		if(jobs == null || jobs.length != num_threads) {
 			// Initialize the jobs if not done yet or of the number of threads changes
 			jobs = new FdJob[num_threads];
 	
 			for(int i=0;i<num_threads;++i) {
 				final short threadnum = (short)i;
 				jobs[i] = new FdJob("Finite Difference Worker "+i, threadnum, this);
 			} // For each job
 		} // If not initialized
 
 		// Initialize
 	
 		for(FdJob j:jobs) {
 			j.cycle = cycle;
 			j.time = time;
 			j.timeDelta = timeDelta;
 		}
 		
 		// Schedule. Jobs can be rescheduled after finished
 		for(FdJob j:jobs) 
 			j.schedule();
 		
 		// Wait until all jobs completed
 		for(FdJob j : jobs) {
 			try {
 				j.join();
 			} catch(InterruptedException ie) {
 				Activator.logError(ie.getMessage(), ie);
 			}
 		}
 		
 		// Set the common time and step size here and validate everything is right
 		//double minStep = Double.MAX_VALUE;
 		//double currentT = jobs[0].t;
 		//for(SimJob j : jobs) {
 			// The jobs have calculated new step sizes after they finished. Pick the
 			// smallest one for the next cycle
 		//	if(j.h < minStep) minStep = j.h;
 		//	if(j.t != currentT) Activator.logError("Error, one thread was in misstep with other threads, its time was "+j.t+" versus "+currentT, new Exception());
 		//}
 		
 		//this.setCurrentX(currentT);
 		//this.setStepSize(minStep); // smallest one from above.
 	}
 	
 	/**
 	 * _step Do the step for a single thread
 	 * 
 	 * @param time
 	 * @param timeDelta
 	 * @param cycle
 	 * @param threadnum
 	 */
 	protected void _step(STEMTime time, long timeDelta, int cycle, short threadnum) {
 		// Now give each decorator a chance to update its dynamic
 		// labels in the canonical graph, but only if it is enabled. A
 		// Decorator might not be enabled if it is the action of a Trigger
 		// and the Predicate of the trigger is false.
 		EList<IntegrationDecorator> iDecorators = new BasicEList<IntegrationDecorator>();
 		for (final Iterator<Decorator> decoratorIter = this
 				.getDecorators().iterator(); decoratorIter.hasNext();) {
 			final Decorator decorator = decoratorIter.next();
 			// Is the decorator enabled?
 			if (decorator.isEnabled() && decorator instanceof IntegrationDecorator) iDecorators.add((IntegrationDecorator)decorator);
 		}
 		
 		for(IntegrationDecorator imodel:iDecorators)
 			imodel.calculateDelta(time, timeDelta, ((Decorator)imodel).getLabelsToUpdate(threadnum, num_threads));
 		for(IntegrationDecorator imodel:iDecorators)
 			imodel.applyExternalDeltas(time, timeDelta, ((Decorator)imodel).getLabelsToUpdate(threadnum, num_threads));
 		
 		for(IntegrationDecorator imodel:iDecorators)
 			updateStandardDiseaseModelLabels((Decorator)imodel, time, timeDelta, cycle, threadnum);
 				
 	}
 	
 	
 	protected void updateStandardDiseaseModelLabels(Decorator model, STEMTime time, long timeDelta, int cycle, short threadnum) {
 		
 		EList<DynamicLabel> myLabels = model.getLabelsToUpdate(threadnum, num_threads);
 		
 		IntegrationDecorator imodel = (IntegrationDecorator)model;
 		
 		int numLabels = myLabels.size();
 		int setProgressEveryNthNode = num_threads * numLabels/(MAX_PROGRESS_REPORTS);
 		if(setProgressEveryNthNode == 0) setProgressEveryNthNode = 1;
 		int n=0;
 		// Initialize the next value from the current value and add the delta
 		for (final Iterator<DynamicLabel> currentStateLabelIter = myLabels
 				.iterator(); currentStateLabelIter.hasNext();) {
 			final IntegrationLabel iLabel = (IntegrationLabel) currentStateLabelIter.next();
 			IntegrationLabelValue nextState = (IntegrationLabelValue)iLabel.getNextValue();
 			
 			IntegrationLabelValue delta = (IntegrationLabelValue)iLabel.getDeltaValue();
 			// For finite difference, we need to make sure we don't
 			// move too many people from one state to another
 			delta.adjustDelta((IntegrationLabelValue)iLabel.getCurrentValue());
 			
 			// Set delta first. This will copy non-additive values like incidence etc.
 			nextState.set(delta);
 			
 			// Add the original value
 			nextState.add((IntegrationLabelValue)iLabel.getCurrentValue());
 
 			// Do any model specific work for instance add noise
 			imodel.doModelSpecificAdjustments((LabelValue)nextState);
 			
 			// The next value is valid now.
 			iLabel.setNextValueValid(true);
 			// Now add in the population so we can compute the reciprocal
 			// next cycle.
 //			addToTotalPopulationCount(nextState.getPopulationCount());
 		
 			double progress = (double)n/(double)numLabels;
 			jobs[threadnum].setProgress(progress);
 			if(n%setProgressEveryNthNode==0) {
 				// Get the progress for all threads
 				for(int i=0;i<num_threads;++i) if(i!=threadnum && jobs[i] != null) progress+=jobs[i].getProgress();
 				progress /= num_threads;
 				model.setProgress(progress);
 			}
 			++n;
 			
 		}
 		// Done
 //		this.setProgress(1.0);
 	}
 	
 	/**
 	 * initialize before simulation begins. Rewind/forward any population model
 	 * values to the start time of the 
 	 * @param time
 	 */
 	private void initialize(STEMTime time) {
 		EList<Decorator> redoList = new BasicEList<Decorator>();
 		
 		boolean redo = false;
 		for(Decorator d:this.getDecorators()) {
 			if(d instanceof IntegrationDecorator) {
 				EList<DynamicLabel> labels = d.getLabelsToUpdate();
 				for(DynamicLabel l:labels) {
 					if(l instanceof IntegrationLabel) {
 						IntegrationLabel il = (IntegrationLabel)l;
 						il.reset(time);
 						if(((SimpleDataExchangeLabelValue)il.getDeltaValue()).getAdditions() > 0.0 ||
 								((SimpleDataExchangeLabelValue)il.getDeltaValue()).getSubstractions() > 0.0)
 							redo = true;
 					}
 				}
 			}
 			if(!redo)redoList.add(d);
 		}
 		// Fix decorators with unapplied deltas
 		if(redo) {
 			for(Decorator d:redoList) {
 				if(d instanceof IntegrationDecorator) {
 					EList<DynamicLabel> labels = d.getLabelsToUpdate();
 					for(DynamicLabel l:labels) {
 						if(l instanceof IntegrationLabel) {
 							IntegrationLabel il = (IntegrationLabel)l;
 							il.reset(time);
 						}
 					}
 				}
 			}
 		}
 		this.setInitialized(true);
 	}
 	/**
 	 * Reset the solver
 	 * @generated NOT
 	 */
 	@Override
 	public void reset() {
 		this.setInitialized(false);
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return FdPackage.Literals.FINITE_DIFFERENCE;
 	}
 
 } //FiniteDifferenceImpl

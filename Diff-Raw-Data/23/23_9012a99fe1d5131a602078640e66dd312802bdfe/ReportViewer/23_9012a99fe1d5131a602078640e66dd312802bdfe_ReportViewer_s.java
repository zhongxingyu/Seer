 // ReportViewer.java
 package org.eclipse.stem.ui.reports.views;
 
 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.dialogs.DialogSettings;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.jobs.simulation.ISimulationManagerListener;
 import org.eclipse.stem.jobs.simulation.Simulation;
 import org.eclipse.stem.jobs.simulation.SimulationManager;
 import org.eclipse.stem.jobs.simulation.SimulationManagerEvent;
 import org.eclipse.stem.ui.reports.Activator;
 import org.eclipse.stem.ui.views.geographic.map.MapControl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * This class presents "views" of the running simulations.
  */
 public class ReportViewer extends Viewer implements ISelectionChangedListener,
 		ISimulationManagerListener {
 
 	/**
 	 * This is the collection of {@link Identifiable}s to display (
 	 * {@link Node}s for now)
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation}s that should be
 	 * displayed.
 	 */
 	private final Set<Identifiable> identifiablesToDisplay = new LinkedHashSet<Identifiable>();
 
 	/**
 	 * This is the {@link SimulationManager} that is the input to the viewer. It
 	 * maintains a collection of active
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation}s in the system.
 	 * This viewer listens to it to discover when new
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation}s are created and
 	 * old ones disappear.
 	 * 
 	 * @see #simulationsChanged(SimulationManagerEvent)
 	 */
 	private SimulationManager simulationManager;
 
 	/**
 	 * The {@link Identifiable} that was most recently selected by a user
 	 * clicking on one of the
 	 * {@link org.eclipse.stem.ui.views.geographic.map.MapControl}s, or
 	 * <code>null</code> if none has been selected.
 	 */
 	private ISelection selection = null;
 
 	/**
 	 * This is the top-level control of the viewer. It contains the
 	 * {@link org.eclipse.stem.ui.views.geographic.map.MapControl}s that
 	 * display the current state of the
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation}s.
 	 */
 	final Composite composite;
 
 	/**
 	 * the user's preferences are stored in {@link DialogSettings}
 	 */
 	private DialogSettings masterSettings = null;
 
 	/**
 	 * Set of preferred Locations to monitor read from the
 	 * {@link DialogSettings}
 	 */
 	private final Set<String> monitorSet = new HashSet<String>();
 
 	/**
 	 * root key element for the {@link DialogSettings} xml file
 	 */
 	private static final String ROOT_KEY = "root";
 
 	/**
 	 * Constants for saving ReportViewPreferences
 	 */
 	public static final String REPORTVIEWPREFERENCES_FILENAME = "Preferences.txt";
 
 	/**
 	 * TODO JFK
 	 */
 	public static final String LOCATION_PREFERENCE_KEY = "location.ids";
 
 	// get the path
 	private static final IPath PATH = Activator.getDefault().getStateLocation();
 
 	/**
 	 * This factory is used to create instances of {@link ReportControl} in the
 	 * method {@link #populateView()}
 	 */
 	private final ReportControlFactory rcf;
 
 	/**
 	 * @param parent
 	 *            the SWT parent of the control that makes up the viewer
 	 * @param rcf
 	 *            the factory that creates instance of {@link ReportControl}
 	 */
 	public ReportViewer(final Composite parent, final ReportControlFactory rcf) {
 
 		composite = new Composite(parent, SWT.NONE);
 		final FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
 		composite.setLayout(compositeLayout);
 
 		// Remember the factory that we'll use to create instances of
 		// ReportControl
 		this.rcf = rcf;
 		refresh();
 		composite.pack();
 
 	} // ReportViewer
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#getControl()
 	 */
 	@Override
 	public Control getControl() {
 		return composite;
 	} // getControl
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#getSelection()
 	 */
 	@Override
 	public ISelection getSelection() {
 		return selection;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#refresh()
 	 */
 	@Override
 	public void refresh() {
 		if (simulationManager != null) {
 			// Yes
 			List<ISimulation> allSimulations = null;
 			List<ISimulation> activeSimulations = simulationManager.getActiveSimulations();
 			if((activeSimulations!=null)&&(activeSimulations.size()>=1)) {
 				for (int i=0 ; i < activeSimulations.size(); i ++) {
 					ISimulation sim = activeSimulations.get(i);
 					updatePreferenceState(sim);
 				}
 			}
 			
 			if (simulationManager != null) {
 				allSimulations = simulationManager.getActiveSimulations();
 				addToDisplayedIdentifiableSet(allSimulations);
 			}
 
 			identifiablesToDisplay.clear();
 			identifiablesToDisplay.addAll(getIdentifiablesInSimulation(simulationManager.getActiveSimulations()));
 		} // if
 		// calling with null means this is a new identifiable for an 
 		// active simulation but not a new simulation
 		if(rcf.getFactoryType().equals(ReportControlFactory.IDENTIFABLE_TYPE)){
 			populateView();
 		} else {
 			populateView(null);
 		}
 		
 	} // refresh
 
 	/**
 	 * 
 	 * @param ident
 	 */
 	public void addMonitor(final Identifiable ident) {
 
 		// this fixes a bug where clicks in the
 		// Designer perspective were creating new ReportViews
 		if (!(ident instanceof Node)) {
 			return;
 		}
 		identifiablesToDisplay.add(ident);
 		populateView();
 		// preference state has changed
 		savePreferenceState();
 	} // addMonitor
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#getInput()
 	 */
 	@Override
 	public Object getInput() {
 		return simulationManager;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#setInput(java.lang.Object)
 	 */
 	@Override
 	public void setInput(final Object input) {
 		final Object oldInput = simulationManager;
 		simulationManager = (SimulationManager) input;
 		inputChanged(input, oldInput);
 	} // setInput
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	@Override
 	protected void inputChanged(final Object input, final Object oldInput) {
 		// Is there already a simulation manager?
 		if (oldInput != null) {
 			// Yes
 			((SimulationManager) oldInput).removeListener(this);
 		} // if
 
 		// Register with the SimulationManager to listen for changes in the set
 		// of active Simulations.
 		// Got input?
 		if (input != null) {
 			// Yes
 			((SimulationManager) input).addSimulationManagerListener(this);
 		} // if
 
 		// Update the viewer with the contents of the new input source
 		refresh();
 	} // inputChanged
 
 	/**
 	 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection,
 	 *      boolean)
 	 */
 	@Override
 	public void setSelection(final ISelection selection,
 			@SuppressWarnings("unused") final boolean reveal) {
 		this.selection = selection;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	public void selectionChanged(final SelectionChangedEvent event) {
 
 		// Did the event come from a ReportControl?
 		if (event.getSelectionProvider() instanceof ReportControl) {
 			// Yes
 			// Just pass it along
 			selection = event.getSelection();
 			fireSelectionChanged(event);
 		} // if
 	} // selectionChanged
 
 	/**
 	 * @see org.eclipse.stem.jobs.simulation.ISimulationManagerListener#simulationsChanged(org.eclipse.stem.jobs.simulation.SimulationManagerEvent)
 	 */
 	public void simulationsChanged(final SimulationManagerEvent event) {
 		
 		// Init the preference
 		removeFromDisplayedSimulationSet(Arrays.asList(event.getSimulationsRemoved()));
 		List<ISimulation> newSimulationList = Arrays.asList(event.getSimulationsAdded());
 		if((newSimulationList!=null)&&(newSimulationList.size() >=1)) {
 			for (int i = 0;  i < newSimulationList.size(); i ++) {
 				ISimulation sim = newSimulationList.get(i);
 				updatePreferenceState(sim);
 				addToDisplayedIdentifiableSet(sim);
 				populateView(); // the time series charts
 				// new simulation !!
 				populateView(sim);// the aggregate charts
 			}
 		}
 		
 	} // simulationsChanged
 
 	/**
 	 * @param simulations
 	 *            the {@link org.eclipse.stem.jobs.simulation.Simulation}s
 	 *            to add to the set being displayed.
 	 */
 	private void addToDisplayedIdentifiableSet(
 			final List<ISimulation> simulations) {
 		// Any simulations to add?
 		if (!simulations.isEmpty()) {
 			// Yes
 			identifiablesToDisplay
 					.addAll(getIdentifiablesInSimulation(simulations));
 		} // if any simulations
 	} // addToDisplayedSimulationSet
 
 	/**
 	 * @param simulations
 	 *            the {@link org.eclipse.stem.jobs.simulation.Simulation}s
 	 *            to add to the set being displayed.
 	 */
 	private void addToDisplayedIdentifiableSet(ISimulation sim) {		
 			identifiablesToDisplay.addAll(getIdentifiablesInSimulation(sim));
 	} // addToDisplayedSimulationSet
 
 	
 	/**
 	 * @param simulations
 	 *            the {@link org.eclipse.stem.jobs.simulation.Simulation}s
 	 *            to remove from the set being displayed.
 	 */
 	private void removeFromDisplayedSimulationSet(
 			final List<ISimulation> simulations) {
 		// Any simulations to remove?
 		if (!simulations.isEmpty()) {
 			// Yes
 			final Set<Identifiable> defunctIdentifiables = getIdentifiablesInSimulation(simulations);
 			identifiablesToDisplay.removeAll(defunctIdentifiables);
 			cleanup(defunctIdentifiables);
 		} // if
 	} // removeFromDisplayedSimulationSet
 
 	/**
 	 * cleans up old displays
 	 * 
 	 * @param defunctIdentifiables
 	 */
 	private void cleanup(final Set<Identifiable> defunctIdentifiables) {
 		if (defunctIdentifiables != null) {
 			final Iterator<Identifiable> iter = defunctIdentifiables.iterator();
 			while ((iter != null) && (iter.hasNext())) {
 				final Identifiable ident = iter.next();
 				monitorSet.remove(ident.getURI().toString());
 			} // while
 
 			final Control[] allControls = composite.getChildren();
 			for (final Control element : allControls) {
 				final ReportControl reportControl = (ReportControl) element;
				final Identifiable testIdentifiable = reportControl
						.getIdentifiable();
				if ((testIdentifiable == null)
						|| (!identifiablesToDisplay.contains(testIdentifiable))) {
					// dispose if we know it is defunct
					reportControl.remove();
				} // if
 			} // for each ReportControl
 
 		}// if set not null
 	}// cleanup
 
 	protected void removeIdentifiable(final Identifiable ident) {
 		// Any simulations to remove?
 		if (ident != null) {
 			// Yes
 			identifiablesToDisplay.remove(ident);
 
 			// preference state has changed
 			savePreferenceState();
 
 		} // if
 	} // removeFromDisplayedSimulationSet
 
 	/**
 	 * Create and dispose of {@link MapControl}s as necessary to display the
 	 * selected {@link Simulation}s.
 	 */
 	private void populateView(ISimulation sim) {
 		// THE Aggregate control is a special case. All others handled by the other populateView method
 		if( ! (this.rcf.getFactoryType().equals(ReportControlFactory.SIMULATION_TYPE))) return;
 		boolean foundControl = false;
 		// problem here when we start up
 		// Are we done?
 		if (composite.isDisposed()) {
 			// Yes
 			return;
 		} // if
 
 		selection = null;
 		
 		List<ISimulation> activeSimList = SimulationManager.getManager().getActiveSimulations();
 		Set<ISimulation> activeSimSet = new HashSet<ISimulation>();
 		activeSimSet.addAll(activeSimList);
 
 		if(sim != null) {
 			// Go through the current set of Controls looking for ones that
 			// we can reassign
 			final Control[] controls = composite.getChildren();
 			for (final Control element : controls) {
 				// Does this ReportControl have a simulation that is still valid
 				// to display?
 				final ReportControl reportControl = (ReportControl) element;
 				// THE Aggregate control is a special case
 				if(reportControl.getControlType().equals(ReportControlFactory.SIMULATION_TYPE)) {
 					if(reportControl.simulation==null) {
 						reportControl.setSimulation(sim);
 						reportControl.redraw();
 						foundControl = true;
 					}
 				} 
 			} // for each MapControl
 
 			// At this point we still could have map controls with invalid
 			// identifiables, let's get rid of them
 			for (final Control element : controls) {
 				final ReportControl reportControl = (ReportControl) element;
 				
 				if(reportControl.getControlType().equals(ReportControlFactory.SIMULATION_TYPE)) {
 					// Does this MapControl have a simulation that is still valid to
 					// display?
 					// THE Aggregate control is a special case
 					if(reportControl.simulation!=null) {
 						if(!activeSimSet.contains(reportControl.simulation)) {
 							reportControl.removeSelectionChangedListener(this);
 							reportControl.dispose();
 						}
 					}	
 				} 
 			}// for each MapControl
 			
 			if(!foundControl) {
 				// We need a new control for this simulation
 				final ReportControl reportControl = rcf.create(composite);
 				reportControl.addSelectionChangedListener(this);
 				reportControl.setSimulation(sim);
 				
 				// must listen - when the children go away we no longer want to
 				// monitor them
 
 				/**
 				 * called when a remove widget is pressed
 				 */
 				reportControl.addIRemoveListener(new IRemoveListener() {
 					/**
 					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 					 */
 					public void identifiableRemoved(final Identifiable ident) {
 						removeIdentifiable(ident);
 					}
 				});
 				reportControl.addDisposeListener(new DisposeListener() {
 					/**
 					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 					 */
 					public void widgetDisposed(
 							@SuppressWarnings("unused") final DisposeEvent e) {
 						composite.layout(true, true);
 						composite.redraw();
 					}
 				});
 			} // not found control
 		} // sim not null
 		else {
 			// If there are no simulations to display, we just put up a "blank"
 			// ReportControl
 			if (composite.getChildren().length == 0) {
 				// Yes
 				final ReportControl reportControl = rcf.create(composite);
 				reportControl.addSelectionChangedListener(this);
 
 				/**
 				 * called when a remove widget is pressed
 				 */
 				reportControl.addIRemoveListener(new IRemoveListener() {
 					/**
 					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 					 */
 					public void identifiableRemoved(final Identifiable ident) {
 						removeIdentifiable(ident);
 					}
 				});
 				reportControl.addDisposeListener(new DisposeListener() {
 					/**
 					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 					 */
 					public void widgetDisposed(
 							@SuppressWarnings("unused") final DisposeEvent e) {
 						composite.layout(true, true);
 						composite.redraw();
 					}
 				});
 			} // if
 		}
 		
 		
 		composite.layout(true, true);
 		composite.redraw();
 	} // populateView(ISimulation)
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Create and dispose of {@link MapControl}s as necessary to display the
 	 * selected {@link Simulation}s.
 	 */
 	private void populateView() {
 		// THE Aggregate control is a special case handled by the other populateView method
 		
 		if(this.rcf.getFactoryType().equals(ReportControlFactory.SIMULATION_TYPE) ) return;
 		
 		// problem here when we start up
 		// Are we done?
 		if (composite.isDisposed()) {
 			// Yes
 			return;
 		} // if
 
 		selection = null;
 		
 		// Find the Simulations that are not currently being displayed and put
 		// them into the Set undisplayedSimulations.
 		final Set<Identifiable> undisplayedIdentifiables = new HashSet<Identifiable>();
 		for (final Identifiable node : identifiablesToDisplay) {
 			// Is this one associated with a ReportControl already?
 			if (!isDisplayed(node)) {
 				// No
 				undisplayedIdentifiables.add(node);
 			} // if not displayed
 		} // for each ISimulation
 
 		// Go through the current set of Controls looking for ones that
 		// we can reassign
 		final Control[] controls = composite.getChildren();
 		for (final Control element : controls) {
 			// Does this ReportControl have a simulation that is still valid
 			// to display?
 			final ReportControl reportControl = (ReportControl) element;
 			if(reportControl.getControlType().equals(ReportControlFactory.IDENTIFABLE_TYPE)) {
 				// all other controls
 				if (!undisplayedIdentifiables.isEmpty()
 						&& !identifiablesToDisplay.contains(reportControl
 								.getIdentifiable())) {
 					// No
 					// Reassign it to one that is valid
 					final Identifiable ident = (Identifiable) undisplayedIdentifiables
 							.toArray()[0];
 					reportControl.setIdentifiable(ident);
 					undisplayedIdentifiables.remove(ident);
 				}
 			}
 		} // for each MapControl
 
 
 		// At this point we still could have map controls with invalid
 		// identifiables, let's get rid of them
 		for (final Control element : controls) {
 			final ReportControl reportControl = (ReportControl) element;
 			if(reportControl.getControlType().equals(ReportControlFactory.IDENTIFABLE_TYPE)) {
 				// Does this MapControl have a identifiable that is still valid to
 				// display?
 				final Identifiable ident = reportControl.getIdentifiable();
 				if (ident != null && !identifiablesToDisplay.contains(ident)) {
 					// No
 					reportControl.removeSelectionChangedListener(this);
 					reportControl.dispose();
 				}
 			}
 		}// for each MapControl
 		
 		// If there're any Nodes not assigned to a ReportControl,
 		// let's take care of that now.
 	
 		for (final Identifiable ident : undisplayedIdentifiables) {
 			if(ident!= null) {
 				final ReportControl reportControl = rcf.create(composite);
 				reportControl.addSelectionChangedListener(this);
 				reportControl.setIdentifiable(ident);
 				// must listen - when the children go away we no longer want to
 				// monitor them
 
 				/**
 				 * called when a remove widget is pressed
 				 */
 				reportControl.addIRemoveListener(new IRemoveListener() {
 					/**
 					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 					 */
 					public void identifiableRemoved(final Identifiable ident) {
 						removeIdentifiable(ident);
 					}
 				});
 				reportControl.addDisposeListener(new DisposeListener() {
 					/**
 					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 					 */
 					public void widgetDisposed(
 							@SuppressWarnings("unused") final DisposeEvent e) {
 						composite.layout(true, true);
 						composite.redraw();
 					}
 				});
 			}// not null
 		} // for each Identifiable
 
 		// If there are no simulations to display, we just put up a "blank"
 		// ReportControl
 		if (composite.getChildren().length == 0) {
 			// Yes
 			final ReportControl reportControl = rcf.create(composite);
 			reportControl.addSelectionChangedListener(this);
 
 			/**
 			 * called when a remove widget is pressed
 			 */
 			reportControl.addIRemoveListener(new IRemoveListener() {
 				/**
 				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 				 */
 				public void identifiableRemoved(final Identifiable ident) {
 					removeIdentifiable(ident);
 				}
 			});
 			reportControl.addDisposeListener(new DisposeListener() {
 				/**
 				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 				 */
 				public void widgetDisposed(
 						@SuppressWarnings("unused") final DisposeEvent e) {
 					composite.layout(true, true);
 					composite.redraw();
 				}
 			});
 
 		} // if
 		composite.layout(true, true);
 		composite.redraw();
 		
 	} // populateView()
 	
 	
 	
 
 	/**
 	 * 
 	 * @param simulations
 	 * @return
 	 */
 	private Set<Identifiable> getIdentifiablesInSimulation(
 			final List<ISimulation> simulationList) {
 		final Set<Identifiable> retVal = new LinkedHashSet<Identifiable>();
 		for (int i = 0; i < simulationList.size(); i++) {
 			final ISimulation simulation = simulationList.get(i);
 			/*
 			 * The default monitor locations always contains the works
 			 * (URIsuffix = ZZZ, or MonitorPreferences.DEFAULT_LOCATION_ID
 			 * However, if a scenario contains only one country then the ZZZ
 			 * nodes does not exist and the top level node will be the country
 			 * code. So here we check that the ZZZ node exists. If it does not
 			 * exist we look for three letter country code(s) and add that
 			 * (those - but there should be only one) to the list.
 			 */
 			final Graph graph = simulation.getScenario().getCanonicalGraph();
 			/*
 			 * this code was used to avoid a bug that should now be fixed
 			 * 
 			 * if (graph == null) { System.out.println("graph is null"); // try
 			 * several times for (int ii = 0 ; ii < 50; ii ++) { try {
 			 * Thread.sleep(100); } catch (Exception e) { // nothing } graph =
 			 * simulation.getScenario().getCanonicalGraph(); if (graph != null)
 			 * break; }// for 10 tries } //if graph null
 			 */
 
 			if (graph != null) {
 				// final EMap<URI, Node> allNodes = graph.getNodes();
 				// final Set<URI> allGraphURIs = allNodes.keySet();
 				// final URI defaultURI = MonitorPreferences.getDefaultURI();
 
 				/*
 				 * We won't use the following code. It creates too many
 				 * countries when scenario includes an entire continent
 				 *  // if the world URI=..ZZZ does NOT exist if
 				 * (!allGraphURIs.contains(defaultURI)) { final Iterator<URI>
 				 * iter = allGraphURIs.iterator(); while ((iter != null) &&
 				 * (iter.hasNext())) { final String uriString =
 				 * (iter.next()).toString(); final String suf =
 				 * MonitorPreferences .getURIsuffix(uriString); if
 				 * ((suf.length() == 3) && (suf.indexOf("-") == -1)) { // add
 				 * the node monitorSet.add(uriString); } // if admin0=country
 				 * level 3 letter uri } // all uri's in Graph } // if world uri
 				 * does not exist
 				 */
 
 				final Iterator<String> iter = monitorSet.iterator();
 				while ((iter != null) && (iter.hasNext())) {
 					final String key = iter.next();
 					Identifiable node = null;
 					URI locURI = null;
 					try {
 						locURI = URI.createURI(key);
 						node = graph.getNode(locURI);
 						if (node != null) {
 							retVal.add(node);
 						}
 					} catch (final Exception e) {
 						org.eclipse.stem.ui.reports.Activator.logError(
 								"ReportViewer.getIdentifiablesInSimulation() Error setting Identifiable to "
 										+ key + " ", e);
 					}
 				}// while locations to monitor
 
 			}// graph not null
 
 		} // for all simulations
 
 		return retVal;
 	}
 	
 	/**
 	 * 
 	 * @param simulation one simulation
 	 * @return
 	 */
 	private Set<Identifiable> getIdentifiablesInSimulation(ISimulation simulation) {
 		final Set<Identifiable> retVal = new LinkedHashSet<Identifiable>();
 			/*
 			 * The default monitor locations always contains the works
 			 * (URIsuffix = ZZZ, or MonitorPreferences.DEFAULT_LOCATION_ID
 			 * However, if a scenario contains only one country then the ZZZ
 			 * nodes does not exist and the top level node will be the country
 			 * code. So here we check that the ZZZ node exists. If it does not
 			 * exist we look for three letter country code(s) and add that
 			 * (those - but there should be only one) to the list.
 			 */
 			final Graph graph = simulation.getScenario().getCanonicalGraph();
 			/*
 			 * this code was used to avoid a bug that should now be fixed
 			 * 
 			 * if (graph == null) { System.out.println("graph is null"); // try
 			 * several times for (int ii = 0 ; ii < 50; ii ++) { try {
 			 * Thread.sleep(100); } catch (Exception e) { // nothing } graph =
 			 * simulation.getScenario().getCanonicalGraph(); if (graph != null)
 			 * break; }// for 10 tries } //if graph null
 			 */
 
 			if (graph != null) {
 				// final EMap<URI, Node> allNodes = graph.getNodes();
 				// final Set<URI> allGraphURIs = allNodes.keySet();
 				// final URI defaultURI = MonitorPreferences.getDefaultURI();
 
 				/*
 				 * We won't use the following code. It creates too many
 				 * countries when scenario includes an entire continent
 				 *  // if the world URI=..ZZZ does NOT exist if
 				 * (!allGraphURIs.contains(defaultURI)) { final Iterator<URI>
 				 * iter = allGraphURIs.iterator(); while ((iter != null) &&
 				 * (iter.hasNext())) { final String uriString =
 				 * (iter.next()).toString(); final String suf =
 				 * MonitorPreferences .getURIsuffix(uriString); if
 				 * ((suf.length() == 3) && (suf.indexOf("-") == -1)) { // add
 				 * the node monitorSet.add(uriString); } // if admin0=country
 				 * level 3 letter uri } // all uri's in Graph } // if world uri
 				 * does not exist
 				 */
 
 				final Iterator<String> iter = monitorSet.iterator();
 				while ((iter != null) && (iter.hasNext())) {
 					final String key = iter.next();
 					Identifiable node = null;
 					URI locURI = null;
 					try {
 						locURI = URI.createURI(key);
 						node = graph.getNode(locURI);
 						if (node != null) {
 							retVal.add(node);
 						}
 					} catch (final Exception e) {
 						org.eclipse.stem.ui.reports.Activator.logError(
 								"ReportViewer.getIdentifiablesInSimulation() Error setting Identifiable to "
 										+ key + " ", e);
 					}
 				}// while locations to monitor
 			}// graph not null
 		return retVal;
 	}
 
 	/**
 	 * @param simulation
 	 * @return
 	 */
 	private boolean isDisplayed(final Identifiable identifiable) {
 		boolean retValue = false;
 		final Control[] allControls = composite.getChildren();
 		for (final Control element : allControls) {
 			final ReportControl reportControl = (ReportControl) element;
 			final Identifiable testIdentifiable = reportControl
 					.getIdentifiable();
 			if (testIdentifiable != null
 					&& testIdentifiable.equals(identifiable)) {
 				retValue = true;
 				break;
 			} // if
 		} // for each ReportControl
 		return retValue;
 	} // isDisplayed
 
 	/**
 	 * save the state of preferred locations to monitor
 	 */
 	private void savePreferenceState() {
 
 		// we need to write preferences by active simulation
 		List<ISimulation> allSimulations = null;
 		final Set<String> nodesInSimulation = new HashSet<String>();
 		if (simulationManager != null) {
 			allSimulations = simulationManager.getActiveSimulations();
 		}
 		if ((allSimulations != null) && (allSimulations.size() >= 1)) {
 			for (int i = 0; i < allSimulations.size(); i++) {
 				nodesInSimulation.clear();
 				final ISimulation sim = allSimulations.get(i);
 				final String scenarioName = sim.getScenario().getURI()
 						.toString();
 
 				// recreate the subsection for this scenario using the locations
 				// now being monitored
 				final IDialogSettings section = new DialogSettings(scenarioName);
 
 				final Graph graph = sim.getScenario().getCanonicalGraph();
 				final Iterator<Identifiable> iter = identifiablesToDisplay
 						.iterator();
 				while ((iter != null) && (iter.hasNext())) {
 					final Identifiable ident = iter.next();
 					if (ident != null) {
 						// is this node in this simulation?
 						final Identifiable node = graph.getNode(ident.getURI());
 						if (node != null) {
 							final String id = ident.getURI().toString();
 							if (id != null) {
 								nodesInSimulation.add(id);
 							}
 						} // if node in simulation
 					}
 
 				}// all currently displayed nodes
 
 				final String[] newPrefs = new String[nodesInSimulation.size()];
 				final Iterator<String> iter2 = nodesInSimulation.iterator();
 				int icount = 0;
 				while ((iter2 != null) && (iter2.hasNext())) {
 					newPrefs[icount] = iter2.next();
 					icount++;
 				}
 
 				section.put(LOCATION_PREFERENCE_KEY, newPrefs);
 				if (masterSettings == null) {
 					masterSettings = new DialogSettings(ROOT_KEY);
 				}
 				masterSettings.addSection(section);
 				try {
 					masterSettings.save(getPrefFileName());
 				} catch (final IOException ioe) {
 					org.eclipse.stem.ui.reports.Activator
 							.logError("exception writing file ["
 									+ ioe.getMessage() + "]", ioe);
 				}
 
 			} // for all simulations
 		}// if not null
 
 	}// savePreferenceState
 
 	/**
 	 * This method uses {@link DialogSettings} to load and update the monitorSet
 	 * which contains (filters) the valid list of preferred {@link Identifiable}s
 	 * to display from the the state of the previously saved preference state.
 	 * for {@link Simulation}s that are running
 	 * 
 	 * @param runningSimulations
 	 */
 	private void updatePreferenceState(ISimulation sim) {
 		// what type of viewer? TimeSeries, PhaseSpace etc
 		if (masterSettings == null) {
 			readMasterSettingsFromFile();
 		}
 		final String scenarioName = sim.getScenario().getURI().toString();
 		IDialogSettings section = masterSettings.getSection(scenarioName);
 		if (section == null) {
 			section = new DialogSettings(scenarioName);
 			masterSettings.addSection(section);
 		}
 		// now add the preferences to the monitorSet
 		final String[] allIDs = section.getArray(LOCATION_PREFERENCE_KEY);
 		if (allIDs != null) {
 			for (final String element : allIDs) {
 				monitorSet.add(element);
 			}// for allIDs
 
 		}// not null
 	}// updatePreferenceState
 
 	/**
 	 * get the {@link DialogSettings} from the file
 	 */
 	public void readMasterSettingsFromFile() {
 		masterSettings = new DialogSettings(ROOT_KEY);
 
 		final String filename = getPrefFileName();
 		final File f = new File(filename);
 		if (f.exists()) {
 			try {
 				masterSettings.load(filename);
 			} catch (final IOException ioe) {
 				org.eclipse.stem.ui.reports.Activator.logError(
 						"exception loading file [" + ioe.getMessage() + "]",
 						ioe);
 			}
 		} else {
 			// else create it
 			try {
 				masterSettings.save(filename);
 			} catch (final IOException ioe) {
 				org.eclipse.stem.ui.reports.Activator.logError(
 						"exception creating file [" + ioe.getMessage() + "]",
 						ioe);
 			}
 		} // else if f !exists
 	}// read master setttings
 
 	/**
 	 * gets the preferences file name
 	 * 
 	 * @return file name containing the users preferences
 	 */
 	public String getPrefFileName() {
 		final String controlCategory = rcf.getUniqueControlName();
 		// Get the right preference file
 		final String filename = PATH.append(
 				controlCategory + "_" + REPORTVIEWPREFERENCES_FILENAME)
 				.toOSString();
 		return filename;
 	}
 
 } // ReportViewer

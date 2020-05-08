 // GeographicControl.java
 package org.eclipse.stem.ui.views.geographic;
 
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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.DynamicNodeLabel;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProvider;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapterFactory;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProvider;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapter;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapterFactory;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong.Segment;
 import org.eclipse.stem.data.geography.centers.GeographicCenters;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.jobs.simulation.ISimulationListener;
 import org.eclipse.stem.jobs.simulation.Simulation;
 import org.eclipse.stem.jobs.simulation.SimulationEvent;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.adapters.color.ColorProviderAdapter;
 import org.eclipse.stem.ui.adapters.color.ColorProviderAdapterFactory;
 import org.eclipse.stem.ui.adapters.color.IColorProviderChangedListener;
 import org.eclipse.stem.ui.views.geographic.map.MapCanvas;
 import org.eclipse.stem.ui.views.geographic.map.Messages;
 import org.eclipse.stem.ui.views.geographic.map.StemPolygon;
 import org.eclipse.stem.ui.views.geographic.map.StemPolygonsList;
 import org.eclipse.stem.ui.widgets.GeoViewOptionsBar;
 import org.eclipse.stem.ui.widgets.GeoViewOptionsBar.PropertySelectionEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 /**
  * This class represents a visualization of the geographic attributes of a
  * {@link org.eclipse.stem.jobs.simulation.Simulation}.
  */
 abstract public class GeographicControl extends Composite implements
 		ISimulationListener, ISelectionProvider, IColorProviderChangedListener,
 		IPropertyChangeListener, ISelectionChangedListener,
 		GeoViewOptionsBar.PropertySelectionListener {
 
 	/**
 	 * The width of the layout margin.
 	 */
 	protected static final int MARGIN_WIDTH = 5;
 
 	/**
 	 * The height of the layout margin.
 	 */
 	protected static final int MARGIN_HEIGHT = 5;
 
 	/**
 	 * This is the {@link Simulation} that this control is listening to and
 	 * visualizing by rendering Lat/Long data on the {@link #mapCanvas}.
 	 */
 	private ISimulation simulation = null;
 
 	/**
 	 * The {@link Identifiable} that was most recently selected, or
 	 * <code>null</code>, if none has been selected.
 	 */
 	ISelection selection = null;
 
 	/**
 	 * The collection of {@link ISelectionChangedListener} waiting to be told
 	 * about selections.
 	 */
 	protected final List<ISelectionChangedListener> listeners = new CopyOnWriteArrayList<ISelectionChangedListener>();
 
 	protected GeographicRenderer geographicRenderer;
 
 	protected String selectedEdge = "";
 
 	/**
 	 * If <code>true</code> then there is a map refresh request pending.
 	 */
 	boolean refreshPending = false;
 
 	/**
 	 * This is the <code>Job</code> that computes the polygon values in the
 	 * background.
 	 */
 	Job refreshJob;
 	/**
 	 * Adapter instance of the selected color provider (to be reused by only
 	 * setting the target)
 	 */
 	protected ColorProviderAdapter colorProviderAdapter = null;
 
 	private Label simulationNameLabel;
 
 	private GeoViewOptionsBar optionsBar;
 
 	/**
 	 * This contains a mapping between the {@link Decorator}s we display and the
 	 * data we need to display them. Not used yet.
 	 */
 	protected final Map<Decorator, DecoratorDisplayData> decoratorToDecoratorDisplayDataMap = new HashMap<Decorator, DecoratorDisplayData>();
 
 	// Cache of generated StemPolygonsList instances
 	private final Map<LatLong, StemPolygonsList> polygonListMap = new HashMap<LatLong, StemPolygonsList>();
 
 	/**
 	 * @param parent
 	 * @param style
 	 */
 	public GeographicControl(final Composite parent, final int style) {
 		super(parent, style);
 
 		Activator.getDefault().getPluginPreferences()
 				.addPropertyChangeListener(this);
 
 		setPreferences();
 
 		createContextMenu(this);
 
 		final FormLayout layout = new FormLayout();
 		layout.marginHeight = MARGIN_HEIGHT;
 		layout.marginWidth = MARGIN_WIDTH;
 		setLayout(layout);
 
 		final Composite topComposite = createTopComposite(this);
 
 		geographicRenderer = createGeographicRenderer();
 		geographicRenderer.setColorProviderAdapter(colorProviderAdapter);
 		geographicRenderer.addSelectionChangedListener(this);
 
 		final Composite bottomComposite = createBottomComposite(this);
 
 		final FormData topCompositeFormData = new FormData();
 		topComposite.setLayoutData(topCompositeFormData);
 		topCompositeFormData.top = new FormAttachment(0, 0);
 		topCompositeFormData.left = new FormAttachment(0, 0);
 		topCompositeFormData.right = new FormAttachment(100, 0);
 
 		final FormData geoRendererFormData = new FormData();
 		geographicRenderer.setLayoutData(geoRendererFormData);
 		geoRendererFormData.top = new FormAttachment(topComposite, 0);
 		geoRendererFormData.bottom = new FormAttachment(bottomComposite, 0);
 		geoRendererFormData.left = new FormAttachment(0, 0);
 		geoRendererFormData.right = new FormAttachment(100, 0);
 
 		// Bottom Composite
 		final FormData bottomCompositeFormData = new FormData();
 		bottomComposite.setLayoutData(bottomCompositeFormData);
 		bottomCompositeFormData.bottom = new FormAttachment(100, 0);
 		bottomCompositeFormData.left = new FormAttachment(0, 0);
 		bottomCompositeFormData.right = new FormAttachment(100, 0);
 
 		pack();
 	} // GeographicControl
 
 	/**
 	 * @return
 	 */
 	abstract protected GeographicRenderer createGeographicRenderer();
 
 	/**
 	 * Set visualization preferences
 	 */
 	protected void setPreferences() {
 		// Nothing yet
 		// try {
 		// // IPreferenceStore
 		// final Preferences preferences = Activator.getDefault()
 		// .getPluginPreferences();
 		//
 		// initialScaleRulerSelection = preferences
 		// .getInt(org.eclipse.stem.ui.views.geographic.map.preferences.
 		// PreferenceConstants.INITIAL_SCALE_SELECTION_PREFERENCE);
 		// } catch (final NullPointerException e) {
 		// // Ignore
 		// } // catch NullPointerException
 	} // setPreferences
 
 	/**
 	 * @return the {@link org.eclipse.stem.jobs.simulation.Simulation}
 	 */
 	public final ISimulation getSimulation() {
 		return simulation;
 	} // getSimulation
 
 	/**
 	 * Setting the {@link org.eclipse.stem.jobs.simulation.Simulation} has
 	 * the side-effect of causing the control to remove itself as a listener
 	 * from any previously set
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation} and adding itself
 	 * as a listener to the new one. It will also cause the image to be
 	 * initialized from the contents of the new
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation} as appropriate.
 	 * 
 	 * @param simulation
 	 *            the {@link org.eclipse.stem.jobs.simulation.Simulation}
 	 *            whose Lat/Long data will be rendered.
 	 */
 	public final void setSimulation(final ISimulation simulation) {
 		// Any need to switch?
 		if (this.simulation == simulation) {
 			// No
 			return;
 		} // if no need to switch?
 
 		// Should we remove ourselves as listeners from the current simulation?
 		if (this.simulation != null) {
 			// Yes
 			this.simulation.removeSimulationListener(this);
 		}
 
 		this.simulation = simulation;
 		selection = null;
 		// Was there a new simulation to switch to?
 		if (this.simulation != null) {
 			// Yes
 			this.simulation.addSimulationListener(this);
 			initializeFromSimulation(this.simulation);
 		}
 
 		refresh();
 	} // setSimulation
 
 	/**
 	 * Initialize the control from a
 	 * {@link org.eclipse.stem.jobs.simulation.Simulation} instance.
 	 * 
 	 * @param simulation
 	 *            the {@link org.eclipse.stem.jobs.simulation.Simulation}
 	 *            whose state will be visualized in the control.
 	 */
 	protected void initializeFromSimulation(final ISimulation simulation) {
 		simulationNameLabel.setText(simulation.getName());
 		optionsBar.setSimulation(simulation);
 	} // initializeFromSimulation
 
 	/**
 	 * Update the contents of {@link #geographicRenderer} from the contents of
 	 * the {@link #simulation}. This method should only be called by the UI
 	 * thread otherwise it contains a race condition that can lock up the
 	 * refreshing of the map canvas.
 	 */
 	void refresh() {
 		// This is complex and potential confusing code. The basic idea is that
 		// we want to determine what to draw on the mapCanvas in a thread
 		// separate from the UI thread, but we also want to capture a request to
 		// refresh the mapCanvas that comes while a background Job is already
 		// running. This request could have been generated because something
 		// significant like a decorator or a property has changed and
 		// we don't want to lose that switch.
 		// We have two fields that capture the state of the refresh. The first
 		// is the flag refreshPending which if true indicates that a call to
 		// refreshMapCanvas was processed while a background Job was executing.
 		// This flag is set instead of creating another background Job. The
 		// second is the field refreshJob which is set to the background Job if
 		// it is present and running.
 		//
 		// If we enter the method and refreshPending is true then we immediately
 		// exit because a refresh job will be created and there's nothing more
 		// for us to request. If, instead, refreshPending is false, then we
 		// check to see if a refreshJob is already running. If so, we set
 		// refreshPending to true, and exit. If not, we create the background
 		// refresh job and assign it to the field refreshJob.
 		//
 		// In the background refreshing job we process the Lat/Long data and
 		// then when done we add a Runnable on to the UI thread to redraw the
 		// mapCanvas. That Runnable also checks to see if the refresh pending
 		// flag is set and if so, resets the flag and then adds another Runnable
 		// to the UI thread to re-enter the refreshMapCanvas method.
 		//
 		// There is a race condition in the method if called by other than the
 		// UI thread. If a thread different from the UI thread were to call this
 		// method then it could reach position "RC #2" in the code below and
 		// then be preempted. The UI thread could then execute the test at "RC
 		// #1" below and find that there is no refresh pending. It would exit
 		// and then the other thread would resume at "RC #2" and set the
 		// refreshPending flag. This would cause all subsequent method
 		// invocations to immediately exit and the map canvas would never be
 		// refreshed again.
 
 		// Is there a refresh pending?
 		if (!refreshPending) {
 			// No
 			// Is there already a refresh job?
 			if (refreshJob == null) {
 				// No
 				// Refresh the map canvas in the background
 				refreshJob = new Job(Messages.getString("IMView.MRefresh")) {
 					/**
 					 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 					 */
 					@Override
 					protected IStatus run(final IProgressMonitor monitor) {
 
 						final StemPolygonsList polygonsToDraw = createPolygonsToDraw(monitor);
 						// Still ok to run?
 						if(polygonsToDraw == null) return Status.OK_STATUS;
 						final Display display = Display.getDefault();
 						if (!display.isDisposed()) {
 							// Yes
 							try {
 								display.asyncExec(new Runnable() {
 									public void run() {
 										// This runs in the UI thread
 										// Is the renderer still around?
 										if (!geographicRenderer.isDisposed()) {
 											// Yes
 											geographicRenderer
 													.render(polygonsToDraw);
 
 											refreshJob = null;
 											// RC #1
 											// Is there a refresh pending?
 											if (refreshPending) {
 												// Yes
 												refreshPending = false;
 												// We create a new Runnable to
 												// avoid recursion
 												// Ok to run?
 												final Display display2 = Display
 														.getDefault();
 												if (!display2.isDisposed()) {
 													// Yes
 													display2
 															.asyncExec(new Runnable() {
 																public void run() {
 																	// This runs
 																	// in the UI
 																	// thread
 																	refresh();
 																} // asyncExec
 															});
 												} // if ok to run
 											} // if refresh pending
 										} // if the mapCanvas still around?
 									} // run (UI thread mapCanvas.redraw())
 								});
 							} // try
 							catch (final NullPointerException npe) {
 								// see 177966
 								// We ignore the exception, there's nothing to
 								// do
 							} // catch NullPointerException
 						} // if ok to run
 						monitor.done();
 						return Status.OK_STATUS;
 					} // run (background polygon processing Job)
 
 				};
 				refreshPending = false;
 				refreshJob.schedule();
 			} // if not already a refresh job
 			else {
 				// Yes
 				// There is already a refresh job so remember that a refresh is
 				// pending
 				// RC #2
 				refreshPending = true;
 			} // else
 		} // if no refresh is pending
 
 	} // refresh
 
 	/**
 	 * @see org.eclipse.swt.widgets.Widget#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		// Is there a simulation we're listening to?
 		if (simulation != null) {
 			// Yes
 			// Not any more
 			simulation.removeSimulationListener(this);
 		}
 
 		optionsBar.removeColorProviderChangedListener(this);
 		optionsBar.removePropertySelectionListener(this);
 
 		geographicRenderer.removeSelectionChangedListener(this);
 
 		// Shouldn't be any selection changed listeners
 		assert listeners.size() == 0;
 
 		Activator.getDefault().getPluginPreferences()
 				.removePropertyChangeListener(this);
 
 	} // dispose
 
 	/**
 	 * Create the view's context menu and add the action handlers to it.
 	 */
 	private void createContextMenu(final Composite parent) {
 
 		// Context Menu
 		final MenuManager contextMenuManager = new MenuManager();
 
 		// ---------------------------------------------------------------------
 
 		contextMenuManager.add(new ResetRenderer());
 
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		// ---------------------------------------------------------------------
 
 		final Menu popUpMenu = contextMenuManager.createContextMenu(parent);
 
 		// Set the context menu for the viewer
 		parent.setMenu(popUpMenu);
 
 	} // createContextMenu
 
 	/**
 	 * @param event
 	 */
 	public void propertyChange(final PropertyChangeEvent event) {
 		setPreferences();
 	} // propertyChange
 
 	/**
 	 * @param event
 	 */
 	public void selectionChanged(final SelectionChangedEvent event) {
 		fireSelection(event.getSelection());
 	}
 
 	protected class ResetRenderer extends Action {
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("MapMenu.Reset");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			geographicRenderer.reset();
 
 			// setTranslation(DEFAULT_INITIAL_X_TRANSLATION,
 			// DEFAULT_INITIAL_Y_TRANSLATION);
 			// zoomFactor = INITIAL_ZOOM_FACTOR;
 			//
 			// // Reset the gain factor and update the checks in the context
 			// menu
 			// lastGainFactorAction.setChecked(false);
 			// defaultGainFactorAction.run();
 			// defaultGainFactorAction.setChecked(true);
 			//
 			// useLogScaling = DEFAULT_USE_LOGSCALING;
 			// logarithmicAction.setChecked(useLogScaling);
 			//
 			// drawPolygonBorders = DEFAULT_DRAW_POLYGON_BORDERS;
 			// drawPolygonBordersAction.setChecked(drawPolygonBorders);
 			//
 			// drawPolygonCenterConnections =
 			// DEFAULT_DRAW_POLYGON_CENTER_CONNECTIONS;
 			// connectPolygonCentersAction
 			// .setChecked(drawPolygonCenterConnections);
 
 			geographicRenderer.redraw();
 		}
 	} // ResetMapCanvasAction
 
 	/**
 	 * This class is used to hold data used to display the relative values of a
 	 * {@link Decorator}. Instances of this class are created when the label
 	 * values of a {@link Decorator} are first drawn and then subsequently
 	 * cached in {@link #decoratorToDecoratorDisplayDataMap} for future
 	 * retrieval.
 	 */
 	protected static class DecoratorDisplayData {
 
 		private RelativeValueProviderAdapter rvp;
 
 		private LatLongProviderAdapter latLongProvider;
 
 		private final List<LabelData> labelDatas = new ArrayList<LabelData>();
 
 		private final List<LabelData> labelsWithUnresolvedLatLong = new ArrayList<LabelData>();
 
 		/**
 		 * Default constructor
 		 */
 		public DecoratorDisplayData() {
 			// nothing
 		}
 
 		/**
 		 * @return <code>true</code> if all of the labels have valid
 		 *         latitude/longitude data retrieved.
 		 */
 		public boolean hasAllLatLong() {
 			return labelsWithUnresolvedLatLong.size() == 0;
 		}
 
 		/**
 		 * @return the label data for the decorator
 		 */
 		public List<LabelData> getLabelData() {
 			return labelDatas;
 		}
 
 		/**
 		 * @return the {@link RelativeValueProviderAdapter}
 		 */
 		public RelativeValueProviderAdapter getRelativeValueProviderAdapter() {
 			return rvp;
 		} // getRelativeValueProviderAdapter
 
 		/**
 		 * Go through each of the label data entries and try to retrieve the
 		 * ones that do not have lat/long data yet.
 		 * 
 		 * @param monitor
 		 *            progress monitor
 		 */
 		void updateLatLong(final IProgressMonitor monitor) {
 			for (final Iterator<LabelData> iter = labelsWithUnresolvedLatLong
 					.iterator(); iter.hasNext();) {
 				final LabelData ld = iter.next();
 
 				latLongProvider.setTarget(ld.getLabel().getNode());
 				ld.setLatLongData(latLongProvider.getLatLongNoWait());
 				// Did we get some data?
 				if (ld.getLatLongData().size() != 0) {
 					// Yes
 					// No need to keep this one on the list
 					iter.remove();
 				} // if we got some data
 			} // for each DynamicNodeLabelImpl
 		} // updateLatLong
 
 		/**
 		 * @param label
 		 *            a {@link DynamicNodeLabel} to add to the set.
 		 */
 		public void addLabel(final DynamicNodeLabel label) {
 			final LabelData ld = new LabelData(label);
 			labelDatas.add(ld);
 			labelsWithUnresolvedLatLong.add(ld);
 		} // addLabel
 
 		/**
 		 * @param latLongProvider
 		 *            the {@link LatLongProviderAdapter} that will provide the
 		 *            latitude/longitude data for the labels.
 		 */
 		public void setLatLongProviderAdapter(
 				final LatLongProviderAdapter latLongProvider) {
 			this.latLongProvider = latLongProvider;
 		}
 
 		/**
 		 * @param rvp
 		 *            the {@link RelativeValueProviderAdapter} that will provide
 		 *            the relative values for the labels.
 		 */
 		public void setRelativeValueProviderAdapter(
 				final RelativeValueProviderAdapter rvp) {
 			this.rvp = rvp;
 		} // setRelativeValueProviderAdapter
 
 		/**
 		 * This class maintains a relationship between a
 		 * {@link org.eclipse.stem.core.graph.Label} and its
 		 * latitude/longitude data.
 		 */
 		public static class LabelData {
 
 			private final DynamicNodeLabel label;
 
 			private LatLong latLongData;
 
 			/**
 			 * @param label
 			 */
 			public LabelData(final DynamicNodeLabel label) {
 				this.label = label;
 			}
 
 			/**
 			 * @return the label
 			 */
 			public DynamicNodeLabel getLabel() {
 				return label;
 			}
 
 			/**
 			 * @return the latitude/longitude data
 			 */
 			public LatLong getLatLongData() {
 				return latLongData;
 			}
 
 			/**
 			 * @param latLongData
 			 */
 			public void setLatLongData(final LatLong latLongData) {
 				this.latLongData = latLongData;
 			}
 
 		} // LabelData
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(
 			final ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(
 			final ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * @return the selection
 	 */
 	public final ISelection getSelection() {
 		return selection;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(final ISelection selection) {
 		this.selection = selection;
 		fireSelection(selection);
 	}
 
 	/**
 	 * Pass a {@link SelectionChangedEvent} along to listeners.
 	 * 
 	 * @param selection
 	 *            the {@link ISelection} to be giving to the listeners.
 	 */
 	public void fireSelection(final ISelection selection) {
 
 		final SelectionChangedEvent event = new SelectionChangedEvent(this,
 				selection);
 		for (final ISelectionChangedListener listener : listeners) {
 			listener.selectionChanged(event);
 		} // for each ISelectionChangedListener
 
 	} // fireSelection
 
 	/**
 	 * @param parent
 	 * @return
 	 */
 	protected Composite createBottomComposite(final Composite parent) {
 		optionsBar = new GeoViewOptionsBar(parent, SWT.NONE);
 		optionsBar.addColorProviderChangedListener(this);
 		optionsBar.addPropertySelectionListener(this);
 		return optionsBar;
 	} // createBottomComposite
 
 	/**
 	 * @param propertySelectionEvent
 	 */
 	public void propertySelected(
 			final PropertySelectionEvent propertySelectionEvent) {
 		if (propertySelectionEvent == null) {
 			selectedEdge = "";
 		}
 		// Hack
 		else if (propertySelectionEvent.getSource() instanceof String) {
 			selectedEdge = (String) propertySelectionEvent.getSource();
 		}
 		refresh();
 	}
 
 	/**
 	 * @param parent
 	 *            the parent SWT control that this {@link Composite} will be a
 	 *            child of
 	 * @return the SWT {@link Composite} to be displayed above the
 	 *         {@link MapCanvas} in the control
 	 */
 	protected Composite createTopComposite(final Composite parent) {
 		final Composite topComposite = new Composite(parent, SWT.NONE);
 
 		final FillLayout topCompositeLayout = new FillLayout(SWT.VERTICAL);
 		topComposite.setLayout(topCompositeLayout);
 
 		simulationNameLabel = new Label(topComposite, SWT.CENTER);
 		simulationNameLabel.setText("");
 
 		return topComposite;
 	} // createTopComposite
 
 	/**
 	 * @see org.eclipse.stem.jobs.simulation.ISimulationListener#simulationChanged(org.eclipse.stem.jobs.simulation.SimulationEvent)
 	 */
 	public void simulationChanged(final SimulationEvent event) {
 
 		switch (event.getSimulationState()) {
 		case COMPLETED_CYCLE:
 			// It could be that the propertySelector is not initialized
 			// yet. This could happen if, when the simulation is first set, its
 			// scenario was not completely initialized, and so didn't have a
 			// canonical graph. If this is the case, we need to try again.
 
 			// Is the control initialized?
 			if (!optionsBar.isInitialized()) {
 				// No
 				optionsBar.setSimulation(getSimulation());
 			} // if
 
 			refresh();
 			break;
 
 		case RESET:
 			refresh();
 			break;
 		default:
 			// Ignore
 			break;
 		} // switch
 	} // simulationChanged
 
 	/**
 	 * This runs in the background {@link Job} created in {@link #refresh()}
 	 * 
 	 * @param decorator
 	 *            the {@link Decorator} for whom that data is being retrieved.
 	 * @param monitor
 	 *            the progress monitor to use to report the progress of
 	 *            obtaining the data
 	 * @return the {@link DecoratorDisplayData} for the {@link Decorator}.
 	 */
 	protected DecoratorDisplayData getDecoratorDisplayData(
 			final Decorator decorator, final IProgressMonitor monitor) {
 		DecoratorDisplayData retValue = decoratorToDecoratorDisplayDataMap
 				.get(decorator);
 		// Is there already an instance for this decorator?
 		if (retValue == null) {
 			// No
 			// Create one then
 			retValue = new DecoratorDisplayData();
 			decoratorToDecoratorDisplayDataMap.put(decorator, retValue);
 
 			if (decorator.getLabelsToUpdate() == null
 					|| decorator.getLabelsToUpdate().isEmpty()) {
 				return retValue;
 			}
 
 			// Get the relative value provider adapter that we'll use for all of
 			// the decorator's labels. We only get it once because it is
 			// implemented using the singleton pattern so we'll always get the
 			// same one.
 			final DynamicNodeLabel firstNodeLabel = (DynamicNodeLabel) decorator
 					.getLabelsToUpdate().get(0);
 			// Was there a label to update?
 			if (firstNodeLabel != null) {
 				// Yes
 				// Get the relative value from the dynamic label
 				final RelativeValueProviderAdapter rvp = (RelativeValueProviderAdapter) RelativeValueProviderAdapterFactory.INSTANCE
 						.adapt(firstNodeLabel, RelativeValueProvider.class);
 				if(rvp == null) return null;
 				retValue.setRelativeValueProviderAdapter(rvp);
 
 				final Node node = firstNodeLabel.getNode();
 				// String nodeTitle = node.getDublinCore().getTitle();
 
 				final LatLongProviderAdapter latLongProvider = (LatLongProviderAdapter) LatLongProviderAdapterFactory.INSTANCE
 						.adapt(node, LatLongProvider.class);
 				retValue.setLatLongProviderAdapter(latLongProvider);
 			} // if there is a label
 
 			for (final Object element : decorator.getLabelsToUpdate()) {
 				final DynamicNodeLabel nodeLabel = (DynamicNodeLabel) element;
 				retValue.addLabel(nodeLabel);
 			} // for
 
 		} // if no instance
 
 		// Does the instance have all of it's lat/long data retrieved yet?
 		if (!retValue.hasAllLatLong()) {
 			// No
 			// Try to get it
 			monitor.subTask(decorator.getDublinCore().getTitle());
 			retValue.updateLatLong(monitor);
 		} // if missing lat/long
 
 		return retValue;
 	} // getDecoratorDisplayData
 
 	protected StemPolygonsList createPolygonsToDraw(
 			final IProgressMonitor monitor) {
 		return createPolygonsToDraw(optionsBar.getSelectedDecorator(), monitor);
 	} // createPolygonsToDraw
 
 	/**
 	 * This runs in the background {@link Job} created in
 	 * {@link #refreshMapCanvas()}.
 	 * 
 	 * @param selectedDecorator
 	 *            the {@link Decorator} that contains the selected property.
 	 * @param selectedProperty
 	 *            the property of the selected {@link Decorator} to visualize
 	 * @param monitor
 	 *            progress monitor for obtaining the polygons
 	 * @return a collection of polygon values to render in the
 	 *         {@link #mapCanvas}
 	 */
 	StemPolygonsList createPolygonsToDraw(final Decorator selectedDecorator,
 			final IProgressMonitor monitor) {
 		final StemPolygonsList retValue = new StemPolygonsList();
 
 		// Is there a decorator selected?
 		if (selectedDecorator != null) {
 			// Yes
 			// Get the display data for this decorator
 			final DecoratorDisplayData displayData = getDecoratorDisplayData(
 					selectedDecorator, monitor);
 			
 			if(displayData == null) return null;
 			final RelativeValueProviderAdapter rvp = displayData
 					.getRelativeValueProviderAdapter();
 			if(rvp == null) return null;
 			
 			monitor.beginTask(selectedDecorator.getDublinCore().getTitle(),
 					displayData.getLabelData().size());
 
 			for (final DecoratorDisplayData.LabelData labelData : displayData
 					.getLabelData()) {
 
 				// This is the Identifiable that will produce the lat/long data
 				// (if available).
 				final Identifiable identifiable = labelData.getLabel()
 						.getIdentifiable();
 
 				monitor.subTask(identifiable.getDublinCore().getTitle());
 
 				final LatLong latLong = labelData.getLatLongData();
 				// Any lat/long data for this label?
 				if (latLong.size() > 0) {
 					// Yes
 					final StemPolygonsList stemPolygonsList = getStemPolygonsList(
 							latLong, identifiable, rvp, labelData);
 
 					// adding it to the list of polygons to be drawn
 					retValue.addAll(stemPolygonsList);
 				} // if Any lat/long data for this label
 				monitor.worked(1);
 
 			} // for each LabelData
 
 			if (selectedEdge != null && !selectedEdge.equals("")) {
 
 				final List<URI> edgeTypes = new LinkedList<URI>();
 
 				final Graph graph = selectedDecorator.getGraph();
 				final EMap<URI, Edge> allEdges = graph.getEdges();
 				final Iterator<URI> iter = allEdges.keySet().iterator();
 				final List<Edge> edges = new ArrayList<Edge>();
 				while (iter.hasNext()) {
 					final URI uri = iter.next();
 					if (edgeTypes.contains(uri)) {
 						edgeTypes.add(uri);
 					}
 					if (uri.toString().startsWith(selectedEdge)) {
 						edges.add(allEdges.get(uri));
 					}
 				}
 
 				if (!edges.isEmpty()) {
 					final Iterator<Edge> edgesIter = edges.iterator();
 					final List<URI> addedEdges = new ArrayList<URI>();
 					while (edgesIter.hasNext()) {
 						final Edge nextEdge = edgesIter.next();
 
 						final Node nodeA = nextEdge.getA();
 						final Node nodeB = nextEdge.getB();
 						if (nodeA == null || nodeB == null) {
 							// Commented out 4/2/2009 since it is possible 
 							// that edges for air transport has a missing node if 
 							// a sub model is not included in the scenario
 							/*final StringBuffer missingEdgeReport = new StringBuffer();
 							missingEdgeReport
 									.append("The following edge has a missing node:\n");
 							missingEdgeReport.append(" Edge URI: "
 									+ nextEdge.getURI() + "\n");
 							if (nodeA == null && nodeB == null) {
 								missingEdgeReport.append(" Missing Node: Both");
 							} else {
 								missingEdgeReport.append(" Missing Node: "
 										+ (nodeA == null ? "A" : "B"));
 								missingEdgeReport.append(" Non-missing Node: "
 										+ (nodeA == null ? "B" : "A"));
 							}
 							Activator.logError(missingEdgeReport.toString(),
 									null);
 							*/
 							continue;
 						}
 
 						final String nodeAISOKey = nodeA.getURI().lastSegment();
 						double[] centerA = GeographicCenters
 								.getCenter(nodeAISOKey);
 
 						// Did we get it?
 						if (centerA == null) {
 							// No
 							// Compute the lat/long of the center of the node
 							final LatLongProviderAdapter latLongProviderA = (LatLongProviderAdapter) LatLongProviderAdapterFactory.INSTANCE
 									.adapt(nodeA, LatLongProvider.class);
 							latLongProviderA.setTarget(nodeA);
 							centerA = latLongProviderA.getCenter();
 						} // if
 
 						final String nodeBISOKey = nodeB.getURI().lastSegment();
 						double[] centerB = GeographicCenters
 								.getCenter(nodeBISOKey);
 						// Did we get it?
 						if (centerB == null) {
 							// No
 
 							// Get the lat/long of the center of the node
 							final LatLongProviderAdapter latLongProviderB = (LatLongProviderAdapter) LatLongProviderAdapterFactory.INSTANCE
 									.adapt(nodeB, LatLongProvider.class);
 							latLongProviderB.setTarget(nodeB);
 							centerB = latLongProviderB.getCenter();
 						} // if centerB
 
 						if (centerA == null || centerB == null) {
 							continue;
 						}
 
 						final double[][] data = new double[2][2];
 
 						data[0][0] = centerA[0];
 						data[0][1] = centerA[1];
 						data[1][0] = centerB[0];
 						data[1][1] = centerB[1];
 
 						if (!addedEdges.contains(nextEdge.getURI())) {
 							final Segment edgeSegment = new Segment(data);
 							final StemPolygon stemPolygon = new StemPolygon(
 									edgeSegment, nextEdge);
 							retValue.add(stemPolygon);
 							addedEdges.add(nextEdge.getURI());
 						}
 					}
 				}
 			}
 		} // if there a decorator and a currently selected property
 
 		return retValue;
 	}// createPolygonsToDraw
 
 	/**
 	 * @param latLong
 	 * @param rvp
 	 * @return
 	 */
 	private StemPolygonsList getStemPolygonsList(final LatLong latLong,
 			final Identifiable identifiable,
 			final RelativeValueProviderAdapter rvp,
 			final DecoratorDisplayData.LabelData labelData) {
 
 		StemPolygonsList retValue = polygonListMap.get(latLong);
 		rvp.setTarget(labelData.getLabel());
 		// final ItemPropertyDescriptor selectedProperty =
 		// null;//bottomComposite.get
 		// Did we find a previously generated polygon list?
 		if (retValue == null) {
 			// // No
 			// // Allocate a mapping between the property names and their
 			// // relative values.
 			// final Map<String, Double> relativeValueMap = new HashMap<String,
 			// Double>();
 			//
 			// // First put the Selected property in the valueMap to
 			// // display
 			// relativeValueMap.put(selectedProperty
 			// .getDisplayName(selectedProperty), new Double(rvp
 			// .getRelativeValue(selectedProperty)));
 
 			// Are we in multicolor display mode?
 			// if (false /* mapCanvas.enableMultiColorMode */) {
 			// // Yes
 			// // Add all relative values by property attribute key
 			// final List<IItemPropertyDescriptor> properties = rvp
 			// .getProperties();
 			// // JHK we don't need to add the selected one separately
 			// // above as the code below should get it as well - check
 			// // this...
 			// assert (properties.contains(selectedProperty));
 			// // Add ALL the properties for MultiColor Display Mode
 			// for (int i = 0; i < properties.size(); i++) {
 			// final ItemPropertyDescriptor itemKey = (ItemPropertyDescriptor)
 			// properties
 			// .get(i);
 			// key = itemKey.getDisplayName(itemKey);
 			// relativeValue = new Double(rvp.getRelativeValue(itemKey));
 			// valueMap.put(key, relativeValue);
 			// } // for all properties
 			// } // if multi color mode
 
 			// creating the polygon from that label
 			retValue = new StemPolygonsList(latLong, // relativeValueMap,
 					identifiable);
 			polygonListMap.put(latLong, retValue);
 		} // if didn't find generated instance
 		// else {
 		// // Update the relative values of the properties in the value map
 		// retValue.clearRelativeValues();
 		// retValue.addRelativeValue(selectedProperty
 		// .getDisplayName(selectedProperty), new Double(rvp
 		// .getRelativeValue(selectedProperty)));
 		// } // else
 		return retValue;
 	} // getStemPolygonsList
 
 	/**
 	 * The method will return the {@link GeographicRenderer} used by this
 	 * object.
 	 * 
 	 * @return The GeographicRenderer used by this object
 	 * @see GeographicRenderer
 	 */
 	public GeographicRenderer getGeographicRenderer() {
 		return geographicRenderer;
 	} // getGeographicRenderer
 
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.IColorProviderChangedListener#colorProviderChanged(java.lang.Class)
 	 */
 	public void colorProviderChanged(final Class selectedColorProvider) {
 		final Decorator decorator = optionsBar.getSelectedDecorator();
 		final AdapterFactory adapterFactory = ColorProviderAdapterFactory.INSTANCE
 				.getFactoryForType(selectedColorProvider);
 		final Adapter adapter = adapterFactory.adapt(decorator.getGraph(),
 				selectedColorProvider);
 		if (adapter instanceof ColorProviderAdapter) {
 			colorProviderAdapter = (ColorProviderAdapter) adapter;
 			colorProviderAdapter.setSelectedDecorator(optionsBar
 					.getSelectedDecorator());
 			geographicRenderer.setColorProviderAdapter(colorProviderAdapter);
 		}
 	} // colorProviderChanged
 } // GeographicControl

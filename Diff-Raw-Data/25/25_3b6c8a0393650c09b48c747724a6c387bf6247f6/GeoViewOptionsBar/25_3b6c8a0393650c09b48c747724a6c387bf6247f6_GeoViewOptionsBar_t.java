 // GeoViewOptionsBar.java
 package org.eclipse.stem.ui.widgets;
 
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
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.model.Decorator;
import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProvider;
import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter;
import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapterFactory;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.jobs.simulation.Simulation;
 import org.eclipse.stem.ui.adapters.color.ColorProvider;
 import org.eclipse.stem.ui.adapters.color.ColorProviderAdapterFactory;
 import org.eclipse.stem.ui.adapters.color.IColorProviderAdapterFactory;
 import org.eclipse.stem.ui.adapters.color.IColorProviderChangedListener;
 import org.eclipse.stem.ui.views.geographic.map.Messages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * This class is a compound SWT {@link Widget} that allows for the selection of
  * a {@link Decorator}s and {@link ColorProvider}s.
  */
 public class GeoViewOptionsBar extends Composite {
 
 	/**
 	 * A list of {@link PropertySelectionListener}s to be notified about
 	 * property selection events
 	 */
 	private final List<PropertySelectionListener> selectionListeners = new CopyOnWriteArrayList<PropertySelectionListener>();
 	/**
 	 * The collection of {@link IColorProviderChangedListener} waiting to be told change of a color provider.
 	 */
 	protected final List<IColorProviderChangedListener> colorProviderChangedListeners = new CopyOnWriteArrayList<IColorProviderChangedListener>();
 	/**
 	 * Combo for selecting decorators
 	 */
 	Combo decoratorsCombo;
 	/**
 	 * Combo for selecting the color provider
 	 */
 	Combo colorProvidersCombo;
 	
 	/**
 	 * A constant to set the width of the decorators combo
 	 */
 	static final int DECORATORS_WIDTH = 55;
 	
 	/**
 	 * A constant to set the width of the color providers combo
 	 */
 	static final int PROVIDERS_WIDTH  = 65;
 	
 	/**
 	 * Combo for selecting edges
 	 */
 	Combo edgesCombo;
 	/**
 	 * The composite that the selected color provider contributed
 	 */
 	Composite colorProviderComposite;
 	/**
 	 * The composite with the colors legend (provided by the selected color
 	 * provider)
 	 */
 	Composite colorsLegendComposite;
 	/**
 	 * The group composite that gathers the widgets
 	 */
 	Group geoViewOptionsGroup;
 	/**
 	 * The group composite that gathers the widgets
 	 */
 	Group edgesGroup;	
 	/**
 	 * List of available decorators
 	 */
 	List<Decorator> decorators;
 	/**
 	 * List of available color providers
 	 */
 	List<Class> supportedColorProviders = null;
 	/**
 	 * List of available color providers names
 	 */
 	List<String> supportedColorProvidersNames = null;
 	/**
 	 * Mapping between edge type and its URI prefix
 	 */
 	Map<String, String> edgeTypeToUriPrefixMap = new HashMap<String, String>();
 	/**
 	 * The selected color provider
 	 */
 	Class selectedColorProvider = null;
 	/**
 	 * The selected decorator
 	 */
 	Decorator selectedDecorator;
 	/**
 	 * Decorator filter to be used
 	 */
 	private DecoratorFilter decoratorFilter = new DecoratorFilter() {
 		/**
		 * Currently only accepts decorators that are able to provide a relative value
		 * TODO: Fix so we can view other types of decorators
		 * 
 		 * @see GeoViewOptionsBar.DecoratorFilter#accept(org.eclipse.stem.core.model.Decorator)
 		 */
 		public boolean accept(
				final Decorator decorator) {
			
			EList<DynamicLabel>labels = decorator.getLabelsToUpdate();
			if(labels == null) return false;
			for(DynamicLabel l:labels) {
				RelativeValueProviderAdapter rvp = (RelativeValueProviderAdapter) RelativeValueProviderAdapterFactory.INSTANCE
				.adapt(l, RelativeValueProvider.class);
				if(rvp != null) return true;
			}
	
			return false;
 		}
 	};
 
 	/**
 	 * Constructor
 	 * 
 	 * @param parent
 	 *            the parent {@link Composite}
 	 * @param style
 	 *            an SWT style
 	 */
 	public GeoViewOptionsBar(final Composite parent, final int style) {
 		super(parent, style);
 
 		final RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
 		rowLayout.fill = true;
 		this.setLayout(rowLayout);
 
 		geoViewOptionsGroup = new Group(this, SWT.NONE);
 		final FormLayout formLayout = new FormLayout();
 		geoViewOptionsGroup.setLayout(formLayout);
 
 		// //$NON-NLS-1$ //$NON-NLS-2$
 		geoViewOptionsGroup.setText(Messages.getString("IMView.MProp"));
 		geoViewOptionsGroup.setVisible(false);
 
 		// Combo Box of Decorators
 		decoratorsCombo = new Combo(geoViewOptionsGroup, SWT.DROP_DOWN
 				| SWT.READ_ONLY | SWT.CENTER);
 
 		// Combo Box of the color providers
 		colorProvidersCombo = new Combo(geoViewOptionsGroup, SWT.DROP_DOWN
 				| SWT.READ_ONLY | SWT.CENTER);
 
 
 		final FormData decoratorsComboFormData = new FormData();
 		decoratorsComboFormData.left = new FormAttachment(0, 0);
 		decoratorsComboFormData.right = new FormAttachment(DECORATORS_WIDTH, 0);
 		decoratorsCombo.setLayoutData(decoratorsComboFormData);
 				
 		final FormData colorProvidersComboFormData = new FormData();
 		colorProvidersComboFormData.top = new FormAttachment(decoratorsCombo, 5);
 		colorProvidersComboFormData.left = new FormAttachment(0, 0);
 		colorProvidersComboFormData.right = new FormAttachment(PROVIDERS_WIDTH, 0);
 		colorProvidersCombo.setLayoutData(colorProvidersComboFormData);
 			
 		supportedColorProviders = ColorProviderAdapterFactory.INSTANCE.getSupportedColorProviders();
 		supportedColorProvidersNames = ColorProviderAdapterFactory.INSTANCE.getSupportedColorProvidersNames();
 
 		this.pack();
 
 		decoratorsCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(
 					@SuppressWarnings("unused") final SelectionEvent e) {
 				// Any decorators?
 				if (!decorators.isEmpty()) {
 					// Yes
 					final Decorator tempDecorator = decorators
 							.get(decoratorsCombo.getSelectionIndex());
 					// Was there a change in selected Decorators?
 					if (selectedDecorator != tempDecorator) {
 						// Yes
 						selectedDecorator = tempDecorator;
 						updateColorProviderComposites();
 						firePropertySelectionEvent(new PropertySelectionEvent(
 								selectedDecorator, null, colorProvidersCombo));
 					} // if Decorators changed
 
 				} // if any decorators
 			}
 		} // SelectionAdapter
 				);
 
 		colorProvidersCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(
 					@SuppressWarnings("unused") final SelectionEvent e) {
 				selectedColorProvider = 
 					supportedColorProviders.get(colorProvidersCombo.getSelectionIndex());
 				updateColorProviderComposites();
 			}
 		} // SelectionAdapter
 				);
 		
 		edgesGroup = new Group(this, SWT.NONE);
 		edgesCombo = new Combo(edgesGroup, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.CENTER);
 		edgesCombo.add("");		
 		edgesGroup.setLayout(new FormLayout());
 		edgesGroup.setVisible(false);
 		edgesGroup.setText(Messages.getString("IMView.Edges"));
 		
 		edgesCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(
 					@SuppressWarnings("unused") final SelectionEvent e) {
 				String selectedEdges = edgesCombo.getText();
 				if (selectedEdges == null || selectedEdges.equals("")) {
 					firePropertySelectionEvent(null);
 				}
 				else {
 					String edgePrefix = edgeTypeToUriPrefixMap.get(selectedEdges);
 					firePropertySelectionEvent(new PropertySelectionEvent(null,
 							null, edgePrefix));
 				}
 			}
 		} // SelectionAdapter
 				);
 
 		setSimulation(null);
 	} // PropertySelector
 
 	/**
 	 * @return <code>true</code> if the control has been initialized.
 	 */
 	public boolean isInitialized() {
 		return !(decorators == null || decorators.isEmpty());
 	} // isInitialized
 
 	/**
 	 * Sets the {@link Decorator}s
 	 * 
 	 * @param decorators
 	 *            {@link Decorator}s to set
 	 */
 	public void setDecorators(final List<Decorator> decorators) {
 		this.decorators = decorators;
 		selectedDecorator = decorators.isEmpty() ? null : decorators.get(0);
 
 		initializeCombo(decoratorsCombo, getDecoratorNames(decorators),
 				getDecoratorIndex(selectedDecorator, decorators));
 	} // setDecorators
 
 	/**
 	 * @param decorators
 	 */
 	public void setColorProviders(final List<Decorator> decorators) {
 		
 		if (supportedColorProviders.isEmpty()) {
 			//No color providers available
 			return;
 		}
 		
 		for (String providerName : supportedColorProvidersNames) {
 			colorProvidersCombo.add(providerName);
 		}
 		colorProvidersCombo.select(0);
 		selectedColorProvider = supportedColorProviders.get(0);
 
 		updateColorProviderComposites();
 	}
 	
 	private void setEdges() {
 		if (selectedDecorator == null)
 			return;
 		
 		Set<URI> edgesURIs = selectedDecorator.getGraph().getEdges().keySet();
 		Iterator<URI> iter = edgesURIs.iterator();
 		while(iter.hasNext()) {
 			URI nextUri = iter.next();
 			String edgePrefix = nextUri.trimSegments(1).toString();
 			String edgeType = org.eclipse.stem.ui.preferences.Messages.getString(edgePrefix);
 			if (edgeType.startsWith("!") && edgeType.endsWith("!")) {
 				edgePrefix = nextUri.trimSegments(2).toString();
 				edgeType = org.eclipse.stem.ui.preferences.Messages.getString(edgePrefix);
 			}
 			
 			if (!edgeTypeToUriPrefixMap.containsKey(edgeType)) {
 				edgeTypeToUriPrefixMap.put(edgeType, edgePrefix);
 				edgesCombo.add(edgeType);
 			}
 		}
 	}
 
 	/**
 	 * Initialize the control from a {@link Simulation} instance.
 	 * 
 	 * @param simulation
 	 *            the {@link Simulation}
 	 */
 	public void setSimulation(final ISimulation simulation) {
 		if (simulation == null)
 			return;
 		
 		setDecorators(getDecoratorsToDisplay(simulation));
 		setColorProviders(decorators);
 		geoViewOptionsGroup.setVisible(true);
 		setEdges();
 		edgesGroup.setVisible(true);
 		getInformation(simulation);
 	} // setSimulation
 
 	/**
 	 * @param decoratorFilter
 	 *            the filter that determines which {@link Decorator}s will be
 	 *            displayed.
 	 */
 	public void setDecoratorFilter(final DecoratorFilter decoratorFilter) {
 		this.decoratorFilter = decoratorFilter;
 	} // setDecoratorFilter
 
 	/**
 	 * 
 	 */
 	protected void updateColorProviderComposites() {
 		if (colorProviderComposite != null) {
 			colorProviderComposite.dispose();
 		}
 		if (colorsLegendComposite != null) {
 			colorsLegendComposite.dispose();
 		}
 		AdapterFactory adapterFactory = ColorProviderAdapterFactory.INSTANCE.getFactoryForType(selectedColorProvider);
 		if (adapterFactory instanceof IColorProviderAdapterFactory) {
 			IColorProviderAdapterFactory selectedColorProviderAdapterFactory = (IColorProviderAdapterFactory)adapterFactory;
 			
 			colorProviderComposite = selectedColorProviderAdapterFactory
 					.createPropertiesComposite(geoViewOptionsGroup, SWT.NONE, selectedDecorator);
 			// if there is no composite contribution from the selected color
 			// provider then return
 			if (colorProviderComposite == null) {
 				return;
 			}
 			if (colorProviderComposite instanceof ColorProviderPropertiesComposite) {
 				((ColorProviderPropertiesComposite) colorProviderComposite)
 						.addPropertySelectionListener(new ColorProviderPropertiesComposite.PropertySelectionListener() {
 							public void propertySelected(
 									final ColorProviderPropertiesComposite.PropertySelectionEvent propertySelectionEvent) {
 								firePropertySelectionEvent(new PropertySelectionEvent(
 										null, null, propertySelectionEvent
 												.getSource()));
 							}
 						});
 			}
 
 			final FormData colorProviderCompositeFormData = new FormData();
 			colorProviderCompositeFormData.top = new FormAttachment(0,0);
 			colorProviderCompositeFormData.left = new FormAttachment(DECORATORS_WIDTH, 0);
 			colorProviderCompositeFormData.right = new FormAttachment(PROVIDERS_WIDTH, 0);
 			colorProviderComposite.setLayoutData(colorProviderCompositeFormData);
 			
 			geoViewOptionsGroup.layout();
 			
 			colorsLegendComposite = selectedColorProviderAdapterFactory
 					.createColorsLegendComposite(this, SWT.NONE);
 			if (colorsLegendComposite != null) {
 				colorsLegendComposite.pack();
 				colorsLegendComposite.layout();
 			}
 			
 			this.layout();
 		}
 		
 		fireColorProviderChangedEvent();
 	}
 
 	/**
 	 * @param simulation
 	 *            the simulation that contains {@link Decorator}s
 	 * @return a {@link List} of the {@link Decorator}s whose properties
 	 */
 	private List<Decorator> getDecoratorsToDisplay(final ISimulation simulation) {
 		final List<Decorator> retValue = new ArrayList<Decorator>();
 
 		// Got Simulation?
 		if (simulation != null) {
 			// Yes
 			final Graph canonicalGraph = simulation.getScenario()
 					.getCanonicalGraph();
 			// Is the canonicalGraph initialized yet?
 			if (canonicalGraph != null) {
 				// Yes
 				// Go through the Decorators keeping the ones that the filter
 				// says are ok.
 				for (final Object element : canonicalGraph.getDecorators()) {
 					final Decorator decorator = (Decorator) element;
 					// Acceptable?
 					if (decoratorFilter.accept(decorator)) {
 						retValue.add(decorator);
 					} // if has a title
 				} // for each decorator
 			} // if got graph
 		} // if got simulation
 		return retValue;
 	} // getDecoratorsToDisplay
 
 	/**
 	 * @param simulation
 	 *            the simulation that contains {@link Decorator}s
 	 */
 	private void getInformation(final ISimulation simulation) {
 		// Got Simulation?
 		if (simulation != null) {
 			// Yes
 			final Graph canonicalGraph = simulation.getScenario()
 					.getCanonicalGraph();
 			// Is the canonicalGraph initialized yet?
 			if (canonicalGraph != null) {
 				canonicalGraph.getGraphLabels();
 				canonicalGraph
 						.getNodeLabelsByTypeURI(URI
 								.createURI("stemtype://org.eclipse.stem/label/population"));
 			} // if got graph
 		} // if got simulation
 	} // getDecoratorsToDisplay
 
 	/**
 	 * @param combo
 	 * @param names
 	 * @param selectionIndex
 	 */
 	private void initializeCombo(final Combo combo, final String[] names,
 			final int selectionIndex) {
 		combo.setItems(names);
 		combo.select(selectionIndex);
 	} // initializeCombo
 
 	/**
 	 * @param decorators
 	 *            a list of {@link Decorator}s
 	 * @return an array of the names of the {@link Decorators}.
 	 */
 	private String[] getDecoratorNames(final List<Decorator> decorators) {
 		final List<String> retValue = new ArrayList<String>();
 		if (decorators != null) {
 			// Yes
 			for (final Decorator decorator : decorators) {
 				String title = decorator.getDublinCore().getTitle();
 				title = title == null ? "null" : title;
 				retValue.add(title);
 			} // for each decorator
 		} // if
 		return retValue.toArray(new String[] {});
 	} // getDecoratorNames
 
 	/**
 	 * @param selectedDecorator
 	 * @param decorators
 	 *            a list of {@link Decorator}s.
 	 * @return the position (0 origin) of the selectedDecorator in decorators,
 	 *         or 0, if the decorators list is empty.
 	 */
 	private int getDecoratorIndex(final Decorator selectedDecorator,
 			final List<Decorator> decorators) {
 		if (selectedDecorator != null && decorators != null
 				&& !decorators.isEmpty()) {
 			// Yes
 			return decorators.indexOf(selectedDecorator);
 		} // if
 		return 0;
 	} // getDecoratorIndex
 
 	/**
 	 * @param listener
 	 */
 	public void addPropertySelectionListener(
 			final PropertySelectionListener listener) {
 		selectionListeners.add(listener);
 	}
 	
 	/**
 	 * @param listener
 	 */
 	public void removePropertySelectionListener(
 			final PropertySelectionListener listener) {
 		selectionListeners.remove(listener);
 	}
 
 	protected void firePropertySelectionEvent(final PropertySelectionEvent pse) {
 		for (final PropertySelectionListener listener : selectionListeners) {
 			listener.propertySelected(pse);
 		} // for each PropertySelectionListener
 	} // firePropertySelectionEvent
 	
 	/**
 	 * @param listener
 	 */
 	public void addColorProviderChangedListener(
 			final IColorProviderChangedListener listener) {
 		colorProviderChangedListeners.add(listener);
 	}
 	
 	/**
 	 * @param listener
 	 */
 	public void removeColorProviderChangedListener(
 			final IColorProviderChangedListener listener) {
 		colorProviderChangedListeners.remove(listener);
 	}
 
 	protected void fireColorProviderChangedEvent() {
 		for (final IColorProviderChangedListener listener : colorProviderChangedListeners) {
 			listener.colorProviderChanged(selectedColorProvider);
 		} // for each PropertySelectionListener
 	} // firePropertySelectionEvent
 
 	/**
 	 * This interface is implemented by classes that determine if a
 	 * {@link Decorator} meets some specified criteria.
 	 */
 	public interface DecoratorFilter {
 		/**
 		 * @param decorator
 		 * @return <code>true</code> if the {@link Decorator} is acceptable.
 		 */
 		boolean accept(final Decorator decorator);
 	} // DecoratorFilter
 
 	/**
 	 * This interface is implemented by classes that wish to be notified
 	 * whenever a property is selected in this widget.
 	 */
 	public interface PropertySelectionListener {
 		/**
 		 * @param propertySelectionEvent
 		 */
 		void propertySelected(PropertySelectionEvent propertySelectionEvent);
 	} // PropertySelectionListener
 
 	/**
 	 * This class represents the event of a property being selected in the
 	 * widget.
 	 */
 	public static class PropertySelectionEvent extends EventObject {
 		private static final long serialVersionUID = 1L;
 		private transient final ItemPropertyDescriptor property;
 		private transient final Decorator decorator;
 
 		/**
 		 * @param decorator
 		 *            the {@link Decorator}
 		 * @param property
 		 *            the property that was selected.
 		 * @param source
 		 */
 		public PropertySelectionEvent(final Decorator decorator,
 				final ItemPropertyDescriptor property, final Object source) {
 			super(source);
 			this.decorator = decorator;
 			this.property = property;
 		}
 
 		/**
 		 * @return the decorator
 		 */
 		public final Decorator getDecorator() {
 			return decorator;
 		}
 
 		/**
 		 * @return the property
 		 */
 		public final ItemPropertyDescriptor getProperty() {
 			return property;
 		}
 
 		/**
 		 * @see java.util.EventObject#toString()
 		 */
 		@Override
 		public String toString() {
 			final StringBuilder sb = new StringBuilder(
 					decorator == null ? "null," : decorator.getDublinCore()
 							.getTitle());
 			sb.append(property == null ? "null" : property
 					.getDisplayName(property));
 			return sb.toString();
 		}
 	} // PropertySelectionEvent
 
 	/**
 	 * @return the selectedDecorator
 	 */
 	public Decorator getSelectedDecorator() {
 		return selectedDecorator;
 	}
 } // PropertySelector

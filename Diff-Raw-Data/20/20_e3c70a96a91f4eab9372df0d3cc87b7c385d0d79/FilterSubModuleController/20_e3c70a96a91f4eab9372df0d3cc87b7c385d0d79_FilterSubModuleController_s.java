 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.internal.navigation.ui.filter.UIFilterRuleRidgetDisabledMarker;
 import org.eclipse.riena.internal.navigation.ui.filter.UIFilterRuleRidgetHiddenMarker;
 import org.eclipse.riena.internal.navigation.ui.filter.UIFilterRuleRidgetMandatoryMarker;
 import org.eclipse.riena.internal.navigation.ui.filter.UIFilterRuleRidgetOutputMarker;
 import org.eclipse.riena.internal.navigation.ui.filter.UIFilterRuleRidgetValidator;
import org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeUtility;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.OutputMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.filter.IUIFilter;
 import org.eclipse.riena.ui.filter.IUIFilterRule;
 import org.eclipse.riena.ui.filter.impl.UIFilter;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.validation.MaxLength;
 import org.eclipse.riena.ui.ridgets.validation.MinLength;
 import org.eclipse.riena.ui.ridgets.validation.ValidEmailAddress;
 
 /**
  * Controller of the sub module that demonstrates UI filters for ridgets.
  */
 public class FilterSubModuleController extends SubModuleController {
 
 	private IComboRidget filterTypeValues;
 	private IComboRidget globalFilterTypeValues;
 	private FilterModel filterModel;
 	private FilterModel globalFilterModel;
 	private MarkerModel markerModel;
 	private IActionRidget addFilter;
 	private IActionRidget globalAddFilter;
 	private IActionRidget addMarker;
 	private IActionRidget removeMarker;
 	private IMarker[] markers = new IMarker[] { new ErrorMarker(), new MandatoryMarker(), new HiddenMarker(),
 			new OutputMarker(), new DisabledMarker() };
 
 	/**
 	 * Enumeration of different kind of UI filters.
 	 */
 	private enum FilterType {
 
 		MARKER("Marker", new MandatoryMarker(), new HiddenMarker(), new OutputMarker(), new DisabledMarker()), //$NON-NLS-1$
 		VALIDATOR("Validator", new MinLength(3), new MaxLength(10), new ValidEmailAddress()); //$NON-NLS-1$
 
 		private String text;
 		private Object[] args;
 
 		private FilterType(String text, Object... args) {
 			this.text = text;
 			this.args = args;
 		}
 
 		@Override
 		public String toString() {
 			return text;
 		}
 
 		public Object[] getArgs() {
 			return args;
 		}
 
 	}
 
 	@Override
 	public void afterBind() {
 
 		super.afterBind();
 
 		initMarkerGroup();
 		initLocalFilterGroup();
 		initGlobalFilterGroup();
 
 		rebindFilterTypeValues(filterModel, filterTypeValues, addFilter);
 		rebindFilterTypeValues(globalFilterModel, globalFilterTypeValues, globalAddFilter);
 
 	}
 
 	/**
 	 * Initializes the ridgets of the marker group.
 	 */
 	private void initMarkerGroup() {
 
 		IComboRidget ridgetToMarkID = (IComboRidget) getRidget("ridgetToMarkID"); //$NON-NLS-1$
 		markerModel = new MarkerModel();
 		ridgetToMarkID.bindToModel(markerModel, "ids", MarkerModel.class, null, markerModel, "selectedId"); //$NON-NLS-1$ //$NON-NLS-2$
 		ridgetToMarkID.updateFromModel();
 
 		IComboRidget markersCombo = (IComboRidget) getRidget("markers"); //$NON-NLS-1$
 		markersCombo.bindToModel(markerModel, "markers", MarkerModel.class, null, markerModel, "selectedMarker"); //$NON-NLS-1$ //$NON-NLS-2$
 		markersCombo.updateFromModel();
 
 		addMarker = (IActionRidget) getRidget("addMarker"); //$NON-NLS-1$
 		addMarker.addListener(new IActionListener() {
 			public void callback() {
 				doAddMarker();
 			}
 		});
 
 		removeMarker = (IActionRidget) getRidget("removeMarker"); //$NON-NLS-1$
 		removeMarker.addListener(new IActionListener() {
 			public void callback() {
 				doRemoveMarker();
 			}
 		});
 
 	}
 
 	/**
 	 * Initializes the ridgets for adding <i>local</i> UI filters.
 	 */
 	private void initLocalFilterGroup() {
 
 		IComboRidget ridgetID = (IComboRidget) getRidget("ridgetID"); //$NON-NLS-1$
 		filterModel = new FilterModel();
 		ridgetID.bindToModel(filterModel, "ids", FilterModel.class, null, filterModel, "selectedId"); //$NON-NLS-1$ //$NON-NLS-2$
 		ridgetID.updateFromModel();
 
 		ISingleChoiceRidget filterType = (ISingleChoiceRidget) getRidget("filterType"); //$NON-NLS-1$		
 		filterType.addPropertyChangeListener(new FilterTypeChangeListener());
 		filterType.bindToModel(filterModel, "types", filterModel, "selectedType"); //$NON-NLS-1$ //$NON-NLS-2$
 		filterType.updateFromModel();
 
 		filterTypeValues = (IComboRidget) getRidget("filterTypeValues"); //$NON-NLS-1$
 		filterTypeValues.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (addFilter != null) {
 					addFilter.setEnabled(evt.getNewValue() != null);
 				}
 			}
 		});
 
 		addFilter = (IActionRidget) getRidget("addFilter"); //$NON-NLS-1$
 		addFilter.addListener(new IActionListener() {
 			public void callback() {
 				doAddFilter();
 			}
 		});
 
 		IActionRidget removeFilters = (IActionRidget) getRidget("removeFilters"); //$NON-NLS-1$
 		removeFilters.addListener(new IActionListener() {
 			public void callback() {
 				doRemoveFilters();
 			}
 		});
 
 	}
 
 	/**
 	 * Initializes the ridgets for adding <i>local</i> UI filters.
 	 */
 	private void initGlobalFilterGroup() {
 
 		ITextRidget ridgetID = (ITextRidget) getRidget("globalRidgetID"); //$NON-NLS-1$
 		globalFilterModel = new FilterModel();
 		ridgetID.bindToModel(globalFilterModel, "selectedId"); //$NON-NLS-1$
 		ridgetID.updateFromModel();
 
 		ISingleChoiceRidget filterType = (ISingleChoiceRidget) getRidget("globalFilterType"); //$NON-NLS-1$		
 		filterType.addPropertyChangeListener(new FilterTypeChangeListener());
 		filterType.bindToModel(globalFilterModel, "types", globalFilterModel, "selectedType"); //$NON-NLS-1$ //$NON-NLS-2$
 		filterType.updateFromModel();
 
 		globalFilterTypeValues = (IComboRidget) getRidget("globalFilterTypeValues"); //$NON-NLS-1$
 		globalFilterTypeValues.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (globalAddFilter != null) {
 					globalAddFilter.setEnabled(evt.getNewValue() != null);
 				}
 			}
 		});
 
 		globalAddFilter = (IActionRidget) getRidget("globalAddFilter"); //$NON-NLS-1$
 		globalAddFilter.addListener(new IActionListener() {
 			public void callback() {
 				doGlobalAddFilter();
 			}
 		});
 
 		IActionRidget removeFilters = (IActionRidget) getRidget("globalRemoveFilters"); //$NON-NLS-1$
 		removeFilters.addListener(new IActionListener() {
 			public void callback() {
 				doGlobalRemoveFilters();
 			}
 		});
 
 	}
 
 	/**
 	 * Adds a filter to the node of this sub module.
 	 */
 	private void doAddFilter() {
 		Collection<IUIFilterRule> attributes = new ArrayList<IUIFilterRule>(1);
 		attributes.add(createFilterRule(filterModel, true));
 		IUIFilter filter = new UIFilter(attributes);
 		getNavigationNode().addFilter(filter);
 	}
 
 	/**
 	 * Adds a filter to the node of the application.
 	 */
 	private void doGlobalAddFilter() {
 		Collection<IUIFilterRule> attributes = new ArrayList<IUIFilterRule>(1);
 		attributes.add(createFilterRule(globalFilterModel, false));
 		IUIFilter filter = new UIFilter(attributes);
 		IApplicationNode applNode = getNavigationNode().getParentOfType(IApplicationNode.class);
 		applNode.addFilter(filter);
 	}
 
 	/**
 	 * Creates a filter rule for a ridget, dependent on the selected type of
 	 * filter.
 	 * 
 	 * @param model
 	 *            - model with selections.
 	 * @return filter rule
 	 */
 	private IUIFilterRule createFilterRule(FilterModel model, boolean local) {
 
 		IUIFilterRule attribute = null;
 
 		String id = model.getSelectedId();
 		if (local) {
 			INavigationNode<ISubModuleNode> node = getNavigationNode();
 			id = NavigationNodeUtility.getNodeLongId(node) + "/" + id; //$NON-NLS-1$
 		} else {
 			id = "*/" + id; //$NON-NLS-1$
 		}
 		Object filterValue = model.getSelectedFilterTypeValue();
 		FilterType type = model.getSelectedType();
 
 		if (type == FilterType.MARKER) {
 			if (filterValue instanceof OutputMarker) {
 				attribute = new UIFilterRuleRidgetOutputMarker(id);
 			} else if (filterValue instanceof DisabledMarker) {
 				attribute = new UIFilterRuleRidgetDisabledMarker(id);
 			} else if (filterValue instanceof MandatoryMarker) {
 				attribute = new UIFilterRuleRidgetMandatoryMarker(id);
 			} else if (filterValue instanceof HiddenMarker) {
 				attribute = new UIFilterRuleRidgetHiddenMarker(id);
 			}
 		} else if (type == FilterType.VALIDATOR) {
 			if (filterValue instanceof IValidator) {
 				attribute = new UIFilterRuleRidgetValidator(id, (IValidator) filterValue,
 						ValidationTime.ON_UI_CONTROL_EDIT);
 			}
 		}
 
 		return attribute;
 
 	}
 
 	/**
 	 * Removes all filters form the node of this sub module.
 	 */
 	private void doRemoveFilters() {
 		getNavigationNode().removeAllFilters();
 	}
 
 	/**
 	 * Removes all filters form the node of application.
 	 */
 	private void doGlobalRemoveFilters() {
 		IApplicationNode applNode = getNavigationNode().getParentOfType(IApplicationNode.class);
 		applNode.removeAllFilters();
 	}
 
 	/**
 	 * The combo box for filter values is update with the given model. Also the
 	 * add button is enabled or disabled.
 	 * 
 	 * @param model
 	 * @param typeValues
 	 *            - combo box
 	 * @param add
 	 *            - add button
 	 */
 	private void rebindFilterTypeValues(FilterModel model, IComboRidget typeValues, IActionRidget add) {
 
 		if (model == null) {
 			return;
 		}
 		model.setSelectedFilterTypeValue(null);
 		if (typeValues != null) {
 			typeValues.bindToModel(new WritableList(Arrays.asList(model.getSelectedType().getArgs()), Object.class),
 					FilterModel.class, null, BeansObservables.observeValue(model, "selectedFilterTypeValue")); //$NON-NLS-1$
 			typeValues.updateFromModel();
 		}
 		if (add != null) {
 			add.setEnabled(model.getSelectedFilterTypeValue() != null);
 		}
 
 	}
 
 	/**
 	 * Adds a marker to a ridget.
 	 */
 	private void doAddMarker() {
 		if (markerModel.getSelectedId() != null) {
			AbstractSWTRidget ridget = (AbstractSWTRidget) getRidget(markerModel.getSelectedId());
 			ridget.addMarker(markerModel.getSelectedMarker().getMarker());
 		}
 	}
 
 	/**
 	 * Removes a marker to a ridget.
 	 */
 	private void doRemoveMarker() {
 		if (markerModel.getSelectedId() != null) {
			AbstractSWTRidget ridget = (AbstractSWTRidget) getRidget(markerModel.getSelectedId());
 			ridget.removeMarker(markerModel.getSelectedMarker().getMarker());
 		}
 	}
 
 	/**
 	 * After changing the type the combo boxes with the values must be updated.
 	 */
 	private class FilterTypeChangeListener implements PropertyChangeListener {
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			rebindFilterTypeValues(filterModel, filterTypeValues, addFilter);
 			rebindFilterTypeValues(globalFilterModel, globalFilterTypeValues, globalAddFilter);
 		}
 
 	}
 
 	/**
 	 * Model this the ID of the ridgets of the UI control group.
 	 */
 	private abstract class AbstractModel {
 
 		private List<String> ids;
 		private String selectedId;
 
 		public List<String> getIds() {
 			if (ids == null) {
 				ids = new ArrayList<String>();
 				Collection<? extends IRidget> ridgets = getRidgets();
 				for (IRidget ridget : ridgets) {
 					if ((ridget.getID() != null) && (ridget.getID().startsWith("ui_"))) { //$NON-NLS-1$
 						ids.add(ridget.getID());
 					}
 				}
 			}
 			return ids;
 		}
 
 		public void setSelectedId(String selectedId) {
 			this.selectedId = selectedId;
 		}
 
 		public String getSelectedId() {
 			if (selectedId == null) {
 				selectedId = getIds().get(0);
 			}
 			return selectedId;
 		}
 
 	}
 
 	/**
 	 * Model with markers of the marker group.
 	 */
 	private class MarkerModel extends AbstractModel {
 
 		private MarkerWrapper selectedMarker;
 		private List<MarkerWrapper> markerWrappers;
 
 		public void setSelectedMarker(MarkerWrapper selectedMarker) {
 			this.selectedMarker = selectedMarker;
 		}
 
 		public MarkerWrapper getSelectedMarker() {
 			return selectedMarker;
 		}
 
 		public List<MarkerWrapper> getMarkers() {
 			if (markerWrappers == null) {
 				markerWrappers = new ArrayList<MarkerWrapper>(markers.length);
 				for (IMarker marker : markers) {
 					markerWrappers.add(new MarkerWrapper(marker));
 				}
 				setSelectedMarker(markerWrappers.get(0));
 			}
 			return markerWrappers;
 		}
 
 	}
 
 	/**
 	 * Model with the filter types and value of the filter groups.
 	 */
 	private class FilterModel extends AbstractModel {
 
 		private List<FilterType> types;
 		private FilterType selectedType;
 		private Object selectedFilterTypeValue;
 
 		public List<FilterType> getTypes() {
 			if (types == null) {
 				types = new ArrayList<FilterType>();
 				types.add(FilterType.MARKER);
 				types.add(FilterType.VALIDATOR);
 			}
 			return types;
 		}
 
 		public void setSelectedType(FilterType selectedType) {
 			this.selectedType = selectedType;
 		}
 
 		public FilterType getSelectedType() {
 			if (selectedType == null) {
 				selectedType = getTypes().get(0);
 			}
 			return selectedType;
 		}
 
 		public void setSelectedFilterTypeValue(Object selectedFilterTypeValue) {
 			this.selectedFilterTypeValue = selectedFilterTypeValue;
 		}
 
 		public Object getSelectedFilterTypeValue() {
 			return selectedFilterTypeValue;
 		}
 
 	}
 
 	/**
 	 * Wrapper for IMarker with an own {@code toString()} method.
 	 */
 	private static class MarkerWrapper {
 
 		private IMarker marker;
 
 		public MarkerWrapper(IMarker marker) {
 			this.marker = marker;
 		}
 
 		public IMarker getMarker() {
 			return marker;
 		}
 
 		@Override
 		public String toString() {
 			if (getMarker() == null) {
 				return ""; //$NON-NLS-1$
 			}
 			return getMarker().getClass().getSimpleName();
 		}
 
 	}
 
 }

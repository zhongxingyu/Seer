 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.views.collections;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.layout.TreeColumnLayout;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.wazaabi.engine.core.editparts.CollectionEditPart;
 import org.eclipse.wazaabi.engine.core.views.CollectionView;
 import org.eclipse.wazaabi.engine.edp.PathException;
 import org.eclipse.wazaabi.engine.edp.locationpaths.IPointersEvaluator;
 import org.eclipse.wazaabi.engine.swt.views.SWTControlView;
 import org.eclipse.wazaabi.mm.core.styles.BooleanRule;
 import org.eclipse.wazaabi.mm.core.styles.ColorRule;
 import org.eclipse.wazaabi.mm.core.styles.FontRule;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 import org.eclipse.wazaabi.mm.core.styles.StyledElement;
 import org.eclipse.wazaabi.mm.core.styles.collections.ColumnDescriptor;
 import org.eclipse.wazaabi.mm.core.styles.collections.DynamicProvider;
 import org.eclipse.wazaabi.mm.core.styles.collections.LookAndFeel;
 import org.eclipse.wazaabi.mm.core.styles.collections.LookAndFeelRule;
 import org.eclipse.wazaabi.mm.core.styles.collections.PathSelector;
 import org.eclipse.wazaabi.mm.core.widgets.Collection;
 import org.eclipse.wazaabi.mm.swt.descriptors.SWTDescriptorsPackage;
 
 public class SWTCollectionView extends SWTControlView implements CollectionView {
 
 	private final ColumnManager columnManager = new ColumnManager(this);
 
 	private ITableLabelProvider labelProvider = null;
 
 	public ITableLabelProvider getLabelProvider() {
 		if (getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Combo
 				&& getViewer() != null)
 			return (ITableLabelProvider) getViewer().getLabelProvider();
 		return labelProvider;
 	}
 
 	public org.eclipse.swt.widgets.Control getSWTCollectionControl() {
 		if (getSWTWidget() instanceof org.eclipse.swt.widgets.Combo)
 			return (org.eclipse.swt.widgets.Combo) getSWTWidget();
 		else
 			return ((org.eclipse.swt.widgets.Composite) getSWTWidget())
 					.getChildren()[0];
 	}
 
 	public void setLabelProvider(ITableLabelProvider labelProvider) {
 		if (getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Combo
 				&& getViewer() != null)
 			getViewer().setLabelProvider(labelProvider);
 		else
 			this.labelProvider = labelProvider;
 	}
 
 	private boolean selectionChangedListenerBlocked = false;
 
 	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
 
 		@SuppressWarnings("unchecked")
 		public void selectionChanged(SelectionChangedEvent event) {
 			if (!selectionChangedListenerBlocked
 					&& event.getSelection() instanceof StructuredSelection) {
 				((CollectionEditPart) getHost()).blockSelectionListening();
 				try {
 					merge(((Collection) getHost().getModel()).getSelection(),
 							((StructuredSelection) event.getSelection())
 									.toList());
 				} finally {
 					((CollectionEditPart) getHost()).blockSelectionListening();
 
 				}
 			}
 		}
 
 	};
 
 	protected static void merge(EList<Object> previousList, List<Object> newList) {
 		List<Object> toRemove = new ArrayList<Object>();
 		for (Object previous : previousList)
 			if (!newList.contains(previous))
 				toRemove.add(previous);
 		for (Object item : toRemove)
 			previousList.remove(item);
 		for (int i = 0; i < newList.size(); i++)
 			if (i >= previousList.size())
 				previousList.add(i, newList.get(i));
 			else {
 				int idx = previousList.indexOf(newList.get(i));
 				if (idx != -1) {
 					if (idx != i)
 						previousList.move(i, idx);
 				} else
 					previousList.add(i, newList.get(i));
 			}
 	}
 
 	public EClass getWidgetViewEClass() {
 		return SWTDescriptorsPackage.Literals.COLLECTION;
 	}
 
 	@Override
 	public boolean needReCreateWidgetView(StyleRule rule) {
 		if (rule instanceof LookAndFeelRule
 				&& CollectionEditPart.LOOK_AND_FEEL_PROPERTY_NAME.equals(rule
 						.getPropertyName()))
 			return !isLookAndFeelCorrect(((LookAndFeelRule) rule).getValue());
 		else if (rule instanceof BooleanRule
 				&& !(getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Combo)
 				&& CollectionEditPart.ALLOW_ROW_SELECTION_PROPERTY_NAME
 						.equals(rule.getPropertyName()))
 			return !(isStyleBitCorrectlySet(getSWTCollectionControl(),
 					org.eclipse.swt.SWT.FULL_SELECTION,
 					((BooleanRule) rule).isValue()));
 		else
 			return super.needReCreateWidgetView(rule);
 	}
 
 	/**
 	 * Returns the LookAndFell associated to the model, null otherwise.
 	 * 
 	 * @return A LookAndFeel if found, null otherwise.
 	 */
 	protected LookAndFeel getLookAndFeel() {
 		for (StyleRule rule : ((StyledElement) getHost().getModel())
 				.getStyleRules())
 			if (CollectionEditPart.LOOK_AND_FEEL_PROPERTY_NAME.equals(rule
 					.getPropertyName()) && rule instanceof LookAndFeelRule)
 				return ((LookAndFeelRule) rule).getValue();
 		return null;
 	}
 
 	/**
 	 * given a LookAndFeel, returns whether the SWT Widget is an instance of the
 	 * corresponding class.
 	 * 
 	 * @param rule
 	 * @return
 	 */
 	protected boolean isLookAndFeelCorrect(LookAndFeel lookAndFeel) {
 		final org.eclipse.swt.widgets.Widget widget = getSWTCollectionControl();
 		switch (lookAndFeel.getValue()) {
 		case LookAndFeel.COMBOBOX_VALUE:
 			return widget instanceof org.eclipse.swt.widgets.Combo;
 		case LookAndFeel.TABLE_VALUE:
 			return widget instanceof org.eclipse.swt.widgets.Table;
 		case LookAndFeel.TREE_VALUE:
 			return widget instanceof org.eclipse.swt.widgets.Tree;
 		}
 		return false;
 	}
 
 	protected int computeSWTCreationStyleForTableOrTree() {
 		int result = SWT.FULL_SELECTION;
 		for (StyleRule rule : ((StyledElement) getHost().getModel())
 				.getStyleRules()) {
 			if (CollectionEditPart.ALLOW_ROW_SELECTION_PROPERTY_NAME
 					.equals(rule.getPropertyName())
 					&& rule instanceof BooleanRule) {
 				if (!((BooleanRule) rule).isValue())
 					result = SWT.NONE;
 			}
 		}
 		return result;
 	}
 
 	// protected int computeSWTCreationStyleForTableOrTree(WidgetEditPart
 	// editPart) {
 	// int style = SWT.None;
 	// ArrayList<String> processedStyles = new ArrayList<String>();
 	// for (StyleRule rule : ((StyledElement) getHost().getModel())
 	// .getStyleRules())
 	// if (!processedStyles.contains(rule.getPropertyName())) {
 	// processedStyles.add(rule.getPropertyName());
 	// style |= computeSWTCreationStyleForTableOrTree(rule);
 	// }
 	// return style;
 	// }
 
 	protected StructuredViewer viewer = null;
 
 	public StructuredViewer getViewer() {
 		return viewer;
 	}
 
 	// ////////////////
 	// TODO : patch
 	private class DefaultComboLabelProvider implements ITableLabelProvider,
 			ILabelProvider {
 
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		public void dispose() {
 		}
 
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			return element == null ? "" : element.toString();//$NON-NLS-1$
 		}
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public Image getImage(Object element) {
 			return null;
 		}
 
 		public String getText(Object element) {
 			return element == null ? "" : element.toString();//$NON-NLS-1$
 		}
 	};
 
 	// ////////////////
 
 	protected Widget createSWTWidget(Widget parent, int swtStyle, int index) {
 		int style = computeSWTCreationStyle(getHost());
 
 		LookAndFeel lookAndFeel = getLookAndFeel();
 		if (lookAndFeel == null)
 			lookAndFeel = LookAndFeel.TABLE;
 
 		switch (lookAndFeel.getValue()) {
 		case LookAndFeel.COMBOBOX_VALUE:
 			viewer = new ComboViewer(
 					(org.eclipse.swt.widgets.Composite) parent, style
 							| SWT.READ_ONLY) {
 
 				public void setLabelProvider(IBaseLabelProvider labelProvider) {
 					assert labelProvider instanceof ITableLabelProvider;
 					IBaseLabelProvider oldProvider = SWTCollectionView.this.labelProvider;
 					// If it hasn't changed, do nothing.
 					// This also ensures that the provider is not disposed
 					// if set a second time.
 					if (labelProvider == oldProvider) {
 						return;
 					}
 
 					SWTCollectionView.this.labelProvider = (ITableLabelProvider) labelProvider;
 
 					refresh();
 
 					// Dispose old provider after refresh, so that items never
 					// refer to stale images.
 					if (oldProvider != null) {
 						oldProvider.dispose();
 					}
 				}
 
 				public IBaseLabelProvider getLabelProvider() {
 					if (labelProvider == null)
 						labelProvider = new DefaultComboLabelProvider();
 					return labelProvider;
 				}
 
 			};
 			viewer.addSelectionChangedListener(getSelectionChangedListener());
 			return ((ComboViewer) viewer).getCombo();
 		case LookAndFeel.TREE_VALUE: {
 			org.eclipse.swt.widgets.Composite layoutHolder = new org.eclipse.swt.widgets.Composite(
 					(org.eclipse.swt.widgets.Composite) parent, SWT.NONE);
 			layoutHolder.setLayout(new TreeColumnLayout());
 			viewer = new TreeViewer(layoutHolder, style
 					| computeSWTCreationStyleForTableOrTree());
 			viewer.addSelectionChangedListener(getSelectionChangedListener());
 			return layoutHolder;
 		}
 		case LookAndFeel.TABLE_VALUE: {
 			org.eclipse.swt.widgets.Composite layoutHolder = new org.eclipse.swt.widgets.Composite(
 					(org.eclipse.swt.widgets.Composite) parent, SWT.NONE);
 			layoutHolder.setLayout(new TableColumnLayout());
 			viewer = new TableViewer(layoutHolder, style
 					| computeSWTCreationStyleForTableOrTree());
 			viewer.addSelectionChangedListener(getSelectionChangedListener());
 			return layoutHolder;
 		}
 		}
 		throw new RuntimeException("Invalid LookAndFeel value"); //$NON-NLS-1$
 	}
 
 	public void setInput(Object input) {
 		if (!getSWTControl().isDisposed() && getViewer() != null
 				&& getViewer().getContentProvider() != null)
 			getViewer().setInput(input);
 	}
 
 	public void updateSameStyleRules(List<StyleRule> rules) {
 		if (CollectionEditPart.COLUMN_DESCRIPTOR_PROPERTY_NAME.equals(rules
 				.get(0).getPropertyName()))
 			columnManager.update(rules);
 		else if (CollectionEditPart.CONTENT_PROVIDER_PROPERTY_NAME.equals(rules
 				.get(0).getPropertyName()))
 			updateContentProvider(rules);
 		else if (CollectionEditPart.LABEL_RENDERER_PROPERTY_NAME.equals(rules
 				.get(0).getPropertyName()))
 			updateLabelRenderer(rules);
 		else if (CollectionEditPart.DYNAMIC_PROVIDER_PROPERTY_NAME.equals(rules
 				.get(0).getPropertyName()))
 			updateDynamicProviders(rules);
 	}
 
 	protected void updateDynamicProviders(List<StyleRule> rules) {
 		if (!rules.isEmpty()) {
 			List<String> uris = new ArrayList<String>();
 			for (StyleRule rule : rules)
 				if (!uris.contains(((DynamicProvider) rule).getUri()))
 					uris.add(((DynamicProvider) rule).getUri());
 
 			if (getViewer() != null) {
 				if (!(getViewer().getContentProvider() instanceof DynamicContentProvider)) {
 					if (getViewer().getContentProvider() != null)
 						getViewer().getContentProvider().dispose();
 					getViewer()
 							.setContentProvider(new DynamicContentProvider());
 				}
 				if (!(getLabelProvider() instanceof DynamicLabelProvider)) {
 					if (getLabelProvider() != null)
 						getLabelProvider().dispose();
 					setLabelProvider(new DynamicLabelProvider());
 				}
 
 				((DynamicContentProvider) getViewer().getContentProvider())
 						.updateDynamicProviderURIs(uris);
 				((DynamicLabelProvider) getLabelProvider())
 						.updateDynamicProviderURIs(uris);
 			}
 		}
 	}
 
 	protected void updateContentProvider(List<StyleRule> rules) {
 		final Hashtable<String, List<String>> selectors = getSelectors(rules);
 		if (getViewer() != null)
 			getViewer().setContentProvider(
 					new PathSelectorContentProvider(this, selectors));
 	}
 
 	protected Hashtable<String, List<String>> getSelectors(List<StyleRule> rules) {
 		Hashtable<String, List<String>> selectors = new Hashtable<String, List<String>>();
 		for (StyleRule rule : rules) {
 			if (rule instanceof PathSelector) {
 				PathSelector pathSelector = (PathSelector) rule;
 				if (pathSelector.getEClassifierName() == null
 						|| "".equals(pathSelector.getEClassifierName()) || pathSelector.getPaths().isEmpty()) //$NON-NLS-1$ 
 					continue;
 				List<String> paths = selectors.get(pathSelector
 						.getEClassifierName());
 				if (paths == null) {
 					paths = new ArrayList<String>();
 					selectors.put(pathSelector.getEClassifierName(), paths);
 				}
 				for (String path : pathSelector.getPaths())
 					paths.add(path);
 			}
 		}
 		return selectors;
 	}
 
 	protected void updateLabelRenderer(List<StyleRule> rules) {
 		final Hashtable<String, List<String>> selectors = getSelectors(rules);
 		setLabelProvider(new org.eclipse.wazaabi.engine.swt.views.collections.PathSelectorLabelProvider(
 				this, selectors));
 	}
 
 	protected List<ColumnDescriptor> getColumnDescriptors() {
 		List<ColumnDescriptor> columnDescriptors = new ArrayList<ColumnDescriptor>();
 		for (StyleRule rule : ((StyledElement) getHost().getModel())
 				.getStyleRules())
 			if (rule instanceof ColumnDescriptor
 					&& CollectionEditPart.COLUMN_DESCRIPTOR_PROPERTY_NAME
 							.equals(rule.getPropertyName()))
 				columnDescriptors.add((ColumnDescriptor) rule);
 		return columnDescriptors;
 	}
 
 	protected ISelectionChangedListener getSelectionChangedListener() {
 		return selectionChangedListener;
 	}
 
 	public void refresh() {
 		if (getSWTCollectionControl().isDisposed())
 			return;
 		if (getViewer() != null)
 			getViewer().refresh();
 	}
 
 	public void setSelection(List<Object> newSelection) {
 		if (getSWTCollectionControl().isDisposed())
 			return;
 		IStructuredSelection selection = new StructuredSelection(newSelection);
 		selectionChangedListenerBlocked = true;
 		try {
 			if (getViewer() != null)
 				viewer.setSelection(selection);
 		} finally {
 			selectionChangedListenerBlocked = false;
 		}
 	}
 
 	protected Object[] getElements(Object inputElement,
 			Hashtable<String, List<String>> selectors) {
 		if (inputElement instanceof EObject) {
 			String eClassName = ((EObject) inputElement).eClass().getName();
 			List<Object> result = new ArrayList<Object>();
 			IPointersEvaluator pointersEvaluator = getHost().getViewer()
 					.getPointersEvaluator();
 			List<String> paths = selectors.get(eClassName);
 			for (String path : paths) {
 				try {
 					List<?> pointers = pointersEvaluator.selectPointers(
 							inputElement, path);
 					for (Object pointer : pointers) {
 						Object value = pointersEvaluator.getValue(pointer);
 						if (value instanceof List)
 							result.addAll((List<?>) value);
 						else
 							result.add(value);
 					}
 				} catch (PathException e) {
 					System.err.println(e.getMessage()); // TODO : log that
 				}
 
 			}
 			return result.toArray();
 		}
 		return new Object[] {};
 	}
 
 	@Override
 	protected void widgetDisposed() {
 		columnManager.dispose();
 		super.widgetDisposed();
 	}
 
 	public void setHeaderVisible(boolean show) {
 		if (getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Tree)
 			((org.eclipse.swt.widgets.Tree) getSWTCollectionControl())
 					.setHeaderVisible(show);
 		else if (getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Table)
 			((org.eclipse.swt.widgets.Table) getSWTCollectionControl())
 					.setHeaderVisible(show);
 	}
 
 	public void setShowHorizontalLines(boolean show) {
 		if (getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Tree)
 			((org.eclipse.swt.widgets.Tree) getSWTCollectionControl())
 					.setLinesVisible(show);
 		else if (getSWTCollectionControl() instanceof org.eclipse.swt.widgets.Table)
 			((org.eclipse.swt.widgets.Table) getSWTCollectionControl())
 					.setLinesVisible(show);
 	}
 
 	@Override
 	public void updateStyleRule(StyleRule rule) {
 		if (rule != null) {
 			if (CollectionEditPart.HEADER_VISIBLE_PROPERTY_NAME.equals(rule
 					.getPropertyName())) {
 				if (rule instanceof BooleanRule)
 					setHeaderVisible(((BooleanRule) rule).isValue());
 				else
 					setHeaderVisible(false);
 			} else if (CollectionEditPart.SHOW_HORIZONTAL_LINES_PROPERTY_NAME
 					.equals(rule.getPropertyName())) {
 				if (rule instanceof BooleanRule)
 					setShowHorizontalLines(((BooleanRule) rule).isValue());
 				else
 					setShowHorizontalLines(false);
 			} else
 				super.updateStyleRule(rule);
 		}
 	}
 
 	@Override
 	protected void setBackgroundColor(ColorRule colorRule) {
 		super.setBackgroundColor(getSWTCollectionControl(), colorRule);
 	}
 
 	@Override
 	protected void setForegroundColor(ColorRule colorRule) {
 		super.setForegroundColor(getSWTCollectionControl(), colorRule);
 	}
 
 	@Override
 	public void setFont(FontRule fontRule) {
 		super.setFont(getSWTCollectionControl(), fontRule);
 	}
 
 }

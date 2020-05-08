 /*******************************************************************************
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.viewerpart;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.diff.AddModelElement;
 import org.eclipse.emf.compare.diff.AttributeChange;
 import org.eclipse.emf.compare.diff.DiffElement;
 import org.eclipse.emf.compare.diff.DiffGroup;
 import org.eclipse.emf.compare.diff.DiffModel;
 import org.eclipse.emf.compare.diff.RemoveModelElement;
 import org.eclipse.emf.compare.match.Match2Elements;
 import org.eclipse.emf.compare.match.MatchModel;
 import org.eclipse.emf.compare.match.UnMatchElement;
 import org.eclipse.emf.compare.ui.EMFCompareUIPlugin;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.TypedElementWrapper;
 import org.eclipse.emf.compare.ui.contentmergeviewer.ModelContentMergeViewer;
 import org.eclipse.emf.compare.ui.contentprovider.PropertyContentProvider;
 import org.eclipse.emf.compare.ui.util.EMFAdapterFactoryProvider;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.swt.widgets.Widget;
 
 //TODO handle horizontal sync of viewer parts
 /**
  * Describes a part of a {@link ModelContentMergeViewer}.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelContentMergeViewerPart {
 	private static final String INVALID_TAB = "Invalid tab index"; //$NON-NLS-1$
 	private final List<ICompareEditorPartListener> editorPartListeners = new ArrayList<ICompareEditorPartListener>();
 	private int selectedTab;
 	private CTabFolder tabFolder;
 	private ModelContentMergeViewer parentViewer;
 	private ModelContentMergeTreePart tree;
 	private ModelContentMergePropertyPart properties;
 	private int partSide;
 	
 	/**
 	 * Instantiates a {@link ModelContentMergeViewerPart} given its parent 
 	 * {@link Composite} and its side.
 	 * 
 	 * @param viewer
 	 * 			Parent viewer of this viewer part.
 	 * @param composite
 	 * 			Parent {@link Composite} for this part.
 	 * @param side
 	 * 			Comparison side of this part. Must be one of 
 	 * 			{@link EMFCompareConstants#LEFT EMFCompareConstants.LEFT},  
 	 * 			{@link EMFCompareConstants#RIGHT EMFCompareConstants.RIGHT} or 
 	 * 			{@link EMFCompareConstants#ANCESTOR EMFCompareConstants.ANCESTOR}.
 	 */
 	public ModelContentMergeViewerPart(ModelContentMergeViewer viewer, Composite composite, int side) {
 		if (side != EMFCompareConstants.LEFT && side != EMFCompareConstants.RIGHT && side != EMFCompareConstants.ANCESTOR)
 			throw new IllegalArgumentException("PartSide cannot be " + side); //$NON-NLS-1$
 		
 		parentViewer = viewer;
 		selectedTab = ModelContentMergeViewer.TREE_TAB;
 		partSide = side;
 		createContents(composite);
 		
 		addCompareEditorPartListener(new ICompareEditorPartListener() {
 			public void selectedTabChanged(final int newIndex) {
 				// Prevents resizing behavior from the part
 				resizeBounds();
 			}
 
 			public void selectionChanged(final SelectionChangedEvent event) {
 				// We don't need to implement this
 			}
 
 			public void updateCenter() {
 				// We don't need to implement this
 			}
 		});
 	}
 	
 	/**
 	 * Creates the contents of this viewer part given its parent composite.
 	 * 
 	 * @param composite
 	 * 			Parent composite of this viewer parts's widgets.
 	 */
 	public void createContents(Composite composite) {
 		tabFolder = new CTabFolder(composite, SWT.BOTTOM);
 		final CTabItem treeTab = new CTabItem(tabFolder, SWT.NONE);
 		treeTab.setText("Tree"); //$NON-NLS-1$
 
 		final CTabItem propertiesTab = new CTabItem(tabFolder, SWT.NONE);
 		propertiesTab.setText("Properties"); //$NON-NLS-1$
 
 		final Composite treePanel = new Composite(tabFolder, SWT.NONE);
 		treePanel.setLayout(new GridLayout());
 		treePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
 		treePanel.setFont(composite.getFont());
 		tree = createTreePart(treePanel);
 		treeTab.setControl(treePanel);
 
 		final Composite propertyPanel = new Composite(tabFolder, SWT.NONE);
 		propertyPanel.setLayout(new GridLayout());
 		propertyPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
 		propertyPanel.setFont(composite.getFont());
 		properties = createPropertiesPart(propertyPanel, partSide);
 		propertiesTab.setControl(propertyPanel);
 		
 		tabFolder.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(final SelectionEvent e) {
 				if (e.item.equals(treeTab)) {
					ModelContentMergeViewerPart.this.selectedTab = ModelContentMergeViewer.TREE_TAB;
 				} else {
 					if (e.item.equals(propertiesTab)) {
 						ModelContentMergeViewerPart.this.selectedTab = ModelContentMergeViewer.PROPERTIES_TAB;
 					}
 				}
 				fireSelectedtabChanged();
 			}
 
 			public void widgetDefaultSelected(final SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		tabFolder.setSelection(treeTab);
 	}
 	
 	/**
 	 * Returns the {@link Widget} representing the given element or <code>null</code>
 	 * if it cannot be found.
 	 * 
 	 * @param element
 	 * 			Element to find the {@link Widget} for.
 	 * @return
 	 * 			The {@link Widget} representing the given element.
 	 */
 	public Widget find(EObject element) {
 		Widget widget = null;
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			widget = tree.findVisibleTreeItemFor(element);
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			if (element instanceof DiffElement)
 				widget = properties.find((DiffElement)element);
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		return widget;
 	}
 	
 	/**
 	 * Returns the visible height of the painted elements (default 0). Allows accurate
 	 * painting of the center area.
 	 * 
 	 * @return
 	 * 			The visible height of the painted elements.
 	 */
 	public int getVisibleHeight() {
 		int visibleHeight;
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			visibleHeight = tabFolder.getClientArea().height - tabFolder.getTabHeight();
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			visibleHeight = properties.getTable().getClientArea().height - properties.getTable().getHeaderHeight();
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		return visibleHeight;
 	}
 	
 	/**
 	 * Returns a list of the selected tab's visible Elements.
 	 * 
 	 * @return
 	 * 			The selected tab's visible Elements.
 	 */
 	public List<TreeItem> getVisibleElements() {
 		List<TreeItem> visibleElements = null;
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			visibleElements = tree.getVisibleElements();
 		} else if (selectedTab != ModelContentMergeViewer.PROPERTIES_TAB) {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		return visibleElements;
 	}
 	
 	/**
 	 * Returns a list of the selected tab's selected Elements.
 	 * 
 	 * @return
 	 * 			The selected tab's selected Elements.
 	 */
 	public List<TreeItem> getSelectedElements() {
 		List<TreeItem> selectedElements = null;
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			selectedElements = tree.getSelectedElements();
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			System.out.println(((StructuredSelection)properties.getSelection()).getFirstElement());
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		return selectedElements;
 	}
 	
 	/**
 	 * Redraws this viewer part.
 	 */
 	public void layout() {
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.getTree().redraw();
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.getTable().redraw();
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 	}
 	
 	/**
 	 * Sets the input of this viewer part.
 	 * 
 	 * @param input
 	 * 			New input of this viewer part.
 	 */
 	public void setInput(Object input) {
 		Object typedInput = input;
 		if (typedInput instanceof TypedElementWrapper) {
 			typedInput = ((TypedElementWrapper)typedInput).getObject();
 		}
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.setReflectiveInput((EObject)typedInput);
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.setInput(typedInput);
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 	}
 	
 	/**
 	 * Sets the receiver's size and location to the rectangular area specified by 
 	 * the arguments.
 	 * @param x
 	 * 			Desired x coordinate of the part.
 	 * @param y
 	 * 			Desired y coordinate of the part.
 	 * @param width
 	 * 			Desired width of the part.
 	 * @param height
 	 * 			Desired height of the part.
 	 */
 	public void setBounds(int x, int y, int width, int height) {
 		setBounds(new Rectangle(x, y, width, height));
 	}
 	
 	/**
 	 * Sets the receiver's size and location to given rectangular area.
 	 * 
 	 * @param bounds
 	 * 			Desired bounds for this receiver.
 	 */
 	public void setBounds(Rectangle bounds) {
 		tabFolder.setBounds(bounds);
 		resizeBounds();
 	}
 	
 	/**
 	 * Sets the background color of this viewer part.
 	 * 
 	 * @param color
 	 * 			{@link Color} to switch the background to.
 	 */
 	public void setBackground(Color color) {
 		tree.getTree().setBackground(color);
 		properties.getTable().setBackground(color);
 	}
 	
 	/**
 	 * Changes the current tab.
 	 * 
 	 * @param index
 	 * 			New tab to set selected.
 	 */
 	public void setSelectedTab(int index) {
 		selectedTab = index;
 		tabFolder.setSelection(selectedTab);
 		resizeBounds();
 	}
 	
 	/**
 	 * Sets the tree's selection given the list of elements to set selected.
 	 * 
 	 * @param selection
 	 * 			New selection for the tree.
 	 */
 	public void setSelectedElements(List<TreeItem> selection) {
 		tree.setSelectedElements(selection);
 	}
 	
 	/**
 	 * Shows the given item on the tree tab or its properties on the property tab.
 	 * 
 	 * @param match
 	 * 			Item to scroll to.
 	 */
 	public void navigateToMatch(Match2Elements match) {
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.showItem(match);
 		} else if (selectedTab != ModelContentMergeViewer.PROPERTIES_TAB) {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		properties.setInput(match);
 		parentViewer.updateCenter();
 	}
 	
 	/**
 	 * Shows the given item on the tree tab or its properties on the property tab.
 	 * 
 	 * @param diff
 	 * 			Item to scroll to.
 	 */
 	public void navigateToDiff(DiffElement diff) {
 		final EObject target = findElementFromDiff(diff);
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.showItem(target);
 			properties.setInput(findMatchFromDiff(target));
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.setInput(findMatchFromDiff(target));
 			properties.showItem(diff);
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		parentViewer.updateCenter();
 	}
 	
 	/**
 	 * Shows the given attribute on the property tab.
 	 * 
 	 * @param attribute
 	 * 			Attribute to scroll to.
 	 */
 	public void navigateToProperty(EAttribute attribute) {
 		properties.showItem(attribute);
 	}
 	
 	/**
 	 * Registers the given listener for notification.
 	 * If the identical listener is already registered the method has no effect.
 	 *
 	 * @param listener
 	 * 			The listener to register for changes of this input.
 	 */
 	public void addCompareEditorPartListener(ICompareEditorPartListener listener) {
 		editorPartListeners.add(listener);
 	}
 	
 	/**
 	 * Unregisters the given listener.
 	 * If the identical listener is not registered the method has no effect.
 	 *
 	 * @param listener
 	 *			The listener to unregister.
 	 */
 	public void removePartListener(ICompareEditorPartListener listener) {
 		editorPartListeners.remove(listener);
 	}
 	
 	protected void fireSelectedtabChanged() {
 		for (ICompareEditorPartListener listener : editorPartListeners) {
 			listener.selectedTabChanged(selectedTab);
 		}
 	}
 
 	protected void fireSelectionChanged(SelectionChangedEvent event) {
 		for (ICompareEditorPartListener listener : editorPartListeners) {
 			listener.selectionChanged(event);
 		}
 	}
 
 	protected void fireUpdateCenter() {
 		for (ICompareEditorPartListener listener : editorPartListeners) {
 			listener.updateCenter();
 		}
 	}
 	
 	private EObject findElementFromDiff(DiffElement diff) {
 		EObject element = null;
 		
 		if (partSide == EMFCompareConstants.LEFT) {
 			element = EMFCompareEObjectUtils.getLeftElement(diff);
 		} else if (partSide == EMFCompareConstants.RIGHT) {
 			element = EMFCompareEObjectUtils.getRightElement(diff);
 		}
 		
 		return element;
 	}
 	
 	private Object findMatchFromDiff(EObject element) {
 		Object theElement = null;
 		final MatchModel match = ((ModelCompareInput)parentViewer.getInput()).getMatch();
 		
 		for (final TreeIterator iterator = match.eAllContents(); iterator.hasNext(); ) {
 			final Object object = iterator.next();
 			
 			if (object instanceof Match2Elements) {
 				final Match2Elements matchElement = (Match2Elements)object;
 				if (matchElement.getLeftElement().equals(element) 
 						|| matchElement.getRightElement().equals(element)) {
 					theElement = matchElement;
 				}
 			} else if (object instanceof UnMatchElement) {
 				final UnMatchElement matchElement = (UnMatchElement)object;
 				if (matchElement.getElement().equals(element)) {
 					theElement = matchElement;
 				}
 			}
 		}
 		
 		return theElement;
 	}
 	
 	private ModelContentMergeTreePart createTreePart(Composite composite) {
 		final ModelContentMergeTreePart treePart = new ModelContentMergeTreePart(composite);
 
 		treePart.setContentProvider(new AdapterFactoryContentProvider(EMFAdapterFactoryProvider.getAdapterFactory()));
 		
 		treePart.getTree().addPaintListener(new TreePaintListener());
 
 		treePart.getTree().getVerticalBar().addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				fireUpdateCenter();
 			}
 			
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		treePart.getTree().addTreeListener(new TreeListener() {
 			public void treeCollapsed(TreeEvent e) {
 				((TreeItem)e.item).setExpanded(false);
 				e.doit = false;
 				fireUpdateCenter();
 			}
 
 			public void treeExpanded(final TreeEvent e) {
 				((TreeItem)e.item).setExpanded(true);
 				e.doit = false;
 				fireUpdateCenter();
 			}
 		});
 		
 		treePart.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				fireSelectionChanged(event);
 			}
 		});
 		
 		treePart.getTree().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
				final TreeItem selected = tree.getSelectedElements().get(0);
				final DiffModel diffElements = ((ModelCompareInput)parentViewer.getInput()).getDiff();
				for (final TreeIterator iterator = diffElements.eAllContents(); iterator.hasNext(); ) {
					final DiffElement diff = (DiffElement)iterator.next();
					if (!(diff instanceof DiffGroup) && partSide == EMFCompareConstants.LEFT) {
						if (selected.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff)))
							parentViewer.setTreeSelection(diff);
					} else if (!(diff instanceof DiffGroup) && partSide == EMFCompareConstants.RIGHT) {
						if (selected.getData().equals(EMFCompareEObjectUtils.getRightElement(diff)))
							parentViewer.setTreeSelection(diff);
					} 
 				}
 			}
 		});
 
 		return treePart;
 	}
 	
 	private ModelContentMergePropertyPart createPropertiesPart(Composite composite, int side) {
 		final ModelContentMergePropertyPart propertiesPart = new ModelContentMergePropertyPart(
 				composite, SWT.NONE, partSide);
 		
 		propertiesPart.setContentProvider(new PropertyContentProvider());
 		
 		propertiesPart.getTable().addPaintListener(new PropertyPaintListener());
 		
 		propertiesPart.getTable().getVerticalBar().addSelectionListener(
 				new SelectionListener() {
 					public void widgetSelected(SelectionEvent e) {
 						parentViewer.updateCenter();
 					}
 					
 					public void widgetDefaultSelected(SelectionEvent e) {
 						widgetSelected(e);
 					}
 				});
 		
 		propertiesPart.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				fireSelectionChanged(event);
 			}
 		});
 		
 		return propertiesPart;
 		
 	}
 	
 	private void resizeBounds() {
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.getTree().setBounds(this.tabFolder.getClientArea());
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.getTable().setBounds(this.tabFolder.getClientArea());
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 	}
 	
 	/**
 	 * This implementation of {@link PaintListener} handles the drawing of
 	 * blocks around modified members in the tree tab.
 	 */
 	private class TreePaintListener implements PaintListener {
 		public void paintControl(PaintEvent event) {
 			for (final DiffElement diff : ((ModelCompareInput)parentViewer.getInput()).getDiffAsList()) {
 				if (partSide == EMFCompareConstants.LEFT) {
 					drawRectangle(event, (TreeItem)find(EMFCompareEObjectUtils.getLeftElement(diff)), diff);
 				} else if (partSide == EMFCompareConstants.RIGHT) {
 					drawRectangle(event, (TreeItem)find(EMFCompareEObjectUtils.getRightElement(diff)), diff);
 				} 
 			}
 		}
 		
 		private void drawRectangle(PaintEvent event, TreeItem treeItem, DiffElement diff) {
 			final Rectangle treeBounds = tree.getTree().getBounds();
 			final Rectangle treeItemBounds = treeItem.getBounds();
 			final IPreferenceStore comparePreferences = EMFCompareUIPlugin.getDefault().getPreferenceStore();
 			RGB color = PreferenceConverter.getColor(comparePreferences, EMFCompareConstants.PREFERENCES_KEY_CHANGED_COLOR);
 			treeItem.setBackground(new Color(treeItem.getDisplay(), 
 					PreferenceConverter.getColor(comparePreferences, EMFCompareConstants.PREFERENCES_KEY_HIGHLIGHT_COLOR)));
 			
 			// Defines the circling Color
 			if (diff instanceof AddModelElement) {
 				color = PreferenceConverter.getColor(comparePreferences, EMFCompareConstants.PREFERENCES_KEY_ADDED_COLOR);
 			} else if (diff instanceof RemoveModelElement) {
 				color = PreferenceConverter.getColor(comparePreferences, EMFCompareConstants.PREFERENCES_KEY_REMOVED_COLOR);
 			}
 			
 			/*
 			 * We add a margin before the rectangle to circle the "+" as
 			 * well as the tree line.
 			 */
 			final int margin = 40;
 			
 			// Defines all variables needed for drawing the rectangle.
 			final int rectangleX = treeItemBounds.x - margin;
 			final int rectangleY = treeItemBounds.y;
 			final int rectangleWidth = treeItemBounds.width + margin;
 			final int rectangleHeight = treeItemBounds.height - 1;
 			final int rectangleArcWidth = 5;
 			final int rectangleArcHeight = 5;
 			
 			int lineWidth = 1;
 			if (getSelectedElements().contains(treeItem)) {
 				lineWidth = 2;
 			}
 			
 			// Performs the actual drawing
 			event.gc.setLineWidth(lineWidth);
 			event.gc.setForeground(new Color(treeItem.getDisplay(), color));
 			if (partSide == EMFCompareConstants.LEFT) {
 				if (!treeItem.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff))
 						|| diff instanceof AddModelElement) {
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(
 							rectangleX, rectangleY + rectangleHeight, 
 							treeBounds.width, rectangleY + rectangleHeight);
 				} else {
 					event.gc.setLineStyle(SWT.LINE_DASHDOT);
 					event.gc.drawRoundRectangle(
 							rectangleX, rectangleY, 
 							rectangleWidth, rectangleHeight,
 							rectangleArcWidth, rectangleArcHeight);
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(
 							rectangleX + rectangleWidth, rectangleY + rectangleHeight / 2,
 							treeBounds.width, rectangleY + rectangleHeight / 2);
 				}
 			} else if (partSide == EMFCompareConstants.RIGHT) {
 				if (!treeItem.getData().equals(EMFCompareEObjectUtils.getRightElement(diff))
 						|| diff instanceof RemoveModelElement) {
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(
 							rectangleX + rectangleWidth, rectangleY + rectangleHeight, 
 							treeBounds.x, rectangleY + rectangleHeight);
 				} else {
 					event.gc.setLineStyle(SWT.LINE_DASHDOT);
 					event.gc.drawRoundRectangle(
 							rectangleX, rectangleY, 
 							rectangleWidth, rectangleHeight,
 							rectangleArcWidth, rectangleArcHeight);
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(
 							rectangleX, rectangleY + rectangleHeight / 2,
 							treeBounds.x, rectangleY + rectangleHeight / 2);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This implementation of {@link PaintListener} handles the drawing of
 	 * blocks around modified members in the properties tab.
 	 */
 	private class PropertyPaintListener implements PaintListener {
 		public void paintControl(PaintEvent event) {
 			for (final DiffElement diff : ((ModelCompareInput)parentViewer.getInput()).getDiffAsList()) {
 				if (diff instanceof AttributeChange && find(diff) != null
 						&& partSide == EMFCompareConstants.LEFT) {
 					drawLine(event, (TableItem)find(diff));
 				} 
 			}
 		}
 		
 		private void drawLine(PaintEvent event, TableItem tableItem) {
 			final Rectangle tableBounds = properties.getTable().getBounds();
 			final Rectangle tableItemBounds = tableItem.getBounds();
 			final IPreferenceStore comparePreferences = EMFCompareUIPlugin.getDefault().getPreferenceStore();
 			tableItem.setBackground(new Color(tableItem.getDisplay(), 
 					PreferenceConverter.getColor(comparePreferences, EMFCompareConstants.PREFERENCES_KEY_HIGHLIGHT_COLOR)));
 			
 			event.gc.setLineWidth(2);
 			event.gc.setForeground(new Color(tableItem.getDisplay(), 
 					PreferenceConverter.getColor(comparePreferences, EMFCompareConstants.PREFERENCES_KEY_CHANGED_COLOR)));
 			event.gc.drawLine(
 					getTotalColumnsWidth(), tableItemBounds.y + tableItemBounds.height / 2, 
 					tableBounds.width, tableItemBounds.y + tableItemBounds.height / 2);
 		}
 		
 		private int getTotalColumnsWidth() {
 			int width = 0;
 			for (final TableColumn col : properties.getTable().getColumns()) {
 				width += col.getWidth();
 			}
 			return width;
 		}
 	}
 }

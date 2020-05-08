 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.viewer.content.part;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChange;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Elements;
 import org.eclipse.emf.compare.match.metamodel.UnmatchElement;
 import org.eclipse.emf.compare.ui.EMFCompareUIMessages;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.compare.ui.viewer.content.ModelContentMergeViewer;
 import org.eclipse.emf.compare.ui.viewer.content.part.diff.ModelContentMergeDiffTab;
 import org.eclipse.emf.compare.ui.viewer.content.part.property.ModelContentMergePropertyTab;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Scrollable;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Describes a part of a {@link ModelContentMergeViewer}.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ModelContentMergeTabFolder {
 	/** This keeps track of the parent viewer of this tab folder. */
 	protected final ModelContentMergeViewer parentViewer;
 
 	/**
 	 * This <code>int</code> represents the side of this viewer part. Must be one of
 	 * <ul>
 	 * <li>{@link EMFCompareConstants#RIGHT}</li>
 	 * <li>{@link EMFCompareConstants#LEFT}</li>
 	 * <li>{@link EMFCompareConstants#ANCESTOR}</li>
 	 * </ul>
 	 */
 	protected final int partSide;
 
 	/** This is the content of the properties tab for this viewer part. */
 	protected IModelContentMergeViewerTab properties;
 
 	/** This is the view displayed by this viewer part. */
 	protected CTabFolder tabFolder;
 
 	/** Keeps references to the tabs contained within this folder. */
 	protected final List<IModelContentMergeViewerTab> tabs = new ArrayList<IModelContentMergeViewerTab>();
 
 	/** This is the content of the tree tab for this viewer part. */
 	protected IModelContentMergeViewerTab tree;
 
 	/** This contains all the listeners registered for this viewer part. */
 	private final List<ICompareEditorPartListener> editorPartListeners = new ArrayList<ICompareEditorPartListener>();
 
 	/**
 	 * Instantiates a {@link ModelContentMergeTabFolder} given its parent {@link Composite} and its side.
 	 * 
 	 * @param viewer
 	 *            Parent viewer of this viewer part.
 	 * @param composite
 	 *            Parent {@link Composite} for this part.
 	 * @param side
 	 *            Comparison side of this part. Must be one of {@link EMFCompareConstants#LEFT
 	 *            EMFCompareConstants.RIGHT}, {@link EMFCompareConstants#RIGHT EMFCompareConstants.LEFT} or
 	 *            {@link EMFCompareConstants#ANCESTOR EMFCompareConstants.ANCESTOR}.
 	 */
 	public ModelContentMergeTabFolder(ModelContentMergeViewer viewer, Composite composite, int side) {
 		if (side != EMFCompareConstants.RIGHT && side != EMFCompareConstants.LEFT
 				&& side != EMFCompareConstants.ANCESTOR) {
 			throw new IllegalArgumentException(EMFCompareUIMessages.getString("IllegalSide", side)); //$NON-NLS-1$
 		}
 
 		parentViewer = viewer;
 		partSide = side;
 		createContents(composite);
 	}
 
 	/**
 	 * Registers the given listener for notification. If the identical listener is already registered the
 	 * method has no effect.
 	 * 
 	 * @param listener
 	 *            The listener to register for changes of this input.
 	 */
 	public void addCompareEditorPartListener(ICompareEditorPartListener listener) {
 		editorPartListeners.add(listener);
 	}
 
 	/**
 	 * Disposes of all resources used by this folder.
 	 */
 	public void dispose() {
 		properties.dispose();
 		tree.dispose();
 		tabs.clear();
 		tabFolder.dispose();
 		editorPartListeners.clear();
 	}
 
 	/**
 	 * Returns a list of all diffs contained by the input DiffModel except for DiffGroups.
 	 * 
 	 * @return List of the DiffModel's differences.
 	 */
 	public List<DiffElement> getDiffAsList() {
 		if (parentViewer.getInput() != null)
 			return ((ModelCompareInput)parentViewer.getInput()).getDiffAsList();
 		return new ArrayList<DiffElement>();
 	}
 
 	/**
 	 * Returns the properties tab of this tab folder.
 	 * 
 	 * @return The properties tab of this tab folder.
 	 */
 	public IModelContentMergeViewerTab getPropertyPart() {
 		return properties;
 	}
 
 	/**
 	 * Returns the tree tab of this tab folder.
 	 * 
 	 * @return The tree tab of this tab folder.
 	 */
 	public IModelContentMergeViewerTab getTreePart() {
 		return tree;
 	}
 
 	/**
 	 * This will be used when drawing the center part's marquees.
 	 * 
 	 * @param element
 	 *            The DiffElement which we need UI variables for.
 	 * @return The item corresponding to the given DiffElement, wrapped along with UI information.
 	 */
 	public ModelContentMergeTabItem getUIItem(DiffElement element) {
 		final EObject data;
 		if (partSide == EMFCompareConstants.ANCESTOR && element instanceof ConflictingDiffElement) {
 			data = ((ConflictingDiffElement)element).getOriginElement();
 		} else if (partSide == EMFCompareConstants.LEFT) {
 			data = EMFCompareEObjectUtils.getLeftElement(element);
 		} else {
 			data = EMFCompareEObjectUtils.getRightElement(element);
 		}
 
 		final EObject featureData;
 		if (element instanceof AttributeChange) {
 			featureData = ((AttributeChange)element).getAttribute();
 		} else if (element instanceof ReferenceChange) {
 			featureData = ((ReferenceChange)element).getReference();
 		} else {
 			featureData = null;
 		}
 
 		ModelContentMergeTabItem result = null;
 		if (data != null) {
 			result = tabs.get(tabFolder.getSelectionIndex()).getUIItem(data);
 		}
 		if (result == null && featureData != null) {
 			result = tabs.get(tabFolder.getSelectionIndex()).getUIItem(featureData);
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the visible elements of the active tab.
 	 * 
 	 * @return The visible elements of the active tab.
 	 */
 	public List<ModelContentMergeTabItem> getVisibleElements() {
 		return tabs.get(tabFolder.getSelectionIndex()).getVisibleElements();
 	}
 
 	/**
 	 * Redraws this viewer part.
 	 */
 	public void layout() {
 		tabs.get(tabFolder.getSelectionIndex()).redraw();
 	}
 
 	/**
 	 * Shows the given item on the tree tab or its properties on the property tab.
 	 * 
 	 * @param diff
 	 *            Item to scroll to.
 	 */
 	public void navigateToDiff(DiffElement diff) {
 		final List<DiffElement> diffs = new ArrayList<DiffElement>();
 		diffs.add(diff);
 		navigateToDiff(diffs);
 	}
 
 	/**
 	 * Ensures the first item of the given list of {@link DiffElement}s is visible, and sets the selection of
 	 * the tree to all those items.
 	 * 
 	 * @param diffs
 	 *            Items to select.
 	 */
 	public void navigateToDiff(List<DiffElement> diffs) {
 		EObject target = null;
 		// finds the object which properties should be found and expands the tree if needed
 		if (partSide == EMFCompareConstants.LEFT) {
 			target = EMFCompareEObjectUtils.getLeftElement(diffs.get(0));
 		} else if (partSide == EMFCompareConstants.RIGHT) {
 			if (diffs.get(0) instanceof DiffGroup
 					&& EMFCompareEObjectUtils.getLeftElement(diffs.get(0)) != null) {
 				target = EMFCompareEObjectUtils.getRightElement(findMatchFromElement(EMFCompareEObjectUtils
 						.getLeftElement(diffs.get(0))));
 			} else if (!(diffs.get(0) instanceof DiffGroup)) {
 				target = EMFCompareEObjectUtils.getRightElement(diffs.get(0));
 			} else
 				// fall through.
 				return;
 		} else {
 			target = EMFCompareEObjectUtils.getAncestorElement(findMatchFromElement(EMFCompareEObjectUtils
 					.getLeftElement(diffs.get(0))));
 		}
 
 		tabs.get(tabFolder.getSelectionIndex()).showItems(diffs);
 		properties.setReflectiveInput(findMatchFromElement(target));
 
 		parentViewer.getConfiguration().setProperty(EMFCompareConstants.PROPERTY_CONTENT_SELECTION,
 				diffs.get(0));
 		parentViewer.updateCenter();
 	}
 
 	/**
 	 * Removes the given listener from this folder's listeners list. This will have no effect if the listener
 	 * is not registered against this folder.
 	 * 
 	 * @param listener
 	 *            The listener to remove from this folder.
 	 */
 	public void removeCompareEditorPartListener(ICompareEditorPartListener listener) {
 		editorPartListeners.remove(listener);
 	}
 
 	/**
 	 * Sets the receiver's size and location to the rectangular area specified by the arguments.
 	 * 
 	 * @param x
 	 *            Desired x coordinate of the part.
 	 * @param y
 	 *            Desired y coordinate of the part.
 	 * @param width
 	 *            Desired width of the part.
 	 * @param height
 	 *            Desired height of the part.
 	 */
 	public void setBounds(int x, int y, int width, int height) {
 		setBounds(new Rectangle(x, y, width, height));
 	}
 
 	/**
 	 * Sets the receiver's size and location to given rectangular area.
 	 * 
 	 * @param bounds
 	 *            Desired bounds for this receiver.
 	 */
 	public void setBounds(Rectangle bounds) {
 		tabFolder.setBounds(bounds);
 		resizeBounds();
 	}
 
 	/**
 	 * Sets the input of this viewer part.
 	 * 
 	 * @param input
 	 *            New input of this viewer part.
 	 */
 	public void setInput(Object input) {
 		final IModelContentMergeViewerTab currentTab = tabs.get(tabFolder.getSelectionIndex());
		if (currentTab == properties) {
 			currentTab.setReflectiveInput(findMatchFromElement((EObject)input));
 		} else {
 			tabs.get(tabFolder.getSelectionIndex()).setReflectiveInput(input);
 		}
 	}
 
 	/**
 	 * Changes the current tab.
 	 * 
 	 * @param index
 	 *            New tab to set selected.
 	 */
 	public void setSelectedTab(int index) {
 		tabFolder.setSelection(index);
 		resizeBounds();
 	}
 
 	/**
 	 * Creates the contents of this viewer part given its parent composite.
 	 * 
 	 * @param composite
 	 *            Parent composite of this viewer parts's widgets.
 	 */
 	protected void createContents(Composite composite) {
 		tabFolder = new CTabFolder(composite, SWT.BOTTOM);
 		final CTabItem treeTab = new CTabItem(tabFolder, SWT.NONE);
 		treeTab.setText(EMFCompareUIMessages.getString("ModelContentMergeViewerTabFolder.tab1.name")); //$NON-NLS-1$
 
 		final CTabItem propertiesTab = new CTabItem(tabFolder, SWT.NONE);
 		propertiesTab.setText(EMFCompareUIMessages.getString("ModelContentMergeViewerTabFolder.tab2.name")); //$NON-NLS-1$
 
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
 		properties = createPropertiesPart(propertyPanel);
 		propertiesTab.setControl(propertyPanel);
 
 		tabs.add(tree);
 		tabs.add(properties);
 
 		tabFolder.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				setSelectedTab(tabFolder.getSelectionIndex());
 				fireSelectedtabChanged();
 			}
 		});
 		tabFolder.setSelection(treeTab);
 	}
 
 	/**
 	 * Returns the {@link Match2Elements} containing the given {@link EObject} as its left or right element.
 	 * 
 	 * @param element
 	 *            Element we seek the {@link Match2Elements} for.
 	 * @return The {@link Match2Elements} containing the given {@link EObject} as its left or right element.
 	 */
 	protected EObject findMatchFromElement(EObject element) {
 		EObject theElement = null;
 		final EObject match = (EObject)((ModelCompareInput)parentViewer.getInput()).getMatch();
 
 		final TreeIterator<EObject> iterator = match.eAllContents();
 		while (iterator.hasNext()) {
 			final Object object = iterator.next();
 
 			if (object instanceof Match3Elements) {
 				final Match3Elements matchElement = (Match3Elements)object;
 				if (matchElement.getLeftElement().equals(element)
 						|| matchElement.getRightElement().equals(element)
 						|| matchElement.getOriginElement().equals(element)) {
 					theElement = matchElement;
 				}
 			} else if (object instanceof Match2Elements) {
 				final Match2Elements matchElement = (Match2Elements)object;
 				if (matchElement.getLeftElement().equals(element)
 						|| matchElement.getRightElement().equals(element)) {
 					theElement = matchElement;
 				}
 			} else if (object instanceof UnmatchElement) {
 				final UnmatchElement unmatchElement = (UnmatchElement)object;
 				if (unmatchElement.getElement().equals(element)) {
 					theElement = unmatchElement;
 				}
 			}
 		}
 
 		return theElement;
 	}
 
 	/**
 	 * Notifies All {@link ICompareEditorPartListener listeners} registered for this viewer part that the tab
 	 * selection has been changed.
 	 */
 	protected void fireSelectedtabChanged() {
 		for (final ICompareEditorPartListener listener : editorPartListeners) {
 			listener.selectedTabChanged(tabFolder.getSelectionIndex());
 		}
 	}
 
 	/**
 	 * Notifies All {@link ICompareEditorPartListener listeners} registered for this viewer part that the user
 	 * selection has changed on the properties or tree tab.
 	 * 
 	 * @param event
 	 *            Source {@link SelectionChangedEvent Selection changed event} of the notification.
 	 */
 	protected void fireSelectionChanged(SelectionChangedEvent event) {
 		for (final ICompareEditorPartListener listener : editorPartListeners) {
 			listener.selectionChanged(event);
 		}
 	}
 
 	/**
 	 * Notifies All {@link ICompareEditorPartListener listeners} registered for this viewer part that the
 	 * center part needs to be refreshed.
 	 */
 	protected void fireUpdateCenter() {
 		for (final ICompareEditorPartListener listener : editorPartListeners) {
 			listener.updateCenter();
 		}
 	}
 
 	/**
 	 * This will resize the tabs displayed by this content merge viewer.
 	 */
 	protected void resizeBounds() {
 		tabs.get(tabFolder.getSelectionIndex()).getControl().setBounds(tabFolder.getClientArea());
 	}
 
 	/**
 	 * Handles the creation of the properties tab of this viewer part given the parent {@link Composite} under
 	 * which to create it.
 	 * 
 	 * @param composite
 	 *            Parent {@link Composite} of the table to create.
 	 * @return The properties part displayed by this viewer part's properties tab.
 	 */
 	private IModelContentMergeViewerTab createPropertiesPart(Composite composite) {
 		final IModelContentMergeViewerTab propertiesPart = new ModelContentMergePropertyTab(composite,
 				partSide, this);
 
 		((Scrollable)propertiesPart.getControl()).getVerticalBar().addSelectionListener(
 				new SelectionListener() {
 					public void widgetDefaultSelected(SelectionEvent e) {
 						widgetSelected(e);
 					}
 
 					public void widgetSelected(SelectionEvent e) {
 						parentViewer.updateCenter();
 					}
 				});
 
 		propertiesPart.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				fireSelectionChanged(event);
 			}
 		});
 
 		return propertiesPart;
 	}
 
 	/**
 	 * Handles the creation of the tree tab of this viewer part given the parent {@link Composite} under which
 	 * to create it.
 	 * 
 	 * @param composite
 	 *            Parent {@link Composite} of the tree to create.
 	 * @return The tree part displayed by this viewer part's tree tab.
 	 */
 	private IModelContentMergeViewerTab createTreePart(Composite composite) {
 		final IModelContentMergeViewerTab treePart = new ModelContentMergeDiffTab(composite, partSide, this);
 
 		((Scrollable)treePart.getControl()).getVerticalBar().addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				fireUpdateCenter();
 			}
 		});
 
 		((Tree)treePart.getControl()).addTreeListener(new TreeListener() {
 			public void treeCollapsed(TreeEvent e) {
 				((TreeItem)e.item).setExpanded(false);
 				e.doit = false;
 				parentViewer.update();
 			}
 
 			public void treeExpanded(TreeEvent e) {
 				((TreeItem)e.item).setExpanded(true);
 				e.doit = false;
 				parentViewer.update();
 			}
 		});
 
 		treePart.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				fireSelectionChanged(event);
 			}
 		});
 
 		((Tree)treePart.getControl()).addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (tree.getSelectedElements().size() > 0) {
 					final Item selected = tree.getSelectedElements().get(0);
 					for (final DiffElement diff : ((ModelCompareInput)parentViewer.getInput())
 							.getDiffAsList()) {
 						if (!(diff instanceof DiffGroup) && partSide == EMFCompareConstants.LEFT) {
 							if (selected.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff))) {
 								parentViewer.setSelection(diff);
 							}
 						} else if (!(diff instanceof DiffGroup) && partSide == EMFCompareConstants.RIGHT) {
 							if (selected.getData().equals(EMFCompareEObjectUtils.getRightElement(diff))) {
 								parentViewer.setSelection(diff);
 							}
 						}
 					}
 					if (!selected.isDisposed() && selected.getData() instanceof EObject) {
 						properties.setReflectiveInput(findMatchFromElement((EObject)selected.getData()));
 					}
 				}
 			}
 		});
 
 		return treePart;
 	}
 }

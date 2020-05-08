 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.internal.utils.DiffUtil;
 import org.eclipse.emf.compare.rcp.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilter;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.tree.TreeNode;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * A specific canvas that must be presented next to a TreeViewer. It shows consequences of a Diff (required
  * and unmergeable differences), as in the TreeViewer, but in a compact format (small colored rectangles) and
  * with links to respectives Treeitems.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  */
 public class EMFCompareDiffTreeRuler extends Canvas {
 
 	/** The vertical offset for an annotation. */
 	private static final int Y_OFFSET = 6;
 
 	/** The height of an annotation. */
 	private static final int ANNOTATION_HEIGHT = 5;
 
 	/** The TreeViewer associated with this Treeruler. */
 	private final TreeViewer fTreeViewer;
 
 	/** The color a required diff. */
 	private final Color requiredDiffFillColor;
 
 	/** The color of an unmergeable diff. **/
 	private final Color unmergeableDiffFillColor;
 
 	/** The border color a required diff. */
 	private final Color requiredDiffBorderColor;
 
 	/** The border color an unmergeable diff. */
 	private final Color unmergeableDiffBorderColor;
 
 	/** The width of this tree ruler. */
 	private final int fWidth;
 
 	/** The list of required Diff that need to be shown in the TreeRuler. */
 	private Set<Diff> requires;
 
 	/** The list of unmergeables Diff that need to be shown in the TreeRuler. */
 	private Set<Diff> unmergeables;
 
 	/** A map that links a diff with tree items. */
 	private Multimap<Diff, TreeItem> diffItems;
 
 	/** A map that links a rectangle with a tree item. */
 	private Map<Rectangle, TreeItem> annotationsData;
 
 	/** The paint listener. */
 	private PaintListener paintListener;
 
 	/** The mouse click listener. */
 	private MouseListener mouseClickListener;
 
 	/** The mouse move listener. */
 	private MouseMoveListener mouseMoveListener;
 
 	/** The mouse track listener. */
 	private MouseTrackListener mouseTrackListener;
 
 	/** The last cursor used. */
 	private Cursor lastCursor;
 
 	/** The configuration for this control. */
 	private CompareConfiguration fConfiguration;
 
 	/** The selected diff in the Treeviewer associated with this Treeruler. */
 	private Diff selectedDiff;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent
 	 *            the control's parent.
 	 * @param style
 	 *            the style of the control to construct.
 	 * @param width
 	 *            the control's width.
 	 * @param treeViewer
 	 *            the TreeViewer associated with this control.
 	 * @param config
 	 *            the configuration for this control.
 	 */
 	EMFCompareDiffTreeRuler(Composite parent, int style, int width, TreeViewer treeViewer,
 			CompareConfiguration config) {
 		super(parent, style);
 		fWidth = width;
 		fTreeViewer = treeViewer;
 		fConfiguration = config;
 
 		requiredDiffFillColor = JFaceResources.getColorRegistry().get(
 				EMFCompareDiffTreeViewer.REQUIRED_DIFF_COLOR);
 		requiredDiffBorderColor = JFaceResources.getColorRegistry().get(
 				EMFCompareDiffTreeViewer.REQUIRED_DIFF_BORDER_COLOR);
 		unmergeableDiffFillColor = JFaceResources.getColorRegistry().get(
 				EMFCompareDiffTreeViewer.UNMERGEABLE_DIFF_COLOR);
 		unmergeableDiffBorderColor = JFaceResources.getColorRegistry().get(
 				EMFCompareDiffTreeViewer.UNMERGEABLE_DIFF_BORDER_COLOR);
 
 		requires = Sets.newHashSet();
 		unmergeables = Sets.newHashSet();
 		diffItems = HashMultimap.create();
 		annotationsData = Maps.newHashMap();
 
 		paintListener = new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				handlePaintEvent(e);
 			}
 		};
 		addPaintListener(paintListener);
 
 		mouseClickListener = new MouseListener() {
 
 			public void mouseUp(MouseEvent e) {
 				handleMouseClickEvent(e);
 			}
 
 			public void mouseDown(MouseEvent e) {
 				// Do nothing.
 			}
 
 			public void mouseDoubleClick(MouseEvent e) {
 				// Do nothing.
 			}
 		};
 		addMouseListener(mouseClickListener);
 
 		mouseMoveListener = new MouseMoveListener() {
 
 			public void mouseMove(MouseEvent e) {
 				handleMouveMoveEvent(e);
 			}
 		};
 		addMouseMoveListener(mouseMoveListener);
 
 		mouseTrackListener = new MouseTrackListener() {
 
 			public void mouseHover(MouseEvent e) {
 				handleMouseHoverEvent(e);
 			}
 
 			public void mouseExit(MouseEvent e) {
 				// Do nothing.
 			}
 
 			public void mouseEnter(MouseEvent e) {
 				// Do nothing.
 			}
 		};
 		addMouseTrackListener(mouseTrackListener);
 	}
 
 	/**
 	 * Compute consequences (required and unmergeable differences) when selection changed occurs.
 	 * 
 	 * @param event
 	 *            the SelectionChangedEvent event.
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		clearAllData();
 		ISelection selection = event.getSelection();
 		if (selection instanceof IStructuredSelection) {
 			Object element = ((IStructuredSelection)selection).getFirstElement();
 			if (element instanceof Adapter) {
 				Object target = ((Adapter)element).getTarget();
 				if (target instanceof TreeNode) {
 					EObject data = ((TreeNode)target).getData();
 					if (data instanceof Diff) {
 						selectedDiff = (Diff)data;
 						computeConsequences();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Compute consequences (required and unmergeable differences).
 	 */
 	public void computeConsequences() {
 		clearAllData();
 		if (selectedDiff != null) {
 			Boolean leftToRight = (Boolean)fConfiguration.getProperty(EMFCompareConstants.MERGE_WAY);
 			boolean ltr = false;
 			if (leftToRight == null || leftToRight.booleanValue()) {
 				ltr = true;
 			}
 			boolean leftEditable = fConfiguration.isLeftEditable();
 			boolean rightEditable = fConfiguration.isRightEditable();
 			boolean bothSidesEditable = leftEditable && rightEditable;
 			if ((ltr && (leftEditable || bothSidesEditable)) || (!ltr && (rightEditable && !leftEditable))) {
 				requires = DiffUtil.getRequires(selectedDiff, true, selectedDiff.getSource());
 				unmergeables = DiffUtil.getUnmergeables(selectedDiff, true);
 			} else {
 				requires = DiffUtil.getRequires(selectedDiff, false, selectedDiff.getSource());
 				unmergeables = DiffUtil.getUnmergeables(selectedDiff, false);
 			}
 			associateTreeItems(Lists.newLinkedList(Iterables.concat(requires, unmergeables)));
 		}
 	}
 
 	/**
 	 * Maps tree items with the given list of diffs.
 	 * 
 	 * @param diffs
 	 *            the given list of diffs.
 	 */
 	private void associateTreeItems(List<Diff> diffs) {
 		Tree tree = fTreeViewer.getTree();
 		for (TreeItem item : tree.getItems()) {
 			associateTreeItem(item, diffs);
 		}
 	}
 
 	/**
 	 * Maps, if necessary, the given tree item and all his children with the given list of diffs.
 	 * 
 	 * @param item
 	 *            the given tree item.
 	 * @param diffs
 	 *            the given list of diffs.
 	 */
 	private void associateTreeItem(TreeItem item, List<Diff> diffs) {
 		Object data = item.getData();
 		if (data instanceof Adapter) {
 			Notifier target = ((Adapter)data).getTarget();
 			if (target instanceof TreeNode) {
 				EObject treeNodeData = ((TreeNode)target).getData();
 				if (diffs.contains(treeNodeData)) {
 					diffItems.put((Diff)treeNodeData, item);
 				}
 			}
 		}
 		for (TreeItem child : item.getItems()) {
 			associateTreeItem(child, diffs);
 		}
 	}
 
 	/**
 	 * Clear all data.
 	 */
 	private void clearAllData() {
 		requires.clear();
 		unmergeables.clear();
 		diffItems.clear();
 		annotationsData.clear();
 	}
 
 	/**
 	 * Handles the dispose event on this control.
 	 */
 	public void handleDispose() {
 		removeMouseTrackListener(mouseTrackListener);
 		removeMouseMoveListener(mouseMoveListener);
 		removeMouseListener(mouseClickListener);
 		removePaintListener(paintListener);
 	}
 
 	/**
 	 * Handles the paint event.
 	 * 
 	 * @param e
 	 *            the paint event.
 	 */
 	private void handlePaintEvent(PaintEvent e) {
 		annotationsData.clear();
 		Collection<IDifferenceFilter> filters = (Collection<IDifferenceFilter>)fConfiguration
 				.getProperty(EMFCompareConstants.SELECTED_FILTERS);
 		Collection<? extends Diff> filteredRequires = filteredDiffs(requires, filters);
 		Collection<? extends Diff> filteredUnmergeables = filteredDiffs(unmergeables, filters);
 		for (Diff diff : filteredRequires) {
 			for (TreeItem item : diffItems.get(diff)) {
 				createAnnotation(e, diff, item, requiredDiffFillColor, requiredDiffBorderColor);
 			}
 		}
 		for (Diff diff : filteredUnmergeables) {
 			for (TreeItem item : diffItems.get(diff)) {
 				createAnnotation(e, diff, item, unmergeableDiffFillColor, unmergeableDiffBorderColor);
 			}
 		}
 	}
 
 	/**
 	 * Handles the mouse click event.
 	 * 
 	 * @param e
 	 *            the mouse click event.
 	 */
 	private void handleMouseClickEvent(MouseEvent e) {
 		for (Rectangle rect : annotationsData.keySet()) {
 			if (e.y >= rect.y && e.y <= rect.y + ANNOTATION_HEIGHT) {
 				TreeItem item = annotationsData.get(rect);
 				TreePath treePath = getTreePathFromItem(item);
 				fTreeViewer.expandToLevel(treePath, 0);
 				fTreeViewer.reveal(treePath);
 				if (isVerticalScrollBarEnabled()) {
 					TreeItem previousItem = getPreviousItem(item, 2);
 					fTreeViewer.getTree().setTopItem(previousItem);
 				}
 				redraw();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Handles the mouse move event.
 	 * 
 	 * @param e
 	 *            the mouse move event.
 	 */
 	private void handleMouveMoveEvent(MouseEvent e) {
 		Cursor cursor = null;
 		for (Rectangle rect : annotationsData.keySet()) {
 			if (e.y >= rect.y && e.y <= rect.y + ANNOTATION_HEIGHT) {
 				cursor = e.display.getSystemCursor(SWT.CURSOR_HAND);
 				break;
 			}
 		}
 		if (cursor != lastCursor) {
 			setCursor(cursor);
 			lastCursor = cursor;
 		}
 	}
 
 	/**
 	 * Handles the mouse hover event.
 	 * 
 	 * @param e
 	 *            the mouve hover event.
 	 */
 	private void handleMouseHoverEvent(MouseEvent e) {
 		String overview = ""; //$NON-NLS-1$
 		for (Rectangle rect : annotationsData.keySet()) {
 			if (e.y >= rect.y && e.y <= rect.y + ANNOTATION_HEIGHT) {
 				TreeItem item = annotationsData.get(rect);
 				overview = item.getText();
 				break;
 			}
 		}
 		setToolTipText(overview);
 	}
 
 	/**
 	 * Create an annotation in the tree ruler.
 	 * 
 	 * @param e
 	 *            the PaintEvent.
 	 * @param diff
 	 *            the Diff for which we want to create the annotation.
 	 * @param treeItem
 	 *            the tree item associated with the diff.
 	 * @param fill
 	 *            the annotation's fill color.
 	 * @param border
 	 *            the annotation's border color.
 	 */
 	private void createAnnotation(PaintEvent e, Diff diff, TreeItem treeItem, Color fill, Color border) {
 		TreeItem item = getDeepestVisibleTreeItem(treeItem, treeItem);
 		if (item != null) {
 			int y = item.getBounds().y;
 			int yRuler = getSize().y;
 			if (isVerticalScrollBarEnabled()) {
 				int yMin = Math.abs(item.getParent().getItems()[0].getBounds().y);
 				int yMax = getLastVisibleItem().getBounds().y;
 				int realYMax = yMax + yMin;
 				y = (y + yMin) * yRuler / realYMax;
 				if (y + Y_OFFSET + ANNOTATION_HEIGHT > yRuler) {
 					y = yRuler - Y_OFFSET - ANNOTATION_HEIGHT;
 				}
 			}
 			Rectangle rect = drawAnnotation(e.gc, 2, y + Y_OFFSET, fWidth - 5, ANNOTATION_HEIGHT, fill,
 					border);
 			annotationsData.put(rect, treeItem);
 		}
 	}
 
 	/**
 	 * Returns the full tree path of the given tree item.
 	 * 
 	 * @param item
 	 *            the given tree item.
 	 * @return the full tree path of the given tree item.
 	 */
 	private TreePath getTreePathFromItem(TreeItem item) {
 		LinkedList<Object> segments = Lists.newLinkedList();
 		TreeItem parent = item;
 		while (parent != null) {
 			Object segment = parent.getData();
 			Assert.isNotNull(segment);
 			segments.addFirst(segment);
 			parent = parent.getParentItem();
 		}
 		return new TreePath(segments.toArray());
 	}
 
 	/**
 	 * Checks if the vertical scroll bar of the tree viewer associated with this tree ruler is activated and
 	 * enabled.
 	 * 
 	 * @return true if the vertical scroll bar is activated and enabled, false otherwise.
 	 */
 	private boolean isVerticalScrollBarEnabled() {
 		ScrollBar verticalBar = fTreeViewer.getTree().getVerticalBar();
 		if (verticalBar != null) {
 			return verticalBar.isVisible() && verticalBar.isEnabled();
 		}
 		return false;
 	}
 
 	/**
 	 * Draw an annotation (a Rectangle) on this tree ruler.
 	 * 
 	 * @param gc
 	 *            the swt GC.
 	 * @param x
 	 *            the x coordinate of the origin of the annotation.
 	 * @param y
 	 *            the y coordinate of the origin of the annotation.
 	 * @param w
 	 *            the width of the annotation.
 	 * @param h
 	 *            the height of the annotation.
 	 * @param fill
 	 *            the annotation's fill color.
 	 * @param border
 	 *            the annotation's border color.
 	 * @return the annotation (a Rectangle).
 	 */
 	private Rectangle drawAnnotation(GC gc, int x, int y, int w, int h, Color fill, Color border) {
 		Rectangle rect = new Rectangle(x, y, w, h);
 		gc.setBackground(fill);
 		gc.fillRectangle(rect);
 
 		gc.setForeground(border);
 		gc.drawRectangle(x, y, w, h);
 		return rect;
 	}
 
 	/**
 	 * Returns, for the given tree item, the deepest visible {@link TreeItem} in the Treeviewer associated
 	 * with this TreeRuler.
 	 * 
 	 * @param currentItem
 	 *            the given tree item.
 	 * @param deepestVisibleItem
 	 *            the deepest visible tree item (a parent or the item itself) of the given item.
 	 * @return the deepest visible tree item (a parent or the item itself).
 	 */
 	private TreeItem getDeepestVisibleTreeItem(final TreeItem currentItem, final TreeItem deepestVisibleItem) {
 		TreeItem item = null;
 		if (!currentItem.isDisposed()) {
 			TreeItem parent = currentItem.getParentItem();
 			if (parent == null) {
 				item = deepestVisibleItem;
 			} else if (parent.getExpanded()) {
 				item = getDeepestVisibleTreeItem(parent, deepestVisibleItem);
 			} else {
 				item = getDeepestVisibleTreeItem(parent, parent);
 			}
 		}
 		return item;
 	}
 
 	/**
 	 * Get the previous item of the given {@link TreeItem}.
 	 * 
 	 * @param treeItem
 	 *            the given {@link TreeItem}.
 	 * @param index
 	 *            the index of the previous item.
 	 * @return the previous item of the given {@link TreeItem}.
 	 */
 	private TreeItem getPreviousItem(TreeItem treeItem, int index) {
 		TreeItem previousItem = treeItem;
 		if (index > 0) {
 			TreeItem parentItem = treeItem.getParentItem();
 			if (parentItem != null) {
 				int treeItemIndex = 0;
 				for (TreeItem siblingItem : parentItem.getItems()) {
 					if (siblingItem.equals(treeItem)) {
 						break;
 					}
 					treeItemIndex++;
 				}
 				if (treeItemIndex == 0) {
 					previousItem = getPreviousItem(parentItem, index - 1);
 				} else if (treeItemIndex == 1) {
 					TreeItem firstChild = parentItem.getItem(0);
 					previousItem = getLastVisibleItem(firstChild);
 					previousItem = getPreviousItem(previousItem, index - 1);
 				} else {
 					previousItem = getPreviousItem(getLastVisibleItem(parentItem.getItem(treeItemIndex - 1)),
 							index - 1);
 				}
 			} else {
 				// It is a root item. May be there are some previous root items.
 				Tree tree = treeItem.getParent();
 				int treeItemIndex = 0;
 				for (TreeItem siblingItem : tree.getItems()) {
 					if (siblingItem.equals(treeItem)) {
 						break;
 					}
 					treeItemIndex++;
 				}
 				if (treeItemIndex == 0) {
 					previousItem = treeItem;
 				} else if (treeItemIndex == 1) {
 					TreeItem firstRoot = tree.getItem(0);
 					if (firstRoot.getExpanded()) {
 						previousItem = getLastVisibleItem(firstRoot);
 					} else {
 						previousItem = firstRoot;
 					}
 					previousItem = getPreviousItem(previousItem, index - 1);
 				} else {
 					previousItem = tree.getItem(treeItemIndex - index);
 				}
 			}
 		}
 		return previousItem;
 	}
 
 	/**
 	 * Returns the last visible {@link TreeItem} in the Treeviewer associated with this TreeRuler.
 	 * 
 	 * @return the last visible TreeItem in the Treeviewer associated with this TreeRuler.
 	 */
 	private TreeItem getLastVisibleItem() {
 		int rootChildren = fTreeViewer.getTree().getItemCount();
 		return getLastVisibleItem(fTreeViewer.getTree().getItem(rootChildren - 1));
 	}
 
 	/**
 	 * Returns the last visible child of the given {@link TreeItem} in the Treeviewer associated with this
 	 * TreeRuler.
 	 * 
 	 * @param item
 	 *            teh given TreeItem.
 	 * @return the last visible child of the given TreeItem in the Treeviewer associated with this TreeRuler.
 	 */
 	private TreeItem getLastVisibleItem(TreeItem item) {
 		TreeItem lastVisibleItem = null;
 		int directChildren = item.getItemCount();
		if (directChildren == 0) {
 			lastVisibleItem = item;
 		} else {
 			TreeItem lastDirectChildren = item.getItem(directChildren - 1);
 			if (lastDirectChildren.getData() == null) {
 				lastVisibleItem = item;
 			} else if (lastDirectChildren.getExpanded()) {
 				lastVisibleItem = getLastVisibleItem(lastDirectChildren);
 			} else {
 				lastVisibleItem = lastDirectChildren;
 			}
 		}
 		return lastVisibleItem;
 	}
 
 	/**
 	 * From a list of {@link Diff}s, returns the diffs which are not filtered by a filter of the given list of
 	 * {@link IDifferenceFilter}.
 	 * 
 	 * @param unfilteredDiffs
 	 *            the given list of unfiltered diffs.
 	 * @param filters
 	 *            the given list of IDifferenceFilter.
 	 * @return A filtered list of diffs.
 	 */
 	protected Collection<? extends Diff> filteredDiffs(Collection<? extends Diff> unfilteredDiffs,
 			Collection<IDifferenceFilter> filters) {
 		if (filters != null) {
 			List<Diff> filteredDiffs = Lists.newArrayList(unfilteredDiffs);
 			for (IDifferenceFilter filter : filters) {
 				for (Diff unfilteredDiff : unfilteredDiffs) {
 					if (filter.getPredicateWhenSelected().apply(unfilteredDiff)) {
 						filteredDiffs.remove(unfilteredDiff);
 					}
 				}
 			}
 			return filteredDiffs;
 		}
 		return unfilteredDiffs;
 
 	}
 }

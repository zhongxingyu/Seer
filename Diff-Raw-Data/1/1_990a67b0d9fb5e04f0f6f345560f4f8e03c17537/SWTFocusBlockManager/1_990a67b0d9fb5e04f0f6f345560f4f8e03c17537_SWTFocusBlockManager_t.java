 /*******************************************************************************
  * Copyright (c) 6 dec. 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.editing.tables;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerRow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.accessibility.ACC;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 
 /**
  * This class is responsible to provide cell management base features for the
  * SWT-Controls {@link org.eclipse.swt.widgets.Table} and
  * {@link org.eclipse.swt.widgets.Tree}. </p>An adaption of
  * {@link SWTFocusCellManager}.</p> It supports drag-copying of cell content
  * from top to bottom. (Other dragging directions are not supported.</p>. </p>
  * {@link #setFocusBlockActionHandler(IFocusBlockActionHandler)} to create and
  * 
  * While dragging an EMF Command command is created and updated with the
  * elements for the {@link ViewerCell}
  * 
  * @since 3.3
  * @author Christophe Bouhier
  */
 abstract class SWTFocusBlockManager {
 
 	private CellNavigationStrategyExposed navigationStrategy;
 
 	private ColumnViewer viewer;
 
 	// Consider putting this in the block.
 	private ViewerCell focusCell;
 
 	// The cells which are below the selection.
 	private List<ViewerCell> focusBlock = new ArrayList<ViewerCell>();
 
 	// Our focus block highlighter.
 	private FocusBlockHighlighter cellHighlighter;
 
 	// Our Action Handler
 	private IFocusBlockActionHandler focusBlockActionHandler;
 
 	private DisposeListener itemDeletionListener = new DisposeListener() {
 		public void widgetDisposed(DisposeEvent e) {
 			setFocusCell(null);
 		}
 	};
 
 	/*
 	 * dragging state.
 	 */
 	private boolean cellDragging;
 
 	/**
 	 * @param viewer
 	 * @param focusDrawingDelegate
 	 * @param navigationDelegate
 	 */
 	public SWTFocusBlockManager(ColumnViewer viewer,
 			FocusBlockHighlighter focusDrawingDelegate,
 			CellNavigationStrategyExposed navigationDelegate) {
 		this.viewer = viewer;
 		this.cellHighlighter = focusDrawingDelegate;
 		if (this.cellHighlighter != null) {
 			this.cellHighlighter.setMgr(this);
 		}
 
 		this.navigationStrategy = navigationDelegate;
 		hookListener(viewer);
 	}
 
 	/**
 	 * This method is called by the framework to initialize this cell manager.
 	 */
 	void init() {
 		this.cellHighlighter.init();
 		this.navigationStrategy.init();
 	}
 
 	/**
 	 * Handle mouse down event. The {@link #getFocusCell() focus cell} because
 	 * the {@link ViewerCell cell} at the {@link Event} coordinates. </p>The
 	 * state is set to 'dragging'.
 	 * 
 	 * @param event
 	 */
 	private void handleMouseDown(Event event) {
 
 		ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
 
 		if (cell != null) {
 			if (!cell.equals(focusCell)) {
 				setFocusCell(cell, event);
 			}
 		}
 		cleanFocusBlock(focusCell, event);
 		cellDragging = true;
 		
 		setCursorAccelerator(isAccelarating(event));
 		// System.out
 		// .println(" moving down, activate cell dragging, block size = "
 		// + focusBlock.size());
 	}
 
 	private boolean isAccelarating(Event event) {
 		return (event.stateMask & focusBlockActionHandler.getDragAccelerator()) != 0;
 	}
 
 	private void setCursorAccelerator(boolean accelerating) {
 		
		@SuppressWarnings("unused")
 		Cursor previousCursor = viewer.getControl().getCursor();
 		
 		if (accelerating) {
 			viewer.getControl().setCursor(
 					Display.getDefault().getSystemCursor(SWT.CURSOR_UPARROW));
 		} else {
 			viewer.getControl().setCursor(
 					Display.getDefault().getSystemCursor(SWT.CURSOR_ARROW));
 		}
 	}
 
 	/**
 	 * Handle mouse dragging event if the state is dragging.
 	 * 
 	 * @param event
 	 */
 	private void handleMouseMove(Event event) {
 
 		if (cellDragging) {
 			// System.out.println(" dragging mouse" + event.x + "," + event.y);
 			handleCellDragging(event);
 		} else {
 			// we are not dragging, do nothing.
 			// System.out.println(" moving mouse" + event.x + "," + event.y);
 		}
 	}
 
 	/**
 	 * Handle mouse dragging.
 	 * 
 	 * @param event
 	 */
 	private void handleCellDragging(Event event) {
 
 		// in the new position, look if we are the focus cell,
 		// if not, add to our cell blow only if we are the below neighbour...
 		ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
 
 		if (cell != null) {
 
 			if (!lastInFocusBlock(cell)) {
 				// if we are not the last cell, but we are in the block, we
 				// should get the position
 				// to remove other cells..
 				if (inFocusBlock(cell)) {
 					cleanFocusBlock(cell, event);
 					// System.out.println("Dragging....removing cell in column:"
 					// + cell.getVisualIndex() + " size of block = "
 					// + focusBlock.size());
 				} else {
 
 					// we can add it now.
 					// do this for the last cell, not last focus cell.
 					ViewerCell belowNeighborCell = lastCellInBlock()
 							.getNeighbor(ViewerCell.BELOW, false);
 					if (cell.equals(belowNeighborCell)) {
 						addCellToBlock(cell, event);
 						// System.out.println("Dragging....Adding cell in column:"
 						// + cell.getVisualIndex() + " size of block = "
 						// + focusBlock.size());
 					}
 				}
 			} else {
 				// last cell in the block do nothing...
 			}
 		}
 	}
 
 	/**
 	 * Remove the {@link ViewerCell cell} from the selection of cells in the
 	 * block when dragging upwards.
 	 * 
 	 * @param cell
 	 * @param event
 	 */
 	private void cleanFocusBlock(ViewerCell cell, Event event) {
 
 		if (focusBlock.isEmpty()) {
 			// do nothing, we always have the focus cell.
 		} else {
 			// remember the old block for the highlighter to repaint.
 			ViewerCell[] oldBlock = block();
 			// When we are the focus cell, we should clean the block.
 			if (focusCell.equals(cell)) {
 				// System.out.println("Dragging....Clearing cells:"
 				// + cell.getVisualIndex());
 				focusBlock.clear();
 				cellHighlighter.focusBlockChanged(block(), oldBlock);
 				focusBlockActionHandler.updateCommand(focusCell, getTargets(),
 						event);
 
 			} else if (focusBlock.contains(cell)) {
 				int indexOf = focusBlock.indexOf(cell);
 				Collection<ViewerCell> retain = new ArrayList<ViewerCell>();
 				// Note pre-increment, to get the first item from the list index
 				// 0.
 				for (int i = -1; i < indexOf; i++) {
 					retain.add(focusBlock.get(i + 1));
 				}
 				focusBlock.retainAll(retain);
 				// System.out.println("Dragging....Clearing cells:"
 				// + cell.getVisualIndex());
 				cellHighlighter.focusBlockChanged(block(), oldBlock);
 				focusBlockActionHandler.updateCommand(focusCell, getTargets(),
 						event);
 			}
 		}
 	}
 
 	/**
 	 * get all the targets for the focus block.
 	 */
 	private Collection<EObject> getTargets() {
 
 		// Do a transformation.
 		Collection<EObject> targets = new ArrayList<EObject>();
 		for (ViewerCell cell : focusBlock) {
 			Object element = cell.getElement();
 			if (element instanceof EObject) {
 				targets.add((EObject) element);
 			}
 		}
 		return targets;
 	}
 
 	private ViewerCell[] block() {
 		ViewerCell[] block = null;
 		block = new ViewerCell[focusBlock.size() + 1];
 		focusBlock.toArray(block);
 		block[focusBlock.size()] = focusCell;
 
 		return block;
 	}
 
 	/**
 	 * Are we the last cell in the focus block.
 	 * 
 	 * @param cell
 	 * @return
 	 */
 	private boolean lastInFocusBlock(ViewerCell cell) {
 		// return the focus cell equality or the the last cell in the focus
 		// block equality.
 		return lastCellInBlock().equals(cell);
 	}
 
 	private boolean inFocusBlock(ViewerCell cell) {
 		return focusCell.equals(cell) || focusBlock.contains(cell);
 	}
 
 	private ViewerCell lastCellInBlock() {
 		if (focusBlock.isEmpty()) {
 			return focusCell;
 		} else {
 			return focusBlock.get(focusBlock.size() == 1 ? 0 : focusBlock
 					.size() - 1);
 		}
 	}
 
 	private void addCellToBlock(ViewerCell cell, Event event) {
 
 		if (!focusBlock.contains(cell)) {
 
 			ViewerCell[] oldBlock = block();
 			focusBlock.add(cell);
 			ViewerCell[] newBlock = block();
 			cellHighlighter.focusBlockChanged(newBlock, oldBlock);
 			focusBlockActionHandler.updateCommand(focusCell, getTargets(),
 					event);
 		}
 	}
 
 	private void handleMouseUp(Event event) {
 
 		// System.out.println(" mouse up, disable dragging");
 		if (cellDragging) {
 			focusBlockActionHandler.executeCommand();
 			// System.out.println("SWTFocusBlockManager: Executing copy command");
 		}
 		this.cellDragging = false;
 		setCursorAccelerator(isAccelarating(event));
 	}
 
 	private void handleKeyDown(Event event) {
 		ViewerCell tmp = null;
 
 		if (navigationStrategy.isCollapseEvent(viewer, focusCell, event)) {
 			navigationStrategy.collapse(viewer, focusCell, event);
 		} else if (navigationStrategy.isExpandEvent(viewer, focusCell, event)) {
 			navigationStrategy.expand(viewer, focusCell, event);
 		} else if (navigationStrategy.isNavigationEvent(viewer, event)) {
 			tmp = navigationStrategy.findSelectedCell(viewer, focusCell, event);
 
 			if (tmp != null) {
 				if (!tmp.equals(focusCell)) {
 					setFocusCell(tmp, event);
 				}
 			}
 		}
 
 		if (navigationStrategy.shouldCancelEvent(viewer, event)) {
 			event.doit = false;
 		}
 	}
 
 	private void handleSelection(Event event) {
 		// ignore while dragging.
 		if (cellDragging) {
 			return;
 		}
 
 		if ((event.detail & SWT.CHECK) == 0 && focusCell != null
 				&& focusCell.getItem() != event.item && event.item != null
 				&& !event.item.isDisposed()) {
 			if (viewer instanceof OpenTreeViewer) {
 				OpenTreeViewer atrViewer = (OpenTreeViewer) viewer;
 				ViewerRow row = atrViewer
 						.getViewerRowFromItemExposed(event.item);
 				Assert.isNotNull(row,
 						"Internal Structure invalid. Row item has no row ViewerRow assigned"); //$NON-NLS-1$
 
 				// CB TODO, update to sync our focus block.
 				ViewerCell tmp = row.getCell(focusCell.getColumnIndex());
 				if (!focusCell.equals(tmp)) {
 					setFocusCell(tmp, event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handles the {@link SWT#FocusIn} event.
 	 * 
 	 * @param event
 	 *            the event
 	 */
 	private void handleFocusIn(Event event) {
 		if (focusCell == null) {
 			setFocusCell(getInitialFocusCell(), event);
 		}
 	}
 
 	abstract ViewerCell getInitialFocusCell();
 
 	private void hookListener(final ColumnViewer viewer) {
 		Listener listener = new Listener() {
 
 			public void handleEvent(Event event) {
 				switch (event.type) {
 				case SWT.MouseDown:
 					handleMouseDown(event);
 					break;
 				case SWT.MouseMove:
 					handleMouseMove(event);
 					break;
 				case SWT.MouseUp:
 					handleMouseUp(event);
 					break;
 				case SWT.KeyDown:
 					handleKeyDown(event);
 					break;
 				case SWT.Selection:
 					handleSelection(event);
 					break;
 				case SWT.FocusIn:
 					handleFocusIn(event);
 					break;
 				}
 			}
 
 		};
 
 		viewer.getControl().addListener(SWT.MouseDown, listener);
 		viewer.getControl().addListener(SWT.MouseUp, listener);
 		viewer.getControl().addListener(SWT.MouseMove, listener);
 		viewer.getControl().addListener(SWT.KeyDown, listener);
 		viewer.getControl().addListener(SWT.Selection, listener);
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (event.getSelection().isEmpty()) {
 					setFocusCell(null);
 				}
 			}
 
 		});
 		viewer.getControl().addListener(SWT.FocusIn, listener);
 
 		// Disable accessibility as we do not have access to the viewer column.
 		// viewer.getControl().getAccessible()
 		// .addAccessibleListener(new AccessibleAdapter() {
 		// public void getName(AccessibleEvent event) {
 		// ViewerCell cell = getFocusCell();
 		// if (cell == null)
 		// return;
 		//
 		// ViewerRow row = cell.getViewerRow();
 		// if (row == null)
 		// return;
 		//
 		// ViewerColumn viewPart = viewer.getViewerColumn(cell
 		// .getColumnIndex());
 		//
 		// if (viewPart == null)
 		// return;
 		//
 		// CellLabelProvider labelProvider = viewPart
 		// .getLabelProvider();
 		//
 		// if (labelProvider == null)
 		// return;
 		// labelProvider.update(cell);
 		// event.result = cell.getText();
 		// }
 		// });
 
 	}
 
 	/**
 	 * @return the cell with the focus
 	 * 
 	 */
 	public ViewerCell getFocusCell() {
 		return focusCell;
 	}
 
 	final ViewerCell _getFocusCell() {
 		return focusCell;
 	}
 
 	// Get the focus block, excluding the focus cell itself.
 	final ViewerCell[] _getFocusBlock() {
 		ViewerCell[] focusBlockArray = new ViewerCell[focusBlock.size()];
 		return focusBlock.toArray(focusBlockArray);
 	}
 
 	protected void setFocusCell(Object object) {
 		setFocusCell(focusCell, null);
 	}
 
 	void setFocusCell(ViewerCell focusCell, Event event) {
 		ViewerCell oldCell = this.focusCell;
 
 		if (this.focusCell != null && !this.focusCell.getItem().isDisposed()) {
 			this.focusCell.getItem()
 					.removeDisposeListener(itemDeletionListener);
 		}
 
 		this.focusCell = focusCell;
 
 		if (this.focusCell != null && !this.focusCell.getItem().isDisposed()) {
 			this.focusCell.getItem().addDisposeListener(itemDeletionListener);
 		}
 
 		if (focusCell != null) {
 			focusCell.scrollIntoView();
 		}
 		this.cellHighlighter.focusCellChanged(focusCell, oldCell);
 		focusBlockActionHandler.updateCommand(focusCell, getTargets(), event);
 		getViewer().getControl().getAccessible().setFocus(ACC.CHILDID_SELF);
 	}
 
 	ColumnViewer getViewer() {
 		return viewer;
 	}
 
 	public IFocusBlockActionHandler getFocusBlockActionHandler() {
 		return focusBlockActionHandler;
 	}
 
 	public void setFocusBlockActionHandler(
 			IFocusBlockActionHandler focusBlockActionHandler) {
 		this.focusBlockActionHandler = focusBlockActionHandler;
 	}
 }

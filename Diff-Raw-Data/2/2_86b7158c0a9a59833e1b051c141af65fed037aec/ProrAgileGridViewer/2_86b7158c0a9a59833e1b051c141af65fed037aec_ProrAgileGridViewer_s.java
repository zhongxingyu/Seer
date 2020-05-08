 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.agilegrid;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.Cell;
 import org.agilemore.agilegrid.CellResizeAdapter;
 import org.agilemore.agilegrid.ICellResizeListener;
 import org.agilemore.agilegrid.ISelectionChangedListener;
 import org.agilemore.agilegrid.SWTX;
 import org.agilemore.agilegrid.SelectionChangedEvent;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
 import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
 import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.rmf.pror.reqif10.configuration.Column;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigPackage;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.pror.reqif10.editor.agilegrid.ProrAgileGridContentProvider.ProrRow;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.pror.reqif10.util.ProrUtil;
 import org.eclipse.rmf.reqif10.Reqif10Factory;
 import org.eclipse.rmf.reqif10.Reqif10Package;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecRelation;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.util.Reqif10Util;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * A Viewer that manages an {@link AgileGrid} rendering a
  * {@link SpecHierarchyRoot}.
  * 
  * @author jastram
  */
 public class ProrAgileGridViewer extends Viewer {
 
 	private final ProrAgileGrid agileGrid;
 	private Specification specification;
 	private ProrSpecViewConfiguration specViewConfig;
 	private ProrAgileGridContentProvider contentProvider;
 	private final EditingDomain editingDomain;
 
 	private IStructuredSelection selection;
 
 	private ISelectionChangedListener selectionChangedistener;
 	private EContentAdapter specHierarchyRootContentAdapter;
 	private Adapter emfColumnListener;
 	private ICellResizeListener agColumnListener;
 
 	/**
 	 * We need to ignore selection requests when a selection is in progress -
 	 * see {@link #setSelection(ISelection, boolean)}
 	 */
 	private boolean settingSelection = false;
 
 	/**
 	 * We need to temporarily store the dragTarget in order to use the EMF
 	 * DragNDrop Stuff.
 	 */
 	protected SpecHierarchy dragTarget;
 	private EContentAdapter specRelationContentAdapter;
 	private AdapterFactory adapterFactory;
 
 	/**
 	 * Constructs the Viewer.
 	 * 
 	 * @param adapterFactory
 	 */
 	public ProrAgileGridViewer(Composite composite, AdapterFactory adapterFactory, EditingDomain editingDomain) {
 		agileGrid = new ProrAgileGrid(composite, SWTX.AUTO_SCROLL
 				| SWTX.FILL_WITH_LASTCOL | SWT.MULTI | SWT.DOUBLE_BUFFERED);
 		agileGrid.setLayoutAdvisor(new ProrLayoutAdvisor(agileGrid));
 		// agileGrid.setAgileGridEditor(new ProrAgileGridEditor(agileGrid));
 		this.editingDomain = editingDomain;
 		this.adapterFactory = adapterFactory;
 		enableDragNDrop();
 		enableKeyHandling();
 	}
 
 	private void enableKeyHandling() {
 		agileGrid.addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 
				if (e.keyCode == 13 && e.stateMask == SWT.CTRL) {
 					e.doit = false;
 					if (selection.isEmpty()) {
 						return;
 					}
 					Object obj = selection.getFirstElement();
 					if (!(obj instanceof SpecHierarchy)) {
 						return;
 					}
 					SpecHierarchy specHierarchy = (SpecHierarchy) obj;
 					if (specHierarchy.getObject() == null) {
 						return;
 					}
 
 					insertNewRowAndEdit(specHierarchy);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Inserts a new SpecHierarchy as the sibling of this one and starts editing
 	 * it.
 	 */
 	private void insertNewRowAndEdit(SpecHierarchy specHierarchy) {
 		// Won't do anything if there is no object
 		if (specHierarchy.getObject() == null
 				|| specHierarchy.getObject().getType() == null) {
 			return;
 		}
 
 		// Create the new elements
 		SpecHierarchy newSpecHierarchy = Reqif10Factory.eINSTANCE.createSpecHierarchy();
 		SpecObject newSpecObject = Reqif10Factory.eINSTANCE.createSpecObject();
 		newSpecHierarchy.setObject(newSpecObject);
 		
 		EReference childFeature;
 		int pos;
 		EObject parent = specHierarchy.eContainer();
 		if (parent instanceof SpecHierarchy) {
 			childFeature = Reqif10Package.Literals.SPEC_HIERARCHY__CHILDREN;
 			pos = ((SpecHierarchy) specHierarchy.eContainer()).getChildren()
 					.indexOf(specHierarchy) + 1;
 		} else if (parent instanceof Specification) {
 			childFeature = Reqif10Package.Literals.SPECIFICATION__CHILDREN;
 			pos = ((Specification) specHierarchy.eContainer())
 					.getChildren().indexOf(specHierarchy) + 1;
 		} else {
 			throw new IllegalStateException("Wrong parent: " + parent);
 		}
 
 		CompoundCommand cmd = ProrUtil.createAddTypedElementCommand(Reqif10Util
 				.getReqIf(specHierarchy).getCoreContent(),
 				Reqif10Package.Literals.REQ_IF_CONTENT__SPEC_OBJECTS,
 				newSpecObject, Reqif10Package.Literals.SPEC_OBJECT__TYPE,
 				specHierarchy.getObject().getType(), -1, -1, editingDomain,
 				adapterFactory);
 		
 		cmd.append(AddCommand.create(editingDomain, specHierarchy.eContainer(),
 				childFeature, newSpecHierarchy, pos));
 		
 		editingDomain.getCommandStack().execute(cmd);
 
 		// The row to be edited may be further down due to indenting.
 		Cell activeCell = agileGrid.getCellSelection()[0];
 		int row = activeCell.row + 1;
 		while (contentProvider.getProrRow(row).getSpecHierarchy() != newSpecHierarchy) {
 			row++;
 		}
 		agileGrid.editCell(row, activeCell.column);
 				
 	}
 
 	@Override
 	public Control getControl() {
 		return agileGrid;
 	}
 
 	@Override
 	public Object getInput() {
 		return specification;
 	}
 
 	@Override
 	public ISelection getSelection() {
 		return selection != null ? selection : StructuredSelection.EMPTY;
 	}
 
 	@Override
 	public void refresh() {
 		agileGrid.redraw();
 	}
 
 	@Override
 	public void setInput(Object input) {
 		unregisterColumnListener();
 		unregisterSelectionChangedListener();
 		unregisterSpecHierarchyListener();
 		unregisterSpecRelationListener();
 
 		this.specification = (Specification) input;
 		this.specViewConfig = ConfigurationUtil
 				.getSpecViewConfiguration(specification, editingDomain);
 		this.contentProvider = new ProrAgileGridContentProvider(
 				specification, specViewConfig);
 		agileGrid.setContentProvider(contentProvider);
 		agileGrid.setCellRendererProvider(new ProrCellRendererProvider(
 				agileGrid, adapterFactory, editingDomain));
 		agileGrid.setCellEditorProvider(new ProrCellEditorProvider(agileGrid,
 				editingDomain, adapterFactory));
 		agileGrid.setRowResizeCursor(new Cursor(agileGrid.getDisplay(),
 				SWT.CURSOR_ARROW));
 
 		updateRowCount();
 		updateColumnInformation();
 		registerColumnListener();
 		registerSelectionChangedListener();
 		registerSpecHierarchyListener();
 		registerSpecRelationListener();
 	}
 
 	/**
 	 * Explicit cleanup to prevent strange errors.
 	 */
 	public void dispose() {
 		unregisterColumnListener();
 		unregisterSelectionChangedListener();
 		unregisterSpecHierarchyListener();
 		unregisterSpecRelationListener();
 		if (!agileGrid.isDisposed()) {
 			agileGrid.dispose();
 		}
 
 	}
 
 	private void unregisterColumnListener() {
 		if (emfColumnListener != null)
 			specViewConfig.eAdapters().remove(emfColumnListener);
 	}
 
 	/**
 	 * Registers two listeners: (1) Listen to the EMF Model to propagate Column
 	 * changes to the agileGrid; (2) Listen to the agileGrid and propagate
 	 * changes to the EMF Model.
 	 * <p>
 	 * 
 	 * (1) Includes column count and labels.<br>
 	 * (2) Includes column width.
 	 */
 	private void registerColumnListener() {
 		emfColumnListener = new AdapterImpl() {
 			@Override
 			public void notifyChanged(Notification msg) {
 				if (!agileGrid.isDisposed()) {
 					updateColumnInformation();
 					refresh();
 				}
 			}
 		};
 		specViewConfig.eAdapters().add(emfColumnListener);
 
 		agColumnListener = new CellResizeAdapter() {
 			@Override
 			public void columnResized(int col, int newWidth) {
 				// Can happen when the label column is resized.
 				if (col < 0)
 					return;
 				Column column = specViewConfig.getColumns().get(col);
 				Command cmd = SetCommand.create(editingDomain, column,
 						ConfigPackage.Literals.COLUMN__WIDTH, newWidth);
 
 				editingDomain.getCommandStack().execute(cmd);
 			}
 		};
 		agileGrid.addCellResizeListener(agColumnListener);
 	}
 
 	private void unregisterSpecHierarchyListener() {
 		if (specHierarchyRootContentAdapter != null) {
 			specification.eAdapters().remove(
 					specHierarchyRootContentAdapter);
 		}
 	}
 
 	private void unregisterSpecRelationListener() {
 		if (specRelationContentAdapter != null) {
 			Reqif10Util.getReqIf(specification).getCoreContent().eAdapters()
 					.remove(specHierarchyRootContentAdapter);
 		}
 	}
 
 	/**
 	 * Attaches a Listener to notify the agileGrid when a
 	 * {@link SpecHierarchyRoot} (or any of its children) changes.
 	 */
 	private void registerSpecHierarchyListener() {
 		specHierarchyRootContentAdapter = new EContentAdapter() {
 			@Override
 			public void notifyChanged(Notification notification) {
 				super.notifyChanged(notification);
 				if (notification.getNewValue() instanceof SpecObject
 						&& notification.getEventType() == Notification.SET) {
 					SpecObject specObject = (SpecObject) notification
 							.getNewValue();
 					contentProvider.updateElement(specObject);
 				} else if (notification.getFeature() == Reqif10Package.Literals.SPECIFICATION__CHILDREN
 						|| notification.getFeature() == Reqif10Package.Literals.SPEC_HIERARCHY__CHILDREN) {
 					updateRowCount();
 					refresh();
 				}
 			}
 		};
 		specification.eAdapters().add(specHierarchyRootContentAdapter);
 	}
 
 	private void registerSpecRelationListener() {
 		specRelationContentAdapter = new EContentAdapter() {
 			@Override
 			public void notifyChanged(Notification notification) {
 				super.notifyChanged(notification);
 				SpecRelation specRelation = null;
 				if (notification.getNotifier() instanceof SpecRelation) {
 					specRelation = (SpecRelation) notification.getNotifier();
 				}
 				if (notification.getNewValue() instanceof SpecRelation) {
 					specRelation = (SpecRelation) notification.getNewValue();
 				}
 
 				if (specRelation != null) {
 					if (specRelation.getSource() != null) {
 						contentProvider.updateElement(specRelation.getSource());
 					}
 					if (specRelation.getTarget() != null) {
 						contentProvider.updateElement(specRelation.getTarget());
 					}
 					if (contentProvider.getShowSpecRelations()) {
 						updateRowCount();
 					}
 					refresh();
 				}
 			}
 		};
 
 		Reqif10Util.getReqIf(specification).getCoreContent().eAdapters()
 				.add(specRelationContentAdapter);
 	}
 
 	private void unregisterSelectionChangedListener() {
 		if (selectionChangedistener != null && ! agileGrid.isDisposed())
 			agileGrid.removeSelectionChangedListener(selectionChangedistener);
 	}
 
 	/**
 	 * Creates an {@link ISelectionChangedListener} for registration with the
 	 * {@link AgileGrid} that relays Selections to the Eclipse environment.
 	 * <p>
 	 * 
 	 * What's confusing is that some of the AgileGrid stuff and Eclipse stuff
 	 * has the same name (e.g. SelectionChangedEvent), so be aware of what's
 	 * going on.
 	 * 
 	 */
 	private void registerSelectionChangedListener() {
 		// Remove the old listener, if present
 		if (selectionChangedistener != null) {
 			agileGrid.removeSelectionChangedListener(selectionChangedistener);
 		}
 
 		// Create a Listener that translates the new selection from Cells to
 		// SpecHierarchies
 		selectionChangedistener = new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				Set<Cell> cells = event.getNewSelections();
 				List<Object> items = new ArrayList<Object>();
 				for (Cell cell : cells) {
 					Object item = contentProvider.getProrRow(cell.row).element;
 
 					// If the item is a SpecRelation and the last column is
 					// selected, show the target instead.
 					if (item instanceof SpecRelation
 							&& cell.column == specViewConfig.getColumns()
 									.size()) {
 						item = ((SpecRelation) item).getTarget();
 					}
 					items.add(item);
 				}
 				selection = new StructuredSelection(items);
 				ProrAgileGridViewer.this
 						.fireSelectionChanged(new org.eclipse.jface.viewers.SelectionChangedEvent(
 								ProrAgileGridViewer.this, selection));
 			}
 		};
 		agileGrid.addSelectionChangedListener(selectionChangedistener);
 	}
 
 	/**
 	 * Updates the number so rows. This is an expensive operation, as we have to
 	 * count the elements one by one.
 	 */
 	private void updateRowCount() {
 		agileGrid.getLayoutAdvisor().setRowCount(contentProvider.getRowCount());
 	}
 
 	/**
 	 * Update Column Count, Column Label and Column Width. This should be called
 	 * whenever a Column changes.
 	 * <p>
 	 * 
 	 */
 	private void updateColumnInformation() {
 		EList<Column> cols = specViewConfig.getColumns();
 		// One more column for the links
 		if (!agileGrid.isDisposed()) {
 			agileGrid.getLayoutAdvisor().setColumnCount(cols.size() + 1);
 			for (int i = 0; i < cols.size(); i++) {
 				agileGrid.getLayoutAdvisor().setTopHeaderLabel(i,
 						cols.get(i).getLabel());
 				agileGrid.getLayoutAdvisor().setColumnWidth(i,
 						cols.get(i).getWidth());
 			}
 		}
 	}
 
 	/**
 	 * Changes the selection. Note that we are only interested in the selected
 	 * row, not column. Thus, if something is already selected in a certain row
 	 * and the row is supposed to stay selected, then we'll leave the selection
 	 * as is.
 	 */
 	@Override
 	public void setSelection(ISelection selection, boolean reveal) {
 		if (!(selection instanceof IStructuredSelection))
 			return;
 
 		// Necessary due to a bug in CellSelectionManager (AgileGrid).
 		if (settingSelection)
 			return;
 		settingSelection = true;
 
 		Set<Cell> cells = new HashSet<Cell>();
 		for (int row = 0; row < contentProvider.getRowCount(); row++) {
 			ProrRow prorRow = contentProvider.getProrRow(row);
 			Object current = prorRow.getSpecHierarchy();
 			if (current == null)
 				current = prorRow.getSpecElement();
 
 			for (Object item : ((IStructuredSelection) selection).toList()) {
 				if (item.equals(current)) {
 					boolean added = false;
 					for (int col = 0; col < agileGrid.getLayoutAdvisor()
 							.getColumnCount(); col++) {
 						if (agileGrid.isCellSelected(row, col)) {
 							cells.add(new Cell(agileGrid, row, col));
 							added = true;
 						}
 					}
 					if (!added) {
 						cells.add(new Cell(agileGrid, row, 0));
 					}
 					break;
 				}
 			}
 		}
 
 		Cell[] cellArray = new Cell[cells.size()];
 		cells.toArray(cellArray);
 
 		// Without clearing the selection, selections would accumulate.
 		agileGrid.clearSelection();
 		// Without focusing a cell, we'd get an overflow in CellSelectionManager
 		// due to a bug there. However, calling focusCell will trigger another
 		// selection event. This is a reason why we introduced #settingSelection
 		// to ignore further selection events when we are updating.
 		if (cellArray.length > 0) {
 			// We do not want to interrupt editing
 			if (! agileGrid.isCellEditorActive()) {
 				agileGrid.focusCell(cellArray[0]);				
 			}
 			agileGrid.selectCells(cellArray);
 		}
 
 		// Notify all Listeners
 		settingSelection = false;
 
 	}
 
 	/**
 	 * Can be called to provide a context menu
 	 */
 	public void setContextMenu(MenuManager contextMenu) {
 		Menu menu = contextMenu.createContextMenu(agileGrid);
 		agileGrid.setMenu(menu);
 	}
 
 	private void enableDragNDrop() {
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance(),
 				LocalSelectionTransfer.getTransfer() };
 		addDragSupport(dndOperations, transfers, new ViewerDragAdapter(this) {
 
 			// Modified to allow resizing of columns
 			@Override
 			public void dragStart(DragSourceEvent event) {
 				Cell cell = agileGrid.getCell(event.x, event.y);
 				if (cell.row == -1 || cell.column == -1) {
 					event.doit = false;
 				}
 				super.dragStart(event);
 			}
 		});
 
 		// Modified to make the EMF-Based Drag and Drop work.
 		addDropSupport(dndOperations, transfers,
 				new EditingDomainViewerDropAdapter(editingDomain, this) {
 
 					/**
 					 * By using an AgileGrid instead of a StructuredViewer, item
 					 * will always be null. Thus, we store the dragTarget when
 					 * dragOver is called.
 					 */
 					@Override
 					protected Object extractDropTarget(Widget item) {
 						return dragTarget;
 					}
 
 					@Override
 					public void dragOver(DropTargetEvent e) {
 						Point pos = agileGrid.toControl(e.x, e.y);
 						Cell cell = agileGrid.getCell(pos.x, pos.y);
 						Object target = contentProvider.getProrRow(cell.row).element;
 						if (target instanceof SpecHierarchy) {
 							dragTarget = (SpecHierarchy) target;
 						}
 					}
 				});
 	}
 
 	/**
 	 * Copied from {@link StructuredViewer}
 	 */
 	private void addDragSupport(int operations, Transfer[] transferTypes,
 			DragSourceListener listener) {
 		Control myControl = getControl();
 		final DragSource dragSource = new DragSource(myControl, operations);
 		dragSource.setTransfer(transferTypes);
 		dragSource.addDragListener(listener);
 	}
 
 	/**
 	 * Copied from {@link StructuredViewer}
 	 */
 	private void addDropSupport(int operations, Transfer[] transferTypes,
 			final DropTargetListener listener) {
 		DropTarget dropTarget = new DropTarget(agileGrid, operations);
 		dropTarget.setTransfer(transferTypes);
 		dropTarget.addDropListener(listener);
 	}
 
 	public void setShowSpecRelations(boolean status) {
 		contentProvider.setShowSpecRelations(status);
 		updateRowCount();
 		agileGrid.redraw();
 	}
 
 }

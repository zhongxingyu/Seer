 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.table;
 
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewerItem;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.InsertionPoint;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.MergeViewerInfoComposite;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.IStructuralFeatureAccessor;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 class TableMergeViewer extends AbstractMergeViewer<Composite> {
 
 	private IStructuralFeatureAccessor fInput;
 
 	private final EMFCompareContentMergeViewer fContentMergeViewer;
 
 	private Composite c;
 
 	private MergeViewerInfoComposite mergeViewerInfoComposite;
 
 	TableMergeViewer(Composite parent, EMFCompareContentMergeViewer contentMergeViewer, MergeViewerSide side) {
 		super(parent, side);
 		fContentMergeViewer = contentMergeViewer;
 
 		getStructuredViewer().getTable().addListener(SWT.EraseItem, new Listener() {
 			public void handleEvent(Event event) {
 				TableMergeViewer.this.handleEraseItemEvent(event);
 			}
 		});
 
 		getStructuredViewer().getTable().addListener(SWT.MeasureItem, new Listener() {
 			private Widget fLastWidget;
 
 			private int fLastHeight;
 
 			public void handleEvent(Event event) {
 				// Windows bug: prevents StackOverflow
 				if (event.item == fLastWidget && event.height == fLastHeight) {
 					return;
 				}
 
 				fLastWidget = event.item;
 				fLastHeight = event.height;
 				int newHeight = (int)(event.gc.getFontMetrics().getHeight() * 1.33d);
				if (newHeight % 2 == 1) {
 					newHeight += 1;
 				}
 				event.height = newHeight;
 			}
 		});
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer#getControl()
 	 */
 	public Composite getControl() {
 		return c;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer#createStructuredViewer()
 	 */
 	@Override
 	protected final TableViewer createStructuredViewer(Composite parent) {
 		c = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginLeft = -1;
 		layout.marginRight = -1;
 		layout.marginTop = -1;
 		layout.marginBottom = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		c.setLayout(layout);
 
 		mergeViewerInfoComposite = new MergeViewerInfoComposite(c);
 		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
 		mergeViewerInfoComposite.setLayoutData(layoutData);
 
 		TableViewer tableViewer = new TableViewer(c, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
 				| SWT.FULL_SELECTION);
 		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		return tableViewer;
 	}
 
 	public int getVerticalOffset() {
 		return mergeViewerInfoComposite.getSize().y - 2;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer#setLabelProvider(org.eclipse.jface.viewers.ILabelProvider)
 	 */
 	@Override
 	public void setLabelProvider(ILabelProvider labelProvider) {
 		super.setLabelProvider(labelProvider);
 		mergeViewerInfoComposite.setLabelProvider(labelProvider);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer#getStructuredViewer()
 	 */
 	@Override
 	protected TableViewer getStructuredViewer() {
 		return (TableViewer)super.getStructuredViewer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer#setInput(java.lang.Object)
 	 */
 	public void setInput(Object object) {
 		if (object instanceof IStructuralFeatureAccessor) {
 			fInput = (IStructuralFeatureAccessor)object;
 			getStructuredViewer().setInput(fInput.getItems());
 			mergeViewerInfoComposite.setInput(fInput, getSide());
 		} else {
 			fInput = null;
 			getStructuredViewer().setInput(null);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IInputProvider#getInput()
 	 */
 	public Object getInput() {
 		return fInput;
 	}
 
 	private void handleEraseItemEvent(Event event) {
 		TableItem tableItem = (TableItem)event.item;
 		Object data = tableItem.getData();
 
 		if (data instanceof InsertionPoint) {
 			InsertionPoint insertionPoint = (InsertionPoint)data;
 			paintItemDiffBox(event, insertionPoint.getDiff(), getBoundsForInsertionPoint(event));
 		} else if (data instanceof IMergeViewerItem) {
 			Diff diff = ((IMergeViewerItem)data).getDiff();
 			if (diff != null) {
 				paintItemDiffBox(event, diff, getBounds(event));
 			}
 		}
 	}
 
 	private void paintItemDiffBox(Event event, Diff diff, Rectangle bounds) {
 		event.detail &= ~SWT.HOT;
 
 		if (diff.getState() == DifferenceState.DISCARDED || diff.getState() == DifferenceState.MERGED) {
 			return;
 		}
 
 		GC g = event.gc;
 		Color oldBackground = g.getBackground();
 		Color oldForeground = g.getForeground();
 
 		setDiffColorsToGC(g, diff, isSelected(event));
 		g.fillRectangle(bounds);
 		g.drawRectangle(bounds);
 
 		if (getSide() == MergeViewerSide.LEFT) {
 			drawLineFromBoxToCenter(event, bounds, g);
 		} else {
 			drawLineFromCenterToBox(event, bounds, g);
 		}
 
 		if (isSelected(event)) {
 			g.setForeground(event.display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
 			g.setBackground(event.display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
 			event.detail &= ~SWT.SELECTED;
 		} else {
 			g.setBackground(oldBackground);
 			g.setForeground(oldForeground);
 		}
 	}
 
 	private void drawLineFromCenterToBox(Event event, Rectangle boxBounds, GC g) {
 		TableItem tableItem = (TableItem)event.item;
 		Rectangle itemBounds = tableItem.getBounds();
 		Point from = new Point(0, 0);
 		from.y = itemBounds.y + (itemBounds.height / 2);
 		Point to = new Point(boxBounds.x, from.y);
 		g.drawLine(from.x, from.y, to.x, to.y);
 	}
 
 	private void drawLineFromBoxToCenter(Event event, Rectangle boxBounds, GC g) {
 		TableItem tableItem = (TableItem)event.item;
 		Rectangle itemBounds = tableItem.getBounds();
 		Rectangle clientArea = tableItem.getParent().getClientArea();
 		Point from = new Point(0, 0);
 		from.x = boxBounds.x + boxBounds.width;
 		from.y = itemBounds.y + (itemBounds.height / 2);
 		Point to = new Point(0, from.y);
 		to.x = clientArea.x + clientArea.width;
 		g.drawLine(from.x, from.y, to.x, to.y);
 	}
 
 	static boolean isSelected(Event event) {
 		return (event.detail & SWT.SELECTED) != 0;
 	}
 
 	private static Rectangle getBounds(Event event) {
 		TableItem tableItem = (TableItem)event.item;
 		Table table = tableItem.getParent();
 		Rectangle tableBounds = table.getClientArea();
 		Rectangle itemBounds = tableItem.getBounds();
 
 		Rectangle fill = new Rectangle(0, 0, 0, 0);
 		fill.x = 2;
 		fill.y = itemBounds.y + 2;
 		// +x to add the icon and the expand "+" if we are in a tree
 		fill.width = itemBounds.width + itemBounds.x;
 		fill.height = itemBounds.height - 3;
 
 		final GC g = event.gc;
 		// If you wish to paint the selection beyond the end of last column, you must change the clipping
 		// region.
 		int columnCount = table.getColumnCount();
 		if (event.index == columnCount - 1 || columnCount == 0) {
 			int width = tableBounds.x + tableBounds.width - event.x;
 			if (width > 0) {
 				Region region = new Region();
 				g.getClipping(region);
 				region.add(event.x, event.y, width, event.height);
 				g.setClipping(region);
 				region.dispose();
 			}
 		}
 		g.setAdvanced(true);
 
 		return fill;
 	}
 
 	private static Rectangle getBoundsForInsertionPoint(Event event) {
 		Rectangle fill = getBounds(event);
 		TableItem tableItem = (TableItem)event.item;
 		Rectangle tableBounds = tableItem.getParent().getClientArea();
 		fill.y = fill.y + 6;
 		fill.width = tableBounds.width / 4;
 		fill.height = fill.height - 12;
 
 		return fill;
 	}
 
 	private void setDiffColorsToGC(GC g, Diff diff, boolean selected) {
 		g.setForeground(fContentMergeViewer.getColors().getStrokeColor(diff,
 				fContentMergeViewer.isThreeWay(), false, selected));
 		g.setBackground(fContentMergeViewer.getColors().getFillColor(diff, fContentMergeViewer.isThreeWay(),
 				false, selected));
 	}
 
 }

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
 
 import java.util.ResourceBundle;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MatchedObject;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MergeViewer;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.TableMergeViewer;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.accessor.IStructuralFeatureAccessor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemFontProvider;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class TableContentMergeViewer extends EMFCompareContentMergeViewer {
 
 	/**
 	 * Bundle name of the property file containing all displayed strings.
 	 */
 	private static final String BUNDLE_NAME = TableContentMergeViewer.class.getName();
 
 	private final ComposedAdapterFactory fAdapterFactory;
 
 	private double[] fBasicCenterCurve;
 
 	/**
 	 * Call the super constructor.
 	 * 
 	 * @see TableContentMergeViewer
 	 */
 	protected TableContentMergeViewer(Composite parent, CompareConfiguration config) {
 		super(SWT.NONE, ResourceBundle.getBundle(BUNDLE_NAME), config);
 		fAdapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		fAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 		fAdapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 
 		buildControl(parent);
 		setContentProvider(new TableContentMergeViewerContentProvider(config));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		fAdapterFactory.dispose();
 		super.handleDispose(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getContents(boolean)
 	 */
 	@Override
 	protected byte[] getContents(boolean left) {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#copyDiff(boolean)
 	 */
 	@Override
 	protected void copyDiff(boolean leftToRight) {
 		final Diff diffToCopy = getDiffToCopy(getRightMergeViewer());
 		if (diffToCopy != null) {
 			Command copyCommand = getEditingDomain().createCopyCommand(diffToCopy, leftToRight);
 			getEditingDomain().getCommandStack().execute(copyCommand);
 
 			if (leftToRight) {
 				setRightDirty(true);
 			} else {
 				setLeftDirty(true);
 			}
 			refresh();
 		}
 	}
 
 	private Diff getDiffToCopy(MergeViewer mergeViewer) {
 		Diff diffToCopy = null;
 		ISelection selection = mergeViewer.getSelection();
 		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
 			Object firstElement = ((IStructuredSelection)selection).getFirstElement();
 			if (firstElement instanceof IMergeViewerItem) {
 				diffToCopy = ((IMergeViewerItem)firstElement).getDiff();
 			}
 		}
 		return diffToCopy;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#getLeftMergeViewer()
 	 */
 	@Override
 	protected TableMergeViewer getLeftMergeViewer() {
 		return (TableMergeViewer)super.getLeftMergeViewer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#getRightMergeViewer()
 	 */
 	@Override
 	protected TableMergeViewer getRightMergeViewer() {
 		return (TableMergeViewer)super.getRightMergeViewer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#getAncestorMergeViewer()
 	 */
 	@Override
 	protected TableMergeViewer getAncestorMergeViewer() {
 		return (TableMergeViewer)super.getAncestorMergeViewer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#createMergeViewer(org.eclipse.swt.widgets.Composite,
 	 *      org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer.MergeViewerSide)
 	 */
 	@Override
 	protected MergeViewer createMergeViewer(Composite parent, final MergeViewerSide side) {
 		TableMergeViewer ret = new TableMergeViewer(parent, side, this);
 		ret.getStructuredViewer().getTable().getVerticalBar().setVisible(false);
 
 		ret.setContentProvider(new ArrayContentProvider() {
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
 			 */
 			@Override
 			public Object[] getElements(Object inputElement) {
 				if (inputElement instanceof IStructuralFeatureAccessor) {
 					return super.getElements(((IStructuralFeatureAccessor)inputElement).getItems());
 				}
 				return super.getElements(inputElement);
 			}
 		});
 		ret.setLabelProvider(new AdapterFactoryLabelProvider.FontAndColorProvider(fAdapterFactory, ret
 				.getStructuredViewer()) {
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getFont(java.lang.Object,
 			 *      int)
 			 */
 			@Override
 			public Font getFont(Object object, int columnIndex) {
 				if (object instanceof MatchedObject) {
 					final Object value = ((MatchedObject)object).getSideValue(side);
 					if (value instanceof EObject && ((EObject)value).eIsProxy()) {
 						return getFontFromObject(IItemFontProvider.ITALIC_FONT);
 					}
 				}
 				return super.getFont(object, columnIndex);
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getColumnText(java.lang.Object,
 			 *      int)
 			 */
 			@Override
 			public String getColumnText(Object object, int columnIndex) {
 				if (object instanceof MatchedObject) {
 					final String text;
 					final Object value = ((MatchedObject)object).getSideValue(side);
 					if (value instanceof EObject && ((EObject)value).eIsProxy()) {
 						text = "proxy : " + ((InternalEObject)value).eProxyURI().toString();
 					} else {
 						text = super.getColumnText(value, columnIndex);
 					}
 					return text;
 				}
 				return super.getColumnText(object, columnIndex);
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getColumnImage(java.lang.Object,
 			 *      int)
 			 */
 			@Override
 			public Image getColumnImage(Object object, int columnIndex) {
 				if (object instanceof MatchedObject) {
 					return super.getColumnImage(((MatchedObject)object).getSideValue(side), columnIndex);
 				}
 				return super.getColumnImage(object, columnIndex);
 			}
 		});
 		ret.getStructuredViewer().getTable().getVerticalBar().addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				redrawCenterControl();
 			}
 		});
 
 		ret.getStructuredViewer().getTable().addMouseWheelListener(new MouseWheelListener() {
 			public void mouseScrolled(MouseEvent e) {
 				redrawCenterControl();
 			}
 		});
 		ret.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				redrawCenterControl();
 			}
 		});
 		return ret;
 	}
 
 	protected void redrawCenterControl() {
 		getCenterControl().redraw();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#paintCenter(org.eclipse.swt.widgets.Canvas,
 	 *      org.eclipse.swt.graphics.GC)
 	 */
 	@Override
 	protected void paintCenter(GC g) {
 		TableMergeViewer leftMergeViewer = getLeftMergeViewer();
 		TableMergeViewer rightMergeViewer = getRightMergeViewer();
 
 		Table leftTable = leftMergeViewer.getStructuredViewer().getTable();
 		Table rightTable = rightMergeViewer.getStructuredViewer().getTable();
 
 		Rectangle leftClientArea = leftTable.getClientArea();
 		Rectangle rightClientArea = rightTable.getClientArea();
 
 		TableItem[] leftItems = leftTable.getItems();
 		TableItem[] rightItems = rightTable.getItems();
 
 		TableItem[] selection = leftTable.getSelection();
 
 		for (TableItem leftItem : leftItems) {
 			final boolean selected;
 			if (selection.length > 0) {
 				selected = leftItem == selection[0];
 			} else {
 				selected = false;
 			}
 			final Diff leftDiff = ((IMergeViewerItem)leftItem.getData()).getDiff();
 
 			if (leftDiff != null && leftDiff.getState() == DifferenceState.UNRESOLVED) {
 				TableItem rightItem = findRightTableItemFromLeftDiff(rightItems, leftDiff);
 
 				if (rightItem != null) {
 					Color strokeColor = getCompareColor().getStrokeColor(leftDiff, isThreeWay(), false,
 							selected);
 					g.setForeground(strokeColor);
 					drawCenterLine(g, leftClientArea, rightClientArea, leftItem, rightItem);
 				}
 			}
 		}
 	}
 
 	private void drawCenterLine(GC g, Rectangle leftClientArea, Rectangle rightClientArea,
 			TableItem leftItem, TableItem rightItem) {
 		Control control = getCenterControl();
 		Point from = new Point(0, 0);
 		Point to = new Point(0, 0);
 
 		Rectangle leftBounds = leftItem.getBounds();
 		Rectangle rightBounds = rightItem.getBounds();
 
 		from.y = leftBounds.y + (leftBounds.height / 2) - leftClientArea.y + 1
 				+ getLeftMergeViewer().getVerticalOffset();
 
 		to.x = control.getBounds().width;
 		to.y = rightBounds.y + (rightBounds.height / 2) - rightClientArea.y + 1
 				+ getRightMergeViewer().getVerticalOffset();
 
 		int[] points = getCenterCurvePoints(from, to);
 		for (int i = 1; i < points.length; i++) {
 			g.drawLine(from.x + i - 1, points[i - 1], i, points[i]);
 		}
 	}
 
 	private TableItem findRightTableItemFromLeftDiff(TableItem[] rightItems, Diff leftDiff) {
 		TableItem ret = null;
 		for (int i = 0; i < rightItems.length && ret == null; i++) {
 			TableItem rightItem = rightItems[i];
 			final Diff rightDiff = ((IMergeViewerItem)rightItem.getData()).getDiff();
 			if (leftDiff == rightDiff) {
 				ret = rightItem;
 			}
 		}
 		return ret;
 	}
 
 	private int[] getCenterCurvePoints(Point from, Point to) {
 		int startx = from.x;
 		int starty = from.y;
 		int endx = to.x;
 		int endy = to.y;
 		if (fBasicCenterCurve == null) {
 			buildBaseCenterCurve(endx - startx);
 		}
 		double height = endy - starty;
 		height = height / 2;
 		int width = endx - startx;
 		int[] points = new int[width];
 		for (int i = 0; i < width; i++) {
 			points[i] = (int)(-height * fBasicCenterCurve[i] + height + starty);
 		}
 		return points;
 	}
 
 	private void buildBaseCenterCurve(int w) {
 		double width = w;
 		fBasicCenterCurve = new double[getCenterWidth()];
 		for (int i = 0; i < getCenterWidth(); i++) {
 			double r = i / width;
 			fBasicCenterCurve[i] = Math.cos(Math.PI * r);
 		}
 	}
 }

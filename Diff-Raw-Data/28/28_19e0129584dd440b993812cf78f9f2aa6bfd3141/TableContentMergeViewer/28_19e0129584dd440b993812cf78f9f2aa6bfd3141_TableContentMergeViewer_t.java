 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.table;
 
 import static com.google.common.base.Predicates.equalTo;
 
import com.google.common.base.Objects;
import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer;
 import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.ICompareAccessor;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.AbstractMergeViewer;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.impl.TableMergeViewer;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.util.MergeViewerUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemFontProvider;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
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
 	protected TableContentMergeViewer(Composite parent, EMFCompareConfiguration config) {
 		super(SWT.NONE, ResourceBundle.getBundle(BUNDLE_NAME), config);
 		fAdapterFactory = new ComposedAdapterFactory(EMFCompareRCPPlugin.getDefault()
 				.getAdapterFactoryRegistry());
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
 	protected AbstractMergeViewer createMergeViewer(Composite parent, final MergeViewerSide side) {
 		TableMergeViewer ret = new TableMergeViewer(parent, side, this, getCompareConfiguration());
 		ret.getStructuredViewer().getTable().getVerticalBar().setVisible(false);
 
 		ret.setContentProvider(new ArrayContentProvider() {
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
 			 */
 			@Override
 			public Object[] getElements(Object inputElement) {
 				if (inputElement instanceof ICompareAccessor) {
 					return super.getElements(((ICompareAccessor)inputElement).getItems());
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
 				if (object instanceof IMergeViewerItem) {
 					final Object value = ((IMergeViewerItem)object).getSideValue(side);
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
 				if (object instanceof IMergeViewerItem) {
 					final String text;
 					IMergeViewerItem mergeViewerItem = (IMergeViewerItem)object;
 					final Object value = mergeViewerItem.getSideValue(side);
 					if (value instanceof EObject && ((EObject)value).eIsProxy()) {
 						text = "proxy : " + ((InternalEObject)value).eProxyURI().toString();
 					} else if (mergeViewerItem.isInsertionPoint()) {
 						// workaround for 406513: Windows specific issue. Only labels of (Tree/Table)Item are
 						// selectable on Windows platform. The labels of placeholders in (Tree/Table)Viewer
 						// are one whitespace. Placeholder are then selectable at the very left of itself.
 						// Add a 42 whitespaces label to workaround.
						text = Strings.repeat(" ", 42);
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
 				if (object instanceof IMergeViewerItem) {
 					IMergeViewerItem mergeViewerItem = (IMergeViewerItem)object;
 					if (((IMergeViewerItem)object).isInsertionPoint()) {
 						return null;
 					} else {
						Object sideValue = mergeViewerItem.getSideValue(side);
						Image superImage = super.getColumnImage(sideValue, columnIndex);
						if (superImage == null) {
							return getDefaultImage(sideValue);
						} else {
							return superImage;
						}
 					}
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
 
 		final ImmutableSet<TableItem> selection = ImmutableSet.copyOf(leftTable.getSelection());
 
 		for (TableItem leftItem : leftItems) {
 			final boolean selected = Iterables.any(selection, equalTo(leftItem));
 			IMergeViewerItem leftData = (IMergeViewerItem)leftItem.getData();
 			final Diff leftDiff = ((IMergeViewerItem)leftItem.getData()).getDiff();
 			if (leftDiff != null) {
 				if (MergeViewerUtil.isVisibleInMergeViewer(leftDiff, getDifferenceGroupProvider(),
 						getDifferenceFilterPredicate())) {
 					TableItem rightItem = findRightTableItemFromLeftDiff(rightItems, leftDiff, leftData);
 
 					if (rightItem != null) {
 						final Color strokeColor = getCompareColor().getStrokeColor(leftDiff, isThreeWay(),
 								false, selected);
 						g.setForeground(strokeColor);
 						drawCenterLine(g, leftClientArea, rightClientArea, leftItem, rightItem);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer#createToolItems(org.eclipse.jface.action.ToolBarManager)
 	 */
 	@Override
 	protected void createToolItems(ToolBarManager toolBarManager) {
 		super.createToolItems(toolBarManager);
 		IContributionItem[] items = toolBarManager.getItems();
 		for (IContributionItem iContributionItem : items) {
 			if (iContributionItem instanceof ActionContributionItem) {
 				IAction action = ((ActionContributionItem)iContributionItem).getAction();
 				String id = action.getActionDefinitionId();
 				if ("org.eclipse.compare.copyAllLeftToRight".equals(id)) {
 					toolBarManager.remove(iContributionItem);
 				} else if ("org.eclipse.compare.copyAllRightToLeft".equals(id)) {
 					toolBarManager.remove(iContributionItem);
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
 
 	private TableItem findRightTableItemFromLeftDiff(TableItem[] rightItems, Diff leftDiff,
			IMergeViewerItem leftData) {
 		TableItem ret = null;
 		for (int i = 0; i < rightItems.length && ret == null; i++) {
 			TableItem rightItem = rightItems[i];
			IMergeViewerItem rightData = (IMergeViewerItem)rightItem.getData();
 			final Diff rightDiff = ((IMergeViewerItem)rightItem.getData()).getDiff();
 			if (leftDiff == rightDiff) {
 				ret = rightItem;
			} else if (Objects.equal(rightData.getAncestor(), leftData.getAncestor())
					&& Objects.equal(rightData.getRight(), leftData.getRight())
					&& Objects.equal(rightData.getLeft(), leftData.getLeft())) {
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

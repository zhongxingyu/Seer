 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.viewerpart;
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.ui.util.EMFAdapterFactoryProvider;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * Represents the property view under a {@link ModelContentMergeViewerPart}'s property
  * tab.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelContentMergePropertyPart extends TableViewer {
 	private int partSide;
 	
 	/**
 	 * Creates a table viewer on a newly-created table control under the given
 	 * parent. The table control is created using the given style bits.
 	 * 
 	 * @param parent
 	 * 			the parent control.
 	 * @param style
 	 * 			SWT style bits.
 	 * @param side
 	 * 			Sideof this viewer part.
 	 */
 	public ModelContentMergePropertyPart(Composite parent, int style, int side) {
 		super(parent, style);
 		partSide = side;
 		
 		setLabelProvider(new PropertyLabelProvider(EMFAdapterFactoryProvider.getAdapterFactory()));
 
 		setUseHashlookup(true);
 		getTable().setLinesVisible(true);
 		getTable().setHeaderVisible(true);
 		
 		final GC gc = new GC(getTable());
 		gc.setFont(getTable().getFont());
 		final FontMetrics metrics = gc.getFontMetrics();
 		gc.dispose();
 
 		final TableColumn nameColumn = new TableColumn(getTable(), SWT.LEFT);
 		nameColumn.setText("Attribute Name"); //$NON-NLS-1$
 		nameColumn.setWidth(Dialog.convertWidthInCharsToPixels(metrics, nameColumn.getText().length() * 3));
 		final TableColumn weightsColumn = new TableColumn(getTable(), SWT.RIGHT);
 		weightsColumn.setText("Value"); //$NON-NLS-1$
 		weightsColumn.setWidth(Dialog.convertWidthInCharsToPixels(metrics, weightsColumn.getText().length() * 3));
		
		getTable().addPaintListener(new PropertyTablePaintListener());
 	}
 	
 	/**
 	 * Returns the side of this viewer part.
 	 * 
 	 * @return
 	 * 			The side of this viewer part.
 	 */
 	public int getSide() {
 		return partSide;
 	}
 	
 	/**
 	 * Returns the widget representing the given {@link DiffElement} in the table.
 	 * 
 	 * @param diff
 	 * 			{@link DiffElement} to seek in the table.
 	 * @return
 	 * 			The widget representing the given {@link DiffElement}.
 	 * @see org.eclipse.jface.viewers.StructuredViewer#findItem(Object)
 	 */
 	public TableItem find(DiffElement diff) {
 		TableItem item = null;
 		if (diff instanceof AttributeChange) {
 			item = (TableItem)findItem(((AttributeChange)diff).getAttribute());
 		}
 		return item;
 	}
 
 	/**
 	 * Returns the widget representing the given {@link EAttribute} in the table.
 	 * 
 	 * @param attr
 	 * 			{@link EAttribute} to seek in the table.
 	 * @return
 	 * 			The widget representing the given {@link EAttribute}.
 	 * @see org.eclipse.jface.viewers.StructuredViewer#findItem(Object)
 	 */
 	public TableItem find(EAttribute attr) {
 		return (TableItem)findItem(attr);
 	}
 	
 	/**
 	 * Ensures that the given diff is visible in the table.
 	 * 
 	 * @param diff
 	 * 			{@link DiffElement} to make visible.
 	 */
 	public void showItem(DiffElement diff) {
 		final TableItem elementItem = find(diff);
 		if (elementItem != null) {
 			getTable().setSelection(elementItem);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#associate(Object, Item)
 	 */
 	@Override
 	protected void associate(Object element, Item item) {
 		if (element instanceof Object[]) {
 			final Object data = item.getData();
 			if (data != ((Object[])element)[0]) {
 				if (data != null) {
 					disassociate(item);
 				}
 				item.setData(((Object[])element)[0]);
 			}
 			// Always map the element, even if data == ((Object[])element)[0],
 			// since unmapAllElements() can leave the map inconsistent
 			// See bug 2741 for details.
 			mapElement(((Object[])element)[0], item);
 		} else {
 			if (element instanceof List) {
 				final Object data = item.getData();
 				if (data != ((List)element).get(0)) {
 					if (data != null) {
 						disassociate(item);
 					}
 					item.setData(((List)element).get(0));
 				}
 				// Always map the element, even if data == ((List)element).get(0),
 				// since unmapAllElements() can leave the map inconsistent
 				// See bug 2741 for details.
 				mapElement(((List)element).get(0), item);
 			} else {
 				super.associate(element, item);
 			}
 		}
 	}
 	
 	/**
 	 * Label provider used by the table control of this part.
 	 */
 	private class PropertyLabelProvider extends AdapterFactoryLabelProvider {
 		/**
 		 * Instantiates this label provider given its {@link AdapterFactory}.
 		 * 
 		 * @param adapterFactory
 		 * 			Adapter factory providing this {@link LabelProvider}'s text and
 		 * 			images.
 		 */
 		public PropertyLabelProvider(AdapterFactory adapterFactory) {
 			super(adapterFactory);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see AdapterFactoryLabelProvider#getColumnText(Object, int)
 		 */
 		public String getColumnText(Object object, int columnIndex) {
 			String text = super.getColumnText(object, columnIndex);
 			if (object instanceof List) {
 				text = super.getColumnText(((List)object).get(columnIndex), columnIndex);
 			}
 			return text;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see AdapterFactoryLabelProvider#getColumnImage(Object, int)
 		 */
 		public Image getColumnImage(Object object, int columnIndex) {
 			Image image = super.getColumnImage(object, columnIndex);
 			if (object instanceof List) {
 				image = super.getColumnImage(((List)object).get(columnIndex), columnIndex);
 
 			}
 			return image;
 		}
 	}
	
	/**
	 * This implementation of {@link PaintListener} handles the drawing of
	 * blocks around properties.
	 */
	private class PropertyTablePaintListener implements PaintListener {
		public void paintControl(PaintEvent event) {
			//TODO LGT handle property painting
		}
	}
 }

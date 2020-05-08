 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.internal;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.StyledCellLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.StyledString.Styler;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.te.ui.interfaces.IFilteringLabelDecorator;
 import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
 
 /**
  * An wrapping decorating label provider to replace the default navigator decorating label provider
  * in order to provide the filtering decoration.
  */
 @SuppressWarnings("restriction")
 public class ViewViewerDecoratingLabelProvider extends NavigatorDecoratingLabelProvider {
 	// The navigator's tree viewer to be decorated.
 	private TreeViewer viewer;
 	
 	/**
 	 * Create an instance with the tree viewer and a common label provider.
 	 * 
 	 * @param viewer The navigator's tree viewer.
 	 * @param commonLabelProvider The navigator's common label provider.
 	 */
 	public ViewViewerDecoratingLabelProvider(TreeViewer viewer, ILabelProvider commonLabelProvider) {
 	    super(commonLabelProvider);
 	    this.viewer = viewer;
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider#getText(java.lang.Object)
 	 */
 	@Override
     public String getText(Object element) {
	    String text = super.getText(element);
 		IFilteringLabelDecorator decorator = getFilteringDecorator(element);
 		if (decorator != null && decorator.isEnabled(viewer, element)) {
 			return decorator.decorateText(text, element);
 		}
 		return text;
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#getImage(java.lang.Object)
 	 */
 	@Override
     public Image getImage(Object element) {
 		Image image = super.getImage(element);
 		if (image != null) {
 			IFilteringLabelDecorator decorator = getFilteringDecorator(element);
 			if (decorator != null && decorator.isEnabled(viewer, element)) {
 				return decorator.decorateImage(image, element);
 			}
 		}
 		return image;
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#getStyledText(java.lang.Object)
 	 */
 	@Override
     protected StyledString getStyledText(Object element) {
 		StyledString styledString = super.getStyledText(element);
 		IFilteringLabelDecorator decorator = getFilteringDecorator(element);
 		if (decorator != null && decorator.isEnabled(viewer, element)) {
 			String decorated = decorator.decorateText(styledString.getString(), element);
 			Styler style = getDecorationStyle(element);
 			return StyledCellLabelProvider.styleDecoratedString(decorated, style, styledString);
 		}
 	    return styledString;
     }
 
 	/**
 	 * Get an adapter of IFilteringLabelProvider from the specified element.
 	 * 
 	 * @param element The element to get the adapter from.
 	 * @return The element's adapter or null if does not adapt to IFilteringLabelProvider.
 	 */
 	private IFilteringLabelDecorator getFilteringDecorator(Object element) {
 		IFilteringLabelDecorator decorator = null;
 		if(element instanceof IFilteringLabelDecorator) {
 			decorator = (IFilteringLabelDecorator) element;
 		}
 		if(decorator == null && element instanceof IAdaptable) {
 			decorator = (IFilteringLabelDecorator) ((IAdaptable)element).getAdapter(IFilteringLabelDecorator.class);
 		}
 		if(decorator == null) {
 			decorator = (IFilteringLabelDecorator) Platform.getAdapterManager().getAdapter(element, IFilteringLabelDecorator.class);
 		}
 		return decorator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider#getColumnImage(java.lang.Object, int)
 	 */
 	@Override
     public Image getColumnImage(Object element, int columnIndex) {
 		Image image = super.getColumnImage(element, columnIndex);
 		if (image != null && columnIndex == 0) {
 			IFilteringLabelDecorator decorator = getFilteringDecorator(element);
 			if (decorator != null && decorator.isEnabled(viewer, element)) {
 				return decorator.decorateImage(image, element);
 			}
 		}
 		return image;
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider#getColumnText(java.lang.Object, int)
 	 */
 	@Override
     public String getColumnText(Object element, int columnIndex) {
 		String text = super.getColumnText(element, columnIndex);
 		if (columnIndex == 0) {
 			IFilteringLabelDecorator decorator = getFilteringDecorator(element);
 			if (decorator != null && decorator.isEnabled(viewer, element)) {
 				return decorator.decorateText(text, element);
 			}
 		}
 		return text;
     }
 }

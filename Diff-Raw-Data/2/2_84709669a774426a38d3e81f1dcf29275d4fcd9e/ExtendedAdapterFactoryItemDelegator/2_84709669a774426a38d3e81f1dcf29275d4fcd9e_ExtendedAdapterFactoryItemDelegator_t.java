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
 package org.eclipse.emf.compare.provider;
 
 import static com.google.common.collect.Iterables.isEmpty;
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.provider.utils.ComposedStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString.IComposedStyledString;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 
 /**
  * An item provider adapter factory delegator that supports our custom item provider interfaces:
  * {@link IItemStyledLabelProvider} and {@link IItemDescriptionProvider}.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ExtendedAdapterFactoryItemDelegator extends AdapterFactoryItemDelegator implements IItemStyledLabelProvider, IItemDescriptionProvider {
 
 	/**
 	 * Creates a new instance that will use the given adapter factory to respond to its implemented protocol.
 	 * 
 	 * @param adapterFactory
 	 *            the adapter factory use to adapt.
 	 */
 	public ExtendedAdapterFactoryItemDelegator(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemDescriptionProvider#getDescription(java.lang.Object)
 	 */
 	public String getDescription(Object object) {
 		return getText(object);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemStyledLabelProvider#getStyledText(java.lang.Object)
 	 */
 	public IComposedStyledString getStyledText(Object object) {
 		final IComposedStyledString result;
 		if (object instanceof EList<?>) {
 			result = new ComposedStyledString();
 			for (Object child : (List<?>)object) {
 				if (!isEmpty(result)) {
 					result.append(", ");
 				}
 				IComposedStyledString styledText = getStyledText(child);
 				for (IStyledString styledString : styledText) {
 					result.append(styledString.getString(), styledString.getStyle());
 				}
 			}
 		} else {
 			IItemStyledLabelProvider itemStyledLabelProvider = (IItemStyledLabelProvider)adapterFactory
 					.adapt(object, IItemStyledLabelProvider.class);
 
 			if (itemStyledLabelProvider != null) {
 				result = itemStyledLabelProvider.getStyledText(object);
 			} else if (object == null) {
 				result = new ComposedStyledString();
 			} else {
				result = new ComposedStyledString(getText(object));
 			}
 		}
 		return result;
 	}
 }

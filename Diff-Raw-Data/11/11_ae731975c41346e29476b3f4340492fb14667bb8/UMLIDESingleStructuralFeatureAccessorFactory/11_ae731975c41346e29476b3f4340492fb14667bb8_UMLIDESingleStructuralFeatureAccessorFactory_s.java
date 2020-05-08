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
 package org.eclipse.emf.compare.uml2.ide.ui.internal.accessor.factory;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.IAccessorFactory.AbstractAccessorFactory;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.uml2.UMLDiff;
 import org.eclipse.emf.compare.uml2.ide.ui.internal.accessor.UMLIDESingleStructuralFeatureAccessor;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class UMLIDESingleStructuralFeatureAccessorFactory extends AbstractAccessorFactory {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.IAccessorFactory#isFactoryFor(java.lang.Object)
 	 */
 	public boolean isFactoryFor(Object target) {
		return target instanceof UMLDiff && !((UMLDiff)target).getEReference().isMany();
 	}
 
 	private ITypedElement createAccessor(UMLDiff diff, MergeViewerSide side) {
 		ITypedElement ret = null;
 		switch (diff.getKind()) {
 			case ADD:
 			case DELETE:
 			case MOVE:
 				ret = new UMLIDESingleStructuralFeatureAccessor(diff, side);
 				break;
 			case CHANGE:
 				// TODO: what to do in change ?
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.IAccessorFactory#createLeft(org.eclipse.emf.common.notify.AdapterFactory,
 	 *      java.lang.Object)
 	 */
 	public ITypedElement createLeft(AdapterFactory adapterFactory, Object target) {
 		return createAccessor((UMLDiff)target, MergeViewerSide.LEFT);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.IAccessorFactory#createRight(org.eclipse.emf.common.notify.AdapterFactory,
 	 *      java.lang.Object)
 	 */
 	public ITypedElement createRight(AdapterFactory adapterFactory, Object target) {
 		return createAccessor((UMLDiff)target, MergeViewerSide.RIGHT);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.IAccessorFactory#createAncestor(org.eclipse.emf.common.notify.AdapterFactory,
 	 *      java.lang.Object)
 	 */
 	public ITypedElement createAncestor(AdapterFactory adapterFactory, Object target) {
 		return createAccessor((UMLDiff)target, MergeViewerSide.ANCESTOR);
 	}
 
 }

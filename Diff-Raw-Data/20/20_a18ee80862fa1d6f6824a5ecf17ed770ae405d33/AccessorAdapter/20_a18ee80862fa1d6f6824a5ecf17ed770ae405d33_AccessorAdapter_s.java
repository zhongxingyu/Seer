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
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public final class AccessorAdapter implements ITypedElement, IStreamContentAccessor {
 
 	private final Object target;
 
 	private AccessorAdapter(Object target) {
 		this.target = target;
 	}
 
 	/**
 	 * 
 	 */
 	public Object getTarget() {
 		return target;
 	}
 
 	public static AccessorAdapter adapt(Object target) {
 		return new AccessorAdapter(target);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.IStreamContentAccessor#getContents()
 	 */
 	public InputStream getContents() throws CoreException {
 		if (target instanceof org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.IStreamContentAccessor) {
 			return ((org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.IStreamContentAccessor)target)
 					.getContents();
 		}
 		/*
 		 * #293926 : Whatever we return has no importance as long as it is not "null", this is only to make
 		 * CompareUIPlugin#guessType happy. However, it is only happy if what we return resembles a text. Note
 		 * that this bug has been fixed in 3.7.1, we're keeping this around for the compatibility with 3.5 and
 		 * 3.6.
 		 */
		return new ByteArrayInputStream(new byte[] {' ' });
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getName()
 	 */
 	public String getName() {
 		if (target instanceof org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement) {
 			return ((org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement)target)
 					.getName();
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getImage()
 	 */
 	public Image getImage() {
 		if (target instanceof org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement) {
 			return ((org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement)target)
 					.getImage();
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getType()
 	 */
 	public String getType() {
 		if (target instanceof org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement) {
 			return ((org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement)target)
 					.getType();
 		}
 		return null;
 	}
 
 }

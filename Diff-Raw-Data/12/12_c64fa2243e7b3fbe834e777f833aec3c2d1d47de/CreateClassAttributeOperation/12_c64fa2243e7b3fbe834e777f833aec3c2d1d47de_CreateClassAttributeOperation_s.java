 /**
  * Copyright (c) 2009 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle Corporation - initial API and implementation
  */
 package org.eclipse.jst.jsf.apache.trinidad.tagsupport.converter.operations;
 
 import org.w3c.dom.Element;
 
 /**
  * ITransformOperation implementation to handle merging of an Element's optional
  * default style class with a specified "styleClass" as the current Element's
  * "class" attribute.
  * 
  * @author Ian Trimble - Oracle
  */
 public class CreateClassAttributeOperation extends AbstractTrinidadTransformOperation {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.operations.AbstractTransformOperation#transform(org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	@Override
 	public Element transform(Element srcElement, Element curElement) {
 		String classValue = null;
 		if (getParameters().length > 0) {
 			classValue = getParameters()[0];
 		}
 		String styleClass = srcElement.getAttribute("styleClass"); //$NON-NLS-1$
 		if (styleClass != null && styleClass.length() > 0) {
 			if (classValue == null) {
 				classValue = styleClass;
 			} else {
				if (classValue.length() > 0 && !classValue.endsWith(",")) { //$NON-NLS-1$
					classValue += ","; //$NON-NLS-1$
				}
				classValue += styleClass;
 			}
 		}
 		if (classValue != null && curElement != null) {
 			curElement.setAttribute("class", classValue); //$NON-NLS-1$
 		}
 		return curElement;
 	}
 
 }

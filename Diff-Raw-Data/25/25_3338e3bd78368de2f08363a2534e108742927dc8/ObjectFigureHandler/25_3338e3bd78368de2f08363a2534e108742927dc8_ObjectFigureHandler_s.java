 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.figurehandler;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jst.pagedesigner.editors.palette.TagImageManager;
 import org.w3c.dom.Element;
 
 /**
  * @author mengbo
  * @version 1.5
  */
 /*package*/ class ObjectFigureHandler extends ImgFigureHandler {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.figurehandler.ImgFigureHandler#initializeImage()
 	 */
 	protected void initializeImage(Element node) {
 		if (_image == null) {
			//FIXME - file/project should NOT be null!
			_image = TagImageManager.getInstance().getSmallIconImage((IFile)null, "HTML", node.getTagName()); //$NON-NLS-1$
 		}
 	}
 }

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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jst.pagedesigner.editors.palette.TagImageManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
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
			if (node instanceof IDOMNode) {
				final IDOMModel model = ((IDOMNode)node).getModel();
				if (model != null) {
					final String location = model.getBaseLocation();
					if (location != null) {
						final IWorkspace workspace = ResourcesPlugin.getWorkspace();
						if (workspace != null) {
							final IResource resource = workspace.getRoot().findMember(location);
							if (resource.getType() == IResource.FILE) {
								_image = TagImageManager.getInstance().getSmallIconImage(
										(IFile)resource, "HTML", node.getTagName()); //$NON-NLS-1$
							}
						}
					}
				}
			}
 		}
 	}
 }

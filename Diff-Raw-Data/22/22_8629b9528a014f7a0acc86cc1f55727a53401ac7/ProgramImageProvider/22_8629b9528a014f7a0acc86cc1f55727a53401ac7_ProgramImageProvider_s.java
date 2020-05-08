 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.columns;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 import org.eclipse.tcf.te.ui.activator.UIPlugin;
 
 /**
  * An image provider using the SWT Program class to retrieve system icons for
  * executables. This image provider is used before a platform specific image
  * provider to find the images. If it can not provide the images, then it falls
  * back to the platform specific image providers.
  */
 public class ProgramImageProvider extends DefaultImageProvider {
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.tcf.te.tcf.filesystem.internal.columns.DefaultImageProvider#getImage(org.eclipse
 	 * .tcf.te.tcf.filesystem.model.FSTreeNode)
 	 */
 	@Override
 	public Image getImage(FSTreeNode node) {
 		if (node.isFile()) {
 			String name = node.name;
			String ending = name.substring(name.lastIndexOf('.'));
			String key = "EXT_IMAGE" + ending; //$NON-NLS-1$
			Image image = UIPlugin.getImage(key);
			Program program = Program.findProgram(ending);
			if (image == null && program != null) {
				ImageData iconData = program.getImageData();
				image = new Image(Display.getCurrent(), iconData);
				UIPlugin.getDefault().getImageRegistry().put(key, image);
 			}
			return image;
 		}
 		return null;
 	}
 }

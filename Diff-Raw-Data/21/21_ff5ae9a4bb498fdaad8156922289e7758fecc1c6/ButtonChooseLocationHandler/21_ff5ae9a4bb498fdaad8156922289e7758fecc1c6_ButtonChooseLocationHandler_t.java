 /*******************************************************************************
  * Copyright (c) 2011 Robert Wloch and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Robert Wloch - initial API and implementation
  *******************************************************************************/
 package de.rowlo.diffeclipse.event;
 
import java.io.File;

 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * 
  * @author Robert Wloch (robert@rowlo.de)
  */
 public class ButtonChooseLocationHandler extends AbstractDirectoryButtonSelectionHandler {
 
 	public ButtonChooseLocationHandler(Button button, Text text) {
 		super(button, text);
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent se) {
 		super.widgetSelected(se);
 
 		if (getButton() != null && getButton().equals(se.getSource()) && getText() != null) {
 			final Shell shell = getButton().getShell();
 			Display display = shell.getDisplay();
 
 			display.syncExec(new Runnable() {
 				@Override
 				public void run() {
 					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
 					Text text = getText();
 					String lastChoosenPath = text.getText();
 					if (lastChoosenPath == null || lastChoosenPath.isEmpty()) {
 						lastChoosenPath = getLastChoosenPath();
 						if (lastChoosenPath == null || lastChoosenPath.isEmpty()) {
 							lastChoosenPath = "/";
 							String platform = SWT.getPlatform();
 							if (platform.equals("win32") || platform.equals("wpf")) {
 								lastChoosenPath = "c:\\";
 							}
 						}
 					}
 					dialog.setFilterPath(lastChoosenPath);
 
 					String path = dialog.open();
 					if (path != null) {
						File fPath = new File(path);
						String parentPath = fPath.getParent();
						if (parentPath != null)
						{
							setLastChoosenPath(parentPath);
						}
						else
						{
							setLastChoosenPath(path);
						}
 						text.setText(path);
 					}
 				}
 			});
 
 		}
 	}
 
 }

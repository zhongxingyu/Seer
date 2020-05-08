 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.launch.ui.editor;
 
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
 
 /**
  * Source lookup launch configuration tab container page implementation.
  */
 public class SourceLookupEditorPage extends AbstractLaunchTabContainerEditorPage {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#createLaunchConfigurationTab()
 	 */
 	@Override
 	protected AbstractLaunchConfigurationTab createLaunchConfigurationTab() {
		return new SourceLookupTab() {
			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.sourcelookup.SourceLookupTab#createControl(org.eclipse.swt.widgets.Composite)
			 */
			@Override
			public void createControl(Composite parent) {
			    super.createControl(parent);
			    ((Composite)getControl()).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}
		};
 	}
 
 }

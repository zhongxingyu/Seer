 /*******************************************************************************
  * Copyright (c) 2011 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylyn.internal.tasks.ui.preferences;
 
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * @author Steffen Pingel
  */
 public class MylynPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
 
 	public MylynPreferencePage() {
 		super(Messages.MylynPreferencePage_Mylyn_Title);
 		setDescription(Messages.MylynPreferencePage_General_settings_Description);
 	}
 
 	public void init(IWorkbench workbench) {
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

//		Label label = new Label(composite, SWT.NONE);
//		label.setText("See sub-pages for settings.");

 		return composite;
 	}
 }

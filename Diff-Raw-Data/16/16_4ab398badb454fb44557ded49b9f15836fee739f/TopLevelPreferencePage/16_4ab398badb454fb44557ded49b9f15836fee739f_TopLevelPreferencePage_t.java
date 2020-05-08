 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
 * $Id$
  */
 package net.sf.rcer.conn.ui.preferences;
 
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import com.sap.conn.jco.JCo;
 
 /**
  * A top-level preference page that just shows some general information.
  * @author vwegert
  *
  */
 public class TopLevelPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
 
 	/**
 	 * Default constructor.
 	 */
 	public TopLevelPreferencePage() {
 		super();
 		setDescription("Use the detail pages to set the preferences of the connector.");
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite top = new Composite(parent, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(top);
 		
 		Label label = new Label(top, SWT.NONE);
 		label.setText("JCo Version:");
 		
 		label = new Label(top, SWT.NONE);
 		label.setText(JCo.getVersion());
 		
 		return top;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 		// nothing to do
 	}
 
 }

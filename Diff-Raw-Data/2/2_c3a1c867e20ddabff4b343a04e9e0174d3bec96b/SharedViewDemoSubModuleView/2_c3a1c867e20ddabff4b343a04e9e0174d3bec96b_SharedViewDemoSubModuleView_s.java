 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.example.client.controllers.SharedViewDemoSubModuleController;
 import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
 import org.eclipse.riena.ui.swt.DefaultButtonManager;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Demonstrates shared views (i.e. one view instance, with several distrinct
  * controllers and data).
  */
 public class SharedViewDemoSubModuleView extends SubModuleView<SharedViewDemoSubModuleController> {
 
 	public static final String ID = SharedViewDemoSubModuleView.class.getName();
 	private static List<SharedViewDemoSubModuleView> instances = new ArrayList<SharedViewDemoSubModuleView>();
 
 	private int instanceIndex = 0;
 
 	public SharedViewDemoSubModuleView() {
 		instances.add(this);
 		instanceIndex = instances.size();
 	}
 
 	@Override
 	public void basicCreatePartControl(Composite parent) {
 		parent.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND));
 
 		GridLayoutFactory.fillDefaults().numColumns(2).margins(20, 20).applyTo(parent);
 
 		String text = String.format("(Instance %d Data)", instanceIndex); //$NON-NLS-1$
 		Label lblInfo = UIControlsFactory.createLabel(parent, text);
 		GridDataFactory.fillDefaults().span(2, 1).applyTo(lblInfo);
 
 		UIControlsFactory.createLabel(parent, "&First Name:"); //$NON-NLS-1$
 		Text txtFirst = UIControlsFactory.createText(parent, SWT.SINGLE, "txtFirst"); //$NON-NLS-1$
 		GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(txtFirst);
 
 		UIControlsFactory.createLabel(parent, "&Last Name:"); //$NON-NLS-1$
 		Text txtLast = UIControlsFactory.createText(parent, SWT.SINGLE, "txtLast"); //$NON-NLS-1$
 		GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(txtLast);
 
 		Button button = UIControlsFactory.createButton(parent, "Default Button"); // TODO [ev] remove
 		DefaultButtonManager dmb = new DefaultButtonManager(parent.getShell());
		dmb.addButton(button, parent);
 	}
 
 	@Override
 	public void setFocus() {
 		super.setFocus();
 	}
 
 }

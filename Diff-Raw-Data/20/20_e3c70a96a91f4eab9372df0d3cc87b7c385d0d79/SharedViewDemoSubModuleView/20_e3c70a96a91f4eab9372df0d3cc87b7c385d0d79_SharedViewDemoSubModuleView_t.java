 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
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
 
 import org.eclipse.riena.example.client.controllers.SharedViewDemoSubModuleController;
 import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class SharedViewDemoSubModuleView extends SubModuleView<SharedViewDemoSubModuleController> {
 
 	public static final String ID = SharedViewDemoSubModuleView.class.getName();
 	private static List<SharedViewDemoSubModuleView> instances = new ArrayList<SharedViewDemoSubModuleView>();
 
 	private static final int FIELD_WIDTH = 100;
 	private static final int TOP = 10;
 	private static final int LEFT = 10;
 	private static final int LABEL_WIDTH = 90;
 
 	private int instanceIndex = 0;
 
 	public SharedViewDemoSubModuleView() {
 
 		instances.add(this);
 		instanceIndex = instances.size();
 	}
 
 	/**
 	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void basicCreatePartControl(Composite parent) {
 		parent.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.SUB_MODULE_BACKGROUND));
 		final Label helloLabel = UIControlsFactory.createLabel(parent, "", SWT.CENTER); //$NON-NLS-1$
 
 		// layout
 		FormLayout layout = new FormLayout();
 		parent.setLayout(layout);
 		addUIControl(helloLabel, "labelFacade"); //$NON-NLS-1$
 		// getController().setLabelFacade(labelFacade);
 
 		Label someText = UIControlsFactory.createLabel(parent, "(Instance " + instanceIndex + ") Data", SWT.LEFT); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
 		FormData fd = new FormData();
 		fd.top = new FormAttachment(0, TOP);
 		fd.left = new FormAttachment(0, LEFT);
 		someText.setLayoutData(fd);
 
 		Text someData = new Text(parent, SWT.BORDER | SWT.SINGLE);
 		fd = new FormData();
 		fd.top = new FormAttachment(someText, 0, SWT.TOP);
 		fd.left = new FormAttachment(someText, LABEL_WIDTH, SWT.LEFT);
 		fd.width = FIELD_WIDTH;
 		someData.setLayoutData(fd);
 		// ta = new TextFieldRidget(someData);
 		addUIControl(someData, "textFacade"); //$NON-NLS-1$
 		// getController().setTextFacade(ta);
 		// layout
 		FormData data = new FormData();
 		data.top = new FormAttachment(0, 45);
 		data.left = new FormAttachment(someText, LABEL_WIDTH, SWT.LEFT);
 		helloLabel.setLayoutData(data);
 	}
 
 	@Override
 	public void setFocus() {
 		super.setFocus();
 		// FIXME implement generic way to rebind controllers. just for
 		// evaluation.
 		// if (ta != null && getController() != null) {
 		// getController().setLabelFacade(labelFacade);
 		// getController().setTextFacade(ta);
 		// }
 		// getController().afterBind();
 	}
 
 }

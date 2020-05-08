 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.demo;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.wazaabi.coderesolution.reflection.java.codelocators.nonosgi.ReflectionJavaHelper;
 import org.eclipse.wazaabi.engine.swt.nonosgi.SWTHelper;
 import org.eclipse.wazaabi.engine.swt.viewers.SWTControlViewer;
 import org.eclipse.wazaabi.mm.core.styles.BooleanRule;
 import org.eclipse.wazaabi.mm.core.styles.ColorRule;
 import org.eclipse.wazaabi.mm.core.styles.CoreStylesFactory;
 import org.eclipse.wazaabi.mm.core.styles.ExpandRule;
 import org.eclipse.wazaabi.mm.core.styles.FontRule;
 import org.eclipse.wazaabi.mm.core.styles.HyperlinkRule;
 import org.eclipse.wazaabi.mm.core.styles.ImageRule;
 import org.eclipse.wazaabi.mm.core.styles.TabRule;
 import org.eclipse.wazaabi.mm.core.styles.TabbedLayoutRule;
 import org.eclipse.wazaabi.mm.core.widgets.Container;
 import org.eclipse.wazaabi.mm.core.widgets.CoreWidgetsFactory;
 import org.eclipse.wazaabi.mm.core.widgets.CoreWidgetsPackage;
 import org.eclipse.wazaabi.mm.core.widgets.Label;
 import org.eclipse.wazaabi.mm.core.widgets.PushButton;
 import org.eclipse.wazaabi.mm.core.widgets.RadioButton;
 import org.eclipse.wazaabi.mm.core.widgets.TextComponent;
 import org.eclipse.wazaabi.mm.edp.events.EDPEventsFactory;
 import org.eclipse.wazaabi.mm.edp.events.Event;
 import org.eclipse.wazaabi.mm.edp.handlers.Action;
 import org.eclipse.wazaabi.mm.edp.handlers.EDPHandlersFactory;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 import org.eclipse.wazaabi.mm.swt.styles.FillLayoutRule;
 import org.eclipse.wazaabi.mm.swt.styles.GridDataAlignment;
 import org.eclipse.wazaabi.mm.swt.styles.GridDataRule;
 import org.eclipse.wazaabi.mm.swt.styles.GridLayoutRule;
 import org.eclipse.wazaabi.mm.swt.styles.RowLayoutRule;
 import org.eclipse.wazaabi.mm.swt.styles.SWTStylesFactory;
 
 public class DemoContainer {
 
 	public static void main(String[] args) {
 
 		// init SWT Engine in standalone mode
 		SWTHelper.init();
 		ReflectionJavaHelper.init();
 		
 		final String tabImageURI = "urn:java:tabb.png";
 
 		// create the shell
 		Display display = new Display();
 		Shell mainShell = new Shell(display, SWT.SHELL_TRIM);
 		mainShell.setLayout(new FillLayout());
 		mainShell.setSize(400, 560);
 
 		// create the viewer
 		SWTControlViewer viewer = new SWTControlViewer(mainShell);
 		Container rootContainer = CoreWidgetsFactory.eINSTANCE.createContainer();
 		GridLayoutRule fillLayout = SWTStylesFactory.eINSTANCE.createGridLayoutRule();
 		fillLayout.setPropertyName("layout");
 		rootContainer.getStyleRules().add(fillLayout);
 		
 		// Create the button
 		Container buttonContainer = CoreWidgetsFactory.eINSTANCE.createContainer();
 		FillLayoutRule buttonLayout = SWTStylesFactory.eINSTANCE.createFillLayoutRule();
 		buttonLayout.setPropertyName("layout");
 		buttonContainer.getStyleRules().add(buttonLayout);
 		
 		PushButton toggleButton = CoreWidgetsFactory.eINSTANCE.createPushButton();
 		toggleButton.setText("Toggle layout");
 		buttonContainer.getChildren().add(toggleButton);
 		
 		EventHandler eventHandler = EDPHandlersFactory.eINSTANCE.createEventHandler();
 		
 		Action action = EDPHandlersFactory.eINSTANCE.createAction();
 		action.setUri("urn:java:org.eclipse.wazaabi.engine.swt.demo.ToggleDemoAction");
 		toggleButton.getHandlers().add(eventHandler);		
 
 		Event event = EDPEventsFactory.eINSTANCE.createEvent();
 		eventHandler.getEvents().add(event);
 		eventHandler.getExecutables().add(action);
 		event.setId("core:ui:selection");
 
 		// create a container that will be the tabbed container
 		
 		Container mainContainer = CoreWidgetsFactory.eINSTANCE.createContainer();
 		
 		GridDataRule gridData = SWTStylesFactory.eINSTANCE.createGridDataRule();
 		gridData.setPropertyName("layout-data");
 		gridData.setGrabExcessVerticalSpace(true);
 		gridData.setGrabExcessHorizontalSpace(true);
 		gridData.setMinimumHeight(500);
 		gridData.setMinimumWidth(390);
 		mainContainer.getStyleRules().add(gridData);
 		
 		TabbedLayoutRule tabLayout = CoreStylesFactory.eINSTANCE.createTabbedLayoutRule();
 		tabLayout.setPropertyName("layout");
		tabLayout.setTop(1);
 		mainContainer.getStyleRules().add(tabLayout);
 		
 		
 		// first tab container
 		TabRule tabStyle1 = CoreStylesFactory.eINSTANCE.createTabRule();
 		tabStyle1.setPropertyName("layout-data");
 		tabStyle1.setLabel("Welcome!");
 		tabStyle1.setImage(tabImageURI);
 		
 		ExpandRule expandStyle1 = CoreStylesFactory.eINSTANCE.createExpandRule();
 		expandStyle1.setPropertyName("layout-data");
 		expandStyle1.setLabel("Welcome!");
 		expandStyle1.setImage(tabImageURI);
 		
 		Container tab1 = CoreWidgetsFactory.eINSTANCE.createContainer();
 		tab1.getStyleRules().add(tabStyle1);
 		mainContainer.getChildren().add(tab1);
 		
 		GridLayoutRule gridLayout1 = SWTStylesFactory.eINSTANCE.createGridLayoutRule();
 		gridLayout1.setPropertyName("layout");
 		gridLayout1.setMarginLeft(40);
 		tab1.getStyleRules().add(gridLayout1);
 		
 		Label wazaabiLogoLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		ImageRule wazaabiLogoImage = CoreStylesFactory.eINSTANCE.createImageRule();
 		wazaabiLogoImage.setPropertyName("image");
 		wazaabiLogoImage.setValue("urn:java:wazaabi-logo.png");
 		wazaabiLogoLabel.getStyleRules().add(wazaabiLogoImage);
 		
 		tab1.getChildren().add(wazaabiLogoLabel);
 		
 		Label welcomeLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		welcomeLabel.setText("\n\nWazaabi wishes you welcome!");
 		tab1.getChildren().add(welcomeLabel);
 		
 		FontRule welcomeLabelFont = CoreStylesFactory.eINSTANCE.createFontRule();
 		welcomeLabelFont.setPropertyName("font");
 		welcomeLabelFont.setBold(true);
 		welcomeLabel.getStyleRules().add(welcomeLabelFont);
 		
 		ColorRule welcomeLabelColor = CoreStylesFactory.eINSTANCE.createColorRule();
 		welcomeLabelColor.setPropertyName("foreground-color");
 		welcomeLabelColor.setBlue(20);
 		welcomeLabelColor.setRed(200);
 		welcomeLabelColor.setGreen(50);
 		welcomeLabel.getStyleRules().add(welcomeLabelColor);
 		
 		Label websiteLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		HyperlinkRule wazaabiHyperlink = CoreStylesFactory.eINSTANCE.createHyperlinkRule();
 		wazaabiHyperlink.setPropertyName("lookandfeel");
 		websiteLabel.getStyleRules().add(wazaabiHyperlink);
 		websiteLabel.setText("\n\tFeel free to visit our <a> website </a> !");
 		tab1.getChildren().add(websiteLabel);
 	
 		// second tab container
 		ExpandRule expandStyle2 = CoreStylesFactory.eINSTANCE.createExpandRule();
 		expandStyle2.setPropertyName("layout-data");
 		expandStyle2.setLabel("Personal details");
 		expandStyle2.setImage(tabImageURI);
 		expandStyle2.setExpanded(true);
 		
 		TabRule tabStyle2 = CoreStylesFactory.eINSTANCE.createTabRule();
 		tabStyle2.setPropertyName("layout-data");
 		tabStyle2.setLabel("Personal details");
 		tabStyle2.setImage(tabImageURI);
 		
 		Container tab2 = CoreWidgetsFactory.eINSTANCE.createContainer();
 		mainContainer.getChildren().add(tab2);
 		tab2.getStyleRules().add(tabStyle2);
 		
 		GridLayoutRule gridLayout2 = SWTStylesFactory.eINSTANCE.createGridLayoutRule();
 		gridLayout2.setPropertyName("layout");
 		gridLayout2.setNumColumns(2);
 		tab2.getStyleRules().add(gridLayout2);
 		
 		Label sexLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		sexLabel.setText("Gender");
 		tab2.getChildren().add(sexLabel);
 
 		Container sexContainer = CoreWidgetsFactory.eINSTANCE.createContainer();
 		FillLayoutRule sexContainerFillLayout = SWTStylesFactory.eINSTANCE.createFillLayoutRule();
 		sexContainerFillLayout.setPropertyName("layout");
 		sexContainer.getStyleRules().add(sexContainerFillLayout);
 		tab2.getChildren().add(sexContainer);
 		
 		RadioButton maleRadioButton = CoreWidgetsFactory.eINSTANCE.createRadioButton();
 		maleRadioButton.setText("Female");
 		sexContainer.getChildren().add(maleRadioButton);
 
 		RadioButton femaleRadioButton = CoreWidgetsFactory.eINSTANCE.createRadioButton();
 		femaleRadioButton.setText("Male");
 		sexContainer.getChildren().add(femaleRadioButton);
 		
 		Label firstnameLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		firstnameLabel.setText("Firstname");
 		tab2.getChildren().add(firstnameLabel);
 		
 		TextComponent firstnameText = CoreWidgetsFactory.eINSTANCE.createTextComponent();
 		tab2.getChildren().add(firstnameText);
 		
 		GridDataRule firstnameGriddataRule =  SWTStylesFactory.eINSTANCE.createGridDataRule();
 		firstnameGriddataRule.setPropertyName("layout-data");
 		firstnameGriddataRule.setHorizontalAlignement(GridDataAlignment.FILL);
 		firstnameGriddataRule.setGrabExcessHorizontalSpace(true);
 		firstnameText.getStyleRules().add(firstnameGriddataRule);
 		
 		Label lastnameLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		lastnameLabel.setText("Lastname");
 		tab2.getChildren().add(lastnameLabel);
 		
 		TextComponent lastnameText = CoreWidgetsFactory.eINSTANCE.createTextComponent();
 		tab2.getChildren().add(lastnameText);
 		GridDataRule lastnameGriddataRule =  SWTStylesFactory.eINSTANCE.createGridDataRule();
 		lastnameGriddataRule.setPropertyName("layout-data");
 		lastnameGriddataRule.setHorizontalAlignement(GridDataAlignment.FILL);
 		lastnameGriddataRule.setGrabExcessHorizontalSpace(true);
 		lastnameText.getStyleRules().add(lastnameGriddataRule);
 
 		Label contactLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		contactLabel.setText("I allow you to get in contact with me by");
 		tab2.getChildren().add(contactLabel);
 		
 		Container contactContainer = CoreWidgetsFactory.eINSTANCE.createContainer();
 		RowLayoutRule contactContainerRowLayout = SWTStylesFactory.eINSTANCE.createRowLayoutRule();
 		contactContainerRowLayout.setPropertyName("layout");
 		contactContainer.getStyleRules().add(contactContainerRowLayout);
 		tab2.getChildren().add(contactContainer);
 		
 		GridDataRule contactGriddataRule =  SWTStylesFactory.eINSTANCE.createGridDataRule();
 		contactGriddataRule.setPropertyName("layout-data");
 		contactGriddataRule.setWidthHint(120);
 		contactContainer.getStyleRules().add(contactGriddataRule);
 		
 		RadioButton phoneRadio = CoreWidgetsFactory.eINSTANCE.createRadioButton();
 		phoneRadio.setText("Phone");
 		contactContainer.getChildren().add(phoneRadio);
 		
 		RadioButton mailRadio = CoreWidgetsFactory.eINSTANCE.createRadioButton();
 		mailRadio.setText("Phone");
 		contactContainer.getChildren().add(mailRadio);
 		
 		RadioButton pigeonRadio = CoreWidgetsFactory.eINSTANCE.createRadioButton();
 		pigeonRadio.setText("Carrier Pigeon");
 		contactContainer.getChildren().add(pigeonRadio);
 
 		Label commentsLabel = CoreWidgetsFactory.eINSTANCE.createLabel();
 		commentsLabel.setText("Comments");
 		tab2.getChildren().add(commentsLabel);
 		
 		GridDataRule commentsGriddataRule =  SWTStylesFactory.eINSTANCE.createGridDataRule();
 		commentsGriddataRule.setPropertyName("layout-data");
 		commentsGriddataRule.setHorizontalSpan(2);
 		commentsGriddataRule.setHorizontalAlignement(GridDataAlignment.FILL);
 		commentsGriddataRule.setGrabExcessHorizontalSpace(true);
 		commentsGriddataRule.setVerticalAlignement(GridDataAlignment.FILL);
 		commentsGriddataRule.setGrabExcessVerticalSpace(true);
 		contactContainer.getStyleRules().add(commentsGriddataRule);
 		
 		TextComponent commentsText = CoreWidgetsFactory.eINSTANCE.createTextComponent();
 		commentsText.getStyleRules().add(commentsGriddataRule);
 		tab2.getChildren().add(commentsText);
 		BooleanRule multilineComments =  CoreStylesFactory.eINSTANCE.createBooleanRule();
 		multilineComments.setPropertyName("multi-line");
 		multilineComments.setValue(true);
 		commentsText.getStyleRules().add(multilineComments);
 		
 		PushButton addMeButton = CoreWidgetsFactory.eINSTANCE.createPushButton();
 		addMeButton.setText("Add");
 		tab2.getChildren().add(addMeButton);
 		
 //		//third tab container
 //		TabRule tabStyle3 = CoreStylesFactory.eINSTANCE.createTabRule();
 //		tabStyle3.setPropertyName("layout-data");
 //		tabStyle3.setLabel("Wazaa!");
 //		tabStyle3.setImage(tabImageURI);
 //			
 //		Container tab3 = CoreWidgetsFactory.eINSTANCE.createContainer();
 //		rootContainer.getChildren().add(tab3);
 //		tab3.getStyleRules().add(tabStyle3);
 //		
 //		FillLayoutRule fillLayout3 = SWTStylesFactory.eINSTANCE.createFillLayoutRule();
 //		fillLayout3.setPropertyName("layout");
 //		tab3.getStyleRules().add(fillLayout3);
 		
 		
 		
 //		TextComponent text = CoreWidgetsFactory.eINSTANCE.createTextComponent();
 //		text.setText("This is a super textComponent");//$NON-NLS-1$
 //		Label label1 = CoreWidgetsFactory.eINSTANCE.createLabel();
 //		label1.setText("Text for item 1\n\none, two, three\n\nabcdefghijklmnop");//$NON-NLS-1$
 //		tab2.getChildren().add(text);
 //		tab2.getChildren().add(label1);
 		
 		rootContainer.getChildren().add(buttonContainer);
 		rootContainer.getChildren().add(mainContainer);
 		
 		// EMF requirements for standalone applications
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"ui", new XMIResourceFactoryImpl());
 		CoreWidgetsPackage.eINSTANCE.eClass();
 
 		// load the EMF resource
 		Resource resource = new ResourceSetImpl().createResource(URI
 				.createURI("models/DemoModel2.ui")); //$NON-NLS-1$
 		resource.getContents().add(rootContainer);
 
 		try {
 			resource.save(Collections.EMPTY_MAP);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		
 		viewer.setContents(rootContainer);
 		
 		//tabLayout.setTop(1);
 
 		mainShell.open();
 
 		while (!mainShell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 		display.dispose();
 	}
 }

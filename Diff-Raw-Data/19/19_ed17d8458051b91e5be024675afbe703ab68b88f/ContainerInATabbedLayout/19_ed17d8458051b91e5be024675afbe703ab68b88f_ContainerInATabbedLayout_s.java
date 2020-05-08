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
 
 package org.eclipse.wazaabi.engine.swt.snippets;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.wazaabi.coderesolution.reflection.java.codelocators.nonosgi.ReflectionJavaHelper;
 import org.eclipse.wazaabi.engine.swt.nonosgi.SWTHelper;
 import org.eclipse.wazaabi.engine.swt.viewers.SWTControlViewer;
 import org.eclipse.wazaabi.mm.core.Position;
 import org.eclipse.wazaabi.mm.core.styles.CoreStylesFactory;
 import org.eclipse.wazaabi.mm.core.styles.FontRule;
 import org.eclipse.wazaabi.mm.core.styles.TabRule;
 import org.eclipse.wazaabi.mm.core.styles.TabbedLayoutRule;
 import org.eclipse.wazaabi.mm.core.widgets.Container;
 import org.eclipse.wazaabi.mm.core.widgets.CoreWidgetsFactory;
 import org.eclipse.wazaabi.mm.core.widgets.Label;
 import org.eclipse.wazaabi.mm.core.widgets.PushButton;
 import org.eclipse.wazaabi.mm.core.widgets.TextComponent;
 import org.eclipse.wazaabi.mm.swt.styles.FillLayoutRule;
 import org.eclipse.wazaabi.mm.swt.styles.SWTStylesFactory;
 
 public class ContainerInATabbedLayout {
 
 	public static void main(String[] args) {
 
 		// init SWT Engine in standalone mode
 		SWTHelper.init();
 		
 		// init the 'urn:java' resolver
 		ReflectionJavaHelper.init();
 
 		// create the shell
 		Display display = new Display();
 		Shell mainShell = new Shell(display, SWT.SHELL_TRIM);
 		mainShell.setText("Wazaabi");
 		mainShell.setLayout(new FillLayout());
 		mainShell.setSize(300, 300);
 		SWTControlViewer viewer = new SWTControlViewer(mainShell);
 
 		// create a container that will be the tabbed container
 		Container tabbedContainer = CoreWidgetsFactory.eINSTANCE.createContainer();
 		
 		TabbedLayoutRule tabLayout = CoreStylesFactory.eINSTANCE.createTabbedLayoutRule();
 		tabLayout.setPropertyName("layout");
 		tabLayout.setMaximizeVisible(true);
 		tabLayout.setPosition(Position.BOTTOM);
 		
 		//tabLayout.setTop(0);
 		tabbedContainer.getStyleRules().add(tabLayout);
 		
 		FontRule font = CoreStylesFactory.eINSTANCE.createFontRule();
 		font.setBold(true);
 		font.setItalic(true);
 		font.setName("Comic Sans MS");
 		font.setPropertyName("font");
 		
 		
 		
 
 		TabRule tabStyle1 = CoreStylesFactory.eINSTANCE.createTabRule();
 		tabStyle1.setPropertyName("layout-data");
		tabStyle1.setLabel("tab1");
 		tabStyle1.setClosable(true);
 		
 		TabRule tabStyle2 = CoreStylesFactory.eINSTANCE.createTabRule();
 		tabStyle2.setPropertyName("layout-data");
 		tabStyle2.setLabel("tab2");
 		tabStyle2.setImage("urn:java:tabb.png");
 		//tabStyle2.setImage("images/tabb.png");
 		
 		// first tab container
 		Container tab1 = CoreWidgetsFactory.eINSTANCE.createContainer();
 		tab1.getStyleRules().add(tabStyle1);
 		
 		tabbedContainer.getStyleRules().add(font);
 		
 		tabbedContainer.getChildren().add(tab1);
 		
 		FillLayoutRule fillLayout1 = SWTStylesFactory.eINSTANCE.createFillLayoutRule();
 		fillLayout1.setPropertyName("layout");
 		tab1.getStyleRules().add(fillLayout1);
 		FillLayoutRule fillLayout2 = SWTStylesFactory.eINSTANCE.createFillLayoutRule();
 		fillLayout2.setPropertyName("layout");
 
 		PushButton pushButton1 = CoreWidgetsFactory.eINSTANCE.createPushButton();
 		pushButton1.setText("Hello World"); //$NON-NLS-1$
 		PushButton pushButton2 = CoreWidgetsFactory.eINSTANCE.createPushButton();
 		pushButton2.setText("Second button"); //$NON-NLS-1$
 		tab1.getChildren().add(pushButton1);
 		tab1.getChildren().add(pushButton2);
 		
 		// second tab container
 		Container tab2 = CoreWidgetsFactory.eINSTANCE.createContainer();
 		tabbedContainer.getChildren().add(tab2);
 		tab2.getStyleRules().add(tabStyle2);
 		
 		tab2.getStyleRules().add(fillLayout2);
 		
 		TextComponent text = CoreWidgetsFactory.eINSTANCE.createTextComponent();
 		text.setText("This is a super textComponent");//$NON-NLS-1$
 		Label label1 = CoreWidgetsFactory.eINSTANCE.createLabel();
 		label1.setText("Text for item 1\n\none, two, three\n\n");//$NON-NLS-1$
 		tab2.getChildren().add(text);
 		tab2.getChildren().add(label1);
 		
 
 		
 		viewer.setContents(tabbedContainer);
 		
 		tabLayout.setTop(1);
 
 
 		
 		
 		
 		
 //		OrientationRule orientation = CoreStylesFactory.eINSTANCE.createOrientationRule();
 //		orientation.setPropertyName("orientation");
 //		orientation.setValue(Orientation.VERTICAL);
 //		tab1.getStyleRules().add(orientation);
 
 		// inject the container into the viewer
 		//viewer.setContents(tabbedContainer);
 
 		mainShell.open();
 
 		while (!mainShell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 		display.dispose();
 	}
 }

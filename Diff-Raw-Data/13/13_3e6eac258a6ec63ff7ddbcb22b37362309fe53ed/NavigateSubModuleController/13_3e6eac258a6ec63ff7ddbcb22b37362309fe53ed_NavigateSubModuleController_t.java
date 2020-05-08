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
 package org.eclipse.riena.example.client.controllers;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.internal.example.client.beans.PersonModificationBean;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.NodePositioner;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 
 /**
  * Controller of a sub module to demonstrate the navigate method of {@code
  * INavigationNode}.
  */
 public class NavigateSubModuleController extends SubModuleController {
 
 	public NavigateSubModuleController() {
 		this(null);
 	}
 
 	public NavigateSubModuleController(ISubModuleNode navigationNode) {
 		super(navigationNode);
 	}
 
 	@Override
 	public void configureRidgets() {
 
 		IActionRidget comboAndList = getRidget(IActionRidget.class, "comboAndList"); //$NON-NLS-1$
 		comboAndList.setText("Combo and List (SubApplication 1)"); //$NON-NLS-1$
 		comboAndList.addListener(new ComboAndListListener());
 
 		IActionRidget tableTextAndTree = getRidget(IActionRidget.class, "tableTextAndTree"); //$NON-NLS-1$
 		tableTextAndTree.setText("Table, Text and Tree (SubApplication 2) [First Position]"); //$NON-NLS-1$
 		tableTextAndTree.addListener(new TableTextAndTreeListener());
 
 		IActionRidget textAssembly = getRidget(IActionRidget.class, "textAssembly"); //$NON-NLS-1$
 		textAssembly.setText("Text Assembly (SubApplication 1)"); //$NON-NLS-1$
 		textAssembly.addListener(new TextAssemblyListener());
 
 		IActionRidget openAsFirstModule = getRidget(IActionRidget.class, "openAsFirstModule"); //$NON-NLS-1$
 		openAsFirstModule.setText("Open Module As First"); //$NON-NLS-1$
 		openAsFirstModule.addListener(new OpenModuleAsFirstListener());
 
 		IActionRidget openAsFirstSubModule = getRidget(IActionRidget.class, "openAsFirstSubModule"); //$NON-NLS-1$
		openAsFirstSubModule.setText("Open SubModule As FIRST in 'Combo And List'"); //$NON-NLS-1$
 		openAsFirstSubModule.addListener(new OpenSubModuleAsFirstListener());
 
 		IActionRidget openAsThirdSubModule = getRidget(IActionRidget.class, "openAsThirdSubModule"); //$NON-NLS-1$
		openAsThirdSubModule.setText("Open SubModule As THIRD in 'Combo And List'"); //$NON-NLS-1$
 		openAsThirdSubModule.addListener(new OpenSubModuleAsThirdListener());
 
 		final PersonModificationBean bean = new PersonModificationBean();
 		bean.setPerson(new Person("Doe", "Jane")); //$NON-NLS-1$ //$NON-NLS-2$
 		IActionRidget navigateRidget = getRidget(IActionRidget.class, "btnNavigateToRidget"); //$NON-NLS-1$
 		navigateRidget.addListener(new IActionListener() {
 			public void callback() {
 				getNavigationNode().navigate(new NavigationNodeId("org.eclipse.riena.example.combo"), //$NON-NLS-1$
 						new NavigationArgument(bean, "textFirst")); //$NON-NLS-1$
 
 			}
 		});
 
 	}
 
 	private class ComboAndListListener implements IActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void callback() {
 			NavigationArgument naviAgr = new NavigationArgument();
 			naviAgr.setPrepareAll(true);
 			getNavigationNode().navigate(
 					new NavigationNodeId("org.eclipse.riena.example.navigate.comboAndList"), naviAgr); //$NON-NLS-1$
 		}
 
 	}
 
 	private class TableTextAndTreeListener implements IActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void callback() {
 			NavigationArgument naviAgr = new NavigationArgument();
 			naviAgr.setNodePositioner(NodePositioner.ADD_BEGINNING);
 			getNavigationNode().navigate(
 					new NavigationNodeId("org.eclipse.riena.example.navigate.tableTextAndTree"), naviAgr); //$NON-NLS-1$
 		}
 
 	}
 
 	private class OpenModuleAsFirstListener implements IActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void callback() {
 			NavigationArgument naviAgr = new NavigationArgument();
 			naviAgr.setNodePositioner(NodePositioner.ADD_BEGINNING);
 			getNavigationNode().navigate(
 					new NavigationNodeId("org.eclipse.riena.example.navigate.firstmodule"), naviAgr); //$NON-NLS-1$
 
 		}
 
 	}
 
 	private class OpenSubModuleAsFirstListener implements IActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void callback() {
 			NavigationArgument naviAgr = new NavigationArgument();
 			naviAgr.setNodePositioner(NodePositioner.ADD_BEGINNING);
 			getNavigationNode().navigate(
 					new NavigationNodeId("org.eclipse.riena.example.navigate.firstSubModule"), naviAgr); //$NON-NLS-1$
 
 		}
 
 	}
 
 	private class OpenSubModuleAsThirdListener implements IActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void callback() {
 			NavigationArgument naviAgr = new NavigationArgument();
 			naviAgr.setNodePositioner(NodePositioner.indexed(2));
 			getNavigationNode().navigate(
 					new NavigationNodeId("org.eclipse.riena.example.navigate.thirdSubModule"), naviAgr); //$NON-NLS-1$
 
 		}
 
 	}
 
 	private class TextAssemblyListener implements IActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void callback() {
 			getNavigationNode().navigate(new NavigationNodeId("org.eclipse.riena.example.client.textExamplesGroup")); //$NON-NLS-1$
 		}
 
 	}
 
 }

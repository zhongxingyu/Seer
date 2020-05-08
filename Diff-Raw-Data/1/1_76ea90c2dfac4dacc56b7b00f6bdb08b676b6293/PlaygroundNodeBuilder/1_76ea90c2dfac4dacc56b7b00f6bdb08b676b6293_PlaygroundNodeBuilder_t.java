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
 package org.eclipse.riena.example.client.navigation.model;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.riena.example.client.controllers.BlockingSubModuleController;
 import org.eclipse.riena.example.client.controllers.ChoiceSubModuleController;
 import org.eclipse.riena.example.client.controllers.ComboSubModuleController;
 import org.eclipse.riena.example.client.controllers.DialogSubModuleController;
 import org.eclipse.riena.example.client.controllers.FocusableSubModuleController;
 import org.eclipse.riena.example.client.controllers.ListSubModuleController;
 import org.eclipse.riena.example.client.controllers.MarkerSubModuleController;
 import org.eclipse.riena.example.client.controllers.MessageBoxSubModuleController;
 import org.eclipse.riena.example.client.controllers.RidgetsSubModuleController;
 import org.eclipse.riena.example.client.controllers.StatuslineSubModuleController;
 import org.eclipse.riena.example.client.controllers.SystemPropertiesSubModuleController;
 import org.eclipse.riena.example.client.controllers.TableSubModuleController;
 import org.eclipse.riena.example.client.controllers.TextDateSubModuleController;
 import org.eclipse.riena.example.client.controllers.TextNumericSubModuleController;
 import org.eclipse.riena.example.client.controllers.TextSubModuleController;
 import org.eclipse.riena.example.client.controllers.TreeSubModuleController;
 import org.eclipse.riena.example.client.controllers.TreeTableSubModuleController;
 import org.eclipse.riena.example.client.controllers.ValidationSubModuleController;
 import org.eclipse.riena.example.client.views.BlockingSubModuleView;
 import org.eclipse.riena.example.client.views.ChoiceSubModuleView;
 import org.eclipse.riena.example.client.views.ComboSubModuleView;
 import org.eclipse.riena.example.client.views.DialogSubModuleView;
 import org.eclipse.riena.example.client.views.FocusableSubModuleView;
 import org.eclipse.riena.example.client.views.ListSubModuleView;
 import org.eclipse.riena.example.client.views.MarkerSubModuleView;
 import org.eclipse.riena.example.client.views.MessageBoxSubModuleView;
 import org.eclipse.riena.example.client.views.NoControllerSubModuleView;
 import org.eclipse.riena.example.client.views.RidgetsSubModuleView;
 import org.eclipse.riena.example.client.views.StatuslineSubModuleView;
 import org.eclipse.riena.example.client.views.SystemPropertiesSubModuleView;
 import org.eclipse.riena.example.client.views.TableSubModuleView;
 import org.eclipse.riena.example.client.views.TextDateSubModuleView;
 import org.eclipse.riena.example.client.views.TextNumericSubModuleView;
 import org.eclipse.riena.example.client.views.TextSubModuleView;
 import org.eclipse.riena.example.client.views.TreeSubModuleView;
 import org.eclipse.riena.example.client.views.TreeTableSubModuleView;
 import org.eclipse.riena.example.client.views.ValidationSubModuleView;
 import org.eclipse.riena.navigation.AbstractNavigationAssembler;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  *
  */
 public class PlaygroundNodeBuilder extends AbstractNavigationAssembler {
 
 	private Set<String> knownTargetIds = null;
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#buildNode(org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public INavigationNode<?> buildNode(NavigationNodeId navigationNodeId, NavigationArgument navigationArgument) {
 
 		IModuleGroupNode moduleGroup = new ModuleGroupNode(navigationNodeId);
 
 		IModuleNode playgroundModule = new ModuleNode(null, "Playground"); //$NON-NLS-1$
 		moduleGroup.addChild(playgroundModule);
 
 		ISubModuleNode buttonsSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.buttons"), "Buttons"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(buttonsSubModule, RidgetsSubModuleController.class,
 				RidgetsSubModuleView.ID, false);
 		playgroundModule.addChild(buttonsSubModule);
 		ISubModuleNode choiceSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.choice"), "Choice"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(choiceSubModule, ChoiceSubModuleController.class,
 				ChoiceSubModuleView.class.getName(), false);
 		playgroundModule.addChild(choiceSubModule);
 
 		ISubModuleNode comboSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.combo"), "Combo"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(comboSubModule, ComboSubModuleController.class,
 				ComboSubModuleView.ID, false);
 		playgroundModule.addChild(comboSubModule);
 
 		ISubModuleNode listSubModule = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.list"), "List"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(listSubModule, ListSubModuleController.class,
 				ListSubModuleView.ID, false);
 		playgroundModule.addChild(listSubModule);
 
 		ISubModuleNode textSubModule = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.text"), "Text"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(textSubModule, TextSubModuleController.class,
 				TextSubModuleView.ID, false);
 		playgroundModule.addChild(textSubModule);
 
 		ISubModuleNode textNumbersSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.text.numeric"), "Text (Numeric)"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(textNumbersSubModule, TextNumericSubModuleController.class,
 				TextNumericSubModuleView.ID, false);
 		playgroundModule.addChild(textNumbersSubModule);
 
 		ISubModuleNode textDateSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.text.date"), "Text (Date)"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(textDateSubModule, TextDateSubModuleController.class,
 				TextDateSubModuleView.ID, false);
 		playgroundModule.addChild(textDateSubModule);
 
 		ISubModuleNode markerSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.marker"), "Marker"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(markerSubModule, MarkerSubModuleController.class,
 				MarkerSubModuleView.ID, false);
 		playgroundModule.addChild(markerSubModule);
 		//
 		//		ISubModuleNode filterSubModule = new SubModuleNode(new NavigationNodeId(
 		//				"org.eclipse.riena.example.filterridget"), "UI-Filter (Ridgets)"); //$NON-NLS-1$ //$NON-NLS-2$
 		//		playgroundModule.addChild(filterSubModule);
 		//
 		//		ISubModuleNode filterNavigationSubModule = new SubModuleNode(new NavigationNodeId(
 		//				"org.eclipse.riena.example.filternavigation"), "UI-Filter (Navigation)"); //$NON-NLS-1$ //$NON-NLS-2$
 		//		playgroundModule.addChild(filterNavigationSubModule);
 
 		ISubModuleNode focusableSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.focusable"), "Focusable"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(focusableSubModule, FocusableSubModuleController.class,
 				FocusableSubModuleView.ID, false);
 		playgroundModule.addChild(focusableSubModule);
 
 		ISubModuleNode validationSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.validation"), "Validation"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(validationSubModule, ValidationSubModuleController.class,
 				ValidationSubModuleView.ID, false);
 		playgroundModule.addChild(validationSubModule);
 
 		ISubModuleNode treeSubModule = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.example.tree"), "Tree"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(treeSubModule, TreeSubModuleController.class,
 				TreeSubModuleView.ID, false);
 		playgroundModule.addChild(treeSubModule);
 
 		ISubModuleNode treeTableSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.treeTable"), "Tree Table"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(treeTableSubModule, TreeTableSubModuleController.class,
 				TreeTableSubModuleView.ID, false);
 		playgroundModule.addChild(treeTableSubModule);
 
 		ISubModuleNode tableSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.table"), "Table"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(tableSubModule, TableSubModuleController.class,
 				TableSubModuleView.ID, false);
 		playgroundModule.addChild(tableSubModule);
 
 		ISubModuleNode systemPropertiesSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.systemProperties"), "System Properties"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(systemPropertiesSubModule,
 				SystemPropertiesSubModuleController.class, SystemPropertiesSubModuleView.ID, false);
 		playgroundModule.addChild(systemPropertiesSubModule);
 
 		ISubModuleNode statusLineSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.statusLine"), "Statusline"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(statusLineSubModule, StatuslineSubModuleController.class,
 				StatuslineSubModuleView.ID, false);
 		playgroundModule.addChild(statusLineSubModule);
 
 		ISubModuleNode blockingSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.blocking"), "Blocking"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(blockingSubModule, BlockingSubModuleController.class,
 				BlockingSubModuleView.ID, false);
 		playgroundModule.addChild(blockingSubModule);
 
 		ISubModuleNode noControllerSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.noController"), "View without Controller"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(noControllerSubModule, NoControllerSubModuleView.ID);
 		playgroundModule.addChild(noControllerSubModule);
 
 		ISubModuleNode dialogSubModule = new SubModuleNode(
 				new NavigationNodeId("org.eclipse.riena.example.dialog"), "Dialog"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(dialogSubModule, DialogSubModuleController.class,
 				DialogSubModuleView.ID, false);
 		playgroundModule.addChild(dialogSubModule);
 
 		ISubModuleNode messageBoxSubModule = new SubModuleNode(new NavigationNodeId(
 				"org.eclipse.riena.example.messageBox"), "Message Box"); //$NON-NLS-1$ //$NON-NLS-2$
 		WorkareaManager.getInstance().registerDefinition(messageBoxSubModule, MessageBoxSubModuleController.class,
 				MessageBoxSubModuleView.ID, false);
 		playgroundModule.addChild(messageBoxSubModule);
 
 		return moduleGroup;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#acceptsTargetId(String)
 	 */
 	public boolean acceptsToBuildNode(NavigationNodeId nodeId, NavigationArgument argument) {
 
 		if (knownTargetIds == null) {
 			knownTargetIds = new HashSet<String>(Arrays.asList("org.eclipse.riena.example.playground", //$NON-NLS-1$
 					"org.eclipse.riena.example.buttons", //$NON-NLS-1$
 					"org.eclipse.riena.example.choice", //$NON-NLS-1$
 					"org.eclipse.riena.example.combo", //$NON-NLS-1$
 					"org.eclipse.riena.example.list", //$NON-NLS-1$
 					"org.eclipse.riena.example.text", //$NON-NLS-1$
 					"org.eclipse.riena.example.text.numeric", //$NON-NLS-1$
 					"org.eclipse.riena.example.text.date", //$NON-NLS-1$
 					"org.eclipse.riena.example.marker", //$NON-NLS-1$
 					"org.eclipse.riena.example.focusable", //$NON-NLS-1$
 					"org.eclipse.riena.example.validation", //$NON-NLS-1$
 					"org.eclipse.riena.example.tree", //$NON-NLS-1$
 					"org.eclipse.riena.example.treeTable", //$NON-NLS-1$
 					"org.eclipse.riena.example.table", //$NON-NLS-1$
 					"org.eclipse.riena.example.systemProperties", //$NON-NLS-1$
 					"org.eclipse.riena.example.statusLine", //$NON-NLS-1$
 					"org.eclipse.riena.example.blocking", //$NON-NLS-1$
 					"org.eclipse.riena.example.noController", //$NON-NLS-1$
 					"org.eclipse.riena.example.dialog", //$NON-NLS-1$
 					"org.eclipse.riena.example.messageBox" //$NON-NLS-1$
 			));
 			knownTargetIds = Collections.unmodifiableSet(knownTargetIds);
 		}
 
 		return knownTargetIds.contains(nodeId.getTypeId());
 	}
 }

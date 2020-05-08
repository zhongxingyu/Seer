 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.navigator.commands;
 
import java.awt.Container;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecp.common.handler.CreateContainmentHandler;
 import org.eclipse.emf.ecp.common.model.ECPWorkspaceManager;
 import org.eclipse.emf.ecp.common.model.NoWorkspaceException;
 import org.eclipse.emf.ecp.common.util.UiUtil;
 import org.eclipse.emf.ecp.navigator.Activator;
 import org.eclipse.emf.ecp.navigator.handler.NewModelElementWizardHandler;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.common.CommonUtil;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.CompoundContributionItem;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 
 /**
  * This class creates a group of commands to create different containments of a model element through context menu.
  * The created commands have all the same ID and are handled with the same handler class {@link CreateMEHandler}.
  * 
  * @author Hodaie
  */
 public class DynamicContainmentCommands extends CompoundContributionItem {
 
 	private static AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(
 		new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 
 	private static final String COMMAND_ID = "org.eclipse.emf.ecp.navigator.createContaiment";
 	private EObject selectedME;
 
 	/**
 	 * . {@inheritDoc}
 	 */
 	@Override
 	protected IContributionItem[] getContributionItems() {
 		// 1. get selected EObject
 		selectedME = UiUtil.getSelectedEObject();
 		if (selectedME == null) {
 			return new IContributionItem[0];
 		}
 		try {
 			if (ECPWorkspaceManager.getInstance().getWorkSpace().isRootObject(selectedME)) {
 				return createNewWizard(selectedME.eClass());
 			}
 		} catch (NoWorkspaceException e) {
 			Activator.getDefault().logException(e.getMessage(), e);
 		}
 
 		// 2. get its containments
 		Set<EClass> eClazz = CommonUtil.getAllEContainments(selectedME.eClass());
 		if (eClazz.size() > 5) {
 			return createNewWizard(selectedME.eClass());
 		}
 
 		AdapterFactoryItemDelegator delegator = new AdapterFactoryItemDelegator(new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 
 		Collection<?> collection = delegator.getNewChildDescriptors(selectedME,
 			AdapterFactoryEditingDomain.getEditingDomainFor(selectedME), null);
 		if (collection instanceof List<?>) {
 			@SuppressWarnings("unchecked")
 			List<CommandParameter> childDescriptors = (List<CommandParameter>) delegator.getNewChildDescriptors(
 				selectedME, AdapterFactoryEditingDomain.getEditingDomainFor(selectedME), null);
 			List<EReference> containments = new ArrayList<EReference>();
 			for (CommandParameter cp : childDescriptors) {
 				containments.add(cp.getEReference());
 			}
 
 			IContributionItem[] commands = createCommands(containments);
 			return commands;
 		}
 
 		// 3. create commands for these containments
 		List<EReference> containments = selectedME.eClass().getEAllContainments();
 		IContributionItem[] commands = createCommands(containments);
 		return commands;
 	}
 
 	private IContributionItem[] createNewWizard(EClass eClass) {
 		CommandContributionItemParameter commandParam = new CommandContributionItemParameter(PlatformUI.getWorkbench(),
 			null, "org.eclipse.emf.ecp.navigator.newModelElementWizard", CommandContributionItem.STYLE_PUSH);
 		List<IContributionItem> commands = new ArrayList<IContributionItem>();
 		Map<Object, Object> commandParams = new HashMap<Object, Object>();
 
 		commandParams.put(NewModelElementWizardHandler.COMMAND_ECLASS_PARAM, eClass);
 		commandParam.label = "New Model Element";
 		// TODO: Replace
 		Image image = labelProvider.getImage(eClass);
 		ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(image);
 		commandParam.icon = imageDescriptor;
 
 		// create command
 		commandParam.parameters = commandParams;
 		CommandContributionItem command = new CommandContributionItem(commandParam);
 		commands.add(command);
 
 		return commands.toArray(new IContributionItem[commands.size()]);
 	}
 
 	/**
 	 * .
 	 * 
 	 * @param containments a list of EReference of containments of selected ME
 	 * @return an array of IContributionsItem (commands) to create different types of containments.
 	 */
 	private IContributionItem[] createCommands(List<EReference> containments) {
 
 		List<IContributionItem> commands = new ArrayList<IContributionItem>();
 
 		// every command takes its corresponding EClass type as parameter
 		for (EReference containment : containments) {
 

 			if (!containment.isMany()) {
				if (selectedME.eGet(containment) != null ) {
					continue;
				}
 			}
 
 			try {
 				if (ECPWorkspaceManager.getInstance().getWorkSpace().getProject(selectedME)
 					.getMetaModelElementContext().isNonDomainElement(containment.getEReferenceType())) {
 					continue;
 				}
 			} catch (NoWorkspaceException e) {
 				Activator.getDefault().logException(e.getMessage(), e);
 			}
 
 			// if containment type is abstract, create a list of
 			// commands for its subclasses
 			if (containment.getEReferenceType().isAbstract() || containment.getEReferenceType().isInterface()) {
 
 				// note that a reference of commands array is passed,
 				// corresponding commands are created and added to it,
 				// then continue
 				// TODO: fix
 				addCommandsForSubTypes(containment.getEReferenceType(), commands);
 				continue;
 			}
 
 			CommandContributionItemParameter commandParam = new CommandContributionItemParameter(
 				PlatformUI.getWorkbench(), null, COMMAND_ID, CommandContributionItem.STYLE_PUSH);
 
 			Map<Object, Object> commandParams = new HashMap<Object, Object>();
 
 			commandParams.put(CreateContainmentHandler.COMMAND_ECLASS_PARAM, containment.getEReferenceType());
 			commandParam.label = "New " + containment.getEReferenceType().getName();
 			commandParam.icon = getImage(containment.getEReferenceType());
 
 			// create command
 			commandParam.parameters = commandParams;
 			CommandContributionItem command = new CommandContributionItem(commandParam);
 			commands.add(command);
 		}
 
 		return commands.toArray(new IContributionItem[commands.size()]);
 
 	}
 
 	private ImageDescriptor getImage(EClass eClass) {
 		EObject instance = eClass.getEPackage().getEFactoryInstance().create(eClass);
 		Image image = labelProvider.getImage(instance);
 		ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(image);
 		return imageDescriptor;
 	}
 
 	/**
 	 * If reference type is abstract create commands for its subclasses.
 	 * 
 	 * @param refClass
 	 * @param commands
 	 */
 	private void addCommandsForSubTypes(EClass refClass, List<IContributionItem> commands) {
 
 		// TODO EMFPlainObjectTransition: do not create commands for subclasses of ModelElement
 		// if (refClass.equals(MetamodelPackage.eINSTANCE.getModelElement())) {
 		// return;
 		// }
 
 		Set<EClass> eClazz = CommonUtil.getAllSubEClasses(refClass);
 		eClazz.remove(refClass);
 		for (EClass eClass : eClazz) {
 			CommandContributionItemParameter commandParam = new CommandContributionItemParameter(
 				PlatformUI.getWorkbench(), null, COMMAND_ID, CommandContributionItem.STYLE_PUSH);
 
 			Map<Object, Object> commandParams = new HashMap<Object, Object>();
 			commandParams.put(CreateContainmentHandler.COMMAND_ECLASS_PARAM, eClass);
 			commandParam.label = "New " + eClass.getName();
 			commandParam.icon = getImage(eClass);
 
 			// create command
 			commandParam.parameters = commandParams;
 			CommandContributionItem command = new CommandContributionItem(commandParam);
 			commands.add(command);
 		}
 
 	}
 
 }

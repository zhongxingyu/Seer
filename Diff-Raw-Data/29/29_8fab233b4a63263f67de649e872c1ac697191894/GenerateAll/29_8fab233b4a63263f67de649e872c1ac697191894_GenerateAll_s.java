 /*******************************************************************************
  * Copyright (c) 2008-2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.ui.generators.common;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.EEFGen.EEFGenModel;
 import org.eclipse.emf.eef.EEFGen.GenEditionContext;
 import org.eclipse.emf.eef.EEFGen.GenViewsRepository;
 import org.eclipse.emf.eef.codegen.core.launcher.AbstractPropertiesGeneratorLauncher;
 import org.eclipse.emf.eef.codegen.core.services.PropertiesGeneratorLaunchersServices;
 
 /**
  * Main entry point of the 'Codegen' generation module.
  * 
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class GenerateAll {
 
 	/**
 	 * The output folder.
 	 */
 	private File targetFolder;
 
 	/**
 	 * The Generation PSM
 	 */
 	private EEFGenModel eefGenModel;
 
 	/**
 	 * A set containing the target folder for generation
 	 */
 	private Set<IContainer> generationTargets;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param modelURI
 	 *            is the URI of the model.
 	 * @param targetFolder
 	 *            is the output folder
 	 * @throws IOException
 	 *             Thrown when the output cannot be saved.
 	 */
 	public GenerateAll(IContainer targetFolder, EEFGenModel eefGenModel) {
 		this.targetFolder = targetFolder.getLocation().toFile();
 		this.eefGenModel = eefGenModel;
 		this.generationTargets = new HashSet<IContainer>();
 		this.generationTargets.add(targetFolder);
 	}
 
 	/**
 	 * @return the generationTargets
 	 */
 	public Set<IContainer> getGenerationTargets() {
 		return generationTargets;
 	}
 
 	/**
 	 * Launches the generation.
 	 * 
 	 * @throws IOException
 	 *             Thrown when the output cannot be saved.
 	 */
 	public void doGenerate(IProgressMonitor monitor) throws IOException {
 		if (!targetFolder.exists()) {
 			monitor.subTask("Creating target folder");
 			targetFolder.mkdirs();
 		}
 		monitor.worked(1);
 		EObject model;
 
 		for (GenEditionContext genEditionContext : eefGenModel.getEditionContexts()) {
 			List<Object> arguments = new ArrayList<Object>();
 			arguments.add(getBasePackage(genEditionContext));
 			monitor.subTask("Generating simple components");
 			org.eclipse.emf.eef.codegen.components.SubPropertiesEditionComponent gen1 = new org.eclipse.emf.eef.codegen.components.SubPropertiesEditionComponent(
 					genEditionContext.getPropertiesEditionContext(), targetFolder, arguments);
 			gen1.doGenerate(BasicMonitor.toMonitor(monitor));
 			monitor.worked(1);
 			monitor.subTask("Generating composed components");
 			model = gen1.getModel();
 			if (model != null) {
 				org.eclipse.emf.eef.codegen.components.PropertiesEditionComponent gen0 = new org.eclipse.emf.eef.codegen.components.PropertiesEditionComponent(
 						model, targetFolder, arguments);
 				gen0.doGenerate(BasicMonitor.toMonitor(monitor));
 				monitor.worked(1);
 
 				monitor.subTask("Generating dynamic composed components");
 				org.eclipse.emf.eef.codegen.components.DynamicPropertiesEditionComponent gen14 = new org.eclipse.emf.eef.codegen.components.DynamicPropertiesEditionComponent(
 						model, targetFolder, arguments);
 				gen14.doGenerate(BasicMonitor.toMonitor(monitor));
 				monitor.worked(1);
 
				monitor.subTask("Generating Global Edition provider");
				org.eclipse.emf.eef.codegen.providers.PackagePropertiesEditionProvider gen11 = new org.eclipse.emf.eef.codegen.providers.PackagePropertiesEditionProvider(
						model, targetFolder, arguments);
				gen11.doGenerate(BasicMonitor.toMonitor(monitor));
				monitor.worked(1);

 				monitor.subTask("Generating Edition provider");
 				org.eclipse.emf.eef.codegen.providers.PropertiesEditionProvider gen12 = new org.eclipse.emf.eef.codegen.providers.PropertiesEditionProvider(
 						model, targetFolder, arguments);
 				gen12.doGenerate(BasicMonitor.toMonitor(monitor));
 				monitor.worked(1);
 
 				if (genEditionContext.isDescriptorsGenericPropertiesViews()) {
 					monitor.subTask("Generating plugin.xml configuration");
 					org.eclipse.emf.eef.codegen.plugin.Plugin_xml gen6 = new org.eclipse.emf.eef.codegen.plugin.Plugin_xml(
 							model, targetFolder, arguments);
 					gen6.doGenerate(BasicMonitor.toMonitor(monitor));
 					monitor.worked(1);
 				}
 
 				monitor.subTask("Generating 'SWT' parts");
 				org.eclipse.emf.eef.codegen.parts.PropertiesEditionPart gen4 = new org.eclipse.emf.eef.codegen.parts.PropertiesEditionPart(
 						model, targetFolder, arguments);
 				gen4.doGenerate(BasicMonitor.toMonitor(monitor));
 				monitor.worked(1);
 
 				monitor.subTask("Generating 'Form' parts");
 				org.eclipse.emf.eef.codegen.parts.FormPropertiesEditionPart gen2 = new org.eclipse.emf.eef.codegen.parts.FormPropertiesEditionPart(
 						model, targetFolder, arguments);
 				gen2.doGenerate(BasicMonitor.toMonitor(monitor));
 				monitor.worked(1);
 
 			}
 		}
 		for (GenViewsRepository genViewsRepository : eefGenModel.getViewsRepositories()) {
 			monitor.subTask("Generating parts interfaces");
 			List<Object> argumentsEmpty = new ArrayList<Object>();
 			List<Object> arguments2 = new ArrayList<Object>();
 			arguments2.add(genViewsRepository.getBasePackage());
 			org.eclipse.emf.eef.codegen.parts.IPropertiesEditionPart gen3 = new org.eclipse.emf.eef.codegen.parts.IPropertiesEditionPart(
 					genViewsRepository.getViewsRepository(), targetFolder, argumentsEmpty);
 			gen3.doGenerate(BasicMonitor.toMonitor(monitor));
 			monitor.worked(1);
 			model = gen3.getModel();
 			monitor.subTask("Generating views repository");
 
 			org.eclipse.emf.eef.codegen.parts.ViewsRepository gen3b = new org.eclipse.emf.eef.codegen.parts.ViewsRepository(
 					model, targetFolder, argumentsEmpty);
 			gen3b.doGenerate(BasicMonitor.toMonitor(monitor));
 			monitor.worked(1);
 
 			monitor.subTask("Generating internationalization provider");
 			org.eclipse.emf.eef.codegen.providers.ContextMessages gen8 = new org.eclipse.emf.eef.codegen.providers.ContextMessages(
 					model, targetFolder, argumentsEmpty);
 			gen8.doGenerate(BasicMonitor.toMonitor(monitor));
 			monitor.worked(1);
 
 			monitor.subTask("Generating internationalization messages");
 			org.eclipse.emf.eef.codegen.providers.ContextMessagesProperties gen9 = new org.eclipse.emf.eef.codegen.providers.ContextMessagesProperties(
 					model, targetFolder, argumentsEmpty);
 			gen9.doGenerate(BasicMonitor.toMonitor(monitor));
 			org.eclipse.emf.eef.codegen.providers.ContextMessagesPropertiesFR gen91 = new org.eclipse.emf.eef.codegen.providers.ContextMessagesPropertiesFR(
 					model, targetFolder, argumentsEmpty);
 			gen91.doGenerate(BasicMonitor.toMonitor(monitor));
 			monitor.worked(1);
 
 			monitor.subTask("Generating Part provider");
 			org.eclipse.emf.eef.codegen.providers.PackagePropertiesEditionPartProvider gen13 = new org.eclipse.emf.eef.codegen.providers.PackagePropertiesEditionPartProvider(
 					model, targetFolder, argumentsEmpty);
 			gen13.doGenerate(BasicMonitor.toMonitor(monitor));
 			monitor.worked(1);
 		}
 		for (AbstractPropertiesGeneratorLauncher launcher : PropertiesGeneratorLaunchersServices
 				.getInstance().getlaunchers()) {
 			launcher.doGenerate(eefGenModel, targetFolder, monitor);
 			if (!launcher.getTargetContainer().isEmpty())
 				generationTargets.addAll(launcher.getTargetContainer());
 		}
 	}
 
 	public String getBasePackage(GenEditionContext genEditionContext) throws IOException {
 		if (genEditionContext != null) {
 			if (genEditionContext.getBasePackage() != null)
 				return genEditionContext.getBasePackage();
 		}
 		return "";
 	}
 
 	public String getBasePackage(GenViewsRepository genViewsRepository) throws IOException {
 		if (genViewsRepository != null) {
 			if (genViewsRepository.getBasePackage() != null)
 				return genViewsRepository.getBasePackage();
 		}
 		return "";
 	}
 }

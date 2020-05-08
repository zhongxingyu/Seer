 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.internal.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.ast.declarations.FakeModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.SourceParserUtil;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 import org.eclipse.dltk.validators.core.IBuildParticipant;
 import org.eclipse.dltk.validators.core.IBuildParticipantExtension;
 import org.eclipse.dltk.validators.core.ISourceModuleValidator;
 import org.eclipse.dltk.validators.core.IValidator;
 import org.eclipse.dltk.validators.core.NullValidatorOutput;
 import org.eclipse.dltk.validators.core.ValidatorRuntime;
 import org.eclipse.osgi.util.NLS;
 
 public class ValidatorBuilder implements IScriptBuilder {
 	private static final boolean DEBUG = false;
 
 	private static final int WORK_BUILD = 100;
 	private static final int WORK_EXTERNAL = 200;
 
 	public IStatus buildModelElements(IScriptProject project, List elements,
 			IProgressMonitor monitor, int buildType) {
 		final IValidator[] validators = ValidatorRuntime.getProjectValidators(
 				project, ISourceModuleValidator.class,
 				ValidatorRuntime.AUTOMATIC);
 		final int validatorWork = validators.length * WORK_EXTERNAL;
 		monitor.beginTask("", validatorWork + WORK_BUILD); //$NON-NLS-1$
 		try {
 			buildModules(project, elements, buildType, ValidatorUtils
 					.subMonitorFor(monitor, WORK_BUILD));
 			if (validators.length != 0) {
 				final long startTime = DEBUG ? System.currentTimeMillis() : 0;
 				final IStatus status = ValidatorRuntime
 						.executeSourceModuleValidators(project, elements,
 								new NullValidatorOutput(), validators,
 								ValidatorUtils.subMonitorFor(monitor,
 										validatorWork));
 				if (DEBUG) {
 					System.out
 							.println("Validate " + project.getElementName() + "(" //$NON-NLS-1$ //$NON-NLS-2$
 									+ elements.size()
 									+ ") in " //$NON-NLS-1$
 									+ (System.currentTimeMillis() - startTime)
 									+ "ms"); //$NON-NLS-1$
 				}
 				return status;
 			}
 			return Status.OK_STATUS;
 		} finally {
 			monitor.done();
 		}
 	}
 
 	private void buildModules(IScriptProject project, List elements,
 			int buildType, IProgressMonitor monitor) {
 		final long startTime = DEBUG ? System.currentTimeMillis() : 0;
 		monitor.beginTask(ValidatorMessages.ValidatorBuilder_buildingModules,
 				elements.size());
 		final Map modulesByNature = splitByNature(elements);
 		for (Iterator i = modulesByNature.entrySet().iterator(); i.hasNext();) {
 			final Map.Entry entry = (Map.Entry) i.next();
 			final List natureModules = (List) entry.getValue();
 			final String natureId = (String) entry.getKey();
 			buildNatureModules(project, buildType, natureId, natureModules,
 					monitor);
 		}
 		monitor.done();
 		if (DEBUG) {
 			System.out.println("Build " + project.getElementName() + "(" //$NON-NLS-1$ //$NON-NLS-2$
 					+ elements.size() + ") in " //$NON-NLS-1$
 					+ (System.currentTimeMillis() - startTime) + "ms"); //$NON-NLS-1$
 		}
 	}
 
 	private void buildNatureModules(IScriptProject project, int buildType,
 			final String nature, final List modules, IProgressMonitor monitor) {
 		final IBuildParticipant[] validators = ValidatorRuntime
 				.getBuildParticipants(project, nature, ValidatorRuntime.ALL);
 		boolean secondPass = false;
 		for (int j = 0; j < validators.length; ++j) {
 			final IBuildParticipant participant = validators[j];
 			if (participant instanceof IBuildParticipantExtension) {
 				((IBuildParticipantExtension) participant)
 						.beginBuild(buildType);
 				secondPass = true;
 			}
 		}
 		int counter = 0;
 		final List reporters = secondPass ? new ArrayList() : null;
 		for (Iterator j = modules.iterator(); j.hasNext();) {
 			if (monitor.isCanceled())
 				return;
 			final ISourceModule module = (ISourceModule) j.next();
 			monitor.subTask(NLS.bind(
 					ValidatorMessages.ValidatorBuilder_buildModuleSubTask,
 					String.valueOf(modules.size() - counter), module
 							.getElementName()));
 			final IResource resource = module.getResource();
 			if (resource != null) {
 				final BuildProblemReporter reporter = new BuildProblemReporter(
 						resource);
 				buildModule(module, validators, reporter);
				//reporter.flush();
 				if (reporters != null) {
 					reporters.add(reporter);
 				}
 			}
 			monitor.worked(1);
 			++counter;
 		}
 		for (int j = 0; j < validators.length; ++j) {
 			final IBuildParticipant participant = validators[j];
 			if (participant instanceof IBuildParticipantExtension) {
 				((IBuildParticipantExtension) participant).endBuild();
 			}
 		}
 		if (reporters != null) {
 			for (Iterator j = reporters.iterator(); j.hasNext();) {
 				final BuildProblemReporter reporter = (BuildProblemReporter) j
 						.next();
 				reporter.flush();
 			}
 		}
 	}
 
 	private void buildModule(final ISourceModule module,
 			final IBuildParticipant[] validators, BuildProblemReporter reporter) {
 		final ModuleDeclaration moduleDeclaration = SourceParserUtil
 				.getModuleDeclaration(module, reporter);
 		final boolean isError = moduleDeclaration == null
 				|| moduleDeclaration instanceof FakeModuleDeclaration
 				|| reporter.hasErrors();
 		if (isError) {
 			if (reporter.isEmpty()) {
 				reporter.reportProblem(new DefaultProblem(
 						ValidatorMessages.ValidatorBuilder_unknownError, 0,
 						null, ProblemSeverities.Error, 0, 0, 0));
 			}
 		}
 		for (int k = 0; k < validators.length; ++k) {
 			final IBuildParticipant participant = validators[k];
 			try {
 				participant.build(module, moduleDeclaration, reporter);
 			} catch (CoreException e) {
 				ValidatorsCore.log(e.getStatus());
 			}
 		}
 	}
 
 	/**
 	 * @param elements
 	 * @return
 	 */
 	private Map splitByNature(List elements) {
 		final Map result = new HashMap();
 		for (Iterator i = elements.iterator(); i.hasNext();) {
 			final IModelElement element = (IModelElement) i.next();
 			if (element.getElementType() == IModelElement.SOURCE_MODULE) {
				final IDLTKLanguageToolkit toolkit = element.getScriptProject()
						.getLanguageToolkit();
				;
 				if (toolkit != null) {
 					List natureModules = (List) result.get(toolkit
 							.getNatureId());
 					if (natureModules == null) {
 						natureModules = new ArrayList();
 						result.put(toolkit.getNatureId(), natureModules);
 					}
 					natureModules.add(element);
 				}
 			}
 		}
 		return result;
 	}
 
 	public IStatus buildResources(IScriptProject project, List resources,
 			IProgressMonitor monitor, int buildType) {
 		return ValidatorRuntime.executeAutomaticResourceValidators(project,
 				resources, new NullValidatorOutput(), monitor);
 	}
 
 	public void clean(IScriptProject project, IProgressMonitor monitor) {
 		ValidatorRuntime.cleanAll(project, new ISourceModule[] {}, new IResource[] { project
 				.getProject() }, monitor);
 	}
 		
 	public int estimateElementsToBuild(IScriptProject project, List elements) {
 		return elements.size();
 	}
 
 	public Set getDependencies(IScriptProject project, Set resources,
 			Set allResources, Set oldExternalFolders, Set externalFolders) {
 		return null;
 	}
 
 	public void initialize(IScriptProject project) {
 	}
 
 	public void reset(IScriptProject project) {
 	}
 }

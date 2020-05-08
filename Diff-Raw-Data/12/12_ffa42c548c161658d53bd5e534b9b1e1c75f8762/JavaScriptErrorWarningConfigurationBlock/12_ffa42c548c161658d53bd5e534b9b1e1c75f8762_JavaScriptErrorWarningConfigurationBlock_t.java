 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.preferences;
 
 import java.util.Collection;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.ast.parser.SourceParserManager;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKContributedExtension;
 import org.eclipse.dltk.core.SourceParserUtil;
 import org.eclipse.dltk.javascript.core.JavaScriptNature;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserPlugin;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserPreferences;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserProblems;
 import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
 import org.eclipse.dltk.ui.preferences.IPreferenceChangeRebuildPrompt;
 import org.eclipse.dltk.ui.preferences.PreferenceChangeRebuildPrompt;
 import org.eclipse.dltk.ui.preferences.PreferenceKey;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 public class JavaScriptErrorWarningConfigurationBlock extends
 		AbstractOptionsBlock {
 
 	public JavaScriptErrorWarningConfigurationBlock(
 			IStatusChangeListener context, IProject project,
 			IWorkbenchPreferenceContainer container) {
 		super(context, project, new PreferenceKey[0], container);
 	}
 
 	// private static PreferenceKey[] getKeys(IProject project) {
 	// List<PreferenceKey> keys = new ArrayList<PreferenceKey>();
 	// keys.add(new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 	// JavaScriptCorePreferences.USE_STRICT_MODE));
 	// if (project != null) {
 	// keys.add(new PreferenceKey(DLTKCore.PLUGIN_ID,
 	// DLTKCore.PROJECT_SOURCE_PARSER_ID));
 	// }
 	// keys.add(new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 	// JavaScriptParserPreferences.ENABLE_TYPE_INFO));
 	// return keys.toArray(new PreferenceKey[keys.size()]);
 	// }
 
 	@Override
 	public Control createOptionsBlock(Composite parent) {
 		final GridLayout layout = new GridLayout(2, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 
 		final Composite composite = new Composite(parent, SWT.NULL);
 		composite.setLayout(layout);
 		composite.setFont(parent.getFont());
 
 		if (isProjectPreferencePage()) {
 			SWTFactory.createLabel(composite, "Parser", 1);
 			final IDLTKContributedExtension[] extensions = SourceParserManager
 					.getInstance().getContributions(JavaScriptNature.NATURE_ID);
 			final String[] ids = new String[extensions.length];
 			final String[] names = new String[extensions.length];
 			for (int i = 0; i < extensions.length; ++i) {
 				ids[i] = extensions[i].getId();
 				names[i] = extensions[i].getName();
 			}
 			bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 					names), new PreferenceKey(DLTKCore.PLUGIN_ID,
 					DLTKCore.PROJECT_SOURCE_PARSER_ID), ids);
 		}
 
 		bindControl(SWTFactory.createCheckButton(composite,
 				JavaScriptPreferenceMessages.ErrorWarning_enableTypeInfo, null,
 				false, 2), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				JavaScriptParserPreferences.ENABLE_TYPE_INFO), null);
 
 		String[] names = new String[] { "Warning", "Error", "Ignore" };
 		String[] ids = new String[] {
 				Integer.toString(ProblemSeverities.Warning),
 				Integer.toString(ProblemSeverities.Error),
 				Integer.toString(ProblemSeverities.Ignore) };
 
 		SWTFactory.createLabel(composite,
 				"Wrong number of parameters to javascript function call", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.WRONG_PARAMETERS),
 				ids);
 		SWTFactory.createLabel(composite,
 				"Wrong number of parameters to java method call", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_"
 						+ JavaScriptProblems.WRONG_JAVA_PARAMETERS), ids);
 
 		SWTFactory.createLabel(composite, "Unknown javascript type", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.UNKNOWN_TYPE), ids);
 
 		SWTFactory.createLabel(composite, "Deprecated type usage", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.DEPRECATED_TYPE),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Deprecated javascript function call", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names),
 				new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 						"JavaScriptProblems_"
 								+ JavaScriptProblems.DEPRECATED_FUNCTION), ids);
 
 		SWTFactory.createLabel(composite,
 				"Deprecated javascript variable access", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names),
 				new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 						"JavaScriptProblems_"
 								+ JavaScriptProblems.DEPRECATED_VARIABLE), ids);
 
 		SWTFactory.createLabel(composite, "Deprecated java method call", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.DEPRECATED_METHOD),
 				ids);
 
 		SWTFactory.createLabel(composite, "Deprecated java property access", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names),
 				new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 						"JavaScriptProblems_"
 								+ JavaScriptProblems.DEPRECATED_PROPERTY), ids);
 
 		SWTFactory.createLabel(composite, "Undefined javascript function call",
 				1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.UNDEFINED_METHOD),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Undefined javascript variable access", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.UNDEFINED_PROPERTY),
 				ids);
 
 		SWTFactory.createLabel(composite, "Undefined java method call", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_"
 						+ JavaScriptProblems.UNDEFINED_JAVA_METHOD), ids);
 
 		SWTFactory.createLabel(composite, "Undefined java property access", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_"
 						+ JavaScriptProblems.UNDEFINED_JAVA_PROPERTY), ids);
 
 		SWTFactory.createLabel(composite, "Private function call", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.PRIVATE_FUNCTION),
 				ids);
 
 		SWTFactory.createLabel(composite, "Private variable access", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.PRIVATE_VARIABLE),
 				ids);
 
 		SWTFactory
 				.createLabel(composite, "Indirect access to static method", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.STATIC_METHOD), ids);
 
 		SWTFactory.createLabel(composite, "Indirect access to static property",
 				1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.STATIC_PROPERTY),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Static reference to none static method", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.INSTANCE_METHOD),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Static reference to none static property", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.INSTANCE_PROPERTY),
 				ids);
 
 		SWTFactory.createLabel(composite, "Unreachable code", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.UNREACHABLE_CODE),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Inconsistent function return values", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names),
 				new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 						"JavaScriptProblems_"
 								+ JavaScriptProblems.RETURN_INCONSISTENT), ids);
 
 		SWTFactory.createLabel(composite,
 				"Function not always returning a value", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_"
 						+ JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Function returns a different type then it declares", 1);
 		bindControl(
 				SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0, names),
 				new PreferenceKey(
 						JavaScriptPlugin.PLUGIN_ID,
 						"JavaScriptProblems_"
 								+ JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE),
 				ids);
 
 		SWTFactory.createLabel(composite,
 				"Assignment (=) when equality (==) test", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_" + JavaScriptProblems.EQUAL_AS_ASSIGN),
 				ids);
 
 		SWTFactory.createLabel(composite, "Reassignment of a constant", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptPlugin.PLUGIN_ID,
 				"JavaScriptProblems_"
 						+ JavaScriptProblems.REASSIGNMENT_OF_CONSTANT), ids);
 
 		SWTFactory.createLabel(composite, "Duplicate variable declaration", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				"JavaScriptParserProblems_"
 						+ JavaScriptParserProblems.DUPLICATE_VAR_DECLARATION),
 				ids);
 
 		SWTFactory.createLabel(composite, "Duplicate constant declaration", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				"JavaScriptParserProblems_"
 						+ JavaScriptParserProblems.DUPLICATE_VAR_DECLARATION),
 				ids);
 
 		SWTFactory.createLabel(composite, "Duplicate parameter declaration", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				"JavaScriptParserProblems_"
 						+ JavaScriptParserProblems.DUPLICATE_PARAMETER), ids);
 
 		SWTFactory.createLabel(composite, "Variable hides argument", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				"JavaScriptParserProblems_"
 						+ JavaScriptParserProblems.VAR_HIDES_ARGUMENT), ids);
 
 		SWTFactory.createLabel(composite, "Constant hides argument", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				"JavaScriptParserProblems_"
 						+ JavaScriptParserProblems.CONST_HIDES_ARGUMENT), ids);
 
 		SWTFactory.createLabel(composite, "Function hides argument", 1);
 		bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1, 0,
 				names), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
 				"JavaScriptParserProblems_"
 						+ JavaScriptParserProblems.FUNCTION_HIDES_ARGUMENT),
 				ids);
 
 		return composite;
 	}
 
 	@Override
 	protected IPreferenceChangeRebuildPrompt getPreferenceChangeRebuildPrompt(
 			boolean workspaceSettings, Collection<PreferenceKey> changedOptions) {
 		return PreferenceChangeRebuildPrompt.create(workspaceSettings,
 				"Parser Settings Changed", "The parser settings have changed.");
 	}
 
 	@Override
 	protected Job[] createBuildJobs(IProject project) {
 		final Job[] jobs = super.createBuildJobs(project);
 		final Job[] result = new Job[jobs.length + 1];
 		System.arraycopy(jobs, 0, result, 1, jobs.length);
 		Job job0 = jobs[0];
 		result[0] = new Job(job0.getName()) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				SourceParserUtil.clearCache();
 				return Status.OK_STATUS;
 			}
 		};
 		result[0].setRule(job0.getRule());
 		result[0].setUser(job0.isUser());
 		result[0].setSystem(job0.isSystem());
 		return result;
 	}
 
 }

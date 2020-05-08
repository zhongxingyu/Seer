 /*******************************************************************************
  * Copyright (c) 2012 Jens von Pilgrim
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package de.jevopi.mitra2.debug.launch.standalone;
 
 import static java.util.Collections.emptyMap;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 
 import de.jevopi.mitra2.MitraUtils;
 import de.jevopi.mitra2.context.PathHandler;
 import de.jevopi.mitra2.context.PathHandler.ResourceType;
 import de.jevopi.mitra2.context.ProjectContext;
 import de.jevopi.mitra2.debug.launch.MitraLaunchConfigurationAdapter;
 import de.jevopi.mitra2.debug.launch.MutableMitraLaunchConfigurationAdapter;
 import de.jevopi.mitra2.debug.ui.IPathTableListener;
 import de.jevopi.mitra2.debug.ui.PathTable;
 import de.jevopi.mitra2.mitra.ExecutionModifier;
 import de.jevopi.mitra2.mitra.FormalParameter;
 import de.jevopi.mitra2.mitra.Module;
 import de.jevopi.mitra2.mitra.Parameter;
 import de.jevopi.mitra2.mitra.ParameterModifier;
 import de.jevopi.mitra2.mitra.ReturnParameter;
 import de.jevopi.mitra2.mitra.RuleDeclaration;
 import de.jevopi.mitra2.mitra.VisibilityModifier;
 import de.jevopi.utils.FileUtils;
 
 /**
  * Dialog for configuring Mitra standalone launch.
  * <dl>
  * <dt>Module</dt>
  * <dd>(File) name of Mitra module
  * <dl>
  * <dt>Fields
  * <dt>
  * <dd>
  * <ul>
  * <li>Name of module</li>
  * <li>Button to browse in workspace</li>
  * </ul>
  * </dd>
  * <dt>Constraints</dt>
  * <dd>
  * <ul>
  * <li>--</li>
  * </ul>
  * </dd>
  * </dl>
  * <dd><!-- end module -->
  * <dt>Rule</dt>
  * <dd>Rule of module to execute
  * <dl>
  * <dt>Fields
  * <dt>
  * <dd>
  * <ul>
  * <li>Combobox with possible rules matching constraints</li>
  * </ul>
  * </dd>
  * <dt>Constraints</dt>
  * <dd>
  * <ul>
  * <li>must define 1..n input parameters, all marked with from modifier</li>
  * <li>must define 1..n output models</li>
  * <li>must be declared called or manual</li>
  * </ul>
  * </dd>
  * </dl>
  * <dd><!-- end rule -->
  * <dt>1..n Input models</dt>
  * <dd>Input model file/resource names
  * <dl>
  * <dt>Fields
  * <dt>
  * <dd>
  * <ul>
  * <li>Name of model resource</li>
  * <li>Button to browse in workspace</li>
  * </ul>
  * </dd>
  * <dt>Constraints</dt>
  * <dd>
  * <ul>
  * <li>Each resource must contain exactly one content element</li>
  * <li>Content element of n-th resource must be type of sub type of n-th input
  * parameter of rule</li>
  * </ul>
  * </dd>
  * </dl>
  * <dd><!-- end input models -->
  * <dt>1..n Output models</dt>
  * <dd>Output models file/resource names
  * <dl>
  * <dt>Fields
  * <dt>
  * <dd>
  * <ul>
  * <li>Name of model resource</li>
  * <li>Button to browse in workspace</li>
  * </ul>
  * </dd>
  * <dt>Constraints</dt>
  * <dd>
  * <ul>
  * <li>all resource names must be different</li>
  * </ul>
  * </dd>
  * </dl>
  * <dd><!-- end output model -->
  * <dt>Replace output models flag</dt>
  * <dd>Flag indicating whether output models are overwritten or if the
  * transformation should be canceled if one or more output model files already
  * exist.
  * <dl>
  * <dt>Fields
  * <dt>
  * <dd>
  * <ul>
  * <li>Radio btton</li>
  * </ul>
  * </dd>
  * <dt>Constraints</dt>
  * <dd>
  * <ul>
  * <li>--</li>
  * </ul>
  * </dd>
  * </dl>
  * <dd><!-- end flag -->
  * <dt>Trace model setting</dt>
  * <dd>Trace model file name</dd>
  * <dl>
  * <dt>Fields
  * <dt>
  * <dd>
  * <ul>
  * <li>Check box indicating whether trace model is to be persisted</li>
  * <li>File name of trace model</li>
  * <li>Browse button to select trace model in workspace</li>
  * <li>Radio button indicating whether trace model is to be overwritten or
  * replaced</li>
  * </ul>
  * </dd>
  * <dt>Constraints</dt>
  * <dd>
  * <ul>
  * <li>--</li>
  * </ul>
  * </dd>
  * </dl>
  * <dd><!-- end trace model --> </dl>
  * 
  * @author Jens von Pilgrim (developer@jevopi.de)
  * @since Jan 31, 2012
  */
 public class MitraStandaloneLaunchConfigTabMain extends
 		AbstractLaunchConfigurationTab {
 	/**
 	 * Logger for this class
 	 */
 	// @SuppressWarnings("unused") //$NON-NLS-1$
 	private static final Logger log = Logger
 		.getLogger(MitraStandaloneLaunchConfigTabMain.class.getName());
 
 	class UpdateListener extends SelectionAdapter implements ModifyListener,
 			IPathTableListener {
 
 		@Override
 		public void modifyText(ModifyEvent i_e) {
 			if (i_e.getSource() == textModuleFileName) {
 				updateModulePathAndRules(textModuleFileName.getText());
 			}
 			updateLaunchConfigurationDialog();
 		}
 
 		@Override
 		public void widgetSelected(SelectionEvent i_e) {
 			if (i_e.getSource() == comboRuleName
 				|| i_e.getSource() == textModuleFileName) {
 				updateParameterTables();
 			}
 			updateLaunchConfigurationDialog();
 		}
 
 		@Override
 		public void pathTableRowChanged(Event event) {
 			updateLaunchConfigurationDialog();
 		}
 
 	}
 
 	private Button checkOverwriteOutput;
 
 	private Button checkOverwriteTraces;
 
 	private Button checkSaveTraces;
 
 	private Combo comboRuleName;
 
 	private UpdateListener modifyListener = new UpdateListener();
 
 	private PathTable tableInputModelFileName;
 
 	private Text textModuleFileName;
 
 	private PathTable tableOutputModelFileName;
 
 	private Text textTraceModelFileName;
 
 	private Map<String, String[]> parameterFileExtension = emptyMap();
 
 	/**
 	 * @todo replace with own tab
 	 */
 	private String modulePath = ".";
 
 	ProjectContext projectContext = new ProjectContext();
 	/** Cached module */
 	Module module;
 
 	/**
 	 * @param i_group
 	 * @return
 	 */
 	private Text createBrowsableFilename(Group group,
 			final int fileDialogStyle, final String... filterExtensions) {
 		Composite composite = new Composite(group, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 		final Text text = new Text(composite, SWT.BORDER);
 		text.setSize(100, 20);
 		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gd.widthHint = 100;
 		text.setLayoutData(gd);
 		Button button = new Button(composite, SWT.PUSH);
 		button.setSize(20, 20);
 		button.setText("Browse...");
 		button
 			.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				handleFileSelection(text, fileDialogStyle, filterExtensions);
 			}
 		});
 
 		text.addModifyListener(modifyListener);
 
 		return text;
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite panel = new Composite(parent, SWT.None);
 		setControl(panel);
 		panel.setSize(100, 100);
 
 		panel.setLayout(new GridLayout(1, false));
 		doCreatePanel(panel);
 	}
 
 	private Group createGroup(Composite parent, String name) {
 		Group group = new Group(parent, SWT.BORDER);
 		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		group.setText(name + ":");
 		group.setLayout(new FillLayout(SWT.VERTICAL));
 		return group;
 	}
 
 	private void doCreatePanel(Composite parent) {
 		Group group = createGroup(parent, "Mitra Module");
 		textModuleFileName =
 			createBrowsableFilename(group, SWT.OPEN, "*.mitra");
 		textModuleFileName.addModifyListener(new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent event) {
 				String name = textModuleFileName.getText();
 				File f = new File(FileUtils.toOS(name));
 				if (f.exists()) {
 					updateModulePathAndRules(name);
 				}
 			}
 		});
 
 		group = createGroup(parent, "Rule");
 		comboRuleName = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
 		comboRuleName.setSize(100, 20);
 		comboRuleName.addSelectionListener(modifyListener);
 
 		group = createGroup(parent, "Input models");
 		tableInputModelFileName = new PathTable(group, SWT.OPEN);
 		tableInputModelFileName.addPathTableListener(modifyListener);
 
 		group = createGroup(parent, "Output models");
 		tableOutputModelFileName = new PathTable(group, SWT.SAVE);
 		tableOutputModelFileName.addPathTableListener(modifyListener);
 		checkOverwriteOutput = new Button(group, SWT.CHECK);
 		checkOverwriteOutput.setText("Overwrite existing output models");
 		checkOverwriteOutput.addSelectionListener(modifyListener);
 
 		group = createGroup(parent, "Trace model");
 		checkSaveTraces = new Button(group, SWT.CHECK);
 		checkSaveTraces.setText("Save trace model");
 		checkSaveTraces.addSelectionListener(modifyListener);
 		textTraceModelFileName =
 			createBrowsableFilename(group, SWT.SAVE, "*.traces");
 		checkOverwriteTraces = new Button(group, SWT.CHECK);
 		checkOverwriteTraces.setText("Overwrite existing trace models");
 		checkOverwriteTraces.addSelectionListener(modifyListener);
 
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	@Override
 	public String getName() {
 		return "Main";
 	}
 
 	private void handleFileSelection(Text textToSet, int fileDialogStyle,
 			String... filterExtensions) {
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		File f = workspaceRoot.getRawLocation().toFile();
 		FileDialog fileDialog = new FileDialog(getShell(), fileDialogStyle);
 		fileDialog.setFilterPath(f.getAbsolutePath());
 		if (filterExtensions != null && filterExtensions.length > 0) {
 			fileDialog.setFilterExtensions(filterExtensions);
 		}
 		String name = fileDialog.open();
 		if (name != null) {
 			textToSet.setText(name);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		try {
 			MitraLaunchConfigurationAdapter config =
 				new MitraLaunchConfigurationAdapter(configuration);
 
 			// this automatically updates the rule combo box
 			textModuleFileName.setText(config.getModuleFilename());
 			modulePath = config.getModulePath();
 			comboRuleName.setText(config.getRuleName());
 
 			tableInputModelFileName
 				.setMap(new HashMap<String, String>(config
 					.getInputModelFilenames()), parameterFileExtension);
 			tableOutputModelFileName.setMap(
 					new HashMap<String, String>(config
 						.getOutputModelFilenames()), parameterFileExtension);
 			checkOverwriteOutput.setSelection(config.isOverwriteOutput());
 			textTraceModelFileName.setText(config.getTraceModelFilename());
 			checkSaveTraces.setSelection(config.isSaveTraces());
 			checkOverwriteTraces.setSelection(config.isOverwriteTraces());
 		} catch (CoreException ex) {
 			ex.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public boolean isValid(ILaunchConfiguration configuration) {
 
 		try {
 			setErrorMessage(null);
 			setMessage(null);
 			MitraLaunchConfigurationAdapter config =
 				new MitraLaunchConfigurationAdapter(configuration);
 			if (!config.validate()) {
 				setErrorMessage(config.getLastErrorMessage());
 				return false;
 			}
 			return true;
 		} catch (Throwable ex) {
 			setErrorMessage("Launch configuration invalid: " + ex.getMessage());
 			ex.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	@Override
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 
 		MutableMitraLaunchConfigurationAdapter config =
 			new MutableMitraLaunchConfigurationAdapter(configuration);
 
 		config.setModulePath(modulePath);
 		config.setRuleName(comboRuleName.getText());
 		config.setInputModelFilenames(tableInputModelFileName.getMap());
 		config.setOutputModelFilenames(tableOutputModelFileName.getMap());
 		config.setTraceModelFilename(textTraceModelFileName.getText());
 		config.setOverwriteOutput(checkOverwriteOutput.getSelection());
 		config.setOverwriteTraces(checkOverwriteTraces.getSelection());
 		config.setSaveTraces(checkSaveTraces.getSelection());
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	@Override
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 		 // no defaults
 	}
 
 	private void updateParameterTables() {
 		Map<String, String> input = new LinkedHashMap<String, String>();
 		Map<String, String> output = new LinkedHashMap<String, String>();
 
 		try {
 			if (module != null) {
 				String fqn = comboRuleName.getText();
 				String simpleRulename = MitraUtils.getSimpleRuleName(fqn);
 				RuleDeclaration rule = null;
 				for (RuleDeclaration r : module.getRuleDeclarations()) {
 					if (r.getName().equals(simpleRulename)) {
 						rule = r;
 						break;
 					}
 				}
 				if (rule != null) {
 					for (FormalParameter fpar : rule.getFormalParameters()) {
 						input.put(fpar.getVardecl().getName(), "");
 					}
 					for (ReturnParameter rpar : rule.getReturnParameters()) {
 						output.put(rpar.getVardecl().getName(), "");
 					}
 				}
 
 				parameterFileExtension = new HashMap<String, String[]>();
 				for (Parameter par : MitraUtils.collectRuleParameters(rule)) {
 					parameterFileExtension.put(par.getVardecl().getName(),
 							MitraUtils.guessFileExtensions(par.getVardecl()
 								.getType()));
 				}
 			}
 		} finally {
 			// update only if the parameters have changed
 			if (!input.keySet().equals(
 					tableInputModelFileName.getMap().keySet()))
 				tableInputModelFileName.setMap(input, parameterFileExtension);
 			if (!output.keySet().equals(
 					tableOutputModelFileName.getMap().keySet()))
 				tableOutputModelFileName.setMap(output, parameterFileExtension);
 		}
 	}
 
 	private void updateModulePathAndRules(String moduleFilename) {
 		comboRuleName.removeAll();
 
 		module = loadModel(new ResourceSetImpl(), moduleFilename);
 		if (module == null) {
 			return;
 		}
 
 		// ensure root file of moduleFile is in path:
 		String computedModulePath =
 			MitraUtils.convertFQNToPathName(MitraUtils.fqn(module));
 
 		if (!moduleFilename.endsWith(computedModulePath)) {
 			String s =
 				"Inconistency, module file name should end with computed path name: "
 					+ moduleFilename + " -- " + computedModulePath;
 			log.warning(s);
 			throw new IllegalArgumentException(s);
 		}
 
 		modulePath =
 			moduleFilename.substring(0, moduleFilename.length()
 				- computedModulePath.length());
 
 		// reload with project context
 		projectContext.getPathHandler().clear();
 		projectContext.getPathHandler().addPath(ResourceType.MODULE,
				PathHandler.createFileBaseURI(modulePath));
 		projectContext.getPathHandler().addPath(ResourceType.METAMODEL,
				PathHandler.createFileBaseURI(modulePath));
 		URI uriModule =
 			projectContext.getPathHandler().resolve(ResourceType.MODULE,
 					computedModulePath);
 		module = loadModel(projectContext.getResourceSet(), uriModule);
 
 		for (RuleDeclaration rule : module.getRuleDeclarations()) {
 			if (rule.getVisibility() != VisibilityModifier.PUBLIC) {
 				continue;
 			}
 			if (rule.getExec() == ExecutionModifier.ABSTRACT
 				|| rule.getExec() == ExecutionModifier.AUTO) {
 				continue;
 			}
 			if (MitraUtils.hasParameterOfKind(rule, ParameterModifier.INTO))
 				continue;
 
 			String fqnRule = MitraUtils.fqn(rule);
 			comboRuleName.add(fqnRule);
 		}
 
 		comboRuleName.setText(comboRuleName.getItem(0));
 		updateParameterTables();
 
 	}
 
 	public <T extends EObject> T loadModel(ResourceSet resourceSet,
 			String filename) {
 		return loadModel(resourceSet, URI.createFileURI(filename));
 	}
 
 	<T extends EObject> T loadModel(ResourceSet resourceSet, URI uriModule) {
 		Resource resource = resourceSet.createResource(uriModule);
 
 		try {
 			if (!resource.isLoaded()) {
 				resource.load(null);
 			}
 			@SuppressWarnings("unchecked")
 			T content = (T) resource.getContents().get(0);
 			return content;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			setErrorMessage(uriModule + " not cannot be loaded: "
 				+ ex.getMessage());
 
 		}
 		return null;
 	}
 
 }

 /*
  * NewProjectWizardWindow.java (JMetricVis)
  * Copyright 2011 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.ui.swing;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileFilter;
 
 import org.lukep.javavis.program.generic.models.ClassModel;
 import org.lukep.javavis.program.generic.models.MethodModel;
 import org.lukep.javavis.program.generic.models.ProjectModel;
 import org.lukep.javavis.program.java.JavaSourceLoaderThread;
 import org.lukep.javavis.ui.IProgramSourceObserver;
 import org.lukep.javavis.ui.UIMain;
 import org.lukep.javavis.util.JavaVisConstants;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class NewProjectWizardWindow extends AbstractWizardWindow implements IProgramSourceObserver {
 
 	private JTextField txtProjectName;
 	private JTextField txtSourceDirectory;
 	private JTextField txtAdditionalClasspath;
 
 	private JButton btnBrowseSourceRootDir;
 	private JButton btnAddClasspath;
 	
 	private File selectedSourceRootDir;
 	
 	public NewProjectWizardWindow(Frame parent, UIMain uiInstance) {
 		super(parent, uiInstance, 
 				"Create a New Project", 
 				new ImageIcon(JavaVisConstants.ICON_PROJECT_WIZARD_NEW));
 		
 		setActionButtonText("Create Project");
 	}
 	
 	@Override
 	protected void initialiseFormControls() {
 		setFormLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblProjectName = new JLabel("Project Name:");
 		addFormControl(lblProjectName, "2, 2, right, default");
 		
 		txtProjectName = new JTextField();
 		addFormControl(txtProjectName, "4, 2, fill, default");
 		txtProjectName.setColumns(30);
 		txtProjectName.addKeyListener(validationListener);
 		
 		JLabel lblSourceDirectory = new JLabel("Source Root Directory:");
 		addFormControl(lblSourceDirectory, "2, 4, right, default");
 		
 		txtSourceDirectory = new JTextField();
 		addFormControl(txtSourceDirectory, "4, 4, fill, default");
 		txtSourceDirectory.setColumns(10);
 		txtSourceDirectory.setEditable(false);
 		
 		btnBrowseSourceRootDir = new JButton("Browse...");
 		btnBrowseSourceRootDir.addActionListener(this);
 		addFormControl(btnBrowseSourceRootDir, "6, 4");
 		
 		JLabel lblSourceLanguage = new JLabel("Source Language:");
 		addFormControl(lblSourceLanguage, "2, 6, right, default");
 		
 		JComboBox cbxSelectedLanguage = new JComboBox();
 		cbxSelectedLanguage.setModel(new DefaultComboBoxModel(new String[] {JavaVisConstants.SUPPORTED_LANG_JAVA_6}));
 		addFormControl(cbxSelectedLanguage, "4, 6, fill, default");
 		
 		JLabel lblAdditionalClasspaths = new JLabel("Additional Classpath:");
 		addFormControl(lblAdditionalClasspaths, "2, 8, right, default");
 		
 		txtAdditionalClasspath = new JTextField();
 		addFormControl(txtAdditionalClasspath, "4, 8, fill, default");
 		txtAdditionalClasspath.setColumns(10);
 		
 		btnAddClasspath = new JButton("Add...");
 		btnAddClasspath.addActionListener(this);
 		addFormControl(btnAddClasspath, "6, 8");
 		
 		JLabel lblOptions = new JLabel("Options:");
 		addFormControl(lblOptions, "2, 10, right, default");
 		
 		JCheckBox chckbxPreloadMetricMeasurements = new JCheckBox("Preload Metric Measurements");
 		chckbxPreloadMetricMeasurements.setSelected(true);
 		addFormControl(chckbxPreloadMetricMeasurements, "4, 10");
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (btnBrowseSourceRootDir == e.getSource()) {
 			selectedSourceRootDir = browseSourceRootDirectorySelect();
 			if (selectedSourceRootDir != null) {
 				txtSourceDirectory.setText(selectedSourceRootDir.getAbsolutePath());
 				refreshActionable();
 			}
 		} else if (btnAddClasspath == e.getSource()) {
 			File[] selectedFiles = browseClasspathSelect();
 			if (selectedFiles != null) {
 				String currentClasspaths = txtAdditionalClasspath.getText(), newClasspaths;
 				for (File f : selectedFiles) {
 					try {
 						newClasspaths = currentClasspaths 
 							+ (currentClasspaths.length() > 0 ? ";" : "") 
 							+ f.getCanonicalPath();
 						txtAdditionalClasspath.setText(newClasspaths);
 						currentClasspaths = newClasspaths;
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		}
 		super.actionPerformed(e);
 	}
 	
 	@Override
 	protected boolean validateFormControls() {
 		if (	// validate project name text length
 				txtProjectName.getText().length() > 0
 				// validate source directory length
 				&& txtSourceDirectory.getText().length() > 0
 				// ensure selected file exists
 				&& selectedSourceRootDir != null
 				&& selectedSourceRootDir.exists()) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected void lockFormControls() {
 		txtProjectName.setEnabled(false);
 		btnBrowseSourceRootDir.setEnabled(false);
 		btnPerformAction.setEnabled(false);
 	}
 	
 	@Override
 	protected void unlockFormControls() {
 		txtProjectName.setEnabled(true);
 		btnBrowseSourceRootDir.setEnabled(true);
 		refreshActionable();
 	}
 
 	@Override
 	protected boolean performAction() throws Exception {
 		loadJavaCodeBase(selectedSourceRootDir);
 		return true;
 	}
 	
 	private File browseSourceRootDirectorySelect() {
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Browse for a directory");
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fc.setMultiSelectionEnabled(false);
 		int returnVal = fc.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 			return fc.getSelectedFile();
 		return null;
 	}
 	
 	private File[] browseClasspathSelect() {
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Add an additional classpath (directory or individual .jars)");
 		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		fc.setMultiSelectionEnabled(true);
 		fc.setFileFilter(new FileFilter() {
 			@Override
 			public String getDescription() {
 				return ".jar files and directories containing .class files";
 			}
 			
 			@Override
 			public boolean accept(File f) {
 				if (f.isDirectory() 
 						|| f.getName().endsWith(".jar")
 						|| f.getName().endsWith(".class"))
 					return true;
 				return false;
 			}
 		});
 		int returnVal = fc.showDialog(this, "Add Directory or JAR(s)");
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 			return fc.getSelectedFiles();
 		return null;
 	}
 	
 	private void loadJavaCodeBase(File selectedDirectory) throws Exception {
 		
 		final ProjectModel project = new ProjectModel(txtProjectName.getText());
 		
 		// build the (custom) classpath to pass to the compiler
 		String customClasspath = txtAdditionalClasspath.getText();
 		String classpath = System.getProperty("java.class.path") 
 							+ (customClasspath.length() > 0 ? ";" + customClasspath : "");
 		
 		// create compiler options list
 		List<String> compilerOptions = new ArrayList<String>(2);
 		compilerOptions.addAll(Arrays.asList("-classpath", classpath));
 		
 		JavaSourceLoaderThread jslt = new JavaSourceLoaderThread(selectedDirectory, 
 				project, compilerOptions) {
 			
 			@Override
 			public void notifyStatusChange(String message) {
 				setProgramStatus(message, true, 0);
 			}
 
 			@Override
 			public void statusFinished() {
 				WorkspacePane workspace = null;
 				try {
 					workspace = new WorkspacePane(project, uiInstance);
 					uiInstance.addChildWorkspaceFrame(workspace);
 					workspace.setVisible(true);
 					thisInstance.setVisible(false);
 					thisInstance.dispose();
 				} catch (Exception e) {
					showError("Error creating workspace: " + e.getLocalizedMessage());
 				}
 			}
 			
 		};
 		jslt.addObserver(this);
 		jslt.addObserver(project);
 		new Thread(jslt).start();
 	}
 
 	@Override
 	public void notifyRootNodeCount(int rootNodes) {
 		progressBar.setMaximum(rootNodes);
 	}
 
 	@Override
 	public void notifyRootNodeProcessing(int rootNode, String name) {
 		setProgramStatus("Processing " + name + "...", false, rootNode);
 	}
 
 	@Override
 	public void notifyRootNodesProcessed() {
 		setProgramStatus("Preparing to post-process elements...", true, 0);
 	}
 
 	@Override
 	public void notifyFindClass(ClassModel clazz) {
 	}
 
 	@Override
 	public void notifyFindMethod(MethodModel method) {
 	}
 
 }

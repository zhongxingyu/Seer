 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.sdk.tom.properties.transformations;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.search.SearchEngine;
 import org.eclipse.jdt.ui.IJavaElementSearchConstants;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.SelectionDialog;
 
 import com.worldline.gmf.propertysections.core.tools.FormDataBuilder;
 
 /**
  * A dialog to generate a transformation
  * 
  * @author tnachtergaele <nachtergaele.thomas@gmail.com>
  * 
  *
  */
 
 public class TransformationGeneratorDialog extends Dialog {
 
 	private static String PREFERRED_SOURCE_FOLDER = "";
 
 	/** The shell of the dialog */
 	private Shell myShell;
 
 	/** The java project */
 	private final IJavaProject javaProject;
 
 	/** The combo box containing the source folder */
 	private Combo sourceFolderCombo;
 
 	/** The text containing the package name */
 	private Text packageText;
 
 	/** The text containing the transformation class name */
 	private Text trnText;
 
 	/** The text containing the factory class name */
 	private Text factoryText;
 
 	/** The text containing the type of the transformed element */
 	private Text typeText;
 
 	/** The transformation data used in the generating process */
 	private TransformationGenerationData transformationData;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param parentShell
 	 *            the container shell.
 	 * @param javaProject
 	 *            the current java project.
 	 * @param transformationData
 	 *            the transformation data with initialization.
 	 */
 	public TransformationGeneratorDialog(Shell parentShell, IJavaProject javaProject,
 			TransformationGenerationData transformationData) {
 		super(parentShell);
 		this.myShell = parentShell;
 		this.javaProject = javaProject;
 		this.transformationData = transformationData;
 		this.setShellStyle(getShellStyle() | SWT.RESIZE);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 
 		Composite background = (Composite) super.createDialogArea(parent);
 		background.setLayout(new FormLayout());
 
 		// Manage the Transformation Source Folder
 		final Label sourceFolderLabel = new Label(background, SWT.NONE);
 		sourceFolderLabel.setText(TransformationDialogMessages.SOURCEFOLDER_LABEL.message());
 		sourceFolderLabel.setToolTipText(TransformationDialogMessages.SOURCEFOLDER_TOOLTIP.message());
 		this.sourceFolderCombo = new Combo(background, SWT.READ_ONLY);
 		final Button sourceFolderButton = new Button(background, SWT.PUSH);
 		sourceFolderButton.setText(TransformationDialogMessages.SOURCEFOLDER_BUTTON.message());
 
 		// Fill the combo box with available source folder
 		try {
			for (IPackageFragmentRoot packageFragmentRoot : this.javaProject.getAllPackageFragmentRoots()) {
 				if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
 					sourceFolderCombo.add(packageFragmentRoot.getPath().removeFirstSegments(1).makeRelative()
 							.toString());
 					sourceFolderCombo.select(0);
 				}
 			}
 		} catch (JavaModelException e2) {
 			e2.printStackTrace();
 		}
 		int indexSourceFolder = this.sourceFolderCombo.indexOf(TransformationGeneratorDialog.PREFERRED_SOURCE_FOLDER);
 		if (indexSourceFolder != -1) {
 			sourceFolderCombo.select(indexSourceFolder);
 		}
 
 		// Manage the transformation package
 		final Label packageLabel = new Label(background, SWT.NONE);
 		packageLabel.setText(TransformationDialogMessages.JAVAPACK_LABEL.message());
 		packageLabel.setToolTipText(TransformationDialogMessages.JAVAPACK_TOOLTIP.message());
 		this.packageText = new Text(background, SWT.BORDER);
 		this.packageText.setText(transformationData.getPackage());
 		final Button packageButton = new Button(background, SWT.PUSH);
 		packageButton.setText(TransformationDialogMessages.JAVAPACK_BUTTON.message());
 
 		// Manage the Transformation Class Name
 		final Label trnLabel = new Label(background, SWT.NONE);
 		trnLabel.setText(TransformationDialogMessages.TRNCLASS_LABEL.message());
 		trnLabel.setToolTipText(TransformationDialogMessages.TRNCLASS_TOOLTIP.message());
 
 		this.trnText = new Text(background, SWT.BORDER);
 		this.trnText.setText(transformationData.getTrn());
 
 		// Manage the Transformation Class Name
 		final Label factoryLabel = new Label(background, SWT.NONE);
 		factoryLabel.setText(TransformationDialogMessages.TRNFACT_LABEL.message());
 		factoryLabel.setToolTipText(TransformationDialogMessages.TRNFACT_TOOLTIP.message());
 
 		this.factoryText = new Text(background, SWT.BORDER);
 		this.factoryText.setText(transformationData.getFactory());
 
 		// Manage the type of transformed object
 		final Label typeLabel = new Label(background, SWT.NONE);
 		typeLabel.setText(TransformationDialogMessages.TRNELT_LABEL.message());
 		typeLabel.setToolTipText(TransformationDialogMessages.TRNELT_TOOLTIP.message());
 		this.typeText = new Text(background, SWT.BORDER);
 		this.typeText.setText(transformationData.getType());
 		this.typeText.setEnabled(false);
 		final Button typeButton = new Button(background, SWT.PUSH);
 		typeButton.setText(TransformationDialogMessages.TRNELT_BUTTON.message());
 
 		// Define the layout between all the objects.
 
 		FormDataBuilder.on(sourceFolderLabel).top(3).left().width(200);
 		FormDataBuilder.on(this.sourceFolderCombo).top().left(sourceFolderLabel).right(sourceFolderButton);
 		FormDataBuilder.on(sourceFolderButton).top().right().width(80).height(22);
 
 		FormDataBuilder.on(packageLabel).top(this.sourceFolderCombo, 10).left().width(200);
 		FormDataBuilder.on(this.packageText).top(this.sourceFolderCombo).left(packageLabel).right(packageButton);
 		FormDataBuilder.on(packageButton).top(this.sourceFolderCombo).right().width(80).height(22);
 
 		FormDataBuilder.on(trnLabel).top(this.packageText, 10).left().width(200);
 		FormDataBuilder.on(this.trnText).top(this.packageText).left(trnLabel).width(600).right();
 
 		FormDataBuilder.on(factoryLabel).top(this.trnText, 10).left().width(200);
 		FormDataBuilder.on(this.factoryText).top(this.trnText).left(factoryLabel).width(600).right();
 
 		FormDataBuilder.on(typeLabel).top(this.factoryText, 10).left().width(200);
 		FormDataBuilder.on(this.typeText).top(this.factoryText).left(typeLabel).right(typeButton);
 		FormDataBuilder.on(typeButton).top(this.factoryText).right().width(80).height(22);
 
 		this.factoryText.addModifyListener(new ModifyListener() {
 
 			private String oldText = TransformationGeneratorDialog.this.factoryText.getText();
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				String newText = TransformationGeneratorDialog.this.factoryText.getText();
 				if (newText.endsWith(TransformationGenerationData.FACTORYDEFAULT)) {
 					this.oldText = newText;
 				} else {
 					TransformationGeneratorDialog.this.factoryText.setText(this.oldText);
 				}
 
 			}
 		});
 
 		packageButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				try {
 					final SelectionDialog createPackageDialog = JavaUI.createPackageDialog(new Shell(
 							TransformationGeneratorDialog.this.myShell),
							TransformationGeneratorDialog.this.javaProject,
							IJavaElementSearchConstants.CONSIDER_REQUIRED_PROJECTS);
 					createPackageDialog.setMessage(TransformationDialogMessages.JAVAPACK_SELECTOR_LABEL.message());
 					createPackageDialog.setTitle(TransformationDialogMessages.DIALOG_TITLE.message());
 					if ((createPackageDialog.open() == Window.OK) && (createPackageDialog.getResult().length > 0)) {
 						final Object o = createPackageDialog.getResult()[0];
 						if (o instanceof IPackageFragment) {
 							TransformationGeneratorDialog.this.packageText.setText(((IPackageFragment) o)
 									.getElementName());
 							TransformationGeneratorDialog.this.packageText.update();
 						}
 					}
 				} catch (final JavaModelException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		typeButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				try {
 					final IType eObjectType = TransformationGeneratorDialog.this.javaProject
 							.findType("org.eclipse.emf.ecore.EObject");
 					if (eObjectType != null) {
 						final Shell childShell = new Shell(TransformationGeneratorDialog.this.myShell);
 						final SelectionDialog createTypeDialog = JavaUI.createTypeDialog(childShell, null,
 								SearchEngine.createHierarchyScope(eObjectType),
 								IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, false);
 						createTypeDialog.setTitle(TransformationDialogMessages.DIALOG_TITLE.message());
 						createTypeDialog.setMessage(TransformationDialogMessages.TRNELT_SELECTOR_LABEL.message());
 						if ((createTypeDialog.open() == Window.OK) && (createTypeDialog.getResult().length > 0)) {
 							final Object o = createTypeDialog.getResult()[0];
 							if (o instanceof IType) {
 								TransformationGeneratorDialog.this.typeText.setText(((IType) o).getFullyQualifiedName());
 								TransformationGeneratorDialog.this.typeText.update();
 							}
 						}
 					}
 				} catch (final JavaModelException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		return background;
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(TransformationDialogMessages.DIALOG_TITLE.message());
 	}
 
 	@Override
 	protected void okPressed() {
 		TransformationGeneratorDialog.PREFERRED_SOURCE_FOLDER = this.sourceFolderCombo.getText();
 		this.transformationData.setSourceFolder(this.sourceFolderCombo.getText());
 		this.transformationData.setPackage(this.packageText.getText());
 		this.transformationData.setTrn(this.trnText.getText());
 		this.transformationData.setFactory(this.factoryText.getText());
 		this.transformationData.setType(this.typeText.getText());
 		super.okPressed();
 	}
 
 	@Override
 	protected Point getInitialSize() {
 		return new Point(600, 250);
 	}
 
 }

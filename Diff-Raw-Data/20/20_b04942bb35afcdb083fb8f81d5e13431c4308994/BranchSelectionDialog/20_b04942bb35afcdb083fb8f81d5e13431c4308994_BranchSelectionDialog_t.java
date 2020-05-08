 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.dialogs;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IDialogLabelKeys;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Dialog for branch selection. Has subclasses which modify the dialog for
  * certain usecases, such as {@link Creation} and {@link CheckoutSelection}
  * 
  * @author wesendon
  */
 public class BranchSelectionDialog extends TitleAreaDialog {
 	/**
 	 * Access for subclasses.
 	 */
 	private final java.util.List<BranchInfo> branches;
 
 	/**
 	 * Access for subclasses.
 	 */
 	private TableViewer tableViewer;
 
 	/**
 	 * Access for subclasses.
 	 */
 	private BranchInfo result;
 
 	/**
 	 * Create the dialog.
 	 * 
 	 * @param parentShell
 	 *            parent shell
 	 * @param baseVersion
 	 *            base version
 	 * @param branches
 	 *            list of branches
 	 */
 	public BranchSelectionDialog(Shell parentShell, List<BranchInfo> branches) {
 		super(parentShell);
 		this.branches = branches;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 * 
 	 * @param parent
 	 *            parent component
 	 * @return a control
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		setHeaderTexts();
 
 		final Composite area = (Composite) super.createDialogArea(parent);
 		final Composite container = new Composite(area, SWT.NONE);
 		container.setLayout(new GridLayout(1, false));
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		addCreationField(container);
 
 		setTableViewer(new TableViewer(container, SWT.BORDER | SWT.V_SCROLL));
 		final Table list = getTableViewer().getTable();
 		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		getTableViewer().setContentProvider(ArrayContentProvider.getInstance());
 
		getTableViewer().setLabelProvider(new CellLabelProvider() {
 			@Override
 			public void update(ViewerCell cell) {
 				final Object element = cell.getElement();
 				if (element instanceof BranchInfo) {
 					final BranchInfo branch = (BranchInfo) element;
					final String styledString = "Branch:  "
						+ branch.getName() + "  " + "[Version: "
						+ branch.getHead().getIdentifier() + "]";
					cell.setText(styledString);
 				}
 			}
 		});
 
 		getTableViewer().setInput(getBranches());
 
 		endOfInit();
 
 		return area;
 	}
 
 	/**
 	 * Hook for initialization.
 	 */
 	protected void endOfInit() {
 	}
 
 	@Override
 	protected void okPressed() {
 		final ISelection selection = getTableViewer().getSelection();
 		if (selection instanceof IStructuredSelection) {
 			setResult((BranchInfo) ((IStructuredSelection) selection)
 				.getFirstElement());
 		}
 		super.okPressed();
 	}
 
 	/**
 	 * Returns a {@link BranchInfo} as result or null for certain dialogs.
 	 * 
 	 * @return {@link BranchInfo}
 	 */
 	public BranchInfo getResult() {
 		return result;
 	}
 
 	/**
 	 * Hook to set header texts.
 	 */
 	protected void setHeaderTexts() {
 		getShell().setText("Branch Selection");
 		setTitle("Branch Selection");
 		setMessage("Please select which Branch you want to merge into your local copy of the project.");
 	}
 
 	/**
 	 * Hook to add additional components to the dialog.
 	 * 
 	 * @param container
 	 *            parent
 	 */
 	protected void addCreationField(Composite container) {
 	}
 
 	/**
 	 * Create contents of the button bar.
 	 * 
 	 * @param parent
 	 *            parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY),
 			true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 			JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY), false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(400, 350);
 	}
 
 	/**
 	 * Returns the branches.
 	 * 
 	 * @return the branches
 	 */
 	protected java.util.List<BranchInfo> getBranches() {
 		return branches;
 	}
 
 	/**
 	 * Returns the table viewer.
 	 * 
 	 * @return the table viewer
 	 */
 	protected TableViewer getTableViewer() {
 		return tableViewer;
 	}
 
 	/**
 	 * Sets the table viewer.
 	 * 
 	 * @param tableViewer
 	 *            the table viewer to be set
 	 */
 	protected void setTableViewer(TableViewer tableViewer) {
 		this.tableViewer = tableViewer;
 	}
 
 	/**
 	 * Sets the result.
 	 * 
 	 * @param result
 	 *            the result to be set
 	 */
 	protected void setResult(BranchInfo result) {
 		this.result = result;
 	}
 
 	/**
 	 * Alternative version of this dialog for Checkout Selection.
 	 * 
 	 * @author wesendon
 	 * 
 	 */
 	public static class CheckoutSelection extends BranchSelectionDialog {
 
 		/**
 		 * Default constructor.
 		 * 
 		 * @param parentShell
 		 *            parent
 		 * @param branches
 		 *            list of branches
 		 */
 		public CheckoutSelection(Shell parentShell, List<BranchInfo> branches) {
 			super(parentShell, branches);
 		}
 
 		@Override
 		protected void setHeaderTexts() {
 			getShell().setText("Checkout Branch");
 			setTitle("Checkout Branch");
 			setMessage("Please choose which Branch should be checked out.");
 
 		}
 
 	}
 
 	/**
 	 * Alternative version of this dialog for Branch Creation. In this version
 	 * branches can't be selected but are displayed in order to avoid duplicate
 	 * naming.
 	 * 
 	 * @author wesendon
 	 * 
 	 */
 	public static class Creation extends BranchSelectionDialog {
 
 		private Text text;
 		private String newName = StringUtils.EMPTY;
 
 		/**
 		 * Default constructor.
 		 * 
 		 * @param parentShell
 		 *            parent
 		 * @param baseVersion
 		 *            baseversion
 		 * @param branches
 		 *            list of branches
 		 */
 		public Creation(Shell parentShell,
 			java.util.List<BranchInfo> branches) {
 			super(parentShell, branches);
 		}
 
 		@Override
 		protected void setHeaderTexts() {
 			getShell().setText("Create Branch");
 			setTitle("Create Branch");
 			setMessage("Please specify a name for the new Branch.");
 		}
 
 		/**
 		 * Returns the selected name for the branch.
 		 * 
 		 * @return String
 		 */
 		public String getNewBranch() {
 			return newName;
 		}
 
 		@Override
 		protected void endOfInit() {
 			getTableViewer().getTable().setEnabled(false);
 			getTableViewer().getTable().setBackground(
 				Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
 		}
 
 		@Override
 		protected void okPressed() {
 			if (text != null) {
 				newName = text.getText();
 			}
 			super.okPressed();
 		}
 
 		@Override
 		protected void addCreationField(Composite container) {
 			final Composite creationContainer = new Composite(container, SWT.NONE);
 			creationContainer.setLayout(new GridLayout(2, false));
 			creationContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP,
 				true, false, 1, 1));
 
 			final Label lblNewBranch = new Label(creationContainer, SWT.NONE);
 			lblNewBranch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
 				false, false, 1, 1));
 			lblNewBranch.setText("New Branch:");
 
 			text = new Text(creationContainer, SWT.BORDER);
 			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
 				1, 1));
 		}
 	}
 }

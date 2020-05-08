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
 package org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.views;
 
 import java.security.cert.X509Certificate;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.client.model.exceptions.CertificateStoreException;
 import org.eclipse.emf.emfstore.client.model.exceptions.InvalidCertificateException;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 
 /**
  * The modified ElementListSelectionDialog. Includes further functionality to
  * import certificates from files instead of choosing from the list.
  * 
  * @author pfeifferc
  */
 public class CertificateSelectionDialog extends ElementListSelectionDialog {
 
 	private TableItem selectedTableItem;
 	private String alias = "";
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param parent
 	 *            Parent
 	 * @param renderer
 	 *            Renderer
 	 */
 	public CertificateSelectionDialog(Shell parent, ILabelProvider renderer) {
 		super(parent, renderer);
 		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
 	}
 
 	/**
 	 * Overridden method to allow adding further elements onto the dialog
 	 * composite.
 	 * 
 	 * @see org.eclipse.ui.dialogs.ElementListSelectionDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
 	 * @return Control
 	 * @param parent
 	 *            Parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		// standard layout used by dialogue area
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
 		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
 		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
 		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
 
 		// two column layout composite
 		Composite grid = new Composite(parent, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(grid);
 
 		// left column composite
 		Composite left = new Composite(grid, SWT.NONE);
 		GridLayoutFactory.fillDefaults().applyTo(left);
 
 		// right column composite
 		Composite right = new Composite(grid, SWT.NONE);
 		GridLayoutFactory.createFrom(layout).margins(layout.marginWidth, 35).applyTo(right);
 		GridDataFactory.fillDefaults().grab(true, true).hint(300, 200).applyTo(right);
 		applyDialogFont(right);
 
 		// right column: certificate details
 		new Label(right, SWT.NONE).setText("Certificate Alias: ");
 		final Text certAlias = new Text(right, SWT.NONE);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(certAlias);
 		certAlias.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 		certAlias.setEditable(false);
 		new Label(right, SWT.NONE).setText("Certificate Details: ");
 		final Text certDetails = new Text(right, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
 		GridDataFactory.fillDefaults().grab(true, false).hint(300, 230).applyTo(certDetails);
 		certDetails.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 		certDetails.setEditable(false);
 
 		// left column: dialogue area composite (displays certificates and
 		// filter)
 		Composite dialogArea = new Composite(left, SWT.NONE);
 		GridLayoutFactory.fillDefaults().applyTo(dialogArea);
 		Control control = super.createDialogArea(dialogArea);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
 
 		// left column: import button, composite used to ensure correct
 		// alignment
 		Composite certButtonsComposite = new Composite(grid, SWT.NONE);
 		GridLayoutFactory.createFrom(layout).numColumns(3).equalWidth(true).margins(layout.marginWidth, 0)
 			.applyTo(certButtonsComposite);
 		applyDialogFont(certButtonsComposite);
 		Button browse = new Button(certButtonsComposite, SWT.NONE);
 		browse.setText("Import...");
 		browse.addSelectionListener(new CertificateSelectionListener());
 
 		// Delete certificate
 		Button delete = new Button(certButtonsComposite, SWT.NONE);
 		delete.setText("Delete");
 		delete.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// nothing to do
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				if (selectedTableItem != null && !selectedTableItem.equals("")) {
 					String alias = selectedTableItem.getText();
 					try {
 						KeyStoreManager.getInstance().deleteCertificate(alias);
 						setListElements(KeyStoreManager.getInstance().getCertificates().toArray());
 					} catch (CertificateStoreException e1) {
 						setErrorMessage(e1.getMessage());
 					}
 				}
 			}
 		});
 		fFilteredList.addSelectionListener(new SelectionListenerImplementation(certDetails, certAlias));
 		return control;
 	}
 
 	/**
	 * @return alias
 	 */
	protected String getCertificateAlias() {
 		return alias;
 	}
 
 	/**
	 * @param message
	 *            error message
 	 */
	protected void setErrorMessage(String message) {
		MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Attention", message);
 	}
 
 	/**
 	 * Certificate Selection Listener.
 	 * 
 	 * @author koegel
 	 * 
 	 */
 	private final class SelectionListenerImplementation implements SelectionListener {
 		private final Text certDetails;
 		private final Text certAlias;
 
 		private SelectionListenerImplementation(Text certDetails, Text certAlias) {
 			this.certDetails = certDetails;
 			this.certAlias = certAlias;
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			// nothing to do
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			if (((Table) e.getSource()).getItems().length > 0) {
 				selectedTableItem = ((Table) e.getSource()).getItems()[((Table) e.getSource()).getSelectionIndex()];
 				alias = selectedTableItem.getText();
 				try {
 					X509Certificate selectedCertificate = (X509Certificate) KeyStoreManager.getInstance()
 						.getCertificate(alias);
 					String[] details = selectedCertificate.toString().split("\n");
 					String tmp = "";
 					for (int i = 2; i < 14; i++) {
 						tmp += (i == 7 || i == 8) ? "" : details[i].trim() + "\n";
 					}
 					certAlias.setText(alias);
 					certDetails.setText(tmp);
 				} catch (CertificateStoreException e1) {
 					setErrorMessage(e1.getMessage());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Choose certificate from file system and name it.
 	 * 
 	 * @author pfeifferc
 	 */
 	class CertificateSelectionListener implements SelectionListener {
 		/**
 		 * Add a certificate specified by the user.
 		 * 
 		 * @param e
 		 *            selection event
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell());
 			fileDialog.open();
 			if (!fileDialog.getFileName().equals("")) {
 				String location = fileDialog.getFilterPath() + System.getProperty("file.separator")
 					+ fileDialog.getFileName();
 
 				InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(),
 					"Select certificate designation",
 					"Please choose a designation for the previously selected certificate: ", "", null);
 
 				inputDialog.setBlockOnOpen(true);
 				if (inputDialog.open() != Window.OK) {
 					return;
 				}
 
 				String alias = inputDialog.getValue();
 				if (alias.equals("")) {
 					alias = "unnamed:" + EcoreUtil.generateUUID();
 				}
 				try {
 					KeyStoreManager.getInstance().addCertificate(alias, location);
 				} catch (final InvalidCertificateException e1) {
 					setErrorMessage("Invalid certificate!");
 				} catch (CertificateStoreException e1) {
 					setErrorMessage(e1.getMessage());
 				}
 				try {
 					setListElements(KeyStoreManager.getInstance().getCertificates().toArray());
 				} catch (CertificateStoreException e1) {
 					setErrorMessage(e1.getMessage());
 				}
 			}
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 * @param e
 		 *            selection event
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			// nothing to do
 		}
 	}
 }

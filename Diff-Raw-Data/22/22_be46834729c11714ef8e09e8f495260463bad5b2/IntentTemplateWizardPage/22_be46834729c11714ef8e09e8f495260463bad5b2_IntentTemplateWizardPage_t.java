 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.ide.wizards;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.Activator;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * The Intent project creation wizard.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentTemplateWizardPage extends WizardPage {
 	private static final String TEMPLATES_EXTENSION_POINT = "org.eclipse.mylyn.docs.intent.client.ui.ide.template.extension"; //$NON-NLS-1$
 
 	private static Map<String, String[]> templateExtensionsByName;
 
 	private Label descriptionLabel;
 
 	private StyledText previewText;
 
 	private Combo combo;
 
 	/**
 	 * Creates a new wizard page.
 	 */
 	public IntentTemplateWizardPage() {
 		super("IntentTemplateWizardPage");
 		setDescription("Initializes the Intent project using one of the installed templates.");
 		initializeRegistry();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(Composite parent) {
 		// Main composite
 		final Composite control = new Composite(parent, SWT.NULL);
 		setControl(control);
 		control.setLayout(new GridLayout(2, false));
 
 		final Label extSelectionLabel = new Label(control, SWT.NONE);
 		extSelectionLabel.setText("Select an installed template: ");
 
 		combo = new Combo(control, SWT.NONE);
 		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		Label infoLabel1 = new Label(control, SWT.NONE);
 		infoLabel1.setText("Description: ");
 		descriptionLabel = new Label(control, SWT.NONE);
 		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
 
 		Label infoLabel2 = new Label(control, SWT.NONE);
 		infoLabel2.setText("Preview: ");
 		previewText = new StyledText(control, SWT.BORDER);
 		previewText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		previewText.setEditable(false);
 
 		combo.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				String[] template = templateExtensionsByName.get(combo.getText());
 				descriptionLabel.setText(template[0]);
 				try {
 					previewText.setText(getContent(template[1]));
 				} catch (IOException error) {
 					IntentUiLogger.logError(error);
 				}
 				setPageComplete(validate());
 			}
 
 		});
 
 		for (String templateName : templateExtensionsByName.keySet()) {
 			combo.add(templateName);
 		}
 
 		setPageComplete(validate());
 	}
 
 	/**
 	 * Returns the initial content according to the selected template.
 	 * 
 	 * @return the initial content
 	 */
 	public String getContent() {
 		try {
 			return getContent(templateExtensionsByName.get(combo.getText())[1]);
 		} catch (IOException e) {
 			IntentUiLogger.logError(e);
 		}
 		return null;
 	}
 
 	private static void initializeRegistry() {
 		if (templateExtensionsByName == null) {
 			templateExtensionsByName = new HashMap<String, String[]>();
 			IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
 					TEMPLATES_EXTENSION_POINT);
 			for (IConfigurationElement element : elements) {
 				String name = element.getAttribute("name");
 				String description = element.getAttribute("description");
 				String template = element.getAttribute("template");
 				if (getURL(template) != null) {
 					templateExtensionsByName.put(name, new String[] {description, template,
 					});
 				}
 			}
 		}
 	}
 
 	private static String getContent(String filePath) throws IOException {
 		URL url = getURL(filePath);
 		String result = "";
 		InputStream fis = url.openStream();
 		BufferedInputStream bis = null;
 		BufferedReader dis = null;
 		StringBuffer sb = new StringBuffer();
 
 		bis = new BufferedInputStream(fis);
 		dis = new BufferedReader(new InputStreamReader(bis));
 
 		while (dis.ready()) {
 			sb.append(dis.readLine() + "\n");
 		}
 
 		fis.close();
 		bis.close();
 		dis.close();
 
 		result = sb.toString();
 		return result;
 	}
 
 	private static URL getURL(String filePath) {
 		return Activator.getDefault().getBundle().getEntry(filePath);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
 	 */
 	@Override
 	public boolean isPageComplete() {
 		return validate();
 	}
 
 	private boolean validate() {
		boolean isValid = combo.getText() != null && !"".equals(combo.getText());
		if (isValid) {
			setErrorMessage(null);
		} else {
			setErrorMessage("A template must be selected");
 		}
		return isValid;
 	}

 }

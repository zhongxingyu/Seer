 /*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.ui.editors;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.ui.IJavaElementSearchConstants;
 import org.eclipse.pde.core.plugin.IPlugin;
 import org.eclipse.pde.core.plugin.IPluginModelBase;
 import org.eclipse.pde.internal.ui.PDEPlugin;
 import org.eclipse.pde.internal.ui.PDEUIMessages;
 import org.eclipse.pde.internal.ui.editor.FormEntryAdapter;
 import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
 import org.eclipse.pde.internal.ui.editor.PDEFormPage;
 import org.eclipse.pde.internal.ui.editor.contentassist.TypeFieldAssistDisposer;
 import org.eclipse.pde.internal.ui.editor.plugin.JavaAttributeValue;
 import org.eclipse.pde.internal.ui.parts.FormEntry;
 import org.eclipse.pde.internal.ui.util.PDEJavaHelperUI;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 
 /**
  * @author Christian Dupuis
  * @author Martin Lippert
  */
 public class BundleGeneralInfoSection extends AbstractPdeGeneralInfoSection {
 
 	private FormEntry fClassEntry;
 
 	private TypeFieldAssistDisposer fTypeFieldAssistDisposer;
 
 	public BundleGeneralInfoSection(PDEFormPage page, Composite parent) {
 		super(page, parent);
 	}
 
 	@Override
 	protected void createSpecificControls(Composite parent, FormToolkit toolkit, IActionBars actionBars) {
 		createClassEntry(parent, toolkit, actionBars);
 	}
 
 	@Override
 	protected void createClient(Section section, FormToolkit toolkit) {
 		section.setText(PDEUIMessages.ManifestEditor_PluginSpecSection_title);
 		section.setLayout(FormLayoutFactory.createClearTableWrapLayout(false, 1));
 		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
 		section.setLayoutData(data);
 
 		section.setDescription(getSectionDescription());
 		Composite client = toolkit.createComposite(section);
 		client.setLayout(FormLayoutFactory.createSectionClientTableWrapLayout(false, 3));
 		section.setClient(client);
 
 		IActionBars actionBars = getPage().getPDEEditor().getEditorSite().getActionBars();
 		createIDEntry(client, toolkit, actionBars);
 		createVersionEntry(client, toolkit, actionBars);
 		createNameEntry(client, toolkit, actionBars);
 		createProviderEntry(client, toolkit, actionBars);
 		createSpecificControls(client, toolkit, actionBars);
 		toolkit.paintBordersFor(client);
 
 		addListeners();
 	}
 
 	private void createClassEntry(Composite client, FormToolkit toolkit, IActionBars actionBars) {
 		boolean isEditable = isEditable();
 		fClassEntry = new FormEntry(client, toolkit, PDEUIMessages.GeneralInfoSection_class,
 				PDEUIMessages.GeneralInfoSection_browse, //
 				isEditable());
 		fClassEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {
 			@Override
 			public void textValueChanged(FormEntry entry) {
 				try {
 					((IPlugin) getPluginBase()).setClassName(entry.getValue());
 				}
 				catch (CoreException e) {
 					PDEPlugin.logException(e);
 				}
 			}
 
 			@Override
 			public void linkActivated(HyperlinkEvent e) {
 				String value = fClassEntry.getValue();
 				IProject project = getPage().getPDEEditor().getCommonProject();
 				value = PDEJavaHelperUI.createClass(value, project, createJavaAttributeValue(), false);
 				if (value != null) {
 					fClassEntry.setValue(value);
 				}
 			}
 
 			@Override
 			public void browseButtonSelected(FormEntry entry) {
 				doOpenSelectionDialog(entry.getValue());
 			}
 		});
 		fClassEntry.setEditable(isEditable);
 
 		if (isEditable) {
 			fTypeFieldAssistDisposer = PDEJavaHelperUI.addTypeFieldAssistToText(fClassEntry.getText(), getProject(),
 					IJavaSearchConstants.CLASS);
 		}
 	}
 
 	private JavaAttributeValue createJavaAttributeValue() {
 		IProject project = getPage().getPDEEditor().getCommonProject();
 		IPluginModelBase model = (IPluginModelBase) getPage().getModel();
 		return new JavaAttributeValue(project, model, null, fClassEntry.getValue());
 	}
 
 	private void doOpenSelectionDialog(String className) {
 		IResource resource = getPluginBase().getModel().getUnderlyingResource();
 		String type = PDEJavaHelperUI.selectType(resource, IJavaElementSearchConstants.CONSIDER_CLASSES, className,
 				null);
 		if (type != null) {
 			fClassEntry.setValue(type);
 		}
 	}
 
 	@Override
 	protected String getSectionDescription() {
 		return "This section describes general information about this bundle";
 	}
 
 	/** For JUnit testing only * */
 	public String getBundleName() {
 		return fNameEntry.getText().getText();
 	}
 
 	public void dispose() {
 		super.dispose();
 		if (fTypeFieldAssistDisposer != null) {
 			fTypeFieldAssistDisposer.dispose();
 		}
 	}
 
 }

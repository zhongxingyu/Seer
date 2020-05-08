 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, staticFieldSecurity, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the tabbed property section for the File Context model 
  * element
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.tabbed;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedFile;
import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class FileContextTabPropertySection extends ModelElementTabPropertySection {
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fBaseFileNameText.
 	 */
 	protected CLabel fBaseFileNameText = null;
 
 	/**
 	 * Field fBaseFilePathAbsoluteText.
 	 */
 	protected CLabel fBaseFilePathAbsoluteText = null;
 
 	/**
 	 * Field fBaseFilePathProjectText.
 	 */
 	protected CLabel fBaseFilePathProjectText = null;
 
 	/**
 	 * Field fBaseFilePathRepositoryText.
 	 */
 	protected CLabel fBaseFilePathRepositoryText = null;
 
 	/**
 	 * Field fBaseFileVersionText.
 	 */
 	protected CLabel fBaseFileVersionText = null;
 
 	/**
 	 * Field fTargetFileNameText.
 	 */
 	protected CLabel fTargetFileNameText = null;
 
 	/**
 	 * Field fTargetFilePathAbsoluteText.
 	 */
 	protected CLabel fTargetFilePathAbsoluteText = null;
 
 	/**
 	 * Field fTargetFilePathProjectText.
 	 */
 	protected CLabel fTargetFilePathProjectText = null;
 
 	/**
 	 * Field fTargetFilePathRepositoryText.
 	 */
 	protected CLabel fTargetFilePathRepositoryText = null;
 
 	/**
 	 * Field fTargetFileVersionText.
 	 */
 	protected CLabel fTargetFileVersionText = null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method createControls.
 	 * 
 	 * @param parent
 	 *            Composite
 	 * @param aTabbedPropertySheetPage
 	 *            TabbedPropertySheetPage
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(Composite, TabbedPropertySheetPage)
 	 */
 	@Override
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 
 		//Tell element to build its own detailed tab layout
 		final TabbedPropertySheetWidgetFactory widgetFactory = aTabbedPropertySheetPage.getWidgetFactory();
 		final Composite composite = widgetFactory.createFlatFormComposite(parent);
 		FormData data = null;
 
 		//Target File Version composite (read-only)
 		final Composite targetFileComposite = widgetFactory.createGroup(composite, "Target File");
 		final FormLayout targetFileLayout = new FormLayout();
 		targetFileComposite.setLayout(targetFileLayout);
 		createTargetFileVersionComposite(targetFileComposite, widgetFactory);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		targetFileComposite.setLayoutData(data);
 
 		//Base File Version composite (read-only)
 		final Group baseFileComposite = widgetFactory.createGroup(composite, "Base File");
 		final FormLayout baseFileLayout = new FormLayout();
 		baseFileComposite.setLayout(baseFileLayout);
 		createBaseFileVersionComposite(baseFileComposite, widgetFactory);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(targetFileComposite, ITabbedPropertyConstants.VSPACE);
 		baseFileComposite.setLayoutData(data);
 	}
 
 	/**
 	 * Method createBaseFileVersionComposite.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 */
 	private void createBaseFileVersionComposite(final Composite aParent, TabbedPropertySheetWidgetFactory aWidgetFactory) {
 
 		//File Name (read-only)
 		fBaseFileNameText = aWidgetFactory.createCLabel(aParent, "");
 		FormData data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(aParent, ITabbedPropertyConstants.VSPACE);
 		fBaseFileNameText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_NAME_TOOLTIP);
 		fBaseFileNameText.setLayoutData(data);
 
 		final CLabel fileNameLabel = aWidgetFactory.createCLabel(aParent, R4EUIConstants.NAME_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fBaseFileNameText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fBaseFileNameText, 0, SWT.CENTER);
 		fileNameLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_NAME_TOOLTIP);
 		fileNameLabel.setLayoutData(data);
 
 		//File Version (read-only)
 		fBaseFileVersionText = aWidgetFactory.createCLabel(aParent, "");
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fBaseFileNameText, ITabbedPropertyConstants.VSPACE);
 		fBaseFileVersionText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_VERSION_TOOLTIP);
 		fBaseFileVersionText.setLayoutData(data);
 
 		final CLabel fileVersionLabel = aWidgetFactory.createCLabel(aParent, R4EUIConstants.VERSION_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fBaseFileVersionText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fBaseFileVersionText, 0, SWT.TOP);
 		fileVersionLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_VERSION_TOOLTIP);
 		fileVersionLabel.setLayoutData(data);
 
 		//Path information section
 		final ExpandableComposite pathSection = aWidgetFactory.createExpandableComposite(aParent,
 				ExpandableComposite.TWISTIE);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fBaseFileVersionText, ITabbedPropertyConstants.VSPACE);
 		pathSection.setLayoutData(data);
 		pathSection.setText(R4EUIConstants.PATH_INFORMATION_LABEL);
 		pathSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				final ScrolledComposite scrolledParent = (ScrolledComposite) aParent.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent();
 				scrolledParent.setMinSize(aParent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				scrolledParent.layout(true, true);
 			}
 		});
 		pathSection.setLayout(new GridLayout(1, false));
 
 		final Composite pathSectionClient = aWidgetFactory.createComposite(pathSection);
 		pathSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		pathSectionClient.setLayout(new GridLayout(4, false));
 		pathSection.setClient(pathSectionClient);
 
 		//Repository File Path (read-only)
 		final CLabel filePathRepositoryLabel = aWidgetFactory.createCLabel(pathSectionClient,
 				R4EUIConstants.PATH_REPOSITORY_LABEL);
 		GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		filePathRepositoryLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_REPOSITORY_TOOLTIP);
 		filePathRepositoryLabel.setLayoutData(gridData);
 
 		fBaseFilePathRepositoryText = aWidgetFactory.createCLabel(pathSectionClient, "");
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fBaseFilePathRepositoryText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_REPOSITORY_TOOLTIP);
 		fBaseFilePathRepositoryText.setLayoutData(gridData);
 
 		//Absolute File Path (read-only)
 		final CLabel filePathAbsoluteLabel = aWidgetFactory.createCLabel(pathSectionClient,
 				R4EUIConstants.PATH_ABSOLUTE_LABEL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		filePathAbsoluteLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_ABSOLUTE_TOOLTIP);
 		filePathAbsoluteLabel.setLayoutData(gridData);
 
 		fBaseFilePathAbsoluteText = aWidgetFactory.createCLabel(pathSectionClient, "");
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fBaseFilePathAbsoluteText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_ABSOLUTE_TOOLTIP);
 		fBaseFilePathAbsoluteText.setLayoutData(gridData);
 
 		//Project Relative File Path (read-only)
 		final CLabel filePathProjectLabel = aWidgetFactory.createCLabel(pathSectionClient,
 				R4EUIConstants.PATH_PROJECT_LABEL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		filePathProjectLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_PROJECT_TOOLTIP);
 		filePathProjectLabel.setLayoutData(gridData);
 
 		fBaseFilePathProjectText = aWidgetFactory.createCLabel(pathSectionClient, "");
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fBaseFilePathProjectText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_PROJECT_TOOLTIP);
 		fBaseFilePathProjectText.setLayoutData(gridData);
 	}
 
 	/**
 	 * Method createTargetFileVersionComposite.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 */
 	private void createTargetFileVersionComposite(final Composite aParent,
 			TabbedPropertySheetWidgetFactory aWidgetFactory) {
 		FormData data = null;
 
 		//File Name (read-only)
 		fTargetFileNameText = aWidgetFactory.createCLabel(aParent, "");
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(aParent, ITabbedPropertyConstants.VSPACE);
 		fTargetFileNameText.setToolTipText(R4EUIConstants.FILECONTEXT_TARGET_FILE_NAME_TOOLTIP);
 		fTargetFileNameText.setLayoutData(data);
 
 		final CLabel fileNameLabel = aWidgetFactory.createCLabel(aParent, R4EUIConstants.NAME_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fTargetFileNameText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fTargetFileNameText, 0, SWT.CENTER);
 		fileNameLabel.setToolTipText(R4EUIConstants.FILECONTEXT_TARGET_FILE_NAME_TOOLTIP);
 		fileNameLabel.setLayoutData(data);
 
 		//File Version (read-only)
 		fTargetFileVersionText = aWidgetFactory.createCLabel(aParent, "");
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fTargetFileNameText, ITabbedPropertyConstants.VSPACE);
 		fTargetFileVersionText.setToolTipText(R4EUIConstants.FILECONTEXT_TARGET_FILE_VERSION_TOOLTIP);
 		fTargetFileVersionText.setLayoutData(data);
 
 		final CLabel fileVersionLabel = aWidgetFactory.createCLabel(aParent, R4EUIConstants.VERSION_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fTargetFileVersionText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fTargetFileVersionText, 0, SWT.TOP);
 		fileVersionLabel.setToolTipText(R4EUIConstants.FILECONTEXT_TARGET_FILE_VERSION_TOOLTIP);
 		fileVersionLabel.setLayoutData(data);
 
 		//Path information section
 		final ExpandableComposite pathSection = aWidgetFactory.createExpandableComposite(aParent,
 				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fTargetFileVersionText, ITabbedPropertyConstants.VSPACE);
 		pathSection.setLayoutData(data);
 		pathSection.setText(R4EUIConstants.PATH_INFORMATION_LABEL);
 		pathSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				final ScrolledComposite scrolledParent = (ScrolledComposite) aParent.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent();
 				scrolledParent.setMinSize(aParent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				scrolledParent.layout(true, true);
 			}
 		});
 		pathSection.setLayout(new GridLayout(1, false));
 
 		final Composite pathSectionClient = aWidgetFactory.createComposite(pathSection);
 		pathSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		pathSectionClient.setLayout(new GridLayout(4, false));
 		pathSection.setClient(pathSectionClient);
 
 		//Repository File Path (read-only)
 		final CLabel filePathRepositoryLabel = aWidgetFactory.createCLabel(pathSectionClient,
 				R4EUIConstants.PATH_REPOSITORY_LABEL);
 		GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		filePathRepositoryLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_REPOSITORY_TOOLTIP);
 		filePathRepositoryLabel.setLayoutData(gridData);
 
 		fTargetFilePathRepositoryText = aWidgetFactory.createCLabel(pathSectionClient, "");
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fTargetFilePathRepositoryText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_REPOSITORY_TOOLTIP);
 		fTargetFilePathRepositoryText.setLayoutData(gridData);
 
 		//Absolute File Path (read-only)
 		final CLabel filePathAbsoluteLabel = aWidgetFactory.createCLabel(pathSectionClient,
 				R4EUIConstants.PATH_ABSOLUTE_LABEL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		filePathAbsoluteLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_ABSOLUTE_TOOLTIP);
 		filePathAbsoluteLabel.setLayoutData(gridData);
 
 		fTargetFilePathAbsoluteText = aWidgetFactory.createCLabel(pathSectionClient, "");
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fTargetFilePathAbsoluteText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_ABSOLUTE_TOOLTIP);
 		fTargetFilePathAbsoluteText.setLayoutData(gridData);
 
 		//Project Relative File Path (read-only)
 		final CLabel filePathProjectLabel = aWidgetFactory.createCLabel(pathSectionClient,
 				R4EUIConstants.PATH_PROJECT_LABEL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		filePathProjectLabel.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_PROJECT_TOOLTIP);
 		filePathProjectLabel.setLayoutData(gridData);
 
 		fTargetFilePathProjectText = aWidgetFactory.createCLabel(pathSectionClient, "");
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fTargetFilePathProjectText.setToolTipText(R4EUIConstants.FILECONTEXT_BASE_FILE_PATH_PROJECT_TOOLTIP);
 		fTargetFilePathProjectText.setLayoutData(gridData);
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final R4EFileContext modelFile = ((R4EUIFileContext) fProperties.getElement()).getFileContext();
 		final R4EFileVersion baseVersion = modelFile.getBase();
 		if (null != modelFile.getBase()) {
 			fBaseFileNameText.setText(baseVersion.getName());
 			final IResource baseResource = baseVersion.getResource();
 			//The properties shows the absolute, project relative and repository path
 			fBaseFilePathRepositoryText.setText(baseVersion.getRepositoryPath());
 			if (null != baseResource) {
				if (CommandUtils.useWorkspaceResource(baseVersion)) {
					fBaseFilePathAbsoluteText.setText(baseResource.getLocation().toPortableString());
				} else {
					fBaseFilePathAbsoluteText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
				}
 				fBaseFilePathProjectText.setText(baseResource.getProjectRelativePath().toPortableString());
 			} else {
				fBaseFilePathAbsoluteText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
				fBaseFilePathProjectText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
 			}
 			fBaseFileVersionText.setText(baseVersion.getVersionID());
 		} else {
 			fBaseFileNameText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
 			fBaseFilePathRepositoryText.setText("");
 			fBaseFilePathAbsoluteText.setText("");
 			fBaseFilePathProjectText.setText("");
 			fBaseFileVersionText.setText("");
 		}
 
 		final R4EFileVersion targetVersion = modelFile.getTarget();
 		if (null != targetVersion) {
 			fTargetFileNameText.setText(targetVersion.getName());
 			final IResource targetResource = targetVersion.getResource();
 			//The properties shows the absolute, project relative and repository path
 			fTargetFilePathRepositoryText.setText(targetVersion.getRepositoryPath());
 			if (null != targetResource) {
				if (CommandUtils.useWorkspaceResource(targetVersion)) {
					fTargetFilePathAbsoluteText.setText(targetResource.getLocation().toPortableString());
				} else {
					fTargetFilePathAbsoluteText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
				}
 				fTargetFilePathProjectText.setText(targetResource.getProjectRelativePath().toPortableString());
 			} else {
				fTargetFilePathAbsoluteText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
				fTargetFilePathProjectText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
 			}
 			fTargetFileVersionText.setText(targetVersion.getVersionID());
 		} else {
 			fTargetFileNameText.setText(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
 			fTargetFilePathRepositoryText.setText("");
 			fTargetFilePathAbsoluteText.setText("");
 			fTargetFilePathProjectText.setText("");
 			fTargetFileVersionText.setText("");
 		}
 		setEnabledFields();
 		fRefreshInProgress = false;
 	}
 
 	/**
 	 * Method setEnabledFields.
 	 */
 	@Override
 	protected void setEnabledFields() {
 		//Since it is a subclass of this classe we need this here.  This should be improved later
 		//Do not show FileContext elements for the Postponed file.
 		if (fProperties.getElement() instanceof R4EUIPostponedFile) {
 			fBaseFileNameText.getParent().getParent().setVisible(false);
 			return;
 		}
 
 		if (R4EUIModelController.isJobInProgress()) {
 			fBaseFileNameText.setEnabled(false);
 			fBaseFilePathRepositoryText.setEnabled(false);
 			fBaseFilePathAbsoluteText.setEnabled(false);
 			fBaseFilePathProjectText.setEnabled(false);
 			fBaseFileVersionText.setEnabled(false);
 			fTargetFileNameText.setEnabled(false);
 			fTargetFilePathRepositoryText.setEnabled(true);
 			fTargetFilePathAbsoluteText.setEnabled(false);
 			fTargetFilePathProjectText.setEnabled(false);
 			fTargetFileVersionText.setEnabled(false);
 		} else {
 			fBaseFileNameText.setEnabled(true);
 			fBaseFilePathRepositoryText.setEnabled(true);
 			fBaseFilePathAbsoluteText.setEnabled(true);
 			fBaseFilePathProjectText.setEnabled(true);
 			fBaseFileVersionText.setEnabled(true);
 			fTargetFileNameText.setEnabled(true);
 			fTargetFilePathRepositoryText.setEnabled(true);
 			fTargetFilePathAbsoluteText.setEnabled(true);
 			fTargetFilePathProjectText.setEnabled(true);
 			fTargetFileVersionText.setEnabled(true);
 		}
 	}
 }

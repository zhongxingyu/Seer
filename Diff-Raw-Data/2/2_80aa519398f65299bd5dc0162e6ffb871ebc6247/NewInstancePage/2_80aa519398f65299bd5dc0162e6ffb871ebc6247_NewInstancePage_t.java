 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.internal.deltacloud.ui.wizards;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateSetStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeanProperties;
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.core.databinding.observable.IObservable;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
 import org.eclipse.jface.databinding.swt.WidgetProperties;
 import org.eclipse.jface.databinding.wizard.WizardPageSupport;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.jboss.tools.common.log.StatusFactory;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudHardwareProfile;
 import org.jboss.tools.deltacloud.core.DeltaCloudImage;
 import org.jboss.tools.deltacloud.core.DeltaCloudKey;
 import org.jboss.tools.deltacloud.core.DeltaCloudRealm;
 import org.jboss.tools.deltacloud.core.job.AbstractCloudElementJob;
 import org.jboss.tools.deltacloud.core.job.AbstractCloudElementJob.CLOUDELEMENT;
 import org.jboss.tools.deltacloud.ui.Activator;
 import org.jboss.tools.deltacloud.ui.SWTImagesFactory;
 import org.jboss.tools.internal.deltacloud.ui.common.databinding.validator.MandatoryStringValidator;
 import org.jboss.tools.internal.deltacloud.ui.common.databinding.validator.SelectedComboItemValidator;
 import org.jboss.tools.internal.deltacloud.ui.utils.DataBindingUtils;
 import org.jboss.tools.internal.deltacloud.ui.utils.ContentProposalFactory;
 
 /**
  * @author Jeff Jonston
  * @author André Dietisheim
  */
 public class NewInstancePage extends WizardPage {
 
 	private static final String NAME_PROPOSAL_KEY = "instance/name";
 	private static final String IMAGE_PROPOSAL_KEY = "instance/image";
 	private static final String KEY_PROPOSAL_KEY = "instance/key";
 
 	private static final int IMAGE_CHECK_DELAY = 500;
 	private static final int KEY_CHECK_DELAY = 500;
 
 	private final static String NAME = "NewInstance.name"; //$NON-NLS-1$
 	private final static String DESCRIPTION = "NewInstance.desc"; //$NON-NLS-1$
 	private final static String TITLE = "NewInstance.title"; //$NON-NLS-1$
 
 	private static final String NAME_LABEL = "Name.label"; //$NON-NLS-1$
 	private static final String IMAGE_LABEL = "Image.label"; //$NON-NLS-1$
 	private static final String ARCH_LABEL = "Arch.label"; //$NON-NLS-1$
 	private static final String HARDWARE_LABEL = "Profile.label"; //$NON-NLS-1$
 	private static final String REALM_LABEL = "Realm.label"; //$NON-NLS-1$
 	private static final String KEY_LABEL = "Key.label"; //$NON-NLS-1$
 	private static final String MANAGE_BUTTON_LABEL = "ManageButton.label"; //$NON-NLS-1$
 	private static final String FIND_BUTTON_LABEL = "FindButton.label"; //$NON-NLS-1$
 	private static final String PROPERTIES_LABEL = "Properties.label"; //$NON-NLS-1$
 	private static final String MUST_ENTER_A_NAME = "ErrorMustProvideName.text"; //$NON-NLS-1$	
 	private static final String MUST_ENTER_A_KEYNAME = "ErrorMustProvideKeyName.text"; //$NON-NLS-1$	
 	private static final String MUST_ENTER_IMAGE_ID = "ErrorMustProvideImageId.text"; //$NON-NLS-1$	
 	private static final String LOADING_VALUE = "Loading.value"; //$NON-NLS-1$
 	private static final String IMAGE_ID_NOT_FOUND = "ErrorImageIdNotFound.text"; //$NON-NLS-1$
 
 	private Composite container;
 	private NewInstancePageModel model;
 	private DeltaCloud cloud;
 	private Label arch;
 	private Text nameText;
 	private Text imageText;
 	private Text keyText;
 	private Combo realmCombo;
 	private Combo hardwareCombo;
 	private Map<String, ProfilePage> profilePages = new HashMap<String, ProfilePage>();
 	private StackLayout groupContainerStackLayout;
 
 	private Group groupContainer;
 
 	private SelectionListener manageListener = new SelectionAdapter() {
 
 		public void widgetSelected(SelectionEvent event) {
 			Shell shell = getShell();
 			ManageKeysWizard wizard = new ManageKeysWizard(cloud); //$NON-NLS-1$
 			WizardDialog dialog = new CustomWizardDialog(shell, wizard,
 					IDialogConstants.OK_LABEL);
 			dialog.create();
 			dialog.open();
 			DeltaCloudKey key = wizard.getKey();
 			if (key != null) {
 				keyText.setText(key.getId());
 			}
 		}
 	};
 
 	private SelectionListener findImageButtonListener = new SelectionAdapter() {
 
 		public void widgetSelected(SelectionEvent event) {
 			Shell shell = getShell();
 			FindImageWizard wizard = new FindImageWizard(cloud);
 			WizardDialog dialog = new CustomWizardDialog(shell, wizard, IDialogConstants.OK_LABEL);
 			dialog.create();
 			dialog.open();
 			String imageId = wizard.getImageId();
 			if (imageId != null) {
 				imageText.setText(imageId);
 			}
 		}
 	};
 
 	public NewInstancePage(DeltaCloud cloud, DeltaCloudImage image) {
 		super(WizardMessages.getString(NAME));
 		this.cloud = cloud;
 		String defaultKeyname = cloud.getLastKeyname();
 		model = new NewInstancePageModel(defaultKeyname, image); //$NON-NLS-1$
 		setDescription(WizardMessages.getString(DESCRIPTION));
 		setTitle(WizardMessages.getString(TITLE));
 		setImageDescriptor(SWTImagesFactory.DESC_DELTA_LARGE);
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		DataBindingContext dbc = new DataBindingContext();
 		WizardPageSupport.create(this, dbc);
 		this.container = createWidgets(parent);
 		setControl(container);
 		bindWidgets(dbc, container);
 		asyncGetProfiles(model, cloud);
 		asyncGetRealms(model, cloud);
 	}
 
 	private Composite createWidgets(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(container);
 
 		Label nameLabel = new Label(container, SWT.NULL);
 		nameLabel.setText(WizardMessages.getString(NAME_LABEL));
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(nameLabel);
 		this.nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		ContentProposalFactory.addPreferencesProposalAdapter(nameText, NAME_PROPOSAL_KEY);
 		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(nameText);
 
 		Label imageLabel = new Label(container, SWT.NULL);
 		imageLabel.setText(WizardMessages.getString(IMAGE_LABEL));
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(imageLabel);
 		this.imageText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		ContentProposalFactory.addPreferencesProposalAdapter(imageText, IMAGE_PROPOSAL_KEY);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(imageText);
 		Button findImageButton = new Button(container, SWT.NULL);
 		findImageButton.setText(WizardMessages.getString(FIND_BUTTON_LABEL));
 		findImageButton.addSelectionListener(findImageButtonListener);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(findImageButton);
 
 		Label archLabel = new Label(container, SWT.NULL);
 		archLabel.setText(WizardMessages.getString(ARCH_LABEL));
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(archLabel);
 		arch = new Label(container, SWT.NULL);
 		GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(arch);
 
 		Label realmLabel = new Label(container, SWT.NULL);
 		realmLabel.setText(WizardMessages.getString(REALM_LABEL));
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(realmLabel);
 		this.realmCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
 		realmCombo.setItems(new String[] { WizardMessages.getString(LOADING_VALUE) });
 		realmCombo.select(0);
 		GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(realmCombo);
 
 		Label keyLabel = new Label(container, SWT.NULL);
 		keyLabel.setText(WizardMessages.getString(KEY_LABEL));
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(keyLabel);
 		keyText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		ContentProposalFactory.addPreferencesProposalAdapter(keyText, KEY_PROPOSAL_KEY);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(keyText);
 		Button keyManageButton = new Button(container, SWT.NULL);
 		keyManageButton.setText(WizardMessages.getString(MANAGE_BUTTON_LABEL));
 		keyManageButton.addSelectionListener(manageListener);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(keyManageButton);
 
 		Label hardwareLabel = new Label(container, SWT.NULL);
 		hardwareLabel.setText(WizardMessages.getString(HARDWARE_LABEL));
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(hardwareLabel);
 		this.hardwareCombo = new Combo(container, SWT.READ_ONLY);
 		hardwareCombo.setItems(new String[] { WizardMessages.getString(LOADING_VALUE) });
 		hardwareCombo.select(0);
 		GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(hardwareCombo);
 
 		this.groupContainer = new Group(container, SWT.BORDER);
 		groupContainer.setText(WizardMessages.getString(PROPERTIES_LABEL));
 		GridDataFactory.fillDefaults().span(3, 5).hint(SWT.DEFAULT, 100).applyTo(groupContainer);
 		groupContainer.setLayout(this.groupContainerStackLayout = new StackLayout());
 
 		return container;
 	}
 
 	private void bindWidgets(DataBindingContext dbc, Composite container) {
 
 		// name
 		bindText(nameText, NewInstancePageModel.PROPERTY_NAME, WizardMessages.getString(MUST_ENTER_A_NAME), dbc);
 		IObservableValue imageObservable = bindImage(imageText, dbc);
 		bindArchLabel(arch, imageObservable, dbc);
 		bindRealmCombo(realmCombo, dbc);
 		bindProfileCombo(hardwareCombo, dbc);
 		bindProfilePages(hardwareCombo, profilePages, dbc);
 		bindKey(keyText, dbc);
 	}
 
 	private void bindArchLabel(final Label architectureLabel, IObservableValue imageObservable, DataBindingContext dbc) {
 		dbc.bindValue(WidgetProperties.text().observe(architectureLabel),
 				imageObservable,
 				new UpdateValueStrategy(UpdateSetStrategy.POLICY_NEVER),
 				new UpdateValueStrategy().setConverter(new Converter(DeltaCloudImage.class, String.class) {
 
 					@Override
 					public Object convert(Object fromObject) {
 						if (!(fromObject instanceof DeltaCloudImage)) {
 							return null;
 						}
 						return ((DeltaCloudImage) fromObject).getArchitecture();
 					}
 				}));
 	}
 
 	private void bindRealmCombo(final Combo realmCombo, DataBindingContext dbc) {
 		dbc.bindList(WidgetProperties.items().observe(realmCombo),
 				BeanProperties.list(NewInstancePageModel.PROPERTY_REALMS).observe(model),
 				new UpdateListStrategy(UpdateListStrategy.POLICY_NEVER),
 				new UpdateListStrategy().setConverter(
 						new Converter(Object.class, String.class) {
 
 							@Override
 							public Object convert(Object fromObject) {
 								Assert.isTrue(fromObject instanceof DeltaCloudRealm);
 								DeltaCloudRealm realm = (DeltaCloudRealm) fromObject;
 								return new StringBuilder()
 										.append(realm.getId())
 										.append(" [").append(realm.getName()).append("]") //$NON-NLS-1$ $NON-NLS-2$ 
 										.toString();
 							}
 						}
 						));
 
 		Binding selectedRealmBinding = dbc.bindValue(
 				WidgetProperties.singleSelectionIndex().observe(realmCombo),
 				BeanProperties.value(NewInstancePageModel.class, NewInstancePageModel.PROPERTY_SELECTED_REALM_INDEX)
 						.observe(model),
 				new UpdateValueStrategy()
 						.setAfterGetValidator(new SelectedComboItemValidator("You must select a realm.")),
 				new UpdateValueStrategy()
 						.setAfterGetValidator(new SelectedComboItemValidator("You must select a realm.")));
 
 		// realm combo enablement
 		IObservableList realmsObservable = BeanProperties.list(NewInstancePageModel.PROPERTY_REALMS).observe(model);
 		DataBindingUtils.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void handleChange(ChangeEvent event) {
 				realmCombo.setEnabled(areRealmsAvailable());
 			}
 		}, realmsObservable, container);
 
 		ControlDecorationSupport.create(selectedRealmBinding, SWT.LEFT | SWT.TOP);
 	}
 
 	private void bindProfileCombo(final Combo profileCombo, DataBindingContext dbc) {
 		// bind combo items
 		dbc.bindList(WidgetProperties.items().observe(profileCombo),
 				BeanProperties.list(NewInstancePageModel.PROP_FILTERED_PROFILES).observe(model),
 				new UpdateListStrategy(UpdateListStrategy.POLICY_NEVER),
 				new UpdateListStrategy().setConverter(
 						new Converter(Object.class, String.class) {
 
 							@Override
 							public Object convert(Object fromObject) {
 								Assert.isTrue(fromObject instanceof DeltaCloudHardwareProfile);
 								DeltaCloudHardwareProfile profile = (DeltaCloudHardwareProfile) fromObject;
 								return profile.getId();
 							}
 						}
 						));
 
 		// bind selected combo item
 		Binding selectedProfileBinding = dbc.bindValue(
 				WidgetProperties.singleSelectionIndex().observe(profileCombo),
 				BeanProperties
 						.value(NewInstancePageModel.class, NewInstancePageModel.PROP_SELECTED_PROFILE_INDEX)
 						.observe(model),
 				new UpdateValueStrategy()
 						.setAfterGetValidator(new SelectedComboItemValidator("You must select a hardware profile.")),
 				new UpdateValueStrategy()
 						.setAfterGetValidator(new SelectedComboItemValidator("You must select a hardware profile.")));
 
 		// bind combo enablement
 		IObservableList filteredProfilesObservable =
 				BeanProperties.list(NewInstancePageModel.PROP_FILTERED_PROFILES).observe(model);
 		DataBindingUtils.addChangeListener(
 				new IChangeListener() {
 
 					@Override
 					public void handleChange(ChangeEvent event) {
 						profileCombo.setEnabled(areProfilesAvailable());
 					}
 				}, filteredProfilesObservable, container);
 
 		ControlDecorationSupport.create(selectedProfileBinding, SWT.LEFT | SWT.TOP);
 	}
 
 	private boolean areProfilesAvailable() {
 		return model.getFilteredProfiles() != null
 				&& model.getFilteredProfiles().size() > 0;
 	}
 
 	private boolean areRealmsAvailable() {
 		return model.getRealms() != null
 				&& model.getRealms().size() > 0;
 	}
 
 	private void bindProfilePages(Combo hardwareCombo, final Map<String, ProfilePage> profilePages,
 			DataBindingContext dbc) {
 		// bind all profiles
 		IObservable allProfilesObservable =
 				BeanProperties.list(NewInstancePageModel.class, NewInstancePageModel.PROP_ALL_PROFILES).observe(
 						model);
 		DataBindingUtils.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void handleChange(ChangeEvent event) {
 				createProfilePages(model.getAllProfiles());
 			}
 		}, allProfilesObservable, container);
 
 		// bind selected profile page
 		IObservableValue selectedProfileIndexObservable =
 				BeanProperties
 						.value(NewInstancePageModel.class, NewInstancePageModel.PROP_SELECTED_PROFILE_INDEX)
 						.observe(model);
 		DataBindingUtils.addChangeListener(new IChangeListener() {
 
 			@Override
 			public void handleChange(ChangeEvent event) {
 				ProfilePage profilePage = profilePages.get(model.getProfileId());
 				selectProfilePage(profilePages, profilePage);
 			}
 		}, selectedProfileIndexObservable, hardwareCombo);
 	}
 
 	private void createProfilePages(Collection<DeltaCloudHardwareProfile> profiles) {
 		for (ProfilePage page : profilePages.values()) {
 			page.getControl().dispose();
 		}
 		profilePages.clear();
 		for (DeltaCloudHardwareProfile p : profiles) {
 			ProfilePage pc = new ProfilePage(p, groupContainer);
 			profilePages.put(p.getId(), pc);
 		}
 	}
 
 	private void selectProfilePage(final Map<String, ProfilePage> profilePages, ProfilePage profilePage) {
 		if (profilePage != null) {
 			groupContainerStackLayout.topControl = profilePage.getControl();
 			groupContainer.layout();
 			model.setCpu(profilePage.getCPU());
 			model.setStorage(profilePage.getStorage());
 			model.setMemory(profilePage.getMemory());
 		}
 	}
 
 	private void bindKey(Text text, DataBindingContext dbc) {
 		Binding textBinding = dbc.bindValue(
 				WidgetProperties.text(SWT.Modify).observeDelayed(KEY_CHECK_DELAY, text),
 				BeanProperties.value(NewInstancePageModel.class, NewInstancePageModel.PROPERTY_KEYID).observe(model),
 				new UpdateValueStrategy()
 						.setAfterGetValidator(
 								new MandatoryStringValidator(WizardMessages.getString(MUST_ENTER_A_KEYNAME)))
 						.setBeforeSetValidator(
 								new KeyValidator()),
 				null);
 		ControlDecorationSupport.create(textBinding, SWT.LEFT | SWT.TOP);
 	}
 
 	private class KeyValidator implements IValidator {
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value instanceof String
 					&& ((String) value).length() > 0) {
 				if (doesKeyExist((String) value)) {
 					if (!isKeyKnownToSsh((String) value)) {
 						return ValidationStatus
 								.warning(
 								"Key not found under SSH preferences, might be needed for login after launch.");
 					}
 					return ValidationStatus.ok();
 				}
 			}
 			return ValidationStatus.error(MessageFormat.format(
 					"The key is not known to cloud \"{0}\"", cloud.getName()));
 		}
 
 		private boolean isKeyKnownToSsh(String keyName) {
 			if (keyName == null) {
 				return false;
 			}
 			for (String key :SshPrivateKeysPreferences.getKeys()) {
 				File file = new File(key);
 				if (file.getName().equals(keyName) 
 						|| file.getName().startsWith(keyName + ".")) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private boolean doesKeyExist(String keyId) {
 			try {
 				return cloud.getKey(keyId) != null;
 			} catch (DeltaCloudException e) {
 				return false;
 			}
 		}
 	}
 
 	private void bindText(Text text, String property, String errorMessage, DataBindingContext dbc) {
 		Binding textBinding = dbc.bindValue(
 				WidgetProperties.text(SWT.Modify).observe(text),
 				BeanProperties.value(NewInstancePageModel.class, property).observe(model),
 				new UpdateValueStrategy().setBeforeSetValidator(
 						new MandatoryStringValidator(errorMessage)),
 				null);
 		ControlDecorationSupport.create(textBinding, SWT.LEFT | SWT.TOP);
 	}
 
 	private IObservableValue bindImage(Text imageText, DataBindingContext dbc) {
 		UpdateValueStrategy widgetToModelUpdateStrategy = new UpdateValueStrategy();
 		ImageLabel2DeltaCloudImageConverter imageConverter = new ImageLabel2DeltaCloudImageConverter();
 		widgetToModelUpdateStrategy.setConverter(imageConverter);
 		widgetToModelUpdateStrategy.setAfterGetValidator(
 				new MandatoryStringValidator(WizardMessages.getString(MUST_ENTER_IMAGE_ID)));
 		widgetToModelUpdateStrategy.setAfterConvertValidator(new DeltaCloudImageValidator());
 
 		UpdateValueStrategy modelToTextUpdateStrategy = new UpdateValueStrategy();
 		modelToTextUpdateStrategy.setConverter(new DeltaCloudImage2LabelConverter());
 
 		Binding imageBinding = dbc.bindValue(
 				WidgetProperties.text(SWT.Modify).observeDelayed(IMAGE_CHECK_DELAY, imageText),
 				BeanProperties.value(NewInstancePageModel.class, NewInstancePageModel.PROPERTY_IMAGE).observe(model),
 				widgetToModelUpdateStrategy,
 				modelToTextUpdateStrategy);
 		ControlDecorationSupport.create(imageBinding, SWT.LEFT | SWT.TOP);
 		return imageConverter.getImageObservable();
 	}
 
 	private class ImageLabel2DeltaCloudImageConverter extends Converter {
 
 		private WritableValue imageObservable = new WritableValue();
 
 		public ImageLabel2DeltaCloudImageConverter() {
 			super(String.class, DeltaCloudImage.class);
 		}
 
 		@Override
 		public Object convert(Object fromObject) {
 			Assert.isLegal(fromObject instanceof String);
 			String id = (String) fromObject;
 			DeltaCloudImage image = null;
 			if (id != null) {
 				image = getImage(id);
 			}
 			imageObservable.setValue(image);
 			return image;
 		}
 
 		private DeltaCloudImage getImage(String id) {
 			try {
 				return cloud.getImage(id);
 			} catch (DeltaCloudException e) {
 				return null;
 			}
 		}
 
 		public IObservableValue getImageObservable() {
 			return imageObservable;
 		}
 	}
 
 	private class DeltaCloudImage2LabelConverter extends Converter {
 		private DeltaCloudImage2LabelConverter() {
 			super(DeltaCloudImage.class, String.class);
 		}
 
 		@Override
 		public Object convert(Object fromObject) {
 			if (fromObject instanceof DeltaCloudImage) {
 				DeltaCloudImage image = (DeltaCloudImage) fromObject;
 				return image.getId();
 			} else {
 				return "";
 			}
 		}
 	}
 
 	private class DeltaCloudImageValidator implements IValidator {
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value instanceof DeltaCloudImage) {
 				return ValidationStatus.ok();
 			} else {
 				return ValidationStatus.error(WizardMessages.getFormattedString(
 						IMAGE_ID_NOT_FOUND, imageText.getText()));
 			}
 		}
 	}
 
 	public NewInstancePageModel getModel() {
 		return model;
 	}
 
 	private void asyncGetProfiles(final NewInstancePageModel model, final DeltaCloud cloud) {
 		// TODO: internationalize strings
 		new AbstractCloudElementJob("Get profiles", cloud, CLOUDELEMENT.PROFILES) {
 			protected IStatus doRun(IProgressMonitor monitor) throws Exception {
 				try {
 					List<DeltaCloudHardwareProfile> profiles = Arrays.asList(cloud.getProfiles());
 					model.setAllProfiles(profiles);
 					return Status.OK_STATUS;
 				} catch (DeltaCloudException e) {
 					// TODO: internationalize strings
 					return StatusFactory.getInstance(IStatus.ERROR, Activator.PLUGIN_ID,
 							MessageFormat.format("Could not get profiles from cloud {0}", cloud.getName()));
 				}
 			}
 		}.schedule();
 	}
 
 	private void asyncGetRealms(final NewInstancePageModel model, final DeltaCloud cloud) {
 		// TODO: internationalize strings
 		new AbstractCloudElementJob("Get realms", cloud, CLOUDELEMENT.REALMS) {
 			protected IStatus doRun(IProgressMonitor monitor) throws Exception {
 				try {
 					List<DeltaCloudRealm> allRealms = new ArrayList<DeltaCloudRealm>();
 					allRealms.addAll(Arrays.asList(cloud.getRealms()));
 					model.setRealms(allRealms);
 					return Status.OK_STATUS;
 				} catch (DeltaCloudException e) {
 					// TODO: internationalize strings
 					return StatusFactory.getInstance(IStatus.ERROR, Activator.PLUGIN_ID,
 							MessageFormat.format("Could not get realms from cloud {0}", cloud.getName()));
 				}
 			}
 		}.schedule();
 	}
 
 }

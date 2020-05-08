 /*******************************************************************************
  * Copyright (c) 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.deployment.ui.wizards;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
 import org.zend.php.zendserver.deployment.core.descriptor.IParameter;
 import org.zend.php.zendserver.deployment.core.descriptor.ParameterType;
 import org.zend.php.zendserver.deployment.core.sdk.SdkTarget;
 import org.zend.php.zendserver.deployment.core.sdk.SdkTargetsManager;
 
 public class ApplicationParametersPage extends WizardPage {
 
 	private IDescriptorContainer model;
 	private Combo deployCombo;
 	private Link targetLocation;
 	private BaseURL baseUrl;
 	private Text userAppName;
 	private Button defaultServer;
 	private Button ignoreFailures;
 
 	private List<DeploymentParameter> parameters;
 	private SdkTargetsManager targetsManager;
 
 	private class DeploymentParameter {
 
 		private Control control;
 		private IParameter parameter;
 		private String name;
 		private ParameterType type;
 
 		private DeploymentParameter(IParameter parameter, String name,
 				ParameterType type) {
 			this.parameter = parameter;
 			this.name = name;
 			this.type = type;
 		}
 
 		public String getValue() {
 			switch (type) {
 			case STRING:
 			case EMAIL:
 			case PASSWORD:
 				return ((Text) control).getText();
 			case CHOICE:
 				return ((Combo) control).getText();
 			case CHECKBOX:
 				return String.valueOf(((Button) control).getSelection());
 			case UNKNOWN:
 			default:
 				return "";
 			}
 		}
 
 		public IParameter getParameter() {
 			return parameter;
 		}
 
 		public String getId() {
 			return parameter.getId();
 		}
 
 		public void createControl(Composite composite) {
 			String tooltip = parameter.getLongDescription();
 			if (tooltip == null) {
 				tooltip = parameter.getDescription();
 			}
 			switch (type) {
 			case STRING:
 			case EMAIL:
 				control = createLabelWithText(name, tooltip, composite);
 				if (parameter.getDefaultValue() != null) {
 					((Text) control).setText(parameter.getDefaultValue());
 				}
 				break;
 			case PASSWORD:
 				control = createLabelWithText(name, tooltip, composite);
 				((Text) control).setEchoChar('*');
 				if (parameter.getDefaultValue() != null) {
 					((Text) control).setText(parameter.getDefaultValue());
 				}
 				break;
 			case CHOICE:
 				control = createLabelWithCombo(name, tooltip, composite);
 				String[] values = parameter.getValidValues();
 				((Combo) control).setItems(values);
 				break;
 			case CHECKBOX:
 				control = createLabelWithCheckbox(name, tooltip, composite);
 				if (parameter.getDefaultValue() != null) {
 					((Button) control).setSelection(Boolean.valueOf(parameter
 							.getDefaultValue()));
 				}
 				break;
 			case UNKNOWN:
 			default:
 				break;
 			}
 		}
 	}
 
 	private class BaseURL {
 
 		private static final String DEFAULT = "<DEFAULT_SERVER>";
 
 		private Label protocol;
 		private Label pathSeparator;
 
 		private Text host;
 		private Text path;
 		private Composite parent;
 
 		public void createControl(Composite composite) {
 			parent = new Composite(composite, SWT.NONE);
 			parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			GridLayout layout = new GridLayout(6, false);
 			layout.horizontalSpacing = 0;
 			layout.verticalSpacing = 0;
 			layout.marginWidth = 0;
 			parent.setLayout(layout);
 
 			protocol = new Label(parent, SWT.NULL);
 			protocol.setText("http://");
 
 			host = new Text(parent, SWT.SINGLE | SWT.BORDER);
 			host.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			host.addKeyListener(new KeyAdapter() {
 				public void keyReleased(KeyEvent e) {
 					validatePage();
 				}
 			});
 
 			pathSeparator = new Label(parent, SWT.NULL);
 			pathSeparator.setText("/");
 
 			path = new Text(parent, SWT.SINGLE | SWT.BORDER);
 			path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			path.addKeyListener(new KeyAdapter() {
 				public void keyReleased(KeyEvent e) {
 					validatePage();
 				}
 			});
 		}
 
 		public void setLayoutData(Object layoutData) {
 			parent.setLayoutData(layoutData);
 		}
 
 		public void setDefaultServer(boolean value) {
 			if (value) {
 				host.setEnabled(false);
 				host.setText(DEFAULT);
 			} else {
 				host.setEnabled(true);
 				host.setText("");
 			}
 		}
 
 		public URL getURL() {
 			URL result = null;
 			String realHost = DEFAULT.equals(host.getText()) ? "default" : host
 					.getText();
 			try {
				result = new URL(protocol.getText().substring(0,
						protocol.getText().length() - 3), realHost,
						path.getText());
 			} catch (MalformedURLException e) {
 				// ignore and return null
 			}
 			return result;
 		}
 
 		public boolean isValid() {
 			if (getURL() != null && !host.getText().isEmpty()) {
 				return true;
 			}
 			return false;
 		}
 
 	}
 
 	protected ApplicationParametersPage(IDescriptorContainer model) {
 		super(Messages.parametersPage_Title);
 		this.model = model;
 		this.parameters = new ArrayList<DeploymentParameter>();
 		this.targetsManager = new SdkTargetsManager();
 		setDescription(Messages.deployWizardPage_Description);
 		setTitle(Messages.parametersPage_Title);
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout(2, false);
 		container.setLayout(layout);
 		createDeployCombo(container);
 		createLocationLink(container);
 		userAppName = createLabelWithText(Messages.parametersPage_appUserName,
 				Messages.parametersPage_appUserNameTooltip, container);
 		createBaseUrl(container);
 		ignoreFailures = createLabelWithCheckbox(
 				Messages.parametersPage_ignoreFailures,
 				Messages.parametersPage_ignoreFailuresTooltip, container);
 		createParameterGroups(container);
 		setControl(container);
 		setPageComplete(false);
 		validatePage();
 	}
 
 	private void createBaseUrl(Composite container) {
 		Label label = new Label(container, SWT.NULL);
 		label.setText(Messages.parametersPage_baseURL);
 		baseUrl = new BaseURL();
 		baseUrl.createControl(container);
 		baseUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		defaultServer = createLabelWithCheckbox(
 				Messages.parametersPage_defaultServer,
 				Messages.parametersPage_defaultServerTooltip, container);
 		defaultServer.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				baseUrl.setDefaultServer(defaultServer.getSelection());
 				validatePage();
 			}
 		});
 	}
 
 	public String getBaseURL() {
 		URL result = baseUrl.getURL();
 		return result != null ? result.toString() : null;
 	}
 
 	public String getUserAppName() {
 		return userAppName.getText();
 	}
 
 	public boolean isDefaultServer() {
 		return defaultServer.getSelection();
 	}
 
 	public boolean isIgnoreFailures() {
 		return ignoreFailures.getSelection();
 	}
 
 	public SdkTarget getTarget() {
 		return targetsManager.getTargetById(deployCombo.getText());
 	}
 
 	public HashMap<String, String> getParameters() {
 		HashMap<String, String> result = new HashMap<String, String>();
 		for (DeploymentParameter param : parameters) {
 			result.put(param.getId(), param.getValue());
 		}
 		return result;
 	}
 
 	private void createParameterGroups(Composite container) {
 		Group paramsGroup = new Group(container, SWT.NULL);
 		paramsGroup.setText(Messages.parametersPage_applicationParams);
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.horizontalSpan = 2;
 		paramsGroup.setLayoutData(gd);
 		paramsGroup.setLayout(new FillLayout(SWT.FILL));
 		if (model.getDescriptorModel() != null) {
 			final ScrolledComposite scrollComposite = new ScrolledComposite(
 					paramsGroup, SWT.V_SCROLL);
 			scrollComposite.setLayout(new FillLayout());
 			final Composite parent = new Composite(scrollComposite, SWT.NONE);
 			parent.setLayout(new GridLayout(2, false));
 			scrollComposite.setExpandVertical(true);
 			scrollComposite.setExpandHorizontal(true);
 			scrollComposite.setContent(parent);
 			scrollComposite.addControlListener(new ControlAdapter() {
 				public void controlResized(ControlEvent e) {
 					Rectangle r = scrollComposite.getClientArea();
 					scrollComposite.setMinSize(parent.computeSize(r.width,
 							SWT.DEFAULT));
 				}
 			});
 			List<ParametersCategory> categories = createCategories(model
 					.getDescriptorModel().getParameters());
 			for (ParametersCategory category : categories) {
 				createGroup(category, parent);
 			}
 		}
 	}
 
 	private void createGroup(ParametersCategory category, Composite parent) {
 		if (category.getParameter() == null) {
 			Group paramsGroup = new Group(parent, SWT.NULL);
 			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 			gd.horizontalSpan = 2;
 			paramsGroup.setLayoutData(gd);
 			paramsGroup.setText(category.getName());
 			paramsGroup.setLayout(new GridLayout(2, true));
 			List<ParametersCategory> categories = category.getCategories();
 			for (ParametersCategory cat : categories) {
 				createGroup(cat, paramsGroup);
 			}
 		} else {
 			DeploymentParameter parameter = null;
 			ParameterType type = ParameterType.byName(category.getParameter()
 					.getType());
 			if (type != ParameterType.UNKNOWN) {
 				parameter = new DeploymentParameter(category.getParameter(),
 						category.getName(), type);
 				parameter.createControl(parent);
 				parameters.add(parameter);
 			}
 		}
 	}
 
 	private List<ParametersCategory> createCategories(List<IParameter> params) {
 		List<ParametersCategory> categories = new ArrayList<ParametersCategory>();
 		for (IParameter param : params) {
 			List<String> labels = new ArrayList<String>(Arrays.asList(param
 					.getDisplay().split(ParametersCategory.SEPARATOR)));
 			ParametersCategory cat = createCategory(categories, labels, param);
 			if (cat != null) {
 				categories.add(cat);
 			}
 		}
 		return categories;
 	}
 
 	private ParametersCategory createCategory(
 			List<ParametersCategory> categories, List<String> labels,
 			IParameter dp) {
 		if (labels.size() > 1) {
 			String catName = labels.remove(0);
 			for (ParametersCategory category : categories) {
 				if (category.getName().equals(catName)) {
 					ParametersCategory cat = createCategory(
 							category.getCategories(), labels, dp);
 					if (cat != null) {
 						category.addCategory(cat);
 					}
 					return null;
 				}
 			}
 			ParametersCategory cat = new ParametersCategory(catName);
 			cat.addCategory(createCategory(categories, labels, dp));
 			return cat;
 		} else {
 			String catName = labels.remove(0);
 			return new ParametersCategory(dp, catName);
 		}
 	}
 
 	private void createLocationLink(Composite container) {
 		targetLocation = new Link(container, SWT.NONE);
 		String text = "<a>" + Messages.parametersPage_TargetLocation + "</a>";
 		targetLocation.setText(text);
 		targetLocation.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				SdkTarget prevSelection = getTarget();
 				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
 						event.display.getActiveShell(),
 						"org.eclipse.php.server.internal.ui.PHPServersPreferencePage",
 						null, null);
 				if (dialog.open() == Window.OK) {
 					populateLocationList();
 					List<SdkTarget> targets = targetsManager.getTargets();
 					for (int i = 0; i < targets.size(); i++) {
 						if (targets.get(i).getId().equals(prevSelection)) {
 							deployCombo.select(i);
 						}
 					}
 					validatePage();
 				}
 			}
 		});
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		gd.horizontalSpan = 2;
 		targetLocation.setLayoutData(gd);
 	}
 
 	private Combo createLabelWithCombo(String labelText, String tooltip,
 			Composite container) {
 		Label label = new Label(container, SWT.NULL);
 		label.setText(labelText);
 		Combo combo = new Combo(container, SWT.SIMPLE | SWT.DROP_DOWN
 				| SWT.READ_ONLY);
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				validatePage();
 			}
 		});
 		combo.setToolTipText(tooltip);
 		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		return combo;
 	}
 
 	private Text createLabelWithText(String labelText, String tooltip,
 			Composite container) {
 		Label label = new Label(container, SWT.NULL);
 		label.setText(labelText);
 		Text text = new Text(container, SWT.BORDER | SWT.SINGLE);
 		text.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				validatePage();
 			}
 		});
 		text.setToolTipText(tooltip);
 		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		return text;
 	}
 
 	private Button createLabelWithCheckbox(String desc, String tooltip,
 			Composite composite) {
 		Button button = new Button(composite, SWT.CHECK);
 		button.setText(desc);
 		button.setToolTipText(tooltip);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		button.setLayoutData(gd);
 		return button;
 	}
 
 	private void createDeployCombo(Composite container) {
 		deployCombo = createLabelWithCombo(Messages.parametersPage_DeployTo,
 				"", container);
 		populateLocationList();
 	}
 
 	private void populateLocationList() {
 		List<SdkTarget> targets = targetsManager.getTargets();
 		deployCombo.removeAll();
 		if (!targets.isEmpty()) {
 			for (SdkTarget target : targets) {
 				deployCombo.add(target.getId());
 			}
 		}
 		if (deployCombo.getItemCount() > 0) {
 			deployCombo.select(-1);
 		}
 	}
 
 	private void validatePage() {
 		setErrorMessage(null);
 		setMessage(null);
 		if (getTarget() == null) {
 			setErrorMessage(Messages.parametersPage_ValidationError_TargetLocation);
 			setPageComplete(false);
 			return;
 		}
 		if (baseUrl.isValid()) {
 			for (DeploymentParameter param : parameters) {
 				if (param.getParameter().isRequired()
 						&& param.getValue().isEmpty()) {
 					setErrorMessage(param.getParameter().getDisplay()
 							+ " is required.");
 					setPageComplete(false);
 					return;
 				}
 			}
 			setPageComplete(true);
 		} else {
 			setErrorMessage(Messages.parametersPage_ValidationError_BaseUrl);
 			setPageComplete(false);
 		}
 	}
 
 }

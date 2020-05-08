 package org.jboss.tools.internal.deltacloud.ui.wizards;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
 import org.jboss.tools.deltacloud.ui.Activator;
 import org.jboss.tools.deltacloud.ui.SWTImagesFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class NewCloudConnectionPage extends WizardPage {
 
 	private static final String DESCRIPTION = "NewCloudConnection.desc"; //$NON-NLS-1$
 	private static final String TITLE = "NewCloudConnection.title"; //$NON-NLS-1$
 	private static final String URL_LABEL = "Url.label"; //$NON-NLS-1$
 	private static final String NAME_LABEL = "Name.label"; //$NON-NLS-1$
 	private static final String USERNAME_LABEL = "UserName.label"; //$NON-NLS-1$
 	private static final String TYPE_LABEL = "Type.label"; //$NON-NLS-1$
 	private static final String PASSWORD_LABEL = "Password.label"; //$NON-NLS-1$
 	private static final String TESTBUTTON_LABEL = "TestButton.label"; //$NON-NLS-1$
 	private static final String UNKNOWN_TYPE_LABEL = "UnknownType.label"; //$NON-NLS-1$
 	private static final String EC2_USER_INFO = "EC2UserNameLink.text"; //$NON-NLS-1$
 	private static final String EC2_PASSWORD_INFO = "EC2PasswordLink.text"; //$NON-NLS-1$
 	private static final String NAME_ALREADY_IN_USE = "ErrorNameInUse.text"; //$NON-NLS-1$
 	private static final String INVALID_URL = "ErrorInvalidURL.text"; //$NON-NLS-1$
 	private static final String NONCLOUD_URL = "ErrorNonCloudURL.text"; //$NON-NLS-1$
 
 	private static final String TEST_SUCCESSFUL = "NewCloudConnectionTest.success"; //$NON-NLS-1$
 	private static final String TEST_FAILURE = "NewCloudConnectionTest.failure"; //$NON-NLS-1$
 	
 	private NewCloudConnection wizard;
 	
 	private Button testButton;
 	
 	private Text nameText;
 	private Text urlText;
 	private Label typeText;
 	private Text usernameText;
 	private Text passwordText;
 	
 	private String name;
 	private String url;
 	private String username;
 	private String password;
 	private String cloudType;
 
 	private boolean urlValid;
 	
 	private class CheckURLThread extends Thread {
 		
 		@Override
 		public void run() {
 			try {
 				checkURL();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private Listener linkListener = new Listener() {
 
 		public void handleEvent(Event event) {
 			try {
 				URL url = new URL(event.text);
 				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
 			} catch (Exception e) {
 				Activator.log(e);
 			}
 		}
 		
 	};
 
 	private SelectionListener buttonListener = new SelectionAdapter() {
 
 		public void widgetSelected(SelectionEvent event) {
 			boolean successful = false;
 			if (getURLValid()) {
 				successful = wizard.performTest();
 			}
 			if (successful) {
 				setMessage(WizardMessages.getString(TEST_SUCCESSFUL));
 			} else {
 				setErrorMessage(WizardMessages.getString(TEST_FAILURE));
 			}
 		}
 		
 	};
 	
 	public NewCloudConnectionPage(String pageName, NewCloudConnection wizard) {
 		super(pageName);
 		this.wizard= wizard;
 		setDescription(WizardMessages.getString(DESCRIPTION));
 		setTitle(WizardMessages.getString(TITLE));
 		setImageDescriptor(SWTImagesFactory.DESC_DELTA_LARGE);
 		setPageComplete(false);
 	}
 
 	private ModifyListener textListener = new ModifyListener() {
 
 		@Override
 		public void modifyText(ModifyEvent e) {
 			validate();
 		}
 	};
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getURL() {
 		return url;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	private void validate() {
 		boolean complete = true;
 		boolean errorFree = true;
 		
 		setMessage(null);
 		
 		name = nameText.getText();
 		if (name.length() > 0) {
 			if (DeltaCloudManager.getDefault().findCloud(name) != null) {
 				errorFree = false;
 				setErrorMessage(WizardMessages.getString(NAME_ALREADY_IN_USE));
 			}
 		} else {
 			complete = false;
 		}
 		
 		// Run check for valid DeltaCloud URL in separate thread
 		String urlValue = urlText.getText();
 		if (urlValue.length() > 5) {
 			CheckURLThread t = new CheckURLThread();
 			t.start();
 		} else if (urlValue.length() > 0){
 			typeText.setText(WizardMessages.getString(NONCLOUD_URL));
 			complete = false;
 		} else {
 			typeText.setText(WizardMessages.getString(UNKNOWN_TYPE_LABEL));
 			complete = false;
 		}
 		
 		username = usernameText.getText();
 		if (username.length() <= 0) {
 			complete = false;
 		}
 		password = passwordText.getText();
 		if (password.length() <= 0) {
 			complete = false;
 		}
 		if (errorFree)
 			setErrorMessage(null);
 		setPageComplete(complete & errorFree);
 	}
 	
 	@Override
 	public boolean isPageComplete() {
 		return super.isPageComplete() & getURLValid();
 	}
 	
 	
 	// Method to check the URL for validity as Delta-cloud API specifier.
 	// Since this is run in thread, it does not use the setErrorMessage()
 	// method and instead writes error messages to the typeText label.
 	private boolean checkURL() {
 		boolean valid = false;
 		String oldurl = url;
 		Display.getDefault().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				cloudType = typeText.getText();
 				url = urlText.getText();
 			}
 		});
 		String oldCloudType = cloudType;
 		if (url.length() > 0) {
 			if (!url.equals(oldurl)) {
 				try {
 					URL u = new URL(url + "/api.xml"); //$NON-NLS-1$
 					Object o = u.getContent();
 					if (o instanceof InputStream) {
 						String xml = "";
 						InputStream is = (InputStream)o;
 						try
 						{
 							if (is != null)
 							{
 								StringBuilder sb = new StringBuilder();
 								String line;
 
 								BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
 								while ((line = reader.readLine()) != null) 
 								{
 									sb.append(line).append("\n"); //$NON-NLS-1$
 								}
 								xml = sb.toString();
 							}
 						}
 						finally
 						{
 							is.close();
 						}
 
 						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 						DocumentBuilder db = dbf.newDocumentBuilder();
 						Document document = db.parse(new InputSource(new StringReader(xml)));
 
 						NodeList elements = document.getElementsByTagName("api");  //$NON-NLS-1$
 						if (elements.getLength() > 0) {
 							Node n = elements.item(0);
 							Node driver = n.getAttributes().getNamedItem("driver"); //$NON-NLS-1$
 							if (driver != null) {
 								valid = true;
 								String driverValue = driver.getNodeValue();
 								cloudType = driverValue.toUpperCase();
 							} else {
 								cloudType = WizardMessages.getString(UNKNOWN_TYPE_LABEL);
 							}
 						}
 					}
 				} catch (MalformedURLException e) {
 					cloudType = WizardMessages.getString(INVALID_URL);
 				} catch (IOException e) {
 					cloudType = WizardMessages.getString(NONCLOUD_URL);
 				} catch (ParserConfigurationException e) {
 					cloudType = WizardMessages.getString(NONCLOUD_URL);
 				} catch (SAXException e) {
 					cloudType = WizardMessages.getString(NONCLOUD_URL);
 				} catch (Exception e) {
 					cloudType = WizardMessages.getString(INVALID_URL);
 				}
 				setURLValid(valid);
 			}
 			if (!oldCloudType.equals(cloudType)) {
 				Display.getDefault().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						testButton.setEnabled(getURLValid());
 						typeText.setText(cloudType);
 					}
 					
 				});
 			}
 		}
 		return valid;
 	}
 	
 	/**
 	 * Set whether the URL is a valid Delta-cloud API URL.
 	 * 
 	 * @param value boolean to set
 	 */
 	private synchronized void setURLValid(boolean value) {
 		urlValid = value;
 	}
 	
 	/**
 	 * Return the validity of the Delta-cloud URL.
 	 * 
 	 * @return true if URL valid, false otherwise
 	 */
 	private synchronized boolean getURLValid() {
 		return urlValid;
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		final Composite container = new Composite(parent, SWT.NULL);
 		FormLayout layout = new FormLayout();
 		layout.marginHeight = 5;
 		layout.marginWidth = 5;
 		container.setLayout(layout);
 	
 		Label dummyLabel = new Label(container, SWT.NULL);
 		// errorLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));
 		
 		Label nameLabel = new Label(container, SWT.NULL);
 		nameLabel.setText(WizardMessages.getString(NAME_LABEL));
 		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		nameText.addModifyListener(textListener);
 		
 		Label urlLabel = new Label(container, SWT.NULL);
 		urlLabel.setText(WizardMessages.getString(URL_LABEL));
 		
 		urlText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		urlText.addModifyListener(textListener);
 
 		Label typeLabel = new Label(container, SWT.NULL);
 		typeLabel.setText(WizardMessages.getString(TYPE_LABEL));
 
 		typeText = new Label(container, SWT.NULL);
 		cloudType = WizardMessages.getString(UNKNOWN_TYPE_LABEL);
 		typeText.setText(cloudType);
 		
 		Label usernameLabel = new Label(container, SWT.NULL);
 		usernameLabel.setText(WizardMessages.getString(USERNAME_LABEL));
 
 		usernameText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		usernameText.addModifyListener(textListener);
 
 		Label passwordLabel = new Label(container, SWT.NULL);
 		passwordLabel.setText(WizardMessages.getString(PASSWORD_LABEL));
 
 		passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD | SWT.SINGLE);
 		passwordText.addModifyListener(textListener);
 		
 		testButton = new Button(container, SWT.NULL);
 		testButton.setText(WizardMessages.getString(TESTBUTTON_LABEL));
 		testButton.setEnabled(false);
 		testButton.addSelectionListener(buttonListener);
 		
 		Link ec2userLink = new Link(container, SWT.NULL);
 		ec2userLink.setText(WizardMessages.getString(EC2_USER_INFO));
 		ec2userLink.addListener(SWT.Selection, linkListener);
 
 		Link ec2pwLink = new Link(container, SWT.NULL);
 		ec2pwLink.setText(WizardMessages.getString(EC2_PASSWORD_INFO));
 		ec2pwLink.addListener(SWT.Selection, linkListener);
 		
 		FormData f = new FormData();
 		f.left = new FormAttachment(0, 0);
 		f.right = new FormAttachment(100, 0);
 		dummyLabel.setLayoutData(f);
 		
 		f = new FormData();
 		f.top = new FormAttachment(dummyLabel, 11);
 		nameLabel.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(dummyLabel, 8);
 		f.left = new FormAttachment(usernameLabel, 5);
 		f.right = new FormAttachment(100, 0);
 		nameText.setLayoutData(f);
 		
 		f = new FormData();
 		f.top = new FormAttachment(nameText, 8);
 		urlLabel.setLayoutData(f);
 		
 		f = new FormData();
 		f.left = new FormAttachment(nameText, 0, SWT.LEFT);
 		f.top = new FormAttachment(nameText, 5);
 		f.right = new FormAttachment(100, 0);
 		urlText.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(urlText, 8);
 		typeLabel.setLayoutData(f);
 		
 		f = new FormData();
 		f.left = new FormAttachment(urlText, 0, SWT.LEFT);
 		f.top = new FormAttachment(urlText, 8);
 		f.right = new FormAttachment(100, 0);
 		typeText.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(typeText, 16);
 		usernameLabel.setLayoutData(f);
 		
 		f = new FormData();
 		f.left = new FormAttachment(typeText, 0, SWT.LEFT);
 		f.top = new FormAttachment(typeText, 10);
 		f.right = new FormAttachment(100, -70);
 		usernameText.setLayoutData(f);
 		
 		f = new FormData();
 		f.left = new FormAttachment(usernameText, 0, SWT.LEFT);
 		f.top = new FormAttachment(usernameText, 5);
 		ec2userLink.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(ec2userLink, 8);
 		passwordLabel.setLayoutData(f);
 
 		f = new FormData();
 		f.left = new FormAttachment(usernameText, 0, SWT.LEFT);
 		f.top = new FormAttachment(ec2userLink, 5);
 		f.right = new FormAttachment(100, -70);
 		passwordText.setLayoutData(f);
 		
 		f = new FormData();
         int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
         Point minSize = testButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
         f.width = Math.max(widthHint, minSize.x);
 		f.left = new FormAttachment(usernameText, 10);
 		f.top = new FormAttachment(usernameText, 0);
 		f.right = new FormAttachment(100, 0);
 		testButton.setLayoutData(f);
 		
 		f = new FormData();
 		f.left = new FormAttachment(passwordText, 0, SWT.LEFT);
 		f.top = new FormAttachment(passwordText, 5);
 		ec2pwLink.setLayoutData(f);
 		
 		setControl(container);
 }
 
 }

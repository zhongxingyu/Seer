 /******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.views;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.wsdl.Binding;
 import javax.wsdl.Definition;
 import javax.wsdl.Operation;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.Service;
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.soap.SOAPBinding;
 import javax.wsdl.extensions.soap12.SOAP12Binding;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.ui.JBossWSUIPlugin;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 import org.jboss.tools.ws.ui.utils.TesterWSDLUtils;
 
 /**
  * @author bfitzpat
  *
  */
 public class WSDLBrowseDialog extends TitleAreaDialog {
 
 	private Label locationLabel = null;
 	private Combo locationCombo = null;
 	private Button workspaceBrowseButton = null;
 	private Button fsBrowseButton = null;
 	private Button urlBrowseButton = null;
 	
 	private static String wsdlTextValue = null;
 	private static String[] oldValues = null;
 	private String serviceTextValue = null;
 	private String portTextValue = null;
 	private String operationTextValue = null;
 	private String initialOperationTextValue = null;
 	private String bindingValue = null;
 	
 	private Definition wsdlDefinition = null;
 	private Label serviceLabel;
 	private Combo serviceCombo;
 	private Combo portCombo;
 	private Label operationLabel;
 	private List opList;
 	private Group group;
 	private Label portLabel;
 	private boolean showServicePortOperaton = true;
 
 	private Timer timer;
 
 	public WSDLBrowseDialog(Shell parentShell) {
 		super(parentShell);
 		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE );
 	}
 	
 	public void setShowServicePortOperation (boolean flag ){
 		this.showServicePortOperaton = flag;
 	}
 	public boolean getShowServicePortOperation() {
 		return this.showServicePortOperaton;
 	}
 	
 	public String getWSDLText(){
 		return WSDLBrowseDialog.wsdlTextValue;
 	}
 	
 	public String getBindingValue() {
 		return bindingValue;
 	}
 
 	public String getServiceTextValue() {
 		return serviceTextValue;
 	}
 
 	public String getPortTextValue() {
 		return portTextValue;
 	}
 
 	public String getOperationTextValue() {
 		return operationTextValue;
 	}
 	
 	public void setInitialOperationTextValue( String value ) {
 		initialOperationTextValue = value;
 	}
 
 	public Definition getWSDLDefinition(){
 		return this.wsdlDefinition;
 	}
 	
 	public void setURLText(String urlText) {
 		wsdlTextValue = urlText;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void okPressed() {
 		WSDLBrowseDialog.wsdlTextValue = locationCombo.getText();
 		
 		ArrayList<String> uriList = new ArrayList<String>();
 		if (WSDLBrowseDialog.oldValues != null) {
 			@SuppressWarnings("rawtypes")
 			java.util.List tempList = (java.util.List) Arrays.asList(WSDLBrowseDialog.oldValues);
 			uriList.addAll(tempList);
 		}
 		if (!uriList.contains(locationCombo.getText())) {
 			uriList.add(locationCombo.getText());
 		}
 		WSDLBrowseDialog.oldValues = uriList.toArray(new String[uriList.size()]);
 		
 		super.okPressed();
 	}
 
 	/*
 	 * Validate the incoming text for the WSDL URL and see if it's actually a valid URL
 	 * @param arg0
 	 */
 	private void validateLocation ( ModifyEvent arg0 ) {
 		this.getContents().getDisplay().asyncExec( new Runnable() {
 			public void run() {
 				setMessage(JBossWSUIMessages.WSDLBrowseDialog_Message);
 				IStatus status = validate(false);
 				if (status != Status.OK_STATUS) {
 					setMessage(status.getMessage(), IMessageProvider.WARNING);
 					if (showServicePortOperaton)
 						setGroupEnabled(false);
 				} else {
 					setMessage(JBossWSUIMessages.WSDLBrowseDialog_Message);
 					if (showServicePortOperaton)
 						setGroupEnabled(true);
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		setTitle(JBossWSUIMessages.WSDLBrowseDialog_Title);
 		setMessage(JBossWSUIMessages.WSDLBrowseDialog_Message);
 
 		Composite mainComposite = new Composite (parent,SWT.NONE);
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.horizontalSpan = 2;
 		mainComposite.setLayoutData(gridData);
 		GridLayout gridLayout = new GridLayout(2, false);
 		mainComposite.setLayout(gridLayout);
 
 		locationLabel = new Label(mainComposite, SWT.NONE);
 		locationLabel.setText(JBossWSUIMessages.WSDLBrowseDialog_WSDL_URI_Field);
 
 		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		locationCombo = new Combo(mainComposite, SWT.BORDER | SWT.DROP_DOWN );
 		locationCombo.setLayoutData(gridData);
 		locationCombo.addModifyListener(new ModifyListener() {
 			public void modifyText(final ModifyEvent arg0) {
 				// this delay code was reused from a question on StackOverflow
 				// http://stackoverflow.com/questions/4386085/delay-in-text-input
 				if(timer != null){
 					timer.cancel();
 				}
 				timer = new Timer();                
 				timer.schedule(new TimerTask() {
 					@Override
 					public void run() {
 						//handler
 						validateLocation(arg0);
 						timer.cancel();
 					};
 				}, 750); // 750 ms
 			};
 		});
 		if (WSDLBrowseDialog.oldValues != null && WSDLBrowseDialog.oldValues.length > 0) {
 			for (int i = 0; i < oldValues.length; i++) {
 				locationCombo.add(WSDLBrowseDialog.oldValues[i]);
 			}
 		}
 
 		Composite buttonBar = new Composite ( mainComposite, SWT.NONE);
 		GridData buttonBarGD = new GridData(SWT.END, SWT.NONE, true, false);
 		buttonBarGD.horizontalSpan = 2;
 		buttonBar.setLayoutData(buttonBarGD);
 		buttonBar.setLayout(new RowLayout());
 
 		workspaceBrowseButton = new Button(buttonBar, SWT.NONE);
 		workspaceBrowseButton.setText(JBossWSUIMessages.WSDLBrowseDialog_WS_Browse);
 		workspaceBrowseButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				ElementTreeSelectionDialog dialog =
 					new ElementTreeSelectionDialog(getShell(),
 							new WorkbenchLabelProvider(),
 							new WorkbenchContentProvider());
 				dialog.setTitle(JBossWSUIMessages.WSDLBrowseDialog_WS_Browse_Select_WSDL_Title);
 				dialog.setMessage(JBossWSUIMessages.WSDLBrowseDialog_WS_Browse_Msg);
 				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
 				dialog.setAllowMultiple(false);
 				dialog.setEmptyListMessage(JBossWSUIMessages.WSDLBrowseDialog_WS_Browse_Select_WSDL_Msg);
 				dialog.setStatusLineAboveButtons(true);
 				dialog.setValidator(new ISelectionStatusValidator() {
 					
 					public IStatus validate(Object[] arg0) {
 						if (arg0.length > 0 && arg0[0] instanceof IFile) {
 							IFile resource = (IFile) arg0[0];
 							if (resource.getFileExtension().equals("wsdl")) { //$NON-NLS-1$
 								return Status.OK_STATUS;
 							}
 						}
 						return StatusUtils.errorStatus(JBossWSUIMessages.WSDLBrowseDialog_WS_Browse_Select_WSDL_Msg);
 					}
 				});
 				int rtnCode = dialog.open();
 				if (rtnCode == Window.OK) {
 					Object[] objects = dialog.getResult(); //fileDialog.getResult();
 					if (objects != null && objects.length > 0){
 						if (objects[0] instanceof IFile) {
 							IFile resource = (IFile) objects[0];
 							File tempFile = new File(resource.getRawLocationURI());
 							try {
 								URL testURL = tempFile.toURI().toURL();
 								locationCombo.setText(testURL.toExternalForm());
 								wsdlDefinition =
 									TesterWSDLUtils.readWSDLURL(testURL);
 								if (showServicePortOperaton)
 									updateServiceCombo();
 							} catch (MalformedURLException e) {
 								e.printStackTrace();
 							} catch (WSDLException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 			}
 			public void widgetSelected(SelectionEvent arg0) {
 				widgetDefaultSelected(arg0);
 			}
 		});
 		
 		fsBrowseButton = new Button(buttonBar, SWT.NONE);
 		fsBrowseButton.setText(JBossWSUIMessages.WSDLBrowseDialog_FS_Browse);
 		fsBrowseButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				FileDialog fileDialog = new FileDialog(getShell());
 				 String[] filterExt = { "*.wsdl", "*.xml", "*.*" };  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
 				 fileDialog.setFilterExtensions(filterExt);				
 			     if (locationCombo.getText().trim().length() > 0) {
 			    	 try {
 						URI uri = new URI(locationCombo.getText());
 						File temp = new File(uri);
 						String parentPath = temp.getParent();
 						fileDialog.setFilterPath(parentPath);
 						fileDialog.setFileName(temp.getName());
 					} catch (URISyntaxException e1) {
 					} catch (IllegalArgumentException e2) {
 					}
 				}
 				String fileText = fileDialog.open();
 				if (fileText != null){
 					File tempFile = new File(fileText);
 					try {
 						URL testURL = tempFile.toURI().toURL();
 						locationCombo.setText(testURL.toExternalForm());
 						wsdlDefinition =
 							TesterWSDLUtils.readWSDLURL(testURL);
 						if (showServicePortOperaton)
 							updateServiceCombo();
 					} catch (MalformedURLException e) {
 						JBossWSUIPlugin.log(e);
 					} catch (WSDLException e) {
 						JBossWSUIPlugin.log(e);
 					}
 				}
 			}
 			public void widgetSelected(SelectionEvent arg0) {
 				widgetDefaultSelected(arg0);
 			}
 		});
 		
 		urlBrowseButton = new Button(buttonBar, SWT.NONE);
 		urlBrowseButton.setText(JBossWSUIMessages.WSDLBrowseDialog_URL_Browse);
 		
 		urlBrowseButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent arg0) {
 				widgetDefaultSelected(arg0);
 			}
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				InputDialog inDialog = null;
 				if (locationCombo.getText().trim().length() > 0) {
 					inDialog = new InputDialog (getShell(), 
 							JBossWSUIMessages.WSDLBrowseDialog_WSDL_URL_Dialog_Title, 
 							JBossWSUIMessages.WSDLBrowseDialog_WSDL_URL_Prompt, locationCombo.getText(), null);
 				} else {
 					inDialog = new InputDialog (getShell(), 
 							JBossWSUIMessages.WSDLBrowseDialog_WSDL_URL_Dialog_Title, 
 							JBossWSUIMessages.WSDLBrowseDialog_WSDL_URL_Prompt, "", null); //$NON-NLS-1$
 				}
 				int rtnCode = inDialog.open();
 				if (rtnCode == Window.OK) {
 					locationCombo.setText(inDialog.getValue());
 					try {
 						final URL testURL = new URL(inDialog.getValue());
 						locationCombo.setText(testURL.toExternalForm());
 						IStatus status = validate(false);
 						if (status != null && !status.isOK()) {
 							setMessage(status.getMessage(), IMessageProvider.WARNING);
 						} else {
 							status =  parseWSDLFromURL(testURL, true);
 							if (status != null && !status.isOK()) {
 								setMessage(status.getMessage(), IMessageProvider.WARNING);
 							} else {
 								setMessage(JBossWSUIMessages.WSDLBrowseDialog_Message);
 								if (showServicePortOperaton) {
 									updateServiceCombo();
 								}
 							}
 						}
 					} catch (MalformedURLException e) {
 						JBossWSUIPlugin.log(e);
 						ErrorDialog.openError(getShell(), JBossWSUIMessages.WSDLBrowseDialog_Error_Retrieving_WSDL,
 								JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Invalid_URL, 
 								StatusUtils.errorStatus(e));
 					}
 				}
 			}
 		});
 		
 		if (this.showServicePortOperaton) {
 			group = new Group(mainComposite, SWT.NONE);
 			group.setText(JBossWSUIMessages.WSDLBrowseDialog_Group_Title);
 			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 			gridData.horizontalSpan = 2;
 			group.setLayoutData(gridData);
 			group.setLayout(new GridLayout(2, false));
 			
 			serviceLabel = new Label(group, SWT.NONE);
 			serviceLabel.setText(JBossWSUIMessages.WSDLBrowseDialog_Service_Field);
 	
 			gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
 			serviceCombo = new Combo(group, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
 			serviceCombo.setLayoutData(gridData);
 			serviceCombo.addSelectionListener(new SelectionListener(){
 				public void widgetDefaultSelected(SelectionEvent arg0) {
 					updatePortCombo();
 				}
 				public void widgetSelected(SelectionEvent arg0) {
 					widgetDefaultSelected(arg0);
 				}
 			});
 			
 			portLabel = new Label(group, SWT.NONE);
 			portLabel.setText(JBossWSUIMessages.WSDLBrowseDialog_Port_Field);
 	
 			gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
 			portCombo = new Combo(group, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
 			portCombo.setLayoutData(gridData);
 			portCombo.addSelectionListener(new SelectionListener(){
 				public void widgetDefaultSelected(SelectionEvent arg0) {
 					updateOperationList();
 				}
 				public void widgetSelected(SelectionEvent arg0) {
 					widgetDefaultSelected(arg0);
 				}
 			});
 	
 			operationLabel = new Label(group, SWT.NONE);
 			operationLabel.setText(JBossWSUIMessages.WSDLBrowseDialog_Operation_Field);
 	
 			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 			gridData.verticalSpan = 3;
 			gridData.heightHint = 50;
 			opList = new List(group, SWT.BORDER | SWT.V_SCROLL );
 			opList.setLayoutData(gridData);
 			opList.addSelectionListener(new SelectionListener(){
 				public void widgetDefaultSelected(SelectionEvent arg0) {
 					WSDLBrowseDialog.this.operationTextValue = opList.getSelection()[0];
 				}
 				public void widgetSelected(SelectionEvent arg0) {
 					widgetDefaultSelected(arg0);
 				}
 			});
 		}
 		
 		mainComposite.pack();
 		
 		return mainComposite;
 	}
 	
 	class ReadWSDLProgress implements IRunnableWithProgress {
 		
 		private URL testURL = null;
 		private IStatus result = null;
 		
 		public void setTestURL ( URL url ) {
 			this.testURL = url;
 		}
 		
 		public IStatus getResult() {
 			return this.result;
 		}
 		
 		public void run(IProgressMonitor monitor) {
 			monitor
 					.beginTask(JBossWSUIMessages.WSDLBrowseDialog_Status_ParsingWSDLFromURL,
 							100);
 			try {
 				IStatus testStatus =
 					TesterWSDLUtils.isWSDLAccessible(testURL);
 				if (testStatus.getSeverity() != IStatus.OK){ 
 					result = testStatus;
 				}
 				wsdlDefinition =
 					TesterWSDLUtils.readWSDLURL(testURL);
 			} catch (WSDLException e) {
 				result = StatusUtils.errorStatus(
 						JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Parse_Error, e);
 			}
 			monitor.done();
 		}
 	}
 	
 	private IStatus parseWSDLFromURL ( final URL testURL, boolean showProgress) {
 		
 		if (showProgress) {
 			ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
 			try {
 				ReadWSDLProgress readWSDLProgress = new ReadWSDLProgress();
 				readWSDLProgress.setTestURL(testURL);
 				dialog.run(true, true, readWSDLProgress);
 				return readWSDLProgress.getResult();
 			} catch (InvocationTargetException e) {
 				return StatusUtils.errorStatus(
 						JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Parse_Error, e);
 			} catch (InterruptedException e) {
 				return StatusUtils.errorStatus(
 						JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Parse_Error, e);
 			} catch (NullPointerException e) {
 				return StatusUtils.errorStatus(
 						JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Parse_Error, e);
 			}
 		} else {
 			try {
 				IStatus testStatus =
 					TesterWSDLUtils.isWSDLAccessible(testURL);
 				if (testStatus.getSeverity() != IStatus.OK) {
 					return StatusUtils.errorStatus(testStatus.getMessage(), 
 							testStatus.getException());
 				}
 				wsdlDefinition =
 					TesterWSDLUtils.readWSDLURL(testURL);
 			} catch (WSDLException e) {
 				return StatusUtils.errorStatus(
 						JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Parse_Error, e);
 			} catch (NullPointerException e) {
 				return StatusUtils.errorStatus(
 						JBossWSUIMessages.WSDLBrowseDialog_Error_Msg_Parse_Error, e);
 			}
 		}
 		return Status.OK_STATUS;
 	}
 
 	private void updateOperationList(){
 		if (portCombo.getSelectionIndex() > -1) {
 			String text = portCombo.getItem(portCombo.getSelectionIndex());
 			portTextValue = text;
 			Port port = (Port) portCombo.getData(text);
 
 			opList.removeAll();
 			
 			Binding wsdlBinding = port.getBinding();
 			this.bindingValue = wsdlBinding.getQName().getLocalPart();
 			PortType portType = wsdlBinding.getPortType();
 			@SuppressWarnings("rawtypes")
 			java.util.List operations = portType.getOperations();
 			
 			@SuppressWarnings("unchecked")
 			Operation[] operationsArray = 
 				(Operation[]) operations.toArray(new Operation[operations.size()]);
 			Arrays.sort(operationsArray, new WSDLOperationComparator());
 
 			for (int i = 0; i < operationsArray.length; i++) {
 				Operation operation = (Operation) operationsArray[i];//iter.next();
 				opList.add(operation.getName());
 				opList.setData(operation.getName(), operation);
 			}
 			if (opList.getItemCount() > 0) {
 				boolean foundIt = false;
 				if (initialOperationTextValue != null) {
 					String[] thelist = opList.getItems();
 					for (int i = 0; i < thelist.length; i++) {
 						if (thelist[i].contentEquals(initialOperationTextValue)) {
 							opList.select(i);
 							foundIt = true;
 							break;
 						}
 					}
 				}
 				if (!foundIt)
 					opList.select(0);
 				this.operationTextValue = opList.getSelection()[0];
 			}
 		}
 	}
 
 	class WSDLOperationComparator implements Comparator<Operation>{
 
 	    public int compare(Operation o1, Operation o2) {
 	        return o1.getName().compareToIgnoreCase(o2.getName());
 	    }
 	}	
 	
 	private void updatePortCombo(){
 		if (serviceCombo.getSelectionIndex() > -1) {
 			String text = serviceCombo.getItem(serviceCombo.getSelectionIndex());
 			serviceTextValue = text;
 			Service service = (Service) serviceCombo.getData(text);
 
 			portCombo.removeAll();
 			opList.removeAll();
 			
 			Iterator<?> iter = service.getPorts().values().iterator();
 			while (iter.hasNext()) {
 				Port port = (Port) iter.next();
 				if (port.getBinding() != null && port.getBinding().getExtensibilityElements() != null) {
 					@SuppressWarnings("rawtypes")
 					java.util.List elements = port.getBinding().getExtensibilityElements();
 					for (int i = 0; i < elements.size(); i++) {
 						if (elements.get(i) instanceof SOAPBinding || elements.get(i) instanceof SOAP12Binding ) {
 							portCombo.add(port.getName());
 							portCombo.setData(port.getName(), port);
 						}
 					}
 				}
 			}
 			if (portCombo.getItemCount() > 0) {
 				portCombo.select(0);
 				portTextValue = portCombo.getText();
 			}
 			updateOperationList();
 		}
 	}
 	
 	private void updateServiceCombo () {
 		serviceCombo.setEnabled(true);
 		portCombo.setEnabled(true);
 		opList.setEnabled(true);
 		serviceCombo.removeAll();
 		portCombo.removeAll();
 		opList.removeAll();
 		getButton(IDialogConstants.OK_ID).setEnabled(true);
 		
 		if (wsdlDefinition != null && wsdlDefinition.getServices() != null && !wsdlDefinition.getServices().isEmpty()) {
 			Iterator<?> iter = wsdlDefinition.getServices().values().iterator();
 			while (iter.hasNext()) {
 				Service service = (Service) iter.next();
 				serviceCombo.add(service.getQName().getLocalPart());
 				serviceCombo.setData(service.getQName().getLocalPart(), service);
 			}
 			if (serviceCombo.getItemCount() > 0) {
 				serviceCombo.select(0);
 				serviceTextValue = serviceCombo.getText();
 			}
 			updatePortCombo();
 		} else {
 			// no services
 			serviceCombo.add(JBossWSUIMessages.WSDLBrowseDialog_No_Services_Available);
 			serviceCombo.select(0);
 			setMessage(JBossWSUIMessages.WSDLBrowseDialog_No_Services_Available_Warning, IMessageProvider.WARNING);
 			serviceCombo.setEnabled(false);
 			portCombo.setEnabled(false);
 			opList.setEnabled(false);
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 		}
 	}
 	
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(JBossWSUIMessages.WSDLBrowseDialog_Dialog_Title);
 	}
 	
 	private void setGroupEnabled ( boolean flag ) {
 		group.setEnabled(flag);
 		operationLabel.setEnabled(flag);
 		opList.setEnabled(flag);
 		portCombo.setEnabled(flag);
 		portLabel.setEnabled(flag);
 		serviceCombo.setEnabled(flag);
 		serviceLabel.setEnabled(flag);
 		if (getButton(IDialogConstants.OK_ID) != null) {
 			getButton(IDialogConstants.OK_ID).setEnabled(flag);
 		}
 		
 		if (!flag) {
 			opList.removeAll();
 			portCombo.removeAll();
 			portCombo.setText(""); //$NON-NLS-1$
 			serviceCombo.removeAll();
 			serviceCombo.setText(""); //$NON-NLS-1$
 		}
 	}
 	
 	private IStatus validate(boolean showProgress){
 		String urlText = locationCombo.getText();
 		try {
 			final URL testURL = new URL(urlText);
 			IStatus status = parseWSDLFromURL(testURL, false);
 			if (status != null && !status.isOK()) {
 				return status;
 			}
 //			parseWSDLFromURL(testURL);
 //			wsdlDefinition =
 //				TesterWSDLUtils.readWSDLURL(testURL);
 			if (showServicePortOperaton)
 				updateServiceCombo();
 		} catch (MalformedURLException e) {
 			return StatusUtils.errorStatus(JBossWSUIMessages.WSDLBrowseDialog_Status_Invalid_URL, e);
 //		} catch (WSDLException e) {
 //			return StatusUtils.errorStatus(JBossWSUIMessages.WSDLBrowseDialog_Status_WSDL_Unavailable, e);
 		}
 		return Status.OK_STATUS;
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		Control control = super.createContents(parent);
 
 		if (showServicePortOperaton)
 			setGroupEnabled(false);
 
 		if (WSDLBrowseDialog.wsdlTextValue != null) {
 			this.locationCombo.setText(wsdlTextValue);
 			IStatus status = validate(false);
 			if (status != Status.OK_STATUS) {
 				if (showServicePortOperaton)
 					setGroupEnabled(false);
 			} else {
 				if (showServicePortOperaton)
 					setGroupEnabled(true);
 			}
 		}
 		control.pack(true);
 		return control;
 	}
 	
 }

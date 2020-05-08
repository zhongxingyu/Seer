 /*******************************************************************************
  * Copyright (c) 2013 Remain Software and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wim Jongman and Markus Kuppe 
  *******************************************************************************/
 package org.eclipse.ecf.example.chat.product.parts;
 
 import java.util.Dictionary;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.ecf.core.ContainerCreateException;
 import org.eclipse.ecf.core.ContainerFactory;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.example.chat.model.IChatMessage;
 import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Text;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.events.SelectionAdapter;
 
 public class ChatPart {
 	private Text txtMessage;
 	private Text txtHandle;
 	private Text text;
 	private ServiceRegistration<?> serviceRegistration;
 	private Label lblNewLabel;
 	private Text txtServer;
 	private Button btnConnect;
	private String lastMessage;
 
 	@PostConstruct
 	public void createComposite(Composite parent) {
 		GridLayout gl_parent = new GridLayout();
 		gl_parent.numColumns = 3;
 		parent.setLayout(gl_parent);
 
 		lblNewLabel = new Label(parent, SWT.NONE);
 		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblNewLabel.setText("Server");
 
 		txtServer = new Text(parent, SWT.BORDER);
 		txtServer.setText("yazafatutu.com");
 		txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
 				1, 1));
 
 		btnConnect = new Button(parent, SWT.NONE);
 		btnConnect.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				startZookeeper();
 			}
 		});
 		btnConnect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1));
 		btnConnect.setText("Connect");
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout gl_composite = new GridLayout(1, false);
 		gl_composite.marginHeight = 0;
 		gl_composite.marginWidth = 0;
 		composite.setLayout(gl_composite);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3,
 				1));
 
 		text = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
 		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		text.setEditable(false);
 		text.setTabs(15);
 
 		txtHandle = new Text(parent, SWT.BORDER);
 		txtHandle.setText("handle");
 		txtHandle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1));
 
 		txtMessage = new Text(parent, SWT.BORDER);
 		txtMessage.setText("message");
 		txtMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 
 		Button btnSend = new Button(parent, SWT.NONE);
 		btnSend.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
 				1, 1));
 		btnSend.setText("Send");
 		btnSend.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
				publish(txtHandle.getText() + ": " + txtMessage.getText()
						+ "\r\n");
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 
 		setupTracker();
 	}
 
 	private void setupTracker() {
 
 		FrameworkUtil.getBundle(getClass()).getBundleContext()
 				.addServiceListener(new ServiceListener() {
 
 					@Override
 					public void serviceChanged(ServiceEvent event) {
 						ServiceReference<?> reference = event
 								.getServiceReference();
						final Object service = FrameworkUtil
								.getBundle(getClass()).getBundleContext()
								.getService(reference);
 						if (event.getType() == event.REGISTERED) {
 							System.out.println("Registered: "
 									+ service.getClass().getSimpleName());
 							if (service instanceof IChatMessage) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										if (!((IChatMessage) service)
												.getMessage().equals(
														lastMessage)) {
											lastMessage = ((IChatMessage) service)
													.getMessage();
											text.setText(lastMessage
													+ text.getText());
										}
									}
								});
 							}
 						}
 						if (event.getType() == event.UNREGISTERING) {
 							System.out.println("UnRegistered: "
 									+ service.getClass().getSimpleName());
 						}
 					}
 				});
 	}
 
 	protected void publish(String message) {
 
 		// get rid of the previous message
 		disposeServiceRegistration();
 
 		// Setup properties for remote service distribution, as per OSGi 4.2
 		// remote services
 		// specification (chap 13 in compendium spec)
 		Properties props = new Properties();
 		// add OSGi service property indicated export of all interfaces exposed
 		// by service (wildcard)
 		props.put("service.exported.interfaces", "*");
 		// add OSGi service property specifying config
 		props.put("service.exported.configs", "ecf.r_osgi.peer");
 		// add ECF service property specifying container factory args
 		props.put("ecf.exported.containerfactoryargs",
 				"r-osgi://localhost:9278");
 		// register remote service
 		serviceRegistration = FrameworkUtil
 				.getBundle(getClass())
 				.getBundleContext()
 				.registerService(IChatMessage.class.getName(),
 						new ChatMessage(message), (Dictionary) props);
 	}
 
 	@PreDestroy
 	private void disposeServiceRegistration() {
 		if (serviceRegistration != null) {
 			serviceRegistration.unregister();
 			serviceRegistration = null;
 		}
 	}
 
 	@Focus
 	public void setFocus() {
 		txtMessage.setFocus();
 	}
 
 	protected void startZookeeper() {
 		IContainer singleton = null;
 		try {
 			singleton = ContainerFactory.getDefault().createContainer(
 					ZooDiscoveryContainerInstantiator.NAME);
 		} catch (ContainerCreateException e1) {
 		}
 		if (singleton.getConnectedID() != null)
 			singleton.disconnect();
 		try {
 			singleton.connect(
 					singleton.getConnectNamespace().createInstance(
 							new String[] { "zoodiscovery.flavor.centralized="
 									+ txtServer.getText() }), null);
 		} catch (Exception e) {
 		}
 	}
 }

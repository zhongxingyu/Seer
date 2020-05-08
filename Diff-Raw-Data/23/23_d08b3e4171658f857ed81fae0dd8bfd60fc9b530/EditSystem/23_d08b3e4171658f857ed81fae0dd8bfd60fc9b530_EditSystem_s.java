 package cmg.org.monitor.module.client;
 
 import cmg.org.monitor.entity.shared.NotifyMonitor;
 import cmg.org.monitor.entity.shared.SystemMonitor;
 import cmg.org.monitor.ext.model.shared.GroupMonitor;
 import cmg.org.monitor.ext.model.shared.MonitorContainer;
 import cmg.org.monitor.util.shared.HTMLControl;
 import cmg.org.monitor.util.shared.MonitorConstant;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.regexp.shared.RegExp;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class EditSystem extends AncestorEntryPoint {
 	int index = 0;
 	SystemMonitor system;
 	MonitorContainer container;
 	ListBox listGroup;
 	ListBox listActive;
 	ListBox listProtocol;
 	TextBox txtName;
 	TextBox txtURL;
 	TextBox txtIP;
 	TextBox txtRemote;
 	TextBox txtEmail;
 	Button btnEdit;
 	Button bttBack;
 	Button bttReset;
 	Label labelName;
 	Label labelurl;
 	Label labelip;
 	Label labelremoteurl;
 	Label labelactive;
 	Label labelprotocol;
 	Label labelmailgroup;
 	Label labelEmail;
 	Label lblNotifyCpu;
 	Label lblNotifyMemory;
 	Label lblNotifyServices;
 	Label lblNotifyServicesConnection;
 	Label lblNotifyJVM;
 	CheckBox cbNotifyJVM;
 	CheckBox cbNotifyCpu;
 	CheckBox cbNotifyMemory;
 	CheckBox cbNotifyServices;
 	CheckBox cbNotifyServicesConnection;
 	AbsolutePanel panelAdding;
 	AbsolutePanel panelValidateName;
 	AbsolutePanel panelValidateURL;
 	AbsolutePanel panelValidateIP;
 	AbsolutePanel panelValidateRemoteURL;
 	AbsolutePanel panelLabelEmail;
 	AbsolutePanel paneltxtEmail;
 	AbsolutePanel panelButton;
 	AbsolutePanel panelValidateEmail;
 	private static FlexTable tableForm;
 	private static Grid tableNotify;
 	private DisclosurePanel advancedDisclosure;
 	private NotifyMonitor notify;
 	StringBuffer description;
 
 	protected void init() {
 		if (currentPage == HTMLControl.PAGE_EDIT_SYSTEM) {
 			final String sysID = HTMLControl.getSystemId(History.getToken());
 			try {
 				monitorGwtSv.validSystemId(sysID,
 						new AsyncCallback<SystemMonitor>() {
 							@Override
 							public void onSuccess(SystemMonitor result) {
 								if (result != null) {
 									tableForm = new FlexTable();
 									addWidget(HTMLControl.ID_BODY_CONTENT,
 											tableForm);
 									initFlexTable(sysID);
 								} else {
 									showMessage(
 											"Invalid System ID.",
 											HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 											"Goto System Management. ",
 											HTMLControl.RED_MESSAGE, true);
 								}
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								showMessage(
 										"Oops! Error.",
 										HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 										"Goto System Management. ",
 										HTMLControl.RED_MESSAGE, true);
 							}
 						});
 			} catch (Exception e) {
 				showMessage("Oops! Error.",
 						HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 						"Goto System Management. ", HTMLControl.RED_MESSAGE,
 						true);
 			}
 		}
 	}
 
 	void initFlexTable(String sysID) {
 		monitorGwtSv.getSystemMonitorContainer(sysID,
 				new AsyncCallback<MonitorContainer>() {
 
 					@Override
 					public void onSuccess(MonitorContainer result) {
 						if (result != null) {
 							system = result.getSys();
 							notify = result.getNotify();
 							container = result;
 							tableNotify = new Grid(5, 2);
 							tableNotify.setCellSpacing(3);
 							cbNotifyCpu = new CheckBox();
 							cbNotifyCpu.setValue(notify.isNotifyCpu());
 							cbNotifyCpu.setStyleName("checkbox-size");
 							
 							cbNotifyMemory = new CheckBox();
 							cbNotifyMemory.setValue(notify.isNotifyMemory());
 							cbNotifyMemory.setStyleName("checkbox-size");
 							
 							cbNotifyServices = new CheckBox();
 							cbNotifyServices.setValue(notify.isNotifyServices());
 							cbNotifyServices.setStyleName("checkbox-size");
 							
 							cbNotifyJVM = new CheckBox();
 							cbNotifyJVM.setValue(notify.isJVM());
 							cbNotifyJVM.setStyleName("checkbox-size");
 							
 							cbNotifyServicesConnection = new CheckBox();
 							cbNotifyServicesConnection.setValue(notify
 									.isNotifyServicesConnection());
 							cbNotifyServicesConnection.setStyleName("checkbox-size");
 							
 							lblNotifyJVM = new Label(MonitorConstant.Notify_JVM);
 							lblNotifyCpu = new Label(MonitorConstant.Notify_Cpu);
 							lblNotifyMemory = new Label(
 									MonitorConstant.Notify_Memory);
 							lblNotifyServices = new Label(
 									MonitorConstant.Notify_Service);
 							lblNotifyServicesConnection = new Label(
 									MonitorConstant.Notify_ServiceConnection);
 							tableNotify.setWidget(0, 0, lblNotifyCpu);
 							tableNotify.setWidget(0, 1, cbNotifyCpu);
 							tableNotify.setWidget(1, 0, lblNotifyMemory);
 							tableNotify.setWidget(1, 1, cbNotifyMemory);
 							tableNotify.setWidget(2, 0, lblNotifyServices);
 							tableNotify.setWidget(2, 1, cbNotifyServices);
 							tableNotify.setWidget(3, 0,
 									lblNotifyServicesConnection);
 							tableNotify.setWidget(3, 1,
 									cbNotifyServicesConnection);
 							tableNotify.setWidget(4, 0, lblNotifyJVM);
 							tableNotify.setWidget(4, 1, cbNotifyJVM);
 							advancedDisclosure = new DisclosurePanel(
 									HTMLControl.NOTIFY_OPTION);
 							advancedDisclosure.setAnimationEnabled(true);
 							advancedDisclosure.setContent(tableNotify);
 							advancedDisclosure.setOpen(true);
 
 							labelEmail = new Label();
 							labelEmail.setText("Email");
 							txtEmail = new TextBox();
 							txtEmail.setWidth("196px");
 							txtEmail.setHeight("30px");
 							tableForm.setCellPadding(3);
 							tableForm.setCellSpacing(3);
 							tableForm.getFlexCellFormatter().setWidth(0, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(1, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(2, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(3, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(4, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(5, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(6, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(7, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(8, 0,
 									"298px");
 							tableForm.getFlexCellFormatter().setWidth(9, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(10, 0,
 									"100px");
 							tableForm.getFlexCellFormatter().setWidth(11, 0,
 									"100px");
 
 							labelName = new Label();
 							labelName.setText("Name");
 
 							labelurl = new Label();
 							labelurl.setText("URL");
 
 							labelip = new Label();
 							labelip.setText("IP");
 
 							labelremoteurl = new Label();
 							labelremoteurl.setText("Remote-URL");
 
 							labelactive = new Label();
 							labelactive.setText("Active");
 
 							labelprotocol = new Label();
 							labelprotocol.setText("Protocol");
 
 							labelmailgroup = new Label();
 							labelmailgroup.setText("Notification mail group");
 
 							txtName = new TextBox();
 							txtName.setWidth("196px");
 							txtName.setHeight("30px");
 							txtName.setText(system.getName());
 
 							txtURL = new TextBox();
 							txtURL.setWidth("196px");
 							txtURL.setHeight("30px");
 							txtURL.setText(system.getUrl());
 
 							txtIP = new TextBox();
 							txtIP.setWidth("196px");
 							txtIP.setHeight("30px");
 							txtIP.setText(system.getIp());
 
 							txtRemote = new TextBox();
 							txtRemote.setWidth("196px");
 							txtRemote.setHeight("30px");
 							txtRemote.setText(system.getRemoteUrl());
 
 							txtEmail.setText(system.getEmailRevice());
 
 							panelLabelEmail = new AbsolutePanel();
 							panelLabelEmail.add(labelEmail);
 							panelLabelEmail.setVisible(false);
 
 							paneltxtEmail = new AbsolutePanel();
 							paneltxtEmail.add(txtEmail);
 
 							listActive = new ListBox();
 							listActive.setWidth("198px");
 							listActive.setHeight("28px");
 							listActive.addItem("Yes");
 							listActive.addItem("No");
 							if (system.isActive()) {
 								listActive.setSelectedIndex(0);
 							} else {
 								listActive.setSelectedIndex(1);
 							}
 
 							listProtocol = new ListBox();
 							listProtocol.setWidth("198px");
 							listProtocol.setHeight("28px");
 							listProtocol.addItem(MonitorConstant.HTTP_PROTOCOL);
 							listProtocol.addItem(MonitorConstant.SMTP_PROTOCOL);
 							if (system.getProtocol().equals("HTTP(s)")) {
 								listProtocol.setSelectedIndex(0);
 								paneltxtEmail.setVisible(false);
 								panelLabelEmail.setVisible(false);
 								txtRemote.setEnabled(true);
 							} else {
 								listProtocol.setSelectedIndex(1);
 								paneltxtEmail.setVisible(true);
 								panelLabelEmail.setVisible(true);
 								txtRemote.setEnabled(false);
 							}
 							listGroup = new ListBox();
 							listGroup.setWidth("198px");
 							listGroup.setHeight("28px");
 							GroupMonitor[] groups = container.getGroups();
 							if (groups != null && groups.length > 0) {
 
 								for (int i = 0; i < groups.length; i++) {
 									listGroup.addItem(groups[i].getName());
 									if (system.getGroupEmail()
 											.equalsIgnoreCase(
 													groups[i].getName())) {
 										index = i;
 									}
 								}
 								listGroup.setSelectedIndex(index);
 							}
 
 							btnEdit = new Button();
 							btnEdit.setText("Update");
 							btnEdit.setStyleName("margin:6px;");
 							btnEdit.addStyleName("form-button");
 
 							bttReset = new Button();
 							bttReset.setText("Reset");
 							bttReset.setStyleName("margin:6px;");
 							bttReset.addStyleName("form-button");
 
 							bttBack = new Button();
 							bttBack.setText("Back");
 							bttBack.setStyleName("margin:6px;");
 							bttBack.addStyleName("form-button");
 
 							panelButton = new AbsolutePanel();
 							panelButton.add(btnEdit);
 							panelButton.add(bttReset);
 							panelButton.add(bttBack);
 
 							panelAdding = new AbsolutePanel();
 							panelAdding
 									.add(new HTML(
 											"<div id=\"img-adding\"><img src=\"images/icon/loading11.gif\"/></div>"));
 							panelAdding.setVisible(false);
 
 							panelValidateName = new AbsolutePanel();
 
 							panelValidateEmail = new AbsolutePanel();
 
 							panelValidateIP = new AbsolutePanel();
 
 							panelValidateURL = new AbsolutePanel();
 
 							panelValidateRemoteURL = new AbsolutePanel();
 
 							tableForm.setWidget(0, 0, labelName);
 							tableForm.setWidget(0, 1, txtName);
 							tableForm.setWidget(0, 2, panelValidateName);
 							tableForm.setWidget(1, 0, labelurl);
 							tableForm.setWidget(1, 1, txtURL);
 							tableForm.setWidget(1, 2, panelValidateURL);
 							tableForm.setWidget(2, 0, labelip);
 							tableForm.setWidget(2, 1, txtIP);
 							tableForm.setWidget(2, 2, panelValidateIP);
 							tableForm.setWidget(3, 0, labelactive);
 							tableForm.setWidget(3, 1, listActive);
 							tableForm.setWidget(4, 0, labelprotocol);
 							tableForm.setWidget(4, 1, listProtocol);
 							tableForm.setWidget(5, 0, panelLabelEmail);
 							tableForm.setWidget(5, 1, paneltxtEmail);
 							tableForm.setWidget(5, 2, panelValidateEmail);
 							tableForm.setWidget(6, 0, labelmailgroup);
 							tableForm.setWidget(6, 1, listGroup);
 							tableForm.setWidget(7, 0, labelremoteurl);
 							tableForm.setWidget(7, 1, txtRemote);
 							tableForm.setWidget(7, 2, panelValidateRemoteURL);
 							tableForm.getFlexCellFormatter()
 									.setColSpan(8, 0, 2);
 							tableForm.setWidget(8, 0, advancedDisclosure);
 							tableForm.getFlexCellFormatter()
 									.setColSpan(9, 0, 2);
 							tableForm.setWidget(9, 0, panelAdding);
 							tableForm.getFlexCellFormatter().setColSpan(10, 0,
 									3);
 							tableForm.setWidget(10, 0, panelButton);
 							initHandler();
 							setVisibleLoadingImage(false);
 							setOnload(false);
 							setVisibleWidget(HTMLControl.ID_BODY_CONTENT, true);
 
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showMessage("Oops! Error.",
 								HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 								"Goto System Management. ",
 								HTMLControl.RED_MESSAGE, true);
 					}
 
 				});
 
 	}
 
 	void initHandler() {
 		listProtocol.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				if (listProtocol.getSelectedIndex() == 0) {
 					txtRemote.setEnabled(true);
 					txtRemote.setText(system.getRemoteUrl());
 					panelLabelEmail.setVisible(false);
 					paneltxtEmail.setVisible(false);
 					txtEmail.setText("");
 					panelValidateEmail.setVisible(false);
 				} else if (listProtocol.getSelectedIndex() == 1) {
 					txtRemote.setText("");
 					txtRemote.setEnabled(false);
 					panelLabelEmail.setVisible(true);
 					paneltxtEmail.setVisible(true);
 					txtEmail.setText(system.getEmail());
 					panelValidateRemoteURL.setVisible(false);
 				}
 			}
 		});
 		bttReset.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				panelValidateEmail.setVisible(false);
 				panelValidateIP.setVisible(false);
 				panelValidateName.setVisible(false);
 				panelValidateIP.setVisible(false);
 				panelValidateURL.setVisible(false);
 				txtName.setText(system.getName());
 				txtURL.setText(system.getUrl());
 				txtIP.setText(system.getIp());
 				txtRemote.setText(system.getRemoteUrl());
 				if (system.isActive()) {
 					listActive.setSelectedIndex(0);
 				} else {
 					listActive.setSelectedIndex(1);
 				}
 				listGroup.setSelectedIndex(index);
 				if (system.getProtocol().equals("HTTP(s)")) {
 					listProtocol.setSelectedIndex(0);
 					txtRemote.setEnabled(true);
 					txtRemote.setText(system.getRemoteUrl());
 					panelLabelEmail.setVisible(false);
 					paneltxtEmail.setVisible(false);
 				} else {
 					listProtocol.setSelectedIndex(1);
 					panelLabelEmail.setVisible(true);
 					paneltxtEmail.setVisible(true);
 					txtRemote.setVisible(false);
 					txtEmail.setText(system.getEmail());
 
 				}
 
 				cbNotifyCpu.setValue(notify.isNotifyCpu());
 
 				cbNotifyMemory.setValue(notify.isNotifyMemory());
 
 				cbNotifyServices.setValue(notify.isNotifyServices());
 
 				cbNotifyServicesConnection.setValue(notify
 						.isNotifyServicesConnection());
 				cbNotifyJVM.setValue(notify.isJVM());
 			}
 		});
 		bttBack.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Window.Location.replace(HTMLControl
 						.trimHashPart(Window.Location.getHref())
 						+ HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME);
 			}
 		});
 		btnEdit.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				if (listProtocol.getItemText(listProtocol.getSelectedIndex())
 						.equals(MonitorConstant.HTTP_PROTOCOL)) {
 					
 					String validateName = validateName(txtName.getText());
 					String validateURL = validateURL(txtURL.getText());
 					String validateIp = validateIP(txtIP.getText());
 					String validateRemoteURL = validateRemoteURL(txtRemote
 							.getText());
 					panelValidateEmail.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateName.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateURL.setVisible(false);
 					if (validateName != "") {
 						panelValidateName.clear();
 						panelValidateName.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateName + "</div>"));
 						panelValidateName.setVisible(true);
 						panelValidateURL.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateRemoteURL.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						return;
 					} else if (validateURL != "") {
 						panelValidateURL.clear();
 						panelValidateURL.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateURL + "</div>"));
 						panelValidateURL.setVisible(true);
 						panelValidateName.setVisible(false);
 						panelValidateRemoteURL.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						return;
 					} else if (validateIp != "") {
 						panelValidateIP.clear();
 						panelValidateIP.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateIp + "</div>"));
 						panelValidateIP.setVisible(true);
 						panelValidateName.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateURL.setVisible(false);
 						return;
 					} else if (validateRemoteURL != "") {
 						if (!listProtocol.getItemText(
 								listProtocol.getSelectedIndex()).equals("SMTP")) {
 							panelValidateRemoteURL.clear();
 							panelValidateRemoteURL.add(new HTML(
 									"<div class=\"error-left\"></div><div class=\"error-inner\">"
 											+ validateRemoteURL + "</div>"));
 							panelValidateRemoteURL.setVisible(true);
 							panelValidateIP.setVisible(false);
 							panelValidateName.setVisible(false);
 							panelValidateEmail.setVisible(false);
 							panelValidateURL.setVisible(false);
 							return;
 						}
 					}
 					panelValidateEmail.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateName.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateURL.setVisible(false);
 					panelAdding.setVisible(true);
 
 					NotifyMonitor nm = new NotifyMonitor();
 					nm.setNotifyCpu(cbNotifyCpu.getValue());
 					nm.setNotifyMemory(cbNotifyMemory.getValue());
 					nm.setNotifyServices(cbNotifyServices.getValue());
 					nm.setNotifyServicesConnection(cbNotifyServicesConnection
 							.getValue());
 					nm.setJVM(cbNotifyJVM.getValue());
 					
 					SystemMonitor sysNew = new SystemMonitor();
 					sysNew.setId(system.getId());
 					sysNew.setName(txtName.getText());
 					sysNew.setUrl(txtURL.getText());
 					sysNew.setProtocol(listProtocol.getItemText(listProtocol
 							.getSelectedIndex()));
 					sysNew.setGroupEmail(listGroup.getItemText(listGroup
 							.getSelectedIndex()));
 					sysNew.setIp(txtIP.getText());
 					sysNew.setRemoteUrl(txtRemote.getText());
 					sysNew.setEmailRevice(txtEmail.getText());
 					sysNew.setActive(isActive(listActive.getItemText(listActive
 							.getSelectedIndex())));
 					sysNew.setNotify(nm);
 				
 					editSystem(sysNew);
 				} else if (listProtocol.getItemText(
 						listProtocol.getSelectedIndex()).equals(
 						MonitorConstant.SMTP_PROTOCOL)) {
 					String validateName = validateName(txtName.getText());
 					String validateURL = validateURL(txtURL.getText());
 					String validateIp = validateIP(txtIP.getText());
 					String validateEmail = validateEmail(txtEmail.getText());
 					panelValidateEmail.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateName.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateURL.setVisible(false);
 					if (validateName != "") {
 						panelValidateName.clear();
 						panelValidateName.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateName + "</div>"));
 						panelValidateName.setVisible(true);
 						panelValidateURL.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateRemoteURL.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						return;
 					} else if (validateURL != "") {
 						panelValidateURL.clear();
 						panelValidateURL.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateURL + "</div>"));
 						panelValidateURL.setVisible(true);
 						panelValidateName.setVisible(false);
 						panelValidateRemoteURL.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						return;
 					} else if (validateIp != "") {
 						panelValidateIP.clear();
 						panelValidateIP.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateIp + "</div>"));
 						panelValidateIP.setVisible(true);
 						panelValidateName.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateURL.setVisible(false);
 						return;
 					} else if (validateEmail != "") {
 						panelValidateEmail.clear();
 						panelValidateEmail.add(new HTML(
 								"<div class=\"error-left\"></div><div class=\"error-inner\">"
 										+ validateEmail + "</div>"));
 						panelValidateEmail.setVisible(true);
 						panelValidateIP.setVisible(true);
 						panelValidateName.setVisible(false);
 						panelValidateIP.setVisible(false);
 						panelValidateURL.setVisible(false);
 						return;
 
 					}
 					panelValidateEmail.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateName.setVisible(false);
 					panelValidateIP.setVisible(false);
 					panelValidateURL.setVisible(false);
 					panelAdding.setVisible(true);
 
 					NotifyMonitor nm = new NotifyMonitor();
 					nm.setNotifyCpu(cbNotifyCpu.getValue());
 					nm.setNotifyMemory(cbNotifyMemory.getValue());
 					nm.setNotifyServices(cbNotifyServices.getValue());
 					nm.setNotifyServicesConnection(cbNotifyServicesConnection
 							.getValue());
 					nm.setJVM(cbNotifyJVM.getValue());
 					SystemMonitor sysNew = new SystemMonitor();
 					sysNew.setId(system.getId());
 					sysNew.setName(txtName.getText());
 					sysNew.setUrl(txtURL.getText());
 					sysNew.setProtocol(listProtocol.getItemText(listProtocol
 							.getSelectedIndex()));
 					sysNew.setGroupEmail(listGroup.getItemText(listGroup
 							.getSelectedIndex()));
 					sysNew.setIp(txtIP.getText());
 					sysNew.setEmailRevice(txtEmail.getText());
 					sysNew.setRemoteUrl(txtRemote.getText());
 					sysNew.setActive(isActive(listActive.getItemText(listActive
 							.getSelectedIndex())));
 					sysNew.setNotify(nm);
 					editSystem(sysNew);
 				}
 			}
 		});
 	}
 
 	private void editSystem(SystemMonitor sys) {
 		panelAdding.setVisible(false);
 		monitorGwtSv.editSystem(sys, new AsyncCallback<Boolean>() {
 			@Override
 			public void onSuccess(Boolean result) {
 				if (result) {
 					showMessage("System edited sucessfully. ",
 							HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 							"View system list. ", HTMLControl.BLUE_MESSAGE,
 							true);
 				} else {
 					showMessage("Server error! ",
 							HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 							"Goto System Management. ",
 							HTMLControl.RED_MESSAGE, true);
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showMessage("Server error! ",
 						HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 						"Goto System Management. ", HTMLControl.RED_MESSAGE,
 						true);
 			}
 		});
 
 	}
 
 	
 
 	private boolean isActive(String active) {
 		boolean isActive = false;
 		if (active.equals("Yes")) {
 			isActive = true;
 		}
 		
 		return isActive;
 	}
 
 	private String validateName(String name) {
 		String msg = "";
 		
 		if (name == null || name.trim().length() == 0) {
 			msg = "This field is required ";
 			
 		} else if (name.contains("$") || name.contains("%")
 				|| name.contains("*")) {
 			msg = "name is not validate";
 			
 		}
 		return msg;
 
 	}
 
 	// validate URL
 	private String validateURL(String url) {
 		String msg = "";
 		boolean check = true;
 		if (url == null || url.trim().length() == 0) {
 			msg = "This field is required ";
 			check = false;
 		} else if (url.length() < 3) {
 			msg = "URL is not validate";
 			check = false;
 		}
 		
 		return msg;
 	}
 
 	private String validateIP(String ip) {
 		String msg = "";
 		boolean check = true;
 		if (ip == "" || ip == null) {
 			msg = "This field is required";
 			check = false;
 		}
 		String patternStr = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
 		RegExp regExp = RegExp.compile(patternStr);
 		boolean matchFound = regExp.test(ip);
 		if (matchFound == false) {
 			msg = "ip is not validate";
 			check = false;
 		}
 		
 		return msg;
 	}
 
 	/**
 	 * @param remoteUrl
 	 * @return
 	 */
 	private String validateRemoteURL(String remoteUrl) {
 		String msg = "";
 		boolean check = false;
 		if (remoteUrl == null || remoteUrl.trim().length() == 0) {
 			msg += "This field is required ";
 			check = true;
 		} else if (remoteUrl.length() < 3) {
 			msg = "Remote url is not validate";
 			check = true;
 		}
 		if (!check) {
 			if (!remoteUrl.equals(system.getRemoteUrl())) {
 				String[] remoteURLs = container.getRemoteUrls();
 				if (remoteURLs != null) {
 					for (int i = 0; i < remoteURLs.length; i++) {
 						if (remoteURLs[i] != null) {
 							if (remoteUrl.equals(remoteURLs[i])) {
 								msg = "Remote URL is exitsting";
 								break;
 							}
 						}
 					}
 				
 				}
 
 			}
 		}
 
 		return msg;
 	}
 
 	/**
 	 * @param email
 	 * @return
 	 */
 
 	private String validateEmail(String email) {
 		String msg = "";
 		boolean check = false;
 		if (email == null || email == "") {
 			msg = "This field is required";
 			check = true;
 		}
 		String pattern = "\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
 		RegExp regExp = RegExp.compile(pattern);
 		boolean matchFound = regExp.test(email);
 		if (matchFound == false) {
 			msg = "email is not validate";
 			check = true;
 		}
 		if (!check) {
 			if (!email.equals(system.getEmail())) {
 				String[] emails = container.getEmails();
 				if (emails != null) {
 					for (int i = 0; i < emails.length; i++) {
 						if (emails[i] != null) {
 							if (emails[i].equals(email)) {
 								msg = "Email is exitsting";
 								break;
 							}
 						}
 					}
 					
 				}
 			}
 		}
 		return msg;
 	}
 }

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
 import com.google.gwt.user.client.DOM;
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
 
 public class AddnewSystem extends AncestorEntryPoint {
 	ListBox listGroup;
 	TextBox txtName;
 	TextBox txtURL;
 	TextBox txtIP;
 	TextBox txtRemote;
 	TextBox txtEmail;
 	ListBox listActive;
 	ListBox listProtocol;
 	Button bttCreate;
 	Button bttBack;
 	Button bttReset;
 	Label labelName;
 	Label labelurl;
 	Label labelip;
 	Label labelremoteurl;
 	Label labelactive;
 	Label labelprotocol;
 	Label labelmailgroup;
 	Label labeladdnew;
 	Label labelEmail;
 	Label lblNotifyCpu;
 	Label lblNotifyMemory;
 	Label lblNotifyServices;
 	Label lblNotifyServicesConnection;
 	Label lblNotifyJVM;
 	CheckBox cbNotifyCpu;
 	CheckBox cbNotifyMemory;
 	CheckBox cbNotifyServices;
 	CheckBox cbNotifyServicesConnection;
 	CheckBox cbNotifyJVM;
 	AbsolutePanel panelAdding;
 	AbsolutePanel panelValidateName;
 	AbsolutePanel panelValidateURL;
 	AbsolutePanel panelValidateIP;
 	AbsolutePanel panelValidateRemoteURL;
 	AbsolutePanel panelButton;
 	AbsolutePanel panelValidateEmail;
 	private static FlexTable tableForm;
 	private static Grid tableNotify;
 	AbsolutePanel panelLabelEmail;
 	AbsolutePanel panelTextEmail;
 	MonitorContainer container;
 	DisclosurePanel advancedDisclosure;
 
 	@Override
 	protected void init() {
 		if (currentPage == HTMLControl.PAGE_ADD_SYSTEM) {
 			initUi();
 			initFlextTable();
 		}
 	}
 
 	/**
 	 * @param active
 	 * @return
 	 */
 	private boolean isActive(String active) {
 		boolean isActive = false;
 		if (active.equals("Yes")) {
 			isActive = true;
 		}
 		return isActive;
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
 			msg = "Email is not validate";
 			check = true;
 		}
 		if (!check) {
 			String[] emails = container.getEmails();
 			if (emails != null && emails.length > 0) {
 				for (String e : emails) {
 					if (e != null && e.equals(email)) {
 						msg = "Email is existing";
 						break;
 					}
 
 				}
 			}
 
 		}
 		return msg;
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
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
 
 	/**
 	 * @param url
 	 * @return
 	 */
 	private String validateURL(String url) {
 		String msg = "";
 		if (url == null || url.trim().length() == 0) {
 			msg = "This field is required ";
 		} else if (url.length() < 3) {
 			msg = "URL is not validate";
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
 			msg = "This field is required ";
 			check = true;
 		} else if (remoteUrl.length() < 3) {
 			msg = "Remote url is not validate";
 			check = true;
 		}
 		if (check != true) {
 			String[] remoteUrls = container.getRemoteUrls();
 			if (remoteUrls != null && remoteUrls.length > 0) {
 				for (String r : remoteUrls) {
 					if (r != null && r.equalsIgnoreCase(remoteUrl)) {
 						msg = "Remote-URL is existing";
 						break;
 					}
 				}
 			}
 		}
 		return msg;
 	}
 
 	/**
 	 * @param ip
 	 * @return
 	 */
 	private String validateIP(String ip) {
 		String msg = "";
 		if (ip == "" || ip == null) {
 			msg = "This field is required";
 		}
 		String patternStr = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
 		RegExp regExp = RegExp.compile(patternStr);
 		boolean matchFound = regExp.test(ip);
 		if (matchFound == false) {
 			msg = "ip is not validate";
 		}
 		return msg;
 	}
 
 	void initUi() {
 		tableNotify = new Grid(5, 2);
 		tableNotify.setCellSpacing(3);
 		
 		cbNotifyCpu = new CheckBox();
 		cbNotifyCpu.setValue(true);
		cbNotifyCpu.setStyleName("");
 		cbNotifyCpu.setStyleName("checkbox-size");
 		lblNotifyCpu = new Label(MonitorConstant.Notify_Cpu);
 		
 		cbNotifyMemory = new CheckBox();
 		cbNotifyMemory.setValue(true);
		cbNotifyMemory.setStyleName("");
 		cbNotifyMemory.setStyleName("checkbox-size");
 		lblNotifyMemory = new Label(MonitorConstant.Notify_Memory);
 		
 		cbNotifyServices = new CheckBox();
 		cbNotifyServices.setValue(true);
		cbNotifyServices.setStyleName("");
 		cbNotifyServices.setStyleName("checkbox-size");
 		lblNotifyServices = new Label(MonitorConstant.Notify_Service);
 		
 		cbNotifyServicesConnection = new CheckBox();
 		cbNotifyServicesConnection.setValue(true);
		cbNotifyServicesConnection.setStyleName("");
 		cbNotifyServicesConnection.setStyleName("checkbox-size");
 		lblNotifyServicesConnection = new Label(
 				MonitorConstant.Notify_ServiceConnection);
 		
 		cbNotifyJVM = new CheckBox();
		cbNotifyJVM.setStyleName("");
 		cbNotifyJVM.setStyleName("checkbox-size");
 		cbNotifyJVM.setValue(true);
 		lblNotifyJVM = new Label(MonitorConstant.Notify_JVM);
 		
 		
 		
 		
 		
 		tableNotify.setWidget(0, 0, lblNotifyCpu);
 		tableNotify.setWidget(0, 1, cbNotifyCpu);
 		tableNotify.setWidget(1, 0, lblNotifyMemory);
 		tableNotify.setWidget(1, 1, cbNotifyMemory);
 		tableNotify.setWidget(2, 0, lblNotifyServices);
 		tableNotify.setWidget(2, 1, cbNotifyServices);
 		tableNotify.setWidget(3, 0, lblNotifyServicesConnection);
 		tableNotify.setWidget(3, 1, cbNotifyServicesConnection);
 		tableNotify.setWidget(4, 0, lblNotifyJVM);
 		tableNotify.setWidget(4, 1, cbNotifyJVM);
 		
 		advancedDisclosure = new DisclosurePanel(HTMLControl.NOTIFY_OPTION);
 		advancedDisclosure.setAnimationEnabled(true);
 		advancedDisclosure.setContent(tableNotify);
 
 		tableForm = new FlexTable();
 		tableForm.setCellPadding(3);
 		tableForm.setCellSpacing(3);
 		tableForm.getFlexCellFormatter().setWidth(0, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(1, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(2, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(3, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(4, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(5, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(6, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(7, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(8, 0, "298px");
 		tableForm.getFlexCellFormatter().setWidth(9, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(10, 0, "100px");
 		tableForm.getFlexCellFormatter().setWidth(11, 0, "100px");
 		
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
 
 		labelEmail = new Label();
 		labelEmail.setText("Email");
 
 		labelmailgroup = new Label();
 		labelmailgroup.setText("Notification mail group");
 
 		txtName = new TextBox();
 		txtName.setWidth("196px");
 		txtName.setHeight("30px");
 
 		txtURL = new TextBox();
 		txtURL.setWidth("196px");
 		txtURL.setHeight("30px");
 
 		txtIP = new TextBox();
 		txtIP.setWidth("196px");
 		txtIP.setHeight("30px");
 
 		txtRemote = new TextBox();
 		txtRemote.setWidth("196px");
 		txtRemote.setHeight("30px");
 
 		txtEmail = new TextBox();
 		txtEmail.setWidth("196px");
 		txtEmail.setHeight("30px");
 
 		listActive = new ListBox();
 		listActive.setWidth("198px");
 		listActive.setHeight("28px");
 		listActive.setSelectedIndex(0);
 		listActive.addItem("Yes");
 		listActive.addItem("No");
 
 		listProtocol = new ListBox();
 		listProtocol.setWidth("198px");
 		listProtocol.setHeight("28px");
 		listProtocol.setSelectedIndex(0);
 		listProtocol.addItem(MonitorConstant.HTTP_PROTOCOL);
 		listProtocol.addItem(MonitorConstant.SMTP_PROTOCOL);
 
 		listGroup = new ListBox();
 		listGroup.setWidth("198px");
 		listGroup.setHeight("28px");
 
 		bttCreate = new Button();
 		bttCreate.setText("Create");
 		bttCreate.setStyleName("margin:6px;");
 		bttCreate.addStyleName("form-button");
 
 		bttReset = new Button();
 		bttReset.setText("Reset");
 		bttReset.setStyleName("margin:6px;");
 		bttReset.addStyleName("form-button");
 
 		bttBack = new Button();
 		bttBack.setText("Back");
 		bttBack.setStyleName("margin:6px;");
 		bttBack.addStyleName("form-button");
 
 		panelLabelEmail = new AbsolutePanel();
 		panelLabelEmail.add(labelEmail);
 		panelLabelEmail.setVisible(false);
 
 		panelTextEmail = new AbsolutePanel();
 		panelTextEmail.add(txtEmail);
 		panelTextEmail.setVisible(false);
 
 		panelButton = new AbsolutePanel();
 		panelButton.add(bttCreate);
 		panelButton.add(bttReset);
 		panelButton.add(bttBack);
 
 		panelAdding = new AbsolutePanel();
 		panelAdding
 				.add(new HTML(
 						"<div id=\"img-adding\"><img src=\"images/icon/loading11.gif\"/></div>"));
 		panelAdding.setVisible(false);
 
 		panelValidateIP = new AbsolutePanel();
 		panelValidateEmail = new AbsolutePanel();
 		panelValidateName = new AbsolutePanel();
 		panelValidateURL = new AbsolutePanel();
 		panelValidateRemoteURL = new AbsolutePanel();
 		panelValidateName.setVisible(false);
 		panelValidateIP.setVisible(false);
 		panelValidateURL.setVisible(false);
 		panelValidateRemoteURL.setVisible(false);
 		panelValidateEmail.setVisible(false);
 
 		MyHandler handler = new MyHandler();
 		bttCreate.addClickHandler(handler);
 		myReset resetHandler = new myReset();
 		bttReset.addClickHandler(resetHandler);
 		myBack backHandler = new myBack();
 		bttBack.addClickHandler(backHandler);
 		myProtocol actionProtocol = new myProtocol();
 		listProtocol.addClickHandler(actionProtocol);
 
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
 		tableForm.setWidget(5, 1, panelTextEmail);
 		tableForm.setWidget(5, 2, panelValidateEmail);
 		tableForm.setWidget(6, 0, labelmailgroup);
 		tableForm.setWidget(6, 1, listGroup);
 		tableForm.setWidget(7, 0, labelremoteurl);
 		tableForm.setWidget(7, 1, txtRemote);
 		tableForm.setWidget(7, 2, panelValidateRemoteURL);
 		tableForm.getFlexCellFormatter().setColSpan(8, 0, 2);
 		tableForm.setWidget(8, 0, advancedDisclosure);
 		tableForm.getFlexCellFormatter().setColSpan(9, 0, 2);
 		tableForm.setWidget(9, 0, panelAdding);
 		tableForm.getFlexCellFormatter().setColSpan(10, 0, 3);
 		tableForm.setWidget(10, 0, panelButton);
 	}
 
 	protected void initFlextTable() {
 		monitorGwtSv
 				.getSystemMonitorContainer(new AsyncCallback<MonitorContainer>() {
 					@Override
 					public void onSuccess(MonitorContainer result) {
 						container = result;
 						GroupMonitor[] groups = container.getGroups();
 						if (groups != null) {
 							for (GroupMonitor g : groups) {
 								listGroup.addItem(g.getName());
 							}
 							listGroup.setSelectedIndex(0);
 						}
 						addWidget(HTMLControl.ID_BODY_CONTENT, tableForm);
 						setVisibleLoadingImage(false);
 						setOnload(false);
 						setVisibleWidget(HTMLControl.ID_BODY_CONTENT, true);
 
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						caught.printStackTrace();
 						showMessage("Oops! Error.",
 								HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 								"Goto System Management. ",
 								HTMLControl.RED_MESSAGE, true);
 					}
 				});
 
 	}
 
 	/**
 	 * @author NDC
 	 * 
 	 */
 	class MyHandler implements ClickHandler {
 
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
 				panelValidateName.setVisible(false);
 				panelValidateRemoteURL.setVisible(false);
 				panelValidateIP.setVisible(false);
 				panelValidateRemoteURL.setVisible(false);
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
 					panelValidateRemoteURL.setVisible(false);
 					panelValidateURL.setVisible(false);
 					panelValidateEmail.setVisible(false);
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
 						panelValidateURL.setVisible(false);
 						panelValidateEmail.setVisible(false);
 						return;
 					}
 				}
 				panelValidateRemoteURL.setVisible(false);
 				panelValidateIP.setVisible(false);
 				panelValidateName.setVisible(false);
 				panelValidateURL.setVisible(false);
 				panelValidateEmail.setVisible(false);
 				SystemMonitor system = new SystemMonitor();
 				system.setName(txtName.getText().toString());
 				system.setUrl(txtURL.getText().toString());
 				system.setActive(isActive(listActive.getValue(listActive
 						.getSelectedIndex())));
 				system.setProtocol(listProtocol.getValue(listProtocol
 						.getSelectedIndex()));
 				system.setGroupEmail(listGroup.getItemText(listGroup
 						.getSelectedIndex()));
 				system.setIp(txtIP.getText());
 				system.setRemoteUrl(txtRemote.getText());
 				panelAdding.setVisible(true);
 
 				NotifyMonitor nm = new NotifyMonitor();
 				nm.setNotifyCpu(cbNotifyCpu.getValue());
 				nm.setNotifyMemory(cbNotifyMemory.getValue());
 				nm.setNotifyServices(cbNotifyServices.getValue());
 				nm.setNotifyServicesConnection(cbNotifyServicesConnection
 						.getValue());
 				nm.setJVM(cbNotifyJVM.getValue());
 				system.setNotify(nm);
 
 				sendData(system);
 
 			} else if (listProtocol
 					.getItemText(listProtocol.getSelectedIndex()).equals(
 							MonitorConstant.SMTP_PROTOCOL)) {
 				String validateName = validateName(txtName.getText());
 				String validateURL = validateURL(txtURL.getText());
 				String validateIp = validateIP(txtIP.getText());
 				String validateEmail = validateEmail(txtEmail.getText());
 				panelValidateEmail.setVisible(false);
 				panelValidateName.setVisible(false);
 				panelValidateRemoteURL.setVisible(false);
 				panelValidateIP.setVisible(false);
 				panelValidateRemoteURL.setVisible(false);
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
 					panelValidateRemoteURL.setVisible(false);
 					panelValidateURL.setVisible(false);
 					panelValidateEmail.setVisible(false);
 					return;
 				} else if (validateEmail != "") {
 					panelValidateEmail.clear();
 					panelValidateEmail.add(new HTML(
 							"<div class=\"error-left\"></div><div class=\"error-inner\">"
 									+ validateEmail + "</div>"));
 					panelValidateEmail.setVisible(true);
 					panelValidateIP.setVisible(false);
 					panelValidateName.setVisible(false);
 					panelValidateRemoteURL.setVisible(false);
 					panelValidateURL.setVisible(false);
 					return;
 				}
 				panelValidateRemoteURL.setVisible(false);
 				panelValidateIP.setVisible(false);
 				panelValidateName.setVisible(false);
 				panelValidateURL.setVisible(false);
 				panelValidateEmail.setVisible(false);
 
 				SystemMonitor system = new SystemMonitor();
 				system.setName(txtName.getText().toString());
 				system.setUrl(txtURL.getText().toString());
 				system.setActive(isActive(listActive.getValue(listActive
 						.getSelectedIndex())));
 				system.setProtocol(listProtocol.getValue(listProtocol
 						.getSelectedIndex()));
 				system.setGroupEmail(listGroup.getItemText(listGroup
 						.getSelectedIndex()));
 				system.setIp(txtIP.getText());
 				system.setEmailRevice(txtEmail.getText());
 				NotifyMonitor nm = new NotifyMonitor();
 				nm.setNotifyCpu(cbNotifyCpu.getValue());
 				nm.setNotifyMemory(cbNotifyMemory.getValue());
 				nm.setNotifyServices(cbNotifyServices.getValue());
 				nm.setNotifyServicesConnection(cbNotifyServicesConnection
 						.getValue());
 				nm.setJVM(cbNotifyJVM.getValue());
 				system.setNotify(nm);
 				panelAdding.setVisible(true);
 				sendData(system);
 			}
 		}
 
 		/**
 		 * @param system
 		 * @param url
 		 */
 		private void sendData(SystemMonitor system) {
 			panelAdding.setVisible(false);
 			monitorGwtSv.addSystem(system, new AsyncCallback<Boolean>() {
 
 				@Override
 				public void onSuccess(Boolean result) {
 					if (result) {
 						showMessage("System added sucessfully. ",
 								HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 								"View system list. ", HTMLControl.RED_MESSAGE,
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
 					caught.printStackTrace();
 					showMessage("Server error! ",
 							HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME,
 							"Goto System Management. ",
 							HTMLControl.RED_MESSAGE, true);
 				}
 			});
 		}
 	}
 
 	/**
 	 * @author NDC
 	 * 
 	 */
 	class myReset implements ClickHandler {
 
 		@Override
 		public void onClick(ClickEvent event) {
 			txtName.setText("");
 			txtIP.setText("");
 			txtRemote.setText("");
 			txtURL.setText("");
 			listActive.setSelectedIndex(0);
 			listGroup.setSelectedIndex(0);
 			listProtocol.setSelectedIndex(0);
 			txtRemote.setEnabled(true);
 			panelLabelEmail.setVisible(false);
 			panelTextEmail.setVisible(false);
 			txtEmail.setText("");
 			panelValidateEmail.setVisible(false);
 			panelValidateRemoteURL.setVisible(false);
 			panelValidateIP.setVisible(false);
 			panelValidateName.setVisible(false);
 			panelValidateURL.setVisible(false);
 
 		}
 	}
 
 	/**
 	 * @author NDC
 	 * 
 	 */
 	class myBack implements ClickHandler {
 		@Override
 		public void onClick(ClickEvent event) {
 			Window.Location.replace(HTMLControl.trimHashPart(Window.Location
 					.getHref()) + HTMLControl.HTML_SYSTEM_MANAGEMENT_NAME);
 		}
 	}
 
 	/**
 	 * @author NDC
 	 * 
 	 */
 	class myProtocol implements ClickHandler {
 
 		@Override
 		public void onClick(ClickEvent event) {
 			if (listProtocol.getSelectedIndex() == 0) {
 				txtRemote.setEnabled(true);
 				panelLabelEmail.setVisible(false);
 				panelTextEmail.setVisible(false);
 				txtEmail.setText("");
 				panelValidateEmail.setVisible(false);
 			} else if (listProtocol.getSelectedIndex() == 1) {
 				txtRemote.setText("");
 				txtRemote.setEnabled(false);
 				panelLabelEmail.setVisible(true);
 				panelTextEmail.setVisible(true);
 				panelValidateRemoteURL.setVisible(false);
 			}
 
 		}
 
 	}
 }

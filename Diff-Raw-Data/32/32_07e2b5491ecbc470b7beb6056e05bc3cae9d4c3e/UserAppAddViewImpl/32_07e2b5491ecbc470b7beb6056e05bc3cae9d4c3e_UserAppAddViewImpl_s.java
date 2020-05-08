 package com.eucalyptus.webui.client.view;
 
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import com.eucalyptus.webui.client.ClientFactory;
 import com.eucalyptus.webui.shared.config.LanguageSelection;
 import com.eucalyptus.webui.shared.resource.VMImageType;
 import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
 import com.eucalyptus.webui.shared.user.UserApp;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.datepicker.client.DateBox;
 import com.google.gwt.user.client.ui.ListBox;
 
 public class UserAppAddViewImpl extends DialogBox implements UserAppAddView, ChangeHandler {
 
 	interface UserAppAddViewImplUiBinder extends
 			UiBinder<Widget, UserAppAddViewImpl> {
 	}
 
 	public UserAppAddViewImpl() {
 		setWidget(uiBinder.createAndBindUi(this));
 		
 		this.startingTime.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
 		this.endingTime.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
 		
 		setGlassEnabled(true);
 		
 		this.TemplateList.addChangeHandler(this);
 	}
 
 	@UiHandler("buttonOk")
 	void onButtonOkClick(ClickEvent event) {
 		
 		if (this.startingTime.getValue() == null || this.endingTime.getValue() == null)
 			return;
 		
 		if (this.startingTime.getValue().getTime() - this.endingTime.getValue().getTime() > 0)
 			return;
 		
 		int vmImageTypeId = 0;
 		int vitListSelIndex = this.VMImageTypeList.getSelectedIndex();
 		if (vitListSelIndex >= 0)
 			vmImageTypeId = Integer.parseInt(this.VMImageTypeList.getValue(vitListSelIndex));
 		
 		int templateId = 0;
 		int temListSelIndex = this.TemplateList.getSelectedIndex();
 		if (temListSelIndex >= 0)
 			templateId = Integer.parseInt(this.TemplateList.getValue(temListSelIndex));
 		
 		String keyPair = null;
 		int keyPairListSelIndex = this.keyPairList.getSelectedIndex();
 		if (keyPairListSelIndex >= 0)
 			keyPair = this.keyPairList.getValue(keyPairListSelIndex);
 		
 		String securityGroup = null;		
 		int securityGroupListSelIndex = this.securityGroupList.getSelectedIndex();
 		if (securityGroupListSelIndex >= 0)
 			securityGroup = this.securityGroupList.getValue(securityGroupListSelIndex);
 		
 		if (vmImageTypeId != 0 && keyPair != null && securityGroup != null) {
 			
 			UserApp userApp = new UserApp();
 			userApp.setVmImageTypeId(vmImageTypeId);
 			userApp.setSrvStartingTime(this.startingTime.getValue());
 			userApp.setSrvEndingingTime(this.endingTime.getValue());
 			userApp.setKeyPair(keyPair);
 			userApp.setSecurityGroup(securityGroup);
 			
 			this.presenter.doCreateUserApp(userApp);
 			
 			clearSelection();
 			
 			this.hide();
 		}
 		else {
 			int lan = LanguageSelection.instance().getCurLanguage().ordinal();
 			Window.alert(USER_APP_PARA_ERROR[lan]);
 		}
 	}
 	
 	@UiHandler("buttonCancle")
 	void onButtonCancleClick(ClickEvent event) {
 		this.hide();
 		clearSelection();
 	}
 	
 	private void clearSelection() {
 		// TODO Auto-generated method stub
 		this.VMImageTypeList.clear();
 		this.keyPairList.clear();
 		this.securityGroupList.clear();
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		// TODO Auto-generated method stub
 		this.presenter = presenter;
 	}
 	
 	@Override
 	public void setClientFactory(ClientFactory clientFactory) {
 		// TODO Auto-generated method stub
 		this.clientFactory = clientFactory;
 	}
 	
 	@Override
 	public void setDeviceTemplates(Map<String, Integer> templates) {
 		// TODO Auto-generated method stub
 		if (templates == null)
 			return;
 		
 		for (Map.Entry<String, Integer> entry : templates.entrySet()) {
 			String name = entry.getKey().toString();
 			Integer id = entry.getValue();
 			
 			this.TemplateList.addItem(name, id.toString());
 		}
 	}
 	
 	@Override
 	public void setVMImageTypeList(List<VMImageType> vmTypeList) {
 		// TODO Auto-generated method stub
 		if (vmTypeList == null)
 			return;
 		
 		for (VMImageType vm : vmTypeList) {
 			String item = vm.getOs() + " (" + vm.getVer() + ")";
 			this.VMImageTypeList.addItem(item, Integer.valueOf(vm.getId()).toString());
 		}
 	}
 	
 	@Override
 	public void setKeyPairList(List<String> keyPairList) {
 		// TODO Auto-generated method stub
 		if (keyPairList == null)
 			return;
 		
 		for (String keyPair : keyPairList) {
 			this.keyPairList.addItem(keyPair);
 		}
 	}
 
 	@Override
 	public void setSecurityGroupList(List<String> securityGroupList) {
 		// TODO Auto-generated method stub
 		if (securityGroupList == null)
 			return;
 		
 		for (String securityGroup : securityGroupList) {
 			this.securityGroupList.addItem(securityGroup);
 		}
 	}
 	
 	@Override
 	public void display() {
 		// TODO Auto-generated method stub
 		clearInputField();
 			
 		this.center();
 		this.show();
 	}
 	
 	@Override
 	public void onChange(ChangeEvent event) {
 		// TODO Auto-generated method stub
 		 int selectedIndex = this.TemplateList.getSelectedIndex();
 		 
 		 if (selectedIndex < 0)
 			 return;
 		 
 		 int id = Integer.parseInt(this.TemplateList.getValue(selectedIndex));
 		 
 		 clientFactory.getBackendService().lookupDeviceTemplateInfoByID(clientFactory.getLocalSession().getSession(), id, new AsyncCallback<TemplateInfo>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onSuccess(TemplateInfo result) {
 				// TODO Auto-generated method stub
 				template_ncpus.setValue(Integer.toString(result.template_ncpus));
 				template_mem.setValue(Long.toString(result.template_mem));
 				template_disk.setValue(Long.toString(result.template_disk));
 				template_bw.setValue(Integer.toString(result.template_bw));
 			}});
 	}
 	
 	private void clearInputField() {
 		
 	}
 	
 	private static UserAppAddViewImplUiBinder uiBinder = GWT
 			.create(UserAppAddViewImplUiBinder.class);
 	
 	private static final Logger LOG = Logger.getLogger( UserViewImpl.class.getName( ) );
 	@UiField DateBox startingTime;
 	@UiField DateBox endingTime;
 	@UiField Button buttonOk;
 	@UiField Button buttonCancle;
 	@UiField ListBox VMImageTypeList;
 	@UiField ListBox TemplateList;
 	@UiField ListBox keyPairList;
 	@UiField ListBox securityGroupList;
 	
 	@UiField TextBox template_ncpus;
 	@UiField TextBox template_mem;
 	@UiField TextBox template_disk;
 	@UiField TextBox template_bw;
 	
 	private Presenter presenter;
 	private ClientFactory clientFactory;
 	
 	private static String[] USER_APP_PARA_ERROR = {"Creating user application para error", "创建用户申请参数错误"};
 
 	
 }

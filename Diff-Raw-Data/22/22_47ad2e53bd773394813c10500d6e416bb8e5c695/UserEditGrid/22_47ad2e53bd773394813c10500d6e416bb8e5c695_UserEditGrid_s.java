 package com.scholastic.sbam.client.uiobjects.uiadmin;
 
 import java.util.List;
 
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.UpdateUserService;
 import com.scholastic.sbam.client.services.UpdateUserServiceAsync;
 import com.scholastic.sbam.client.services.UserListService;
 import com.scholastic.sbam.client.services.UserListServiceAsync;
 import com.scholastic.sbam.client.services.UserNameValidationService;
 import com.scholastic.sbam.client.services.UserNameValidationServiceAsync;
 import com.scholastic.sbam.client.uiobjects.foundation.BetterFilterEditGrid;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.shared.objects.UpdateResponse;
 import com.scholastic.sbam.shared.objects.UserInstance;
 import com.scholastic.sbam.shared.security.SecurityManager;
 import com.scholastic.sbam.shared.validation.AppUserNameValidator;
 import com.scholastic.sbam.shared.validation.EmailValidator;
 import com.scholastic.sbam.shared.validation.NameValidator;
 
 public class UserEditGrid extends BetterFilterEditGrid<UserInstance> {
 	
 	private final UserListServiceAsync userListService = GWT.create(UserListService.class);
 	private final UpdateUserServiceAsync updateUserService = GWT.create(UpdateUserService.class);
 	private final UserNameValidationServiceAsync userNameValidationService = GWT.create(UserNameValidationService.class);
 
 	@Override
 	public void onRender(Element parent, int index) {
 	//	setForceHeight(500);
 		setAdditionalWidthPadding(14);
 	//	setForceWidth(600);
 	//	setAutoExpandColumn("spacer");
 		setLayout(new CenterLayout());
 	//	setLayout(new FillLayout(Orientation.VERTICAL));
 		setPanelHeading("Users");
 		IconSupplier.setIcon(this.panel, IconSupplier.getUsersIconName());
 		super.onRender(parent, index);
 	}
 	
 	@Override
 	protected void asyncLoad(Object loadConfig,
 			AsyncCallback<List<UserInstance>> callback) {
 		userListService.getUsers(null, null, null, null, callback);
 	}
 
 	@Override
 	protected void addColumns(List<ColumnConfig> columns) {
 		columns.add(getEditColumn(			"userName", 		"User", 			80,		"Enter a unique name for the user.",	new AppUserNameValidator(), userNameValidationService));
 	//	columns.add(getEditPasswordColumn(	"password", 		"Password", 		60, 	null));
 		columns.add(getEditColumn(			"firstName", 		"First Name", 		100,	null,									new NameValidator(),		null));
 		columns.add(getEditColumn(			"lastName", 		"Last Name", 		100,	null,									new NameValidator(),		null));
 		columns.add(getEditColumn(			"email", 			"Email", 			200,	null,									new EmailValidator(),		null));
 		columns.add(getComboColumn(			"roleGroupTitle",	"Role",				130,	"Select the role that will define the user's capabilities.",		getRoleGroupTitles()));	
 		columns.add(getEditCheckColumn(		"resetPassword",	"Reset Password", 	100,	"Check to reset user's password"));
 		columns.add(getDateColumn(			"createdDatetime",	"Created", 			75,		"The date that this row was created."));
 		
 	}
 	
 	protected String [] getRoleGroupTitles() {
 		String [] titles = new String [SecurityManager.ROLE_GROUPS.length];
 		for (int i = 0; i < SecurityManager.ROLE_GROUPS.length; i++) {
 			titles [i] = SecurityManager.ROLE_GROUPS [i].getGroupTitle();
 		}
 		return titles;
 	}
 
 	@Override
 	protected UserInstance getNewInstance() {
 		UserInstance user = new UserInstance();
 		user.setUserName("");
 		user.setFirstName("");
 		user.setLastName("");
 		user.setEmail("");
 		user.setRoleGroupTitle("None");
 		
 		return user;
 	}
 
 	@Override
 	protected void asyncUpdate(BeanModel beanModel) {
 		final BeanModel targetBeanModel = beanModel;
 		//	System.out.println("Before update: " + targetBeanModel.getProperties());
 			updateUserService.updateUser((UserInstance) beanModel.getBean(),
 					new AsyncCallback<UpdateResponse<UserInstance>>() {
 						public void onFailure(Throwable caught) {
 							// Show the RPC error message to the user
 							if (caught instanceof IllegalArgumentException)
 								MessageBox.alert("Alert", caught.getMessage(), null);
 							else {
 								MessageBox.alert("Alert", "User update failed unexpectedly.", null);
 								System.out.println(caught.getClass().getName());
 								System.out.println(caught.getMessage());
 							}
 						}
 
 						public void onSuccess(UpdateResponse<UserInstance> updateResponse) {
 							UserInstance updatedUser = (UserInstance) updateResponse.getInstance();
 							UserInstance  storeInstance = targetBeanModel.getBean();
 							storeInstance.setNewRecord(false);
 							if (storeInstance.getId() == null)
 								storeInstance.setId(updatedUser.getId());
							if (storeInstance.getCreatedDatetime() == null)
								storeInstance.setCreatedDatetime(updatedUser.getCreatedDatetime());
 							storeInstance.setNewRecord(false);
 							storeInstance.setResetPassword(false);
 							if (updateResponse.getMessage() != null && updateResponse.getMessage().length() > 0)
 								MessageBox.info("Please Note...", updateResponse.getMessage(), null);
 					}
 				});
 			UserInstance  targetInstance = targetBeanModel.getBean();
 			targetInstance.setResetPassword(false);
 	}
 	
 }

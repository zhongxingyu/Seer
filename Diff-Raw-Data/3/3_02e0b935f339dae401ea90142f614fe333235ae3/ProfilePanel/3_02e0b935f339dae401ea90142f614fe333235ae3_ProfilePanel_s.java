 /**
  * 
  */
 package org.cotrix.web.permissionmanager.client.profile;
 
 import org.cotrix.web.permissionmanager.client.PermissionServiceAsync;
 import org.cotrix.web.permissionmanager.client.profile.PasswordUpdateDialog.PassworUpdatedEvent;
 import org.cotrix.web.permissionmanager.client.profile.PasswordUpdateDialog.PasswordUpdatedHandler;
 import org.cotrix.web.permissionmanager.shared.UIUserDetails;
 import org.cotrix.web.share.client.error.ManagedFailureCallback;
 import org.cotrix.web.share.client.event.CotrixBus;
 import org.cotrix.web.share.client.event.UserLoggedEvent;
 import org.cotrix.web.share.client.util.AccountValidator;
 import org.cotrix.web.share.client.util.StatusUpdates;
 
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class ProfilePanel extends ResizeComposite {
 
 	interface ProfilePanelUiBinder extends UiBinder<Widget, ProfilePanel> { }
 
 	protected interface Style extends CssResource {
 		String invalidValue();
 	}
 
 	@UiField Label username;
 	@UiField TextBox fullname;
 	@UiField TextBox email;
 	
 	@Inject PasswordUpdateDialog passwordUpdateDialog;
 
 	@UiField Style style;
 
 	@Inject
 	protected PermissionServiceAsync service;
 	protected UIUserDetails userDetails = new UIUserDetails();
 
 	@Inject
 	protected void init(ProfilePanelUiBinder uiBinder) {
 		initWidget(uiBinder.createAndBindUi(this));
 		passwordUpdateDialog.addPasswordUpdateHandler(new PasswordUpdatedHandler() {
 			
 			@Override
 			public void onAddUser(PassworUpdatedEvent event) {
 				StatusUpdates.statusSaving();
 				service.saveUserPassword(userDetails.getId(), event.getPassword(), new ManagedFailureCallback<Void>() {
 
 					@Override
 					public void onSuccess(Void result) {
 						StatusUpdates.statusSaved();
 					}
 				});
 			}
 		});
 	}
 
 	@Inject
 	protected void bind(@CotrixBus EventBus cotrixBus) {
 		cotrixBus.addHandler(UserLoggedEvent.TYPE, new UserLoggedEvent.UserLoggedHandler() {
 
 			@Override
 			public void onUserLogged(UserLoggedEvent event) {
 				updateUserProfile();
 			}
 		});
 	}
 	
 	@UiHandler("password")
 	protected void onPasswordChange(ClickEvent event) {
 		passwordUpdateDialog.center();
 	}
 	
 	@UiHandler({"fullname","email"})
 	protected void onKeyDown(KeyDownEvent event)
 	{
 		 if (event.getSource() instanceof UIObject) {
 			 UIObject uiObject = (UIObject)event.getSource();
 			 uiObject.setStyleName(style.invalidValue(), false);
 		 }
 	}
 
 	@UiHandler({"fullname", "email"})
 	protected void onBlur(BlurEvent event) {
 		boolean valid = validate();
 		if (valid) {
 			StatusUpdates.statusSaving();
 			service.saveUserDetails(getUserDetails(), new ManagedFailureCallback<Void>() {
 
 				@Override
 				public void onSuccess(Void result) {
 					StatusUpdates.statusSaved();
 				}
 			});
 		}
 	}
 
 	protected boolean validate() {
 		boolean valid = true;
 		
 		if (!AccountValidator.validateFullName(fullname.getText())) {
 			fullname.setStyleName(style.invalidValue(), true);
 			valid = false;
 		}
 
 		if (!AccountValidator.validateEMail(email.getText())) {
 			email.setStyleName(style.invalidValue(), true);
 			valid = false;
 		}
 		return valid;
 	}
 
 	protected void updateUserProfile() {
 		service.getUserDetails(new ManagedFailureCallback<UIUserDetails>() {
 
 			@Override
 			public void onSuccess(UIUserDetails result) {
 				setUserDetails(result);				
 			}
 		});
 	}
 
 	protected void setUserDetails(UIUserDetails userDetails) {
 		this.userDetails = userDetails;
 		username.setText(userDetails.getUsername());
 		fullname.setText(userDetails.getFullName());
 		email.setText(userDetails.getEmail());
 	}
 
 	protected UIUserDetails getUserDetails() {
 		userDetails.setUsername(username.getText());
 		userDetails.setFullName(fullname.getText());
 		userDetails.setEmail(email.getText());
 		return userDetails;
 	}
 }

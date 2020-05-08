 /**
  * 
  */
 package org.mklab.taskit.client.ui;
 
 import org.mklab.taskit.client.ClientFactory;
 import org.mklab.taskit.client.Messages;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 
 /**
  * {@link LoginView}の実装クラスです。
  * 
  * @author Yuhi Ishikura
  * @version $Revision$, Jan 21, 2011
  */
 public class LoginViewImpl extends Composite implements LoginView {
 
   private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);
 
   interface LoginViewUiBinder extends UiBinder<Widget, LoginViewImpl> {
     // no members
   }
 
   @UiField
   Button loginButton;
   @UiField
   TextBox idText;
   @UiField
   Label statusLabel;
   @UiField
   PasswordTextBox passwordText;
   @UiField
   CheckBox autoLoginCheck;
 
   @UiField
   InlineLabel idLabel;
   @UiField
   InlineLabel passwordLabel;
 
   /**
    * {@link LoginViewImpl}オブジェクトを構築します。
    * 
    * @param clientFactory クライアントファクトリ
    */
   public LoginViewImpl(ClientFactory clientFactory) {
     initWidget(uiBinder.createAndBindUi(this));
 
     final Messages messages = clientFactory.getMessages();
     localizeStrings(messages);
 
     bindTextBoxEnterKeyToSubmitButton();
   }
 
   private void localizeStrings(final Messages messages) {
    this.idLabel.setText(messages.userIdLabel());
     this.passwordLabel.setText(messages.passwordLabel());
     this.loginButton.setText(messages.loginButton());
     this.autoLoginCheck.setText(messages.autoLoginCheck());
   }
 
   /**
    * テキストフィールド内でEnterキーを入力したときの挙動と、サブミットボタンを押した時の挙動をバインドします。
    */
   private void bindTextBoxEnterKeyToSubmitButton() {
     final KeyDownHandler enterHandler = new KeyDownHandler() {
 
       @SuppressWarnings("synthetic-access")
       @Override
       public void onKeyDown(KeyDownEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER == false) return;
 
         enterKeyPressed();
       }
     };
 
     this.idText.addKeyDownHandler(enterHandler);
     this.passwordText.addKeyDownHandler(enterHandler);
   }
 
   private void enterKeyPressed() {
     this.loginButton.click();
   }
 
   /**
    * @see org.mklab.taskit.client.ui.LoginView#getId()
    */
   @Override
   public String getId() {
     return this.idText.getText();
   }
 
   /**
    * @see org.mklab.taskit.client.ui.LoginView#getPassword()
    */
   @Override
   public String getPassword() {
     return this.passwordText.getText();
   }
 
   /**
    * @see org.mklab.taskit.client.ui.LoginView#isAutoLoginEnabled()
    */
   @Override
   public boolean isAutoLoginEnabled() {
     final Boolean value = this.autoLoginCheck.getValue();
     if (value == null) return false;
     return value.booleanValue();
   }
 
   /**
    * @see org.mklab.taskit.client.ui.LoginView#getSubmitButton()
    */
   @Override
   public HasClickHandlers getSubmitButton() {
     return this.loginButton;
   }
 
   /**
    * @see org.mklab.taskit.client.ui.LoginView#setStatusText(java.lang.String)
    */
   @Override
   public void setStatusText(String status) {
     this.statusLabel.setText(status);
   }
 
   /**
    * @see org.mklab.taskit.client.ui.LoginView#requestFocus()
    */
   @Override
   public void requestFocus() {
     this.idText.setFocus(true);
   }
 
 }

 /**
  * 
  */
 package org.mklab.taskit.client.ui.admin;
 
 import org.mklab.taskit.client.ClientFactory;
 import org.mklab.taskit.client.Messages;
 import org.mklab.taskit.client.ui.AbstractTaskitView;
 import org.mklab.taskit.shared.UserProxy;
 import org.mklab.taskit.shared.UserType;
 import org.mklab.taskit.shared.Validator;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.shared.Renderer;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ValueListBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent.Handler;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 
 /**
  * @author Yuhi Ishikura
  */
 public class UserEditorViewImpl extends AbstractTaskitView implements UserEditView {
 
   static interface Binder extends UiBinder<Widget, UserEditorViewImpl> {
     // empty. for UI bindings.
   }
 
   @UiField(provided = true)
   CellList<UserProxy> userList;
   @UiField
   Label userIdLabel;
   @UiField
   Label userTypeLabel;
   @UiField(provided = true)
   ValueListBox<UserType> userType;
   @UiField
   TextBox userId;
   @UiField
   Label passwordLabel;
   @UiField
   PasswordTextBox password;
   @UiField
   Label nameLabel;
   @UiField
   TextBox name;
   @UiField
   CaptionPanel userInfoCaption;
   @UiField
   Button applyButton;
   @UiField
   CheckBox newCheck;
 
   private UserProxy edittingUser;
   private Presenter presenter;
 
   /**
    * {@link UserEditorViewImpl}オブジェクトを構築します。
    * 
    * @param clientFactory クライアントファクトリ
    */
   public UserEditorViewImpl(ClientFactory clientFactory) {
     super(clientFactory);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected Widget initContent() {
     this.userList = new CellList<UserProxy>(new AbstractCell<UserProxy>() {
 
       @Override
       public void render(@SuppressWarnings("unused") com.google.gwt.cell.client.Cell.Context context, UserProxy value, SafeHtmlBuilder sb) {
         sb.appendEscaped(value.getAccount().getId());
       }
     });
 
     final SingleSelectionModel<UserProxy> selectionModel = new SingleSelectionModel<UserProxy>();
     selectionModel.addSelectionChangeHandler(new Handler() {
 
       @Override
       public void onSelectionChange(@SuppressWarnings("unused") SelectionChangeEvent event) {
         final UserProxy selectedUser = selectionModel.getSelectedObject();
         setEdittingUser(selectedUser);
       }
     });
     this.userList.setSelectionModel(selectionModel);
     this.userType = new ValueListBox<UserType>(new Renderer<UserType>() {
 
       @Override
       public String render(UserType object) {
         if (object == null) return null;
         return object.name();
       }
 
       @Override
       public void render(UserType object, Appendable appendable) throws IOException {
         appendable.append(render(object));
       }
     });
     this.userType.setAcceptableValues(Arrays.asList(UserType.values()));
 
     final Binder binder = GWT.create(Binder.class);
     final Widget content = binder.createAndBindUi(this);
 
     final Messages messages = getClientFactory().getMessages();
     this.userIdLabel.setText(messages.userIdLabel());
     this.passwordLabel.setText(messages.passwordLabel());
     this.nameLabel.setText(messages.userNameLabel());
     this.userInfoCaption.setCaptionText(messages.userInfoLabel());
     this.applyButton.setText(messages.applyLabel());
     this.userTypeLabel.setText(messages.userTypeLabel());
     this.newCheck.setText(messages.newLabel());
 
     updateEditMode();
     return content;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setUserList(List<UserProxy> users) {
     this.userList.setRowData(users);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setEdittingUser(UserProxy user) {
     this.edittingUser = user;
     if (user == null) {
       this.userId.setText(null);
       this.userType.setValue(null);
       this.name.setText(null);
       return;
     }
 
     this.userId.setText(user.getAccount().getId());
     this.userType.setValue(user.getType());
     this.password.setText(null);
     this.name.setText(user.getName());
   }
 
   @UiHandler("newCheck")
   void newCheckClicked(@SuppressWarnings("unused") ClickEvent evt) {
     updateEditMode();
   }
 
   private void updateEditMode() {
     this.userId.setText(""); //$NON-NLS-1$
     if (this.newCheck.getValue().booleanValue() == true) {
       clearUserSelection();
       this.userId.setEnabled(true);
     } else {
       this.userId.setEnabled(false);
     }
   }
 
   private boolean checkValid(boolean isNew) {
     final Messages messages = getClientFactory().getMessages();
     if (isNew) {
       try {
         Validator.validateUserId(this.userId.getText());
       } catch (IllegalArgumentException e) {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append(messages.isInvalidMessage(messages.userIdLabel()));
         stringBuilder.append(":"); //$NON-NLS-1$
         stringBuilder.append(e.getMessage());
         showErrorDialog(stringBuilder.toString());
         return false;
       }
     }
     if (isNew) {
       if (this.userType.getValue() == null) {
         showErrorDialog(messages.userTypeNotSelectedMessage());
         return false;
       }
     }
     try {
       Validator.validateUserName(this.name.getText());
     } catch (Throwable e) {
       showErrorDialog(messages.isInvalidMessage(messages.userNameLabel()) + ":" + e.getMessage()); //$NON-NLS-1$
       return false;
     }
     if (isNew || this.password.getText().length() > 0) {
       try {
         Validator.validatePassword(this.password.getText());
       } catch (Throwable e) {
         showErrorDialog(messages.isInvalidMessage(messages.passwordLabel()) + ":" + e.getMessage()); //$NON-NLS-1$
         return false;
       }
     }
 
     return true;
   }
 
   private void clearUserSelection() {
     @SuppressWarnings("unchecked")
     SingleSelectionModel<UserProxy> selectionModel = (SingleSelectionModel<UserProxy>)this.userList.getSelectionModel();
     for (int i = 0; i < this.userList.getVisibleItemCount(); i++) {
       UserProxy user = this.userList.getVisibleItem(i);
       selectionModel.setSelected(user, false);
     }
   }
 
   @UiHandler("applyButton")
   void applyButtonPressed(@SuppressWarnings("unused") ClickEvent evt) {
     final boolean isNew = this.newCheck.getValue().booleanValue();
     if (isNew == false && this.edittingUser == null) return;
     if (checkValid(isNew) == false) return;
 
     if (isNew) {
       this.presenter.createUser(this.userId.getText(), this.password.getText(), this.userType.getValue());
      this.presenter.changeUserName(this.edittingUser.getAccount().getId(), this.name.getText());
     } else {
       String p = this.password.getText();
       if (p != null && p.length() > 0) {
         this.presenter.changePassword(this.edittingUser.getAccount().getId(), p);
       }
       String n = this.name.getText();
       if (n != null && n.equals(this.edittingUser.getName()) == false) {
         this.presenter.changeUserName(this.edittingUser.getAccount().getId(), n);
       }
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setPresenter(Presenter presenter) {
     this.presenter = presenter;
   }
 }

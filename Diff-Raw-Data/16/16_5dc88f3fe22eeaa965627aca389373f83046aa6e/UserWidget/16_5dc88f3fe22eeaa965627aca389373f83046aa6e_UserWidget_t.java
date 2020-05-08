 package com.szas.server.gwt.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.szas.data.UserTuple;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class UserWidget extends Composite {
 
 	private static UserWidgetUiBinder uiBinder = GWT
 	.create(UserWidgetUiBinder.class);
 	@UiField Button saveButton;
 	@UiField TextBox nameTextBox;
	@UiField Button deleteButton;
 	private UserTuple userTuple;
	private boolean update;
 
 	interface UserWidgetUiBinder extends UiBinder<Widget, UserWidget> {
 	}
 
 	public UserWidget() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	public static UserTuple findTuple(long id) {
 		ArrayList<UserTuple> users = StaticGWTSyncer.getUsersdao().getAll();
 		for (UserTuple user : users) {
 			if (user.getId() == id) {
 				return user;
 			}
 		}
 		return null;
 	}
 	public UserWidget(UserTuple userTuple) {
 		this.userTuple = userTuple;
 		initWidget(uiBinder.createAndBindUi(this));
 		nameTextBox.setText(userTuple.getName());
		update = findTuple(userTuple.getId()) != null;
		deleteButton.setVisible(update);
 	}
 
 	@UiHandler("saveButton")
 	void onSaveButtonClick(ClickEvent event) {
 		userTuple.setName(nameTextBox.getText());
 		if (update)
 			StaticGWTSyncer.getUsersdao().update(userTuple);
 		else
 			StaticGWTSyncer.getUsersdao().insert(userTuple);
 		History.back();
 	}
	@UiHandler("deleteButton")
	void onDeleteButtonClick(ClickEvent event) {
		if (!update)
			return;
		StaticGWTSyncer.getUsersdao().delete(userTuple);
		History.back();
	}
 }

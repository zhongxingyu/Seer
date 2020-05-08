 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
public class RegisterPanel extends PopupPanel implements ChangeListener {
     TextBox username = new TextBox();
     TextBox email = new TextBox();
     PasswordTextBox pw1 = new PasswordTextBox();
     PasswordTextBox pw2 = new PasswordTextBox();
 
     Label errmsg1 = new Label();
     Label errmsg2 = new Label();
     Label errmsg3 = new Label();
 
     public RegisterPanel() {
         super(false);
 
         username.setVisibleLength(40);
         email.setVisibleLength(40);
         pw1.setVisibleLength(40);
         pw2.setVisibleLength(40);
 
        username.addChangeListener(this);
        email.addChangeListener(this);
        pw1.addChangeListener(this);
        pw2.addChangeListener(this);

         errmsg1.addStyleName(errmsg1.getStylePrimaryName() + "-error");
         errmsg2.addStyleName(errmsg1.getStylePrimaryName() + "-error");
         errmsg3.addStyleName(errmsg1.getStylePrimaryName() + "-error");
 
         VerticalPanel vpanel = new VerticalPanel();
 
         Grid grid = new Grid(7, 2);
         grid.setWidth("100%");
 
         CellFormatter cf = grid.getCellFormatter();
         // right align field lables
         cf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
         cf.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
         cf.setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
         cf.setHorizontalAlignment(6, 0, HasHorizontalAlignment.ALIGN_RIGHT);
 
         // right align error messages
         cf.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
         cf.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);
         cf.setHorizontalAlignment(5, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 
         grid.setText(0, 0, "Username:");
         grid.setText(2, 0, "Email:");
         grid.setText(4, 0, "Password:");
         grid.setText(6, 0, "Verify Password:");
 
         grid.setWidget(1, 1, errmsg1);
         grid.setWidget(3, 1, errmsg2);
         grid.setWidget(5, 1, errmsg3);
 
         grid.setWidget(0, 1, username);
         grid.setWidget(2, 1, email);
         grid.setWidget(4, 1, pw1);
         grid.setWidget(6, 1, pw2);
 
         vpanel.add(grid);
 
         HorizontalPanel hpanel = new HorizontalPanel();
         hpanel.setWidth("100%");
 
         hpanel.add(new Button("Submit", new ClickListener() {
             public void onClick(Widget sender) {
                 if (!validateEntries()) {
                     return;
                 }
 
                 GoteFarm.testService.newUser(username.getText(), email.getText(), pw1.getText(), new AsyncCallback<String>() {
                     public void onSuccess(String result) {
                         GoteFarm.sessionID = result;
                         hide();
                         History.newItem("events");
                     }
 
                     public void onFailure(Throwable caught) {
                         try {
                             throw caught;
                         }
                         catch (AlreadyExistsError e) {
                             errmsg1.setText("Username already exists!");
                         }
                         catch (Throwable e) {
                             errmsg1.setText(e.toString());
                         }
                     }
                 });
             }
         }));
 
         hpanel.add(new Button("Cancel", new ClickListener() {
             public void onClick(Widget sender) {
                 hide();
                 History.newItem("events");
             }
         }));
 
         vpanel.add(hpanel);
 
         setWidget(vpanel);
     }
 
     boolean validateEntries() {
         boolean valid = true;
 
         String user = username.getText();
         if (user.length() == 0) {
             valid = false;
             errmsg1.setText("Username is empty!");
         }
         else {
             if (!user.matches("^\\S+$")) {
                 valid = false;
                 errmsg1.setText("Username contains whitespace!");
             }
             else {
                 errmsg1.setText("");
             }
         }
 
         String em = email.getText();
         if (em.length() == 0) {
             valid = false;
             errmsg2.setText("Email is empty!");
         }
         else {
             if (!em.matches("^\\S+$")) {
                 valid = false;
                 errmsg2.setText("Email contains whitespace!");
             }
             else {
                 errmsg2.setText("");
             }
         }
 
         String pw = pw1.getText();
         if (pw.length() == 0) {
             valid = false;
             errmsg3.setText("Password is empty!");
         }
         else {
             if (!pw1.getText().equals(pw2.getText())) {
                 valid = false;
                 errmsg3.setText("Passwords do not match!");
             }
             else {
                 errmsg3.setText("");
             }
         }
 
         return valid;
     }

    public void onChange(Widget sender) {
        validateEntries();
    }
 }

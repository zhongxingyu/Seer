 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import java.util.List;
 
 public class RoleAndBadgeEditor extends Composite {
 
     VerticalPanel vpanel = new VerticalPanel();
     String flavor1; // e.g., Roles
 
     public RoleAndBadgeEditor(String flavor1) {
         this.flavor1 = flavor1;
         vpanel.setWidth("100%");
         initWidget(vpanel);
         setStyleName("RoleAndBadgeEditor");
     }
 
     public <A extends BadgeAndRole, B extends ChrBadgeAndRole, C extends BadgeAndRoleClickHandlerFactory>
     void update(List<A> roles, B[] chrroles, C clickListener) {
         vpanel.clear();
 
         FlexTable flex = new FlexTable();
         flex.setWidth("100%");
 
         vpanel.add(new Label(flavor1 + " - Check all that apply"));
         int row = 0;
         for (final A role : roles) {
             final SimpleCheckBox hasrole = new SimpleCheckBox();
             flex.setWidget(row, 0, hasrole);
 
             boolean has_role = false;
             boolean is_waiting = false;
             for (B chrrole : chrroles) {
                if (chrrole.getKey() == role.getKey()) {
                     has_role = true;
                     hasrole.addClickHandler(
                         clickListener.newClickHandler(flex, row, chrrole)
                     );
                     is_waiting = chrrole.isWaiting();
                     break;
                 }
             }
 
             if (has_role) {
                 hasrole.setChecked(true);
                 if (is_waiting) {
                     flex.setText(row, 2, "pending admin approval");
                 }
             }
             else {
                 hasrole.addClickHandler(clickListener.newClickHandler(
                     flex, row, new ChrBadgeAndRole() {
                     public String getKey() { return role.getKey(); }
                     public String getName() { return role.getName(); }
                     public String getMessage() { return null; }
                     public boolean isRestricted() { return role.isRestricted(); }
                     public boolean isApproved() { return !role.isRestricted(); }
                     public boolean isWaiting() { return role.isRestricted(); }
                 }));
             }
 
             flex.setText(row, 1, role.getName());
 
             // TODO: Show if request has been denied, show admin message if
             // any
 
             ++row;
         }
 
         vpanel.add(flex);
     }
 }

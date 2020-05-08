 package org.mule.galaxy.web.client.property;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WUser;
 
 public class UserListPropertyPanel extends ListPropertyPanel {
 
     private Collection users;
     private InlineFlowPanel addPanel = new InlineFlowPanel();
     private Button addButton;
     private ListBox userLB;
     
     protected void loadRemote() {
         galaxy.getSecurityService().getUsers(new AbstractCallback(errorPanel) {
 
             public void onSuccess(Object o) {
                 users = (Collection) o;
                 onFinishLoad();
             }
             
         });
     }
     
     protected void onFinishLoad() {
         userLB = new ListBox();
         updateUsers();
         addPanel.add(userLB);
         
         addButton = new Button();
         addButton.setText("Add");
         addButton.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 addUserLabelForSelection();
             }
         });
         addPanel.add(addButton);
         
         super.onFinishLoad();
     }
     
     protected void removeLabel(String id) {
         updateUsers();
     }
 
     private void updateUsers() {
         userLB.clear();
         for (Iterator itr = users.iterator(); itr.hasNext();) {
             WUser user = (WUser) itr.next();
             
             if (!values.contains(user.getId())) {
                 userLB.addItem(user.getName(), user.getId());
             }
         }
     }
 
     protected void addUserLabelForSelection() {
         int idx = userLB.getSelectedIndex();
 
         if (idx == -1) {
             return;
         }
         String id = userLB.getValue(idx);
         
         values.add(id);
 
         editValuesPanel.add(createLabel(id));
         userLB.removeItem(idx);
     }
 
    @Override
    protected String getRenderedText(Object id) {
        WUser user = getUser((String) id);
         
         if (user != null) {
             return user.getName();
         }
         return "";
     }
     
     private WUser getUser(String id) {
         for (Iterator itr = users.iterator(); itr.hasNext();) {
             WUser user = (WUser) itr.next();
             
             if (id.equals(user.getId())) {
                 return user;
             }
         }
         return null;
     }
     
     protected Widget getAddWidget() {
         return addPanel;
     }
 
     @Override
     public boolean saveAsCollection() {
         return true;
     }
 
 }

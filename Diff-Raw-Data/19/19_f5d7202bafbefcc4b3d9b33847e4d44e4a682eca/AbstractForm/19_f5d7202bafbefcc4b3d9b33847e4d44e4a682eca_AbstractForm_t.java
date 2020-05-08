 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.util;
 
 import org.mule.galaxy.web.client.AbstractComposite;
 import org.mule.galaxy.web.client.ErrorPanel;
import org.mule.galaxy.web.client.admin.AdministrationPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.List;
 
 public abstract class AbstractForm extends AbstractComposite implements ClickListener {
 
     protected FlowPanel panel;
     protected boolean newItem;
     private Button save;
     private Button delete;
     private Button cancel;
     private String successToken;
     private final String successMessage;
     private final ErrorPanel errorPanel;
     private final String deleteMessage;
    private final String CANCEL_MESSAGE = "Action Canceled";
 
     public AbstractForm(ErrorPanel errorPanel, String successToken,
                         String successMessage, String deleteMessage) {
         super();
         this.errorPanel = errorPanel;
         this.successToken = successToken;
         this.successMessage = successMessage;
         this.deleteMessage = deleteMessage;
 
         panel = new FlowPanel();
 
         initWidget(panel);
     }
 
     public void onShow(List params) {
         super.onShow();
 
         if (params.size() > 0) {
             String param = (String) params.get(0);
             if ("new".equals(param)) {
                 newItem = true;
                 initializeNewItem();
                 onShowPostInitialize();
             } else {
                 newItem = false;
                 fetchItem(param);
             }
         } else {
             newItem = true;
             initializeNewItem();
             onShowPostInitialize();
         }
     }
 
     protected void onShowPostInitialize() {
         panel.clear();
         panel.add(createPrimaryTitle(getTitle()));
 
         save = new Button("Save");
         save.addClickListener(this);
 
         delete = new Button("Delete");
         delete.addClickListener(this);
 
         cancel = new Button("Cancel");
         cancel.addClickListener(this);
 
         FlexTable table = createFormTable();
         addFields(table);
 
         panel.add(table);
 
         if (newItem) {
             panel.add(asHorizontal(save, cancel));
         } else {
             panel.add(asHorizontal(save, delete, cancel));
         }
     }
 
     /**
      * Too many anonymous inner classes will create a listener object overhead.
      * This will allow a single listener to distinguish between multiple event publishers.
      * Also,those anonymous inner classes will eventually make your eyes bug out.
      *
      * @param sender
      */
     public void onClick(Widget sender) {
         if (sender == save) {
             save();
         } else if (sender == delete) {
             delete();
         } else if (sender == cancel) {
             cancel();
         }
 
     }
 
     protected abstract void fetchItem(String id);
 
     protected abstract void initializeItem(Object o);
 
     protected abstract void initializeNewItem();
 
     protected AsyncCallback getFetchCallback() {
         return new AbstractCallback(errorPanel) {
 
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
             }
 
             public void onSuccess(Object o) {
                 initializeItem(o);
                 onShowPostInitialize();
             }
 
         };
     }
 
     protected FlexTable createFormTable() {
         return createColumnTable();
     }
 
     protected abstract void addFields(FlexTable table);
 
     protected void delete() {
         setEnabled(false);
         save.setText("Deleting...");
     }
 
     protected void setEnabled(boolean enabled) {
         save.setEnabled(enabled);
         delete.setEnabled(enabled);
         cancel.setEnabled(enabled);
 
         if (enabled) {
             save.setText("Save");
             delete.setText("Delete");
             cancel.setText("Cancel");
         }
     }
 
     protected void save() {
         setEnabled(false);
         save.setText("Saving...");
     }
 
     /**
      *
      * @return list of validation receipts TODO class name
      */
     protected boolean validate() {
         return true;
     }
 
     /* Use the successToken page as the cancel redirect page */
     protected void cancel() {
         setEnabled(false);
         cancel.setText("Canceling...");
        ((AdministrationPanel)errorPanel).getGalaxy().setMessageAndGoto(successToken,
                                                 CANCEL_MESSAGE);
     }
 
 
     public abstract String getTitle();
 
     protected AsyncCallback getSaveCallback() {
         return new AbstractCallback(errorPanel) {
 
             public void onFailure(Throwable caught) {
                 setEnabled(true);
                 super.onFailure(caught);
             }
 
             public void onSuccess(Object arg0) {
                 setEnabled(false);
                ((AdministrationPanel)errorPanel).getGalaxy().setMessageAndGoto(successToken,
                                                         successMessage);
             }
 
         };
     }
 
     protected AsyncCallback getDeleteCallback() {
         return new AbstractCallback(errorPanel) {
 
             public void onFailure(Throwable caught) {
                 setEnabled(true);
                 super.onFailure(caught);
             }
 
             public void onSuccess(Object arg0) {
                 setEnabled(false);
                ((AdministrationPanel)errorPanel).getGalaxy().setMessageAndGoto(successToken,
                                                         deleteMessage);
             }
 
         };
     }
 
     public ErrorPanel getErrorPanel() {
         return errorPanel;
     }
 }

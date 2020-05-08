 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-08-25 00:59:35Z mark $
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
 
 package org.mule.galaxy.web.client.admin;
 
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.mule.galaxy.web.client.util.TooltipListener;
 import org.mule.galaxy.web.client.validation.StringNotEmptyValidator;
 import org.mule.galaxy.web.client.validation.ui.ValidatableListBox;
 import org.mule.galaxy.web.client.validation.ui.ValidatableTextArea;
 import org.mule.galaxy.web.client.validation.ui.ValidatableTextBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WScript;
 import org.mule.galaxy.web.rpc.WScriptJob;
 
 
 public class ScheduleForm extends AbstractAdministrationForm {
 
     private ValidatableListBox scriptLB;
     private ValidatableTextBox nameTB;
     private ValidatableTextBox cronTB;
     private ValidatableTextArea descriptionTA;
     private WScriptJob job;
 
     public ScheduleForm(AdministrationPanel administrationPanel) {
         super(administrationPanel, "schedules", "Scheduled item was saved.", "Scheduled item was deleted.",
               "A Scheduled item with that name already exists");
     }
 
     protected void fetchItem(String id) {
         adminPanel.getGalaxy().getAdminService().getScriptJob(id, getFetchCallback());
     }
 
     protected void initializeItem(Object o) {
         job = (WScriptJob) o;
     }
 
     protected void initializeNewItem() {
         job = new WScriptJob();
     }
 
     protected void addFields(FlexTable table) {
 
         // a simple row counter to simplify table.setWidget() calls
         int row = 0;
         table.setText(row++, 0, "Script:");
         table.setText(row++, 0, "Name:");
         table.setText(row++, 0, "Description:");
         table.setText(row++, 0, "Cron Command:");
 
         row = 0;
         scriptLB = new ValidatableListBox(new StringNotEmptyValidator());
         table.setWidget(row, 1, scriptLB);
         loadScripts();
 
         row++;
         nameTB = new ValidatableTextBox(new StringNotEmptyValidator());
         table.setWidget(row, 1, nameTB);
         table.setWidget(row, 2, new Label(" "));
         nameTB.setText(job.getName());
         
         row++;
         descriptionTA = new ValidatableTextArea(new StringNotEmptyValidator());
         descriptionTA.getTextArea().setCharacterWidth(18);
         descriptionTA.getTextArea().setVisibleLines(4);
         table.setWidget(row, 1, descriptionTA);
         descriptionTA.setText(job.getDescription());
         
         row++;
         cronTB = new ValidatableTextBox(new StringNotEmptyValidator());
         cronTB.setText(job.getExpression());
         table.setWidget(row, 1, cronTB);
         Image help = new Image("images/help_16x16.gif");
         help.addMouseListener(new TooltipListener(getCronHelpString(),
                                                   10000));
         table.setWidget(row, 2, help);
 
         styleHeaderColumn(table);
     }
 
     private void loadScripts() {
         adminPanel.getGalaxy().getAdminService().getScripts(new AbstractCallback<List<WScript>>(adminPanel) {
 
             public void onSuccess(List<WScript> scripts) {
                 finishLoadScripts(scripts);
             }
             
         });
     }
 
     protected void finishLoadScripts(List<WScript> scripts) {
         ListBox lb = scriptLB.getListBox();
         for (WScript s : scripts) {
             lb.addItem(s.getName(), s.getId());
             
            if (s.getId().equals(job.getScript())) {
                 lb.setSelectedIndex(lb.getItemCount()-1);
             }
         }
     }
 
     private String getCronHelpString() {
         FlexTable t = new FlexTable();
         t.setText(0, 0, "Field Name");
         t.setText(0, 1, "Mandatory");
         t.setText(0, 2, "Allowed Values");
         t.setText(0, 3, "Allowed Special Characters");
 
         t.setText(1, 0, "Seconds");
         t.setText(1, 1, "YES");
         t.setText(1, 2, "0-59");
         t.setText(1, 3, ", - * / ");
 
         t.setText(2, 0, "Minutes");
         t.setText(2, 1, "YES");
         t.setText(2, 2, "0-59");
         t.setText(2, 3, ", - * / ");
 
         t.setText(3, 0, "Hours");
         t.setText(3, 1, "YES");
         t.setText(3, 2, "0-23");
         t.setText(3, 3, ", - * / ");
 
         t.setText(4, 0, "Day Of Month");
         t.setText(4, 1, "YES");
         t.setText(4, 2, "0-31");
         t.setText(4, 3, ", - * / L W");
 
         t.setText(5, 0, "Month");
         t.setText(5, 1, "YES");
         t.setText(5, 2, "1-12 or JAN-DEC");
         t.setText(5, 3, ", - * / ");
 
         t.setText(6, 0, "Day Of Week");
         t.setText(6, 1, "YES");
         t.setText(6, 2, "1-7 or SUN-SAT");
         t.setText(6, 3, ", - * / L #");
 
         t.setText(7, 0, "Year");
         t.setText(7, 1, "NO");
         t.setText(7, 2, "empty, 1970-2099");
         t.setText(7, 3, ", - * / ");
         return t.toString();
     }
 
     public String getTitle() {
         String s = (newItem) ? "Add" : "Edit";
         return s + " Scheduled Item";
     }
 
     protected void save() {
         if (!validate()) {
             return;
         }
         super.save();
         
         job.setName(nameTB.getText());
         job.setDescription(descriptionTA.getText());
         job.setExpression(cronTB.getText());
         ListBox lb = scriptLB.getListBox();
         int selectedIndex = lb.getSelectedIndex();
         if (selectedIndex != -1) {
             job.setScript(lb.getValue(selectedIndex));
         }
         adminPanel.getGalaxy().getAdminService().save(job, getSaveCallback());
     }
 
 
     protected boolean validate() {
         getErrorPanel().clearErrorMessage();
         boolean isOk = true;
 
         //isOk &= scriptLB.validate();
         isOk &= nameTB.validate();
         isOk &= cronTB.validate();
         return isOk;
     }
 
 }

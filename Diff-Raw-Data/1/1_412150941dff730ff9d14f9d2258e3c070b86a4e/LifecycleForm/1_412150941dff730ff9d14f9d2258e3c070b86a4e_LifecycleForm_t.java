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
 
 package org.mule.galaxy.web.client.admin;
 
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.client.util.ConfirmDialog;
 import org.mule.galaxy.web.client.util.ConfirmDialogAdapter;
 import org.mule.galaxy.web.client.validation.StringNotEmptyValidator;
 import org.mule.galaxy.web.client.validation.ui.ValidatableTextBox;
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WPhase;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.FocusListener;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.gwtwidgets.client.ui.LightBox;
 
 public class LifecycleForm extends AbstractAdministrationForm {
 
     private WLifecycle lifecycle;
     private ValidatableTextBox nameTB;
     private FlexTable nextPhasesPanel;
     private ListBox phases;
     private ListBox nextPhases;
     private ValidatableTextBox phaseNameTB;
     private Button deletePhase;
     private Button addBtn;
     private WPhase initialPhase;
     private CheckBox defaultLifecycleCB;
 
     public LifecycleForm(AdministrationPanel adminPanel) {
         super(adminPanel, "lifecycles", "Lifecycle was saved.", "Lifecycle was deleted.");
 
         panel.setStyleName("lifecycle-form-base");
     }
     
     protected void fetchItem(String id) {
         adminPanel.getRegistryService().getLifecycle(id, getFetchCallback());
     }
 
     protected void initializeItem(Object o) {
         lifecycle = (WLifecycle) o;
         
         initialPhase = lifecycle.getInitialPhase();
     }
 
     protected void initializeNewItem() {
         lifecycle = new WLifecycle();
        initialPhase = null;
     }
     
     protected FlexTable createFormTable() {
         FlexTable table = new FlexTable();
         
         table.setCellSpacing(5);
         table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
         table.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
         
         return table;
     }
     
     protected void addFields(FlexTable table) {
         FlexTable nameAndPhases = createColumnTable();
 
         nameTB = new ValidatableTextBox(new StringNotEmptyValidator());
         nameTB.getTextBox().setText(lifecycle.getName());
         nameAndPhases.setText(0, 0, "Lifecycle Name:");
         nameAndPhases.setWidget(0, 1, nameTB);
 
         defaultLifecycleCB = new CheckBox();
         if (lifecycle.isDefaultLifecycle()) {
             nameAndPhases.setText(1, 0, "Is Default Lifecycle:");
             nameAndPhases.setText(1, 1, "Yes");
         } else {
             nameAndPhases.setText(1, 0, "Make Default Lifecycle:");
             nameAndPhases.setWidget(1, 1, defaultLifecycleCB);
         }
 
         phases = new ListBox();
         phases.setVisibleItemCount(10);
         if (lifecycle.getPhases() != null) {
             for (Iterator itr = lifecycle.getPhases().iterator(); itr.hasNext();) {
                 WPhase p = (WPhase) itr.next();
 
                 phases.addItem(p.getName(), p.getId());
             }
         }
 
         addBtn = new Button("Add");
         addBtn.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 addPhase();
             }
         });
 
         deletePhase = new Button("Delete");
         deletePhase.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 deletePhase();
             }
         });
 
 
         FlowPanel addDelPhase = new FlowPanel();
         addDelPhase.add(asDiv(phases));
         addDelPhase.add(asDiv(addBtn));
         addDelPhase.add(asDiv(deletePhase));
 
         nameAndPhases.setText(2, 0, "Phases:");
         nameAndPhases.setWidget(2, 1, addDelPhase);
 
         // right side of the panel
         nextPhasesPanel = createColumnTable();
         phases.addClickListener(new ClickListener() {
 
             public void onClick(Widget arg0) {
                 showNextPhases();
             }
 
         });
 
         // add to main panel
         styleHeaderColumn(nameAndPhases);
         table.setWidget(0, 0, nameAndPhases);
         table.setWidget(0, 1, nextPhasesPanel);
     }
 
     public String getTitle() {
         String title;
         if (newItem) {
             title = "Add Lifecycle";
             lifecycle.setPhases(new ArrayList());
         } else {
             title = "Edit Lifecycle " + lifecycle.getName();
         }
         return title;
     }
 
 
     protected void addPhase()
     {
         new LightBox(new AddDialog(this)).show();
     }
 
     protected void addPhase(String name) {
         WPhase p = new WPhase();
         p.setName(name);
 
         lifecycle.getPhases().add(p);
         phases.addItem(name);
 
         phases.setSelectedIndex(phases.getItemCount()-1);
         showNextPhases();
     }
 
     protected void deletePhase() {
         WPhase phase = getSelectedPhase();
         if (phase == null) return;
 
         lifecycle.getPhases().remove(phase);
 
         int idx = findPhaseInList(phases, phase.getName());
         phases.removeItem(idx);
 
         for (Iterator itr = lifecycle.getPhases().iterator(); itr.hasNext();) {
             WPhase p2 = (WPhase) itr.next();
 
             if (p2.getNextPhases() != null && p2.getNextPhases().contains(phase)) {
                 p2.getNextPhases().remove(phase);
             }
         }
 
         nextPhasesPanel.clear();
     }
 
     protected void showNextPhases() {
         final WPhase phase = getSelectedPhase();
         if (phase == null) return;
 
         nextPhasesPanel.clear();
 
         nextPhases = new ListBox();
         nextPhases.setMultipleSelect(true);
         nextPhases.setVisibleItemCount(10);
 
         phaseNameTB = new ValidatableTextBox(new StringNotEmptyValidator());
         phaseNameTB.getTextBox().setText(phase.getName());
         phaseNameTB.getTextBox().addFocusListener(new FocusListener() {
             public void onFocus(Widget arg0) {
             }
 
             public void onLostFocus(Widget arg0) {
                 if (!phaseNameTB.validate()) {
                     phaseNameTB.getTextBox().setFocus(true);
                     return;
                 }
                 String newName = phaseNameTB.getTextBox().getText();
 
                 // update left hand phases list with new name
                 int idx = findPhaseInList(phases, phase.getName());
                 phases.setItemText(idx, newName);
                 phases.setValue(idx, newName);
                 
                 // update next phases list with new name
                 idx = findPhaseInList(nextPhases, phase.getName());
                 if (idx != -1) {
                     nextPhases.setItemText(idx, newName);
                     nextPhases.setValue(idx, newName);
                 }
                 
                 // update actual phase object
                 phase.setName(newName);
             }
         });
         nextPhasesPanel.setText(0, 0, "Phase Name:");
         nextPhasesPanel.setWidget(0, 1, phaseNameTB);
 
         final CheckBox initialPhaseCB = new CheckBox();
         initialPhaseCB.addClickListener(new ClickListener() {
             public void onClick(Widget w) {
                 initialPhase = phase;
             }
         });
         initialPhaseCB.setChecked(initialPhase == phase);
 
         nextPhasesPanel.setText(1, 0, "Initial Phase:");
         nextPhasesPanel.setWidget(1, 1, initialPhaseCB);
 
         int i = 0;
         for (Iterator itr = lifecycle.getPhases().iterator(); itr.hasNext();) {
             WPhase p = (WPhase) itr.next();
 
             if (p.equals(phase)) continue;
 
             nextPhases.addItem(p.getName(), p.getId());
 
             if (phase.getNextPhases() != null && phase.getNextPhases().contains(p)) {
                 nextPhases.setItemSelected(i, true);
             }
             i++;
         }
 
         nextPhasesPanel.setText(2, 0, "Next Phases:");
         nextPhasesPanel.setWidget(2, 1, nextPhases);
 
         nextPhases.addChangeListener(new ChangeListener() {
             public void onChange(Widget arg0) {
                 updateNextPhases(phase, nextPhases);
             }
         });
         styleHeaderColumn(nextPhasesPanel);
     }
 
     private WPhase getSelectedPhase() {
         int idx = phases.getSelectedIndex();
 
         if (idx == -1) return null;
 
         String id = phases.getValue(idx);
 
         WPhase p = lifecycle.getPhaseById(id);
         if (p == null) {
             p = lifecycle.getPhase(id);
         }
         return p;
     }
 
     protected void updateNextPhases(WPhase phase, ListBox nextPhases) {
         phase.setNextPhases(new ArrayList());
         for (int i = 0; i < nextPhases.getItemCount(); i++) {
             if (nextPhases.isItemSelected(i)) {
                 phase.getNextPhases().add(lifecycle.getPhaseById(nextPhases.getValue(i)));
             }
         }
     }
 
     protected int findPhaseInList(ListBox phases, String name) {
         for (int i = 0; i < phases.getItemCount(); i++) {
             String txt = phases.getItemText(i);
 
             if (txt.equals(name)) {
                 return i;
             }
         }
         return -1;
     }
 
 
     protected void save() {
         if (!validate()) {
             return;
         }
 
         super.save();
 
         if (defaultLifecycleCB.isChecked()) {
             lifecycle.setDefaultLifecycle(true);
         }
         lifecycle.setName(nameTB.getTextBox().getText());
         lifecycle.setInitialPhase(initialPhase);
 
         adminPanel.getRegistryService().saveLifecycle(lifecycle, getSaveCallback());
     }
 
     protected void delete() {
         final ConfirmDialog dialog = new ConfirmDialog(new ConfirmDialogAdapter() {
             public void onConfirm() {
                 LifecycleForm.super.delete();
                 adminPanel.getRegistryService().deleteLifecycle(lifecycle.getId(), getDeleteCallback());
             }
         }, "Are you sure you want to delete lifecycle " + lifecycle.getName() + "?");
         new LightBox(dialog).show();
     }
 
     protected void setEnabled(boolean enabled) {
         nameTB.getTextBox().setEnabled(enabled);
         phases.setEnabled(enabled);
 
         if (nextPhases != null) {
             nextPhases.setEnabled(enabled);
             phaseNameTB.getTextBox().setEnabled(enabled);
         }
 
         super.setEnabled(enabled);
     }
 
     public static final class AddDialog extends DialogBox {
 
         public AddDialog(final LifecycleForm panel) {
             // Set the dialog box's caption.
             setText("Please enter the name of the phase you would like to add:");
 
             FlexTable buttonPanel = new FlexTable();
 
             final ValidatableTextBox tb = new ValidatableTextBox(new StringNotEmptyValidator());
 
             Button cancel = new Button("Cancel");
             cancel.addClickListener(new ClickListener() {
                 public void onClick(Widget sender) {
                     AddDialog.this.hide();
                 }
             });
             Button ok = new Button("OK");
             ok.addClickListener(new ClickListener() {
                 public void onClick(Widget sender) {
                     if (!tb.validate()) {
                         return;
                     }
                     AddDialog.this.hide();
                     panel.addPhase(tb.getTextBox().getText());
                 }
             });
 
             // allow keyboard shortcuts
             tb.getTextBox().addKeyboardListener(new KeyboardListenerAdapter() {
                 public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                     if ((keyCode == KEY_ENTER) && (modifiers == 0)) {
                         if (!tb.validate()) {
                             return;
                         }
                         AddDialog.this.hide();
                         panel.addPhase(tb.getTextBox().getText());
                     }
                     if ((keyCode == KEY_ESCAPE) && (modifiers == 0)) {
                         AddDialog.this.hide();
                     }
                 }
             });
 
             buttonPanel.setWidget(0, 0, tb);
             buttonPanel.setWidget(0, 1, ok);
             buttonPanel.setWidget(0, 2, cancel);
 
             setWidget(buttonPanel);
         }
     }
 
     protected boolean validate() {
         final ErrorPanel errorPanel = getErrorPanel();
         errorPanel.clearErrorMessage();
 
         boolean isOk = true;
         if (initialPhase == null) {
             errorPanel.addMessage("You must set one phase as the initial phase before the lifecycle can be saved.");
             isOk = false;
         }
         
         if (phases.getItemCount() == 0) {
             errorPanel.addMessage("Lifecycle must have at least one phase");
             isOk = false;
         }
         
         isOk &= nameTB.validate();
         
         if (phaseNameTB != null) {
             isOk &= phaseNameTB.validate();
         }
         
         return isOk;
     }
 }

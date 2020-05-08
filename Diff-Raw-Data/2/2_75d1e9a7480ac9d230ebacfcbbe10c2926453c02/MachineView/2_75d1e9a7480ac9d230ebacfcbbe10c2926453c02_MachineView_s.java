 /**
  *
  * SIROCCO
  * Copyright (C) 2013 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  */
 package org.ow2.sirocco.cloudmanager;
 
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
 import org.ow2.sirocco.cloudmanager.core.api.IdentityContext;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.model.cimi.Machine;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineDisk;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterface;
 
 import com.vaadin.cdi.UIScoped;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.BeanContainer;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.VerticalSplitPanel;
 
 @UIScoped
 public class MachineView extends VerticalSplitPanel implements ValueChangeListener {
     private static final long serialVersionUID = 1L;
 
     private Button startMachineButton;
 
     private Button stopMachineButton;
 
     private Button restartMachineButton;
 
     private Button deleteMachineButton;
 
     private Table machineTable;
 
     BeanContainer<String, MachineBean> machines;
 
     @Inject
     private MachineCreationWizard machineCreationWizard;
 
     private MachineDetailView detailView;
 
     @Inject
     IMachineManager machineManager;
 
     @Inject
     IdentityContext identityContext;
 
     public MachineView() {
         this.setSizeFull();
 
         VerticalLayout verticalLayout = new VerticalLayout();
         verticalLayout.setSizeFull();
 
         HorizontalLayout actionButtonHeader = new HorizontalLayout();
         actionButtonHeader.setMargin(true);
         actionButtonHeader.setSpacing(true);
         actionButtonHeader.setWidth("100%");
         actionButtonHeader.setHeight("50px");
 
         Button button = new Button("Launch Instance...");
         button.setIcon(new ThemeResource("img/add.png"));
         button.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 if (MachineView.this.machineCreationWizard.init(MachineView.this)) {
                     UI.getCurrent().addWindow(MachineView.this.machineCreationWizard);
                 }
             }
         });
         actionButtonHeader.addComponent(button);
 
         this.startMachineButton = new Button("Start");
         this.startMachineButton.setIcon(new ThemeResource("img/poweron.png"));
         this.startMachineButton.setEnabled(false);
         this.startMachineButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 Set<?> selectedMachineIds = (Set<?>) MachineView.this.machineTable.getValue();
                 String id = (String) selectedMachineIds.iterator().next();
                 try {
                     MachineView.this.machineManager.startMachine(id);
                 } catch (CloudProviderException e) {
                     Util.diplayErrorMessageBox("Cannot start instance", e);
                 }
             }
         });
         actionButtonHeader.addComponent(this.startMachineButton);
 
         this.stopMachineButton = new Button("Stop");
         this.stopMachineButton.setIcon(new ThemeResource("img/poweroff.png"));
         this.stopMachineButton.setEnabled(false);
         this.stopMachineButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 Set<?> selectedMachineIds = (Set<?>) MachineView.this.machineTable.getValue();
                 String id = (String) selectedMachineIds.iterator().next();
                 try {
                     MachineView.this.machineManager.stopMachine(id);
                 } catch (CloudProviderException e) {
                     Util.diplayErrorMessageBox("Cannot stop instance", e);
                 }
             }
         });
         actionButtonHeader.addComponent(this.stopMachineButton);
 
         this.restartMachineButton = new Button("Reboot");
         this.restartMachineButton.setIcon(new ThemeResource("img/restart.png"));
         this.restartMachineButton.setEnabled(false);
         this.restartMachineButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 Set<?> selectedMachineIds = (Set<?>) MachineView.this.machineTable.getValue();
                 String id = (String) selectedMachineIds.iterator().next();
                 try {
                     MachineView.this.machineManager.restartMachine(id, false);
                 } catch (CloudProviderException e) {
                     Util.diplayErrorMessageBox("Cannot reboot instance", e);
                 }
             }
         });
         actionButtonHeader.addComponent(this.restartMachineButton);
 
         this.deleteMachineButton = new Button("Delete");
         this.deleteMachineButton.setIcon(new ThemeResource("img/delete.png"));
         this.deleteMachineButton.setEnabled(false);
         this.deleteMachineButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 final Set<?> selectedMachineIds = (Set<?>) MachineView.this.machineTable.getValue();
                 StringBuilder sb = new StringBuilder();
                 sb.append("Are you sure you want to delete ");
                 if (selectedMachineIds.size() == 1) {
                     Object id = selectedMachineIds.iterator().next();
                     sb.append("instance " + MachineView.this.machines.getItem(id).getBean().getName() + " ?");
                 } else {
                     sb.append(" these " + selectedMachineIds.size() + " instances ?");
                 }
                 ConfirmDialog confirmDialog = ConfirmDialog.newConfirmDialog("Delete Machine", sb.toString(),
                     new ConfirmDialog.ConfirmationDialogCallback() {
 
                         @Override
                         public void response(final boolean ok, final boolean ignored) {
                             if (ok) {
                                 for (Object id : selectedMachineIds) {
                                     try {
                                         MachineView.this.machineManager.deleteMachine(id.toString());
                                     } catch (CloudProviderException e) {
                                         Util.diplayErrorMessageBox("Cannot delete instance", e);
                                     }
                                 }
                                 MachineView.this.valueChange(null);
                             }
                         }
                     });
                 MachineView.this.getUI().addWindow(confirmDialog);
             }
         });
         actionButtonHeader.addComponent(this.deleteMachineButton);
 
         Label spacer = new Label();
         spacer.setWidth("100%");
         actionButtonHeader.addComponent(spacer);
         actionButtonHeader.setExpandRatio(spacer, 1.0f);
 
         button = new Button("Refresh", new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 MachineView.this.refresh();
             }
         });
         button.setIcon(new ThemeResource("img/refresh.png"));
         actionButtonHeader.addComponent(button);
 
         verticalLayout.addComponent(actionButtonHeader);
         verticalLayout.addComponent(this.machineTable = this.createMachineTable());
         verticalLayout.setExpandRatio(this.machineTable, 1.0f);
 
         this.setFirstComponent(verticalLayout);
         this.setSecondComponent(this.detailView = new MachineDetailView(this));
         this.setSplitPosition(60.0f);
 
     }
 
     void refresh() {
         this.machineTable.setValue(null);
         this.machineTable.getContainerDataSource().removeAllItems();
         try {
             for (Machine machine : this.machineManager.getMachines().getItems()) {
                 this.machines.addBean(new MachineBean(machine));
             }
         } catch (CloudProviderException e) {
             Util.diplayErrorMessageBox("Internal error", e);
         }
         this.valueChange(null);
     }
 
     @SuppressWarnings("serial")
     Table createMachineTable() {
         this.machines = new BeanContainer<String, MachineBean>(MachineBean.class);
         this.machines.setBeanIdProperty("id");
         Table table = new Table();
         table.setContainerDataSource(this.machines);
 
         table.setSizeFull();
         table.setPageLength(0);
 
         table.setSelectable(true);
         table.setMultiSelect(true);
         table.setImmediate(true);
 
         table.setColumnHeader("addresses", "IP addresses");
 
         table.addGeneratedColumn("state", new Util.StateColumnGenerator());
         table.addGeneratedColumn("location", new Util.LocationColumnGenerator());
         table.addGeneratedColumn("addresses", new IPAddressesColumnGenerator());
 
         table.setVisibleColumns("name", "state", "addresses", "cpu", "memory", "disks", "provider", "location");
 
         table.addValueChangeListener(this);
 
         return table;
     }
 
     @Override
     public void valueChange(final ValueChangeEvent event) {
         Set<?> selectedMachineIds = (Set<?>) this.machineTable.getValue();
         if (selectedMachineIds != null && selectedMachineIds.size() > 0) {
             if (selectedMachineIds.size() == 1) {
                 Object id = selectedMachineIds.iterator().next();
                 String state = (String) this.machineTable.getItem(id).getItemProperty("state").getValue();
                 this.startMachineButton.setEnabled(state.endsWith("STOPPED"));
                 this.stopMachineButton.setEnabled(state.endsWith("STARTED"));
                 this.restartMachineButton.setEnabled(state.endsWith("STARTED"));
                 this.deleteMachineButton.setEnabled(!state.endsWith("DELETING") && !state.endsWith("DELETED"));
                 this.detailView.update(this.machines.getItem(id).getBean());
             } else {
                 this.detailView.hide();
                 this.startMachineButton.setEnabled(false);
                 this.stopMachineButton.setEnabled(false);
                 this.restartMachineButton.setEnabled(false);
                 boolean allowMultiDelete = true;
                 for (Object machineId : selectedMachineIds) {
                     String state = (String) this.machineTable.getItem(machineId).getItemProperty("state").getValue();
                     if (state.endsWith("DELETING") || state.endsWith("DELETED")) {
                         allowMultiDelete = false;
                         break;
                     }
                 }
                 this.deleteMachineButton.setEnabled(allowMultiDelete);
             }
         } else {
             this.detailView.hide();
             this.startMachineButton.setEnabled(false);
             this.stopMachineButton.setEnabled(false);
             this.restartMachineButton.setEnabled(false);
             this.deleteMachineButton.setEnabled(false);
         }
     }
 
     @Override
     public void attach() {
         super.attach();
         this.refresh();
     }
 
     @SuppressWarnings("serial")
     private static class IPAddressesColumnGenerator implements Table.ColumnGenerator {
         public com.vaadin.ui.Component generateCell(final Table source, final Object itemId, final Object columnId) {
             Property<?> prop = source.getItem(itemId).getItemProperty(columnId);
             String addresses = (String) prop.getValue();
             addresses.replaceFirst(" P", "\nP");
             Label label = new Label(addresses, ContentMode.PREFORMATTED);
             return label;
         }
     }
 
     void updateMachine(final Machine machine) {
         BeanItem<MachineBean> item = this.machines.getItem(machine.getUuid());
         if (item != null) {
             MachineBean machineBean = item.getBean();
             machineBean.init(machine);
             item.getItemProperty("state").setValue(machineBean.getState());
             item.getItemProperty("addresses").setValue(machineBean.getAddresses());
             item.getItemProperty("name").setValue(machineBean.getName());
             if (this.detailView.getMachine().getUuid().equals(machine.getUuid())) {
                 this.detailView.update(machineBean);
             }
             this.valueChange(null);
         }
     }
 
     MachineBean updateMachineAttribute(final MachineBean machineBean, final String attribute, final String value) {
         this.machineTable.getItem(machineBean.getId()).getItemProperty(attribute).setValue(value);
         return this.machines.getItem(machineBean.getId()).getBean();
     }
 
     public static class MachineBean {
         Machine machine;
 
         String id;
 
         String name;
 
         String description;
 
         String state;
 
         String addresses;
 
         Integer cpu;
 
         String memory;
 
         String disks;
 
         String provider;
 
         String location;
 
         MachineBean(final Machine machine) {
             this.init(machine);
         }
 
         void init(final Machine machine) {
             this.machine = machine;
             this.id = machine.getUuid();
             this.name = machine.getName();
             this.description = machine.getDescription();
             this.state = machine.getState().toString();
             this.addresses = this.addressesFrom(machine);
             this.cpu = machine.getCpu();
             this.memory = Util.printKibibytesValue(machine.getMemory());
             this.disks = this.disksFrom(machine);
             this.provider = this.providerFrom(machine);
             this.location = this.locationFrom(machine);
         }
 
         public String getId() {
             return this.id;
         }
 
         public void setId(final String id) {
             this.id = id;
         }
 
         public String getName() {
             return this.name;
         }
 
         public void setName(final String name) {
             this.name = name;
         }
 
         public String getDescription() {
             return this.description;
         }
 
         public void setDescription(final String description) {
             this.description = description;
         }
 
         public String getState() {
             return this.state;
         }
 
         public void setState(final String state) {
             this.state = state;
         }
 
         public String getAddresses() {
             return this.addresses;
         }
 
         public void setAddresses(final String addresses) {
             this.addresses = addresses;
         }
 
         public Integer getCpu() {
             return this.cpu;
         }
 
         public void setCpu(final Integer cpu) {
             this.cpu = cpu;
         }
 
         public String getMemory() {
             return this.memory;
         }
 
         public void setMemory(final String memory) {
             this.memory = memory;
         }
 
         public String getDisks() {
             return this.disks;
         }
 
         public void setDisks(final String disks) {
             this.disks = disks;
         }
 
         public String getProvider() {
             return this.provider;
         }
 
         public void setProvider(final String provider) {
             this.provider = provider;
         }
 
         public String getLocation() {
             return this.location;
         }
 
         public void setLocation(final String location) {
             this.location = location;
         }
 
         public String addressesFrom(final Machine machine) {
             StringBuilder sb = new StringBuilder();
             if (machine.getNetworkInterfaces() != null) {
                 for (MachineNetworkInterface nic : machine.getNetworkInterfaces()) {
                     if (nic.getAddresses() != null && !nic.getAddresses().isEmpty()) {
                         // sb.append(nic.getNetwork().getNetworkType() + " ");
                        if (nic.getAddresses().size() == 2) {
                             sb.append("PUBLIC " + nic.getAddresses().get(1).getAddress().getIp() + "\n");
                             sb.append("PRIVATE " + nic.getAddresses().get(0).getAddress().getIp());
                         } else {
                             sb.append("PRIVATE " + nic.getAddresses().get(0).getAddress().getIp());
                         }
                     }
                 }
             }
             return sb.toString();
         }
 
         public String disksFrom(final Machine machine) {
             StringBuffer sb = new StringBuffer();
             if (machine.getDisks() != null) {
                 for (MachineDisk disk : machine.getDisks()) {
                     sb.append(Util.printKilobytesValue(disk.getCapacity()) + " ");
                 }
             }
             return sb.toString();
         }
 
         public String providerFrom(final Machine machine) {
             if (machine.getCloudProviderAccount() != null) {
                 return machine.getCloudProviderAccount().getCloudProvider().getDescription();
             } else {
                 return "";
             }
         }
 
         public String locationFrom(final Machine machine) {
             if (machine.getLocation() != null) {
                 return machine.getLocation().description(true);
             } else {
                 return "";
             }
         }
     }
 }

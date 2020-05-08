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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.core.api.exception.ResourceNotFoundException;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 
 import com.vaadin.cdi.UIScoped;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.BeanContainer;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 
 @UIScoped
 public class MachineImageView extends VerticalLayout implements ValueChangeListener {
     private static final long serialVersionUID = 1L;
 
     private Button deleteMachineImageButton;
 
     private Table machineImageTable;
 
     BeanContainer<Integer, MachineImageBean> images;
 
     // @Autowired
     // private MachineImageCreationWizard machineCreationWizard;
 
     @Inject
     private IMachineImageManager machineImageManager;
 
     public MachineImageView() {
         this.setSizeFull();
 
         HorizontalLayout actionButtonHeader = new HorizontalLayout();
         actionButtonHeader.setMargin(true);
         actionButtonHeader.setSpacing(true);
         actionButtonHeader.setWidth("100%");
         actionButtonHeader.setHeight("50px");
 
         Button button = new Button("Create Image...");
         button.setIcon(new ThemeResource("img/add.png"));
         button.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 // MachineImageView.this.machineCreationWizard.init(MachineImageView.this);
                 // UI.getCurrent().addWindow(MachineImageView.this.machineCreationWizard);
             }
         });
         actionButtonHeader.addComponent(button);
 
         this.deleteMachineImageButton = new Button("Delete");
         this.deleteMachineImageButton.setIcon(new ThemeResource("img/delete.png"));
         this.deleteMachineImageButton.setEnabled(false);
         this.deleteMachineImageButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 final Set<?> selectedImageIds = (Set<?>) MachineImageView.this.machineImageTable.getValue();
                 String name = MachineImageView.this.images.getItem(selectedImageIds.iterator().next()).getBean().getName();
                 ConfirmDialog confirmDialog = new ConfirmDialog("Delete Image", "Are you sure you want to delete image " + name
                     + " ?", "Ok", "Cancel", new ConfirmDialog.ConfirmationDialogCallback() {
 
                     @Override
                     public void response(final boolean ok) {
                         if (ok) {
                             for (Object id : selectedImageIds) {
                                 try {
                                     MachineImageView.this.machineImageManager.deleteMachineImage(id.toString());
                                     MachineImage machineImage = MachineImageView.this.machineImageManager
                                         .getMachineImageById(id.toString());
                                     MachineImageBean newMachineImageBean = new MachineImageBean(machineImage);
                                     int index = MachineImageView.this.images.indexOfId(id);
                                     MachineImageView.this.images.removeItem(id);
                                     MachineImageView.this.images.addBeanAt(index, newMachineImageBean);
                                 } catch (CloudProviderException e) {
                                     e.printStackTrace();
                                 }
                             }
                             MachineImageView.this.valueChange(null);
                         }
                     }
                 });
                 MachineImageView.this.getUI().addWindow(confirmDialog);
             }
         });
         actionButtonHeader.addComponent(this.deleteMachineImageButton);
 
         Label spacer = new Label();
         spacer.setWidth("100%");
         actionButtonHeader.addComponent(spacer);
         actionButtonHeader.setExpandRatio(spacer, 1.0f);
 
         button = new Button("Refresh", new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 MachineImageView.this.refresh();
             }
         });
         button.setIcon(new ThemeResource("img/refresh.png"));
         actionButtonHeader.addComponent(button);
 
         this.addComponent(actionButtonHeader);
         this.addComponent(this.machineImageTable = this.createMachineImageTable());
         this.setExpandRatio(this.machineImageTable, 1.0f);
 
         // refresh();
     }
 
     Label createLabel(final String iconFileName, final String text) {
         Label label = new Label();
         label.setContentMode(ContentMode.HTML);
         label.setValue("<img src=\"" + "VAADIN/themes/mytheme/img/" + iconFileName + "\" /> " + text);
         return label;
     }
 
     Label makeCountryLabel(final String country) {
         return this.createLabel(country.toLowerCase() + "Flag.png", "");
     }
 
     void refresh() {
         this.machineImageTable.getContainerDataSource().removeAllItems();
         try {
             for (MachineImage machineImage : this.machineImageManager.getMachineImages()) {
                 System.out.println("MachineImage id=" + machineImage.getId() + " name=" + machineImage.getName());
                 this.images.addBean(new MachineImageBean(machineImage));
             }
         } catch (CloudProviderException e) {
             e.printStackTrace();
         }
         this.valueChange(null);
     }
 
     Table createMachineImageTable() {
         this.images = new BeanContainer<Integer, MachineImageBean>(MachineImageBean.class);
         this.images.setBeanIdProperty("id");
         Table table = new Table();
         table.setContainerDataSource(this.images);
 
         table.setSizeFull();
         table.setPageLength(0);
 
         table.setSelectable(true);
         table.setMultiSelect(true);
         table.setImmediate(true);
 
         table.addGeneratedColumn("state", new Util.StateColumnGenerator());
         table.addGeneratedColumn("location", new Util.LocationColumnGenerator());
 
         table.setVisibleColumns("name", "state", "provider", "location");
 
         table.addValueChangeListener(this);
 
         return table;
     }
 
     @Override
     public void valueChange(final ValueChangeEvent event) {
         Set<?> selectedMachineImageIds = (Set<?>) this.machineImageTable.getValue();
         if (selectedMachineImageIds != null && selectedMachineImageIds.size() > 0) {
             if (selectedMachineImageIds.size() == 1) {
                 String state = (String) this.machineImageTable.getItem(selectedMachineImageIds.iterator().next())
                     .getItemProperty("state").getValue();
                 this.deleteMachineImageButton.setEnabled(!state.endsWith("DELETING"));
             } else {
                 boolean allowMultiDelete = true;
                 for (Object machineId : selectedMachineImageIds) {
                     String state = (String) this.machineImageTable.getItem(machineId).getItemProperty("state").getValue();
                     if (state.endsWith("DELETING")) {
                         allowMultiDelete = false;
                         break;
                     }
                 }
                 this.deleteMachineImageButton.setEnabled(allowMultiDelete);
             }
         } else {
             this.deleteMachineImageButton.setEnabled(false);
         }
     }
 
     @Override
     public void attach() {
         super.attach();
         this.refresh();
     }
 
     void pollMachineImages() {
         List<Integer> ids = new ArrayList<>(this.images.getItemIds());
         for (Integer id : ids) {
             MachineImageBean machineImageBean = this.images.getItem(id).getBean();
             if (machineImageBean.getState().endsWith("ING")) {
                 try {
                     MachineImage machineImage = this.machineImageManager.getMachineImageById(id.toString());
                     System.out.println("MachineImage id=" + id + " state=" + machineImage.getState());
                     if (!machineImage.getState().toString().endsWith("ING")) {
 
                         MachineImageBean newMachineImageBean = new MachineImageBean(machineImage);
                         int index = this.images.indexOfId(id);
                         this.images.removeItem(id);
                         this.images.addBeanAt(index, newMachineImageBean);
                         this.machineImageTable.setValue(null);
                         this.valueChange(null);
                         this.getUI().push();
                     }
                 } catch (ResourceNotFoundException e) {
                     this.images.removeItem(id);
                     this.machineImageTable.setValue(null);
                     this.valueChange(null);
                     System.out.println("REMOVE PUSH");
                     this.getUI().push();
                 } catch (CloudProviderException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public static class MachineImageBean {
         Integer id;
 
         String name;
 
         String description;
 
         String state;
 
         String provider;
 
         String location;
 
         MachineImageBean(final MachineImage machineImage) {
             this.id = machineImage.getId();
             this.name = machineImage.getName();
             this.description = machineImage.getDescription();
             this.state = machineImage.getState().toString();
             this.provider = this.providerFrom(machineImage);
             this.location = this.locationFrom(machineImage);
         }
 
         public Integer getId() {
             return this.id;
         }
 
         public void setId(final Integer id) {
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
 
         public String providerFrom(final MachineImage machineImage) {
            if (machineImage.getCloudProviderAccount() != null) {
                return machineImage.getCloudProviderAccount().getCloudProvider().getDescription();
            } else {
                return "";
            }
         }
 
         public String locationFrom(final MachineImage machineImage) {
            if (machineImage.getLocation() != null) {
                return machineImage.getLocation().getCountryName();
             } else {
                 return "";
             }
         }
     }
 }

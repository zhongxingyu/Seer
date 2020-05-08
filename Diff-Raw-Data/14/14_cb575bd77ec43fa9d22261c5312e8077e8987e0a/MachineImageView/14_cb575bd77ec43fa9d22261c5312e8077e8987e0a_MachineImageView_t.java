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
 
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.ProviderMapping;
 
 import com.vaadin.cdi.UIScoped;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.BeanContainer;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.server.ThemeResource;
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
 public class MachineImageView extends VerticalSplitPanel implements ValueChangeListener {
     private static final long serialVersionUID = 1L;
 
     private Button registerMachineImageButton;
 
     private Button deleteMachineImageButton;
 
     private Table machineImageTable;
 
     private MachineImageDetailView detailView;
 
     BeanContainer<String, MachineImageBean> images;
 
     @Inject
     private MachineImageRegisterWizard machineImageRegisterWizard;
 
     @Inject
     IMachineImageManager machineImageManager;
 
     public MachineImageView() {
         this.setSizeFull();
 
         VerticalLayout verticalLayout = new VerticalLayout();
         verticalLayout.setSizeFull();
 
         HorizontalLayout actionButtonHeader = new HorizontalLayout();
         actionButtonHeader.setMargin(true);
         actionButtonHeader.setSpacing(true);
         actionButtonHeader.setWidth("100%");
         actionButtonHeader.setHeight("50px");
 
         this.registerMachineImageButton = new Button("Register Image...");
         this.registerMachineImageButton.setIcon(new ThemeResource("img/add.png"));
         this.registerMachineImageButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 if (MachineImageView.this.machineImageRegisterWizard.init(MachineImageView.this)) {
                     UI.getCurrent().addWindow(MachineImageView.this.machineImageRegisterWizard);
                 }
             }
         });
         actionButtonHeader.addComponent(this.registerMachineImageButton);
 
         this.deleteMachineImageButton = new Button("Delete");
         this.deleteMachineImageButton.setIcon(new ThemeResource("img/delete.png"));
         this.deleteMachineImageButton.setEnabled(false);
         this.deleteMachineImageButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 final Set<?> selectedImageIds = (Set<?>) MachineImageView.this.machineImageTable.getValue();
                StringBuilder sb = new StringBuilder();
                sb.append("Are you sure you want to delete ");
                if (selectedImageIds.size() == 1) {
                    Object id = selectedImageIds.iterator().next();
                    sb.append("image " + MachineImageView.this.images.getItem(id).getBean().getName() + " ?");
                } else {
                    sb.append(" these " + selectedImageIds.size() + " images ?");
                }
                 String name = MachineImageView.this.images.getItem(selectedImageIds.iterator().next()).getBean().getName();
                ConfirmDialog confirmDialog = ConfirmDialog.newConfirmDialogWithOption("Delete Image", sb.toString(),
                    "delete image on provider", new ConfirmDialog.ConfirmationDialogCallback() {
 
                         @Override
                         public void response(final boolean ok, final boolean deleteOnProvider) {
                             if (ok) {
                                 for (Object id : selectedImageIds) {
                                     try {
                                         if (deleteOnProvider) {
                                             MachineImageView.this.machineImageManager.deleteMachineImage(id.toString());
                                         } else {
                                             MachineImageView.this.machineImageManager.unregisterMachineImage(id.toString());
                                         }
                                     } catch (CloudProviderException e) {
                                         Util.diplayErrorMessageBox("Image delete failure", e);
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
 
         Button button = new Button("Refresh", new ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 MachineImageView.this.refresh();
             }
         });
         button.setIcon(new ThemeResource("img/refresh.png"));
         actionButtonHeader.addComponent(button);
 
         verticalLayout.addComponent(actionButtonHeader);
         verticalLayout.addComponent(this.machineImageTable = this.createMachineImageTable());
         verticalLayout.setExpandRatio(this.machineImageTable, 1.0f);
 
         this.setFirstComponent(verticalLayout);
         this.setSecondComponent(this.detailView = new MachineImageDetailView(this));
         this.setSplitPosition(60.0f);
 
     }
 
     void refresh() {
         this.machineImageTable.getContainerDataSource().removeAllItems();
         try {
             for (MachineImage machineImage : this.machineImageManager.getMachineImages()) {
                 this.images.addBean(new MachineImageBean(machineImage));
             }
         } catch (CloudProviderException e) {
             Util.diplayErrorMessageBox("Internal error", e);
         }
         this.valueChange(null);
     }
 
     void updateMachineImage(final MachineImage machineImage) {
         BeanItem<MachineImageBean> item = this.images.getItem(machineImage.getUuid());
         if (item != null) {
             MachineImageBean machineImageBean = item.getBean();
             machineImageBean.init(machineImage);
             item.getItemProperty("state").setValue(machineImageBean.getState());
             item.getItemProperty("name").setValue(machineImageBean.getName());
             if (this.detailView.getMachineImage().getUuid().equals(machineImage.getUuid())) {
                 this.detailView.update(machineImageBean);
             }
             this.valueChange(null);
         }
     }
 
     Table createMachineImageTable() {
         this.images = new BeanContainer<String, MachineImageBean>(MachineImageBean.class);
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
                 Object id = selectedMachineImageIds.iterator().next();
                 String state = (String) this.machineImageTable.getItem(id).getItemProperty("state").getValue();
                 this.deleteMachineImageButton.setEnabled(!state.endsWith("DELETING"));
                 this.detailView.update(this.images.getItem(id).getBean());
             } else {
                 this.detailView.hide();
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
             this.detailView.hide();
             this.deleteMachineImageButton.setEnabled(false);
         }
     }
 
     @Override
     public void attach() {
         super.attach();
         this.refresh();
     }
 
     MachineImageBean updateMachineImageAttribute(final MachineImageBean machineImageBean, final String attribute,
         final String value) {
         this.machineImageTable.getItem(machineImageBean.getId()).getItemProperty(attribute).setValue(value);
         return this.images.getItem(machineImageBean.getId()).getBean();
     }
 
     public static class MachineImageBean {
         MachineImage machineImage;
 
         String id;
 
         String name;
 
         String description;
 
         String state;
 
         String provider;
 
         String location;
 
         MachineImageBean(final MachineImage machineImage) {
             this.init(machineImage);
         }
 
         void init(final MachineImage machineImage) {
             this.machineImage = machineImage;
             this.id = machineImage.getUuid();
             this.name = machineImage.getName();
             this.description = machineImage.getDescription();
             this.state = machineImage.getState().toString();
             this.provider = this.providerFrom(machineImage);
             this.location = this.locationFrom(machineImage);
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
             ProviderMapping mapping = machineImage.getProviderMappings().get(0);
             return mapping.getProviderAccount().getCloudProvider().getDescription();
         }
 
         public String locationFrom(final MachineImage machineImage) {
             ProviderMapping mapping = machineImage.getProviderMappings().get(0);
             if (mapping.getProviderLocation() != null) {
                 return mapping.getProviderLocation().getIso3166_1();
             } else {
                 return "";
             }
         }
     }
 }

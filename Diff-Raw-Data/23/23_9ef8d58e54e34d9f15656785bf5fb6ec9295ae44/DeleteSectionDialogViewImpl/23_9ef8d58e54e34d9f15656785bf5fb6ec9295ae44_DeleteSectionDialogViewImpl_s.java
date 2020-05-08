 /**
  * Copyright (C) 2011  jtalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  * Also add information on how to contact you by electronic and paper mail.
  * Creation date: Apr 12, 2011 / 8:05:19 PM
  * The jtalks.org Project
  */
 package org.jtalks.poulpe.web.controller.section;
 
 import java.util.List;
 
 import org.jtalks.poulpe.model.entity.Section;
 import org.zkoss.zk.ui.Components;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.ext.AfterCompose;
 import org.zkoss.zul.Combobox;
 import org.zkoss.zul.Comboitem;
 import org.zkoss.zul.ComboitemRenderer;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Window;
 import org.zkoss.zul.api.Radiogroup;
 
 public class DeleteSectionDialogViewImpl extends Window implements
         DeleteSectionDialogView, AfterCompose {
 
     /**
      * 
      */
     private static final long serialVersionUID = -4999382692611273729L;
     private Radiogroup deleteMode;
     private Combobox selectedSection;
     private Section deletedSection;
     private DeleteSectionDialogPresenter presenter;
 
     public void setPresenter(DeleteSectionDialogPresenter presenter) {
         this.presenter = presenter;
     }
 
     @Override
     public void afterCompose() {
         Components.wireVariables(this, this);
         Components.addForwards(this, this);
 
         presenter.setView(this);
         presenter.initView();
         selectedSection.setItemRenderer(new ComboitemRenderer() {
 
             @Override
             public void render(Comboitem item, Object data) throws Exception {
                 Section section = (Section) data;
                 item.setLabel(section.getName());
                 item.setDescription(section.getDescription());
             }
         });
 
     }
 
     @Override
     public Section getDeleteSection() {
         return deletedSection;
     }
 
     @Override
     public Section getSelectedSection() {
         return (Section) selectedSection.getModel().getElementAt(
                 selectedSection.getSelectedIndex());
     }
 
     @Override
     public String getDeleteMode() {
         return deleteMode.getItemAtIndexApi(deleteMode.getSelectedIndex())
                 .getValue();
     }
 
     @Override
     public void closeDialog() {
         setVisible(false);
         Events.postEvent(new Event("onHideDialog", getDesktop().getPage("mainPage").getFellow("mainWindow")));
     }
 
     @Override
     public void showDialog() {
         setDefaultSection();
         deleteMode.setSelectedIndex(-1);
         setVisible(true);
     }
 
     private void setDefaultSection() {
         ListModelList model = (ListModelList) selectedSection.getModel();
         model.clearSelection();
         model.addSelection(model.get(0));
         selectedSection.setModel(model);
     }
     
     public void onOpenDeleteSectionDialog(Event event){
         deletedSection = (Section)event.getData();
         showDialog();
     }
 
     public void onClick$confirmButton() {
         presenter.delete();
     }
 
     public void onClick$rejectButton() {
         closeDialog();
     }
 
     @Override
     public void initSectionList(List<Section> selectableSections) {
         selectedSection.setModel(new ListModelList(selectableSections));
     }
 
 }

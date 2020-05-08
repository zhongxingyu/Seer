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
import org.zkoss.zul.Radio;
 import org.zkoss.zul.Window;
 import org.zkoss.zul.api.Radiogroup;
 
 /**
  * @author Bekrenev Dmitry
  * 
  *         This class implementation for Delete section dialog
  * */
 public class DeleteSectionDialogViewImpl extends Window implements
         DeleteSectionDialogView, AfterCompose {
 
     private static final long serialVersionUID = -4999382692611273729L;
     private Radiogroup deleteMode;
    private Radio removeAndMoveMode;
     private Combobox selectedSection;
     private Section deletedSection;
     private DeleteSectionDialogPresenter presenter;
 
     private static ComboitemRenderer itemRenderer = new ComboitemRenderer() {
 
         @Override
         public void render(Comboitem item, Object data) {
             Section section = (Section) data;
             item.setLabel(section.getName());
             item.setDescription(section.getDescription());
         }
     };
 
     /**
      * Set presenter
      * 
      * @param presenter
      *            DeleteSectionDialogPresenter instance
      * */
     public void setPresenter(DeleteSectionDialogPresenter presenter) {
         this.presenter = presenter;
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public void afterCompose() {
         Components.wireVariables(this, this);
         Components.addForwards(this, this);
 
         presenter.setView(this);
         selectedSection.setItemRenderer(itemRenderer);
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public Section getDeleteSection() {
         return deletedSection;
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public Section getSelectedSection() {
         return (Section) selectedSection.getModel().getElementAt(
                 selectedSection.getSelectedIndex());
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public String getDeleteMode() {
         return deleteMode.getItemAtIndexApi(deleteMode.getSelectedIndex())
                 .getValue();
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public void closeDialog() {
         setVisible(false);
         Events.postEvent(new Event("onHideDialog", getDesktop().getPage(
                 "mainPage").getFellow("mainWindow")));
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public void showDialog() {
         presenter.initView();
         setDefaultSection();
         deleteMode.setSelectedIndex(0);
         setVisible(true);
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public void initSectionList(List<Section> selectableSections) {
         selectableSections.remove(deletedSection);
         selectedSection.setModel(new ListModelList(selectableSections));
        selectedSection.setRawValue(null);
        if (!selectableSections.isEmpty()) {
            selectedSection.setDisabled(false);
            removeAndMoveMode.setDisabled(false);
        } else {
            selectedSection.setDisabled(true);
            removeAndMoveMode.setDisabled(true);
        }

     }
 
     /**
      * Choice default section in combobox
      * 
      * */
     private void setDefaultSection() {
         ListModelList model = (ListModelList) selectedSection.getModel();
         model.clearSelection();

        if (!model.isEmpty()) {
            model.addSelection(model.get(0));
        } else
            model.addSelection(null);

         selectedSection.setModel(model);
     }
 
     /**
      * This event cause show dialog
      * 
      * @param event
      *            information about event contain Section which will be deleted
      * */
     public void onOpenDeleteSectionDialog(Event event) {
         deletedSection = (Section) event.getData();
         showDialog();
     }
 
     /**
      * This event cause delete
      * */
     public void onClick$confirmButton() {
         presenter.delete();
     }
 
     /**
      * This event cause close dialog
      * */
     public void onClick$rejectButton() {
         closeDialog();
     }
 
 }

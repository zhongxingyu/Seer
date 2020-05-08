 /**
  * Copyright (C) 2011  JTalks.org Team
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
  */
 package org.jtalks.poulpe.web.controller.branch;
 
 import java.util.List;
 
 import org.jtalks.poulpe.model.entity.Branch;
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.DialogManagerImpl;
import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Components;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.ext.AfterCompose;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.ListitemRenderer;
 import org.zkoss.zul.Vbox;
 import org.zkoss.zul.Window;
 
 /**
  * This class is implementation view for show list branches
  * 
  * @author Bekrenev Dmitry
  * */
 
 public class BranchEditorViewImpl extends Window implements BranchEditorView,
         AfterCompose {
 
     private static final long serialVersionUID = -7175904766962858866L;
     private Listbox branchesList;
 
     /**
      * Important! If we are going to serialize/deserialize this class, this
      * field must be initialized explicitly during deserialization
      */
     private transient BranchEditorPresenter presenter;
 
     private ListModelList branchesListModel;
 
     /**
      * Use for render ListItem This class draws two labels for branch name and
      * description for change view attributes branch list item use css classes:
      * .branch-name and .branch-description
      * */
     private static ListitemRenderer branchRenderer = new ListitemRenderer() {
         @Override
         public void render(Listitem item, Object data) {
             Branch branch = (Branch) data;
 
             Listcell cell = new Listcell();
             Label name = new Label(branch.getName());
             Label desc = new Label(branch.getDescription());
             Vbox vbox = new Vbox();
 
             name.setSclass("branch-name");
             desc.setSclass("branch-description");
             name.setParent(vbox);
             desc.setParent(vbox);
             vbox.setParent(cell);
             cell.setParent(item);
             item.setId(String.valueOf(branch.getId()));
         }
     };
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public void afterCompose() {
         Components.addForwards(this, this);
         Components.wireVariables(this, this);
 
         branchesListModel = new ListModelList();
         branchesList.setModel(branchesListModel);
         branchesList.setItemRenderer(branchRenderer);
 
         presenter.setView(this);
         presenter.updateView();
     }
 
     /**
      * Set presenter
      * 
      * @see BranchEditorPresenter
      * @param presenter
      *            instance presenter for view
      * */
     public void setPresenter(BranchEditorPresenter presenter) {
         this.presenter = presenter;
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public void showBranches(List<Branch> branches) {
         branchesListModel.clear();
         branchesListModel.addAll(branches);
     }
 
     /**
      * Handle click on add button
      * */
     public void onClick$addBranchButton() {
        Component component = getDesktop().getPage("BranchDialog").getFellow("editWindow");
        component.setAttribute("presenter", presenter);
        Events.postEvent( new Event("onOpenAddDialog", component));       
     }
 
     /**
      * Handle click on del button
      * */
     public void onClick$delBranchButton() {
         if (branchesList.getSelectedCount() == 1) {
             Branch branch = getSelectedBranch();
             DialogManager dmanager = new DialogManagerImpl();
             dmanager.confirmDeletion(branch.getName(),
                     new DialogManager.Performable() {
 
                         @Override
                         public void execute() {
                             presenter.deleteBranch();
                             presenter.updateView();
                         }
                     });
         }
     }
 
     /**
      * Handle double click on branch list
      * */
     public void onDoubleClick$branchesList() {
         Events.postEvent( new Event("onOpenEditDialog", getDesktop().getPage("BranchDialog").getFellow("editWindow"), getSelectedBranch()));        
     }
 
     /**
      * Handle event when child dialog was hiding
      * */
     public void onHideDialog() {
         presenter.updateView();
     }
 
     /**
      * {@inheritDoc}
      * */
     @Override
     public Branch getSelectedBranch() {
         return (Branch) branchesListModel.get(branchesList.getSelectedIndex());
     }
 
 }

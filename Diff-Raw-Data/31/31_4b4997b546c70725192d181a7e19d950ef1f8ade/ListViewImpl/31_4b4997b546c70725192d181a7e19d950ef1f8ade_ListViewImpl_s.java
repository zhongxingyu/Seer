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
 package org.jtalks.poulpe.web.controller.component;
 
 import java.util.List;
 
 import org.jtalks.poulpe.model.entity.Component;
 import org.zkoss.zk.ui.Components;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.ext.AfterCompose;
 import org.zkoss.zkplus.databind.BindingListModelList;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.ListitemRenderer;
 import org.zkoss.zul.Window;
 
 /**
  * The class which manages actions and represents information about components displayed in
  * administrator panel.
  * @author Dmitriy Sukharev
  */
 public class ListViewImpl extends Window implements ListView, AfterCompose {
 
     private static final long serialVersionUID = -5891403019261284676L;
 
     /*
      * Important! If we are going to serialize/deserialize this class, this field must be
      * initialised explicitly during deserialization
      */
     private transient ListPresenter presenter;
     private ListModelList model;
     private Listbox listbox;
 
     /** {@inheritDoc} */
     @Override
     public void afterCompose() {
         Components.wireVariables(this, this);
         Components.addForwards(this, this);
         presenter.initView(this);
         listbox.setItemRenderer(new ListitemRenderer() {
             /** {@inheritDoc} */
             @Override
             public void render(Listitem arg0, Object arg1) {
                 final Component item = (Component) arg1;
                 Listcell cell = new Listcell();
                 Label nameLabel = new Label(item.getName());
                 cell.appendChild(nameLabel);
                 nameLabel.setSclass("boldstyle");
                 arg0.appendChild(cell);
                 arg0.appendChild(new Listcell(item.getDescription()));
                 arg0.appendChild(new Listcell(item.getComponentType().toString()));
                 arg0.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener() {
                     /** {@inheritDoc} */
                     @Override
                     public void onEvent(@SuppressWarnings("unused") Event event) {
                        ItemView editor = (ItemView) getDesktop().getPage("editCompPage").getFellow("editCompWindow");
                        editor.show(getSelectedItem().getId());
                     }
                 });
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void createModel(List<Component> list) {
         model = new BindingListModelList(list, true);
         listbox.setModel(model);
     }
 
     /**
      * Sets the presenter which is linked with this window.
      * @param presenter new value of the presenter which is linked with this window
      */
     public void setPresenter(ListPresenter presenter) {
         this.presenter = presenter;
     }
 
     /**
      * Tells to presenter that the window for adding new component must be shown.
      * @see ListPresenter
      */
     public void onClick$addCompButton() {
        ItemView editor = (ItemView) getDesktop().getPage("editCompPage").getFellow(
                 "editCompWindow");
        editor.show(null);
     }
 
     /**
      * Tells to presenter to delete selected component (it knows which one it is).
      * @see ListPresenter
      */
     public void onClick$delCompButton() {
         presenter.deleteComponent();
     }
 
     /** {@inheritDoc} */
     @Override
     public void updateList(List<Component> list) {
         model.clear();
         model.addAll(list);
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean hasSelectedItem() {
         return listbox.getSelectedIndex() != -1;
     }
 
     /** {@inheritDoc} */
     @Override
     public Component getSelectedItem() {
         return (Component) model.get(listbox.getSelectedIndex());
     }
 
     /** {@inheritDoc} */
     @Override
     public void updateList() {
         presenter.updateList();
     }
 
 }

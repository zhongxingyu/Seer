 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import de.aidger.view.UI;
 import de.aidger.view.tabs.DetailViewerTab;
 
 /**
  * This action replaces the current tab with the model viewer tab when user
  * cancelled his previous step in detail viewer tab.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class DetailViewerCloseAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public DetailViewerCloseAction() {
         putValue(Action.NAME, _("Close"));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         DetailViewerTab tab = (DetailViewerTab) UI.getInstance()
             .getCurrentTab();
 
        UI.getInstance().replaceCurrentTab(tab.getPredecessor());
     }
 }

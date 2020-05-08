 /*
  *  Copyright 2011 Yannick LOTH.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.googlecode.wicketelements.components.accordion.ui;
 
 
 import com.googlecode.jbp.common.requirements.Reqs;
 import com.googlecode.wicketelements.components.accordion.AccordionState;
 import com.googlecode.wicketelements.components.accordion.DefaultAccordionState;
 import com.googlecode.wicketelements.components.togglepane.TogglePaneStateEvent;
 import com.googlecode.wicketelements.components.togglepane.ui.TogglePane;
 import com.googlecode.wicketelements.library.behavior.AttributeAppenderFactory;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Accordion extends Panel implements AccordionState {
     private static final String TOGGLE_PANES_LIST_WICKET_ID = "we-accordion-togglePanes";
     private static final String TOGGLE_PANES_LIST_ELEMENT_WICKET_ID = "we-accordion-togglePane";
     private final List<TogglePane> togglePanes = new ArrayList<TogglePane>();
     private final AccordionState state;
 
     public void togglePaneEnabled(final TogglePaneStateEvent stateEventParam) {
         state.togglePaneEnabled(stateEventParam);
     }
 
     public void togglePaneDisabled(final TogglePaneStateEvent stateEventParam) {
         state.togglePaneDisabled(stateEventParam);
     }
 
     public void togglePaneCollapsed(final TogglePaneStateEvent stateEventParam) {
         state.togglePaneCollapsed(stateEventParam);
     }
 
     public void togglePaneExpanded(final TogglePaneStateEvent stateEventParam) {
         state.togglePaneExpanded(stateEventParam);
     }
 
     public boolean isMaximumOneTogglePaneExpanded() {
         return state.isMaximumOneTogglePaneExpanded();
     }
 
     public void setMaximumOneTogglePaneExpanded(final boolean maximumOneTogglePaneExpandedParam) {
         state.setMaximumOneTogglePaneExpanded(maximumOneTogglePaneExpandedParam);
     }
 
     public void disableAllTogglePanes() {
         state.disableAllTogglePanes();
     }
 
     public void enableAllTogglePanes() {
         state.enableAllTogglePanes();
     }
 
     public void collapseAllTogglePanes() {
         state.collapseAllTogglePanes();
     }
 
     public void expandAllTogglePanes() {
         state.expandAllTogglePanes();
     }
 
     public static final String getTogglePanesListElementWicketId() {
         return TOGGLE_PANES_LIST_ELEMENT_WICKET_ID;
     }
 
     public void addTogglePane(int index, final TogglePane togglePaneParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(togglePaneParam, "Toggle pane must not be null.");
         Reqs.PRE_COND.Logic.requireFalse(togglePanes.contains(togglePaneParam), "Toggle pane must not already be contained in the accordion.");
         Reqs.PRE_COND.Logic.requireTrue(index < togglePanes.size(), "Index must be smaller than value returned by togglePanesQuantity()");
         togglePanes.add(index, togglePaneParam);
        togglePaneParam.addComponentToUpdateOnAjaxRequest(this);
        togglePaneParam.addEventListener(this);
     }
 
     public void addTogglePane(final TogglePane togglePaneParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(togglePaneParam, "Toggle pane must not be null.");
         Reqs.PRE_COND.Logic.requireFalse(togglePanes.contains(togglePaneParam), "Toggle pane must not already be contained in the accordion.");
         togglePanes.add(togglePaneParam);
         togglePaneParam.addComponentToUpdateOnAjaxRequest(this);
         togglePaneParam.addEventListener(this);
     }
 
     public void removeTogglePane(final TogglePane togglePaneParam) {
         togglePanes.remove(togglePaneParam);
         togglePaneParam.removeComponentToUpdateOnAjaxRequest(this);
         togglePaneParam.removeEventListener(this);
     }
 
     public int togglePanesQuantity() {
         return togglePanes.size();
     }
 
     public final List<TogglePane> getTogglePanes() {
         return Collections.unmodifiableList(togglePanes);
     }
 
     public Accordion(final String idParam) {
         super(idParam);
         state = new DefaultAccordionState(this);
         setOutputMarkupId(true);
         final ListView<TogglePane> lv = new ListView<TogglePane>(TOGGLE_PANES_LIST_WICKET_ID, togglePanes) {
             @Override
             protected void populateItem(final ListItem<TogglePane> item) {
                 final TogglePane togglePane = item.getModelObject();
                 item.add(togglePane);
             }
         };
         add(lv);
         add(AttributeAppenderFactory.newAttributeAppenderForClass("we-accordion"));
     }
 
     @Override
     protected void onBeforeRender() {
         super.onBeforeRender();
     }
 }

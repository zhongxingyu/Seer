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
 package com.googlecode.wicketelements.components.accordion;
 
 import com.googlecode.jbp.common.requirements.Reqs;
 import com.googlecode.wicketelements.components.togglepane.TogglePane;
 import com.googlecode.wicketelements.components.togglepane.TogglePaneState;
 import com.googlecode.wicketelements.components.togglepane.TogglePaneStateEvent;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 class DefaultAccordionState implements AccordionState {
     boolean maximumOneTogglePaneExpanded;
     private final Accordion accordion;
     private final List<TogglePaneState> expandedTogglePaneStates = new ArrayList<TogglePaneState>();
 
     public DefaultAccordionState(final Accordion accordionParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(accordionParam, "Accordion must not be null.");
         accordion = accordionParam;
     }
 
     public boolean isMaximumOneTogglePaneExpanded() {
         return maximumOneTogglePaneExpanded;
     }
 
     public void setMaximumOneTogglePaneExpanded(final boolean maximumOneTogglePaneExpandedParam) {
         maximumOneTogglePaneExpanded = maximumOneTogglePaneExpandedParam;
     }
 
     public void disableAllTogglePanes() {
         for (final TogglePane current : accordion.getTogglePanes()) {
             if (current.isEnabled()) {
                 current.toggleEnableState();
             }
         }
     }
 
     public void enableAllTogglePanes() {
         for (final TogglePane current : accordion.getTogglePanes()) {
             if (current.isDisabled()) {
                 current.toggleEnableState();
             }
         }
     }
 
     public void collapseAllTogglePanes() {
         for (final TogglePane current : accordion.getTogglePanes()) {
             if (current.isExpanded()) {
                 current.toggleContent();
             }
         }
     }
 
     public void expandAllTogglePanes() {
         for (final TogglePane current : accordion.getTogglePanes()) {
             if (current.isCollapsed()) {
                 current.toggleContent();
             }
         }
     }
 
     public final void togglePaneEnabled(final TogglePaneStateEvent stateEventParam) {
         //Do nothing
     }
 
     public final void togglePaneDisabled(final TogglePaneStateEvent stateEventParam) {
         //Do nothing
     }
 
     public final void togglePaneCollapsed(final TogglePaneStateEvent stateEventParam) {
         //Do nothing
         expandedTogglePaneStates.remove(stateEventParam.getSource());
     }
 
     public final void togglePaneExpanded(final TogglePaneStateEvent stateEventParam) {
         //TODO check that the source corresponds to a toggle pane that actually belongs to this accordion
         final TogglePaneState state = stateEventParam.getSource();
         Reqs.PRE_COND.Logic.requireTrue(isTogglePaneBelongingToThisAccordion(state), "Expanded toggle pane does not belong to this accordion!");
         if (maximumOneTogglePaneExpanded) {
             Reqs.PRE_COND.Logic.requireTrue(expandedTogglePaneStates.size() <= 1, "There must be maximum one expanded pane.");
             final List<TogglePaneState> expandedTogglePanesStatesCopy = new ArrayList<TogglePaneState>(expandedTogglePaneStates);
             for (final TogglePaneState current : expandedTogglePanesStatesCopy) {
                 current.toggleContent(); //collapse all expanded panes
             }
            Reqs.POST_COND.Logic.requireTrue(expandedTogglePaneStates.size() == 1, "There must be exactly one expanded pane.");
         }
         expandedTogglePaneStates.add(stateEventParam.getSource());
     }
 
     private boolean isTogglePaneBelongingToThisAccordion(final TogglePaneState togglePaneStateParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(togglePaneStateParam, "TogglePaneState parameter must not be null.");
         boolean belongs = false;
         final Iterator<TogglePane> it = accordion.getTogglePanes().iterator();
         while (!belongs && it.hasNext()) {
             belongs = togglePaneStateParam.equals(it.next());
         }
         return belongs;
     }
 }

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
 package com.googlecode.wicketelements.components.togglepane;
 
 import com.googlecode.jbp.common.requirements.Reqs;
import com.googlecode.wicketelements.components.togglepane.DefaultTogglePaneState;
import com.googlecode.wicketelements.components.togglepane.TogglePaneState;
import com.googlecode.wicketelements.components.togglepane.TogglePaneStateListener;
 import com.googlecode.wicketelements.library.behavior.AttributeAppenderFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.panel.Panel;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Base class to implement a toggle pane.
  */
 public abstract class TogglePane extends Panel implements TogglePaneState {
     private static final String TITLE_WICKET_ID = "title";
     private static final String CONTENT_WICKET_ID = "content";
     private final TogglePaneState state;
     private Component titleComponent;
     private Component contentComponent;
     private Set<Component> componentsToUpdateOnAjaxRequest = new HashSet<Component>();
     private final WebMarkupContainer container;
 
     public void toggleContent() {
         state.toggleContent();
     }
 
     public void toggleEnableState() {
         state.toggleEnableState();
     }
 
     public boolean isCollapsed() {
         return state.isCollapsed();
     }
 
     public boolean isExpanded() {
         return state.isExpanded();
     }
 
     @Override
     public boolean isEnabled() {
         return state.isEnabled();
     }
 
     public boolean isDisabled() {
         return state.isDisabled();
     }
 
     public void addEventListener(final TogglePaneStateListener togglePaneStateListenerParam) {
         state.addEventListener(togglePaneStateListenerParam);
     }
 
     public void removeEventListener(final TogglePaneStateListener togglePaneStateListenerParam) {
         state.removeEventListener(togglePaneStateListenerParam);
     }
 
     /**
      * Add a component that should be refreshed when the toggle pane is updated using an AJAX request.
      * Use this if other components must be updated consequently to the update of the toggle panel.
      *
      * @param componentParam The component that must be added to the list of updated components.
      */
     public final void addComponentToUpdateOnAjaxRequest(final Component componentParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(componentParam, "Component to update on ajax request must not be null.");
         componentsToUpdateOnAjaxRequest.add(componentParam);
     }
 
     /**
      * Removes the component from the list of components that must be updated when the toggle pane is updated
      * with an AJAX request.
      *
      * @param componentParam
      */
     public final void removeComponentToUpdateOnAjaxRequest(final Component componentParam) {
         if (componentParam != null) {
             componentsToUpdateOnAjaxRequest.remove(componentParam);
         }
     }
 
     /**
      * Returns the title component.
      *
      * @return The title component.
      */
     public final Component getTitleComponent() {
         return titleComponent;
     }
 
     /**
      * Returns the content component.
      *
      * @return The content component.
      */
     public final Component getContentComponent() {
         return contentComponent;
     }
 
     /**
      * Implement this method to provide the title component of this toggle pane.
      *
      * @param titleWicketIdParam The Wicket id that the title component must have.
      * @return The created component.
      */
     protected abstract Component createTitleComponent(final String titleWicketIdParam);
 
     /**
      * Implement this method to provide the content component of this toggle pane.
      *
      * @param contentWicketIdParam The Wicket id that the content component must have.
      * @return The created component.
      */
     protected abstract Component createContentComponent(final String contentWicketIdParam);
 
     /**
      * Updates all components that need to be updated in case of an AJAX request.
      *
      * @param targetParam
      */
     public final void handleAjaxOnClickEvent(final AjaxRequestTarget targetParam) {
         //update state of togglePane
         handleOnClickEvent();
         //update components that must be updated because of the event
         updateComponentsForAjaxRequest(targetParam);
     }
 
     /**
      * Similar to {@code handleAjaxOnClickEvent(AjaxRequestTarget t)} but should be used when the request is not an AJAX request.
      */
     public final void handleOnClickEvent() {
         //update state of togglePane
         toggleContent();
     }
 
     private final void updateComponentsForAjaxRequest(final AjaxRequestTarget targetParam) {
         for (final Component current : componentsToUpdateOnAjaxRequest) {
             targetParam.addComponent(current);
         }
         targetParam.addComponent(this);
     }
 
     /**
      * Creates a toggle pane with the specified Wicket id.
      *
      * @param idParam The toggle pane's Wicket id.
      */
     public TogglePane(final String idParam) {
         super(idParam);
         titleComponent = createTitleComponent(TITLE_WICKET_ID);
         contentComponent = createContentComponent(CONTENT_WICKET_ID);
         container = new WebMarkupContainer("togglePane-container");
         container.add(titleComponent);
         container.add(contentComponent);
         contentComponent.setVisible(false);
         setOutputMarkupId(true);
         state = new DefaultTogglePaneState(this);
         add(container);
     }
 
     @Override
     protected void onBeforeRender() {
         super.onBeforeRender();
         if (isExpanded()) {
            container.add(AttributeAppenderFactory.newAttributeAppenderForClass("we-togglepane-expanded"));
         }
     }
 }

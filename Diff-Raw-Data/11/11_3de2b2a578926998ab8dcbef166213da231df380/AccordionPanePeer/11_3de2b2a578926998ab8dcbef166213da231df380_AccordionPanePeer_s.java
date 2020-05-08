 /* 
  * This file is part of the Echo2 Extras Project.
  * Copyright (C) 2005-2006 NextApp, Inc.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package nextapp.echo2.extras.webcontainer;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.w3c.dom.Element;
 
 import nextapp.echo2.app.Border;
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Component;
 import nextapp.echo2.app.Extent;
 import nextapp.echo2.app.FillImage;
 import nextapp.echo2.app.ImageReference;
 import nextapp.echo2.app.Insets;
 import nextapp.echo2.app.Pane;
 import nextapp.echo2.app.update.ServerComponentUpdate;
 import nextapp.echo2.extras.app.AccordionPane;
 import nextapp.echo2.extras.app.layout.AccordionPaneLayoutData;
 import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
 import nextapp.echo2.webcontainer.ContainerInstance;
 import nextapp.echo2.webcontainer.LazyRenderContainer;
 import nextapp.echo2.webcontainer.PartialUpdateManager;
 import nextapp.echo2.webcontainer.PartialUpdateParticipant;
 import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
 import nextapp.echo2.webcontainer.RenderContext;
 import nextapp.echo2.webcontainer.RenderState;
 import nextapp.echo2.webcontainer.SynchronizePeerFactory;
 import nextapp.echo2.webcontainer.image.ImageRenderSupport;
 import nextapp.echo2.webcontainer.propertyrender.BorderRender;
 import nextapp.echo2.webcontainer.propertyrender.ColorRender;
 import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
 import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
 import nextapp.echo2.webrender.ServerMessage;
 import nextapp.echo2.webrender.Service;
 import nextapp.echo2.webrender.WebRenderServlet;
 import nextapp.echo2.webrender.output.CssStyle;
 import nextapp.echo2.webrender.servermessage.DomUpdate;
 import nextapp.echo2.webrender.service.JavaScriptService;
 
 public class AccordionPanePeer 
 implements ComponentSynchronizePeer, ImageRenderSupport, LazyRenderContainer, PropertyUpdateProcessor {
 
     private static final String PROPERTY_ACTIVE_TAB = "activeTab";
     private static final String IMAGE_ID_TAB_BACKGROUND = "tabBackground";
     private static final String IMAGE_ID_TAB_ROLLOVER_BACKGROUND = "tabRolloverBackground";
     
     /**
      * Component property to enabled/disable lazy rendering of child tabs.
      * Default value is interpreted to be true.
      */
     public static final String PROPERTY_LAZY_RENDER_ENABLED 
             = "nextapp.echo2.extras.webcontainer.AccordionPanePeer.lazyRenderEnabled";
 
     /**
      * Service to provide supporting JavaScript library.
      */
     public static final Service ACCORDION_PANE_SERVICE = JavaScriptService.forResource("Echo2Extras.AccordionPane",
             "/nextapp/echo2/extras/webcontainer/resource/js/AccordionPane.js");
 
     static {
         WebRenderServlet.getServiceRegistry().add(ACCORDION_PANE_SERVICE);
     }
     
     /**
      * <code>RenderState</code> implementation to store data on whether child
      * components have been lazily rendered to client.
      */
     private static class AccordionPaneRenderState implements RenderState {
         
         /**
          * Render id of currently active tab.
          */
         private String activeTabId;
         
         /**
          * Set of rendered child components.
          */
         private Set renderedChildren = new HashSet();
     }
 
     /**
      * <code>PartialUpdateParticipant</code> to update active tab.
      */
     private PartialUpdateParticipant activeTabUpdateParticipant = new PartialUpdateParticipant() {
     
         /**
          * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#renderProperty(nextapp.echo2.webcontainer.RenderContext,
          *       nextapp.echo2.app.update.ServerComponentUpdate)
          */
         public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
             renderSetActiveTab(rc, update, (AccordionPane) update.getParent());
         }
     
         /**
          * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#canRenderProperty(nextapp.echo2.webcontainer.RenderContext, 
          *      nextapp.echo2.app.update.ServerComponentUpdate)
          */
         public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
             return true;
         }
     };
     
     /**
      * The <code>PartialUpdateManager</code> for this synchronization peer.
      */
     private PartialUpdateManager partialUpdateManager;
     
     /**
      * Default constructor.
      */
     public AccordionPanePeer() {
         partialUpdateManager = new PartialUpdateManager();
         partialUpdateManager.add(AccordionPane.ACTIVE_TAB_INDEX_CHANGED_PROPERTY, activeTabUpdateParticipant);
     }
 
     /**
      * Performs configuration tasks related to the active tab of a <code>AccordionPane</code>.
      * 
      * @param ci the relevant <code>ContainerInstance</code>
      * @param accordionPane the rendering <code>AccordionPane</code>
      * @return true if the active tab requires rendering
      */
     private boolean configureActiveTab(ContainerInstance ci, AccordionPane accordionPane) {
         AccordionPaneRenderState renderState = (AccordionPaneRenderState) ci.getRenderState(accordionPane);
         
         int componentCount = accordionPane.getVisibleComponentCount();
         int activeTabIndex = accordionPane.getActiveTabIndex();
         
         // Retrieve currently active component according to AccordionPane.activeTabIndex.
         Component activeTab = (activeTabIndex >= 0 && activeTabIndex < componentCount) 
                 ? accordionPane.getComponent(activeTabIndex) : null;
 
         // If AccordionPane component does not specify a valid active tab, query render state.
         if (activeTab == null && renderState.activeTabId != null) {
             activeTab = getChildByRenderId(accordionPane, renderState.activeTabId);
         }
     
         // If neither component nor render state have active tab information, pick a tab to be active.
         if (activeTab == null) {
             if (componentCount == 0) {
                 // No tabs available, return false indicating that active tab DOES not require rendering.
                 return false;
             }
             
             if (accordionPane.getActiveTabIndex() == -1) {
                 // AccordionPane component state indicates no tab selected: select first tab.
                 activeTab = accordionPane.getVisibleComponent(0);
             } else {
                 // AccordionPane component state indicates a now-defunct tab was selected: select last tab. 
                 activeTab = accordionPane.getVisibleComponent(componentCount - 1);
             }
         }
 
         // Store active tab in render state.
         renderState.activeTabId = activeTab.getRenderId();
         
         if (isLazyRenderEnabled(accordionPane)) {
             // Determine if active tab is rendered or not.  If it is not rendered, mark its state rendered and
             // return true to indicate that it should be rendered.
             if (!isRendered(ci, accordionPane, activeTab)) {
                 setRendered(ci, accordionPane, activeTab);
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
      */
     public String getContainerId(Component child) {
         return ContainerInstance.getElementId(child.getParent()) + "_content_" + child.getRenderId();
     }
 
     /**
      * Returns the child component with the specified render id.
      * 
      * @param accordionPane the <code>AccordionPane</code>
      * @param renderId the render identifier
      * @return the child component, or null if no child exists with the
      *         specified id.
      */
     private Component getChildByRenderId(AccordionPane accordionPane, String renderId) {
         Component[] children = accordionPane.getVisibleComponents();
         for (int i = 0; i < children.length; ++i) {
             if (children[i].getRenderId().equals(renderId)) {
                 return children[i];
             }
         }
         return null;
     }
     
     /**
      * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
      */
     public ImageReference getImage(Component component, String imageId) {
         if (IMAGE_ID_TAB_BACKGROUND.equals(imageId)) {
             FillImage fillImage = (FillImage) component.getRenderProperty(AccordionPane.PROPERTY_TAB_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_TAB_ROLLOVER_BACKGROUND.equals(imageId)) {
             FillImage fillImage = (FillImage) component.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else {
             return null;
         }
     }
     
     /**
      * Determines if a <code>AccordionPane</code> should be lazy-rendered.
      * 
      * @param accordionPane the <code>AccordionPane</code> to query
      * @return true if lazy-rendering should be enabled
      */
     private boolean isLazyRenderEnabled(AccordionPane accordionPane) {
         Boolean lazyRenderEnabled = (Boolean) accordionPane.getRenderProperty(PROPERTY_LAZY_RENDER_ENABLED);
         return lazyRenderEnabled == null ? false : lazyRenderEnabled.booleanValue();
     }
     
     /**
      * @see nextapp.echo2.webcontainer.LazyRenderContainer#isRendered(nextapp.echo2.webcontainer.ContainerInstance, 
      *      nextapp.echo2.app.Component, nextapp.echo2.app.Component)
      */
     public boolean isRendered(ContainerInstance ci, Component parent, Component child) {
         AccordionPaneRenderState renderState = (AccordionPaneRenderState) ci.getRenderState(parent);
         if (renderState == null ) {
             // Entire component has not been rendered, thus child has not been rendered.
             return false;
         }
         
         return renderState.renderedChildren.contains(child);
     }
 
     /**
      * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(nextapp.echo2.webcontainer.ContainerInstance, 
      *      nextapp.echo2.app.Component, org.w3c.dom.Element)
      */
     public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
         String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
         if (PROPERTY_ACTIVE_TAB.equals(propertyName)) {
             String propertyValue = propertyElement.getAttribute("value");
             Component[] children = component.getVisibleComponents();
             for (int i = 0; i < children.length; ++i) {
                 if (children[i].getRenderId().equals(propertyValue)) {
                     ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                             AccordionPane.INPUT_TAB_INDEX, new Integer(i));
                     return;
                 }
             }
         }
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
      *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
      */
     public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
         serverMessage.addLibrary(ACCORDION_PANE_SERVICE.getId());
         AccordionPane accordionPane = (AccordionPane) component;
         
         ContainerInstance ci = rc.getContainerInstance();
         resetRenderState(ci, accordionPane);
 
         configureActiveTab(ci, accordionPane);
         
         renderInitDirective(rc, accordionPane, targetId);
         Component[] children = accordionPane.getVisibleComponents();
         for (int i = 0; i < children.length; ++i) {
             renderAddTabDirective(rc, update, accordionPane, children[i]);
         }
         boolean lazyRenderEnabled = isLazyRenderEnabled(accordionPane);
         for (int i = 0; i < children.length; ++i) {
             if (!lazyRenderEnabled || isRendered(ci, accordionPane, children[i])) {
                 renderChild(rc, update, accordionPane, children[i]);
             }
         }
         renderRedrawDirective(rc, accordionPane);
     }
 
     private void renderAddChildren(RenderContext rc, ServerComponentUpdate update, boolean activeTabRenderRequired) {
         AccordionPane accordionPane = (AccordionPane) update.getParent();
         ContainerInstance ci = rc.getContainerInstance();
         
         Component activeTab = null;
         if (activeTabRenderRequired) {
             AccordionPaneRenderState renderState = (AccordionPaneRenderState) ci.getRenderState(accordionPane);
             activeTab = getChildByRenderId(accordionPane, renderState.activeTabId);
             if (activeTab == null) {
                 activeTabRenderRequired = false;
             }
         }
         
         if (update.hasAddedChildren()) {
             Component[] addedChildren = update.getAddedChildren();
             Component[] children = accordionPane.getVisibleComponents();
             
             // Iterating through arrays and checking for reference equality is used here (versus loading daddedChildren
             // into a hashtable) because we'll be dealing with very small array lengths, typically less than 10.
             for (int i = 0; i < children.length; ++i) {
                 for (int j = 0; j < addedChildren.length; ++j) {
                     if (children[i] == addedChildren[j]) {
                         renderAddTabDirective(rc, update, accordionPane, children[i]);
                         break;
                     }
                 }
             }
 
             boolean lazyRenderEnabled = isLazyRenderEnabled(accordionPane);
             
             // Add children.
             for (int i = 0; i < addedChildren.length; ++i) {
                 if (!lazyRenderEnabled || isRendered(ci, accordionPane, addedChildren[i])) {
                     renderChild(rc, update, accordionPane, addedChildren[i]);
                     if (addedChildren[i] == activeTab) {
                         activeTabRenderRequired = false;
                     }
                 }
             }
         }
         
         if (activeTabRenderRequired) {
             renderChild(rc, update, accordionPane, activeTab);
         }
     }
 
     private void renderAddTabDirective(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane, 
             Component child) {
         ContainerInstance ci = rc.getContainerInstance();
         boolean rendered = !isLazyRenderEnabled(accordionPane) || isRendered(ci, accordionPane, child);
 
         AccordionPaneLayoutData layoutData = (AccordionPaneLayoutData) child.getLayoutData();
         String elementId = ContainerInstance.getElementId(accordionPane);
         Element addPartElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                 "ExtrasAccordionPane.MessageProcessor", "add-tab");
         addPartElement.setAttribute("eid", elementId);
         addPartElement.setAttribute("tab-id", child.getRenderId());
         addPartElement.setAttribute("tab-index", Integer.toString(accordionPane.indexOf(child)));
         if (rendered) {
             addPartElement.setAttribute("rendered", "true");
         }
         if  (child instanceof Pane) {
             addPartElement.setAttribute("pane", "true");
         }
         if (layoutData != null) {
             if (layoutData.getTitle() != null) {
                 addPartElement.setAttribute("title", layoutData.getTitle()); 
             }
         }
     }
     
     /**
      * Renders an individual child component of the <code>AccordionPane</code>.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> being performed
      * @param child The child <code>Component</code> to be rendered
      */
     private void renderChild(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane, Component child) {
         ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
         syncPeer.renderAdd(rc, update, getContainerId(child), child);
     }
 
     /**
      * Renders a create directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param accordionPane the <code>AccordionPane</code> being rendered
      * @param targetId the id of the container element
      */
     private void renderInitDirective(RenderContext rc, AccordionPane accordionPane, String targetId) {
         String elementId = ContainerInstance.getElementId(accordionPane);
         ServerMessage serverMessage = rc.getServerMessage();
         Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "ExtrasAccordionPane.MessageProcessor");
         Element initElement = serverMessage.getDocument().createElement("init");
         initElement.setAttribute("container-eid", targetId);
         initElement.setAttribute("eid", elementId);
         
         if (!accordionPane.isRenderEnabled()) {
             initElement.setAttribute("enabled", "false");
         }
         Color background = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_BACKGROUND);
         if (background != null) {
             initElement.setAttribute("background", ColorRender.renderCssAttributeValue(background));
         }
         Color foreground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_FOREGROUND);
         if (foreground != null) {
             initElement.setAttribute("foreground", ColorRender.renderCssAttributeValue(foreground));
         }
         Insets defaultContentInsets = (Insets) accordionPane.getRenderProperty(AccordionPane.PROPERTY_DEFAULT_CONTENT_INSETS);
         if (defaultContentInsets != null) {
             initElement.setAttribute("default-content-insets", InsetsRender.renderCssAttributeValue(defaultContentInsets));
         }
         
         Color tabBackground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_BACKGROUND);
         if (tabBackground != null) {
             initElement.setAttribute("tab-background", ColorRender.renderCssAttributeValue(tabBackground));
         }
         FillImage tabBackgroundImage = (FillImage) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_BACKGROUND_IMAGE);
         if (tabBackgroundImage != null) {
             CssStyle backgroundImageCssStyle = new CssStyle();
             FillImageRender.renderToStyle(backgroundImageCssStyle, rc, this, accordionPane, IMAGE_ID_TAB_BACKGROUND, 
                     tabBackgroundImage, 0);
             initElement.setAttribute("tab-background-image", backgroundImageCssStyle.renderInline());
         }
         Border tabBorder = (Border) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_BORDER);
         if (tabBorder != null) {
             if (tabBorder.getColor() != null) {
                 initElement.setAttribute("tab-border-color", ColorRender.renderCssAttributeValue(tabBorder.getColor()));
             }
             if (tabBorder.getSize() != null && tabBorder.getSize().getUnits() == Extent.PX) {
                 initElement.setAttribute("tab-border-size", Integer.toString(tabBorder.getSize().getValue()));
             }
             initElement.setAttribute("tab-border-style", BorderRender.getStyleValue(tabBorder.getStyle())); 
         }
         Color tabForeground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_FOREGROUND);
         if (tabForeground != null) {
             initElement.setAttribute("tab-foreground", ColorRender.renderCssAttributeValue(tabForeground));
         }
         Insets tabInsets = (Insets) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_INSETS);
         if (tabInsets != null) {
             initElement.setAttribute("tab-insets", InsetsRender.renderCssAttributeValue(tabInsets));
         }
         Boolean tabRolloverEnabled = (Boolean) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_ENABLED);
         if (tabRolloverEnabled == null || tabRolloverEnabled.booleanValue()) {
             initElement.setAttribute("tab-rollover-enabled","true");
             Color tabRolloverBackground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_BACKGROUND);
             if (tabRolloverBackground != null) {
                 initElement.setAttribute("tab-rollover-background", ColorRender.renderCssAttributeValue(tabRolloverBackground));
             }
             FillImage tabRolloverBackgroundImage = (FillImage) accordionPane.getRenderProperty(
                     AccordionPane.PROPERTY_TAB_ROLLOVER_BACKGROUND_IMAGE);
             if (tabRolloverBackgroundImage != null) {
                 CssStyle rolloverBackgroundImageCssStyle = new CssStyle();
                 FillImageRender.renderToStyle(rolloverBackgroundImageCssStyle, rc, this, accordionPane, 
                         IMAGE_ID_TAB_ROLLOVER_BACKGROUND, tabRolloverBackgroundImage, 0);
                 initElement.setAttribute("tab-rollover-background-image", rolloverBackgroundImageCssStyle.renderInline());
             }
             Border tabRolloverBorder = (Border) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_BORDER);
             if (tabRolloverBorder != null) {
                 if (tabRolloverBorder.getColor() != null) {
                     initElement.setAttribute("tab-rollover-border-color", 
                             ColorRender.renderCssAttributeValue(tabRolloverBorder.getColor()));
                 }
                 initElement.setAttribute("tab-rollover-border-style", BorderRender.getStyleValue(tabRolloverBorder.getStyle())); 
             }
             Color tabRolloverForeground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_FOREGROUND);
             if (tabRolloverForeground != null) {
                 initElement.setAttribute("tab-rollover-foreground", ColorRender.renderCssAttributeValue(tabRolloverForeground));
             }
         }
 
         int activeTabIndex = accordionPane.getActiveTabIndex();
         if (activeTabIndex != -1 && activeTabIndex < accordionPane.getVisibleComponentCount()) {
             initElement.setAttribute("active-tab", accordionPane.getVisibleComponent(activeTabIndex).getRenderId());
         }
 
         partElement.appendChild(initElement);
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
      */
     public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
         serverMessage.addLibrary(ACCORDION_PANE_SERVICE.getId());
         renderDisposeDirective(rc, (AccordionPane) component);
     }
 
     /**
      * Renders a dispose directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param accordionPane the <code>AccordionPane</code> being rendered
      */
     private void renderDisposeDirective(RenderContext rc, AccordionPane accordionPane) {
         String elementId = ContainerInstance.getElementId(accordionPane);
         ServerMessage serverMessage = rc.getServerMessage();
         Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                 "ExtrasAccordionPane.MessageProcessor", "dispose");
         initElement.setAttribute("eid", elementId);
     }
     
     /**
      * Renders an update directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param accordionPane the <code>AccordionPane</code> being rendered
      */
     private void renderRedrawDirective(RenderContext rc, AccordionPane accordionPane) {
         String elementId = ContainerInstance.getElementId(accordionPane);
         ServerMessage serverMessage = rc.getServerMessage();
         Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "ExtrasAccordionPane.MessageProcessor");
         Element initElement = serverMessage.getDocument().createElement("redraw");
         initElement.setAttribute("eid", elementId);
 
         partElement.appendChild(initElement);
     }
     
     private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
         AccordionPane accordionPane = (AccordionPane) update.getParent();
         Component[] removedChildren = update.getRemovedChildren();
         for (int i = 0; i < removedChildren.length; ++i) {
             renderRemoveTabDirective(rc, update, accordionPane, removedChildren[i]);
         }
     }
     
     private void renderRemoveTabDirective(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane, 
             Component child) {
         String elementId = ContainerInstance.getElementId(accordionPane);
         Element removePartElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_REMOVE, 
                 "ExtrasAccordionPane.MessageProcessor", "remove-tab");
         removePartElement.setAttribute("eid", elementId);
         removePartElement.setAttribute("tab-id", child.getRenderId());
     }
     
     /**
      * Updates the active tab of a pre-existing <code>AccordionPane</code> on the
      * client.  This method will render the tab's component hierarchy to the
      * client if it has not yet been loaded, and then render a set-active-tab
      * directive to select the tab. 
      *
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> describing the 
      *        change
      * @param accordionPane the <code>AccordionPane</code> being updated
      */
     private void renderSetActiveTab(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane) {
         ContainerInstance ci = rc.getContainerInstance();
 
         boolean activeTabRenderRequired = configureActiveTab(ci, accordionPane);
 
         Component activeTab = null;
         if (activeTabRenderRequired) {
             AccordionPaneRenderState renderState = (AccordionPaneRenderState) ci.getRenderState(accordionPane);
             activeTab = getChildByRenderId(accordionPane, renderState.activeTabId);
             if (activeTab != null) {
                 renderChild(rc, update, accordionPane, activeTab);
             }
         }
         
         renderSetActiveTabDirective(rc, update, accordionPane);
     }
     
     /**
      * Renders a directive to the <code>ServerMessage</code> to set the
      * active tab of a pre-exisiting <code>AccordionPane</code>
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> describing the 
      *        change
      * @param accordionPane the <code>AccordionPane</code> being updated
      */
     private void renderSetActiveTabDirective(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane) {
         Component activeTab = null;
         AccordionPaneRenderState renderState = (AccordionPaneRenderState) rc.getContainerInstance().getRenderState(accordionPane);
         activeTab = getChildByRenderId(accordionPane, renderState.activeTabId);
         if (activeTab == null) {
             return;
         }
         
         String elementId = ContainerInstance.getElementId(accordionPane);
         Element setActiveTabElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                 "ExtrasAccordionPane.MessageProcessor", "set-active-tab");
         setActiveTabElement.setAttribute("eid", elementId);
         setActiveTabElement.setAttribute("active-tab", activeTab.getRenderId());
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
      */
     public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
         ContainerInstance ci = rc.getContainerInstance();
         AccordionPane accordionPane = (AccordionPane) update.getParent();
         
         // Determine if fully replacing the component is required.
         boolean fullReplace = false;
         if (update.hasUpdatedLayoutDataChildren()) {
             // TODO: Perform fractional update on LayoutData change instead of full replace.
             fullReplace = true;
         } else if (update.hasUpdatedProperties()) {
             if (!partialUpdateManager.canProcess(rc, update)) {
                 fullReplace = true;
             }
         }
         
         if (fullReplace) {
             // Perform full update.
             renderDisposeDirective(rc, accordionPane);
             DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(accordionPane));
             renderAdd(rc, update, targetId, accordionPane);
         } else {
             // Perform incremental updates.
             if (update.hasRemovedChildren() || update.hasAddedChildren()) {
                 
                 boolean activeTabRenderRequired = configureActiveTab(ci, accordionPane);
                 if (update.hasRemovedChildren()) {
                     renderRemoveChildren(rc, update);
                 }
                 if (update.hasAddedChildren() || activeTabRenderRequired) {
                     renderAddChildren(rc, update, activeTabRenderRequired);
                     
                 }
                 renderSetActiveTabDirective(rc, update, accordionPane);
                 renderRedrawDirective(rc, accordionPane);
             }
             
             if (update.hasUpdatedProperties()) {
                 partialUpdateManager.process(rc, update);
             }
         }
         
         return fullReplace;
     }
 
     /**
      * Resets the <code>RenderState</code> of an <code>AccordionPane</code>
      * in the <code>ContainerInstance</code>. Invoked when an
      * <code>AccordionPane</code> is initially rendered to the client.
      * 
      * @param ci
      *            the relevant <code>ContainerInstance</code>
      * @param accordionPane
      *            the <code>AccordionPane</code> being rendered
      */
     private void resetRenderState(ContainerInstance ci, AccordionPane accordionPane) {
         AccordionPaneRenderState renderState = new AccordionPaneRenderState();
         ci.setRenderState(accordionPane, renderState);
     }
     
     /**
      * Sets a flag in the <code>RenderState</code> to indicate that a particular
      * tab of an <code>AccordionPane</code> has been/is being rendered to the 
      * client.  This method is used to facilitate lazy-rendering, ensuring each
      * tab of a <code>AccordionPane</code> is rendered tot he client only once.
      * 
      * @param ci the relevant <code>ContainerInstance</code>
      * @param accordionPane the <code>AccordionPane</code> being rendered
      * @param child the child tab component
      */
     private void setRendered(ContainerInstance ci, AccordionPane accordionPane, Component child) {
         AccordionPaneRenderState renderState = (AccordionPaneRenderState) ci.getRenderState(accordionPane);
         if (renderState == null ) {
             renderState = new AccordionPaneRenderState();
             ci.setRenderState(accordionPane, renderState);
         }
         renderState.renderedChildren.add(child);
     }
 }

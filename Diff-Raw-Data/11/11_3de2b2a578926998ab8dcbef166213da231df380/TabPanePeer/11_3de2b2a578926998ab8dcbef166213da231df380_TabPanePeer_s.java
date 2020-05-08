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
 import nextapp.echo2.app.Font;
 import nextapp.echo2.app.ImageReference;
 import nextapp.echo2.app.Insets;
 import nextapp.echo2.app.Pane;
 import nextapp.echo2.app.update.ServerComponentUpdate;
 import nextapp.echo2.extras.app.TabPane;
 import nextapp.echo2.extras.app.layout.TabPaneLayoutData;
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
 import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
 import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
 import nextapp.echo2.webcontainer.propertyrender.FontRender;
 import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
 import nextapp.echo2.webrender.ServerMessage;
 import nextapp.echo2.webrender.Service;
 import nextapp.echo2.webrender.WebRenderServlet;
 import nextapp.echo2.webrender.output.CssStyle;
 import nextapp.echo2.webrender.servermessage.DomUpdate;
 import nextapp.echo2.webrender.service.JavaScriptService;
 
 /**
  * <code>ComponentSynchronizePeer</code> implementation for synchronizing
  * <code>TabPane</code> components.
  */
 public class TabPanePeer 
 implements ComponentSynchronizePeer, ImageRenderSupport, LazyRenderContainer, PropertyUpdateProcessor {
 
     private static final String IMAGE_ID_TAB_ACTIVE_BACKGROUND = "tabActiveBackground";
     private static final String IMAGE_ID_TAB_INACTIVE_BACKGROUND = "tabInactiveBackground";
     
     private static final String PROPERTY_ACTIVE_TAB = "activeTab";
     
     /**
      * Component property to enabled/disable lazy rendering of child tabs.
      * Default value is interpreted to be true.
      */
     public static final String PROPERTY_LAZY_RENDER_ENABLED = "nextapp.echo2.extras.webcontainer.TabPanePeer.lazyRenderEnabled";
     
     /**
      * Service to provide supporting JavaScript library.
      */
     public static final Service TAB_PANE_SERVICE = JavaScriptService.forResource("Echo2Extras.TabPane",
             "/nextapp/echo2/extras/webcontainer/resource/js/TabPane.js");
     
     static {
         WebRenderServlet.getServiceRegistry().add(TAB_PANE_SERVICE);
     }
     
     /**
      * <code>RenderState</code> implementation to store data on whether child
      * components have been lazily rendered to client.
      */
     private static class TabPaneRenderState implements RenderState {
         
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
             renderSetActiveTab(rc, update, (TabPane) update.getParent()); 
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
     public TabPanePeer() {
         partialUpdateManager = new PartialUpdateManager();
         partialUpdateManager.add(TabPane.ACTIVE_TAB_INDEX_CHANGED_PROPERTY, activeTabUpdateParticipant);
     }
     
     private String getRenderedActiveTabId(ContainerInstance ci, TabPane tabPane) {
         TabPaneRenderState renderState = (TabPaneRenderState) ci.getRenderState(tabPane);
         return renderState.activeTabId;
     }
     
     /**
      * Performs configuration tasks related to the active tab of a <code>TabPane</code>.
      * 
      * @param ci the relevant <code>ContainerInstance</code>
      * @param tabPane the rendering <code>TabPane</code>
      * @return true if the active tab requires rendering
      */
     private boolean configureActiveTab(ContainerInstance ci, TabPane tabPane) {
         TabPaneRenderState renderState = (TabPaneRenderState) ci.getRenderState(tabPane);
         
         int componentCount = tabPane.getVisibleComponentCount();
         int activeTabIndex = tabPane.getActiveTabIndex();
         
         // Retrieve currently active component according to TabPane.activeTabIndex.
         Component activeTab = (activeTabIndex >= 0 && activeTabIndex < componentCount) 
                 ? tabPane.getComponent(activeTabIndex) : null;
 
         // If TabPane component does not specify a valid active tab, query render state.
         if (activeTab == null && renderState.activeTabId != null) {
             activeTab = getChildByRenderId(tabPane, renderState.activeTabId);
         }
     
         // If neither component nor render state have active tab information, pick a tab to be active.
         if (activeTab == null) {
             if (componentCount == 0) {
                 // No tabs available, return false indicating that active tab DOES not require rendering.
                 return false;
             }
             
             if (tabPane.getActiveTabIndex() == -1) {
                 // TabPane component state indicates no tab selected: select first tab.
                 activeTab = tabPane.getVisibleComponent(0);
             } else {
                 // TabPane component state indicates a now-defunct tab was selected: select last tab. 
                 activeTab = tabPane.getVisibleComponent(componentCount - 1);
             }
         }
 
         // Store active tab in render state.
         renderState.activeTabId = activeTab.getRenderId();
         
         if (isLazyRenderEnabled(tabPane)) {
             // Determine if active tab is rendered or not.  If it is not rendered, mark its state rendered and
             // return true to indicate that it should be rendered.
             if (!isRendered(ci, tabPane, activeTab)) {
                 setRendered(ci, tabPane, activeTab);
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
      * @param tabPane the <code>TabPane</code>
      * @param renderId the render identifier
      * @return the child component, or null if no child exists with the
      *         specified id.
      */
     private Component getChildByRenderId(TabPane tabPane, String renderId) {
         Component[] children = tabPane.getVisibleComponents();
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
         if (IMAGE_ID_TAB_ACTIVE_BACKGROUND.equals(imageId)) {
             FillImage backgroundImage = (FillImage) component.getRenderProperty(TabPane.PROPERTY_TAB_ACTIVE_BACKGROUND_IMAGE);
             return backgroundImage == null ? null : backgroundImage.getImage();
         } else if (IMAGE_ID_TAB_INACTIVE_BACKGROUND.equals(imageId)) {
             FillImage backgroundImage = (FillImage) component.getRenderProperty(TabPane.PROPERTY_TAB_INACTIVE_BACKGROUND_IMAGE);
             return backgroundImage == null ? null : backgroundImage.getImage();
         } else {
             return null;
         }
     }
 
     /**
      * Determines if a <code>TabPane</code> should be lazy-rendered.
      * 
      * @param tabPane the <code>TabPane</code> to query
      * @return true if lazy-rendering should be enabled
      */
     private boolean isLazyRenderEnabled(TabPane tabPane) {
         Boolean lazyRenderEnabled = (Boolean) tabPane.getRenderProperty(PROPERTY_LAZY_RENDER_ENABLED);
         return lazyRenderEnabled == null ? true : lazyRenderEnabled.booleanValue();
     }
     
     /**
      * @see nextapp.echo2.webcontainer.LazyRenderContainer#isRendered(nextapp.echo2.webcontainer.ContainerInstance, 
      *      nextapp.echo2.app.Component, nextapp.echo2.app.Component)
      */
     public boolean isRendered(ContainerInstance ci, Component parent, Component child) {
         TabPaneRenderState renderState = (TabPaneRenderState) ci.getRenderState(parent);
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
                             TabPane.INPUT_TAB_INDEX, new Integer(i));
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
         serverMessage.addLibrary(TAB_PANE_SERVICE.getId());
         serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
         TabPane tabPane = (TabPane) component;
         
         ContainerInstance ci = rc.getContainerInstance();
         resetRenderState(ci, tabPane);
         
         configureActiveTab(ci, tabPane);
         
         renderInitDirective(rc, tabPane, targetId);
         Component[] children = tabPane.getVisibleComponents();
         for (int i = 0; i < children.length; ++i) {
             renderAddTabDirective(rc, update, tabPane, children[i]);
         }
         boolean lazyRenderEnabled = isLazyRenderEnabled(tabPane);
         for (int i = 0; i < children.length; ++i) {
             if (!lazyRenderEnabled || isRendered(ci, tabPane, children[i])) {
                 renderChild(rc, update, tabPane, children[i]);
             }
         }
     }
 
     private void renderAddChildren(RenderContext rc, ServerComponentUpdate update, boolean activeTabRenderRequired) {
         TabPane tabPane = (TabPane) update.getParent();
         ContainerInstance ci = rc.getContainerInstance();
 
         Component activeTab = null;
         if (activeTabRenderRequired) {
             TabPaneRenderState renderState = (TabPaneRenderState) ci.getRenderState(tabPane);
             activeTab = getChildByRenderId(tabPane, renderState.activeTabId);
             if (activeTab == null) {
                 activeTabRenderRequired = false;
             }
         }
         
         if (update.hasAddedChildren()) {
             Component[] addedChildren = update.getAddedChildren();
             Component[] children = tabPane.getVisibleComponents();
             
             // Iterating through arrays and checking for reference equality is used here (versus loading daddedChildren
             // into a hashtable) because we'll be dealing with very small array lengths, typically less than 10.
             for (int i = 0; i < children.length; ++i) {
                 for (int j = 0; j < addedChildren.length; ++j) {
                     if (children[i] == addedChildren[j]) {
                         renderAddTabDirective(rc, update, tabPane, children[i]);
                         break;
                     }
                 }
             }
             
             boolean lazyRenderEnabled = isLazyRenderEnabled(tabPane);
             
             // Add children.
             for (int i = 0; i < addedChildren.length; ++i) {
                 if (!lazyRenderEnabled || isRendered(ci, tabPane, addedChildren[i])) {
                     renderChild(rc, update, tabPane, addedChildren[i]);
                     if (addedChildren[i] == activeTab) {
                         activeTabRenderRequired = false;
                     }
                 }
             }
         }
         
         if (activeTabRenderRequired) {
             renderChild(rc, update, tabPane, activeTab);
         }
     }
     
     private void renderAddTabDirective(RenderContext rc, ServerComponentUpdate update, TabPane tabPane, Component child) {
         ContainerInstance ci = rc.getContainerInstance();
         boolean rendered = !isLazyRenderEnabled(tabPane) || isRendered(ci, tabPane, child);
         
         TabPaneLayoutData layoutData = (TabPaneLayoutData) child.getLayoutData();
         String elementId = ContainerInstance.getElementId(tabPane);
         Element addPartElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                 "ExtrasTabPane.MessageProcessor", "add-tab");
         addPartElement.setAttribute("eid", elementId);
         addPartElement.setAttribute("tab-id", child.getRenderId());
         addPartElement.setAttribute("tab-index", Integer.toString(tabPane.indexOf(child)));
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
      * Renders an individual child component of the <code>TabPane</code>.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> being performed
      * @param child The child <code>Component</code> to be rendered
      */
     private void renderChild(RenderContext rc, ServerComponentUpdate update, TabPane tabPane, Component child) {
         ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
         syncPeer.renderAdd(rc, update, getContainerId(child), child);
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
      */
     public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(TAB_PANE_SERVICE.getId());
         serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
         renderDisposeDirective(rc, (TabPane) component);
     }
 
     /**
      * Renders a dispose directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param tabPane the <code>TabPane</code> being rendered
      */
     private void renderDisposeDirective(RenderContext rc, TabPane tabPane) {
         String elementId = ContainerInstance.getElementId(tabPane);
         ServerMessage serverMessage = rc.getServerMessage();
         Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                 "ExtrasTabPane.MessageProcessor", "dispose");
         initElement.setAttribute("eid", elementId);
     }
     
     /**
      * Renders an initialization directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param tabPane the <code>TabPane</code> being rendered
      */
     private void renderInitDirective(RenderContext rc, TabPane tabPane, String targetId) {
         String elementId = ContainerInstance.getElementId(tabPane);
         ServerMessage serverMessage = rc.getServerMessage();
         Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "ExtrasTabPane.MessageProcessor");
         Element initElement = serverMessage.getDocument().createElement("init");
         initElement.setAttribute("container-eid", targetId);
         initElement.setAttribute("eid", elementId);
         
         if (!tabPane.isRenderEnabled()) {
         	initElement.setAttribute("enabled", "false");
         }
         Color background = (Color) tabPane.getRenderProperty(TabPane.PROPERTY_BACKGROUND);
         if (background != null) {
             initElement.setAttribute("default-background", ColorRender.renderCssAttributeValue(background));
         }
         Color foreground = (Color) tabPane.getRenderProperty(TabPane.PROPERTY_FOREGROUND);
         if (foreground != null) {
             initElement.setAttribute("default-foreground", ColorRender.renderCssAttributeValue(foreground));
         }
         Insets defaultContentInsets = (Insets) tabPane.getRenderProperty(TabPane.PROPERTY_DEFAULT_CONTENT_INSETS);
         if (defaultContentInsets != null) {
             initElement.setAttribute("default-content-insets", InsetsRender.renderCssAttributeValue(defaultContentInsets));
         }
         Insets insets = (Insets) tabPane.getRenderProperty(TabPane.PROPERTY_INSETS);
         if (insets != null) {
         	initElement.setAttribute("insets", InsetsRender.renderCssAttributeValue(insets));
         }
         Extent tabInset = (Extent) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_INSET);
         if (tabInset != null) {
             initElement.setAttribute("tab-inset", ExtentRender.renderCssAttributeValue(tabInset));
         }
         Extent tabSpacing = (Extent) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_SPACING);
         if (tabSpacing != null) {
             initElement.setAttribute("tab-spacing", ExtentRender.renderCssAttributeValue(tabSpacing));
         }
         
         Integer tabPosition = (Integer) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_POSITION);
         if (tabPosition != null) {
             initElement.setAttribute("tab-position", tabPosition.intValue() == TabPane.TAB_POSITION_BOTTOM ? "bottom" : "top");
         }
         
         Integer borderType = (Integer) tabPane.getRenderProperty(TabPane.PROPERTY_BORDER_TYPE);
         if (borderType != null) {
             switch (borderType.intValue()) {
             case TabPane.BORDER_TYPE_ADJACENT_TO_TABS:
                 initElement.setAttribute("border-type", "adjacent");
                 break;
             case TabPane.BORDER_TYPE_NONE:
                 initElement.setAttribute("border-type", "none");
                 break;
             case TabPane.BORDER_TYPE_PARALLEL_TO_TABS:
                 initElement.setAttribute("border-type", "parallel");
                 break;
             case TabPane.BORDER_TYPE_SURROUND:
                 initElement.setAttribute("border-type", "surround");
                 break;
             }
         }
         
         // Render tab active properties.
         Color tabActiveBackground = (Color) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_ACTIVE_BACKGROUND);
         if (tabActiveBackground != null) {
             initElement.setAttribute("tab-active-background", ColorRender.renderCssAttributeValue(tabActiveBackground));
         }
         FillImage tabActiveBackgroundImage = (FillImage) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_ACTIVE_BACKGROUND_IMAGE);
         if (tabActiveBackgroundImage != null) {
             CssStyle backgroundImageStyle = new CssStyle();
             FillImageRender.renderToStyle(backgroundImageStyle, rc, this, tabPane, IMAGE_ID_TAB_ACTIVE_BACKGROUND, 
                     tabActiveBackgroundImage, 0);
             initElement.setAttribute("tab-active-background-image", backgroundImageStyle.renderInline());
         }
         Border tabActiveBorder = (Border) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_ACTIVE_BORDER);
         if (tabActiveBorder != null) {
             if (tabActiveBorder.getColor() != null) {
                 initElement.setAttribute("tab-active-border-color", 
                         ColorRender.renderCssAttributeValue(tabActiveBorder.getColor()));
             }
             if (tabActiveBorder.getSize() != null && tabActiveBorder.getSize().getUnits() == Extent.PX) {
                 initElement.setAttribute("tab-active-border-size", Integer.toString(tabActiveBorder.getSize().getValue()));
             }
             initElement.setAttribute("tab-active-border-style", BorderRender.getStyleValue(tabActiveBorder.getStyle())); 
         }
         Font tabActiveFont= (Font) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_ACTIVE_FONT);
         if (tabActiveFont != null) {
             CssStyle fontStyle = new CssStyle();
             FontRender.renderToStyle(fontStyle, tabActiveFont);
             initElement.setAttribute("tab-active-font", fontStyle.renderInline());
         }
         Color tabActiveForeground = (Color) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_ACTIVE_FOREGROUND);
         if (tabActiveForeground != null) {
             initElement.setAttribute("tab-active-foreground", ColorRender.renderCssAttributeValue(tabActiveForeground));
         }
         
         // Render tab inactive properties.
         Color tabInactiveBackground = (Color) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_INACTIVE_BACKGROUND);
         if (tabInactiveBackground != null) {
             initElement.setAttribute("tab-inactive-background", ColorRender.renderCssAttributeValue(tabInactiveBackground));
         }
         FillImage tabInactiveBackgroundImage = (FillImage) tabPane.getRenderProperty(
                 TabPane.PROPERTY_TAB_INACTIVE_BACKGROUND_IMAGE);
         if (tabInactiveBackgroundImage != null) {
             CssStyle backgroundImageStyle = new CssStyle();
             FillImageRender.renderToStyle(backgroundImageStyle, rc, this, tabPane, IMAGE_ID_TAB_INACTIVE_BACKGROUND, 
                     tabInactiveBackgroundImage, 0);
             initElement.setAttribute("tab-inactive-background-image", backgroundImageStyle.renderInline());
         }
         Border tabInactiveBorder = (Border) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_INACTIVE_BORDER);
         if (tabInactiveBorder != null) {
             if (tabInactiveBorder.getColor() != null) {
                 initElement.setAttribute("tab-inactive-border-color", 
                         ColorRender.renderCssAttributeValue(tabInactiveBorder.getColor()));
             }
             if (tabInactiveBorder.getSize() != null && tabInactiveBorder.getSize().getUnits() == Extent.PX) {
                 initElement.setAttribute("tab-inactive-border-size", Integer.toString(tabInactiveBorder.getSize().getValue()));
             }
             initElement.setAttribute("tab-inactive-border-style", BorderRender.getStyleValue(tabInactiveBorder.getStyle())); 
         }
         Font tabInactiveFont= (Font) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_INACTIVE_FONT);
         if (tabInactiveFont != null) {
             CssStyle fontStyle = new CssStyle();
             FontRender.renderToStyle(fontStyle, tabInactiveFont);
             initElement.setAttribute("tab-inactive-font", fontStyle.renderInline());
         }
         Color tabInactiveForeground = (Color) tabPane.getRenderProperty(TabPane.PROPERTY_TAB_INACTIVE_FOREGROUND);
         if (tabInactiveForeground != null) {
             initElement.setAttribute("tab-inactive-foreground", ColorRender.renderCssAttributeValue(tabInactiveForeground));
         }
         
         String activeTabId = getRenderedActiveTabId(rc.getContainerInstance(), tabPane);
         if (activeTabId != null) {
             initElement.setAttribute("active-tab", activeTabId);
         }
 
         partElement.appendChild(initElement);
     }
     
     /**
      * Renders directives to remove any children from the client that were
      * removed in the specified <code>ServerComponentUpdate</code>.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> to process
      */
     private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
         TabPane tabPane = (TabPane) update.getParent();
         Component[] removedChildren = update.getRemovedChildren();
         for (int i = 0; i < removedChildren.length; ++i) {
             renderRemoveTabDirective(rc, update, tabPane, removedChildren[i]);
         }
     }
     
     /**
      * Renders a directive to the <code>ServerMessage</code> to remove a tab 
      * from a <code>TabPane</code>.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> describing the 
      *        change
      * @param tabPane the <code>TabPane</code> being updated
      * @param child the child <code>Component</code> being removed form the
      *        <code>TabPane</code>
      */
     private void renderRemoveTabDirective(RenderContext rc, ServerComponentUpdate update, TabPane tabPane, Component child) {
         String elementId = ContainerInstance.getElementId(tabPane);
         Element removeTabElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_REMOVE, 
                 "ExtrasTabPane.MessageProcessor", "remove-tab");
         removeTabElement.setAttribute("eid", elementId);
         removeTabElement.setAttribute("tab-id", child.getRenderId());
     }
 
     /**
      * Updates the active tab of a pre-existing <code>TabPane</code> on the
      * client.  This method will render the tab's component hierarchy to the
      * client if it has not yet been loaded, and then render a set-active-tab
      * directive to select the tab. 
      *
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> describing the 
      *        change
      * @param tabPane the <code>TabPane</code> being updated
      */
     private void renderSetActiveTab(RenderContext rc, ServerComponentUpdate update, TabPane tabPane) {
         ContainerInstance ci = rc.getContainerInstance();
 
         boolean activeTabRenderRequired = configureActiveTab(ci, tabPane);
 
         Component activeTab = null;
         if (activeTabRenderRequired) {
             TabPaneRenderState renderState = (TabPaneRenderState) ci.getRenderState(tabPane);
             activeTab = getChildByRenderId(tabPane, renderState.activeTabId);
             if (activeTab != null) {
                 renderChild(rc, update, tabPane, activeTab);
             }
         }
         
         renderSetActiveTabDirective(rc, update, tabPane);
     }
     
     /**
      * Renders a directive to the <code>ServerMessage</code> to set the
      * active tab of a pre-exisiting <code>TabPane</code>
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the <code>ServerComponentUpdate</code> describing the 
      *        change
      * @param tabPane the <code>TabPane</code> being updated
      */
     private void renderSetActiveTabDirective(RenderContext rc, ServerComponentUpdate update, TabPane tabPane) {
         Component activeTab = null;
         TabPaneRenderState renderState = (TabPaneRenderState) rc.getContainerInstance().getRenderState(tabPane);
         activeTab = getChildByRenderId(tabPane, renderState.activeTabId);
         if (activeTab == null) {
             return;
         }
         
         String elementId = ContainerInstance.getElementId(tabPane);
         Element setActiveTabElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                 "ExtrasTabPane.MessageProcessor", "set-active-tab");
         setActiveTabElement.setAttribute("eid", elementId);
         setActiveTabElement.setAttribute("active-tab", activeTab.getRenderId());
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
      */
     public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
         ContainerInstance ci = rc.getContainerInstance();
         TabPane tabPane = (TabPane) update.getParent();
         
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
             DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
             renderAdd(rc, update, targetId, update.getParent());
         } else {
 
             // Perform incremental updates.
             if (update.hasRemovedChildren() || update.hasAddedChildren()) {
                 
                 boolean activeTabRenderRequired = configureActiveTab(ci, tabPane);
                 if (update.hasRemovedChildren()) {
                     renderRemoveChildren(rc, update);
                 }
                 if (update.hasAddedChildren() || activeTabRenderRequired) {
                     renderAddChildren(rc, update, activeTabRenderRequired);
                 }
                 renderSetActiveTabDirective(rc, update, tabPane);
             }
             
             if (update.hasUpdatedProperties()) {
                 partialUpdateManager.process(rc, update);
             }
             
         }
         
         return fullReplace;
     }
     
     /**
      * Resets the <code>RenderState</code> of a <code>TabPane</code> in the
      * <code>ContainerInstance</code>.  Invoked when a <code>TabPane</code> is
      * initially rendered to the client.
      * 
      * @param ci the relevant <code>ContainerInstance</code>
      * @param tabPane the <code>TabPane</code> being rendered
      */
     private void resetRenderState(ContainerInstance ci, TabPane tabPane) {
         TabPaneRenderState renderState = new TabPaneRenderState();
         ci.setRenderState(tabPane, renderState);
     }
     
     /**
      * Sets a flag in the <code>RenderState</code> to indicate that a particular
      * tab of a <code>TabPane</code> has been/is being rendered to the client.
      * This method is used to facilitate lazy-rendering, ensuring each tab of
      * a <code>TabPane</code> is rendered tot he client only once.
      * 
      * @param ci the relevant <code>ContainerInstance</code>
      * @param tabPane the <code>TabPane</code> being rendered
      * @param child the child tab component
      */
     private void setRendered(ContainerInstance ci, TabPane tabPane, Component child) {
         TabPaneRenderState renderState = (TabPaneRenderState) ci.getRenderState(tabPane);
         if (renderState == null ) {
             renderState = new TabPaneRenderState();
             ci.setRenderState(tabPane, renderState);
         }
         renderState.renderedChildren.add(child);
     }
 }

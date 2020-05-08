 package nextapp.echo2.extras.webcontainer;
 
 import org.w3c.dom.Element;
 
 import nextapp.echo2.app.Border;
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Component;
 import nextapp.echo2.app.Insets;
 import nextapp.echo2.app.update.ServerComponentUpdate;
 import nextapp.echo2.extras.app.AccordionPane;
 import nextapp.echo2.extras.app.layout.AccordionPaneLayoutData;
 import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
 import nextapp.echo2.webcontainer.ContainerInstance;
 import nextapp.echo2.webcontainer.PartialUpdateManager;
 import nextapp.echo2.webcontainer.PartialUpdateParticipant;
 import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
 import nextapp.echo2.webcontainer.RenderContext;
 import nextapp.echo2.webcontainer.SynchronizePeerFactory;
 import nextapp.echo2.webcontainer.propertyrender.BorderRender;
 import nextapp.echo2.webcontainer.propertyrender.ColorRender;
 import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
 import nextapp.echo2.webrender.ServerMessage;
 import nextapp.echo2.webrender.Service;
 import nextapp.echo2.webrender.WebRenderServlet;
 import nextapp.echo2.webrender.servermessage.DomUpdate;
 import nextapp.echo2.webrender.service.JavaScriptService;
 
 public class AccordionPanePeer 
 implements ComponentSynchronizePeer, PropertyUpdateProcessor {
 
     private static final String PROPERTY_ACTIVE_TAB = "activeTab";
 
     /**
      * Service to provide supporting JavaScript library.
      */
     public static final Service ACCORDION_PANE_SERVICE = JavaScriptService.forResource("Echo2Extras.AccordionPane",
             "/nextapp/echo2/extras/webcontainer/resource/js/AccordionPane.js");
 
     static {
         WebRenderServlet.getServiceRegistry().add(ACCORDION_PANE_SERVICE);
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
             renderSetActiveTabDirective(rc, update, (AccordionPane) update.getParent());
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
         partialUpdateManager.add(AccordionPane.ACTIVE_TAB_CHANGED_PROPERTY, activeTabUpdateParticipant);
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
      */
     public String getContainerId(Component child) {
         return ContainerInstance.getElementId(child.getParent()) + "_content_" + child.getRenderId();
     }
 
     /**
      * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(nextapp.echo2.webcontainer.ContainerInstance, 
      *      nextapp.echo2.app.Component, org.w3c.dom.Element)
      */
     public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
         String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
         if (PROPERTY_ACTIVE_TAB.equals(propertyName)) {
             String propertyValue = propertyElement.getAttribute("value");
             int length = component.getVisibleComponentCount();
             for (int i = 0; i < length; ++i) {
                 Component child = component.getVisibleComponent(i);
                 if (propertyValue.equals(child.getRenderId())) {
                     ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                             AccordionPane.INPUT_ACTIVE_TAB, child);
                     break;
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
         serverMessage.addLibrary(ACCORDION_PANE_SERVICE.getId());
         serverMessage.addLibrary(ExtrasUtil.SERVICE.getId());
         AccordionPane accordionPane = (AccordionPane) component;
         renderInitDirective(rc, accordionPane, targetId);
         Component[] children = accordionPane.getVisibleComponents();
         for (int i = 0; i < children.length; ++i) {
             renderAddTabDirective(rc, update, accordionPane, children[i]);
         }
         for (int i = 0; i < children.length; ++i) {
             renderChild(rc, update, accordionPane, children[i]);
         }
         renderRedrawDirective(rc, accordionPane);
     }
 
     private void renderAddChildren(RenderContext rc, ServerComponentUpdate update) {
         AccordionPane accordionPane = (AccordionPane) update.getParent();
         Component[] addedChildren = update.getAddedChildren();
         
         // Create tab containers for children (performed in distinct loop from adding 
         // children in order to minimize ServerMessage length).
         for (int i = 0; i < addedChildren.length; ++i) {
             renderAddTabDirective(rc, update, accordionPane, addedChildren[i]);
         }
         
         // Add children.
         for (int i = 0; i < addedChildren.length; ++i) {
             renderChild(rc, update, accordionPane, addedChildren[i]);
         }
 
         renderRedrawDirective(rc, accordionPane);
     }

     private void renderAddTabDirective(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane, 
             Component child) {
         AccordionPaneLayoutData layoutData = (AccordionPaneLayoutData) child.getLayoutData();
         String elementId = ContainerInstance.getElementId(accordionPane);
         Element addPartElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                 "ExtrasAccordionPane.MessageProcessor", "add-tab");
         addPartElement.setAttribute("eid", elementId);
         addPartElement.setAttribute("tab-id", child.getRenderId());
         addPartElement.setAttribute("tab-index", Integer.toString(accordionPane.indexOf(child)));
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
         
         Color background = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_BACKGROUND);
         if (background != null) {
             initElement.setAttribute("background", ColorRender.renderCssAttributeValue(background));
         }
         Color foreground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_FOREGROUND);
         if (foreground != null) {
             initElement.setAttribute("foreground", ColorRender.renderCssAttributeValue(foreground));
         }
         
         Color tabBackground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_BACKGROUND);
         if (tabBackground != null) {
             initElement.setAttribute("tab-background", ColorRender.renderCssAttributeValue(tabBackground));
         }
         Border tabBorder = (Border) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_BORDER);
         if (tabBorder != null) {
             initElement.setAttribute("tab-border", BorderRender.renderCssAttributeValue(tabBorder));
         }
         Color tabForeground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_FOREGROUND);
         if (tabForeground != null) {
             initElement.setAttribute("tab-foreground", ColorRender.renderCssAttributeValue(tabForeground));
         }
         Insets tabInsets = (Insets) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_INSETS);
         if (tabInsets != null) {
             initElement.setAttribute("tab-insets", InsetsRender.renderCssAttributeValue(tabInsets));
         }
         Color tabRolloverBackground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_BACKGROUND);
         if (tabRolloverBackground != null) {
             initElement.setAttribute("tab-rollover-background", ColorRender.renderCssAttributeValue(tabRolloverBackground));
         }
         Border tabRolloverBorder = (Border) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_BORDER);
         if (tabRolloverBorder != null) {
             initElement.setAttribute("tab-rollover-border", BorderRender.renderCssAttributeValue(tabRolloverBorder));
         }
         Color tabRolloverForeground = (Color) accordionPane.getRenderProperty(AccordionPane.PROPERTY_TAB_ROLLOVER_FOREGROUND);
         if (tabRolloverForeground != null) {
             initElement.setAttribute("tab-rollover-foreground", ColorRender.renderCssAttributeValue(tabRolloverForeground));
         }
         
         partElement.appendChild(initElement);
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
      */
     public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(ACCORDION_PANE_SERVICE.getId());
         serverMessage.addLibrary(ExtrasUtil.SERVICE.getId());
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
         
         Component activeTabComponent = accordionPane.getActiveTab();
         if (activeTabComponent != null) {
             initElement.setAttribute("active-tab", activeTabComponent.getRenderId());
         }
         partElement.appendChild(initElement);
     }
     
     private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
         AccordionPane accordionPane = (AccordionPane) update.getParent();
         Component[] removedChildren = update.getRemovedChildren();
         for (int i = 0; i < removedChildren.length; ++i) {
             renderRemoveTabDirective(rc, update, accordionPane, removedChildren[i]);
         }
         renderRedrawDirective(rc, accordionPane);
     }
     
     private void renderRemoveTabDirective(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane, 
             Component child) {
         String elementId = ContainerInstance.getElementId(accordionPane);
         Element removePartElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_REMOVE, 
                 "ExtrasAccordionPane.MessageProcessor", "remove-tab");
         removePartElement.setAttribute("eid", elementId);
         removePartElement.setAttribute("tab-id", child.getRenderId());
     }
     
     private void renderSetActiveTabDirective(RenderContext rc, ServerComponentUpdate update, AccordionPane accordionPane) {
         Component activeTab = accordionPane.getActiveTab();
         if (activeTab == null) {
             return;
         }
         String elementId = ContainerInstance.getElementId(accordionPane);
         Element removePartElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                 "ExtrasAccordionPane.MessageProcessor", "set-active-tab");
         removePartElement.setAttribute("eid", elementId);
         removePartElement.setAttribute("tab-id", activeTab.getRenderId());
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
      */
     public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
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
             renderDisposeDirective(rc, (AccordionPane) update.getParent());
             DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
             renderAdd(rc, update, targetId, update.getParent());
         } else {
             // Perform incremental updates.
             if (update.hasRemovedChildren()) {
                 renderRemoveChildren(rc, update);
             }
             if (update.hasUpdatedProperties()) {
                 partialUpdateManager.process(rc, update);
             }
             if (update.hasAddedChildren()) {
                 renderAddChildren(rc, update);
             }
         }
         
         return fullReplace;
     }
 }

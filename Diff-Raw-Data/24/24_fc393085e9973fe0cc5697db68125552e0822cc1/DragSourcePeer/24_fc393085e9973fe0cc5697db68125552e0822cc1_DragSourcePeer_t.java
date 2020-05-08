 /* 
  * This file is part of the Echo2 DnD Project.
  * Copyright (C) 2005-2006 Jason Dalton
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
 
 import nextapp.echo2.app.Component;
 import nextapp.echo2.app.update.ServerComponentUpdate;
 import nextapp.echo2.extras.app.DragSource;
 import nextapp.echo2.webcontainer.ActionProcessor;
 import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
 import nextapp.echo2.webcontainer.ContainerInstance;
 import nextapp.echo2.webcontainer.DomUpdateSupport;
 import nextapp.echo2.webcontainer.RenderContext;
 import nextapp.echo2.webcontainer.SynchronizePeerFactory;
 import nextapp.echo2.webrender.ServerMessage;
 import nextapp.echo2.webrender.Service;
 import nextapp.echo2.webrender.WebRenderServlet;
 import nextapp.echo2.webrender.servermessage.DomUpdate;
 import nextapp.echo2.webrender.service.JavaScriptService;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * Synchronization peer for <code>nextapp.echo2.contrib.dnd.DragSource</code> components.
  */
 public class DragSourcePeer 
 implements ActionProcessor, ComponentSynchronizePeer, DomUpdateSupport {
     
 	/**
      * Service to provide supporting JavaScript library.
      */
     public static final Service DRAG_SOURCE_SERVICE = JavaScriptService.forResource("Echo2Extras.DND",
             "/nextapp/echo2/extras/webcontainer/resource/js/DND.js");
 
     static {
         WebRenderServlet.getServiceRegistry().add(DRAG_SOURCE_SERVICE);
     }
     
     /**
      * Default constructor.
      */
     public DragSourcePeer() {
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
      */
     public String getContainerId(Component child) {
         return ContainerInstance.getElementId(child.getParent()) + "_dnd_" + ContainerInstance.getElementId(child);
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance,
      *      nextapp.echo2.app.Component, org.w3c.dom.Element)
      */
     public void processAction(ContainerInstance ci, Component component, Element actionElement) {
         Component target = ci.getComponentByElementId(actionElement.getAttribute(ActionProcessor.ACTION_VALUE));
         ci.getUpdateManager().getClientUpdateManager().setComponentAction(component, DragSource.INPUT_DROP, target);
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
      *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
      */
     public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
         Element domAddElement = DomUpdate.renderElementAdd(rc.getServerMessage());
         DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
         renderHtml(rc, update, htmlFragment, component);
         DomUpdate.renderElementAddContent(rc.getServerMessage(), domAddElement, targetId, htmlFragment);
     }
     
     /**
      * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * render and initialize the state of a <code>DragSource</code>.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param dragSource the <code>DragSource</code>
      */
     private void renderInitDirective(RenderContext rc, DragSource dragSource) {
         String elementId = ContainerInstance.getElementId(dragSource);
         ServerMessage serverMessage = rc.getServerMessage();
         Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_POSTUPDATE, "ExtrasDragSource.MessageProcessor");
         Element initElement = serverMessage.getDocument().createElement("init");
         
         initElement.setAttribute("eid", elementId);
         if (dragSource.getRenderProperty(DragSource.PROPERTY_TOOL_TIP_TEXT) != null) {
             initElement.setAttribute("tooltip", (String) dragSource.getRenderProperty(DragSource.PROPERTY_TOOL_TIP_TEXT));
         }
         for (int i=0; i<dragSource.getDropTargetCount(); i++) {
         	Component dropTarget = (Component) dragSource.getDropTargetAt(i);
         	String targetId = ContainerInstance.getElementId(dropTarget);
         	Element dropTargetElement = serverMessage.getDocument().createElement("drop-target");
         	dropTargetElement.setAttribute("eid",targetId);
         	initElement.appendChild(dropTargetElement);
         }
         partElement.appendChild(initElement);
     }
     
     /**
      * Renders a child component.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param update the update
      * @param parentElement the HTML element which should contain the child
      * @param child the child component to render
      */
     private void renderAddChild(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component child) {
         ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
         if (syncPeer instanceof DomUpdateSupport) {
             ((DomUpdateSupport) syncPeer).renderHtml(rc, update, parentElement, child);
         } else {
             syncPeer.renderAdd(rc, update, getContainerId(child), child);
         }
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
      *      nextapp.echo2.app.update.ServerComponentUpdate,
      *      nextapp.echo2.app.Component)
      */
     public void renderDispose(RenderContext rc, ServerComponentUpdate update,
             Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(DRAG_SOURCE_SERVICE.getId());
         renderDisposeDirective(rc, (DragSource) component);
     }
 
     /**
      * Renders a directive to the outgoing <code>ServerMessage</code> to 
      * dispose the state of a <code>DragSource</code>, performing tasks such as
      * unregistering event listeners on the client.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param dragSource the <code>DragSource</code>
      */
     private void renderDisposeDirective(RenderContext rc, DragSource dragSource) {
         String elementId = ContainerInstance.getElementId(dragSource);
         ServerMessage serverMessage = rc.getServerMessage();
         Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                 "ExtrasDragSource.MessageProcessor", "dispose");
         initElement.setAttribute("eid", elementId);
     }
 
     /**
      * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
      *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
      */
     public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
         rc.getServerMessage().addLibrary(DRAG_SOURCE_SERVICE.getId());
     	DragSource dragSource = (DragSource) component;
     	
     	renderInitDirective(rc, dragSource);
     	String elementId = ContainerInstance.getElementId(dragSource);
         Document document = parentNode.getOwnerDocument();
         
         Element divElement = document.createElement("div");
         divElement.setAttribute("id", elementId);
         
         parentNode.appendChild(divElement);
         
         renderAddChild(rc, update, divElement, dragSource.getComponent(0));
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
      *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
      */
     public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
         DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
         renderAdd(rc, update, targetId, update.getParent());
         return true;
     }
 }

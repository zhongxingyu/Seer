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
 
 import java.util.StringTokenizer;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import nextapp.echo2.app.Border;
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
 import nextapp.echo2.app.FillImage;
 import nextapp.echo2.app.ImageReference;
 import nextapp.echo2.app.ResourceImageReference;
 import nextapp.echo2.app.update.ServerComponentUpdate;
 import nextapp.echo2.extras.app.MenuBarPane;
 import nextapp.echo2.extras.app.menu.ItemModel;
 import nextapp.echo2.extras.app.menu.MenuModel;
 import nextapp.echo2.extras.app.menu.MenuStateModel;
 import nextapp.echo2.extras.app.menu.OptionModel;
 import nextapp.echo2.extras.app.menu.RadioOptionModel;
 import nextapp.echo2.extras.app.menu.SeparatorModel;
 import nextapp.echo2.extras.app.menu.ToggleOptionModel;
 import nextapp.echo2.webcontainer.ActionProcessor;
 import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
 import nextapp.echo2.webcontainer.ContainerInstance;
 import nextapp.echo2.webcontainer.PartialUpdateManager;
 import nextapp.echo2.webcontainer.RenderContext;
 import nextapp.echo2.webcontainer.image.ImageRenderSupport;
 import nextapp.echo2.webcontainer.image.ImageTools;
 import nextapp.echo2.webcontainer.propertyrender.BorderRender;
 import nextapp.echo2.webcontainer.propertyrender.ColorRender;
 import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
 import nextapp.echo2.webrender.ServerMessage;
 import nextapp.echo2.webrender.Service;
 import nextapp.echo2.webrender.WebRenderServlet;
 import nextapp.echo2.webrender.output.CssStyle;
 import nextapp.echo2.webrender.servermessage.DomUpdate;
 import nextapp.echo2.webrender.service.JavaScriptService;
 
 /**
  * <code>ComponentSynchronizePeer</code> implementation for synchronizing
  * <code>MenuBarPane</code> components.
  */
 public class MenuBarPanePeer 
 implements ActionProcessor, ComponentSynchronizePeer, ImageRenderSupport {
 
     /**
      * Service to provide supporting JavaScript library.
      */
     public static final Service MENU_SERVICE = JavaScriptService.forResource("Echo2Extras.Menu",
             "/nextapp/echo2/extras/webcontainer/resource/js/Menu.js");
 
     static {
         WebRenderServlet.getServiceRegistry().add(MENU_SERVICE);
     }
     
     private static final String IMAGE_PREFIX = "/nextapp/echo2/extras/webcontainer/resource/image/";
     private static final ImageReference DEFAULT_ICON_TOGGLE_OFF = new ResourceImageReference(IMAGE_PREFIX + "ToggleOff.gif");
     private static final ImageReference DEFAULT_ICON_TOGGLE_ON = new ResourceImageReference(IMAGE_PREFIX + "ToggleOn.gif");
     private static final ImageReference DEFAULT_ICON_RADIO_OFF = new ResourceImageReference(IMAGE_PREFIX + "RadioOff.gif");
     private static final ImageReference DEFAULT_ICON_RADIO_ON = new ResourceImageReference(IMAGE_PREFIX + "RadioOn.gif");
     
     private static final String IMAGE_ID_BACKGROUND = "background";
     private static final String IMAGE_ID_DISABLED_BACKGROUND = "disabledBackground";
     private static final String IMAGE_ID_MENU_BACKGROUND = "menuBackground";
     private static final String IMAGE_ID_MENU_ITEM_PREFIX = "menuItem.";
     private static final String IMAGE_ID_SELECTION_BACKGROUND = "selectionBackground";
     
     private static final String IMAGE_ID_TOGGLE_OFF = "toggleOff";
     private static final String IMAGE_ID_TOGGLE_ON = "toggleOn";
     private static final String IMAGE_ID_RADIO_OFF = "radioOff";
     private static final String IMAGE_ID_RADIO_ON = "radioOn";
     
 
     /**
      * The <code>PartialUpdateManager</code> for this synchronization peer.
      */
     private PartialUpdateManager partialUpdateManager;
     
     /**
      * Default constructor.
      */
     public MenuBarPanePeer() {
         partialUpdateManager = new PartialUpdateManager();
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
      */
     public String getContainerId(Component component) {
         throw new UnsupportedOperationException("Component does not support children.");
     }
 
     /**
      * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
      */
     public ImageReference getImage(Component component, String imageId) {
         FillImage fillImage = null;
         if (IMAGE_ID_BACKGROUND.equals(imageId)) {
             fillImage = (FillImage) component.getRenderProperty(MenuBarPane.PROPERTY_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_MENU_BACKGROUND.equals(imageId)) {
             fillImage = (FillImage) component.getRenderProperty(MenuBarPane.PROPERTY_MENU_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_SELECTION_BACKGROUND.equals(imageId)) {
             fillImage = (FillImage) component.getRenderProperty(MenuBarPane.PROPERTY_SELECTION_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_TOGGLE_OFF.equals(imageId)) {
             return DEFAULT_ICON_TOGGLE_OFF;
         } else if (IMAGE_ID_TOGGLE_ON.equals(imageId)) {
             return DEFAULT_ICON_TOGGLE_ON;
         } else if (IMAGE_ID_RADIO_OFF.equals(imageId)) {
             return DEFAULT_ICON_RADIO_OFF;
         } else if (IMAGE_ID_RADIO_ON.equals(imageId)) {
             return DEFAULT_ICON_RADIO_ON;
         } else if (imageId.startsWith(IMAGE_ID_MENU_ITEM_PREFIX)) {
             String itemPath = imageId.substring(IMAGE_ID_MENU_ITEM_PREFIX.length());
             ItemModel itemModel = getItemModel((MenuBarPane) component, itemPath);
             if (itemModel instanceof MenuModel) {
                 return ((MenuModel) itemModel).getIcon();
             } else if (itemModel instanceof OptionModel) {
                 return ((OptionModel) itemModel).getIcon();
             } else {
                 return null;
             }
         } else {
             return null;
         }
     }
     
     private String getItemPath(MenuModel menuModel, ItemModel targetItemModel) {
         StringBuffer out = new StringBuffer();
         getItemPath(menuModel, targetItemModel, out);
         return out.length() == 0 ? null : out.toString();
     }
     
     private void getItemPath(MenuModel menuModel, ItemModel targetItemModel, StringBuffer out) {
         int itemCount = menuModel.getItemCount();
         for (int i = 0; i < itemCount; ++i) {
             ItemModel currentItemModel = menuModel.getItem(i);
             if (targetItemModel.equals(currentItemModel)) {
                 out.append(i);
                 return;
             }
             if (currentItemModel instanceof MenuModel) {
                 getItemPath((MenuModel) currentItemModel, targetItemModel, out); 
             }
             if (out.length() != 0) {
                 out.insert(0, i + ".");
                 return;
             }
         }
     }
     
     private ItemModel getItemModel(MenuBarPane menu, String itemPath) {
         ItemModel itemModel = menu.getModel();
         StringTokenizer st = new StringTokenizer(itemPath, ".");
         while (st.hasMoreTokens()) {
             int index = Integer.parseInt(st.nextToken());
             itemModel = ((MenuModel) itemModel).getItem(index);
         }
         return itemModel;
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, 
      *      nextapp.echo2.app.Component, org.w3c.dom.Element)
      */
     public void processAction(ContainerInstance ci, Component component, Element element) {
         MenuBarPane menu = (MenuBarPane) component;
         String actionName = element.getAttribute(ActionProcessor.ACTION_NAME);
         String actionValue = element.getAttribute(ActionProcessor.ACTION_VALUE);
         if ("select".equals(actionName)) {
             ItemModel itemModel = getItemModel((MenuBarPane) component, actionValue);
             if (!(itemModel instanceof MenuModel || itemModel instanceof OptionModel)) {
                 // Should not occur unless client input tampered with.
                 return;
             }
             ci.getUpdateManager().getClientUpdateManager().setComponentAction(menu, MenuBarPane.INPUT_SELECT, itemModel);
         }
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
      *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
      */
     public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
         serverMessage.addLibrary(MENU_SERVICE.getId());
         MenuBarPane menu = (MenuBarPane) component;
         renderInitDirective(rc, menu, targetId);
     }
     
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(
      *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
      */
     public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
         ServerMessage serverMessage = rc.getServerMessage();
         serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
         serverMessage.addLibrary(MENU_SERVICE.getId());
         renderDisposeDirective(rc, (MenuBarPane) component);
     }
 
     /**
      * Renders a dispose directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param menu the <code>MenuBarPane</code> being rendered
      */
     private void renderDisposeDirective(RenderContext rc, MenuBarPane menu) {
         String elementId = ContainerInstance.getElementId(menu);
         ServerMessage serverMessage = rc.getServerMessage();
         Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                 "ExtrasMenu.MessageProcessor", "dispose");
         initElement.setAttribute("eid", elementId);
     }
 
     /**
      * Renders an initialization directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param menu the <code>MenuBarPane</code> being rendered
      */
     private void renderInitDirective(RenderContext rc, MenuBarPane menu, String targetId) {
         String elementId = ContainerInstance.getElementId(menu);
         ServerMessage serverMessage = rc.getServerMessage();
         Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "ExtrasMenu.MessageProcessor");
         Element initElement = serverMessage.getDocument().createElement("init");
         initElement.setAttribute("container-eid", targetId);
         initElement.setAttribute("eid", elementId);
         if (!menu.isRenderEnabled()) {
             initElement.setAttribute("enabled", "false");
         }
         
         Color background = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_BACKGROUND);
         if (background != null) {
             initElement.setAttribute("background", ColorRender.renderCssAttributeValue(background));
         }
         FillImage backgroundImage = (FillImage) menu.getRenderProperty(MenuBarPane.PROPERTY_BACKGROUND_IMAGE);
         if (backgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_BACKGROUND, backgroundImage, 0);
             initElement.setAttribute("background-image", cssStyle.renderInline());
         }
         Border border = (Border) menu.getRenderProperty(MenuBarPane.PROPERTY_BORDER);
         if (border != null) {
            if (border.getColor() != null) {
                initElement.setAttribute("border-color", 
                        ColorRender.renderCssAttributeValue(border.getColor()));
            }
            if (border.getSize() != null && border.getSize().getUnits() == Extent.PX) {
                initElement.setAttribute("border-size", Integer.toString(border.getSize().getValue()));
            }
            initElement.setAttribute("border-style", BorderRender.getStyleValue(border.getStyle())); 
         }
         Color foreground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_FOREGROUND);
         if (foreground != null) {
             initElement.setAttribute("foreground", ColorRender.renderCssAttributeValue(foreground));
         }
         Color menuBackground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_MENU_BACKGROUND);
         if (menuBackground != null) {
             initElement.setAttribute("menu-background", ColorRender.renderCssAttributeValue(menuBackground));
         }
         FillImage menuBackgroundImage = (FillImage) menu.getRenderProperty(MenuBarPane.PROPERTY_MENU_BACKGROUND_IMAGE);
         if (menuBackgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_MENU_BACKGROUND, menuBackgroundImage, 0);
             initElement.setAttribute("menu-background-image", cssStyle.renderInline());
         }
         Border menuBorder = (Border) menu.getRenderProperty(MenuBarPane.PROPERTY_MENU_BORDER);
         if (menuBorder != null) {
            if (menuBorder.getColor() != null) {
                initElement.setAttribute("menu-border-color", 
                        ColorRender.renderCssAttributeValue(menuBorder.getColor()));
            }
            if (menuBorder.getSize() != null && menuBorder.getSize().getUnits() == Extent.PX) {
                initElement.setAttribute("menu-border-size", Integer.toString(menuBorder.getSize().getValue()));
            }
            initElement.setAttribute("menu-border-style", BorderRender.getStyleValue(menuBorder.getStyle())); 
         }
         Color menuForeground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_MENU_FOREGROUND);
         if (menuForeground != null) {
             initElement.setAttribute("menu-foreground", ColorRender.renderCssAttributeValue(menuForeground));
         }
         Color selectionBackground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_SELECTION_BACKGROUND);
         if (selectionBackground != null) {
             initElement.setAttribute("selection-background", ColorRender.renderCssAttributeValue(selectionBackground));
         }
         FillImage selectionBackgroundImage = (FillImage) menu.getRenderProperty(MenuBarPane.PROPERTY_SELECTION_BACKGROUND_IMAGE);
         if (selectionBackgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_SELECTION_BACKGROUND, selectionBackgroundImage, 0);
             initElement.setAttribute("selection-background-image", cssStyle.renderInline());
         }
         Color selectionForeground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_SELECTION_FOREGROUND);
         if (selectionForeground != null) {
             initElement.setAttribute("selection-foreground", ColorRender.renderCssAttributeValue(selectionForeground));
         }
         Color disabledBackground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_DISABLED_BACKGROUND);
         if (disabledBackground != null) {
             initElement.setAttribute("disabled-background", ColorRender.renderCssAttributeValue(disabledBackground));
         }
         FillImage disabledBackgroundImage = (FillImage) menu.getRenderProperty(MenuBarPane.PROPERTY_DISABLED_BACKGROUND_IMAGE);
         if (disabledBackgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_DISABLED_BACKGROUND, disabledBackgroundImage, 0);
             initElement.setAttribute("disabled-background-image", cssStyle.renderInline());
         }
         Color disabledForeground = (Color) menu.getRenderProperty(MenuBarPane.PROPERTY_DISABLED_FOREGROUND);
         if (disabledForeground != null) {
             initElement.setAttribute("disabled-foreground", ColorRender.renderCssAttributeValue(disabledForeground));
         }
         
         initElement.setAttribute("icon-toggle-off", ImageTools.getUri(rc, this, menu, IMAGE_ID_TOGGLE_OFF));
         initElement.setAttribute("icon-toggle-on", ImageTools.getUri(rc, this, menu, IMAGE_ID_TOGGLE_ON));
         initElement.setAttribute("icon-radio-off", ImageTools.getUri(rc, this, menu, IMAGE_ID_RADIO_OFF));
         initElement.setAttribute("icon-radio-on", ImageTools.getUri(rc, this, menu, IMAGE_ID_RADIO_ON));
         
         renderModel(rc, menu, menu.getModel(), initElement);
         
         partElement.appendChild(initElement);
     }
     
     /**
      * Renders an XML representation of a <code>MenuModel</code> to the 
      * <code>ServerMessage</code>.  This method is invoked recursively to render
      * hierarchies of menus.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param menuModel the <code>MenuModel</code> to render
      * @param parentElement the parent <code>Element</code>, either the 'init'
      *        element or the containing 'menu' element
      */
     private void renderModel(RenderContext rc, MenuBarPane menu, MenuModel menuModel, Element parentElement) {
         Document document = rc.getServerMessage().getDocument();
         Element menuModelElement = document.createElement("menu");
 
         MenuStateModel stateModel = menu.getStateModel();
         
         if (menuModel.getText() != null) {
             menuModelElement.setAttribute("text", menuModel.getText());
         }
         if (menuModel.getIcon() != null) {
             String itemPath = getItemPath(menu.getModel(), menuModel);
             menuModelElement.setAttribute("icon", ImageTools.getUri(rc, this, menu, IMAGE_ID_MENU_ITEM_PREFIX + itemPath));
         }
         if (menuModel.getId() != null && !stateModel.isEnabled(menuModel.getId())) {
             menuModelElement.setAttribute("enabled", "false");
         }
 
         int length = menuModel.getItemCount();
         for (int i = 0; i < length; ++i) {
             ItemModel itemModel = menuModel.getItem(i);
             if (itemModel instanceof MenuModel) {
                 renderModel(rc, menu, (MenuModel) itemModel, menuModelElement); 
             } else if (itemModel instanceof OptionModel) {
                 Element optionModelElement = document.createElement("option");
                 OptionModel optionModel = (OptionModel) itemModel;
                 if (optionModel.getId() != null && !stateModel.isEnabled(optionModel.getId())) {
                     optionModelElement.setAttribute("enabled", "false");
                 }
                 if (optionModel instanceof ToggleOptionModel) {
                     if (optionModel instanceof RadioOptionModel) {
                         optionModelElement.setAttribute("type", "radio");
                     } else {
                         optionModelElement.setAttribute("type", "toggle");
                     }
                     if (stateModel != null && stateModel.isSelected(((ToggleOptionModel) optionModel).getId())) {
                         optionModelElement.setAttribute("selected", "true");
                     }
                 } else {
                     optionModelElement.setAttribute("type", "default");
                 }
                 if (optionModel.getText() != null) {
                     optionModelElement.setAttribute("text", optionModel.getText());
                 }
                 if (optionModel.getIcon() != null) {
                     String itemPath = getItemPath(menu.getModel(), optionModel);
                     optionModelElement.setAttribute("icon", ImageTools.getUri(rc, this, menu, 
                             IMAGE_ID_MENU_ITEM_PREFIX + itemPath));
                 }
                 menuModelElement.appendChild(optionModelElement);
             } else if (itemModel instanceof SeparatorModel) {
                 menuModelElement.appendChild(document.createElement("separator"));
             }
         }
         parentElement.appendChild(menuModelElement);
     }
 
     /**
      * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext,
      *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
      */
     public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
         // Determine if fully replacing the component is required.
         if (partialUpdateManager.canProcess(rc, update)) {
             partialUpdateManager.process(rc, update);
         } else {
             // Perform full update.
             DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
             renderAdd(rc, update, targetId, update.getParent());
         }
         return true;
     }
 }

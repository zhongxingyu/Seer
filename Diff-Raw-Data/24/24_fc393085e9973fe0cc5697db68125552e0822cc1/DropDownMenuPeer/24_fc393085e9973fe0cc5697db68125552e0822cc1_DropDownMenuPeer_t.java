 package nextapp.echo2.extras.webcontainer;
 
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Component;
 import nextapp.echo2.app.Extent;
 import nextapp.echo2.app.FillImage;
 import nextapp.echo2.app.ImageReference;
 import nextapp.echo2.app.ResourceImageReference;
 import nextapp.echo2.extras.app.DropDownMenu;
 import nextapp.echo2.extras.app.menu.ItemModel;
 import nextapp.echo2.extras.app.menu.MenuSelectionModel;
 import nextapp.echo2.webcontainer.ContainerInstance;
 import nextapp.echo2.webcontainer.RenderContext;
 import nextapp.echo2.webcontainer.image.ImageTools;
 import nextapp.echo2.webcontainer.propertyrender.ColorRender;
 import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
 import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
 import nextapp.echo2.webrender.ServerMessage;
 import nextapp.echo2.webrender.output.CssStyle;
 
 import org.w3c.dom.Element;
 
 public class DropDownMenuPeer extends AbstractMenuPeer {
 
     static final ImageReference DEFAULT_ICON_EXPAND = new ResourceImageReference(IMAGE_PREFIX + "ArrowDown.gif");
     
     public static final String IMAGE_ID_DISABLED_EXPAND_ICON = "disabledExpandIcon";
     public static final String IMAGE_ID_EXPAND_ICON = "expandIcon";
     
     /**
      * @see nextapp.echo2.extras.webcontainer.AbstractMenuPeer#getImage(nextapp.echo2.app.Component, java.lang.String)
      */
     public ImageReference getImage(Component component, String imageId) {
         ImageReference image = super.getImage(component, imageId);
         if (image != null) {
             return image;
         }
         
         FillImage fillImage = null;
         if (IMAGE_ID_BACKGROUND.equals(imageId)) {
             fillImage = (FillImage) component.getRenderProperty(DropDownMenu.PROPERTY_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_MENU_BACKGROUND.equals(imageId)) {
             fillImage = (FillImage) component.getRenderProperty(DropDownMenu.PROPERTY_MENU_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_SELECTION_BACKGROUND.equals(imageId)) {
             fillImage = (FillImage) component.getRenderProperty(DropDownMenu.PROPERTY_SELECTION_BACKGROUND_IMAGE);
             return fillImage == null ? null : fillImage.getImage();
         } else if (IMAGE_ID_EXPAND_ICON.equals(imageId)) {
             return (ImageReference) component.getRenderProperty(DropDownMenu.PROPERTY_EXPAND_ICON,
                     DEFAULT_ICON_EXPAND);
         } else if (IMAGE_ID_DISABLED_EXPAND_ICON.equals(imageId)) {
             return (ImageReference) component.getRenderProperty(DropDownMenu.PROPERTY_DISABLED_EXPAND_ICON);
         } else {
             return null;
         }
     }
 
     /**
      * Renders a dispose directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
     * @param component the <code>MenuBarPane</code> being rendered
      */
     void renderDisposeDirective(RenderContext rc, Component component) {
         DropDownMenu menu = (DropDownMenu) component;
         String elementId = ContainerInstance.getElementId(menu);
         ServerMessage serverMessage = rc.getServerMessage();
         Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                 "ExtrasDropDownMenu.MessageProcessor", "dispose");
         initElement.setAttribute("eid", elementId);
     }
 
     /**
      * Renders an initialization directive.
      * 
      * @param rc the relevant <code>RenderContext</code>
     * @param component the <code>MenuBarPane</code> being rendered
      */
     void renderInitDirective(RenderContext rc, Component component, String targetId) {
         DropDownMenu menu = (DropDownMenu) component;
         String elementId = ContainerInstance.getElementId(menu);
         ServerMessage serverMessage = rc.getServerMessage();
         Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "ExtrasDropDownMenu.MessageProcessor");
         Element initElement = serverMessage.getDocument().createElement("init");
         initElement.setAttribute("container-eid", targetId);
         initElement.setAttribute("eid", elementId);
         if (!menu.isRenderEnabled()) {
             initElement.setAttribute("enabled", "false");
         }
         MenuSelectionModel selectionModel = menu.getSelectionModel();
         if (selectionModel != null) {
             initElement.setAttribute("selection", "true");
             String selectedId = selectionModel.getSelectedId();
             ItemModel selectedModel = selectedId == null ? null :  getItemModelById(menu, selectedId);
             if (selectedModel != null) {
                 initElement.setAttribute("selected-path", getItemPath(menu.getModel(), selectedModel));
             }
         }
         
         Color background = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_BACKGROUND);
         if (background != null) {
             initElement.setAttribute("background", ColorRender.renderCssAttributeValue(background));
         }
         FillImage backgroundImage = (FillImage) menu.getRenderProperty(DropDownMenu.PROPERTY_BACKGROUND_IMAGE);
         if (backgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_BACKGROUND, backgroundImage, 0);
             initElement.setAttribute("background-image", cssStyle.renderInline());
         }
         Color foreground = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_FOREGROUND);
         if (foreground != null) {
             initElement.setAttribute("foreground", ColorRender.renderCssAttributeValue(foreground));
         }
         Extent height = (Extent) menu.getRenderProperty(DropDownMenu.PROPERTY_HEIGHT);
         if (height != null) {
             initElement.setAttribute("height", ExtentRender.renderCssAttributeValue(height));
         }
         Extent width = (Extent) menu.getRenderProperty(DropDownMenu.PROPERTY_WIDTH);
         if (width != null) {
             initElement.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
         }
         Color menuBackground = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_MENU_BACKGROUND);
         if (menuBackground != null) {
             initElement.setAttribute("menu-background", ColorRender.renderCssAttributeValue(menuBackground));
         }
         FillImage menuBackgroundImage = (FillImage) menu.getRenderProperty(DropDownMenu.PROPERTY_MENU_BACKGROUND_IMAGE);
         if (menuBackgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_MENU_BACKGROUND, menuBackgroundImage, 0);
             initElement.setAttribute("menu-background-image", cssStyle.renderInline());
         }
         Color selectionBackground = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_SELECTION_BACKGROUND);
         if (selectionBackground != null) {
             initElement.setAttribute("selection-background", ColorRender.renderCssAttributeValue(selectionBackground));
         }
         FillImage selectionBackgroundImage = (FillImage) menu.getRenderProperty(DropDownMenu.PROPERTY_SELECTION_BACKGROUND_IMAGE);
         if (selectionBackgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_SELECTION_BACKGROUND, selectionBackgroundImage, 0);
             initElement.setAttribute("selection-background-image", cssStyle.renderInline());
         }
         Color selectionForeground = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_SELECTION_FOREGROUND);
         if (selectionForeground != null) {
             initElement.setAttribute("selection-foreground", ColorRender.renderCssAttributeValue(selectionForeground));
         }
         Color disabledBackground = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_DISABLED_BACKGROUND);
         if (disabledBackground != null) {
             initElement.setAttribute("disabled-background", ColorRender.renderCssAttributeValue(disabledBackground));
         }
         FillImage disabledBackgroundImage = (FillImage) menu.getRenderProperty(DropDownMenu.PROPERTY_DISABLED_BACKGROUND_IMAGE);
         if (disabledBackgroundImage != null) {
             CssStyle cssStyle = new CssStyle();
             FillImageRender.renderToStyle(cssStyle, rc, this, menu, IMAGE_ID_DISABLED_BACKGROUND, disabledBackgroundImage, 0);
             initElement.setAttribute("disabled-background-image", cssStyle.renderInline());
         }
         Color disabledForeground = (Color) menu.getRenderProperty(DropDownMenu.PROPERTY_DISABLED_FOREGROUND);
         if (disabledForeground != null) {
             initElement.setAttribute("disabled-foreground", ColorRender.renderCssAttributeValue(disabledForeground));
         }
         
         initElement.setAttribute("submenu-image", ImageTools.getUri(rc, this, menu, IMAGE_ID_SUBMENU_RIGHT));
 
         initElement.setAttribute("icon-toggle-off", ImageTools.getUri(rc, this, menu, IMAGE_ID_TOGGLE_OFF));
         initElement.setAttribute("icon-toggle-on", ImageTools.getUri(rc, this, menu, IMAGE_ID_TOGGLE_ON));
         initElement.setAttribute("icon-radio-off", ImageTools.getUri(rc, this, menu, IMAGE_ID_RADIO_OFF));
         initElement.setAttribute("icon-radio-on", ImageTools.getUri(rc, this, menu, IMAGE_ID_RADIO_ON));
         initElement.setAttribute("expand-icon", ImageTools.getUri(rc, this, menu, IMAGE_ID_EXPAND_ICON));
                
         if (menu.getRenderProperty(DropDownMenu.PROPERTY_DISABLED_EXPAND_ICON) != null) {
             initElement.setAttribute("disabled-expand-icon", ImageTools.getUri(rc, this, menu, IMAGE_ID_DISABLED_EXPAND_ICON));
         }
         
         renderModel(rc, menu, menu.getModel(), initElement);
         
         partElement.appendChild(initElement);
     }
 }

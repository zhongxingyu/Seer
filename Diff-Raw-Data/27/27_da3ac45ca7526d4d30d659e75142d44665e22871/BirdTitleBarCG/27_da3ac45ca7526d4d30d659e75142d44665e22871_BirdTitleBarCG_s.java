 /*
  * Copyright (c) 2005 Your Corporation. All Rights Reserved.
  */
 package desktop;
 
 import org.wings.SComponent;
 import org.wings.SIcon;
 import org.wings.SInternalFrame;
 import org.wings.event.SInternalFrameEvent;
 import org.wings.session.SessionManager;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.CGManager;
 import org.wings.io.Device;
 
 import java.io.IOException;
 
 /**
  * @author hengels
  */
 public class BirdTitleBarCG
         extends AbstractComponentCG
 {
     protected static final String WINDOWICON_CLASSNAME = "WindowIcon";
     protected static final String BUTTONICON_CLASSNAME = "WindowButton";
     private SIcon closeIcon;
     private SIcon deiconifyIcon;
     private SIcon iconifyIcon;
     private SIcon maximizeIcon;
     private SIcon unmaximizeIcon;
 
     /**
      * Initialize properties from config
      */
     public BirdTitleBarCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
 
         setCloseIcon((SIcon) manager.getObject("InternalFrameCG.closeIcon", SIcon.class));
         setDeiconifyIcon((SIcon) manager.getObject("InternalFrameCG.deiconifyIcon", SIcon.class));
         setIconifyIcon((SIcon) manager.getObject("InternalFrameCG.iconifyIcon", SIcon.class));
         setMaximizeIcon((SIcon) manager.getObject("InternalFrameCG.maximizeIcon", SIcon.class));
         setUnmaximizeIcon((SIcon) manager.getObject("InternalFrameCG.unmaximizeIcon", SIcon.class));
     }
 
     public void installCG(SComponent component) {
         component.setStyle("BirdTitleBar");
     }
 
     protected void writeIcon(Device device, SIcon icon, String cssClass) throws IOException {
         device.print("<img");
         if (cssClass != null) {
             device.print(" class=\"");
             device.print(cssClass);
             device.print("\"");
         }
         Utils.optAttribute(device, "src", icon.getURL());
         Utils.optAttribute(device, "width", icon.getIconWidth());
         Utils.optAttribute(device, "height", icon.getIconHeight());
        Utils.atribute(device, "alt", icon.getIconTitle());
         device.print("/>");
     }
 
     protected void writeWindowIcon(Device device, SInternalFrame frame,
             int event, SIcon icon, String cssClass) throws IOException {
         boolean showAsFormComponent = frame.getShowAsFormComponent();
 
         // RequestURL addr = frame.getRequestURL();
         // addr.addParameter(MSIEUtils.event(frame), event);
 
         // we don't need this to be buttons
 //        if (showAsFormComponent) {
 //            device.print("<button");
 //            if (cssClass != null) {
 //                device.print(" class=\"");
 //                device.print(cssClass);
 //                device.print("\"");
 //            }
 //            device.print(" name=\"").print(MSIEUtils.event(frame)).print(
 //                    "\" value=\"").print(event).print("\">");
 //        } else {
             device.print("<a");
             if (cssClass != null) {
                 device.print(" class=\"");
                 device.print(cssClass);
                 device.print("\"");
             }
             device.print(" href=\"").print(
                     frame.getRequestURL().addParameter(
                             Utils.event(frame) + "=" + event).toString())
                     .print("\">");
 //        }
         writeIcon(device, icon, null);
 
 //        if (showAsFormComponent) {
 //            device.print("</button>");
 //        } else {
             device.print("</a>");
 //        }
     }
 
 
     public void writeInternal(final Device device, final SComponent _c)
             throws IOException {
         Bird.TitleBar titleBar = (Bird.TitleBar) _c;
         Bird frame = titleBar.getBird();
         writeWindowBar(device, frame);
     }
 
     protected void writeWindowBar(final Device device, SInternalFrame frame) throws IOException {
         String text = frame.getTitle();
         if (text == null)
             text = "wingS";
 
         device.print("<div class=\"WindowBar\">");
 
         if (frame.isClosable() && closeIcon != null) {
             writeWindowIcon(device, frame,
                     SInternalFrameEvent.INTERNAL_FRAME_CLOSED, closeIcon, BUTTONICON_CLASSNAME);
         }
 
         if (frame.isIconified()) {
             if (frame.isIconifyable() && deiconifyIcon != null) {
                 writeWindowIcon(device, frame,
                         SInternalFrameEvent.INTERNAL_FRAME_DEICONIFIED, deiconifyIcon, BUTTONICON_CLASSNAME);
             }
         } else {
             if (frame.isIconifyable() && iconifyIcon != null) {
                 writeWindowIcon(device, frame,
                         SInternalFrameEvent.INTERNAL_FRAME_ICONIFIED, iconifyIcon, BUTTONICON_CLASSNAME);
             }
         }
 
         if (frame.isMaximized()) {
             if (frame.isMaximizable() && unmaximizeIcon != null) {
                 writeWindowIcon(device, frame,
                         SInternalFrameEvent.INTERNAL_FRAME_UNMAXIMIZED, unmaximizeIcon, BUTTONICON_CLASSNAME);
             }
         } else {
             if (frame.isMaximizable() && maximizeIcon != null) {
                 writeWindowIcon(device, frame,
                         SInternalFrameEvent.INTERNAL_FRAME_MAXIMIZED, maximizeIcon, BUTTONICON_CLASSNAME);
             }
         }
         device.print("<div class=\"WindowBar_title\">");
         // float right end
         if (frame.getIcon() != null) {
             writeIcon(device, frame.getIcon(), WINDOWICON_CLASSNAME);
         }
         device.print(text);
         device.print("</div>");
 
         device.print("</div>");
     }
 
     private String getIconWidth(SIcon icon) {
         if (icon.getIconWidth() == -1)
             return "0%";
         else
             return "" + icon.getIconWidth();
     }
 
 
     public SIcon getCloseIcon() {
         return closeIcon;
     }
 
     public void setCloseIcon(SIcon closeIcon) {
         this.closeIcon = closeIcon;
     }
 
     public SIcon getDeiconifyIcon() {
         return deiconifyIcon;
     }
 
     public void setDeiconifyIcon(SIcon deiconifyIcon) {
         this.deiconifyIcon = deiconifyIcon;
     }
 
     public SIcon getIconifyIcon() {
         return iconifyIcon;
     }
 
     public void setIconifyIcon(SIcon iconifyIcon) {
         this.iconifyIcon = iconifyIcon;
     }
 
     public SIcon getMaximizeIcon() {
         return maximizeIcon;
     }
 
     public void setMaximizeIcon(SIcon maximizeIcon) {
         this.maximizeIcon = maximizeIcon;
     }
 
     public SIcon getUnmaximizeIcon() {
         return unmaximizeIcon;
     }
 
     public void setUnmaximizeIcon(SIcon unmaximizeIcon) {
         this.unmaximizeIcon = unmaximizeIcon;
     }
 
 }

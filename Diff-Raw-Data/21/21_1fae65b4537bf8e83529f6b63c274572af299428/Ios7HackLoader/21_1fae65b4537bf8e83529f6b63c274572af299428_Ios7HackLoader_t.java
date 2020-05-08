 package org.vaadin.addon.ios7fixes.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.MetaElement;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.Window.Navigator;
 
 public class Ios7HackLoader implements EntryPoint {
 
     private Integer bootWidth;
     private Integer bootHeight;
 
     public void onModuleLoad() {
 
         /*
          * Most critical issues happen in "Home Screen Web Apps". That is when
          * apps use <meta name="apple-mobile-web-app-capable" content="yes">
          * meta tag and have been added to home screen. There e.g. viewport
          * settings cause major head ache and alert/prompt/confirm don't work at
          * all.
          * 
          * At least currently this tool only fixes stuff for thos apps.
          */
         if (isiOS7HomeScreenApp()) {
             // Defer setting "boot values", else may be something weird in iphone
             // when app is started in landscape mode
             new Timer() {
                 @Override
                 public void run() {
                     initSizeIfNeeded();
                     /* "Somewhat" working viewport settings */
                     addHeightToViewPort();
                     
                 }}.schedule(1000);
 
             // ... and set it each time size changes (most often orientation
             // change)
             Window.addResizeHandler(new ResizeHandler() {
                 private Timer deferredResizeHandler;
 
                 @Override
                 public void onResize(ResizeEvent event) {
                     if (deferredResizeHandler != null) {
                         deferredResizeHandler.cancel();
                     }
                     // Defer to get correct orientation, 1000 ms seems to be just
                     // enough (tested on ipad mini)
                     deferredResizeHandler = new Timer() {
                         @Override
                         public void run() {
                             if (!isVirtualKeyboardOn()) {
                                 fixHtmlHeightToWindowInnerHeight();
                             } else {
                                 deferredResizeHandler.schedule(1000);
                             }
                         }
                     };
 
                     deferredResizeHandler.schedule(1000);
                 }
             });
 
            if(hasBrokenAlert()) {
                hookCustomAlerts();
            }
         }
 
     }
 
     private void initSizeIfNeeded() {
         if (bootWidth == null) {
             if (isLandscape()) {
                 bootWidth = getWindowInnerHeight();
                 bootHeight = getWindowInnerWidth();
             } else {
                 bootHeight = getWindowInnerHeight();
                 bootWidth = getWindowInnerWidth();
             }
 //            log("BS" + bootWidth + " x " + bootHeight);
         }
     }
 
     private boolean isLandscape() {
         switch (getOrientation()) {
         case 0:
         case 180:
             return false;
         case 90:
         case -90:
         default:
             return true;
         }
     }
 
     /**
      * @return does the magic described by method name based on UA inspection
      */
     private boolean isiOS7HomeScreenApp() {
         String ua = Navigator.getUserAgent();
         return ua != null && ua.contains("OS 7_0") && ua.contains("iP")
                 && !ua.contains("Safari");
     }
    
    protected boolean hasBrokenAlert() {
        if(isiOS7HomeScreenApp()) {
            String ua = Navigator.getUserAgent();
            if(ua.contains("7_0 ") || ua.contains("7_0_1 ") || ua.contains("7_0_2 ")) {
                return true;
            }
        }
        return false;
    }
 
     private boolean isVirtualKeyboardOn() {
         int referenceHeight = isLandscape() ? bootWidth : bootHeight;
         int differeceToStart = Math.abs(referenceHeight
                 - getWindowInnerHeight());
 //        log("IVKBON il" + isLandscape() + " wh" + getWindowInnerHeight()
 //                + " ww" + getWindowInnerWidth() + " bw" + bootWidth + " bh"
 //                + bootHeight);
         // Allow small changes (~ status bar & e.g. hotspot notification)
         if (differeceToStart > 100) {
             return true;
         }
         return false;
     }
 
     private static final native void log(String s)
     /*-{
         $wnd.console.log(s);
     }-*/;
 
     private static final native void d()
     /*-{
         debugger;
     }-*/;
 
     /**
      * Modifies viewport tag to include both width=device-width AND
     * height=device-height. The latter is not generally known or used, but seems
     * to prevent ios from changing screen size when virtual keyboard pops on.
      * This is how it works in Safari, Android, mobile IE and in previous
      * version of iOS home screen web apps.
      * <p>
      * Instead of "device-height" we use pixel height reported by
      * window.innerHeight. On orientation changes that must be updated.
      */
     private void addHeightToViewPort() {
         MetaElement item = getViewportTag();
         if (item != null) {
             String attribute = item.getContent();
             if (!attribute.contains("width")) {
                 attribute += ",width=device-width";
             }
             int viewPortHeight = getWindowInnerHeight();
             if (!attribute.contains("height")) {
                 attribute += ",height=" + viewPortHeight;
             } else {
                 attribute = updateViewPortHeight(attribute, viewPortHeight);
             }
             item.setContent(attribute);
         }
     }
 
     private MetaElement getViewportTag() {
         NodeList<Element> metas = Document.get().getElementsByTagName("meta");
         for (int i = 0; i < metas.getLength(); i++) {
             MetaElement item = metas.getItem(i).cast();
             if ("viewport".equals(item.getAttribute("name"))) {
                 return item;
             }
         }
         return null;
     }
 
     private native final void hookCustomAlerts()
     /*-{
         console.log("fixing broken alert in ios7 alert");
         $wnd.alert = function(msg) {
             @org.vaadin.addon.ios7fixes.client.AlertBox::alert(Ljava/lang/String;)(msg);
         };
         $wnd.confirm  = function(msg) {
             @org.vaadin.addon.ios7fixes.client.AlertBox::confirm(Ljava/lang/String;)(msg);
         };
         $wnd.prompt = function(msg) {
             @org.vaadin.addon.ios7fixes.client.AlertBox::confirm(Ljava/lang/String;)(msg);
         };
         console.log("alert,confirm and prompt now 'polyfilled'");
     }-*/;
 
     private void fixHtmlHeightToWindowInnerHeight() {
         final MetaElement tag = getViewportTag();
         int viewPortHeight = getWindowInnerHeight();
         String c = updateViewPortHeight(tag.getContent(), viewPortHeight);
         tag.setContent(c);
     }
 
     private static final native String updateViewPortHeight(String s, int h)
     /*-{
         return s.replace(/,height=\w+/,",height=" + h);
     }-*/;;
 
     private static final native int getOrientation()
     /*-{
         return $wnd.orientation;
     }-*/;
 
     private static native int getWindowInnerHeight()
     /*-{
         return $wnd.innerHeight;
     }-*/;
 
     private static native int getWindowInnerWidth()
     /*-{
         return $wnd.innerWidth;
     }-*/;
 
 }

 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.header.Link;
 import org.wings.header.Script;
 import org.wings.io.Device;
 import org.wings.plaf.CGManager;
 import org.wings.resource.ClassPathStylesheetResource;
 import org.wings.resource.ClasspathResource;
 import org.wings.resource.DefaultURLResource;
 import org.wings.resource.DynamicCodeResource;
 import org.wings.script.DynamicScriptResource;
 import org.wings.script.JavaScriptListener;
 import org.wings.script.ScriptListener;
 import org.wings.session.Browser;
 import org.wings.session.BrowserType;
 import org.wings.session.SessionManager;
 import org.wings.style.CSSSelector;
 import org.wings.style.DynamicStyleSheetResource;
 
 import java.io.IOException;
 import java.util.*;
 
 public class FrameCG implements org.wings.plaf.FrameCG {
     private final transient static Log log = LogFactory.getLog(FrameCG.class);
 
     /**
      * The default DOCTYPE enforcing standard (non-quirks mode) in all current browsers.
      * Please be aware, that changing the DOCTYPE may change the way how browser renders the generate
      * document i.e. esp. the CSS attribute inheritance does not work correctly on <code>table</code> elements.
      * See i.e. http://www.ericmeyeroncss.com/bonus/render-mode.html
      */
     public final static String STRICT_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
             "\"http://www.w3.org/TR/REC-html40/strict.dtd\">";
 
     /**
      * The HTML DOCTYPE setting all browsers to Quirks mode.
      * We need this to force IE to use the correct box rendering model. It's the only browser
      * you cannot reconfigure via an CSS tag.
      */
     public final static String QUIRKS_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
 
     private String documentType = STRICT_DOCTYPE;
 
     private Boolean renderXmlDeclaration = Boolean.FALSE;
     /**
      * Initialize properties from config
      */
     public FrameCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
         final String userDocType = (String) manager.getObject("FrameCG.userDocType", String.class);
         final Boolean userRenderXmlDecl = (Boolean) manager.getObject("FrameCG.renderXmlDeclaration", Boolean.class);
 
         if (userDocType != null)
             setDocumentType(userDocType);
 
         if (userRenderXmlDecl != null)
             setRenderXmlDeclaration(userRenderXmlDecl);
     }
 
     private static final String PROPERTY_STYLESHEET = "Stylesheet.";
     private static final String BROWSER_DEFAULT = "default";
     
     private final static Set javascriptResourceKeys;
     static {
         javascriptResourceKeys = new HashSet();
         javascriptResourceKeys.add("JScripts.domlib");
         javascriptResourceKeys.add("JScripts.domtt");
     }
     
     
     /** 
      * Externalizes the style sheet(s) for this session.
      * Look up according style sheet file name in org.wings.plaf.css.properties file under Stylesheet.BROWSERNAME.
      * The style sheet is loaded from the class path.
      * @return the URLs under which the css file(s) was externalized
      */
     private List externalizeBrowserStylesheets() {
         final ExternalizeManager extManager = SessionManager.getSession().getExternalizeManager();
         final CGManager manager = SessionManager.getSession().getCGManager();
         final String browserName = SessionManager.getSession().getUserAgent().getBrowserType().getShortName();
         final String cssResource = PROPERTY_STYLESHEET + browserName;
         String cssClassPaths = (String)manager.getObject(cssResource, String.class);
         // catch missing browser entry in properties file
         if (cssClassPaths == null) {
             cssClassPaths = (String)manager.getObject(PROPERTY_STYLESHEET + BROWSER_DEFAULT, String.class);
         }
 
         StringTokenizer tokenizer = new StringTokenizer(cssClassPaths,",");
         ArrayList cssUrls = new ArrayList();
         while (tokenizer.hasMoreTokens()) {
             String cssClassPath = tokenizer.nextToken();
             ClassPathStylesheetResource res = new ClassPathStylesheetResource(cssClassPath, "text/css");
             String cssUrl = extManager.externalize(res, ExternalizeManager.GLOBAL);
             if (cssUrl != null)
                 cssUrls.add(cssUrl);
         }
 
         return cssUrls;
     }
 
 
     /**
      * @param jsResKey
      * @return
      */
     private String externalizeJavaScript(String jsResKey) {
         final ExternalizeManager extManager = SessionManager.getSession().getExternalizeManager();
         final CGManager manager = SessionManager.getSession().getCGManager();
         String jsClassPath = (String)manager.getObject(jsResKey, String.class);
         // catch missing script entry in properties file
         if (jsClassPath != null) {
             ClasspathResource res = new ClasspathResource(jsClassPath, "text/javascript");
             return extManager.externalize(res, ExternalizeManager.GLOBAL);
         }
         return null;
     }
 
     public static final String UTILS_SCRIPT = (String) SessionManager
             .getSession().getCGManager().getObject("JScripts.utils",
                     String.class);
 
     public static final String FORM_SCRIPT = (String) SessionManager
             .getSession().getCGManager().getObject("JScripts.form",
                     String.class);
 
     public static final JavaScriptListener FOCUS_SCRIPT =
             new JavaScriptListener("onfocus", "storeFocus(event)");
 
     public static final JavaScriptListener SCROLL_POSITION_SCRIPT =
             new JavaScriptListener("onscroll", "storeScrollPosition(event)");
 
 
     public void installCG(final SComponent comp) {
         final SFrame component = (SFrame) comp;
 
         DynamicCodeResource dynamicCodeRessource;
         DynamicStyleSheetResource styleSheetResource;
         DynamicScriptResource scriptResource;
         Link stylesheetLink;
 
         // dynamic code resource.
         dynamicCodeRessource = new DynamicCodeResource(component);
         component.addDynamicResource(dynamicCodeRessource);
 
         // dynamic stylesheet resource.
         styleSheetResource = new DynamicStyleSheetResource(component);
         stylesheetLink = new Link("stylesheet", null, "text/css", null, styleSheetResource);
         component.addDynamicResource(styleSheetResource);
         component.addHeader(stylesheetLink);
 
         // dynamic java script resource.
         scriptResource = new DynamicScriptResource(component);
         component.addDynamicResource(scriptResource);
         component.addHeader(new Script("text/javascript", scriptResource));
 
         Iterator iter = javascriptResourceKeys.iterator();
         while (iter.hasNext()) {
             String jsResKey = (String) iter.next();
             String jScriptUrl = externalizeJavaScript(jsResKey);
             if (jScriptUrl != null) {
                 component.addHeader(new Script("text/javascript", new DefaultURLResource(jScriptUrl)));
             }
         }
 
         final List externalizedBrowserCssUrls = externalizeBrowserStylesheets();
         for (int i = 0; i < externalizedBrowserCssUrls.size(); i++) {
               component.headers().add(i, new Link("stylesheet", null, "text/css", null, new DefaultURLResource((String) externalizedBrowserCssUrls.get(i))));;
         }
 
         addExternalizedHeader(component, UTILS_SCRIPT, "text/javascript");
         addExternalizedHeader(component, FORM_SCRIPT, "text/javascript");
         component.addScriptListener(FOCUS_SCRIPT);
         component.addScriptListener(SCROLL_POSITION_SCRIPT);
         CaptureDefaultBindingsScriptListener.install(component);
     }
 
     /** 
      * adds the file found at the classPath to the parentFrame header with
      * the specified mimeType
      * @param classPath the classPath to look in for the file
      * @param mimeType the mimetype of the file
      */
     private void addExternalizedHeader(SFrame parentFrame, String classPath, String mimeType) {
         ClasspathResource res = new ClasspathResource(classPath, mimeType);
         String jScriptUrl = SessionManager.getSession().getExternalizeManager().externalize(res, ExternalizeManager.GLOBAL);
         parentFrame.addHeader(new Script(mimeType, new DefaultURLResource(jScriptUrl)));
     }
 
     public void uninstallCG(final SComponent comp) {
         final SFrame component = (SFrame) comp;
 
         component.removeDynamicResource(DynamicCodeResource.class);
         component.removeDynamicResource(DynamicStyleSheetResource.class);
         component.removeDynamicResource(DynamicScriptResource.class);
         component.clearHeaders();
     }
 
     public void write(final Device device, final SComponent _c)
             throws IOException {
         if (!_c.isVisible()) return;
         _c.fireRenderEvent(SComponent.START_RENDERING);
         final SFrame component = (SFrame) _c;
 
         Browser browser = SessionManager.getSession().getUserAgent();
         SFrame frame = (SFrame) component;
         String language = SessionManager.getSession().getLocale().getLanguage();
         String title = frame.getTitle();
         List headers = frame.headers();
         String encoding = SessionManager.getSession().getCharacterEncoding();
 
         /**
          *  We need to put IE6 into quirks mode
          *  for box model compatibility. (border-box).
          *  For that we make use of a comment in the first line.
          *  This is a known bug in IE6
          */
         if (BrowserType.IE.equals(browser.getBrowserType())) {
             if (browser.getMajorVersion() == 6) {
                 device.print("<!-- IE6 quirks mode switch -->\n");
             }
         }
 
         if (renderXmlDeclaration == null || renderXmlDeclaration.booleanValue()) {
             device.print("<?xml version=\"1.0\" encoding=\"");
             Utils.write(device, encoding);
             device.print("\"?>\n");
         }
 
         Utils.writeRaw(device, documentType);
         device.print("\n");
         device.print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
         Utils.write(device, language);
         device.print("\" lang=\"");
         Utils.write(device, language);
        device.print("\">\n");
 
         /* Insert version and compile time.
          * Since the Version Class is generated on compile time, build errors
          * in SDK's are quite normal. Just run the Version.java ant task.
          */
         device.print("<!-- This is wingS (http://www.j-wings.org) version ");
         device.print(Version.getVersion());
         device.print(" (Build date: ");
         device.print(Version.getCompileTime());
         device.print(") -->\n");
         
         device.print("<head>");
         if (title != null) {
             device.print("<title>");
             Utils.write(device, title);
             device.print("</title>\n");
         }
 
         device.print("<meta http-equiv=\"Content-type\" content=\"text/html; charset=");
         Utils.write(device, encoding);
         device.print("\"/>\n");
 
         for (Iterator iterator = headers.iterator(); iterator.hasNext();) {
             Object next = iterator.next();
             if (next instanceof Renderable) {
                 ((Renderable) next).write(device);
             } else {
                 Utils.write(device, next.toString());
             }
             device.print("\n");
         }
 
         SComponent focus = frame.getFocus();
         Object lastFocus = frame.getClientProperty("focus");
         if (focus != lastFocus) {
             if (lastFocus != null) {
                 ScriptListener[] scriptListeners = frame.getScriptListeners();
 
                 for (int i = 0; i < scriptListeners.length; i++) {
                     ScriptListener scriptListener = scriptListeners[i];
                     if (scriptListener instanceof FocusScriptListener)
                         component.removeScriptListener(scriptListener);
                 }
             }
             if (focus != null) {
                 FocusScriptListener listener = new FocusScriptListener("onload", "requestFocus('" + focus.getName() + "')");
                 frame.addScriptListener(listener);
             }
             frame.putClientProperty("focus", focus);
         }
         
         // let ie understand hover css styles on elements other than anchors
         if (BrowserType.IE.equals(browser.getBrowserType())) {
             // externalize hover behavior
             final String classPath = (String)SessionManager.getSession().getCGManager().getObject("Behaviors.ieHover", String.class);
             ClasspathResource res = new ClasspathResource(classPath, "text/x-component");
             String behaviorUrl = SessionManager.getSession().getExternalizeManager().externalize(res, ExternalizeManager.GLOBAL);
             device.print("<style type=\"text/css\" media=\"screen\">\n");
             device.print("body{behavior:url(");
             device.print(behaviorUrl);
             device.print(");}\n");
             device.print("</style>\n");
         }
         
 
 
         // TODO: move this to a dynamic script resource
         SToolTipManager toolTipManager = component.getSession().getToolTipManager();
         device
                 .print("<script type=\"text/javascript\">\n")
                 .print("domTT_addPredefined('default', 'caption', false");
         if (toolTipManager.isFollowMouse())
             device.print(", 'trail', true");
         device.print(", 'delay', ").print(toolTipManager.getInitialDelay());
         device.print(", 'lifetime', ").print(toolTipManager.getDismissDelay());
         device
                 .print(");\n")
                 .print("</script>\n");
         
         device.print("</head>\n");
         device.print("<body");
         Utils.optAttribute(device, "id", frame.getName());
         Utils.optAttribute(device, "class", frame.getStyle());
         Utils.writeEvents(device, frame);
         device.print(">\n");
         if (frame.isVisible()) {
             frame.getLayout().write(device);
             device.print("\n");
             // now add all menus
             Iterator iter = frame.getMenus().iterator();
             while (iter.hasNext()) {
                 SComponent menu = (SComponent)iter.next();
                 menu.write(device);
             }
         }
         device.print("\n</body></html>\n");
         _c.fireRenderEvent(SComponent.DONE_RENDERING);
     }
 
     public String getDocumentType() {
         return documentType;
     }
 
     public void setDocumentType(String documentType) {
         this.documentType = documentType;
     }
 
     /**
      * @return The current rendered DOCTYPE of this document. {@link #STRICT_DOCTYPE}
      */
     public Boolean getRenderXmlDeclaration() {
         return renderXmlDeclaration;
     }
 
     public void setRenderXmlDeclaration(Boolean renderXmlDeclaration) {
         this.renderXmlDeclaration = renderXmlDeclaration;
     }
 
     public CSSSelector mapSelector(SComponent addressedComponent, CSSSelector selector) {
         // Default: Do not map/modify the passed CSS selector.
         return selector;
     }
 }

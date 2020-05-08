 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 package org.richfaces.renderkit.html;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.event.AjaxEvent;
 import org.ajax4jsf.javascript.JSFunction;
 import org.ajax4jsf.javascript.JSFunctionDefinition;
 import org.ajax4jsf.javascript.JSReference;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.renderkit.AjaxComponentRendererBase;
 import org.ajax4jsf.renderkit.AjaxRendererUtils;
 import org.ajax4jsf.resource.InternetResource;
 import org.richfaces.component.UIToolTip;
 import org.richfaces.skin.Skin;
 
 public class ToolTipRenderer extends AjaxComponentRendererBase {
     
     private static final String AJAX_MODE = "ajax";
 
     private final InternetResource[] styles = { getResource("/org/richfaces/renderkit/html/css/tooltip.xcss") };
     
     private InternetResource[] stylesAll = null;
     
     protected InternetResource[] getStyles() {
         synchronized (this) {
             if (stylesAll == null) {
                 InternetResource[] rsrcs = super.getStyles();
                 boolean ignoreSuper = rsrcs == null || rsrcs.length == 0;
                 boolean ignoreThis = styles == null || styles.length == 0;
                 
                 if (ignoreSuper) {
                     if (ignoreThis) {
                         stylesAll = new InternetResource[0];
                     } else {
                         stylesAll = styles;
                     }
                 } else {
                     if (ignoreThis) {
                         stylesAll = rsrcs;
                     } else {
                         Set<InternetResource> rsrcsSet = new LinkedHashSet<InternetResource>();
                         
                         for (int i = 0; i < rsrcs.length; i++) {
                             rsrcsSet.add(rsrcs[i]);
                         }
                         
                         for (int i = 0; i < styles.length; i++) {
                             rsrcsSet.add(styles[i]);
                         }
                         
                         stylesAll = rsrcsSet.toArray(new InternetResource[rsrcsSet.size()]);
                     }
                 }
             }
         }
         
         return stylesAll;
     }
     
     private final InternetResource[] scripts = {
             new org.ajax4jsf.javascript.PrototypeScript(),
             new org.ajax4jsf.javascript.AjaxScript(),
             getResource("/org/richfaces/renderkit/html/scripts/jquery/jquery.js"),
             getResource("/org/richfaces/renderkit/html/scripts/utils.js"),
             getResource("/org/richfaces/renderkit/html/scripts/tooltip.js") };
     
     private InternetResource[] scriptsAll = null;
     
     protected InternetResource[] getScripts() {
         synchronized (this) {
             if (scriptsAll == null) {
                 InternetResource[] rsrcs = super.getScripts();
                 boolean ignoreSuper = rsrcs == null || rsrcs.length == 0;
                 boolean ignoreThis = scripts == null || scripts.length == 0;
                 
                 if (ignoreSuper) {
                     if (ignoreThis) {
                         scriptsAll = new InternetResource[0];
                     } else {
                         scriptsAll = scripts;
                     }
                 } else {
                     if (ignoreThis) {
                         scriptsAll = rsrcs;
                     } else {
                         Set<InternetResource> rsrcsSet = new LinkedHashSet<InternetResource>();
                         
                         for (int i = 0; i < rsrcs.length; i++) {
                             rsrcsSet.add(rsrcs[i]);
                         }
                         
                         for (int i = 0; i < scripts.length; i++) {
                             rsrcsSet.add(scripts[i]);
                         }
                         
                         scriptsAll = rsrcsSet.toArray(new InternetResource[rsrcsSet.size()]);
                     }
                 }
             }
         }
         
         return scriptsAll;
     }
     
     protected Class<? extends UIComponent> getComponentClass() {
         return org.richfaces.component.UIToolTip.class;
     }
     
     public String getBgColor(FacesContext context, UIComponent component) {
         Skin skin = getSkin(context);
         return String.valueOf(skin.getParameter(context, "headerBackgroundColor"));
     }
     
     public String getColor(FacesContext context, UIComponent component) {
         Skin skin = getSkin(context);
         return String.valueOf(skin.getParameter(context, "headerTextColor"));
     }
     
     public Map<String, Object> buildEventOptions(FacesContext context,
             UIComponent component, String targetId) {
         Map<String, Object> eventOptions = AjaxRendererUtils.buildEventOptions(
                 context, component);
         String jsVarName = "_toolTip";
         
         // after element is subsituted in DOM tree, we have to re-create
         // it's JS-reference, cause old one is already invalid
         
         String clientId = component.getClientId(context);
         String oncompleteTooltip = ";" + "{" + "var " + jsVarName + " = $('"
                 + clientId + "').component;" + jsVarName
                 + ".toolTipContent = $('" + clientId + "content');" + jsVarName
                 + ".displayDiv();" + "}";
         
         // before element will be substituted in DOM tree, we need to hide
         // toolTipe to avoid blinking
         String fireBeforeUpdateDOM = ";" + "{ var " + jsVarName + " = $('"
                 + clientId + "').component;" + jsVarName
                 + ".toolTip.style.display = 'none'; }";
         
         // enable ajaxSingle mode, i.e. we do not need to submit all form
         // controls to get tooltip content
         eventOptions.put("control", JSReference.THIS);
         
         if (eventOptions.containsKey("oncomplete")) {
             JSFunctionDefinition onComplete = 
                 (JSFunctionDefinition) eventOptions.get("oncomplete");
             onComplete.addToBody(oncompleteTooltip);
             eventOptions.put("oncomplete", onComplete);
         } else {
             JSFunctionDefinition onComplete = new JSFunctionDefinition();
             onComplete.addParameter("request");
             onComplete.addParameter("showEvent");
             onComplete.addParameter("data");
             onComplete.addToBody(oncompleteTooltip);
             eventOptions.put("oncomplete", onComplete);
         }
         
         if (eventOptions.containsKey(AjaxRendererUtils.ONBEFOREDOMUPDATE_ATTR_NAME)) {
             JSFunctionDefinition beforeUpdate = (JSFunctionDefinition) eventOptions
                     .get(AjaxRendererUtils.ONBEFOREDOMUPDATE_ATTR_NAME);
             beforeUpdate.addToBody(fireBeforeUpdateDOM);
             eventOptions.put(AjaxRendererUtils.ONBEFOREDOMUPDATE_ATTR_NAME, beforeUpdate);
         } else {
             JSFunctionDefinition beforeUpdate = new JSFunctionDefinition();
             beforeUpdate.addParameter("request");
             beforeUpdate.addParameter("showEvent");
             beforeUpdate.addParameter("data");
             beforeUpdate.addToBody(fireBeforeUpdateDOM);
             eventOptions.put(AjaxRendererUtils.ONBEFOREDOMUPDATE_ATTR_NAME, beforeUpdate);
         }
         
         return eventOptions;
     }
     
     public void insertScript(FacesContext context, UIComponent component)
             throws IOException {
         
         StringBuffer ret = new StringBuffer();
         ret.append("<script ");
         ret.append("type=\"text/javascript\" ");
         ret.append("id =\"script").append(component.getClientId(context)).append("\">\n");
         ret.append(constructJSVariable(context, component)).append(";\n\n");
         ret.append("</script>");
 
         context.getResponseWriter().write(ret.toString());
     }
     
     public Map<String, Object> getParamsMap(FacesContext context, UIToolTip toolTip) {
         List<UIComponent> children = toolTip.getChildren();
         Map<String, Object> paramsMap = new HashMap<String, Object>();
         for (UIComponent child : children) {
             if (child instanceof UIParameter) {
                 UIParameter param = (UIParameter) child;
                 paramsMap.put(param.getName(), param.getValue());
             }
         }
         return paramsMap;
     }
     
     private String getTargetId(FacesContext context, UIComponent component) {
         UIToolTip toolTip = (UIToolTip) component;
         String forValue = toolTip.getFor();
         
         if (forValue != null && forValue.length() != 0) {
             UIComponent targetComponent = getUtils().findComponentFor(component, forValue);
             if (targetComponent != null) {
                 return targetComponent.getClientId(context);
             } else {
                 return forValue;
             }
         } else {
             return component.getParent().getClientId(context);
         }
     }
     
     private final static String COMMA = ",";
     private final static String QUOT = "\"";
     private final static String QUOT_COMMA = "\"" + ",";  
     
     public String constructJSVariable(FacesContext context, UIComponent component) {
         UIToolTip toolTip = (UIToolTip) component;
         String targetClientId = getTargetId(context, component);
         
         
         Map<JSReference, Object> eventsMap = new HashMap<JSReference, Object>();
         
         String eventShow = (toolTip.isAttached()) ? toolTip.getShowEvent() : "";
         if (eventShow.startsWith("on")) {
             eventShow = eventShow.substring(2);
         }
         eventsMap.put(new JSReference("showEvent"), eventShow);
 
         String eventHide = (toolTip.isAttached()) ? toolTip.getHideEvent() : "";
         if (eventHide.startsWith("on")) {
             eventHide = eventHide.substring(2);
         }
         eventsMap.put(new JSReference("hideEvent"), eventHide);
 
         eventsMap.put(new JSReference("delay"), new Integer(toolTip.getShowDelay()));
         eventsMap.put(new JSReference("hideDelay"), new Integer(toolTip.getHideDelay()));
         
         JSFunctionDefinition ajaxFunc = null;
         if (AJAX_MODE.equalsIgnoreCase(toolTip.getMode())) {
             ajaxFunc = new JSFunctionDefinition("event", "ajaxOptions");
             JSFunction function = AjaxRendererUtils.buildAjaxFunction(component, context);
             JSReference ref = new JSReference("ajaxOptions");
             function.addParameter(ref);
             ajaxFunc.addToBody(function);
         } 
         
         Map<String, Object> ajaxOptions = buildEventOptions(context, toolTip, targetClientId);
         ajaxOptions.putAll(getParamsMap(context, toolTip));
         
         Map<JSReference, Object> funcMap = new HashMap<JSReference, Object>();
         JSFunctionDefinition completeFunc = getUtils().getAsEventHandler(
                 context, component, "oncomplete", "; return true;");
         funcMap.put(new JSReference("oncomplete"), completeFunc);
 
         JSFunctionDefinition hideFunc = getUtils().getAsEventHandler(
                 context, component, "onhide", "; return true;");
         funcMap.put(new JSReference("onhide"), hideFunc);
 
         JSFunctionDefinition showFunc = getUtils().getAsEventHandler(
                 context, component, "onshow", "; return true;");
         funcMap.put(new JSReference("onshow"), showFunc);
         
         StringBuffer ret = new StringBuffer();
         ret.append("new ToolTip(").append(ScriptUtils.toScript(eventsMap)).append(COMMA)
            .append(ScriptUtils.toScript(funcMap)).append(COMMA)
            .append(QUOT).append(toolTip.getClientId(context)).append(QUOT_COMMA)
            .append(QUOT).append(targetClientId).append(QUOT_COMMA)
            .append(QUOT).append(toolTip.getMode()).append(QUOT_COMMA)
            .append(toolTip.isDisabled()).append(COMMA)
            .append(QUOT).append(toolTip.getDirection()).append(QUOT_COMMA)
            .append(toolTip.isFollowMouse()).append(COMMA)
            .append(toolTip.getHorizontalOffset()).append(COMMA)
            .append(toolTip.getVerticalOffset()).append(COMMA)
            .append(ajaxFunc == null ? "null" : ajaxFunc.toScript()).append(COMMA)
            .append(ScriptUtils.toScript(ajaxOptions)).append(");");
         
         return ret.toString();
     }
     
     protected void doDecode(FacesContext context, UIComponent component) {
         
         UIToolTip tooltip = (UIToolTip) component;
         
         String clientId = tooltip.getClientId(context);
         
         if (context.getExternalContext().getRequestParameterMap().containsKey(clientId)) {
             if (AJAX_MODE.equals(tooltip.getMode())) {
                 new AjaxEvent(component).queue();
                 new ActionEvent(component).queue();
             }
         }
     }
     
     public void encodeTooltipText(FacesContext context, UIToolTip component)
             throws IOException {
         ResponseWriter responseWriter = context.getResponseWriter();
         responseWriter.startElement(component.getUsedElementType(), component);
         
         String clientId = component.getClientId(context);
         Object value = component.getValue();
         
         responseWriter.writeAttribute("id", clientId + "content", null);
         if (AJAX_MODE.equals(component.getMode())) {
             // we want to avoid rendering toolTip content during initialy page
             // displaying
             AjaxContext ajaxContext = AjaxContext.getCurrentInstance();
            if (ajaxContext != null && ajaxContext.getAjaxAreasToRender().contains(clientId + "content")) {
         
                 responseWriter.write(value != null ? value.toString() : "");
                 
                 super.renderChildren(context, component);
             }
         } else {
             // client mode
             responseWriter.write(value != null ? value.toString() : "");
             super.renderChildren(context, component);
         }
         responseWriter.endElement(component.getUsedElementType());
     }
     
     protected void doEncodeBegin(ResponseWriter writer, FacesContext context,
             UIComponent component) throws IOException {
         
         try {
             ToolTipRenderer renderer = getRenderer((UIToolTip) component);
             renderer.doEncodeBegin(writer, context, component);
         } catch (Exception e) {
             throw new FacesException(e.getMessage(), e);
         }
     }
     
     protected void doEncodeChildren(ResponseWriter writer,
             FacesContext context, UIComponent component) throws IOException {
         
         try {
             ToolTipRenderer renderer = getRenderer((UIToolTip) component);
             renderer.doEncodeChildren(writer, context, component);
         } catch (Exception e) {
             throw new FacesException(e.getMessage(), e);
         }
         
     }
     
     protected void doEncodeEnd(ResponseWriter writer, FacesContext context,
             UIComponent component) throws IOException {
         
         try {
             ToolTipRenderer renderer = getRenderer((UIToolTip) component);
             renderer.doEncodeEnd(writer, context, component);
         } catch (Exception e) {
             throw new FacesException(e.getMessage(), e);
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see javax.faces.render.Renderer#getRendersChildren()
      */
     public boolean getRendersChildren() {
         return true;
     }
     
     private ToolTipRenderer getRenderer(UIToolTip toolTip) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
         Class<?> rendererClass;
         if ("block".equals(toolTip.getLayout())) {
             rendererClass = Class.forName("org.richfaces.renderkit.html.HtmlToolTipRendererBlock");
         } else {
             rendererClass = Class.forName("org.richfaces.renderkit.html.HtmlToolTipRenderer");
         }
         
         return (ToolTipRenderer) rendererClass.newInstance();
         
     }
     
 }

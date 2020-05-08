 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
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
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.faces.FacesException;
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIData;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.javascript.JSFunction;
 import org.ajax4jsf.javascript.JSFunctionDefinition;
 import org.ajax4jsf.renderkit.AjaxComponentRendererBase;
 import org.ajax4jsf.renderkit.AjaxRendererUtils;
 import org.ajax4jsf.renderkit.RendererBase;
 import org.ajax4jsf.renderkit.RendererUtils;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.ajax4jsf.renderkit.compiler.HtmlCompiler;
 import org.ajax4jsf.renderkit.compiler.PreparedTemplate;
 import org.ajax4jsf.renderkit.compiler.TemplateContext;
 import org.ajax4jsf.resource.InternetResource;
 import org.ajax4jsf.resource.TemplateCSSResource;
 import org.richfaces.component.AjaxSuggestionEvent;
 import org.richfaces.component.UISuggestionBox;
 import org.richfaces.component.util.HtmlUtil;
 import org.richfaces.skin.Skin;
 import org.richfaces.skin.SkinFactory;
 import org.richfaces.util.ReferenceMap;
 
 /**
  * Renderer for SuggestionBox component.
  */
 public class SuggestionBoxRenderer extends AjaxComponentRendererBase {
 
     private static final Map<String, Pattern> tokensCache = new ReferenceMap<String, Pattern>();
 
     /**
      * Component options.
      */
     private static final String[] OPTIONS = {"popupClass", "popupStyle",
             "width", "height", "entryClass", "selectedClass", "param",
             "frequency", "minChars", "tokens", "rows", "selectValueClass", "useSuggestObjects" };
 
     /**
      * Shadow depth.
      */
     public static final int SHADOW_DEPTH = 4;
 
     /**
      * Styles.
      */
     private InternetResource[] styles = {new TemplateCSSResource(
             "org/richfaces/renderkit/html/css/suggestionbox.xcss")};
 
     /**
      * Additional scripts.
      */
     private final InternetResource[] additionalScripts = {
         new org.ajax4jsf.javascript.PrototypeScript(),
 		getResource("/org/richfaces/renderkit/html/scripts/jquery/jquery.js"),
         getResource("/org/richfaces/renderkit/html/scripts/available.js"),
         new org.ajax4jsf.javascript.SmartPositionScript(),
         getResource("/org/richfaces/renderkit/html/scripts/browser_info.js"),
         getResource("/org/richfaces/renderkit/html/scripts/utils.js"),
         getResource("/org/richfaces/renderkit/html/scripts/scriptaculous/effects.js"),
         getResource("scripts/suggestionbox.js")};
 
     /**
      * Template for table.
      */
     private PreparedTemplate body = HtmlCompiler
         .compileResource("org/richfaces/renderkit/html/templates/table.jspx");
 
     /**
      * Template for popup.
      */
     private PreparedTemplate popup = HtmlCompiler
         .compileResource("org/richfaces/renderkit/html/templates/popup.jspx");
 
     /**
      * Gets component class.
      *
      * @return component class
      */
     protected final Class getComponentClass() {
         return UISuggestionBox.class;
     }
 
     /**
      * Is render children.
      *
      * @return boolean
      */
     public final boolean getRendersChildren() {
         return true;
     }
 
     private Pattern getTokensPattern(UIComponent component) {
         //TODO nick - cache ?
         String tokens = (String) component.getAttributes().get("tokens");
         if (tokens != null && tokens.length() != 0) {
             Pattern pattern;
             
             synchronized (tokensCache) {
         		pattern = tokensCache.get(tokens);
         		if (pattern == null) {
     	            StringBuilder patternSource = new StringBuilder();
     	            char[] array = tokens.toCharArray();
     	            int l = array.length;
     	            for (int i = 0; i < l; i++) {
         	        	if (i != 0) {
         	        	    patternSource.append('|');
         	        	}
         	        	patternSource.append(Pattern.quote(String.valueOf(array[i])));
     	            }
     	        
     	            pattern = Pattern.compile(patternSource.toString());
     	            tokensCache.put(tokens, pattern);
         		}
     	    }
             
             return pattern;
         } else {
             return null;
         }
     }
     
     /**
      * Decode.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      *
      * @see org.ajax4jsf.framework.renderer.RendererBase#doDecode(
      *      javax.faces.context.FacesContext,
      *      javax.faces.component.UIComponent)
      */
     protected final void doDecode(final FacesContext context,
                             final UIComponent component) {
         String clientId = component.getClientId(context);
         Map<String, String> requestParameterMap = context.getExternalContext()
                 .getRequestParameterMap();
         String reqValue = requestParameterMap.get(clientId);
         if (reqValue != null && reqValue.equals(clientId)) {
             UISuggestionBox suggestionBox = ((UISuggestionBox) component);
             String paramName = (String) component.getAttributes().get("param");
             if (null == paramName) {
                 paramName = "inputvalue";
             }
             String elementValue = requestParameterMap.get(paramName);
             suggestionBox.setSubmitted(true);
             component.queueEvent(
                     new AjaxSuggestionEvent(component, elementValue));
 
             String[] requestedValues = null;
 
             if (suggestionBox.isUsingSuggestObjects()) {
                 String requestedParamName = paramName + "request";
                 String requestedValuesParam = requestParameterMap.get(requestedParamName);
 
                 if (requestedValuesParam != null) {
         	    String requestedString = requestedValuesParam.toString();
         	    Pattern pattern = getTokensPattern(component);
 
         	    if (pattern != null) {
         		requestedValues = pattern.split(requestedString);
         	    } else {
         		requestedValues = new String[] {requestedString};
         	    }
         	} else {
         	    //TODO nick - review together with pasha
         	}
             }
 
             suggestionBox.setSubmitedValue(elementValue, 
         	    requestedValues);
         }
     }
 
     /**
      * Encode begin.
      *
      * @param writer    {@link javax.faces.context.ResponseWriter}
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @throws IOException
      *
      * @see {@link org.ajax4jsf.framework.renderer.RendererBase#doEncodeBegin}
      */
     protected final void doEncodeBegin(final ResponseWriter writer,
                                  final FacesContext context,
                                  final UIComponent component)
             throws IOException {
         super.doEncodeBegin(writer, context, component);
         org.richfaces.component.util.FormUtil.throwEnclFormReqExceptionIfNeed(
                 context, component);
     }
 
     /**
      * Encode end.
      *
      * @param writer    {@link javax.faces.context.ResponseWriter}
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @throws IOException
      *
      * @see {@link org.ajax4jsf.framework.renderer.RendererBase#doEncodeEnd}
      */
     protected void doEncodeEnd(final ResponseWriter writer,
                                final FacesContext context,
                                final UIComponent component) throws IOException {
         UISuggestionBox suggestionBox = (UISuggestionBox) component;
         if (!suggestionBox.isSubmitted()) {
             suggestionBox.setRowIndex(-1);
             
             writer.startElement(HTML.DIV_ELEM, component);
             getUtils().encodeId(context, component);
 
             StringBuffer clazz = new StringBuffer("rich-sb-common-container ");
             clazz.append(suggestionBox.getPopupClass() + " ")
                  .append(suggestionBox.getStyleClass());
             writer.writeAttribute("class", clazz, "popupClass");
 
             int zIndex = suggestionBox.getZindex();
 
             StringBuffer style = new StringBuffer("display:none; z-index: " + (zIndex + 1) + ";");
 
             style.append(getSizeForStyle(component, "width", null, false));
             style.append(getSizeForStyle(component, "height", null, false));
             style.append(suggestionBox.getPopupStyle() + ";")
                  .append(suggestionBox.getStyle() + ";");
             
             writer.writeAttribute("style", style, "popupStyle");
 
             UIComponent popupFacet = component.getFacet("popup");
             if (null == popupFacet) {
                 popup.encode(this, context, component);
             } else {
                 // Use facet as content of popup window
                 // suggestionBox.setPopup(popupFacet.getClientId(context));
                 renderChild(context, popupFacet);
             }
             writer.endElement(HTML.DIV_ELEM);
             
             writer.startElement(HTML.DIV_ELEM, component);
             writer.writeAttribute("id", component.getClientId(context) + "_script", null);
             writer.writeAttribute("style", "display:none;", null);
             writer.startElement(HTML.SCRIPT_ELEM, component);
             writer.writeAttribute("type", "text/javascript", null);
             writer.writeText(getScript(context, component), "script");
             writer.endElement(HTML.SCRIPT_ELEM);
             writer.endElement(HTML.DIV_ELEM);
             
             writer.startElement("iframe", component);
             writer.writeAttribute("src", getResource("/org/richfaces/renderkit/html/images/spacer.gif").getUri(context, null), null);
             writer.writeAttribute("id", component.getClientId(context) + "_iframe", null);
             writer.writeAttribute("style", "position:absolute;display:none;z-index:" + zIndex + ";", null);
             writer.endElement("iframe");
 
             writer.startElement("input", component);
     		writer.writeAttribute("type", "hidden", null);
    		writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
     		writer.writeAttribute("id", component.getClientId(context) + "_selection", null);
     		writer.writeAttribute("name", component.getClientId(context) + "_selection", null);
     		writer.endElement("input");
             
         } else {
             suggestionBox.setSubmitted(false);
         }
 
         // Fix for bug CH-1323.
         ((UISuggestionBox) component).setValue(null);
     }
 
     /**
      * Encode children.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @throws IOException
      *
      * @see javax.faces.render.Renderer#encodeChildren
      */
     public void encodeChildren(final FacesContext context,
                                final UIComponent component)
             throws IOException {
         UISuggestionBox suggestionBox = (UISuggestionBox) component;
         AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
         Set<String> ajaxRenderedAreas = ajaxContext.getAjaxRenderedAreas();
         String clientId = component.getClientId(context);
         
         if (suggestionBox.isSubmitted()) {
             body.encode(getTemplateContext(context, suggestionBox));
             // Replace rendered area ID from component to suggestion table
             suggestionBox.setRowIndex(-1);
             ajaxContext.removeRenderedArea(clientId);
             ajaxContext.addRenderedArea(getContentId(context, component));
         }
         else if (ajaxContext.isAjaxRequest() && ajaxRenderedAreas.contains(clientId))
         {
         	ajaxRenderedAreas.add(clientId + "_script");
         }
     }
 
     /**
      * Gets component.
      *
      * @param component {@link javax.faces.component.UIComponent}
      * @return component
      */
     private UIComponent getTarget(final UIComponent component) {
         String target = ((UISuggestionBox) component).getFor();
         if (null != target) {
         	target = RendererUtils.getInstance().correctForIdReference(target,component);
         	// Use parent since UIData - naming container
             UIComponent targetComponent = RendererUtils.getInstance().
             	findComponentFor(component, target);
             if (null != targetComponent) {
                 return targetComponent;
             } else {
                 throw new FacesException("Component for target " + target
                         + " not found in SuggestionBox " + component.getId());
             }
         } else {
             UIComponent parent = component.getParent();
             if (parent != null) {
         	if (HtmlUtil.shouldWriteId(parent)) {
         	    return parent;
         	} else {
         	    throw new FacesException("SuggestonBox cannot be attached to the component with id = " + parent.getId() + 
         		    ", because a client identifier of the component won't be rendered onto the page. Please, set the identifier.");
         	}
             } else {
         	throw new FacesException("Parent component is null for SuggestionBox " + 
         		component.getId());
             }
         }
     }
 
     private boolean isDefOptionValue(Object value) {
     	return  value != null ? "".equals(((String)value).trim()) : true; 
     }
     
     /**
      * Gets script.
      *
      * @param context {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return script
      */
     private String getScript(final FacesContext context,
                              final UIComponent component) {
         UIComponent targetComponent = getTarget(component);
         String targetId = targetComponent.getClientId(context);
 
         Map<String, Object> attributes = component.getAttributes();
         StringBuffer script = new StringBuffer(" new ");
         // Build ajax function call
         JSFunction submitSuggest = AjaxRendererUtils.buildAjaxFunction(
                 component, context, "RichFaces.Suggestion");
 	submitSuggest.addParameter(targetId);
         submitSuggest.addParameter(component.getClientId(context));
         submitSuggest.addParameter(component.getAttributes().get("onsubmit"));
         Map<String, Object> options = AjaxRendererUtils.buildEventOptions(context, component);
         options.put("popup", component.getClientId(context));
         for (int i = 0; i < OPTIONS.length; i++) {
             String option = OPTIONS[i];
             Object value = attributes.get(option);
             
             if(option.equals("entryClass") && isDefOptionValue(value)) {
             	value = "richfaces_suggestionEntry";
             }
             
             if(option.equals("selectValueClass") && isDefOptionValue(value)) {
             	value = "richfaces_suggestionSelectValue";
             }
             
             if (null != value) {
                 if (!"frequency".equals(option) || ((Double) value).doubleValue() > 1E-9) {
                 	
                 	options.put(option, value);
                 }
             }
         }
         // If implicit ajax queue name not set, put clientId
         String implicitEventsQueue = (String) options.get("implicitEventsQueue");
         if (null == implicitEventsQueue) {
             options.put("implicitEventsQueue", component.getClientId(context));
         }
         String onselect = (String) attributes.get("onselect");
         if (null != onselect) {
             JSFunctionDefinition function = new JSFunctionDefinition("suggestion");
             function.addParameter("event");
             function.addToBody(onselect);
 
             options.put("onselect", function);
 
         }
         String onobjectchange = (String) attributes.get("onobjectchange");
         if (null != onobjectchange) {
             JSFunctionDefinition function = new JSFunctionDefinition("suggestion","event");
             function.addToBody(onobjectchange);
 
             options.put("onobjectchange", function);
 
         }
         if (component.getValueBinding("fetchValue") != null
                 || attributes.get("fetchValue") != null) {
         	Object select = attributes.get("selectValueClass");
         	if (isDefOptionValue(select)) {
         		select = "richfaces_suggestionSelectValue";
         	}
             options.put("select", select);
         }
         
         UISuggestionBox suggestionBox = (UISuggestionBox) component;
         options.put("usingSuggestObjects", suggestionBox.isUsingSuggestObjects());
 
         // pass "zindex" attribute to js though the "options" attribute
         options.put("zindex", suggestionBox.getZindex());
 
         submitSuggest.addParameter(options);
         script.append(submitSuggest.toScript()).append(";\n");
         return "Richfaces.onAvailable('" + targetId + "', function() {" + script.toString() + "});";
     }
 
     /**
      * Gets template.
      *
      * @param context {@link javax.faces.context.FacesContext}
      * @param data
      * @return {@link org.ajax4jsf.framework.renderer.compiler.TemplateContext}
      */
     private TemplateContext getTemplateContext(final FacesContext context,
                                                final UIData data) {
         data.setRowIndex(-1);
         return new DataTemplateContext(this, context, data);
     }
 
     /**
      * Special html templates context class with pre-defined properties for
      * iterations over rows and columns.
      *
      * @author shura (latest modification by $Author: alexsmirnov $)
      * @version $Revision: 1.20 $ $Date: 2007/03/01 22:37:49 $
      */
     private static class DataTemplateContext extends TemplateContext {
 
         private List<UIComponent> columns;
 
         private int first;
 
         private int last;
 
         private int rows;
 
         private int rowCount;
 
         private int current;
 
         private String[] rowClasses = new String[0];
 
         private String entryClass;
 
         /**
          * Constructor.
          *
          * @param renderer {@link org.ajax4jsf.framework.renderer.RendererBase}
          * @param facesContext {@link javax.faces.context.FacesContext}
          * @param component    {@link javax.faces.component.UIComponent}
          */
         public DataTemplateContext(final RendererBase renderer,
                                    final FacesContext facesContext,
                                    final UIComponent component) {
             super(renderer, facesContext, component);
             
             
             
             if (component.getFacet("header") != null) {
                 this.putParameter("hasHead", Boolean.TRUE);
             }
              // Fill child columns components
             columns = new ArrayList<UIComponent>(component.getChildCount());
             for (Iterator<UIComponent> iter = component.getChildren().iterator(); iter
                     .hasNext();) {
                 UIComponent column = (UIComponent) iter.next();
                 if (column instanceof UIColumn) {
                     columns.add(column);
                     if (column.getFacet("header") != null) {
                         this.putParameter("hasHead", Boolean.TRUE);
                         this.putParameter("hasColumnHead", Boolean.TRUE);
                     }
                     if (column.getFacet("footer") != null) {
                         this.putParameter("hasFooter", Boolean.TRUE);
                         this.putParameter("hasColumnFooter", Boolean.TRUE);
                     }
                 }
             }
             // fill rows counters
             UISuggestionBox box = (UISuggestionBox) component;
             this.first = box.getFirst();
             this.rows = box.getRows();
             this.rowCount = box.getRowCount();
             // return all records; CH-1330
             if (rows <= 0 || true) {
                 rows = rowCount - first;
             }
             last = first + rows;
             if (last > rowCount) {
                 last = rowCount;
             }
             current = first;
             // rows classes
             entryClass = box.getEntryClass();
             String rowClasses = box.getRowClasses();
             if (null != rowClasses && rowClasses.length() > 0) {
                 this.rowClasses = rowClasses.split("\\s+");
             }
 
         }
 
         /**
          * Gets parameter.
          *
          * @param key parameter key
          * @return parameter
          */
         public Object getParameter(Object key) {
             if ("rows".equals(key)) {
                 // Iterate over rows in datatable
                 return new Iterator() {
 
                     public boolean hasNext() {
                         if (current >= last) {
                             return false;
                         }
                         UIData data = ((UIData) getComponent());
                         data.setRowIndex(current);
                         return data.isRowAvailable();
                     }
 
                     public Object next() {
                         // TODO reset rows and columns classes counters
                         current++;
                         return getComponent();
                     }
 
                     public void remove() {
                         throw new UnsupportedOperationException(
                                 "remove row from UIData not supported");
                     }
 
                 };
             } else if ("rowClass".equals(key)) {
                 // Build row class string from entryClass and row classes
                 StringBuffer rowClass = new StringBuffer();
                 if (null != entryClass) {
                     rowClass.append(entryClass);
                     if (rowClasses.length > 0) {
                         rowClass.append(" ");
                     }
                 }
                 if (rowClasses.length > 0) {
                     int currentClass = (current - first - 1)
                             % rowClasses.length;
                     if (currentClass < 0) {
                         currentClass = 0;
                     }
                     rowClass.append(rowClasses[currentClass]);
                 }
                 // for iterate over columns
                 return rowClass.toString();
             } else if ("columns".equals(key)) {
                 // for iterate over columns
                 return columns;
             } else if ("columnsCount".equals(key)) {
                 return new Integer(columns.size());
             } else {
                 return super.getParameter(key);
             }
         }
     }
 
     /**
      * Gets opacity style.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return style
      */
     public final String opacityStyle(final FacesContext context,
                                      final UIComponent component) {
         String opacity = (String) component.getAttributes().get("shadowOpacity");
         String filterOpacity;
 
         if (null == opacity) {
             Skin skin = SkinFactory.getInstance().getSkin(context);
             opacity = (String) skin.getParameter(context, "shadowOpacity");
         }
         try {
             Double op = Double.valueOf(opacity);
             filterOpacity = Integer.toString(op.intValue() * 10);
             opacity = Double.toString(op.doubleValue() / 10);
         } catch (Exception e) {
             // illegal opacity
             return ";";
         }
         return "opacity:" + opacity
                 + "; filter:alpha(opacity=" + filterOpacity + ");";
     }
 
     /**
      * Gets border style.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return style
      */
     public final String border(final FacesContext context,
                                final UIComponent component) {
 
         String border = (String) component.getAttributes().get("border");
 
         String frame = (String) component.getAttributes().get("frame");
         if (null == frame) {
             frame = "box";
         }
         StringBuffer stringBuffer = new StringBuffer();
 
         if (null != border && Pattern.matches("\\d*", border)) {
             border += "px";
         }
 
         boolean top = false, right = false, bottom = false, left = false;
         if (frame.equalsIgnoreCase("above")) {
             top = true;
             // else if (frame.equalsIgnoreCase("border") |
             // frame.equalsIgnoreCase("box")) top=right=bottom=left=true;
         } else if (frame.equalsIgnoreCase("below")) {
             bottom = true;
         } else if (frame.equalsIgnoreCase("hsides")) {
             top = true;
             bottom = true;
         } else if (frame.equalsIgnoreCase("lhs")) {
             left = true;
         } else if (frame.equalsIgnoreCase("rhs")) {
             right = true;
         } else if (frame.equalsIgnoreCase("vsides")) {
             right = true;
             left = true;
         } else {
             top = true;
             right = true;
             bottom = true;
             left = true;
         }
         stringBuffer.append("; border-width:");
         if (top) {
             stringBuffer.append(" ").append(border).append(" ");
         } else {
             stringBuffer.append(" 0px ");
         }
         if (right) {
             stringBuffer.append(" ").append(border).append(" ");
         } else {
             stringBuffer.append(" 0px ");
         }
         if (bottom) {
             stringBuffer.append(" ").append(border).append(" ");
         } else {
             stringBuffer.append(" 0px ");
         }
         if (left) {
             stringBuffer.append(" ").append(border).append(" ");
         } else {
             stringBuffer.append(" 0px ");
         }
         stringBuffer.append(";");
         return stringBuffer.toString();
     }
 
     /**
      * Gets background-color style.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return background-color style
      */
     public final String bgcolor(final FacesContext context,
                                 final UIComponent component) {
         String bgcolor = (String) component.getAttributes().get("bgcolor");
         if (bgcolor != null) {
             return "background-color: " + bgcolor + ";";
         }
         return ";";
     }
 
     /**
      * Gets cellpadding style.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return cellpadding style
      */
     public final String cellPadding(final FacesContext context,
                                     final UIComponent component) {
         UISuggestionBox box = (UISuggestionBox) component;
         String cp = box.getCellpadding();
         if (cp != null) {
             return "padding: " + getUtils().encodePctOrPx(cp) + ";";
         }
         return ";";
     }
 
     /**
      * Gets border size
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return border size if set, 0 if none
      */
     public final String getBorder(final FacesContext context,
                                      final UIComponent component) {
 
     	String border = (String) component.getAttributes().get("border");
     	if (border == null || border.length() == 0) {
     		return "0";
     	}
     	
     	return border;
     }
 
     
     /**
      * Gets context identifier.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return context identifier
      */
     public final String getContentId(final FacesContext context,
                                      final UIComponent component) {
         return component.getClientId(context)
                 + NamingContainer.SEPARATOR_CHAR + "suggest";
     }
 
     /**
      * Gets overflow sizes.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return overflow style
      */
     public final String overflowSize(final FacesContext context,
                                      final UIComponent component) {
         StringBuffer style = new StringBuffer();
 
         style.append(getSizeForStyle(component, "width", null, true));
         style.append(getSizeForStyle(component, "height", null, true));
 
         return style.toString();
     }
 
     /**
      * Gets shadow style.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return shadow style
      */
     public final String shadowDepth(final FacesContext context,
                                     final UIComponent component) {
         String shadow = (String) component.getAttributes().get("shadowDepth");
         if (shadow == null) {
             shadow = Integer.toString(SHADOW_DEPTH);
         }
 
         return "top: " + shadow + "px; left: " + shadow +"px; ";
     }
 
     /**
      * Gets additional scripts.
      *
      * @return array of resources
      * {@link org.ajax4jsf.framework.resource.InternetResource}
      */
     protected final InternetResource[] getAdditionalScripts() {
         return additionalScripts;
     }
 
     /**
      * Gets styles.
      *
      * @return array of styles
      * {@link org.ajax4jsf.framework.resource.InternetResource}
      */
     protected final InternetResource[] getStyles() {
         return styles;
     }
 
     /**
      * Gets style for width & height.
      * @param component {@link javax.faces.component.UIComponent}
      * @param attr attribute
      * @param def default value
      * @param isShadow TRUE if shadow exists
      * @return style
      */
     private String getSizeForStyle(final UIComponent component,
                                    final String attr,
                                    final String def,
                                    final boolean isShadow) {
         Map attributes = component.getAttributes();
         StringBuffer style = new StringBuffer();
 
         String attribute = (String) attributes.get(attr);
         if (attribute == null && def != null) {
             attribute = def;
         }
 
         if (attribute != null && (!attribute.equals(""))) {
             if (isShadow) {
                 attribute = String.valueOf(Integer.parseInt(attribute)
                         - SHADOW_DEPTH);
             }
 
             style.append(attr).append(":").append(attribute);
             if (Pattern.matches("\\d*", attribute)) {
                 style.append("px");
             }
             style.append(";");
         }
 
         return style.toString();
     }
     
     public void insertNothingLabel(final FacesContext context,
                                     final UIComponent component) throws IOException {
     	ResponseWriter writer = context.getResponseWriter();
     	UISuggestionBox suggestionBox = (UISuggestionBox)component;
     	final String startHtml = 
     		"<tr id=\"" + suggestionBox.getClientId(context) + "NothingLabel\" class=\"rich-sb-int " + suggestionBox.getRowClasses() + 
     		"\" style=\"display: none;\">" +
     		"<td nowrap=\"nowrap\" class=\"rich-sb-cell-padding\" style=\"" + this.cellPadding(context, component) + "\">";
     	final String endHtml = "</td></tr>";
     	
     	UIComponent nothingLabelFacet = component.getFacet("nothingLabel"); 
     	if (nothingLabelFacet != null && nothingLabelFacet.isRendered()) {
     		writer.write(startHtml);
     		renderChild(context, nothingLabelFacet);
     		writer.write(endHtml);
     	} else {
     		String nothingLabel = suggestionBox.getNothingLabel();
 			if (nothingLabel != null && !"".equals(nothingLabel)) {
     			writer.write(startHtml);
     			writer.write(nothingLabel);
     			writer.write(endHtml);
     		}
     	}
     }
     
     /**
      * Gets 'class' attribute for suggestion entry.
      *
      * @param context   {@link javax.faces.context.FacesContext}
      * @param component {@link javax.faces.component.UIComponent}
      * @return 'class' attribute for 'tr' element of suggestion entry.
      */
     public final String getEntryClass(final FacesContext context,
                                      final UIComponent component) {
     	String entryClass = (String) component.getAttributes().get("entryClass");
     	if (isDefOptionValue(entryClass)) {
     		entryClass = "richfaces_suggestionEntry";
     	}	
     	
     	String rowClass = (String) component.getAttributes().get("rowClasses");
     	if (null == rowClass)
     		rowClass = "";
     	
         return "rich-sb-int " + entryClass + " " + rowClass;
     }
 }

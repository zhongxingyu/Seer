 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.render.html;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.faces.application.ViewHandler;
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIForm;
 import javax.faces.component.UIParameter;
 import javax.faces.component.html.HtmlCommandLink;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import javax.faces.internal.IgnoreAttribute;
 import javax.faces.internal.UIComponentUtil;
 
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.AbstractRenderer;
 import org.seasar.teeda.core.util.ExternalContextUtil;
 import org.seasar.teeda.core.util.FacesContextUtil;
 import org.seasar.teeda.core.util.HtmlFormRendererUtil;
 import org.seasar.teeda.core.util.JavaScriptPermissionUtil;
 import org.seasar.teeda.core.util.JavaScriptUtil;
 import org.seasar.teeda.core.util.RendererUtil;
 
 /**
  * @author manhole
  * @author shot
  */
 public class HtmlCommandLinkRenderer extends AbstractRenderer {
 
     public static final String COMPONENT_FAMILY = "javax.faces.Command";
 
     public static final String RENDERER_TYPE = "javax.faces.Link";
 
     private static final String HIDDEN_FIELD_NAME_SUFFIX = "__link_clicked__";
 
     private final IgnoreAttribute ignoreComponent = new IgnoreAttribute();
     {
         ignoreComponent.addAttributeName(JsfConstants.ID_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.VALUE_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.ONCLICK_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.ACTION_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.IMMEDIATE_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.HREF_ATTR);
     }
 
     public void encodeBegin(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlCommandLinkBegin(context, (HtmlCommandLink) component);
     }
 
     protected void encodeHtmlCommandLinkBegin(FacesContext context,
             HtmlCommandLink commandLink) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement(JsfConstants.ANCHOR_ELEM, commandLink);
         RendererUtil.renderIdAttributeIfNecessary(writer, commandLink,
                 getIdForRender(context, commandLink));
 
         if (JavaScriptPermissionUtil.isJavaScriptPermitted(context)) {
             encodeHtmlCommandLinkWithJavaScript(context, writer, commandLink);
         } else {
             encodeHtmlCommandLinkWithoutJavaScript(context, writer, commandLink);
         }
     }
 
     protected void encodeHtmlCommandLinkWithJavaScript(FacesContext context,
             ResponseWriter writer, HtmlCommandLink commandLink)
             throws IOException {
         final UIForm parentForm = UIComponentUtil.findParentForm(commandLink);
         final String formId = getIdForRender(context, parentForm);
 
         RendererUtil.renderAttribute(writer, JsfConstants.HREF_ATTR, "#");
 
         final StringBuffer sb = new StringBuffer(320);
         final String onclick = commandLink.getOnclick();
         if (onclick != null) {
             sb.append(onclick);
             if (!onclick.endsWith(";")) {
                 sb.append(";");
             }
         }
        final String formName = parentForm.getId();
         final String functionName = JavaScriptUtil
                .getClearHiddenCommandFormParamsFunctionName(formName) +
                "();";
         sb.append(functionName).append("var f = document.forms['").append(
                 formId).append("'];");
 
         final String hiddenFieldName = getHiddenFieldName(formId);
         sb.append(" f['").append(hiddenFieldName).append("'].value = '")
                 .append(commandLink.getClientId(context)).append("';");
 
         HtmlFormRenderer.setCommandLinkHiddenParameter(parentForm,
                 hiddenFieldName, null);
 
         for (final Iterator it = commandLink.getChildren().iterator(); it
                 .hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof UIParameter) {
                 final UIParameter p = ((UIParameter) child);
                 final String name = p.getName();
                 final Object value = p.getValue();
                 sb.append(" f['" + name + "'].value = '").append(
                         String.valueOf(value)).append("';");
                 HtmlFormRenderer.setCommandLinkHiddenParameter(parentForm,
                         name, null);
             }
         }
 
         final String target = commandLink.getTarget();
         if (target != null && target.trim().length() > 0) {
             sb.append(" f.target = '").append(target).append("';");
         }
 
         sb.append(" if (f.onsubmit) { f.onsubmit(); } f.submit();").append(
                 functionName).append(" return false;");
         RendererUtil.renderAttribute(writer, JsfConstants.ONCLICK_ATTR, sb
                 .toString());
 
         //renderRemainAttributes(commandLink, writer);
         renderRemainAttributes(commandLink, writer, ignoreComponent);
 
         Object value = commandLink.getValue();
         if (value != null) {
             writer.writeText(value.toString(), JsfConstants.VALUE_ATTR);
         }
     }
 
     protected void encodeHtmlCommandLinkWithoutJavaScript(FacesContext context,
             ResponseWriter writer, HtmlCommandLink commandLink)
             throws IOException {
         //TODO PortletSupport
         ViewHandler viewHandler = FacesContextUtil.getViewHandler(context);
         String viewId = context.getViewRoot().getViewId();
         String path = viewHandler.getActionURL(context, viewId);
         StringBuffer hrefBuf = new StringBuffer(100);
         hrefBuf.append(path);
         if (path.indexOf("?") == -1) {
             hrefBuf.append("?");
         } else {
             hrefBuf.append("&");
         }
         UIForm parentForm = UIComponentUtil.findParentForm(commandLink);
 
         final String formSubmitKey = HtmlFormRendererUtil.getFormSubmitKey(
                 context, parentForm);
         hrefBuf.append(formSubmitKey).append("=").append(formSubmitKey).append(
                 "&");
 
         final String formId = getIdForRender(context, parentForm);
         final String hiddenFieldName = getHiddenFieldName(formId);
         hrefBuf.append(hiddenFieldName).append("=").append(
                 commandLink.getClientId(context));
         if (commandLink.getChildCount() > 0) {
             addChildParametersToHref(commandLink, hrefBuf, writer
                     .getCharacterEncoding());
         }
 
         String href = ExternalContextUtil.encodeActionURL(context, hrefBuf
                 .toString());
         writer.writeURIAttribute(JsfConstants.HREF_ATTR, href, null);
         Object value = commandLink.getValue();
         if (value != null) {
             writer.writeText(value.toString(), JsfConstants.VALUE_ATTR);
         }
     }
 
     private void addChildParametersToHref(UIComponent linkComponent,
             StringBuffer hrefBuf, String charEncoding) throws IOException {
         for (Iterator it = linkComponent.getChildren().iterator(); it.hasNext();) {
             UIComponent child = (UIComponent) it.next();
             if (child instanceof UIParameter) {
                 UIParameter param = (UIParameter) child;
                 String name = param.getName();
                 Object value = param.getValue();
                 addParameterToHref(name, value, hrefBuf, charEncoding);
             }
         }
     }
 
     private static void addParameterToHref(String name, Object value,
             StringBuffer hrefBuf, String charEncoding)
             throws UnsupportedEncodingException {
         if (name == null) {
             throw new IllegalArgumentException(
                     "Unnamed parameter value not allowed within command link.");
         }
         hrefBuf.append("&");
         hrefBuf.append(URLEncoder.encode(name, charEncoding));
         hrefBuf.append("=");
         if (value != null) {
             hrefBuf.append(URLEncoder.encode(value.toString(), charEncoding));
         }
     }
 
     private String getHiddenFieldName(final String formId) {
        return formId + NamingContainer.SEPARATOR_CHAR +
                HIDDEN_FIELD_NAME_SUFFIX;
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlCommandLinkEnd(context, (HtmlCommandLink) component);
     }
 
     protected void encodeHtmlCommandLinkEnd(FacesContext context,
             HtmlCommandLink commandLink) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.endElement(JsfConstants.ANCHOR_ELEM);
     }
 
     public void decode(FacesContext context, UIComponent component) {
         assertNotNull(context, component);
         decodeHtmlCommandLink(context, (HtmlCommandLink) component);
     }
 
     protected void decodeHtmlCommandLink(FacesContext context,
             HtmlCommandLink commandLink) {
         Map paramMap = context.getExternalContext().getRequestParameterMap();
         String clientId = commandLink.getClientId(context);
 
         UIForm parentForm = UIComponentUtil.findParentForm(commandLink);
         String formId = getIdForRender(context, parentForm);
         String hiddenFieldName = getHiddenFieldName(formId);
         String entry = (String) paramMap.get(hiddenFieldName);
         if (clientId.equals(entry)) {
             commandLink.queueEvent(new ActionEvent(commandLink));
         }
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
 }

 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.extension.render.html;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.component.html.HtmlOutputLink;
 import javax.faces.context.FacesContext;
 
 import org.seasar.teeda.core.render.html.HtmlOutputLinkRenderer;
 import org.seasar.teeda.core.render.html.support.PortletUrlBuilder;
 import org.seasar.teeda.core.util.PortletUtil;
 import org.seasar.teeda.core.util.ValueHolderUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 
 /**
  * @author shot
  * @author higa
  */
 public class THtmlOutputLinkRenderer extends HtmlOutputLinkRenderer {
 
     public static final String COMPONENT_FAMILY = "javax.faces.Output";
 
     public static final String RENDERER_TYPE = "org.seasar.teeda.extension.Link";
 
     protected String buildHref(FacesContext context,
             HtmlOutputLink htmlOutputLink, String encoding) throws IOException {
         if (PortletUtil.isPortlet(context)) {
            if (htmlOutputLink.getId().startsWith(ExtensionConstants.GO_PREFIX)) {
                 PortletUrlBuilder urlBuilder = new PortletUrlBuilder();
                 urlBuilder.setBase(ValueHolderUtil.getValueForRender(context,
                         htmlOutputLink));
                 for (Iterator it = htmlOutputLink.getChildren().iterator(); it
                         .hasNext();) {
                     UIComponent child = (UIComponent) it.next();
                     if (child instanceof UIParameter) {
                         UIParameter parameter = (UIParameter) child;
                         urlBuilder.add(URLEncoder.encode(parameter.getName(),
                                 encoding), URLEncoder.encode(
                                 toBlankIfNull(parameter.getValue()), encoding));
                     }
                 }
                 //TODO newwindow on portal??
                 //                if (WindowIdUtil.isNewWindowTarget(htmlOutputLink.getTarget())) {
                 //                    urlBuilder.add(URLEncoder.encode(WindowIdUtil.NEWWINDOW, encoding),
                 //                            URLEncoder.encode(JsfConstants.TRUE, encoding));
                 //                }
 
                 return context.getExternalContext().encodeResourceURL(
                         urlBuilder.build());
             }
         }
         return super.buildHref(context, htmlOutputLink, encoding);
     }
}

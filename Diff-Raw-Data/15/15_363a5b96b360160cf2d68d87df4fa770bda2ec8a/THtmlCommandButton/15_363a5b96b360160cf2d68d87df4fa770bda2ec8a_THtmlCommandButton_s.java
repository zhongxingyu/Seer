 /*
  * Copyright 2004-2010 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.extension.component.html;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlCommandButton;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.internal.RenderPreparable;
 import javax.faces.internal.RenderPreparableUtil;
 import javax.faces.internal.scope.RedirectScope;
 import javax.faces.internal.scope.SubApplicationScope;
 
 import org.seasar.teeda.core.util.NavigationHandlerUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.component.TViewRoot;
 import org.seasar.teeda.extension.exception.DoubleSubmittedException;
 import org.seasar.teeda.extension.util.TransactionTokenUtil;
 
 /**
  * @author higa
  * @author shot
  */
 public class THtmlCommandButton extends HtmlCommandButton implements
         RenderPreparable {
 
     public static final String COMPONENT_TYPE = "org.seasar.teeda.extension.HtmlCommandButton";
 
     public static final String DEFAULT_RENDERER_TYPE = "org.seasar.teeda.extension.HtmlCommandButton";
 
     private static final long TIME_DEFAULT = 50000;
 
     private Boolean renderJs = null;
 
     private Long time;
 
     private String baseViewId;
 
     public THtmlCommandButton() {
         super();
         setRendererType(DEFAULT_RENDERER_TYPE);
     }
 
     public void setParent(UIComponent parent) {
         super.setParent(parent);
         if (baseViewId == null) {
             while (parent != null) {
                 if (parent instanceof TViewRoot) {
                     baseViewId = ((TViewRoot) parent).getViewId();
                     break;
                 }
                 parent = parent.getParent();
             }
         }
     }
 
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         if (isDoubleSubmitted()) {
             return;
         }
         super.broadcast(event);
     }
 
     protected boolean isDoubleSubmitted() {
         final FacesContext context = FacesContext.getCurrentInstance();
         if (!TransactionTokenUtil.isDoOnce(getId())) {
             TransactionTokenUtil.resetContext(context);
             return false;
         }
         if (TransactionTokenUtil.verify(context)) {
             return false;
         }
         if (!TransactionTokenUtil.isPrevious(context)) {
             throw new DoubleSubmittedException(context.getViewRoot()
                     .getViewId(), getId());
         }
         if (SubApplicationScope.getContext(context) == null) {
             throw new DoubleSubmittedException(context.getViewRoot()
                     .getViewId(), getId());
         }
         String path = RedirectScope.getRedirectingPath(context);
         if (path == null) {
             path = RedirectScope.getRedirectedPath(context);
         }
         if (path == null) {
             throw new DoubleSubmittedException(context.getViewRoot()
                     .getViewId(), getId());
         }
         NavigationHandlerUtil.redirect(context, path);
         return true;
     }
 
     public boolean isRenderJs() {
         if (renderJs != null) {
             return renderJs.booleanValue();
         }
         ValueBinding vb = getValueBinding(ExtensionConstants.RENDERJS_ATTR);
         Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return v != null ? v.booleanValue() : false;
     }
 
     public void setRenderJs(boolean renderJs) {
         this.renderJs = new Boolean(renderJs);
     }
 
     public String getBaseViewId() {
         return baseViewId;
     }
 
     public void setBaseViewId(String baseViewId) {
         this.baseViewId = baseViewId;
     }
 
     public long getTime() {
         if (time != null) {
             return time.longValue();
         }
         ValueBinding vb = getValueBinding(ExtensionConstants.TIME_ATTR);
         Long v = vb != null ? (Long) vb.getValue(getFacesContext()) : null;
         return v != null ? v.longValue() : TIME_DEFAULT;
     }
 
     public void setTime(long time) {
         this.time = new Long(time);
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         renderJs = (Boolean) values[1];
         baseViewId = (String) values[2];
     }
 
     public Object saveState(FacesContext context) {
        Object values[] = new Object[3];
         values[0] = super.saveState(context);
         values[1] = renderJs;
         values[2] = baseViewId;
         return values;
     }
 
     public void preEncodeBegin(FacesContext context) throws IOException {
         RenderPreparableUtil.encodeBeforeForRenderer(context, this);
     }
 
     public void postEncodeEnd(FacesContext context) throws IOException {
     }
 
 }

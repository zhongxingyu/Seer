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
 package org.seasar.teeda.extension.component;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.component.ComponentUtil_;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIComponentBase;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.PhaseId;
 import javax.faces.internal.FacesMessageUtil;
 import javax.faces.internal.PhaseUtil;
 import javax.faces.internal.scope.PageScope;
 
 import org.seasar.framework.util.AssertionUtil;
import org.seasar.teeda.core.util.PostbackUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 
 /**
  * @author shot
  */
 public class TCondition extends UIComponentBase {
 
     public static final String COMPONENT_TYPE = "org.seasar.teeda.extension.Condition";
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.Condition";
 
     private static final String DEFAULT_RENDERER_TYPE = "org.seasar.teeda.extension.Condition";
 
     private static final int CONDITION_SIZE = 17;
 
     private Boolean renderSpan = null;
 
     private Boolean refresh;
 
     private Boolean invisible;
 
     private Boolean omittag;
 
     private Set encodedClientId = new HashSet();
 
     public TCondition() {
         super.setRendererType(DEFAULT_RENDERER_TYPE);
     }
 
     public boolean isRendered() {
         final PhaseId phaseId = PhaseUtil.getCurrentPhase();
         if (phaseId != null && phaseId.equals(PhaseId.RENDER_RESPONSE)) {
             final FacesContext context = FacesContext.getCurrentInstance();
             final String clientId = getClientId(context);
             if (!encodedClientId.contains(clientId)) {
                 encodedClientId.add(clientId);
                final Boolean condition = getEncodedCondition();
                if (condition == null) {
                    final boolean b = super.isRendered();
                    saveEncodedCondition(b);
                    return b;
                }
                final boolean postback = PostbackUtil.isPostback(context
                        .getExternalContext().getRequestMap());
                 final boolean noError = !FacesMessageUtil
                         .hasErrorOrFatalMessage(context);
                 final boolean isRefresh = (refresh != null) ? refresh
                         .booleanValue() : false;
                if (!postback || noError || isRefresh) {
                     clearEncodedCondition();
                     final boolean b = super.isRendered();
                     saveEncodedCondition(b);
                     return b;
                 }
             }
         }
         final Boolean condition = getEncodedCondition();
         if (condition != null) {
             return condition.booleanValue();
         }
         return super.isRendered();
     }
 
     protected Boolean removeEncodedCondition() {
         FacesContext context = FacesContext.getCurrentInstance();
         Map conditions = getConditions(context);
         if (conditions == null) {
             return null;
         }
         return (Boolean) conditions.remove(getClientId(context));
     }
 
     protected Boolean getEncodedCondition() {
         FacesContext context = FacesContext.getCurrentInstance();
         Map conditions = getConditions(context);
         if (conditions == null) {
             return null;
         }
         return (Boolean) conditions.get(getClientId(context));
     }
 
     protected Map getConditions(FacesContext context) {
         Map map = PageScope.getContext(context);
         if (map == null) {
             return null;
         }
         return (Map) map.get(COMPONENT_TYPE);
     }
 
     protected Map getOrCreateConditions(FacesContext context) {
         Map map = PageScope.getOrCreateContext(context);
         Map conditions = (Map) map.get(COMPONENT_TYPE);
         if (conditions == null) {
             conditions = new HashMap(CONDITION_SIZE);
             map.put(COMPONENT_TYPE, conditions);
         }
         return conditions;
     }
 
     protected void clearEncodedCondition() {
         final FacesContext context = getFacesContext();
         final Map conditions = getConditions(context);
         if (conditions == null) {
             return;
         }
         final String cid = getClientId(context);
         conditions.remove(cid);
     }
 
     protected void saveEncodedCondition(boolean rendered) {
         final FacesContext context = getFacesContext();
         final Map conditions = getOrCreateConditions(context);
         final Boolean b = Boolean.valueOf(rendered);
         final String cid = getClientId(context);
         conditions.put(cid, b);
     }
 
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public void processDecodes(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         decode(context);
         processAppropriateAction(context, PhaseId.APPLY_REQUEST_VALUES);
     }
 
     public void processUpdates(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         processAppropriateAction(context, PhaseId.UPDATE_MODEL_VALUES);
     }
 
     public void processValidators(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         processAppropriateAction(context, PhaseId.PROCESS_VALIDATIONS);
     }
 
     protected void processAppropriateAction(FacesContext context, PhaseId phase) {
         if (!isRendered()) {
             return;
         }
         for (Iterator children = getFacetsAndChildren(); children.hasNext();) {
             UIComponent component = (UIComponent) children.next();
             ComponentUtil_.processAppropriatePhaseAction(context, component,
                     phase);
         }
     }
 
     public boolean isRenderSpan() {
         if (renderSpan != null) {
             return renderSpan.booleanValue();
         }
         ValueBinding vb = getValueBinding(ExtensionConstants.RENDERSPAN_ATTR);
         Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return v != null ? v.booleanValue() : false;
     }
 
     public void setRenderSpan(boolean renderSpan) {
         this.renderSpan = new Boolean(renderSpan);
     }
 
     public boolean isRefresh() {
         if (refresh != null) {
             return refresh.booleanValue();
         }
         ValueBinding vb = getValueBinding("refresh");
         Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return v != null ? v.booleanValue() : false;
     }
 
     public void setRefresh(boolean ref) {
         this.refresh = new Boolean(ref);
     }
 
     public boolean isInvisible() {
         if (invisible != null) {
             return invisible.booleanValue();
         }
         ValueBinding vb = getValueBinding(ExtensionConstants.INVISIBLE_ATTR);
         Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return v != null ? v.booleanValue() : false;
     }
 
     public void setInvisible(boolean invisible) {
         this.invisible = Boolean.valueOf(invisible);
     }
 
     public boolean isOmittag() {
         if (omittag != null) {
             return omittag.booleanValue();
         }
         ValueBinding vb = getValueBinding("omittag");
         Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return v != null ? v.booleanValue() : false;
     }
 
     public void setOmittag(boolean omittag) {
         this.omittag = new Boolean(omittag);
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         renderSpan = (Boolean) values[1];
         refresh = (Boolean) values[2];
         omittag = (Boolean) values[3];
     }
 
     public Object saveState(FacesContext context) {
         Object[] values = new Object[4];
         values[0] = super.saveState(context);
         values[1] = renderSpan;
         values[2] = refresh;
         values[3] = omittag;
         return values;
     }
 
 }

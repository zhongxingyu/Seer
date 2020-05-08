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
 package javax.faces.component;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.FacesListener;
 import javax.faces.internal.AttachedObjectStateWrapper;
 import javax.faces.internal.ComponentAttributesMap;
 import javax.faces.internal.ComponentChildrenListWrapper;
 import javax.faces.internal.ComponentFacetAndChildrenIterator;
 import javax.faces.internal.ComponentFacetMapWrapper;
 import javax.faces.internal.SerializableStateHolder;
 import javax.faces.internal.UIComponentUtil;
 import javax.faces.render.Renderer;
 
 import org.seasar.framework.util.AssertionUtil;
 
 /**
  * @author shot
  * @author manhole
  */
 public abstract class UIComponentBase extends UIComponent {
 
     private String id;
 
     private ComponentAttributesMap attributesMap = null;
 
     private Map bindingMap = new HashMap();
 
     private UIComponent parent;
 
     private String clientId = null;
 
     private Boolean isRendered;
 
     private String renderType = null;
 
     private List childrenList = null;
 
     private Map facetMap = null;
 
     private List listeners = null;
 
     private boolean isTransient = false;
 
     private String uniqueId;
 
     private static final String RENDERED = "rendered";
 
     private static final String RENDERER_TYPE = "rendererType";
 
     private static final int LIST_NULL_SIZE = 0;
 
     public Map getAttributes() {
         if (attributesMap == null) {
             attributesMap = new ComponentAttributesMap(this);
         }
         return attributesMap;
     }
 
     public ValueBinding getValueBinding(String name) {
         AssertionUtil.assertNotNull("name", name);
         return (ValueBinding) bindingMap.get(name);
     }
 
     public void setValueBinding(String name, ValueBinding binding) {
         AssertionUtil.assertNotNull("name", name);
         if (name.equals("id") || name.equals("parent")) {
             throw new IllegalArgumentException("invalid name is specified");
         }
         if (binding != null) {
             bindingMap.put(name, binding);
         } else {
             bindingMap.remove(name);
         }
     }
 
     public String getClientId(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (clientId == null) {
             clientId = createClientId(context);
         }
         return clientId;
     }
 
     private String createClientId(FacesContext context) {
         String parentId = "";
         for (UIComponent component = getParent(); component != null; component = component
                 .getParent()) {
             if (component instanceof NamingContainer) {
                 parentId = component.getClientId(context)
                         + NamingContainer.SEPARATOR_CHAR;
                 break;
             }
         }
 
         String clientId = parentId
                 + context.getExternalContext().encodeNamespace(
                         (id != null) ? id : getUniqueId(context));
         Renderer renderer = getRenderer(context);
         if (renderer != null) {
             clientId = renderer.convertClientId(context, clientId);
         }
         return clientId;
     }
 
     private String getUniqueId(FacesContext context) {
         if (uniqueId == null) {
             uniqueId = context.getViewRoot().createUniqueId();
         }
         return uniqueId;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         validateId(id);
         this.id = id;
         clientId = null;
     }
 
     public UIComponent getParent() {
         return parent;
     }
 
     public void setParent(UIComponent parent) {
         this.parent = parent;
     }
 
     public boolean isRendered() {
         if (isRendered != null) {
             return isRendered.booleanValue();
         }
         Boolean v = null;
         ValueBinding vb = getValueBinding(RENDERED);
         if (vb != null) {
             v = ((Boolean) this.getValueFromBinding(vb));
         }
         if (v != null) {
             return v.booleanValue();
         } else {
             return true;
         }
     }
 
     public void setRendered(boolean isRendered) {
         this.isRendered = Boolean.valueOf(isRendered);
     }
 
     public String getRendererType() {
         if (renderType != null) {
             return renderType;
         }
         ValueBinding vb = getValueBinding(RENDERER_TYPE);
         String result = null;
         if (vb != null) {
             result = (String) this.getValueFromBinding(vb);
         }
         return result;
     }
 
     public void setRendererType(String renderType) {
         this.renderType = renderType;
     }
 
     private Object getValueFromBinding(ValueBinding vb) {
         return vb.getValue(getFacesContext());
     }
 
     public boolean getRendersChildren() {
         boolean result = false;
         Renderer renderer = getRenderer(getFacesContext());
         if (getRendererType() != null && renderer != null) {
             result = renderer.getRendersChildren();
         }
         return result;
     }
 
     public List getChildren() {
         if (childrenList == null) {
             childrenList = new ComponentChildrenListWrapper(this);
         }
         return childrenList;
     }
 
     public int getChildCount() {
         return (childrenList != null) ? childrenList.size() : LIST_NULL_SIZE;
     }
 
     public UIComponent findComponent(String expr) {
         AssertionUtil.assertNotNull("expr", expr);
         UIComponent base = this;
         if (expr.charAt(0) == NamingContainer.SEPARATOR_CHAR) {
             getComponentRoot(base);
             expr = expr.substring(1);
         } else {
             for (; base.getParent() != null; base = base.getParent()) {
                 if (base instanceof NamingContainer) {
                     break;
                 }
             }
         }
 
         UIComponent result = null;
         while (expr.length() > 0) {
             String id = null;
             int separator = expr.indexOf(NamingContainer.SEPARATOR_CHAR);
             if (separator >= 0) {
                 id = expr.substring(0, separator);
                 expr = expr.substring(separator + 1);
             } else {
                 id = expr;
                 expr = null;
             }
 
             result = findComponent(base, id);
             if (result == null || expr == null) {
                 break;
             }
             if (result instanceof NamingContainer) {
                 result = result.findComponent(expr);
                 break;
             } else {
                 throw new IllegalArgumentException(id);
             }
         }
         return result;
     }
 
     private UIComponent findComponent(UIComponent base, String id) {
         if (containsSameId(base, id)) {
             return base;
         }
 
         UIComponent child = null;
         UIComponent result = null;
         for (Iterator itr = base.getFacetsAndChildren(); itr.hasNext();) {
             child = (UIComponent) itr.next();
             if (!(child instanceof NamingContainer)) {
                 result = findComponent(child, id);
                 if (result != null) {
                     break;
                 }
             } else if (containsSameId(child, id)) {
                 result = child;
                 break;
             }
         }
         return result;
     }
 
     protected boolean containsSameId(UIComponent base, String id) {
         return id.equals(base.getId());
     }
 
     protected UIComponent getComponentRoot(UIComponent base) {
         UIComponent parent;
         for (parent = base; parent.getParent() != null; parent = parent
                 .getParent()) {
             ;
         }
         return parent;
     }
 
     public Map getFacets() {
         if (facetMap == null) {
             facetMap = new ComponentFacetMapWrapper(this);
         }
         return facetMap;
     }
 
     public UIComponent getFacet(String name) {
         return (facetMap != null) ? (UIComponent) facetMap.get(name) : null;
     }
 
     public Iterator getFacetsAndChildren() {
         return new ComponentFacetAndChildrenIterator(facetMap, childrenList);
     }
 
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         AssertionUtil.assertNotNull("event", event);
         if (listeners != null) {
             for (Iterator itr = listeners.iterator(); itr.hasNext();) {
                 FacesListener listener = (FacesListener) itr.next();
                 if (event.isAppropriateListener(listener)) {
                     event.processListener(listener);
                 }
             }
         }
     }
 
     public void decode(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         Renderer renderer = getRendererForEncodeOrDecode(context);
         renderer.decode(context, this);
     }
 
     public void encodeBegin(FacesContext context) throws IOException {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
 
         Renderer renderer = getRendererForEncodeOrDecode(context);
         renderer.encodeBegin(context, this);
     }
 
     public void encodeChildren(FacesContext context) throws IOException {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         Renderer renderer = getRendererForEncodeOrDecode(context);
         renderer.encodeChildren(context, this);
     }
 
     public void encodeEnd(FacesContext context) throws IOException {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         Renderer renderer = getRendererForEncodeOrDecode(context);
         renderer.encodeEnd(context, this);
     }
 
     private Renderer getRendererForEncodeOrDecode(FacesContext context) {
         Renderer renderer = getRenderer(context);
         if (renderer == null) {
             renderer = new NullRenderer();
         }
         return renderer;
     }
 
     protected void addFacesListener(FacesListener listener) {
         AssertionUtil.assertNotNull("listener", listener);
         if (listeners == null) {
             listeners = new ArrayList();
         }
         listeners.add(listener);
     }
 
     protected FacesListener[] getFacesListeners(Class clazz) {
         AssertionUtil.assertNotNull("class", clazz);
         if (!FacesListener.class.isAssignableFrom(clazz)) {
             throw new IllegalArgumentException(clazz.getName()
                     + " is not FacesListener");
         }
 
         if (listeners == null) {
             return (FacesListener[]) Array.newInstance(clazz, 0);
         }
 
         List result = new ArrayList();
         FacesListener listener = null;
         for (Iterator itr = listeners.iterator(); itr.hasNext();) {
             listener = (FacesListener) itr.next();
             if (clazz.isAssignableFrom(listener.getClass())) {
                 result.add(listener);
             }
         }
 
         Object[] args = (Object[]) Array.newInstance(clazz, result.size());
         return (FacesListener[]) result.toArray(args);
     }
 
     protected void removeFacesListener(FacesListener listener) {
         AssertionUtil.assertNotNull("listener", listener);
 
         if (listeners != null) {
             listeners.remove(listener);
         }
     }
 
     public void queueEvent(FacesEvent event) {
         AssertionUtil.assertNotNull("event", event);
         UIComponent parent = getParent();
         if (parent == null) {
             throw new IllegalStateException("parent not found");
         }
         parent.queueEvent(event);
     }
 
     public void processDecodes(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         UIComponent component = null;
         for (Iterator itr = getFacetsAndChildren(); itr.hasNext();) {
             component = (UIComponent) itr.next();
             component.processDecodes(context);
         }
         try {
             decode(context);
         } catch (RuntimeException e) {
             context.renderResponse();
             throw e;
         }
     }
 
     public void processValidators(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         UIComponent component = null;
         for (Iterator itr = getFacetsAndChildren(); itr.hasNext();) {
             component = (UIComponent) itr.next();
             component.processValidators(context);
         }
     }
 
     public void processUpdates(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         UIComponent component = null;
         for (Iterator itr = getFacetsAndChildren(); itr.hasNext();) {
             component = (UIComponent) itr.next();
             component.processUpdates(context);
         }
     }
 
     public Object processSaveState(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
 
         if (isTransient()) {
             return null;
         }
 
         Map facetMap = new HashMap();
         for (Iterator itr = getFacets().entrySet().iterator(); itr.hasNext();) {
             Map.Entry entry = (Map.Entry) itr.next();
             String key = (String) entry.getKey();
             UIComponent component = (UIComponent) entry.getValue();
             if (!component.isTransient()) {
                 facetMap.put(key, component.processSaveState(context));
             }
         }
 
         List children = new ArrayList();
         for (Iterator itr = getChildren().iterator(); itr.hasNext();) {
             UIComponent component = (UIComponent) itr.next();
             if (!component.isTransient()) {
                 children.add(component.processSaveState(context));
             }
         }
 
         return new SerializableStateHolder(saveState(context), facetMap,
                 children);
     }
 
     public void processRestoreState(FacesContext context, Object state) {
         AssertionUtil.assertNotNull("context", context);
         if (!(state instanceof SerializableStateHolder)) {
             throw new IllegalArgumentException();
         }
 
         SerializableStateHolder holder = (SerializableStateHolder) state;
 
         Object thisState = holder.getState();
         Map facetMap = holder.getFacetMap();
         List children = holder.getChildren();
 
         for (Iterator itr = getFacets().entrySet().iterator(); itr.hasNext();) {
             Map.Entry entry = (Map.Entry) itr.next();
            Object facetState = facetMap.get(entry.getKey());
            if (facetState != null) {
                UIComponent component = (UIComponent) entry.getValue();
                component.processRestoreState(context, facetState);
             }
         }
 
         int index = 0;
        for (Iterator itr = getChildren().iterator(); itr.hasNext(); index++) {
            Object childrenState = children.get(index);
            if (childrenState != null) {
                UIComponent component = (UIComponent) itr.next();
                component.processRestoreState(context, childrenState);
             }
         }
 
         restoreState(context, thisState);
     }
 
     protected FacesContext getFacesContext() {
         return FacesContext.getCurrentInstance();
     }
 
     protected Renderer getRenderer(FacesContext context) {
         return UIComponentUtil.getRenderer(context, this);
     }
 
     public boolean isTransient() {
         return isTransient;
     }
 
     public void setTransient(boolean transientFlag) {
         isTransient = transientFlag;
     }
 
     public Object saveState(FacesContext context) {
         Object[] values = new Object[7];
         values[0] = id;
         values[1] = isRendered;
         values[2] = renderType;
         values[3] = clientId;
         values[4] = saveAttributesMap();
         values[5] = saveAttachedState(context, listeners);
         values[6] = saveValueBindingMap(context);
 
         return (Object) values;
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object[] values = (Object[]) state;
         id = (String) values[0];
         isRendered = (Boolean) values[1];
         renderType = (String) values[2];
         clientId = (String) values[3];
         restoreAttributeMap(values[4]);
         listeners = (List) restoreAttachedState(context, values[5]);
         restoreValueBindingMap(context, values[6]);
     }
 
     public static Object saveAttachedState(FacesContext context,
             Object attachedObject) {
         AssertionUtil.assertNotNull("context", context);
 
         if (attachedObject == null) {
             return null;
         }
 
         if (attachedObject instanceof List) {
             List attachedList = (List) attachedObject;
             List resultList = new ArrayList(attachedList.size());
             for (Iterator itr = attachedList.iterator(); itr.hasNext();) {
                 Object obj = itr.next();
                 resultList.add(new AttachedObjectStateWrapper(context, obj));
             }
             return resultList;
         } else {
             return new AttachedObjectStateWrapper(context, attachedObject);
         }
     }
 
     public static Object restoreAttachedState(FacesContext context,
             Object stateObject) {
         AssertionUtil.assertNotNull("context", context);
 
         if (stateObject == null) {
             return null;
         }
 
         if (stateObject instanceof List) {
             List list = (List) stateObject;
             List restoredList = new ArrayList(list.size());
             for (Iterator itr = list.iterator(); itr.hasNext();) {
                 AttachedObjectStateWrapper wrapper = (AttachedObjectStateWrapper) itr
                         .next();
                 Object restoredObject = wrapper.restore(context);
                 if (restoredObject != null) {
                     restoredList.add(restoredObject);
                 }
             }
             return restoredList;
         } else if (stateObject instanceof AttachedObjectStateWrapper) {
             AttachedObjectStateWrapper wrapper = (AttachedObjectStateWrapper) stateObject;
             return wrapper.restore(context);
         } else {
             throw new IllegalStateException("Unsupported:" + stateObject);
         }
     }
 
     private Object saveAttributesMap() {
         return (attributesMap != null) ? attributesMap.getAttributesActual()
                 : null;
     }
 
     private void restoreAttributeMap(Object state) {
         if (state != null) {
             attributesMap = new ComponentAttributesMap(this, (Map) state);
         } else {
             clearAttributeMap();
         }
     }
 
     private Object saveValueBindingMap(FacesContext context) {
         Map states = null;
         if (bindingMap != null) {
             states = new HashMap();
             for (Iterator itr = bindingMap.entrySet().iterator(); itr.hasNext();) {
                 Map.Entry entry = (Map.Entry) itr.next();
                 states.put(entry.getKey(), saveAttachedState(context, entry
                         .getValue()));
             }
         }
         return states;
     }
 
     private void restoreValueBindingMap(FacesContext context, Object state) {
         bindingMap = null;
         if (state != null) {
             Map stateMap = (Map) state;
             bindingMap = new HashMap();
             for (Iterator itr = stateMap.entrySet().iterator(); itr.hasNext();) {
                 Map.Entry entry = (Map.Entry) itr.next();
                 bindingMap.put(entry.getKey(), restoreAttachedState(context,
                         entry.getValue()));
             }
         }
     }
 
     private void clearAttributeMap() {
         attributesMap = null;
     }
 
     private void validateId(String id) {
         if (id == null) {
             return;
         }
         if (id.length() < 1) {
             throw new IllegalArgumentException("UIComponentBase");
         }
         char ch;
         for (int i = 0; i < id.length(); i++) {
             ch = id.charAt(i);
             if (i == 0 && !Character.isLetter(ch) && ch != '_') {
                 throw new IllegalArgumentException(
                         "The first character is invalid(id = " + id + ")");
             } else if (!Character.isDigit(ch) && !Character.isLetter(ch)
                     && ch != '-' && ch != '_') {
                 throw new IllegalArgumentException(
                         "Subsequent character is invalid(id = " + id + ")");
             }
         }
     }
 
     private static class NullRenderer extends Renderer {
         public NullRenderer() {
         }
     }
 
 }

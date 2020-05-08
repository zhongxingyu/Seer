 package org.icefaces.ace.component.tree;
 
 import org.icefaces.ace.component.node.Node;
 import org.icefaces.ace.event.TableFilterEvent;
 import org.icefaces.ace.model.tree.*;
 
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.component.*;
 import javax.faces.component.visit.VisitCallback;
 import javax.faces.component.visit.VisitContext;
 import javax.faces.component.visit.VisitHint;
 import javax.faces.component.visit.VisitResult;
 import javax.faces.context.FacesContext;
 import javax.faces.event.*;
 import javax.faces.model.DataModel;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Copyright 2010-2011 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils
  * Date: 2012-08-14
  * Time: 2:01 PM
  */
 public class Tree<N> extends TreeBase implements Serializable {
     NodeDataModel<N> model;
 
     /**
      * <p> During iteration through the nodes of this tree, This ivar is used to
      * store the previous "var" value for this instance.  When the node iteration
      * is complete, this value is restored to the request map.
      */
     private Boolean isNested;
     private Object oldVar;
     private Object oldStateVar;
     private String baseClientId = null;
     private int baseClientIdLength;
     private StringBuilder clientIdBuilder = null;
     private Map<String, Node> typeToNodeMap = null;
     private NodeStateMap stateMap;
 
     public NodeState getNodeState() {
         if (getKey() == NodeKey.ROOT_KEY)
             return NodeState.ROOT_STATE;
         return getStateMap().get(getKey());
     }
 
     public NodeStateMap getStateMap() {
         if (stateMap == null)
             stateMap = super.getStateMap();
 
         if (stateMap == null) {
             stateMap = new NodeStateMap();
            System.out.println(stateMap + " " + getClientId(FacesContext.getCurrentInstance()));
             super.setStateMap(stateMap);
         }
 
         return stateMap;
     }
 
     enum PropertyKeys {
         saved
     }
 
     public boolean isLeaf() {
         if (model == null) getDataModel();
         return model.isLeaf();
     }
 
     public N getData() {
         if (model == null) getDataModel();
         return model.getData();
     }
 
     // Pseudo setRowIndex()
     public void setKey(NodeKey key) {
         if (model == null) getDataModel();
             setNodeToKey(key);
     }
 
     // Pseudo getRowIndex()
     public NodeKey getKey() {
         if (model == null) getDataModel();
         return model.getKey();
     }
 
     // Pseudo isRowAvailable()
     public boolean isNodeAvailable() {
         if (model == null) getDataModel();
         return model.isRowAvailable();
     }
 
     protected Iterator<Map.Entry<NodeKey, N>> children() {
         if (model == null) getDataModel();
         return (model.children());
     }
 
     public N setNodeToRoot() {
         if (model == null) getDataModel();
         return setNodeToKey(new NodeKey());
     }
 
     public N setNodeToKey(NodeKey key) {
         if (model == null) getDataModel();
         saveNodeContext(model.getEntry());
         model.navToKey(key);
         Map.Entry<NodeKey, N> entry = model.getEntry();
         restoreNodeContext(entry);
         return entry.getValue();
     }
 
     public N setNodeToParent() {
         if (model == null) getDataModel();
         saveNodeContext(model.getEntry());
         model.navToParent();
         Map.Entry<NodeKey, N> entry = model.getEntry();
         restoreNodeContext(entry);
         return entry.getValue();
     }
 
     public N setNodeToChild(Object keySegment) {
         if (model == null) getDataModel();
         saveNodeContext(model.getEntry());
 
         model.navToChild(keySegment);
 
         Map.Entry<NodeKey, N> entry = model.getEntry();
         restoreNodeContext(entry);
         return entry.getValue();
     }
 
     public N setNodeToSibling(Object keySegment) {
         if (model == null) getDataModel();
         saveNodeContext(model.getEntry());
 
         model.navToParent();
         model.navToChild(keySegment);
 
         Map.Entry<NodeKey, N> entry = model.getEntry();
         restoreNodeContext(entry);
         return entry.getValue();
     }
 
     public Node getNodeForType() {
         if (typeToNodeMap == null) {
             generateTypeToNodeMap();
         }
 
         Node ret = typeToNodeMap.get(getType());
         //if (ret == null)
         //    throw new FacesException("ace:tree - " + this.getClientId(FacesContext.getCurrentInstance()) +
         //            " - Has no ace:node template for node type: \"" + getType() + "\".");
         if (ret == null) ret = typeToNodeMap.get(Node.DEFAULT_NODE_TYPE);
         return ret;
     }
 
     private void generateTypeToNodeMap() {
         typeToNodeMap = new HashMap<String, Node>();
 
         for (UIComponent c : getChildren())
             if (c instanceof Node) {
                 Node n = (Node) c;
                 String type = n.getType();
 
                 if (type == null)
                     type = Node.DEFAULT_NODE_TYPE;
 
                 typeToNodeMap.put(type, (Node) c);
             }
     }
 
     @Override
     public KeySegmentConverter getKeyConverter() {
         KeySegmentConverter c = super.getKeyConverter();
         if (c != null) return c;
         else return ((NodeDataModel)getDataModel()).getConverter();
     }
 
     @Override
     protected DataModel getDataModel() {
         // Return any previously cached DataModel instance
         if (this.model != null) {
             return (model);
         }
 
         // Synthesize a DataModel around our current value if possible
         Object current = getValue();
         if (current == null) {
             setDataModel(new ListNodeDataModel(Collections.EMPTY_LIST));
         } else if (current instanceof DataModel) {
             setDataModel((NodeDataModel) current);
         } else if (current instanceof List) {
             setDataModel(new ListNodeDataModel((List) current));
         } else {
             throw new IllegalArgumentException(current.toString());
         }
         return (model);
     }
 
     @Override
     protected void setDataModel(DataModel model) {
         if (model != null && !(model instanceof NodeDataModel))
             throw new IllegalArgumentException(model.toString());
 
         this.model = (NodeDataModel) model;
     }
 
     public void queueEvent(FacesEvent event) {
         if (event == null) {
             throw new NullPointerException();
         }
         UIComponent parent = getParent();
         if (parent == null) {
             throw new IllegalStateException();
         } else {
             parent.queueEvent(new WrapperEvent(this, event, getKey()));
         }
     }
 
     @Override
     public void processUpdates(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
 
         pushComponentToEL(context, this);
         //preUpdate(context);
         iterate(context, PhaseId.UPDATE_MODEL_VALUES);
         popComponentFromEL(context);
         // This is not a EditableValueHolder, so no further processing is required
     }
 
     @Override
     public void processDecodes(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
 
         pushComponentToEL(context, this);
         //super.preDecode() - private and difficult to port
         iterate(context, PhaseId.APPLY_REQUEST_VALUES);
         decode(context);
         popComponentFromEL(context);
     }
 
     @Override
     public void processValidators(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
         pushComponentToEL(context, this);
         Application app = context.getApplication();
         app.publishEvent(context, PreValidateEvent.class, this);
         //preValidate(context);
         iterate(context, PhaseId.PROCESS_VALIDATIONS);
         app.publishEvent(context, PostValidateEvent.class, this);
         popComponentFromEL(context);
     }
 
     @Override
     public void broadcast(FacesEvent event)
             throws AbortProcessingException {
 
         if (!(event instanceof Tree.WrapperEvent)) {
             super.broadcast(event);
             return;
         }
         FacesContext context = FacesContext.getCurrentInstance();
         // Set up the correct context and fire our wrapped event
         WrapperEvent revent = (WrapperEvent) event;
 
 //        if (isNestedWithinUIData()) {
 //            setDataModel(null);
 //        }
 
         NodeKey oldNodeKey = getKey();
         setNodeToKey(revent.getNodeKey());
 
         FacesEvent rowEvent = revent.getFacesEvent();
         UIComponent source = rowEvent.getComponent();
         UIComponent compositeParent = null;
         try {
             if (!UIComponent.isCompositeComponent(source)) {
                 compositeParent = UIComponent.getCompositeComponentParent(source);
             }
             if (compositeParent != null) {
                 compositeParent.pushComponentToEL(context, null);
             }
             source.pushComponentToEL(context, null);
             source.broadcast(rowEvent);
         } finally {
             source.popComponentFromEL(context);
             if (compositeParent != null) {
                 compositeParent.popComponentFromEL(context);
             }
         }
         setNodeToKey(oldNodeKey);
     }
 
 
     @Override
     public boolean visitTree(VisitContext context,
                              VisitCallback callback) {
 
         if (!isVisitable(context))
             return false;
 
         NodeDataModel model = (NodeDataModel) getDataModel();
        // Initialized cached state map before clientId begins to change
        getStateMap();
         FacesContext facesContext = context.getFacesContext();
         boolean visitRows = !context.getHints().contains(VisitHint.SKIP_ITERATION);
 
         // if we are iterating over nodes, save current key
         // and start at no node
         NodeKey oldNodeKey = NodeKey.ROOT_KEY;
         if (visitRows) {
             oldNodeKey = getKey();
             setKey(NodeKey.ROOT_KEY);
         }
 
         // Push ourselves to EL
         pushComponentToEL(facesContext, null);
 
         try {
             // Visit ourselves.  Note that we delegate to the
             // VisitContext to actually perform the visit.
             VisitResult result = context.invokeVisitCallback(this, callback);
 
             // If the visit is complete, short-circuit out and end the visit
             if (result == VisitResult.COMPLETE)
                 return true;
 
             // Visit children, short-circuiting as necessary
             if ((result == VisitResult.ACCEPT)
                     && doVisitChildren(context, visitRows)) {
 
                 // No facets to visit.
                 // First visit facets
                 // if (visitFacets(context, callback, visitRows))
                 //   return true;
 
                 // Visit node definitions and facets & node adaptors
                 if (visitFixedComponents(context, callback, visitRows))
                     return true;
 
                 // And finally, visit nodes
                 if (visitIterativeChildren(context, callback, visitRows))
                     return true;
             }
         }
         finally {
             // Clean up - pop EL and restore old row index
             popComponentFromEL(facesContext);
             if (visitRows) {
                 setKey(oldNodeKey);
             }
         }
 
         // Return false to allow the visit to continue
         return false;
     }
 
     // Tests whether we need to visit our children as part of
     // a tree visit
     private boolean doVisitChildren(VisitContext context, boolean fromRoot) {
 
         // Just need to check whether there are any ids under this subtree.  
  
         // Make sure row index is cleared out since initially
         // getSubtreeIdsToVisit() needs our row-less client id.
         if (fromRoot) {
             setKey(NodeKey.ROOT_KEY);
         }
         
         Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);
         assert(idsToVisit != null);
 
         // All ids or non-empty collection means we need to visit our children.
         return (!idsToVisit.isEmpty());
     }
 
     // Visit each TreeNode node template and any facets it may have defined
     private boolean visitFixedComponents(VisitContext context,
                                          VisitCallback callback,
                                          boolean visitRows) {
         if (visitRows) {
             setKey(NodeKey.ROOT_KEY);
         }
         if (getChildCount() > 0) {
             for (UIComponent nodeTemplate : getChildren()) {
                 if (nodeTemplate instanceof Node) {
                     // visit the node type def directly
                     VisitResult result = context
                             .invokeVisitCallback(nodeTemplate, callback);
 
                     if (result == VisitResult.COMPLETE) {
                         return true;
                     }
 
                     if (nodeTemplate.getFacetCount() > 0) {
                         for (UIComponent templateFacet : nodeTemplate.getFacets().values()) {
                             if (templateFacet.visitTree(context, callback)) {
                                 return true;
                             }
                         }
                     }
                 }
             }
         }
 
         return false;
     }
 
     // Visit each column and row
     private boolean visitIterativeChildren(VisitContext context,
                                            VisitCallback callback,
                                            boolean visitRows) {
         // Iterate over our TreeNode template components, once per node
         setKey(NodeKey.ROOT_KEY);
         Integer nodeCount = isPagination() ? getPageSize() : 0;
         TreeWalkContext twc = new TreeWalkContext(visitRows, nodeCount, isPagination());
 
         if (visitRows && twc.isPagination()) {
             // go to position and max nodes according
             // to pagination
             // Will have to add style padding for when this results in beginning at
             // a tree root.
             // setKey(getFirstNode());
         }
 
         return visitTreeLevel(context, callback, twc);
     }
 
     private boolean visitTreeLevel(VisitContext context,
                                    VisitCallback callback,
                                    TreeWalkContext twc) {
         if (twc.isVisitRows()) {
             // If we have processed all the nodes of the page, break
             if ((twc.getMaxCount() > 0) &&
                     ((++twc.processedCount) > twc.getMaxCount())) {
                 return false;
             }
 
             // Move to the sibling and key & node if possible
             //nextVisitNode(context);
 
             if (!isNodeAvailable()) {
                 return false;
             }
         }
 
         // Visit as required on the *children* of the node template
         // (facets have been done a single time with uniterative id already)
         if (getChildCount() > 0 && getKey() != NodeKey.ROOT_KEY) {
             // Only visit the node template with the matching type for this node
             Node n = getNodeForType();
 
             if (n.getChildCount() > 0) {
                 for (UIComponent grandkid : n.getChildren()) {
                     if (grandkid.visitTree(context, callback)) {
                         return true;
                     }
                 }
             }
         }
 
         if (!twc.isVisitRows()) {
             return false;
         }
 
         // If this node is expanded, has children and those children are
         // part of the visiting (as judged by context.getSubTreeIdsToVisit())
         // iterate over the children.
         NodeState state = getNodeState();
         if (!context.getSubtreeIdsToVisit(this).isEmpty() && state.isExpanded()) {
             for (Iterator<Map.Entry<NodeKey, N>> children = children();
                  children.hasNext();) {
                 Map.Entry<NodeKey, N> entry = children.next();
                 Object[] nodeKeys = entry.getKey().getKeys();
 
                 setNodeToChild(nodeKeys[nodeKeys.length - 1]);
                 if (visitTreeLevel(context, callback, twc))
                     return true;
                 setNodeToParent();
             }
         }
 
         return false;
     }
 
     protected void restoreNodeContext(Map.Entry<NodeKey, N> node) {
         boolean preserveState = isRowStatePreserved();
 
         // if null clear context variables
         if (preserveState)
             restoreNodeContextStatePreserved(node);
         else
             restoreNodeContextStateNotPreserved(node);
     }
 
     private void restoreNodeContextStatePreserved(Map.Entry<NodeKey, N> node) {
         // TODO: Implement state preserved state saving.
         throw new UnsupportedOperationException();
     }
 
     private void restoreNodeContextStateNotPreserved(Map.Entry<NodeKey, N> node) {
         String var = getVar();
         String stateVar = getStateVar();
         if (var != null) {
             Map<String, Object> requestMap =
                     getFacesContext().getExternalContext().getRequestMap();
             if (getKey().equals(NodeKey.ROOT_KEY)) {
                 oldVar = requestMap.remove(var);
                 oldStateVar = requestMap.remove(stateVar);
             } else if (isRowAvailable()) {
                 requestMap.put(var, getData());
                 requestMap.put(stateVar, getNodeState());
             } else {
                 requestMap.remove(var);
                 requestMap.remove(stateVar);
                 if (null != oldVar) {
                     requestMap.put(var, oldVar);
                     requestMap.put(var, oldStateVar);
                     oldVar = null;
                     oldStateVar = null;
                 }
             }
         }
 
         // Reset current state information for the new row index
         FacesContext context = getFacesContext();
         if (getChildCount() > 0) {
             for (UIComponent kid : getChildren()) {
                 if (kid instanceof Node) {
                     restoreDescendantState(kid, context);
                 }
             }
         }
     }
 
     private void restoreDescendantState(UIComponent component,
                                         FacesContext context) {
 
         // Reset the client identifier for this component
         String id = component.getId();
         component.setId(id); // Forces client id to be reset
         Map<String, SavedState> saved = (Map<String,SavedState>)
                 getStateHelper().get(PropertyKeys.saved);
         // Restore state for this component (if it is a EditableValueHolder)
         if (component instanceof EditableValueHolder) {
             EditableValueHolder input = (EditableValueHolder) component;
             String clientId = component.getClientId(context);
 
             SavedState state = saved.get(clientId);
             if (state == null) {
                 state = new SavedState();
             }
             input.setValue(state.getValue());
             input.setValid(state.isValid());
             input.setSubmittedValue(state.getSubmittedValue());
             // This *must* be set after the call to setValue(), since
             // calling setValue() always resets "localValueSet" to true.
             input.setLocalValueSet(state.isLocalValueSet());
         } else if (component instanceof UIForm) {
             UIForm form = (UIForm) component;
             String clientId = component.getClientId(context);
             SavedState state = saved.get(clientId);
             if (state == null) {
                 state = new SavedState();
             }
             form.setSubmitted(state.getSubmitted());
             state.setSubmitted(form.isSubmitted());
         }
 
         // Restore state for children of this component
         if (component.getChildCount() > 0) {
             for (UIComponent kid : component.getChildren()) {
                 restoreDescendantState(kid, context);
             }
         }
 
         // Restore state for facets of this component
         if (component.getFacetCount() > 0) {
             for (UIComponent facet : component.getFacets().values()) {
                 restoreDescendantState(facet, context);
             }
         }
 
     }
 
     protected void saveNodeContext(Map.Entry<NodeKey, N> node) {
         boolean preserveState = isRowStatePreserved();
 
         // if null clear context variables
         if (preserveState)
             saveNodeContextStatePreserved(node);
         else
             saveNodeContextStateNotPreserved(node);
     }
 
     private void saveNodeContextStatePreserved(Map.Entry<NodeKey, N> node) {
         throw new UnsupportedOperationException();
     }
 
     private void saveNodeContextStateNotPreserved(Map.Entry<NodeKey, N> node) {
         FacesContext context = getFacesContext();
         if (getChildCount() > 0) {
             for (UIComponent kid : getChildren()) {
                 if (kid instanceof Node) {
                     saveDescendantState(kid, context);
                 }
             }
         }
     }
 
     private void saveDescendantState(UIComponent component,
                                      FacesContext context) {
         // Save state for this component (if it is a EditableValueHolder)
         Map<String, SavedState> saved = (Map<String, SavedState>)
                 getStateHelper().get(PropertyKeys.saved);
         if (component instanceof EditableValueHolder) {
             EditableValueHolder input = (EditableValueHolder) component;
             SavedState state = null;
             String clientId = component.getClientId(context);
             if (saved == null) {
                 state = new SavedState();
                 getStateHelper().put(PropertyKeys.saved, clientId, state);
             }
             if (state == null) {
                 state = saved.get(clientId);
                 if (state == null) {
                     state = new SavedState();
                     //saved.put(clientId, state);
                     getStateHelper().put(PropertyKeys.saved, clientId, state);
                 }
             }
             state.setValue(input.getLocalValue());
             state.setValid(input.isValid());
             state.setSubmittedValue(input.getSubmittedValue());
             state.setLocalValueSet(input.isLocalValueSet());
         } else if (component instanceof UIForm) {
             UIForm form = (UIForm) component;
             String clientId = component.getClientId(context);
             SavedState state = null;
             if (saved == null) {
                 state = new SavedState();
                 getStateHelper().put(PropertyKeys.saved, clientId, state);
             }
             if (state == null) {
                 state = saved.get(clientId);
                 if (state == null) {
                     state = new SavedState();
                     //saved.put(clientId, state);
                     getStateHelper().put(PropertyKeys.saved, clientId, state);
                 }
             }
             state.setSubmitted(form.isSubmitted());
         }
 
         // Save state for children of this component
         if (component.getChildCount() > 0) {
             for (UIComponent uiComponent : component.getChildren()) {
                 saveDescendantState(uiComponent, context);
             }
         }
 
         // Save state for facets of this component
         if (component.getFacetCount() > 0) {
             for (UIComponent facet : component.getFacets().values()) {
                 saveDescendantState(facet, context);
             }
         }
     }
 
     private void iterate(FacesContext context, PhaseId phaseId) {
         // Process each facet of this component exactly once
         setKey(NodeKey.ROOT_KEY);
         if (getFacetCount() > 0) {
             for (UIComponent facet : getFacets().values()) {
                 if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                     facet.processDecodes(context);
                 } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                     facet.processValidators(context);
                 } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                     facet.processUpdates(context);
                 } else {
                     throw new IllegalArgumentException();
                 }
             }
         }
 
         // Process each facet of our child UIColumn components exactly once
         setKey(NodeKey.ROOT_KEY);
         if (getChildCount() > 0) {
             for (UIComponent node : getChildren()) {
                 if (!(node instanceof Node) || !node.isRendered()) {
                     continue;
                 }
                 if (node.getFacetCount() > 0) {
                     for (UIComponent columnFacet : node.getFacets().values()) {
                         if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                             columnFacet.processDecodes(context);
                         } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                             columnFacet.processValidators(context);
                         } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                             columnFacet.processUpdates(context);
                         } else {
                             throw new IllegalArgumentException();
                         }
                     }
                 }
             }
         }
         Integer nodeCount = isPagination() ? getPageSize() : 0;
 
         // Iterate over our UIColumn children, once per row
         setKey(NodeKey.ROOT_KEY);
         iterateTreeLevel(context, phaseId,
                 new TreeWalkContext(true, nodeCount, isPagination()));
 
         // Clean up after ourselves
         setKey(NodeKey.ROOT_KEY);
     }
 
     private void iterateTreeLevel(FacesContext context,
                                   PhaseId phaseId,
                                   TreeWalkContext twc) {
         if (twc.isVisitRows()) {
             // If we have processed all the nodes of the page, break
             if ((twc.getMaxCount() > 0) &&
                     ((++twc.processedCount) > twc.getMaxCount())) {
                 return;
             }
 
             // Move to the sibling and key & node if possible
             //nextVisitNode(context);
 
             if (!isNodeAvailable() && getKey() != NodeKey.ROOT_KEY) {
                 return;
             }
         }
 
         // Visit as required on the *children* of the TreeNodes
         // (facets have been done a single time with uniterative id already)
         if (getKey() != NodeKey.ROOT_KEY && getChildCount() > 0) {
             Node n = getNodeForType();
 
             if (n.getChildCount() > 0) {
                 for (UIComponent grandkid : n.getChildren()) {
                     if (!grandkid.isRendered()) {
                         continue;
                     }
                     if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                         grandkid.processDecodes(context);
                     } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                         grandkid.processValidators(context);
                     } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                         grandkid.processUpdates(context);
                     } else {
                         throw new IllegalArgumentException();
                     }
                 }
             }
         }
 
         NodeState state = getNodeState();
         // If this node is expanded and has children iterate over the children.
         if (state.isExpanded()) {
             for (Iterator<Map.Entry<NodeKey, N>> children = children();
                  children.hasNext();) {
                 Map.Entry<NodeKey, N> entry = children.next();
                 Object[] nodeKeys = entry.getKey().getKeys();
 
                 setNodeToChild(nodeKeys[nodeKeys.length - 1]);
                 iterateTreeLevel(context, phaseId, twc);
                 setNodeToParent();
             }
         }
     }
 
     @Override
     public boolean invokeOnComponent(FacesContext context, String clientId,
                                      ContextCallback callback)
             throws FacesException {
         if (null == context || null == clientId || null == callback) {
             throw new NullPointerException();
         }
 
         String myId = super.getClientId(context);
         boolean found = false;
 
         if (clientId.equals(myId)) {
             try {
                 this.pushComponentToEL(context, UIComponent.getCompositeComponentParent(this));
                 callback.invokeContextCallback(context, this);
                 return true;
             }
             catch (Exception e) {
                 throw new FacesException(e);
             }
             finally {
                 this.popComponentFromEL(context);
             }
         }
 
         // check the facets, if any, of UIData
         if (this.getFacetCount() > 0) {
             for (UIComponent c : this.getFacets().values()) {
                 if (clientId.equals(c.getClientId(context))) {
                     callback.invokeContextCallback(context, c);
                     return true;
                 }
             }
         }
 
         // check column level facets, if any
         if (this.getChildCount() > 0) {
             for (UIComponent node : this.getChildren()) {
                 if (node instanceof Node) {
                     if (node.getFacetCount() > 0) {
                         for (UIComponent facet : node.getFacets().values()) {
                             if (facet.invokeOnComponent(context, clientId, callback)) {
                                 return true;
                             }
                         }
                     }
                 }
             }
         }
 
         int lastSep;
         NodeKey newKey, savedNodeKey = this.getKey();
         try {
             char sepChar = UINamingContainer.getSeparatorChar(context);
             String nodeKeySep = sepChar+"-"+sepChar;
             // If we need to strip out the rowIndex from our id
             // PENDING(edburns): is this safe with respect to I18N?
             if (myId.endsWith(sepChar + savedNodeKey.toString())) {
                 lastSep = myId.lastIndexOf(sepChar);
                 assert (-1 != lastSep);
                 myId = myId.substring(0, lastSep);
             }
 
             // myId will be something like form:outerTree for a non-nested tree,
             // and form:outerTree:-:0:1:3:-:tree for a nested tree.
             // clientId will be something like form:outerTree:-:0:1:3:-:outerNode
             // for a non-nested table.  clientId will be something like
             // outerTree:-:0:1:3:-:tree:-:0:1:3:-:input for a nested table.
             if (clientId.startsWith(myId)) {
                 int preKeySep = clientId.indexOf(nodeKeySep, myId.length());
                 int postKeySep;
 
                 if (-1 != preKeySep) {
                     // Check the length
                     if (++preKeySep < clientId.length()) {
                         postKeySep = clientId.indexOf(sepChar, preKeySep + 1);
                         if (-1 != postKeySep) {
                             try {
                                 newKey = getKeyConverter().parseSegments(clientId
                                         .substring(preKeySep, postKeySep)
                                         .split(Character.toString(sepChar)));
                             } catch (NumberFormatException ex) {
                                 // PENDING(edburns): I18N
                                 String message =
                                         "Trying to extract nodeKey from clientId \'"
                                                 +
                                                 clientId
                                                 + "\' "
                                                 + ex.getMessage();
                                 throw new NumberFormatException(message);
                             }
                             this.setNodeToKey(newKey);
                             if (this.isNodeAvailable()) {
                                 found = super.invokeOnComponent(context,
                                         clientId,
                                         callback);
                             }
                         }
                     }
                 }
             }
         }
         catch (FacesException fe) {
             throw fe;
         }
         catch (Exception e) {
             throw new FacesException(e);
         }
         finally {
             this.setNodeToKey(savedNodeKey);
         }
         return found;
     }
 
     private Boolean isNestedWithinUIData() {
         if (isNested == null) {
             UIComponent parent = this;
             while (null != (parent = parent.getParent())) {
                 if (parent instanceof UIData) {
                     isNested = Boolean.TRUE;
                     break;
                 }
             }
             if (isNested == null) {
                 isNested = Boolean.FALSE;
             }
             return isNested;
         } else {
             return isNested;
         }
     }
 
     @Override
     public String getClientId(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
 
         // If baseClientId and clientIdBuilder are both null, this is the
         // first time that getClientId() has been called.
         // If we're not nested within another UIData, then:
         //   - create a new StringBuilder assigned to clientIdBuilder containing
         //   our client ID.
         //   - toString() the builder - this result will be our baseClientId
         //     for the duration of the component
         //   - append UINamingContainer.getSeparatorChar() to the builder
         //  If we are nested within another UIData, then:
         //   - create an empty StringBuilder that will be used to build
         //     this instance's ID
         if (baseClientId == null && clientIdBuilder == null) {
             if (!isNestedWithinUIData()) {
                 clientIdBuilder = new StringBuilder(super.getClientId(context));
                 baseClientId = clientIdBuilder.toString();
                 baseClientIdLength = (baseClientId.length() + 3);
                 clientIdBuilder
                         .append(UINamingContainer.getSeparatorChar(context))
                         .append('-')
                         .append(UINamingContainer.getSeparatorChar(context));
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 clientIdBuilder = new StringBuilder();
             }
         }
         NodeKey nodeKey = getKey();
         if (!nodeKey.equals(NodeKey.ROOT_KEY)) {
             String cid;
             if (!isNestedWithinUIData()) {
                 // we're not nested, so the clientIdBuilder is already
                 // primed with clientID +
                 // UINamingContainer.getSeparatorChar().  Append the
                 // current rowIndex, and toString() the builder.  reset
                 // the builder to it's primed state.
                 cid = clientIdBuilder.append(nodeKey.toString())
                         .append(UINamingContainer.getSeparatorChar(context))
                         .append('-')
                         .toString();
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 // we're nested, so we have to build the ID from scratch
                 // each time.  Reuse the same clientIdBuilder instance
                 // for each call by resetting the length to 0 after
                 // the ID has been computed.
                 cid = clientIdBuilder.append(super.getClientId(context))
                         .append(UINamingContainer.getSeparatorChar(context))
                         .append('-')
                         .append(UINamingContainer.getSeparatorChar(context))
                         .append(nodeKey.toString())
                         .append(UINamingContainer.getSeparatorChar(context))
                         .append('-')
                         .toString();
                 clientIdBuilder.setLength(0);
             }
             return (cid);
         } else {
             if (!isNestedWithinUIData()) {
                 // Not nested and no row available, so just return our baseClientId
                 return (baseClientId);
             } else {
                 // nested and no row available, return the result of getClientId().
                 // this is necessary as the client ID will reflect the row that
                 // this table represents
                 return super.getClientId(context);
             }
         }
 
     }
 
     @SuppressWarnings({"SerializableHasSerializationMethods",
             "NonSerializableFieldInSerializableClass"})
     class SavedState implements Serializable {
 
         private static final long serialVersionUID = 2920252657338389849L;
         private Object submittedValue;
         private boolean submitted;
 
         Object getSubmittedValue() {
             return (this.submittedValue);
         }
 
         void setSubmittedValue(Object submittedValue) {
             this.submittedValue = submittedValue;
         }
 
         private boolean valid = true;
 
         boolean isValid() {
             return (this.valid);
         }
 
         void setValid(boolean valid) {
             this.valid = valid;
         }
 
         private Object value;
 
         Object getValue() {
             return (this.value);
         }
 
         public void setValue(Object value) {
             this.value = value;
         }
 
         private boolean localValueSet;
 
         boolean isLocalValueSet() {
             return (this.localValueSet);
         }
 
         public void setLocalValueSet(boolean localValueSet) {
             this.localValueSet = localValueSet;
         }
 
         public boolean getSubmitted() {
             return this.submitted;
         }
 
         public void setSubmitted(boolean submitted) {
             this.submitted = submitted;
         }
 
         public String toString() {
             return ("submittedValue: " + submittedValue +
                     " value: " + value +
                     " localValueSet: " + localValueSet);
         }
     }
 
     class WrapperEvent extends FacesEvent {
         public WrapperEvent(UIComponent component, FacesEvent event, NodeKey nodeKey) {
             super(component);
             this.event = event;
             this.nodeKey = nodeKey;
         }
 
         private FacesEvent event = null;
         private NodeKey nodeKey = NodeKey.ROOT_KEY;
 
         public FacesEvent getFacesEvent() {
             return (this.event);
         }
 
         public NodeKey getNodeKey() {
             return nodeKey;
         }
 
         public PhaseId getPhaseId() {
             return (this.event.getPhaseId());
         }
 
         public void setPhaseId(PhaseId phaseId) {
             this.event.setPhaseId(phaseId);
         }
 
         public boolean isAppropriateListener(FacesListener listener) {
             return (false);
         }
 
         public void processListener(FacesListener listener) {
             throw new IllegalStateException();
         }
     }
 
     class TreeWalkContext {
         private boolean visitRows;
         private int maxCount;
         protected int processedCount = 0;
         private boolean pagination;
 
         public TreeWalkContext(boolean visitRows, int maxCount, boolean pagination) {
             this.maxCount = maxCount;
             this.visitRows = visitRows;
             this.pagination = pagination;
         }
 
         public boolean isVisitRows() {
             return visitRows;
         }
 
         public int getMaxCount() {
             return maxCount;
         }
 
         public int getProcessedCount() {
             return processedCount;
         }
 
         public boolean isPagination() {
             return pagination;
         }
     }
 }

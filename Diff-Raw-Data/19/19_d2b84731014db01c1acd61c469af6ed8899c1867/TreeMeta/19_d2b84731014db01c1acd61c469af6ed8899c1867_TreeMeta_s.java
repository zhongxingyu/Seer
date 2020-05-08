 package org.icefaces.ace.component.tree;
 
 import org.icefaces.ace.meta.annotation.*;
 import org.icefaces.ace.meta.baseMeta.UIDataMeta;
 import org.icefaces.ace.model.tree.*;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 
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
  * Time: 2:02 PM
  */
 @Component(
         tagName = "tree",
         componentClass = "org.icefaces.ace.component.tree.Tree",
         generatedClass = "org.icefaces.ace.component.tree.TreeBase",
         rendererClass  = "org.icefaces.ace.component.tree.TreeRenderer",
         extendsClass   = "javax.faces.component.UIData",
         componentType  = "org.icefaces.ace.component.Tree",
         rendererType   = "org.icefaces.ace.component.TreeRenderer",
         componentFamily = "org.icefaces.ace.Tree",
        tlddoc = "")
 @ResourceDependencies({
         @ResourceDependency(library="icefaces.ace", name="util/combined.css"),
         @ResourceDependency(library="icefaces.ace", name="util/ace-jquery.js"),
         @ResourceDependency(library="icefaces.ace", name="util/ace-components.js")
 })
 @ClientBehaviorHolder(events = {
         @ClientEvent(name="expand",
             javadoc="Fired when a node switch is clicked to show children of a node.",
             tlddoc="Fired when a node switch is clicked to show children a node.",
             defaultRender="@all", defaultExecute="@this"),
 
         @ClientEvent(name="contract",
             javadoc="Fired when a node switch is clicked to hide children a node.",
             tlddoc="Fired when a node switch is clicked to hide children a node.",
             defaultRender="@all", defaultExecute="@this"),
 
         @ClientEvent(name="select",
             javadoc="Fired when a node is clicked to select it.",
             tlddoc="Fired when a node is clicked to select it.",
             defaultRender="@all", defaultExecute="@this"),
 
         @ClientEvent(name="deselect",
             javadoc="Fired when a selected node is clicked to deselect it.",
             tlddoc="Fired when a selected node is clicked to deselect it.",
             defaultRender="@all", defaultExecute="@this"),
 
         @ClientEvent(name="reorder",
             javadoc="Fired when a node is dragged into a new position.",
             tlddoc="Fired when a node is dragged into a new position.",
             defaultRender="@all", defaultExecute="@this")
         },
         defaultEvent = "select"
 )
 public class TreeMeta extends UIDataMeta {
     @Property(tlddoc = "Optionally define a custom KeySegmentConverter object to produce segmented identifier " +
             "keys for nodes based on something other than indexes.")
     KeySegmentConverter keyConverter;
 
     @Property(tlddoc = "The Java objects representing the nodes of the tree. Supports either " +
             "a List of javax.swing.tree.TreeNode implementations, MutableTreeNode implementations" +
             " or an implementation of NodeDataModel.")
     Object value;
 
     //@Property(defaultValue = "false", defaultValueType = DefaultValueType.EXPRESSION)
     //Boolean pagination;
 
     //@Property(tlddoc = "")
     //Integer pageSize;
 
     //@Property(defaultValue = "org.icefaces.ace.model.tree.NodeKey.ROOT_KEY",
     //        defaultValueType = DefaultValueType.EXPRESSION)
     //NodeKey firstNode;
 
     @Property(defaultValue = "nodeState", defaultValueType = DefaultValueType.STRING_LITERAL,
         tlddoc = "The request-scope attribute exposing the state object for the current" +
             "node when iterating.")
     String stateVar;
 
     @Property(defaultValue = "org.icefaces.ace.component.tree.TreeExpansionMode.server",
             defaultValueType = DefaultValueType.EXPRESSION,
             tlddoc = "Select the request behaviour of the expansion feature. When 'client', " +
                     "the children of every node are pre-rendered in the DOM and exposed by JavaScript " +
                     "when nodes are expanded. In the default 'server' mode, only visible nodes are in" +
                     "the DOM and expansion and contraction are caused by ajax page updates.")
     TreeExpansionMode expansionMode;
 
     @Property(expression = Expression.VALUE_EXPRESSION,
             tlddoc = "Define a ValueExpression that returns a String representation of the " +
                     "'rendering type' of this node. The rendering type is matched against the " +
                     "String 'type' attribute of ace:node tag instances to determine which node " +
                     "template should be used to render a given node object.")
     String type;
 
     @Property(defaultValue = "false", defaultValueType = DefaultValueType.EXPRESSION,
             tlddoc = "Enable selection feature of this tree component. This toggles this feature " +
                     "for the entire tree, per-node configuration of this feature available via the NodeState.")
     Boolean selection;
 
     @Property(defaultValue = "false", defaultValueType = DefaultValueType.EXPRESSION,
             tlddoc = "Enable expansion feature of this tree component. This toggles this feature " +
                     "for the entire tree, per-node configuration of this feature available via the NodeState.")
     Boolean expansion;
 
     @Property(defaultValue = "false", defaultValueType = DefaultValueType.EXPRESSION,
             tlddoc = "Enable reordering of the nodes of this tree.")
     Boolean reordering;
 
     @Property(defaultValue = "true", defaultValueType = DefaultValueType.EXPRESSION,
             tlddoc = "Disable the selection of multiple nodes simultaneously.")
     Boolean selectMultiple;
 
     @Property(defaultValue = "org.icefaces.ace.component.tree.TreeSelectionMode.server",
             defaultValueType = DefaultValueType.EXPRESSION,
             tlddoc = "Select the request behaviour of the selection feature. When 'client', " +
                     "the (de)selection of a node is recorded on the client, and communicated to " +
                     "the server on the next request executing this component. In the default 'server' " +
                     "mode, when a node is (de)selected, the component communicates the change the server " +
                     "immediately with an ajax update.")
     TreeSelectionMode selectionMode;
 
     @Property(tlddoc = "Define a NodeStateMap ValueExpression to access the store of Tree node object state " +
             "information. The state map provides an API for looking up the state of a particular node object," +
             " as well as reverse look-ups to get node objects with a particular state.")
     NodeStateMap stateMap;
 
     @Property(tlddoc = "Bind an implementer of the NodeStateCreationCallback interface to take as input" +
             ", a node object and a default NodeState and return a NodeState configured with the state appropriate" +
             "for the given node object.")
     NodeStateCreationCallback stateCreationCallback;
 }

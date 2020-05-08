 package org.icefaces.ace.model.tree;
 
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
  * Time: 2:04 PM
  */
public class NodeState {
     public static final NodeState ROOT_STATE = new NodeState() {{
         setExpanded(true);
     }};
 
     boolean selectionEnabled = true;
     boolean expansionEnabled = true;
     boolean expanded = true;
     boolean selected;
 
     public NodeState() {
     }
 
     public boolean isSelectionEnabled() {
         return selectionEnabled;
     }
 
     public void setSelectionEnabled(boolean selectionEnabled) {
         this.selectionEnabled = selectionEnabled;
     }
 
     public boolean isExpansionEnabled() {
         return expansionEnabled;
     }
 
     public void setExpansionEnabled(boolean expansionEnabled) {
         this.expansionEnabled = expansionEnabled;
     }
 
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
 
     public boolean isExpanded() {
         return expanded;
     }
 
     public void setExpanded(boolean expanded) {
         this.expanded = expanded;
     }
 }

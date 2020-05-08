 /*******************************************************************************
  * Copyright 2012 AdrianIonescu
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package ro.zg.netcell.vaadin.action.application;
 
 import java.util.Map;
 
 import ro.zg.netcell.vaadin.action.ActionContext;
 import ro.zg.netcell.vaadin.action.OpenGroupsActionHandler;
 import ro.zg.open_groups.OpenGroupsApplication;
 import ro.zg.open_groups.gui.components.CausalHierarchyContainer;
 import ro.zg.opengroups.vo.EntityList;
 
 public class RefreshCausalHierarchyHandler extends OpenGroupsActionHandler {
 
     /**
      * 
      */
     private static final long serialVersionUID = 6863283657273727248L;
 
     /**
      * specifies the depth level of the hierarchy to bring in the view used for smooth drill down of the hierarchy tree
      */
 
     @Override
     public void handle(ActionContext actionContext) throws Exception {
 	CausalHierarchyContainer chc = actionContext.getWindow().getHierarchyContainer();
 	int startDepth = chc.getStartDepth();
 	OpenGroupsApplication app = actionContext.getApp();
 	long parentNodeId = app.getRootEntity().getId();
 	Map<String, Object> params = actionContext.getParams();
 	boolean refresh = true;
 	if (params != null) {
 	    if (params.containsKey("parentId")) {
 		parentNodeId = (Long) params.get("parentId");
 	    }
 	    if(params.containsKey("refresh")) {
 		refresh = (Boolean)params.get("refresh");
 	    }
 	    if(params.containsKey("startDepth")) {
 		startDepth = (Integer)params.get("startDepth");
 	    }
 	}
 
 	EntityList hierarchyList = app.getModel().getCausalHierarchy(parentNodeId, startDepth, chc.getCacheDepth());
 	chc.updateHierarchy(hierarchyList, refresh);
 	chc.setSelected(app.getActiveEntity().getId());
     }
 
 }

 package org.zkoss.addon;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.zkoss.json.JSONAware;
 import org.zkoss.json.JSONObject;
 import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
 
 public class SpaceTreeNode<E extends SpaceTreeData<?>> extends
 		DefaultTreeNode<E> implements JSONAware {
 
 
 	public SpaceTreeNode(E data, Collection<? extends SpaceTreeNode<E>> children) {
 		super(data, children);
 	}
 
 	public String getId() {
 		return getData().getId();
 	}
 
 	public String getName() {
 		return getData().getName();
 	}
 
 	public boolean hasChildren() {
 		return getChildren() != null && getChildren().size() != 0;
 	}
 
 	public void setName(String name) {
 		getData().setName(name);
 	}
 
 	public String toJSONString() {
 		if(getData() == null) {
 			return "{}";
 		} else {
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put("id", getId());
 			map.put("name", getName());
 			map.put("data", getData().getJsonData());
 			map.put("children", getChildren());
 			return JSONObject.toJSONString(map);
 		}
 		
 	}
 }

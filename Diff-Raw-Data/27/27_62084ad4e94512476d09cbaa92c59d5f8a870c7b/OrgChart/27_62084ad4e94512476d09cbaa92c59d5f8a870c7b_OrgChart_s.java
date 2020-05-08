 package org.zkoss.addon;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.zkoss.json.JSONAware;
 import org.zkoss.json.JSONObject;
 import org.zkoss.lang.Objects;
 import org.zkoss.zk.au.AuRequest;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.SelectEvent;
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.TreeNode;
 import org.zkoss.zul.impl.XulElement;
 
 public class OrgChart<E extends SpaceTreeData<?>> extends XulElement {
 
 	static {
 		addClientEvent(OrgChart.class, Events.ON_SELECT, CE_IMPORTANT);
 	}
 
 	/* Here's a simple example for how to implements a member field */
 
 	private String _align = "center";
 	private int _duration = 700;
 	private int _level = 2;
 	private String _nodetype = "rectangle";
 	private String _orient = "left";
 	private DefaultTreeModel<E> _model;
 	private String _json = "{}";
 	private SelectEvent _onSelect;
 	private String _selectedNodeId;
 	private String _cmd = "";
 	private String _addNodeJson = "{}";
 	private boolean init = true;
 
 	public SpaceTreeNode<E> add(SpaceTreeNode<E> childNode, String parentId) {
 		SpaceTreeNode<E> parent = find(parentId);
 		parent.add(childNode);
 		setAddNodeJson(childNode.toJSONString());
 		setCmd("add");
 		return parent;
 	}
 
 	private SpaceTreeNode<E> find(String id) {
 		return query(_model.getRoot(), id, new HashMap<String, Boolean>());
 	}
 
 	public String getAddNodeJson() {
 		return _addNodeJson;
 	}
 
 	public String getAlign() {
 		return _align;
 	}
 
 	public String getCmd() {
 		return _cmd;
 	}
 
 	public int getDuration() {
 		return _duration;
 	}
 
 	public String getJson() {
 		return _json;
 	}
 
 	public int getLevel() {
 		return _level;
 	}
 
 	public DefaultTreeModel getModel() {
 		return _model;
 	}
	
 	public String getNodetype() {
 		return _nodetype;
 	}
 
 	public SelectEvent getOnSelect() {
 		return _onSelect;
 	}
 
 	public String getOrient() {
 		return _orient;
 	}
 
 	public SpaceTreeNode<E> getSelectedNode() {
 		return find(_selectedNodeId);
 	}
 
 	public String getSelectedNodeId() {
 		return _selectedNodeId;
 	}
 
 	/**
 	 * The default zclass is "z-orgchart"
 	 */
 	public String getZclass() {
 		return (this._zclass != null ? this._zclass : "z-orgchart");
 	}
 
	public void initModel(DefaultTreeModel model){
		if(init) {
 			setSelectedNodeId(((SpaceTreeNode) model.getRoot()).getId());
 			setModel(model);
 			init = false;
 		}
 	}
 
 	// graph depth-first-search algorithms
 	@SuppressWarnings("unchecked")
 	private SpaceTreeNode<E> query(TreeNode<E> node, String id,
 			Map<String, Boolean> checked) {
 
 		// check children
 		for (TreeNode<E> rawChild : node.getChildren()) {
 			SpaceTreeNode<E> child = (SpaceTreeNode<E>) rawChild;
 			String childId = child.getId();
 
 			if (checked.get(childId) != null) {
 				// if it checked, then skip
 				continue;
 			} else if (child.hasChildren()) {
 				// check its children
 				return query(child, id, checked);
 			} else {
 				// check leaf node id
 				if (childId.equals(id)) {
 					return child;
 				} else {
 					checked.put(childId, true);
 				}
 			}
 		}
 
 		// check itself after all children is checked
 		SpaceTreeNode<E> thisNode = (SpaceTreeNode<E>) node;
 		if (thisNode.getId().equals(id)) {
 			return (SpaceTreeNode<E>) node;
 		} else {
 			checked.put(thisNode.getId(), true);
 		}
 
 		TreeNode<E> parent = thisNode.getParent();
 		if (parent != null) {
 			// check bother if parent exists
 			return query(parent, id, checked);
 		} else {
 			// return null if not found
 			return null;
 		}
 	}
 
 	public SpaceTreeNode<E> remove(String id) {
 		SpaceTreeNode<E> node = find(id);
 		node.removeFromParent();
 		setCmd("remove");
 		return node;
 	}
 
 	// super//
 	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer)
 			throws java.io.IOException {
 		super.renderProperties(renderer);
 
 		if (_level != 2)
 			render(renderer, "level", _level);
 		if (_duration != 700)
 			render(renderer, "duration", _duration);
 		if (!Objects.equals(_orient, "left"))
 			render(renderer, "orient", _orient);
 		if (!Objects.equals(_align, "center"))
 			render(renderer, "align", _align);
 		if (!Objects.equals(_nodetype, "rectangle"))
 			render(renderer, "nodetype", _nodetype);
 		if (!Objects.equals(_json, "{}"))
 			render(renderer, "json", _json);
 		if (!Objects.equals(_cmd, ""))
 			render(renderer, "cmd", _cmd);
 		if (!Objects.equals(_selectedNodeId, ""))
 			render(renderer, "selectedNodeId", _selectedNodeId);
 		if (!Objects.equals(_addNodeJson, "{}"))
 			render(renderer, "addNodeJson", _addNodeJson);
 		render(renderer, "onSelect", _onSelect);
 
 	}
 
 	public void service(AuRequest request, boolean everError) {
 		final String cmd = request.getCommand();
 		SelectEvent evt = SelectEvent.getSelectEvent(request);
 		Component comp = evt.getTarget();
 		Map data = request.getData();
 
 		String jsonTree = ((SpaceTreeNode<?>) _model.getRoot()).toJSONString();
		if ("onSelect".equals(cmd)) {
 			setSelectedNodeId(data.get("selectedNodeId").toString());
 			Events.sendEvent(evt);
 			String newJsonTree = ((SpaceTreeNode<?>) _model.getRoot())
 					.toJSONString();
 			if (!newJsonTree.equals(jsonTree)) {
 				setCmd("refresh");
 			}
 		} else {
 			super.service(request, everError);
 		}
 	}
 
 	public void setAddNodeJson(String addNodeJson) {
 		if (!Objects.equals(_addNodeJson, addNodeJson)) {
 			_addNodeJson = addNodeJson;
 		}
 		smartUpdate("addNodeJson", _addNodeJson);
 	}
 
 	public void setAlign(String align) {
 		if (!Objects.equals(_align, align)
 				&& ("left".equals(align) || "center".equals(align) || "right"
 						.equals(align))) {
 			_align = align;
 			smartUpdate("align", _align);
 		}
 	}
 
 	public void setCmd(String cmd) {
 		if (!Objects.equals(_cmd, cmd)) {
 			_cmd = cmd;
 		}
 		updateJson();
 		smartUpdate("cmd", _cmd);
 	}
 
 	public void setDuration(int duration) {
 		if (_duration != duration && duration >= 0) {
 			_duration = duration;
 			smartUpdate("duration", _duration);
 		}
 	}
 
 	public void setJson(String json) {
 		if (!Objects.equals(_json, json)) {
 			_json = json;
 			smartUpdate("json", _json);
 		}
 	}
 
 	public void setLevel(int level) {
 		if (_level != level) {
 			_level = level;
 			smartUpdate("level", _level);
 		}
 	}
 
 	public void setModel(DefaultTreeModel model) {
 		if (!Objects.equals(_model, model)) {
 			_model = model;
 			updateJson();
 		}
 	}
 
 	public void setNodetype(String nodetype) {
 		if (!Objects.equals(_nodetype, nodetype)) {
 			_nodetype = nodetype;
 			smartUpdate("nodetype", _nodetype);
 		}
 	}
 
 	public void setOnSelect(SelectEvent onSelect) {
 		if (!Objects.equals(_onSelect, onSelect)) {
 			_onSelect = onSelect;
 			smartUpdate("onSelect", _onSelect);
 		}
 	}
 
 	public void setOrient(String orient) {
 		if (!Objects.equals(_orient, orient)
 				&& ("left".equals(orient) || "right".equals(orient)
 						|| "top".equals(orient) || "bottom".equals(orient))) {
 			_orient = orient;
 			smartUpdate("orient", _orient);
 		}
 	}
 
 	public void setSelectedNodeId(String selectedNodeId) {
 		if (!Objects.equals(_selectedNodeId, selectedNodeId)) {
 			_selectedNodeId = selectedNodeId;
 			smartUpdate("selectedNodeId", _selectedNodeId);
 		}
 	}
 
 	private void updateJson() {
 		if (_model == null)
 			_json = "{}";
 		else {
 			JSONAware node = (JSONAware) _model.getRoot();
 			_json = node.toJSONString();
 		}
 		smartUpdate("json", _json);
 	}
 }

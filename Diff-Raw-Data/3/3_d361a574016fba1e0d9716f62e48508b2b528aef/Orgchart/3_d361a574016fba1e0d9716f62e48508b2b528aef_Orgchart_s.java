 package org.zkoss.addon;
 
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.zkoss.json.JSONObject;
 import org.zkoss.json.JSONValue;
 import org.zkoss.lang.Objects;
 import org.zkoss.xel.VariableResolver;
 import org.zkoss.zk.au.AuRequest;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.UiException;
 import org.zkoss.zk.ui.WrongValueException;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.SelectEvent;
 import org.zkoss.zk.ui.util.ForEachStatus;
 import org.zkoss.zk.ui.util.Template;
 import org.zkoss.zul.ItemRenderer;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.RendererCtrl;
 import org.zkoss.zul.TreeModel;
 import org.zkoss.zul.TreeNode;
 import org.zkoss.zul.event.TreeDataEvent;
 import org.zkoss.zul.event.TreeDataListener;
 import org.zkoss.zul.event.ZulEvents;
 import org.zkoss.zul.impl.XulElement;
 
 @SuppressWarnings("serial")
 public class Orgchart extends XulElement {
 
 	/** Used to render treeitem if _model is specified. */
 	private class Renderer implements java.io.Serializable {
 		private final ItemRenderer _renderer;
 		private boolean _rendered, _ctrled;
 
 		private Renderer() {
 			_renderer = getRealRenderer();
 		}
 
 		private void doCatch(Throwable ex) {
 			if (_ctrled) {
 				try {
 					((RendererCtrl) _renderer).doCatch(ex);
 				} catch (Throwable t) {
 					throw UiException.Aide.wrap(t);
 				}
 			} else {
 				throw UiException.Aide.wrap(ex);
 			}
 		}
 
 		private void doFinally() {
 			if (_ctrled)
 				((RendererCtrl) _renderer).doFinally();
 		}
 
 		private String render(SpaceTreeNode<?> node, String defVal) {
 			try {
 				String reuslt = render(node);
 				return reuslt;
 			} catch (Throwable e) {
 				return defVal;
 			}
 		}
 
 		private String render(SpaceTreeNode<?> node) throws Throwable {
 			if (node == null || node.getData() == null) {
 				return "{}";
 			}
 
 			JSONObject json = new JSONObject();
 			if (!_rendered && (_renderer instanceof RendererCtrl)) {
 				((RendererCtrl) _renderer).doTry();
 				_ctrled = true;
 			}
 			try {
 				try {
 					json.put("id", node.getId());
 					json.put("name", _renderer.render(owner, node.getData(), node.getId()));
 
 					if (node.isLeaf())
 						json.put("children", "null");
 					else {
 						boolean first = true;
 						StringBuffer sb = new StringBuffer();
 						Iterator iter = node.getChildren().iterator();
 
 						sb.append('[');
 						while (iter.hasNext()) {
 							if (first)
 								first = false;
 							else
 								sb.append(',');
 
 							Object value = iter.next();
 							if (value == null) {
 								sb.append("null");
 								continue;
 							}
 							sb.append(render((SpaceTreeNode) value));
 						}
 						sb.append(']');
 						json.put("children", JSONValue.parse(sb.toString()));
 					}
 				} catch (AbstractMethodError ex) {
 					final Method m = _renderer.getClass().getMethod("render",
 							new Class<?>[] { SpaceTreeNode.class });
 					m.setAccessible(true);
 					m.invoke(_renderer, new Object[] { node });
 				}
 			} catch (Throwable ex) {
 				throw ex;
 			}
 			_rendered = true;
 			return json.toJSONString();
 		}
 	}
 
 	/* Here's a simple example for how to implements a member field */
 
 	static {
 		addClientEvent(Orgchart.class, Events.ON_SELECT, CE_DUPLICATE_IGNORE
 				| CE_IMPORTANT);
 	}
 	private Component owner = this;
 	private String _align = "center";
 	private int _duration = 700;
 	private int _level = 2;
 	private String _nodetype = "rectangle";
 	private String _orient = "left";
 	private SpaceTreeModel<?> _model;
 	private String _json = "{}";
 	private SpaceTreeNode<?> _sel = null;
 	private String _cmd = "";
 	private String _addNodeJson = "{}";
 	private boolean init = true;
 	private transient ItemRenderer _renderer;
 	private transient TreeDataListener _dataListener;
 	private static final String ATTR_ON_INIT_RENDER_POSTED = "org.zkoss.zul.Tree.onInitLaterPosted";
 	private static final ItemRenderer _defRend = new ItemRenderer() {
 		@Override
 		public String render(final Component owner, final Object data, final int index) {
 			final Orgchart self = (Orgchart) owner;
 			final Template tm = self.getTemplate("model");
 			if (tm == null)
 				return Objects.toString(data);
 			else {
 				final Component[] items = tm.create(owner, null,
 						new VariableResolver() {
 							public Object resolveVariable(String name) {
 								if ("each".equals(name)) {
 									return data;
 								} else if ("forEachStatus".equals(name)) {
 									return new ForEachStatus() {
 										@Override
 										public ForEachStatus getPrevious() {
 											return null;
 										}
 										@Override
 										public Object getEach() {
 											return data;
 										}
 										@Override
 										public int getIndex() {
 											return index;
 										}
 										@Override
 										public Integer getBegin() {
 											return 0;
 										}
 										@Override
 										public Integer getEnd() {
 											throw new UnsupportedOperationException("end not available");
 										}
 									};
 								} else {
 									return null;
 								}
 							}
 						}, null);
 				if (items.length != 1)
 					throw new UiException(
 							"The model template must have exactly one item, not "
 									+ items.length);
 				if (!(items[0] instanceof Label))
 					throw new UiException(
 							"The model template can only support Label component, not "
 									+ items[0]);
 				items[0].detach();
 				return ((Label) items[0]).getValue();
 			}
 		}
 	};
 
 	public SpaceTreeNode<?> find(String id) {
 		return _model.find(id);
 	}
 
 	private String getAddNodeJson() {
 		return _addNodeJson;
 	}
 
 	private void setAddNodeJson(String addNodeJson) {
 		if (!Objects.equals(_addNodeJson, addNodeJson)) {
 			_addNodeJson = addNodeJson;
 		}
 		smartUpdate("addNodeJson", _addNodeJson);
 	}
 
 	public String getAlign() {
 		return _align;
 	}
 
 	public void setAlign(String align) {
 		if (!"|left|center|right|".contains(align))
 			throw new WrongValueException("Illegal align: " + align);
 		if (!Objects.equals(_align, align)) {
 			_align = align;
 			smartUpdate("align", _align);
 		}
 	}
 
 	public ItemRenderer getItemRenderer() {
 		return _renderer;
 	}
 
 	public void setItemRenderer(ItemRenderer _renderer) {
 		this._renderer = _renderer;
 	}
 
 	private String getCmd() {
 		return _cmd;
 	}
 
 	private void setCmd(String cmd) {
 		if (!"|add|remove|refresh|".contains(cmd))
 			throw new WrongValueException("Illegal cmd: " + cmd);
 		if (!Objects.equals(_cmd, cmd)) {
 			_cmd = cmd;
 		}
 		smartUpdate("cmd", _cmd);
 	}
 
 	public int getDuration() {
 		return _duration;
 	}
 
 	public void setDuration(int duration) {
 		if (_duration != duration && duration >= 0) {
 			_duration = duration;
 			smartUpdate("duration", _duration);
 		}
 	}
 
 	private String getJson() {
 		return _json;
 	}
 
 	private void setJson(String json) {
 		if (!Objects.equals(_json, json)) {
 			_json = json;
 			smartUpdate("json", _json);
 		}
 	}
 
 	public int getLevel() {
 		return _level;
 	}
 
 	public void setLevel(int level) {
 		if (_level != level) {
 			_level = level;
 			smartUpdate("level", _level);
 		}
 	}
 
 	public SpaceTreeModel<?> getModel() {
 		return _model;
 	}
 
 	public void setModel(SpaceTreeModel<?> model) {
 		if (model != null) {
 			if (!(model instanceof SpaceTreeModel))
 				throw new UiException(model.getClass() + " must implement "
 						+ SpaceTreeModel.class);
 
 			SpaceTreeNode<?> root = (SpaceTreeNode<?>) model.getRoot();
 			SpaceTreeNode<?> spacetreeRoot = model.getSpaceTreeRoot();
 			if (init) {
 				SpaceTreeNode seldNode = model.getSelectedNode();
 				if(seldNode != null)
 					setSelectedNode(seldNode);
 				else 
 					setSelectedNode(spacetreeRoot);
 				init = false;
 			}
 			if (_model != model) {
 				if (_model != null) {
 					_model.removeTreeDataListener(_dataListener);
 				}
 				_model = (SpaceTreeModel<?>) model;
 				initDataListener();
 			}
 			postOnInitRender();
 		} else if (_model != null) {
 			_model.removeTreeDataListener(_dataListener);
 			_model = null;
 		}
 	}
 
 	public String getNodetype() {
 		return _nodetype;
 	}
 
 	public void setNodetype(String nodetype) {
 		if (!"|circle|rectangle|square|ellipse|".contains(nodetype))
 			throw new WrongValueException("Illegal nodetype: " + nodetype);
 		if (!Objects.equals(_nodetype, nodetype)) {
 			_nodetype = nodetype;
 			smartUpdate("nodetype", _nodetype);
 		}
 	}
 
 	public String getOrient() {
 		return _orient;
 	}
 
 	public void setOrient(String orient) {
		if (!"left".equals(orient) && !"right".equals(orient)
				&& !"top".equals(orient) && !"bottom".equals(orient))
 			throw new WrongValueException("Illegal orient: " + orient);
 		if (!Objects.equals(_orient, orient)) {
 			_orient = orient;
 			smartUpdate("orient", _orient);
 		}
 	}
 
 	public SpaceTreeNode<?> getSelectedNode() {
 		return _sel;
 	}
 
 	private void setSelectedNode(SpaceTreeNode<?> sel) {
 		if (!Objects.equals(_sel, sel)) {
 			_sel = sel;
 			smartUpdate("selectedNode", new Renderer().render(_sel, "{}"));
 		}
 	}
 
 	private void setJsonWithoutUpdate() {
 		SpaceTreeNode<?> spacetreeRoot = (SpaceTreeNode<?>) _model
 				.getSpaceTreeRoot();
 		final Renderer renderer = new Renderer();
 		try {
 			_json = renderChildren(renderer, spacetreeRoot);
 		} catch (Throwable ex) {
 			renderer.doCatch(ex);
 		} finally {
 			renderer.doFinally();
 		}
 	}
 
 	/**
 	 * Returns the renderer used to render items.
 	 */
 	@SuppressWarnings("unchecked")
 	private ItemRenderer getRealRenderer() {
 		return _renderer != null ? _renderer : _defRend;
 	}
 
 	/**
 	 * The default zclass is "z-orgchart"
 	 */
 	public String getZclass() {
 		return (this._zclass != null ? this._zclass : "z-orgchart");
 	}
 
 	/*
 	 * Initial Tree data listener
 	 */
 	private void initDataListener() {
 		if (_dataListener == null)
 			_dataListener = new TreeDataListener() {
 				public void onChange(TreeDataEvent event) {
 					onTreeDataChange(event);
 				}
 			};
 
 		_model.addTreeDataListener(_dataListener);
 	}
 
 	/**
 	 * Handles a private event, onInitRender. It is used only for
 	 * implementation, and you rarely need to invoke it explicitly.
 	 * 
 	 * @since 6.0.0
 	 */
 	public void onInitRender() {
 		removeAttribute(ATTR_ON_INIT_RENDER_POSTED);
 		renderTree();
 	}
 
 	// -- ComponentCtrl --//
 	/**
 	 * Handles when the tree model's content changed
 	 */
 	@SuppressWarnings("unchecked")
 	private void onTreeDataChange(TreeDataEvent event) {
 		final int type = event.getType();
 		final int[] path = event.getPath();
 		final TreeModel<?> tm = event.getModel();
 
 		SpaceTreeNode<?> node = null;
 		if (path == null) {
 			node = (SpaceTreeNode<?>) _model.getSpaceTreeRoot();
 		} else {
 			node = (SpaceTreeNode<?>) event.getModel().getChild(path);
 		}
 
 		if (node != null)
 			switch (type) {
 			case TreeDataEvent.INTERVAL_ADDED:
 				SpaceTreeNode<?> lastChild = (SpaceTreeNode<?>) node
 				.getChildAt(node.getChildCount() - 1);
 				SpaceTreeNode root = (SpaceTreeNode) _model.getRoot();
 				if(node == root && node.getChildCount() > 1) {
 					try {
 						throw new Exception("the root has one child at most");
 					} catch (Exception e) {
 						e.printStackTrace();
 						lastChild.removeFromParent();
 					}
 				} else {
 					setAddNodeJson(new Renderer().render(lastChild, "{}"));
 					setJsonWithoutUpdate();
 					setCmd("add");
 				}
 				return;
 			case TreeDataEvent.INTERVAL_REMOVED:
 				_model.clearSelection();
 				_sel = null;
 				setJsonWithoutUpdate();
 				setCmd("remove");
 				return;
 			case TreeDataEvent.CONTENTS_CHANGED:
 				renderTree();
 				setCmd("refresh");
 				return;
 			case TreeDataEvent.SELECTION_CHANGED:
 				setSelectedNode(node);
 				return;
 			}
 
 	}
 
 	private void postOnInitRender() {
 		// 20080724, Henri Chen: optimize to avoid postOnInitRender twice
 		if (getAttribute(ATTR_ON_INIT_RENDER_POSTED) == null) {
 			setAttribute(ATTR_ON_INIT_RENDER_POSTED, Boolean.TRUE);
 			Events.postEvent("onInitRender", this, null);
 		}
 	}
 
 	private String renderChildren(Renderer renderer, SpaceTreeNode<?> node)
 			throws Throwable {
 		return renderer.render(node);
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
 		if (!Objects.equals(_sel, ""))
 			render(renderer, "selectedNode", new Renderer().render(_sel, "{}"));
 		if (!Objects.equals(_addNodeJson, "{}"))
 			render(renderer, "addNodeJson", _addNodeJson);
 
 	}
 
 	private void renderTree() {
 		SpaceTreeNode<?> node = (SpaceTreeNode<?>) _model.getSpaceTreeRoot();
 		final Renderer renderer = new Renderer();
 		try {
 			setJson(renderChildren(renderer, node));
 		} catch (Throwable ex) {
 			renderer.doCatch(ex);
 		} finally {
 			renderer.doFinally();
 		}
 		// notify the tree when items have been rendered.
 		Events.postEvent(ZulEvents.ON_AFTER_RENDER, this, null);
 	}
 
 	public void service(AuRequest request, boolean everError) {
 		final String cmd = request.getCommand();
 		SelectEvent evt = SelectEvent.getSelectEvent(request);
 		Map data = request.getData();
 
 		if (Events.ON_SELECT.equals(cmd)) {
 			String seldNodeStr = data.get("selectedNode").toString();
 			JSONObject json = (JSONObject) JSONValue.parse(seldNodeStr);
 			SpaceTreeNode<?> seldNode = find(json.get("id").toString());
 			_model.addToSelection((TreeNode) seldNode);
 			Events.postEvent(evt);
 		} else {
 			super.service(request, everError);
 		}
 	}
 
 }

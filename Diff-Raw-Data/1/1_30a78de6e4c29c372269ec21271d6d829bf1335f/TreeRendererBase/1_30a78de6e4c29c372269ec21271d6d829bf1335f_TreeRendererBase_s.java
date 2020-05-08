 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.richfaces.renderkit;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.FacesException;
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.el.MethodBinding;
 
 import org.ajax4jsf.javascript.JSFunction;
 import org.ajax4jsf.javascript.JSReference;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.model.DataVisitor;
 import org.ajax4jsf.renderkit.AjaxRendererUtils;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.RendererUtils;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UITree;
 import org.richfaces.component.UITreeNode;
 import org.richfaces.component.nsutils.NSUtils;
 import org.richfaces.component.state.TreeState;
 import org.richfaces.component.state.TreeStateAdvisor;
 import org.richfaces.model.LastElementAware;
 import org.richfaces.model.TreeRange;
 import org.richfaces.model.TreeRowKey;
 
 public abstract class TreeRendererBase extends CompositeRenderer {
 
 	protected static final class RowKeyHolder {
 
 		private TreeRowKey<Object> rowKey;
 		
 		private boolean nodeKey;
 		
 		public RowKeyHolder(TreeRowKey<Object> rowKey, boolean nodeKey) {
 			super();
 			this.rowKey = rowKey;
 			this.nodeKey = nodeKey;
 		}
 		
 		public boolean isNodeKey() {
 			return nodeKey;
 		}
 		
 		public TreeRowKey<Object> getRowKey() {
 			return rowKey;
 		}
 
 		@Override
 		public String toString() {
 			return this.getClass().getSimpleName() + "[" + rowKey + "]";
 		}
 	};
 	
 	private final class RendererDataModelEventNavigator extends
 	TreeDataModelEventNavigator {
 		private final FacesContext context;
 		private final UITree tree;
 		private final Flag droppedDownToLevelFlag;
 		private final ResponseWriter writer;
 
 		private String clientId;
 		private boolean expanded;
 		private boolean showLines;
 
 		private RendererDataModelEventNavigator(UITree tree,
 				TreeRowKey floatingKey, FacesContext context, Flag droppedDownToLevelFlag) {
 			super(tree, floatingKey);
 			this.context = context;
 			this.tree = tree;
 			this.droppedDownToLevelFlag = droppedDownToLevelFlag;
 			this.writer = context.getResponseWriter();
 
 			this.expanded = this.tree.isExpanded();
 			this.showLines = this.tree.isShowConnectingLines();
 			this.clientId = getClientId();
 		}
 
 		public void followRowKey(FacesContext context, TreeRowKey newRowKey) throws IOException {
 			super.followRowKey(context, newRowKey);
 
 			this.expanded = this.tree.isExpanded();
 			this.clientId = getClientId();
 		}
 
 		private String getClientId() {
 			Object rowKey = tree.getRowKey();
 			String id;
 			if (rowKey == null) {
 				id = tree.getClientId(context)
 				+ NamingContainer.SEPARATOR_CHAR;
 			} else {
 				id = tree.getNodeFacet().getClientId(context)
 				+ NamingContainer.SEPARATOR_CHAR;
 			}
 			return id;
 		}
 
 		public void afterUp(int levels) throws IOException {
 			Context c = droppedDownToLevelFlag.getContext();
 			if (c != null) {
 				c.setHasChildren(false);
 				openDiv(c);
 				closeDiv();
 				droppedDownToLevelFlag.setContext(null);
 			}
 
 			//writer.write("** afterUp **");
 			for (int i = 0; i < levels; i++) {
 				closeDiv();
 			}
 
 			//if (!isLastElement) closeDiv();
 		}
 
 		public void afterDown() throws IOException {
 		}		
 
 		public void beforeDown() throws IOException {
 			Context c = droppedDownToLevelFlag.getContext();
 			droppedDownToLevelFlag.setContext(null);
 			openDiv(c);
 			//writer.write("** beforeDown **");
 
 			//if (this.getRowKey()==null ) openDiv();
 
 		}
 
 		public void beforeUp(int levels) throws IOException {
 		}
 
 		public void openDiv(Context context) throws IOException {
 			writer.startElement("div", tree);
 
 			if (context == null) {
 				context = new Context();
 				context.setLast(this.actualLast);
 				context.setClientId(this.clientId);
 				context.setExpanded(this.expanded);
 				context.setRowKey(this.getRowKey());
 			}
 
 			getUtils().writeAttribute(writer, "id", context.getClientId() + "childs");
 
 			if (!context.isExpanded() || !context.isHasChildren()) {
 				getUtils().writeAttribute(writer, "style", "display: none;");
 			} else {
 				if (tree.isShowConnectingLines()) {
 					TreeRowKey floatingKey = getFloatingKey();
 					//need the expression only for AJAX update root
 					if (floatingKey != null && floatingKey.equals(context.getRowKey())) {
 						String expression = "background-image:expression(this.nextSibling ? '' : 'none')";
 						getUtils().writeAttribute(writer, "style", expression);
 					}
 				}
 			}
 
 			String styleClasses = "";
 			if (context.getRowKey() != null) {
 				styleClasses = "rich-tree-node-children";
 				if (!context.isLast() && showLines) styleClasses += " rich-tree-h-ic-line";
 			}
 			if (styleClasses!="") getUtils().writeAttribute(writer, "class", styleClasses);
 		}
 
 		public void closeDiv() throws IOException {
 			writer.endElement("div");
 		}
 	}
 
 	private class DataVisitorWithLastElement implements DataVisitor,
 	LastElementAware {
 
 		private boolean isLastElement = false;
 
 		private final Flag flag;
 
 		private final UITree tree;
 
 		private final RendererDataModelEventNavigator navigator;
 
 		private TreeStateAdvisor methodBindingAdvisor = null;
 
 		private Object floatingKey;
 
 		private DataVisitorWithLastElement(Flag flag, UITree tree,
 				RendererDataModelEventNavigator navigator, Object rowKey) {
 			this.flag = flag;
 			this.tree = tree;
 			this.navigator = navigator;
 			this.floatingKey = rowKey;
 		}
 
 		public void process(FacesContext context, Object rowKey, Object argument)
 		throws IOException {
 			TreeRowKey<?> treeRowKey = (TreeRowKey<?>) rowKey;
 
 			processAdvisors(context, treeRowKey);
 
 			navigator.followRowKey(context, treeRowKey);
 
 			Context c = flag.getContext();
 			if (c != null) {
 				c.setHasChildren(false);
 				navigator.openDiv(c);
 				navigator.closeDiv();
 			}
 
 			UITreeNode nodeFacet = tree.getNodeFacet();
 			Object oldAttrValue = nodeFacet.getAttributes().get("isLastElement");
 			Object oldAjaxRootAttrValue = nodeFacet.getAttributes().get("isAjaxUpdateRoot");
 			try {
 				nodeFacet.getAttributes().put("isLastElement", new Boolean(isLastElement));
 				nodeFacet.getAttributes().put("isAjaxUpdateRoot", new Boolean(floatingKey != null && floatingKey.equals(rowKey)));
 				ResponseWriter writer = context.getResponseWriter();
 				if (isLastElement && this.navigator.showLines) {
 					writer.startElement("p", tree);
 					writer.writeAttribute("class", "rich-tree-last-node-marker", null);
 					writer.endElement("p");
 				}
 
 				renderChild(context, nodeFacet);
 
 
 				c = new Context();
 				c.setClientId(nodeFacet.getClientId(context) + NamingContainer.SEPARATOR_CHAR);
 				c.setLast(this.isLastElement);
 				c.setExpanded(tree.isExpanded());
 				c.setRowKey(tree.getRowKey());
 				flag.setContext(c);
 
 				//writer.write("** after renderChild **");
 				//navigator.openDiv();
 			} finally {
 				if (oldAttrValue != null) {
 					nodeFacet.getAttributes().put("isLastElement", oldAttrValue);
 				} else {
 					nodeFacet.getAttributes().remove("isLastElement");
 				}
 
 				if (oldAjaxRootAttrValue != null) {
 					nodeFacet.getAttributes().put("isAjaxUpdateRoot", oldAjaxRootAttrValue);
 				} else {
 					nodeFacet.getAttributes().remove("isAjaxUpdateRoot");
 				}
 			}
 		}
 
 		public void setLastElement() {
 			isLastElement = true;
 			navigator.setLastElement();
 		}
 
 		public void resetLastElement() {
 			isLastElement = false;
 			navigator.resetLastElement();
 		}
 
 		public void processAdvisors(FacesContext context, TreeRowKey rowKey) throws IOException {
 			TreeState state = (TreeState) tree.getComponentState();
 			TreeStateAdvisor stateAdvisor = (TreeStateAdvisor)tree.getStateAdvisor();
 
 			if (null == stateAdvisor) {
 				if (null == methodBindingAdvisor) {
 					methodBindingAdvisor = new TreeStateAdvisor() {
 						public Boolean adviseNodeOpened(UITree tree) {
 							MethodBinding adviseNodeOpened = tree.getAdviseNodeOpened();
 							if (null != adviseNodeOpened) {
 								return (Boolean) adviseNodeOpened.invoke(FacesContext.getCurrentInstance(), new Object[] {tree});
 							}
 							return null;
 						}
 
 						public Boolean adviseNodeSelected(UITree tree) {
 							MethodBinding adviseNodeSelected = tree.getAdviseNodeSelected();
 							if (null != adviseNodeSelected) {
 								return (Boolean) adviseNodeSelected.invoke(FacesContext.getCurrentInstance(), new Object [] {tree});
 							}
 							return null;
 						}
 					};
 				}
 				stateAdvisor = methodBindingAdvisor;
 			}
 
 			Boolean adviseOpened = stateAdvisor.adviseNodeOpened(tree); 
 			if (null != adviseOpened) {
 				if (adviseOpened.booleanValue()) {
 					state.makeExpanded(rowKey);
 				} else {
 					state.makeCollapsed(rowKey);
 				}
 			}
 			
 			Boolean adviseSelected = stateAdvisor.adviseNodeSelected(tree); 
 			if (null != adviseSelected) {
 				if (adviseSelected.booleanValue()) {
 					if (!state.isSelected(rowKey)) {
 						state.setSelected(rowKey);
 					}
 				}
 				else {
 					if (state.isSelected(rowKey)) {
 						state.setSelected(null);
 					}
 				}
 			}
 		}
 	}
 
 	public TreeRendererBase() {
 		super();
 		addContributor(DraggableRendererContributor.getInstance());
 		addContributor(DropzoneRendererContributor.getInstance());
 
 		addParameterEncoder(DnDParametersEncoder.getInstance());
 	}
 
 	public void writeNamespace(FacesContext context, UIComponent component) throws IOException {
 		NSUtils.writeNameSpace(context, component);
 	}
 
 	private List<RowKeyHolder> getKeyHoldersList(Set subTreeKeys, 
 			Set nodeKeys, String treePath) {
 		
 		if (subTreeKeys != null && subTreeKeys.contains(null)) {
 			List<RowKeyHolder> list = new ArrayList<RowKeyHolder>(1);
 			list.add(new RowKeyHolder(null, false));
 			return list;
 		}
 		
 		List<RowKeyHolder> list = new ArrayList<RowKeyHolder>((subTreeKeys == null ? 0 : subTreeKeys.size()) + 
 				(nodeKeys == null ? 0 : nodeKeys.size()));
 		
 		if (subTreeKeys != null) {
 			for (Object subTreeKey : subTreeKeys) {
 				list.add(new RowKeyHolder((TreeRowKey<Object>) subTreeKey, false));
 			}
 		}
 
 		if (nodeKeys != null) {
 			for (Object nodeKey : nodeKeys) {
 				TreeRowKey<Object> treeRowKey = (TreeRowKey<Object>) nodeKey;
 				if (treeRowKey != null && treeRowKey.depth() != 0) {
 					list.add(new RowKeyHolder(treeRowKey, true));
 				} else {
 					log.warn("Top node of the [" + treePath + "] tree cannot be re-rendered without subnodes");
 				}
 			}
 		}
 		
 		return list;
 	}
 	
 	public void encodeAjaxChildren(FacesContext context, UIComponent component,
 			String path, Set ids, Set renderedAreas) throws IOException {
 		super.encodeAjaxChildren(context, component, path, ids, renderedAreas);
 
 		try {
 			if (component instanceof UITree) {
 				UITree tree = (UITree) component;
 
 				String id = path + tree.getId();
 
 				tree.captureOrigValue();
 				//Object rowKey = tree.getRowKey();
 
 				boolean encodeScripts = false;
 
 				tree.setRowKey(context, null);
 
 				//we should add xmlns to AJAX response
 				//we'll write neutral inner element and add xmlns there
 				ResponseWriter responseWriter = context.getResponseWriter();
 				responseWriter.startElement("div", tree);
 				writeNamespace(context, component);
 
 				List encodedAreaIds = new ArrayList();
 				
 				try {
 					List<RowKeyHolder> keyHoldersList = getKeyHoldersList(
 						tree.getAllAjaxKeys(), 
 						tree.getAllAjaxNodeKeys(),
 						id);
 
 					Collections.sort(keyHoldersList, new Comparator<RowKeyHolder>() {
 
 						public int compare(RowKeyHolder o1, RowKeyHolder o2) {
 							int d1 = o1.rowKey == null ? 0 : o1.rowKey.depth();
 							int d2 = o2.rowKey == null ? 0 : o2.rowKey.depth();
 
 							return d1 < d2 ? -1 : (d2 > d1 ? 1 : 0);
 						}
 
 					});
 
 					List<RowKeyHolder> holders = new ArrayList<RowKeyHolder>();
 					for (RowKeyHolder holder : keyHoldersList) {
 						boolean isSubKey = false;
 
 						for (RowKeyHolder rowKeyHolder : holders) {
 							if (rowKeyHolder.rowKey == null || 
 									rowKeyHolder.rowKey.isSubKey(holder.rowKey)) {
 
 								isSubKey = true;
 								break;
 							}
 						}
 
 						if (!isSubKey) {
 							holders.add(holder);
 						}
 					}
 
 					Iterator<RowKeyHolder> ajaxKeysItr = holders.iterator();
 					while (ajaxKeysItr.hasNext()) {
 						RowKeyHolder keyHolder = ajaxKeysItr.next();
 						TreeRowKey key = keyHolder.getRowKey();
 
 						if (key != null && key.depth() == 0) {
 							key = null;
 						}
 
 						tree.setRowKey(context, key);
 
 						if (key == null || tree.isRowAvailable()) {
 							String treeClientId;
 							if (key == null) {
 								treeClientId = tree.getClientId(context);
 							} else {
 								treeClientId = tree.getNodeFacet().getClientId(context);
 							}
 
 							encodeScripts = true;
 							
 							//should be added before children id
 							renderedAreas.add(treeClientId);
 
 							if (keyHolder.isNodeKey()) {
 								writeContent(context, tree, key, false);
 							} else {
 								writeContent(context, tree, key, true);
 								String treeChildrenId = treeClientId + NamingContainer.SEPARATOR_CHAR + "childs";
 								renderedAreas.add(treeChildrenId);
 							}
 
 							//add node to set of nodes refreshed by script
 							encodedAreaIds.add(treeClientId);
 						} else {
 							String cid = tree.getClientId(context);
 							String message = MessageFormat.format(
 									"Failed to re-render tree node: {0} due to model data unavailability! " +
 									"Maybe parent node should be re-rendered instead?", 
 									new Object[] { cid });
 
 							ExternalContext externalContext = context.getExternalContext();
 							externalContext.log(message);
 						}
 					}
 					//ajaxKeys.clear();
 				} catch (Exception e) {
 					throw new FacesException(e);
 				} finally {
 					try {
 						tree.setRowKey(context, null);
 						tree.restoreOrigValue();
 					} catch (Exception e) {
 						context.getExternalContext().log(e.getMessage(), e);
 					}
 				}
 
 				if (encodeScripts) {
 					writeScript(context, tree, encodedAreaIds, renderedAreas);
 				}
 
 				responseWriter.endElement("div");
 				tree.clearRequestKeysSet();
 			}
 		} finally {
 			try {
 				ComponentsVariableResolver.removeVariables(this, component);
 			} catch (Exception e) {
 				context.getExternalContext().log(e.getMessage(), e);
 			}
 		}
 	}
 
 	protected String getSelectionValue(FacesContext context, UITree tree) {
 		String result = "";
 		TreeState treeState = (TreeState) tree.getComponentState();
 		TreeRowKey selectedNodeKey = treeState.getSelectedNode();
 		if (selectedNodeKey != null) {
 			Object rowKey = tree.getRowKey();
 			try {
 				tree.setRowKey(selectedNodeKey);
 				if (tree.isRowAvailable()) {
 					result = tree.getNodeFacet().getClientId(context);
 				}
 			} finally {
 				try {
 					tree.setRowKey(rowKey);
 				} catch (Exception e) {
 					context.getExternalContext().log(e.getMessage(), e);
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	public String encodeSelectionStateInput(FacesContext context, UITree tree) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		writer.startElement("input", tree);
 		writer.writeAttribute("type", "hidden", null);
 		String selectionHolderInputId = tree.getSelectionStateInputName(context);
 		writer.writeAttribute("id", selectionHolderInputId, null);
 		writer.writeAttribute("name", selectionHolderInputId, null);
 
 		writer.writeAttribute("value", getSelectionValue(context, tree), null);
 		writer.endElement("input");
 
 		return selectionHolderInputId;
 	}
 
 	protected String getAjaxScript(FacesContext context, UITree tree) {
 		String id = tree.getBaseClientId(context);
 		JSFunction function = AjaxRendererUtils
 		.buildAjaxFunction(tree, context);
 		Map eventOptions = AjaxRendererUtils.buildEventOptions(context, tree);
 		Map parameters = (Map) eventOptions.get("parameters");
 		parameters.remove(id);
 		parameters.put(id + UITree.SELECTED_NODE_PARAMETER_NAME,
 				new JSReference("event.selectedNode"));
 		function.addParameter(eventOptions);
 		StringBuffer buffer = new StringBuffer();
 		function.appendScript(buffer);
 		buffer.append("; return false;");
 		return buffer.toString();
 	}
 
 	public String writeScriptElement(FacesContext context, UITree tree, String code) throws IOException {
 		/*
 		<div id="#{clientId}:script" class="rich-tree-h">
 			<script type="text/javascript">
 			</script>
 		</div>
 		*/
 		
 		String scriptId = tree.getBaseClientId(context) + NamingContainer.SEPARATOR_CHAR + "script";
 		ResponseWriter responseWriter = context.getResponseWriter();
 		responseWriter.startElement(HTML.DIV_ELEM, tree);
 		responseWriter.writeAttribute(HTML.id_ATTRIBUTE, scriptId, null);
 		responseWriter.writeAttribute(HTML.class_ATTRIBUTE, "rich-tree-h", null);
 
 		responseWriter.startElement(HTML.SCRIPT_ELEM, tree);
 		responseWriter.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
 
 		if (code != null && code.length() != 0) {
 			responseWriter.writeText(code, null);
 		}
 		
 		responseWriter.endElement(HTML.SCRIPT_ELEM);
 		
 		responseWriter.endElement(HTML.DIV_ELEM);
 		
 		return scriptId;
 	}
 	
 	private void writeScript(FacesContext context, UITree tree, List encodedAreaIds,
 			Set renderedAreas) throws IOException {
 
 		final String clientId = tree.getBaseClientId(context);
 		StringBuffer sb = new StringBuffer("$(");
 		sb.append(ScriptUtils.toScript(clientId));
 		sb.append(").component.");
 		
 		new JSFunction("refreshAfterAjax", encodedAreaIds, getSelectionValue(context, tree)).appendScript(sb);
 		
 		renderedAreas.add(writeScriptElement(context, tree, sb.toString()));
 	}
 
 	public void encodeChildren(FacesContext context, UIComponent component)
 	throws IOException {
 
 		writeContent(context, (UITree) component, null, true);
 	}
 
 	public void writeContent(final FacesContext context, final UITree input)
 	throws IOException {
 		writeContent(context, input, null, true);
 	}
 
 	public void writeContent(final FacesContext context, final UITree input,
 			TreeRowKey key, final boolean withSubnodes) throws IOException {
 		// simple flag can be used here because
 		// we cannot jump more than one level down until next node
 		// when rendering
 		Flag droppedDownToLevelFlag = new Flag();
 
 		TreeRowKey rowKey = (TreeRowKey) key;
 
 		//Object savedRowKey = input.getRowKey();
 		try {
 			input.captureOrigValue();
 
 			input.setRowKey(context, key);
 
 			RendererDataModelEventNavigator levelNavigator = new RendererDataModelEventNavigator(input, rowKey, context,
 					droppedDownToLevelFlag);
 
 			final TreeRange stateRange = (TreeRange) input.getComponentState().getRange();
 			TreeRange treeRange = new TreeRange() {
 
 				public boolean processChildren(TreeRowKey rowKey) {
 					return withSubnodes ? stateRange.processChildren(rowKey) : false;
 				}
 
 				public boolean processNode(TreeRowKey rowKey) {
 					Object currentKey = input.getRowKey();
 
 					if (currentKey == null ? rowKey != null : !currentKey.equals(rowKey)) {
 						//currentKey NE rowKey
 						input.setRowKey(context, rowKey);
 					}
 
 					UITreeNode nodeFacet = input.getNodeFacet();
 					if (!nodeFacet.isRendered()) {
 						return false;
 					}
 
 					return stateRange.processNode(rowKey);
 				}
 
 			};
 
 			input.transferQueuedNode();
 			
 			//TODO should render if current node not in range?
 			input.walk(context, new DataVisitorWithLastElement(droppedDownToLevelFlag, input,
 					levelNavigator, key), treeRange, key, null);
 
 			levelNavigator.followRowKey(context, null);
 		} finally {
 			input.setRowKey(context, null);
 			input.restoreOrigValue();
 		}
 	}
 	
 	private static final String[] OPTIONS_ATTRIBUTES_LIST = { "showConnectingLines", "toggleOnClick", 
 		"disableKeyboardNavigation", "rightClickSelection"};
 	
 	public String getOptions(FacesContext context, UITree tree) {
 		Map<String, Object> attributes = tree.getAttributes();
 		RendererUtils utils = getUtils();
 		
 		Map<String, Object> options = new HashMap<String, Object>();
 		for (String optionAttributeName : OPTIONS_ATTRIBUTES_LIST) {
 			Object value = attributes.get(optionAttributeName);
 			
 			if (utils.shouldRenderAttribute(value)) {
 				options.put(optionAttributeName, value);
 			}
 		}
 		
 		return ScriptUtils.toScript(options);
 	}
 }
 
 class Flag {
 	private Context context;
 
 	public Context getContext() {
 		return context;
 	}
 
 	public void setContext(Context context) {
 		this.context = context;
 	}
 }
 
 class Context {
 	private String clientId;
 	private Object rowKey;
 	private boolean expanded;
 	private boolean last;
 	private boolean hasChildren = true;
 	public String getClientId() {
 		return clientId;
 	}
 	public void setClientId(String clientId) {
 		this.clientId = clientId;
 	}
 	public Object getRowKey() {
 		return rowKey;
 	}
 	public void setRowKey(Object rowKey) {
 		this.rowKey = rowKey;
 	}
 	public boolean isExpanded() {
 		return expanded;
 	}
 	public void setExpanded(boolean expanded) {
 		this.expanded = expanded;
 	}
 	public boolean isLast() {
 		return last;
 	}
 	public void setLast(boolean last) {
 		this.last = last;
 	}
 	public boolean isHasChildren() {
 		return hasChildren;
 	}
 	public void setHasChildren(boolean hasChildren) {
 		this.hasChildren = hasChildren;
 	}
 }

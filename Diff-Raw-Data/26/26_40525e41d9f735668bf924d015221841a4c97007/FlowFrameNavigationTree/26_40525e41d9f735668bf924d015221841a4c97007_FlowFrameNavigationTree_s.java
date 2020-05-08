 package org.flowframe.ui.vaadin.addons.common;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.HierarchicalContainer;
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 
 public class FlowFrameNavigationTree extends VerticalLayout {
 	private static final long serialVersionUID = -5422749160920764252L;
 
 	public static final String NAV_NODE_HEIGHT = "30px";
 	public static final byte NAV_INDENT_WIDTH = 30;
 
 	private FlowFrameNavigationTreeNode selectedNode = null;
 	private HashMap<Object, FlowFrameNavigationTreeNode> id2NodeMap = new HashMap<Object, FlowFrameNavigationTree.FlowFrameNavigationTreeNode>();
 	private String iconPropertyId = "icon", titlePropertyId = "name";
 	private ValueChangeListener navigationListener;
 
 	public FlowFrameNavigationTree() {
 		setWidth("100%");
 		setHeight(-1.0f, Component.UNITS_PIXELS);
 	}
 
 	private FlowFrameNavigationTreeNode createNode(HierarchicalContainer container, Object itemId) {
 		Item item;
 		FlowFrameNavigationTreeNode node, childNode;
 		Resource icon;
 		String title;
 		Collection<?> children;
 
 		item = container.getItem(itemId);
 		if (item != null) {
 			if (item.getItemProperty(this.iconPropertyId) != null && item.getItemProperty(this.iconPropertyId).getValue() != null) {
 				icon = (Resource) item.getItemProperty(this.iconPropertyId).getValue();
 			} else {
 				if (container.getChildren(itemId) != null && container.getChildren(itemId).size() > 0) {
 					icon = new ThemeResource("tree/img/conx-nav-default-node-1.png");
 				} else {
 					icon = new ThemeResource("tree/img/conx-nav-default-node-2.png");
 				}
 			}
 			if (item.getItemProperty(this.titlePropertyId) != null && item.getItemProperty(this.titlePropertyId).getValue() != null) {
 				title = (String) item.getItemProperty(this.titlePropertyId).getValue();
 			} else {
 				title = "Untitled";
 			}
 			node = new FlowFrameNavigationTreeNode(icon, title, itemId);
 			this.id2NodeMap.put(itemId, node);
 			// Take care of children first
 			if ((children = container.getChildren(itemId)) != null) {
 				for (Object childItemId : children) {
 					childNode = createNode(container, childItemId);
 					node.addChild(childNode);
 				}
 			}
 			// Root elements must be added at the highest level
			if (container.getParent(itemId) == null) {
 				addComponent(node);
 			}
 			return node;
 		} else {
 			return null;
 		}
 	}
 
	public void setContainerItemDataSource(HierarchicalContainer container) {
 		if (container == null)
 			throw new IllegalArgumentException("The container parameter (HierarchicalContainer container) was null.");
 		// Clear the tree first
 		removeAllComponents();
 		// Add the root items and their children recursively
		for (Object itemId : container.rootItemIds()) {
			if (itemId != null) {
				createNode(container, itemId);
 			}
 		}
 	}
 
 	public ValueChangeListener getNavigationListener() {
 		return this.navigationListener;
 	}
 
 	public void setNavigationListener(ValueChangeListener navigationListener) {
 		this.navigationListener = navigationListener;
 	}
 
 	public void setIconPropertyId(String iconPropertyId) {
 		this.iconPropertyId = iconPropertyId;
 	}
 
 	public String getIconPropertyId() {
 		return this.iconPropertyId;
 	}
 
 	public void setTitlePropertyId(String titlePropertyId) {
 		this.titlePropertyId = titlePropertyId;
 	}
 
 	public String getTitlePropertyId() {
 		return this.titlePropertyId;
 	}
 
 	public final class FlowFrameNavigationTreeNode extends VerticalLayout {
 		private static final long serialVersionUID = 7820784759188639717L;
 
 		private final HorizontalLayout node;
 		private VerticalLayout children;
 
 		private final HorizontalLayout indent;
 		private Embedded icon, arrow;
 		private final Label title;
 
 		private int level = 0;
 		private FlowFrameNavigationTreeNode parent;
 		private final Object id;
 
 		public Object getId() {
 			return this.id;
 		}
 
 		public void removeChild(FlowFrameNavigationTreeNode child) {
 			if (this.children != null) {
 				this.children.removeComponent(child);
 				if (this.children.getComponentCount() == 0) {
 					removeComponent(this.children);
 					this.children = null;
 					this.node.removeStyleName("parent");
 				}
 			}
 		}
 
 		public void addChild(FlowFrameNavigationTreeNode newChild) {
 			if (newChild.parent != null) {
 				// Check if this child already has a parent...
 				// ...if so, we need to orphan it first
 				newChild.parent.removeChild(newChild);
 			}
 
 			newChild.setIndentLevel(this.level + 1);
 			newChild.parent = this;
 
 			if (this.children == null) {
 				this.children = new VerticalLayout();
 				this.children.setWidth("100%");
 				this.children.setHeight(-1.0f, Component.UNITS_PIXELS);
 				this.children.setVisible(false);
 				addComponent(this.children);
 				if (!this.node.getStyleName().contains("parent"))
 					this.node.addStyleName("parent");
 			}
 
 			this.children.addComponent(newChild);
 		}
 
 		public void setIndentLevel(int indentLevel) {
 			this.level = indentLevel;
 			this.indent.setWidth(NAV_INDENT_WIDTH * indentLevel, Component.UNITS_PIXELS);
 			if (this.children != null) {
 				Iterator<Component> it = this.children.getComponentIterator();
 				Component childComponent;
 				while (it.hasNext()) {
 					childComponent = it.next();
 					if (childComponent instanceof FlowFrameNavigationTreeNode) {
 						((FlowFrameNavigationTreeNode) childComponent).setIndentLevel(indentLevel + 1);
 					}
 				}
 			}
 		}
 
 		public FlowFrameNavigationTreeNode(Resource icon, String title, Object id) {
 			this.id = id;
 
 			this.node = new HorizontalLayout();
 			this.node.setWidth("100%");
 			this.node.setHeight(NAV_NODE_HEIGHT);
 
 			this.indent = new HorizontalLayout();
 			this.setIndentLevel(level);
 
 			this.arrow = new Embedded();
 			this.arrow.setHeight("9px");
 			this.arrow.setWidth("6px");
 			this.arrow.setStyleName("conx-side-nav-node-arrow");
 
 			this.icon = new Embedded();
 			this.icon.setIcon(icon);
 			this.icon.setHeight("16px");
 			this.icon.setWidth("16px");
 			this.icon.setStyleName("conx-side-nav-node-icon");
 
 			this.title = new Label(title);
 			this.title.setWidth(-1.0f, Component.UNITS_PIXELS);
 			this.title.setHeight(-1.0f, Component.UNITS_PIXELS);
 			this.title.setStyleName("conx-side-nav-node-title");
 
 			this.node.addComponent(this.indent);
 			this.node.addComponent(this.arrow);
 			this.node.setComponentAlignment(this.arrow, Alignment.MIDDLE_LEFT);
 			this.node.addComponent(this.icon);
 			this.node.addComponent(this.title);
 			this.node.setComponentAlignment(this.title, Alignment.MIDDLE_LEFT);
 			this.node.setExpandRatio(this.title, 1.0f);
 
 			this.node.setSpacing(true);
 			this.node.setStyleName("conx-side-nav-node");
 			this.node.addListener(new LayoutClickListener() {
 				private static final long serialVersionUID = -1318694925279326919L;
 
 				public void layoutClick(LayoutClickEvent event) {
 					if (FlowFrameNavigationTreeNode.this.children != null && FlowFrameNavigationTreeNode.this.children.getComponentCount() > 0) {
 						// Is a parent node
 						if (!FlowFrameNavigationTreeNode.this.node.getStyleName().contains("expanded")) {
 							// We need to expand this node
 							FlowFrameNavigationTreeNode.this.node.addStyleName("expanded");
 							FlowFrameNavigationTreeNode.this.children.setVisible(true);
 						} else {
 							// This node is already expanded, so now we have to
 							// collapse it
 							FlowFrameNavigationTreeNode.this.node.removeStyleName("expanded");
 							FlowFrameNavigationTreeNode.this.children.setVisible(false);
 						}
 					} else {
 						if (FlowFrameNavigationTree.this.selectedNode != FlowFrameNavigationTreeNode.this) {
 							// Is a leaf node
 							if (FlowFrameNavigationTree.this.selectedNode != null) {
 								FlowFrameNavigationTree.this.selectedNode.node.removeStyleName("selected");
 							}
 							if (!FlowFrameNavigationTreeNode.this.node.getStyleName().contains("selected")) {
 								FlowFrameNavigationTreeNode.this.node.addStyleName("selected");
 							}
 							FlowFrameNavigationTree.this.selectedNode = FlowFrameNavigationTreeNode.this;
 							// Trigger the navigation event
 							if (FlowFrameNavigationTree.this.navigationListener != null) {
 								FlowFrameNavigationTree.this.navigationListener.valueChange(new ValueChangeEvent() {
 									private static final long serialVersionUID = 2913638320938169329L;
 
 									public Property getProperty() {
 										return new Property() {
 											private static final long serialVersionUID = -8365483164249526714L;
 
 											public Object getValue() {
 												return FlowFrameNavigationTree.this.selectedNode.getId();
 											}
 
 											public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 											}
 
 											public Class<?> getType() {
 												return FlowFrameNavigationTree.this.selectedNode.getId().getClass();
 											}
 
 											public boolean isReadOnly() {
 												return true;
 											}
 
 											public void setReadOnly(boolean newStatus) {
 											}
 
 										};
 									}
 								});
 							}
 						}
 					}
 				}
 			});
 
 			setWidth("100%");
 			setHeight(-1.0f, Component.UNITS_PIXELS);
 			addComponent(this.node);
 		}
 	}
 }

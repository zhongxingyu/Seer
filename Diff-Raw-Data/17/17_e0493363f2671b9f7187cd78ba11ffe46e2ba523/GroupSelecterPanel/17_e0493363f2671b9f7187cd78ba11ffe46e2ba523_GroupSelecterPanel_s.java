 /**
  * 
  */
 package net.frontlinesms.ui.handler.contacts;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.apache.log4j.Logger;
 
 import thinlet.Thinlet;
 
 import net.frontlinesms.FrontlineSMS;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.unmodifiable.UnmodifiableGroupDao;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.handler.BasePanelHandler;
 
 /**
  * @author aga
  */
 public class GroupSelecterPanel extends BasePanelHandler {
 	private Logger LOG = Logger.getLogger(this.getClass());
 
 	private static final String XML_LAYOUT_GROUP_PANEL = "/ui/core/contacts/pnGroupSelecter.xml";
 	private static final String COMPONENT_GROUP_TREE = "trGroups";
 	
 	/** Thinlet UI component: the group tree */
 	private Object groupTreeComponent;
 	
 	private GroupSelecterPanelOwner owner;
 
 	private UnmodifiableGroupDao groupDao;
 	
 	private boolean allowMultipleSelections;
 
 	private Group rootGroup;
 
 //> CONSTRUCTORS
 	public GroupSelecterPanel(UiGeneratorController ui, GroupSelecterPanelOwner owner) {
 		super(ui);
 		this.owner = owner;
 		
 		allowMultipleSelections = owner instanceof MultiGroupSelecterPanelOwner;
 	}
 
 	/** Initialise the selecter. */
 	public void init(Group rootGroup) {
 		super.loadPanel(XML_LAYOUT_GROUP_PANEL);
 		
 		// TODO update selection of group tree appropriate to allowMultipleSelections
 
 		// Cache the group tree
 		groupTreeComponent = super.find(COMPONENT_GROUP_TREE);
 		
 		// add nodes for group tree
 		this.rootGroup = rootGroup;
 	}
 
 	/**
 	 * Refresh the list of groups
 	 * This method will reload the view of groups.
 	 */
 	public void refresh() {
 		Object groupTree = getGroupTreeComponent();
 		
 		FrontlineSMS frontlineController = ((UiGeneratorController) super.ui).getFrontlineController();
 		this.groupDao = new UnmodifiableGroupDao(frontlineController.getGroupDao());
 		
 		ui.removeAll(groupTree);
 		ui.add(groupTree, createNode(rootGroup, true));
 	}
 	
 //> ACCESSORS
 	/** @return a single group selected in the tree, or <code>null</code> if none is selected */
 	public Group getSelectedGroup() {
 		assert(!this.allowMultipleSelections) : "Cannot get a single selection if multiple groups are selectable.";
 		return ui.getAttachedObject(ui.getSelectedItem(this.getGroupTreeComponent()), Group.class);
 	}
 	
 	/** @return groups selected in the tree */
 	public Collection<Group> getSelectedGroups() {
 		assert(this.allowMultipleSelections) : "Cannot get multiple groups if multiple selection is not enabled.";
 		Object[] selectedItems = ui.getSelectedItems(this.getGroupTreeComponent());
 		HashSet<Group> groups = new HashSet<Group>();
 		for(Object selected : selectedItems) {
 			groups.add(ui.getAttachedObject(selected, Group.class));
 		}
 		return groups;
 	}
 	
 	/** FIXME this is only here for debug purposes. */
 	@Override
 	public Object getPanelComponent() {
 		return super.getPanelComponent();
 	}
 	
 	/** Adds a new group to the tree */
 	public void addGroup(Group group) {
 		Object groupTree = this.getGroupTreeComponent();
 		Group parent = group.getParent();
 		Object parentGroupNode;
 		if(parent != null && !parent.isRoot()) {
 			parentGroupNode = getNodeForGroup(groupTree, parent);
 		} else {
 			// There is no parent for the group, so we need to get the node for the root group
 			parentGroupNode = ui.getItem(groupTree, 0);
 		}
 		ui.add(parentGroupNode, createNode(group, true));
 	}
 	
 //> UI EVENT METHODS
 	public void selectionChanged() {
 		if(owner instanceof SingleGroupSelecterPanelOwner) {
 			((SingleGroupSelecterPanelOwner) owner).groupSelectionChanged(getSelectedGroup());
 		} else {
 			((MultiGroupSelecterPanelOwner) owner).groupSelectionChanged(getSelectedGroups());
 		}
 	}
 
 //> UI HELPER METHODS
 	/**
 	 * Gets the node we are currently displaying for a group.
 	 * @param component
 	 * @param group
 	 * @return
 	 */
 	private Object getNodeForGroup(Object component, Group group) {
 		assert(group!=null) : "Cannot get a node for a null group.";
 		Object ret = null;
 		for (Object o : this.ui.getItems(component)) {
 			Group g = ui.getAttachedObject(o, Group.class);
 			if (g.equals(group)) {
 				ret = o;
 				break;
 			} else {
 				ret = getNodeForGroup(o, group);
 				if (ret != null) break;
 			}
 		}
 		return ret;
 	}
 	
 	/** @return the group tree TODO cache this - it's not going to change, and we use it a lot */
 	public Object getGroupTreeComponent() {
 		return this.groupTreeComponent;
 	}
 	
 	/**
 	 * Creates a node for the supplied group, creating nodes for its sub-groups and contacts as well.
 	 * 
 	 * @param group The group to be put into a node.
 	 * @param showContactsNumber set <code>true</code> to show the number of contacts per group in the node's text or <code>false</code> otherwise
 	 *   TODO removing this argument, and treating it as always <code>false</code> speeds up the contact tab a lot
 	 * @return
 	 */
 	private Object createNode(Group group, boolean showContactsNumber) {
 		LOG.trace("ENTER");
 		
 		LOG.debug("Group [" + group.getName() + "]");
 		
 		String toSet = group.getName();
 //		if (showContactsNumber) {
 //			toSet += " (" + this.groupMembershipDao.getMemberCount(group) + ")";
 //		}
 		
 		Object node = ui.createNode(toSet, group);
 
 		if(ui.getBoolean(node, Thinlet.EXPANDED)
 				/*&& groupDao.hasDescendants(group)*/) {
 			ui.setIcon(node, Icon.FOLDER_OPEN);
 		} else {
 			ui.setIcon(node, Icon.FOLDER_CLOSED);
 		}
 		
 		// Add subgroup components to this node
 		for (Group subGroup : groupDao.getChildGroups(group)) {
 			Object groupNode = createNode(subGroup, showContactsNumber);
 			ui.add(node, groupNode);
 		}
 		LOG.trace("EXIT");
 		return node;
 	}
 }

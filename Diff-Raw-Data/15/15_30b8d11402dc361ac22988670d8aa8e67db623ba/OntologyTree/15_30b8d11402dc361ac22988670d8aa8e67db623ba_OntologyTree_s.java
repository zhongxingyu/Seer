 package net.shrine.webclient.client.widgets;
 
 import static net.shrine.webclient.client.util.Util.first;
 import static net.shrine.webclient.client.util.Util.last;
 import static net.shrine.webclient.client.widgets.DataDictionaryRow.shrineRoot;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import net.shrine.webclient.client.controllers.Controllers;
 import net.shrine.webclient.client.services.OntologyService;
 import net.shrine.webclient.client.services.Services;
 import net.shrine.webclient.client.util.Util;
 import net.shrine.webclient.shared.domain.OntNode;
 
 import org.fusesource.restygwt.client.Method;
 import org.fusesource.restygwt.client.MethodCallback;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 
 /**
  * 
  * @author clint
  * @date Mar 30, 2012
  */
 public final class OntologyTree extends Composite {
 	private static final OntologyService ontologyService = Services.makeOntologyService();
 	
 	private final Controllers controllers;
 	
 	private final EventBus eventBus;
 	
 	private final Map<OntNode, OntNode> parentsToChildren = Util.makeHashMap();
 	
 	private final OntNode ontNodeToBrowseTo;
 	
 	public OntologyTree(final EventBus eventBus, final Controllers controllers, final List<OntNode> pathFromRoot) {
 		super();
 		
 		Util.requireNotNull(eventBus);
 		Util.requireNotNull(controllers);
 		
 		this.controllers = controllers;
 		this.eventBus = eventBus;
 		
 		initParentsToDescendantsMap(pathFromRoot);
 		
 		ontNodeToBrowseTo = last(pathFromRoot);
 		
 		initWidget(makeTree(makeTreeItems(pathFromRoot)));
 	}
 
 	private void initParentsToDescendantsMap(List<OntNode> pathFromRoot) {
 		if(pathFromRoot.size() < 2) {
 			return;
 		}
 		
 		for(final List<OntNode> pair : Util.pairWise(pathFromRoot)) {
 			final OntNode parent = first(pair);
 			final OntNode child = last(pair);
 			
 			parentsToChildren.put(parent, child);
 		}
 	}
 
 	private TreeItem dummyItem() {
 		return new TreeItem("");
 	}
 	
 	private Tree makeTree(final List<OntTreeItem> treeItems) {
 		final OntTreeItem rootItem = first(treeItems);
 		
 		final Tree tree = new Tree();
 		
 		addOpenHandler(tree);
 		
 		tree.addItem(rootItem);
 		
 		makeOpenable(rootItem, true, true);
 
 		return tree;
 	}
 
 	List<OntTreeItem> makeTreeItems(final List<OntNode> pathFromRoot) {
 		Log.debug("Making ont tree: path from root: " + pathFromRoot);
 
 		if(pathFromRoot.isEmpty() || pathOnlyContainsRoot(pathFromRoot)) {
 			return Arrays.asList(new OntTreeItem(new OntNode(shrineRoot, false)));
 		}
 		
 		final List<OntTreeItem> treeItems = Util.makeArrayList();
 		
 		for(final OntNode current : pathFromRoot) {
 			final OntTreeItem currentItem = new OntTreeItem(current);
 			
 			currentItem.setState(true, true);
 			
 			treeItems.add(currentItem);
 		}
 		
 		for(final List<OntTreeItem> pair : Util.pairWise(treeItems)) {
 			final OntTreeItem parent = first(pair);
 			final OntTreeItem child = last(pair);
			
 			parent.addItem(child);
 		}
 		
 		return treeItems;
 	}
 
 	boolean pathOnlyContainsRoot(final List<OntNode> pathFromRoot) {
 		return pathFromRoot.size() == 1 && first(pathFromRoot).getValue().equals(shrineRoot.getPath());
 	}
 	
 	void makeOpenable(final OntTreeItem item, final boolean open, final boolean fireEvents) {
 		item.setState(false, false);
 		
 		item.addItem(dummyItem());
 	
 		item.setState(open, fireEvents);
 	}
 
 	static boolean isSpuriousMetaDataTerm(final OntNode childNode) {
 		return childNode.getValue().contains("SHRINE_With_Syns_08.30.10");
 	}
 
 	boolean isEdgeOnPathFromRoot(final OntNode openedParent, final OntNode child) {
 		return child.equals(parentsToChildren.get(openedParent));
 	}
 	
 	boolean isVertexOnPathFromRoot(final OntTreeItem item) {
 		return parentsToChildren.containsKey(item.ontNode);
 	}
 	
 	void select(final OntTreeItem childItem) {
 		childItem.setSelected(true);
 
 		childItem.getOntTreeNode().select();
 		
 		eventBus.fireEvent(new VerticalScrollRequestEvent(childItem.getWidget()));
 	}
 	
 	void addOpenHandler(final Tree tree) {
 		tree.addOpenHandler(new OntologyTreeItemOpenHandler());
 	}
 	
 	private final class OntologyTreeItemOpenHandler implements OpenHandler<TreeItem> {
 		@Override
 		public void onOpen(final OpenEvent<TreeItem> event) {
 			final OntTreeItem itemToBeOpened = (OntTreeItem)event.getTarget();
 			
 			Log.debug("Opening " + itemToBeOpened.ontNode.getValue());
 			
 			//If we haven't been opened (only have the dummy item)
 			if(shouldLoadChildren(itemToBeOpened)) {
 				itemToBeOpened.removeItems();
 				
 				ontologyService.getChildrenFor(itemToBeOpened.ontNode.getValue(), new OntologyChildrenReceivedCallback(itemToBeOpened));
 			}
 		}
 
 		boolean shouldLoadChildren(final OntTreeItem item) {
 			return hasOnlyDummyChild(item) || isVertexOnPathFromRoot(item);
 		}
 
 		boolean hasOnlyDummyChild(final OntTreeItem item) {
 			return hasOnlyOneChild(item) && hasDummyChild(item);
 		}
 
 		boolean hasOnlyOneChild(final OntTreeItem item) {
 			return item.getChildCount() == 1;
 		}
 
 		boolean hasDummyChild(final OntTreeItem item) {
 			return item.getChild(0).getText().equals("");
 		}
 	}
 
 	private final class OntologyChildrenReceivedCallback implements MethodCallback<List<OntNode>> {
 		private final OntTreeItem itemToBeOpened;
 
 		private OntologyChildrenReceivedCallback(OntTreeItem itemToBeOpened) {
 			this.itemToBeOpened = itemToBeOpened;
 		}
 
 		@Override
 		public void onSuccess(final Method method, final List<OntNode> childNodes) {
 			for(final OntNode childNode : childNodes) {
 				//TODO: TOTAL HACK: skip spurious metadata
 				if(!isSpuriousMetaDataTerm(childNode)) {
 					final OntTreeItem childItem = new OntTreeItem(childNode);
 					
 					//If this node isn't a leaf, give it a dummy child so it's openable
 					if(!childNode.isLeaf()) {
 						childItem.addItem(dummyItem());
 					}
 					
 					//NB: Child item must be added to something before it can be opened
 					itemToBeOpened.addItem(childItem);
 					
 					//In the child is on the path from the root to the term to browse to, open the term 
 					if(isEdgeOnPathFromRoot(itemToBeOpened.ontNode, childNode)) {
 						childItem.setState(true, true);
 					}
 					
 					if(childNode.equals(ontNodeToBrowseTo)) {
 						select(childItem);
 					}
 				}
 			}
 		}
 
 		@Override
 		public void onFailure(final Method method, final Throwable caught) {
 			Log.error("Couldn't get children of term: " + itemToBeOpened.ontNode.getValue(), caught);
 		}
 	}
 	
 	final class OntTreeItem extends TreeItem {
 
 		final OntNode ontNode;
 		
 		OntTreeItem(final OntNode ontNode) {
 			super(new OntTreeNode(eventBus, controllers, ontNode));
 			
 			this.ontNode = ontNode;
 		}
 		
 		public OntTreeNode getOntTreeNode() {
 			return (OntTreeNode)getWidget();
 		}
 	}
 }

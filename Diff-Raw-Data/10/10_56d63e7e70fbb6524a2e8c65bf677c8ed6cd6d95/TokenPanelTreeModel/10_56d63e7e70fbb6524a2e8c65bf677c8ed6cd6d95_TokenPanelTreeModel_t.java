 package net.rptools.maptool.client.ui.tokenpanel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.swing.JTree;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 
 public class TokenPanelTreeModel implements TreeModel, ModelChangeListener {
 
     public enum View {
 		VISIBLE("Visible", Zone.Layer.TOKEN, true),
     	PLAYERS("Players", Zone.Layer.TOKEN, false),
 		GROUPS("Groups", Zone.Layer.TOKEN, false),
 		STAMPS("Stamps", Zone.Layer.STAMP, true),
 		BACKGROUND("Background", Zone.Layer.BACKGROUND, true),
 		CLIPBOARD("Clipboard", Zone.Layer.TOKEN, false);
 
 		String displayName;
 		boolean required;
 		Zone.Layer layer;
 
 		private View(String displayName, Zone.Layer layer, boolean required) {
 			this.displayName = displayName;
 			this.required = required;
 			this.layer = layer;
 		}
 		public String getDisplayName() {
 			return displayName;
 		}
 		public Zone.Layer getLayer() {
 			return layer;
 		}
 	}
 
     private List<TokenFilter> filterList = new ArrayList<TokenFilter>();
     
 	private String root = "Views";
 	
 	private Zone zone;
 	
     private JTree tree;
     
     public TokenPanelTreeModel(JTree tree) {
     	this.tree = tree;
 		update();
 
 		// It would be useful to have this list be static, but it's really not that big of a memory footprint
 		// TODO: refactor to more tightly couple the View enum and the corresponding filter
     	filterList.add(new VisibleTokenFilter());
     	filterList.add(new PlayerTokenFilter());
     	filterList.add(new StampFilter());
     	filterList.add(new BackgroundFilter());
     }
 	
     private List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();
 
 	private Map<View, List<Token>> viewMap = new HashMap<View, List<Token>>();
 
 	private List<View> currentViewList = new ArrayList<View>();
 
 	public Object getRoot() {
 		return root;
 	}
 
 	public void setZone(Zone zone) {
 		if (zone != null) {
 			zone.removeModelChangeListener(this);
 		}
 		this.zone = zone;
 		
 		if (zone != null) {
 			zone.addModelChangeListener(this);
 		}
 	}
 	
 	public Object getChild(Object parent, int index) {
 
 		if (parent == root) {
 			return currentViewList.get(index);
 		}
 		
 		if (parent instanceof View) {
 			return getViewList((View) parent).get(index);
 		}
 		
 		return null;
 	}
 
 	public int getChildCount(Object parent) {
 
 		if (parent == root) {
 			return currentViewList.size();
 		}
 		
 		if (parent instanceof View) {
 			return getViewList((View)parent).size();
 		}
 		
 		return 0;
 	}
 	
 	private List<Token> getViewList(View view) {
 		List<Token> list = viewMap.get(view);
 		if (list == null) {
 			return Collections.emptyList();
 		}
 		return list;
 	}
 
 	public boolean isLeaf(Object node) {
 		return node instanceof Token;
 	}
 
 	public void valueForPathChanged(TreePath path, Object newValue) {
 		// Nothing to do
 	}
 
 	public int getIndexOfChild(Object parent, Object child) {
 		
 		if (parent == root) {
 			return currentViewList.indexOf(child);
 		}
 		
 		if (parent instanceof View) {
 			getViewList((View) parent).indexOf(child);
 		}
 		
 		return -1;
 	}
 
 	public void addTreeModelListener(TreeModelListener l) {
 		listenerList.add(l);
 	}
 
 	public void removeTreeModelListener(TreeModelListener l) {
 		listenerList.remove(l);
 	}
 	
 	public void update() {
 		
 		currentViewList.clear();
 		viewMap.clear();
 		
         if (zone == null) {
             return;
         }
 
         // Plan to show all of the views in order to keep the 
         // order
         for (TokenFilter filter : filterList) {
         	currentViewList.add(filter.view);
         }
         
         // Add in the appropriate views
         for (Token token : zone.getAllTokens()) {
         	for (TokenFilter filter : filterList) {
         		filter.filter(token);
         	}
         }
 
         // Clear out any view without any tokens
         for (ListIterator<View> viewIter = currentViewList.listIterator(); viewIter.hasNext();) {
         	View view = viewIter.next();
         	
         	if (viewMap.get(view) == null || viewMap.get(view).size() == 0) {
         		viewIter.remove();
         	}
         }
         
         // Sort
         for (List<Token> tokenList : viewMap.values()) {
         	Collections.sort(tokenList, Token.NAME_COMPARATOR);
         }
 
         // Keep the expanded branches consistent
 		Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(root));
         fireStructureChangedEvent(new TreeModelEvent(this, new Object[]{getRoot()}, 
                 new int[] { currentViewList.size() - 1 }, new Object[] {View.VISIBLE}));
         while (expandedPaths != null && expandedPaths.hasMoreElements()) {
         	tree.expandPath(expandedPaths.nextElement());
         }
 		
 	}
 
     private void fireStructureChangedEvent(TreeModelEvent e) {
         TreeModelListener[] listeners = listenerList.toArray(new TreeModelListener[listenerList.size()]);
         for (TreeModelListener listener : listeners) {
           listener.treeStructureChanged(e);
         }
     }
       
     private void fireNodesInsertedEvent(TreeModelEvent e) {
         TreeModelListener[] listeners = listenerList.toArray(new TreeModelListener[listenerList.size()]);
         for (TreeModelListener listener : listeners) {
           listener.treeNodesInserted(e);
         }
     }
 
     private abstract class TokenFilter {
     	
     	private View view;
     	
     	public TokenFilter(View view) {
     		this.view = view;
     	}
     	
     	private void filter(Token token) {
     		
     		if (accept(token)) {
     			List<Token> tokenList = viewMap.get(view);
     			if (tokenList == null) {
     				tokenList = new ArrayList<Token>();
     				viewMap.put(view, tokenList);
     			}
     			
     			tokenList.add(token);
     		}
     	}
     	
     	protected abstract boolean accept(Token token);
     }
     
     private class PlayerTokenFilter extends TokenFilter {
     	
     	public PlayerTokenFilter() {
     		super(View.PLAYERS);
     	}
     	
     	@Override
     	protected boolean accept(Token token) {
     		if (!MapTool.getServerPolicy().useStrictTokenManagement()) {
     			return false;
     		}
 
     		// TODO: Simplify these cases
         	if (token.isStamp() || (!token.hasOwners() && !token.isOwnedByAll())) {
         		return false;
         	}

        	return token.hasOwners();
     	}
     }
     
     private class StampFilter extends TokenFilter {
     	
     	public StampFilter() {
     		super(View.STAMPS);
     	}
     	
     	@Override
     	protected boolean accept(Token token) {
     		return MapTool.getPlayer().isGM() && token.isStamp();
     	}
     }
     
     private class BackgroundFilter extends TokenFilter {
     	
     	public BackgroundFilter() {
     		super(View.BACKGROUND);
     	}
     	
     	@Override
     	protected boolean accept(Token token) {
     		return MapTool.getPlayer().isGM() && token.isBackground();
     	}
     }
     
     private class VisibleTokenFilter extends TokenFilter {
     	
     	public VisibleTokenFilter() {
     		super(View.VISIBLE);
     	}
     	
     	@Override
     	protected boolean accept(Token token) {
     		
     		if (token.isStamp() || token.isBackground()) {
     			return false;
     		}
     		
        	return AppUtil.playerCanSee(zone, token);
     	}
     }
     
     ////
     // MODEL CHANGE LISTENER
     public void modelChanged(ModelChangeEvent event) {
 //    	if (event.getModel() instanceof Token) {
 //    		Token token = (Token) event.getModel();
 //    		token.addModel
 //    	}
     	update();
     }
 }

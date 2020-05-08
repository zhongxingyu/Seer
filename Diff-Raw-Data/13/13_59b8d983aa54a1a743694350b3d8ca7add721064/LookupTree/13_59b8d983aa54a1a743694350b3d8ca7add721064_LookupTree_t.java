 /* * GWT-Ext Widget Library * Copyright(c) 2007-2008, GWT-Ext. * licensing@gwt-ext.com * 
  * * http://www.gwt-ext.com/license */
 
 package org.wyona.yanel.navigation.gwt.lookuptree.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.i18n.client.Dictionary;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.gwtext.client.core.Connection;
 import com.gwtext.client.core.EventObject;
 import com.gwtext.client.data.Node;
 import com.gwtext.client.data.NodeTraversalCallback;
 import com.gwtext.client.widgets.Panel;
 import com.gwtext.client.widgets.Resizable;
 import com.gwtext.client.widgets.ResizableConfig;
 import com.gwtext.client.widgets.event.ResizableListenerAdapter;
 import com.gwtext.client.widgets.form.TextField;
 import com.gwtext.client.widgets.menu.BaseItem;
 import com.gwtext.client.widgets.menu.Item;
 import com.gwtext.client.widgets.menu.Menu;
 import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;
 import com.gwtext.client.widgets.tree.AsyncTreeNode;
 import com.gwtext.client.widgets.tree.TreeEditor;
 import com.gwtext.client.widgets.tree.TreeLoader;
 import com.gwtext.client.widgets.tree.TreeNode;
 import com.gwtext.client.widgets.tree.TreePanel;
 import com.gwtext.client.widgets.tree.XMLTreeLoader;
 import com.gwtext.client.widgets.tree.event.TreePanelListenerAdapter;
 
 public class LookupTree implements EntryPoint {
     private Menu menu;
     private TreeNode ctxNode;
 //    private TreeEditor treeEditor;
 
     public void onModuleLoad() {
         String lookupPanelBorder = "false";
         String lookupPanelPadding = "15";
         String width = "190";
        String height = "400";
         String lookupRootNodeLabel = "/";
         String lookupHook = "lookupHook";
         String requestParameterType = "";
         // Get config from host/html page via json var 'lookupTreeConfig'
         try {
             Dictionary dict = Dictionary.getDictionary("lookupTreeConfig");
             lookupPanelBorder = dict.get("lookup-panel-border");
             lookupPanelPadding = dict.get("lookup-panel-padding");
             width = dict.get("lookup-treepanel-width");
             height = dict.get("lookup-treepanel-height");
             lookupRootNodeLabel = dict.get("lookup-root-node-label");
             lookupHook = dict.get("lookup-hook");
             requestParameterType = dict.get("lookup-request-paramter-type");
         } catch (java.util.MissingResourceException e) {
             // just use default values
         }
         
         Panel panel = new Panel();
        panel.setBorder(new Boolean(lookupPanelBorder).booleanValue());
        panel.setPaddings(Integer.parseInt(lookupPanelPadding));
         final TreePanel treePanel = new LookupTreePanel(lookupRootNodeLabel, requestParameterType);
         treePanel.setEnableDD(false);
         treePanel.setContainerScroll(true);
         treePanel.setAutoScroll(true);
        treePanel.setWidth(Integer.parseInt(width));
        treePanel.setHeight(Integer.parseInt(height));
         treePanel.addListener(new TreePanelListenerAdapter() {
             public void onClick(TreeNode node, EventObject e) {
                 onNodeClick(node.getId());
             }
         });
         panel.add(treePanel);
         
         ResizableConfig config = new ResizableConfig();  
         config.setHandles(Resizable.SOUTH_EAST);  
         
         final Resizable resizable = new Resizable(treePanel, config);  
         resizable.addListener(new ResizableListenerAdapter() {  
             public void onResize(Resizable self, int width, int height) {  
                 treePanel.setWidth(width);  
                 treePanel.setHeight(height);  
             }  
         }); 
         
         RootPanel.get(lookupHook).add(panel);
     }
 
     public native void onNodeClick(final String path)/*-{
         $wnd.callback(path); 
     }-*/;
     
     class LookupTreePanel extends TreePanel {
         public LookupTreePanel(String lookupRootNodeLabel, String requestParameterType) {
             final TreeLoader loader = new TreeLoader();
             String dataUrl = "?yanel.resource.viewid=json-node";
             if (requestParameterType != null && !requestParameterType.equals("")) {
                 dataUrl += "&type="+requestParameterType;
             }
             loader.setDataUrl(dataUrl);
             loader.setMethod(com.gwtext.client.core.Connection.GET);
             AsyncTreeNode root = new AsyncTreeNode(lookupRootNodeLabel, loader);
             root.setId("/");
             setRootNode(root);
         }
     }
 }

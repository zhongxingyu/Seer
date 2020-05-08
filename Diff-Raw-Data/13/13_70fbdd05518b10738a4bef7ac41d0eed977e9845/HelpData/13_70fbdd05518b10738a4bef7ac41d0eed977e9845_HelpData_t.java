 /*
  * Copyright (c) 2010 The Jackson Laboratory
  * 
  * This is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jax.drakegenetics.gwtclientapp.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.jax.drakegenetics.shareddata.client.LibraryNode;
 
 import com.extjs.gxt.ui.client.data.BaseTreeModel;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.TreePanelEvent;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author <A HREF="mailto:dave.walton@jax.org">Dave Walton</A>
  */
 public class HelpData {
 
     private Label failMessage = null;
     private Folder root = null;
     private Window displayWindow;
     private final ContentPanel helpDocumentPanel = new ContentPanel();
     
     public HelpData(Window w) {
         this.displayWindow = w;
     }
 
     public Folder getTreeModel(DrakeGeneticsServiceAsync drakeGeneticsService) {
         final DrakeGeneticsServiceAsync dgs = drakeGeneticsService;
         drakeGeneticsService.getHelp(new AsyncCallback<LibraryNode>() {
               public void onSuccess(LibraryNode results)
               {
             	  getTreeModelSucceeded(results, dgs);
               }
               
               public void onFailure(Throwable caught)
               {
                   getTreeModelFailed(caught);
               }
           });
                     
           return this.root;  
       }
 
     private void getTreeModelSucceeded(LibraryNode results,
             DrakeGeneticsServiceAsync drakeGeneticsService) {
 
         this.root = (Folder) parseLibraryNode(results, null,
                 drakeGeneticsService);
         TreeStore<ModelData> store = new TreeStore<ModelData>();
         store.add(this.root.getChildren(), true);
         final TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
         tree.setDisplayProperty("name");    
         tree.setWidth(175); 
         tree.setHeight(368);
         tree.addListener(Events.OnClick, new Listener<TreePanelEvent<ModelData>>() {
 
                     public void handleEvent(TreePanelEvent<ModelData> be) {
                         ModelData item = be.getItem();
                         String url = (String) item.get("url");
                         if (url != null && !url.equals("")) {
                            if(url.startsWith("/"))
                            {
                                url = url.substring(1);
                            }
                             helpDocumentPanel.setUrl(GWT.getHostPageBaseURL() + url);
                         }
                     }
                 });
         HorizontalPanel displayPanel = new HorizontalPanel();
         displayPanel.setLayout(new FitLayout());
         
         ContentPanel treePanel = new ContentPanel();
         treePanel.setHeaderVisible(false);
         treePanel.add(tree);
         
         displayPanel.add(treePanel);
         
         helpDocumentPanel.setHeaderVisible(false);
         helpDocumentPanel.setWidth(410);
         helpDocumentPanel.setHeight(370);
         helpDocumentPanel.setUrl(GWT.getHostPageBaseURL() + "Help/index.html");
 
         displayPanel.add(helpDocumentPanel);
         displayWindow.add(displayPanel);
         displayWindow.show();
         GWT.log("Window size = " + displayWindow.getSize().toString());
         GWT.log("Showing Display Window");
 
 
     }
 
     private void getTreeModelFailed(Throwable caught) {
         caught.printStackTrace();
         this.failMessage = new Label(caught.getMessage());
         this.displayWindow.add(failMessage);
         this.displayWindow.show();
     }
 
     /**
      * Method used to transfer the nodes of a LibrayNode based tree to a
      * BaseTreeModel based tree of Folder and Document objects for use in a GXT
      * display
      * 
      * @param node
      *            A LibraryNode object that was returned from the server
      * @return The equivalent BaseTreeModel node (Folder or Document) for
      *         display in a GXT Widget.
      */
     private BaseTreeModel parseLibraryNode(LibraryNode node, TreeModel parent,
             DrakeGeneticsServiceAsync drakeGeneticsService) {
         BaseTreeModel displayNode = null;
 
         if (node.isDocument()) {
 
             final Document document = new Document(node.getDisplayName(),
                     node.getFileName());
             if (parent != null) {
                 document.setParent(parent);
             }
 
             // Add code here to actually fetch the document and add it to
             // the Document object
             List<String> path = new ArrayList<String>();
             path.add(document.getDocument());
             Folder fParent = (Folder) parent;
             if (! fParent.getName().equals("/Help/")) {
                 path.add(fParent.getName());
                 Folder curParent = (Folder) parent.getParent();
                 while (curParent != null) {
                     if (!curParent.getName().equals("/Help/"))
                         path.add(curParent.getName());
                     curParent = (Folder) curParent.getParent();
                 }
             }
             Collections.reverse(path);
             GWT.log(path.toString());
 
             drakeGeneticsService.getHelpDocument(path,
                     new AsyncCallback<String>() {
                         public void onSuccess(String documentUrl) {
                             GWT.log(document.getName());
                             GWT.log(" Node URL = " + documentUrl);
                             document.setUrl(documentUrl);
                         }
 
                         public void onFailure(Throwable caught) {
                             getTreeModelFailed(caught);
                         }
                     });
             displayNode = document;
 
         } else if (node.isLeaf()) {
             displayNode = new Folder(node.getData());
             if (parent != null) {
                 displayNode.setParent(parent);
             }
 
         } else {
             displayNode = new Folder(node.getData());
             if (parent != null) {
                 displayNode.setParent(parent);
             }
             for (int i = 0; i < node.getChildCount(); i++) {
                 displayNode.add(parseLibraryNode(
                         (LibraryNode) node.getChild(i), displayNode,
                         drakeGeneticsService));
             }
         }
 
         return displayNode;
     }
 
     public Label getFailMessage() {
         return this.failMessage;
     }
 }

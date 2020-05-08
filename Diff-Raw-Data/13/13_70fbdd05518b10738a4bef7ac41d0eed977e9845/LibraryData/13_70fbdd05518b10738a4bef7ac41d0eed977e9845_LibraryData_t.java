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
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author <A HREF="mailto:dave.walton@jax.org">Dave Walton</A>
  */
 public class LibraryData {
 
     private Label failMessage = null;
     private Folder root = null;
     private HorizontalPanel libraryPanel = new HorizontalPanel();
     private final ContentPanel libraryDocumentPanel = new ContentPanel();
     private ContentPanel treePanel = new ContentPanel();
     private TreeStore<ModelData> store = new TreeStore<ModelData>();
     private TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
 
     public LibraryData(HorizontalPanel lp, 
             DrakeGeneticsServiceAsync drakeGeneticsService) {
         this.libraryPanel = lp;
 
         final DrakeGeneticsServiceAsync dgs = drakeGeneticsService;
         drakeGeneticsService.getLibrary(new AsyncCallback<LibraryNode>() {
             public void onSuccess(LibraryNode results) {
                 getTreeModelSucceeded(results, dgs);
             }
 
             public void onFailure(Throwable caught) {
                 getTreeModelFailed(caught);
             }
         });
 
         treePanel.setHeaderVisible(true);
         treePanel.setLayout(new FitLayout());
         treePanel.setHeading("Publications");
 
         tree.setDisplayProperty("name");
         tree.setWidth(200);
         //tree.setHeight(450);
         tree.setHeight(670);
         tree.addListener(Events.OnClick,
                 new Listener<TreePanelEvent<ModelData>>() {
 
                     public void handleEvent(TreePanelEvent<ModelData> be) {
                         ModelData item = be.getItem();
                         String url = (String) item.get("url");
                         if (url != null && !url.equals("")) {
                            if(url.startsWith("/"))
                            {
                                url = url.substring(1);
                            }
                             libraryDocumentPanel.setUrl(GWT.getHostPageBaseURL() + url);
                         }
                     }
                 });
 
         treePanel.add(tree);
 
         libraryPanel.add(treePanel);
 
         libraryDocumentPanel.setHeaderVisible(false);
         libraryDocumentPanel.setWidth(494);
         //libraryDocumentPanel.setHeight(451);
         libraryDocumentPanel.setHeight(671);
         libraryDocumentPanel.setUrl(GWT.getHostPageBaseURL() + "Library/index.html");
         libraryPanel.add(libraryDocumentPanel);
 
     }
 
     private void getTreeModelSucceeded(LibraryNode results,
             DrakeGeneticsServiceAsync drakeGeneticsService) {
 
         this.root = (Folder) parseLibraryNode(results, null,
                 drakeGeneticsService);
         store.add(this.root.getChildren(), true);
         //tree.show();
     }
 
     private void getTreeModelFailed(Throwable caught) {
         caught.printStackTrace();
         this.failMessage = new Label(caught.getMessage());
         this.libraryPanel.add(failMessage);
         //this.libraryPanel.show();
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
 
             GWT.log(node.getDisplayName() + "--" + node.getFileName());
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
             if (! fParent.getName().equals("/Library/")) {
                 path.add(fParent.getName());
                 Folder curParent = (Folder) parent.getParent();
                 while (curParent != null) {
                     if (!curParent.getName().equals("/Library/"))
                         path.add(curParent.getName());
                     curParent = (Folder) curParent.getParent();
                 }
             }
             Collections.reverse(path);
             GWT.log(path.toString());
             
             drakeGeneticsService.getPublication(path,
                     new AsyncCallback<String>() {
                         public void onSuccess(String documentUrl) {
                             GWT.log(document.getName());
                             GWT.log(" Node URL = " + documentUrl);
                             document.setUrl(documentUrl);
                             tree.repaint();
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

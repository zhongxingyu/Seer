 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */   
 package org.netbeans.modules.javafx.fxd.composer.navigator;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.WeakHashMap;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.text.Document;
 import javax.swing.tree.DefaultTreeSelectionModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.editor.structure.api.DocumentModel;
 import org.netbeans.modules.editor.structure.api.DocumentModelException;
 import org.netbeans.modules.editor.structure.api.DocumentModelListener;
 import org.netbeans.modules.javafx.fxd.composer.model.FXDElement;
 import org.netbeans.modules.javafx.fxd.composer.model.actions.SelectActionFactory;
 import org.netbeans.modules.javafx.fxd.composer.source.SourceTopComponent;
 import org.netbeans.modules.javafx.fxd.dataloader.FXDZDataObject;
 import org.openide.cookies.EditorCookie;
 import org.openide.nodes.Node.Cookie;
 import org.openide.text.DataEditorSupport;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 import org.openide.util.UserQuestionException;
 import org.openide.windows.TopComponent;
 
 /**
  * @author Marek Fukala
  * @author Pavel Benes
  */
 public class FXDNavigatorContent extends JPanel implements SelectActionFactory.SelectionListener, PropertyChangeListener {
     public static final String ATTRIBUTES_FILTER = "attrs"; //NOI18N
     public static final String ID_FILTER         = "id";    //NOI18N
     
     //private static final boolean DEBUG = false;
     private static FXDNavigatorContent s_navigatorContentInstance = null;
     
     public static synchronized FXDNavigatorContent getDefault() {
         if(s_navigatorContentInstance == null) {
             s_navigatorContentInstance = new FXDNavigatorContent();
         }
         return s_navigatorContentInstance;
     }
     
     private final JPanel                emptyPanel;    
     private final JLabel                msgLabel;    
     private final WeakHashMap<FXDZDataObject, WeakReference<NavigatorContentPanel>> uiCache = new WeakHashMap<FXDZDataObject, WeakReference<NavigatorContentPanel>>();    
     private       FXDZDataObject        peerDO = null;
     private       String                entryName = null;
     private       NavigatorContentPanel navigatorPanel = null; 
     private       boolean               blockNotification = false;
     private       boolean               editorOpened      = false;    
         
     private FXDNavigatorContent() {
         setLayout(new BorderLayout());
         //init empty panel
         setBackground(Color.WHITE);
         emptyPanel = new JPanel();
         emptyPanel.setBackground(Color.WHITE);
         emptyPanel.setLayout(new BorderLayout());
         msgLabel = new JLabel();
         emptyPanel.add(msgLabel, BorderLayout.CENTER);
     }
 
     public void selectionChanged( FXDElement [] newSelection, FXDElement [] oldSelection) {
         blockNotification = true;
         try {
             select( newSelection != null && newSelection.length > 0 ? newSelection[0] : null);
         } finally {
             blockNotification = false;
         }
     }
         
     public void navigate(FXDZDataObject d) {
         if(peerDO != null && peerDO != d) {
             //release the original document (see closeDocument() javadoc)
             closeDocument(peerDO);
         }
         
         if ( d != null) {
             EditorCookie ec = (EditorCookie) d.getEditorSupport();
             if(ec == null) {
     //            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie!?");
                 System.err.println("The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie!?");  //NOI18N
             } else {
                 try {
                     //System.err.println("[fxd navigator] navigating to DATAOBJECT " + d.hashCode());
                     //test if the document is opened in editor
                     BaseDocument bdoc = (BaseDocument)ec.openDocument();
                     //create & show UI
                     if(bdoc != null) {
                         //there is something we can navigate in
                         navigate(d, bdoc);
                         //remember the peer dataobject to be able the call EditorCookie.close() when closing navigator
                         peerDO = d;
                         entryName = peerDO.getEntryName();
                         //check if the editor for the DO has an opened pane
                         editorOpened = ec.getOpenedPanes() != null && ec.getOpenedPanes().length > 0;
                     }
 
                 }catch(UserQuestionException uqe) {
                     //do not open a question dialog when the document is just loaded into the navigator
                     showError("LBL_TooLarge");  //NOI18N
                     uqe.printStackTrace();
                 }catch(IOException e) {
                     //ErrorManager.getDefault().notify(e);
                     e.printStackTrace();
                 }
             }
         } else {
             setContent(null, null);
         }
     }
    
    /** A hacky fix for XMLSyncSupport - I need to call EditorCookie.close when the navigator
      * is deactivated and there is not view pane for the navigated document. Then a the synchronization
      * support releases a strong reference to NbEditorDocument. */
     private void closeDocument(FXDZDataObject dobj) {
         if(dobj != null) {
             EditorCookie ec = (EditorCookie) peerDO.getEditorSupport();
             if(ec != null) {
                 JEditorPane panes[] = ec.getOpenedPanes();
                 //call EC.close() if there isn't any pane and the editor was opened
                 if((panes == null || panes.length == 0)) {
                     ((EditorCookie.Observable)ec).removePropertyChangeListener(this);
                     
                     if(editorOpened) {
                         ec.close();
                         //if(DEBUG) System.out.println("document instance for dataobject " + dobj.hashCode() + " closed.");
                     }
                 }
                 editorOpened = false;
             }
         }
     }    
     
     public void navigate(final FXDZDataObject documentDO, final BaseDocument bdoc) {
         //if(DEBUG) System.out.println("[xml navigator] navigating to DOCUMENT " + bdoc.hashCode());
         //called from AWT thread
         showWaitPanel();
         
         //try to find the UI in the UIcache
         final NavigatorContentPanel cachedPanel;
         WeakReference<NavigatorContentPanel> panelWR = uiCache.get(documentDO);
         if(panelWR != null) {
             NavigatorContentPanel cp = panelWR.get();
             if(cp != null) {
                 //if(DEBUG) System.out.println("panel is cached");
                 //test if the document associated with the panel is the same we got now
                 cachedPanel = bdoc == cp.m_doc ? cp : null;
                 if(cachedPanel == null) {
                     //if(DEBUG) System.out.println("but the document is different - creating a new UI...");
                     //if(DEBUG) System.out.println("the cached document : " + cp.getDocument());
                     
                     //remove the old mapping from the cache
                     uiCache.remove(documentDO);
                 }
             } else
                 cachedPanel = null;
         } else
             cachedPanel = null;
        
         //get the model and create the new UI on background
         RequestProcessor.getDefault().post(new Runnable() {
             public void run() {
                 //get document model for the file
                 try {
                     final DocumentModel model;
                     if(cachedPanel == null)
                         model = DocumentModel.getDocumentModel(bdoc);
                     else
                         model = null; //if the panel is cached it holds a refs to the model - not need to init it again
                     
                     
                     if(cachedPanel != null || model != null) {                        
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     showWaitPanel();
                                     NavigatorContentPanel panel = null;
                                     if(cachedPanel == null) {
                                         try {
                                             //lock the model for modifications during the
                                             //navigator tree creation
                                             model.readLock();
                                             
                                             //cache the newly created panel
                                             panel = new NavigatorContentPanel(documentDO, model);
                                             //use the document dataobject as a key since the document itself is very easily discarded and hence
                                             //harly usable as a key of the WeakHashMap
                                             uiCache.put(documentDO, new WeakReference<NavigatorContentPanel>(panel));
                                             //if(DEBUG) System.out.println("[xml navigator] panel created");
                                             
                                             //start to listen to the document property changes - we need to get know when the document is being closed
                                             EditorCookie.Observable eco = documentDO.getCookie(EditorCookie.Observable.class);
                                             if(eco != null) {
                                                 eco.addPropertyChangeListener(FXDNavigatorContent.this);
                                             } else {
 //                                                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The DataObject " + documentDO.getName() + "(class=" + documentDO.getClass().getName() + ") has no EditorCookie.Observable!");
                                                 System.err.println("The DataObject " + documentDO.getName() + "(class=" + documentDO.getClass().getName() + ") has no EditorCookie.Observable!"); //NOI18N
                                             }
                                         }finally{
                                             //unlock the model
                                             model.readUnlock();
                                         }
                                     } else {
                                         panel = cachedPanel;
                                         //if(DEBUG) System.out.println("[xml navigator] panel gotten from cache");
                                     }
                                     
                                     setContent(peerDO, panel);
                                 }
                             });
                     } else {
                         //model is null => show message
                         showError( "LBL_CannotNavigate");  //NOI18N
                     }
                 }catch(DocumentModelException dme) {
                     dme.printStackTrace();
 //                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dme);
                 }
             }
         });
     }
     
     synchronized void setContent( final FXDZDataObject obj, final NavigatorContentPanel panel) {
         navigatorPanel = panel;
                 
         removeAll();
         if (panel != null) {
             add(panel, BorderLayout.CENTER);  
         }
         validate();                                
         repaint();                                
     }
     
     synchronized void select(FXDElement elem) {
 //        System.err.println( String.format("Selecting id %s in the navigator", elemId));
         if (navigatorPanel != null) {
             navigatorPanel.tree.selectNode(elem);
         }
     }
 
     public void release() {
         removeAll();
         peerDO = null;
         repaint();
     }
         
     public void showError(String messageID) {
         removeAll();
         msgLabel.setIcon(null);
         msgLabel.setForeground(Color.GRAY);
         msgLabel.setText(NbBundle.getMessage(FXDNavigatorContent.class, messageID));
         msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
         add(emptyPanel, BorderLayout.CENTER);
         revalidate();
         repaint();
     }
     
     private void showWaitPanel() {
         removeAll();
         msgLabel.setIcon(null);
         msgLabel.setForeground(Color.GRAY);
         msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
         msgLabel.setText(NbBundle.getMessage(FXDNavigatorContent.class, "LBL_Wait")); //NOI18N
         add(emptyPanel, BorderLayout.NORTH);
         repaint();
     }
 
      private class NavigatorContentPanel extends JPanel implements FiltersManager.FilterChangeListener {
         private final FXDZDataObject   m_doj;
         private final Document         m_doc;
         private final FXDNavigatorTree tree;
         private final FiltersManager   m_filters;
 
         private final DocumentModelListener m_modelListener = new DocumentModelListener() {
             public void documentElementAdded(DocumentElement de) {
                 modelChanged();
             }
             public void documentElementRemoved(DocumentElement de) {
                 modelChanged();
             }
             public void documentElementChanged(DocumentElement de) {
                 modelChanged();
             }
             public void documentElementAttributesChanged(DocumentElement de) {
                 modelChanged();
             }
             protected void modelChanged() {
                 tree.repaint();
             }
         };       
                 
         public NavigatorContentPanel(FXDZDataObject dObj, DocumentModel dm)  {
             setLayout(new BorderLayout());
             m_doj = dObj;
             m_doc = dm.getDocument();
             //create the JTree pane
             tree = new FXDNavigatorTree(dm);
             ToolTipManager.sharedInstance().registerComponent(tree);
             
             MouseListener ml = new MouseAdapter() {
                 @Override
                 public void mousePressed(MouseEvent e) {
                     int selRow = tree.getRowForLocation(e.getX(), e.getY());
                     if(selRow != -1 && e.getClickCount() == 2) {
                         TreePath         selPath = tree.getPathForLocation(e.getX(), e.getY());
                         FXDNavigatorNode tna     = (FXDNavigatorNode)selPath.getLastPathComponent();                       
                         DocumentElement  de      = tna.getDocumentElement();
                         SourceTopComponent.selectElement(m_doj, de.getStartOffset(), true);
                     }
                 }
             };
             tree.addMouseListener(ml);
             
             final TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
             selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
             tree.setSelectionModel(selectionModel);
             
             selectionModel.addTreeSelectionListener( new TreeSelectionListener() { 
                 public void valueChanged(TreeSelectionEvent e) {
                     if ( !blockNotification) {
                         TreePath        selPath = e.getPath();
                         DocumentElement de      = null;
                         
                         if ( selPath != null && !selectionModel.isSelectionEmpty()) {
                             FXDNavigatorNode tna     = (FXDNavigatorNode)selPath.getLastPathComponent();                       
                             de = tna.getDocumentElement();
                         }
                         TopComponent tc = m_doj.getMVTC();
 
                         if ( tc != null && de != null) {
                             Lookup           lkp    = tc.getLookup();                                
                             SelectionCookie  cookie = lkp.lookup(SelectionCookie.class);
                             if ( cookie != null) {
                                 cookie.updateSelection( m_doj, de, false);
                             } else {
     //                            System.err.println("Selection cookie not found.");
                             }
                         }
 //                        System.err.println("TC: " + tc);
 
                     }
                 }
             });
                                 
             JScrollPane treeView = new JScrollPane(tree);
             treeView.setBorder(BorderFactory.createEmptyBorder());
             treeView.setViewportBorder(BorderFactory.createEmptyBorder());
             add(treeView, BorderLayout.CENTER);
             
             //add popup menu mouse listener
             MouseListener pmml = new MouseAdapter() {
                 @Override
                 public void mousePressed(final MouseEvent e) {
                     if(e.getClickCount() == 1 && e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                         //show popup
                         JPopupMenu pm = new JPopupMenu();
                         
 //                        final DocumentElement de         = getElementAt(e.getX(), e.getY());
                                                 
                         JMenuItem[] items = new FilterActions(m_filters).createMenuItems();
                         //add filter actions
                         for(int i = 0; i < items.length; i++) {
                             pm.add(items[i]);
                         }
                         pm.pack();
                         pm.show(tree, e.getX(), e.getY());
                     }
                 }
             };
             tree.addMouseListener(pmml);
             
             //expand all root elements which are tags
             TreeNode rootNode = (TreeNode)tree.getTreeModel().getRoot();
             for(int i = 0; i < rootNode.getChildCount(); i++) {
                 TreeNode node = rootNode.getChildAt(i);
                 if(node.getChildCount() > 0)
                     tree.expandPath(new TreePath(new TreeNode[]{rootNode, node}));
             }
                                 
             //create the TapPanel
             TapPanel filtersPanel = new TapPanel();
             JLabel filtersLbl = new JLabel(NbBundle.getMessage(FXDNavigatorContent.class, "LBL_Filter")); //NOI18N
             filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
             filtersPanel.add(filtersLbl);
             filtersPanel.setOrientation(TapPanel.DOWN);
             // tooltip
             KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
             String keyText = org.openide.util.Utilities.keyToString(toggleKey);
             filtersPanel.setToolTipText(NbBundle.getMessage(FXDNavigatorContent.class, "TIP_TapPanel", keyText)); //NOI18N
             
             //create FiltersManager
             m_filters = createFilters();
             //listen to filters changes
             m_filters.hookChangeListener(this);
             
             filtersPanel.add(m_filters.getComponent());
             
             add(filtersPanel, BorderLayout.SOUTH);
             dm.addDocumentModelListener(m_modelListener);
         }        
         
         @SuppressWarnings("unchecked")
         protected Cookie getCookie(Class clazz) {
             Cookie       cookie = null;
             TopComponent tc     = m_doj.getMVTC();
 
             if ( tc != null) {
                 cookie = (Cookie) tc.getLookup().lookup(clazz);
             }
             return cookie;
         }
 
         protected DocumentElement getElementAt( int x, int y) {
             DocumentElement de     = null;
             int             selRow = tree.getRowForLocation(x, y);
             
             if(selRow != -1) {
                 TreePath         selPath = tree.getPathForLocation(x, y);
                 FXDNavigatorNode tna     = (FXDNavigatorNode)selPath.getLastPathComponent();                       
                 de = tna.getDocumentElement();
             }
             return de;
         }
         
         /** Creates filter descriptions and filters itself */
         private FiltersManager createFilters() {
             FiltersDescription desc = new FiltersDescription();
             
             desc.addFilter(ATTRIBUTES_FILTER,
                     NbBundle.getMessage(FXDNavigatorContent.class, "LBL_ShowAttributes"),     //NOI18N
                     NbBundle.getMessage(FXDNavigatorContent.class, "LBL_ShowAttributesTip"),     //NOI18N
                     FXDNavigatorTree.showAttributes,
                     new ImageIcon(org.openide.util.ImageUtilities.loadImage("org/netbeans/modules/javafx/fxd/composer/resources/showAttribs.png")), //NOI18N
                     null
             );
             desc.addFilter(ID_FILTER,
                     NbBundle.getMessage(FXDNavigatorContent.class, "LBL_ShowId"),     //NOI18N
                     NbBundle.getMessage(FXDNavigatorContent.class, "LBL_ShowIdTip"),     //NOI18N
                     FXDNavigatorTree.showIdOnly,
                     new ImageIcon(org.openide.util.ImageUtilities.loadImage("org/netbeans/modules/javafx/fxd/composer/resources/filterSignificant.png")), //NOI18N
                     null
             );
             
             return FiltersDescription.createManager(desc);
         }
 
         public void filterStateChanged(ChangeEvent e) {
             boolean filterChanged         = false;   
             boolean attrVisibilityChanged = false;
             boolean selected;
             
             selected = m_filters.isSelected(ATTRIBUTES_FILTER);
             if ( selected != FXDNavigatorTree.showAttributes) {
                 FXDNavigatorTree.showAttributes = selected;
                 attrVisibilityChanged = true;
             }
 
             if ( (selected=m_filters.isSelected(ID_FILTER)) != FXDNavigatorTree.showIdOnly) {
                 filterChanged = true;
                 FXDNavigatorTree.showIdOnly = selected;
             }
                         
             if (filterChanged) {
                 tree.filterChanged();
             } else {
                 if (attrVisibilityChanged) {
                     tree.validate();
                     tree.repaint();
                 }
             }
         }
      }
 
     public void propertyChange(PropertyChangeEvent evt) {
         if( EditorCookie.Observable.PROP_DOCUMENT.equals( evt.getPropertyName())) {
             if(evt.getNewValue() == null) {
                 final FXDZDataObject dobj =(FXDZDataObject) ((DataEditorSupport)evt.getSource()).getDataObject();
                 if(dobj != null) {
                     editorOpened = false;
                     //document is being closed
                     //if(DEBUG) System.out.println("document has been closed for DO: " + dobj.hashCode());
                     //System.err.println("document has been closed for DO: " + dobj.hashCode());
                     
                     
                     //remove the property change listener from the DataObject's EditorSupport
                     EditorCookie ec = (EditorCookie) dobj.getEditorSupport();
                     if(ec != null)
                         ((EditorCookie.Observable)ec).removePropertyChangeListener(this);
                     
                     //and navigate the document again (must be called asynchronously
                     //otherwise the ClonableEditorSupport locks itself (new call to CES from CES.propertyChange))
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             if(dobj.isValid()) navigate(dobj);
                         }
                     });
                 }
             } else {
                 //a new pane created
                 editorOpened = true;
             }
         }
     }
 }
 

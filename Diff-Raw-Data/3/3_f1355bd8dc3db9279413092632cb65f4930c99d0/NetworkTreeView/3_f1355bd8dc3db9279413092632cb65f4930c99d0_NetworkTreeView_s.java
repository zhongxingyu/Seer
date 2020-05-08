 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe.views.network.view;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.internal.command.navigation.SetViewportCenterCommand;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.awe.views.view.provider.NetworkTreeContentProvider;
 import org.amanzi.awe.awe.views.view.provider.NetworkTreeLabelProvider;
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.views.neighbours.views.NeighboursView;
 import org.amanzi.awe.views.neighbours.views.TransmissionView;
 import org.amanzi.awe.views.network.NetworkTreePlugin;
 import org.amanzi.awe.views.network.property.NetworkPropertySheetPage;
 import org.amanzi.awe.views.network.proxy.NeoNode;
 import org.amanzi.awe.views.network.proxy.Root;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.service.listener.NeoServiceProviderEventAdapter;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.geotools.referencing.CRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.neoclipse.Activator;
 import org.neo4j.neoclipse.view.NeoGraphViewPart;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 /**
  * This View contains a tree of objects found in the database. The tree is built based on the
  * existence of the NetworkRelationshipTypes.CHILD relation, and the set of Root nodes defined by
  * the Root.java class.
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 
 public class NetworkTreeView extends ViewPart {
 
     /*
      * ID of this View
      */
     public static final String NETWORK_TREE_VIEW_ID = "org.amanzi.awe.views.network.views.NetworkTreeView";
 
     private static final String RENAME_MSG = "Enter new Name";
 
     /*
      * TreeViewer for database Nodes
      */
     protected TreeViewer viewer;
 
     /*
      * NeoService provider
      */
     private NeoServiceProvider neoServiceProvider;
 
     /*
      * PropertySheetPage for Properties of Nodes
      */
     private IPropertySheetPage propertySheetPage;
 
     /*
      * Listener for Neo-Database Events
      */
     private NeoServiceEventListener neoEventListener;
 
     private Text tSearch;
     private Iterator<Node> searchIterator = new ArrayList<Node>().iterator();
     private boolean autoZoom = true;
 
     /**
      * The constructor.
      */
     public NetworkTreeView() {
     }
 
     /**
      * This is a callback that will allow us to create the viewer and initialize it.
      */
     @Override
     public void createPartControl(Composite parent) {
 
         tSearch = new Text(parent, SWT.BORDER);
         viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
         viewer.addSelectionChangedListener(new NetworkSelectionListener());
         viewer.addDoubleClickListener(new IDoubleClickListener() {
             @Override
             public void doubleClick(DoubleClickEvent event) {
                 StructuredSelection sel = (StructuredSelection)event.getSelection();
                 Object s = sel.getFirstElement();
                 if ((s != null) && (s instanceof NeoNode)) {
                     NeoNode node = (NeoNode)s;
                     if (viewer != event.getViewer()) {
                         return;
                     }
                     showSelection(node);
                     showSelectionOnMap(node);
                 }
             }
         });
 
         neoServiceProvider = NeoServiceProvider.getProvider();
         neoEventListener = new NeoServiceEventListener();
         neoServiceProvider.addServiceProviderListener(neoEventListener);
         Transaction tx = neoServiceProvider.getService().beginTx();
         try {
             setProviders(neoServiceProvider);
             setEditable(false);
             viewer.setInput(getSite());
             getSite().setSelectionProvider(viewer);
 
             hookContextMenu();
         } finally {
             tx.finish();
         }
         tSearch.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 searchIterator = new ArrayList<Node>().iterator();
                 // findAndSelectNode();
             }
         });
         tSearch.addKeyListener(new KeyListener() {
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                     findAndSelectNode();
                 }
             }
         });
         setLayout(parent);
         Activator.getDefault().getPluginPreferences().addPropertyChangeListener(new PreferenceChangeHandler());
     }
 
     /**
      * Searches for a node based on matching text to the name field. The resulting node is show in
      * the tree. Multiple searches on the same text will step through multiple results, and loop
      * back to the first.
      */
     protected void findAndSelectNode() {
         tSearch.setEditable(false);
         try {
             String text = tSearch.getText();
             text = text == null ? "" : text.toLowerCase().trim();
             if (text.isEmpty()) {
                 return;
             }
             Root rootTree = (Root)((ITreeContentProvider)viewer.getContentProvider()).getElements(0)[0];
             if (searchIterator == null || !searchIterator.hasNext()) {
                 // Do completely new search on changed text
                 searchIterator = createSearchTraverser(rootTree.getNode(), text);
                 viewer.collapseAll();
             }
             if (!searchIterator.hasNext()) {
                 return;
             }
             Node lastNode = searchIterator.next();
             selectNode(lastNode);
             // } catch (Exception e) {
             // e.printStackTrace();
             // throw (RuntimeException)new RuntimeException().initCause(e);
         } finally {
             tSearch.setEditable(true);
         }
     }
 
     /**
      * Select node
      * 
      * @param node - node to select
      */
     public void selectNode(Node node) {
         viewer.reveal(new NeoNode(node));
         viewer.setSelection(new StructuredSelection(new Object[] {new NeoNode(node)}));
     }
 
     /**
      * Create iterator of nodes with name, that contains necessary text
      * 
      * @param node - start node
      * @param text text to find
      * @return search iterator
      */
     private Iterator<Node> createSearchTraverser(Node node, final String text) {
         Traverser traverse = node.traverse(Order.DEPTH_FIRST, getSearchStopEvaluator(), new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 String type = NeoUtils.getNodeType(currentPos.currentNode(), "");
                 return !(type.equals(NodeTypes.AWE_PROJECT.getId()) || type.equals(NodeTypes.GIS.getId()))
                         && NeoUtils.getFormatedNodeName(currentPos.currentNode(),"").toLowerCase().contains(text);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
         // TODO needs sort by name
         return traverse.iterator();
     }
 
     /**
      * gets search stop Evaluator
      * 
      * @return search stop Evaluator
      */
     protected StopEvaluator getSearchStopEvaluator() {
         return new StopEvaluator() {
 
             @Override
             public boolean isStopNode(TraversalPosition currentPos) {
                 String nodeType = NeoUtils.getNodeType(currentPos.currentNode(), "");
                 boolean result = (nodeType.equals(NodeTypes.FILE.getId()) || nodeType.equals(NodeTypes.DATASET.getId()));
                 return result;
             }
         };
     }
 
     /**
      * @param parent
      */
     private void setLayout(Composite parent) {
         FormLayout layout = new FormLayout();
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         layout.marginWidth = 0;
         layout.spacing = 0;
         parent.setLayout(layout);
         FormData formData = new FormData();
         formData.top = new FormAttachment(0, 5);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(100, -5);
         tSearch.setLayoutData(formData);
 
         formData = new FormData();
         formData.top = new FormAttachment(tSearch, 5);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(100, -5);
         formData.bottom = new FormAttachment(100, -5);
         viewer.getTree().setLayoutData(formData);
 
     }
 
     /**
      * Set Label and Content providers for TreeView
      * 
      * @param neoServiceProvider
      */
 
     protected void setProviders(NeoServiceProvider neoServiceProvider) {
         viewer.setContentProvider(new NetworkTreeContentProvider(neoServiceProvider));
         viewer.setLabelProvider(new NetworkTreeLabelProvider(viewer));
     }
 
     /**
      * sets edit mode for tree
      */
     protected void setEditable(boolean editable) {
         if (editable) {
             TextCellEditor textCellEditor = new TextCellEditor(viewer.getTree());
             viewer
                     .setCellEditors(new CellEditor[] {textCellEditor, textCellEditor, textCellEditor, textCellEditor,
                             textCellEditor});
             viewer.setColumnProperties(new String[] {"name"});
             ICellModifier modifier = new ICellModifier() {
 
                 @Override
                 public void modify(Object element, String property, Object value) {
                     String valueStr = value == null ? "" : value.toString().trim();
                     if (valueStr.isEmpty()) {
                         return;
                     }
                     NeoNode neoNode = element instanceof TreeItem ? (NeoNode)((TreeItem)element).getData() : (NeoNode)element;
                     neoNode.setName(valueStr);
                     System.out.println(element);
                 }
 
                 @Override
                 public Object getValue(Object element, String property) {
                     System.out.println(element);
                     return ((NeoNode)element).getNode().getProperty(INeoConstants.PROPERTY_NAME_NAME);
                 }
 
                 @Override
                 public boolean canModify(Object element, String property) {
                     System.out.println(element);
                     return !(element instanceof Root) && ((NeoNode)element).getNode().hasProperty(INeoConstants.PROPERTY_NAME_NAME);
                 }
             };
             viewer.setCellModifier(modifier);
         } else {
             viewer.setCellEditors(null);
             viewer.setCellModifier(null);
         }
     }
 
     /**
      * Creates a popup menu
      */
 
     private void hookContextMenu() {
         MenuManager menuMgr = new MenuManager("#PopupMenu");
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager manager) {
                 NetworkTreeView.this.fillContextMenu(manager);
             }
         });
         Menu menu = menuMgr.createContextMenu(viewer.getControl());
         viewer.getControl().setMenu(menu);
         getSite().registerContextMenu(menuMgr, viewer);
     }
 
     /**
      * Creates items for popup menu
      * 
      * @param manager
      */
 
     private void fillContextMenu(IMenuManager manager) {
         // TODO remove all disabled part of menu??
         manager.add(new Action("Properties") {
             @Override
             public void run() {
                 try {
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IPageLayout.ID_PROP_SHEET);
                 } catch (PartInitException e) {
                     NetworkTreePlugin.error(null, e);
                 }
             }
         });
         RenameAction reanmeAction = new RenameAction((IStructuredSelection)viewer.getSelection());
         manager.add(reanmeAction);
         RevertNameAction revertAction = new RevertNameAction((IStructuredSelection)viewer.getSelection());
         if (revertAction.isEnabled()) {
             manager.add(revertAction);
         }
         manager.add(new Action("Refresh") {
             @Override
             public void run() {
                 viewer.refresh(((IStructuredSelection)viewer.getSelection()).getFirstElement());
             }
         });
         manager.add(new DeltaReportAction());
         manager.add(new GenerateReportFileAction());
         manager.add(new Action("Show in database graph") {
             @Override
             public void run() {
                 IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                 showSelection((NeoNode)selection.getFirstElement());
             }
 
             @Override
             public boolean isEnabled() {
                 return ((IStructuredSelection)viewer.getSelection()).size() == 1;
             }
         });
         manager.add(new Action("Show in active map") {
             @Override
             public void run() {
                 IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                 showSelectionOnMap((NeoNode)selection.getFirstElement());
             }
 
             @Override
             public boolean isEnabled() {
                 return ((IStructuredSelection)viewer.getSelection()).size() == 1
                         && ApplicationGIS.getActiveMap() != ApplicationGIS.NO_MAP;
             }
         });
         NeighbourAction neighb = new NeighbourAction((IStructuredSelection)viewer.getSelection());
         manager.add(neighb);
         TransmissionAction transmission = new TransmissionAction((IStructuredSelection)viewer.getSelection());
         manager.add(transmission);
         manager.add(new DeleteAction((IStructuredSelection)viewer.getSelection()));
 
     }
 
     /**
      * Shows selected node on map
      * 
      * @param node selected node
      */
     protected void showSelectionOnMap(NeoNode node) {
         try {
             IMap map = ApplicationGIS.getActiveMap();
             if (map == ApplicationGIS.NO_MAP || node == null) {
                 return;
             }
             Node gis = getGisNode(node.getNode());
             if (gis == null) {
                 return;
             }
             boolean presentFlag = false;
             for (ILayer layer : map.getMapLayers()) {
                 IGeoResource resource = layer.findGeoResource(Node.class);
                 if (resource != null && gis.equals(resource.resolve(Node.class, null))) {
                     presentFlag = true;
                     break;
                 }
             }
             if (!presentFlag) {
                 return;
             }
             CoordinateReferenceSystem crs = null;
             if (!gis.getProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, "").toString().equalsIgnoreCase(("projected"))) {
                 autoZoom = false;
             }
             if (gis.hasProperty(INeoConstants.PROPERTY_CRS_NAME)) {
                 crs = CRS.decode(gis.getProperty(INeoConstants.PROPERTY_CRS_NAME).toString());
             } else if (gis.hasProperty(INeoConstants.PROPERTY_CRS_HREF_NAME)) {
                 URL crsURL = new URL(gis.getProperty(INeoConstants.PROPERTY_CRS_HREF_NAME).toString());
                 crs = CRS.decode(crsURL.getContent().toString());
             }
             double[] c = getCoords(node.getNode());
             if (c == null) {
                 return;
             }
             if (autoZoom) {
                 // TODO: Check that this works with all CRS
                 map.sendCommandASync(new net.refractions.udig.project.internal.command.navigation.SetViewportWidth(30000));
                 autoZoom = false; // only zoom first time, then rely on user to control zoom level
             }
             map.sendCommandASync(new SetViewportCenterCommand(new Coordinate(c[0], c[1]), crs));
         } catch (NoSuchAuthorityCodeException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         } catch (MalformedURLException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
 
     }
 
     /**
      * Find a parent node of the specified type, following the NEXT relations back up the chain
      * 
      * @param node subnode
      * @return parent node of specified type or null
      */
     private Node getParentNode(Node node, final String type) {
         Traverser traverse = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(type);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.INCOMING);
         return traverse.iterator().hasNext() ? traverse.iterator().next() : null;
     }
 
     /**
      * Gets GIS node of necessary subnode
      * 
      * @param node subnode
      * @return gis node or null
      */
     private Node getGisNode(Node node) {
         Traverser traverse = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(
                         NodeTypes.GIS.getId());
             }
         }, NetworkRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING);
         return traverse.iterator().hasNext() ? traverse.iterator().next() : null;
     }
 
     /**
      * shows database graph with selected node
      * 
      * @param nodeToSelect - selected node
      */
     private void showSelection(NeoNode nodeToSelect) {
         try {
             IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(NeoGraphViewPart.ID);
             NeoGraphViewPart viewGraph = (NeoGraphViewPart)view;
             viewGraph.showNode(nodeToSelect.getNode());
             final StructuredSelection selection = new StructuredSelection(new Object[] {nodeToSelect.getNode()});
             viewGraph.getViewer().setSelection(selection, true);
             showThisView();
         } catch (Exception e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
 
     }
 
     /**
      * show this view (brings it forward)
      */
     protected void showThisView() {
         try {
             PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(NETWORK_TREE_VIEW_ID);
         } catch (Exception e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
 
     }
 
     @Override
     public void dispose() {
         if (propertySheetPage != null) {
             propertySheetPage.dispose();
         }
 
         if (neoEventListener != null) {
             neoServiceProvider.removeServiceProviderListener(neoEventListener);
         }
         super.dispose();
     }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     @Override
     public void setFocus() {
         viewer.getControl().setFocus();
     }
 
     /**
      * Returns (and creates is it need) property sheet page for this View
      * 
      * @return PropertySheetPage
      */
 
     private IPropertySheetPage getPropertySheetPage() {
         if (propertySheetPage == null) {
             propertySheetPage = new NetworkPropertySheetPage();
         }
 
         return propertySheetPage;
     }
 
     /**
      * This is how the framework determines which interfaces we implement.
      */
     @Override
     @SuppressWarnings("unchecked")
     public Object getAdapter(final Class key) {
         if (key.equals(IPropertySheetPage.class)) {
             return getPropertySheetPage();
         } else {
             return super.getAdapter(key);
         }
     }
 
     /**
      * Listener handling selection events on the tree view. Selected items are added to a selection
      * collection provided to the map renderer so that highlighting can be draw on the map.
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private final class NetworkSelectionListener implements ISelectionChangedListener {
         private List<ILayer> layers = new ArrayList<ILayer>();
 
         @SuppressWarnings("unchecked")
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
             StructuredSelection stSel = (StructuredSelection)event.getSelection();
             if (!stSel.isEmpty() && tSearch.getText().isEmpty()) {
                 tSearch.setText(stSel.getFirstElement().toString());
             }
             ITreeSelection selected = (ITreeSelection)event.getSelection();
             Iterator iterator = selected.iterator();
             layers = findAllNetworkLayers();
             if (layers.isEmpty()) {
                 return;
             };
             dropLayerSelection(layers);
             while (iterator.hasNext()) {
                 NeoNode selectedNode = (NeoNode)iterator.next();
                 addNodeToSelect(selectedNode);
             }
             for (ILayer singleLayer : layers) {
                 singleLayer.refresh(null);
             }
         }
 
         /**
          * Drop selection in layers
          * 
          * @param list of layers
          */
         private void dropLayerSelection(List<ILayer> layers) {
             try {
                 for (ILayer singleLayer : layers) {
                     GeoNeo resource = singleLayer.findGeoResource(GeoNeo.class).resolve(GeoNeo.class, null);
                     resource.setSelectedNodes(null);
                 }
             } catch (IOException e) {
                 // TODO Handle IOException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
 
         /**
          * Add selected node
          * 
          * @param selectedNode - selected node
          */
         private void addNodeToSelect(NeoNode selectedNode) {
             try {
                 if (selectedNode == null) {
                     return;
                 }
                 String nodeType = selectedNode.getType();
                 if (NodeTypes.SITE.getId().equals(nodeType) || NodeTypes.SECTOR.getId().equals(nodeType)
                         || NodeTypes.CITY.getId().equals(nodeType)
                         || NodeTypes.BSC.getId().equals(nodeType) || NodeTypes.DELTA_NETWORK.getId().equals(nodeType)
                         || NodeTypes.DELTA_SITE.getId().equals(nodeType) || NodeTypes.DELTA_SECTOR.getId().equals(nodeType) || NodeTypes.MISSING_SITES.getId().equals(nodeType)
                         || NodeTypes.MISSING_SECTORS.getId().equals(nodeType) || NodeTypes.MISSING_SITE.getId().equals(nodeType)
                         || NodeTypes.MISSING_SECTOR.getId().equals(nodeType) || NodeTypes.M.getId().equalsIgnoreCase(nodeType)
                         || NodeTypes.MP.getId().equalsIgnoreCase(nodeType)
                         || NodeTypes.FILE.getId().equalsIgnoreCase(nodeType)
                         || NodeTypes.DATASET.getId().equalsIgnoreCase(nodeType)) {
                     for (ILayer singleLayer : layers) {
                         GeoNeo resource = singleLayer.findGeoResource(GeoNeo.class).resolve(GeoNeo.class, null);
                         if (containsGisNode(resource, selectedNode)) {
                             resource.addNodeToSelect(selectedNode.getNode());
                         }
                     }
                 }
             } catch (IOException e) {
                 // TODO Handle IOException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
 
         /**
          * checks if node is part of GIS tree
          * 
          * @param gisNode - gis node
          * @param selectedNode - selected node
          * @return now this method always return true, because expenses for check at present are not
          *         justified
          */
         private boolean containsGisNode(GeoNeo gisNode, NeoNode selectedNode) {
             return true;
         }
 
         /**
          * find all layers, that contains network gis node
          * 
          * @return
          */
         private List<ILayer> findAllNetworkLayers() {
             List<ILayer> result = new ArrayList<ILayer>();
             for (IMap activeMap : ApplicationGIS.getOpenMaps()) {
                 for (ILayer layer : activeMap.getMapLayers()) {
                     IGeoResource resourse = layer.findGeoResource(GeoNeo.class);
                     if (resourse != null) {
                         try {
                             GeoNeo geo = resourse.resolve(GeoNeo.class, null);
                             if (geo != null /* &&geo.getGisType() == GisTypes.Network */) {
                                 result.add(layer);
                             }
                         } catch (IOException e) {
                             // TODO Handle IOException
                             throw (RuntimeException)new RuntimeException().initCause(e);
                         }
                     }
                 }
             }
             return result;
         }
     }
 
     /**
      * NeoProvider Event listener for this View
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
 
     private class NeoServiceEventListener extends NeoServiceProviderEventAdapter {
 
         @Override
         public void onNeoStop(Object source) {
             neoServiceProvider.shutdown();
         }
 
         /**
          * If some data was committed to database than we must refresh content of TreeView
          */
         @Override
         public void onNeoCommit(Object source) {
             // TODO: Only modify part of tree specific to data modified
             viewer.getControl().getDisplay().syncExec(new Runnable() {
                 public void run() {
                     NetworkTreeView.this.viewer.refresh();
                 }
             });
         }
     }
 
     /**
      * Returns layer, that contains necessary gis node
      * 
      * @param map map
      * @param gisNode gis node
      * @return layer or null
      */
     public static ILayer findLayerByNode(IMap map, Node gisNode) {
         try {
             for (ILayer layer : map.getMapLayers()) {
                 IGeoResource resource = layer.findGeoResource(GeoNeo.class);
                 if (resource != null && resource.resolve(GeoNeo.class, null).getMainGisNode().equals(gisNode)) {
                     return layer;
                 }
             }
             return null;
         } catch (IOException e) {
             // TODO Handle IOException
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * get coordinates of selected node if node do not contains coordinates, try to find it in
      * parent node
      * 
      * @param node node
      * @return
      */
     private static double[] getCoords(Node node) {
         for (int i = 0; i <= 1; i++) {
             if (node.hasProperty(INeoConstants.PROPERTY_LAT_NAME)) {
                 if (node.hasProperty(INeoConstants.PROPERTY_LON_NAME)) {
                     try {
                         return new double[] {(Float)node.getProperty(INeoConstants.PROPERTY_LON_NAME),
                                 (Float)node.getProperty(INeoConstants.PROPERTY_LAT_NAME)};
                     } catch (ClassCastException e) {
                         return new double[] {(Double)node.getProperty(INeoConstants.PROPERTY_LON_NAME),
                                 (Double)node.getProperty(INeoConstants.PROPERTY_LAT_NAME)};
                     }
                 }
             }
             // try to up by 1 lvl
             Traverser traverse = node.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                     ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.BOTH);
             if (traverse.iterator().hasNext()) {
                 node = traverse.iterator().next();
             } else {
                 return null;
             }
         }
         return null;
 
     }
 
     /**
      * delete all incoming references
      * 
      * @param node
      */
     public static void deleteIncomingRelations(Node node) {
         Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             for (Relationship relation : node.getRelationships(Direction.INCOMING)) {
                 relation.delete();
             }
             transaction.success();
         } finally {
             transaction.finish();
         }
     }
 
     private class RenameAction extends Action {
 
         private boolean enabled;
         private final String text;
         private Node node = null;
         private NeoNode neoNode;
 
         /**
          * Constructor
          * 
          * @param selection - selection
          */
         public RenameAction(IStructuredSelection selection) {
             text = "Rename";
             enabled = selection.size() == 1 && selection.getFirstElement() instanceof NeoNode
                     && !(selection.getFirstElement() instanceof Root);
             if (enabled) {
                 neoNode = (NeoNode)selection.getFirstElement();
                 node = neoNode.getNode();
                 enabled = node.hasProperty(INeoConstants.PROPERTY_NAME_NAME);
             }
         }
 
         @Override
         public boolean isEnabled() {
             return enabled;
         }
 
         @Override
         public String getText() {
             return text;
         }
 
         @Override
         public void run() {
             neoNode.setName(getNewName(node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString()));
         }
     }
 
     private class TransmissionAction extends NeighbourAction {
 
         /**
          * @param selection
          */
         public TransmissionAction(IStructuredSelection selection) {
             super(selection);
             text = "Analyse transmission";
         }
 
         @Override
         public void run() {
             IViewPart transmissionView;
             try {
                 transmissionView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                         TransmissionView.ID);
             } catch (PartInitException e) {
                 NeoCorePlugin.error(e.getLocalizedMessage(), e);
                 transmissionView = null;
             }
             if (transmissionView != null) {
                 ((TransmissionView)transmissionView).setInput(getInputNodes(selection));
             }
         }
 
     }
 
     /**
      * <p>
      * Action for start neighbour analyser
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private class NeighbourAction extends Action {
 
         protected boolean enabled;
         protected String text;
         protected IStructuredSelection selection;
 
         /**
          * Constructor
          * 
          * @param selection - selection
          */
         public NeighbourAction(IStructuredSelection selection) {
             text = "Analyse neighbours";
             enabled = true;// selection.getFirstElement() instanceof NeoNode &&
             // !(selection.getFirstElement() instanceof Root);
             if (enabled) {
                 this.selection = selection;
             }
         }
 
         @Override
         public boolean isEnabled() {
             return enabled;
         }
 
         @Override
         public String getText() {
             return text;
         }
 
         @Override
         public void run() {
             ActionUtil.getInstance().runTask(new Runnable() {
 
                 @Override
                 public void run() {
                     IViewPart neighbourView;
                     try {
                         neighbourView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                                 NeighboursView.ID);
                     } catch (PartInitException e) {
                         NeoCorePlugin.error(e.getLocalizedMessage(), e);
                         neighbourView = null;
                     }
                     if (neighbourView != null) {
                         ((NeighboursView)neighbourView).setInput(getInputNodes(selection));
                     }
                 }
             }, true);
         }
 
         /**
          * get set of input nodes from selection
          * 
          * @param selection - selection
          * @return set of input nodes
          */
         protected Set<Node> getInputNodes(IStructuredSelection selection) {
             Set<Node> result = new HashSet<Node>();
             Iterator iterator = selection.iterator();
             while (iterator.hasNext()) {
                 Object element = iterator.next();
                 if (element instanceof Root || !(element instanceof NeoNode)) {
                     continue;
                 }
                 result.add(((NeoNode)element).getNode());
 
             }
             return result;
         }
     }
 
     /**
      * <p>
      * Action to revert node name
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private class RevertNameAction extends Action {
 
         private boolean enabled;
         private String text;
         private Object oldName;
         private Node node = null;
 
         /**
          * @param selection
          */
 
         public RevertNameAction(IStructuredSelection selection) {
             text = "Revert name to '";
             enabled = selection.size() == 1 && selection.getFirstElement() instanceof NeoNode;
             if (enabled) {
                 NeoNode neoNode = (NeoNode)selection.getFirstElement();
                 node = neoNode.getNode();
                 enabled = node.hasProperty(INeoConstants.PROPERTY_OLD_NAME) && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME);
                 if (enabled) {
                     oldName = node.getProperty(INeoConstants.PROPERTY_OLD_NAME);
                     text += oldName + "'";
                 }
             }
 
         }
 
         @Override
         public void run() {
             Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
             try {
                 Object name = node.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                 node.setProperty(INeoConstants.PROPERTY_NAME_NAME, oldName);
                 node.setProperty(INeoConstants.PROPERTY_OLD_NAME, name);
                 tx.success();
                 NeoServiceProvider.getProvider().commit();// viewer will be refreshed after commit
             } finally {
                 tx.finish();
             }
 
         }
 
         @Override
         public boolean isEnabled() {
             return enabled;
         }
 
         @Override
         public String getText() {
             return text;
         }
     }
 
     /**
      * Action to delete all selected nodes and their child nodes in the graph, but not nodes related
      * by other geographic relationships. The result is designed to remove sub-tree's from the tree
      * view, leaving remaining tree nodes in place.
      * 
      * @author Cinkel_A
      * @author craig
      * @since 1.0.0
      */
     private class DeleteAction extends Action {
         private final List<NeoNode> nodesToDelete;
         private String text = null;
         private boolean interactive = false;
 
         private DeleteAction(List<NeoNode> nodesToDelete, String text) {
             this.nodesToDelete = nodesToDelete;
             this.text = text;
         }
 
         @SuppressWarnings("unchecked")
         private DeleteAction(IStructuredSelection selection) {
             interactive = true;
             nodesToDelete = new ArrayList<NeoNode>();
             Iterator iterator = selection.iterator();
             HashSet<String> nodeTypes = new HashSet<String>();
             while (iterator.hasNext()) {
                 Object element = iterator.next();
                 if (element != null && element instanceof NeoNode && !(element instanceof Root)) {
                     nodesToDelete.add((NeoNode)element);
                     nodeTypes.add(NeoUtils.getNodeType(((NeoNode)element).getNode()));
                 }
             }
             String type = nodeTypes.size() == 1 ? nodeTypes.iterator().next() : "node";
             switch (nodesToDelete.size()) {
             case 0:
                 text = "Select nodes to delete";
                 break;
             case 1:
                 text = "Delete " + type + " '" + nodesToDelete.get(0).toString() + "'";
                 break;
             case 2:
             case 3:
             case 4:
                 for (NeoNode node : nodesToDelete) {
                     if (text == null) {
                         text = "Delete " + type + "s " + node;
                     } else {
                         text += ", " + node;
                     }
                 }
                 break;
             default:
                 text = "Delete " + nodesToDelete.size() + " " + type + "s";
                 break;
             }
             // TODO: Find a more general solution
             text = text.replaceAll("citys", "cities");
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public void run() {
             if (interactive) {
                 MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.YES | SWT.NO);
                 msg.setText("Delete node");
                 msg.setMessage(getText() + "?\n\nAll contained data will also be deleted!");
                 int result = msg.open();
                 if (result != SWT.YES) {
                     return;
                 }
             }
             // First we cut the selected nodes out of the tree so the user does not try interact
             // with them
             // and so a delta report action below will not use them
             final Node gisNode;
             final Node networkNode;
             Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
             try {
                 gisNode = getGisNode(nodesToDelete.get(0).getNode());
                 networkNode = getParentNode(nodesToDelete.get(0).getNode(), "network");
                 for (NeoNode neoNode : nodesToDelete) {
                     Node node = neoNode.getNode();
                     for (Relationship relation : node.getRelationships(NetworkRelationshipTypes.CHILD, Direction.INCOMING)) {
                         relation.delete();
                     }
                 }
                 transaction.success();
             } finally {
                 transaction.finish();
             }
             NeoServiceProvider.getProvider().commit();
             viewer.refresh();
 
             // Create a job for deleting all the nodes and sub-nodes in the disconnected graph from
             // the database
             Job job = new Job(getText()) {
 
                 @SuppressWarnings("unchecked")
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     int size = 2;
                     int count = 0;
                     for (NeoNode neoNode : nodesToDelete) {
                         size += 2 + getTreeSize(neoNode.getNode());
                     }
                     size = size * 2;
                     monitor.beginTask(getText(), size);
                     // First we cut the tree out of the graph so only CHILD relations exist
                     for (NeoNode neoNode : nodesToDelete) {
                         if (monitor.isCanceled()) {
                             break;
                         }
                         monitor.subTask("Extracting " + neoNode.toString());
                         Node node = neoNode.getNode();
                         cleanTree(node, monitor); // Delete non-tree relations (and relink gis
                                                     // next
                         // links)
                         deleteIncomingRelations(node); // only delete all incoming relations once
                         // we've dealt with the GIS relations in
                         // cleanTree()
                         monitor.worked(1);
                         count++;
                     }
                     // Since nodes are not in the tree anymore, we can re-draw the tree (but don't
                     // need to wait for that to happen)
                     viewer.getControl().getDisplay().asyncExec(new Runnable() {
                         public void run() {
                             NetworkTreeView.this.viewer.refresh();
                         }
                     });
                     monitor.worked(1);
                     if (count < nodesToDelete.size()) {
                         return Status.CANCEL_STATUS;
                     }
                     monitor.worked(1);
                     for (NeoNode neoNode : nodesToDelete) {
                         if (monitor.isCanceled()) {
                             break;
                         }
                         monitor.subTask("Deleting " + neoNode.toString());
                         Node node = neoNode.getNode();
                         NeoCorePlugin.getDefault().getProjectService().deleteNode(node);
                         monitor.worked(1);
                     }
                     monitor.worked(1);
                     if (gisNode != null && networkNode != null) {
                         // TODO: Remove this code once we trust the delete function more fully
                         fixOrphanedNodes(gisNode, networkNode, monitor);
                     }
                     monitor.done();
                     return Status.OK_STATUS;
                 }
 
                 private void fixOrphanedNodes(final Node gisNode, final Node networkNode, IProgressMonitor monitor) {
                     final ArrayList<Node> orphans = new ArrayList<Node>();
                     final ArrayList<String> orphanNames = new ArrayList<String>();
                     Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
                     try {
                         final long refId = NeoServiceProvider.getProvider().getService().getReferenceNode().getId();
                         final long netId = networkNode.getId();
                         monitor.subTask("Searching for orphaned nodes");
                         for (Node node : gisNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL,
                                 GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
                             if (!node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                                 @Override
                                 public boolean isReturnableNode(TraversalPosition currentPos) {
                                     Node cn = currentPos.currentNode();
                                     // if(orphans.size()<1){
                                     // System.out.println("Checking node:
                                     // "+cn.getProperty("name",cn.toString()));
                                     // System.out.println("\tnode id = "+cn.getId());
                                     // }
                                     return cn.getId() == refId || cn.getId() == netId;
                                 }
                             }, NetworkRelationshipTypes.CHILD, Direction.INCOMING).iterator().hasNext()) {
                                 orphans.add(node);
                                 orphanNames.add(node.getProperty("name", node.toString()).toString());
                             }
                         }
                         transaction.success();
                     } finally {
                         transaction.finish();
                     }
                     if (orphans.size() > 0) {
                         monitor.subTask("Fixing " + orphans.size() + " orphaned nodes");
                         System.out.println("Found " + orphans.size() + " orphaned nodes from cancelled deletion");
                         int count = 0;
                         for (String name : orphanNames) {
                             System.out.println("\tOrphan: " + name);
                             if (count++ > 10)
                                 break;
                         }
                         transaction = NeoServiceProvider.getProvider().getService().beginTx();
                         try {
                             String parentType = "unknown";
                             try {
                                 parentType = networkNode.getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)
                                         .iterator().next().getEndNode().getProperty("type").toString();
                             } catch (Exception e) {
                             }
                             Node parent = NeoServiceProvider.getProvider().getService().createNode();
                             parent.setProperty("name", Integer.toString(orphans.size()) + " - Orphans from failed deletion");
                             parent.setProperty("type", parentType);
                             networkNode.createRelationshipTo(parent, NetworkRelationshipTypes.CHILD);
                             for (Node node : orphans) {
                                 for (Relationship relation : node.getRelationships(NetworkRelationshipTypes.CHILD,
                                         Direction.INCOMING)) {
                                     relation.delete();
                                 }
                                 parent.createRelationshipTo(node, NetworkRelationshipTypes.CHILD);
                             }
                             transaction.success();
                         } finally {
                             transaction.finish();
                         }
                     }
                 }
 
                 private int getTreeSize(Node node) {
                     int size = 0;
                     Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
                     try {
                         for (@SuppressWarnings("unused")
                         Node nodeToClean : node.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL,
                                 NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                             size++;
                         }
                         transaction.success();
                         return size;
                     } finally {
                         transaction.finish();
                     }
                 }
 
                 private void cleanTree(Node node, IProgressMonitor monitor) {
                     Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
                     try {
                         for (Node nodeToClean : node.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
                                 ReturnableEvaluator.ALL, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                             cleanNode(nodeToClean);
                             monitor.worked(1);
                             if (monitor.isCanceled()) {
                                 break;
                             }
                         }
                         transaction.success();
                     } finally {
                         transaction.finish();
                     }
                 }
 
                 private void cleanNode(Node node) {
                     Iterator<Relationship> relations = node.getRelationships(Direction.BOTH).iterator();
                     Node geoPrev = null;
                     Node geoNext = null;
                     while (relations.hasNext()) {
                         Relationship relationship = relations.next();
                         // Ignore child relationships, so we maintain the tree structure
                         // (for now)
                         if (!(relationship.getType().equals(NetworkRelationshipTypes.CHILD))) {
                             // Take note of GIS relationships, so we can re-link around the
                             // spatial node
                             if (relationship.getType().equals(GeoNeoRelationshipTypes.NEXT)) {
                                 if (relationship.getEndNode().equals(node)) {
                                     geoPrev = relationship.getStartNode();
                                 } else {
                                     geoNext = relationship.getEndNode();
                                 }
                             }
                             relationship.delete();
                         }
                     }
                     if (geoPrev != null && geoNext != null) {
                         // System.out.println("Creating new geo-next relationship: " +
                         // geoPrev.getProperty("name", null) + " -("
                         // + GeoNeoRelationshipTypes.NEXT + ")-> " + geoNext.getProperty("name",
                         // null));
                         geoPrev.createRelationshipTo(geoNext, GeoNeoRelationshipTypes.NEXT);
                     }
                 }
 
             };
             job.schedule(50);
 
         }
 
         @Override
         public String getText() {
             return text;
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public boolean isEnabled() {
             return nodesToDelete.size() > 0;
         }
 
     }
 
     /**
      * General a delta report based on two selected networks, sites or sectors
      * 
      * @author Craig
      * @since 1.0.0
      */
     private class DeltaReportAction extends Action {
 
         private ArrayList<NeoNode> validNodes;
         private String reportText;
         private String reportName;
         private NeoNode previousReport;
         private Node reportNode;
 
         @SuppressWarnings("unchecked")
         @Override
         public void run() {
 
             if (getPreviousReport() != null) {
                 MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.YES | SWT.NO);
                 msg.setText("Delta Report");
                 msg.setMessage("Delta report already exists: " + getName() + "\n\nThis will be deleted if you continue.\n\n"
                         + getText() + "?");
                 int result = msg.open();
                 if (result != SWT.YES) {
                     return;
                 }
                 (new DeleteAction(Arrays.asList(new NeoNode[] {previousReport}), "Deleting previous delta report: " + getName()))
                         .run();
             }
             // TODO: Consider building on previous report instead of rebuilding for performance
             // reasons
             makePreviousReport();
 
             NeoServiceProvider.getProvider().commit();
             Job job = new Job(getName()) {
 
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     monitor.beginTask(getName(), 100);
                     int initPerc = 5;
                     int searchPerc = 15;
                     int calcPerc = 40;
                     int reportPerc = 40;
                     NeoService neo = NeoServiceProvider.getProvider().getService();
                     Transaction tx = neo.beginTx();
                     try {
                         monitor.subTask("Preparing report");
                         LinkedHashMap<String, Node> deltaSitesNodes = new LinkedHashMap<String, Node>();
                         HashMap<String, Node> missingSitesNodes = new HashMap<String, Node>();
                         // The code below was originally designed for building on a previous report
                         // for performance
                         // but currently has no affect, since we delete previous reports
                         // TODO: Either enable building on previous reports, or delete this code
                         for (Node node : reportNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                                 ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                             for (NeoNode networkNode : getValidNodes()) {
                                 if (node.getProperty("name", "").equals(networkNode.toString())) {
                                     missingSitesNodes.put(networkNode.toString(), node);
                                 }
                             }
                         }
                         for (NeoNode networkNode : getValidNodes()) {
                             if (!missingSitesNodes.containsKey(networkNode.toString())) {
                                 Node node = neo.createNode();
                                 node.setProperty("name", "sites missing from " + networkNode);
                                 node.setProperty("type", "missing_sites");
                                 node.setProperty("sites missing from", networkNode.toString());
                                 reportNode.createRelationshipTo(node, NetworkRelationshipTypes.CHILD);
                                 missingSitesNodes.put(networkNode.toString(), node);
                             }
                             for (NeoNode otherNode : getValidNodes()) {
                                 if (!otherNode.equals(networkNode)) {
                                     String key = networkNode.toString() + " & " + otherNode.toString();
                                     String altkey = otherNode.toString() + " & " + networkNode.toString();
                                     if (!deltaSitesNodes.containsKey(key) && !deltaSitesNodes.containsKey(altkey)) {
                                         Node deltaSitesNode = neo.createNode();
                                         deltaSitesNode.setProperty("name", "changed sites");
                                         deltaSitesNode.setProperty("type", "delta_site");
                                         deltaSitesNode.setProperty("key", key);
                                         deltaSitesNode.setProperty("network1", networkNode.toString());
                                         deltaSitesNode.setProperty("network2", otherNode.toString());
                                         reportNode.createRelationshipTo(deltaSitesNode, NetworkRelationshipTypes.CHILD);
                                         deltaSitesNodes.put(key, deltaSitesNode);
                                     }
                                 }
                             }
                         }
                         // remove name ambiguity on multiple comparisons
                         if (deltaSitesNodes.size() > 1) {
                             for (java.util.Map.Entry<String, Node> entry : deltaSitesNodes.entrySet()) {
                                 entry.getValue().setProperty("name", "changed sites: " + entry.getKey());
                             }
                         }
 
                         // Persist current state
                         tx = commit(neo, tx);
                         monitor.worked(initPerc);
                         monitor.subTask("Searching for all sites");
 
                         // Build internal data structures
                         TreeMap<String, DeltaSite> siteMap = new TreeMap<String, DeltaSite>();
                         for (NeoNode networkNode : getValidNodes()) {
                             String networkName = networkNode.toString();
                             for (Node site : networkNode.getNode().traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                                     new ReturnableEvaluator() {
 
                                         @Override
                                         public boolean isReturnableNode(TraversalPosition currentPos) {
                                             return currentPos.currentNode().getProperty("type", "").toString().equals("site");
                                         }
                                     }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                                 String siteName = site.getProperty("name", "").toString();
                                 if (siteMap.containsKey(siteName)) {
                                     siteMap.get(siteName).put(networkName, site);
                                 } else {
                                     siteMap.put(siteName, new DeltaSite(siteName, networkName, site));
                                 }
                             }
                         }
                         reportNode.setProperty("sites_count", siteMap.size());
 
                         // Persist current state
                         tx = commit(neo, tx);
                         monitor.worked(searchPerc);
                         monitor.subTask("Calculating differences");
 
                         HashMap<NeoNode, ArrayList<DeltaSite>> missingSites = new HashMap<NeoNode, ArrayList<DeltaSite>>();
                         HashMap<String, ArrayList<DeltaSite>> deltaSites = new HashMap<String, ArrayList<DeltaSite>>();
                         for (NeoNode networkNode : getValidNodes()) {
                             missingSites.put(networkNode, new ArrayList<DeltaSite>());
                         }
                         for (java.util.Map.Entry<String, Node> entry : deltaSitesNodes.entrySet()) {
                             deltaSites.put(entry.getKey(), new ArrayList<DeltaSite>());
                         }
                         for (DeltaSite site : siteMap.values()) {
                             for (NeoNode networkNode : getValidNodes()) {
                                 if (site.missingIn(networkNode.toString())) {
                                     missingSites.get(networkNode).add(site);
                                 }
                             }
                             for (java.util.Map.Entry<String, Node> entry : deltaSitesNodes.entrySet()) {
                                 String deltaKey = entry.getKey();
                                 Node deltaSitesNode = entry.getValue();
                                 if (site.hasDelta(deltaKey, deltaSitesNode)) {
                                     deltaSites.get(deltaKey).add(site);
                                 }
                             }
                         }
                         for (java.util.Map.Entry<String, Node> entry : deltaSitesNodes.entrySet()) {
                             deltaSitesNodes.get(entry.getKey()).setProperty("sites_count", deltaSites.get(entry.getKey()).size());
                         }
                         for (NeoNode networkNode : getValidNodes()) {
                             missingSitesNodes.get(networkNode.toString()).setProperty("site_count",
                                     missingSites.get(networkNode).size());
                         }
                         // Persist current state
                         tx = commit(neo, tx);
                         monitor.worked(calcPerc);
                         monitor.subTask("Building report structures in the database");
 
                         int reportWorkedUnit = reportPerc / (getValidNodes().size() + 1);
                         for (NeoNode networkNode : getValidNodes()) {
                             for (DeltaSite site : missingSites.get(networkNode)) {
                                 Node missingSite = neo.createNode();
                                 missingSite.setProperty("name", site.name);
                                 missingSite.setProperty("type", "missing_site");
                                 missingSitesNodes.get(networkNode.toString()).createRelationshipTo(missingSite,
                                         NetworkRelationshipTypes.CHILD);
                                 for (Node node : site.nodes.values()) {
                                     missingSite.createRelationshipTo(node, NetworkRelationshipTypes.MISSING);
                                 }
                             }
                             tx = commit(neo, tx);
                             monitor.worked(reportWorkedUnit);
                         }
                         for (java.util.Map.Entry<String, Node> entry : deltaSitesNodes.entrySet()) {
                             String deltaKey = entry.getKey();
                             Node deltaSitesNode = deltaSitesNodes.get(deltaKey);
                             for (DeltaSite site : deltaSites.get(entry.getKey())) {
                                 Node deltaSite = neo.createNode();
                                 deltaSite.setProperty("name", site.name);
                                 deltaSite.setProperty("type", "delta_site");
                                 deltaSitesNode.createRelationshipTo(deltaSite, NetworkRelationshipTypes.CHILD);
                                 for (Node node : site.nodes.values()) {
                                     deltaSite.createRelationshipTo(node, NetworkRelationshipTypes.DIFFERENT);
                                 }
                                 for (Map.Entry<String, DeltaSite.Pair> delta : site.getDelta(deltaKey).entrySet()) {
                                     deltaSite.setProperty("Delta " + delta.getKey(), delta.getValue().toString());
                                 }
                             }
                         }
                         tx = commit(neo, tx);
                         monitor.worked(reportWorkedUnit);
 
                         tx.success();
                         monitor.done();
                         viewer.getControl().getDisplay().asyncExec(new Runnable() {
                             public void run() {
                                 NetworkTreeView.this.viewer.refresh();
                             }
                         });
                         return Status.OK_STATUS;
                     } finally {
                         tx.finish();
                     }
                 }
 
                 private Transaction commit(NeoService neo, Transaction tx) {
                     tx.success();
                     tx.finish();
                     tx = neo.beginTx();
                     return tx;
                 }
 
             };
             job.schedule();
 
         }
 
         private NeoNode getPreviousReport() {
             if (previousReport == null) {
                 Root rootTree = (Root)((ITreeContentProvider)viewer.getContentProvider()).getElements(0)[0];
                 for (NeoNode neoNode : rootTree.getChildren()) {
                     if (neoNode.getType().equals("delta_report") && neoNode.toString().equals(getName())) {
                         previousReport = neoNode;
                         reportNode = previousReport.getNode();
                         break;
                     }
                 }
             }
             return previousReport;
         }
 
         private NeoNode makePreviousReport() {
             NeoService neo = NeoServiceProvider.getProvider().getService();
             Transaction tx = neo.beginTx();
             try {
                 reportNode = neo.createNode();
                 reportNode.setProperty("type", "delta_report");
                 reportNode.setProperty("name", getName());
                 for (NeoNode networkNode : getValidNodes()) {
                     reportNode.createRelationshipTo(networkNode.getNode(), NetworkRelationshipTypes.DELTA_REPORT);
                 }
                 tx.success();
                 // NeoServiceProvider.getProvider().commit();
             } finally {
                 tx.finish();
             }
             // viewer.refresh();
             return getPreviousReport();
         }
 
         @Override
         public String getText() {
             if (reportText == null) {
                 if (getValidNodes().size() == 2) {
                     reportText = "Calculate differences between '" + getValidNodes().get(0) + "' and '" + getValidNodes().get(1)
                             + "'";
                 } else {
                     reportText = "Select two networks, sites or sectors to compare";
                 }
             }
             return reportText;
         }
 
         public String getName() {
             if (reportName == null) {
                 if (getValidNodes().size() == 2) {
                     reportName = "Delta Report: " + getValidNodes().get(0) + " <=> " + getValidNodes().get(1);
                 } else {
                     reportName = "Delta Report";
                 }
             }
             return reportName;
         }
 
         @SuppressWarnings("unchecked")
         private List<NeoNode> getValidNodes() {
             if (validNodes == null) {
                 validNodes = new ArrayList<NeoNode>();
                 IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                 final Iterator iterator = selection.iterator();
                 while (iterator.hasNext()) {
                     Object node = iterator.next();
                     if (node instanceof NeoNode) {
                         NeoNode neoNode = (NeoNode)node;
                         String type = neoNode.getType();
                         if ("network".equals(type) || "site".equals(type) || "sector".equals(type)) {
                             NeoNode prevNode = validNodes.size() > 0 ? validNodes.get(0) : null;
                             if (prevNode == null || prevNode.getType().equals(neoNode.getType())) {
                                 validNodes.add(neoNode);
                             }
                         }
                     }
                 }
             }
             return validNodes;
         }
 
         @Override
         public boolean isEnabled() {
             return getValidNodes().size() == 2;
         }
 
     }
 
     /**
      * Action creates a report file on the base of a delta report selected
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     private class GenerateReportFileAction extends Action {
         private NeoNode reportNode;
 
         @Override
         public void run() {
             StringBuffer sb = new StringBuffer("report '").append(reportNode.getNode().getProperty("name"));
             sb.append("' do\n  author '").append(System.getProperty("user.name")).append("'\n  date '").append(
                     new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("'");
             for (NeoNode node : reportNode.getChildren()) {
                 sb.append("\n  table '").append(node.getNode().getProperty("name")).append("' do\n");
                 NeoNode[] children = node.getChildren();
                 HashSet<String> properties = new HashSet<String>();
                 List<Long> ids = new ArrayList<Long>();
                 for (NeoNode neoNode : children) {
                     if (node.getType().equals("delta_site")) {
                         Iterable<String> propertyKeys = neoNode.getNode().getPropertyKeys();
                         for (String propKey : propertyKeys) {
                             Matcher matcher = Pattern.compile("(\\d+)([A-Z]{1})").matcher(propKey);
                             if (!propKey.matches(".*(#).*")) {// "# '...' neighbours" excluded
                                 if (propKey.matches("Delta.*")) {
                                     if (matcher.find()) {
                                         // sector's property has been changed
                                         String sector = matcher.group();
                                         properties.add(propKey.substring(propKey.indexOf(sector) + sector.length() + 1));
                                     } else {
                                         // site's property has been changed
                                         properties.add(propKey.split("\\s")[1]);
                                     }
                                 } else {
                                     properties.add(propKey);
                                 }
                             }
                         }
                         // add sites
                         Iterable<Relationship> relationshipsToSite = neoNode.getNode().getRelationships(
                                 NetworkRelationshipTypes.DIFFERENT, Direction.OUTGOING);
                         for (Relationship relToSite : relationshipsToSite) {
                             Node siteNode = relToSite.getEndNode();
                             ids.add(siteNode.getId());
                             // add sectors
                             Iterable<Relationship> relationshipsToSector = siteNode.getRelationships(
                                     NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
                             for (Relationship relToSector : relationshipsToSector) {
                                 ids.add(relToSector.getEndNode().getId());
                             }
                         }
                     } else {
                         ids.add(neoNode.getNode().getId());
                     }
                 }
                 
                 addArray(sb, ids.toArray(), "    self.nodes=");
                 addArray(sb, properties.toArray(), "    self.properties=");
                 sb.append("  end");
             }
             sb.append("\nend");
             IProject project = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0];//TODO correct
             IFile file;
             int i = 0;
             while ((file = project.getFile(new Path(("report" + i) + ".r"))).exists()) {
                 i++;
             }
             System.out.println("Repost script:\n" + sb.toString());
             InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
             try {
                 file.create(is, true, null);
                 is.close();
             } catch (CoreException e) {
                 // TODO Handle CoreException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             } catch (IOException e) {
                 // TODO Handle IOException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
             try {
                 getViewSite().getPage().openEditor(new FileEditorInput(file), "org.amanzi.splash.editors.ReportEditor");
             } catch (PartInitException e) {
                 // TODO Handle PartInitException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
 
         /**
          * @param sb
          * @param prefix TODO
          * @param ids
          */
         private void addArray(StringBuffer sb, Object[] array, String prefix) {
             if (array.length != 0) {
                 sb.append(prefix).append("[");
                 for (int i = 0; i < array.length; i++) {
                     Object obj = array[i];
                     if (obj instanceof String) {
                         sb.append("'").append(obj).append("'");
                     } else {
                         sb.append(obj);
                     }
                     if (i < array.length - 1)
                         sb.append(",");
                 }
                 sb.append("]\n");
             }
         }
 
         @Override
         public String getText() {
             return "Generate delta report file";
         }
 
         @Override
         public boolean isEnabled() {
             IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
             final Iterator iterator = selection.iterator();
             while (iterator.hasNext()) {
                 Object node = iterator.next();
                 if (node instanceof NeoNode) {
                     NeoNode neoNode = (NeoNode)node;
                    if (neoNode.getType().equals("delta_report")) {
                         reportNode = neoNode;
                         return true;
                     }
                 }
             }
             return false;
         }
 
     }
 
     private static class DeltaSite {
         private final String name;
         private final HashMap<String, Node> nodes = new HashMap<String, Node>();
         private final HashMap<String, HashMap<String, Pair>> deltas = new HashMap<String, HashMap<String, Pair>>();
 
         public DeltaSite(String name, String network, Node node) {
             this.name = name;
             this.nodes.put(network, node);
         }
 
         public String getName() {
             return name;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
         public boolean missingIn(String network) {
             return !nodes.containsKey(network);
         }
 
         public Node nodeIn(String network) {
             return nodes.get(network);
         }
 
         public void put(String network, Node node) {
             nodes.put(network, node);
         }
 
         public boolean hasDelta(String deltaKey, Node deltaSitesNode) {
             if (nodes.size() < 2) {
                 return false;
             } else {
                 if (!deltas.containsKey(deltaKey)) {
                     HashMap<String, Pair> delta = new HashMap<String, Pair>();
                     deltas.put(deltaKey, delta);
                     String[] networks = new String[] {deltaSitesNode.getProperty("network1").toString(),
                             deltaSitesNode.getProperty("network2").toString()};
                     for (String network : networks) {
                         Node site = nodes.get(network);
                         addProperties(delta, site, null);
                         for (Node sector : site.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                                 ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                             addProperties(delta, sector, sector.getProperty("name").toString());
                         }
                     }
                     // remove identical properties
                     ArrayList<String> identical = new ArrayList<String>();
                     for (Map.Entry<String, Pair> entry : delta.entrySet()) {
                         if (entry.getValue().same()) {
                             identical.add(entry.getKey());
                             // } else {
                             // System.out.println("Property changed for '"+entry.getKey()+"':
                             // "+entry.getValue());
                         }
                     }
                     for (String key : identical) {
                         delta.remove(key);
                     }
                 }
                 return deltas.get(deltaKey).size() > 0;
             }
         }
 
         private void addProperties(HashMap<String, Pair> delta, Node node, String prefix) {
             for (String property : node.getPropertyKeys()) {
                 String key = property;
                 if (prefix != null) {
                     key = prefix + " " + property;
                 }
                 Pair properties = delta.get(key);
                 if (properties == null) {
                     properties = new Pair();
                     delta.put(key, properties);
                     properties.left = node.getProperty(property);
                 } else {
                     properties.right = node.getProperty(property);
                 }
             }
         }
 
         public Map<String, Pair> getDelta(String deltaKey) {
             return deltas.get(deltaKey);
         }
 
         private static class Pair {
             private Object left;
             private Object right;
 
             private boolean same() {
                 return left == right || left != null && left.equals(right);
             }
 
             @Override
             public String toString() {
                 return left.toString() + " <=> " + right + typeChanged();
             }
 
             public String typeChanged() {
                 if (right == null) {
                     return " (" + typeString(left) + " <=>  null )";
                 }
                 if (left.getClass() == right.getClass()) {
                     return "";
                 } else {
                     return " (" + typeString(left) + " <=> " + typeString(right) + ")";
                 }
             }
 
             private static String typeString(Object obj) {
                 return obj.getClass().toString().replace("class java.lang.", "");
             }
         }
     }
 
     /**
      * Opens a dialog asking the user for a new name.
      * 
      * @return The new name of the element.
      */
     private String getNewName(String oldName) {
         InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(), RENAME_MSG, "", oldName, null); //$NON-NLS-1$
         int result = dialog.open();
         if (result == Dialog.CANCEL)
             return oldName;
         return dialog.getValue();
     }
 
     /**
      * Class that responds to changes in preferences.
      */
     private class PreferenceChangeHandler implements IPropertyChangeListener {
         /**
          * Forward event, then refresh view.
          */
         public void propertyChange(final PropertyChangeEvent event) {
             // TODO use constant
             if ("nodePropertyNames".equals(event.getProperty())) {
                 viewer.refresh();
             }
         }
     }
 }

 package org.amanzi.awe.views.neighbours.views;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.views.neighbours.NeighboursPlugin;
 import org.amanzi.awe.views.neighbours.RelationWrapper;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkElementTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.part.ViewPart;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.neoclipse.NeoIcons;
 
 
 /**
  * This sample class demonstrates how to plug-in a new
  * workbench view. The view shows data obtained from the
  * model. The sample creates a dummy model on the fly,
  * but a real implementation would connect to the model
  * available either in this or another plug-in (e.g. the workspace).
  * The view is connected to the model using a content provider.
  * <p>
  * The view uses a label provider to define how model
  * objects should be presented in the view. Each
  * view can present the same model objects using
  * different labels and icons, if needed. Alternatively,
  * a single label provider can be shared between views
  * in order to ensure that objects of the same type are
  * presented in the same way everywhere.
  * <p>
  */
 
 public class NeighboursView extends ViewPart {
 
 
 
     /** String ROLLBACK field */
     private static final String ROLLBACK = "Rollback";
 
     /** String COMMIT field */
     private static final String COMMIT = "Commit";
 
     /**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "org.amanzi.awe.views.neighbours.views.NeighboursView";
 
 	private TableViewer viewer;
 	private Action actionCommit;
 	private Action actionRollback;
 	private Action doubleClickAction;
     private Node network;
     private Node gis = null;
     private ViewContentProvider provider;
     private boolean editMode = false;
     private Combo neighbour;
 
     private ViewLabelProvider labelProvider;
 
     private List<String> integerProperties = new ArrayList<String>();
 
     private List<String> doubleProperties = new ArrayList<String>();
 
     private Button rollback;
 
     private Button commit;
 
 	/*
 	 * The content provider class is responsible for
 	 * providing objects to the view. It can wrap
 	 * existing objects in adapters or simply return
 	 * objects as-is. These objects may be sensitive
 	 * to the current input of the view, or ignore
 	 * it and always show the same content 
 	 * (like Task List, for example).
 	 */
 	 
 	class ViewContentProvider implements IStructuredContentProvider {
         private static final int MAX_FIELD = 1000;
         private Collection<Node> input;
 
 
         public void inputChanged(Viewer v, Object oldInput, Object newInput) {
             if (!(newInput instanceof Collection)) {
                 input = new ArrayList<Node>(0);
                 network = null;
             } else {
                 input = (Collection<Node>)newInput;
                 if (input == null) {
                     input = new ArrayList<Node>(0);
                     network = null;
                 } else {
                     network = input.iterator().hasNext() ? NeoUtils.findNodeByChild(input.iterator().next(),
                             NetworkElementTypes.NETWORK.toString()) : null;
                     if (network == null) {
                         input = new ArrayList<Node>(0);
                     } else {
                         gis = network.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).getOtherNode(network);
                     }
                 }
             }
             updateNeighbourList(network);
             if (neighbour.getItemCount() > 0) {
                 neighbour.select(0);
                 neighbourSelectionChange();
             }
         }
 		public void dispose() {
 		}
 		public Object[] getElements(Object parent) {
             final RelationWrapper[] emptyArray = new RelationWrapper[0];
             Node neighbour = getNeighbour();
             if (neighbour == null || input == null) {
                 return emptyArray;
             }
             List<RelationWrapper> results = new ArrayList<RelationWrapper>();
             // Transaction tx = NeoUtils.beginTransaction();
             // try {
             Iterator<Relationship> iterator = new InputIterator(input, neighbour);
             int count = 0;
             while (iterator.hasNext() && ++count < MAX_FIELD) {
                 Relationship relation = (Relationship)iterator.next();
                 results.add(new RelationWrapper(relation));
             }
             return results.toArray(emptyArray);
             // } finally {
             // tx.finish();
             // }
 		}
 
 
 
         /**
          * <p>
          * Iterator of relationships
          * </p>
          * 
          * @author Cinkel_A
          * @since 1.0.0
          */
         public class InputIterator implements Iterator<Relationship> {
 
             private final Collection<Node> input;
             private String name;
             private Iterator<Node> iterator1;
             private Iterator<Node> nodeIterator;
             private Iterator<Relationship> iterator2;
 
             /**
              * @param input
              * @param neighbour
              */
             public InputIterator(Collection<Node> input, Node neighbour) {
                 this.input = input;
                 name = NeoUtils.getSimpleNodeName(neighbour, null);
                 iterator1 = input.iterator();
                 nodeIterator = new ArrayList<Node>().iterator();
                 iterator2 = new ArrayList<Relationship>().iterator();
             }
 
             @Override
             public boolean hasNext() {
                 if (iterator2.hasNext()) {
                     return true;
                 }
                 createIterator2();
                 return iterator2.hasNext();
             }
 
             /**
              * create Relationship iterator
              */
             private void createIterator2() {
                 createNodeIterator();
                 if (nodeIterator.hasNext()) {
                     Node mainNode = nodeIterator.next();
                     List<Relationship> result = new ArrayList<Relationship>();
                     for (Relationship relation : mainNode.getRelationships(NetworkRelationshipTypes.NEIGHBOUR, Direction.OUTGOING)) {
                         if (NeoUtils.getNeighbourName(relation, "").equals(name)) {
                             result.add(relation);
                         }
                     }
                     iterator2 = result.iterator();
                 } else {
                     iterator2 = new ArrayList<Relationship>().iterator();
                 }
             }
 
             /**
              * create node iterator
              */
             private void createNodeIterator() {
                 if (nodeIterator.hasNext()) {
                     return;
                 }
                 if (iterator1.hasNext()) {
                     Node mainNode = iterator1.next();
                     nodeIterator = mainNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                                             return currentPos.currentNode().hasRelationship(NetworkRelationshipTypes.NEIGHBOUR,
                                                     Direction.OUTGOING);
                         }
                     }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)
                             .iterator();
                 }
             }
 
             @Override
             public Relationship next() {
                 return iterator2.next();
             }
 
             @Override
             /**
              * Method do not support
              */
             public void remove() {
                 throw new UnsupportedOperationException();
             }
 
         }
 	}
 	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
         /** int DEF_SIZE field */
         protected static final int DEF_SIZE = 100;
         private ArrayList<String> columns = new ArrayList<String>();
 
         public String getColumnText(Object obj, int index) {
             // Transaction tx = NeoUtils.beginTransaction();
             // try {
             RelationWrapper relation = (RelationWrapper)obj;
             if (index == 0) {
                 return NeoUtils.getSimpleNodeName(relation.getServeNode(), "");
             } else if (index == 1) {
                 return NeoUtils.getSimpleNodeName(relation.getNeighbourNode(), "");
                 } else {
                 return relation.getRelation().getProperty(columns.get(index), "").toString();
                 }
             // } finally {
             // tx.finish();
             // }
             // return getText(obj);
 		}
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 
 
         /**
          *Create the table columns of the Neighbour types view.
          */
         private void createTableColumn() {
             Table table = viewer.getTable();
             TableViewerColumn column;
             TableColumn col;
             if (columns.isEmpty()) {
                 column = new TableViewerColumn(viewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText("Serving cell");
                 columns.add(col.getText());
 
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
                 column = new TableViewerColumn(viewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText("Neighbour cell");
                 columns.add(col.getText());
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
             }
             List<String> allNeighbourEditableProperties = getAllNeighbourEditableProperties();
             for (String name : allNeighbourEditableProperties) {
                 if (!columns.contains(name)) {
                     Class cl = doubleProperties.contains(name) ? Double.class : integerProperties.contains(name) ? Integer.class
                             : String.class;
                     int swt;
                     if (Number.class.isAssignableFrom(cl)) {
                         swt = SWT.RIGHT;
                     } else {
                         swt = SWT.LEFT;
                     }
                     column = new TableViewerColumn(viewer, swt);
                     col = column.getColumn();
                     col.setText(name);
                     columns.add(col.getText());
                     col.setWidth(DEF_SIZE);
                     col.setResizable(true);
 
                     column.setEditingSupport(new NeighbourEditableSupport(viewer, name, cl));
                 }
             }
             for (int i = 2; i < columns.size(); i++) {
                 TableColumn colum = table.getColumn(i);
                 if (!allNeighbourEditableProperties.contains(columns.get(i))) {
                     colum.setWidth(0);
                     colum.setResizable(false);
                 } else {
                     if (colum.getWidth() == 0) {
                         colum.setWidth(DEF_SIZE);
                         colum.setResizable(true);
                     }
                 }
             }
             table.setHeaderVisible(true);
             table.setLinesVisible(true);
             viewer.setLabelProvider(this);
            viewer.refresh();
         }
 	}
 	class NameSorter extends ViewerSorter {
 	}
 
 	/**
 	 * The constructor.
 	 */
 	public NeighboursView() {
 	}
 
     /**
      *launch if neighbour selection changed
      */
     public void neighbourSelectionChange() {
         integerProperties = new ArrayList<String>();
         doubleProperties = new ArrayList<String>();
         if (neighbour.getSelectionIndex() < 0 || gis == null) {
             return;
         } else {
             PropertyHeader header = new PropertyHeader(gis);
             String neighbourName = neighbour.getText();
             String[] arrayInt = header.getNeighbourIntegerFields(neighbourName);
             if (arrayInt != null) {
                 integerProperties.addAll(Arrays.asList(arrayInt));
             }
             String[] arrayDouble = header.getNeighbourDoubleFields(neighbourName);
             if (arrayDouble != null) {
                 doubleProperties.addAll(Arrays.asList(arrayDouble));
             }
 
         }
         if (labelProvider != null) {
             labelProvider.createTableColumn();
         }
     }
 
     /**
      * updates list of Neighbour
      * 
      * @param network network node
      */
     private void updateNeighbourList(Node network) {
         if (network == null) {
             neighbour.setItems(new String[0]);
             return;
         }
         // Transaction tx = NeoUtils.beginTransaction();
         List<String> neighbourName = new ArrayList<String>();
         // try{
             Node gisNode = network.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).getOtherNode(network);
             for (Relationship relation : gisNode.getRelationships(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING)) {
                 neighbourName.add(NeoUtils.getSimpleNodeName(relation.getOtherNode(gisNode), null));
             }
             neighbour.setItems(neighbourName.toArray(new String[0]));
         // }finally{
         // tx.finish();
         // }
     }
 
     /**
      * This is a callback that will allow us to create the viewer and initialize it.
      */
 	public void createPartControl(Composite parent) {
         Composite child = new Composite(parent, SWT.NONE);
         final GridLayout layout = new GridLayout(6, false);
         child.setLayout(layout);
 
         Label label = new Label(child, SWT.FLAT);
         label.setText("Neighbour list:");
         GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
         label.setLayoutData(layoutData);
 
         neighbour = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
         neighbour.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 neighbourSelectionChange();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.horizontalSpan = 2;
         Label spaser = new Label(child, SWT.FLAT);
         spaser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
         neighbour.setLayoutData(layoutData);
 
         commit = new Button(child, SWT.BORDER | SWT.PUSH);
         commit.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 NeighboursPlugin.getDefault().commit();
                 updateDirty(false);
                 viewer.refresh();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         commit.setToolTipText(COMMIT);
         commit.setImage(NeoIcons.COMMIT_ENABLED.image());
 
         rollback = new Button(child, SWT.BORDER | SWT.PUSH);
         rollback.setImage(NeoIcons.ROLLBACK_ENABLED.image());
         rollback.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 NeighboursPlugin.getDefault().rollback();
                 updateDirty(false);
                 viewer.refresh();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         rollback.setToolTipText(ROLLBACK);
 
         updateDirty(false);
         viewer = new TableViewer(child, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
         GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
         data.horizontalSpan = layout.numColumns;
         viewer.getControl().setLayoutData(data);
         labelProvider = new ViewLabelProvider();
         viewer.setLabelProvider(labelProvider);
         labelProvider.createTableColumn();
         provider = new ViewContentProvider();
         viewer.setContentProvider(provider);
 
         viewer.setSorter(new NameSorter());
         getSite().setSelectionProvider(viewer);
         viewer.getControl().addMouseListener(new MouseListener() {
 
             @Override
             public void mouseUp(MouseEvent e) {
             }
 
             @Override
             public void mouseDown(MouseEvent e) {
             }
 
             @Override
             public void mouseDoubleClick(MouseEvent e) {
                 Table table = viewer.getTable();
                 Point point = new Point(e.x, e.y);
                 TableItem item = table.getItem(point);
                 if (item != null) {
                     if (item.getBounds(0).contains(point)) {
 
                         showServe((RelationWrapper)item.getData());
                     } else if (item.getBounds(1).contains(point)) {
                         showNeighbour((RelationWrapper)item.getData());
                     }
                     item.getBounds();
                 }
             }
         });
         // viewer.addDoubleClickListener(new IDoubleClickListener() {
         // public void doubleClick(DoubleClickEvent event) {
         // System.out.println(event);
         // event.getSelection();
         // System.out.println(event.getSelection());
         // }
         // });
         // hookContextMenu();
         // hookDoubleClickAction();
 	}
 
     /**
      * @param relationWrapper
      */
     protected void showNeighbour(RelationWrapper relationWrapper) {
         IMap map = ApplicationGIS.getActiveMap();
             for (ILayer layer : map.getMapLayers()) {
                 IGeoResource resourse = layer.findGeoResource(GeoNeo.class);
                 if (resourse != null) {
                     try {
                         GeoNeo geo = resourse.resolve(GeoNeo.class, null);
                         if (geo.getMainGisNode().equals(gis)) {
                             HashMap<String,Object> properties=new HashMap<String, Object>();
                         properties.put(GeoNeo.NEIGH_MAIN_NODE, null);
                         properties.put(GeoNeo.NEIGH_NAME, relationWrapper.toString());
                         properties.put(GeoNeo.NEIGH_RELATION, relationWrapper.getRelation());
                             geo.setProperties(properties);
                             layer.refresh(null);
                             return;
                         }
                     } catch (IOException e) {
                         throw (RuntimeException)new RuntimeException().initCause(e);
                     }
                 }
             }
     }
 
     /**
      * @param relationWrapper
      */
     protected void showServe(RelationWrapper relationWrapper) {
         IMap map = ApplicationGIS.getActiveMap();
         for (ILayer layer : map.getMapLayers()) {
             IGeoResource resourse = layer.findGeoResource(GeoNeo.class);
             if (resourse != null) {
                 try {
                     GeoNeo geo = resourse.resolve(GeoNeo.class, null);
                     if (geo.getMainGisNode().equals(gis)) {
                         HashMap<String, Object> properties = new HashMap<String, Object>();
                         properties.put(GeoNeo.NEIGH_MAIN_NODE, relationWrapper.getServeNode());
                         properties.put(GeoNeo.NEIGH_NAME, relationWrapper.toString());
                         properties.put(GeoNeo.NEIGH_RELATION, null);
                         geo.setProperties(properties);
                         layer.refresh(null);
                         return;
                     }
                 } catch (IOException e) {
                     throw (RuntimeException)new RuntimeException().initCause(e);
                 }
             }
         }
     }
 
     /**
      * @param b
      */
     private void updateDirty(boolean dirty) {
         editMode = dirty;
         commit.setEnabled(editMode);
         rollback.setEnabled(editMode);
 
     }
 
     /**
      * get all Neighbour properties
      * 
      * @return list of properties name
      */
     private List<String> getAllNeighbourEditableProperties() {
         List<String> result = new ArrayList<String>();
         if (gis == null || neighbour.getSelectionIndex() < 0) {
             return result;
         }
         String[] array = new PropertyHeader(gis).getNeighbourAllFields(neighbour.getText());
         return array == null ? result : Arrays.asList(array);
     }
 
     /**
      * get all Neighbour properties
      * 
      * @return list of properties name
      */
     private List<String> getNeighbourNumericProperties() {
         List<String> result = new ArrayList<String>();
         if (gis == null || neighbour.getSelectionIndex() < 0) {
             return result;
         }
         String[] array = new PropertyHeader(gis).getNeighbourNumericFields(neighbour.getText());
         return array == null ? result : Arrays.asList(array);
     }
 
     // private void hookContextMenu() {
     // MenuManager menuMgr = new MenuManager("#PopupMenu");
     // menuMgr.setRemoveAllWhenShown(true);
     // menuMgr.addMenuListener(new IMenuListener() {
     // public void menuAboutToShow(IMenuManager manager) {
     // NeighboursView.this.fillContextMenu(manager);
     // }
     // });
     // Menu menu = menuMgr.createContextMenu(viewer.getControl());
     // viewer.getControl().setMenu(menu);
     // getSite().registerContextMenu(menuMgr, viewer);
     // }
 
     // private void contributeToActionBars() {
     // IActionBars bars = getViewSite().getActionBars();
     // // fillLocalPullDown(bars.getMenuManager());
     // bars.getToolBarManager().removeAll();
     // fillLocalToolBar(bars.getToolBarManager());
     // bars.updateActionBars();
     // }
 
     // private void fillLocalPullDown(IMenuManager manager) {
     // manager.add(actionCommit);
     // manager.add(new Separator());
     // manager.add(actionRollback);
     // }
     //
     // private void fillContextMenu(IMenuManager manager) {
     // manager.add(actionCommit);
     // manager.add(actionRollback);
     // // Other plug-ins can contribute there actions here
     // manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
     // }
     //	
     // private void fillLocalToolBar(IToolBarManager manager) {
     // manager.add(actionCommit);
     // manager.add(actionRollback);
     // }
 
     // private void makeActions() {
     // actionCommit = new CommitAction();
     //		
     // actionRollback = new RollbackAction();
     //
     // }
     //
     // private void showMessage(String message) {
     // MessageDialog.openInformation(
     // viewer.getControl().getShell(),
     // "Neighbours",
     // message);
     // }
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
     /**
      * Sets Input of nodes
      * 
      * @param inputNodes input nodes
      */
     public void setInput(Collection<Node> inputNodes) {
         viewer.setInput(inputNodes);
     }
 
     /**
      * gets selected Neighbour node
      * 
      * @return node
      */
     private Node getNeighbour() {
         if (gis == null || neighbour.getSelectionIndex() < 0) {
             return null;
         }
         return NeoUtils.findNeighbour(gis, neighbour.getText());
     }
 
     /**
      * <p>
      * Support edit cells
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public class NeighbourEditableSupport extends EditingSupport {
 
         private String name;
         private Class valueClass;
         private CellEditor editor;
         private String value;
 
         /**
          * Constructoe
          * 
          * @param viewer
          * @param name
          * @param class1
          */
         public NeighbourEditableSupport(TableViewer viewer, String name, Class valueClass) {
             super(viewer);
             this.name = name;
             this.valueClass = valueClass;
             editor = new TextCellEditor(viewer.getTable());
         }
 
         @Override
         protected boolean canEdit(Object element) {
             return true;
         }
 
         @Override
         protected CellEditor getCellEditor(Object element) {
             return editor;
         }
 
         @Override
         protected Object getValue(Object element) {
             value = ((RelationWrapper)element).getRelation().getProperty(name, "").toString();
             return value;
         }
 
         @Override
         protected void setValue(Object element, Object value) {
             try {
                 if (this.value.equals(value)) {
                     return;
                 }
                 Object valueToSave;
                 if (valueClass.isAssignableFrom(Double.class)) {
                     valueToSave = Double.parseDouble(value.toString());
                 } else if (valueClass.isAssignableFrom(Integer.class)) {
                     valueToSave = Integer.parseInt(value.toString());
                 } else {
                     valueToSave = value.toString();
                 }
                 ((RelationWrapper)element).getRelation().setProperty(name, valueToSave);
                 updateDirty(true);
                 getViewer().update(element, null);
             } catch (NumberFormatException e) {
             }
 
         }
 
     }
 
     // public class CommitAction extends Action {
     // public CommitAction() {
     // super();
     // setText("Commit");
     // setImageDescriptor(NeoIcons.COMMIT_ENABLED.descriptor());
     // setDisabledImageDescriptor(NeoIcons.COMMIT_DISABLED.descriptor());
     // }
     //
     // @Override
     // public void run() {
     // NeighboursPlugin.getDefault().commit();
     // editMode = false;
     // viewer.refresh();
     // }
     //
     // @Override
     // public boolean isEnabled() {
     // return super.isEnabled() && editMode;
     // }
     // }
     //
     // public class RollbackAction extends Action {
     // public RollbackAction() {
     // super();
     // setText("Rollback");
     // setImageDescriptor(NeoIcons.ROLLBACK_ENABLED.descriptor());
     // setDisabledImageDescriptor(NeoIcons.ROLLBACK_DISABLED.descriptor());
     // }
     //
     // @Override
     // public void run()
     // {
     // NeighboursPlugin.getDefault().rollback();
     // editMode = false;
     // viewer.refresh();
     // }
     //
     // @Override
     // public boolean isEnabled() {
     // return super.isEnabled() && editMode;
     // }
     // }
 }

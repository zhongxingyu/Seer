 /*
  * @(#) $RCSfile: DefaultTableContext.java,v $ $Revision: 1.15 $ $Date: 2004/09/16 13:44:48 $ $Name:  $
  *
  * Center for Computational Genomics and Bioinformatics
  * Academic Health Center, University of Minnesota
  * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * see: http://www.gnu.org/copyleft/gpl.html
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  */
 
 
 package edu.umn.genomics.table;
 import edu.umn.genomics.component.SaveImage;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.TableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeModel;
 
 /*
   All returned values are interfaces so that they can be implemented as needed.
 */
 
 /**
  * DefaultTableContext manages TableModels and any subtables, views, or selections
  * related to those tables.  
  * The managed objects are maintained in a TreeModel that may be viewed by JTree.
  * @author       J Johnson
  * @version $Revision: 1.15 $ $Date: 2004/09/16 13:44:48 $  $Name:  $ 
  * @since        1.0
  */
 public class DefaultTableContext implements TableContext {
   DefaultSetOperator setOperator = new DefaultSetOperator(); 
   DefaultTreeModel dtm = new DefaultTreeModel(new DefaultMutableTreeNode("Tables"));
   Hashtable tm_vtm = new Hashtable();     // tm -> vtm
   Hashtable rowSelHash = new Hashtable(); // tm -> rsm
   Hashtable colSelHash = new Hashtable(); // tm -> csm
   Hashtable tblMapHash = new Hashtable(); // tm -> ColumnMaps
   Hashtable obj_nodeHt = new Hashtable(); // obj -> node
   Hashtable node_objHt = new Hashtable(); // node -> obj
   static Hashtable defaultViews = new Hashtable(); // viewName -> viewClass
   static Vector defaultViewList = new Vector();    // an ordered list of built in viewNames
   static Hashtable defaultViewIcons24 = new Hashtable(); // viewClass -> 16x16icon
   static Hashtable defaultViewIcons16 = new Hashtable(); // viewClass -> 24x24icon
   static {  // add the built in views
     try {
       String path = "edu/umn/genomics/table/view.properties";
       ClassLoader cl = DefaultTableContext.class.getClassLoader();
       Properties properties = new Properties();
       properties.load(cl.getResourceAsStream(path));
       setDefaultViews(properties);
     } catch(Exception ex) {
       ExceptionHandler.popupException(""+ex);
     }
   }
 
   private Hashtable views = (Hashtable)defaultViews.clone(); // viewName -> viewClass
   private Vector viewList = (Vector)defaultViewList.clone();  // an ordered list of viewNames
   private Hashtable viewIcons24 = (Hashtable)defaultViewIcons24.clone();
   private Hashtable viewIcons16 = (Hashtable)defaultViewIcons16.clone();
   // Locations and Sizes of frames 
   // so that recreated frames are placed in the previous location:
   Hashtable frameLoc = new Hashtable(); // Frame location
   Hashtable frameDim = new Hashtable(); // Frame size
   /** tracks the location and size of frames and stores values in Hashtables.
    * The title of the frame is used as the key for the Hashtables.
    */
   ComponentAdapter ca = new ComponentAdapter() {
     public void componentResized(ComponentEvent e) {
       if (e.getComponent() instanceof Frame) {
         Frame f = (Frame)e.getComponent();
         frameDim.put(f.getTitle(),f.getSize());
       }
     }
     public void componentMoved(ComponentEvent e) {
       if (e.getComponent() instanceof Frame) {
         Frame f = (Frame)e.getComponent();
         frameLoc.put(f.getTitle(),f.getLocation());
       }
     }
     public void componentShown(ComponentEvent e) {
       if (e.getComponent() instanceof Frame) {
         Frame f = (Frame)e.getComponent();
         frameDim.put(f.getTitle(),f.getSize());
         frameLoc.put(f.getTitle(),f.getLocation());
       }
     }
   };
 
   class ColumnMaps implements TableModelListener {
     TableModel tm = null;
     Vector  maps = null;
     ColumnMaps(TableModel tm) {
       this.tm = tm;
       maps = new Vector();
       maps.setSize(tm.getColumnCount());
       tm.addTableModelListener(this); 
     }
     public synchronized ColumnMap getColumnMap(int columnIndex) {
       ColumnMap cmap = null;
       if (columnIndex < tm.getColumnCount()) {
         if (columnIndex < maps.size()) {
           cmap = (ColumnMap)maps.get(columnIndex);
         } else {
           maps.setSize(tm.getColumnCount());
         }
         if (cmap == null) {
           if (tm instanceof TableColumnMap) {
             cmap = ((TableColumnMap)tm).getColumnMap(columnIndex);
           }
           if (cmap == null) {
             cmap = new DefaultColumnMap(tm, columnIndex);
           }
           cmap.setSelectionModel(getRowSelectionModel(tm));
           maps.set(columnIndex,cmap);
         }
       }
       return cmap;
     }
     public int getColumnIndex(ColumnMap cmap) {
       return maps.indexOf(cmap);
     }
     public void tableChanged(TableModelEvent e) {
       if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
 System.err.println(" >>>> ColumnMaps " + e.getSource());
         destroyMaps();
         maps.setSize(tm.getColumnCount());
 System.err.println(" <<<< ColumnMaps " + e.getSource());
       }
     }
     public void setSetOperator(int setop) {
       for (Iterator iter = maps.iterator(); iter.hasNext(); ) {
         ColumnMap cmap = (ColumnMap)iter.next();
         if (cmap != null) {
           cmap.setSetOperator(setop);
         }
       }
     }
     private synchronized void destroyMaps() {
       for (ListIterator li = maps.listIterator(); li.hasNext(); ) {
         ColumnMap cmap = (ColumnMap)li.next();
         if (cmap != null) {
           cmap.cleanUp();
         }
       }
       maps.clear();
     }
   };
 
   ChangeListener socl = new ChangeListener() {
     public void stateChanged(ChangeEvent e) {
       if (e.getSource() instanceof SetOperator) {
         setColumnMapSetOperator(((SetOperator)e.getSource()).getSetOperator());
       }
     }
   };
 
   /** Comparator for Set of TableModels. */
   Comparator objectComparator = new Comparator() {
     public int compare(Object o1, Object o2) {
       return o1 == o2 ? 0 : o1 == null ? -1 : o2 == null ? 1 : o1.hashCode() < o2.hashCode() ? -1 : 1;
     }
     public boolean equals(Object obj) {
       return this == obj;
     }
   };
 
   private void setColumnMapSetOperator(int setop) {
     for (Iterator iter = tblMapHash.values().iterator(); iter.hasNext(); ) {
       ColumnMaps maps = (ColumnMaps)iter.next();
       if (maps != null) {
         maps.setSetOperator(setop);
       }
     }
   }
 
 
   /**
    * Set the location and size of the given frame.
    * @param f the frame to size and position
    * @param x the default x location for the frame
    * @param y the default y location for the frame
    * @param w the default width for the frame
    * @param h the default height for the frame
    */
   private void setFrameBounds(Frame f, int x, int y, int w, int h) {
     if (f != null) {
       Point p = (Point)frameLoc.get(f.getTitle());
       f.setLocation(p!=null?p:new Point(x,y));
       Dimension d = (Dimension)frameDim.get(f.getTitle());
       f.setSize(d!=null?d:new Dimension(w,h));
       f.addComponentListener(ca);
     }
   }
 
   /** 
    * Set the default view resources.
    * The properties identifiers:
    *  identifiers=scatterplot cluster
    * 
    *  scatterplot.name=ScatterPlot
    *  scatterplot.class=edu.umn.genomics.table.ScatterPlotView
    *  scatterplot.icon24=edu/umn/genomics/table/Icons/scatterplot.gif
    *  scatterplot.icon16=edu/umn/genomics/table/Icons/scatterplot16.gif
    *  
    *  cluster.name=Cluster Rows
    *  cluster.class=edu.umn.genomics.table.cluster.ClusterView
    *  cluster.icon24=edu/umn/genomics/table/Icons/cluster.gif
    *  cluster.icon16=edu/umn/genomics/table/Icons/cluster16.gif
    *  cluster.classdependency=cern.colt.matrix.DoubleMatrix2D
    *
    * @param properties The list of view resources.
    */
   public static void setDefaultViews(Properties properties) {
     ClassLoader cl = DefaultTableContext.class.getClassLoader();
     String ids = properties.getProperty("identifiers");
     if (ids != null) {
       StringTokenizer st = new StringTokenizer(ids);
       while (st.hasMoreTokens()) {
         String id = st.nextToken();
         String className = properties.getProperty(id+".class");
         String depends = properties.getProperty(id+".classdependency");
         String libdepends = properties.getProperty(id+".libdependency");
         if (depends != null) {
           try {
             for (StringTokenizer stk = new StringTokenizer(depends); stk.hasMoreTokens(); ) {
               Class.forName(stk.nextToken());
             }
           } catch (ClassNotFoundException cnfex) {
             continue;
           }
         }
         if (libdepends != null) {
           String libName = "";
           try {
             for (StringTokenizer stk = new StringTokenizer(libdepends); stk.hasMoreTokens(); ) {
               libName = stk.nextToken();
               System.loadLibrary(libName);
             }
           } catch (UnsatisfiedLinkError err) {
             continue;
           } catch (SecurityException ex) {
             ExceptionHandler.popupException(""+ex);
             continue;
           } catch (Throwable t) {
             ExceptionHandler.popupException(""+t);
             continue;
           }
         }
         if (className != null) {
           String viewName = properties.getProperty(id+".name");
           if (viewName == null || viewName.length() < 1) {
             int idx = className.lastIndexOf('.');
             viewName = idx < 0 ? className : className.substring(idx+1);
           }
           try {
             Class theClass = Class.forName(className);
             if (edu.umn.genomics.table.TableModelView.class.isAssignableFrom(theClass)) {
               addDefaultViewClass(viewName, Class.forName(className));          
             }
             String icon = properties.getProperty(id+".icon24");
             if (icon != null) {
               defaultViewIcons24.put(theClass, new ImageIcon(cl.getResource(icon)));
             }
             icon = properties.getProperty(id+".icon16");
             if (icon != null) {
               defaultViewIcons16.put(theClass, new ImageIcon(cl.getResource(icon)));
             }
           } catch (Exception ex) {
             ExceptionHandler.popupException(""+ex);
           } catch (NoClassDefFoundError err) {
             ExceptionHandler.popupException(""+err);
           }
         }
       }
     }
   }
 
   /**
    * Register the viewName for the viewClass.
    * @param viewName The name this view is referred as.
    * @param viewClass The viewing class.
    */
   private static void addDefaultViewClass(String viewName, Class viewClass) {
     defaultViewList.remove(viewName);
     defaultViewList.add(viewName);
     defaultViews.put(viewName,viewClass);
   }
 
   /**
    * Register a TableModel viewing class. The view class must implement 
    * the edu.umn.genomics.table.TableModelView interface.
    * @param viewName The name this view is referred as.
    * @param className The fully qualified class name for the viewing class.
    */
   public void addViewClass(String viewName, String className) 
     throws ClassNotFoundException, ClassCastException, NullPointerException {
     addViewClass(viewName, Class.forName(className));
   }
 
   /**
    * Register a TableModel viewing class. The view class must implement 
    * the edu.umn.genomics.table.TableModelView interface.
    * @param viewName The name this view is referred as.
    * @param viewClass The viewing class.
    */
   public void addViewClass(String viewName, Class viewClass) 
          throws ClassCastException, NullPointerException {
     if (viewClass == null) {
       throw new NullPointerException();
     }
     if (edu.umn.genomics.table.TableModelView.class.isAssignableFrom(viewClass) || 
         javax.swing.JTable.class.isAssignableFrom(viewClass)) {
       viewList.remove(viewName);
       viewList.add(viewName);
       views.put(viewName,viewClass);
     } else {
       throw new ClassCastException(viewClass.toString() + " doesn't implement " + 
          edu.umn.genomics.table.TableModelView.class.toString());
     }
   }
 
   /**
    * Remove a registered view class.
    * @param viewName The name for the view to be removed.
    * @return the view class this name referred to.
    */
   public TableModelView removeViewClass(String viewName) {
     viewList.remove(viewName);
     return (TableModelView)views.remove(viewName);
   }
 
   /**
    * Remove all registered view classes.
    */
   public void removeAllViewClasses() {
     viewList.clear();
     views.clear();
   }
 
   /**
    * Register a view class icon 24x24 pixels.
    * @param viewClass The viewing class.
    * @param icon The icon for this viewing class.
    */
   public void setViewIcon24(Class viewClass, Icon icon) {
     viewIcons24.put(viewClass,icon);
   }
 
   /**
    * Register a view class icon 16x16 pixels.
    * @param viewClass The viewing class.
    * @param icon The icon for this viewing class.
    */
   public void setViewIcon16(Class viewClass, Icon icon) {
     viewIcons16.put(viewClass,icon);
   }
 
   /**
    * Get an icon for a view class.
    * @param viewClass The viewing class.
    * @return the icon for this view.
    */
   public Icon getViewIcon16(Class viewClass){
       Icon icon = getViewIcon(viewClass, viewIcons16);
       return icon;
   }
   
   public Icon getViewIcon(Class viewClass) {
     Icon icon = getViewIcon(viewClass, viewIcons24);
     // if (icon == null)
     //   icon = defaultViewIcon;
     return icon;
   }
 
   /**
    * Get an icon for a view class.
    * @param viewClass The viewing class.
    * @param viewClass The hash of Icons
    * @return the icon for this view.
    */
   private Icon getViewIcon(Class viewClass, Hashtable iconHash) {
     Icon icon = viewClass != null ? (Icon)iconHash.get(viewClass) : null;
     return icon;
   }
 
   /**
    * Get an icon for a view.
    * @param viewName The name this view is referred as.
    * @return the icon for this view.
    */
   public Icon getViewIcon(String viewName) {
     return getViewIcon((Class)views.get(viewName));
   }
 
   public Icon getViewIcon16(String viewName) {
     return getViewIcon16((Class)views.get(viewName));
   }
   /**
    * Get an icon for a view.
    * @param treeNode The tree node for the view.
    * @return the icon for this view.
    */
   public Icon getViewIcon(DefaultMutableTreeNode treeNode) {
     Object obj = node_objHt.get(treeNode);
     if (obj instanceof JFrame) {
       try {
         return getViewIcon((((JFrame)obj).getContentPane().getComponent(0).getClass()),viewIcons16);
       } catch (Exception ex) {
       }
     } else if  (obj instanceof TableModel) {
       TableModel tm = (TableModel)obj;
       for (; tm instanceof VirtualTableModel; tm = ((VirtualTableModel)tm).getTableModel());
       return getViewIcon(tm.getClass(),viewIcons16);
     }
     return getViewIcon(obj != null ? obj.getClass() : null,viewIcons16);
   }
 
   /**
    * Get a list of all registered view class names.
    * @return A list of all registered view class names.
    */
   public String[] getViewNames() {
     String viewNames[] = new String[viewList.size()];
     try {
       viewNames = (String[])viewList.toArray(viewNames);
     } catch(Exception ex) {
     }
     return viewNames;
   }
 
   /**
    * Return the View for the tree node, or null if the node does not represent a View.
    * @param treeNode The tree node for the view.
    * @return the view at this tree node.
    */
   public TableModelView getTableModelView(DefaultMutableTreeNode treeNode) {
     if (treeNode != null) {
       Object obj = node_objHt.get(treeNode);
       if (obj instanceof JFrame) {
         try {
           Component c = ((JFrame)obj).getContentPane().getComponent(0);
           if (c instanceof TableModelView) {
             return  (TableModelView)c;
           }
         } catch (Exception ex) {
         }
       }
     }
     return null;
   }
 
   /** 
    * Create a DefaultTableContext.
    */
   public DefaultTableContext() {
     setOperator.addChangeListener(socl);
   }
 
 
   /**
    * Retrieve the TreeNode for the given object, creating it 
    * if it doesn't already exist.
    * @param obj The object for which to get a tree node.
    * @return The tree node for the object.
    */
   private DefaultMutableTreeNode getNode(Object obj) {
     DefaultMutableTreeNode tn;
     tn = (DefaultMutableTreeNode)obj_nodeHt.get(obj);
     if (tn == null) {
       tn = new DefaultMutableTreeNode(obj);
       obj_nodeHt.put(obj,tn);
       node_objHt.put(tn,obj);
     }
     return tn;
   }
 
   /**
    * Add the child object as a child node to the parent object in the tree.
    * @param parent The parent object, if null add the child to the root node.
    * @param child The object to add as a child node in the tree.
    * @return The tree node of the child object.
    */
   private DefaultMutableTreeNode addNode(Object parent, Object child) {
     DefaultMutableTreeNode pn = parent == null 
                                 ? (DefaultMutableTreeNode)dtm.getRoot()
                                 : getNode(parent);
     DefaultMutableTreeNode cn = getNode(child);
     //System.err.println("addNode " +  pn + "\t" + cn);
     dtm.insertNodeInto(cn,pn,dtm.getChildCount(pn));
     return cn;
   }
 
   /**
    * Remove the object from the tree.
    * @param obj The object to remove from the tree.
    */
   private void removeNode(Object obj) {
     MutableTreeNode tn = (MutableTreeNode)obj_nodeHt.get(obj);
     removeNode(tn);
   }
 
   /**
    * Remove the node from the tree, destroying the object and all descendents.
    * @param tn The tree node to remove and destroy.
    */
   private void removeNode(MutableTreeNode tn) {
     if (tn != null) {
       if (!tn.isLeaf()) {
         for(int i = tn.getChildCount()-1;  i >= 0; i--) {
           MutableTreeNode cn = (MutableTreeNode)tn.getChildAt(i);
           removeNode(cn);
         }
       }
       //System.err.println("removeNode " +  tn);
       Object obj = node_objHt.get(tn);
       node_objHt.remove(tn);
       if (obj != null) {
         obj_nodeHt.remove(obj);
         if (obj instanceof JFrame) {
           ((JFrame)obj).dispose();
         } else if (obj instanceof TableModel) {
           TableModel tm = (TableModel)obj;
           VirtualTableModelProxy vtm = getTableModelProxy(tm);
           tm_vtm.remove(tm);
           if (vtm.getTableModel() != tm && tm_vtm.containsKey(vtm.getTableModel())) {
             if (tm_vtm.get(vtm.getTableModel()) == vtm) {
               tm_vtm.remove(vtm.getTableModel());
             }
           }
           rowSelHash.remove(vtm);
           colSelHash.remove(vtm);
           tblMapHash.remove(vtm);
         }
       }
       dtm.removeNodeFromParent(tn);
     }
   }
 
   /**
    * Get a VirtualTableModelProxy for the given TableModel.
    * If the given TableModel is a VirtualTableModelProxy, it will be 
    * registered and returned.
    * This routine is used to guarantee that all references to a TableModel
    * are the same.
    * @param tm the TableModel for which to return a VirtualTableModelProxy.
    * @return The VirtualTableModelProxy to which tm refers.
    */
   private VirtualTableModelProxy getTableModelProxy(TableModel tm) {
     if (tm == null) 
       return null;
     VirtualTableModelProxy vtm = (VirtualTableModelProxy)tm_vtm.get(tm);
     if (vtm == null) {
       vtm = tm instanceof VirtualTableModelProxy ? (VirtualTableModelProxy)tm 
                                                  : new VirtualTableModelProxy(tm);
       tm_vtm.put(tm,vtm); 
     }
     ColumnMaps maps = (ColumnMaps)tblMapHash.get(vtm);
     if (maps == null) {
       maps = new ColumnMaps(vtm);
       tblMapHash.put(vtm,maps);
     }
     return vtm;
   }
 
   /**
    * Get a VirtualTableModel for the given TableModel.
    * If the given TableModel is a VirtualTableModel, it will be 
    * registered and returned.
    * This routine is used to guarantee that all references to a TableModel
    * are the same.
    * @param tm the TableModel for which to return a VirtualTableModel.
    * @return The VirtualTableModel for the given tm.
    */
   public VirtualTableModel getVirtualTableModel(TableModel tm) {
     return getTableModelProxy(tm);
   }
 
   /**
    * Add a TableModel to the tree of managed tables.
    * @param tm The TableModel to add.
    */
   public void addTableModel(TableModel tm) {
     if (tm == null)
       return;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     DefaultMutableTreeNode tn = addNode(null, vtm);  
     tn.setAllowsChildren(true);
     //System.err.println("addTableModel:" + "\n\t" + tm  + "\n\t" + vtm );
   }
 
   /**
    * Remove a TableModel from the tree of managed tables.
    * @param tm The TableModel to remove.
    */
   public void removeTableModel(TableModel tm) {
     if (tm == null) 
       return;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     MutableTreeNode tn = getNode(vtm);
     removeNode(tn);
     rowSelHash.remove(vtm);
     colSelHash.remove(vtm);
     tblMapHash.remove(vtm);
     tm_vtm.remove(tm);
   }
 
   /**
    * Remove all TableModels from the tree of managed tables.
    */
   public void removeAllTableModels() {
     DefaultMutableTreeNode tn = (DefaultMutableTreeNode)dtm.getRoot();
     for(int i = tn.getChildCount()-1;  i >= 0; i--) {
       MutableTreeNode cn = (MutableTreeNode)tn.getChildAt(i);
       removeNode(cn);
     }
   }
 
   /**
    * Get a new TableModel that contains the selected rows in the given TableModel.
    * The selections will be mapped so that the selection of rows for one
    * TableModel will be reflected in the other TableModel.
    * The new TableModel is added as a child tree node to the given TableModel.
    * @param tm the TableModel from which to derive a new TableModel
    * @param rows the selected rows of the given TableModel to include in the new TableModel
    * @return The TableModel derived from the selected rows of the given TableModel.
    */
   public TableModel getTableModel(TableModel tm, ListSelectionModel rows) {
     if (tm == null || rows == null || rows.isSelectionEmpty()) 
       return null;
     VirtualTableModelProxy vtm = new VirtualTableModelProxy(tm);
     boolean useSelection = false;
     int count = tm.getRowCount();
     // Check if the entire table is selected
     if (rows.getMinSelectionIndex() == 0 && 
         rows.getMaxSelectionIndex() == tm.getRowCount()-1) {
       for (int i = tm.getRowCount()-1; i > 0; i--) {
         if (!rows.isSelectedIndex(i)) {
           useSelection = true;
           break;
         }
       } 
     } else {
       useSelection = true;
     }
     if (useSelection) {
       count = 0; 
       for (int i = rows.getMinSelectionIndex(); i <= rows.getMaxSelectionIndex(); i++) {
         if (rows.isSelectedIndex(i))
           count++;
       }
       LSMIndexMap map = new LSMIndexMap(rows,true,false);
       map.setSize(tm.getRowCount());
       vtm.setName("Selected " + count + " Rows of " + getTableModelProxy(tm).getName());
       vtm.setIndexMap(map);
       ListSelectionModel lsm1 = getRowSelectionModel(tm);
       ListSelectionModel lsm2 = getRowSelectionModel(vtm);
       // need to store this somewhere
       IndexMapSelection ims = new IndexMapSelection(lsm1,lsm2,map);
     } else { // If the entire table is selected forget the mapping
       rowSelHash.put(vtm,getRowSelectionModel(tm));
       vtm.setName("All " + count + " Rows of " + getTableModelProxy(tm).getName());
     }
     DefaultMutableTreeNode tn = addNode(tm, vtm);  
     tn.setAllowsChildren(true);
     return vtm;
   }
 
     @Override
   public TableModel getTableModel(TableModel tm, Partition partition) {
     if (tm == null || partition == null) 
       return null;
     VirtualTableModelProxy vtm = new VirtualTableModelProxy(tm);
     vtm.setIndexMap(partition.getPartitionIndexMap()); 
     ListSelectionModel lsm1 = getRowSelectionModel(tm);
     ListSelectionModel lsm2 = getRowSelectionModel(vtm);
     IndexMapSelection ims = new IndexMapSelection(lsm1,lsm2,vtm.getIndexMap());
     DefaultMutableTreeNode tn = addNode(tm, vtm);  
     tn.setAllowsChildren(true);
     return vtm;
   }
 
   /**
    * Display and manage a view of the given TableModel.
    * This does a combination of getView and addView.
    * @param tm
    * @param viewName The name of the type of view to create.
    * @return The view display component.
    */
   public JFrame getTableModelView(TableModel tm, String viewName) {
     if (tm == null) 
       return null;
     JComponent jc =  getView(tm, viewName);
     JFrame jf = addView(tm, viewName, jc);
     return jf;
   }
 
   /**
    * Create a view for the given TableModel.  
    * @param tm The TableModel to view.
    * @param viewName The name of the type of view to create.
    * @return A view compenent for the given TableModel.
    */
   public JComponent getView(TableModel tm, String viewName) {
     if (tm == null) 
       return null;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     JComponent jc = null;
     try {
       Class vc = (Class)views.get(viewName); 
       try {
         //Class pc[] = new Class[1];
         //pc[0] = Class.forName("edu.umn.genomics.table.TableModel");
         Constructor cons = vc.getConstructor((Class[])null);
         Object po[] = null; // new Object[0];
         jc = (JComponent)cons.newInstance((Object[])null);
         if (jc instanceof TableModelView) {
           ((TableModelView)jc).setTableContext(this);
           ((TableModelView)jc).setSelectionModel(getRowSelectionModel(vtm));
           ((TableModelView)jc).setTableModel(vtm);
         }
       } catch (Exception exc) {
         if (vc.isAssignableFrom(javax.swing.JTable.class)) {
           Class pc[] = new Class[1];
           pc[0] = Class.forName("javax.swing.table.TableModel");
           Constructor cons = vc.getConstructor(pc);
           Object po[] = new Object[1];
           po[0] = vtm;
           JTable jt = (JTable)cons.newInstance(po);
           jt.setSelectionModel(getRowSelectionModel(vtm));
           JScrollPane js = new JScrollPane(jt);
           jc = js;
         } else {
           System.err.println(viewName + "\t" + exc);
           exc.printStackTrace();
         }
       }
     } catch (Exception ex) {
         System.err.println(viewName + "\t" +ex);
     }
     return jc;
   }
 
   /**
    * Display and manage the view component. The component is added to a Frame.
    * The display frame is added as a child tree node of the TableModel. 
    * @param tm The TableModel for this view
    * @param viewName The name of the type of view to create.
    * @param jc The view compenent for the given TableModel.
    * @return The view display component.
    */
   public JFrame addView(TableModel tm, String viewName, JComponent jc) {
     if (tm == null) 
       return null;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     JFrame jf = null;
     try {
       if (jc != null) {
         jf = getViewFrame(viewName + " of " + vtm, jc);
         setViewToolBar(jf, jc); 
         DefaultMutableTreeNode tn = addNode(vtm, jf);
         tn.setAllowsChildren(false);
       }
     } catch (Exception ex) {
         System.err.println(viewName + "\t" +ex);
     }
     return jf;
   }
 
   /** 
    * Get an editor for a TableModel.  
    * @param tm The TableModel to edit.
    * @return A component that displays the editor.
    */
   public JFrame getEditorFrame(TableModel tm) {
     if (tm == null) 
       return null;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     JFrame jf = null;
     if (vtm != null) {
       VirtualTableModelView vtv = new VirtualTableModelView(vtm);
       jf = getViewFrame("Edit " + vtm, vtv);
       setViewToolBar(jf, vtv);
       DefaultMutableTreeNode tn = addNode(tm, jf);
       tn.setAllowsChildren(false);
     }
     return jf;
   }
 
   /**
    * Place the given component into a frame, make it visible, 
    * track the frame's location and size, and listen for window 
    * events so that the tree can be maintained.
    * @param title The title for the JFrame
    * @param view The compenent to be placed in the frame to be viewed.
    * @return The frame created.
    */
   public JFrame getViewFrame(String title, final JComponent view) {
     JFrame frame = new JFrame(title) {
       public String toString() {
         return getTitle();
       }
     };
     frame.addWindowListener(new WindowAdapter() {
       final JComponent _view = view;
       private void doClose(WindowEvent e) {
         removeNode(e.getSource());
         if (_view instanceof CleanUp) {
            ((CleanUp)_view).cleanUp();
         }
       }
       public void windowClosing(WindowEvent e) {
         doClose(e);
       }
       public void windowClosed(WindowEvent e) {
         doClose(e);
       }
     });
     frame.getContentPane().add(view,BorderLayout.CENTER);
     frame.pack();
     Insets insets = frame.getInsets(); 
     int sw = Toolkit.getDefaultToolkit().getScreenSize().width/10*8;
     int sh = Toolkit.getDefaultToolkit().getScreenSize().height/10*8;
     Dimension d = frame.getSize();
     int pw = Math.min(sw,Math.max(d.width,sw/10*4))
            - insets.left - insets.right;
     int ph = Math.min(sh,Math.max(d.height,sh/10*4))
            - insets.top - insets.bottom;
     setFrameBounds(frame,(int)((sw-pw)/2),(int)((sh-ph)/2), pw, ph);
     //System.err.println(frame + "\n" + sw + " " + sh + "   " + pw + " " + ph + " " + insets);
     frame.setVisible(true);
     return frame;
   }
 
 
   /** 
    * Add a standard toolbar with close and saveimage buttons to the frame.
    * The save button will create an image of the given view Component.
    * The frame must have BorderLayout, as the toolbar placed in the 
    * BorderLayout.NORTH location. 
    * @param frame The frame to which the toolbar is to be added.
    * @param view The view to be saved as an image.
    */
   public static void setViewToolBar(Window frame, Component view) {
     setViewToolBar(frame, view, null);
   }
 
   /** 
    * Add a standard toolbar with close and saveimage buttons to the frame.
    * The save button will create an image of the given view Component.
    * The frame must have BorderLayout, as the toolbar placed in the 
    * BorderLayout.NORTH location. 
    * @param frame The frame to which the toolbar is to be added.
    * @param view The view to be saved as an image.
    * @param extraButtons Extra buttons to be added to the toolbar.
    */
   public static void setViewToolBar(Window frame, Component view, JComponent extraButtons[]) {
     final Component comp = view;
     final Window theframe = frame;
     JButton jBtn;
     JToolBar jtb = new JToolBar();
     // Close
     jBtn = new JButton("Close");
     jBtn.addActionListener(
       new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           WindowEvent we = new WindowEvent(theframe,WindowEvent.WINDOW_CLOSING);
           WindowListener[] wl = theframe.getWindowListeners(); 
           if (wl != null) {
             for (int i = 0; i < wl.length; i++) {
               wl[i].windowClosing(we);
             }
           }
           theframe.dispose();
         }
       }
     );
     jBtn.setToolTipText("Close this view");
     jtb.add(jBtn);
     if (view != null) {
 
       // Save Image
       jBtn = new JButton("Save Image");
       if (System.getProperty("java.specification.version").compareTo("1.4")>=0) {
         jBtn.setToolTipText("Save this view as an image");
         jBtn.addActionListener(
           new ActionListener() {
             public void actionPerformed(ActionEvent e) {
               try {
                 SaveImage.saveImage(comp instanceof TableModelView ? ((TableModelView)comp).getCanvas() : comp);
               } catch (IOException ioex) {
                 ExceptionHandler.popupException(""+ioex);
               }
             }
           }
         );
       } else {
         jBtn.setToolTipText("Save Image requires Java 1.4");
         jBtn.setEnabled(false);
       }
       jtb.add(jBtn);
 
       // Save PDF
       if (true) {
         jBtn = new JButton("Save PDF");
         try {
          Class.forName("com.itextpdf.text.pdf.PdfWriter");
           jBtn.setToolTipText("Save this view as a PDF file");
           jBtn.addActionListener(
             new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                 try {
                  Class[] paramClass = new Class[1];
                  paramClass[0] = java.awt.Component.class;
                  Object[] args = new Object[1];
                  args[0] = comp instanceof TableModelView 
                            ? ((TableModelView)comp).getCanvas() 
                            : comp;
                  Class.forName("edu.umn.genomics.component.SavePDF").
                     getMethod("savePDF",paramClass).invoke(null,args);
                 } catch (Exception ex) {
                   ExceptionHandler.popupException(""+ex);
                 }
               }
             }
           );
         } catch(ClassNotFoundException cnfex) {
           jBtn.setToolTipText("Save PDF requires the Lowagie iText package");
           jBtn.setEnabled(false);
         }
         jtb.add(jBtn);
       }
 
     }
     if (extraButtons != null) {
       for (int i = 0; i < extraButtons.length; i++) {
         jtb.add(extraButtons[i]);
       }
     }
     if (frame instanceof JFrame) {
       ((JFrame)theframe).getContentPane().add( jtb, BorderLayout.NORTH);
     } else if (frame instanceof JDialog) {
       ((JDialog)theframe).getContentPane().add( jtb, BorderLayout.NORTH);
     } else {
       theframe.add( jtb, BorderLayout.NORTH);
     }
     theframe.validate();
   }
 
 
   /**
    * Get the row selection model for the given TableModel.
    * @param tm The TableModel for which to get the selection model.
    * @return The selection model for this TableModel.
    */
   public ListSelectionModel getRowSelectionModel(TableModel tm) {
     return getSelectionModel(tm, rowSelHash);
   }
 
   /**
    * Get the column selection model for the given TableModel.
    * @param tm The TableModel for which to get the selection model.
    * @return The selection model for this TableModel.
    */
   public ListSelectionModel getColumnSelectionModel(TableModel tm) {
     return getSelectionModel(tm, colSelHash);
   }
 
   /**
    * Get the selection model for the given TableModel from the given Hashtable.
    * @param tm The TableModel for which to get the selection model.
    * @param ht The Hashtable in which to store the ListSelectionModel.
    * @return The selection model for this TableModel.
    */
   private ListSelectionModel getSelectionModel(TableModel tm, Hashtable ht) {
     ListSelectionModel lsm = null;
     if (tm != null) {
       VirtualTableModelProxy vtm = getTableModelProxy(tm);
       lsm = (ListSelectionModel)ht.get(vtm);
       if (lsm == null) {
         lsm = new DefaultListSelectionModel();
         lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         ht.put(vtm,lsm);
       }
     }
     return lsm;
   }
 
   /**
    * Return a ColumnMap for the given TableModel column.
    * @param tm The TableModel for which to get the map.
    * @param columnIndex The index of the column to map.
    * @return The ColumnMap for the given column of the TableModel
    * @see edu.umn.genomics.table.ColumnMap
    */
   public ColumnMap getColumnMap(TableModel tm, int columnIndex) {
     if (tm == null) 
       return null;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     // Need a hashtable of these 
     ColumnMaps maps = (ColumnMaps)tblMapHash.get(vtm);
     if (maps == null) {
       maps = new ColumnMaps(vtm);
       tblMapHash.put(vtm,maps);
     }
     return maps.getColumnMap(columnIndex);
     // or TableModel provides them
   }; 
 
   /**
    * Return the column index in the given TableModel that this ColumnMap represents.
    * @param tm The TableModel for which to get the index.
    * @param columnMap The ColumnMap for which to find the TableModel column index.
    * @return The column index in the TableModel that ColumnMap represents.
    * @see edu.umn.genomics.table.ColumnMap
    */
   public int getColumnIndex(TableModel tm, ColumnMap columnMap) {
     if (tm == null)
       return -1;
     VirtualTableModelProxy vtm = getTableModelProxy(tm);
     // Need a hashtable of these
     ColumnMaps maps = (ColumnMaps)tblMapHash.get(vtm);
     if (maps != null) {
       return maps.getColumnIndex(columnMap);
     }
     return -1;
   };
 
   /**
    * Get a TreeModel representation of the TableModels and views managed.
    * @return A tree of the managed TableModels and views.
    */
   public TreeModel getTreeModel() {
     return dtm;
   }
 
   /**
    * Get an array of the TableModels being managed.
    * @return An array of the managed TableModels.
    */
   public TableModel[] getTableModels() {
     Collection tms = tm_vtm.values();
     TableModel tma[] = new TableModel[tms.size()];
     return (TableModel[])tms.toArray(tma);
   }
 
   /**
    * Get the Set of the TableModels, including VirtualTableModels, that are being managed.
    * @return A Set of the managed TableModels.
    */
   public Set getTableModelList() {
     TreeSet ts = new TreeSet(objectComparator);
     ts.addAll(tm_vtm.values());
     ts.addAll(tm_vtm.keySet());
     return ts;
   }
 
   /**
    * Tests if the given TableModel is being managed.
    * @return Whether the given TableModel is being managed.
    */
   public boolean hasTableModel(TableModel tm) {
     return tm == null ? false : tm_vtm.containsKey(tm) || tm_vtm.contains(tm);
   }
 
   /**
    * Return the SetOperator context for set operations on selections.
    * @param tm The TableModel for which to retrive the SetOperator.
    * @return The selection SetOperator context for the TableModel.
    */
   public SetOperator getSetOperator(TableModel tm) {
     return setOperator;
   }
 }
 
 class DbgDefaultListSelectionModel extends DefaultListSelectionModel {
   public void addListSelectionListener(ListSelectionListener l) {
     System.err.println(" +++ " + l + " >>> " + getListSelectionListeners().length + " " + this.hashCode());
     // super.addListSelectionListener(ListenerRefFactory.getListener(l));
     super.addListSelectionListener(l);
     printList();
   }
   public void removeListSelectionListener(ListSelectionListener l) {
     super.removeListSelectionListener(l);
     System.err.println(" --- " + l + " <<< "  + getListSelectionListeners().length + " " + this.hashCode());
     printList();
   }
   public void printList() {
     ListSelectionListener[] list = getListSelectionListeners();
     for (int i = 0; i < list.length; i++) {
       if (list[i] != null) {
         System.err.println("\t" + i + "\t" + list[i]);
       }
     }
   }
 }

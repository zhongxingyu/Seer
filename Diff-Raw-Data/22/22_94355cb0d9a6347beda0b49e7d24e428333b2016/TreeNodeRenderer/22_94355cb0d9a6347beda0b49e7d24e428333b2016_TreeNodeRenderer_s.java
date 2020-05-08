 package org.geworkbench.builtin.projects;
 
 import java.awt.Component;
 
 import javax.swing.ImageIcon;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeCellRenderer;
 
 import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  * <p/>
  * <code>JTree</code> renderer to be used to render the Project Tree
  *
  * @author First Genetic Trust
  * @version $Id$
  */
 public class TreeNodeRenderer extends DefaultTreeCellRenderer {
 	private static final long serialVersionUID = -1879887785935786137L;
 
     /**
      * Current <code>ProjectNodeOld</code> selection
      */
     public ProjectNode projectNodeSelection = null;
     /**
      * Current <code>ImageNode</code> selection
      */
     public ImageNode imageNodeSelection = null;
 
     /**
      * Default Constructor
      */
     ProjectSelection selection = null;
 
     public TreeNodeRenderer(ProjectSelection selection) {
         this.selection = selection;
     }
 
     /**
      * Clears all Node selections
      */
     public void clearNodeSelections() {
         projectNodeSelection = null;
         imageNodeSelection = null;
     }
 
     /**
      * <code>Component</code> used for rendering on the <code>ProjectTree</code>
      * based on type and selection
      *
      * @param tree     the <code>ProjectTree</code>
      * @param value    the node to be rendered
      * @param sel      if selected
      * @param expanded if node expanded
      * @param leaf     if leaf
      * @param row      row in the tree model
      * @param hasFocus if the node has focus
      * @return the <code>Component</code> to be used for rendering
      */
     @SuppressWarnings("unchecked")
 	@Override
     public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 
         if (value == tree.getModel().getRoot()) {
             setIcon(Icons.WORKSPACE_ICON);
             setToolTipText("This is the workspace.");
         } else if (value.getClass() == ProjectNode.class) {
             setIcon(Icons.PROJECT_ICON);
             setToolTipText("This is a project folder.");
         } else if(value.getClass() == PendingTreeNode.class){
         	setIcon(Icons.BUSY_STATIC_ICON);
         } else {
             if (value.getClass() == DataSetNode.class) {
                 DSDataSet<? extends DSBioObject> df = ((DataSetNode) value).dataFile;
                 ImageIcon icon = ProjectPanel.getIconForType(df.getClass());
                 if (icon != null) {
                     setIcon(icon);
                 } else {
                     setIcon(Icons.MICROARRAYS_ICON);
                 }
                 String[] descriptions = df.getDescriptions();
                 if (df != null && (df instanceof DSMicroarraySet)){
                 	DSMicroarraySet<? extends DSMicroarray> microarraySet = (DSMicroarraySet<? extends DSMicroarray>)df;
                     setToolTipText("# of microarrays: " +
                             microarraySet.size() + ",   " +
                             "# of markers: " +
                             microarraySet.getMarkers().size() + "\n");
                 }
                 else if (descriptions.length > 0) {
                     setToolTipText(descriptions[0]);
                 } else {
                     setToolTipText("This is an undefined Data set");
                 }
             } else if (value.getClass() == DataSetSubNode.class) {
                DSAncillaryDataSet<? extends DSBioObject> adf = ((DataSetSubNode) value)._aDataSet;
                 ImageIcon icon = ProjectPanel.getIconForType(adf.getClass());
                 if (icon != null) {
                     setIcon(icon);
                 } else {
                     setIcon(Icons.DATASUBSET_ICON);
                 }
                 String[] descriptions = adf.getDescriptions();
                 if (descriptions.length > 0) {
                     setToolTipText("This is a Ancillary Data set: " + descriptions[0]);
                } else {
                    setToolTipText("This is an undefined Ancillary Data set");
                }
             } else if (value.getClass() == ImageNode.class) {
                 setIcon(Icons.IMAGE_ICON);
             }
         }
 
         return this;
     }
 
 }

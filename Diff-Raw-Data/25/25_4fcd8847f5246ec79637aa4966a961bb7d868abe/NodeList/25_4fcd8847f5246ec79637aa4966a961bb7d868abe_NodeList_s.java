 /*
  * A JList filled with ISGCINodes
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodeList.java,v 2.0 2011/09/25 12:37:13 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.view.gui;
 
 import java.awt.Component;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.ListCellRenderer;
 
 import teo.isgci.data.gc.GraphClass;
 
 
 /**
  * A JList filled with ISGCINodes, that are displayed with formatted html.
  */
 public class NodeList extends JList {
     LatexGraphics latex;
 
 
     /**
      * Create a new list with the given nodes in it.
      */
     public NodeList(LatexGraphics latex, Vector nodes) {
         super(nodes);
         init(latex);
     }
 
 
     /**
      * Create a new list.
      */
     public NodeList(LatexGraphics latex) {
         super();
         init(latex);
     }
 
     protected void init(LatexGraphics latex) {
         this.latex = latex;
         setCellRenderer(new NodeListCellRenderer());
         setFont(latex.getFont());
     }
 
 
     /**
      * Return the number of nodes in this list.
      */
     public int getElementCount() {
         return getModel().getSize();
     }
 
 
     /**
      * Replace all nodes in this list by the given data.
      */
     public void setListData(Iterator data) {
     	int error = 0;
         Vector v = new Vector();
         while (data.hasNext()) {
         	GraphClass gc = (GraphClass)data.next(); 
         	if (gc != null) { 
         		v.add(gc);
         	} else {
         		error++;
         	}
         }
         setListData(v);
         if (error > 0) {
         	JOptionPane.showMessageDialog(null,  "The search returned "
         			+ error + " GraphClasses\n"
         			+ "that are not part of your local copy.\n"
         			+ "Please update your database."
         			, "Attention", JOptionPane.OK_CANCEL_OPTION);
         }
     }
 
 
     /**
      * Replace all nodes in this list by the given data.
      */
     public void setListData(Collection data) {
         Vector v = new Vector(data);
         setListData(v);
     }
 
 
     /**
      * Return the smallest selected node.
      */
     public GraphClass getSelectedNode() {
         return (GraphClass) getSelectedValue();
     }
 
 
     /**
      * The renderer for nodes: We use a label with html code in it.
      */
     protected class NodeListCellRenderer implements ListCellRenderer {
         public Component getListCellRendererComponent(
                 JList list, Object value, int index,
                 boolean isSelected, boolean cellHasFocus) {
        	if (value == null) {
        		return latex.newLabel("");
        	}
        	System.out.println(((GraphClass) value).toString());
             LatexLabel label =
                     latex.newLabel(((GraphClass) value).toString());
             if (isSelected) {
                 label.setBackground(list.getSelectionBackground());
                 label.setForeground(list.getSelectionForeground());
             } else {
                 label.setBackground(list.getBackground());
                 label.setForeground(list.getForeground());
             }
             label.setEnabled(list.isEnabled());
             label.setFont(list.getFont());
             //setOpaque(this);
             return label;
         }
     }
 }
 
 /* EOF */

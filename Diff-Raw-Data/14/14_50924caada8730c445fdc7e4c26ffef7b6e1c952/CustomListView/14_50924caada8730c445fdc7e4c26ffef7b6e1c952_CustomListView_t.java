 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.demo.filebrowser.explorer.view;
 
 import java.awt.Color;
 import java.awt.Component;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import org.openide.explorer.view.ListView;
 import org.openide.explorer.view.Visualizer;
 import org.openide.nodes.Node;
 
 /**
  *
  * @author ECO
  */
 public class CustomListView extends ListView {
 
     private static class ListCellRendererImpl implements ListCellRenderer {
 
         public ListCellRendererImpl() {
         }
 
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 
             Node node = Visualizer.findNode(value);
 
            ListViewPanel label = new ListViewPanel(node.getDisplayName());
             label.setOpaque(true);
                 label.setBackground(new Color(236, 243, 254));
             if (isSelected) {
                 label.setBackground(Color.YELLOW);
             }
             return label;
 
 //            File file=node.getLookup().lookup(File.class);
 //            ListViewPanel cell=new ListViewPanel(file);
 //            cell.setBackground(Color.CYAN);
 //            if(isSelected){
 //                cell.setBackground(Color.YELLOW);
 //            }
 //            return cell;
 
         }
     }
     private JList jList;
 
     @Override
     protected JList createList() {
         jList = super.createList();
         jList.setOpaque(true);
         jList.setBackground(getBackground());
         jList.setForeground(getForeground());
         jList.setCellRenderer(new ListCellRendererImpl());
         return jList;
     }
 
     @Override
     public void setBackground(Color bg) {
         super.setBackground(bg);
         if (jList != null) {
             jList.setBackground(bg);
         }
     }
 
     @Override
     public void setForeground(Color fg) {
         super.setForeground(fg);
         if (jList != null) {
             jList.setForeground(fg);
         }
     }
 
     @Override
     public void setOpaque(boolean isOpaque) {
         super.setOpaque(isOpaque);
         if (jList != null) {
             jList.setOpaque(isOpaque);
         }
     }
 }

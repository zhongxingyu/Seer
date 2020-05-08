 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tdc.gui.entidades;
 
 import java.awt.Color;
 import java.awt.Component;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 import tdc.entidades.DataInput;
 import tdc.entidades.DataInputCatalog;
 
 /**
  *
  * @author facundo
  */
 public class MyColorCellRenderer implements TableCellRenderer{
    private static final DefaultTableCellRenderer default_cell_renderer = new DefaultTableCellRenderer();
     private DataInputCatalog data_input;
     public MyColorCellRenderer(DataInputCatalog data_input){
         this.data_input=data_input;
     }
 
     public Component getTableCellRendererComponent(JTable jtable, Object value,boolean isSelected, boolean hasFocus, int row, int column)  {
         if(isSelected){
            return default_cell_renderer.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row, column);
         }
        Component renderer = default_cell_renderer.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row, column);
         ((JLabel) renderer).setOpaque(true);
         try{
             DataInput di = data_input.get(row);
             renderer.setBackground(di.getColor());
             renderer.setForeground(Color.BLACK);
             return renderer;
        }catch(Throwable t){
            System.out.println("some error: "+t.getMessage());
            return default_cell_renderer.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row, column);
         }
     }
 }

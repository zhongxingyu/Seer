 package at.ac.tuwien.sepm.ui.metaLva;
 
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.ui.UI;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.util.List;
 
 /**
  * Author: Lena Lenz
  */
 @UI
 public class MetaLVATable extends JTable{
     List<MetaLVA> lvas;
     int[] colWidth = new int[]{60,30,420,40};
     int width = colWidth[0]+colWidth[1]+colWidth[2]+colWidth[3];
     DefaultTableModel model;
     public MetaLVATable(int width){
         init(width);
     }
     public MetaLVATable(List<MetaLVA> lvas,int width){
         this.lvas = lvas;
         init(width);
         setMetaLVAs(lvas);
     }
     public MetaLVATable(List<MetaLVA> lvas){
         this.lvas = lvas;
         init(this.width);
         setMetaLVAs(lvas);
     }
     public int getColWidth(int index){
         return colWidth[index];
     }
     private void init(int width){
         model = new DefaultTableModel(new String[]{"Nr","Typ","Name","ECTS"},0){
             @Override
             public boolean isCellEditable(int row,int col){
                 return false;
             }
         };
         setModel(model);
         colWidth[2] = Math.max(0,colWidth[2]+(width-this.width));
 
         for(int i=0;i<4;i++){
             if(i!=2){
                 getColumnModel().getColumn(i).setMinWidth(colWidth[i]);
                 getColumnModel().getColumn(i).setMaxWidth(colWidth[i]);
             }else{
                 getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
             }
         }
     }
 
     public void setMetaLVAs(List<MetaLVA> lvas){
         for(MetaLVA lva: lvas){
             model.addRow(new String[]{lva.getNr(),lva.getType().toString(),lva.getName(),""+lva.getECTS()});
         }
         this.lvas=lvas;
     }
 
     public void refreshMetaLVAs(List<MetaLVA> list) {
         model.setRowCount(0);
         this.setMetaLVAs(list);
     }
 
     public MetaLVA getSelectedMetaLVA(){
         return lvas.get(getSelectedRow());
     }
 
     public void removeSelectedMetaLVA() {
         lvas.remove(getSelectedRow());
         model.removeRow(getSelectedRow());
     }
 }
 

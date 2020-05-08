 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.PedFile;
 
 import javax.swing.*;
 import java.util.Vector;
 
 public class TDTPanel extends JPanel {
 
     Vector result;
     JTable table;
     Vector tableColumnNames = new Vector();
     private int type;
 
    public TDTPanel(PedFile pf, int t) {
         type = t;
         if (type == 1){
             result = TDT.calcTrioTDT(pf);
         }else{
             result = TDT.calcCCTDT(pf);
         }
 
         tableColumnNames.add("#");
         tableColumnNames.add("Name");
         if (type == 1){
             tableColumnNames.add("Overtransmitted");
             tableColumnNames.add("T:U");
         }else{
             tableColumnNames.add("Major Alleles");
             tableColumnNames.add("Case, Control Ratios");
         }
         tableColumnNames.add("Chi Squared");
         tableColumnNames.add("p value");
 
         refreshTable();
     }
 
     public void refreshNames() {
         for (int i = 0; i < table.getRowCount(); i++){
             table.setValueAt(Chromosome.getUnfilteredMarker(i).getName(),i,1);
         }
     }
 
 	public JTable getTable(){
 		return table;
 	}
 
     public void refreshTable(){
         this.removeAll();
         Vector tableData = new Vector();
 
         int numRes = Chromosome.getSize();
         for (int i = 0; i < numRes; i++){
             Vector tempVect = new Vector();
             TDTResult currentResult = (TDTResult)result.get(Chromosome.realIndex[i]);
             tempVect.add(new Integer(Chromosome.realIndex[i]+1));
             tempVect.add(currentResult.getName());
             tempVect.add(currentResult.getOverTransmittedAllele(type));
             tempVect.add(currentResult.getTURatio(type));
             tempVect.add(new Double(currentResult.getChiSq(type)));
             tempVect.add(currentResult.getPValue());
 
             tableData.add(tempVect.clone());
         }
 
         table = new JTable(tableData,tableColumnNames);
         table.getColumnModel().getColumn(0).setPreferredWidth(50);
         table.getColumnModel().getColumn(1).setPreferredWidth(100);
         if (type != 1){
             table.getColumnModel().getColumn(3).setPreferredWidth(160);
         }
         table.getColumnModel().getColumn(2).setPreferredWidth(100);
 
         JScrollPane tableScroller = new JScrollPane(table);
         add(tableScroller);
 
     }
 }
 
 
 
 

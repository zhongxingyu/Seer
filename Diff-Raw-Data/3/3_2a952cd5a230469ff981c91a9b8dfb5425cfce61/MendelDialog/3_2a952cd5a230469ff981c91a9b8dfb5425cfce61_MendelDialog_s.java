 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.MarkerResult;
 import edu.mit.wi.pedfile.MendelError;
 
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.util.Vector;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 
 
 /**
  * Custom Dialog showing Mendel Errors
  *
  * this class is not thread safe (untested).
  * modified version of IndividualsDialog
  * @author David Bender
  */
 
 
 
 
 public class MendelDialog extends JDialog implements ActionListener, Constants {
     private BasicTableModel tableModel;
 
 
     public MendelDialog (HaploView h, String title) {
         super(h,title);
 
         JPanel contents = new JPanel();
         JTable table;
 
         contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
 
         Vector results = h.theData.getPedFile().getResults();
 
         Vector colNames = new Vector();
         colNames.add("FamilyID");
         colNames.add("ChildID");
         colNames.add("Marker");
         colNames.add("Position");
         Vector data = new Vector();
 
         for(int i=0;i<results.size();i++) {
             MarkerResult currentResult = (MarkerResult)results.get(i);
             if (currentResult.getMendErrNum() > 0){
                Vector tmpVec = new Vector();
                 Vector mendelErrors = currentResult.getMendelErrors();
                 for (int j = 0; j < mendelErrors.size(); j++){
                     MendelError error = (MendelError)mendelErrors.get(j);
                     tmpVec.add(error.getFamilyID());
                     tmpVec.add(error.getChildID());
                     tmpVec.add(Chromosome.getUnfilteredMarker(i).getDisplayName());
                     tmpVec.add(new Long(Chromosome.getUnfilteredMarker(i).getPosition()));
                     data.add(tmpVec);
                 }
             }
         }
 
         TableSorter sorter = new TableSorter(new BasicTableModel(colNames, data));
         table = new JTable(sorter);
         sorter.setTableHeader(table.getTableHeader());
         table.getColumnModel().getColumn(2).setPreferredWidth(30);
 
         JScrollPane tableScroller = new JScrollPane(table);
         int tableHeight = (table.getRowHeight()+table.getRowMargin())*(table.getRowCount()+2);
         if (tableHeight > 300){
             tableScroller.setPreferredSize(new Dimension(400, 300));
         }else{
             tableScroller.setPreferredSize(new Dimension(400, tableHeight));
         }
         tableScroller.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
 
         contents.add(tableScroller);
 
         JButton okButton = new JButton("Close");
         okButton.addActionListener(this);
         okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
         contents.add(okButton);
         setContentPane(contents);
 
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
     }
 
     public MendelDialog(HaploData hd){
         Vector results = hd.getPedFile().getResults();
 
         Vector colNames = new Vector();
         colNames.add("FamilyID");
         colNames.add("ChildID");
         colNames.add("Marker");
         colNames.add("Position");
         Vector data = new Vector();
 
         for(int i=0;i<results.size();i++) {
             MarkerResult currentResult = (MarkerResult)results.get(i);
             if (currentResult.getMendErrNum() > 0){
                 Vector tmpVec = new Vector();
                 Vector mendelErrors = currentResult.getMendelErrors();
                 for (int j = 0; j < mendelErrors.size(); j++){
                     MendelError error = (MendelError)mendelErrors.get(j);
                     tmpVec.add(error.getFamilyID());
                     tmpVec.add(error.getChildID());
                     tmpVec.add(Chromosome.getUnfilteredMarker(i).getDisplayName());
                     tmpVec.add(new Long(Chromosome.getUnfilteredMarker(i).getPosition()));
                     data.add(tmpVec);
                 }
             }
         }
 
         tableModel = new BasicTableModel(colNames, data);
     }
 
     public void printTable(File outfile) throws IOException {
         FileWriter checkWriter = null;
         if (outfile != null){
             checkWriter = new FileWriter(outfile);
         }
 
         int numCols = tableModel.getColumnCount();
         StringBuffer header = new StringBuffer();
         for (int i = 0; i < numCols; i++){
             header.append(tableModel.getColumnName(i)).append("\t");
         }
         header.append("\n");
 
         if (outfile != null){
             checkWriter.write(header.toString());
         }else{
             System.out.print(header.toString());
         }
         for (int i = 0; i < tableModel.getRowCount(); i++){
             StringBuffer sb = new StringBuffer();
             for (int j = 0; j < numCols; j++){
                 sb.append(tableModel.getValueAt(i,j)).append("\t");
             }
             sb.append("\n");
 
             if (outfile != null){
                 checkWriter.write(sb.toString());
             }else{
                 System.out.print(sb.toString());
             }
         }
         if (outfile != null){
             checkWriter.close();
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if(command.equals("Close")) {
             this.dispose();
         }
     }
 }

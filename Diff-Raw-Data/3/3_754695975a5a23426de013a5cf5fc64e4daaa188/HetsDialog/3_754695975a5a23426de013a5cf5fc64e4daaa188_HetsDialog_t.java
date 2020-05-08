 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.util.Vector;
 import java.util.StringTokenizer;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 
 
 /**
  * Custom Dialog showing Male Heterozygotes
  *
  * this class is not thread safe (untested).
  * modified version of MendelDialog
  * @author David Bender
  */
 
 
 
 
 public class HetsDialog extends JDialog implements ActionListener, Constants {
     private BasicTableModel tableModel;
 
 
     public HetsDialog (HaploView h, String title) {
         super(h,title);
 
         JPanel contents = new JPanel();
         JTable table;
 
         contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
 
         Vector results = h.theData.getPedFile().getHaploidHets();
 
         Vector colNames = new Vector();
         colNames.add("FamilyID");
         colNames.add("IndividualID");
         colNames.add("Marker");
         Vector data = new Vector();
 
         for(int i=0;i<results.size();i++) {
             StringTokenizer st = new StringTokenizer((String)results.get(i));
             Vector tmpVec = new Vector();
             tmpVec.add(st.nextToken());
             tmpVec.add(st.nextToken());
             tmpVec.add(Chromosome.getUnfilteredMarker(Integer.parseInt(st.nextToken())).getDisplayName());
             data.add(tmpVec);
 
         }
 
         tableModel = new BasicTableModel(colNames,data);
         TableSorter sorter = new TableSorter(tableModel);
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
 
         JPanel buttonPanel = new JPanel();
         JButton exportButton = new JButton("Export to File");
         exportButton.addActionListener(this);
         JButton okButton = new JButton("Close");
         okButton.addActionListener(this);
         buttonPanel.add(exportButton);
         buttonPanel.add(okButton);
         contents.add(buttonPanel);
         setContentPane(contents);
 
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
     }
 
     public HetsDialog(HaploData hd){
         Vector results = hd.getPedFile().getHaploidHets();
 
         Vector colNames = new Vector();
         colNames.add("FamilyID");
         colNames.add("IndividualID");
         colNames.add("Marker");
         Vector data = new Vector();
 
         for(int i=0;i<results.size();i++) {
             StringTokenizer st = new StringTokenizer((String)results.get(i));
             Vector tmpVec = new Vector();
             tmpVec.add(st.nextToken());
             tmpVec.add(st.nextToken());
             tmpVec.add(Chromosome.getUnfilteredMarker(Integer.parseInt(st.nextToken())).getDisplayName());
             data.add(tmpVec);
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
         }else if (command.equals("Export to File")){
             HaploView.fc.setSelectedFile(new File(""));
 
             if (HaploView.fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                 File file = HaploView.fc.getSelectedFile();
                 try{
                     printTable(file);
                 }catch(IOException ioe){
                     JOptionPane.showMessageDialog(this,
                             ioe.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
 }
 

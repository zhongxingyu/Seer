 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.Individual;
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.util.Vector;
 import java.util.Arrays;
 
 
 public class FilteredIndividualsDialog extends JDialog implements ActionListener, Constants{
 
     public FilteredIndividualsDialog(HaploView h, String title) {
         super(h,title);
 
         BasicTableModel tableModel;
         JTable table;
         JPanel contents = new JPanel();
 
         contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
 
         Vector axedPeople = h.theData.getPedFile().getAxedPeople();
 
        Vector colNames = new Vector();
        colNames.add("FamilyID");
        colNames.add("IndividualID");
        colNames.add("Reason");
        Vector data = new Vector();
 
         for(int i=0;i<axedPeople.size();i++) {
            Vector tmpVec = new Vector();
             Individual currentInd = (Individual) axedPeople.get(i);
            tmpVec.add(currentInd.getFamilyID());
            tmpVec.add(currentInd.getIndividualID());
            tmpVec.add(currentInd.getReasonImAxed());
            data.add(tmpVec);
         }
 
        tableModel = new BasicTableModel(colNames, data);
         table = new JTable(tableModel);
         table.getColumnModel().getColumn(2).setPreferredWidth(300);
 
         JScrollPane tableScroller = new JScrollPane(table);
         int tableHeight = (table.getRowHeight()+table.getRowMargin())*(table.getRowCount()+2);
         if (tableHeight > 300){
             tableScroller.setPreferredSize(new Dimension(400, 300));
         }else{
             tableScroller.setPreferredSize(new Dimension(400, tableHeight));
         }
         tableScroller.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
 
         contents.add(tableScroller);
 
         JButton okButton = new JButton("OK");
         okButton.addActionListener(this);
         okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
         contents.add(okButton);
         setContentPane(contents);
 
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if(command.equals("OK")) {
             this.dispose();
         }
     }
 }
 
 
 

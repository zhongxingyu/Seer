 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fofo.presentation.view;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import org.fofo.services.management.ManagementService;
 
 /**
  *
  * @author Roger Torra
  */
 public class ClubListPanel extends JPanel{
 
     public ClubListPanel(JFrame frame, ManagementService services){
        frame.setTitle("Club List");
         frame.setSize(600, 400);
 
         JTable table = createTable();
         
             
         this.add(table.getTableHeader());    
         this.add(table);
 
     
     }
     
     private JTable createTable(){
         String[] columnNames = {"Club Name", "Email","Teams","in Competition"};
         
         
        JTable table = new JTable(getDataTable(), columnNames);
         JScrollPane scrollPane = new JScrollPane(table);
         table.setFillsViewportHeight(true);
         TableColumn column = null;
             for (int i = 0; i < 4; i++) {
                 column = table.getColumnModel().getColumn(i);
                 if (i == 1) {
                     column.setPreferredWidth(250); //third column is bigger
                 } else {
                     column.setPreferredWidth(100);
                 }
             }
             
          return table;
     
     }
     //This function uses FALSE data, need to call services to retrieve list.
     private Object[][] getDataTable(){
     
         Object[][] data = {
             {"club1", "club1@gmail.es", new Integer(5),new Boolean(true) },
             {"club2", "club2@gmail.es",  new Integer(3),new Boolean(true) },
             {"club3", "club3@gmail.es", new Integer(2),new Boolean(false) },
             {"club4", "club4@gmail.es", new Integer(2),new Boolean(true) },
             {"club5", "club5@gmail.es", new Integer(1),new Boolean(false) },
             {"club6", "club3@gmail.es", new Integer(2),new Boolean(false) },
             {"club7", "club4@gmail.es", new Integer(2),new Boolean(true) },
             {"club8", "club3@gmail.es", new Integer(2),new Boolean(false) },
             {"club9", "club4@gmail.es", new Integer(2),new Boolean(true) },
             {"club10", "club3@gmail.es", new Integer(2),new Boolean(true) },
             {"club11", "club4@gmail.es", new Integer(1),new Boolean(true) },
             {"club12", "club3@gmail.es", new Integer(0),new Boolean(false) },
             {"club13", "club4@gmail.es", new Integer(1),new Boolean(true) },
             {"club14", "club3@gmail.es", new Integer(2),new Boolean(false) },
             {"club15", "club4@gmail.es", new Integer(2),new Boolean(true) }
                 
         };
     
         return data;
     }
 }

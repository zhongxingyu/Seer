 package csheets.ext.database.ui;
 
 import csheets.core.Spreadsheet;
 import csheets.core.formula.compiler.FormulaCompilationException;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 /**
  * A window for user to confirm data to be imported from the database
  * @author João Carreira
  */
 class ConfirmImportUI extends JFrame
 {
     private Spreadsheet spreadSheet;
     private JLabel sysMsg;
     private JTable table;
     private JScrollPane scroll;
     private JButton btnOk, btnCancel;
     private String[][] tableData;
     private boolean flag;
     
     public ConfirmImportUI(Spreadsheet spreadSheet, String [][] tableData)
     {
         setTitle("Data import preview window");
         this.tableData = tableData;
         this.spreadSheet = spreadSheet;
         this.flag = flag;
         
         /* setting up column names */
         String []columns = new String[tableData[0].length - 1];
         for(int i = 0; i < columns.length; i++)
         {
             columns[i] = tableData[0][i + 1];
         }
         
         /* setting up table data */
         String [][]data = new String[tableData.length - 1][tableData[0].length - 1];
         for(int i = 0; i < data.length; i++)
         {
            for(int j = 0; j < data.length; j++)
             {
                 data[i][j] = tableData[i + 1][j + 1];
             }
         }
         
         /* label for text */
         JLabel sysMsg = new JLabel("Confirm import?");
         sysMsg.setForeground(Color.BLUE);
         
         /* buttons */
         btnOk = new JButton("OK");
         btnCancel = new JButton("Cancel");
         
         /* main panel */
         JPanel mainPanel = new JPanel(new GridLayout(3,3));
         
         /* table panel */
         JPanel tabPanel = new JPanel(new BorderLayout());
         table = new JTable(data, columns);
         scroll = new JScrollPane(table);
         tabPanel.add(scroll, BorderLayout.CENTER);
         
         /* msg panel */
         JPanel msgPanel = new JPanel();
         msgPanel.add(sysMsg);
         
         /* button panel */
         JPanel btnPanel = new JPanel();
         btnPanel.add(btnOk);
         btnPanel.add(btnCancel);
         
         /* setting up action listeners */
         HandlesEvent t = new HandlesEvent();
         btnOk.addActionListener(t);
         btnCancel.addActionListener(t);
         
         /* putting everything together */
         Container c = getContentPane();
         mainPanel.add(tabPanel);
         mainPanel.add(msgPanel);
         mainPanel.add(btnPanel);
         c.add(mainPanel);
         
         /* other settings */
         //pack();
         setSize(400,300);
         setVisible(true);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setLocationRelativeTo(null);
         setResizable(false);
     }
     
         /**
      * handles events on different GUI objects
      */
     public class HandlesEvent implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             /* ok button */
             if(e.getSource() == btnOk)
             {
                 try 
                 {
                     /* getting the starting row, which is defined in any of the first columns */
                     //int startRow = Integer.parseInt(tableData[1][0]);
                     
                     /* cycles the entire tableData array */
                     for(int i = 0; i < tableData.length; i++)
                     {
                         for(int j = 1; j < tableData[0].length; j++)
                         {         
                          /* changes the content of the given cell taking into account the row
                          (we have to subtract 2 to go to right place) */
                          //spreadSheet.getCell(startRow + (j - 2), i).setContent(tableData[i][j]);
                          spreadSheet.getCell(j - 1, i).setContent(tableData[i][j]);
                         }
                     }
                         JOptionPane.showMessageDialog(null, "Data successfully imported");
                 }
                 catch (FormulaCompilationException ex) 
                 {
                     JOptionPane.showMessageDialog(null, "Error: importing data");
                     Logger.getLogger(UITableSelect.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 dispose();
             }
             /* cancel button */
             if(e.getSource() == btnCancel)
             {
                 System.out.println("Confirm Import = " + getFlag());
                 dispose();
             }     
         }
     }
     
      
     public boolean getFlag()
     {
         return this.flag;
     }
     
     public void setFlag()
     {
         this.flag = true;
     }
 }

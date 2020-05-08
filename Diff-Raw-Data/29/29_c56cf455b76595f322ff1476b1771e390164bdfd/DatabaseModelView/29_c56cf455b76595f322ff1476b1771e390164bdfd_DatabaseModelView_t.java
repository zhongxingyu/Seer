 package com.esn.idea.liquibaseejb.toolwindow;
 
 import com.esn.idea.liquibaseejb.model.database.DatabaseColumnModel;
 import com.esn.idea.liquibaseejb.model.database.DatabaseModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
 import com.intellij.openapi.ui.VerticalFlowLayout;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Author: Marcus Nilsson
  * Date: 2008-nov-01
  * Time: 11:03:03
  */
 public class DatabaseModelView extends JPanel
 {
 
     private DatabaseModel databaseModel;
     private JScrollPane currentScrollPane;
     private JTable table;
     private DefaultTableModel tableModel;
     private int currentRow;
 
     public DatabaseModelView(String name)
     {
         super(new VerticalFlowLayout());
 
         ((VerticalFlowLayout)getLayout()).setVerticalFill(true);
         add(new JLabel(name));
 
         currentScrollPane = new JScrollPane();
         currentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         add(currentScrollPane);
 
         table = new JTable();
         currentScrollPane.getViewport().add(table);
 
         beginTable();
 
     }
 
     private void beginTable()
     {
         if (tableModel == null)
         {
             tableModel = new DefaultTableModel(Arrays.asList("Table", "Column", "Type").toArray(), 0)
             {
                 public boolean isCellEditable(int row, int column)
                 {
                     return false;
                 }
             };
 
             table.setModel(tableModel);
         }
 
         currentRow = 0;
     }
 
    public void setDatabaseModel(final DatabaseModel databaseModel)
     {
        if (!ApplicationManager.getApplication().isDispatchThread()) {
           ApplicationManager.getApplication().invokeLater(new Runnable() {
               public void run() {
                   setDatabaseModel(databaseModel);
               }
           }, ModalityState.NON_MODAL);
           return;
        }
         if (this.databaseModel != databaseModel)
 		{
 			this.databaseModel = databaseModel;
 
             if (databaseModel != null)
             {
                 beginTable();
 
                 List<String> tableNames = databaseModel.getTableNames();
                 Collections.sort(tableNames);
 
                 for (String tableName : tableNames)
                 {
                     JLabel tableLabel = new JLabel(tableName);
                     tableLabel.setFont(new Font("monospaced", Font.BOLD, 16));
 
                     addTableRow(Arrays.asList(tableName, "", ""));
 
                     for (String columnName : databaseModel.getColumnNames(tableName))
                     {
                         DatabaseColumnModel columnModel = databaseModel.getColumnModel(tableName, columnName);
 
                         StringWriter writer = new StringWriter();
                         PrintWriter w = new PrintWriter(writer);
                         columnModel.describe(w);
                         addTableRow(Arrays.asList("", columnName, writer.toString()));
                     }
                     addTableRow(Arrays.asList("", "", ""));
                 }
 
                 endTable();
             }
         }
 
     }
 
     private void addTableRow(List<String> values)
     {
         if (tableModel.getRowCount() > currentRow)
         {
             int column = 0;
             for (String value : values)
             {
                 tableModel.setValueAt(value, currentRow, column);
                 column++;
             }
         }
         else
         {
             tableModel.addRow(values.toArray());
         }
         currentRow++;
     }
 
     private void endTable()
     {
         while (currentRow < tableModel.getRowCount())
         {
             tableModel.removeRow(currentRow);
         }
     }
 
 }

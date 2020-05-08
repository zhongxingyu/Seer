 package com.Grateds.Reversi.GUI;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Scanner;
 import java.util.Vector;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.table.DefaultTableCellRenderer;
 
 public class TopScoresWindow extends JFrame{
     
     private JScrollPane jScrollPane1;
     private JTable scoresTable;
     private JButton deleteButton;
     private JButton exitButton;
     
     public TopScoresWindow(){
         initComponents();
         this.setTitle("Top Scores");
        this.setLocationRelativeTo(getOwner());
     }
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         scoresTable = new javax.swing.JTable();
         deleteButton = new javax.swing.JButton();
         exitButton = new javax.swing.JButton();
 
         scoresTable.setEnabled(false);
 
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
         //read scores file
         scoresTable.setModel(readModelScores());
         //center values fields
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
         centerRenderer.setHorizontalAlignment( JLabel.CENTER );
         scoresTable.setDefaultRenderer(String.class, centerRenderer);
         scoresTable.setDefaultRenderer(Integer.class, centerRenderer);
         //sorting score column
         scoresTable.setAutoCreateRowSorter(true);
         scoresTable.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));
 
         jScrollPane1.setViewportView(scoresTable);
 
         deleteButton.setText("Delete Scores");
         deleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteButtonActionPerformed(evt);
             }
         });
         exitButton.setText("Exit");
         exitButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exitButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(deleteButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(exitButton)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(deleteButton)
                     .addComponent(exitButton))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pack();
     }
     private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
         File file = new File("src/main/java/com/Grateds/Reversi/users.txt");
         if(file.delete()){
//            System.out.println("Scores deleted");
         }else{
//            System.out.println("Scores not deleted");
         }
         this.dispose();
     }
     private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {
         this.dispose();
     }
     public javax.swing.table.DefaultTableModel readModelScores(){
         String id;
         String name;
         Integer score;
         javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(){
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 switch (columnIndex){
                     case 0:
                         return String.class;
                     case 1:
                         return Integer.class;
                     default:
                         return String.class;
                 }
             }
         };
         model.addColumn("User");
         model.addColumn("Score");
         try{
             Scanner usersFile = new Scanner(new FileReader("src/main/java/com/Grateds/Reversi/users.txt"));
             while(usersFile.hasNext()){
                 Vector<Object> usr = new Vector<Object>();
                 id = usersFile.next();
                 name = usersFile.next();
                 score = Integer.parseInt( usersFile.next() );
                 usr.add(name);
                 usr.add(score);
                 model.addRow(usr);
             }
             usersFile.close();
         }catch (IOException e){
             e.getStackTrace();
         }
         return model;
     }
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new TopScoresWindow().setVisible(true);
//            }
//        });
//    }
     
 }

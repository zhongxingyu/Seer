 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package interfaces.editClass;
 
 import interfaces.MainFrame;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.InputMap;
 import javax.swing.JOptionPane;
 import javax.swing.JFileChooser;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 
 import java.io.File;
 import java.io.IOException;
 
 import io.Exporter;
 import io.parseXML;
 
 import java.awt.event.KeyEvent;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 /**
  *
  * @author Lilong
  */
 public class EditSelectedClass extends javax.swing.JPanel implements ActionListener{
 
     public CreateCategoryPanel categoryWindow;
     public AddAssignmentPanel assignmentWindow;
     public MainFrame parent;
     public int assignmentIndex, categoryIndex, courseIndex;
     private boolean isTableSet = false;
     public String categorySelected = ""; 
     
     /**
      * Creates new form EditCourse
      * @param frame
      * @param currentCourseInd
      */
     public EditSelectedClass(MainFrame frame, int currentCourseInd) {
         parent = frame;
         courseIndex = currentCourseInd;
         assignmentWindow = new AddAssignmentPanel(this);
         categoryWindow  = new CreateCategoryPanel(this);
         initComponents();
         if (courseIndex != -1)
         	setup();
     }
     
     private void setup() {
         model = (DefaultTableModel)assignmentTable.getModel();
         if (parent.courses.get(courseIndex) != null)
         	courseName.setText(parent.courses.get(courseIndex).getName() 
         			+ "-" + parent.courses.get(courseIndex).getSection());
         if (parent.courses.get(courseIndex).getNumberOfAssignmentCategories() > 0) {
             loadCourseData();
         }
     }
     
     private void loadCourseData() {
         for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfAssignmentCategories(); i++) {
             javax.swing.JMenu categoryMenu = new javax.swing.JMenu();
             categoryMenu.setText(parent.courses.get(courseIndex).getAssignmentCategory(i).getName());
             getCategoryName(categoryMenu);
 
             for (int j = 0; j < parent.courses.get(courseIndex).getAssignmentCategory(i).getNumberOfAssignments(); j++) {
                 final int indexOfCategory = i;
                 final int indexOfAssignment = j;
                 assignmentIndex = j;
              	categoryIndex = i;
              	final javax.swing.JMenuItem assignmentMenuItem = new javax.swing.JMenuItem();
              	assignmentMenuItem.setText(parent.courses.get(courseIndex).getAssignmentCategory(i).getAssignment(j).getName());
              	assignmentMenuItem.addActionListener(new java.awt.event.ActionListener() {
              		public void actionPerformed(java.awt.event.ActionEvent evt) {
              			categoryIndex = indexOfCategory;
              			assignmentIndex  = indexOfAssignment;
              			isTableSet = false;
              			populateTable();
              			courseName.setText(parent.courses.get(courseIndex).getName() 
                      		+ "-" + parent.courses.get(courseIndex).getSection() 
                      		+ " " + assignmentMenuItem.getText());
                      }
                 });
              	categoryMenu.add(assignmentMenuItem);   		
             }
             menuBar.add(categoryMenu);
             addToRemoveCategoryMenu(categoryMenu);
             categoryMenu.add(new javax.swing.JPopupMenu.Separator());
             addNewAssignmentButton(categoryMenu);
             removeAssignmentButton(categoryMenu);
         }     
     }
     
     
     private void addToRemoveCategoryMenu(JMenu category) {
         final JMenuItem caToBeRemove = new JMenuItem(category.getText());
         removeCategory.add(caToBeRemove);
         caToBeRemove.addActionListener(this);
         caToBeRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeCategoryActionPerformed(evt, caToBeRemove);
             }
         });
     }
     
     
     private void populateTable() {
     	for (int i = model.getRowCount()-1; i >= 0; i--) {
     		model.removeRow(i);
     	}
     	for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfStudents(); i++) {
     		model.insertRow(i, new Object[]{ 
     				parent.courses.get(courseIndex).getStudent(i).getFullName(),
     				parent.courses.get(courseIndex).getStudent(i).getPseudoName(),
     				parent.courses.get(courseIndex)
     				.getCategories().get(categoryIndex)
     				.getAssignment(assignmentIndex)
     				.getGrade(parent.courses.get(courseIndex).getStudent(i).getPseudoName())});
     	}
     	isTableSet = true;
     }
     
     public void setPanelMenu() {
     	parent.setJMenuBar(menuBar);
     }
     
     public void actionPerformed(ActionEvent evt) {
         if (categoryWindow.actionStatus.equals("addCategory")) {
             createNewCategory();
         }
         
         if (parent.currentAssignmentWindow.actionStatus.equals("addAssignment")) {
             refreshMenu(parent.currentCourseWindow);
         }      
     }
     
     private void refreshMenu(EditSelectedClass window) {
         window.removeAll();
         window.menuBar.removeAll();
         window.initComponents();
         window.setup();
         parent.setEditSelectedClassVisible(window);
         parent.currentAssignmentWindow.actionStatus = "waiting";
     }
     
     private void createNewCategory() {
         if (repeatCategoryChecker()) {
             JMenu newCategory = new JMenu(categoryWindow.getCategoryName());
             menuBar.add(newCategory);
             getCategoryName(newCategory);
            parent.courses.get(courseIndex).addAssignmentCategory(categoryWindow.getCategoryName()); // add new category
                                                                                                     // to the course object
             //adding add new assignment button
             addNewAssignmentButton(newCategory);
 
             //adding remove assignment button
             removeAssignmentButton(newCategory);
            
             addToRemoveCategoryMenu(newCategory); //add to remove category menu
             categoryWindow.actionStatus = "waiting";
         }
     }
     
     private void getCategoryName(final JMenu category) {
         category.addMenuListener(new javax.swing.event.MenuListener() {
             @Override
             public void menuCanceled(javax.swing.event.MenuEvent evt) {
             }
             @Override
             public void menuSelected(javax.swing.event.MenuEvent evt) {
                 categorySelected = category.getActionCommand();
             }
             @Override
             public void menuDeselected(javax.swing.event.MenuEvent evt) {
             }
         });
     }
     
     private void addNewAssignmentButton(JMenu category) {
         final JMenuItem addAssignmentButton = new JMenuItem("Add");
         category.add(addAssignmentButton, -1);
         addAssignmentButton.addActionListener(assignmentWindow);
         addAssignmentButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addAssignmentActionPerformed(evt);
             }
         });
     }
     
     private void removeAssignmentButton(JMenu category) {
         final JMenu removeAssignmentButton = new JMenu("Remove");
         category.add(removeAssignmentButton, -1);
        System.out.println(parent.courses.get(courseIndex).getNumberOfAssignmentCategories());
         int cateIndex = parent.courses.get(courseIndex).getAssignmentCategoryIndex(category.getText());
         for (int i = 0; i < parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).getNumberOfAssignments(); i++) {
             final JMenuItem removeItem = new JMenuItem(parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).getAssignment(i).getName());
             removeAssignmentButton.add(removeItem);
             removeItem.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     removeAssignmentActionPerformed(evt);
                 }
             });
         }
     }
     
     public boolean repeatCategoryChecker() {
         for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfAssignmentCategories(); i++) {
             if (parent.courses.get(courseIndex).getAssignmentCategory(i).getName().equals(categoryWindow.getCategoryName())) {
                 System.out.print(i);
                 JOptionPane.showMessageDialog(null,
                             String.format("%33s",categoryWindow.getCategoryName() + 
                             " category already exited"),"Error",
                             JOptionPane.ERROR_MESSAGE);
                 return false;
             }
         }
         return true;
     }
     
     @SuppressWarnings("serial")
     public DefaultTableModel model = new DefaultTableModel(
         new Object [][] {
 
         },
         new String [] {
             "Student", "Ghost Name", "Grade"
         }
     ) {
         public boolean isCellEditable(int row, int column) {
                 if (column != 2)
                         return false;
                 else
                         return true;
         }
     };
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("serial")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         menuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         File_Save = new javax.swing.JMenuItem();
         File_ExportToHTML = new javax.swing.JMenuItem();
         createMenu = new javax.swing.JMenu();
         addCategory = new javax.swing.JMenuItem();
         removeCategory = new javax.swing.JMenu();
         studentMenu = new javax.swing.JMenu();
         addStudent = new javax.swing.JMenuItem();
         removeStudent = new javax.swing.JMenuItem();
         jScrollPane1 = new javax.swing.JScrollPane();
         
         assignmentTable = new JTable() {
         	public void changeSelection(final int row, final int column, boolean toggle, boolean extend) {
     		 super.changeSelection(row, column, toggle, extend);
     		 assignmentTable.editCellAt(row, column);
     		 assignmentTable.transferFocus();
         	}
         };
         
         goBackButton = new javax.swing.JButton();
         courseName = new javax.swing.JLabel();
 
         fileMenu.setText("File");
 
         File_Save.setText("Save");
         File_Save.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
             	parseXML.saveXML(parent.courses.get(courseIndex));
             }
         });
         fileMenu.add(File_Save);
 
         File_ExportToHTML.setText("Export To HTML");
         File_ExportToHTML.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 File_ExportToHTMLActionPerformed(evt);
             }
         });
         fileMenu.add(File_ExportToHTML);
 
         menuBar.add(fileMenu);
 
         createMenu.setText("Create");
 
         addCategory.setText("Add Category");
         addCategory.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addCategoryActionPerformed(evt);
             }
         });
         createMenu.add(addCategory);
 
         removeCategory.setText("Remove Category");
         createMenu.add(removeCategory);
 
         menuBar.add(createMenu);
 
         studentMenu.setText("Student");
 
         addStudent.setText("Add Student");
         studentMenu.add(addStudent);
 
         removeStudent.setText("Remove Student");
         studentMenu.add(removeStudent);
 
         menuBar.add(studentMenu);
 
         assignmentTable.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
         assignmentTable.setModel(model);
         changeTabActionsToEnterActions(assignmentTable);
         
         jScrollPane1.setViewportView(assignmentTable);
 
         goBackButton.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
         goBackButton.setText("Go Back");
         assignmentTable.getModel().addTableModelListener(changedData());
         goBackButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 goBackButtonActionPerformed(evt);
             }
         });
 
         courseName.setFont(new java.awt.Font("Georgia", 0, 18)); // NOI18N
         courseName.setText("jLabel1");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(courseName)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(goBackButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(courseName)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(goBackButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
     @SuppressWarnings("serial")
 	public void changeTabActionsToEnterActions(JTable assignmentTable) {
         InputMap im = assignmentTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
         KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
         
         KeyStroke shiftTab = KeyStroke.getKeyStroke("shift TAB");
         KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
         
         im.put(tab, im.get(enter));
         im.put(shiftTab, im.get(shiftEnter));
         
         final Action oldTabAction = assignmentTable.getActionMap().get(im.get(tab));
         Action tabAction = new AbstractAction()
         {
             public void actionPerformed(ActionEvent e)
             {
                 oldTabAction.actionPerformed(e);
                 JTable table = (JTable)e.getSource();
                 int rowCount = table.getRowCount();
                 int columnCount = table.getColumnCount();
                 int row = table.getSelectedRow();
                 int column = table.getSelectedColumn();
 
                 while (!table.isCellEditable(row, column)) {
                     column += 1;
 
                     if (column == columnCount) {
                         column = 0;
                         row +=1;
                     }
 
                     if (row == rowCount)
                     	row = 0;
 
                     if (row == table.getSelectedRow() &&  column == table.getSelectedColumn())
                         break;
                 }
 
                 table.changeSelection(row, column, false, false);
             }
         };
         final Action oldShiftTabAction = assignmentTable.getActionMap().get(im.get(shiftTab));
         Action shiftTabAction = new AbstractAction()
         {
             public void actionPerformed(ActionEvent e)
             {
                 oldShiftTabAction.actionPerformed(e);
                 JTable table = (JTable)e.getSource();
                 int rowCount = table.getRowCount();
                 int columnCount = table.getColumnCount();
                 int row = table.getSelectedRow();
                 int column = table.getSelectedColumn();
 
                 while (!table.isCellEditable(row, column)) {
                     column -= 1;
 
                     if (column == -1)
                         column = columnCount-1;
 
                     if (row == -1)
                         row = rowCount-1;
 
                     if (row == table.getSelectedRow() &&  column == table.getSelectedColumn())
                         break;
                 }
                 table.changeSelection(row, column, false, false);
             }
         };
         assignmentTable.getActionMap().put(im.get(tab), tabAction);
         assignmentTable.getActionMap().put(im.get(shiftTab), shiftTabAction);
     }
     
     private TableModelListener changedData() {
     	TableModelListener cha = new TableModelListener() {
     		public void tableChanged(TableModelEvent e) {
    			   if (isTableSet) {
    				   int r = e.getLastRow();
    				   String message = "";
    				   try {
    					   if (Integer.parseInt(model.getValueAt(r, 2).toString()) > 
    					       parent.courses.get(courseIndex).getCategories()
    					       .get(categoryIndex)
    					   	   .getAssignment(assignmentIndex).getWorth()) 
    					   {
    						   message = "INVALID INPUT:\nInput grade was greater than assignment worth total of "
    								   	  + parent.courses.get(courseIndex).getCategories()
    	  					   				.get(categoryIndex)
    	  					   				.getAssignment(assignmentIndex).getWorth() + ".";
    						   
    						   throw new NumberFormatException();
    					   }
  					   parent.courses.get(courseIndex).getCategories()
  					   .get(categoryIndex)
  					   .getAssignment(assignmentIndex)
  					   .setGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName(), 
  							     Integer.parseInt(model.getValueAt(r, 2).toString()));
  				   } catch (NumberFormatException changeback) {
  					   if (message.isEmpty()) message = "INVALID INPUT:\n" + model.getValueAt(r, 2).toString()
  							   							+ " is not a valid integer number.";
  					   if (model.getValueAt(r, 2).toString().isEmpty()) {
  						   model.setValueAt(0, r, 2);
  						   parent.courses.get(courseIndex).getCategories()
  						   .get(categoryIndex)
  						   .getAssignment(assignmentIndex)
  						   .setGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName(), 0);
  					   } else {
  						   model.setValueAt( parent.courses.get(courseIndex).getCategories()
  								   .get(categoryIndex)
  								   .getAssignment(assignmentIndex)
  								   .getGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName()), r, 2);
  					       JOptionPane.showMessageDialog(null, message);
  					   }
  				   }
    			   }
  		   }
 		};
 		return cha;
     }
     
     
     private void removeCategoryActionPerformed(java.awt.event.ActionEvent evt, JMenuItem category) {
          String categoryName = evt.getActionCommand();
          for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfAssignmentCategories(); i++) {
              if (parent.courses.get(courseIndex).getAssignmentCategory(i).getName().equals(categoryName)) {
             	parent.courses.get(courseIndex).removeAssignmentCategory(categoryName); //remove from course object
                 removeCategory.remove(category); // rmove from the menu
                 for (int j = 0; j < menuBar.getComponentCount(); j++) {
                     if (menuBar.getMenu(j).getText().equals(category.getText())) {
                         menuBar.remove(j);
                         
                         j = menuBar.getComponentCount();
                     }
                 }
                 i = parent.courses.get(courseIndex).getNumberOfAssignmentCategories();
              }
          }
          this.setPanelMenu();
     }
     
     private void addAssignmentActionPerformed(java.awt.event.ActionEvent evt) {
         parent.setAssignmentWindowVisible();  
     }
     
     private void removeAssignmentActionPerformed(java.awt.event.ActionEvent evt) {
         String assignmentName = evt.getActionCommand();
        int cateIndex = 0;
        if (!categorySelected.equals("")) {
            cateIndex = parent.courses.get(courseIndex).getAssignmentCategoryIndex(categorySelected);
        } else {
            cateIndex = parent.courses.get(courseIndex).getNumberOfAssignmentCategories() - 1;
        }
         for (int i = 0; i < parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).getNumberOfAssignments(); i++) {
             if (parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).getAssignment(i).getName().equals(assignmentName)) {
                 parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).removeAssignment(assignmentName); //remove from course object
                 refreshMenu(this);
             }
         }
     }                               
     
     private void goBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goBackButtonActionPerformed
         parent.setSimpleModeVisible();
     }//GEN-LAST:event_goBackButtonActionPerformed
 
     private void addCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCategoryActionPerformed
         parent.setCreateCategoryVisible();
     }//GEN-LAST:event_addCategoryActionPerformed
    
     private void File_ExportToHTMLActionPerformed(java.awt.event.ActionEvent evt) {                                                  
         JFileChooser fc = new JFileChooser();
         int returnVal = fc.showSaveDialog(EditSelectedClass.this);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             Exporter exp = new Exporter();
             File file = fc.getSelectedFile();
             try {
                 exp.exportCourseToHTML(parent.courses.get(courseIndex), file.getCanonicalPath());
             }
             catch (IOException ex) {
                 JOptionPane.showMessageDialog(null, "Error exporting HTML.");
             }
         }
     }                                                      
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem File_ExportToHTML;
     private javax.swing.JMenuItem File_Save;
     private javax.swing.JMenuItem addCategory;
     private javax.swing.JMenuItem addStudent;
     private javax.swing.JTable assignmentTable;
     private javax.swing.JLabel courseName;
     private javax.swing.JMenu createMenu;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JButton goBackButton;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenu removeCategory;
     private javax.swing.JMenuItem removeStudent;
     private javax.swing.JMenu studentMenu;
     // End of variables declaration//GEN-END:variables
 }

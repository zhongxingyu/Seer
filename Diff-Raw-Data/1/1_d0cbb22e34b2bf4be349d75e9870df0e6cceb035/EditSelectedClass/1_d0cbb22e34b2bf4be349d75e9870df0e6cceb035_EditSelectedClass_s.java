 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package interfaces.editClass;
 
 import interfaces.MainFrame;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableRowSorter;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.InputMap;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JFileChooser;
 import javax.swing.JScrollPane;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import io.Exporter;
 import io.parseXML;
 import objects.Assignment;
 
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
     public Integer assignmentIndex, categoryIndex = null;
     public int courseIndex;
     private boolean isTableSet = false;
     public String categorySelected = ""; 
     public AddNewStudent studentWindow;
     
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
         studentWindow = new AddNewStudent(this);
         initComponents();
         
         if (parent.courses.get(courseIndex).getLastAssignmentIndex() != null && parent.courses.get(courseIndex).getLastCategoryIndex() != null) {
         	loadTable(parent.courses.get(courseIndex).getLastCategoryIndex(), parent.courses.get(courseIndex).getLastAssignmentIndex());
         }
         else {
            	courseName.setText(parent.courses.get(courseIndex).getName());
         }
        	setup();
     }
     
     @SuppressWarnings("unchecked")
 	private void setup() {
         model = (DefaultTableModel)assignmentTable.getModel();
     	TableRowSorter sorter = new TableRowSorter(model);
     	sorter.setComparator(0, new Comparator(){
             @Override
             public int compare(Object arg0, Object arg1) {
                 return arg0.toString().compareTo(arg1.toString());
             }
     	});
         if (parent.courses.get(courseIndex).getNumberOfAssignmentCategories() > 0) {
             loadCourseData();
         }
         ArrayList sortKeys = new ArrayList();
         sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
         sorter.setSortKeys(sortKeys);
         assignmentTable.setRowSorter(sorter);
         sorter.sort();
     }
     
     private void loadCourseData() {
         for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfAssignmentCategories(); i++) {
             javax.swing.JMenu catMenu = new javax.swing.JMenu();
             catMenu.setText(parent.courses.get(courseIndex).getAssignmentCategory(i).getName());
          	catMenu.setName(parent.courses.get(courseIndex).getAssignmentCategory(i).getName());
             getCategoryName(catMenu); 
 
             for (int j = 0; j < parent.courses.get(courseIndex).getAssignmentCategory(i).getNumberOfAssignments(); j++) {
                 final int indexOfCategory = i;
                 final int indexOfAssignment = j;
                 assignmentIndex = j;
              	categoryIndex = i;
              	final javax.swing.JMenuItem assignmentMenuItem = new javax.swing.JMenuItem();
              	assignmentMenuItem.setText(parent.courses.get(courseIndex).getAssignmentCategory(i).getAssignment(j).getName());
              	assignmentMenuItem.addActionListener(new java.awt.event.ActionListener() {
              		public void actionPerformed(java.awt.event.ActionEvent evt) {
              			loadTable(indexOfCategory, indexOfAssignment);
                      }
                 });
              	catMenu.add(assignmentMenuItem);  
             }
             categoryMenu.add(catMenu);
             addToRemoveCategoryMenu(catMenu);
             catMenu.add(new javax.swing.JPopupMenu.Separator());
             addNewAssignmentButton(catMenu);
             removeAssignmentButton(catMenu);
         }     
         
         for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfStudents(); i++) {
         	addRemoveStudent(i);
         }
     }
     
     public void addRemoveStudent(int i) {
     	javax.swing.JMenuItem newStudentMenu = new javax.swing.JMenuItem();
     	newStudentMenu.setText(parent.courses.get(courseIndex).getStudent(i).getFullName());
         newStudentMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeStudentActionPerformed(evt, studentMenu);
             }
         });
         removeStudent.add(newStudentMenu);
     }
     
     public void loadTable(Integer i, Integer j) {
 			categoryIndex = i;
 			assignmentIndex  = j;
 			populateTable();
 			if (i != null && j != null) {
 				courseName.setText(parent.courses.get(courseIndex).getName() + " " + 
 				parent.courses.get(courseIndex).getCategories().get(i).getAssignment(j).getName() + " / Worth: " +
 				parent.courses.get(courseIndex).getCategories().get(i).getAssignment(j).getWorth());
 			}
 			else
 				courseName.setText(parent.courses.get(courseIndex).getName());
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
     
     
     public void populateTable() {
     	if (parent.courses.get(courseIndex).getNumberOfAssignmentCategories() == 0)
     		return;
     	
     	boolean hasAssignments = false;
     	
     	for (int i = 0; i <parent.courses.get(courseIndex).getNumberOfAssignmentCategories(); i++) {
     		if (parent.courses.get(courseIndex).getAssignmentCategory(i).getAssignments().size() == 0)
     			continue;
     		else
     			hasAssignments = true;
     	}
     	if (!hasAssignments)
     		return;
     	
 		isTableSet = false;
     	for (int i = model.getRowCount()-1; i >= 0; i--) {
     		model.removeRow(i);
     	}
     	for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfStudents(); i++) {
     		String grade;
     		if (parent.courses.get(courseIndex)
 			.getCategories().get(categoryIndex)
 			.getAssignment(assignmentIndex)
 			.getGrade(parent.courses.get(courseIndex).getStudent(i).getPseudoName()) == null)
     			grade = "";
     		else {
     			grade = String.valueOf(parent.courses.get(courseIndex)
     					.getCategories().get(categoryIndex)
     					.getAssignment(assignmentIndex)
     					.getGrade(parent.courses.get(courseIndex).getStudent(i).getPseudoName()));
     		}
     			
     		model.insertRow(i, new Object[]{ 
     				parent.courses.get(courseIndex).getStudent(i).getFullName(),
     				parent.courses.get(courseIndex).getStudent(i).getPseudoName(),
     				grade});
     	}
     	isTableSet = true;
     }
     
     public void setStudentWindowVisible() {
         parent.setContentPane(studentWindow);
         parent.setJMenuBar(null);
         this.setVisible(false);
         studentWindow.setVisible(true);
         studentWindow.firstNameTextField.requestFocus();
         studentWindow.populateTable();
         studentWindow.firstNameTextField.setText("");
         studentWindow.lastNameTextField.setText("");
         parent.getRootPane().setDefaultButton(studentWindow.addButton);
         parent.pack();
     }
     
     public void setAssignmentWindowVisible() {
         parent.setContentPane(assignmentWindow);
         parent.setJMenuBar(null);
         setVisible(false);
         assignmentWindow.courseInfo.setText(parent.courses.get(courseIndex).getCategories().get(assignmentWindow.categoryIndex).getName());
         assignmentWindow.setVisible(true);
         assignmentWindow.nameTextField.requestFocus();
         assignmentWindow.populateTable();
         parent.getRootPane().setDefaultButton(assignmentWindow.addButton);
         parent.pack();
     }
     
     public void setCreateCategoryVisible() {
         parent.setContentPane(categoryWindow);
         parent.setJMenuBar(null);
         categoryWindow.categoryNameTextField.setText("");
         categoryWindow.categoryNameTextField.requestFocus();
         setVisible(false);
         categoryWindow.setVisible(true);
         parent.getRootPane().setDefaultButton(categoryWindow.addButton);
        parent.pack();
     }
     
     public void setPanelMenu() {
     	parent.setJMenuBar(menuBar);
     }
     
     public void actionPerformed(ActionEvent evt) {
      
     }
     
     public void refreshMenu(EditSelectedClass window) {
         window.removeAll();
         window.menuBar.removeAll();
         window.initComponents();
         window.setup();
         parent.setEditSelectedClassVisible(window);
         assignmentWindow.actionStatus = "waiting";
     }
     
     public void createNewCategory() {
         if (repeatCategoryChecker()) {
             JMenu newCategory = new JMenu(categoryWindow.getCategoryName());
             newCategory.setName(categoryWindow.getCategoryName());
             categoryMenu.add(newCategory);
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
         categoryMenu = new javax.swing.JMenu();
         File_Save = new javax.swing.JMenuItem();
         File_ExportToHTML = new javax.swing.JMenuItem();
         createMenu = new javax.swing.JMenu();
         addCategory = new javax.swing.JMenuItem();
         removeCategory = new javax.swing.JMenu();
         studentMenu = new javax.swing.JMenu();
         addStudent = new javax.swing.JMenuItem();
         removeStudent = new javax.swing.JMenu();
         jScrollPane1 = new javax.swing.JScrollPane();
         
         helpMenu = new javax.swing.JMenu();
         helpAbout = new javax.swing.JMenuItem();
         helpContent = new javax.swing.JMenuItem();
         
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
             	saveCurrentState();
             }
         });
         fileMenu.add(File_Save);
        
 
         File_ExportToHTML.setText("Export Class To HTML");
         File_ExportToHTML.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 File_ExportToHTMLActionPerformed(evt);
             }
         });
         fileMenu.add(File_ExportToHTML);
 
         menuBar.add(fileMenu);
 
         addCategory.setText("Add Category");
         addCategory.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addCategoryActionPerformed(evt);
             }
         });
         categoryMenu.add(addCategory);
 
         removeCategory.setText("Remove Category");
         categoryMenu.add(removeCategory);
 
         categoryMenu.addSeparator();
         
         menuBar.add(createMenu);
 
         studentMenu.setText("Student");
 
         addStudent.setText("Add Student");
         addStudent.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addStudentActionPerformed(evt);
             }
         });
         studentMenu.add(addStudent);
 
         removeStudent.setText("Remove");
         studentMenu.add(removeStudent);
 
         menuBar.add(studentMenu);
         
         categoryMenu.setText("Categories");
         menuBar.add(categoryMenu);
         
         helpMenu.setText("Help");
         menuBar.add(helpMenu);
         
         helpAbout.setText("About");
         helpAbout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 helpAboutActionPerformed(evt);
             }
         });
         
         helpContent.setText("Help");
         helpContent.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 helpContentActionPerformed(evt);
             }
         });
         
         helpMenu.add(helpAbout);
         helpMenu.add(helpContent);
         
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
         courseName.setText(parent.courses.get(courseIndex).getName());
 
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
                     Assignment currentAssignment = parent.courses.get(courseIndex).getCategories()
                                                         .get(categoryIndex).getAssignment(assignmentIndex);
                     try {
                         Integer newValue = Integer.parseInt(model.getValueAt(r, 2).toString());
                         int worth = currentAssignment.getWorth();
                         if (newValue > worth || newValue < 0) {
                             message = "INVALID INPUT:\nInput grade was not between 0 and " + worth + ".";
                             throw new NumberFormatException();
                         }
                         currentAssignment.setGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName(), newValue, true);
                         saveCurrentState();
                     } catch (NumberFormatException changeback) {
                         if (message.isEmpty()) {
                             message = "INVALID INPUT:\n"
                                         + model.getValueAt(r, 2).toString()
                                         + " is not a valid integer number.";
                         }
                         if (model.getValueAt(r, 2).toString().isEmpty()) {
                             currentAssignment.setGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName(), null, true);
                         }
                         else {
                             Integer oldGrade = currentAssignment.getGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName());
                             if (oldGrade != null) {
                                 model.setValueAt(oldGrade, r, 2);
                             }
                             else {
                                 model.setValueAt("", r, 2);
                                 JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
                             }
                         }
                     }
                 }
             }
         };
         return cha;
     }
     
     private void removeStudentActionPerformed(java.awt.event.ActionEvent evt, JMenuItem student) {
         String studentName = evt.getActionCommand();
         parent.courses.get(courseIndex).removeStudent(studentName);; //remove from course object
         for (int j = 0; j < removeStudent.getItemCount(); j++) {
         	if (removeStudent.getItem(j).getText() != null && 
         			removeStudent.getItem(j).getText().equals(studentName)) {
                    	removeStudent.remove(j);
                    	break;
         	}
         }
         //TODO remove student from student table
         this.loadTable(categoryIndex, assignmentIndex);
         this.setPanelMenu();
         saveCurrentState();
    }
     
     private void removeCategoryActionPerformed(java.awt.event.ActionEvent evt, JMenuItem category) {
          String categoryName = evt.getActionCommand();
          for (int i = 0; i < parent.courses.get(courseIndex).getNumberOfAssignmentCategories(); i++) {
              if (parent.courses.get(courseIndex).getAssignmentCategory(i).getName().equals(categoryName)) {
             	parent.courses.get(courseIndex).removeAssignmentCategory(categoryName); //remove from course object
                 removeCategory.remove(category); // rmove from the menu
                 for (int j = 0; j < categoryMenu.getItemCount(); j++) {
                 	if (categoryMenu.getMenuComponent(j).getName() != null && 
                 	categoryMenu.getMenuComponent(j).getName().toString().equals(category.getText())) {
                     	categoryMenu.remove(j);
                         j = categoryMenu.getMenuComponentCount();
                     }
                 }
                 i = parent.courses.get(courseIndex).getNumberOfAssignmentCategories();
              }
          }
          this.setPanelMenu();
          saveCurrentState();
     }
     
     private void addAssignmentActionPerformed(java.awt.event.ActionEvent evt) {
         String assignmentName = evt.getActionCommand();
         int cateIndex = 0;
         if (!categorySelected.equals("")) {
             cateIndex = parent.courses.get(courseIndex).getAssignmentCategoryIndex(categorySelected);
         } else {
             cateIndex = parent.courses.get(courseIndex).getNumberOfAssignmentCategories() - 1;
         }
         assignmentWindow.categoryIndex = cateIndex;
     	saveCurrentState();
     	setAssignmentWindowVisible();
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
                 if (parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).getAssignmentIndex(assignmentName) == assignmentIndex) {
                 	assignmentIndex = null;
                 	categoryIndex = null;
                 	courseName.setText(parent.courses.get(courseIndex).getName());
                 }
                 parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).removeAssignment(assignmentName); //remove from course object
                 refreshMenu(this);
                 populateTable();
                 saveCurrentState();
                 return;
             }
         }
     	saveCurrentState();
     }                               
     
     private void goBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goBackButtonActionPerformed
     	saveCurrentState();
         parent.setSimpleModeVisible();
     }//GEN-LAST:event_goBackButtonActionPerformed
 
     private void addCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCategoryActionPerformed
        setCreateCategoryVisible();
     }//GEN-LAST:event_addCategoryActionPerformed
     
     private void addStudentActionPerformed(java.awt.event.ActionEvent evt) {
     	setStudentWindowVisible();
     }
    
     private void File_ExportToHTMLActionPerformed(java.awt.event.ActionEvent evt) {
         parent.courses.get(courseIndex).setGhostGrades();
         saveCurrentState();
         JFileChooser fc = new JFileChooser();
         FileFilter htmlFilter = new FileNameExtensionFilter("HTML File", "html", "htm");
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setAcceptAllFileFilterUsed(false);
         fc.setMultiSelectionEnabled(false);
         fc.addChoosableFileFilter(htmlFilter);
         int returnVal = fc.showSaveDialog(EditSelectedClass.this);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             Exporter exp = new Exporter();
             try {
                 File fileToSave = fc.getSelectedFile();
                 String pathToExport = fileToSave.getCanonicalPath();
                 // Force an html extension
                 if (!pathToExport.endsWith(".html") && !pathToExport.endsWith(".htm")) {
                     pathToExport = pathToExport.concat(".html");
                 }
                 exp.exportCourseToHTML(parent.courses.get(courseIndex), pathToExport);
             }
             catch (IOException ex) {
                 JOptionPane.showMessageDialog(null, "Error exporting HTML.");
             }
         }
     }       
     
     private void helpAboutActionPerformed(java.awt.event.ActionEvent evt) {
     	JFrame helpAbout = new JFrame();
         JTextArea helpAboutText = new JTextArea( "INTRODUCTION\n\n" +
         	    "Ghost Grader is an obfuscating gradebook program for professors to manage students` grades and provides professors an HTML page of students` grades online for students to view. The HTML page contains a list of registered students with their grades for tests, quizzes, assignments, etc. and obscures the students` identities by having 'ghost' students added to the list that are indistinguishable from real students. All of the students` psuedo names are permutations of various colors and animal names. A professor can provide the real students their corresponding pseudo name so they can identify their scores online.\n\n" + 
         	    "THE OBFUSCATION PROCESS\n\n" +
         	    "HOW IT IS DONE:\n" + 
         	    "In order to safely post all of the information about students and their scores online, the Ghost Grader program incorporates several different ways of hiding the data in plain sight. As the professor adds students to his or her course, the program creates a random number of 'ghost' students per student added. The number of ghosts added is random so there will be no way to tell the actual size of the class if the information is posted publicly. An outsider would be able to tell that at least one student was added, but they will not be able to tell if it was more than one student added. If a student drops the class, that student is immediately treated as a ghost from there on out, therefore, an outsider would never know if the class size was decreased.\n\n" +
         	    "Once grades are entered for the real students by the professor, the program calculates the grade statistics for the class. After that is done, the program assigns random scores to all of the ghost students that keep the statistical information for the class valid. For example, if the class average for homework assignment one is 76.4 percent, the program will assign random scores to all the ghost students so that the students and the ghost students together are still 76.4 percent.\n\n" + 
         	    "All of the students and ghost students are assigned a unique pseudo name by the program. This will keep all the names unique for when the grades are posted publicly. All the professor has to do is let each student know what their pseudo name is and each student will be able to see how they are doing in the class on a public web page with no chance of anyone else being able to know who is who.\n\n" +
         	    "WHY IT IS EFFECTIVE:\n" + 
         	    "Since there is enough random ghost students added per each student in the class, it will be statistically impossible to know or guess who is who with any reliable accuracy. The program verifies a minimum number of ghost students are added per actual student to maintain this constraint. So even though people may be able to get to a webpage that is located on the professor`s public web page, there will be no way to tell who is who or what a student`s grades are.\n\n" +
         	    "STATISTICAL VALIDATION:\n" +
         	    "Even though the number of ghost students will be random, a constant number of 6 ghost students per student will be used here to show why this program hides the data adequately. Suppose the class has 20 students, so there would be 120 ghost students as well. It will be shown by calculating all of the combinations of choosing 20 students out of a group of 140 that there are over 827 sextillion different combinations. So the probability of choosing the correct 20 students with any accuracy is approximately zero, which keeps all of the grades obfuscated.\n\n" + 
         	    "HOW THE GHOST STUDENTS ARE ASSIGNED:\n" + 
         	    "    1.    A set of ranges are determined with a maximum of 11 ranges allowed. A score of zero is in a range by itself and the rest of the ranges are evenly distributed. For example, if the assignment worth is 100 points, the ranges are {0, 1-10, 11-20, ... , 91-100}.\n" + 
         	    "    2.    Percentages of students within the determined ranges are found. Then, that percentage per ghost pool is assigned a random grade within their particular range.\n" +
         	    "    3.    The mean of the students` scores are determined as well as the mean of the ghost scores.\n" +
         	    "    4.    If the mean of the ghost scores is more than the students` mean, the program randomly selects a ghost with a score higher than the mean and decrements it by one. This continues until the means match up to within one decimal point. If the ghost scores mean is higher than the students` mean, the opposite is done, ignoring the zero range (so as not to pull all of the ghosts out of the zero range).\n" +
         	    "    5.    Once the values of the means, which are rounded to one decimal point, match the program, it shuffles the grade array and randomly assigns those values to the ghost students.\n\n");
         helpAboutText.setEditable(false);
         Color colorGray = new Color(230, 231, 236);
         helpAboutText.setBackground(colorGray);
         helpAboutText.setFont(new Font("Georgia", Font.PLAIN, 14));
         helpAboutText.setLineWrap(true);
         helpAboutText.setWrapStyleWord(true);
         JScrollPane areaScrollPane = new JScrollPane(helpAboutText);
         areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         areaScrollPane.setPreferredSize(new Dimension(300, 500));
         helpAbout.add(areaScrollPane);
         helpAbout.setSize(500, 300);
         helpAbout.setLocation(700, 300);
         helpAbout.setVisible(true);
     }
     
     private void helpContentActionPerformed(java.awt.event.ActionEvent evt) {
     	JFrame helpContent = new JFrame();
         JTextArea helpContentText = new JTextArea( "GHOST GRADER HELP\n\n"+
         		"MAIN WINDOW:\n" +
         	    "This is the window you will see when you open the Ghost Graders program. Initially, the window will be blank aside from the following menu options:\n\n" +
         	    "    File > Save All: This option will allow you to save the currently open gradebook.\n\n" +
         	    "    Edit > Add Course: Opens a new window for the user to add a new class to the current gradebook. This window will have several fields to fill in to create a new class. Only the fields denoted with an '*' are required. Click the 'Create' button to create and save the new class. Click the 'Cancel' button to go back to the previous window.\n\n" +
         	    "    Edit > Remove Course: Displays a drop down menu of all the current classes and removes the class you select.\n\n" +
         	    "Below the menu, under 'Courses', there is an 'Add New Course' button. This option has the same functionality as the Edit > Add Class menu option.\n\n" +
         	    "After adding a course, clicking on a course will bring you to the view category window.\n\n" +
         	    "VIEW CATEGORY WINDOW:\n" +
         	    "To access the view category window, click on the course you want to view from the main window. This window`s menu has the following options:\n\n" + 
         	    "    File > Save: Saves current progress.\n\n" +
         	    "    File > Export Class To HTML: Opens a new window to let you save an HTML page of the current class information and statistics in the file you choose.\n\n" + 
         	    "    Categories > Add Category: Opens a new window and you input the name of the new category in the field 'Category Name'. This creates a new category with the name you specified once you press the 'Add' button. Press 'Cancel' to go back without adding a new category.\n\n" +
         	    "    Categories > Remove Category: Displays a drop down menu of the current categories. Click the category you would like to remove to delete it.\n\n" +
         	    "    Student > Add Student: This opens the View Students Window. Add a new student by filling in the two required 'First Name' and 'Last Name' fields at the bottom of the window. Once you have typed in the student`s first name in the first field and last name in the second field, click the 'Add' button to add the student to the class. Click the 'Return' button to go back to the previous window.\n\n" +
         	    "    Student > Remove Student: Displays a drop down menu of current students. To remove a student, click on one of the students in the list.\n\n" +
         	    "VIEW STUDENTS WINDOW:\n" +
         	    "To access the View Students Window, click on the menu option Student > Add Student from the View Category Window.\n\n" +
         	    "To edit a student`s name, click on that student`s name and a field will appear. Type in the new student`s name and it will save once you press the enter key.\n\n" +
         	    "To edit a student`s grade, click the student`s grade and a field will appear. Type in the new student`s grade and it will save once you press the enter key.\n\n" +
         	    "ADD/EDIT CATEGORY ASSIGNMENT WINDOW:\n" +
         	    "To access the Add/Edit Category Assignment Window, click on the category you would like to add an assignment to. Each category will appear in the Edit Class Window menu. When you click on a category, the following drop down menu options appear:\n\n" +
         	    "    Add: Displays a new menu to add a new assignment to the selected category. Click 'Add' to save the new category assignment. Click 'Cancel' to go back and not save the new assignment.\n\n" +
         	    "    Remove: Displays a drop down menu with all of the current category assignments. Remove an assignment from that category by clicking the assignment you would like to remove.\n\n" +
         	    "Once category assignments have been created, they will appear in the category`s drop down menu. Selecting the category will open the View Category Window for that particular category.\n\n"
         	    );
         helpContentText.setEditable(false);
         Color colorGray = new Color(230, 231, 236);
         helpContentText.setBackground(colorGray);
         helpContentText.setFont(new Font("Georgia", Font.PLAIN, 14));
         JScrollPane areaScrollPane = new JScrollPane(helpContentText);
         helpContentText.setLineWrap(true);
         helpContentText.setWrapStyleWord(true);
         areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         areaScrollPane.setPreferredSize(new Dimension(300, 500));
         helpContent.add(areaScrollPane);
         helpContent.setSize(500, 300);
         helpContent.setLocation(700, 300);
         helpContent.setVisible(true);
     }
     
     public void saveCurrentState() {
     	if (assignmentIndex != null && categoryIndex != null) {
 	    	parent.courses.get(courseIndex).setLastAssignmentIndex(assignmentIndex);
 	    	parent.courses.get(courseIndex).setLastCategoryIndex(categoryIndex);
     	}
     	else {
 	    	parent.courses.get(courseIndex).setLastAssignmentIndex(null);
 	    	parent.courses.get(courseIndex).setLastCategoryIndex(null);
     	}
     	parseXML.saveXML(parent.courses.get(courseIndex));
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
     private javax.swing.JMenu removeStudent;
     private javax.swing.JMenu studentMenu;
     private javax.swing.JMenu categoryMenu;
     private javax.swing.JMenu helpMenu;
     private javax.swing.JMenuItem helpAbout;
     private javax.swing.JMenuItem helpContent;
     // End of variables declaration//GEN-END:variables
 }

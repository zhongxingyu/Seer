 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package interfaces.editClass;
 
 import interfaces.MainFrame;
 
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
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JFileChooser;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 
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
 				parent.courses.get(courseIndex).getCategories().get(i).getAssignment(j).getName());
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
        
 
         File_ExportToHTML.setText("Export To HTML");
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
    				   try {
    					   if (Integer.parseInt(model.getValueAt(r, 2).toString()) > 
    					       parent.courses.get(courseIndex).getCategories()
    					       .get(categoryIndex)
    					   	   .getAssignment(assignmentIndex).getWorth() || Integer.parseInt(model.getValueAt(r, 2).toString()) < 0) 
    					   {
    						   message = "INVALID INPUT:\nInput grade was not between 0 and "
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
  					   saveCurrentState();
  				   } catch (NumberFormatException changeback) {
  					   if (message.isEmpty()) message = "INVALID INPUT:\n" + model.getValueAt(r, 2).toString()
  							   							+ " is not a valid integer number.";
  					   if (model.getValueAt(r, 2).toString().isEmpty()) {
  						  parent.courses.get(courseIndex).getCategories()
  	 					   .get(categoryIndex)
  	 					   .getAssignment(assignmentIndex)
  	 					   .setGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName(), 
  	 							     null);
  					   } else {
  						   model.setValueAt( parent.courses.get(courseIndex).getCategories()
  								   .get(categoryIndex)
  								   .getAssignment(assignmentIndex)
  								   .getGrade(parent.courses.get(courseIndex).getStudent(r).getPseudoName()), r, 2);
  					       JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
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
                 	courseName.setText(parent.courses.get(courseIndex).getName());
                	for (int j = model.getRowCount()-1; j >= 0; j--) {
                		model.removeRow(j);
                	}
                 }
                 parent.courses.get(courseIndex).getAssignmentCategory(cateIndex).removeAssignment(assignmentName); //remove from course object
                 refreshMenu(this);
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
     // End of variables declaration//GEN-END:variables
 }

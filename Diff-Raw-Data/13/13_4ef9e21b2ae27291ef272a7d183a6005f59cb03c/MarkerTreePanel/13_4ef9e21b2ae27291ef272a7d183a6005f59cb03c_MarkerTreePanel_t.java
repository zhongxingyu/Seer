 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.View.marker;
 
 import java.awt.Toolkit;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import melt.Controller;
 import melt.Model.Essay;
 import melt.Model.Fibq;
 import melt.Model.Mcq;
 import melt.Model.Question;
 import melt.Model.Section;
 import melt.Model.Student;
 import melt.Model.Subsection;
 import melt.Model.Test;
 
 /**
  *
  * @author Abdu
  */
 public class MarkerTreePanel extends JTree {
 
     private Controller controller;
     private MarkerPanel markerPanel;
     private DefaultMutableTreeNode allStudents;
     private DefaultTreeModel treeModel;
     private Toolkit toolkit = Toolkit.getDefaultToolkit();
 
     /**
      * Creates new form TreePanel
      */
     public MarkerTreePanel(Controller controller, MarkerPanel markerPanel) {
 
         this.controller = controller;
         this.markerPanel = markerPanel;
         setOpaque(true);
         allStudents = new DefaultMutableTreeNode(this.controller.getStudents());
         treeModel = new DefaultTreeModel(allStudents);
     //    treeModel.addTreeModelListener(new MyTreeModelListener());
         this.setModel(treeModel);
         this.setEditable(false);
         this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         this.setShowsRootHandles(true);
         this.addTreeSelectionListener(new TreeNodesListener());
         //createNodes();
     }
 
     public void createNodes() {
         for (Student student : this.controller.getStudents().getStudentList()) {
             DefaultMutableTreeNode studentList = new DefaultMutableTreeNode(student);
             allStudents.add(studentList);
             if (student.getSelectedTest() == null) {
                 break;
             }
             Test test = student.getSelectedTest();
            
             DefaultMutableTreeNode selectedTest = new DefaultMutableTreeNode(test);
             studentList.add(selectedTest);
               
             for (Section sect : student.getSelectedTest().getSections()) {
                 DefaultMutableTreeNode sectionList = new DefaultMutableTreeNode(sect);
                 selectedTest.add(sectionList);
                 if (sect.getSubsections() == null) {
                     break;
                 }
                 for (Subsection subsect : sect.getSubsections()) {
                     if (subsect.getQuestions() == null) {
                         break;
                     }
                     DefaultMutableTreeNode subsectionList = new DefaultMutableTreeNode(subsect);
                     sectionList.add(subsectionList);
                     for (Question question : subsect.getQuestions()) {
 
                         DefaultMutableTreeNode questionList = new DefaultMutableTreeNode(question);
                         subsectionList.add(questionList);
                     }
                 }
             }
             }
         }
     
 
     public Object getParentObject() {
         TreePath currentSelection = this.getSelectionPath();
         if (currentSelection != null) {
             DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
             DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (currentNode.getParent());
             if (parent != null) {
                 return parent.getUserObject();
             }
         }
 
         return null;
     }
 
     public Object getSelectedObject() {
         TreePath currentSelection = this.getSelectionPath();
         if (currentSelection != null) {
             DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
 
             if (currentNode != null) {
                 return currentNode.getUserObject();
             }
         }
         return null;
     }
 
     
         
     private class TreeNodesListener implements TreeSelectionListener {
 
         /**
          * Required by TreeSelectionListener interface.
          */
         @Override
         
         public void valueChanged(TreeSelectionEvent e) {
             Object currentNode = getSelectedObject();
             Object parentNode = getParentObject();
             if (currentNode instanceof Question) {
               //Subsection subsection = (Subsection) parentNode;
                 if (currentNode instanceof Mcq) {  
                     markerPanel.redrawMCQPanel((Mcq) currentNode);
                 } else if (currentNode instanceof Fibq) {  
                     markerPanel.redrawFIBQPanel((Fibq) currentNode);
                 } else if (currentNode instanceof Essay) {
                     markerPanel.redrawEssayPanel((Essay) currentNode);
                 }
             }
 /*
             Object currentNode = getSelectedObject();
             Object parentNode = getParentObject();
 
             if (currentNode instanceof TestBank) {
                 markerPanel.redrawTestBankPanel();
             } else if (currentNode instanceof Test) {
                 TestBank testBank = (TestBank) parentNode;
                 markerPanel.redrawTestPanel("Add Section", testBank, (Test) currentNode);
             } else if (currentNode instanceof Section) {
                 Test test = (Test) parentNode;
                 markerPanel.redrawSectionPanel("Add Subsection", test, (Section) currentNode );
             } else if (currentNode instanceof Subsection) {
                 Section section = (Section) parentNode;
                 markerPanel.redrawSubsectionPanel("Add Question", section, (Subsection) currentNode);
             } else if (currentNode instanceof Question) {
                 Subsection subsection = (Subsection) parentNode;
                 if (currentNode instanceof Mcq) {
                     markerPanel.redrawMCQPanel("Add Question", subsection, (Mcq) currentNode);
                 }
                 if (currentNode instanceof Fibq) {
                     markerPanel.redrawFIBQPanel("Add Question", subsection, (Fibq) currentNode);
                 }
                 if (currentNode instanceof Essay) {
                     markerPanel.redrawEssayPanel("Add Question", subsection, (Essay) currentNode);
                 }
             }*/ 
         }
     }
 }

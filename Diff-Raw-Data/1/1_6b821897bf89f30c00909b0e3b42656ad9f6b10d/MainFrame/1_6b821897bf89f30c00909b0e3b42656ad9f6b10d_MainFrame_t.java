 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package interfaces;
 
 import io.Preloader;
 import interfaces.editClass.*;
 
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import objects.MyCourse;
 
 /**
  *
  * @author Lilong
  * @edit Zach; preloader changes
  * @edit Brett; whatever
  */
 @SuppressWarnings("serial")
 public class MainFrame extends javax.swing.JFrame {
 
     public boolean isDetialMode = false;
     
     public SimpleMode simpleMode;
     public AddNewClass addNewClass;
     public EditSelectedClass currentCourseWindow;
     
     public ArrayList<MyCourse> courses;
     public ArrayList<EditSelectedClass> courseWindows = new ArrayList<EditSelectedClass>();
     public MyCourse currentCourse;
 
     public MainFrame() {
         initComponents();
         SetUp();
     }
 
     private void SetUp() {
         this.setLocation(400, 300);
         preloadGradebooks();
         simpleMode = new SimpleMode(this);
         addNewClass = new AddNewClass(this);     
         if (courseWindows.size() > 0) {
             currentCourseWindow = courseWindows.get(0);
         }    
         setSimpleModeVisible();
         synchronize();    //Update all other JPanel classes
         pack();
     }
     
     public void setSimpleModeVisible() {
         simpleMode.setPanelMenu();
         setContentPane(simpleMode);
         simpleMode.setVisible(true);
         addNewClass.setVisible(false);
         if (currentCourseWindow != null)
         	currentCourseWindow.setVisible(false);
         pack();
     }
     
     public void setEditSelectedClassVisible(EditSelectedClass selectedCourse) {
         setCurrentCourseWindow(selectedCourse);
         setContentPane(selectedCourse);
         /*if (courses.get(selectedCourse.courseIndex).getLastCategoryIndex() != null  && courses.get(selectedCourse.courseIndex).getLastAssignmentIndex() != null)
         	selectedCourse.loadTable(courses.get(selectedCourse.courseIndex).getLastCategoryIndex(), courses.get(selectedCourse.courseIndex).getLastAssignmentIndex());*/
         selectedCourse.setPanelMenu();
         simpleMode.setVisible(false);
         addNewClass.setVisible(false);
         currentCourseWindow.setVisible(true);
         pack();
     }
     
     public void setAddNewClass() {
         setContentPane(addNewClass);
         simpleMode.setVisible(false);
         if (currentCourseWindow != null)
         	currentCourseWindow.setVisible(false);
         addNewClass.clearComponents();
         addNewClass.setVisible(true);
         pack();
     }
     
     public void sortCourses() {
     	for (int i = 0; i < courses.size(); i++) {
     		for (int j = i; j < courses.size(); j++) {
     			if (courses.get(i).getIdentifier().compareTo(courses.get(j).getIdentifier()) > 0) {
     				Collections.swap(courses, i, j);
     			}
     		}
     	}
     }
     
     /**
      *  set which course needs to be edited
      * @param window a class for editSelectedClass
      */
     public void setCurrentCourseWindow(EditSelectedClass window) {
     	currentCourseWindow = window;
     }
     
     /*
      * add a single course into the edit course window arraylist
      * @course my courses object
      */
     public void addCourseWindow(int courseIndex) {
     	courseWindows.add(new EditSelectedClass(this, courseIndex));
     }
     
     /*
      * return the single course object which is editing now
      */
     public MyCourse getCurrentCourse() {
     	return currentCourse;
     }
     
     /*
      * return the arralylist of MyCourses object
      */
     public  ArrayList<MyCourse> getCourses() {
     	return courses;
     }
     
     /*
      * return the the a single editClass in editClassArrayList
      */
     public EditSelectedClass getCourseWindow(int i) {
     	return courseWindows.get(i);
     }
     
     
     private void preloadGradebooks() {
     	Preloader preload = new Preloader(".xml");
     	courses = preload.getCourseArray();
     }
     
     /*
      * important:
      * invokeLater in main method make the action performed to run in a reverse order 
      */
     private void synchronize() {
     	addNewClass.cancelButton.addActionListener(addNewClass);
         addNewClass.addButton.addActionListener(addNewClass); //run first        
     }    
 
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("GhostGrader");
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 589, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 433, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
     	
     	File f = new File("gradebooks" + File.separator + "archive" + File.separator + "records");
     	
     	f.mkdirs();
     	
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
             
             public void run() {
                 MainFrame GhostGrader = new MainFrame();
                 GhostGrader.setVisible(true);
             }
         });
     }
 
     private int toInteger(String string_courseNumber) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }

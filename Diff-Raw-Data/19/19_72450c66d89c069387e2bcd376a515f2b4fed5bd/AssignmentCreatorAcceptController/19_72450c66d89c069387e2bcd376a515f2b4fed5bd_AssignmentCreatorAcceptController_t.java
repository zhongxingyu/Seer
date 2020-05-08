 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /** Sends data for a new assignment to the specified Course by reading date
  * from the given JTextFields. Closes the AssignmentCreator once the assignment 
  * is created.
  * @author Qasim Ali
  */
 public class AssignmentCreatorAcceptController implements ActionListener{
   private Course course;
   private AssignmentCreator creator;
   private JTextField[] totals;
   private JTextField[] weightings;
   private JTextField nameField;
 
   /** Create the controller with the passed course, creator, and text fields.
    * @param course    the course model 
    * @param creator   the parent AssignmentCreator 
    * @param totals    array of textfields used to contain assignment totals 
    * @param weightings  array of textfields used to contain weightings 
    * @param nameField  text field that contains assignment name 
    */
   public AssignmentCreatorAcceptController(Course course,
           AssignmentCreator creator, JTextField[] totals,
           JTextField[]  weightings, JTextField nameField) {
     this.course = course;
     this.creator = creator;
     this.totals = totals;
     this.weightings = weightings;
     this.nameField = nameField;
   }
 
   /** Add a course with the user-entered data from the AssignmentCreator, if 
    * it doesn't already exist
    * @param e    the triggered event 
    */
   public void actionPerformed(ActionEvent e) {
     // Check if the assignment already exists
     if(this.course.assignmentExists(this.nameField.getText())) {
       // Alert the user if the assignment already exists 
       JOptionPane.showMessageDialog(this.creator,
               "An assignment with the specified name already exists.", 
               "Error adding course",
               JOptionPane.INFORMATION_MESSAGE);
     }
     // The assignment does not exist, so add it to the Course
     else
     {
       try {
       // Parse the values for totals 
       int[] totalVals = new int[4];
       totalVals[Course.C] = Integer.parseInt(this.totals[Course.C].getText());
       totalVals[Course.T] = Integer.parseInt(this.totals[Course.T].getText());
       totalVals[Course.K] = Integer.parseInt(this.totals[Course.K].getText());
       totalVals[Course.A] = Integer.parseInt(this.totals[Course.A].getText());
 
       // Make sure totals are > 0
       for(int i : totalVals) {
         if(i <= 0)
           throw new NumberFieldException(i, "category totals");
       }
     
       // Parse the values for weightings 
       double[] weightVals = new double[4];
       weightVals[Course.C] = Double.parseDouble(this.weightings[Course.C].getText());
       weightVals[Course.T] = Double.parseDouble(this.weightings[Course.T].getText());
       weightVals[Course.K] = Double.parseDouble(this.weightings[Course.K].getText());
       weightVals[Course.A] = Double.parseDouble(this.weightings[Course.A].getText());
 
       // Make sure weightings are >= 0
       for(double i : weightVals) {
         if(i < 0)
           throw new NumberFieldException((int)i, "category weightings");
       }
 
       this.course.createAssignment(this.nameField.getText(), 
               totalVals[Course.K], totalVals[Course.A], totalVals[Course.T],
              totalVals[Course.C]);
      this.course.setWeightings(this.nameField.getText(), weightVals);
 
       // Close the creator window
       this.creator.dispose();
       }
       catch(NumberFormatException ex) {
         // Do nothing if one of the input boxes has invalid input 
       }
       catch(NumberFieldException ex) {
         // Alert the user 
         JOptionPane.showMessageDialog(this.creator, ex.toString(),
                 "Error creating assignment", JOptionPane.ERROR_MESSAGE);
       }
     }
   }
 } 

 package main.ui;
 
 import edu.css.model.Exam;
 import edu.css.model.Student;
 import edu.css.operations.DAOLoader;
 import edu.css.operations.ExamDAO;
 import edu.css.operations.StudentDAO;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class AddStudentWindow extends JDialog {
     private JPanel contentPane;
     private JButton buttonOK;
     private JButton buttonCancel;
     private JTextField tfName;
     private JTextField tfBac;
     private JTextField tfMediaExamen;
     private JTextField admissionAverage;
     private boolean validInput = true;
     public static final String ASSERTION_FAIL = "[Assertion Fail]\n\t";
 
     private StudentDAO studentDAO = DAOLoader.getStudentDAO();
     private ExamDAO examDAO = DAOLoader.getExamDAO();
 
     private Student student;
     private Exam exam;
 
     public AddStudentWindow() {
         setContentPane(contentPane);
         setModal(true);
         getRootPane().setDefaultButton(buttonOK);
 
         assert studentDAO != null : ASSERTION_FAIL + "AddStudentWindow, bad initialization of studentDAO is not null";
         assert examDAO != null :  ASSERTION_FAIL + "AddStudentWindow, bad initialization of examDAO is not null";
 
         assert student == null : ASSERTION_FAIL + "AddStudentWindow, " + student.getName() + "is not null";
         student = new Student();
         assert student != null;
 
         assert exam == null : ASSERTION_FAIL + "AddStudentWindow, exam is not null";
         exam = new Exam();
         assert exam != null;
 
         buttonOK.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onOK();
             }
         });
 
         buttonCancel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() when cross is clicked
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() on ESCAPE
         contentPane.registerKeyboardAction(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     public AddStudentWindow(Student student){
         this();
         if(student == null){
             assert student == null : ASSERTION_FAIL + "AddStudentWindow, " + student.getName() + "is not null";
             MainWindow.showMessageWindow("Invalid Student", "Edit");
             onCancel();
         }
         this.student = student;
 
         this.exam = examDAO.getExamForStudent(student);
         assert exam != null : ASSERTION_FAIL + "Invalid Exam, no exam found for student " + student.getName();
 
         this.buttonOK.setText("Edit");
 
         this.tfName.setText(student.getName());
         this.tfBac.setText(student.getAverage().toString());
         this.tfMediaExamen.setText(exam.getMark().toString());
     }
 
     private void onOK() {
         validateInput();
         if(!validInput){
             assert !validInput : ASSERTION_FAIL + "Invalid case, validInput is true";
             return;
         }
         assert studentDAO != null : ASSERTION_FAIL + "OnOK -> Null studentDAO";
         studentDAO.addStudent(student);
         assert !studentDAO.getStudents().isEmpty() : ASSERTION_FAIL + "OnOK -> studentDAO.addStudent(x) didn't worked!!!";
 
         assert exam != null : ASSERTION_FAIL + "OnOK -> Null exam";
         exam.setStudentId(student.getId());
         assert exam.getStudentId() == student.getId() : ASSERTION_FAIL + "OnOK -> mismatch between exam.studentId and student.Id!!!";
 
         assert examDAO != null : ASSERTION_FAIL + "OnOK -> Null examDAO";
         examDAO.addExam(exam);
        assert examDAO.getExamForStudent(student).equals(exam) : ASSERTION_FAIL + "OnOK -> invalid AddExam operation!!!";
         dispose();
     }
 
     private void validateInput(){
         validInput = true;
         assert student != null : ASSERTION_FAIL + "validateInput, student cannot be null";
         assert exam != null : ASSERTION_FAIL + "validateInput, exam cannot be null";
 
         if(!isValidName(tfName.getText())){
             validInput = false;
             tfName.setBackground(Color.RED);
         }else {
             tfName.setBackground(Color.WHITE);
 
             student.setName(tfName.getText());
             assert student.getName().equals(tfName.getText()) : ASSERTION_FAIL + "validateInput, invalid student name";
         }
 
         if(!isValidNumber(tfBac.getText())){
             validInput = false;
             tfBac.setBackground(Color.RED);
         }else {
             tfBac.setBackground(Color.WHITE);
 
             student.setAverage(Double.valueOf(tfBac.getText()));
             assert student.getAverage().equals(Double.valueOf(tfBac.getText())) : ASSERTION_FAIL + "validateInput, invalid student bac average";
         }
 
         if(tfMediaExamen.getText().trim().length() == 0)
         {
             exam.setMark(0.0);
             assert exam.getMark().equals(0.0) : ASSERTION_FAIL + "validateInput, invalid exam mark";
         }
         else if(!isValidNumber(tfMediaExamen.getText())){
             validInput = false;
             tfMediaExamen.setBackground(Color.RED);
         }else {
             tfMediaExamen.setBackground(Color.WHITE);
 
             exam.setMark(Double.valueOf(tfMediaExamen.getText()));
             assert exam.getMark().equals(Double.valueOf(tfMediaExamen.getText())) : ASSERTION_FAIL + "validateInput, invalid set exam mark operation";
         }
 
 //        if(validInput)
 //            student.setPassed(AdmissionHelper.passed(student, exam));
     }
 
     private boolean isValidName(String name){
         assert name != null : ASSERTION_FAIL + "isValidName() method fail, name cannot be null.";
         if(name.length() > 2){
             return true;
         }else{
             return false;
         }
     }
 
     private boolean isValidNumber(String number){
         assert number != null : ASSERTION_FAIL + "isValidNumber cannot get a null parameter";
         try {
             double value = Double.parseDouble(number);
             if(value > 10){
                 return false;
             }
 
             assert value <= 10 : ASSERTION_FAIL + "isValidNumber, number cannot be larger than 10";
 
         }catch (NumberFormatException e){
             return false;
         }
         return true;
     }
 
     private void onCancel() {
         dispose();
     }
 
     public Student getStudent() {
         return student;
     }
 
     public Exam getExam() {
         return exam;
     }
 
     public void setExam(Exam exam) {
         this.exam = exam;
     }
 }

 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import org.antlr.runtime.*;
 
 /**
  *
  * @author jorgejaso
  */
 
 public class LispIntGUI extends JFrame implements ActionListener {
     /** GUI Buttons **/
     private JButton selectButton, analyzeButton, executeButton, saveButton, exitButton;
     /** GUI TextArea to display source lisp code and output */
     private JTextArea CodeTextArea, OutputTextArea;
     /** GUI Label output */
     private JLabel SourceLabel, SelectLabel, OutputLabel;
     /** GUI Label output */
     private JFileChooser FileChooser;
     private String FilePath="";
     private DisplayCode SourceCodeObject =new DisplayCode(FilePath);
  
     String lispFile, ResultFile; 
     
     // Constructor for a new GUI
     public LispIntGUI() {
             setDefaultCloseOperation(EXIT_ON_CLOSE);
             setTitle("Lisp Interpreter: List Manipulation");
             setSize(700, 520);
             layoutTop();
             layoutMiddle();
             layoutBottom();	
     } 
                 
     // Class JPanel containing Select Button
     public void layoutTop() {
             JPanel topJPanel = new JPanel();
             topJPanel.setLayout(new BorderLayout());
             JPanel MiddleTop = new JPanel();
             //MiddleTop.setBackground(Color.black);
             selectButton= new JButton("Select File");
             selectButton.addActionListener(this);
             MiddleTop.add(selectButton);
             topJPanel.add(MiddleTop, BorderLayout.CENTER);
             add(topJPanel, BorderLayout.NORTH);
     } // End layoutTop
     
     // Class JPanel containing Label, JTextArea and Execute Button
     public void layoutMiddle() {
             JPanel middleJPanel = new JPanel();
             middleJPanel.setLayout(new BorderLayout());
             JPanel UpperMiddle = new JPanel();
             //UpperMiddle.setBackground(Color.blue);
             JPanel MiddleMiddle = new JPanel();
             //MiddleMiddle.setBackground(Color.green);
             JPanel LowerMiddle = new JPanel();
             //LowerMiddle.setBackground(Color.yellow);
             
             SourceLabel= new JLabel("Source Lisp Code:");
             UpperMiddle.add(SourceLabel, BorderLayout.NORTH);
             
             CodeTextArea = new JTextArea(10,75);
             CodeTextArea.setFont(new Font("Courier", Font.PLAIN, 14));
             CodeTextArea.setEditable(true);
             CodeTextArea.setText("");
             JScrollPane scrollPane = new JScrollPane(CodeTextArea);
             MiddleMiddle.add(scrollPane,BorderLayout.CENTER);
             
             analyzeButton = new JButton("Analyze");
             analyzeButton.addActionListener(this);
             executeButton = new JButton("Execute");
             executeButton.addActionListener(this);
             LowerMiddle.add(analyzeButton, BorderLayout.SOUTH);
             LowerMiddle.add(executeButton, BorderLayout.SOUTH);
 
             middleJPanel.add(UpperMiddle, BorderLayout.NORTH);
             middleJPanel.add(MiddleMiddle, BorderLayout.CENTER);
             middleJPanel.add(LowerMiddle, BorderLayout.SOUTH);
             add(middleJPanel, BorderLayout.CENTER);           
     } // End layoutMiddle
     
     /**
      * Class JPanel containing Label, JTextArea and Exit Button
      */
     public void layoutBottom() {
             JPanel bottomJPanel = new JPanel();
             bottomJPanel.setLayout(new BorderLayout());
             JPanel UpperBottom = new JPanel();
             //UpperBottom.setBackground(Color.DARK_GRAY);
             JPanel MiddleBottom = new JPanel();
             //MiddleBottom.setBackground(Color.gray);
             JPanel LowerBottom = new JPanel();
             //LowerBottom.setBackground(Color.LIGHT_GRAY);
             
             OutputLabel= new JLabel("Output:");
             UpperBottom.add(OutputLabel, BorderLayout.NORTH);
 
             OutputTextArea = new JTextArea(10,75);
             OutputTextArea.setFont(new Font("Courier", Font.PLAIN, 14));
             OutputTextArea.setEditable(false);
             OutputTextArea.setText("");
             JScrollPane scrollPane = new JScrollPane(OutputTextArea);
             MiddleBottom.add(scrollPane,BorderLayout.CENTER);
             
             saveButton = new JButton("Save");
             saveButton.addActionListener(this);
             exitButton = new JButton("Exit");
             exitButton.addActionListener(this);
             LowerBottom.add(saveButton, BorderLayout.SOUTH);
             LowerBottom.add(exitButton, BorderLayout.SOUTH);
 
             bottomJPanel.add(UpperBottom, BorderLayout.NORTH);
             bottomJPanel.add(MiddleBottom, BorderLayout.CENTER);
             bottomJPanel.add(LowerBottom, BorderLayout.SOUTH);
             add(bottomJPanel, BorderLayout.SOUTH);
     } // End layoutBottom
         
     /**
      * Process button clicks.
      * @param ae the ActionEvent
      */
     public void actionPerformed(ActionEvent ae) {
             if(ae.getSource()==selectButton){
                 // Code to choose the source file
                 JFileChooser chooser = new JFileChooser("/Users/jorgejaso/NetBeansProjects/LispInt_List/LispCode");
                 FileNameExtensionFilter extfilter = new FileNameExtensionFilter("lisp files", ("lisp"),("lsp"),("txt"));
                 chooser.setFileFilter(extfilter);
                 int returnVal = chooser.showOpenDialog(LispIntGUI.this);
                 // if file chosen, display file contents
                 if (returnVal == JFileChooser.APPROVE_OPTION){
                   FilePath = chooser.getSelectedFile().getPath();
                   // System.out.println(FilePath);
                   DisplayCode SourceCodeObject =new DisplayCode(FilePath);
                   String Code=SourceCodeObject.DisplaySource();
                   CodeTextArea.setText(Code);
                 }
             } // End if selectButton
             
             if (ae.getSource()==analyzeButton)
             {	    	
                 if (CodeTextArea.getText().equals("")){
                         JOptionPane.showMessageDialog(null, "Enter Lisp Code or Select Lisp File!", "Warning Message", JOptionPane.WARNING_MESSAGE);
                 }else{
                 // Lisp Code Analysis
                         OutputTextArea.setText("The source lisp code contains: ");
                         OutputTextArea.append("\n S-expression: ");
                         OutputTextArea.append("\n Atoms: ");
                 }
             }
             
             if (ae.getSource()==executeButton){
                 // Execute selected file
                 try {
                     PrintWriter writerout = null; 
                     writerout = new PrintWriter("/Users/jorgejaso/NetBeansProjects/LispInt_List/LispSource");
                     if (CodeTextArea.getText().equals("")){
                         JOptionPane.showMessageDialog(null, "Enter Lisp Code or Select Lisp File!", "Warning Message", JOptionPane.WARNING_MESSAGE);
                     }
                     writerout.println(CodeTextArea.getText());
                     writerout.close();
                     CharStream cs= new ANTLRFileStream("/Users/jorgejaso/NetBeansProjects/LispInt_List/LispSource");
                     ListmanLexer lexer = new ListmanLexer (cs); // Modify for new grammar
                     CommonTokenStream tokens = new CommonTokenStream(lexer);
                     ListmanParser parser = new ListmanParser(tokens); // Modify for new grammar
                     try {
                         parser.prog();
                     }catch(RecognitionException ex) {
                         Logger.getLogger(LispIntGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }catch(IOException e){
                     JOptionPane.showMessageDialog(null, "File not found. \n", "Error Message", JOptionPane.ERROR_MESSAGE); 
                     System.out.println("File not found");
                     System.exit(0);
                 }
 
                 //Display Results in text field
                 DisplayCode SourceCodeObject =new DisplayCode();
                 String Results=SourceCodeObject.DisplayOutput();
                 OutputTextArea.setText(Results);	   
             } // End if executeButton 
             
             if (ae.getSource()==saveButton)
             {	    	
                 // Save the input code and the results in a file
                 ResultFile = JOptionPane.showInputDialog ("Please enter file name: ");
 
                 try {
                     PrintWriter writerout = null; 
                     writerout = new PrintWriter("/Users/jorgejaso/NetBeansProjects/LispInt_List/"+ResultFile+".txt"); 
                     writerout.println("INPUT CODE:");
                     writerout.println("---------------------------------------------------\n");
                     writerout.println(CodeTextArea.getText());
                     writerout.println("\nRESULTS:");
                     writerout.println("---------------------------------------------------");
                     writerout.println(OutputTextArea.getText());
                     writerout.close();
                 }catch(IOException e){
                     JOptionPane.showMessageDialog(null, "File not found. \n", "Error Message", JOptionPane.ERROR_MESSAGE); 
                     System.out.println("File not found");
                     System.exit(0);
                 } 
                 
             }
             
             
             if (ae.getSource()==exitButton)
             {	    	
                 // Closing the application
                 System.exit(0);		
             }
      	
     } // end actionPerformed
   
     
 }

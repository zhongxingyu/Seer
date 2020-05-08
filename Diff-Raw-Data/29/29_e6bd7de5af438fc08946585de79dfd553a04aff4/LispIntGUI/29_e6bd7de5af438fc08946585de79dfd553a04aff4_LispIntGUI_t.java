 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.filechooser.*;
 import java.util.*;
 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
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
             setTitle("Lisp Interpreter: Arithmetic Expressions");
             setSize(700, 520);
             setResizable(false);
             setLocation(300,100);
             layoutTop();
             layoutMiddle();
             layoutBottom();	
     } 
                 
     // Class JPanel containing Select Button
     public void layoutTop() {
             JPanel topJPanel = new JPanel();
             topJPanel.setLayout(new BorderLayout());
             JPanel MiddleTop = new JPanel();
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
             JPanel MiddleMiddle = new JPanel();
             JPanel LowerMiddle = new JPanel();
             
             SourceLabel= new JLabel("Source Lisp Code:");
             UpperMiddle.add(SourceLabel, BorderLayout.NORTH);
             
             CodeTextArea = new JTextArea(10,75);
             CodeTextArea.setFont(new Font("Courier", Font.PLAIN, 14));
             CodeTextArea.setEditable(true);
             CodeTextArea.setText("");
             JScrollPane scrollPane = new JScrollPane(CodeTextArea);
             MiddleMiddle.add(scrollPane,BorderLayout.CENTER);
             
             executeButton = new JButton("Execute");
             executeButton.addActionListener(this);
             analyzeButton = new JButton("Analyze");
             analyzeButton.addActionListener(this);
             LowerMiddle.add(executeButton, BorderLayout.SOUTH);
             LowerMiddle.add(analyzeButton, BorderLayout.SOUTH);
 
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
             JPanel MiddleBottom = new JPanel();
             JPanel LowerBottom = new JPanel();
             
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
                 JFileChooser chooser = new JFileChooser("./LispCode");
                 //chooser.setFileFilter(".lisp");
                 FileNameExtensionFilter extfilter = new FileNameExtensionFilter("lisp files", ("lisp"),("lsp"),("txt"));
                 chooser.setFileFilter(extfilter);
                 
                 
                 int returnVal = chooser.showOpenDialog(LispIntGUI.this);
                 // if file chosen, display file contents
                 if (returnVal == JFileChooser.APPROVE_OPTION){
                   FilePath = chooser.getSelectedFile().getPath();
                   if (FilePath.endsWith("lsp") | FilePath.endsWith("txt") | FilePath.endsWith("lisp")){
                   // System.out.println(FilePath);
                   DisplayCode SourceCodeObject =new DisplayCode(FilePath);
                   String Code=SourceCodeObject.DisplaySource();
                   CodeTextArea.setText(Code);
                 }else{
                       JOptionPane.showMessageDialog(null, "Select a valid file: .lsp, .lisp or .txt", "Warning Message", JOptionPane.WARNING_MESSAGE);
                 }
                   }
             } // End if selectButton
             
             if (ae.getSource()==analyzeButton)
             {	
                 // Display Code Analysis
                 if (CodeTextArea.getText().equals("")){
                         JOptionPane.showMessageDialog(null, "Enter Lisp Code or Select Lisp File!", "Warning Message", JOptionPane.WARNING_MESSAGE);
                 }else if (OutputTextArea.getText().equals("")){
                         JOptionPane.showMessageDialog(null, "First EXECUTE lisp code to display analysis", "Warning Message", JOptionPane.WARNING_MESSAGE);
                 }       
                 else{
                     // Display Analysis
                     try{    
                     FileReader reader = new FileReader("./analysis");
                     Scanner in = new Scanner(reader);
                     OutputTextArea.setText("");
                     while (in.hasNextLine()) { 
                         OutputTextArea.setForeground(Color.blue);
                         OutputTextArea.append(in.nextLine());
                         OutputTextArea.append("\n");
                     }
                     reader.close(); 
                     }catch(IOException e){
                         JOptionPane.showMessageDialog(null, "Error on analysis file \n", "Error Message", JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
             
             if (ae.getSource()==executeButton){
                 
                 // Redirect errors 
                 LispIntRun.errorsfile = new File("./errorlog");
                 LispIntRun.errorsfile.deleteOnExit();
                 try {
                     LispIntRun.efos = new FileOutputStream(LispIntRun.errorsfile);
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(LispIntGUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 LispIntRun.eps = new PrintStream(LispIntRun.efos);
                 System.setErr(LispIntRun.eps);
                 
                 // Create Analysis
                 LispIntRun.file = new File("./analysis");
                 LispIntRun.file.deleteOnExit();
                 try {
                     LispIntRun.fos = new FileOutputStream(LispIntRun.file);
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(LispIntGUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 LispIntRun.ps = new PrintStream(LispIntRun.fos);
                 System.setOut(LispIntRun.ps);
                 
                 // Execute lisp code
                 try {
                     PrintWriter writerout = null; 
                     writerout = new PrintWriter("./LispSource"); 
                     if (CodeTextArea.getText().equals("")){
                         JOptionPane.showMessageDialog(null, "Enter Lisp Code or Select Lisp File!", "Warning Message", JOptionPane.WARNING_MESSAGE);
                     }    
                     writerout.println(CodeTextArea.getText());
                     writerout.close();
                     CharStream cs= new ANTLRFileStream("./LispSource");
                     LispLexer lexer = new LispLexer (cs); 
                     CommonTokenStream tokens = new CommonTokenStream(lexer);
                     LispParser parser = new LispParser(tokens); 
                     try {
                         parser.prog();
                     }catch(RecognitionException ex) {
                         Logger.getLogger(LispIntGUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }catch(IOException e){
                     JOptionPane.showMessageDialog(null, "Location not found. \n", "Error Message", JOptionPane.ERROR_MESSAGE); 
                     System.out.println("Location not found");
                     System.exit(0);
                 }
                 
                 // Display Code Output 
                     DisplayCode SourceCodeObject =new DisplayCode();
                     String Results=SourceCodeObject.DisplayOutput();
                     OutputTextArea.setForeground(Color.black);
                     OutputTextArea.setText(Results);
                 
                 // Display errors
                     try{    
                     FileReader reader = new FileReader("./errorlog");
                     Scanner in = new Scanner(reader);
                     //OutputTextArea.setText("");
                     while (in.hasNextLine()) {
                         OutputTextArea.setForeground(Color.red);
                         OutputTextArea.append(in.nextLine());
                         OutputTextArea.append("\n");
                     }
                     reader.close(); 
                     }catch(IOException e){
                         JOptionPane.showMessageDialog(null, "Error on syntax \n", "Error Message", JOptionPane.ERROR_MESSAGE);
                     }
                 
             } // End if executeButton 
             
             if (ae.getSource()==saveButton)
             {	    	
                 // Save the input code and the results to a file
                 JFileChooser chooser = new JFileChooser(".");
                 chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
                 chooser.showSaveDialog(null);
                 String savepath = chooser.getSelectedFile().getAbsolutePath();
                 try {
                     PrintWriter writerout = null; 
                     writerout = new PrintWriter(savepath+".txt"); 
                     writerout.println("INPUT CODE:");
                     writerout.println("---------------------------------------------------\n");
                     writerout.println(CodeTextArea.getText());
                     writerout.println("\nOUTPUT:");
                     writerout.println("---------------------------------------------------");
                     JOptionPane.showMessageDialog(null, "File saved.", "Information Message", JOptionPane.INFORMATION_MESSAGE);
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

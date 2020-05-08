 import javax.swing.BoxLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Dimension;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.*;
 import java.awt.datatransfer.*;
 import java.awt.Toolkit;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 /**
  * Class responsible for the view to the user. 
  * Handles input and ouput from and to the user.
  */
 public class View extends JFrame {
 
     private JButton btnCalculate;
     private JButton btnMatrix;
     private JButton btnCopy;
     private JButton btnMultiply;
     private JButton btnGauss;
     private JButton btnAdd;
     private JButton btnDeterminant;
     private JButton btnInvert;
     private JPanel backgroundPanel;
     private JPanel mainPanel;
     private JPanel buttonPanel;
     private JPanel operationsPanel;
     private JTextField txtInput;
     private JTextArea txtOutput;
     private InputParser parser;
     
     /**
      * Class constructor. Initializes GUI
      */
     public View(InputParser parser) {
         super("Matrix");
         this.parser = parser;
     }
     
     /**
      * Create the GUI and display to the user.
      */
     public void createGUI() {
         setPreferredSize(new Dimension(600, 600));
         backgroundPanel = new JPanel();
         backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
         mainPanel = new JPanel();
         mainPanel.setLayout(new FlowLayout());
         
         Font f = new Font("Verdana", Font.BOLD, 12);
 
         txtInput = new JTextField();
         txtInput.setPreferredSize(new Dimension(400, 30));
         txtInput.setFont(f);
         txtInput.addKeyListener(new KeyAdapter() {
             public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     btnCalculate.doClick();
                 }
             }
         });
         mainPanel.add(txtInput);
 
         btnCalculate = new JButton("=");
         btnCalculate.addActionListener(new onCalculateClick());
         btnCalculate.setAlignmentX(Component.RIGHT_ALIGNMENT);
         mainPanel.add(btnCalculate);
 
         backgroundPanel.add(mainPanel);
 
         buttonPanel = new JPanel();
         buttonPanel.setLayout(new FlowLayout());
 
         btnMatrix = new JButton("Ny matris");
         btnMatrix.addActionListener(new onMatrixClick());
         buttonPanel.add(btnMatrix);
 
         btnCopy = new JButton("Kopiera");
         btnCopy.addActionListener(new onCopyClick());
         buttonPanel.add(btnCopy);
 
         backgroundPanel.add(buttonPanel);
 
         operationsPanel = new JPanel();
         operationsPanel.setLayout(new FlowLayout());
 
         btnMultiply = new JButton("*");
         btnMultiply.addActionListener(new onMultiplyClick());
         operationsPanel.add(btnMultiply);
 
         btnAdd = new JButton("+");
         btnAdd.addActionListener(new onAddClick());
         operationsPanel.add(btnAdd);
 
         btnGauss = new JButton("Gaussa");
         btnGauss.addActionListener(new onGaussClick());
         operationsPanel.add(btnGauss);
 
         btnInvert = new JButton("^-1");
         btnInvert.addActionListener(new onInvertClick());
         operationsPanel.add(btnInvert);
 
        btnDeterminant = new JButton("det(M)");
         btnDeterminant.addActionListener(new onDeterminantClick());
         operationsPanel.add(btnDeterminant);
 
         backgroundPanel.add(operationsPanel);
 
         txtOutput = new JTextArea();
         txtOutput.setFont(f);
         txtOutput.setEditable(false);
         backgroundPanel.add(txtOutput);
 
         setContentPane(backgroundPanel);
         pack();
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         displayHelp();
     }
 
     private void displayHelp(){
         final String fileName = "help.txt";
          
         int errCode = 0; // Unix error handling
         BufferedReader file = null;
         try {
             file = new BufferedReader(new FileReader(fileName));
             String help = read(file);
             txtOutput.setText(help);
         } catch (IOException e) {
                System.err.printf("%s: %s%n", fileName, e);
                 errCode = 1;
         } finally {
             try {
                 if (file != null) {
                     file.close();
                 }
             } catch (IOException e) {
                 System.err.printf("%s: %s%n", fileName, e);
                 errCode = 1;
             }
             if (errCode != 0) {
                 System.exit(errCode);
             }
         }
     }
         
     private  String read( BufferedReader in)
 	 throws IOException {
 	        String line;
 	        StringBuilder lines = new StringBuilder();
 	        while ((line = in.readLine()) != null) {
 	            lines.append(line + "\n");
 	        }
 	        return lines.toString();
 	}
     
 
     /**
      * Handle click on calculate button.
      */
     private class onCalculateClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData(txtInput.getText()); 
             // What to do with output? hashmap for later?
             // ...
             txtOutput.setText(output);
             
             
         }
     }
 
     /**
      * Handle click on copy button.
      */
     private class onDeterminantClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData("determinant", txtInput.getText());
             txtOutput.setText(output);
         }
     }
 
     /**
      *
      */
     private class onGaussClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData("gauss", txtInput.getText());
             txtOutput.setText(output);
         }
     }
 
     /**
      *
      */
     private class onInvertClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData("invert", txtInput.getText());
             txtOutput.setText(output);
         }
     }
 
     /**
      *
      */
     private class onMultiplyClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData("multiply", txtInput.getText());
             txtOutput.setText(output);
         }
     }
 
     /**
      *
      */
     private class onAddClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData("add", txtInput.getText());
             txtOutput.setText(output);
         }
     }
 
     /**
      *
      */
     private class onCopyClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
             cb.setContents(new StringSelection(txtOutput.getText()), null);
         }
     }
 
     /**
      * Handle click on matrix button.
      */
     private class onMatrixClick implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             String output = parser.parseData(txtInput.getText());
             txtOutput.setText(output);
         }
     }
 }

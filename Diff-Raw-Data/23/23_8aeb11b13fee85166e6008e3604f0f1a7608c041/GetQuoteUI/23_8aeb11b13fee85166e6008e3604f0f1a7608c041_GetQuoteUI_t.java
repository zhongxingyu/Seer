 package main.java;
 
 /**
  * Created with IDEA.
  * User: Alan Derrick
  * Date: 10/13/12
  * Time: 5:10 PM
  */
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 
 
 public class GetQuoteUI extends JFrame {
 
     String symbol;
 
     public JPanel createContentPane (){
 
         /**
          * Create a bottom JPanel to place everything on.
          */
         JPanel totalGUI = new JPanel();
         totalGUI.setLayout(null);
 
 
         /**
          * Create a Panel to contain the "Enter Symbol:" & "Result:" JLabels
          */
         JPanel titlePanel = new JPanel();
         titlePanel.setLayout(null);
         titlePanel.setLocation(10, 0);
         titlePanel.setSize(250, 30);
         totalGUI.add(titlePanel);
 
         /**
          * Create JLabel, in red, on left that says Enter Symbol:
          */
         JLabel redLabel = new JLabel("Enter Symbol:");
         redLabel.setLocation(0, 0);
         redLabel.setSize(150, 40);
         redLabel.setHorizontalAlignment(10);
         redLabel.setForeground(Color.red);
         titlePanel.add(redLabel);
 
         /**
          * Create JLabel, in blue, on right that says Result:
          */
         JLabel blueLabel = new JLabel("Result:");
         blueLabel.setLocation(110, 0);
         blueLabel.setSize(150, 40);
         blueLabel.setHorizontalAlignment(10);
         blueLabel.setForeground(Color.blue);
         titlePanel.add(blueLabel);
 
 
         /**
          * Create a Panel (textPanel) to contain the text fields.
          */
         JPanel textPanel = new JPanel();
         textPanel.setLayout(null);
         textPanel.setLocation(10, 40);
         textPanel.setSize(450, 30);
         totalGUI.add(textPanel);
 
         /**
          * Create text field for user input
          * Place inputField into textPanel
          */
         final JTextField inputField = new JTextField(16);
         inputField.setLocation(0, 0);
         inputField.setSize(100, 30);
         inputField.setHorizontalAlignment(0);
         textPanel.add(inputField);
 
         /**
          * Create text field for output of results
          * Place outputField into textpanel
          */
         final JTextField outputField = new JTextField(16);
         outputField.setLocation(110, 0);
         outputField.setSize(350, 30);
         outputField.setHorizontalAlignment(0);
         textPanel.add(outputField);
 
 
         /**
          * Create a Panel to contain "get price" and "quit" JButtons.
          */
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(null);
         buttonPanel.setLocation(10, 80);
         buttonPanel.setSize(250, 70);
         totalGUI.add(buttonPanel);
 
 
         /**
          * Create "Get Price" button using the syntax used before.
          * Create a text tool tip if you mouse over the button
          * Give button action: put text from inputField into variable symbol (of type String)
          * Adds button to the buttonPanel
          */
         JButton priceButton = new JButton("Get Price");
         priceButton.setLocation(0, 0);
         priceButton.setSize(100, 30);
         priceButton.setToolTipText("Pulls stock quote");
         priceButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {

                 symbol = inputField.getText();

                 try {
                    String downloadedInfo;
                    downloadedInfo = GetStockQuote.getQuote(symbol);
                    outputField.setText(downloadedInfo);
                 }
                 catch(IOException exception)
                 {
                     System.out.println(exception);
                 }
             }
         });
         buttonPanel.add(priceButton);
 
         /**
          * Create "Quit" button using the syntax used before.
          * Create a text tool tip if you mouse over the button
          * Give button action: to quit the application
          * Adds button to the buttonPanel
          */
         JButton quitButton = new JButton("Quit");
         quitButton.setLocation(120, 0);
         quitButton.setSize(100, 30);
         quitButton.setToolTipText("Exits the application");
         quitButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 System.exit(0);
             }
         });
         buttonPanel.add(quitButton);
 
 
         totalGUI.setOpaque(true);
         return totalGUI;
     } // end createContentPane
 
     private static void createAndShowGUI() {
 
         JFrame.setDefaultLookAndFeelDecorated(true);
         JFrame frame = new JFrame("StockQuote v1.0");
 
         /**
          * Create and set up the content pane.
          * Sets it up so that if the JFrame is closed by a method other than the quit
          * button that the application will still end.
          */
         GetQuoteUI demo = new GetQuoteUI();
         frame.setContentPane(demo.createContentPane());
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(550, 190);
         frame.setVisible(true);
     } // end createAndShowGUI
 
 
     public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     } // end main
 } // end class GetQuoteUI

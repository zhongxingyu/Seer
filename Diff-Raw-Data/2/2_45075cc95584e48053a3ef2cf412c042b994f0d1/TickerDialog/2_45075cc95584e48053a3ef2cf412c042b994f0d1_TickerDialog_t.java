 /**
  *
  */
 package edu.brown.cs32.atian.crassus.gui.tickerdialog;
 
 import edu.brown.cs32.atian.crassus.backend.StockList;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import edu.brown.cs32.atian.crassus.gui.CrassusButton;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 
 /**
  * @author Matthew
  *
  */
 @SuppressWarnings("serial")
 public class TickerDialog extends JDialog {
 
     /**
      * @author Matthew
      *
      */
     public class OkListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             setVisible(false);
             if (listener != null) {
                 listener.tickerDialogClosedWithTicker(text.getText());
             }
             dispose();
         }
     }
 
     public class CancelListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             dispose();
         }
     }
 
     private class UserInpListener implements KeyListener {
 
         //JTextArea suggestions;
         JTextField userInp;
         JComboBox<String> suggBox;
 
         public UserInpListener(JTextField f, JComboBox<String> b) {
             //this.suggestions = a;
             this.userInp = f;
             this.suggBox = b;
         }
 
         @Override
         public void keyPressed(KeyEvent arg0) {
         }
 
         /**
          * Autocorrect begin here
          *
          * @param e
          */
         @Override
         public void keyReleased(KeyEvent e) {
             //suggestions.setText("");
 
             String inputString = userInp.getText();
             
             inputString = inputString.trim();
             if (inputString.length() == 0) {
                 String sug[] = {""};
                 suggBox.setModel(new DefaultComboBoxModel<String>(sug));
                 suggBox.hidePopup();
                 suggBox.setVisible(true);
                 return;
             }
             //if inupt string is empty i.e. text box is empty, then dont call methods, to avoid error
             if (inputString.length() > 0) {
                         List<String> tickerSugg = stockList.getTickerSuggestion(inputString);
                         String sug[] = new String[tickerSugg.size()];
                                 tickerSugg.toArray(sug);
                         suggBox.setModel(new DefaultComboBoxModel<String>(sug));
 
                         suggBox.showPopup();
                         suggBox.setVisible(true);
                     
 
             }
 
         }
 
         @Override
         public void keyTyped(KeyEvent arg0) {
         }
     }
     
     private class ItemCListener implements ActionListener {
 
         JTextField userInp;
         JComboBox<String> suggBox;
 
         public ItemCListener(JTextField f, JComboBox<String> b) {
             this.userInp = f;
             this.suggBox = b;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             userInp.setText(suggBox.getSelectedItem().toString());
         }
     }
     
     private JTextField text;
     private JComboBox<String> tickerSugg;
     private TickerDialogCloseListener listener;
     private StockList stockList;
 
     public TickerDialog(JFrame frame, StockList stockList) {
         super(frame, "Add Ticker");
 
 
         this.setLayout(new BorderLayout());
 
         text = new JTextField();
         text.setMinimumSize(new Dimension(50, 15));
         text.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createEmptyBorder(10, 10, 0, 10),
                 BorderFactory.createEtchedBorder()));
         text.addActionListener(new OkListener());
         this.add(text, BorderLayout.NORTH);
 
         tickerSugg = new JComboBox<String>();
         //tickerSugg.setSize(new Dimension(25, 15));
         tickerSugg.setMinimumSize(new Dimension(25, 15));
        tickerSugg.setBackground(Color.WHITE);
        tickerSugg.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
         KeyListener u1 = new UserInpListener(this.text, this.tickerSugg);
         this.text.addKeyListener(u1);
         ItemCListener cblisteners1 = new ItemCListener(this.text, this.tickerSugg);
         this.tickerSugg.addActionListener(cblisteners1);
         this.add(tickerSugg, BorderLayout.CENTER);
         
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new FlowLayout());
         buttonPanel.setBackground(Color.WHITE);
 
         JButton okButton = new CrassusButton("OK");
         okButton.addActionListener(new OkListener());
         buttonPanel.add(okButton);
         JButton cancelButton = new CrassusButton("CANCEL");
         cancelButton.addActionListener(new CancelListener());
         buttonPanel.add(cancelButton);
 
         this.add(buttonPanel, BorderLayout.SOUTH);
 
         this.setModal(true);
 
         this.pack();
         this.setResizable(false);
         
         this.stockList = stockList;
     }
 
     
     public void setTickerDialogCloseListener(TickerDialogCloseListener listener) {
         this.listener = listener;
     }
 }

 /**
  *
  */
 package edu.brown.cs32.atian.crassus.gui.dialogs;
 
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
 import javax.swing.ComboBoxEditor;
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
                 listener.tickerDialogClosedWithTicker(searchbox.getEditor().getItem().toString());
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
 
     /**
      * responds to the user pressing a key. Updates the autocomplete suggestions
      *
      * @author mbs6
      *
      */
     public class UserTypedListener implements KeyListener {
 
         @Override
         public void keyTyped(KeyEvent e) {
             //stub method 
         }
 
         @Override
         public void keyPressed(KeyEvent e) {
             //stub method
         }
 
         @Override
         public void keyReleased(KeyEvent e) {
             if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_LEFT) {
                 //don't do anything for arrow keys
             } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                 //updateModel();
                 setVisible(false);
                 if (listener != null) {
                     listener.tickerDialogClosedWithTicker(searchbox.getEditor().getItem().toString());
                 }
                 dispose();
             } else if (e.getKeyCode() != KeyEvent.CHAR_UNDEFINED) {
                 String itemText = cbe.getItem().toString();//stores current dialogue string so it can be fixed later
                 JTextField jtf = (JTextField) cbe.getEditorComponent();
                 int cursorPosition = jtf.getCaretPosition();//stores caret position so it can be fixed later
                 updateModel();
                 cbe.setItem(itemText);//replaces dialogue string
                 jtf.setCaretPosition(cursorPosition);//fixes caret position
             }
         }
     }
 
     /**
      * Updates the model to give references to the string currently stored
      * within the dialogue box.
      */
     public void updateModel() {
 
         String inputString = searchbox.getEditor().getItem().toString();
 
         inputString = inputString.trim();
 
         //if inupt string is empty i.e. text box is empty, then dont call methods, to avoid error
         if (inputString.length() > 0) {
             List<String> tickerSugg = stockList.getTickerSuggestion(inputString);
             String sug[] = new String[tickerSugg.size()];
             tickerSugg.toArray(sug);
             model.removeAllElements();
 
             for (String elem : tickerSugg) {
                 model.addElement(elem);
             }
             //fix popup size
             searchbox.hidePopup();
             searchbox.showPopup();
 
         }
     }
     private JComboBox<String> searchbox;
     private ComboBoxEditor cbe;
     private DefaultComboBoxModel<String> model;
     private TickerDialogCloseListener listener;
     private StockList stockList;
 
     public TickerDialog(JFrame frame, StockList stockList) {
         super(frame, "Add Ticker");
 
         this.setLayout(new BorderLayout());
 
         searchbox = new JComboBox<String>();
         searchbox.setMinimumSize(new Dimension(25, 15));
         searchbox.setBackground(Color.WHITE);
        searchbox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         model = new DefaultComboBoxModel<String>();
         searchbox.setEditable(true);
         searchbox.setModel(model);
         cbe = searchbox.getEditor();
         cbe.getEditorComponent().addKeyListener(new UserTypedListener());
  
        this.add(searchbox, BorderLayout.CENTER);
 
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

 package lt.banelis.aurelijus.data;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 /**
  * Text representation.
  *
  * @author Aurelijus Banelis
  */
 public class Text extends AbstractDataStructure {
     private static final int BIT_16 = 32768;
     private List<Boolean> data = new LinkedList<Boolean>();
     private boolean shiftNeeded = false;
     private JTextArea text = new JTextArea();
     
     public Text(boolean inputEnabled) {
         super(inputEnabled);
         if (inputEnabled) {
             initialiseEditable();
         } else {
             initialiseVisible();
         }
     }
     
     
     /*
      * Data storage
      */
     
     @Override
     protected void putDataImplementation(Collection<Boolean> data) {
         this.data.addAll(data);
         updateText();
     }
     
     private void updateText() {
         int modulus = viewAllData().size() % Character.SIZE;
         if (shiftNeeded && modulus != 0) {
             Collection<Boolean> shifted = shift(viewAllData(), -modulus);
             text.setText(toText(shifted));
         } else {
             text.setText(toText(viewAllData()));
         }        
     }
     
     private Collection<Boolean> shift(Collection<Boolean> data, int difference) {
         if (difference > 0 && difference < data.size()) {
             int size = data.size() - difference;
             ArrayList<Boolean> result = new ArrayList<Boolean>(size);
             int i = 0;
             for (Boolean bit : data) {
                 if (i >= difference) {
                     result.add(bit);
                 } else {
                     i++;
                 }
             }
             return result;
         } else if (difference < 0 && -difference < data.size()) {
             int size = data.size() + difference;
             ArrayList<Boolean> result = new ArrayList<Boolean>(size);
             int i = 0;
             for (Boolean bit : data) {
                 if (i < size) {
                     result.add(bit);
                 } else {
                     break;
                 }
                 i++;
             }
             return result;
         } else {
             return data;
         }
     }
 
     @Override
     protected Collection<Boolean> viewData() {
         return data;
     }
 
     @Override
     protected Collection<Boolean> retrieveDataImplementation() {
         Collection<Boolean> toRetrieve = data;
         data = new LinkedList<Boolean>();
         return toRetrieve;
     }
 
     @Override
     public void resetOwn() {
         data = new LinkedList<Boolean>();
         text.setText("");
         text.requestFocus();
     }
 
     
     /*
      * Data transformation
      */
     
     protected static String toText(Collection<Boolean> data) {
         StringBuilder result = new StringBuilder();
         char word = 0;
         int upper = BIT_16;
         int i = 0;
         for (Boolean bit : data) {
             if (i != 0 && i % Character.SIZE == 0) {
                 result.append(word);
                 word = 0;
                 upper = BIT_16;
             }
             if (bit) {
                 word += upper;
             }
             upper >>>= 1;
             i++;
         }
         result.append(word);
         return result.toString();
     }
     
     protected static List<Boolean> toBinary(String text) {
         LinkedList<Boolean> result = new LinkedList<Boolean>();
         for (int i = 0; i < text.length(); i++) {
             char word = text.charAt(i);
             int bit = BIT_16;
             for (int j = 0; j < Character.SIZE; j++) {
                 result.add((word & bit) != 0);
                 bit >>>= 1;
             }
         }
         return result;
     }
     
     
     /*
      * Graphical user interface
      */
     
     private void initialiseEditable() {
         JPanel buttons = new JPanel();
         buttons.setLayout(new BorderLayout());
         JButton send = new JButton("Į kanalą");
         send.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 putData(toBinary(text.getText()));
             }
         });
         JButton finalize = new JButton("Paskutinis");
         finalize.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 putData(dataToSynchronize());
             }
         });
         finalize.setToolTipText("Sintėjo-gavėjo sinchronizacijai");
         buttons.add(send, BorderLayout.CENTER);
         buttons.add(finalize, BorderLayout.EAST);
         
         
         setLayout(new BorderLayout());
         JScrollPane pane = new JScrollPane(text);
         add(pane, BorderLayout.CENTER);
         add(buttons, BorderLayout.SOUTH);
         JPanel binary = getStreamPanel();
         add(binary, BorderLayout.NORTH);
     }
     
     private void initialiseVisible() {
         text.setEditable(false);
         final JPanel binary = getStreamPanel();
         final String tooltip = "Du kartus bakstelėkite, norėdami pakeisti " +
                                "viso/dalinio vektoriaus rodymą";
         text.setToolTipText(tooltip);
         text.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() > 1) {
                     shiftNeeded = !shiftNeeded;
                     String mode;
                     if (shiftNeeded) {
                         mode = "Rodomi tik pilni. ";
                     } else {
                         mode = "Rodomi ir dalinai gauti. ";
                     }
                     binary.setToolTipText(mode + tooltip);
                     updateText();
                 }
             }
         });
         final JCheckBox complete = new JCheckBox("Tik pilnus");
         complete.setToolTipText("Ingoruoti vektorius trumnpesnius už raidės " + 
                 "ilgį ( " + Character.SIZE + ")");
           binary.setLayout(new FlowLayout(FlowLayout.LEFT));
         setLayout(new BorderLayout());
         JScrollPane pane = new JScrollPane(text);
         add(pane, BorderLayout.CENTER);
         add(binary, BorderLayout.SOUTH);
     }
 }

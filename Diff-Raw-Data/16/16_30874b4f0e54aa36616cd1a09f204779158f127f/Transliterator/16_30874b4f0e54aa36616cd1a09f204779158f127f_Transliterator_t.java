 import org.romppu.translit.TranslitDocumentException;
 import org.romppu.translit.dictionary.TranslitDictionaryFactory;
 import org.romppu.translit.document.TranslitDocument;
 import org.romppu.translit.document.TranslitDocumentFactory;
 import org.romppu.translit.swing.document.TranslitDocumentFilter;
 
 import javax.swing.*;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.MessageFormat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: roman
  * Date: 7.2.2013
  * Time: 17:51
  * To change this template use File | Settings | File Templates.
  */
 public class Transliterator extends JFrame {
     private static final String POS_STATUS_STRING_PATTERN = "{0,number,#}:{1,number,#}";
     private static final String FORMAT_STATUS_STRING_PATTERN = "{0}";
 
     private final JTextPane textArea = new JTextPane();
     private final JToolBar toolBar = new JToolBar();
     private final JToggleButton button = new JToggleButton("Translit mode");
     private final TranslitDocumentFilter documentFilter = new TranslitDocumentFilter();
     private final TranslitDocument translitDocument = TranslitDocumentFactory.newInstance().newTranslitDocument();
     private final JLabel formatAtPos = new JLabel();
     private final JLabel atPos = new JLabel();
 
 
     public Transliterator() {
         super("Transliterator");
         documentFilter.setTranslitDocument(translitDocument);
         documentFilter.setTranslitForeground(Color.black);
         documentFilter.setTextForeground(Color.gray);
         setSize(400, 200);
 
         setLocationByPlatform(true);
         textArea.registerKeyboardAction(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 button.doClick();
             }
         }, KeyStroke.getKeyStroke("control T"), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         getContentPane().setLayout(new BorderLayout());
 
         getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
         toolBar.add(button);
         getContentPane().add(toolBar, BorderLayout.NORTH);
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 documentFilter.setTranslitMode(button.isSelected());
             }
         });
         DefaultStyledDocument defaultStyledDocument = new DefaultStyledDocument();
         defaultStyledDocument.setDocumentFilter(documentFilter);
         textArea.setDocument(defaultStyledDocument);
 
         JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
         statusPanel.setBorder(BorderFactory.createEtchedBorder());
         formatAtPos.setPreferredSize(new Dimension(150, 18));
         statusPanel.add(atPos);
         statusPanel.add(new JSeparator());
         statusPanel.add(formatAtPos);
         getContentPane().add(statusPanel, BorderLayout.SOUTH);
         updateStatus(0);
         textArea.addCaretListener(new CaretListener() {
             @Override
             public void caretUpdate(CaretEvent e) {
                 updateStatus(e.getDot());
 
             }
         });
     }
 
     public static int getRow(int pos, JTextComponent editor) {
         int rn = (pos==0) ? 1 : 0;
         try {
             int offs=pos;
             while( offs>0) {
                 offs= Utilities.getRowStart(editor, offs)-1;
                 rn++;
             }
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         return rn;
     }
 
     public static int getColumn(int pos, JTextComponent editor) {
         try {
             return pos-Utilities.getRowStart(editor, pos)+1;
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         return -1;
     }
 
     private void updateStatus(int index) {
         try {
             atPos.setText(MessageFormat.format(POS_STATUS_STRING_PATTERN, getRow(index, textArea), getColumn(index, textArea)));
             formatAtPos.setText(MessageFormat.format(FORMAT_STATUS_STRING_PATTERN,
                     translitDocument.getSize() == 0 || index >= translitDocument.getSize() ? "NONE" :
                             translitDocument.isTranslitAt(index) ? "TRANSLIT" : "TEXT"));
 
         } catch (TranslitDocumentException e1) {
             e1.printStackTrace();
         }
 
     }
 
     public static void main(String... params) {
         //System.setProperty(TranslitDictionaryFactory.TRANSLIT_DICT, "dictionary_def.xml");
         new Transliterator().setVisible(true);
     }
 }

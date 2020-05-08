 /*
  * ObjectSelector - Object selection library for Java
  * Copyright (C) 2013 Fabian Prasser
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 
 import de.linearbits.objectselector.ICallback;
 import de.linearbits.objectselector.Selector;
 import de.linearbits.objectselector.SelectorBuilder;
 import de.linearbits.objectselector.SelectorTokenizer;
 import de.linearbits.objectselector.util.ObjectAccessor;
 
 /**
  * Example for a user interface with syntax highlighting
  * @author Fabian Prasser
  */
 public class Example6 {
 
     /** Thread for updates */
     private static Runnable updater = new Runnable(){
         
         @Override
         public void run() {
             
             String previous = null;
             ObjectAccessor<Element> accessor = new ObjectAccessor<Element>(Element.class);
             
             while (true){
                 
                 try { Thread.sleep(INTERVAL); } 
                 catch (InterruptedException e) { /* Ignore */ }
                 
                 if (query != previous){
                     previous = query;
                     
                     try {
                         Selector<Element> selector = new SelectorBuilder<Element>(accessor, query).build();
                         int count = 0;
                         for (Element element : elements) {
                             if (selector.isSelected(element)) count++;
                         }
                        labelFeedback.setText("Matching documents: "+count+ " of " + elements.size());
                         buttonOk.setEnabled(true);
                     } catch (Exception e) {
                         labelFeedback.setText(e.getMessage());
                         buttonOk.setEnabled(false);
                     }
                 }
             }
         }
     };
     
     /** Callback for the tokenizer */
     private static ICallback callback = new ICallback() {
 
         public void and(int start, int length)  { setStyle(start, length, styleLogical);    }
         public void begin(int start)            { setStyle(start, 1,      stylePrecedence); }
         public void end(int start)              { setStyle(start, 1,      stylePrecedence); }
         public void equals(int start)           { setStyle(start, 1,      styleOperator);   }
         public void field(int start, int length){ setStyle(start, length, styleField);      }
         public void geq(int start, int length)  { setStyle(start, length, styleOperator);   }
         public void greater(int start)          { setStyle(start, 1,      styleOperator);   }
         public void invalid(int start)          { /*Ignore*/                                }
         public void leq(int start, int length)  { setStyle(start, length, styleOperator);   }
         public void less(int start)             { setStyle(start, 1,      styleOperator);   }
         public void or(int start, int length)   { setStyle(start, length, styleLogical);    }
         public void value(int start, int length){ setStyle(start, length, styleValue);      }
     };
 
     /** Interval for the update thread */
     private static final int             INTERVAL = 500;
 
     /** Style for syntax highlighting*/
     private static Style                 styleLogical;
     /** Style for syntax highlighting*/
     private static Style                 stylePrecedence;
     /** Style for syntax highlighting*/
     private static Style                 styleOperator;
     /** Style for syntax highlighting*/
     private static Style                 styleField;
     /** Style for syntax highlighting*/
     private static Style                 styleValue;
     /** Style for syntax highlighting*/
     private static Style                 styleMain;
 
     /** The swing document*/
     private static DefaultStyledDocument document;
     /** Swing control*/
     private static JLabel                labelFeedback;
     /** Swing control*/
     private static JButton               buttonOk;
     /** Swing control*/
     private static JButton               buttonCancel;
     
     /** The current query*/
     private static volatile String       query = "";
 
     /** The list of elements to select from*/
     private static List<Element>         elements = new ArrayList<Element>();
     
 
     /**
      * Main entry point
      * @param args
      * @throws InterruptedException
      * @throws InvocationTargetException
      * @throws BadLocationException 
      */
 	public static void main(String[] args) throws InterruptedException, InvocationTargetException, BadLocationException {
 	    
 	    // Use font anti aliasing
 	    System.setProperty("awt.useSystemAAFontSettings","on");
 	    System.setProperty("swing.aatext", "true");
 	    
 	    // Create list of elements
         Random random = new Random();
         for (int i=0; i<100000; i++){
             elements.add(Element.getRandomElement(random));
         }
 	    
 	    // Create frame
 	    JFrame f = new JFrame("ObjectSelector Dialog Example");
 	    
 	    // Create text pane
 	    StyleContext sc = new StyleContext();
 	    document = new DefaultStyledDocument(sc);
 	    JTextPane pane = new JTextPane(document);
 	    
 	    // Builder center panel
 	    JPanel center = new JPanel();
 	    center.setLayout(new BorderLayout());
 	    center.add(new JScrollPane(pane), BorderLayout.CENTER);
 	    
 	    // Build feedback panel
 	    labelFeedback = new JLabel();
 	    center.add(labelFeedback, BorderLayout.SOUTH);
 	    labelFeedback.setText("");
 	    
 	    // Build buttons panel
 	    JPanel buttons = new JPanel();
 	    buttons.setLayout(new GridLayout(0,2));
 	    buttonOk = new JButton("OK");
 	    buttonOk.setEnabled(false);
 	    buttons.add(buttonOk);
 	    buttonCancel = new JButton("Cancel");
 	    buttons.add(buttonCancel, BorderLayout.WEST);
 	    
 	    // Add listeners
         buttonOk.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         buttonCancel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
 	    
 	    // Create all necessary styles
 	    createStyles(sc);
 
         // Bring up frame
         f.getContentPane().setLayout(new BorderLayout());
         f.getContentPane().add(center, BorderLayout.CENTER);
         f.getContentPane().add(buttons, BorderLayout.SOUTH);
         f.setSize(400, 150);
         f.setLocationRelativeTo(null);
         f.setVisible(true);
         
 	    // Register listener
 	    document.addDocumentListener(new DocumentListener(){
             public void insertUpdate(DocumentEvent e)  { update(); }
             public void removeUpdate(DocumentEvent e)  { update(); }
             public void changedUpdate(DocumentEvent e) {}
 	    });
 	    
 	    // Set initial text
 	    document.insertString(0, "('bool'='true' and 'integer'>='50') or 'numeric'<='30'", null);
 	    
 	    // Start threat
 	    Thread t = new Thread(updater);
 	    t.setDaemon(true);
 	    t.start();
 	  }
 	
 	/**
 	 * Reacts on a document update 
 	 */
 	private static void update()   {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     query = document.getText(0, document.getLength());
                     setStyle(0, document.getLength(), styleMain);
                     new SelectorTokenizer<Element>(callback).tokenize(query);
                 } catch (BadLocationException e) {
                     throw new RuntimeException(e);
                 }
             }
         });
 	}
 	
 	/**
 	 * Sets the style on the document
 	 * @param offset
 	 * @param length
 	 * @param style
 	 */
 	private static void setStyle(final int offset, final int length, final Style style) {
 	    document.setCharacterAttributes(offset, length, style, true);
 	}
 
 	/**
 	 * Creates all styles for syntax highlighting
 	 * @param sc
 	 */
     private static void createStyles(StyleContext sc) {
 
         Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
 
         styleMain = sc.addStyle("MainStyle", defaultStyle);
         StyleConstants.setLeftIndent(styleMain, 16);
         StyleConstants.setRightIndent(styleMain, 16);
         StyleConstants.setFirstLineIndent(styleMain, 16);
         StyleConstants.setFontFamily(styleMain, "monospaced");
         StyleConstants.setFontSize(styleMain, 12);
         StyleConstants.setForeground(styleMain, Color.BLACK);
         StyleConstants.setBold(styleMain, false);
 
         styleLogical = sc.addStyle("LogicalStyle", styleMain);
         StyleConstants.setForeground(styleLogical, Color.GRAY);
         StyleConstants.setBold(styleLogical, true);
 
         stylePrecedence = sc.addStyle("PrecedenceStyle", styleMain);
         StyleConstants.setForeground(stylePrecedence, Color.GREEN);
         StyleConstants.setBold(stylePrecedence, true);
 
         styleOperator = sc.addStyle("OperatorStyle", styleMain);
         StyleConstants.setForeground(styleOperator, Color.BLUE);
         StyleConstants.setBold(styleOperator, true);
 
         styleField = sc.addStyle("FieldStyle", styleMain);
         StyleConstants.setForeground(styleField, Color.RED);
         StyleConstants.setBold(styleField, true);
 
         styleValue = sc.addStyle("ValueStyle", styleMain);
         StyleConstants.setForeground(styleValue, Color.DARK_GRAY);
         StyleConstants.setBold(styleValue, true);
     }
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.javascool.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Event;
 import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
import javax.swing.JButton;
 import javax.swing.JPanel;
import javax.swing.JToggleButton;
 import javax.swing.KeyStroke;
 import javax.swing.ToolTipManager;
 import javax.swing.text.BadLocationException;
 import org.fife.ui.autocomplete.AutoCompletion;
 import org.fife.ui.autocomplete.CompletionProvider;
 import org.fife.ui.autocomplete.DefaultCompletionProvider;
 import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rtextarea.Gutter;
 import org.fife.ui.rtextarea.RTextScrollPane;
 import org.fife.ui.rtextarea.ToolTipSupplier;
 import org.javascool.core.ProgletEngine;
 import org.javascool.widgets.ToolBar;
 
 /** Define a JVSEditor
  * Use JVSEditor to edit jvs files, it can be used as a panel
  * @author Philippe VIENNE
  */
 class JVSEditor extends JPanel {
 
     private static final long serialVersionUID = 1L;
    private static boolean completion=true;
     /** The editor */
     private RSyntaxTextArea TextPane;
     /** The scroll pane */
     private RTextScrollPane scrollPane;
     /** La ToolBar */
     private ToolBar toolBar;
 
     /** Create a new JVSEditor
      * Common setup
      */
     public JVSEditor() {
         this.setLayout(new BorderLayout());
         toolBar = new ToolBar();
         TextPane = this.createTextArea();
         TextPane.setSyntaxEditingStyle(org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JAVA);
         JVSAutoCompletionProvider jacp = new JVSAutoCompletionProvider(TextPane);
 
         scrollPane = new RTextScrollPane(TextPane, true);
         Gutter gutter = scrollPane.getGutter();
         gutter.setBorderColor(Color.BLUE);
         toolBar.addTool("Reformater le code", new Runnable() {
 
             @Override
             public void run() {
                 setText(org.javascool.core.JvsBeautifier.run(getText()));
             }
         });
        final JButton completionButton=new JButton();
        completionButton.setText("Désactiver la completion");
        completionButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if(JVSEditor.completion){
                    JVSEditor.completion=false;
                    completionButton.setText("Activer la completion");
                } else {
                    JVSEditor.completion=true;
                    completionButton.setText("Désactiver la completion");
                }
            }
        });
        toolBar.addTool("Activer/Désactiver la complétion", completionButton);
         this.add(toolBar, BorderLayout.NORTH);
         this.add(scrollPane, BorderLayout.CENTER);
         this.setVisible(true);
     }
 
     /** TextArea initialization
      * Creates the text area for this application.
      * @return The text area.
      */
     private RSyntaxTextArea createTextArea() {
         RSyntaxTextArea textArea = new RSyntaxTextArea(25, 70);
         textArea.setCaretPosition(0);
         // textArea.addHyperlinkListener(this);
         textArea.requestFocusInWindow();
         textArea.setMarkOccurrences(true);
         textArea.setTextAntiAliasHint("VALUE_TEXT_ANTIALIAS_ON");
         textArea.setText("");
 
         KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
         if (isMac()) {
             key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.META_MASK);
         }
         KeyStroke copy_key = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK);
         if (isMac()) {
             copy_key = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.META_MASK);
         }
         textArea.getInputMap().put(key,
                 "save");
         textArea.getActionMap().put("save",
                 new AbstractAction() {
 
                     private static final long serialVersionUID = 1L;
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         JVSPanel.getInstance().saveFile();
                     }
                 });
 
         textArea.getInputMap().put(copy_key,
                 "copy");
         textArea.getActionMap().put("copy",
                 new AbstractAction() {
 
                     private static final long serialVersionUID = 1L;
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         getRTextArea().copyAsRtf();
                     }
                 });
 
         return textArea;
     }
 
     /** Tests if on mac. */
     private static boolean isMac() {
         return System.getProperty("os.name").toUpperCase().contains("MAC");
     }
 
     /** Get text into the TextArea
      * @return The code
      */
     public String getText() {
         return TextPane.getText();
     }
 
     /** Set the text
      * @param text The text to write on screen
      */
     public void setText(String text) {
         TextPane.setText(text);
     }
 
     /** Get the RSyntaxTextArea */
     public RSyntaxTextArea getRTextArea() {
         return TextPane;
     }
 
     /** Retourne le RTextScrollPane de l'éditeur */
     public RTextScrollPane getScrollPane() {
         return scrollPane;
     }
 
     public void removeLineSignals() {
         getScrollPane().getGutter().removeAllTrackingIcons();
     }
 
     public void signalLine(int line) {
         Gutter gutter = getScrollPane().getGutter();
         gutter.setBookmarkingEnabled(true);
         ImageIcon icon = null;
         BufferedImage img;
         try {
             img = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/javascool/widgets/icons/error.png"));
             icon = new ImageIcon(img);
         } catch (IOException ex1) {
         }
         try {
             getRTextArea().setCaretPosition(getRTextArea().getLineStartOffset(line - 1));
             getScrollPane().getGutter().addLineTrackingIcon(line - 1, icon);
         } catch (BadLocationException ex) {
             Logger.getLogger(JVSEditor.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private class JVSAutoCompletionProvider extends AutoCompletion {
 
         public JVSAutoCompletionProvider(RSyntaxTextArea TextPane) {
             super(createCodeCompletionProvider());
             setShowDescWindow(true);
             //setParameterAssistanceEnabled(true);
             install(TextPane);
             TextPane.setToolTipSupplier((ToolTipSupplier) this.getCompletionProvider());
             ToolTipManager.sharedInstance().registerComponent(TextPane);
             TextPane.addKeyListener(new KeyListener() {
 
                 @Override
                 public void keyTyped(KeyEvent e) {
                     int ch = e.getKeyChar();
                    if (ch>33&&ch!=127&&ch!=129&&ch!=141&&ch!=143&&ch!=144&&ch!=157&&ch!=160&&JVSEditor.completion) {
                         showPopupWindow();
                     } else {
                         hideChildWindows();
                     }
                 }
 
                 @Override
                 public void keyPressed(KeyEvent e) {
                 }
 
                 @Override
                 public void keyReleased(KeyEvent e) {
                 }
             });
         }
 
         public void showPopupWindow() {
             this.refreshPopupWindow();
             return;
         }
     }
 
     /**
      * Returns the provider to use when editing code.
      *
      * @return The provider.
      */
     public static CompletionProvider createCodeCompletionProvider() {
         DefaultCompletionProvider cp = new DefaultCompletionProvider();
         if(!ProgletEngine.getInstance().getProglet().getCompletion().equals("")){
             System.err.println("loadCompletion");
             JvsXMLCompletion.readCompletionToProvider(ProgletEngine.getInstance().getProglet().getCompletion(), cp);
         }
         JvsXMLCompletion.readCompletionToProvider("org/javascool/gui/completion-macros.xml", cp);
         LanguageAwareCompletionProvider lacp = new LanguageAwareCompletionProvider(cp);
         return lacp;
     }
 }

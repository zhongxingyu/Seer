 package edu.gwu.raminfar.iauthor.ui;
 
 import edu.gwu.raminfar.iauthor.core.Sentence;
 import edu.gwu.raminfar.iauthor.core.TextEditorEvent;
 import edu.gwu.raminfar.iauthor.core.Word;
 import edu.gwu.raminfar.iauthor.nlp.NlpService;
 
 import javax.swing.*;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.Collection;
 
 
 /**
  * Author: Amir Raminfar
  * Date: Sep 25, 2010
  */
 public class TextEditorPane extends JComponent implements CaretListener {
     private final JTextPane textPane = new JTextPane();
     private Collection<AbstractTool> tools;
     private TextEditorEvent lastEvent;
     private static boolean ready = false;
 
     static {
         new SwingWorker<Void, Void>() {
             @Override
             protected Void doInBackground() throws Exception {
                 System.out.println("Initializing NLP Services...");
                 NlpService.detectedSentences("");
                 System.out.println("Completed setting up NLP Services...");
                 return null;
             }
 
             @Override
             protected void done() {
                 ready = true;
             }
         }.execute();
     }
 
     public TextEditorPane() {
         setLayout(new BorderLayout());
         JScrollPane editorScrollPane = new JScrollPane(textPane);
         add(editorScrollPane, BorderLayout.CENTER);
         textPane.addCaretListener(this);
     }
 
     public void setTools(Collection<AbstractTool> tools) {
         this.tools = new ArrayList<AbstractTool>(tools);
     }
 
     public JTextPane getTextPane() {
         return textPane;
     }
 
     public void caretUpdate(CaretEvent e) {
         if (!ready) {
             return;
         }
 
         String text = textPane.getText();
         int len = text.length();
         if (len > 0) {
             // find current paragraph
             int startOfP = Math.max(text.lastIndexOf("\n", e.getDot()), 0);
             int endOfP = text.indexOf("\n", e.getDot());
             if (endOfP == -1) {
                 endOfP = len;
             } else {
                 ++endOfP;
             }
             String paragraph = text.substring(startOfP, endOfP);
 
             if (paragraph.length() > 0) {
                 // find the start of the current paragraph
                 int fromStartOfParagraph = e.getDot() - startOfP;
                 while (paragraph.startsWith("\n")) {
                     paragraph = paragraph.substring(1);
                     --fromStartOfParagraph;
                 }
                 String[] sentences = NlpService.detectedSentences(paragraph);
 
                 TextEditorEvent event = null;
                 if (sentences.length > 0) {
                     // find the current sentence
                     String detectedSentence = findCurrentSentence(fromStartOfParagraph, sentences);
                     int fromStartOfSentence = fromStartOfParagraph - paragraph.indexOf(detectedSentence);
                     Sentence sentence = NlpService.tagSentence(detectedSentence);
 
                     // find the current word
                     Word currentWord = findCurrentWord(fromStartOfSentence, sentence);
                     event = new TextEditorEvent(sentence, currentWord);
                 }
 
 
                 // invoke a new event for each tool
                 // only invoke if word and sentence has changed
                 if (event != null && !event.equals(lastEvent)) {
                     final TextEditorEvent finalEvent = event;
                     for (final AbstractTool tool : tools) {
                         SwingUtilities.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 tool.onTextEvent(finalEvent);
                             }
                         });
                     }
                     lastEvent = finalEvent;
                 }
             }
         }
     }
 
     private String findCurrentSentence(int fromStartOfParagraph, String[] sentences) {
         int l = 0;
         String detectedSentence = null;
         // todo optimize?
         for (String s : sentences) {
             detectedSentence = s;
             l += detectedSentence.length();
             if (fromStartOfParagraph <= l) {
                 break;
             }
         }
         return detectedSentence;
     }
 
     private Word findCurrentWord(int fromStartOfSentence, Sentence sentence) {
         int l = 0;
         Word currentWord = null;
         for (Word word : sentence) {
             currentWord = word;
             l += word.getText().length() + 1 /* space */;
            if (fromStartOfSentence < l) {
                 break;
             }
         }
         return currentWord;
     }
 
 
 }

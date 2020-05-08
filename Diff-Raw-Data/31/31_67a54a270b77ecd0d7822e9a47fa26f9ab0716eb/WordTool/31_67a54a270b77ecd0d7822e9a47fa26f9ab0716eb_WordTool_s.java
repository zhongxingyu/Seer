 package edu.gwu.raminfar.iauthor.ui;
 
 import edu.gwu.raminfar.iauthor.core.TextEditorEvent;
 import edu.gwu.raminfar.iauthor.core.Word;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.apache.lucene.util.Version;
 
 import javax.swing.*;
import javax.swing.text.Caret;
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.RoundRectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 
 /**
  * @author Amir Raminfar
  */
public class WordTool extends AbstractTool implements MouseListener{
     private final IndexSearcher searcher;
     private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
     private final QueryParser parser = new QueryParser(Version.LUCENE_30, "", analyzer);
 
     private TextEditorEvent event;
     private JTextPane textPane;
     private int lastDot;
 
     public WordTool() {
         try {
             searcher = new IndexSearcher(new NIOFSDirectory(new File(getClass().getResource("/lucene/wordnet/index").getFile())));
         } catch (IOException e) {
             ApplicationFrame.logger.log(Level.SEVERE, "Error while reading indexer data for wordnet", e);
             throw new RuntimeException(e);
         }
         setBackground(Color.white);
         addMouseListener(this);
     }
 
     public List<String> findSynonyms(Word word) {
         List<String> synonyms = Collections.emptyList();
         try {
            Query query = parser.parse(String.format("word:%s AND type:%s", word.getText(), word.getType().toString()));
             TopDocs docs = searcher.search(query, 1);
             if (docs.scoreDocs.length == 1) {
                 Document document = searcher.doc(docs.scoreDocs[0].doc);
                 String[] words = document.getValues("synonym");
                 synonyms = Arrays.asList(words);
             }
         } catch (ParseException e) {
             ApplicationFrame.logger.log(Level.WARNING, "Error reading solr data for synonyms", e);
         } catch (CorruptIndexException e) {
             ApplicationFrame.logger.log(Level.WARNING, "Error reading solr data for synonyms", e);
         } catch (IOException e) {
             ApplicationFrame.logger.log(Level.WARNING, "Error reading solr data for synonyms", e);
         }
         return synonyms;
     }
 
     @Override
     public void onTextEvent(TextEditorEvent e) {
         event = e;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 repaint();
             }
         });
     }
 
     @Override
     void setTextPane(JTextPane pane) {
         this.textPane = pane;
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (event != null) {
             List<String> synonyms = findSynonyms(event.getCurrentWord());
             if (synonyms.size() > 0) {
                 Graphics2D gd = (Graphics2D) g;
                 gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 gd.setColor(Color.lightGray);
                 gd.draw(new RoundRectangle2D.Double(10, 10, SIZE.getWidth() - 20, SIZE.getHeight() - 20, 10, 10));
                 gd.setColor(new Color(51, 51, 51));
                 FontMetrics metrics = gd.getFontMetrics();
                 for (int i = 0, synonymsSize = synonyms.size(), h = metrics.getHeight(); i < synonymsSize; i++) {
                    String s = synonyms.get(i);                    
                     gd.drawString(s, 20, 40 + i * h);
                 }
             }
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         String text = textPane.getText();
         int start = text.lastIndexOf(" ", textPane.getCaret().getDot()) + 1;
        int end = text.indexOf(" ", textPane.getCaret().getDot());
         lastDot = textPane.getCaret().getDot();
         textPane.getCaret().setSelectionVisible(false);
         textPane.getCaret().setDot(start);
         textPane.getCaret().moveDot(end);
         textPane.getCaret().setSelectionVisible(true);
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         textPane.getCaret().setDot(lastDot);
         textPane.getCaret().setSelectionVisible(false);
     }
 }

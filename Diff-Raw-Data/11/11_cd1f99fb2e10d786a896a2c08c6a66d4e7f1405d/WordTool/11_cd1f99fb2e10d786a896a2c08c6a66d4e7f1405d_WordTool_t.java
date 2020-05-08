 package edu.gwu.raminfar.iauthor.wordtool;
 
 import edu.gwu.raminfar.animation.Animator;
 import edu.gwu.raminfar.iauthor.core.AbstractTool;
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
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Amir Raminfar
  */
 public class WordTool extends AbstractTool implements MouseListener, MouseMotionListener {
     public static final Logger logger = Logger.getLogger(WordTool.class.getName());
     private final IndexSearcher searcher;
     private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
     private final QueryParser parser = new QueryParser(Version.LUCENE_30, "word", analyzer);
     private final Pattern endings = Pattern.compile(".+(ing|s|ed)$");
     private final Animator animator = new Animator(this);
 
     private JTextPane textPane;
     private int lastDot;
     private List<WordShape> shapes = new ArrayList<WordShape>();
 
     public WordTool() {
        IndexSearcher temp;
         try {
            temp = new IndexSearcher(new NIOFSDirectory(new File(getClass().getResource("/lucene/wordnet/index").getFile())), true);
         } catch (IOException e) {
            try {
            temp = new IndexSearcher(new NIOFSDirectory(new File("./lucene/wordnet/index")), true);
        } catch (IOException e2) {
             logger.log(Level.SEVERE, "Error while reading indexer data for wordnet", e);
             throw new RuntimeException(e);
         }
        }
        searcher = temp;
         setBackground(Color.white);
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
     public List<Word> findSynonyms(Word word) {
         List<Word> synonyms = Collections.emptyList();
         try {
             String text = QueryParser.escape(word.getText());
             StringBuilder sb = new StringBuilder();
             sb.append(text);
             if (word.getType() != Word.Type.UNKNOWN) {
                 sb.append(" AND type:").append(word.getType().toString());
             }
             String q = sb.toString();
             Query query = parser.parse(q);
             logger.info(q);
             TopDocs docs = searcher.search(query, 1);
             if (docs.scoreDocs.length == 1) {
                 Document document = searcher.doc(docs.scoreDocs[0].doc);
                 String[] words = document.getValues("synonym");
                 synonyms = new ArrayList<Word>();
                 for (String w : words) {
                     synonyms.add(new Word(w, word.getType()));
                 }
             } else {
                 Matcher matcher = endings.matcher(word.getText());
                 if (matcher.find()) {
                     return findSynonyms(new Word(word.getText().replaceFirst(matcher.group(1), ""), word.getType()));
                 }
             }
         } catch (ParseException e) {
             logger.log(Level.WARNING, "Error reading SOLR data for synonyms", e);
         } catch (CorruptIndexException e) {
             logger.log(Level.WARNING, "Error reading SOLR data for synonyms", e);
         } catch (IOException e) {
             logger.log(Level.WARNING, "Error reading SOLR data for synonyms", e);
         }
         return synonyms;
     }
 
     @Override
     public void onTextEvent(TextEditorEvent event) {
         List<Word> synonyms = findSynonyms(event.getCurrentWord());
         if (synonyms.size() > 0) {
             if (synonyms.size() > 10) {
                 synonyms = synonyms.subList(0, 10);
             }
             shapes.clear();
             FontMetrics fm = getFontMetrics(getFont());
             WordShape shape = new WordShape(event.getCurrentWord(), fm, 100, 100, 0);
             Point center = new Point((int) (getBounds().getCenterX() - shape.getWrapper().getShape().getBounds().getWidth() / 2), (int) (getBounds().getCenterY() - shape.getWrapper().getShape().getBounds().getHeight() / 2));
             center.y -= 10;
             shapes.add(shape);
             animator.newAnimation(shape.getWrapper()).setDuration(750).moveTo(center).animate();
             double delta = 2 * Math.PI / synonyms.size();
             int h = 105;
             for (int i = 1, synonymsSize = synonyms.size(); i < synonymsSize; i++) {
                 Word syn = synonyms.get(i);
                 double angle = (i) * delta;
                 WordShape _shape = new WordShape(syn, fm, (int) getBounds().getCenterX(), (int) getBounds().getCenterY());
                 int x = (int) ((((Math.cos(angle) * h) + getBounds().getCenterX()) - _shape.getWrapper().getShape().getBounds().getWidth() / 2));
                 int y = (int) (((Math.sin(angle) * h) + getBounds().getCenterY()) - _shape.getWrapper().getShape().getBounds().getHeight() / 2);
                 x -= 20;
                 y -= 20;
                 animator.newAnimation(_shape.getWrapper()).setDuration(900).moveTo(new Point(x, y)).animate();
                 shapes.add(_shape);
             }
         }
     }
 
     @Override
     public void setTextPane(JTextPane pane) {
         this.textPane = pane;
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D gd = (Graphics2D) g;
         gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         if (shapes.size() > 0) {
             int wordX = (int) shapes.get(0).getWrapper().getShape().getBounds().getCenterX();
             int wordY = (int) shapes.get(0).getWrapper().getShape().getBounds().getCenterY();
 
             for (int i = 1, l = shapes.size(); i < l; i++) {
                 WordShape syn = shapes.get(i);
                 gd.drawLine(wordX, wordY, (int) syn.getWrapper().getShape().getBounds().getCenterX(), (int) syn.getWrapper().getShape().getBounds().getCenterY());
             }
 
         }
         for (WordShape ws : shapes) {
             ws.paint(g);
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
         int end = text.indexOf(" ", textPane.getCaret().getDot() + 1);
         if (end == -1) {
             end = text.length();
         }
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
 
     @Override
     public void mouseDragged(MouseEvent e) {
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
         for (WordShape shape : shapes) {
             if (shape.getWrapper().getShape().contains(e.getPoint())) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 return;
             }
         }
 
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
 }

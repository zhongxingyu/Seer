 package edu.gwu.raminfar.iauthor.wikitool;
 
 import edu.gwu.raminfar.iauthor.Utils;
 import edu.gwu.raminfar.iauthor.core.Sentence;
 import edu.gwu.raminfar.iauthor.core.TextEditorEvent;
 import edu.gwu.raminfar.iauthor.core.Word;
 import edu.gwu.raminfar.iauthor.nlp.NlpService;
 import edu.gwu.raminfar.iauthor.ui.AbstractTool;
 import edu.gwu.raminfar.iauthor.ui.ApplicationFrame;
 import edu.gwu.raminfar.iauthor.ui.ToolWrapper;
 import edu.gwu.raminfar.wiki.WikiPage;
 import edu.gwu.raminfar.wiki.WikiSearch;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.apache.lucene.util.Version;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 
 /**
  * @author Amir Raminfar
  */
 public class WikiTool extends AbstractTool {
     // load background
     protected final static BufferedImage BACKGROUND;
 
     static {
         try {
             BACKGROUND = ImageIO.read(ToolWrapper.class.getResource("/images/wiki.png"));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     // lucene
     private final static File index = new File("./lucene/wiki/index");
     private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
     private final QueryParser parser = new QueryParser(Version.LUCENE_30, "noun", analyzer);
     private IndexWriter writer;
 
     // loader gif
     private final JLabel loader = new JLabel(new ImageIcon(this.getClass().getResource("/images/loader.gif")));
    private final JTextPane needMoreLabel = new JTextPane();
     private boolean loading = false;
    private boolean needMoreNouns = false;
 
     // delay thread
     private final Timer timer = new Timer(true);
     private TimerTask task = null;
 
     public WikiTool() {
         try {
             Directory directory = new NIOFSDirectory(index);
             try {
                 writer = new IndexWriter(directory, analyzer, false, IndexWriter.MaxFieldLength.UNLIMITED);
             } catch (FileNotFoundException e) {
                 writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
             }
         } catch (IOException e) {
             ApplicationFrame.logger.log(Level.SEVERE, "Error reading SOLR data for wiki", e);
             throw new RuntimeException(e);
         }
         setBackground(Color.white);
         setPreferredSize(SIZE);
         setMaximumSize(SIZE);
        needMoreLabel.setText("This sentence needs more nouns \nto be evaluated correctly.");
        needMoreLabel.setBackground(null);
        needMoreLabel.setEditable(false);
        needMoreLabel.setBorder(null);
        needMoreLabel.setOpaque(false);
     }
 
     @Override
     public void onTextEvent(final TextEditorEvent event) {
         if (task != null) {
             task.cancel();
             task = null;
         }
         final Set<Word> nouns = event.getSentence().find(Word.Type.NOUN);
 
         // only query wiki if there is more than 3 nouns

         if (nouns.size() > 2) {
            showLoader();
             timer.schedule((task = new TimerTask() {
                 @Override
                 public void run() {
                     try {
                         indexQuery(Utils.join(nouns, " "));
                         List<Document> docs = findSimilarSentences(event.getSentence());
                         JPanel list = new JPanel();
                         list.setOpaque(false);
                         list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));
                         list.setPreferredSize(getSize());
                         for (Document doc : docs) {
                             JLabel f = new JLabel(doc.get("originalSentence"));
                             f.setToolTipText(doc.get("originalSentence") + " from [" + doc.get("url") + "]");
                             list.add(f);
                         }
                         add(list);
                     } catch (IOException e) {
                         ApplicationFrame.logger.log(Level.WARNING, "Error parsing sentences", e);
                     } catch (Exception e) {
                         ApplicationFrame.logger.log(Level.WARNING, "Unknown exception", e);
                     } finally {
                         hideLoader();
                         task = null;
                        needMoreNouns = false;
                     }
                 }
             }), 300);
        } else if (!needMoreNouns) {
            removeAll();
            add(needMoreLabel);
            revalidate();
            repaint();
            needMoreNouns = true;
         }
     }
 
     private List<Document> findSimilarSentences(Sentence sentence) {
         List<Document> sentences = new ArrayList<Document>();
         IndexReader reader = null;
         try {
             reader = writer.getReader();
             IndexSearcher searcher = new IndexSearcher(reader);
             Query query = parser.parse(QueryParser.escape(Utils.join(sentence.find(Word.Type.NOUN), " ")));
             TopDocs docs = searcher.search(query, 15);
             for (ScoreDoc doc : docs.scoreDocs) {
                 Document document = searcher.doc(doc.doc);
                 sentences.add(document);
             }
         } catch (IOException e) {
             ApplicationFrame.logger.log(Level.WARNING, "IOException", e);
         } catch (ParseException e) {
             ApplicationFrame.logger.log(Level.WARNING, "Error parsing sentences", e);
         } finally {
             Utils.close(reader);
         }
         return sentences;
     }
 
     private void indexQuery(String keywords) throws IOException {
         IndexReader reader = null;
         try {
             reader = writer.getReader();
             ApplicationFrame.logger.log(Level.INFO, "Querying wiki: \"{0}\"", keywords);
             Set<WikiPage> pages = new WikiSearch(keywords).parseResults(3 /* max docs */);
             IndexSearcher searcher = new IndexSearcher(reader);
 
             for (WikiPage page : pages) {
                 try {
                     Query query = parser.parse("url:" + QueryParser.escape(page.getUrl()));
                     // make sure it hasn't already been parsed
                     if (searcher.search(query, 1).totalHits == 0) {
                         ApplicationFrame.logger.log(Level.INFO, "Indexing \"{0}\"", page.getUrl());
                         for (String s : NlpService.detectedSentences(page.fetchContent())) {
                             try {
                                 Document doc = toDocument(new Sentence(s));
                                 doc.add(new Field("url", page.getUrl(), Field.Store.YES, Field.Index.ANALYZED));
                                 writer.addDocument(doc);
                             } catch (Exception e) {
                                 ApplicationFrame.logger.log(Level.WARNING, "Unknown exception", e);
                             }
                         }
                         writer.commit();
                         writer.optimize(true);
                     } else {
                         ApplicationFrame.logger.log(Level.INFO, "Already cached \"{0}\"", page.getUrl());
                     }
                 } catch (ParseException e) {
                     ApplicationFrame.logger.log(Level.WARNING, "Parsing exception", e);
                 }
             }
         } finally {
             Utils.close(reader);
         }
     }
 
 
     @Override
     public void onClose() {
         Utils.close(writer);
     }
 
     @Override
     protected BufferedImage getBackgroundImage() {
         return BACKGROUND;
     }
 
     private void showLoader() {
         if (!loading) {
             removeAll();
             add(loader);
             revalidate();
             repaint();
             loading = true;
         }
     }
 
     private void hideLoader() {
         if (loading) {
             remove(loader);
             revalidate();
             repaint();
             loading = false;
         }
     }
 
     private Document toDocument(Sentence sentence) {
         Document doc = new Document();
         doc.add(new Field("originalSentence", sentence.getRawText(), Field.Store.YES, Field.Index.ANALYZED));
         for (Word w : sentence.find(Word.Type.NOUN)) {
             doc.add(new Field("noun", w.getText(), Field.Store.NO, Field.Index.ANALYZED));
         }
         return doc;
     }
 }

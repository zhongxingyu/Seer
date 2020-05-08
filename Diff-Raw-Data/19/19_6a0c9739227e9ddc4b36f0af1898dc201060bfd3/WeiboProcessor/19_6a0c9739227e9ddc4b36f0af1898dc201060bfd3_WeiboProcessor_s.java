 package preprocess;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 import util.StringValidator;
 
 import db.DBInterface;
 
 public class WeiboProcessor {
     private static void process(String weiboDir) {
         File dir = new File(weiboDir);
         File[] weiboCorpus = dir.listFiles();
 
         DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
         WordSegmenter segmenter = new WordSegmenter();
         SentimentAnalyzer analyzer = new SentimentAnalyzer();
 
         DBInterface db = new DBInterface();
         Connection conn = db.getConnection();
         String insql="insert into weibo (id, create_at, target, text, segment, sentiment) values(?,?,?,?,?,?)";
         PreparedStatement ps = null;
         try {
             ps = conn.prepareStatement(insql);
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         int count = 0;
         for (File corpus : weiboCorpus) {
             String target = corpus.getName().replace(".txt", "");
             System.out.println(target);
             try {
                 List<String> lines = FileUtils.readLines(corpus, "utf-8");
                 int lineNum = 0;
                 String time = null;
                 String id = null;
                 String text = null;
                 String seg = null;
 
                 for (String line : lines) {
                     switch (lineNum % 4) {
                     case 0:
                         time = line;
                         break;
                     case 1:
                         id = line;
                         break;
                     case 2:
                         text = StringValidator.stripNonValidXMLCharacters(line);
                         break;
                     case 3:
                         seg = line;
 
                         if (seg.isEmpty()) {
                             break;
                         }
 
                         Date createAt = null;
                         try {
                             createAt = format.parse(time);
                         } catch (ParseException e) {
                             e.printStackTrace();
                             break;
                         }
                         System.out.println(id + " " + text + " "+ seg);
                         //String words = segmenter.segment(text);
                         String words = seg;
                         int sentiment = analyzer.analyze(text, words);
                         System.out.println(sentiment);
                         ps.setString(1, id);
                         ps.setTimestamp(2, new java.sql.Timestamp(createAt.getTime()));
                         ps.setString(3, target);
                         ps.setString(4, text);
                         ps.setString(5, words);
                         ps.setInt(6, sentiment);
                         ps.addBatch();
                         if ((++count) % 1000 == 0) {
                             ps.executeBatch();
                         }
                         break;
                     }
                     lineNum ++;
                 }
                 ps.executeBatch();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public static void main(String[] args) {
        process("/home/gods/new/");
     }
 }

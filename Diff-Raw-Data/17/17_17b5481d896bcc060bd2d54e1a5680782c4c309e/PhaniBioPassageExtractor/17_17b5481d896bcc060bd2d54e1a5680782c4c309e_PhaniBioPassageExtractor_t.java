 package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.lang.Math;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.jsoup.Jsoup;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.mysql.jdbc.MysqlParameterMetadata;
 
 import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
 import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
 import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
 import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
 import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm.QueryExpansion;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PhaniBioPassageExtractor extends SimplePassageExtractor {
 
   private class PhaniPassageSpan {
 
     public int begin, end, size;
 
     public int keyterms, matches;
 
     public String text;
 
     public PhaniPassageSpan(int begin, int end, String text) {
       this.begin = begin;
       this.end = end;
       this.text = text;
       this.size = end - begin + 1;
     }
 
     public void addMatches(int matches, int keyterms) {
       this.keyterms = keyterms;
       this.matches = matches;
     }
   }
 
   private List<PassageCandidate> paradigm1(String question, List<Keyterm> keyterms,
           List<RetrievalResult> documents, SolrWrapper wrapper) {
     HashMap<PassageCandidate, Float> M = new HashMap<PassageCandidate, Float>();
     List<PassageCandidate> result = new ArrayList<PassageCandidate>();
     for (RetrievalResult document : documents) {
       String id = document.getDocID();
       try {
         String htmlText = wrapper.getDocText(id);
         ArrayList<PhaniPassageSpan> basicTextSpans = getBasicHTMLSpans(htmlText);
         List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
           public String apply(Keyterm keyterm) {
             return keyterm.getText();
           }
         });
         String[] keytermStringsArray = keytermStrings.toArray(new String[0]);
         List<PassageCandidate> passageSpans = extractWithNybergScoring(keytermStringsArray,
                 basicTextSpans, document);
         // printQuesAnalysis(document, keytermStringsArray, passageSpans);
         for (PassageCandidate passageSpan : passageSpans) {
           M.put(passageSpan, passageSpan.getProbability());
         }
       } catch (SolrServerException e) {
         e.printStackTrace();
       } catch (AnalysisEngineProcessException e) {
         e.printStackTrace();
       }
     }
 
     List<Map.Entry<PassageCandidate, Float>> list = new ArrayList<Map.Entry<PassageCandidate, Float>>(
             M.entrySet());
     Collections.sort(list, new Comparator<Map.Entry<PassageCandidate, Float>>() {
       @Override
       public int compare(java.util.Map.Entry<PassageCandidate, Float> o1,
               java.util.Map.Entry<PassageCandidate, Float> o2) {
         return o2.getValue().compareTo(o1.getValue());
       }
     });
     int topk = 0;
     for (Map.Entry<PassageCandidate, Float> E : list) {
       result.add(E.getKey());
       topk++;
       if (topk == 10*documents.size())
         break;
     }
     System.err.println("No. of passages returned for Question:" + question + " " + result.size());
     return result;
   }
 
   @SuppressWarnings("unused")
   private List<PassageCandidate> paradigm2(String question, List<Keyterm> keyterms,
           List<RetrievalResult> documents) {
     List<PassageCandidate> result = new ArrayList<PassageCandidate>();
     return result;
   }
 
   @Override
   protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
           List<RetrievalResult> documents) {
 
    QueryExpansion qe = new QueryExpansion();
    List<Keyterm> expanded = qe.expand(keyterms);
    return paradigm1(question, expanded, documents, wrapper);
    //return paradigm1(question, keyterms, documents, wrapper);
     // return paradigm2(question, keyterms, documents, wrapper);
   }
 
   @SuppressWarnings("unused")
   private void oldCode() {
     /*
      * 
      * List<PassageCandidate> result = new ArrayList<PassageCandidate>();
      * 
      * for (RetrievalResult document : documents) { // System.out.println("RetrievalResult: " +
      * document.toString()); String id = document.getDocID(); try { String htmlText =
      * wrapper.getDocText(id); ArrayList<PhaniPassageSpan> basicTextSpans =
      * getBasicHTMLSpans(htmlText); // cleaning HTML text // String text =
      * Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "") // .trim() ; // for now,
      * making sure the text isn't too long // text = text.substring(0, Math.min(5000,
      * text.length())); // System.out.println(text); List<String> keytermStrings =
      * Lists.transform(keyterms, new Function<Keyterm, String>() { public String apply(Keyterm
      * keyterm) { return keyterm.getText(); } }); List<PassageCandidate> passageSpans =
      * extract(keytermStrings.toArray(new String[0]), basicTextSpans, document); for
      * (PassageCandidate passageSpan : passageSpans) result.add(passageSpan); } catch
      * (SolrServerException e) { e.printStackTrace(); } catch (AnalysisEngineProcessException e) {
      * e.printStackTrace(); } } System.out.println("Phani Passage extractor returning " +
      * result.size() + " passages"); return result;
      */
   }
 
   @SuppressWarnings("unused")
   private void printQuesAnalysis(RetrievalResult document, String[] keyTermStrings,
           List<PassageCandidate> L) {
     try {
       String docId = document.getDocID();
       // System.out.println("Here " + document.getDocID());
       System.out.println("Inside");
       BufferedWriter writer = new BufferedWriter(new FileWriter(
               "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/AllPassages/" + docId
                       + "passages.txt"));
 
       BufferedWriter keytermWriter = new BufferedWriter(new FileWriter(
               "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/AllPassages/" + docId
                       + "keyterms.txt"));
 
       BufferedWriter documentWriter = new BufferedWriter(new FileWriter(
               "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/AllPassages/" + docId + ".txt"));
 
       String htmlText = wrapper.getDocText(document.getDocID());
       int goldStart = 6440;
       int goldEnd = 6901;
       documentWriter.write(htmlText + "\n\n");
       documentWriter.close();
 
       for (String keyTerm : keyTermStrings) {
         keytermWriter.write(keyTerm + "\n");
       }
       keytermWriter.close();
 
       writer.write("Gold:\n");
       writer.write("-----------------------------------\n");
       writer.write(htmlText.substring(goldStart, goldEnd) + "\n");
       writer.write("-----------------------------------\n");
 
       writer.write("Mine:\n");
       for (PassageCandidate P : L) {
         writer.write("-----------------------------------\n");
         writer.write(P.toString() + "\n");
         writer.write(htmlText.substring(P.getStart(), P.getEnd()) + "\n");
         writer.write("-----------------------------------\n");
       }
       writer.close();
     } catch (Exception E) {
       E.printStackTrace();
       System.out.println("Not printed!!");
     }
   }
 
   private ArrayList<PhaniPassageSpan> getBasicHTMLSpans(String htmlText) {
     // System.out.println(htmlText);
     Pattern P = Pattern.compile("<P>([\\S\\s]*?)<P>");
 
     // Taking only first 5000
     // htmlText.substring(0, Math.min(1000, htmlText.length()));
 
     Matcher M = P.matcher(htmlText);
     ArrayList<PhaniPassageSpan> passageSpans = new ArrayList<PhaniPassageSpan>();
     // System.out.println("Outside");
     while (M.find()) {
       // System.out.println("Inside basic match");
       int begin = M.start(1);
       int end = M.end(1);
       String text = M.group(1);
       // int lastP = Math.max(
       // begin,
       // begin
       // + (text.lastIndexOf("<P>") > 0 ? text.lastIndexOf("<P>") + 3 : text
       // .lastIndexOf("<P>")));
       // System.out.println("last index:" + lastP);
       // System.out.println("\nText: "+text);
       // PhaniPassageSpan p = new PhaniPassageSpan(lastP, end, htmlText.substring(lastP, end));
       PhaniPassageSpan p = new PhaniPassageSpan(begin, end, text);
       passageSpans.add(p);
     }
     /*
      * List<String> L = Arrays.asList(htmlText.split("<P>")); //ArrayList<PhaniPassageSpan>
      * passageSpans = new ArrayList<PhaniPassageSpan>(); for (String str : L) { int begin =
      * htmlText.indexOf(str); int end = begin + str.length(); PhaniPassageSpan p = new
      * PhaniPassageSpan(begin, end, str); passageSpans.add(p); }
      */
     return passageSpans;
 
   }
 
   @SuppressWarnings("unused")
   private String formAllKeyTerms(String[] keyTermStrings) {
     StringBuffer sb = new StringBuffer();
     boolean flag = false;
 
     String[] stopWords = { "in", "the", "is", "of", "What" };
     List<String> stopWordsList = Arrays.asList(stopWords);
     for (String keyTermString : keyTermStrings) {
       if (stopWordsList.contains(keyTermString))
         continue;
       if (flag)
         sb.append('|');
       // sb.append(keyTermString);
       sb.append("\\b" + keyTermString + "\\b");
       flag = true;
     }
     String allKeyTerms = sb.toString();
     return allKeyTerms;
   }
 
   private ArrayList<Integer> getOverallMacthes(RetrievalResult document, String[] keyTermStrings)
           throws SolrServerException {
     HashSet<String> keyterms = new HashSet<String>();
     String text = wrapper.getDocText(document.getDocID());
 
     // text = text.toLowerCase();
     // text = text.substring(0, Math.min(1000, text.length()));
     // text = Jsoup.parse(text).text().replaceAll("([\177-\377\0-\32]*)", "");
 
     ArrayList<Integer> Matches = new ArrayList<Integer>();
     int matches = 0;
     for (String keyTerm : keyTermStrings) {
       Pattern p = Pattern.compile(keyTerm);
       Matcher m = p.matcher(text);
       while (m.find()) {
         matches++;
         keyterms.add(keyTerm);
       }
     }
     Matches.add(matches);
     Matches.add(keyterms.size());
     Matches.add(text.length());
     return Matches;
   }
 
   private List<PassageCandidate> extractWithNybergScoring(String[] keyTermStrings,
           ArrayList<PhaniPassageSpan> basicTextSpans, RetrievalResult document)
           throws AnalysisEngineProcessException, SolrServerException {
     String docID = document.getDocID();
     List<PassageCandidate> L = new ArrayList<PassageCandidate>();
     ArrayList<Integer> overallMatches = getOverallMacthes(document, keyTermStrings);
     int totalMatches = overallMatches.get(0);
     int totalKeyterms = overallMatches.get(1);
     int totalTextLength = overallMatches.get(2);
     KeytermWindowScorer scorer = new KeytermWindowScorerSum();
     for (PhaniPassageSpan textSpan : basicTextSpans) {
       int matches = 0;
       int keyterms = 0;
 
       int minStart = textSpan.end;
       int maxEnd = textSpan.begin;
 
       boolean flag = false;
       for (String keyTerm : keyTermStrings) {
         flag = false;
         Pattern p = Pattern.compile(keyTerm);
 
         String text = textSpan.text;
         // text = Jsoup.parse(text).text().replaceAll("([\177-\377\0-\32]*)", "");
 
         Matcher m = p.matcher(text);
         while (m.find()) {
 
           if (minStart > textSpan.begin + m.start())
             minStart = textSpan.begin + m.start();
           if (maxEnd < textSpan.begin + m.end())
             maxEnd = textSpan.begin + m.end();
 
           matches++;
           flag = true;
         }
         if (flag)
           keyterms++;
       }
       if (matches > 0) {
 
         int begin = Math.max(textSpan.begin, minStart);
         int end = (int) Math.min(textSpan.end, maxEnd);
 
         //double score = scorer.scoreWindow(begin, end, matches, totalMatches, keyterms,
         // totalKeyterms, totalTextLength);
 
         double score = scorer.scoreWindow(textSpan.begin, textSpan.end, matches, totalMatches,
                 keyterms, totalKeyterms, totalTextLength);
 
         PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end,
                 (float) (score * document.getProbability()), document.getQueryString());
 
         // PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end,
         // (float) (score), document.getQueryString());
 
         L.add(pc);
       }
     }
     return L;
   }
 
   @SuppressWarnings("unused")
   private List<PassageCandidate> extract(String[] keyTermStrings,
           ArrayList<PhaniPassageSpan> basicTextSpans, RetrievalResult document)
           throws AnalysisEngineProcessException {
     String docID = document.getDocID();
     StringBuffer sb = new StringBuffer();
     boolean flag = false;
 
     String[] stopWords = { "in", "the", "is", "of", "What" };
     List<String> stopWordsList = Arrays.asList(stopWords);
     for (String keyTermString : keyTermStrings) {
       if (stopWordsList.contains(keyTermString))
         continue;
       if (flag)
         sb.append('|');
       // sb.append(keyTermString);
       sb.append("\\b" + keyTermString + "\\b");
       flag = true;
     }
     String allKeyTerms = sb.toString();
     // System.out.println("All keyterms:"+allKeyTerms);
     Pattern allKeyTermsPattern = Pattern.compile(allKeyTerms);
     List<PassageCandidate> L = new ArrayList<PassageCandidate>();
     for (PhaniPassageSpan textSpan : basicTextSpans) {
       // String cleanText = Jsoup.parse(textSpan.text).text().replaceAll("([\177-\377\0-\32]*)",
       // "");
       Matcher m = allKeyTermsPattern.matcher(textSpan.text);
       // Matcher m = allKeyTermsPattern.matcher(cleanText);
       int score = 0;
       int minStart = textSpan.end;
       int maxEnd = textSpan.begin;
       while (m.find()) {
         if (minStart > textSpan.begin + m.start())
           minStart = textSpan.begin + m.start();
         if (maxEnd < textSpan.begin + m.end())
           maxEnd = textSpan.begin + m.end();
         score++;
       }
       if (score > 0) {
         // System.out.println(score+" "+textSpan.begin+" "+textSpan.end);
         int start = Math.max(textSpan.begin, minStart - 100);
         int end = (int) Math.min(textSpan.end, maxEnd + 100);
         // System.out.println("Start:" + start + " End:" + end);
         PassageCandidate pc = new PassageCandidate(docID, start, end, score
                 * document.getProbability(), document.getQueryString());
         // PassageCandidate pc = new PassageCandidate(docID, start, end, score,
         // document.getQueryString());
 
         // PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end, score
         // * document.getProbability(), document.getQueryString());
         // PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end, score,
         // null);
         L.add(pc);
       }
     }
 
     try {
       // System.out.println("Here " + document.getDocID());
       if (document.getDocID().toString().equals("11152682")) {
         System.out.println("Inside");
         BufferedWriter writer = new BufferedWriter(new FileWriter(
                 "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/passages.txt"));
 
         BufferedWriter keytermWriter = new BufferedWriter(new FileWriter(
                 "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/keyterms.txt"));
 
         BufferedWriter documentWriter = new BufferedWriter(new FileWriter(
                 "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/document.txt"));
 
         String htmlText = wrapper.getDocText(document.getDocID());
         int goldStart = 6440;
         int goldEnd = 6901;
         documentWriter.write(htmlText + "\n\n");
         documentWriter.close();
 
         for (String keyTerm : keyTermStrings) {
           keytermWriter.write(keyTerm + "\n");
         }
         keytermWriter.close();
 
         writer.write("Gold:\n");
         writer.write("-----------------------------------\n");
         writer.write(htmlText.substring(goldStart, goldEnd) + "\n");
         writer.write("-----------------------------------\n");
 
         writer.write("Mine:\n");
         for (PassageCandidate P : L) {
           writer.write("-----------------------------------\n");
           writer.write(P.toString() + "\n");
           writer.write(htmlText.substring(P.getStart(), P.getEnd()) + "\n");
           writer.write("-----------------------------------\n");
         }
         writer.close();
       }
     } catch (Exception E) {
       E.printStackTrace();
       System.out.println("Not printed!!");
     }
 
     return L;
   }
 
   @SuppressWarnings("unused")
   private List<PassageCandidate> extractSepRegex(String[] keyTermStrings,
           ArrayList<PhaniPassageSpan> basicTextSpans, RetrievalResult document)
           throws AnalysisEngineProcessException {
     String docID = document.getDocID();
     List<PassageCandidate> L = new ArrayList<PassageCandidate>();
     for (PhaniPassageSpan textSpan : basicTextSpans) {
       int score = 0;
       for (String keyTermString : keyTermStrings) {
         Pattern allKeyTermsPattern = Pattern.compile(keyTermString);
         Matcher m = allKeyTermsPattern.matcher(textSpan.text);
         while (m.find()) {
           score++;
         }
       }
       if (score > 0) {
         PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end, score
                 * document.getProbability(), null);
         L.add(pc);
       }
     }
     return L;
   }
 }

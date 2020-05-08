 package edu.cmu.lti.oaqa.openqa.test.team06.passage;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 
 import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
 import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;
 import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
 import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
 
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 
 import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
 
 public class KentonPassageCanditateFinder {
   private String text;
   private String docId;
 
   private int textSize; // values for the entire text
 
   private int totalMatches;
 
   private int totalKeyterms;
 
   private KentonKeytermWindowScorerSum scorer;
 
   public KentonPassageCanditateFinder(String docId, String text, KentonKeytermWindowScorerSum scorer) {
     super();
     this.text = text;
     this.docId = docId;
     this.textSize = text.length();
     this.scorer = scorer;
   }
 
   public List<PassageCandidate> extractPassages(String[] keyterms) {
     List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
     
     String[] sentences = text.split("[.?!]");
     
     List<PassageCandidate> result = new ArrayList<PassageCandidate>();
     int accu = 0;
     int sentinel = 0;
     int sentenceMatches = 0;
     totalMatches = 0;
     totalKeyterms = 0;
     for(String sent: sentences){
    // Find all keyterm matches.
       sentinel = 0;
       sentenceMatches = 0;
       List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
       for (String keyterm : keyterms) {
         Pattern p = Pattern.compile(keyterm);
         Matcher m = p.matcher(sent);
         while (m.find()) {
           //if(sentinel == 0){
             //PassageSpan match = new PassageSpan(m.start() + accu, m.end() + accu);
             PassageSpan match = new PassageSpan(accu + 1, sent.length() + accu + 1);
             //System.out.println(keyterm + " " + (accu+1) +" "+ (sent.length() + accu + 1));
             matchedSpans.add(match);
             
           //  sentinel = 1;
           //}
           totalMatches++;
           sentenceMatches++;
           continue;
         }
         if (!matchedSpans.isEmpty()) {
           matchingSpans.add(matchedSpans);
           totalKeyterms++;
         }
         
 //        if(sentenceMatches != 1){
 //          double score = scorer.scoreWindow(accu + 1, sent.length() + accu + 1,
 //                  sentenceMathces, totalMatches, keytermsFound,
 //                  totalKeyterms, textSize);
 //          PassageCandidate window = null;
 //          try {
 //            window = new PassageCandidate(docId, begin, end, (float) score, null);
 //          } catch (AnalysisEngineProcessException e) {
 //            e.printStackTrace();
 //          }
 //          result.add(window);
 //        }
         
       }
       accu += (sent.length() + 1);
     }
     System.out.println("TotalMatches:" + totalMatches + "TotalKeyterms" + totalKeyterms);
 //    // Find all keyterm matches.
 //    for (String keyterm : keyterms) {
 //      Pattern p = Pattern.compile(keyterm);
 //      Matcher m = p.matcher(text);
 //      while (m.find()) {
 //        PassageSpan match = new PassageSpan(m.start(), m.end());
 //        matchedSpans.add(match);
 //        totalMatches++;
 //      }
 //      if (!matchedSpans.isEmpty()) {
 //        matchingSpans.add(matchedSpans);
 //        totalKeyterms++;
 //      }
 //    }
 
     // create set of left edges and right edges which define possible windows.
     HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
     
     List<Integer> leftEdges = new ArrayList<Integer>();
     List<Integer> rightEdges = new ArrayList<Integer>();
     for (List<PassageSpan> keytermMatches : matchingSpans) {
       for (PassageSpan keytermMatch : keytermMatches) {
         Integer leftEdge = keytermMatch.begin;
         Integer rightEdge = keytermMatch.end;
         //System.out.println(leftEdge + " " + rightEdge);
         hash.put(leftEdge, rightEdge);
        if (!leftEdges.contains(leftEdge)){
           leftEdges.add(leftEdge);
         }
         if (!rightEdges.contains(rightEdge)){
           rightEdges.add(rightEdge);
         }
       }
     }
 
     // For every possible window, calculate keyterms found, matches found; score window, and create
     // passage candidate.
 //    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
     for (Integer begin : leftEdges) {
       for (Integer end : rightEdges) {
         if (end <= begin)
           continue;
         //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
         //if(hash.get(begin) != end)continue;
         if(hash.get(begin).intValue() + 1500 < end.intValue())
         	continue;
         // This code runs for each window.
         int keytermsFound = 0;
         int matchesFound = 0;
         for (List<PassageSpan> keytermMatches : matchingSpans) {
           boolean thisKeytermFound = false;
           for (PassageSpan keytermMatch : keytermMatches) {
             if (keytermMatch.containedIn(begin, end)) {
               matchesFound++;
               thisKeytermFound = true;
             }
           }
           if (thisKeytermFound)
             keytermsFound++;
         }
         double score = scorer.scoreWindow(begin, end, matchesFound, totalMatches, keytermsFound,
                 totalKeyterms, textSize);
         PassageCandidate window = null;
         try {
           System.out.println("Key Sentence: " + text.substring(begin, begin+3));
           System.out.println("Score: " + score);
           window = new PassageCandidate(docId, begin, end, (float) score, null);
         } catch (AnalysisEngineProcessException e) {
           e.printStackTrace();
         }
         result.add(window);
       }
     }
 
     // Sort the result in order of decreasing score.
     // Collections.sort ( result , new PassageCandidateComparator() );
     return result;
 
   }
 
   @SuppressWarnings("unused")
   private class PassageCandidateComparator implements Comparator {
     // Ranks by score, decreasing.
     public int compare(Object o1, Object o2) {
       PassageCandidate s1 = (PassageCandidate) o1;
       PassageCandidate s2 = (PassageCandidate) o2;
       if (s1.getProbability() < s2.getProbability()) {
         return 1;
       } else if (s1.getProbability() > s2.getProbability()) {
         return -1;
       }
       return 0;
     }
   }
 
   class PassageSpan {
     private int begin, end;
 
     public PassageSpan(int begin, int end) {
       this.begin = begin;
       this.end = end;
     }
 
     public boolean containedIn(int begin, int end) {
       if (begin <= this.begin && end >= this.end) {
         return true;
       } else {
         return false;
       }
     }
   }
 
   public static void main(String[] args) {
     PassageCandidateFinder passageFinder1 = new PassageCandidateFinder("1",
             "The quick brown fox jumped over the quick brown fox.",
             new KeytermWindowScorerProduct());
     PassageCandidateFinder passageFinder2 = new PassageCandidateFinder("1",
             "The quick brown fox jumped over the quick brown fox.", new KentonKeytermWindowScorerSum());
     String[] keyterms = { "quick", "jumped" };
     List<PassageCandidate> windows1 = passageFinder1.extractPassages(keyterms);
     System.out.println("Windows (product scoring): " + windows1);
     List<PassageCandidate> windows2 = passageFinder2.extractPassages(keyterms);
     System.out.println("Windows (sum scoring): " + windows2);
   }
 
}

 package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.jsoup.Jsoup;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.mysql.jdbc.MysqlParameterMetadata;
 
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
 import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
 import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PhaniBioPassageExtractor extends SimplePassageExtractor {
 
   private class PhaniPassageSpan {
 
     public int begin, end;
 
     public String text;
 
     public PhaniPassageSpan(int begin, int end, String text) {
       this.begin = begin;
       this.end = end;
       this.text = text;
     }
   }
 
   @Override
   protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
           List<RetrievalResult> documents) {
 
     List<PassageCandidate> result = new ArrayList<PassageCandidate>();
 
     for (RetrievalResult document : documents) {
 
       System.out.println("RetrievalResult: " + document.toString());
       String id = document.getDocID();
 
       try {
         String htmlText = wrapper.getDocText(id);
         ArrayList<PhaniPassageSpan> basicTextSpans = getBasicHTMLSpans(htmlText);
         // cleaning HTML text
         // String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/*
         // .trim() */;
         // for now, making sure the text isn't too long
         // text = text.substring(0, Math.min(5000, text.length()));
         // System.out.println(text);
         List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
           public String apply(Keyterm keyterm) {
             return keyterm.getText();
           }
         });
 
         List<PassageCandidate> passageSpans = extract(keytermStrings.toArray(new String[0]),
                 basicTextSpans, document);
         for (PassageCandidate passageSpan : passageSpans)
           result.add(passageSpan);
       } catch (SolrServerException e) {
         e.printStackTrace();
       } catch (AnalysisEngineProcessException e) {
         e.printStackTrace();
       }
     }
     return result;
   }
 
   private ArrayList<PhaniPassageSpan> getBasicHTMLSpans(String htmlText) {
     List<String> L = Arrays.asList(htmlText.split("<P>"));
     ArrayList<PhaniPassageSpan> passageSpans = new ArrayList<PhaniPassageSpan>();
     for (String str : L) {
       int begin = htmlText.indexOf(str);
       int end = begin + str.length();
       PhaniPassageSpan p = new PhaniPassageSpan(begin, end, str);
       passageSpans.add(p);
     }
     return passageSpans;
   }
 
   private List<PassageCandidate> extract(String[] keyTermStrings,
           ArrayList<PhaniPassageSpan> basicTextSpans, RetrievalResult document)
           throws AnalysisEngineProcessException {
     String docID = document.getDocID();
     StringBuffer sb = new StringBuffer();
     boolean flag = false;
     for (String keyTermString : keyTermStrings) {
       if (flag)
         sb.append('|');
       sb.append(keyTermString);
       flag = true;
     }
     String allKeyTerms = sb.toString();
     Pattern allKeyTermsPattern = Pattern.compile(allKeyTerms);
     List<PassageCandidate> L = new ArrayList<PassageCandidate>();
     for (PhaniPassageSpan textSpan : basicTextSpans) {
       Matcher m = allKeyTermsPattern.matcher(textSpan.text);
       int score = 0;
       while (m.find()) {
 
         score++;
       }
       // String cleanText = Jsoup.parse(textSpan.text).text().replaceAll("([\177-\377\0-\32]*)",
       // "");
       if (score > 0) {
         PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end, score*document.getProbability(), null);
         L.add(pc);
       }
     }
     return L;
   }
 }

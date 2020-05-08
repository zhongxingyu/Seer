 package com.taitooz.app;
 
 import com.cdyne.ws.Check;
 import com.cdyne.ws.CheckSoap;
 import com.cdyne.ws.DocumentSummary;
 import com.cdyne.ws.Words;
 import java.util.List;
 import javax.xml.ws.WebServiceRef;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ejl
  * Date: 6/26/13
  * Time: 5:15 PM
  */
public class WebServiceConnectionTest {
   
   @WebServiceRef(wsdlLocation = "http://wsf.cdyne.com/SpellChecker/check.asmx?WSDL")
   private static Check service = new Check();
   
   private static final String VERIFY_TEXT = 
       "An assault on welfare including a seven-day delay before "
       + "the newly unemployed receive dole money was announced yesterday as "
       + "George Osborne spelt out a sixth year of austerity. Migrants will lose "
       + "benefits if they refuse to improve their English to the equivalent of "
       + "an average 9-year-old under the plans approved by the Lib Dems. And no "
       + "one will receive unemployment benefits until they have drawn up a CV, "
       + "registered for online job searches and started looking for work. "
       + "The details were included in the Chancellor’s spending plans for "
       + "2015-16, which lay down the enanoComeGato battlelines for the rest of "
       + "the Parliament and ensure that whoever wins the next election will "
       + "inherit a budget  squeeze for their first year in power. Public servants,"
       + "pensioners and welfare claimants emerged as the biggest losers as the "
       + "Chancellor saw through his promise to cut Whitehall spending by an extra";
   
   
   
   private static DocumentSummary checkTextBodyV2(java.lang.String bodyText) {
     CheckSoap port = service.getCheckSoap();
     return port.checkTextBodyV2(bodyText);
   }
   
   public static void main(String args[]) throws java.io.IOException {
     
     com.cdyne.ws.DocumentSummary doc = checkTextBodyV2(VERIFY_TEXT);
     String allcontent = doc.getBody();
     int no_of_mistakes = doc.getMisspelledWordCount();
     List allwrongwords = doc.getMisspelledWord();
     System.out.println("<h2><font color='red'>Spell Checker Report</font></h2>");
     System.out.println("<hr><b>Your text:</b> \"" + allcontent + "\"" + "<p>");
     for (int i = 0; i < allwrongwords.size(); i++) {
         String onewrongword = ((Words) allwrongwords.get(i)).getWord();
         int onewordsuggestioncount = ((Words) allwrongwords.get(i)).getSuggestionCount();
         List allsuggestions = ((Words) allwrongwords.get(i)).getSuggestions();
         System.out.println("<hr><p><b>Wrong word:</b><font color='red'> " + onewrongword + "</font>");
         System.out.println("<p><b>" + onewordsuggestioncount + " suggestions:</b><br>");
         for (int k = 0; k < allsuggestions.size(); k++) {
             String onesuggestion = (String) allsuggestions.get(k);
             System.out.println(onesuggestion);
         }
     }
     System.out.println("<font color='red'><b>Summary:</b> " + no_of_mistakes + " mistakes (");
     for (int i = 0; i < allwrongwords.size(); i++) {
         String onewrongword = ((Words) allwrongwords.get(i)).getWord();
         System.out.println(onewrongword);
     }
     
   }
 }

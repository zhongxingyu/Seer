 package vinna.samples.programmatic;
 
 import vinna.outcome.Outcome;
 import vinna.outcome.StringOutcome;
 
 import java.util.Collection;
 import java.util.Map;
 
 public class Controller {
 
     public StringOutcome index() {
         return new StringOutcome("hello here");
     }
 
     public StringOutcome test(int id) {
         return new StringOutcome("Your id is " + id);
     }
 
     public StringOutcome test2(String inject, Integer id) {
         return new StringOutcome("Your id is " + id + "\n" + inject);
     }
 
     public StringOutcome test2(String inject, boolean yo, Integer id) {
         return new StringOutcome("Your id is " + id + "\n" + inject + "\n" + yo);
     }
 
     public Outcome writeHeaders(Collection<String> headers) {
         if (headers == null || headers.size() == 0) {
             return new StringOutcome("no header");
         }
         StringBuilder builder = new StringBuilder();
         for (String header : headers) {
             builder.append(header);
             builder.append("\n");
         }
         return new StringOutcome(builder.toString());
     }
 
     public Outcome writeHeader(String header) {
         return new StringOutcome(header);
     }
 
     public Outcome printHeaders(Map<String, Collection<String>> headers) {
         if (headers == null || headers.size() == 0) {
             return new StringOutcome("no header");
         }
         StringBuilder builder = new StringBuilder();
         for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
             builder.append(entry.getKey() + ":" + entry.getValue() + "\n");
         }
         return new StringOutcome(builder.toString());
     }
 }

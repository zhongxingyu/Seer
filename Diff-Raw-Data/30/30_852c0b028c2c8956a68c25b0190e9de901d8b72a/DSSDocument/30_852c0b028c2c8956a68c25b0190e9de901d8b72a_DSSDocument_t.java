 package com.silentmatt.dss;
 
 import com.silentmatt.dss.parser.DSSParser;
 import com.silentmatt.dss.parser.Scanner;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Matthew Crumley
  */
 public class DSSDocument {
     private final List<Rule> rules = new ArrayList<Rule>();
 
     public static DSSDocument parse(String url, ErrorReporter errors) throws IOException {
         return parse(new URL(url), errors);
     }
 
     public static DSSDocument parse(URL url, ErrorReporter errors) throws IOException {
         return parse(url.openStream(), errors);
     }
 
     public static DSSDocument parse(InputStream input, ErrorReporter errors) throws IOException {
         Scanner scanner = new Scanner(input);
         return parse(scanner, errors);
     }
 
     public static DSSDocument parse(Scanner scanner, ErrorReporter errors) {
        int existingErrors = errors.getErrorCount();
         DSSParser parser = new DSSParser(scanner);
         parser.setErrors(errors);
         parser.parse();
        if (parser.getErrors().getErrorCount() > existingErrors) {
             return null;
         }
         else {
             return parser.getDocument();
         }
     }
 
     public void addRule(Rule rule) {
         this.rules.add(rule);
     }
 
     public List<Rule> getRules() {
         return rules;
     }
 
     @Override
     public String toString() {
         StringBuilder txt = new StringBuilder();
         for (Rule r : rules) {
             txt.append(r).append("\n");
         }
         return txt.toString();
     }
 
     public String toCssString() {
         StringBuilder txt = new StringBuilder();
         for (Rule r : rules) {
             String ruleString = r.toCssString(0);
             if (ruleString.length() > 0) {
                 txt.append(ruleString).append("\n");
             }
         }
         return txt.toString();
     }
 }

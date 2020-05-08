 package ru.spbau.cheque.server.recognition;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class RegexTableExtractor implements TableExtractor {
     public List<BlueObject> extract(ChequeFormat format, List<String> text) {
         List<BlueObject> entries = new ArrayList<BlueObject>();
         String nameAccum = "";
         for (String line : text) {
             Matcher matcher = Pattern.compile("^([\\p{Digit}\\p{Punct}]+)\\p{Blank}+(.*)").matcher(line);
             if (matcher.matches()) {
                 String f1 = matcher.group(1);
                 String f2 = matcher.group(2);
                Matcher matcher2 = Pattern.compile("(.*)\\p{Blank}(.*)?+$").matcher(f2);
                 if (matcher2.matches()) {
                     String price = matcher2.group(2);
                    String price2 = price.replaceAll("^\\p{Punct}|\\p{Punct}$", "");
                    if (price2.matches("(\\p{Digit}+\\p{Punct}{1}\\p{Digit}+)")) {
                            entries.add(new BlueObject(nameAccum + matcher2.group(1),
                                        Float.parseFloat(price2)));
                    } else {
                        entries.add(new BlueObject(nameAccum + matcher2.group(1), -1));
                     }
                 }
             }
         }
         return entries;
     }
 }

 package com.graphsfm.stservice.text;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 public class WikiExtractor {
     private static final String[] TEXT_START = { "text" };
     private static final String[] PAGE_TITLE = { "title" };
 
     private static final Pattern TEXT_PATTERN = Pattern.compile(".*[a-zA-Z]+.*", Pattern.DOTALL); 
         
     private static final String REDIRECT = "(^#REDIRECT.*)";
     private static final String URL = "(https?\\:\\/\\/\\S*)";
     private static final String WIKIMETA1 = "(\\[\\[[^:\\s]*?:.*?\\]\\])";
     private static final String REFBLOCK = "(<ref[^>]*?/>)|(<ref.*?</ref>)";
     private static final String MATHBLOCK = "(<math[^>]*?/>)|(<math.*?</math>)";
     private static final String FOOTERS = "(=*See also.*)|(=*References.*)";
     private static final Pattern LINK_OR_REF = Pattern.compile(MATHBLOCK + "|"
             + REFBLOCK + "|" + WIKIMETA1 + "|" + URL + "|" + FOOTERS + "|"
             + REDIRECT, Pattern.DOTALL);
 
     private static final Pattern WIKILINK = Pattern.compile(
             "\\[\\[([^\\]]*?\\|)?(.*?)\\]\\]", Pattern.DOTALL);
 
     private XMLStreamReader reader;
     private String outdir;
 
     public WikiExtractor(String outdir, String fname) throws IOException,
             XMLStreamException {
         File d = new File(outdir);
         if (! d.exists()) {
             System.err.printf("Target directory %s does not exist.", outdir);
             throw new IOException("Output Directory Not Found");
         }
         if (! d.isDirectory()) {
             System.err.printf("Target %s is not a directory.", outdir);
             throw new IOException("Output Directory Not Found");
         }
         
         XMLInputFactory f = XMLInputFactory.newInstance();
         reader = f.createXMLStreamReader(new FileInputStream(fname));
         this.outdir = outdir;
     }
 
     public String findElement(String[] names) {
         try {
             while (true) {
                 int event = reader.next();
                 if (event == XMLStreamConstants.END_DOCUMENT)
                     return null;
                 if (event == XMLStreamConstants.START_ELEMENT) {
                     String lname = reader.getLocalName();
                     for (String name : names) {
                         if (lname.equalsIgnoreCase(name))
                             return name;
                     }
                 }
             }
         } catch (XMLStreamException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void filterNestedBraces(StringBuilder sb) {
         int level = 0;
         int start = -1;
         for (int i = 0; i < sb.length(); i++) {
             switch (sb.charAt(i)) {
             case '{':
                 if (level == 0)
                     start = i;
                 level++;
                 break;
             case '}':
                 level--;
                 if (level == 0) {
                     for (int j = start; j <= i; j++)
                         sb.setCharAt(j, ' ');
                 }
             }
         }
     }
 
     private String filterText(StringBuilder sb) {
         Matcher matcher = LINK_OR_REF.matcher(sb);
         while (matcher.find()) {
             for (int i = matcher.start(); i < matcher.end(); i++)
                 sb.setCharAt(i, ' ');
         }
 
         matcher = WIKILINK.matcher(sb);
         while (matcher.find()) {
             if (matcher.groupCount() == 2) {
                 for (int i = matcher.start(1); i < matcher.end(1); i++) {
                     sb.setCharAt(i, ' ');
                 }
             }
         }
         filterNestedBraces(sb);
         return sb.toString();
     }
 
     public String getTitleText() {
         StringBuilder sb = new StringBuilder();
         try {
             while (true) {
                 int event = reader.next();
                 if (event == XMLStreamConstants.CHARACTERS)
                     sb.append(reader.getText());
                 else {
                     return sb.toString();
                 }
             }
         } catch (XMLStreamException e) {
             throw new RuntimeException(e);
         }
     }
 
     public String getText() {
         StringBuilder sb = new StringBuilder();
         try {
             while (true) {
                 int event = reader.next();
                 if (event == XMLStreamConstants.CHARACTERS)
                     sb.append(reader.getText());
                 else {
                     return filterText(sb);
                 }
             }
         } catch (XMLStreamException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void writeFile(String title, String text) {
         try {
             if (title.length() == 0) {
                 System.err.printf("title not found. text: ...",
                         text.subSequence(0, 20));
                 return;
             }
             
             if (! TEXT_PATTERN.matcher(text).matches())
                 return;
             
             title = title.replaceAll("[^a-zA-Z]", "_");
             
             StringBuilder sb = new StringBuilder();
             char c0 = title.charAt(0);
             char c1 = (title.length() > 1 ? title.charAt(1) : '_');
             char c2 = (title.length() > 2 ? title.charAt(2) : '_');
             
             sb.append(outdir).append(File.separator);
             sb.append(c0).append(File.separator);
             sb.append(c1).append(File.separator);
             sb.append(c2).append(File.separator);
             
             File d = new File(sb.toString());
             d.mkdirs();
 
             File f = new File(d, title);
             f.createNewFile();
             
             FileWriter fw = new FileWriter(f);
             fw.write(text);
             fw.flush();
             fw.close();
         } catch (IOException e) {
             System.err.printf("Could not write %s: %s", title, e.toString());
             throw new RuntimeException(e);
         }
     }
 
     public void process() throws XMLStreamException {
         long len = 0;
         long last = 0;
         while (true) {
             String e = findElement(PAGE_TITLE);
             if (e == null)
                 break;
             String title = getTitleText();
 
             e = findElement(TEXT_START);
             if (e == null)
                 break;
             String t = getText();
 
             writeFile(title, t);
             // System.out.println(t);
 
             len += t.length();
             if (len > last + 1024 * 1024) {
                 System.err.printf("%d Mb processed\n", len / 1024 / 1024);
                 last = len;
             }
         }
     }
 
     public static void main(String args[]) throws Exception {
         WikiExtractor w = new WikiExtractor(args[0], args[1]);
         w.process();
     }
 }

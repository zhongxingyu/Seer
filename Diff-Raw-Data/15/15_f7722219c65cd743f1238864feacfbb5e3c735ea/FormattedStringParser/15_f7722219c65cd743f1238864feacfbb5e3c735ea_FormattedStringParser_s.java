 package at.yawk.fimfiction.core;
 
 import at.yawk.fimfiction.data.FormattedString;
 import java.io.IOException;
 import java.io.StringReader;
 import org.ccil.cowan.tagsoup.Parser;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Utility class for parsing BB / HTML markup to FormattedString instances.
  *
  * @author Jonas Konrad (yawkat)
  */
 public class FormattedStringParser {
     public static FormattedString parseBb(String val) {
         FormattedString.FormattedStringBuilder builder = FormattedString.builder();
         int i = 0;
         while (true) {
             int nextPara = val.indexOf('[', i);
             if (nextPara == -1) {
                 builder.append(val.substring(i));
                 break;
             }
             builder.append(val.substring(i, nextPara));
             i = nextPara;
             nextPara = val.indexOf(']', i);
             if (nextPara == -1) {
                 builder.append(val.substring(i));
                 break;
             }
             boolean start = val.charAt(i + 1) != '/';
             String tag = val.substring(i + (start ? 1 : 2), nextPara).toLowerCase();
             for (FormattedString.Formatting formatting : FormattedString.SimpleFormatting.values()) {
                 if (formatting.getTag(FormattedString.Markup.BB, start).equals(tag)) {
                     builder.append(formatting, start);
                     break;
                 }
             }
             if (tag.startsWith("size=")) {
                 try {
                     FormattedString.SizeUnit unit = FormattedString.SizeUnit.PX;
                     if (tag.endsWith("em")) {
                         unit = FormattedString.SizeUnit.EM;
                     } else if (tag.endsWith("pt")) {
                         unit = FormattedString.SizeUnit.PT;
                     }
                    builder.append(FormattedString.size(Float.parseFloat(tag.substring(5, tag.length() - 2)), unit),
                                   start);
                 } catch (NumberFormatException ignored) {}
             } else if ("size".equals(tag)) {
                 builder.append(FormattedString.size(0, FormattedString.SizeUnit.PX), start);
             }
             i = nextPara + 1;
         }
         return builder.build();
     }
 
     public static FormattedString parseHtml(String val) throws IOException, SAXException {
         XMLReader reader = new Parser();
         FormattedStringHandler handler = new FormattedStringHandler();
         reader.setContentHandler(handler);
         reader.parse(new InputSource(new StringReader(val)));
         return handler.build();
     }
 
     static class FormattedStringHandler extends DefaultHandler {
         private final FormattedString.FormattedStringBuilder builder = FormattedString.builder();
 
         public FormattedString build() {
             return builder.build();
         }
 
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attributes)
                 throws SAXException {
             if ("br".equals(qName) || "p".equals(qName)) {
                 if (builder.length() > 0) { builder.append('\n'); }
             } else if ("b".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, true);
             } else if ("i".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, true);
             } else if ("s".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, true);
             } else if ("center".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, true);
             } else if ("u".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, true);
             }
         }
 
         @Override
         public void endElement(String uri, String localName, String qName) throws SAXException {
             if ("b".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, false);
             } else if ("i".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, false);
             } else if ("s".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, false);
             } else if ("center".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, false);
             } else if ("u".equals(qName)) {
                 builder.append(FormattedString.SimpleFormatting.BOLD, false);
             }
         }
 
         @Override
         public void characters(char[] ch, int start, int length) throws SAXException {
             // clip leading whitespace
             if (builder.length() == 0) {
                 while (length > 0 && SearchHtmlParser.isWhitespace(ch[start])) {
                     length--;
                     start++;
                 }
             }
             builder.append(ch, start, length);
         }
     }
 }

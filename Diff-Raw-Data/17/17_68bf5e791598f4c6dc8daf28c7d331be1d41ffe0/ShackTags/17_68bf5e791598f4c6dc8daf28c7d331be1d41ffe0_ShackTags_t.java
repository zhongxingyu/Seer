 package cc.hughes.droidchatty;
 import java.io.StringReader;
 import java.util.HashMap;
 
 import org.ccil.cowan.tagsoup.HTMLSchema;
 import org.ccil.cowan.tagsoup.Parser;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.graphics.Typeface;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.Spanned;
 import android.text.SpannedString;
 import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
 import android.text.style.StrikethroughSpan;
 import android.text.style.StyleSpan;
 import android.text.style.TypefaceSpan;
 import android.text.style.URLSpan;
 import android.text.style.UnderlineSpan;
 import android.util.Log;
 import android.view.View;
 
 public class ShackTags
 {
     private static final HTMLSchema schema = new HTMLSchema();
     
     public static Spanned fromHtml(String source, View owner, Boolean single_line, Boolean showTags)
     {
         Parser parser = new Parser();
         try
         {
             parser.setProperty(Parser.schemaProperty, schema);
             TagConverter converter = new TagConverter(source, parser, owner, single_line, showTags);
             return converter.convert();
         } catch (Exception e)
         {
             Log.e("ShackTags", "Error parsing shack tags", e);
             return new SpannedString("Error!");
         }
     }
 }
     
 class TagConverter implements ContentHandler
 {
     private String _source;
     private XMLReader _reader;
     private SpannableStringBuilder _builder;
     private View _owner;
     private Boolean _singleLine;
     private Boolean _showTags;
     
     public TagConverter(String source, Parser parser, View owner, Boolean singleLine, Boolean showTags)
     {
         _source = source;
         _reader = parser;
         _owner = owner;
         _singleLine = singleLine;
         _showTags = showTags;
         _builder = new SpannableStringBuilder();
     }
     
     public Spanned convert() throws Exception
     {
         _reader.setContentHandler(this);
         _reader.parse(new InputSource(new StringReader(_source)));
         
         return _builder;
     }
     
     private void handleStartTag(String tag, Attributes attributes)
     {
         if (tag.equalsIgnoreCase("p"))
             handleP(_builder);
         else if (tag.equalsIgnoreCase("b"))
             start(new Bold());
         else if (tag.equalsIgnoreCase("i"))
             start(new Italic());
         else if (tag.equalsIgnoreCase("a"))
             startA(attributes);
         else if (tag.equalsIgnoreCase("span"))
         {
             String c = attributes.getValue("class");
             if (c.equalsIgnoreCase("jt_quote"))
                 start(new Blockquote());
             else if (c.equalsIgnoreCase("jt_bold"))
                 start(new Bold());
             else if (c.equalsIgnoreCase("jt_italic"))
                 start(new Italic());
             else if (c.equalsIgnoreCase("jt_underline"))
                 start(new Underline());
             else if (c.equalsIgnoreCase("jt_strike"))
                 start(new Strikethrough());
             else if (c.equalsIgnoreCase("jt_spoiler"))
                 start(new Spoiler());
            else if (c.equalsIgnoreCase("jt_sample"))
                start(new Sample());
            else if (c.equalsIgnoreCase("jt_code"))
                start(new Code());
             else if (c.equalsIgnoreCase("jt_red"))
                 start(new Font("red"));
             else if (c.equalsIgnoreCase("jt_green"))
                 start(new Font("green"));
             else if (c.equalsIgnoreCase("jt_pink"))
                 start(new Font("pink"));
             else if (c.equalsIgnoreCase("jt_olive"))
                 start(new Font("olive"));
             else if (c.equalsIgnoreCase("jt_fuchsia"))
                 start(new Font("fuchsia"));
             else if (c.equalsIgnoreCase("jt_yellow"))
                 start(new Font("yellow"));
             else if (c.equalsIgnoreCase("jt_blue"))
                 start(new Font("blue"));
             else if (c.equalsIgnoreCase("jt_lime"))
                 start(new Font("lime"));
             else if (c.equalsIgnoreCase("jt_orange"))
                 start(new Font("orange"));
         }
     }
     
     private void handleEndTag(String tag)
     {
         if (tag.equalsIgnoreCase("br"))
             handleBr(_builder);
         else if (tag.equalsIgnoreCase("p"))
             handleP(_builder);
         else if (tag.equalsIgnoreCase("b"))
             end(Bold.class, new StyleSpan(Typeface.BOLD));
         else if (tag.equalsIgnoreCase("i"))
             end(Italic.class, new StyleSpan(Typeface.ITALIC));
         else if (tag.equalsIgnoreCase("a"))
             endA();
         else if (tag.equalsIgnoreCase("span"))
             endLastSpan();
     }
     
         
     private void handleP(SpannableStringBuilder text) {
         if (_singleLine)
         {
             text.append(" ");
             return;
         }
         
         int len = text.length();
 
         if (len <= 1 && text.charAt(len - 1) == '\n') {
             if (len >= 2 && text.charAt(len - 2) == '\n') {
                 return;
             }
 
             text.append("\n");
             return;
         }
 
         if (len != 0) {
             text.append("\n\n");
         }
     }       
 
     private void handleBr(SpannableStringBuilder text)
     {
         if (_singleLine)
             text.append(" ");
         else
             text.append("\n");
     }
     
     private Object getLast(Spanned text, Class<?> kind)
     {
         Object[] objs = _builder.getSpans(0, _builder.length(), kind);
         
         if (objs.length == 0)
             return null;
         return objs[objs.length -1];
     }
     
     private void start(Object mark)
     {
         int len = _builder.length();
         _builder.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
     }
     
     private void startA(Attributes attributes)
     {
         String href = attributes.getValue("", "href");
         
         int len = _builder.length();
         _builder.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
     }
     
     private void endA()
     {
         int len = _builder.length();
         Object obj = getLast(_builder, Href.class);
         int where = _builder.getSpanStart(obj);
         
         _builder.removeSpan(obj);
         
         if (where != len)
         {
             Href h = (Href)obj;
             
             if (h.getHref() != null)
             {
                 _builder.setSpan(new URLSpan(h.getHref()), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             }
         }
     }
     
     private void end(Class<?> kind, Object repl)
     {
         int len = _builder.length();
         Object obj = getLast(_builder, kind);
         int where = _builder.getSpanStart(obj);
         
         _builder.removeSpan(obj);
         
         if (where != len)
             _builder.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     }
     
     private void endLastSpan()
     {
         int len = _builder.length();
         Object obj = getLast(_builder, Span.class);
         int where = _builder.getSpanStart(obj);
         
        _builder.removeSpan(obj);
         
         if (where != len)
         {
             Object span = null;
             
             if (obj instanceof Bold)
                 span = new StyleSpan(Typeface.BOLD);
             else if (obj instanceof Italic)
                 span = new StyleSpan(Typeface.ITALIC);
             else if (obj instanceof Blockquote)
                 span = new TypefaceSpan("serif");
             else if (obj instanceof Underline)
                 span = new UnderlineSpan();
             else if (obj instanceof Strikethrough)
                 span = new StrikethroughSpan();
             else if (obj instanceof Spoiler)
                 span = new SpoilerSpan(_owner);
            else if (obj instanceof Sample)
                span = new RelativeSizeSpan((float)0.80);
            else if (obj instanceof Code)
                span = new TypefaceSpan("monospace");
             else if (obj instanceof Font && _showTags)
                 span = new ForegroundColorSpan(getColor(((Font)obj).getColor()) | 0xFF000000);
             
             if (span != null)
                 _builder.setSpan(span, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException
     {
         StringBuilder sb = new StringBuilder();
         
         /*
          * Ignore whitespace that immediately follows other whitespace;
          * newlines count as spaces.
          */
 
         for (int i = 0; i < length; i++) {
             char c = ch[i + start];
 
             if (c == ' ' || c == '\n') {
                 char pred;
                 int len = sb.length();
 
                 if (len == 0) {
                     len = _builder.length();
 
                     if (len == 0) {
                         pred = '\n';
                     } else {
                         pred = _builder.charAt(len - 1);
                     }
                 } else {
                     pred = sb.charAt(len - 1);
                 }
 
                 if (pred != ' ' && pred != '\n') {
                     sb.append(' ');
                 }
             } else {
                 sb.append(c);
             }
         }
         
         _builder.append(sb);
     }
 
     public void endDocument() throws SAXException { } 
     
     public void endElement(String uri, String localName, String qName) throws SAXException
     {
         handleEndTag(localName);
     }
     
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
     {
         handleStartTag(localName, attributes);
     }
 
     public void endPrefixMapping(String prefix) throws SAXException { } 
     
     public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { } 
     
     public void processingInstruction(String target, String data) throws SAXException { } 
     
     public void setDocumentLocator(Locator locator) { } 
     
     public void skippedEntity(String name) throws SAXException { } 
     
     public void startDocument() throws SAXException { } 
 
     public void startPrefixMapping(String prefix, String uri) throws SAXException { } 
     
     private static class Span { }
     private static class Bold extends Span { }
     private static class Italic extends Span { }
     private static class Blockquote extends Span { }
     private static class Underline extends Span { }
     private static class Strikethrough extends Span { }
     private static class Spoiler extends Span { }
    private static class Sample extends Span { }
    private static class Code extends Span { }
     private static class Font extends Span
     {
         String _color;
         
         public Font(String color)
         {
             _color = color;
         }
         
         public String getColor()
         {
             return _color;
         }
     }
     
     private static class Href
     {
         String _href;
         
         public Href(String href)
         {
             _href = href;
         }
         
         public String getHref()
         {
             return _href;
         }
     }
     
     private static HashMap<String, Integer> COLORS = buildColorMap();
     
     private static HashMap<String, Integer> buildColorMap()
     {
         HashMap<String, Integer> map = new HashMap<String, Integer>();
         map.put("red", 0xFF0000);
         map.put("green", 0x8DC63F);
         map.put("pink", 0xF49AC1);
         map.put("olive", 0x808000);
         map.put("fuchsia", 0xC0FFC0);
         map.put("yellow", 0xFFDE00);
         map.put("blue", 0x44AEDF);
         map.put("lime", 0xC0FFC0);
         map.put("orange", 0xF7941C);
         return map;
     }
     
     private static int getColor(String color)
     {
         Integer i = COLORS.get(color.toLowerCase());
         if (i != null)
             return i;
         return -1;
     }
     
     
 }

 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.context.html;
 
 import java.io.IOException;
 import java.io.Writer;
import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 import javax.faces.context.ResponseWriter;
 
 import junit.framework.TestCase;
 
 import org.seasar.framework.util.SPrintWriter;
 import org.seasar.teeda.core.mock.NullWriter;
 import org.seasar.teeda.core.unit.ExceptionAssert;
 
 /**
  * @author manhole
  * @author yone
  */
 public class HtmlResponseWriterTest extends TestCase {
 
     public void testStartDocument() throws Exception {
         // nothing to do
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         responseWriter.startDocument();
     }
 
     public void testEndDocument() throws Exception {
         // nothing to do
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         responseWriter.endDocument();
     }
 
     public void testCloneWithWriter() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         responseWriter.setContentType("foo content type");
         responseWriter.setCharacterEncoding("bar encoding");
 
         final Writer writer = new NullWriter();
         final ResponseWriter newWriter = responseWriter.cloneWithWriter(writer);
 
         assertEquals(true, newWriter instanceof HtmlResponseWriter);
         final HtmlResponseWriter newResponseWriter = (HtmlResponseWriter) newWriter;
         assertSame(writer, newResponseWriter.getWriter());
         assertEquals("foo content type", newResponseWriter.getContentType());
         assertEquals("bar encoding", newResponseWriter.getCharacterEncoding());
     }
 
     public void testClose() throws Exception {
         // ## Arrange ##
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final boolean[] calls = { false };
         final Writer writer = new NullWriter() {
             public void close() throws IOException {
                 calls[0] = true;
             }
         };
         responseWriter.setWriter(writer);
 
         // ## Act ##
         responseWriter.close();
 
         // ## Assert ##
         assertTrue(calls[0]);
     }
 
     public void testFlush() throws Exception {
         // ## Arrange ##
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final boolean[] calls = { false };
         final Writer writer = new NullWriter() {
             public void flush() throws IOException {
                 calls[0] = true;
             }
         };
         responseWriter.setWriter(writer);
 
         // ## Act ##
         responseWriter.flush();
 
         // ## Assert ##
         assertFalse(calls[0]);
     }
 
     public void testCloneWithWriterWithNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.cloneWithWriter(null);
             fail();
         } catch (final NullPointerException npe) {
             final String message = npe.getMessage();
             assertTrue(((message != null) && (0 < message.length())));
         }
     }
 
     public void testStartElement() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("a", null);
 
         final String value = writer.toString();
         assertEquals("<a", value);
     }
 
     public final void testStartElement_NameIsNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.startElement(null, null);
             fail();
         } catch (final NullPointerException npe) {
             final String message = npe.getMessage();
             assertNotNull(message);
             assertTrue(message.trim().length() > 0);
         }
     }
 
     public void testEndElement() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.endElement("span");
 
         final String value = writer.toString();
         assertEquals("<span></span>", value);
     }
 
     public final void testEndElement_NameIsNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.endElement(null);
             fail();
         } catch (final NullPointerException npe) {
             final String message = npe.getMessage();
             assertNotNull(message);
             assertTrue(message.trim().length() > 0);
         }
     }
 
     public void testWrite_Chars() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.write("1234<a>".toCharArray());
         final String value = writer.toString();
         assertEquals("write raw characters", "1234<a>", value);
     }
 
     public void testWrite_CharsAndIndex() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.write("abcdefg".toCharArray(), 3, 2);
 
         final String value = writer.toString();
         assertEquals("de", value);
     }
 
     public void testWrite_CharsAndIndex_NoEncoding() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.write("1234<a>".toCharArray(), 3, 4);
 
         final String value = writer.toString();
         assertEquals("write raw characters", "4<a>", value);
     }
 
     public void testWrite_Int() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.write((int) '<');
 
         final String value = writer.toString();
         assertEquals("write raw characters", "<", value);
     }
 
     public void testWrite_String() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.write("aa>");
 
         final String value = writer.toString();
         assertEquals("write raw characters", "aa>", value);
     }
 
     public void testWrite_StringAndIndex() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.write("ab><d", 1, 3);
 
         final String value = writer.toString();
         assertEquals("write raw characters", "b><", value);
     }
 
     public void testWriteText_Object() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeText("abc", null);
         final String value = writer.toString();
         assertEquals("abc", value);
     }
 
     public void testWriteText_Object_WithNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.writeText(null, null);
             fail();
         } catch (final NullPointerException npe) {
             final String message = npe.getMessage();
             assertTrue(((message != null) && (0 < message.length())));
         }
     }
 
     public void testWriteText_Object_Encoding() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeText("<a>", null);
         final String value = writer.toString();
         assertEquals("&lt;" + "a" + "&gt;", value);
     }
 
     public void testWriteText_SingleQuart() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeText("abc'def", null);
         final String value = writer.toString();
 
         assertEquals("abc&#39;def", value);
     }
 
     public void testWriteText_SingleQuart2() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeText("'''''", null);
         final String value = writer.toString();
 
         assertEquals("&#39;&#39;&#39;&#39;&#39;", value);
     }
 
     public void testWriteText_Chars() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeText("abcdefg".toCharArray(), 3, 2);
         final String value = writer.toString();
         assertEquals("de", value);
     }
 
     public void testWriteText_Chars_WithNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.writeText((char[]) null, 0, 0);
             fail();
         } catch (final NullPointerException npe) {
             final String message = npe.getMessage();
             assertTrue(((message != null) && (0 < message.length())));
         }
     }
 
     public void testWriteText_Chars_Encoding() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeText("ab\"efgh".toCharArray(), 2, 4);
         final String value = writer.toString();
         assertEquals("&quot;" + "efg", value);
     }
 
     public void testWriteComment() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.writeComment("abc");
         final String value = writer.toString();
         assertEquals("<!--abc-->", value);
     }
 
     public void testWriteCommentWithNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
         try {
             responseWriter.writeComment(null);
             fail();
         } catch (final NullPointerException npe) {
             final String message = npe.getMessage();
             assertTrue(((message != null) && (0 < message.length())));
         }
     }
 
     public void testStartElementShouldBeClosed_ByStartElement()
             throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.startElement("a", null);
 
         final String value = writer.toString();
         assertEquals("<x><a", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteComment()
             throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeComment("foo");
 
         final String value = writer.toString();
         assertEquals("<span><!--foo-->", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteTextCharsAndIndex()
             throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeText("zxcvbnm".toCharArray(), 1, 3);
 
         final String value = writer.toString();
         assertEquals("<span>xcv", value);
     }
 
     public void testStartElementShouldBeClosed_ByEndDocument() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.endDocument();
 
         final String value = writer.toString();
         assertEquals("<span>", value);
     }
 
     public void testStartElementShouldBeClosed_ByClose() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter() {
             public void close() {
             }
         };
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("v", null);
         responseWriter.close();
 
         final String value = writer.toString();
         assertEquals("<v>", value);
     }
 
     public void testStartElementShouldBeClosed_ByFlush() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("r", null);
         responseWriter.flush();
 
         final String value = writer.toString();
         assertEquals("<r>", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteChars() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.write("aa".toCharArray());
 
         final String value = writer.toString();
         assertEquals("<x>aa", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteCharsAndIndex()
             throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.write("aa123".toCharArray(), 0, 2);
 
         final String value = writer.toString();
         assertEquals("<x>aa", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteInt() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.write((int) 'b');
 
         final String value = writer.toString();
         assertEquals("<x>b", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteString() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.write("aa");
 
         final String value = writer.toString();
         assertEquals("<x>aa", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteStringAndIndex()
             throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.write("aa123", 0, 2);
 
         final String value = writer.toString();
         assertEquals("<x>aa", value);
     }
 
     public void testStartElementShouldBeClosed_ByWriteTextObject()
             throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("x", null);
         responseWriter.writeText("aa", null);
 
         final String value = writer.toString();
         assertEquals("<x>aa", value);
     }
 
     public void testStartElementAndWriteTextAndEndElement() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeText("aa", null);
         responseWriter.endElement("span");
 
         final String value = writer.toString();
         assertEquals("<span>aa</span>", value);
     }
 
     public void testSetGetContentType() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         assertNull(responseWriter.getContentType());
         responseWriter.setContentType("some content type");
         assertEquals("some content type", responseWriter.getContentType());
     }
 
     public void testSetGetCharacterEncoding() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         // assertNull(responseWriter.getCharacterEncoding());
         responseWriter.setCharacterEncoding("some character encoding");
         assertEquals("some character encoding", responseWriter
                 .getCharacterEncoding());
     }
 
     public void testWriteAttribute() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeAttribute("a", "bb", null);
 
         final String value = writer.toString();
         assertEquals("<span a=\"bb\"", value);
     }
 
     public void testWriteAttribute_Escape() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeAttribute("a", "<b>", null);
 
         final String value = writer.toString();
         assertEquals("<span a=\"&lt;b&gt;\"", value);
     }
 
     public void testWriteAttribute_JavaScript() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeAttribute("aa",
                 "javascript:document.write('<b>foo</b>');", null);
 
         final String value = writer.toString();
         assertEquals(
                 "<span aa=\"javascript:document.write('&lt;b&gt;foo&lt;/b&gt;');\"",
                 value);
     }
 
     public void testWriteAttribute_NameIsNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.writeAttribute(null, "attrValue", null);
             fail();
         } catch (final NullPointerException npe) {
             ExceptionAssert.assertMessageExist(npe);
         }
     }
 
     public void testWriteAttribute_NoOpenElement() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.writeAttribute("attrName", "attrValue", null);
             fail();
         } catch (final IllegalStateException ise) {
             ExceptionAssert.assertMessageExist(ise);
         }
     }
 
     public void testWriteURIAttribute_NameIsNull() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.writeURIAttribute(null, "attrValue", null);
             fail();
         } catch (final NullPointerException npe) {
             ExceptionAssert.assertMessageExist(npe);
         }
     }
 
     public void testWriteURIAttribute_NoOpenElement() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         try {
             responseWriter.writeURIAttribute("attrName", "attrValue", null);
             fail();
         } catch (final IllegalStateException ise) {
             ExceptionAssert.assertMessageExist(ise);
         }
     }
 
     public void testWriteURIAttribute() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter() {
             protected String encodeURIAttribute(String url) throws IOException {
                 return url + "_add";
             }
         };
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("z", null);
         responseWriter.writeURIAttribute("aaa", "bbb", null);
 
         assertEquals("<z aaa=\"bbb_add\"", writer.toString());
     }
 
     public void testWriteURIAttribute_JavaScript() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("span", null);
         responseWriter.writeURIAttribute("aa",
                 "javascript:document.write('<b>foo</b>');", null);
 
         final String value = writer.toString();
         assertEquals(
                 "<span aa=\"javascript:document.write('&lt;b&gt;foo&lt;/b&gt;');\"",
                 value);
     }
 
     public void testWriteText_ScriptBody() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("script", null);
         responseWriter.writeText("<>\"&", null);
         responseWriter.endElement("script");
 
         final String value = writer.toString();
         assertEquals("script body is not escaped", "<script><>\"&</script>",
                 value);
     }
 
     public void testWriteText_ScriptBodyUpperCase() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
         final SPrintWriter writer = new SPrintWriter();
         responseWriter.setWriter(writer);
 
         responseWriter.startElement("SCRIPT", null);
         responseWriter.writeText("<>\"&", null);
         responseWriter.endElement("SCRIPT");
 
         final String value = writer.toString();
         assertEquals("SCRIPT body is not escaped", "<SCRIPT><>\"&</SCRIPT>",
                 value);
     }
 
     public void testEncodeURIAttribute() throws Exception {
         final HtmlResponseWriter responseWriter = new HtmlResponseWriter();
 
         assertEquals("url", responseWriter.encodeURIAttribute("url"));
         assertEquals("/a/b.html", responseWriter
                 .encodeURIAttribute("/a/b.html"));
         assertEquals("url?a=b", responseWriter.encodeURIAttribute("url?a=b"));
         assertEquals("url?a=b%3Fc=d", responseWriter
                 .encodeURIAttribute("url?a=b?c=d"));
         assertEquals("ur%25l?a=b", responseWriter
                 .encodeURIAttribute("ur%l?a=b"));
         assertEquals("url?a=b&amp;c=d", responseWriter
                 .encodeURIAttribute("url?a=b&c=d"));
         assertEquals("a?1=%20", responseWriter.encodeURIAttribute("a?1= "));
 
         assertEquals("url?a=%E3%81%82", responseWriter
                .encodeURIAttribute("url?a=あ"));
         assertEquals("%E3%81%82?a=1", responseWriter
                .encodeURIAttribute("あ?a=1"));
 
         assertEquals("#", responseWriter.encodeURIAttribute("#"));
         assertEquals("/a.html#aaa", responseWriter
                 .encodeURIAttribute("/a.html#aaa"));
         assertEquals("http://hoge.foo/bar#", responseWriter
                 .encodeURIAttribute("http://hoge.foo/bar#"));
         assertEquals("/a.html?1=2&amp;3=4#aaa", responseWriter
                 .encodeURIAttribute("/a.html?1=2&3=4#aaa"));
     }
 
     public void testLeaningURLEncoder() throws Exception {
         assertEquals("%3B", URLEncoder.encode(";", "UTF-8"));
         assertEquals("%2F", URLEncoder.encode("/", "UTF-8"));
         assertEquals("%3F", URLEncoder.encode("?", "UTF-8"));
         assertEquals("%3A", URLEncoder.encode(":", "UTF-8"));
         assertEquals("%40", URLEncoder.encode("@", "UTF-8"));
         assertEquals("%26", URLEncoder.encode("&", "UTF-8"));
         assertEquals("%3D", URLEncoder.encode("=", "UTF-8"));
         assertEquals("%2B", URLEncoder.encode("+", "UTF-8"));
         assertEquals("%24", URLEncoder.encode("$", "UTF-8"));
         assertEquals("%2C", URLEncoder.encode(",", "UTF-8"));
         assertEquals("%23", URLEncoder.encode("#", "UTF-8"));
 
         assertEquals("-", URLEncoder.encode("-", "UTF-8"));
         assertEquals("_", URLEncoder.encode("_", "UTF-8"));
         assertEquals(".", URLEncoder.encode(".", "UTF-8"));
 
         assertEquals("%21", URLEncoder.encode("!", "UTF-8"));
         assertEquals("%7E", URLEncoder.encode("~", "UTF-8"));
         assertEquals("*", URLEncoder.encode("*", "UTF-8"));
         assertEquals("%27", URLEncoder.encode("'", "UTF-8"));
         assertEquals("%28", URLEncoder.encode("(", "UTF-8"));
         assertEquals("%29", URLEncoder.encode(")", "UTF-8"));
         assertEquals("%25", URLEncoder.encode("%", "UTF-8"));
         assertEquals("%3C", URLEncoder.encode("<", "UTF-8"));
         assertEquals("%3E", URLEncoder.encode(">", "UTF-8"));
         assertEquals("%22", URLEncoder.encode("\"", "UTF-8"));
         assertEquals("%27", URLEncoder.encode("\'", "UTF-8"));
 
         assertEquals("+", URLEncoder.encode(" ", "UTF-8"));
 
        assertEquals("3042", Integer.toHexString('あ'));
        assertEquals("%E3%81%82", URLEncoder.encode("あ", "UTF-8"));
        assertEquals("%82%A0", URLEncoder.encode("あ", "Windows-31J"));
    }

    public void testLeaningURLDecoder() throws Exception {
        assertEquals(
                "https://hogehoge.co.jp/test/cgi-bin/test.cgi?cmd=login",
                URLDecoder
                        .decode(
                                "https%3A//hogehoge.co.jp/test/cgi-bin/test.cgi%3Fcmd=login",
                                "UTF-8"));
     }
 
     public void testLearningCharacter() throws Exception {
         assertEquals(true, Character.isLetter('A'));
        assertEquals(true, Character.isLetter('あ'));
 
         assertEquals(false, Character.isLetter('0'));
         assertEquals(false, Character.isLetter((char) 55893));
     }
 
 }

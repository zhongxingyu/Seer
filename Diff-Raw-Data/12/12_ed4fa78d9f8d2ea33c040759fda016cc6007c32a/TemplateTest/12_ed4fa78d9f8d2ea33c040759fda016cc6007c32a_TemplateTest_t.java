 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import java.util.*;
 
 /**
  * Junit tests for the Template class.
  */
 
 public class TemplateTest extends junit.framework.TestCase {
     StringBuilder out;
     Dataset data;
     Template.ExpandInfo info;
     Template.TextFragment text = new Template.TextFragment("hello");
     ArrayList list = new ArrayList();
     Template.ParsedTemplate parsed;
     Template.SpecialChars none = Template.SpecialChars.NONE;
 
     public void setUp() {
         list = new ArrayList();
         list.add(text);
         parsed  = new Template.ParsedTemplate(list);
         out = new StringBuilder();
         data = new Dataset("hello", "olleh", "foo", "oof", "bar", "rab");
         info = new Template.ExpandInfo(out, "mytemplate", none, null, data, "hi", "bye", null);
     }
 
     // The following class definition provides a mechanism for accessing
     // protected/private fields and methods.
     protected static class TemplateFixture {
         public static int end;
         public static String text;
         public static int lastDeletedSpace;
 
         public static String expandCache(Template.ParsedTemplate cache, Dataset data) {
             StringBuilder out = new StringBuilder();
             Template.ExpandInfo info = new Template.ExpandInfo(out, "", Template.SpecialChars.NONE,
                                                                null, data, "hi", "bye");
             cache.expand(info);
             return info.out.toString();
         }
 
         public static String parseTo(String template, Dataset data,
                 int start, String ... delims) {
             Template.ParseInfo info = new Template.ParseInfo(template);
             info.end = -1;
             Template.parseTo(info, start, delims);
             TemplateFixture.end = info.end;
             return TemplateFixture.expandCache(info.parse, data);
         }
 
         public static String parseAtSign(String template, Dataset data,
                 boolean conditional, int start) {
             Template.ParseInfo info = new Template.ParseInfo(template);
             info.end = -1;
             Template.parseAtSign(info, start);
             end = info.end;
             text = info.text.toString();
             return TemplateFixture.expandCache(info.parse, data);
         }
 
         public static String parseChoice(String template, Dataset data,
                 String name, int start) {
             Template.ParseInfo info = new Template.ParseInfo(template);
             info.end = -1;
             Template.parseChoice(info, name, start);
             end = info.end;
             return TemplateFixture.expandCache(info.parse, data);
         }
 
         public static String parseParenName(String template, Dataset data,
                 boolean conditional, int start,
                 Template.SpecialChars encoding) {
             Template.ParseInfo info = new Template.ParseInfo(template);
             info.end = -1;
             Template.parseParenName(info, start);
             end = info.end;
             return TemplateFixture.expandCache(info.parse, data);
         }
 
         public static String parseBraces(String template, Dataset data,
                                          int start, int lastDeletedSpace) {
             Template.ParseInfo info = new Template.ParseInfo(template);
             info.end = -1;
             Template.parseBraces(info, start);
             end = info.end;
             lastDeletedSpace = info.lastDeletedSpace;
             return TemplateFixture.expandCache(info.parse, data);
         }
 
         public static String parseBraces(String template, Dataset data,
                                          int start) {
             return parseBraces(template, data, start, -1);
         }
     }
 
     public void test_missingValueError() {
         Template.MissingValueError e = new Template.MissingValueError("a", "b");
         assertEquals("error message", "missing value \"a\" in template \"b\"",
                      e.getMessage());
     }
     public void test_syntaxError() {
         Template.SyntaxError e = new Template.SyntaxError(
                 "sample message");
         assertEquals("error message", "sample message", e.getMessage());
     }
     public void test_ParsedTemplate() {
         Template.ParsedTemplate c = new Template.ParsedTemplate();
         assertEquals("number fragments, empty", 0, c.fragments.size());
 
         list.add(new Template.IdFragment("blah"));
         c = new Template.ParsedTemplate(list);
         assertEquals("number fragments with list", 2, c.fragments.size());
 
         Template.TextFragment f = new Template.TextFragment("blah2");
         c.addFragment(f);
         assertEquals("add fragement", f, c.fragments.get(2));
 
         c.expand(new Template.ExpandInfo(out, "", none, null, new Dataset("blah", "halb")));
         assertEquals("expand", "hellohalbblah2", out.toString());
 
     }
     public void test_TextFragment() {
         Template.TextFragment text = new Template.TextFragment("@hi");
         text.expand(info);
         assertEquals("@hi", out.toString());
     }
     public void test_IDFragment_string() {
         Template.IdFragment text = new Template.IdFragment("foo");
         text.expand(info);
         assertEquals("oof", out.toString());
     }
     public void test_IDFragment_int() {
         Template.IdFragment text = new Template.IdFragment("1");
         text.expand(info);
         assertEquals("hi", out.toString());
     }
     public void test_IDFragment_parens() {
         Template.ParsedTemplate pt = new Template.ParsedTemplate(list);
         Template.IdFragment text = new Template.IdFragment(pt);
         text.expand(new Template.ExpandInfo(out, null, none, null, new Dataset("hello", "olleh")));
         assertEquals("olleh", out.toString());
     }
     public void test_IDFragment_findValue_parens() {
         Template.IdFragment idf = new Template.IdFragment(parsed);
         String val = idf.findValue(info, true);
         assertEquals("dataset value", "olleh", val);
 
         list = new ArrayList<Template.Fragment>();
         list.add(new Template.TextFragment("2"));
         parsed = new Template.ParsedTemplate(list);
         idf = new Template.IdFragment(parsed);
         info = new Template.ExpandInfo(out, null, none, null, data, null, "bye");
         val = idf.findValue(info, true);
         assertEquals("indexed value", "bye", val);
     }
     public void test_IDFragment_findValue_String() {
         Template.IdFragment idf = new Template.IdFragment("hello");
         String val = idf.findValue(info, true);
         assertEquals("dataset value", "olleh", val);
 
         idf = new Template.IdFragment("2");
         val = idf.findValue(info, true);
         assertEquals("index value", "bye", val);
     }
     public void test_IDFragment_findValue_throwError() {
         Template.IdFragment idf = new Template.IdFragment("bogus");
         try {
             idf.findValue(info, true);
             fail("Exception not thrown");
         } catch (Template.MissingValueError e) {
             assertEquals("missing value \"bogus\" in template \"mytemplate\"",
                          e.getMessage());
         }
     }
     public void test_IDFragment_findValue_dontThrowError() {
         Template.IdFragment idf;
         String val;
 
         idf = new Template.IdFragment("bogus");
         val = idf.findValue(info, false);
         assertEquals("bogus dataset value", null, val);
 
         idf = new Template.IdFragment("10");
         val = idf.findValue(info, false);
         assertEquals("bogus index", null, val);
 
         idf = new Template.IdFragment("3");
         val = idf.findValue(info, false);
         assertEquals("null index", null, val);
 
         idf = new Template.IdFragment("bogus");
         val = idf.findValue(new Template.ExpandInfo(out, null, none, null, null,
                                                     false), false);
         assertEquals("null dataset", null, val);
     }
     public void test_DefaultFragment() {
         Template.IdFragment id = new Template.IdFragment("foo");
         Template.DefaultFragment frag = new Template.DefaultFragment(id, parsed);
         frag.expand(new Template.ExpandInfo(out, null, none, null, data));
         assertEquals("use @ variable", "oof", out.toString());
 
         out.setLength(0);
         id = new Template.IdFragment("bogus");
         frag = new Template.DefaultFragment(id, parsed);
         frag.expand(new Template.ExpandInfo(out, null, none, null, data));
         assertEquals("use default value", "hello", out.toString());
     }
     public void test_ChoiceFragment() {
         text = new Template.TextFragment("goodbye");
         ArrayList list2 = new ArrayList();
         list2.add(text);
         Template.ParsedTemplate cache2 = new Template.ParsedTemplate(list2);
 
         Template.IdFragment id = new Template.IdFragment("foo");
         Template.ChoiceFragment frag = new Template.ChoiceFragment(id, parsed, cache2);
         frag.expand(new Template.ExpandInfo(out, null, none, null, data));
         assertEquals("first choice", "hello", out.toString());
 
         out.setLength(0);
         id = new Template.IdFragment("bogus");
         frag = new Template.ChoiceFragment(id, parsed, cache2);
         frag.expand(new Template.ExpandInfo(out, null, none, null, data));
         assertEquals("second choice", "goodbye", out.toString());
     }
 
     public void test_ConditionalFragment() {
         Template.ConditionalFragment bracket = new Template.ConditionalFragment(parsed);
         bracket.expand(new Template.ExpandInfo(out, null, none, null, data));
         assertEquals("add to output", "hello", out.toString());
 
         // make sure it doesn't just wipe out the string builder
         out.setLength(2);
         list = new ArrayList();
         Template.IdFragment id = new Template.IdFragment("bogus");
         list.add(id);
         parsed = new Template.ParsedTemplate(list);
         bracket = new Template.ConditionalFragment(parsed);
         bracket.expand(new Template.ExpandInfo(out, null, none, null, data));
         assertEquals("do not add to output", "he", out.toString());
     }
 
     public void test_addValue() {
         String val = "< \"a";
         info = new Template.ExpandInfo(out, null, Template.SpecialChars.HTML,
                                        null, data);
         Template.addValue(info, val);
         assertEquals("html", "&lt; &quot;a", out.toString());
 
         out.setLength(0);
         info = new Template.ExpandInfo(out, null, Template.SpecialChars.JS,
                                        null, data);
         Template.addValue(info, val);
         assertEquals("javascript", "< \\\"a", out.toString());
 
         out.setLength(0);
         info = new Template.ExpandInfo(out, null, Template.SpecialChars.URL,
                                        null, data);
         Template.addValue(info, val);
         assertEquals("url", "%3c+%22a", out.toString());
 
         out.setLength(0);
         info = new Template.ExpandInfo(out, null, Template.SpecialChars.NONE,
                                        null, data);
         Template.addValue(info, val);
         assertEquals("none", "< \"a", out.toString());
 
         out.setLength(0);
         info = new Template.ExpandInfo(out, null, null,
                                        new ArrayList<String>(), data);
         Template.addValue(info, val);
         assertEquals("sql out", "?", out.toString());
         assertEquals("sql add to array", "< \"a", info.sqlParameters.get(0).toString());
     }
 
     public void test_expand_getFromCache() {
         out = Template.expand(null, "foo", none, null);
         assertEquals("first expand", "foo", out.toString());
 
         Template.parsedTemplates.put("foo", new Template.ParsedTemplate(list));
         out = Template.expand(null, "foo", none, null);
         assertEquals("second expand", "hello", out.toString());
     }
 
     public void test_expandHtml() {
         Dataset data = new Dataset("name", "<Alice>", "age", "28");
         String result = Template.expandHtml("name: @name, age: @2",
                 data, "abc", "25");
         assertEquals("output string", "name: &lt;Alice&gt;, age: 25",
                 result);
     }
 
     public void test_expandHtml_noDataset() {
         String result = Template.expandHtml("name: @1, age: @2",
                 "<Alice>", "25");
         assertEquals("output string", "name: &lt;Alice&gt;, age: 25",
                 result);
     }
 
     public void test_appendHtml() {
         Dataset data = new Dataset("name", "<Alice>", "age", "28");
         StringBuilder out = new StringBuilder("123");
         Template.appendHtml(out, "name: @name, age: @2",
                 data, "abc", "25");
         assertEquals("output string", "123name: &lt;Alice&gt;, age: 25",
                 out.toString());
     }
 
     public void test_appendHtml_noDataset() {
         StringBuilder out = new StringBuilder("123");
         Template.appendHtml(out, "name: @1, age: @2", "<Alice>", "25");
         assertEquals("output string", "123name: &lt;Alice&gt;, age: 25",
                 out.toString());
     }
 
     public void test_expandJs() {
         Dataset data = new Dataset("name", "\"Alice\n", "age", "28");
         String result = Template.expandJs("name: @name, age: @2",
                 data, "abc", "a\tb");
         assertEquals("output string", "name: \\\"Alice\\n, age: a\\tb",
                 result);
     }
 
     public void test_expandJs_noDataset() {
         String result = Template.expandJs("name: @1, age: @2",
                 "\"Alice\n", "a\tb");
         assertEquals("output string", "name: \\\"Alice\\n, age: a\\tb",
                 result);
     }
 
     public void test_appendJs() {
         Dataset data = new Dataset("name", "\"Alice\n", "age", "28");
         StringBuilder out = new StringBuilder("123");
         Template.appendJs(out, "name: @name, age: @2",
                 data, "abc", "a\tb");
         assertEquals("output string", "123name: \\\"Alice\\n, age: a\\tb",
                 out.toString());
     }
 
     public void test_appendJs_noDataset() {
         StringBuilder out = new StringBuilder("123");
         Template.appendJs(out, "name: @1, age: @2", "\"Alice\n", "25");
         assertEquals("output string", "123name: \\\"Alice\\n, age: 25",
                 out.toString());
     }
 
     public void test_expandUrl() {
         Dataset data = new Dataset("name", "C&H Sugar");
         String result = Template.expandUrl("/a/b?name=@name&count=@2",
                 data, "abc", 99);
         assertEquals("output string", "/a/b?name=C%26H+Sugar&count=99",
                 result);
     }
 
     public void test_expandUrl_noDataset() {
         String result = Template.expandUrl("/a/b?name=@1&count=@2",
                 "C&H Sugar", 99);
         assertEquals("output string", "/a/b?name=C%26H+Sugar&count=99",
                 result);
     }
 
     public void test_appendUrl() {
         Dataset data = new Dataset("name", "C&H Sugar", "age", "28");
         StringBuilder out = new StringBuilder("123");
         Template.appendUrl(out, "/a/b?name=@name&count=@2",
                 data, "abc", 99);
         assertEquals("output string", "123/a/b?name=C%26H+Sugar&count=99",
                 out.toString());
     }
 
     public void test_appendUrl_noDataset() {
         StringBuilder out = new StringBuilder("123");
         Template.appendUrl(out, "/a/b?name=@1&count=@2", "C&H Sugar", 99);
         assertEquals("output string", "123/a/b?name=C%26H+Sugar&count=99",
                 out.toString());
     }
 
     public void test_expandRaw() {
         Dataset data = new Dataset("x", "<\">");
         String result = Template.expandRaw("first: @x, second: @2",
                 data, "abc", "&\n");
         assertEquals("output string", "first: <\">, second: &\n",
                 result);
     }
 
     public void test_expandRaw_noDataset() {
         String result = Template.expandRaw("first: @1, second: @2",
                 "<\">", "&\n");
         assertEquals("output string", "first: <\">, second: &\n",
                 result);
     }
 
     public void test_appendRaw() {
         Dataset data = new Dataset("x", "<\">");
         StringBuilder out = new StringBuilder("123");
         Template.appendRaw(out, "first: @x, second: @2",
                 data, "abc", "&\n");
         assertEquals("output string", "123first: <\">, second: &\n",
                 out.toString());
     }
 
     public void test_appendRaw_noDataset() {
         StringBuilder out = new StringBuilder("123");
         Template.appendRaw(out, "first: @1, second: @2", "<\">", "&\n");
         assertEquals("output string", "123first: <\">, second: &\n",
                 out.toString());
     }
 
     public void test_expandSql() {
         ArrayList<String> parameters = new ArrayList<String>();
         String result = Template.expandSql("SELECT * FROM people " +
                 "WHERE name=@name AND age=@age;",
                 new Dataset("name", "\"Alice\"", "age", "28"), parameters);
         assertEquals("output string",
                 "SELECT * FROM people WHERE name=? AND age=?;",
                 result);
         assertEquals("saved variables", "\"Alice\", 28",
                 StringUtil.join(parameters, ", "));
     }
     public void test_expandSql_withConditional() {
         ArrayList<String> parameters = new ArrayList<String>();
         String result = Template.expandSql("xyz{{@a @x @c}}abc{{b:@b}}",
                 new Dataset("a", "0", "b", "1", "c", "2"), parameters);
         assertEquals("output string", "xyzabcb:?",
                 result);
         assertEquals("saved variables", "1",
                 StringUtil.join(parameters, ", "));
     }
     public void test_expandSql_withSkips() {
         ArrayList<String> parameters = new ArrayList<String>();
         String result = Template.expandSql("@a?{b:@b} @x?{a:@a|c:@c}",
                 new Dataset("a", "0", "b", "1", "c", "2"), parameters);
         assertEquals("output string", "? c:?",
                 result);
         assertEquals("saved variables", "0, 2",
                 StringUtil.join(parameters, ", "));
     }
 
     public void test_parseTo_atSign() {
         Dataset data = new Dataset("name", "Alice", "age", "28");
         String s = TemplateFixture.parseTo("---name: @name ---", data, 2);
         assertEquals("output string", "-name: Alice ---", s);
     }
     public void test_parseTo_openBracesAtEnd() {
         Dataset data = new Dataset("name", "Alice", "age", "28");
         String s = TemplateFixture.parseTo("abc{", data, 0);
         assertEquals("output string", "abc{", s);
         boolean gotException = false;
         try {
             TemplateFixture.parseTo("abc{{", data, 1);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "unmatched \"{{\" in template \"abc{{\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseTo_singleBrace() {
         Dataset data = new Dataset("name", "Alice", "age", "28");
         String s = TemplateFixture.parseTo("{}}", data, 0);
         assertEquals("output string", "{}}", s);
     }
     public void test_parseAtSign_nothingAfterAtSign() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseAtSign("xx@", data, false, 3);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "dangling \"@\" in template \"xx@\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseAtSign_choice() {
         Dataset data = new Dataset("age", "28");
         StringBuilder out = new StringBuilder("123");
         Template.appendHtml(out, "name: @name?{unknown}, xyz", data, out);
         assertEquals("output string", "123name: unknown, xyz", out.toString());
     }
     public void test_parseAtSign_simpleName() {
         Dataset data = new Dataset("name", "Alice");
         String s = TemplateFixture.parseAtSign("xx@name + foo", data, false, 3);
         assertEquals("end of specifier", 7, TemplateFixture.end);
         assertEquals("output string", "Alice", s);
     }
     public void test_parseAtSign_simpleNameStartsWithDigit() {
         String s = TemplateFixture.parseAtSign("xx@2t + foo", data, false, 3);
         assertEquals("end of specifier", 4, TemplateFixture.end);
         assertEquals("output string", "bye", s);
     }
     public void test_parseAtSign_nameInParens() {
         Dataset data = new Dataset("name", "Alice");
         String s = TemplateFixture.parseAtSign("xx@(name)", data, false, 3);
         assertEquals("end of specifier", 9, TemplateFixture.end);
         assertEquals("output string", "Alice", s);
     }
     public void test_parseAtSign_atSign() {
         Dataset data = new Dataset("name", "Alice");
         TemplateFixture.parseAtSign("xx@@yyy", data, false, 3);
         assertEquals("end of specifier", 4, TemplateFixture.end);
         assertEquals("output string", "@", TemplateFixture.text);
     }
     public void test_parseAtSign_openBrace() {
         Dataset data = new Dataset("name", "Alice");
         TemplateFixture.parseAtSign("xx@{yyy", data, false, 3);
         assertEquals("end of specifier", 4, TemplateFixture.end);
         assertEquals("output string", "{", TemplateFixture.text);
     }
     public void test_parseAtSign_closeBrace() {
         Dataset data = new Dataset("name", "Alice");
         TemplateFixture.parseAtSign("xx@}yyy", data, false, 3);
         assertEquals("end of specifier", 4, TemplateFixture.end);
         assertEquals("output string", "}", TemplateFixture.text);
     }
     public void test_parseAtSign_illegalCharacter() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseAtSign("xx@+yyy", data, false, 3);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "invalid sequence \"@+\" in template \"xx@+yyy\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseChoice_endOfTemplate() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseChoice("@foo?", data, "name", 5);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "missing \"{\" after \"?\" in template \"@foo?\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseChoice_braceMissing() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseChoice("@foo?(", data, "name", 5);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "missing \"{\" after \"?\" in template \"@foo?(\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseChoice_noBarOrCloseBrace() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseChoice("@foo?{aaa", data, "name", 5);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "incomplete @...?{...} substitution in template " +
                     "\"@foo?{aaa\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseChoice_noVerticalBar_variableExists() {
         Dataset data = new Dataset("name", "<West>");
         String s = TemplateFixture.parseChoice("@foo?{aaa}zz", data, "name", 5);
         assertEquals("output string", "<West>", s);
         assertEquals("info.end", 10, TemplateFixture.end);
     }
     public void test_parseChoice_noVerticalBar_variableDoesntExist() {
         Dataset data = new Dataset("age", "50");
         String s = TemplateFixture.parseChoice("@foo?{@age!}zz", data,
                     "name", 5);
         assertEquals("output string", "50!", s);
         assertEquals("info.end", 12, TemplateFixture.end);
     }
     public void test_parseChoice_noVerticalBar_emptyValue() {
         Dataset data = new Dataset("name", "", "age", "50");
         String s = TemplateFixture.parseChoice("@foo?{@age!}zz", data,
                     "name", 5);
         assertEquals("output string", "50!", s);
     }
     public void test_parseChoice_barButNoCloseBrace() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseChoice("@foo?{aaa|zz", data, "name", 5);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "incomplete @...?{...} substitution in template " +
                     "\"@foo?{aaa|zz\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseChoice_verticalBar_variableExists() {
         Dataset data = new Dataset("name", "<West>", "age", "24",
                 "weight", "125");
         String template = "@foo?{(@age)|[@weight]}zz";
         String s = TemplateFixture.parseChoice(template, data, "name", 5);
         assertEquals("output string", "(24)", s);
         assertEquals("remainder of template", "zz",
                 template.substring(TemplateFixture.end));
     }
     public void test_parseChoice_verticalBar_variableDoesntExist() {
         Dataset data = new Dataset("age", "24", "weight", "125");
         String template = "@foo?{(@age)|[@weight]}zz";
         String s = TemplateFixture.parseChoice(template, data, "name", 5);
         assertEquals("output string", "[125]", s);
         assertEquals("remainder of template", "zz",
                 template.substring(TemplateFixture.end));
     }
     public void test_parseChoice_verticalBar_emptyValue() {
         Dataset data = new Dataset("name", "", "age", "24", "weight", "125");
         String template = "@foo?{(@age)|[@weight]}zz";
         String s = TemplateFixture.parseChoice(template, data, "name", 5);
         assertEquals("output string", "[125]", s);
     }
     public void test_parseParenName_saveAndRestoreEncoding() {
         Dataset data = new Dataset("<name>", "<West>");
         String s = TemplateFixture.parseParenName("@(<name>)", data,
                 false, 2, Template.SpecialChars.HTML);
         assertEquals("output string", "<West>", s);
     }
     public void test_parseParenName_nestedAtSign() {
         Dataset data = new Dataset("last_name", "West", "name", "last_name");
         String s = TemplateFixture.parseParenName("@(@name)", data, false, 2,
                 Template.SpecialChars.NONE);
         assertEquals("end of specifier", 8, TemplateFixture.end);
         assertEquals("output string", "West", s);
     }
     public void test_parseParenName_doublyNestedAtSign() {
         Dataset data = new Dataset("last_name", "West", "name", "name2",
                 "name2", "last_name");
         String s = TemplateFixture.parseParenName("@(@(@name))", data, false, 2,
                 Template.SpecialChars.NONE);
         assertEquals("end of specifier", 11, TemplateFixture.end);
         assertEquals("output string", "West", s);
     }
     public void test_parseParenName_concatenation() {
         Dataset data = new Dataset("last_name", "West", "name1", "last",
                 "name2", "name");
         String s = TemplateFixture.parseParenName("@(@(name1)_@name2)", data, false, 2,
                 Template.SpecialChars.NONE);
         assertEquals("end of specifier", 18, TemplateFixture.end);
         assertEquals("output string", "West", s);
     }
     public void test_parseParenName_missingCloseParen() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseParenName("@(abcdef", data, false, 2,
                     Template.SpecialChars.NONE);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "missing \")\" for \"@(\" in template \"@(abcdef\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseBraces_basics() {
         Dataset data = new Dataset("last_name", "West");
         String s = TemplateFixture.parseBraces("{{abc@last_name@}}x}}y", data, 2);
         assertEquals("end of specifier", 21, TemplateFixture.end);
         assertEquals("output string", "abcWest}}x", s);
     }
     public void test_parseBraces_closeBracesAtEnd() {
         Dataset data = new Dataset("last_name", "West");
         String s = TemplateFixture.parseBraces("{{abc}}", data, 2);
         assertEquals("output string", "abc", s);
     }
     public void test_parseBraces_singleCloseBraceAtEnd() {
         Dataset data = new Dataset("last_name", "West");
         boolean gotException = false;
         try {
             TemplateFixture.parseBraces("{{abc}", data, 2);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "unmatched \"{{\" in template \"{{abc}\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_parseBraces_missingData() {
         Dataset data = new Dataset("last_name", "West");
         String s = TemplateFixture.parseBraces("{{abc@data.47@data2}}x", data, 2);
         assertEquals("end of specifier", 21, TemplateFixture.end);
         assertEquals("output string", "", out.toString());
     }
     public void test_parseBraces_discardCollectedVariables() {
         Dataset data = new Dataset("a", "0", "x", "1", "y", "2");
         ArrayList<String> parameters = new ArrayList<String>();
         String out = Template.expandSql("a:@a {{x:@x x:@x z:@z}} y:@y", data, parameters);
         assertEquals("expanded template", "a:? y:?", out);
         assertEquals("saved variables", "0, 2",
                 StringUtil.join(parameters, ", "));
     }
     public void test_parseBraces_missingCloseBrace() {
         boolean gotException = false;
         try {
             Dataset data = new Dataset();
             TemplateFixture.parseBraces("{{abc", data, 2);
         }
         catch (Template.SyntaxError e) {
             assertEquals("exception message",
                     "unmatched \"{{\" in template \"{{abc\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_collapseSpaces_precedingSpace() {
         Dataset data = new Dataset();
         StringBuilder out = new StringBuilder();
         Template.appendHtml(out, "x {{@foo}}", data);
         assertEquals("braces at end of string", "x", out.toString());
         out.setLength(0);
         Template.appendHtml(out, "- {{@foo}} -", data);
         assertEquals("spaces on both sides", "- -", out.toString());
         out.setLength(0);
         Template.appendHtml(out, "x {{@x}}] x {{@x}}> x {{@x}}} x {{@x}}) x "
                 + "{{@x}}\" x {{@x}}\'",
                 data);
         assertEquals("close-delimiter follows braces", "x] x> x} x) x\" x'",
                 out.toString());
 
         out.setLength(0);
         Template.appendHtml(out, "<{{@x}} {{@x}}>", data);
         assertEquals("don't delete the same space twice", "<>",
                 out.toString());
         out.setLength(0);
         Template.appendHtml(out, "{{@x}}) + b{{@x}}> + {{@x}}y", data);
         assertEquals("don't remove spaces", ") + b> + y",
                 out.toString());
     }
    /*
    public void test_collapseSpaces_atExpansionTime() {
        out.setLength(0);
        Template.appendHtml(out, "<{{@a}} {{@x}}>", new Dataset("a", "y"));
        assertEquals("brackets on the left stay", "<y>",
                out.toString());
        out.setLength(0);
        Template.appendHtml(out, "<{{@x}} {{@a}}>", new Dataset("a", "y"));
        assertEquals("brackets on the right stay", "<y>",
                out.toString());
    }
    */
     public void test_collapseSpaces_trailingSpace() {
         Dataset data = new Dataset();
         StringBuilder out = new StringBuilder();
         Template.appendHtml(out, "{{@x}} bcd", data);
         assertEquals("braces at start of string", "bcd", out.toString());
         out.setLength(0);
         Template.appendHtml(out, "[{{@x}} + <{{@x}} + @{{{@x}} + ({{@x}} + "
                 + "\"{{@x}} + \'{{@x}} +",
                 data);
         assertEquals("open-delimiter precedes braces", "[+ <+ {+ (+ \"+ '+",
                 out.toString());
 
         out.setLength(0);
         Template.appendHtml(out, "<{{@x}}abc + y{{@x}}y + <{{@x}}", data);
         assertEquals("don't remove spaces", "<abc + yy + <",
                 out.toString());
     }
     /*
     public void test_movePreceedingSpace() {
         Template.ParseInfo pInfo;
         ArrayList<Template.Fragment> bogusId = new ArrayList<Template.Fragment>();
         bogusId.add(new Template.IdFragment("bogus"));
         Template.Fragment emptyConditional = new Template.ConditionalFragment(
             new Template.ParsedTemplate(bogusId));
 
         ArrayList<Template.Fragment> list = new ArrayList<Template.Fragment>();
         list.add(new Template.TextFragment("abc "));
         list.add(emptyConditional);
         parsed = new Template.ParsedTemplate(list);
         pInfo = new Template.ParseInfo("");
         pInfo.parse = parsed;
         Template.movePreceedingSpace(pInfo);
         parsed.expand(info);
         assertEquals("empty conditional", "abc", info.out.toString());
     }
     */
     public void test_foundDelimiter() {
         assertEquals("one delim, not at end", true, Template.foundDelimiter("}|", 0, "}"));
         assertEquals("two delim", true, Template.foundDelimiter("o}", 1, "|", "}"));
         assertEquals("multiple characters", true, Template.foundDelimiter("abc", 0, "ab"));
         assertEquals("no delim", false, Template.foundDelimiter("}", 0));
         assertEquals("not found", false, Template.foundDelimiter("abc", 0, "d", "e", "fghij"));
         assertEquals("at end", false, Template.foundDelimiter("abc", 3, "d"));
     }
     public void test_flushText() {
         Template.ParseInfo parseInfo = new Template.ParseInfo("");
         Template.flushText(parseInfo);
         assertEquals("no text", 0, parseInfo.parse.fragments.size());
 
         parseInfo.text = new StringBuilder("hey");
         Template.flushText(parseInfo);
         assertEquals("with text", "hey",
                      ((Template.TextFragment) parseInfo.parse.fragments.get(0)).text);
     }
     public void test_variousComplexTemplates() {
         StringBuilder out = new StringBuilder();
         Template.appendHtml(out, "first {{name: @name, age: @age?{unknown}}}",
                 new Dataset("name", "Bob"));
         assertEquals("output string", "first name: Bob, age: unknown",
                 out.toString());
 
         out.setLength (0);
         Template.appendHtml(out, "first {{name: {{@name}},{{@bogus}} age: @age?{unknown}, " +
                 "weight: @weight}}", new Dataset("name", "Bob"));
         assertEquals("output string", "first", out.toString());
     }
 
 }

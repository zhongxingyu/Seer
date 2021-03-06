 /*
  * Copyright (c) 2002-2008 Gargoyle Software Inc. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. The end-user documentation included with the redistribution, if any, must
  *    include the following acknowledgment:
  *
  *       "This product includes software developed by Gargoyle Software Inc.
  *        (http://www.GargoyleSoftware.com/)."
  *
  *    Alternately, this acknowledgment may appear in the software itself, if
  *    and wherever such third-party acknowledgments normally appear.
  * 4. The name "Gargoyle Software" must not be used to endorse or promote
  *    products derived from this software without prior written permission.
  *    For written permission, please contact info@GargoyleSoftware.com.
  * 5. Products derived from this software may not be called "HtmlUnit", nor may
  *    "HtmlUnit" appear in their name, without prior written permission of
  *    Gargoyle Software Inc.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GARGOYLE
  * SOFTWARE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.gargoylesoftware.htmlunit.javascript.regexp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.JavaScriptException;
 import org.mozilla.javascript.ScriptableObject;
 
 import com.gargoylesoftware.htmlunit.WebTestCase;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.javascript.host.Window;
 
 /**
  * Tests for {@link HtmlUnitRegExpProxy}.
  *
  * @version $Revision$
  * @author Marc Guillemot
  * @author Ahmed Ashour
  */
 public class HtmlUnitRegExpProxyTest extends WebTestCase {
 
     private final String str_ = "(?:<script.*?>)((\\n|\\r|.)*?)(?:<\\/script>)";
     private final String begin_ = "<div>bla</div>";
     private final String end_ = "foo\n<span>bla2</span>";
     private final String text_ = begin_
         + "<script>var a = 123;</script>"
         + end_;
     private final String expected_ = begin_ + end_;
     private final String src_ = "var re = new RegExp(str, 'img');\n"
         + "var s = text.replace(re, '');\n"
         + "if (s != expected)\n"
         + " throw 'Expected >' + expected + '< but got >' + s + '<';";
 
     private final String scriptTestMatch_ = "function arrayToString(_arr)\n"
         + "{\n"
         + "  if (_arr == null) return null;\n"
         + "  var s = '[';\n"
         + "  for (var i=0; i<_arr.length; ++i)\n"
         + "  {\n"
         + "    if (i != 0) s += ', '\n"
         + "    s += _arr[i];\n"
         + "  }\n"
         + "  s += ']';\n"
         + "  return s;\n"
         + "}\n"
         + "function assertArrEquals(actual, expected)\n"
         + "{\n"
         + "  if (expected == null)\n"
         + "  {\n"
         + "    if (actual != null)\n"
         + "      throw 'Expected >null< got >' + actual + '<';\n"
         + "    else return;\n"
         + "  }\n"
         + "  var expectedStr = arrayToString(expected);\n"
         + "  var actualStr = arrayToString(actual);\n"
         + "  if (expectedStr != actualStr)\n"
         + "    throw 'Expected >' + expectedStr + '< got >' + actualStr + '<';\n"
         + "}\n"
         + "assertArrEquals('ab'.match(), null);\n"
         + "assertArrEquals('ab'.match('foo'), null);\n"
         + "assertArrEquals('ab'.match('a'), ['a']);\n"
         + "assertArrEquals('abab'.match('a'), ['a']);\n"
         + "assertArrEquals('abab'.match('.a'), ['ba']);\n"
         + "assertArrEquals('abab'.match(/.a/), ['ba']);\n"
         + "assertArrEquals('li'.match(/^([^a-z0-9_-])?([a-z0-9_-]+)(.*)/i), ['li', undefined, 'li', '']);\n"
         + "assertArrEquals('abab'.match(new RegExp('.a')), ['ba']);\n"
         + "var s = '<script>var a = 1;</' + 'script>';\n"
         + "var re = '(?:<script.*?>)((\\n|\\r|.)*?)(?:<\\/script>)';\n"
         + "assertArrEquals(s.match(re), [s, 'var a = 1;', ';']);\n";
 
     /**
      * Create an instance
      * @param name The name of the test
      */
     public HtmlUnitRegExpProxyTest(final String name) {
         super(name);
     }
 
     /**
      * Test that string.replace works correctly (?) in htmlunit
      * @throws Exception if the test fails
      */
     public void testFixedInHtmlUnit() throws Exception {
         final String html = "<html></html>";
         final HtmlPage page = loadPage(html);
         final Window topScope = ((Window) page.getEnclosingWindow().getScriptObject());
         topScope.put("str", topScope, str_);
         topScope.put("text", topScope, text_);
         topScope.put("expected", topScope, expected_);
         page.executeJavaScript(src_);
     }
 
     /**
      * Test if custom patch is still needed
      */
     public void testNeedCustomFix() {
         final Context ctx = Context.enter();
         final ScriptableObject topScope = ctx.initStandardObjects();
         
         topScope.put("str", topScope, str_);
         topScope.put("text", topScope, text_);
         topScope.put("expected", topScope, expected_);
         
         assertEquals(begin_ + end_, text_.replaceAll(str_, ""));
         try {
             ctx.evaluateString(topScope, src_, "test script", 0, null);
         }
         catch (final JavaScriptException e) {
             assertTrue(e.getMessage().indexOf("Expected >") == 0);
         }
     }
 
     /**
      * Test for bug
      * http://sourceforge.net/tracker/index.php?func=detail&aid=1780089&group_id=47038&atid=448266.
      * @throws Exception if the test fails
      */
     public void testReplaceNormalStringWithRegexpChars() throws Exception {
         final String content = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    alert('123456'.replace('/{\\d+}', ''));\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
 
         final String[] expectedAlerts = {"123456"};
         final List collectedAlerts = new ArrayList();
         createTestPageForRealBrowserIfNeeded(content, expectedAlerts);
         loadPage(content, collectedAlerts);
         assertEquals(expectedAlerts, collectedAlerts);
     }
 
     /**
      * @throws Exception if the test fails
      */
     public void testReplaceWithUndefinedPattern() throws Exception {
         final String content = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    var pattern;\n"
             + "    alert('123456'.replace(pattern, ''));\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
         
         final String[] expectedAlerts = {"123456"};
         final List collectedAlerts = new ArrayList();
         createTestPageForRealBrowserIfNeeded(content, expectedAlerts);
         loadPage(content, collectedAlerts);
         assertEquals(expectedAlerts, collectedAlerts);
     }
 
     /**
      * @throws Exception if the test fails
      */
     public void testReplace() throws Exception {
         final String content = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    var pattern = /{\\d+}/g;\n"
             + "    alert('123456'.replace(pattern, ''));\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
 
         final String[] expectedAlerts = {"123456"};
         final List collectedAlerts = new ArrayList();
         createTestPageForRealBrowserIfNeeded(content, expectedAlerts);
         loadPage(content, collectedAlerts);
         assertEquals(expectedAlerts, collectedAlerts);
     }
 
     /**
      * @throws Exception if the test fails
      */
     public void testMatch() throws Exception {
         final String content = "<html><head><title>foo</title><script>\n"
             + scriptTestMatch_
             + "</script></head><body>\n"
             + "</body></html>";
 
         final String[] expectedAlerts = {};
         final List collectedAlerts = new ArrayList();
         createTestPageForRealBrowserIfNeeded(content, expectedAlerts);
         loadPage(content, collectedAlerts);
         assertEquals(expectedAlerts, collectedAlerts);
     }
     
     /**
      * Test if the custom fix is needed or not. When this test fails, then it means that the problem is solved in
      * Rhino and that custom fix for String.match in {@link HtmlUnitRegExpProxy} is not needed anymore (and that
      * this test can be removed, or turned positive).
      * @throws Exception if the test fails
      */
     public void testMatchFixNeeded() throws Exception {
         final Context ctx = Context.enter();
         final ScriptableObject topScope = ctx.initStandardObjects();
         
         ctx.evaluateString(topScope, scriptTestMatch_, "test script String.match", 0, null);
         try {
             ctx.evaluateString(topScope, scriptTestMatch_, "test script String.match", 0, null);
         }
         catch (final JavaScriptException e) {
             assertTrue(e.getMessage().indexOf("Expected >") == 0);
         }
     }
 
     /**
      * @throws Exception if the test fails
      */
     public void testIndex() throws Exception {
         final String content = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    var match = '#{tests} tests'.match(/(^|.|\\r|\\n)(#\\{(.*?)\\})/);\n"
             + "    alert(match.index);\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
         
         final String[] expectedAlerts = {"0"};
         final List collectedAlerts = new ArrayList();
         createTestPageForRealBrowserIfNeeded(content, expectedAlerts);
         loadPage(content, collectedAlerts);
         assertEquals(expectedAlerts, collectedAlerts);
     }
 
 }

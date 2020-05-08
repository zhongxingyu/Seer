 /**
  * 
  */
 package org.ubimix.commons.parser.css;
 
 import junit.framework.TestCase;
 
 import org.ubimix.commons.parser.CharStream;
 import org.ubimix.commons.parser.ICharStream;
 
 /**
  * @author kotelnikov
  */
 public class CssSelectorParserTest extends TestCase {
 
     /**
      * @param name
      */
     public CssSelectorParserTest(String name) {
         super(name);
     }
 
     public void test() {
        test("ns|tag.toto", "ns|tag[class~=toto]");
        test("a.toto", "a[class~=toto]");
        test("::toto", "[:element~=toto]");
         test("[ns1|attr=value]", "[ns1|attr=value]");
 
         test("", "");
        test("a.toto", "a[class~=toto]");
        test("a.toto#tata", "a[class~=toto id=tata]");
        test(".titi#abc", "[class~=titi id=abc]");
        test(".titi[a=b]#abc", "[class~=titi a=b id=abc]");
        test(".titi[ a = b]#abc", "[class~=titi a=b id=abc]");
        test(".titi[ a = b ]#abc", "[class~=titi a=b id=abc]");
         test(
             ".titi[  title  *=  'hello'  ]#abc",
            "[class~=titi title*='hello' id=abc]");
 
         test(
             "div.umx-block .title > a",
            "div[class~=umx-block] [class~=title] > a[]");
         test(
             "div.umx-block .title > a:hover",
            "div[class~=umx-block] [class~=title] > a[:class~=hover]");
 
         test("h1, h2, h3, h4, h5", "h1[], h2[], h3[], h4[], h5[]");
     }
 
     private void test(String str, String control) {
         ICharStream stream = new CharStream(str);
         ICssSelectorParser parser = new CssSelectorParser();
         final StringBuilder buf = new StringBuilder();
         ICssSelectorListener listener = new CssSelectorListener() {
 
             int fPos;
 
             @Override
             public void beginAttributeGroup() {
             }
 
             @Override
             public void beginElement(String elementMask) {
                 if (elementMask != null && !"".equals(elementMask)) {
                     buf.append(elementMask);
                 }
                 buf.append("[");
                 fPos = 0;
             }
 
             @Override
             public void endAttributeGroup() {
             }
 
             @Override
             public void endElement(String elementMask) {
                 buf.append("]");
             }
 
             @Override
             public void onAttribute(
                 String attributeName,
                 String matchType,
                 String matchValue) {
                 if (fPos > 0) {
                     buf.append(" ");
                 }
                 buf.append(attributeName);
                 if (!"".equals(matchType)) {
                     buf.append(matchType);
                     buf.append(matchValue);
                 }
                 fPos++;
             }
 
             @Override
             public void onElementCombinator(char combinator) {
                 buf.append(combinator);
             }
         };
         parser.parse(stream, listener);
         assertEquals(control, buf.toString());
     }
 
 }

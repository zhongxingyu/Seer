 package org.exigencecorp.selenify;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class SelenifyParserTest extends TestCase {
 
     public void testCommandOnly() {
         this.assertParsed("goBackAndWait", "goBackAndWait", "", "");
     }
 
     public void testCommandWithOnlyValue() {
        this.assertParsed("assertTextNotPresent bob", "assertTextNotPresent", "", "bob");
     }
 
     public void testCommandBothArgsWithSpace() {
         this.assertParsed("assertText link=Foo Bar: text", "assertText", "link=Foo Bar", "text");
     }
 
     public void testCommandBothArgsWithSpaceAndEscapedColon() {
         this.assertParsed("assertText link=F\\:oo Bar: text", "assertText", "link=F:oo Bar", "text");
     }
 
     public void testHtml() {
         this.assertParsed("type description: <a>foo</a>", "type", "description", "&lt;a&gt;foo&lt;/a&gt;");
     }
 
     private void assertParsed(String line, String command, String arg1, String arg2) {
         String[] parsed = SelenifyParser.parse(line);
         Assert.assertEquals(command, parsed[0]);
         Assert.assertEquals(arg1, parsed[1]);
         Assert.assertEquals(arg2, parsed[2]);
     }
 
 }

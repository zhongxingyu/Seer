 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 
 package org.jenkinsci.plugins.darcs;
 import junit.framework.TestCase;
 import org.junit.Ignore;
 
 /**
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsSaxHandlerTest extends TestCase {
 
     public DarcsSaxHandlerTest(String name) {
         super(name);
     }
 
     public void testStripIgnoreThisFromComment() {
         assertEquals("", 
                      DarcsSaxHandler.stripIgnoreThisFromComment(""));
         assertEquals("Foo bar baz.", 
                      DarcsSaxHandler.stripIgnoreThisFromComment("Foo bar baz."));
         assertEquals("", 
                      DarcsSaxHandler.stripIgnoreThisFromComment("Ignore-this: 8ac0d466c0df3e66ea7a9935b669b86"));
         assertEquals("", 
                      DarcsSaxHandler.stripIgnoreThisFromComment("Ignore-this: 606c40ef0d257da9b7a916e7f1c594aa"));
         assertEquals("this is a test patch", 
                     DarcsSaxHandler.stripIgnoreThisFromComment("Ignore-this: fe9a0bb6ebadda018ea88c252a033ec8\nthis is a test patch"));
        assertEquals("\nthis is a test patch", 
                      DarcsSaxHandler.stripIgnoreThisFromComment("Ignore-this: fe9a0bb6ebadda018ea88c252a033ec8\n\nthis is a test patch"));
     }
     
     @Ignore("Not ready yet")
     public void testEndDocument() {
 //        DarcsSaxHandler sut = new DarcsSaxHandler();
 //        sut.endDocument();
 //        assertTrue(sut.isReady());
     }
 }

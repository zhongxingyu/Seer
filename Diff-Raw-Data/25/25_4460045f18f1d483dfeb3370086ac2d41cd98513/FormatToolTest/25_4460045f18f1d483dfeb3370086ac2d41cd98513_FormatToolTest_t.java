 package org.otherobjects.cms.tools;
 
 import java.util.Locale;
 
 import junit.framework.TestCase;
 
 import org.springframework.context.support.StaticMessageSource;
 
 public class FormatToolTest extends TestCase
 {
 
     public void testFormatTextileString()
     {
         String html = FormatTool.formatTextile("bq. A test");
         System.out.println(html);
         assertTrue(html.startsWith("<blockquote"));
 
         // Check that existing HTML is kept
         html = FormatTool.formatTextile("<i>test</i>");
         assertTrue(html.startsWith("<p><i>"));
     }
 
     public void testBigDeanAllenTest()
     {
         String testInput = "h2{color:green}. This is a title\n\nh3. This is a subhead\n\np{color:red}. This is some text of dubious character. Isn't the use of \"quotes\" just lazy writing -- and "
                 + "theft of 'intellectual property' besides? I think the time has come to see a block quote.\n\nbq[fr]. This is a block quote. I'll admit it's not the most exciting block quote ever "
                 + "devised.\n\nSimple list:\n\n#{color:blue} one\n# two\n# three\n\nMulti-level list:\n\n# one\n## aye\n## bee\n## see\n# two\n## x\n## y\n# three\n\nMixed list:\n\n* Point one\n"
                 + "* Point two\n## Step 1\n## Step 2\n## Step 3\n* Point three\n** Sub point 1\n** Sub point 2\n\n\nWell, that went well. How about we insert an <a href=\"/\" "
                 + "title=\"watch out\">old-fashioned hypertext link</a>? Will the quote marks in the tags get messed up? No!\n\n\"This is a link (optional title)\":"
                 + "http://www.textism.com\n\ntable{border:1px solid black}.\n|_. this|_. is|_. a|_. header|\n<{background:gray}. |\2. this is|{background:red;"
                 + "width:200px}. a|^<>{height:200px}. row|\n|this|<>{padding:10px}. is|^. another|(bob#bob). row|\n\nAn image:\n\n!/common/textist.gif(optional alt text)"
                 + "!\n\n# Librarians rule\n# Yes they do\n# But you knew that\n\nSome more text of dubious character. Here is a noisome string of CAPITAL letters. "
                 + "Here is something we want to _emphasize_. \nThat was a linebreak. And something to indicate *strength*. Of course I could use <em>my own HTML tags</em> "
                 + "if I <strong>felt</strong> like it.\n\nh3. Coding\n\nThis <code>is some code, \"isn't it\"</code>. Watch those quote marks! Now for some preformatted t"
                 + "ext:\n\n<pre>\n<code>\n  $text = str_replace(\"<p>%::%</p>\",\"\",$text);\n  $text = str_replace(\"%::%</p>\",\"\",$text);\n $text = str_replace"
                 + "(\"%::%\",\"\",$text);\n\n</code>\n</pre>\n\nThis isn't code.\n\n\nSo you see, my friends:\n\n* The time is now\n* The time is not later\n* "
                 + "The time is not yesterday\n* We must act\n";
         String html = FormatTool.formatTextile(testInput);
         System.out.print(html);
     }
 
     public void testParseMessage()
     {
         StaticMessageSource messageSource = new StaticMessageSource();
         messageSource.addMessage("message.code", Locale.ENGLISH, "My message");
         FormatTool formatTool = new FormatTool(messageSource);
         
         assertEquals("not a message",formatTool.getMessage("not a message"));
         assertEquals("My message",formatTool.getMessage("message.code"));
         assertEquals("My message",formatTool.getMessage("${message.code}"));
         
     }
 }

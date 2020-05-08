 package ca.mcgill.music.ddmal.meisearch;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import ca.mcgill.music.ddmal.mei.MeiDocument;
 import ca.mcgill.music.ddmal.mei.MeiElement;
 
 public class NaiveDocSearcherTest {
 
     @Test
     public void endOfString() {
         MeiDocument d = new MeiDocument();
         MeiElement e = new MeiElement("mei");
         MeiElement a = new MeiElement("note");
         a.addAttribute("pname", "a");
         MeiElement a1 = new MeiElement("note");
         a1.addAttribute("pname", "a");
         MeiElement b = new MeiElement("note");
         b.addAttribute("pname", "b");
         MeiElement c = new MeiElement("note");
         c.addAttribute("pname", "c");
         e.addChild(a);
         e.addChild(a1);
         e.addChild(b);
         e.addChild(c);
         d.setRootElement(e);
        d.setFilename("filename");
 
         NaiveDocSearcher sr = new NaiveDocSearcher();
         List<Response> res = sr.find(d, "abc");
 
         assertThat(res.size(), is(1));
 
     }
 
 }

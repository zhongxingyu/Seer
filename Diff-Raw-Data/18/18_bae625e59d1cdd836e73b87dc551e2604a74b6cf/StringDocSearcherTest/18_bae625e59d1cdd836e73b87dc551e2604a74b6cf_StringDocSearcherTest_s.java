 package ca.mcgill.music.ddmal.meisearch;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.*;
 
 import java.net.URL;
 import java.util.List;
 
 import org.junit.Test;
 
 import ca.mcgill.music.ddmal.mei.MeiDocument;
 import ca.mcgill.music.ddmal.mei.MeiElement;
 import ca.mcgill.music.ddmal.mei.MeiXmlReader;
 
 public class StringDocSearcherTest {
 
     @Test
     public void test() {
         URL url = getClass().getResource("/400.mei");
         MeiDocument doc = MeiXmlReader.loadFile(url.getFile());
         StringDocSearcher sr = new StringDocSearcher();
         sr.find(doc, "abc");
 
         assertThat(doc.getRootElement().getName(), is("mei"));
     }
 
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
 
         StringDocSearcher sr = new StringDocSearcher();
         List<Response> res = sr.find(d, "abc");
 
         assertThat(res.size(), is(1));
     }
 
 }

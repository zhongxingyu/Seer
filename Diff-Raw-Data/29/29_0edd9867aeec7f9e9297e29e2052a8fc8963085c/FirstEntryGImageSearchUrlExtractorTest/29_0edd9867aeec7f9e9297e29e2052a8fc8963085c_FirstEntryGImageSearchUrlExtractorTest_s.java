 package de.behrfriedapp.webshop.server.web;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author marcus
  */
 public class FirstEntryGImageSearchUrlExtractorTest {
 
 	private ImageSearchUrlExtractor imageSearchUrlExtractor;
 
 	@Before
 	public void setUp() throws Exception {
 		this.imageSearchUrlExtractor = new BingImageSearchUrlExtractor();
 	}
 
 	@Test
 	public void testExtractUrl() throws Exception {
 		final String src = "<div class=\"mmsp\"></div><div id=\"vm_c\"><script type=\"text/javascript\">_G.KPT=new " +
 						   "Date()</script><div style=\"\"><div id=\"dg_c\" mu=\"1000\" cw=\"1585\" ch=\"788\" " +
 						   "aria-label=\"Bildergebnisse\" style=\"height:1068px;width:1549px;\"><div class=\"dg_b\" " +
						   "dgw=\"1549\" dgh=\"1068\" beg=\"0\" end=\"69\" noac=\"3\" dgst=\"ro_u1074*\"><div class=\"border imgres\"><div class=\"dg_u\" style=\"width:165px;height:144px;left:25px;top:0px\"><a href=\"?FORM=IDFRIR#\" ihk=\"H.4538544616441176\" m=\"{ns:&quot;API.images&quot;,k:&quot;6&quot;,mid:&quot;630A96AFF3464CD912F4C2C7954EACF6A860A3BA&quot;,surl:&quot;http://www.badische-zeitung.de/gesundheit-ernaehrung/dem-staub-auf-der-spur--20462121.html&quot;,imgurl:&quot;http://ais.badische-zeitung.de/piece/01/38/3a/20/20462112.png&quot;,oh:&quot;256&quot;,tft:&quot;24&quot;,oi:&quot;http://ais.badische-zeitung.de/piece/01/38/3a/20/20462112.jpg&quot;}\" mid=\"630A96AFF3464CD912F4C2C7954EACF6A860A3BA\" onclick=\"return false;\" t1=\"Gesundheit &amp; Ernährung: Dem Staub auf der Spur - badische-zeitung.de\" t2=\"1024 x 875 · 104 kB · jpeg\" t3=\"www.badische-zeitung.de/gesundheit-ernaehrung/dem-staub-auf-der...\" h=\"ID=API.images,6.1\"><img src=\"http://ts1.mm.bing.net/th?id=H.4538544616441176&amp;pid=1.7&amp;w=165&amp;h=144&amp;c=7&amp;rs=1\" style=\"height:144px;\"/><img title=\"Weitere Größen\" class=\"ovr ovrIconMS\"";
 
		final List<String> expected = Arrays.asList("http://ais.badische-zeitung.de/piece/01/38/3a/20/20462112.png");
 		final List<String> actual = this.imageSearchUrlExtractor.extractUrls(src);
 
 		Assert.assertEquals(expected, actual);
 	}
 }

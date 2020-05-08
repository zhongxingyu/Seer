 package com.xiuhao.commons.lang;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.xiuhao.commons.lang.HtmlKit;
 
 public class HtmlKitTest {
 
 	@Test
 	public void testAddHtmlATag() {
 		Assert.assertEquals("hi,<a href=\"http://www.xiuhao.com/item/38\">http://www.xiuhao.com/item/38</a> 这个普8片三个戳的", //
 				HtmlKit.addHtmlATag("hi,http://www.xiuhao.com/item/38 这个普8片三个戳的"));
 
 		Assert.assertEquals("<a href=\"https://www.xiuhao.com/item/38\">https://www.xiuhao.com/item/38</a>", //
 				HtmlKit.addHtmlATag("https://www.xiuhao.com/item/38"));
 
 		Assert.assertEquals("abc <a href=\"https://\">https://</a>", HtmlKit.addHtmlATag("abc https://"));
 	}
 
 	@Test
 	public void testAddHtmlATagInvalid() {
 		Assert.assertEquals("http", HtmlKit.addHtmlATag("http"));
 
 		Assert.assertEquals("abc www.xiuhao.com abc", HtmlKit.addHtmlATag("abc www.xiuhao.com abc"));
 
 		Assert.assertEquals("http//xxx", HtmlKit.addHtmlATag("http//xxx"));
 	}
 
 	@Test
 	public void testGetImageSrcs() {
 		Assert.assertEquals("static/image/smiley/default/smile.gif", //
 				HtmlKit.getImageSrcs("<img src=\"static/image/smiley/default/smile.gif\" smilieid=\"1\" border=\"0\" alt=\"\">").get(0));
 	}
 
 	@Test
 	public void testGetImageSrcs2() {
 		Assert.assertEquals(null, //
 				HtmlKit.getImageSrcs("hello image>"));
 		Assert.assertEquals(null, //
 				HtmlKit.getImageSrcs("<image src"));
 	}
 
 	@Test
 	public void testGetImageSrcs3() {
 		List<String> imageSrcs = HtmlKit.getImageSrcs("hello<img src=\"static/image/smiley/default/smile.gif\" smilieid=\"1\" border=\"0\" alt=\"\">" + //
 				"<img src=\"images/smilies/default/shy.gif\" smilieid=\"8\" border=\"0\">");
 		Assert.assertEquals("static/image/smiley/default/smile.gif", imageSrcs.get(0));
 		Assert.assertEquals("images/smilies/default/shy.gif", imageSrcs.get(1));
 	}
 
 }

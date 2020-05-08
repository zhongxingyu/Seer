 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.it.render;
 
 import java.net.URL;
 
 import junit.framework.Test;
 import junitx.framework.StringAssert;
 
 import org.custommonkey.xmlunit.Diff;
 import org.seasar.teeda.it.AbstractTestCase;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlImage;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * @author manhole
  */
 public class GraphicImageTest extends AbstractTestCase {
 
     public static Test suite() throws Exception {
         return setUpTestSuite(GraphicImageTest.class);
     }
 
     public void testRender() throws Exception {
         // ## Arrange ##
         URL url = getUrl("faces/render/graphicImage.jsp");
         System.out.println(url);
 
         WebClient webClient = new WebClient();
 
         // ## Act ##
         HtmlPage page = getHtmlPage(webClient, url);
 
         // ## Assert ##
         final String body1 = getBody(page).trim();
         System.out.println(body1);
         assertEquals("graphicImage.jsp", page.getTitleText());
 
         HtmlImage image = (HtmlImage) page.getHtmlElementById("imageA");
         StringAssert.assertStartsWith("/image/item_large_b.gif", image
                 .getSrcAttribute());
         assertEquals("image title", image.getTitleAttribute());
 
         final String expected = readText("testRender.html");
         Diff diff = diff(expected, body1);
         assertEquals(diff.toString(), true, diff.similar());
     }
 
    public void testRender_borderAttribute() throws Exception {
        // ## Arrange ##
        URL url = getUrl("faces/render/graphicImageBorderAttribute.jsp");
        System.out.println(url);

        WebClient webClient = new WebClient();

        // ## Act ##
        HtmlPage page = getHtmlPage(webClient, url);

        // ## Assert ##
        final String body1 = getBody(page).trim();
        System.out.println(body1);

        final String expected = readText("testRenderBorderAttribute.html");
        Diff diff = diff(expected, body1);
        assertEquals(diff.toString(), true, diff.similar());
    }

 }

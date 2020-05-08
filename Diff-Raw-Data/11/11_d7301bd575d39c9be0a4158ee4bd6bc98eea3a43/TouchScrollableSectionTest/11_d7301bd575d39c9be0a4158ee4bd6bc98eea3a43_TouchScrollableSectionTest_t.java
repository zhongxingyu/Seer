 /* Copyright (c) 2010 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz.section;
 
 import org.fiz.*;
 import org.fiz.section.TouchScrollableSection;
 import org.fiz.section.TemplateSection;
 import org.fiz.test.*;
 
 /**
  * Junit tests for the TouchScrollableSection class.
  */
 
 public class TouchScrollableSectionTest extends junit.framework.TestCase {
     protected ClientRequest cr;
 
     public void setUp() {
         cr = new ClientRequestFixture();
     }
     
     public void test_render_includeDeviceJs() {
         cr.getMainDataset().set("device", "IPhone");
         TouchScrollableSection scroller = new TouchScrollableSection(
                 new Dataset());
         cr.showSections(scroller);
         TestUtil.assertSubstring("JS files requested",
                 "IPhoneTouchScrollableSection.js",
                 cr.getHtml().getJsFiles());
     }
 
     public void test_render_includeDeviceCss() {
         cr.getMainDataset().set("device", "IPhone");
         TouchScrollableSection scroller = new TouchScrollableSection(
                 new Dataset());
         cr.showSections(scroller);
         TestUtil.assertSubstring("CSS files requested",
                 "IPhoneTouchScrollableSection.css",
                 cr.getHtml().getCssFiles());
     }
 
     public void test_render_dontIncludeCss() {
        cr.getMainDataset().set("device", "IPhone");
         TouchScrollableSection scroller = new TouchScrollableSection(
                    new Dataset("class", "special"));
         cr.showSections(scroller);
         TestUtil.assertSubstring("CSS files requested",
                 "", cr.getHtml().getCssFiles());
     }
 
     public void test_render_accumulatedJs() {
         cr.getMainDataset().set("device", "IPhone");
         TouchScrollableSection scroller = new TouchScrollableSection(
                 new Dataset());
         cr.showSections(scroller);
         TestUtil.assertSubstring("Accumulated Js",
                 "new Fiz.IPhoneTouchScrollableSection(" +
                 "\"touchScrollableSectionContent0Container\", " +
                 "\"touchScrollableSectionContent0\")",
                 cr.getHtml().getJs());
     }
     
     public void test_render_accumulatedJsCustomId() {
         cr.getMainDataset().set("device", "IPhone");
         TouchScrollableSection scroller = new TouchScrollableSection(
                 new Dataset("id", "testId"));
         cr.showSections(scroller);
         TestUtil.assertSubstring("Accumulated Js",
                 "new Fiz.IPhoneTouchScrollableSection(\"testIdContainer\", " +
                     "\"testId\")",
                 cr.getHtml().getJs());
     }
 
     public void test_render_basics() {
        cr.getMainDataset().set("device", "IPhone");
         TouchScrollableSection scroller = new TouchScrollableSection(
                    new Dataset(),
                    new TemplateSection ("<p>Here is some sample text " +
                        "to fill the body of this section.</p>\n"),
                    new TemplateSection ("<p>This is a second " + 
                        "paragraph, which follows the first.</p>\n"));
         cr.showSections(scroller);
         assertEquals("generated HTML",
                 "\n<!-- Start TouchScrollableSection -->\n" + 
                 "<div id=\"touchScrollableSectionContent0Container\" " +
                     "class=\"touchScrollableSectionContainer\">\n" + 
                 "  <div id=\"touchScrollableSectionContent0\" " +
                     "class=\"touchScrollableSection\">\n" +
                 "<p>Here is some sample text to fill the body of this " + 
                     "section.</p>\n" + 
                 "<p>This is a second paragraph, which follows the first.</p>\n" +
                 "  </div>\n" +
                 "</div>\n" +
                 "<!-- End TouchScrollableSection -->\n",
                 cr.getHtml().getBody().toString());
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
 }

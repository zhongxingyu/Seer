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
 import org.fiz.section.PhoneToolbarSection;
 import org.fiz.test.*;
 
 /**
  * Junit tests for the PhoneToolbarSection class.
  */
 
 public class PhoneToolbarSectionTest extends junit.framework.TestCase {
     protected ClientRequest cr;
 
     protected Dataset data4 = new Dataset(
             "record", new Dataset("label", "BM", "image", "Bookmarks",
                     "ajaxUrl", "newMethod1"),
             "record", new Dataset("label", "Like", "image", "Favorite",
                     "url", "http://www.google.com/"),
             "record", new Dataset("label", "Chat", "image", "Chat",
                     "javascript", "alert(\"Hello World\");"),
             "record", new Dataset("label", "Add", "image", "Plus",
                     "url", "newMethod4"));
 
     public void setUp() {
         cr = new ClientRequestFixture();
     }
     
     public void test_constructor_noData() {
         boolean gotException = false;
         try {
             new PhoneToolbarSection(new Dataset());
         }
         catch (org.fiz.InternalError e) {
             assertEquals("exception message",
                     "PhoneToolbarSection constructor " +
                     " invoked without a \"buttons\" property",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_render_includeDeviceCss() {
         cr.getMainDataset().set("device", "IPhone");
         PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("CSS files requested",
                 "IPhonePhoneToolbarSection.css",
                 cr.getHtml().getCssFiles());
     }
     
     public void test_render_includeDeviceJs() {
         cr.getMainDataset().set("device", "IPhone");
         PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("JS files requested",
                 "IPhonePhoneToolbarSection.js",
                 cr.getHtml().getJsFiles());
     }
     
     public void test_render_includeCss() {
         cr.getMainDataset().set("device", "IPhone");
         PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("CSS files requested",
                 "IPhonePhoneToolbarSection.css",
                 cr.getHtml().getCssFiles());
     }
 
     public void test_render_includeJs() {
         cr.getMainDataset().set("device", "IPhone");
         PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("JS files requested",
                 "IPhonePhoneToolbarSection.js",
                 cr.getHtml().getJsFiles());
     }
 
     public void test_render_accumulatedJs() {
         cr.getMainDataset().set("device", "IPhone");
         PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("Accumulated Js",
                 "new Fiz.IPhonePhoneToolbarSection(\"footer\")",
                 cr.getHtml().getJs());
     }
     
     public void test_render_accumulatedJsCustomId() {
         cr.getMainDataset().set("device", "IPhone");
         PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4, "id", "myToolbar"));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("Accumulated Js",
                 "new Fiz.IPhonePhoneToolbarSection(\"myToolbar\")",
                 cr.getHtml().getJs());
     }
     
     public void test_render_dontIncludeCss() {
     	PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4, "class", "special"));
         cr.showSections(toolbar);
         TestUtil.assertSubstring("CSS files requested",
                 "", cr.getHtml().getCssFiles());
     }
 
     public void test_render_basics() {
     	PhoneToolbarSection toolbar = new PhoneToolbarSection(
                 new Dataset("buttons", data4));
         cr.showSections(toolbar);
         assertEquals("generated HTML",
                 "\n<!-- Start PhoneToolbarSection -->\n" + 
                 "<div id=\"footer\" class=\"toolbarSection\">\n" +
                 "   <table><tr>\n" + 
                 
                 "  <td id=\"tdBookmarksBM\"><a href=\"#\" " +
                 "onclick=\"void new Fiz.Ajax({url: &quot;newMethod1&quot;});" +
                 " return false;\">" +
                 "<div class=\"toolbarButton\" " + 
                     "id=\"BM-toolbarButton-Bookmarks\">" +
                 "<div><img id=\"BookmarksBM\" alt=\"BM\" " +
                     "src=\"/static/fiz/images/Bookmarks.png\"/>" + 
                 "</div><span>BM</span></div></a></td>\n" +
                 
                 "  <td id=\"tdFavoriteLike\"><a href=\"http://www.google.com/\">" +
                 "<div class=\"toolbarButton\" " + 
                     "id=\"Like-toolbarButton-Favorite\">" +
                 "<div><img id=\"FavoriteLike\" alt=\"Like\" " +
                     "src=\"/static/fiz/images/Favorite.png\"/>" + 
                 "</div><span>Like</span></div></a></td>\n" +
                     
                 "  <td id=\"tdChatChat\"><a href=\"#\" " +
                 "onclick=\"alert(&quot;Hello World&quot;); return false;\">" +
                 "<div class=\"toolbarButton\" " + 
                     "id=\"Chat-toolbarButton-Chat\">" +
                 "<div><img id=\"ChatChat\" alt=\"Chat\" " +
                     "src=\"/static/fiz/images/Chat.png\"/>" + 
                 "</div><span>Chat</span></div></a></td>\n" +
                 
                 "  <td id=\"tdPlusAdd\"><a href=\"newMethod4\">" +
                 "<div class=\"toolbarButton\" " + 
                     "id=\"Add-toolbarButton-Plus\">" +
                 "<div><img id=\"PlusAdd\" alt=\"Add\" " +
                     "src=\"/static/fiz/images/Plus.png\"/>" + 
                 "</div><span>Add</span></div></a></td>\n" +
                 
                 "   </tr></table>\n" +
                 "</div>\n" + 
                 "<!-- End PhoneToolbarSection -->\n",
                 cr.getHtml().getBody().toString());
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
 }

 package no.get.cms.plugin.resourcecompressor.content;
 
 import no.get.cms.plugin.resourcecompressor.ContentLoader;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 public class JsCompressedContentTest {
     @Test
     public void testGetFilename() throws Exception {
         String content = "<link/>";
         JsCompressedContent compressedContent = new JsCompressedContent("", "", content);
         assertEquals(DigestUtils.md5Hex(content) + ".js", compressedContent.getFilename());
     }
 
     @Test
     public void expected_replaced_content() {
         JsCompressedContent compressedContent = new JsCompressedContent("http://www.get.no", "/_compressed/", "");
         String expectedReplacement = "<!-- get.compressor js -->" +
                 "<script src=\"http://www.get.no/_compressed/d41d8cd98f00b204e9800998ecf8427e.js\" type=\"text/javascript\"></script>" +
                 "<!-- get.compressor end -->";
         assertEquals(expectedReplacement, compressedContent.getContentReplacement());
     }
 
     @Test
     public void different_content_gives_different_filename() {
         JsCompressedContent compressedContent1 = new JsCompressedContent("", "", "a");
         JsCompressedContent compressedContent2 = new JsCompressedContent("", "", "b");
         assertThat(compressedContent1.getFilename(), not(equalTo(compressedContent2.getFilename())));
     }
 
     @Test
     public void different_content_gives_same_filename() {
         JsCompressedContent compressedContent1 = new JsCompressedContent("", "", "a");
         JsCompressedContent compressedContent2 = new JsCompressedContent("", "", "a");
         assertThat(compressedContent1.getFilename(), equalTo(compressedContent2.getFilename()));
     }
 
     @Test
     public void returns_expected_content_type() {
         CompressedContent compressedContent = new JsCompressedContent("", "", "");
         assertEquals("application/javascript;charset=UTF-8", compressedContent.getContentType());
     }
 
     @Test
     public void loads_expected_url() throws InterruptedException {
         final String hrefUrl = "http://www.get.no";
         final String hrefUrl2 = "http://www.get2.no/_public/test.js";
 
         final JsCompressedContent compressedContent = new JsCompressedContent("", "",
                 String.format("<script src='%s'></script>" +
                         "<script src='%s'></script>", hrefUrl, hrefUrl2));
         compressedContent.setContentLoader(new ContentLoader() {
             @Override
             public String load(String url) {
                 return url;
             }
         });
 
         assertEquals(hrefUrl + "\n" + hrefUrl2, compressedContent.getCompressedContent());
     }
 
     @Test
     public void loads_expected_url_when_script_tag_has_many_attributes() throws InterruptedException {
         final String hrefUrl = "http://www.get.no";
 
         final JsCompressedContent compressedContent = new JsCompressedContent("", "",
                 String.format("<script src=\"%s\" charset=\"iso-8859-1\"></script>", hrefUrl));
         compressedContent.setContentLoader(new ContentLoader() {
             @Override
             public String load(String url) {
                 return url;
             }
         });
 
         assertEquals(hrefUrl, compressedContent.getCompressedContent());
     }
 
     @Test
     public void inlines_script_content_without_src() throws InterruptedException {
         final String expectedContent = "My content";
         final String expectedContent2 = "My second content";
         final JsCompressedContent compressedContent = new JsCompressedContent("", "",
                 String.format("<script>%s</script><script>%s</script>", expectedContent, expectedContent2));
         assertEquals(expectedContent + "\n" + expectedContent2, compressedContent.getCompressedContent());
     }
 
     @Test
     public void supports_both_src_and_inlined_scripts_within_compress_block() throws InterruptedException {
         final String hrefUrl = "http://www.get.no";
         final String inlined = "var test = \"test\";";
 
         final JsCompressedContent compressedContent = new JsCompressedContent("", "",
                 String.format("<script src='%s'></script>" +
                        "<script>%s</script>", hrefUrl, inlined));
         compressedContent.setContentLoader(new ContentLoader() {
             @Override
             public String load(String url) {
                 return url;
             }
         });
 
        assertEquals(hrefUrl + "\n" + inlined, compressedContent.getCompressedContent());
     }
 
     @Test(timeout = 2000)
     public void load_is_synchronized() throws InterruptedException {
         final Object lock = new Object();
         class TestContentLoader extends ContentLoader {
 
             public int loadInvocations = 0;
 
             @Override
             public String load(String url) {
                 synchronized (lock) {
                     try {
                         loadInvocations += 1;
                         lock.wait();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
                 return "";
             }
         }
 
         final JsCompressedContent compressedContent = new JsCompressedContent("", "", "<script src='http://www.get.no'></script>");
         final TestContentLoader contentLoader = new TestContentLoader();
         compressedContent.setContentLoader(contentLoader);
 
         final Runnable runnable = new Runnable() {
             @Override
             public void run() {
                 compressedContent.getCompressedContent();
             }
         };
 
         final Thread thread1 = new Thread(runnable);
         final Thread thread2 = new Thread(runnable);
 
         thread1.start();
 
         while (!thread1.getState().equals(Thread.State.WAITING)) {
             Thread.sleep(5);
         }
 
         thread2.start();
 
         while (!thread2.getState().equals(Thread.State.BLOCKED)) {
             Thread.sleep(5);
         }
 
         synchronized (lock) {
             lock.notify();
         }
 
         thread1.join();
         thread2.join();
 
         assertEquals(1, contentLoader.loadInvocations);
     }
 
 
 }

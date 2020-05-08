 package no.bouvet.kpro.renderer.lyric;
 
 import org.junit.Test;
 import org.apache.log4j.Logger;
 import no.bouvet.kpro.model.stigstest.TopicMapEvent;
 import no.bouvet.kpro.renderer.Renderer;
 
 
 public class LyricRendererGUITest {
     @Test
     public void testRendering() throws InterruptedException {
         final LyricGUI lyricGUI = new LyricGUI();
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 lyricGUI.createAndShowGUI();
             }
         });
 
         String[] texts = new String[]{"Hello", "World", "And", "Something"};
 
         for (final String text : texts) {
             javax.swing.SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     lyricGUI.fire(text);
                 }
             });
             Thread.sleep(500L);
         }
     }
 
     @Test
     public void testRenderingText() throws Exception {
         final LyricGUI lyricGUI = new LyricGUI();
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 lyricGUI.createAndShowGUI();
             }
         });
 
         LyricTopicMapInstructions instructions = new LyricTopicMapInstructions(new TopicMapEvent("https://wiki.bouvet.no/snap_vs_corona"));
 
         Renderer renderer = new Renderer(instructions);
         renderer.addRenderer(new LyricRenderer(lyricGUI));
 
         renderer.start(0);
 
         while (renderer.isRendering()) {
             try {
                 Thread.sleep(2000);
             } catch (InterruptedException e) {
             }
         }
         renderer.stop();
     }
 }

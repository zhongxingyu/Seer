 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package removeImages;
 
 import java.util.ArrayList;
 import org.junit.Test;
 import removeImage.SelectedThumbnailLister;
 import removeImage.Thumbnail;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Johan LG
  */
 public class SelectedThumbnailListerTest {
 
     @Test
     public void TestListing() {
         System.out.println("Testing listing of selected thumbnails");
         ArrayList<Thumbnail> thumbnails = new ArrayList<>();
         for (int i = 0; i < 10; i++) {
             Thumbnail tn = new Thumbnail();
            tn.loadImage(i + "" + i + "" + i);
             thumbnails.add(tn);
         }
         SelectedThumbnailLister lister = new SelectedThumbnailLister(thumbnails);
        lister.ListSelectedThumbnails();
        assertTrue(lister.getUrls().isEmpty());
         
         thumbnails.get(1).setSelected(true);
         thumbnails.get(3).setSelected(true);
         thumbnails.get(5).setSelected(true);
         thumbnails.get(1).setSelected(false);
         thumbnails.get(2).setSelected(false);
        lister.ListSelectedThumbnails();
        assertTrue(lister.getUrls().size() == 2);
        assertTrue(lister.getUrls().get(0).equals("555") || lister.getUrls().get(1).equals("555"));
 
     }
 }

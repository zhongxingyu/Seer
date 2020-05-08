 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package removeImages;
 
 import java.util.ArrayList;
import model.Picture;
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
            Picture pic = new Picture(i + "" + i + "" + i, i + "" + i + "" + i);
            tn.setPicture(pic);
             thumbnails.add(tn);
         }
         SelectedThumbnailLister lister = new SelectedThumbnailLister(thumbnails);
        ArrayList<Picture> images = lister.ListSelectedThumbnails();
        assertTrue(lister.getImages().isEmpty());
         
         thumbnails.get(1).setSelected(true);
         thumbnails.get(3).setSelected(true);
         thumbnails.get(5).setSelected(true);
         thumbnails.get(1).setSelected(false);
         thumbnails.get(2).setSelected(false);
        images = lister.ListSelectedThumbnails();
        assertTrue(lister.getImages().size() == 2);
        assertTrue(lister.getImages().get(0).getLargeUrl().equals("555") || lister.getImages().get(1).getLargeUrl().equals("555"));
 
     }
 }

package ch01.ex01_14;

 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class WalkmanTest {
   private Walkman walkman;
 
   @Before
   public void setUp() throws Exception {
     walkman = new Walkman();
   }
 
   @Test
   public void testAddMusicTest() {
     int index = walkman.addMusic("music1");
 
     assertEquals(walkman.getMusic(index), "music1");
   }
 
   @Test
   public void testRemoveMusicTest() {
     int index = walkman.addMusic("music1");
     walkman.removeMusic("music1");
 
     assertEquals(walkman.getMusic(index), null);
   }
 
   @Test
   public void testRemoveMusicTestOnPlaying() {
     int index = walkman.addMusic("music1");
     walkman.playMusic(index);
     walkman.removeMusic("music1");
 
     assertEquals(walkman.getMusic(index), "music1");
   }
 
   @Test
   public void testAddMusicFull() {
     for (int i = 0; i < Walkman.MAX_MUSIC_NUM; ++i) {
       walkman.addMusic("music");
     }
 
     assertEquals(walkman.addMusic("music"), -1);
   }
 }

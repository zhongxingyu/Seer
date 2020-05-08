 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package flipbox.sackrace.object;
 
 import flipbox.sackrace.staticvalue.StaticData;
 import flipbox.sackrace.staticvalue.TypeList;
 import flipbox.sackrace.ui.AnimatedSprite;
 import java.io.IOException;
 import javax.microedition.lcdui.Image;
 
 /**
  *
  * @author QED
  */
 public class PlayerData {
 
     Player gareng, bagong, petruk, pocong;
 
     public static Player getGareng() {
         Player gar = new Player();
         gar.setName("Gareng");
         gar.setBloodLevel(3);
         gar.setJumpHeight(10);
         gar.setCoinCount(0);
         Image sprite = null;
         try {
             sprite = StaticData.rotateImage(Image.createImage("/resource/chars/sprite jalan gareng.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         gar.setSprite(new AnimatedSprite(sprite, 75, 280 / 4, 2));
         gar.setNormalSprite(new AnimatedSprite(sprite, 75, 280 / 4, 2));
         Image gareng_slide = null;
         try {
             gareng_slide = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/sprite ngesot gareng.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         gar.setSlideSprite(new AnimatedSprite(gareng_slide, 70, gareng_slide.getHeight() / 4, 3));
 
         Image gareng_jump = null;
         try {
             gareng_jump = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/sprite loncat gareng.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         gar.setJumpSprite(new AnimatedSprite(gareng_jump, 103, gareng_jump.getHeight() / 10, 2));
 
         Image gareng_crashUp=null;
         try {
             gareng_crashUp = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/Sprite Obstacle Gareng.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         gar.setCrashedUpSprite(new AnimatedSprite(gareng_crashUp, 78, gareng_crashUp.getHeight() / 4, 2));
         
         return gar;
     }
 
     public static Player getPetruk() {
         Player pet = new Player();
         pet.setName("Petruk");
         pet.setBloodLevel(3);
         pet.setJumpHeight(10);
         pet.setCoinCount(0);
         Image sprite = null;
         try {
             sprite = StaticData.rotateImage(Image.createImage("/resource/chars/sprite jalan petruk.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         pet.setSprite(new AnimatedSprite(sprite, 99, 280 / 4, 2));
         pet.setNormalSprite(new AnimatedSprite(sprite, 99, 280 / 4, 2));
        Image bagong_slide = null;
         try {
            bagong_slide = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/sprite ngesot petruk.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
        pet.setSlideSprite(new AnimatedSprite(bagong_slide, 77, bagong_slide.getHeight() / 4, 3));
 
        Image bagong_jump = null;
         try {
            bagong_jump = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/sprite loncat petruk.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
        pet.setJumpSprite(new AnimatedSprite(bagong_jump, 126, bagong_jump.getHeight() / 10, 2));
 
         Image petruk_crashUp=null;
         try {
             petruk_crashUp = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/Sprite Obstacle Petruk.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
        pet.setCrashedUpSprite(new AnimatedSprite(petruk_crashUp, 78, petruk_crashUp.getHeight() / 4, 2));
         
         return pet;
     }
 
     public static Player getBagong() {
         Player bag = new Player();
         bag.setName("Bagong");
         bag.setBloodLevel(3);
         bag.setJumpHeight(10);
         bag.setCoinCount(0);
         Image sprite = null;
         try {
             sprite = StaticData.rotateImage(Image.createImage("/resource/chars/bagong_lompat.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         bag.setSprite(new AnimatedSprite(sprite, 75, 280 / 4, 2));
         bag.setNormalSprite(new AnimatedSprite(sprite, 75, 280 / 4, 2));
         Image bagong_slide = null;
         try {
             bagong_slide = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/bagong_ngesot.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         bag.setSlideSprite(new AnimatedSprite(bagong_slide, 70, bagong_slide.getHeight() / 4, 3));
 
         Image bagong_jump = null;
         try {
             bagong_jump = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/sprite loncat bagong.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         bag.setJumpSprite(new AnimatedSprite(bagong_jump, 100, bagong_jump.getHeight() / 10, 2));
         
         Image bagong_crashUp=null;
         try {
             bagong_crashUp = StaticData.rotateImage(Image.createImage(
                     "/resource/chars/Sprite Obstacle Bagong.png"), 90);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         bag.setCrashedUpSprite(new AnimatedSprite(bagong_crashUp, 74, bagong_crashUp.getHeight() / 4, 2));
         return bag;
     }
 
     public static Player getPocong() {
         Player poc = new Player();
         return poc;
     }
 }

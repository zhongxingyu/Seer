 package com.katspow.caatjagwtdemos.client.welcome.hypernumber.startmenu;
 
 import com.katspow.caatja.behavior.Interpolator;
 import com.katspow.caatja.core.canvas.CaatjaImage;
 import com.katspow.caatja.foundation.Director;
 import com.katspow.caatja.foundation.Scene;
 import com.katspow.caatja.foundation.actor.Actor;
 import com.katspow.caatja.foundation.actor.Button;
 import com.katspow.caatja.foundation.actor.ImageActor;
 import com.katspow.caatja.foundation.image.CompoundImage;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.HyperNumber;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.core.GameScene;
 
 public class GardenScene {
     
     public GameScene gameScene=      null;
     public Scene directorScene=  null;
     Director director=       null;
     CompoundImage buttonImage=    null;
 
     /**
      * Creates the main game Scene.
      * @param director a Director instance.
      * @throws Exception 
      */
     public GardenScene create (final Director director, int gardenSize) throws Exception {
         this.director= director;
         this.directorScene= director.createScene();
 
         int dw= director.canvas.getCoordinateSpaceWidth();
         int dh= director.canvas.getCoordinateSpaceHeight();
 
         Garden g =  (Garden) new Garden().
         
         setBounds(0,0,dw,dh);
         g.initialize( director.ctx, gardenSize, dh/2 );
         
         this.directorScene.addChild(
                 new ImageActor().
                         
                         setBounds(0,0,dw,dh).
                         setImage(director.getImage("background")).
                         setOffsetY( -director.getImage("background").getHeight()+dh ).
                         setClip(true)
                 );
         
         // fondo. jardin.
         this.directorScene.addChild(
                 g
                 );
 
         Button madeWith= (Button) new Button();
                 madeWith.initialize( new CompoundImage().initialize(director.getImage("madewith"),1,1), 0,0,0,0 ).
                 setLocation( dw-60, 0 );
         this.directorScene.addChild(madeWith);
 
 
         CaatjaImage buttons = director.getImage("buttons");
         this.buttonImage= new CompoundImage().initialize(
                buttons, 7,4 );
 
         double bw=         this.buttonImage.singleWidth;
         double bh=         this.buttonImage.singleHeight;
         int numButtons= 4;
         int yGap=       10;
 
         double offsetY= (dh -((bh+yGap)*numButtons - yGap))/2;
 
         // opciones del menu.
         Button easy= (Button) new Button() {
             public void fnOnClick() throws Exception {
                 //director.switchToNextScene(2000,false,true);
                 director.easeInOut(
                         director.getSceneIndex(HyperNumber.getPlayScene()),
                         Scene.Ease.TRANSLATE,
                         Actor.Anchor.RIGHT,
                         director.getSceneIndex(HyperNumber.getMenuScene()),
                         Scene.Ease.TRANSLATE,
                         Actor.Anchor.LEFT,
                         1000,
                         false,
                         new Interpolator().createExponentialInOutInterpolator(3,false),
                         new Interpolator().createExponentialInOutInterpolator(3,false) );
             }
         }.
                 
                 initialize(this.buttonImage, 0, 1, 2, 3).
                 setBounds( (dw-bw)/2, offsetY, bw, bh );
 
         Button medium= (Button) new Button() {
             @Override
             public void fnOnClick() throws Exception {
                 gameScene.setDifficulty(1);
                 gameScene.prepareSceneIn();
                 director.easeInOut(
                         director.getSceneIndex(HyperNumber.getPlayScene()),
                         Scene.Ease.TRANSLATE,
                         Actor.Anchor.TOP,
                         director.getSceneIndex(HyperNumber.getMenuScene()),
                         Scene.Ease.TRANSLATE,
                         Actor.Anchor.BOTTOM,
                         1000,
                         false,
                         new Interpolator().createExponentialInOutInterpolator(3,false),
                         new Interpolator().createExponentialInOutInterpolator(3,false) );
             }
         }.
                 
                 initialize(this.buttonImage, 4,5,6,7).
                 setBounds( (dw-bw)/2, offsetY + yGap+bh, bw, bh );
 
         Button hard= (Button) new Button() {
             @Override
             public void fnOnClick() throws Exception {
             gameScene.setDifficulty(2);
             gameScene.prepareSceneIn();
             director.easeInOut(
                     director.getSceneIndex(HyperNumber.getPlayScene()),
                     Scene.Ease.TRANSLATE,
                     Actor.Anchor.BOTTOM,
                     director.getSceneIndex(HyperNumber.getMenuScene()),
                     Scene.Ease.TRANSLATE,
                     Actor.Anchor.TOP,
                     1000,
                     false,
                     new Interpolator().createExponentialInOutInterpolator(3,false),
                     new Interpolator().createExponentialInOutInterpolator(3,false) );
             }
         }.
                 
                 initialize(this.buttonImage, 8,9,10,11).
                 setBounds( (dw-bw)/2, offsetY + 2*(yGap+bh), bw, bh );
 
         Button info= (Button) new Button().
                 
                 initialize(this.buttonImage, 16,17,18,19).
                 setBounds( (dw-bw)/2, offsetY + 3.5*(yGap+bh), bw, bh );
 
         this.directorScene.addChild(easy);
         this.directorScene.addChild(medium);
         this.directorScene.addChild(hard);
         this.directorScene.addChild(info);
 
         return this;
     }
 
 }

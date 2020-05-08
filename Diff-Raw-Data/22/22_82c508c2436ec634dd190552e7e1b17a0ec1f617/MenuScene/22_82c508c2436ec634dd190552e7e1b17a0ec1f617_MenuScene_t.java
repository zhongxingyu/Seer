 package circlesvssquares;
 
 import org.jbox2d.common.Vec2;
 
import processing.core.PConstants;

 public class MenuScene extends Scene {
 
     public MenuScene(CirclesVsSquares app_) {
         super(app_);
     }
 
     @Override
     public void draw() {
         this.app.background(255);
         super.draw();
         this.app.textSize(50);
        this.app.textAlign(PConstants.CENTER);
         this.app.text("Circle vs. Squares", this.app.width/2, this.app.height/2 - 100);
     }
 
     @Override
     public void init() {
         Button playButton = Button.createButton(new Vec2(this.app.width/2 - 50, this.app.height/2), 100, 30, new ButtonCallback() {
             @Override
             public void call() {
                 app.changeScene(new GameScene(app, 1, false));
             }
         });
         playButton.text = "Play";
         
         Button levelButton = Button.createButton(new Vec2(this.app.width/2 - 50, this.app.height/2 + 60), 100, 30, new ButtonCallback() {
             @Override
             public void call() {
                 app.changeScene(new GameScene(app, 1, true));
             }
         });
         levelButton.text = "Level Editor";
         
         Button aboutButton = Button.createButton(new Vec2(this.app.width/2 - 50, this.app.height/2 + 120), 100, 30, new ButtonCallback() {
             @Override
             public void call() {
                 app.changeScene(new AboutScene(app));
             }
         });
         aboutButton.text = "About";
     }
 }

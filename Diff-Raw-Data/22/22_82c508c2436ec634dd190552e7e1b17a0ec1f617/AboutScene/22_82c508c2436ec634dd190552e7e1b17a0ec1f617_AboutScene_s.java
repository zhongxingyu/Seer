 package circlesvssquares;
 
 import org.jbox2d.common.Vec2;
 
 public class AboutScene extends Scene {
 
     public AboutScene(CirclesVsSquares app_) {
         super(app_);
     }
 
     @Override
     public void draw() {
         this.app.background(255);
         super.draw();
         this.app.textSize(50);
        this.app.textAlign(this.app.CENTER);
         this.app.text("About", this.app.width/2, this.app.height/2 - 100);
         this.app.textSize(30);
         this.app.text("Programmers:\n Griffin Schneider \n TJ Higgins", this.app.width/2, this.app.height/2);
     }
 
     @Override
     public void init() {
         Button backButton = Button.createButton(new Vec2(30, 30), 100, 30, new ButtonCallback() {
             @Override
             public void call() {
                 app.changeScene(new MenuScene(app));
             }
         });
         backButton.text = "<- Back";
     }
 }

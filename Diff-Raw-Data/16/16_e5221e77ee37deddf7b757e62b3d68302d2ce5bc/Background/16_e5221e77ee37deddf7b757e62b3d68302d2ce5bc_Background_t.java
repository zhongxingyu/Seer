 import textures.SpriteSheet;
 import java.awt.*;
 import java.util.LinkedList;
 
 public class Background {
     private LinkedList<Star> stars;
     public Background(){
         stars = new LinkedList<Star>();
         addRandomStars(50);
     }
     public void draw(Graphics2D g){
         int size = stars.size();
         for (int i = 0; i < size; i++){
             stars.get(i).draw(g);
         }
     }
     public void update(){
         for (int i = 0; i < stars.size(); i++){
             stars.get(i).update();
         }
     }
     public void addRandomStars(int amount){
         for (int i = 0; i < amount; i++){
             stars.add(new Star());
         }
     }
 
     public class Star {
         private double x;
         private double y;
         private double speedX;
         private double speedY;
         private int starType;
         private int alphaType;
         private double opacity;
         private double maxOpacity;
         private double minOpacity;
         private double blinkRate;
         private boolean increaseOpacity;
         private SpriteSheet sprite;
 
         public Star(){
 
             sprite = new SpriteSheet(Window.tt.star,3,1,0);
             sprite.center();
             sprite.animate((int)(Math.random()*3)); //set random sprite
             x = (int)(Math.random()*Window.getWidth());
             y = (int)(Math.random()*Window.getHeight());
             //sprite.update(x,y);
            //System.out.println(sprite.getCurrentFrame());
             speedY = (int)(Math.random()*3)+1;
             //opacity properties
             alphaType = AlphaComposite.SRC_OVER;
             maxOpacity = 0.75;
             minOpacity = 0.0;
             blinkRate = 0.025;
             opacity = (Math.random()*maxOpacity);
         }
         public void draw(Graphics2D g){
             g.setComposite(AlphaComposite.getInstance(alphaType, (float)opacity));
             sprite.draw(g);
             g.setComposite(AlphaComposite.getInstance(alphaType, 1.0f));
         }
         public void update(){
             //update star placement
             y += speedY;
             sprite.update(x,y);
             //update opacity values
             if (increaseOpacity){
                 if (opacity+blinkRate < maxOpacity) opacity+=blinkRate;
                 else{
                     opacity=maxOpacity;
                     increaseOpacity=false;
                 }
             }
             else {
                 if (opacity-blinkRate > minOpacity) opacity-=blinkRate;
                 else{
                     opacity=minOpacity;
                     increaseOpacity=true;
                 }
             }
             //if the star goes off the screen, reset its position
             if (y-sprite.getImageHeight() > Window.getHeight()){
                 y = -sprite.getImageHeight();
                 x = (int)(Math.random()*Window.getWidth());
             }
         }
     }
 }

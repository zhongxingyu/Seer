 package edu.mines.csci598B.entejagd;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import edu.mines.csci598B.backend.GameManager;
 
 public class Duck extends GameObject {
   private final float velocity;
   private final GameManager game;
  private static final float RADIUS = 0.075f;
 
   public Duck(GameManager game, GameField field) {
     super(field,
           Math.random() < 0.5? 1:0,
           game.vheight() * ((float)Math.random()/2 + 0.5f));
     this.game = game;
     this.velocity = (float)Math.random()*0.35f *
                     (x > 0.5f? -1.0f : +1.0f);
   }
 
   public void update(float et) {
     x += velocity * et;
     alive &= (x >= 0.0f && x < 1.0f);
   }
 
   public void draw(Graphics2D g) {
     g.setColor(Color.yellow);
    g.fillOval(game.vcxtopx(x - RADIUS/2),
               game.vcytopx(y - RADIUS/2) + game.vdytopx(RADIUS),
               game.vdxtopx(RADIUS),
               -game.vdytopx(RADIUS));
   }
 
   public void shoot(float x, float y) {
     float dx = x-this.x, dy = y-this.y;
     if (dx*dx + dy*dy < RADIUS*RADIUS) {
       alive = false;
       field.add(new Gore(game, field, this));
     }
   }
 }

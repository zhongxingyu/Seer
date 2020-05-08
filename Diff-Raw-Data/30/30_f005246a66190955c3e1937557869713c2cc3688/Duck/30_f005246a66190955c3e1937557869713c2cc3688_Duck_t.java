package edu.mines.csci598.recycler.entejagd;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.SoundEffect;
 
 public class Duck extends GameObject {
     private final float velocity;
     private final GameManager game;
     private static final float RADIUS = 0.075f/2.0f;
 
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
         g.fillOval(game.vcxtopx(x - RADIUS),
                 game.vcytopx(y - RADIUS) + game.vdytopx(RADIUS*2),
                 game.vdxtopx(RADIUS*2),
                 -game.vdytopx(RADIUS*2));
     }
 
     public void shoot(float x, float y) {
         float dx = x-this.x, dy = y-this.y;
         if (dx*dx + dy*dy < RADIUS*RADIUS) {
             alive = false;
             field.add(new Gore(game, field, this));
             //TODO: Get this from the jar somehow
             SoundEffect.play("src/resources/sounds/wilhelm.pcm",
                     game.getAudioPlayer(), (short)0x7FFF);
         }
     }
 }

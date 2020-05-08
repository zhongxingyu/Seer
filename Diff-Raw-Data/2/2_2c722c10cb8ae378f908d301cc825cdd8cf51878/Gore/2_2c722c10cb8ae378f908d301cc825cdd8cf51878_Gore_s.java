 package edu.mines.csci598B.entejagd;
 
 import static java.lang.Math.*;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import edu.mines.csci598B.backend.GameManager;
 
 class Gore extends GameObject {
   private static final class Particle {
     private static final float INITV = 0.5f;
     private static final float GRAVITY = 0.3f;
     public float x, y, vx, vy;
 
     Particle(float x, float y) {
       this.x = x;
       this.y = y;
       float theta = (float)(random()*PI*2);
       float f = (float)random();
       this.vx = INITV * f*(float)cos(theta);
       this.vy = INITV * f*(float)sin(theta);
     }
 
     void update(float et) {
       x += vx*et;
       y += vy*et;
       vy -= GRAVITY*et;
     }
 
     void draw(GameManager game, Graphics2D g) {
       g.drawLine(game.vcxtopx(x), game.vcytopx(y),
                  game.vcxtopx(x + vx*0.03f),
                  game.vcytopx(y + vy*0.03f));
     }
   }
 
  private static final Particle particles[] = new Particle[64];
   private final GameManager game;
 
   public Gore(GameManager game, GameField field, GameObject src) {
     super(field, src.getX(), src.getY());
     this.game = game;
 
     for (int i = 0; i < particles.length; ++i)
       particles[i] = new Particle(x, y);
   }
 
   public void update(float et) {
     alive = false;
     for (Particle p: particles) {
       p.update(et);
       alive |= p.y > 0;
     }
   }
 
   public void draw(Graphics2D g) {
     g.setColor(Color.red);
     for (Particle p: particles)
       p.draw(game, g);
   }
 }

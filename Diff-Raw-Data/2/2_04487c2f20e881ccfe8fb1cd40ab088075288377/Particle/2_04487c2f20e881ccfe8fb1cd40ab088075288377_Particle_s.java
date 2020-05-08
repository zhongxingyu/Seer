 package io.github.christiangaertner.mastergardner.entity.particle;
 
 import io.github.christiangaertner.mastergardner.entity.Entity;
 import io.github.christiangaertner.mastergardner.graphics.Renderer;
 import io.github.christiangaertner.mastergardner.graphics.Sprite;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Christian
  */
 public class Particle extends Entity {
 
     protected List<Particle> particles = new ArrayList<Particle>();
     public int updates = 0;
     protected int lifetime;
     protected double x, y, xa, ya;
 
     public Particle(int x, int y, int lifetime) {
         this.x = x;
         this.y = y;
         
         this.lifetime = lifetime * 60;
 
         sprite = Sprite.particle_basic;
 
         this.xa = random.nextGaussian();
         this.ya = random.nextGaussian();
     }
 
     public Particle(int x, int y, int lifetime, int amount) {
         this(x, y, lifetime);
         for (int i = 1; i < amount; i++) {
            particles.add(new Particle(x, y, lifetime);
         }
         particles.add(this);
     }
 
     @Override
     public void update() {
         for (Particle p : particles) {
             p.x += p.xa;
             p.y += p.ya;
             p.updates++;
             if (p.updates > p.getLifeTime()) {
                 p.remove();
             }
         }
     }
 
     @Override
     public void render(Renderer renderer) {
         for (Particle p : particles) {
             renderer.renderSprite((int) p.x, (int) p.y, p.sprite, false);
         }
     }
     
     public int getLifeTime() {
         return lifetime;
     }
 }

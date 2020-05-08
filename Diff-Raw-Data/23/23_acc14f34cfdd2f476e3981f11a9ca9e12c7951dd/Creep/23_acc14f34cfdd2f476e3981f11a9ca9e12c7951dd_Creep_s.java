 /**
  *   The MIT License
  *
  *  Copyright 2011 Andrew James <ephphatha@thelettereph.com>.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 package au.edu.csu.bofsa;
 
 import java.util.concurrent.Callable;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import au.edu.csu.bofsa.Behaviours.ActorRenderBehaviour;
 import au.edu.csu.bofsa.Behaviours.CollisionBehaviour;
 import au.edu.csu.bofsa.Behaviours.HealthBehaviour;
 import au.edu.csu.bofsa.Behaviours.MoveBehaviour;
 import au.edu.csu.bofsa.Behaviours.VelocityBehaviour;
 import au.edu.csu.bofsa.Behaviours.WaypointBehaviour;
 import au.edu.csu.bofsa.Events.DamageEvent;
 import au.edu.csu.bofsa.Events.Event;
 import au.edu.csu.bofsa.Events.EventSink;
 import au.edu.csu.bofsa.Events.GenericEvent;
 
 /**
  * @author ephphatha
  *
  */
 public class Creep implements Callable<Boolean>, Comparable<Object>, EventSink {
 
   private HealthBehaviour h;
   private MoveBehaviour m;
   private WaypointBehaviour w;
   private VelocityBehaviour v;
   private CollisionBehaviour c;
   private ActorRenderBehaviour arb;
   
   private CreepManager cm;
   
   private boolean isDead;
   
   Creep(CreepManager cm) {
     this.cm = cm;
     
     this.isDead = false;
   }
   
   public Rectangle getBounds() {
     CopyableVector2f position = this.m.getSignal().read();
     return new Rectangle(position.x - 0.25f, position.y - 0.25f, 0.5f, 0.5f);
   }
   
   public Vector2f getPosition() {
     return this.m.getSignal().read();
   }
   
   public Vector2f getVelocity() {
     return this.v.getSignal().read();
   }
   
   public void draw(Graphics g) {
     this.arb.draw(g);
   }
   
   public Boolean call() {
     this.arb.call();
     
     this.h.call();
     
     this.v.call();
 
     this.m.call();
     
     this.c.call();
     
     this.w.call();
     
     if (this.isDead) {
       this.cm.onDeath(this);
     }
     
     return false;
   }
 
   public void takeDamage(float damage) {
     this.h.handleEvent(new DamageEvent(this, damage, Event.Type.TARGETTED, System.nanoTime()));
   }
 
   @Override
   public void handleEvent(Event event) {
     if (event.value == GenericEvent.Message.DEATH) {
       this.isDead = true;
     } else if (event.value == GenericEvent.Message.NEW_BEHAVIOUR) {
       Object o = event.getSource();
       if (o instanceof HealthBehaviour) {
         this.h = (HealthBehaviour) o;
       } else if (o instanceof VelocityBehaviour) {
         this.v = (VelocityBehaviour) o;
       } else if (o instanceof MoveBehaviour) {
         this.m = (MoveBehaviour) o;
       } else if (o instanceof CollisionBehaviour) {
         this.c = (CollisionBehaviour) o;
       } else if (o instanceof WaypointBehaviour) {
         this.w = (WaypointBehaviour) o;
       } else if (o instanceof ActorRenderBehaviour) {
         this.arb = (ActorRenderBehaviour) o;
       }
     }
   }
 
   @Override
   public int compareTo(Object o) {
     return this.hashCode() - o.hashCode();
   }
 }

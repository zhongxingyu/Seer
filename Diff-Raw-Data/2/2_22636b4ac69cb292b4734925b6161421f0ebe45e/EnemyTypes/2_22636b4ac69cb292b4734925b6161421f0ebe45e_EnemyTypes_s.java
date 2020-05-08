 package enemies;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Set;
 import util.SetCallback;
 import player.Player;
 
 public final class EnemyTypes 
 {
     private EnemyTypes() 
     {
     }
 
     public static class Circle extends Enemy 
     {
         protected float invSpeed = 30000;
 
         public Color getColor() 
         {
             return Color.RED;
         }
 
         public Circle(float _x, float _y, float _invSpeed) 
         {
             super(_x, _y);
             invSpeed = _invSpeed;
         }
 
         public void move(List ps, float speedAdjust) 
         {
             float dx = Float.MAX_VALUE, d = 0; Player pm = null;
             Iterator i = ps.iterator();
             while(i.hasNext()) {
                 Player p = (Player)i.next();
                 d = distanceFrom(p.getX(), p.getY());
                 if(d<dx) {
                     dx = d;
                     pm = p;
                 }
             }
             
             int directionX = 1;
             int directionY = 1;
             float p1 = (pm.getY() - 5) - y;
             float p2 = (pm.getX() - 5) - x;
 
             if(p1 < 0) 
             {
                 directionY = -1;
             }
             
             if(p2 < 0) 
             {
                 directionX = -1;
             }
 
             float angle = (float) Math.atan(p1 / p2);
             float deltaD = dx/invSpeed;
             float deltaX = deltaD * (float) Math.abs(Math.cos(angle)) * directionX;
             float deltaY = deltaD * (float) Math.abs(Math.sin(angle)) * directionY;
 
             x += deltaX*speedAdjust;
             y += deltaY*speedAdjust;
         }
 
         public boolean isMortal() 
         {
             return true;
         }
     }
 
     public static class Monster extends Enemy 
     {
         protected float speed = 8;
 
         public Color getColor() 
         {
             return Color.MAGENTA.darker();
         }
 
         public Monster(float _x, float _y, float _speed) 
         {
             super(_x, _y);
             speed = _speed;
         }
 
         public void move(List ps, float speedAdjust) 
         {
             float dx = Float.MAX_VALUE, d = 0; Player pm = null;
             Iterator i = ps.iterator();
             while(i.hasNext()) {
                 Player p = (Player)i.next();
                 d = distanceFrom(p.getX(), p.getY());
                 if(d<dx) {
                     dx = d;
                     pm = p;
                 }
             }
             
             int directionX = 1;
             int directionY = 1;
             float p1 = (pm.getY() - 5) - y;
             float p2 = (pm.getX() - 5) - x;
 
             if(p1 < 0) 
             {
                 directionY = -1;
             }
             
             if(p2 < 0) 
             {
                 directionX = -1;
             }
 
             float angle = (float) Math.atan(p1 / p2);
             float deltaX = speed * (float) Math.abs(Math.cos(angle)) * directionX;
             float deltaY = speed * (float) Math.abs(Math.sin(angle)) * directionY;
 
             x += deltaX*speedAdjust;
             y += deltaY*speedAdjust;
         }
     }
 
     public static class Random extends Enemy 
     {
         private float vx, vy;
         private int[] borders;
         private int bounces = 0;
 
         public Color getColor() 
         {
             return Color.GREEN;
         }
 
         public Random(float _x, float _y, float _speed, int[] _borders) 
         {
             super(_x, _y);
             double ang = Math.random() * Math.PI;
             int qx = (Math.random() > .5)? 1: -1;
             int qy = (Math.random() > .5)? 1: -1;
             vx = _speed * qx * (float) Math.abs(Math.cos(ang));
             vy = _speed * qy * (float) Math.abs(Math.sin(ang));
             
             borders = _borders;
         }
 
         public Random(int _x, int _y) 
         {
             this(_x, _y, 4, null);
         }
 
         public void move(List ps, float speedAdjust)
         {     
             if(x > borders[2])
             {                
                 vx = Math.abs(vx)*-1;
                 bounces++;
             }
             if(x < borders[0])
             {
             	vx = Math.abs(vx);
                 bounces++;
             }
             if(y > borders[3])
             {                
                 vy = Math.abs(vy)*-1;
                 bounces++;
             }
             if(y < borders[1])
             {
             	vy = Math.abs(vy);
                 bounces++;
             }
             
             x += vx*speedAdjust;
             y += vy*speedAdjust;
         }
         
         public int getBounces()
         {
             return bounces;
         }
     }
 
     public static class Rain extends Enemy 
     {
         private float vx, vy;
         private int floor;
 
         public Color getColor() 
         {
             return Color.YELLOW;
         }
         
         public Rain(float _x, float _y, float _speed, int _floor) 
         {
             super(_x, _y);
             floor = _floor;
             vx = -_speed;
             vy = _speed;
         }
 
         public Rain(int _x, int _y, int _floor) 
         {
             this(_x, _y, (float)2.4, _floor);
         }
 
         public void move(List ps, float speedAdjust) 
         {
             x += vx*speedAdjust*0.2;
             y += vy*speedAdjust*0.2;
             if(y > floor)
             {
                 x = x+y;
                 y = 0;
             }
         }
     }
     
     public static class Bomb extends Monster {
         private SetCallback mod;
         private int[] borders;
         private static final int PIECES = 50;
         private boolean existant = true;
         
         public Color getColor() {
             return Color.BLUE;
         }
         
         public Bomb (float _x, float _y, float _speed, int[] _borders, SetCallback _mod) {
             super(_x, _y, _speed);
             mod = _mod;
             borders = _borders;
         }
         
         public void move(List ps, float speedAdjust) {
             if (existant) {
                 super.move(ps, speedAdjust);
                 float dx = 0, d = 0;
                 Player pm = null;
                 Iterator i = ps.iterator();
                 while (i.hasNext()) {
                     Player p = (Player) i.next();
                     d = distanceFrom(p.getX(), p.getY());
                     if (d < dx) {
                         dx = d;
                         pm = p;
                     }
                 }
                if (d < 8000) {
                     for (int j = 0; j < PIECES; j++) {
                         mod.add(new Shrapnel(x, y, speed, borders));
                     }
                     existant = false;
                 }
             }
         }
         
         public boolean isMortal() {
             return !existant;
         }
         
         public void paint(Graphics g) {
             if(existant)
                 super.paint(g);
         }
         
         public boolean collidesWith(int mx, int my) {
             return existant && super.collidesWith(mx, my);
         }
     }
     
     public static class Shrapnel extends Random {
         
         public Shrapnel(float _x, float _y, float _speed, int[] _borders) {
             super(_x, _y, _speed, _borders);
         }
         
         public Color getColor() {
             return Color.CYAN;
         }         
     }
 }

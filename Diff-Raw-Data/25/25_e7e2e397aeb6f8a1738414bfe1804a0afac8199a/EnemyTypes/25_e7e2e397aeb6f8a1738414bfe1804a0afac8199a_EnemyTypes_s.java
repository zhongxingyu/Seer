package insanity;
 
 import javax.swing.*;
 import java.awt.*;
 
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
 
         public void move(int mx, int my) 
         {
             int directionX = 1;
             int directionY = 1;
             float p1 = (my - 5) - y;
             float p2 = (mx - 5) - x;
 
             if(p1 < 0) 
             {
                 directionY = -1;
             }
             
             if(p2 < 0) 
             {
                 directionX = -1;
             }
 
             float angle = (float) Math.atan(p1 / p2);
             float dx = (float)Math.pow(p2, 2) + (float)Math.pow(p1, 2);
             float deltaD = dx/invSpeed;
             float deltaX = deltaD * (float) Math.abs(Math.cos(angle)) * directionX;
             float deltaY = deltaD * (float) Math.abs(Math.sin(angle)) * directionY;
 
             x += deltaX;
             y += deltaY;
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
 
         public void move(int mx, int my) 
         {
             int directionX = 1;
             int directionY = 1;
             float p1 = (my - 5) - y;
             float p2 = (mx - 5) - x;
 
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
 
             x += deltaX;
             y += deltaY;
         }
     }
 
     public static class Random extends Enemy 
     {
         private float vx, vy;
         int[] borders;
 
         public Color getColor() 
         {
             return Color.GREEN;
         }
 
         public Random(float _x, float _y, float _speed, int[] _borders) 
         {
             super(_x, _y);
             double ang = Math.random() * Math.PI;
             vx = _speed * (float) Math.abs(Math.cos(ang));
             vy = _speed * (float) Math.abs(Math.sin(ang));
             
             borders = _borders;
         }
 
         public Random(int _x, int _y) 
         {
             this(_x, _y, 4, null);
         }
 
         public void move(int mx, int my)
         {     
             if((x > borders[2])||(x < borders[0]))
             {                
                 vx *= -1;
             }
             
             if ((y > borders[3])||(y < borders[1]))
             {                
                 vy *= -1;
             }
             
             x += vx;
             y += vy;
         }
     }
 
     public static class Rain extends Enemy 
     {
         private float vx, vy;
 
         public Color getColor() 
         {
             return Color.YELLOW;
         }
 
         public Rain(float _x, float _y, float _speed) 
         {
             super(_x, _y);
             vx = -_speed;
             vy = _speed;
         }
 
         public Rain(int _x, int _y) 
         {
             this(_x, _y, (float)2.4);
         }
 
         public void move(int mx, int my) 
         {
             x += vx;
             y += vy;
         }
     }
 }

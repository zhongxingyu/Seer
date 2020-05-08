 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 public class Car {
     private static final Graphics Graphics2D = null;
     private final Wheel wheel1;
     private final Wheel wheel2;
     private int centerX;
     private int centerY;
     private final double CAR_LENGTH = 150;
 
     public Car() {
         wheel1 = new Wheel();
         wheel2 = new Wheel();
         wheel1.x += 200;
         wheel1.y = 300;
         wheel2.y = 300;
     }
 
     public static int getWheelSize() {
         return Wheel.WHEEL_SIZE;
     }
 
     public static void forward(boolean b) {
         Wheel.forward = b;
     }
 
     public void update(Grass grass) {
         wheel1.update(grass);
         wheel2.update(grass);
         restrictWheelDistance(wheel1, wheel2);
     }
 
     private void restrictWheelDistance(Wheel wheel1, Wheel wheel2) {
 
         centerX = (wheel1.x + wheel2.x) / 2;
         centerY = (wheel1.y + wheel2.y) / 2;
 
         double distance = Math.sqrt((wheel1.x - wheel2.x) * (wheel1.x - wheel2.x) + (wheel1.y - wheel2.y) * (wheel1.y - wheel2.y));
         int distanceX = Math.abs(wheel1.x - wheel2.x);
         if (distanceX == 0) distanceX = 1;
         int distanceY = Math.abs(wheel1.y - wheel2.y);
         if (distanceY == 0) distanceY = 1;
 
         double delta = distance - CAR_LENGTH;
         double deltaX = delta * distanceX / distance;
         if (wheel1.x < wheel2.x) deltaX = -deltaX;
         double deltaY = delta * distanceY / distance;
         if (wheel1.y < wheel2.y) deltaY = -deltaY;
 
 
         wheel1.x -= deltaX / 2;
         wheel2.x += deltaX / 2;
         wheel1.y -= deltaY / 2;
         wheel2.y += deltaY / 2;
        // System.out.println(String.format("delta: %s,  deltaX: %s, deltaY: %s",
        // delta, deltaX, deltaY));
     }
 
     public int getX() {
         return wheel1.x;
     }
 
     public void draw(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
 
         g2.setColor(Color.GRAY);
         g2.setStroke(new BasicStroke(10));
         g2.drawLine(wheel1.x, wheel1.y, wheel2.x, wheel2.y);
         g2.setColor(Color.RED);
         wheel1.draw(g2);
         g2.setColor(Color.BLUE);
         wheel2.draw(g2);
 
         // Center markers
         g2.setColor(Color.BLACK);
         g2.fillOval(centerX - 5, centerY - 5, 10, 10);
         g2.fillOval(wheel1.x - 5, wheel1.y - 5, 10, 10);
         g2.fillOval(wheel2.x - 5, wheel2.y - 5, 10, 10);
     }
 }
 
 class Wheel {
 
     public int x, y, dx;
 
     public float dy;
 
     private static final double BOUNCYNESS = 0.3;
    private static final double GRAVITY = 0.3f;
    public float ACCELERATION = 0.8f;
    public float MAX_SPEED = 20;
     public float SPEED = 0;
     public static final int WHEEL_SIZE = 75;
     private static final float RETARDATION = 0.2f;
     public static boolean forward = false;
 
     public Wheel() {
         x = 0;
         y = 500;
     }
 
     public void draw(Graphics g) {
         g.fillOval(x - (WHEEL_SIZE / 2), y - (WHEEL_SIZE / 2), WHEEL_SIZE, WHEEL_SIZE);
     }
 
     public void update(Grass grass) {
         if (SPEED > MAX_SPEED) SPEED = MAX_SPEED;
         x += SPEED;
         dy += GRAVITY;
         y += dy;
         int grassHeight = grass.getGrassHeight(x);
         if (dy > 0 && y > grassHeight) {
             if (forward) {
                 SPEED += ACCELERATION;
             }
             if (!forward && SPEED > 0) {
                 SPEED -= RETARDATION;
             }
 
             if (SPEED < 0) SPEED = 0;
 
             dy = (int) -(dy * BOUNCYNESS);
             // if (dy < 0.5) dy = 0;
        }
        if (y > grassHeight) {
             y = grassHeight;
         }
     }
 }

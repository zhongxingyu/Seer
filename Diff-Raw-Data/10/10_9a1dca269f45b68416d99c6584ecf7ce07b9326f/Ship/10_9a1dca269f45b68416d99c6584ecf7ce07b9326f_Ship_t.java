 import java.awt.*;
 
 public class Ship extends ObjectOnMap {
 
 
     private int direction_degrees;
     private int direction_degrees_before;
     private int length;
     private int width;
     private int velocity;
     private Color color;
     public Turret ship_turret = null;
     public final static Color PLAYER1 = new Color(78, 194, 19);
     public final static Color ENEMY = new Color(255, 89, 63);
     public final static Color SHIP = new Color(163, 82, 0);
 
     public boolean collision_detected = false;
 
 
     Ship(Vec v, Color color) {
         position.setV(v);
         direction_degrees = 0;
         length = 20;
         width = 10;
         velocity = 5;
 
 
         for (int i = 0; i < 5; i++) {
             Points.add(new Vec(0, 0));
         }
 
         ship_turret = new Turret(this, color);
     }
 
     public void update() {
         super.update();
         ship_turret.setPosition(position);
         ship_turret.fpos.setV(ship_turret.getPosition().addReturn(ship_turret.getFire_direction().multiplied(10)));
         ship_turret.incTimeToCD();
 
         Points = GeomHelp.findPolygon(position.getX(), position.getY(), length, width, (float) Math.toRadians(direction_degrees));
     }
 
     public void moveStraight() {
         //position.add(velocity * (float) (Math.cos(Math.toRadians(direction_degrees))), velocity * (float) (-Math.sin(Math.toRadians(direction_degrees))));
         Vec increment_position = new Vec(velocity * (float) (Math.cos(Math.toRadians(direction_degrees))), velocity * (float) (-Math.sin(Math.toRadians(direction_degrees))));
         move(increment_position, 0);
     }
 
     public void turnLeft() {
         //direction_degrees += 5;
         move(new Vec(0,0), 5);
         ship_turret.turnLeft();
     }
 
     public void turnRight() {
         //direction_degrees -= 5;
         move(new Vec(0,0), -5);
         ship_turret.turnRight();
     }
 
    public boolean Collision(Ship ship) {                          ////!!!!!!!!!! MISTAKE HERE. SOMEHOW SHIP INTERSECTS WITH ITSELF. CHECK TOMORROW
         for (int i = 0; i < 4; i++)
             for (int j = 0; j < 4; j++)
                 return Vec.linesIntersect(Points.get(i), Points.get(i + 1), ship.Points.get(j), ship.Points.get(j + 1));
 
         return false;
     }
 
     public void render(Graphics2D g) {
         if (!is_destroyed) {
             g.setColor(SHIP);
             GeomHelp.fillPolygon(position.getX(), position.getY(), length, width, (float) Math.toRadians(direction_degrees), g);
             ship_turret.render(g);
         } else {
             g.setColor(Color.RED);
             GeomHelp.fillPolygon(position.getX(), position.getY(), length, width, (float) Math.toRadians(direction_degrees), g);
         }
     }
 
     public Vec getPosition() {
         return position;
     }
 
     public Vec getDirectionVector() {
         return new Vec((float) Math.cos(Math.toRadians(direction_degrees)), (float) -Math.sin(Math.toRadians(direction_degrees)));
     }
 
 
     private void move(Vec increment_position, int increment_direction_degrees) {
         position_before.setV(position);
         position.add(increment_position);
         direction_degrees_before = direction_degrees;
         direction_degrees += increment_direction_degrees;
     }
 
     public void moveback() {
         position.setV(position_before);
         direction_degrees = direction_degrees_before;
        collision_detected=false;
     }
 }

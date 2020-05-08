 package jgpfun.world2d;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import jgpfun.life.SensorInput;
 import jgpfun.util.Settings;
 import jgpfun.world2d.senses.RadarSense;
 import jgpfun.world2d.senses.WallSense;
 
 /**
  *
  * @author Hansinator
  */
 public class RadarAntBody extends Body2d {
 
     public final WallSense wallSense;
 
     public final Motor2d motor;
 
     private static final int foodPickupRadius = Settings.getInt("foodPickupRadius");
 
     private final Organism2d organism;
 
     private final World2d world;
 
    private final RadarSense radarSense;
 
 
     public RadarAntBody(Organism2d organism, World2d world) {
        super(0.0, 0.0, 0.0, new SensorInput[7]);
         this.organism = organism;
         this.world = world;
 
         //init senses
         wallSense = new WallSense(world.worldWidth, world.worldHeight, this);
         radarSense = new RadarSense(this, world);
 
         //outputs
         this.motor = new TankMotor(this);
 
         //inputs
         inputs[0] = new OrientationSense();
         inputs[1] = radarSense;
         inputs[2] = new SpeedSense();
         inputs[3] = wallSense;
     }
 
 
     @Override
     public void prepareInputs() {
     }
 
 
     @Override
     public void postRoundTrigger() {
         Food food = world.findNearestFood(Math.round((float) x), Math.round((float) y));
         if ((food.x >= (x - foodPickupRadius))
                 && (food.x <= (x + foodPickupRadius))
                 && (food.y >= (y - foodPickupRadius))
                 && (food.y <= (y + foodPickupRadius))) {
             organism.incFood();
             food.randomPosition();
         }
     }
 
 
     @Override
     public void draw(Graphics g) {
         final double sindir = Math.sin(dir);
         final double cosdir = Math.cos(dir);
         final double x_len_displace = 6.0 * sindir;
         final double y_len_displace = 6.0 * cosdir;
         final double x_width_displace = 4.0 * sindir;
         final double y_width_displace = 4.0 * cosdir;
         final double x_bottom = x - x_len_displace;
         final double y_bottom = y + y_len_displace;
         final int x_center = Math.round((float) x);
         final int y_center = Math.round((float) y);
 
         Polygon p = new Polygon();
         p.addPoint(Math.round((float) (x + x_len_displace)), Math.round((float) (y - y_len_displace))); //top of triangle
         p.addPoint(Math.round((float) (x_bottom + y_width_displace)), Math.round((float) (y_bottom + x_width_displace))); //right wing
         p.addPoint(Math.round((float) (x_bottom - y_width_displace)), Math.round((float) (y_bottom - x_width_displace))); //left wing
 
         g.setColor(Color.darkGray);
         g.drawLine(x_center, y_center, Math.round((float) (x + RadarSense.beamLength * Math.sin(radarSense.direction))), Math.round((float) (y - RadarSense.beamLength * Math.cos(radarSense.direction))));
 
         g.setColor(Color.red);
         g.drawPolygon(p);
         g.fillPolygon(p);
 
         g.setColor(Color.green);
         g.drawString("" + organism.getFitness(), x_center + 8, y_center + 8);
     }
 
     private class SpeedSense implements SensorInput {
 
         @Override
         public int get() {
             return (int) (lastSpeed * Organism2d.intScaleFactor);
         }
 
     }
 }

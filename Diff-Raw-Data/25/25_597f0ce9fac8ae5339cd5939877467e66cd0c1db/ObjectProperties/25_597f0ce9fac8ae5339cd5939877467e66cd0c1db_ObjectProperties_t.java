 package de.engine.objects;
 
 import de.engine.environment.EnvProps;
 import de.engine.math.*;
 
 public abstract class ObjectProperties implements Cloneable
 {
     public enum Material
     {
         STEEL(1), ALUMINIUM(1), NACL(1), RUBBER(1), WATER(1);
         
         private final double elasticity;
         
         private Material(double elasticity)
         {
             this.elasticity = elasticity;
         }
         
         public double elasticity()
         {
             return elasticity;
         }
     };
     
    private double frametime = 0;
     
     public double getFrameTime()
     {
         if(isPinned)
             return 0;
         return frametime;
     }
     
     public void setFrameTime(double frametime)
     {
         if(isPinned)
             return;
         this.frametime = frametime;
     }
     
     public double getTime()
     {
         return EnvProps.deltaTime() - frametime;
     }
     
     public double getTime(double time)
     {
         return time - frametime;
     }
     
     public boolean isPinned = false;
     
     private ObjectProperties sleepOn = null;
     
     public void fallAsleep(ObjectProperties o)
     {
         sleepOn = o;
     }
     
     public boolean sleeps()
     {
         return (sleepOn != null);
     }
     
     public void wakeUp()
     {
         sleepOn = null;
     }
     
     // will get incremented every time it's used, to apply unique id's to each
     // of the new objects
     public static int idCounter = 0;
     
     public Transformation world_position;
     
     public Vector last_intersection;
     public Vector closest_point;
     
     public Vector getPosition()
     {
         return world_position.translation;
     }
     
     public void setPosition(double x, double y)
     {
         sleepOn = null;
         world_position.translation = new Vector(x, y);
     }
     
     
     public Vector getPosition(double time)
     {
         if(isPinned)
             return getPosition();
         double localtime = getTime(time);
         return Util.add(world_position.translation, new Vector(velocity.getX(), (EnvProps.grav_acc() / 2d * localtime + velocity.getY())).scale(localtime));
     }
     
     public Vector getNextPosition()
     {
         return getPosition(EnvProps.deltaTime());
     }
     
     
     public double getX()
     {
         return world_position.translation.getX();
     }
     
     public double getY()
     {
         return world_position.translation.getY();
     }
     
     
     public double getRotationAngle()
     {
         return world_position.rotation.getAngle();
     }
     
     public void setRotationAngle(double angle)
     {
         world_position.rotation = new Rotation(angle);
     }
     
     
     // TODO forces, velocity, momentum should be a vector, because of their
     // direction
     protected double mass = 1;
     
     public Vector velocity = null;
     public double angular_velocity;
     
     public Vector momentum = null;
     public Vector normal_force = null;
     public Vector downhill_force = null;
     public double kinetic_energy = 0;
     public double potential_energy = 0;
     public double angular_momentum = 0;
     
     public double moment_of_inertia = 0;
     
     // Schwerpunkt relativ zum Objektursprung
     public Vector centroid = null;
     public Vector moving_direction = null;
     
     public Material surface = Material.STEEL;
     
     public int id = 0;
     
     protected double radius;
     
     // function for collision
     public abstract double getRadius();
     
     public abstract void setRadius(double radius);
     
     public double getMass()
     {
         return mass;
     }
     
     public abstract void setMass(double mass);
     
     public Vector[] getAABB()
     {
         Vector aabb[] = new Vector[2];
         aabb[0] = new Vector(getPosition().getX() - radius, getPosition().getY() - radius);
         aabb[1] = new Vector(getPosition().getX() + radius, getPosition().getY() + radius);
         return aabb;
     }
     
     public abstract Vector[] getAABB(double time);
     
     public abstract Vector[] getNextAABB();
     
     protected ObjectProperties()
     {
         this.id = idCounter++;
         this.last_intersection = new Vector(Double.MAX_VALUE, Double.MAX_VALUE);
         this.closest_point = new Vector(Double.MAX_VALUE, Double.MAX_VALUE);
     }
     
     public int getId()
     {
         return id;
     }
     
     public void update()
     {
         if (isPinned || sleeps())
             return;
         world_position.translation = getNextPosition();
         world_position.rotation.setAngle(world_position.rotation.getAngle() + (angular_velocity * getTime() * (1 - EnvProps.friction())));
         // obj.getPosition().setY(
         // -9.81 / 2d * deltaTime + obj.velocity.getY()
         // * deltaTime + obj.getPosition().getY());
         velocity.add(0, (EnvProps.grav_acc() / 2d * getTime())).scale(1 - EnvProps.friction());
         
         // calc potential energy: Epot = m*g*h (mass * grav_const * y-coordinate)
         potential_energy = -mass * EnvProps.grav_acc() * world_position.translation.getY();
         
         // calc kinetic energy: Epot = m/2*v² (mass * grav_const * y-coordinate)
         kinetic_energy = 0.5 * mass * Math.abs( this.velocity.getX() );
         
        frametime = 0;
     }
     
     public void update(double time)
     {
         if (isPinned || sleeps())
             return;
         double localtime = getTime(time);
         world_position.translation = getPosition(time);
         velocity.add(0, (EnvProps.grav_acc() / 2d * localtime)).scale(1 - EnvProps.friction());
         world_position.rotation.setAngle(world_position.rotation.getAngle() + (angular_velocity * localtime * (1 - EnvProps.friction())));
         frametime = time;
     }
     
     @Override
     public abstract ObjectProperties clone();
     public abstract ObjectProperties clone(boolean cloneId);
     
     public abstract boolean contains(double x, double y);
 }

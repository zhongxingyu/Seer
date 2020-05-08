 package org.skullforge.asteroidpush.entities.spaceship;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
import org.jbox2d.common.MathUtils;
 import org.jbox2d.common.Transform;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.skullforge.asteroidpush.SignalController;
 import org.skullforge.asteroidpush.SimulatorCommand;
 import org.skullforge.asteroidpush.SpawnEntityCommand;
 import org.skullforge.asteroidpush.designer.data.effectors.EntityEmitterData;
 import org.skullforge.asteroidpush.entities.ProjectileFactory;
 
 public class EntityEmitter implements Effector {
 
    final private EntityEmitterData data;
    private Body shooter;
    private Transform placement;
    private boolean shotHandled;
 
    public EntityEmitter(EntityEmitterData data) {
       this.data = data;
       this.placement = new Transform();
       this.shooter = null;
       this.shotHandled = false;
    }
 
    @Override
    public Collection<SimulatorCommand> update(int frameNumber,
                                               SignalController controller) {
       if (controller.primaryFire == 0.0f) {
          this.shotHandled = false;
       } else if (!this.shotHandled) {
          Transform shooterTransform = shooter.getTransform();
 
          Vec2 offset = data.getOffset();
          offset = Transform.mul(placement, offset);
          offset = Transform.mul(shooterTransform, offset);
 
          Vec2 velocity = data.getVelocity();
          velocity = placement.R.mul(velocity);
          velocity = shooterTransform.R.mul(velocity);
          velocity.addLocal(shooter.getLinearVelocity());
 
         float angle = placement.getAngle();
         angle += shooterTransform.getAngle();
         angle += MathUtils.HALF_PI;

          ArrayList<SimulatorCommand> commands = new ArrayList<SimulatorCommand>();
         ProjectileFactory factory = new ProjectileFactory(shooter.getWorld(),
               velocity, angle);
          commands.add(new SpawnEntityCommand(factory, offset, null));
          this.shotHandled = true;
          return commands;
       }
       return null;
    }
 
    public void setShooter(Body shooter) {
       this.shooter = shooter;
    }
 
    public void setPlacement(Transform transform) {
       this.placement.set(transform);
    }
 
 }

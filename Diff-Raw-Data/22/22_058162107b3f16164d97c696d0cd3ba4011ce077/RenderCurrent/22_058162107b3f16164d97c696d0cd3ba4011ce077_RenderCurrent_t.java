 package game.triggers.effects;
 
 import game.components.interfaces.IAnimatedComponent;
 import game.pods.GameTime;
 import game.triggers.IEffect;
 import game.world.World;
 
 import org.newdawn.slick.Graphics;
 
 public class RenderCurrent implements IEffect {
   private final IAnimatedComponent anim;
   private final Graphics graphics;
 
   public RenderCurrent(IAnimatedComponent anim, Graphics graphics) {
     this.anim     = anim;
     this.graphics = graphics;
   }
 
   @Override
   public void execute(GameTime time, World world) {
     // FIXME: Not correctly translated
    anim.render(graphics);
    graphics.flush();
   }
 }

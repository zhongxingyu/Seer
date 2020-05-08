 /*
  * 
  */
 package forscene.core.events.system;
 
 import forscene.core.entities.toTest.AbstractAnimation;
 import forscene.system.managers.AbstractGameLoopManager;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class EventAnimationUpdate.
  */
 public class AnimationUpdateEvent extends AbstractEvent {
 
   /** The animation. */
   private AbstractAnimation animation;
 
   /**
    * Instantiates a new event animation update.
    * 
    * @param anim
    *          the anim
    */
   public AnimationUpdateEvent(AbstractAnimation anim) {
     animation = anim;
     setDone(false);
     setPriority(-1);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.events.AbstractEvent#run()
    */
   @Override
   public void run() {
     if (animation.isStarted()) {
       long updateRate = animation.getUpdateRate();
       /*
        * PlayN.log().debug( " ticks " +
        * GameLoopManager.getInstance().getTicks()); PlayN.log().debug(
        * " ticksRAte " + GameLoopManager.getInstance().getTickRate());
        * PlayN.log().debug( " second " +
        * GameLoopManager.getInstance().getSeconds()); PlayN.log().debug(
        * " updateRate " + updateRate );
        */
       long scaledFps;
       if (updateRate != 0) {
         scaledFps = (AbstractGameLoopManager.getInstance().getTickRate() / updateRate);
       } else {
         scaledFps = 1;
       }
 
       if ((updateRate == 0)
           || ((((AbstractGameLoopManager.getInstance().getTicks())) % scaledFps) == 0)) {
         animation.run();
         animation.getTarget().setToUpdate(true);
        setDone(false);
       }
     } /*
        * else { setDone(true); }
        */
   }
 }

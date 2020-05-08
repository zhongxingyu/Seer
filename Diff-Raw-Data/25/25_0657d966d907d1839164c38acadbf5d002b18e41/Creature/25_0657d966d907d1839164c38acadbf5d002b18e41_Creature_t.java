 package soundfriend.types;
 
 import java.io.File;
 
 import javax.swing.JOptionPane;
 
import soundfriend.SoundController;
import soundfriend.SoundController.Sounds;
 import toritools.debug.Debug;
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.entity.sprite.ImageSprite;
 import toritools.math.MidpointChain;
 import toritools.math.Vector2;
 import toritools.scripting.EntityScript;
 import toritools.scripting.ScriptUtils;
 
 public class Creature extends Entity implements EntityScript {
 
     /**
      * Important state stuff.
      */
     private float mood, energy;
     private long age;
     private String name;
     private State state;
     private boolean isSick = false;
 
     private float maxEnergy = 1f;
 
     // Movement internals
     private Vector2 moveTarget;
     private float moveTargetSpeed = .6f;
     private MidpointChain moveChain;
     int sneezeTimer = 0;
 
     public Creature() {
 
         this.setType("pet");
 
         setDim(new Vector2(64, 64));
         setPos(new Vector2(100, 100));
 
         addScript(this);
 
         setSprite(new ImageSprite(new File("tamodatchi/kitten.png"), 4, 6));
         getSprite().setTimeStretch(40);
     }
 
     @Override
     public void onSpawn(Entity self, Level level) {
         age = 0;
         mood = .5f;
         energy = .5f;
         name = "Mozart";
         moveTarget = self.getPos();
         moveChain = new MidpointChain(self.getPos(), 20);
         state = State.ROAM;
     }
 
     @Override
     public void onUpdate(Entity self, float time, Level level) {
 
         Debug.print("Mood: " + mood + " | Energy: " + energy);
 
         // State Transitions
         stateTransitions(level);
 
         // Actions
         float energyExpended = stateActions(level);
 
         // Handle stat shanges
         maintenence(energyExpended);
 
         // Check for issues
         checkForHealthIssues();
     }
 
     private void maintenence(float energyExpended) {
 
         if (energy > 0) {
             energy -= .00002 * energyExpended * energyExpended * (energy > 1 ? 8f * energy : 1f);
             if (energy > 1 || isSick) {
                 isSick = energy > maxEnergy;
             }
         }
         energy = Math.min(energy, maxEnergy * 2f);
 
         // mood
         if (mood >= 0 && state != State.SICK_INCAP && state != State.SLEEP) {
             mood -= (1f / energy) * .00001f * (isSick ? energy * 30 : 1);
         }
 
         mood = Math.min(1, Math.max(0, mood));
     }
 
     private float stateActions(final Level level) {
         /*
          * State Actions
          */
 
         float moveTargetSpeed = this.moveTargetSpeed;
 
         if (state == State.ROAM && Math.random() < .006 * energy * energy) {
             moveTarget = new Vector2((level.getDim().getWidth() - getDim().getWidth() / 2) * Math.random(), (level
                     .getDim().getHeight() - getDim().getHeight() / 2) * Math.random());
         }
 
         if (state == State.PLAYING) {
 
             moveTargetSpeed *= 4;
 
             if (level.getEntitiesWithType("ball").isEmpty()) {
                 state = State.ROAM;
             }
 
             Vector2 closest = null;
             float bestDist = Float.MAX_VALUE;
             for (Entity e : level.getEntitiesWithType("ball")) {
                 if (closest == null || getPos().dist(e.getPos()) < bestDist) {
                     closest = e.getPos().add(e.getDim().scale(.5f));
                     bestDist = getPos().dist(e.getPos());
                 }
             }
 
             mood = Math.min(1, mood + energy * (1f / bestDist) * .004f);
 
             moveTarget = closest;
         }
 
         if (state == State.HUNTING) {
 
             moveTargetSpeed *= 2;
 
             Vector2 closest = null;
             float bestDist = Float.MAX_VALUE;
             for (Entity e : level.getEntitiesWithType("food")) {
                 if (ScriptUtils.isColliding(this, e)) {
                     eatFood((Food) e, level);
                     break;
                 }
                 if (closest == null || getPos().dist(e.getPos()) < bestDist) {
                     closest = e.getPos().add(e.getDim().scale(.5f));
                     bestDist = getPos().dist(e.getPos());
                 }
             }
             moveTarget = closest;
         }
 
         if (state == State.SICK_INCAP || state == State.SLEEP)
             moveTarget = null;
 
         if (moveTarget != null) {
             Vector2 move = moveTarget.sub(getPos().add(getDim().scale(.5f)));
             if (move.mag() > moveTargetSpeed) {
                 move = move.unit().scale(moveTargetSpeed * Math.min(1f, energy));
                 moveChain.setA(moveChain.getA().add(move));
                 moveChain.smoothTowardA();
                 setPos(moveChain.getB().sub(getDim().scale(.5f)));
             } else {
                 moveTarget = null;
             }
         }
 
         return moveTargetSpeed;
 
     }
 
     private void stateTransitions(final Level level) {
 
         /*
          * Transitions
          */
         if (state == State.ROAM || state == State.PLAYING || state == State.HUNTING) {
 
             if (sneezeTimer <= 0)
                 getSprite().setCycle(mood >= .5 ? 0 : 1);
 
             if (!level.getEntitiesWithType("ball").isEmpty()) {
                 state = State.PLAYING;
             }
 
             if ((energy < .5f || mood < .5f) && !level.getEntitiesWithType("food").isEmpty()) {
                 state = State.HUNTING;
             }
 
             if (energy < .1) {
                 state = State.SLEEP;
             }
         }
 
         if (state == State.SICK_INCAP && sickPercentage() < 100) {
             state = State.ROAM;
         }
 
         if (state == State.SLEEP) {
             sprite.set(0, 5);
             energy += .0001;
             if (energy > .5f) {
                 state = State.ROAM;
             }
         }
         
         // Play a cute little animation from time to time
         if (state != State.SICK_INCAP && state != State.SLEEP) {
             getSprite().setTimeStretch((int) (40 * energy));
             sprite.nextFrame();
             if (--sneezeTimer <= 0 && Math.random() < .001) {
                 sprite.setCycle((Math.random() < .95) ? 4 : 2);
                 sneezeTimer = 60;
             }
         }
 
     }
 
     private void checkForHealthIssues() {
         if (energy > maxEnergy * 1.9f) {
             getSprite().setTimeStretch(1);
             getSprite().set(1, 5);
             state = State.SICK_INCAP;
         }
 
         if (mood <= 0) {
             JOptionPane.showMessageDialog(null, name + " hates you! It has run away.");
             System.exit(0);
         }
        
        SoundController.play(Sounds.MEOW);
        
     }
 
     private void eatFood(final Food e, final Level level) {
         level.despawnEntity(e);
         energy += .3f;
         state = State.ROAM;
         mood = mood + .2f * mood;
     }
 
     @Override
     public void onDeath(Entity self, Level level, boolean isRoomExit) {
 
     }
 
     public float getMood() {
         return mood;
     }
 
     /**
      * Energy relational to maxEnergy
      */
     public float getEnergy() {
         return energy / maxEnergy;
     }
 
     public long getAge() {
         return age;
     }
 
     public String getName() {
         return name;
     }
 
     public State getState() {
         return state;
     }
 
     public int sickPercentage() {
         return (int) ((energy - maxEnergy) / ((maxEnergy * 1.9f) - maxEnergy) * 100f);
     }
 
     public boolean isSick() {
         return isSick;
     }
 
     public static enum State {
         SLEEP, ROAM, HUNTING, PLAYING, SICK_INCAP;
     }
 }

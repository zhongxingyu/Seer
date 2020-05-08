 package aigilas.creatures.impl;
 
 import aigilas.Aigilas;
 import aigilas.AigilasConfig;
 import aigilas.creatures.BaseCreature;
 import aigilas.entities.SkillEffect;
 import aigilas.items.ItemFactory;
 import aigilas.skills.SkillId;
 import sps.bridge.ActorType;
 import sps.bridge.ActorTypes;
 import sps.bridge.Sps;
 import sps.core.Logger;
 import sps.core.Point2;
 import sps.core.RNG;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public class CreatureFactory {
     private static int activePlayers = 0;
 
     public static BaseCreature create(ActorType actorType, Point2 position) {
         BaseEnemy result;
         if (actorType.is(Sps.Actors.Player)) {
             result = new Player(activePlayers++);
         }
         else {
             result = generateCreature(actorType);
         }
 
         result.setup(position);
        EntityManager.get().

                addEntity(result);

        if (AigilasConfig.get().debugInventory)
 
        {
             for (int ii = 0; ii < 1000; ii++) {
                 result.pickupItem(ItemFactory.createRandomPlain());
             }
         }
 
         return result;
     }
 
     private static BaseEnemy generateCreature(ActorType actorType) {
         if (actorType.is(Aigilas.Actors.Wrath)) {
             return new Wrath();
         }
         if (actorType.is(Aigilas.Actors.Hand)) {
             return new Hand();
         }
         if (actorType.is(Aigilas.Actors.Pride)) {
             return new Pride();
         }
         if (actorType.is(Aigilas.Actors.Envy)) {
             return new Envy();
         }
         if (actorType.is(Aigilas.Actors.Gluttony)) {
             return new Gluttony();
         }
         if (actorType.is(Aigilas.Actors.Lust)) {
             return new Lust();
         }
         if (actorType.is(Aigilas.Actors.Sloth)) {
             return new Sloth();
         }
         if (actorType.is(Aigilas.Actors.Greed)) {
             return new Greed();
         }
         if (actorType.is(Aigilas.Actors.Serpent)) {
             return new Serpent();
         }
         if (actorType.is(Aigilas.Actors.Breaking_Wheel)) {
             return new BreakingWheel();
         }
         if (actorType.is(Aigilas.Actors.Wrath_Acolyte)) {
             return new WrathEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Pride_Acolyte)) {
             return new PrideEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Envy_Acolyte)) {
             return new EnvyEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Gluttony_Acolyte)) {
             return new GluttonyEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Lust_Acolyte)) {
             return new LustEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Sloth_Acolyte)) {
             return new SlothEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Greed_Acolyte)) {
             return new GreedEnemyAcolyte();
         }
         if (actorType.is(Aigilas.Actors.Dummy)) {
             return new Dummy();
         }
 
         Logger.error("Unknown actorType passed into CreatureFactory: " + actorType.Name);
         return null;
     }
 
     public static BaseCreature createRandom(Point2 randomPoint) {
         return create(ActorTypes.getRandomGeneratable(), randomPoint);
     }
 
     public static BaseCreature createMinion(SkillId skillId, BaseCreature source, SkillEffect effectGraphic, Point2 location) {
         Minion result = null;
         switch (skillId) {
             case Acid_Nozzle:
                 result = new AcidNozzle();
                 break;
             case Dart_Trap:
                 result = new DartTrap();
                 break;
             case Explode:
                 result = new Explosion();
                 break;
             case Ice_Shard:
                 result = new IceShard();
                 break;
             case Vapor_Cloud:
                 result = new VaporCloud();
                 break;
             case Plague:
                 result = new PoisonCloud();
                 break;
             default:
                 try {
                     throw new Exception("No minion was defined for the given skillId.");
                 }
                 catch (Exception e) {
 
                     e.printStackTrace();
                 }
         }
         result.init(source, effectGraphic);
         if (location != null) {
             result.setLocation(location);
         }
         EntityManager.get().addEntity(result);
         return result;
     }
 
     public static BaseCreature createMinion(SkillId skillId, BaseCreature source, SkillEffect effectGraphic) {
         return createMinion(skillId, source, effectGraphic, null);
     }
 
     public static BaseCreature createMinion(SkillId skillId, BaseCreature source) {
         return createMinion(skillId, source, null, null);
     }
 
     private static List<ActorType> __remainingBosses;
 
     static {
         __remainingBosses = new LinkedList<ActorType>(Arrays.asList(ActorTypes.get(Aigilas.Actors.Wrath), ActorTypes.get(Aigilas.Actors.Envy), ActorTypes.get(Aigilas.Actors.Pride), ActorTypes.get(Aigilas.Actors.Sloth), ActorTypes.get(Aigilas.Actors.Greed), ActorTypes.get(Aigilas.Actors.Lust), ActorTypes.get(Aigilas.Actors.Gluttony)));
     }
 
     public static void reset() {
         __remainingBosses = new LinkedList<ActorType>(Arrays.asList(ActorTypes.get(Aigilas.Actors.Wrath), ActorTypes.get(Aigilas.Actors.Envy), ActorTypes.get(Aigilas.Actors.Pride), ActorTypes.get(Aigilas.Actors.Sloth), ActorTypes.get(Aigilas.Actors.Greed), ActorTypes.get(Aigilas.Actors.Lust), ActorTypes.get(Aigilas.Actors.Gluttony)));
         activePlayers = 0;
     }
 
     public static int bossesRemaining() {
         return __remainingBosses.size();
     }
 
     public static Entity createNextBoss(Point2 randomPoint) {
         ActorType nextBoss = __remainingBosses.get(RNG.next(0, __remainingBosses.size()));
         __remainingBosses.remove(nextBoss);
         return create(nextBoss, randomPoint);
     }
 }

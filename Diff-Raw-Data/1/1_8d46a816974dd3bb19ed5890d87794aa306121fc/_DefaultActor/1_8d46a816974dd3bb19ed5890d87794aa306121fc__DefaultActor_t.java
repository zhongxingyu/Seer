 package org.kornicameister.sise.lake.types.actors;
 
 import com.google.common.base.Objects;
 import org.kornicameister.sise.lake.types.*;
 import org.kornicameister.util.StatField;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public abstract class _DefaultActor
         extends DefaultClispType
         implements ClispType,
                    ClispReady,
                    EffectivenessReady {
     protected static final String  EMPTY_STRING = "";
     private static         Integer ID           = 0;
     @ClispAsserted
     protected final Integer      id;
     @ClispAsserted
     protected       LakeActors   type;
     @ClispAsserted
     protected       WorldField   atField;
     @ClispAsserted
     protected       Boolean      canAttack;
     @ClispAsserted
     protected       Boolean      canFly;
     @ClispAsserted
     protected       Boolean      canSwim;
     @ClispAsserted
     protected       Integer      weight;
     @ClispAsserted
     protected       Integer      howManyFishes;
     @ClispAsserted
     protected       Integer      hp;
     @ClispAsserted
     protected       Integer      visionRange;
     @ClispAsserted
     protected       Integer      attackRange;
     @ClispAsserted
     protected       Integer      moveRange;
     @ClispAsserted
     protected       Integer      hunger;
     @ClispAsserted
     protected       DefaultActor target;
     @ClispAsserted
     protected       Boolean      aggressive;
     @ClispAsserted
     protected       Integer      cash;
     @ClispAsserted
     protected       Integer      corruptionThreshold;
     @ClispAsserted
     protected       Boolean      validId;
     //no clips fields
     protected       Boolean      targetHit;
     protected       Boolean      tookBribe;
     protected       Integer      attackPower;
     protected       Boolean      isMoveChanged;
     protected       Integer      roundsAlive;
     protected       Boolean      isAlive;
     protected       Double       effectivity_1;
     protected       Double       effectivity_2;
     //no clips fields
 
     protected _DefaultActor() {
         this.id = _DefaultActor.ID++;
         this.roundsAlive = 0;
         this.effectivity_1 = 0d;
         this.effectivity_2 = 0d;
     }
 
     protected static int getRandomInt(final int lower, final int higher, final Random seed) {
         if (lower > higher) {
             throw new IllegalArgumentException("Start cannot exceed End.");
         }
         final long range = (long) higher - (long) lower + 1;
         final long fraction = (long) (range * seed.nextDouble());
         return (int) (fraction + lower);
     }
 
     protected String appendExtraDataToFact() {
         return EMPTY_STRING;
     }
 
     public void newRound() {
         this.roundsAlive = this.roundsAlive + 1;
     }
 
     public LakeActors getType() {
         return type;
     }
 
     public void setType(final LakeActors type) {
         this.type = type;
     }
 
     public Integer getId() {
         return id;
     }
 
     public Boolean getCanAttack() {
         return canAttack;
     }
 
     public void setCanAttack(final Boolean canAttack) {
         this.canAttack = canAttack;
     }
 
     public Boolean getCanFly() {
         return canFly;
     }
 
     public void setCanFly(final Boolean canFly) {
         this.canFly = canFly;
     }
 
     public Boolean getCanSwim() {
         return canSwim;
     }
 
     public void setCanSwim(final Boolean canSwim) {
         this.canSwim = canSwim;
     }
 
     public Integer getHp() {
         return hp;
     }
 
     public void setHp(final Integer hp) {
         this.hp = hp;
     }
 
     public Integer getVisionRange() {
         return visionRange;
     }
 
     public void setVisionRange(final Integer visionRange) {
         this.visionRange = visionRange;
     }
 
     public Boolean isIdValid() {
         return validId;
     }
 
     public void setValidId(final Boolean validId) {
         this.validId = validId;
     }
 
     /**
      * Resets all required fields to their initial state
      */
     public void clear() {
         this.tookBribe = false;
         this.effectivity_1 = 0d;
         this.effectivity_2 = 0d;
        this.targetHit = false;
     }
 
     public List<StatField> getStats() {
         final List<StatField> stats = new LinkedList<>();
 
         stats.add(new StatField("ID", this.getFactId()));
         stats.add(new StatField("Field", this.getAtField().getId()));
         stats.add(new StatField("-------", ""));
         stats.add(new StatField("HP", this.getHp()));
         stats.add(new StatField("Alive", this.isAlive()));
         stats.add(new StatField("Hunger", this.getHunger()));
         stats.add(new StatField("Rounds", this.getRoundsAlive()));
         stats.add(new StatField("-------", ""));
         stats.add(new StatField("Fished", this.getHowManyFishes()));
         stats.add(new StatField("Target", this.getTarget() != null ? this.getTarget().getFactId() : "none"));
         stats.add(new StatField("TargetHit", this.getTargetHit()));
         stats.add(new StatField("-------", ""));
         stats.add(new StatField("Cash", this.getCash()));
         stats.add(new StatField("ValidId", this.isIdValid()));
         stats.add(new StatField("CorruptionT", this.getCorruptionThreshold()));
         stats.add(new StatField("TookBribe", this.getTookBribe()));
         stats.add(new StatField("-------", ""));
         stats.add(new StatField("AttackPwr", this.getAttackPower()));
         stats.add(new StatField("AttackRg", this.getAttackRange()));
         stats.add(new StatField("-------", ""));
         stats.add(new StatField("MoveRange", this.getMoveRange()));
         stats.add(new StatField("VisionRange", this.getVisionRange()));
         stats.add(new StatField("-------", ""));
 
         return stats;
     }
 
     public Boolean getTookBribe() {
         return tookBribe;
     }
 
     public void setTookBribe(Boolean tookBribe) {
         this.tookBribe = tookBribe;
     }
 
     public Integer getAttackRange() {
         return attackRange;
     }
 
     public void setAttackRange(final Integer attackRange) {
         this.attackRange = attackRange;
     }
 
     public Integer getAttackPower() {
         return attackPower;
     }
 
     public void setAttackPower(final Integer attackPower) {
         this.attackPower = attackPower;
     }
 
     public Boolean isAlive() {
         return isAlive;
     }
 
     public void setAlive(Boolean alive) {
         isAlive = alive;
     }
 
     public Integer getHowManyFishes() {
         return this.howManyFishes;
     }
 
     public void setHowManyFishes(Integer howManyFishes) {
         this.howManyFishes = howManyFishes;
     }
 
     public WorldField getAtField() {
         return atField;
     }
 
     public void setAtField(final WorldField atField) {
         this.atField = atField;
     }
 
     public DefaultActor getTarget() {
         return target;
     }
 
     public void setTarget(final DefaultActor target) {
         this.target = target;
     }
 
     public Boolean getTargetHit() {
         return targetHit;
     }
 
     public void setTargetHit(final Boolean targetHit) {
         this.targetHit = targetHit;
     }
 
     public Integer getCash() {
         return cash;
     }
 
     public void setCash(final Integer cash) {
         this.cash = cash;
     }
 
     public Integer getCorruptionThreshold() {
         return corruptionThreshold;
     }
 
     public void setCorruptionThreshold(final Integer corruptionThreshold) {
         this.corruptionThreshold = corruptionThreshold;
     }
 
     public Integer getHunger() {
         return hunger;
     }
 
     public void setHunger(Integer hunger) {
         this.hunger = hunger;
     }
 
     public Integer getMoveRange() {
         return moveRange;
     }
 
     public void setMoveRange(final Integer moveRange) {
         this.moveRange = moveRange;
     }
 
     public int getWeight() {
         return this.weight;
     }
 
     public void setWeight(final int weight) {
         this.weight = weight;
     }
 
     public Boolean getAggressive() {
         return aggressive;
     }
 
     public void setAggressive(Boolean aggressive) {
         this.aggressive = aggressive;
     }
 
     public Integer getRoundsAlive() {
         return roundsAlive;
     }
 
     public Double getEffectivity_1() {
         return effectivity_1;
     }
 
     public Double getEffectivity_2() {
         return effectivity_2;
     }
 
     protected abstract LakeActors setType();
 
     protected abstract void doNormalInit(final Properties properties);
 
     protected abstract void doRandomInit(final Properties properties);
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                       .add("id", id)
                       .add("type", type)
                       .add("atField", atField)
                       .add("canAttack", canAttack)
                       .add("canFly", canFly)
                       .add("canSwim", canSwim)
                       .add("isAlive", isAlive)
                       .add("hp", hp)
                       .add("visionRange", visionRange)
                       .add("attackRange", attackRange)
                       .add("moveRange", moveRange)
                       .add("hunger", hunger)
                       .add("target", target)
                       .add("targetHit", targetHit)
                       .add("aggressive", aggressive)
                       .add("cash", cash)
                       .add("corruptionThreshold", corruptionThreshold)
                       .add("tookBribe", tookBribe)
                       .add("validId", validId)
                       .add("attackPower", attackPower)
                       .add("weight", weight)
                       .add("isMoveChanged", isMoveChanged)
                       .add("howManyFishes", howManyFishes)
                       .add("roundsAlive", roundsAlive)
                       .add("effectivity_1", effectivity_1)
                       .add("effectivity_2", effectivity_2)
                       .toString();
     }
 }

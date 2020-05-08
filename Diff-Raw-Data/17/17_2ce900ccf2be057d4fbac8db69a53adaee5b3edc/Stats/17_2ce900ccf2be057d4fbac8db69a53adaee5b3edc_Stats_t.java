 package aigilas.creatures;
 
 import sps.core.Settings;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Stats {
     public static final float DefaultMoveDistance = Settings.get().spriteHeight;
 
     private HashMap<StatType, Float> _stats = new HashMap<>();
     private final List<StatBuff> _buffs = new ArrayList<>();
 
     public Stats(Stats target) {
         _stats = new HashMap<>(target._stats);
     }
 
     public Stats(float health, float mana, float strength, float wisdom, float defense, float luck, float age, float weightInLbs, float heightInFeet, float moveCoolDown, float regenRate) {
 
         setup(health, mana, strength, wisdom, defense, luck, age, weightInLbs, heightInFeet, moveCoolDown, 0, regenRate);
     }
 
     public Stats(float health, float mana, float strength, float wisdom, float defense, float luck, float age, float weightInLbs, float heightInFeet, float moveCoolDown) {
         setup(health, mana, strength, wisdom, defense, luck, age, weightInLbs, heightInFeet, moveCoolDown, 0, Settings.get().defaultRegen);
     }
 
     public Stats(float health, float mana, float strength, float wisdom, float defense, float luck, float age, float weightInLbs, float heightInFeet) {
         setup(health, mana, strength, wisdom, defense, luck, age, weightInLbs, heightInFeet, Settings.get().defaultSpeed, 0, Settings.get().defaultRegen);
 
     }
 
     private void setup(float... list) {
         for (int ii = 0; ii < list.length; ii++) {
             _stats.put(StatType.values()[ii], list[ii]);
         }
     }
 
     private float statSum = 0;
 
     public float get(StatType stat) {
         if (_buffs != null) {
             if (!_buffs.contains(null)) {
                 statSum = 0;
                 for (StatBuff _buff : _buffs) {
                     if (_buff.Stat == stat) {
                         statSum += _buff.Amount;
                     }
                 }
                 return getRaw(stat) + statSum;
             }
         }
         return getRaw(stat);
     }
 
     public float getRaw(StatType stat) {
         return _stats.get(stat);
     }
 
     public float set(StatType stat, float value) {
         return _stats.put(stat, value);
     }
 
     public void addBuff(StatBuff buff) {
         if (_buffs.contains(buff)) {
             _buffs.remove(buff);
             return;
         }
         _buffs.add(buff);
     }
 
     private final List<Float> deltas = new ArrayList<>();
 
     public List<Float> getDeltas(Stats stats) {
         deltas.clear();
         for (StatType stat : StatType.values()) {
             deltas.add(_stats.get(stat) - stats._stats.get(stat));
         }
         return deltas;
     }
 
     public float getBonus(int level, StatType stat) {
         return _stats.get(stat) * level;
     }
 
     public float getSum() {
         float result = 0;
         for (StatType key : _stats.keySet()) {
             if (key != StatType.HEALTH && key != StatType.MOVE_COOL_DOWN && key != StatType.REGEN) {
                 result += _stats.get(key);
             }
         }
         return result;
     }
 
    String hash;
 
     @Override
     public int hashCode() {
        hash = "";
         for (StatType key : _stats.keySet()) {
            hash += _stats.get(key).toString();
         }
         return hash.hashCode();
     }
 }

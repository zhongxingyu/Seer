 //calculates damage (durr)
 public class DamageCalculator {
     private static int MIN_RANGE = 217;
     private static int MAX_RANGE = 255;
     //rangeNum should range from 217 to 255 
     //crit indicates if there is a crit or not
     private static int damage(Move attack, Pokemon attacker, Pokemon defender,
             StatModifier atkMod, StatModifier defMod, int rangeNum, boolean crit) {
         if (rangeNum < MIN_RANGE) {
             rangeNum = MIN_RANGE;
         }
         if (rangeNum > MAX_RANGE) {
             rangeNum = MAX_RANGE;
         }
         
         if(attack.getPower() <= 0) {
             //TODO: special cases
             return 0;
         }
         //stat modifiers
         int aa_orig = attacker.getTrueAtk();
         int atk_atk = atkMod.modAtk(attacker);
         int dd_orig = defender.getTrueDef();
         int def_def = defMod.modDef(defender);
         int as_orig = attacker.getTrueSpc();
         int atk_spc = atkMod.modSpc(attacker);
         int ds_orig = defender.getTrueSpc();
         int def_spc = defMod.modSpc(defender);
         
         boolean STAB = attack.getType() == attacker.getSpecies().getType1() || 
                     attack.getType() == attacker.getSpecies().getType2();
         double effectiveMult = Type.effectiveness(attack.getType(), defender.getSpecies().getType1(),
              defender.getSpecies().getType2());
        if(effectiveMult == 0) {
            return 0;
        }
      
         if(Type.isPhysicalType(attack.getType())) {
             int a =(int) (((int)((attacker.getLevel() * 0.4 * (crit ? 2 : 1)) + 2) * (crit ? aa_orig : atk_atk) * 
                    attack.getPower() / 50 / (crit ? dd_orig : def_def) + 2) *
                    (STAB ? 1.5 : 1) * effectiveMult) * rangeNum / 255;
             return Math.max(a, 1);
         } else {
             int a =(int) (((int)((attacker.getLevel() * 0.4 * (crit ? 2 : 1)) + 2) * (crit ? as_orig : atk_spc) * 
                    attack.getPower() / 50 / (crit ? ds_orig : def_spc) + 2) *
                    (STAB ? 1.5 : 1) * effectiveMult) * rangeNum / 255; 
             return Math.max(a, 1);
         }
             
     }
     public static int minDamage(Move attack, Pokemon attacker, Pokemon defender,
             StatModifier atkMod, StatModifier defMod) {
         return damage(attack, attacker, defender, atkMod, defMod, MIN_RANGE, false);
     }
     public static int maxDamage(Move attack, Pokemon attacker, Pokemon defender,
             StatModifier atkMod, StatModifier defMod) {
         return damage(attack, attacker, defender, atkMod, defMod, MAX_RANGE, false);
     }
     public static int minCritDamage(Move attack, Pokemon attacker, Pokemon defender,
             StatModifier atkMod, StatModifier defMod) {
         return damage(attack, attacker, defender, atkMod, defMod, MIN_RANGE, true);
     }
     public static int maxCritDamage(Move attack, Pokemon attacker, Pokemon defender,
             StatModifier atkMod, StatModifier defMod) {
         return damage(attack, attacker, defender, atkMod, defMod, MAX_RANGE, true);
     }
     
     //printout of move damages between the two pokemon
     //assumes you are p1
     public static String summary(Pokemon p1, Pokemon p2, BattleOptions options) {
         StringBuilder sb = new StringBuilder();
         String endl = Constants.endl;
         StatModifier mod1 = options.getMod1();
         StatModifier mod2 = options.getMod2();
         
         sb.append(p1.levelName() + " vs " + p2.levelName() + endl);
         //sb.append(String.format("EXP to next level: %d EXP gained: %d", p1.expToNextLevel(), p2.expGiven()) + endl);
         sb.append(String.format("%s (%s) ", p1.pokeName(), p1.statsStr()));       
         if(mod1.hasMods() || mod1.hasBBs()) {
             sb.append(String.format("%s -> (%s) ", mod1.summary(), mod1.modStatsStr(p1)) + endl);    
         }  else {
             sb.append(endl);
         }
         
         sb.append(summary_help(p1,p2,mod1,mod2));
         
         sb.append(endl);
         
         if(mod2.hasMods()) {
         sb.append(String.format("%s (%s) %s -> (%s): ", p2.pokeName(), p2.statsStr(),
                 mod2.summary(), mod2.modStatsStr(p2)) + endl);
         } else {
             sb.append(String.format("%s (%s): ", p2.pokeName(), p2.statsStr()) + endl);
         }
         sb.append(summary_help(p2,p1,mod2,mod1));
         
         return sb.toString();
     }
     
     //String summary of all of p1's moves used on p2
     //(would be faster if i didn't return intermediate strings)
     private static String summary_help(Pokemon p1, Pokemon p2, StatModifier mod1, StatModifier mod2) {
         StringBuilder sb = new StringBuilder();
         String endl = Constants.endl;
         
         int enemyHP = p2.getHP();
         
         for(Move m : p1.getMoveset()) {
             sb.append(m.getName() + "\t");
             //calculate damage of this move, and its percentages on opposing pokemon
             int minDmg = minDamage(m, p1, p2, mod1, mod2);
             int maxDmg = maxDamage(m, p1, p2, mod1, mod2);
             
             //don't spam if the move doesn't do damage
             //TODO: better test of damaging move, to be done when fixes are made
             if (maxDmg == 0) {
                 sb.append(endl);
                 continue;
             }
             double minPct = 100.0 * minDmg / enemyHP;
             double maxPct = 100.0 * maxDmg / enemyHP;
             sb.append(String.format("%d-%d %.02f-%.02f",minDmg,maxDmg,minPct,maxPct));
             sb.append("%\t(crit: ");
             //do it again, for crits
             int critMinDmg = minCritDamage(m, p1, p2, mod1, mod2);
             int critMaxDmg = maxCritDamage(m, p1, p2, mod1, mod2);
             
             double critMinPct = 100.0 * critMinDmg / enemyHP;
             double critMaxPct = 100.0 * critMaxDmg / enemyHP;
             sb.append(String.format("%d-%d %.02f-%.02f",critMinDmg,critMaxDmg,critMinPct,critMaxPct));
             sb.append("%)" + endl);
             
             int oppHP = p2.getHP();
             //test if noncrits can kill in 1shot
             if(maxDmg >= oppHP && minDmg < oppHP) {
                 double oneShotPct = oneShotPercentage(m, p1, p2, mod1, mod2, false);
                 sb.append(String.format("\t(One shot prob.: %.02f%%)",oneShotPct) + endl);
             }
             //test if crits can kill in 1shot
             if(critMaxDmg >= oppHP && critMinDmg < oppHP) {
                 double oneShotPct = oneShotPercentage(m, p1, p2, mod1, mod2, true);
                 sb.append(String.format("\t(Crit one shot prob.: %.02f%%)",oneShotPct) + endl);
             }
             
         }
         
         return sb.toString();
     }
     
     //used for the less verbose option
     public static String shortSummary(Pokemon p1, Pokemon p2, BattleOptions options) {
         StringBuilder sb = new StringBuilder();
         String endl = Constants.endl;
         
         StatModifier mod1 = options.getMod1();
         StatModifier mod2 = options.getMod2();
         
         sb.append(p1.levelName() + " vs " + p2.levelName() + endl);
         //sb.append(String.format("EXP to next level: %d EXP gained: %d", p1.expToNextLevel(), p2.expGiven()) + endl);
         sb.append(String.format("%s (%s) ", p1.pokeName(), p1.statsStr()));       
         if(mod1.hasMods() || mod1.hasBBs()) {
             sb.append(String.format("%s -> (%s) ", mod1.summary(), mod1.modStatsStr(p1)) + endl);    
         }  else {
             sb.append(endl);
         }
         
         sb.append(summary_help(p1,p2,mod1,mod2) + endl);
         if(mod2.hasMods()) {
             sb.append(String.format("%s (%s) %s -> (%s): ", p2.pokeName(), p2.statsStr(),
                     mod2.summary(), mod2.modStatsStr(p2)));
             } else {
                 sb.append(String.format("%s (%s): ", p2.pokeName(), p2.statsStr()));
             }
             
         sb.append(" " + p2.getMoveset().toString() + endl);
         return sb.toString();
     }
     
     private static double oneShotPercentage(Move attack, Pokemon attacker, Pokemon defender,
             StatModifier atkMod, StatModifier defMod, boolean crit) {
         //iterate until damage is big enough
         int rangeNum = MIN_RANGE;
         while(damage(attack, attacker, defender, atkMod, defMod, rangeNum, crit) < defender.getHP()) {
             rangeNum++;
         }
         return 100.0 * (MAX_RANGE - rangeNum + 1) / (MAX_RANGE - MIN_RANGE + 1);
     }
 }

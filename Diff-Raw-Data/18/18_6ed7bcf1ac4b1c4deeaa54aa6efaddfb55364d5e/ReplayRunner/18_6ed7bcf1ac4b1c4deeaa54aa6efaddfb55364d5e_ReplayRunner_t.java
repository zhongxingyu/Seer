 package com.battlespace.main;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.battlespace.domain.AttackOptions;
 import com.battlespace.domain.Booster;
 import com.battlespace.domain.CommanderPower;
 import com.battlespace.domain.Coordinate;
 import com.battlespace.domain.DamageEntry;
 import com.battlespace.domain.Deployment;
 import com.battlespace.domain.EnemyShip;
 import com.battlespace.domain.EnemyShipDatabase;
 import com.battlespace.domain.EnemyShipInstance;
 import com.battlespace.domain.FileData;
 import com.battlespace.domain.Formation;
 import com.battlespace.domain.PlayerShip;
 import com.battlespace.domain.PlayerShipInstance;
 import com.battlespace.domain.RangedStat;
 import com.battlespace.domain.Ship;
 import com.battlespace.domain.ShipInstance;
 import com.battlespace.domain.Stat;
 import com.battlespace.service.CommanderPowerService;
 import com.battlespace.service.DataLoaderService;
 import com.battlespace.service.FormationService;
 import com.battlespace.service.PlayerShipDatabase;
 import com.battlespace.service.PlayerSkillModifier;
 import com.battlespace.service.StatFactory;
 
 public class ReplayRunner
 {
 
     /**
      * @param args
      */
     public static void main(String[] args) throws Exception
     {
         FileData config = DataLoaderService.loadFile("conf/settings.txt");
         EnemyShipDatabase esd = EnemyShipDatabase.load();
         
         //String replayFile = "data/replays/" + args[0];
        String replayFile = "data/replays/" + "dvthalla_1.txt";
         
         FileData replay = DataLoaderService.loadFile(replayFile);
 
         int militarySkill = replay.getInt("attacker", 0);
         int commanderSkill = replay.getInt("attacker", 1);
         double commanderBoost = commanderSkill/Double.valueOf(config.get("skill_factor"));
         int commanderLuck = replay.getInt("attacker", 2);
         String commanderPowerName = replay.get("attacker", 3);
         CommanderPower commanderPower = CommanderPowerService.get(commanderPowerName);
         
         String attackFormationName = replay.get("attacker",4);
         
         Formation attackFormation = FormationService.get(attackFormationName);
         
         List<String> attackShipNames = replay.getList("attacker_ships");
         List<String> attackShipUpgrades = replay.getList("attacker_upgrades");
         Map<String, int[]> upgradeLevels = new HashMap<String, int[]>();
         for(String attackShipUpgrade : attackShipUpgrades)
         {
             String[] parts = attackShipUpgrade.split(":");
             int[] ul = new int[4];
             for(int i=0;i<4;i++)
             {
                 ul[i] = parts[1].codePointAt(i) - '0';
             }
             upgradeLevels.put(parts[0], ul);
         }
 
         List<PlayerShipInstance> attackShips = new LinkedList<PlayerShipInstance>();
         
         for(String attackShipName : attackShipNames)
         {
             PlayerShip psi = PlayerShipDatabase.lookup(attackShipName);
             //System.out.println(psi);
             
             // apply ship upgrades
             psi = psi.applyUpgrades( upgradeLevels.get(attackShipName) );
             //System.out.println(psi);
 
             // calculate boost percentages
             Booster booster = new Booster(psi.size);
             
             PlayerSkillModifier.upgrade(booster, militarySkill);
             commanderPower.upgrade(booster);
             booster.skillUpgrade(commanderBoost);
             //System.out.println(booster);
             
             psi = psi.applyBooster(booster);
             //System.out.println(psi);
             attackShips.add(psi.createInstance());
         }
         Deployment attackDeployment = attackFormation.deploy(attackShips);
 
         String defendFormationName = replay.get("defender");
         List<String> defendShipNames = replay.getList("defender_ships");
         
         List<EnemyShipInstance> defendShips = new LinkedList<EnemyShipInstance>();
         for(String defendShipName : defendShipNames)
         {
             defendShips.add(esd.instantiate(defendShipName));
         }
         Formation defendFormation = FormationService.get(defendFormationName);
         Deployment defendDeployment = defendFormation.deploy(defendShips);
         
         // attempt to load the battle stats.1.attacker, stats.1.defender, damage.1.attacker, damage.1.defender
         for(int turn=1;;turn++)
         {
             List<String> attackStats = replay.getList("stats."+turn+".attacker");
             if(attackStats == null)
             {
                 break;
             }
             // allow for display rounding and make sure they're compatible with our calculations
             List<RangedStat> displayRange = RangedStat.statsFromDisplay(attackStats);
             List<Stat> realRange = attackDeployment.getStats();
             
             //System.out.println(displayRange);
             //System.out.println(realRange);
             
             List<Stat> extractedAttackStats = RangedStat.merge(displayRange, realRange);
             
             // How to analyze defend stats. Obviously we need to do a display breakdown
             List<String> defendStats = replay.getList("stats."+turn+".defender");
             displayRange = RangedStat.statsFromDisplay(defendStats);
             
             // now the trick is as follows. Summarize deployment as a map of
             // ship name to effective ship count.
             // Then for each ship type, min is given by back calculation (assuming all other ships maxed)
             // and likewise for max
             Map<String, Double> deploymentSummary = defendDeployment.getSummary();
             Set<String> enemyNames = deploymentSummary.keySet();
             for(String treatFixed : enemyNames)
             {
                 // sum the raw stats from the remaining ships
                 List<Stat> others = RangedStat.createEmptyStats();
                 for(String otherShipName : enemyNames)
                 {
                     if(otherShipName.equals(treatFixed)) continue;
                     EnemyShip otherShip = esd.lookup(otherShipName);
                     List<Stat> stats = otherShip.getSummaryStats();
                     List<Stat> scaled = RangedStat.scale(stats, deploymentSummary.get(otherShipName));
                     others = RangedStat.sum(others, scaled);
                 }
                 // display range sub others what our ship might be.
                 List<Stat> fixedStats = RangedStat.diff(displayRange, others);
                 List<Stat> newStatEstimate = RangedStat.scale(fixedStats, 1.0 / deploymentSummary.get(treatFixed));
                 EnemyShip fixedShip = esd.lookup(treatFixed);
                 esd.refineSummaryStats(fixedShip, newStatEstimate);
             }
             
             List<String> attackDamage = replay.getList("damage."+turn+".attacker");
             List<DamageEntry> ad = DamageEntry.parseDamage(attackDamage, attackDeployment);
             
             List<String> defendDamage = replay.getList("damage."+turn+".defender");
             List<DamageEntry> dd = DamageEntry.parseDamage(defendDamage, defendDeployment);
             
             // based on defender damage, update estimates of defender shields.
             // Method: establish attack vectors, for each attacking ship, work out
             // which ships are possibly targeted. Note that's a max of 4 per attacker
             // so we have a max of 4^n permutations.
             Map<Coordinate, AttackOptions> attackVectors = attackDeployment.getAttackVectors(defendDeployment);
             //System.out.println(attackVectors);
             // DON'T APPLY ATTACK FUZZ HERE
             List< Map<Coordinate,Coordinate> > allAttackCombos = new LinkedList< Map<Coordinate,Coordinate> >();
             allAttackCombos.add(new HashMap<Coordinate, Coordinate>());    // start with an empty hash map
             for(Map.Entry<Coordinate, AttackOptions> e : attackVectors.entrySet() )
             {
                 Coordinate k = e.getKey();
                 AttackOptions v = e.getValue();     // boring boring
                 
                 List< Map<Coordinate,Coordinate> > rewrite = new LinkedList< Map<Coordinate,Coordinate> >();
                 Collection<Coordinate> allTargets = v.getAllTargets();
                 for(Coordinate target : allTargets)
                 {
                     for(Map<Coordinate,Coordinate> attackCombo : allAttackCombos)
                     {
                         Map<Coordinate,Coordinate> dup = new HashMap<Coordinate, Coordinate>();
                         dup.putAll(attackCombo);
                         dup.put(k, target);
                         rewrite.add(dup);
                     }
                 }
                 allAttackCombos = rewrite;
             }
            System.out.println("Attack permutations: " + allAttackCombos.size());
        
             // The idea now is to play out these attacks and see which match the dd DamageEntry lists.
             // Remember if a defender is totally obliterated, not all hits may have registered
             int fl = defendDeployment.frontLine();
             // we need to find exactly one attack that matches
             List< Map<Coordinate, Coordinate>> matchingAttacks = new LinkedList<Map<Coordinate, Coordinate>>();
             for(Map<Coordinate, Coordinate> attackCombo : allAttackCombos)
             {
                 // flip the map into the form (defender)-(list all attackers)
                 Map<Coordinate, List<Coordinate>> attackersPerDefender = invertAttackers(attackCombo);
                 
                 boolean match = true;
                 for(int i=0;match && (i<10);i++)
                 {
                     int r = i%5;
                     int c = fl + ((i>=5)?1:0);          // defender location
                     DamageEntry de = dd.get(i);
                     Coordinate dl = new Coordinate(r,c);
                     List<Coordinate> targeting = attackersPerDefender.get(dl);
                     // simple match fails, no entry, but attackers, or entry with no attackers
                     if(de==null)
                     {
                         if(targeting!=null) match=false;
                     }
                     else
                     {
                         if(targeting==null) match=false;
                         else
                         {
                             // damage is done, and attackers exist. We must make sure the listed attackers can effect the
                             // listed amount of damage. Note if it's a wipeout then we may not need all the attackers,
                             // just a subset of them. (2^n-1 possible subsets). Crap.
                             if(de.remainingShips == 0)
                             {
                                 List< List<Coordinate> > es = findEliminatingSubsets(attackDeployment, defendDeployment, targeting, dl, de);
                                 match = (es.size() > 0);    // anything will do
                             }
                             else
                             {
                                 match = isCompatibleAttack(attackDeployment, defendDeployment, targeting, dl, de);
                             }
                         }
                     }
                             
                 }
                 
                 if(match)
                 {
                     matchingAttacks.add(attackCombo);
                 }
             }
            System.out.println("Matching attack configurations " + matchingAttacks.size());
             
             if(matchingAttacks.size()==1)
             {
                 Map<Coordinate, Coordinate> attackCombo = matchingAttacks.get(0);
                 // now do it all again, this time to compute enemy shield effectiveness.
                 // note you may need to test the subsets harder this time.
                 Map<Coordinate, List<Coordinate>> attackersPerDefender = invertAttackers(attackCombo);
                 // fl is still the defender front line
                 for(Map.Entry<Coordinate, List<Coordinate>> e : attackersPerDefender.entrySet())
                 {
                     Coordinate defenderPos = e.getKey();
                     List<Coordinate> attackerPos = e.getValue();
                     DamageEntry de = dd.get(defenderPos.r + ((defenderPos.c==fl)?0:5) );
                     // special handling of a wipeout
                     if(de.remainingShips==0)
                     {
                         List< List<Coordinate> > es = findEliminatingSubsets(attackDeployment, defendDeployment, attackerPos, defenderPos, de);
                         //System.out.println("Uncertain: " + es.size());
                         if(es.size()==1)
                         {
                             adjustShields(attackDeployment, defendDeployment, es.get(0), defenderPos, de, esd);
                         }
                     }
                     else
                     {
                         adjustShields(attackDeployment, defendDeployment, attackerPos, defenderPos, de, esd);
                     }
                 }
             }
             
             // update damage and ship counts
             int afl = attackDeployment.frontLine();
             for(int i=0;i<10;i++)
             {
                 attackDeployment.updateDamage(i%5, afl+((i>=5)?1:0), ad.get(i));
             }
             int dfl = defendDeployment.frontLine();
             for(int i=0;i<10;i++)
             {
                 defendDeployment.updateDamage(i%5, dfl+((i>=5)?1:0), dd.get(i));
             }
             // use ship count to refine durability estimates
             Collection<ShipInstance> defenders = defendDeployment.getAllShips();
             for(ShipInstance defender : defenders)
             {
                 EnemyShip nme = (EnemyShip)defender.getParent();
                 int total = nme.getUnits();
                 int current = defender.getUnits();
                 int removed = total - current;
                 Stat damage = defender.getDamage();
                 // estimate durability based on damage data
                 // removed = damage/durability * total, rounded down
                 // so removed <= damage/durability * total < removed+1
                 // in other words durability <= damage*total/removed
                 // and durability > damage*total/(removed+1)
                 
                 // of course, this is wrong for overkill :)
                 double min = 0;
                 if(current > 0) // you can't set a min if you wiped them out
                 {
                     min = (damage.value(false)*total) / (removed+1);
                 }
                 double max = 10000.0;
                 if(removed>0) // you can't know a max if you havent hurt anyone yet
                 {
                     max = (damage.value(true) * total) / removed;
                 }
                 Stat refined = StatFactory.create(min, max);
                 esd.refineDurability(nme, refined);
             }
         }
         
         esd.update();
     }
 
     private static List<List<Coordinate>> findEliminatingSubsets(
             Deployment attackDeployment, Deployment defendDeployment,
             List<Coordinate> targeting, Coordinate dl, DamageEntry de) throws Exception
     {
         List< List<Coordinate> > out = new LinkedList< List<Coordinate> >();
         for(int t=1; (t< (1<<targeting.size()) ); t++)
         {
             // use the bitmask for t to build a sublist
             List<Coordinate> sl = new LinkedList<Coordinate>();
             for(int b=0; b<targeting.size(); b++)
             {
                 if( (t&(1<<b)) != 0)
                 {
                     sl.add(targeting.get(b));
                 }
             }
             if( isCompatibleAttack(attackDeployment, defendDeployment, sl, dl, de) )
             {
                 out.add(sl);
             }
         }
         return out;
     }
 
     private static Map<Coordinate, List<Coordinate>> invertAttackers(
             Map<Coordinate, Coordinate> attackCombo)
     {
         Map<Coordinate, List<Coordinate>> attackersPerDefender = new HashMap<Coordinate, List<Coordinate>>();
         for(Map.Entry<Coordinate, Coordinate> e : attackCombo.entrySet())
         {
             Coordinate attacker = e.getKey();
             Coordinate defender = e.getValue();
             List<Coordinate> list = attackersPerDefender.get(defender);
             if(list == null)
             {
                 list = new LinkedList<Coordinate>();
                 attackersPerDefender.put(defender, list);
             }
             list.add(attacker);
         }
         return attackersPerDefender;
     }
 
     private static void adjustShields(Deployment attackDeployment,
             Deployment defendDeployment, List<Coordinate> al, Coordinate dl,
             DamageEntry de, EnemyShipDatabase esd) throws Exception
     {
         // find out whether it's possible the list of attackers at al can do the suggested damage to the defender at dl
         Stat torpedoDamage = StatFactory.create(0.0, 0.0);
         Stat plasmaDamage = StatFactory.create(0.0, 0.0);
         // get the defending ship
         ShipInstance si = defendDeployment.getLivingShip(dl.r, dl.c);
         EnemyShip ss = (EnemyShip)si.getParent();
         String size = ss.getSize();
         
         double critmul = de.critical ? 1.30 : 1.00;
         
         for(Coordinate a : al)
         {
             ShipInstance ai = attackDeployment.getLivingShip(a.r, a.c);
             Ship as = ai.getParent();
             double c = ai.getEffectiveCount();  // scalar for damage inflicted
             
             Stat td = as.getTorpedoDamage(size, c*critmul);
             Stat pd = as.getPlasmaDamage(size, c*critmul);
             
             torpedoDamage = RangedStat.sum2(torpedoDamage, td);
             plasmaDamage = RangedStat.sum2(plasmaDamage, pd);
         }
         
         // calculate min and max potential damage
         List<Stat> shields = ss.getShieldStats();
         Stat ts = shields.get(0);
         Stat ps = shields.get(1);
     
         // this is the data we've got to make match
         //System.out.println("TD " + torpedoDamage);
         //System.out.println("PD " + plasmaDamage);
         //System.out.println("SH " + shields);
         //System.out.println("DA " + de);
         
         // find the min and max damage of each type possible (assume min/max damage of other type)
         double minTD = de.damage.value(false) - shieldedDamage( plasmaDamage.value(true), ps.value(false) );    // sub max plasma, min shield
         if(minTD<0.0) minTD = 0.0;
         double maxTD = de.damage.value(true) - shieldedDamage( plasmaDamage.value(false), ps.value(true) );     // sub min plasma, max shield
         if(maxTD>torpedoDamage.value(true)) maxTD=torpedoDamage.value(true);
         
         double minPD = de.damage.value(false) - shieldedDamage( torpedoDamage.value(true), ts.value(false) );    // sub max plasma, min shield
         if(minPD<0.0) minPD = 0.0;
         double maxPD = de.damage.value(true) - shieldedDamage( torpedoDamage.value(false), ts.value(true) );     // sub min plasma, max shield
         if(maxPD>plasmaDamage.value(true)) maxPD=plasmaDamage.value(true);
         
         //System.out.println("TDX " + minTD + " " + maxTD);
         //System.out.println("PDX " + minPD + " " + maxPD);
         
         // basic formula
         // X = D * R (X result, D damage, R ratio).
         // so ratio is simply X/D (x awarded d delivered)
         double minTR = 0.0;
         double maxTR = 1.0;
         double minPR = 0.0;
         double maxPR = 1.0;
         
         if(torpedoDamage.value(true)>0.0) minTR = minTD / torpedoDamage.value(true);
         if(torpedoDamage.value(false)>0.0) maxTR = maxTD / torpedoDamage.value(false);
         if(plasmaDamage.value(true)>0.0) minPR = minPD / plasmaDamage.value(true);
         if(plasmaDamage.value(false)>0.0) maxPR = maxPD / plasmaDamage.value(false);
 
         //System.out.println("TDR " + minTR + " " + maxTR);
         //System.out.println("PDR " + minPR + " " + maxPR);
 
         List<Stat> shieldRange = new LinkedList<Stat>();
         shieldRange.add(StatFactory.create(1000.0*(1.0-maxTR), 1000.0*(1.0-minTR)));
         shieldRange.add(StatFactory.create(1000.0*(1.0-maxPR), 1000.0*(1.0-minPR)));
         
         esd.refineShields(ss, shieldRange);
     }
     
     private static boolean isCompatibleAttack(Deployment attackDeployment,
             Deployment defendDeployment, List<Coordinate> al, Coordinate dl,
             DamageEntry de) throws Exception
     {
         // find out whether it's possible the list of attackers at al can do the suggested damage to the defender at dl
         Stat torpedoDamage = StatFactory.create(0.0, 0.0);
         Stat plasmaDamage = StatFactory.create(0.0, 0.0);
         // get the defending ship
         ShipInstance si = defendDeployment.getLivingShip(dl.r, dl.c);
         Ship ss = si.getParent();
         String size = ss.getSize();
         
         double critmul = de.critical ? 1.30 : 1.00;
         
         for(Coordinate a : al)
         {
             ShipInstance ai = attackDeployment.getLivingShip(a.r, a.c);
             Ship as = ai.getParent();
             double c = ai.getEffectiveCount();  // scalar for damage inflicted
             
             Stat td = as.getTorpedoDamage(size, c*critmul);
             Stat pd = as.getPlasmaDamage(size, c*critmul);
             
             torpedoDamage = RangedStat.sum2(torpedoDamage, td);
             plasmaDamage = RangedStat.sum2(plasmaDamage, pd);
         }
         
         // calculate min and max potential damage
         List<Stat> shields = ss.getShieldStats();
         
         // max damage is when both shields are at minimum and torpedo, plasma are maxed
         double mind = shieldedDamage(torpedoDamage.value(false), shields.get(0).value(true))
                 +shieldedDamage(plasmaDamage.value(false), shields.get(1).value(true));
         double maxd = shieldedDamage(torpedoDamage.value(true), shields.get(0).value(false))
                 +shieldedDamage(plasmaDamage.value(true), shields.get(1).value(false));
         
         // check that's compatible with de
         double minm = de.damage.value(false);
         double maxm = de.damage.value(true);
         
         // check for nonempty intersection
         if(minm>mind) mind=minm;
         if(maxm<maxd) maxd=maxm;
         return (mind<=maxd);
     }
 
     private static double shieldedDamage(double damage, double shield)
     {
         if(shield<=0) return damage;
         if(shield>=1000) return 0.0;
         return damage * (1000.0-shield)/1000.0;
     }
 }

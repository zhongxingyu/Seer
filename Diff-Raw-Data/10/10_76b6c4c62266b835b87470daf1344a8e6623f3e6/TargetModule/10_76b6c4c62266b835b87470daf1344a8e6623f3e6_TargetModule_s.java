 package autoeq.modules.target;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import autoeq.ExpressionEvaluator;
 import autoeq.TargetPattern;
 import autoeq.ThreadScoped;
 import autoeq.commandline.CommandLineParser;
 import autoeq.eq.Command;
 import autoeq.eq.Condition;
 import autoeq.eq.EverquestSession;
 import autoeq.eq.ExpressionRoot;
 import autoeq.eq.Me;
 import autoeq.eq.Module;
 import autoeq.eq.Spawn;
 import autoeq.eq.UserCommand;
 import autoeq.ini.Section;
 import autoeq.modules.camp.CampModule;
 import autoeq.modules.target.TargetConf.Mode;
 
 import com.google.inject.Inject;
 
 @ThreadScoped
 public class TargetModule implements Module {
   private final EverquestSession session;
 
   private final String validTargets;
   private final List<String> conditions;
   private final List<String> assistConditions;
 
   private TargetConf conf = new TargetConf();
   private boolean attacking;
   private String[] names;
 
   private final CampModule campModule;
 
   @Inject
   public TargetModule(final EverquestSession session, CampModule campModule) {
     this.session = session;
     this.campModule = campModule;
 
     Section section = session.getIni().getSection("Assist");
 
     if(section == null) {
       section = session.getIni().getSection("Target");
     }
 
     if(section.get("Active") != null) {
       System.err.println("WARN: Target/Active is deprecated, set Mode to 'off' if you donot want targetting");
     }
 
     conf.setMode(Mode.valueOf(section.getDefault("Mode", "assist").toUpperCase()));
 
     validTargets = section.getDefault("ValidTargets", "war pal shd mnk rog ber rng bst brd clr shm dru enc mag nec wiz pet");
     conditions = section.getAll("Condition");
     assistConditions = section.getAll("AssistCondition");
     names = section.getDefault("Names", "").split(",");
 
     session.addUserCommand("target", Pattern.compile(".+"), CommandLineParser.getHelpString(TargetConf.class), new UserCommand() {
       @Override
       public void onCommand(Matcher matcher) {
         TargetConf newConf = new TargetConf(conf);
 
         CommandLineParser.parse(newConf, matcher.group(0));
 
         conf = newConf;
 
         session.echo("==> Target acquirement is " + (conf.getMode() == Mode.OFF ? "off" : "on") + ".  Mode is: " + conf.getMode() + ".  Range is " + conf.getRange() + ".");
       }
     });
   }
 
   @Override
   public List<Command> pulse() {
     return null;
   }
 
   private boolean isValidTarget(Spawn spawn) {
     if(spawn.isEnemy()) {
       if(TargetPattern.isValidTarget(validTargets, spawn)) {
         if(ExpressionEvaluator.evaluate(conditions, new ExpressionRoot(session, spawn, null, null, null), this)) {
           if(conf.getMode() != Mode.ASSIST || ExpressionEvaluator.evaluate(assistConditions, new ExpressionRoot(session, spawn, null, null, null), this)) {
             return true;
           }
         }
       }
     }
 
     return false;
   }
 
   @Override
   public boolean isLowLatency() {
     return false;
   }
 
   private Spawn currentTarget;
 
   public Spawn getMainAssistTarget() {
     Spawn bestTarget = currentTarget;
 
     if(conf.getMode() != Mode.OFF) {
       Me me = session.getMe();
       boolean campSet = false;
       float x = me.getX();
       float y = me.getY();
 
       if(campModule != null) {
         campSet = campModule.isCampSet();
         if(campSet) {
           x = campModule.getCampX();
           y = campModule.getCampY();
         }
       }
 
       if(conf.getMode() == Mode.ASSIST) {
         Spawn target = getFromAssist();
 
         if(target != null) {
           if(Math.abs(target.getZ() - me.getZ()) < 30 && target.getDistance(x, y) < conf.getRange()) {
             if(isValidTarget(target)) {
               bestTarget = target;
             }
           }
         }
       }
       else {
         if(bestTarget != null) {
           bestTarget = session.getSpawn(bestTarget.getId());
         }
 
         if(bestTarget != null && (!bestTarget.isEnemy() || (bestTarget.getDistance() > conf.getRange() && !me.isExtendedTarget(bestTarget)))) {
           bestTarget = null;
         }
 
         if(bestTarget == null) {
           // session.echo("TARGET: Mode = " + targetSelection + ":" + this);
 
           if(conf.getMode() == Mode.OLDEST) {
             for(Spawn target : session.getSpawns()) {
               if(!target.isIgnored() && Math.abs(target.getZ() - me.getZ()) < 30 && target.getDistance(x, y) < conf.getRange()) {
                 if(isValidTarget(target) && (bestTarget == null || target.getCreationDate().before(bestTarget.getCreationDate()))) {
                   bestTarget = target;
                 }
               }
             }
 
             if(bestTarget != null) {
               session.echo("TARGET: Targetting Oldest [ " + bestTarget.getName() + " ]. " + (System.currentTimeMillis() - bestTarget.getCreationDate().getTime()) / 1000 + "s old.  attacking = " + attacking + " ; melee status = " + me.getMeleeStatus());
             }
           }
           else if(conf.getMode() == Mode.HIGHEST_LEVEL) {
             for(Spawn target : session.getSpawns()) {
               if((!target.isIgnored() && Math.abs(target.getZ() - me.getZ()) < 30 && target.getDistance(x, y) < conf.getRange()) || me.isExtendedTarget(target)) {
                 if(isValidTarget(target) && (bestTarget == null || target.getLevel() > bestTarget.getLevel())) {
                   bestTarget = target;
                 }
               }
             }
 
             if(bestTarget != null) {
               session.echo("TARGET: Targetting Highest [ " + bestTarget.getName() + " ].  Level " + bestTarget.getLevel() + ".");
             }
           }
           else if(conf.getMode() == Mode.SMART) {
             int bestScore = 0;
 
             for(Spawn target : session.getSpawns()) {
               if((!target.isIgnored() && Math.abs(target.getZ() - me.getZ()) < 30 && target.getDistance(x, y) < conf.getRange()) || me.isExtendedTarget(target)) {
                 // First unmezzables, then named, then by level
                 int score = target.getLevel() + (target.isUnmezzable() ? 1000 : 0) + (target.isNamedMob() ? 500 : 0);
 
                 if(isValidTarget(target) && (bestTarget == null || score > bestScore)) {
                   bestTarget = target;
                   bestScore = score;
                 }
               }
             }
 
             if(bestTarget != null) {
               session.echo("TARGET: Targetting [ " + bestTarget.getName() + " ].  Level " + bestTarget.getLevel() + (bestTarget.isUnmezzable() ? ", Unmezzable" : "") + (bestTarget.isNamedMob() ? ", Named" : "") + ".");
             }
           }
           else if(conf.getMode() == Mode.NEAREST) {  // nearest
             for(Spawn target : session.getSpawns()) {
               if(!target.isIgnored() && Math.abs(target.getZ() - me.getZ()) < 30 && target.getDistance(x, y) < conf.getRange()) {
                 if(isValidTarget(target) && (bestTarget == null || bestTarget.getDistance(x, y) > target.getDistance(x, y))) {
                   bestTarget = target;
                 }
               }
             }
 
             if(bestTarget != null) {
               session.echo("TARGET: Targetting Nearest [ " + bestTarget.getName() + " ].  Level " + bestTarget.getLevel() + ".");
             }
           }
           else {
             throw new IllegalStateException("Unknown targetSelection: " + conf.getMode());
           }
 
           currentTarget = bestTarget;
 
           if(currentTarget != null) {
             session.doCommand("/target id " + currentTarget.getId());
           }
         }
 
         if(bestTarget != null) {
           bestTarget.increaseTankTime(2000000);  // We're assuming that when we are picking targets, we also tank them.  This may change in the future.
         }
       }
     }
 
     return bestTarget;
   }
 
   private long lastAgroMillis;
   private long lastAssistMillis;
   private Spawn lastMainTarget;
   private Spawn lastAgroTarget;
 
   public Spawn getMainAssist() {
     if(conf.getMode() == Mode.ASSIST) {
       for(String mainAssist : names) {
         Spawn spawn = session.getSpawn(mainAssist);
 
         if(spawn != null && spawn.getDistance() < 150) {
           return spawn;
         }
       }
     }
 
     return session.getMe();
   }
 
   public Spawn getFromAssist() {
    boolean haveBots = false;

     Spawn mainAssist = getMainAssist();
     Spawn target = null;
 
     if(session.getBots().contains(mainAssist)) {
       target = mainAssist.getTarget();
      haveBots = true;
     }
    else if(lastAssistMillis + 5000 < System.currentTimeMillis() && !haveBots) {
       boolean mobNearby = false;
 
       for(Spawn nearbySpawn : session.getSpawns()) {
         if(nearbySpawn.isEnemy() && nearbySpawn.getDistance() < 100 && nearbySpawn.inLineOfSight()) {
           //System.err.println("assisting cause of : " + nearbySpawn);
           mobNearby = true;
           break;
         }
       }
 
      if(mobNearby) {
         final String currentTarget = session.translate("${Target.ID}");
 
         lastAssistMillis = System.currentTimeMillis();
 
         session.doCommand("/assist " + mainAssist.getName());
         session.delay(500, new Condition() {
           @Override
           public boolean isValid() {
             return !session.translate("${Target.ID}").equals(currentTarget);
           }
         });
 
         // System.out.println("Assisting no bot (" + mainAssist + ", bots = " + session.getBots() + ")");
         session.log("Assisting no bot (" + mainAssist.getName() + ", bots = " + session.getBots() + ")");
 
         String result = session.translate("${Target.ID}");
         target = result.equals("NULL") ? null : session.getSpawn(Integer.parseInt(result));
       }
     }
     else {
       target = lastMainTarget;
     }
 
     if(target != null && target.isEnemy()) {
 
       /*
        * Update agro
        */
 
       if(mainAssist.isFacing(target, 45)) {
         if(target.equals(lastAgroTarget)) {
           long timeSpentAgroing = System.currentTimeMillis() - lastAgroMillis;
           target.increaseTankTime(timeSpentAgroing);
         }
 
         lastAgroTarget = target;
         lastAgroMillis = System.currentTimeMillis();
       }
       else {
         lastAgroTarget = null;
       }
 
       /*
        * Return target
        */
 
       lastMainTarget = target;
       return target;
     }
 
     return null;
   }
 }

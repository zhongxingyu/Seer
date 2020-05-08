 package colloid.model.combat;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import colloid.model.combat.CombatEntity;
 
 public abstract class Effect extends CombatEntity {
 
     public Effect(String log) {
         super(log);
     }
 
     public abstract boolean isEnterCombat();
     public abstract boolean isExitCombat();
     public abstract boolean isDamage();
     public abstract boolean isHeal();
    static final Pattern pattern = Pattern.compile(".*\\((\\d+)(.*)");
 
     public double getValue() {
        return parseValue(logData);
     }
 
     public static double parseValue(String data) {
         double result = 0;
 
         try {
            Matcher matcher = pattern.matcher(data.trim());
             if (matcher.matches()) {
                 String firstGroup = matcher.group(1);
                 result = Double.parseDouble(firstGroup);
             }
         } catch (NumberFormatException ex) {
             System.out.println(ex);
         } catch (IllegalStateException ex) {
             System.out.println(ex);
         }
 
         return result;
     }
 
 }

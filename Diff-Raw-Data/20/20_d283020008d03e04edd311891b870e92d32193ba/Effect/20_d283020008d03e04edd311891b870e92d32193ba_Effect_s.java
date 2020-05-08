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
 
     public double getValue() {
        value = parseValue(logData);

        return value;
     }
 
     public static double parseValue(String data) {
         double result = 0;
 
         try {
            Pattern pattern = Pattern.compile(".*\\((\\d+)(.*)");
            Matcher matcher = pattern.matcher(data);
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

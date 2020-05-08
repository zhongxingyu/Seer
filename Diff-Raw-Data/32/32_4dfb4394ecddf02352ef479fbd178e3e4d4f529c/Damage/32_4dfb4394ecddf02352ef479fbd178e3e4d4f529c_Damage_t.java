 package net.sozinsoft.tokenlab;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Damage {
 
 
     public static Matcher getDamageRegexMacher(String expression) {
         return damageRegex.matcher(expression);
     }
 
     private int numDamageDice = 0;
     private int damageDice = 0;
     private int bonusDamage = 0;
     private String damageType = "";
 
     public int getBonusDamage() {
         return bonusDamage;
     }
 
     public int getDamageDice() {
         return damageDice;
     }
 
     public int getNumDamageDice() {
         return numDamageDice;
     }
 
     public String getDamageType() {
         return damageType;
     }
 
 
     public String asExpression() {
         return Integer.toString(numDamageDice) + "d" + Integer.toString(damageDice);
     }
 
     private static Pattern damageRegex =  Pattern.compile("(\\d*)d*(\\d*)\\s*\\+*\\s*(-*\\d+)\\s*(\\w*)");
     private static Pattern bonusDamageOnly = Pattern.compile( "^(\\d+)$");
     private static Pattern noBonusDamage = Pattern.compile("^(\\d+)d(\\d+)$");
 
     public Damage(String expression) throws Exception {
 
         Matcher matcher = bonusDamageOnly.matcher(expression); //"6"
 


         if ( expression.startsWith("--") ) {
             //there are some herolabs exports that contain this odd string for damage
             //for example, the advanced wasp swarm.
             //assume  this means just zero damage.
             ;
         }
        else if ( expression.matches("^[a-zA-Z]+$") ) { //"Rust Monster"
            ;
        }

         else if (matcher.matches() ) {
             bonusDamage = Integer.parseInt( matcher.group(0));
         }
         else { //1d6
             matcher = noBonusDamage.matcher(expression);
             if ( matcher.matches()) {
                 numDamageDice = Integer.parseInt( matcher.group(1  ) );
                 damageDice    = Integer.parseInt( matcher.group( 2 ) );
             }
             else {  //1d6+3 Fire
                 matcher = getDamageRegexMacher(expression);
                 if (matcher.matches()) {
                     String ndd = matcher.group(1);
                     String dd = matcher.group(2);
                     String bd = matcher.group(3);
                     String dt = matcher.group(4);
                     if (ndd != null && !ndd.isEmpty()) {
                         numDamageDice = Integer.parseInt(ndd);
                     }
                     if (dd != null && !dd.isEmpty()) {
                         damageDice = Integer.parseInt(dd);
                     }
                     if (bd != null && !bd.isEmpty()) {
                         bonusDamage = Integer.parseInt(bd);
                     }
                     if (dt != null && !dt.isEmpty()) {
                         damageType = dt;
                     }
                 }
                 else {
                     throw new Exception("Unable to parse damage expression " + expression);
                 }
             }
         }
     }
 
 }

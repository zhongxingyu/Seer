 package ca.q0r.msocial.vars;
 
 import ca.q0r.mchat.configs.config.ConfigType;
 import ca.q0r.mchat.configs.locale.LocaleType;
 import ca.q0r.mchat.variables.Var;
 import ca.q0r.mchat.variables.VariableManager;
 import ca.q0r.msocial.MSocial;
 import org.bukkit.entity.Player;
 
 public class MSocialVars {
     public static void addVars() {
         VariableManager.addVar(new DistanceType());
     }
 
     private static class DistanceType extends Var {
         @Var.Keys( keys = {"distancetype","dtype"} )
         public Object getValue(Object obj) {
             String dType = "";
 
             if (obj != null && obj instanceof Player) {
                 Player player = (Player) obj;
                 String pName = player.getName();
 
                 if (MSocial.isShouting.get(pName) != null
                         && MSocial.isShouting.get(pName)) {
                    dType = MSocial.shoutFormat;
                 } else if (ConfigType.MCHAT_CHAT_DISTANCE.getDouble() > 0) {
                     dType = LocaleType.FORMAT_LOCAL.getVal();
                 }
             }
 
             return dType;
         }
     }
 }

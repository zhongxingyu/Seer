 package dungeon.ui.messages;
 
 import dungeon.models.DamageType;
 
 public class CastSpellCommand implements Command {
   private final DamageType damageType;
 
   public CastSpellCommand (DamageType damageType) {
     this.damageType = damageType;
   }
 
   public DamageType getDamageType () {
    return damageType;
   }
 }

 package Model.StatusEffects;
 
 import Model.Player;
 import Model.Skill;
 import Model.StatusEffect;
 import Model.StatusEffectShell;
 
 public class StatusEffectDodge extends StatusEffectShell{
 
 	public StatusEffectDodge(Skill skill, int evasion, int seconds) {
 		
		super(null, skill, "Dodge", 0, 0, 0, 0, 1, 0, 0, evasion, false, false, false, seconds+1, 0);
 	}
 	
 	@Override
 	public StatusEffect createStatusEffectTo(Player newPlayer, Player fromPlayer) {
 		//Finding the next free space in list to add player to
 		super.addPlayerGivenTo(newPlayer.getName());
 		
 		StatusEffect newSE;
 		newSE = new StatusEffect(newPlayer, super.getSkill(), super.getName(), super.getDmgEff(), 
 				super.getMoveXEff(), super.getMoveYEff(), super.getMoveSpeedEff(), 
 				super.getArmEff(), super.getAttackSpeedEff(), super.getRangeEff(), super.getEvasionEff(), 
 				super.hasStealth(), super.hasStun(), super.getChannel(), super.getMaxCounts(), super.getDelay());
 
 		return newSE;
 	}
 
 }

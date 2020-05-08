 package Model.StatusEffects;
 
 import Model.Player;
 import Model.Skill;
 import Model.StatusEffect;
 import Model.StatusEffectShell;
 
 public class StatusEffectShieldstance extends StatusEffectShell{
 
	public StatusEffectShieldstance(Skill skill, int seconds) {
 		
 		//player, skill, name, damage, moveX, moveY, speed, arm, attackSpeed, range, isStun, isChanneling, counts, delay
 	
		super(null, skill, "Shieldstance", 0, 0, 0, -0.75, 0.2, 0, 0, 0, false, false, false, seconds+1, 0);
 
 		super.setChangeModel();
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
 		
 		newSE.setChangeModel();
 		
 		return newSE;
 	}
 }
 	
 	
 	

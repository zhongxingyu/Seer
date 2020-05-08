 package herbstJennrichLehmannRitter.engine.model.action.complexImpl;
 
 import herbstJennrichLehmannRitter.engine.annotation.ComplexCard;
 import herbstJennrichLehmannRitter.engine.enums.BuildingType;
 import herbstJennrichLehmannRitter.engine.model.Player;
 import herbstJennrichLehmannRitter.engine.model.action.ComplexCardAction;
 
 //TODO: We have to implement the text of the special cards, like Baumgeist, where no data is in xml. Maybe we add an extra xml field like <text>			Unknown	Task
 
 
 @ComplexCard("Auferstehung")
 public class AuferstehungComplexAction implements ComplexCardAction {
 
 	@Override
 	public void applyActionOnPlayer(Player sourcePlayer, Player targetPlayer) {
 		sourcePlayer.getTower().setActualPoints((int)(sourcePlayer.getTower().getActualPoints() * 1.3));
 	}
 
 	@Override
 	public String getOwnEffectDescription() {
		return "+30% " + BuildingType.TOWER.toString();
 	}
 
 	@Override
 	public String getEnemyEffectDescription() {
 		return "";
 	}
 }

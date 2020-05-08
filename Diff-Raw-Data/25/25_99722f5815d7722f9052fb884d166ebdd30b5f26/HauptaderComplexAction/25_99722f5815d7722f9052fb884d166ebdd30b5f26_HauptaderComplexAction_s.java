 package herbstJennrichLehmannRitter.engine.model.action.complexImpl;
 
 import herbstJennrichLehmannRitter.engine.annotation.ComplexCard;
 import herbstJennrichLehmannRitter.engine.model.Player;
 import herbstJennrichLehmannRitter.engine.model.action.ComplexCardAction;
 
 @ComplexCard("Hauptader")
 public class HauptaderComplexAction implements ComplexCardAction {
 
 	@Override
 	public void applyActionOnPlayer(Player sourcePlayer, Player targetPlayer) {
 		if (sourcePlayer.getMine().getLevel() < targetPlayer.getMine().getLevel()) {
 			sourcePlayer.getMine().addLevel(2);
 		} else {
 			sourcePlayer.getMine().addLevel(1);
 		}
 	}
 
 	@Override
 	public String getOwnEffectDescription() {
 		return "Wenn der eigene Steinbruch kleiner als der des Gegners ist, " +
				"wird es um 2 erhöht, sonst nur um 1";
 	}
 
 	@Override
 	public String getEnemyEffectDescription() {
 		return "";
 	}
 }

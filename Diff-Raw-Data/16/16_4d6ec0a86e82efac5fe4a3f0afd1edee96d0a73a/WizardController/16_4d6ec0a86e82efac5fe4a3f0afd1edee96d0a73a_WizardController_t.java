 package cardGame.wizard;
 
 
 import cardGame.CardGameCard;
 import cardGame.CardGamePlayer;
 import cardGame.PlayerController;
 import java.util.List;
 
 
 
 public interface WizardController extends PlayerController {
	public void bid();
         public void notifyTrickCompleted(List<CardGameCard> trick, CardGamePlayer player);
 }

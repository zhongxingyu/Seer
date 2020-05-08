 import java.util.ArrayList;
 
 
 public class UnstablePower extends Crash {
 	
 	public UnstablePower() {
 		this.cardColor.add(CardColor.PURPLE);
 		this.cardType = cardType.STAR;
 		this.defense = true;
 		this.opposing = true;
 		this.name = "UnstablePower";
 		this.imagePath = "/Jaina/" + name + ".png";
 		this.value = 0;
 		this.cost = 0;
 	}
 
 	public void use(ArrayList<Choice> choices, Game game) {
 		Choice oppChoice = choices.get(0);
 		Choice gemChoice = choices.get(1);
 		Player crashee = (Player) oppChoice.getChoice().get(0);
 		Player crasher = game.getCurrentPlayer();
 		int gem = (Integer) gemChoice.getChoice().get(0);
 		crasher.gemPile[gem] = crasher.gemPile[gem] - 1;
 		crashee.gemPile[0] = crashee.gemPile[0] + gem + 1;
 		crasher.money += 2;
		crasher.discard.add(new Wound());
		crasher.discard.add(new Wound());
 	}
 	
 	
 	public ChoiceGroup getChoice(Game g) {
 		return super.getChoice(g);
 	}
 	
 	public Card newCard(){
 		return new UnstablePower();
 	}
 
 	@Override
 	public void react(Card cardUsed, Player reacting, ArrayList<Choice> choices, Game game) {
 		super.react(cardUsed, reacting, choices, game);
 	}
 
 	@Override
 	public boolean canReactTo(Card card) {
 		if (card instanceof Crash){
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public ChoiceGroup getReactChoices(Game g, Player p) {
 		return super.getReactChoices(g, p);
 	}
 	
 	@Override
 	public void prepare(ArrayList<Choice> choice, Game g){
 		super.prepare(choice, g);
 	}
 }

 import java.util.ArrayList;
 
 
 public class StoneWall extends ReactionCard{
 	
 	public StoneWall() {
 		this.cardColor = new ArrayList<CardColor>();
 		cardColor.add(CardColor.PURPLE);
 		this.cardType = cardType.STAR;
 		this.defense = true;
 		this.opposing = false;
 		this.cost = 0;
 		this.name = "StoneWall";
 		this.imagePath = "/Rook/"+ name + ".png";
 	}
 	
 	public void use(ArrayList<Choice> choices, Game game) {
 		super.use(choices, game);
 		game.getCurrentPlayer().drawFromBag(1);
 	}
 	
 	public ChoiceGroup getChoice(Game g) {
 		return new ChoiceGroup();
 	}
 	
 	public Card newCard() {
 		return new StoneWall();
 	}
 	
 	public void setEqual(ParamCard card) {
 		super.setEqual(card);
 	}
 	
 	public void react(Card cardUsed, Player reacting, ArrayList<Choice> choices, Game game) {
		Player opp = game.getCurrentPlayer();
 		ArrayList<Choice> cardChoices = cardUsed.getChosenEffect();
 		int gemSelected = (Integer) cardChoices.get(1).getChoice().get(0);
 		reacting.gemPile[gemSelected] = reacting.gemPile[gemSelected] - 1;
		opp.useTurn(cardUsed);
		opp.cardWasUsed(cardUsed);
		reacting.cardWasUsed(this);
 	}
 	
 	public boolean canReactTo(Card card) {
 		if (card instanceof Crash){
 			return true;
 		}
 		return false;
 	}
 	
 	public ChoiceGroup getReactChoices(Game g) {
 		return new ChoiceGroup();
 	}
 
 	@Override
 	public Choice getTrickyChoice(int i, Choice choice, Game g) {
 		// ignore
 		return null;
 	}
 }

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
 		this.draw += 1;
 		Choice choice = choices.get(0);
 		super.use(choices, game);
 		Card card = (Card) choice.getChoice().get(0);
 		game.getCurrentPlayer().lockedCards.add(card);
 		game.getCurrentPlayer().hand.remove(card);
 	}
 	
 	public ChoiceGroup getChoice(Game g) {
 		Choice c1 = new Choice(g.choices.getString("cardKeep"), g.getHand(this), g.getHandObj(this), 1);
 		ChoiceGroup choice = new ChoiceGroup();
 		choice.addChoiceToGroup(c1);
 		return choice;
 	}
 	
 	public Card newCard() {
 		return new StoneWall();
 	}
 	
 	public void setEqual(ParamCard card) {
 		super.setEqual(card);
 	}
 	
 	public void react(Card cardUsed, Player reacting, ArrayList<Choice> choices, Game game) {
 		ArrayList<Choice> cardChoices = cardUsed.getChosenEffect();
 		int gemSelected = (Integer) cardChoices.get(1).getChoice().get(0);
		Player opp = game.getCurrentPlayer();
		opp.gemPile[gemSelected] = opp.gemPile[gemSelected] - 1;
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
 	
 	public ChoiceGroup getReactChoices(Game g, Player P) {
 		return new ChoiceGroup();
 	}
 
 	@Override
 	public Choice getTrickyChoice(int i, Choice choice, Game g) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }

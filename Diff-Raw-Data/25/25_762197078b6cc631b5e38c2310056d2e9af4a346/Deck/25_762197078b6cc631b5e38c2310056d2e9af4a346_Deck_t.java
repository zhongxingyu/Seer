 package deck;
 import gui.CardNotInRulesetException;
 
 import java.util.ArrayList;
 
 
 public class Deck {
 	private enum Rules{
 		BASIC, STANDARD, DRAFT
 	}
 	public ArrayList<MTGCard> cards;
 	public RuleSet rules;
 	public Deck(){
 		this.cards = new ArrayList<MTGCard>();
 		this.rules = new BasicRuleSet();
 	}
 	public Deck(String rule){
 		this.cards = new ArrayList<MTGCard>();
 		this.rules = determineRules(rule);
 	}
 	
 	public String getprintable(String line){
 		return UserInteraction.messages.getString(line);
 	}
 	
 	public void setRules(String rule) throws CardNotInRulesetException{
 		RuleSet newSet = determineRules(rule);
 		if(!(this.isValidRules(newSet))){
 			throw new CardNotInRulesetException();
 		}
 		else{
 			this.rules = newSet;
 		}
 	}
 	
 	private boolean isValidRules(RuleSet newSet) {
 		for(MTGCard card : this.cards){
 			if(!(newSet.canBeAdded(card, this))){
 				return false;
 			}
 		}
 		return true;
 	}
 	private RuleSet determineRules(String rule) {
 		String rules = rule.toUpperCase();
 		RuleSet ruleSet = null;
 		Rules r;
 		try{
 			r = Rules.valueOf(rules);
 		}
 		catch(IllegalArgumentException e){
 			//System.out.println(getprintable("invalidrule")+ rules);
 			r = Rules.BASIC;
 		}
 		
 		switch(r){
 		case BASIC:
 			ruleSet = new BasicRuleSet();
 			break;
 		case STANDARD:
 			ruleSet = new StandardRuleSet();
 		case DRAFT:
 			ruleSet = new DraftRuleSet();
 		}
 		return ruleSet;
 	}
 	
 	public boolean addCardToDeck(MTGCard card) throws CardNotInRulesetException{
 		boolean valid = rules.canBeAdded(card, this);
 		if(valid){
 			cards.add(card);
 			return true;
 		}else{
 			throw new CardNotInRulesetException();
 		}
 	}
 	
 	public void removeCardFromDeck(MTGCard cardToRemove){
 		for(MTGCard card : this.cards){
 			if(card.equals(cardToRemove)){
 				this.cards.remove(card);
 				break;
 			}
 		}
 	}
 	
 	public ArrayList<String> statistics(){
 		ArrayList<String> stats = new ArrayList<String>();
 		String cardCount = (String) String.valueOf(this.cards.size());
 		String landCount = (String) String.valueOf(getTypeCount("Land"));
 		String creatureCount = (String) String.valueOf(getTypeCount("Creature"));
 		String enchantCount = (String) String.valueOf(getTypeCount("Enchantment"));
 		String sorceryCount = (String) String.valueOf(getTypeCount("Sorcery"));
 		String instantCount = (String) String.valueOf(getTypeCount("Instant"));
 		String artifactCount = (String) String.valueOf(getTypeCount("Artifact"));
 		String forestCount = (String) String.valueOf(getLCount("Forest"));
 		String swampCount = (String) String.valueOf(getLCount("Swamp"));
 		String plainCount = (String) String.valueOf(getLCount("Plains"));
 		String islandCount = (String) String.valueOf(getLCount("Island"));
 		String mountainCount = (String) String.valueOf(getLCount("Mountain"));
		String temp = (String) String.valueOf(getTypeCount("Land") - (getLCount("Forest") + getLCount("Swamp") + getLCount("Plain") + getLCount("Island") + getLCount("Mountain")));
 		String otherCount = "";
 		if((Integer) Integer.valueOf(temp) > 0){
 			otherCount = temp;
 		}else{
 			otherCount = "0";
 		}
 		String averageCost = "0";
 		if(this.cards.size() == 0){
 			averageCost = "0";
 		}else{
 			averageCost = (String) String.valueOf(getCardCostNoColor() / this.cards.size());
 		}
 		String blackManaUsed = (String) String.valueOf(getColorCost('B'));
 		String blueManaUsed = (String) String.valueOf(getColorCost('U'));
 		String greenManaUsed = (String) String.valueOf(getColorCost('G'));
 		String whiteManaUsed = (String) String.valueOf(getColorCost('P'));
 		String redManaUsed = (String) String.valueOf(getColorCost('R'));
 		stats.add(cardCount);
 		stats.add(landCount);
 		stats.add(swampCount);
 		stats.add(plainCount);
 		stats.add(islandCount);
 		stats.add(mountainCount);
 		stats.add(forestCount);
 		stats.add(otherCount);
 		stats.add(creatureCount);
 		stats.add(enchantCount);
 		stats.add(sorceryCount);
 		stats.add(instantCount);
 		stats.add(artifactCount);
 		stats.add(averageCost);
 		stats.add(blackManaUsed);
 		stats.add(blueManaUsed);
 		stats.add(greenManaUsed);
 		stats.add(whiteManaUsed);
 		stats.add(redManaUsed);
 		return stats;
 				
 	}
 	
 	public int getColorCost(char color) {
 		int temp = 0;
 		for(MTGCard card : this.cards){
 			for(char chr : card.castingCost.toCharArray()){
 				if(chr == color){
 					temp++;
 				}
 			}
 		}
 		return temp;
 	}
 	public int getCardCostNoColor() {
 		int temp = 0;
 		for(MTGCard card : this.cards){
 			if(!(card.type.equals("Land"))){
 				for(char item : card.castingCost.toCharArray()){
 					try{
 						int hold = (Integer)Integer.valueOf((String) String.valueOf(item));
 						temp += hold;
 					}
 					catch(Exception e){
 						temp++;
 					}
 				}
 			}
 		}
 		return temp;
 	}
 	public int getLCount(String Lname){
 		int temp = 0;
 		for(MTGCard card : this.cards){
 			if(card.name.equals(Lname) && card.type.contains("Land")){
 				temp++;
 			}
 		}
 		return temp;
 	}
 	
 	public int getTypeCount(String type){
 		int temp = 0;
 		for(MTGCard card : this.cards){
 			if(card.type.contains(type)){
 				temp++;
 			}
 		}
 		return temp;
 	}
 
 	public String displayDeck(){
 		String deckString = "";
 		if(this.cards.isEmpty()){
 			return getprintable("nocards");
 		}
 		
 		for(MTGCard card : cards){
 			deckString = deckString + (card.toString());
 		}
 		System.out.println(deckString);
 		
 		return deckString;
 	}
 }

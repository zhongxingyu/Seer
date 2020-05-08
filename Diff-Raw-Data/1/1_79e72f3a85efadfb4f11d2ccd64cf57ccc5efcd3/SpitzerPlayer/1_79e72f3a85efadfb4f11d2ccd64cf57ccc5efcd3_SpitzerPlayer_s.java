 package game.player;
 
 import game.SpitzerDeclaration;
 import game.cards.Card;
 import game.cards.Cards;
 import game.player.bot.FirstCardSpitzerBot;
 import game.player.bot.SpitzerBot;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.codehaus.jackson.annotate.JsonSubTypes;
 import org.codehaus.jackson.annotate.JsonTypeInfo;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 /**
  * Due to our use of JSON mapping, we must explicity declare each subclass for the bots
  */
 @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
 @JsonSubTypes({
 
 	// Define all spitzer bot classes here
       @JsonSubTypes.Type(value=FirstCardSpitzerBot.class, name="FirstCardSpitzerBot")
 
      // @JsonSubTypes.Type(value=Cat.class, name="cat")
 
   }) 
 public class SpitzerPlayer
 {
 	public String name;
 	public Integer trickPoints = 0;
 	public Integer gamePoints = 0;
 	public boolean isDealer;
 	public Integer userId;
 	public Cards hand;
 	public List<SpitzerDeclaration> declarations;
 	public List<Integer> gamePointHistory = Lists.newArrayList();
 	public Collection<Card> validCards = null;
 	public SpitzerDeclaration activeDeclaration;
 
 	public SpitzerPlayer(){}
 
 	// Copy constructor
 	public SpitzerPlayer(SpitzerPlayer player) 
 	{
 		this.name = player.name;
 		this.trickPoints = player.trickPoints;
 		this.gamePoints = player.gamePoints;
 		this.isDealer = player.isDealer;
 		this.userId = player.userId;
 		if(player.hand != null)
 			this.hand = new Cards(player.hand);
 		if(player.declarations != null)
 			this.declarations = Lists.newArrayList(player.declarations);
 		if(player.gamePointHistory != null)
 			this.gamePointHistory = Lists.newArrayList(player.gamePointHistory);
 		if(player.validCards != null)
 			this.validCards = Sets.newHashSet(player.validCards);
 		this.activeDeclaration = player.activeDeclaration;
 	}
 
 	public void orderCards()
 	{
 		this.hand.orderBySuit();
 	}
 	
 	// This method is called when this player is not the active player
 	// Remove all information that could be used to cheat
 	public void sanitize()
 	{
 		this.hand = null;
 		this.declarations = null;
 		this.activeDeclaration = null;
 	}
 	
 	public void grantGamePoints(Integer points)
 	{
 		this.gamePointHistory.add(this.gamePoints);
 		this.gamePoints += points;
 	}
 	
 	public void addCardToHand(Card card)
 	{
 		if(hand == null)
 			hand = new Cards();
 		
 		hand.add(card);
 	}
 	
 	
 	@Override
 	public boolean equals(Object other)
 	{
 		if(!(other instanceof SpitzerPlayer))
 			return false;
 		
 		SpitzerPlayer otherPlayer = (SpitzerPlayer)other;
 		if(otherPlayer.userId.equals(this.userId))
 			return true;
 		
 		return false;
 	}
 }

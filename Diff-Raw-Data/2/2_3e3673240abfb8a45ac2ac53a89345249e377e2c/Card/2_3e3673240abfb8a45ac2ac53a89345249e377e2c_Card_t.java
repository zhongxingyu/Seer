 package ucbang.core;
 
 import java.util.Arrays;
 
 public class Card {
 	public String description="";
 	public Enum e;
 	public String name;
 	public int ordinal;
 
 	public int type; // 1 = char, 2 = play, 3 = greenfield, 4 = miss, 5 = bluefield
 	public int target; // 1 = self, 2 = choose 1 player, 3 = all, 4 = all others
 	public int effect; // 1 = deal damage, 2 = heal, 3 = miss, 4 = draw
 	public int effect2; // secondary effects only affect player
 	public int special; // HP for char cards, ???? for other cards, 1 for beer
 						// and bangs, 1 for miss, 2 for dodge
 	public boolean discardToPlay; // cards that need a discard to play
 	public int range; // used for guns and panic and #cards drawn
         
 	public int location; //0 = in hand, 1 = on field, 2 = played
 	public static enum play {
 		DAMAGE, HEAL, MISS, DRAW, STEAL, DISCARD, DUEL, JAIL
 	}; // played cards
 
 	public static enum field {
 		DAMAGE, HEAL, MISS, DRAW, STEAL, DISCARD, BARREL, DYNAMITE, GUN, HORSE_RUN, HORSE_CHASE
 	}; // field cards
 
 	public Card(Enum e) {
 		this.e = e;
 		ordinal = e.ordinal();
 		name = e.toString();
 		if (e instanceof Deck.Characters) {
 			type = 1;
 			int[] threehp = new int[] { 3, 6, 8, 16, 21, 27, 28, 30 };
 			if (Arrays.binarySearch(threehp, ordinal) >= 0
 					&& ordinal == threehp[Arrays.binarySearch(threehp, ordinal)]) { // awkward
 																					// way
 																					// of
 																					// doing
 																					// contains
 				special = 3;
 			} else
 				special = 4;
 		} else {
 			// TODO: find out what kind of card it is
 
 			switch ((Deck.CardName) e) {
 			// put all direct damage cards here
 			case BACK:
 				break;
 			case BANG:
 				type = 2;
 				special = 1;
 				range = 1;
 				target = 2;
 				effect = play.DAMAGE.ordinal();
 				description = "BANG! cards are the main method to reduce other players' life points. \n" +
 						"If you want to play a BANG! card to hit one of the players, determine: a) what \n" +
 						"the distance to that player is, and b) if your weapon is capable of reaching that distance. ";
 				break;
 			case PUNCH:
 				type = 2;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				description="This cards has same effect as BANG! card, but on distance 1.\n" +
 						" This cards isnt count as BANG!";
 				break;
 			case GATLING:
 				type = 2;
 				target = 4;
 				effect = play.DAMAGE.ordinal();
 				description="The symbols show: a BANG! to all the other players.";
 				break;
 			case HOWITZER:
 				type = 3;
 				target = 4;
 				effect = play.DAMAGE.ordinal();
 				description="The current player play a Howitzer card in front of him.\n" +
 						"Starting with the next player's turn, he can discard it for BANG!\n" +
 						"effect to all players.. This card is not count as BANG!.";
 				break;
 			case INDIANS:
 				type = 2;
 				special = 2;
 				target = 4;
 				effect = play.DAMAGE.ordinal();
 				description="Each player, excluding the one who played this card, may discard a BANG!\n" +
 						" card, or lose a life point. Neither Missed! nor Barrel has effect in this case.";
 				break;
 			case KNIFE:
				type = 3;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				description="The current player play a Knife card in front of him. Starting with the next\n" +
 						"player's turn, he can discard it for BANG! effect. This card is not count as BANG!";
 				break;
 			case BUFFALO_RIFLE:
 				type = 3;
 				target = 2;
 				range = -1;
 				effect = play.DAMAGE.ordinal();
 				description="The current player play a Buffalo Rifle card in front of him. Starting with the\n" +
 						"next player's turn, he can discard it for BANG! effect to 1 player at every distance.\n" +
 						"This card is not count as BANG!";
 				break;
 			case SPRINGFIELD:
 				type = 2;
 				target = 2;
 				discardToPlay = true;
 				range = -1;
 				effect = play.DAMAGE.ordinal();
 				description="Player which is on turn, discard card Springfield together with another card on the\n" +
 						"deck. Than he choose one player, which is target of attack with BANG! effect.";
 				break;
 			case PEPPERBOX:
 				type = 3;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				description="The current player play a PepperBox card in front of him. Starting with the next\n" +
 						" player's turn, he can discard it for BANG! effect to 1 player at visible distance.\n" +
 						"This card is not count as BANG!.";
 				break; // TODO: make same range as bang
 			case DERRINGER:
 				type = 3;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				effect2 = play.DRAW.ordinal();
 				description="The current player plays a Derringer card in front of her. During one of her following\n" +
 						"turns, provided she still has the card in front of her, she can choose to discard it to a BANG!\n" +
 						"on a player at a distance of 1, and also draw a card from the deck. ";
 				break;
 			case DUEL:
 				type = 2;
 				target = 2;
 				range = -1;
 				effect = play.DUEL.ordinal();
 				description="The player playing this card challenges any other player (at any distance), staring him in\n" +
 						"the eyes. The challenged player may discard a BANG! card (even though it is not his turn!).\n" +
 						"If he does, the player who played the Duel card may discard a BANG! card, and so on: the first\n" +
 						"player failing to play a BANG! card loses one life point, and the duel is over. Note: you cannot\n" +
 						"use the Barrel or play Missed! cards during a duel, and the Duel is not considered a BANG! card.";
 				break;
 			case DYNAMITE:
 				type = 3;
 				target = 1;
 				effect = field.DYNAMITE.ordinal();
 				break;
 
 			case MISS:
 				type = 4;
 				special = 1;
 				effect = play.MISS.ordinal();
 				break;
 			case DODGE:
 				type = 4;
 				effect = play.MISS.ordinal();
                                 effect2 = play.DRAW.ordinal();
 				break;
 			case BIBLE:
 				type = 3;
 				effect = play.MISS.ordinal();
                                 effect2 = play.DRAW.ordinal();
 				break;
 			case IRON_PLATE:
 				type = 3;
 				effect = play.MISS.ordinal();
 				break;
 			case SOMBRERO:
 				type = 3;
 				effect = play.MISS.ordinal();
 				break;
 			case TEN_GALLON_HAT:
 				type = 3;
 				effect = play.MISS.ordinal();
 				break;
 
 			case BARREL:
 				type = 5;
 				effect = field.BARREL.ordinal();
 				break;
 
 			case WELLS_FARGO:
 				type = 2;
 				range = 3;
 				effect = play.DRAW.ordinal();
 				break;
 			case STAGECOACH:
 				type = 2;
 				range = 2;
 				effect = play.DRAW.ordinal();
 				break;
 			case CONESTOGA:
 				type = 3;
 				range = 2;
 				effect = play.DRAW.ordinal();
 				break;
 			case PONY_EXPRESS:
 				type = 3;
 				range = 3;
 				effect = play.DRAW.ordinal();
 				break;
 			case GENERAL_STORE:
 				type = 2;
 				target = 3;
 				range = 1;
 				effect = play.DRAW.ordinal();
 				break; //TODO: fix general store
 
 			case JAIL:
 				type = 2; //TODO: make special case for jail
 				range = -1;
 				effect = play.JAIL.ordinal();
 				break; // special case: even though jail remains on the field of
 						// a player, it is "played"
 
 			case APPALOOSA:
 				type = 5;
 				effect = field.HORSE_CHASE.ordinal();
 				break;
 			case SILVER:
 				type = 5;
 				effect = field.HORSE_CHASE.ordinal();
 				break;
 			case MUSTANG:
 				type = 5;
 				effect = field.HORSE_RUN.ordinal();
 				break;
 			case HIDEOUT:
 				type = 5;
 				effect = field.HORSE_RUN.ordinal();
 				break; // you heard me: a hideout is a horse.
 
 			case BEER:
 				type = 2;
 				target = 1;
                                 range = 1;
 				special = 1;
 				effect = play.HEAL.ordinal();
 				break;
 			case TEQUILA:
 				type = 2;
 				target = 2;
                                 range = 1;
 				discardToPlay = true;
 				effect = play.HEAL.ordinal();
 				break;
 			case WHISKY:
 				type = 2;
 				target = 1;
 				range = 2;
 				discardToPlay = true;
 				effect = play.HEAL.ordinal();
 				break; // special case: heals 2 hp, so i guess i'll use "range"
 			case CANTEEN:
 				type = 3;
 				target = 1;
                                 range = 1;
 				effect = play.HEAL.ordinal();
 				break;
 			case SALOON:
 				type = 2;
 				target = 3;
                                 range = 1;
 				effect = play.HEAL.ordinal();
 				break;
 
 			case BRAWL:
 				type = 2;
 				target = 4;
 				discardToPlay = true;
 				play.DISCARD.ordinal();
 				break;
 			case CAN_CAN:
 				type = 3;
 				target = 2;
 				range = -1;
 				effect = play.STEAL.ordinal();
 				break;
 			case RAG_TIME:
 				type = 2;
 				target = 2;
 				range = -1;
 				discardToPlay = true;
 				effect = play.STEAL.ordinal();
 				break;
 			case PANIC:
 				type = 2;
 				target = 2;
 				range = 1;
 				effect = play.STEAL.ordinal();
 				break;
 			case CAT_BALLOU:
 				type = 2;
 				target = 2;
 				range = -1;
 				effect = play.DISCARD.ordinal();
 				break;
 
 			case VOLCANIC:
 				type = 5;
 				special = 1;
 				range = 1;
 				effect = field.GUN.ordinal();
 				break;
 			case SCHOFIELD:
 				type = 5;
 				range = 2;
 				effect = field.GUN.ordinal();
 				break;
 			case REMINGTON:
 				type = 5;
 				range = 3;
 				effect = field.GUN.ordinal();
 				break;
 			case REV_CARBINE:
 				type = 5;
 				range = 4;
 				effect = field.GUN.ordinal();
 				break;
 			case WINCHESTER:
 				type = 5;
 				range = 5;
 				effect = field.GUN.ordinal();
 				break;
 
 			default:
 				break; // special = 1; type = 2; effect = play.DAMAGE.ordinal();
 						// break; //all cards left untreated are treated as
 						// bangs
 			}
                         /*if(type==3||type==5){ //TODO: remove this debug feature
                             setLocation((int)(2*Math.random()));
                         }
                         else{
                             setLocation((int)(2*Math.random())==0?0:2);
                         }*/
 		}
 	}
 
     //for display purposes only:
     public static Card playedCard(Enum e){
         return null;
     }
     
     public void setLocation(int i){
         location = i;
     }
         
 	public String toString(){
 		return name;
 	}
 }

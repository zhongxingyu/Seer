 package ucbang.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 public class Card {
 
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
 			case BANG:
 				type = 2;
 				special = 1;
 				range = 1;
 				target = 2;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case PUNCH:
 				type = 2;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case GATLING:
 				type = 2;
 				target = 4;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case HOWITZER:
 				type = 3;
 				target = 4;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case INDIANS:
 				type = 2;
 				special = 2;
 				target = 4;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case KNIFE:
 				type = 2;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case BUFFALO_RIFLE:
 				type = 3;
 				target = 2;
 				range = -1;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case SPRINGFIELD:
 				type = 2;
 				target = 2;
 				discardToPlay = true;
 				range = -1;
 				effect = play.DAMAGE.ordinal();
 				break;
 			case PEPPERBOX:
 				type = 3;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				break; // TODO: make same range as bang
 			case DERRINGER:
 				type = 3;
 				target = 2;
 				range = 1;
 				effect = play.DAMAGE.ordinal();
 				effect2 = play.DRAW.ordinal();
 				break;
 
 			case DUEL:
 				type = 2;
 				target = 2;
 				range = -1;
 				effect = play.DUEL.ordinal();
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
 				break;
 			case BIBLE:
				type = 3;
 				effect = play.MISS.ordinal();
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
 				type = 3;
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
 				break; // fix general store
 
 			case JAIL:
 				type = 2;
 				range = -1;
 				effect = play.JAIL.ordinal();
 				break; // special case: even though jail remains on the field of
 						// a player, it is "played"
 
 			case APPALOOSA:
 				type = 3;
 				effect = field.HORSE_CHASE.ordinal();
 				break;
 			case SILVER:
 				type = 3;
 				effect = field.HORSE_CHASE.ordinal();
 				break;
 			case MUSTANG:
 				type = 3;
 				effect = field.HORSE_RUN.ordinal();
 				break;
 			case HIDEOUT:
 				type = 3;
 				effect = field.HORSE_RUN.ordinal();
 				break; // you heard me: a hideout is a horse.
 
 			case BEER:
 				type = 2;
 				target = 1;
 				special = 1;
 				effect = play.HEAL.ordinal();
 				break;
 			case TEQUILA:
 				type = 2;
 				target = 2;
 				discardToPlay = true;
 				effect = play.HEAL.ordinal();
 				break;
 			case WHISKY:
 				type = 2;
 				target = 1;
 				range = 1;
 				discardToPlay = true;
 				effect = play.HEAL.ordinal();
 				break; // special case: heals 2 hp, so i guess i'll use "range"
 			case CANTEEN:
 				type = 3;
 				target = 1;
 				effect = play.HEAL.ordinal();
 				break;
 			case SALOON:
 				type = 2;
 				target = 3;
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
 				type = 3;
 				special = 1;
 				range = 1;
 				effect = field.GUN.ordinal();
 				break;
 			case SCHOFIELD:
 				type = 3;
 				range = 2;
 				effect = field.GUN.ordinal();
 				break;
 			case REMINGTON:
 				type = 3;
 				range = 3;
 				effect = field.GUN.ordinal();
 				break;
 			case REV_CARBINE:
 				type = 3;
 				range = 4;
 				effect = field.GUN.ordinal();
 				break;
 			case WINCHESTER:
 				type = 3;
 				range = 5;
 				effect = field.GUN.ordinal();
 				break;
 
 			default:
 				break; // special = 1; type = 2; effect = play.DAMAGE.ordinal();
 						// break; //all cards left untreated are treated as
 						// bangs
 			}
 		}
 	}
 
 	public Enum e;
 	public String name;
 	public int ordinal;
 
 	public int type; // 1 = char, 2 = play, 3 = field, 4 = miss
 	public int target; // 1 = self, 2 = choose 1 player, 3 = all, 4 = all others
 	public int effect; // 1 = deal damage, 2 = heal, 3 = miss, 4 = draw
 	public int effect2; // secondary effects only affect player
 	public int special; // HP for char cards, ???? for other cards, 1 for beer
 						// and bangs, 1 for miss, 2 for dodge
 	public boolean discardToPlay; // cards that need a discard to play
 	public int range; // used for guns and panic and #cards drawn
 	public String toString(){
 		return name;
 	}
 }

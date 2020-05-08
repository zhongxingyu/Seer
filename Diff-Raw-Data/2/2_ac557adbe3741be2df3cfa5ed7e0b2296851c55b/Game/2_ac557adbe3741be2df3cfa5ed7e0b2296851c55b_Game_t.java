 package redistadelle;
 
 import static redistadelle.Redis.JEDIS;
 import static redistadelle.Utils.key;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 import play.libs.Json;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Tuple;
 
 public class Game {
 
 	private String id;
 	
 	
 	public Game(String id) {
 		this.id = id;
 	}
 	
 	
 	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
 		Set<String> playerKeys = JEDIS.get().keys(key("player",id,"*"));
 		for(String playerKey : playerKeys) {
 			players.add(new Player(playerKey, playerKey, id));
 		}
 		return players;
 	}
 	
 	public void initGame() {
 		JEDIS.get().hset(key("game",id), "turn", "0");
 		createPile();
 		setPlayerOrder();
 	}
 	
 	
 	
 	/** Create a pile for this game. */
 	private void createPile(){
 		List<String> gameDeck = new ArrayList<String>();
 		Set<Tuple> deck = JEDIS.get().zrangeWithScores("deck", 0, -1);
 		for (Tuple tuple : deck) {
 			for(int i=0; i<tuple.getScore();i++) {
 				gameDeck.add(tuple.getElement());
 			}
 		}
 		Collections.shuffle(gameDeck);
 		for(String value :gameDeck){
 			JEDIS.get().lpush(key("pile",id),value);
 		}
 	}
 	
 
 	public void setPlayerOrder() {
 		String turn = JEDIS.get().hget(key("game",id), "turn");
 		Set<String> players = JEDIS.get().keys(key("player",id,"*"));
 		for (String playerId : players) {
 			JEDIS.get().lpush(key("order", id), playerId);
 		}
 	}
 	
 	
 	
     private List<Player> players;
     private List<District> pile;
     private Integer turn = 0;
     private Integer firstPlayer = 0;
     private Integer activePlayer = 0;
     private EnumSet<Job> availableJob;
 
     
     public Game(List<Player> players, List<District> pile) {
     	Jedis jedis = new Jedis("localhost");
         this.players = new ArrayList<Player>(players);
         this.pile = new ArrayList<District>(pile);
         this.availableJob = EnumSet.allOf(Job.class);
     }
     
     
     public List<Map<String,String>> getPlayerInfos() {
     	List<Map<String,String>> playersInfos = new ArrayList<Map<String,String>>();
     	for (String player : JEDIS.get().keys(key("player", id, "*"))) {
     		playersInfos.add(JEDIS.get().hgetAll(player));
     	}
     	return playersInfos;
     }
     
     
 //    public void initTurn() {
 //        this.availableJob = EnumSet.allOf(Job.class);
 //        for(int i=0; i<=players.size(); i++) {
 //            if(players.get(i).isFirst) {
 //                firstPlayer = i;
 //                activePlayer = i;
 //                break;
 //            }
 //        }
 //    }
     
     public Player getActivePlayer() {
         return players.get(activePlayer);
     }
     
     public void takeJob(Player player, Job job) {
 //        if(!availableJob.contains(job)) return;
 //        player.init();
 //        player.job = job;
 //        switch (job) {
 //            case ASSASSIN:
 //                player.canBeKill = false;
 //                player.canBeStolen = false;
 //                break;
 //            case THIEF:
 //                player.canBeStolen = false;
 //                break;
 //            case KING:
 //                player.isFirst = true;
 //                break;
 //            case BISHOP:
 //                player.canBeDestroy = false;
 //                break;
 //            case WARLORD:
 //                player.canBeDestroy = false;
 //                break;
 //        }
 //        
 //        if(activePlayer >= players.size()) {
 //            activePlayer = 0;
 //        } else {
 //            activePlayer++;
 //        }
 //        availableJob.remove(job);
     }
 
     public void playAbility(Power power, Player player, Player target, District targetDistrict) {
 //        if (!player.powers.contains(power)) return;
 //        switch (power) {
 //            case ASSASSIN_KILL:
 //                if (target.canBeKill) {
 //                    target.isAlive = false;
 //                    player.powers.remove(power);
 //                }
 //                break;
 //            case THIEF_STOLE:
 //                if (target.canBeStolen) {
 //                    target.isStolen = true;
 //                    player.powers.remove(power);
 //                }
 //                break;
 //            case WIZARD_SWITCH:
 //                Integer cardsSize = player.cards.size();
 //                player.cards.clear();
 //                for (int i = 0; i < cardsSize; i++) {
 //                    player.cards.add(pile.get(0));
 //                    pile.remove(0);
 //                }
 //                player.powers.remove(power);
 //                break;
 //            case WIZARD_STOLE:
 //                Set<District> switchDistrict = new HashSet<District>(target.cards);
 //                target.cards = new HashSet<District>(player.cards);
 //                player.cards = switchDistrict;
 //                player.powers.clear();
 //                break;
 //            case KING_HARVEST:
 //                for (District district : player.city) {
 //                    if (LORDLY.equals(district.type)) {
 //                        player.gold++;
 //                    }
 //                }
 //                player.powers.remove(power);
 //                break;
 //            case BISHOP_HARVEST:
 //                for (District district : player.city) {
 //                    if (SACRED.equals(district.type)) {
 //                        player.gold++;
 //                    }
 //                }
 //                player.powers.remove(power);
 //                break;
 //            case TRADER_HARVEST:
 //                for (District district : player.city) {
 //                    if (SHOP.equals(district.type)) {
 //                        player.gold++;
 //                    }
 //                }
 //                player.powers.remove(power);
 //                break;
 //            case WARLORD_HARVEST:
 //                for (District district : player.city) {
 //                    if (MILITARY.equals(district.type)) {
 //                        player.gold++;
 //                    }
 //                }
 //                player.powers.remove(power);
 //                break;
 //            case WARLORD_DESTROY:
 //                if (target.canBeDestroy && player.gold >= (targetDistrict.value - 1)) {
 //                    target.city.remove(targetDistrict);
 //                    player.gold -= (targetDistrict.value - 1);
 //                    player.powers.remove(power);
 //                }
 //                break;
 //        }
     }
 
 //    public Boolean isFinish() {
 //        for(Player player : players) {
 //            if(player.city.size() >= 8) return true;
 //        }
 //        return false;
 //    }
     
     
 }

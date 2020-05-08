 package net.dracolair.games.applestoapples.commands;
 
 import java.util.Map.Entry;
 
 import net.dracolair.games.applestoapples.ApplesToApples;
 import net.dracolair.games.applestoapples.Bot;
 import net.dracolair.games.applestoapples.Card;
 import net.dracolair.games.applestoapples.Name;
 import net.dracolair.games.applestoapples.Player;
 import net.dracolair.games.applestoapples.State;
 
 import static net.dracolair.games.applestoapples.Factories.*;
 import static net.dracolair.games.applestoapples.MessageMap.*;
 
 public class CmdStart extends Command {
 
 	@Override
 	public void run(Bot bot, ApplesToApples ata, String[] msgMap) {
 		if(ata == null || ata.m_players.size() < 3) {
 			m_responses.add(MSG(CHANNEL(msgMap), NICK(msgMap) + 
 					                          " is fail, needs " + 
 					                          (ata==null?3:(3-ata.m_players.size())) + 
 					                          " to play."));
 		} else {
 			m_responses.add(MSG(CHANNEL(msgMap), "We have >=3 players, the game will begin!"));
 			m_responses.add(MSG(CHANNEL(msgMap), "Dealing out cards..."));
 			for(Entry<Name, Player> e : ata.m_players.entrySet()) {
 				ata.m_activePlayers.add(e.getKey());
 				for(int i = 1; i < 8; i++) {
 					Card c = CARD("Card", String.valueOf(i));
 					e.getValue().m_cards.add(c);
 					m_responses.add(MSG(e.getKey().toString(), c.toString()));
 				}
 			}
 			ata.m_state = State.PLAY;
 			m_responses.add(MSG(bot.getName(), "!botplay"));
 		}
 	}
 
 }

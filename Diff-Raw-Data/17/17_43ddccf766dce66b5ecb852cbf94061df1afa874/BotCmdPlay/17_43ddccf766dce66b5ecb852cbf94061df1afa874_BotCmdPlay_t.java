 package net.dracolair.games.applestoapples.commands;
 
 import net.dracolair.games.applestoapples.ApplesToApples;
 import net.dracolair.games.applestoapples.Bot;
 
 import static net.dracolair.games.applestoapples.Factories.*;
 import static net.dracolair.games.applestoapples.MessageMap.*;
 
 public class BotCmdPlay extends BotCommand {
 
 	@Override
 	public void run(Bot bot, ApplesToApples ata, String[] msgMap) {
		ata.m_waiting.addAll(ata.m_activePlayers);
		ata.m_judge = ata.m_waiting.remove(0);
 		
 		m_responses.add(MSG(CHANNEL(msgMap), ata.m_judge + " is the judge.  Green card is: hax"));
 		m_responses.add(MSG(CHANNEL(msgMap), "Waiting for players to play cards..."));
 	}
 
 }

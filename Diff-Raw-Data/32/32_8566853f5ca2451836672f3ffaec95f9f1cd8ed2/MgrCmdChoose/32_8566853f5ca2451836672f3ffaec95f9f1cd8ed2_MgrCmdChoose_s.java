 package net.dracolair.games.applestoapples.commands;
 
 import static net.dracolair.games.applestoapples.Factories.*;
 
 import java.util.List;
 
 import net.dracolair.games.applestoapples.Game;
 import net.dracolair.games.applestoapples.GameManager;
 import net.dracolair.games.applestoapples.Message;
 import net.dracolair.games.applestoapples.MessageInfo;
 import net.dracolair.games.applestoapples.State;
 
 public class MgrCmdChoose extends ManagerCommand {
 
 	@Override
 	public void run(GameManager gameManager, Game ata, MessageInfo msgInfo, List<Message> responses) {
 		ata.m_state = State.CHOOSE;		
 		
 		responses.add(MSG(msgInfo.MESSAGE, "The green card is: " + ata.m_greenCard));
 		
 		for(int cnt = 0; cnt < ata.m_cards.size(); cnt++) {
 			responses.add(MSG(msgInfo.MESSAGE, (cnt + 1) + ". " + ata.m_cards.get(cnt)));
 		}
 		
 		responses.add(MSG(msgInfo.MESSAGE, ata.m_judge + " must choose a red card!  Type '!choose number'"));
 	}
 	
 }

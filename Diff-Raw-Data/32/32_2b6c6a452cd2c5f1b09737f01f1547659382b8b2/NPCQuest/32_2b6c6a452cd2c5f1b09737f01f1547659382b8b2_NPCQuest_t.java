 package fart.dungeoncrawler.actor;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 import Utils.Vector2;
 import fart.dungeoncrawler.Game;
 
public class NPCQuest extends NPCTrigger {
 	private ArrayList<Quest> quests;
 	private int actQuest;
 	private Rectangle triggerArea;
 
 	public NPCQuest(Game game, Vector2 position, NPCDescription npcDesc, Rectangle triggerRect, ArrayList<Integer> questIDs) {
 		super(game, position, npcDesc);
 		
 		this.triggerArea = triggerRect;
 
 		quests = new ArrayList<Quest>();
 		for(int i = 0; i < questIDs.size(); i++) {
 			quests.add(QuestCollection.getQuestByID(questIDs.get(i)));
 		}
 		
 		actQuest = 0;
 	}
 
 	@Override
 	public void trigger(Actor actor) {
 		if(actQuest >= quests.size())
 			return;
 		
 		if(actor instanceof NewPlayer) {
 			NewPlayer player = (NewPlayer)actor;
 			Quest quest = QuestCollection.getQuestByID(actQuest);
 			
 			QuestLog log = player.getQuestLog();
 			
 			//bereits angenommen
 			if(log.contains(quest)) {
 				if(log.getQuest(quest).checkQuestDone(player)) {
 					actQuest += 1;
 				}
 			} 
 			//noch nicht angenommen
 			else {
 				log.addQuest(quest);
 				System.out.println("Added quest to the log");
 			}
 		} else {
 			return;
 		}
 	}
 
 	@Override
 	public Rectangle getTriggerArea() {
 		return triggerArea;
 	}
 
 }

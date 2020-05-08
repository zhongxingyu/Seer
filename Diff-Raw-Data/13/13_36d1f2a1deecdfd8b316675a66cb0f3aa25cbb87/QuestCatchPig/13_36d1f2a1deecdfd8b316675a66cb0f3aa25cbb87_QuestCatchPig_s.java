 package gamedev.quests;
 
 import gamedev.ai.RandomMoveStrategy;
 import gamedev.game.ResourcesManager;
 import gamedev.objects.OldCaveman;
 import gamedev.objects.Pig;
 import gamedev.scenes.GameMapScene;
 
 import java.util.ArrayList;
 
 import android.widget.Toast;
 
 public class QuestCatchPig extends Quest {
 
 	protected Pig pig;
 	protected ArrayList<String> conversation = new ArrayList<String>();
 	protected OldCaveman caveman;
 
 	public QuestCatchPig(GameMapScene map) {
 		super(map);
 		this.title = "Cross the forest";
 		this.description = "Catch the pig and bring it to the cave man";
 
 		this.conversation
 				.add("Hi, my name is Detlef. I need to go to the other side of the forest, can you help me?");
 		this.conversation.add("Hmm... My back hurts, I'm so old!");
 		this.conversation.add("And even worse, I lost my pig this morning");
 		this.conversation
 				.add("I'm scared that the dinosaurs will kill it... Can you catch it for me?");
 		this.conversation
 				.add("I will give you an axe so you can destroy the trees");
 		this.conversation.add("OK! Let's find this pig...");
 
 		// Create the pig :-D
 		this.pig = new Pig(500, 500);
 		RandomMoveStrategy rm = new RandomMoveStrategy(pig, 200, 800, 1, 100,
 				1000, 100, 1000);
 		pig.setMoveStrategy(rm);
 		map.attachChild(pig);
 
 		this.caveman = new OldCaveman(951, 1133);
 		map.attachChild(caveman);
 	}
 
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
 		if (active) {
 			for (String s : this.conversation) {
 				ResourcesManager.getInstance().activity.toastOnUIThread(s,
 						Toast.LENGTH_LONG);
 			}
 		}
 	}
 
 	@Override
 	public void onFinish() {
 		ResourcesManager.getInstance().activity
 				.toastOnUIThread(
 						"Thank you very much young man! Here is your axe, as promised. Be safe!",
 						Toast.LENGTH_LONG);
 		// TODO Remove the trees and the body so the player can pass the forest
 		// :)
		this.setFinished(true);
 	}
 
 	@Override
 	public String getStatus() {
 		return "Bring me the pig and you'll receive an axe!";
 	}
 
 	@Override
 	public boolean isCompleted() {
 		// This flag is set to true from the BodiesContact Listener when we
 		// catched the pig
 		return this.isCompleted;
 	}
 
 }

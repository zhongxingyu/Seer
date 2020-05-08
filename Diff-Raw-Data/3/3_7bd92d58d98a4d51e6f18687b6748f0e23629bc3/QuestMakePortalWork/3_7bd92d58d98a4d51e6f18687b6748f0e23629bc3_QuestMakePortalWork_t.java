 package gamedev.quests;
 
 import gamedev.game.ResourcesManager;
 import gamedev.game.SceneManager;
 import gamedev.objects.AnimatedObject.GameState;
 import gamedev.scenes.GameMapScene;
 import android.widget.Toast;
 
 public class QuestMakePortalWork extends Quest {
 
 	public QuestMakePortalWork(GameMapScene map) {
 		super(map);
 		this.title = "Go back home with the portal";
 		this.description = "I need to find some information how to make the portal work...";
 	}
 
 	@Override
 	public void onFinish() {
 		SceneManager.getInstance().loadGameEndScene(
 				ResourcesManager.getInstance().engine);
 		ResourcesManager.getInstance().avatar.setState(GameState.IDLE, -1);
 	}
 
 	@Override
 	public String getStatus() {
 		return "The portal still isn't working...";
 	}
 
 	@Override
 	public String statusForQuestScene() {
		if (SceneManager.getInstance().getCurrentGameMapScene().getQuest(3)
				.isCompleted()) {
 			return "I have the keys for the portal";
 		} else {
 			return "I need to find both keys";
 		}
 	}
 
 	@Override
 	public boolean isCompleted() {
 		return SceneManager.getInstance().getCurrentGameMapScene().getQuest(3)
 				.isFinished();
 	}
 
 	public void setActive(boolean bool) {
 		super.setActive(bool);
 		ResourcesManager.getInstance().activity.toastOnUIThread(
 				"There it is... the portal!", Toast.LENGTH_LONG);
 		ResourcesManager.getInstance().activity.toastOnUIThread(
 				"But it's not working...looks like I need two keys!",
 				Toast.LENGTH_LONG);
 		ResourcesManager.getInstance().activity.toastOnUIThread(
 				"I should look around, maybe I find something?",
 				Toast.LENGTH_LONG);
 	}
 
 }

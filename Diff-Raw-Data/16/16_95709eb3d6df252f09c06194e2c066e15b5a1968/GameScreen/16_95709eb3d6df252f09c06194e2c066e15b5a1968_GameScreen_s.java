 package se.chalmers.dat255.risk.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.model.Player;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 /**
  * shows the gameboard, including provinces, cards and buttons.
  * 
  */
 public class GameScreen extends AbstractScreen {
 	private boolean isWorld;
 	private AbstractStage worldStage;
 	private List<AbstractStage> cardStages;
 	private UIStage uiStage;
 	private InputMultiplexer multi;
 	private boolean created;
 
 	public GameScreen(IGame model) {
 		super(model);
 
 		isWorld = true;
 
 	}
 
 	@Override
 	public void show() {
 
 		Gdx.input.setInputProcessor(multi);
 	}
 
 	public List<Actor> getSpecActors() {
 		return uiStage.getOthers();
 	}
 
 	public List<AbstractView> getViews() {
 		List<AbstractView> tmp = new ArrayList<AbstractView>();
 
 		tmp.addAll(worldStage.getViews());
 		tmp.addAll(uiStage.getViews());
 		for (AbstractStage s : cardStages) {
 			tmp.addAll(s.getViews());
 		}
 
 		return tmp;
 	}
 
 	@Override
 	public void render(float render) {
 		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 0.7f);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		camera.update();
 		checkStageChange();
 		getStage().draw();
 		uiStage.draw();
		Table.drawDebug(uiStage);
 	}
 
 	private void checkStageChange() {
 		if (isWorld != uiStage.renderWorld()) {
 			changeStage();
 		}
 	}
 
 	public void changeStage() {
 		multi.removeProcessor(getStage().getProcessor());
 		isWorld = !isWorld;
 		multi.addProcessor(getStage().getProcessor());
 
 	}
 
 	private AbstractStage getStage() {
 		return isWorld ? worldStage : cardStages.get(model.getActivePlayer()
 				.getId());
 	}
 
 	public void setupGame() {
 		worldStage = new WorldStage(model.getGameProvinces(),
 				Resource.getInstance().cords);
 
 		// Creates a cardStage for every player
 		cardStages = new ArrayList<AbstractStage>();
 
 		for (Player i : model.getPlayers()) {
 			CardStage stage = new CardStage(i.getCards());
 			i.addListener(stage);
 			cardStages.add(stage);
 		}
 		uiStage = new UIStage(model);
 
 		multi = new InputMultiplexer(uiStage, worldStage.getProcessor());
 		created = true;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (created) {
 			worldStage.dispose();
 			uiStage.dispose();
 			for (AbstractStage s : cardStages) {
 				s.dispose();
 			}
 		}
 	}
 }

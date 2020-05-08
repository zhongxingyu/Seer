 package se.chalmers.dat255.risk.view;
 
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.model.IGame.GameMode;
 import se.chalmers.dat255.risk.model.Player;
 
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 public class StatStage extends AbstractStage {
 
 	private Table main, primary, secondary;
 	private List<PlayerView> players;
 
 	public StatStage(IGame model) {
 		super(model);
		model.addListener(this);
 
 		players = new ArrayList<PlayerView>();
 
 		for (Player p : model.getPlayers()) {
 			players.add(new PlayerView(p,
 					model.getGameMode() == GameMode.SECRET_MISSION ? model
 							.getMissionText(p) : ""));
 		}
 
 		main = new Table();
 		main.setFillParent(true);
 		primary = new Table();
 		secondary = new Table();
 
 		primary.add();
 
 		main.add(primary).expand().fill();
 		main.add(secondary).expand().fill();
 
 		addActor(main);
 
 		placeViews();
 
 	}
 
 	private void placeViews() {
 		for (PlayerView v : players) {
 			if (v.getPlayer() != model.getActivePlayer()) {
 				secondary.add(v).expand().fill().row();
 				v.setMain(false);
 			} else {
 				primary.add(v).expand().fill();
 				v.setMain(true);
 			}
 		}
 	}
 
 	private PlayerView getMain() {
 		for (PlayerView v : players) {
 			if (v.isMain()) {
 				return v;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public InputProcessor getProcessor() {
 		return this;
 	}
 	
 	
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equalsIgnoreCase(IGame.CHANGE_TURN)){
			primary.clearChildren();
			secondary.clearChildren();
 			placeViews();
 		}
 	}
 
 }

 package se.chalmers.dat255.risk.view;
 
 import java.beans.PropertyChangeEvent;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.view.resource.ColorHandler;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 public class UIStage extends AbstractStage {
 
 	private ChangePhase phase;
 	private SwitchButton switchButton;
 	private boolean renderWorld;
 	private Label label;
 	private IGame model;
 	private ColorHandler color;
 	private PopUp pop;
 	private Button giveUp;
 	private Table table, buttonTable;
 
 	public UIStage(IGame model) {
 		this.model = model;
 		model.addListener(this);
 
 		table = new Table();
 		table.setFillParent(true);
 
 		buttonTable = new Table();
 		
 		phase = new ChangePhase(model);
 		others.add(phase);
 
 		switchButton = new SwitchButton();
 		others.add(switchButton);
 
 		renderWorld = true;
 
 		color = ColorHandler.getInstance();
 
 		label = new Label(model.getActivePlayer().getName() + "	\nPhase: "
 				+ model.getCurrentPhase(), Resource.getInstance().skin,
 				"default-font", color.getColor(0));
 		label.setFontScale(label.getFontScaleX() * 1.8f);//TODO magic number
 
 		giveUp = new Button(Resource.getInstance().skin);
 		giveUp.add(new Label("Surrender",Resource.getInstance().skin));
 		others.add(giveUp);
 
 		pop = new PopUp("Title");
 		others.add(pop);
 
 		table.add(label).expandX().center();
 		table.row().expandX().row();
 		buttonTable.columnDefaults(0).expand().fill().width(phase.getLargestWidth());
 		buttonTable.add(giveUp).row();
 		buttonTable.add(switchButton).height(Gdx.graphics.getHeight()/9).row();
 		buttonTable.add(phase).height(Gdx.graphics.getHeight()/9).row();
 		
 		table.add().expand();
 		table.add(buttonTable).bottom();
		table.debug();
 		
 		addActor(table);
 	}
 
 	public void showPopUp(String title, String msg, int maxValue, int minValue) {
 		pop.setSliderStop(minValue, maxValue);
 		pop.setTexts(title, msg);
 		pop.show(this);
 	}
 
 	public boolean renderWorld() {
 		return renderWorld;
 	}
 
 	public void switchRender() {
 		renderWorld = !renderWorld;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equalsIgnoreCase("Attack")) {
 			showPopUp("Attack", "How many dice \ndo you want?",
 					(Integer) event.getOldValue(), 1);
 		} else if (event.getPropertyName().equalsIgnoreCase("Movement")) {
 			showPopUp("Movement", "How many units do \nyou want to move?",
 					(Integer) event.getOldValue() - 1, 1);
 		} else if (event.getPropertyName().equalsIgnoreCase("Again?")) {
 			showPopUp("Again?", "Do you want \nto attack again?",
 					(Integer) event.getOldValue(), 1);
 		} else if (event.getPropertyName().equalsIgnoreCase("takeOver")) {
 			showPopUp("Occupy", "How many units do \nyou want to move?",
 					(Integer) event.getOldValue() - 1,
 					Integer.parseInt((String) event.getNewValue()));
 		} else if (event.getPropertyName().equalsIgnoreCase("Win")) {
 			showPopUp("Congratz", "You have won!", 0, 0);
 		}
 
 	}
 
 	@Override
 	public InputProcessor getProcessor() {
 		return this;
 	}
 
 	@Override
 	public void draw() {
 		label.setText(model.getActivePlayer().getName() + "	\nPhase: "
 				+ model.getCurrentPhase());
 		label.setColor(color.getColor(model.getActivePlayer().getId()));
 		super.draw();
 	}
 
 }

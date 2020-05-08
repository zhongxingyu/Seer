 package se.chalmers.dat255.risk.view;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.model.IGame.Phase;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 
 public class ChangePhase extends AbstractView {
 
 	private IGame model;
 	
 	//By adding more strings here, you can add more behavior
 	// as long as the strings are added in getText as well
 	//public static String PHASE1 = "PHASE1";// placing units, show number of
 											// units left to place and then
 											// "Next Phase"
 //	public static String PHASE2 = "PHASE2";// show "Next Phase"
 	//public static String PHASE3 = "PHASE3";// show "End Turn"
 
 //	private String current = PHASE1;
 
 	private Label label;
 
 	public ChangePhase(IGame model) {
 		super(Resource.getInstance().bucket, Resource.getInstance().bucket);
 		this.model = model;
 		size(width, height);
 		setPosition(Gdx.graphics.getWidth() - getWidth(), 0);
 		label = new Label(getText(), new LabelStyle(font, Color.RED));
 		label.setPosition(getX(), getY() + getHeight() / 2);
 
 	}
 
 	/**
 	 * changes text output. If param isn't correct the text/behavior of
 	 * the class will not be used
 	 * @param s use one of the static strings in this class
 	 */
 /**	public void setCurrent(String s) {
 		current = s;
 		label.setText(getText());
 	}
 
 	public String getCurrent() {
 		return current;
 	}
 */
 	@Override
 	public void check() {
 		super.check();
 		super.check();
 	}
 
 	private String getText() {
 		if ((model.getPhase() == Phase.F1) || (model.getPhase() == Phase.FBuild) ) {
			if(model.getBonusUnitsLeft() == 0){
				return "Next";
			}
 			return "Units: " + model.getBonusUnitsLeft();
 		} else if (model.getPhase() == Phase.F2) {
 			return "Next Phase";
 		} else if (model.getPhase() == Phase.F3) {
 			return "End Turn";
 		}
 		return "value not found";
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float alpha) {
 		label.setText(getText());
 		super.draw(batch, alpha);
 		label.draw(batch, alpha);
 	}
 
 }

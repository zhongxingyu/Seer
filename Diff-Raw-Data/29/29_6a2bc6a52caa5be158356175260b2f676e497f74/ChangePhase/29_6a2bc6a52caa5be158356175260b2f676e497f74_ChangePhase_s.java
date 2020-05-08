 package se.chalmers.dat255.risk.view;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 
 public class ChangePhase extends AbstractView {
 
 	private IGame model;
 	public static String PHASE1 = "PHASE1";// placing units, show number of
 											// units left to place and then
 											// "Next Phase"
 	public static String PHASE2 = "PHASE2";// show "Next Phase"
 	public static String PHASE3 = "PHASE3";// show "End Turn"
	public static String NEXT = "NEXT";// "Next Phase"
 
 	private String current = PHASE1;
 
 	private Label label;
 
 	public ChangePhase(IGame model) {
 		super(Resource.getInstance().bucket, Resource.getInstance().bucket);
 		this.model = model;
 		size(width, height);
		setPosition(Gdx.graphics.getWidth()-getWidth(),0);
 		label = new Label(getText(), new LabelStyle(font, Color.RED));
		label.setPosition(getX(), getY() + getHeight()/2);
 
 	}
 
 	public void setCurrent(String s) {
 		current = s;
 		label.setText(getText());
 	}
	
 	@Override
 	public void check() {
 		super.check();
 		super.check();
 	}
 
 	private String getText() {
 		if (current == PHASE1) {
 			return "Units: " + model.getBonusUnitsLeft();
 		} else if (current == PHASE2) {
 			return "Next Phase";
 		} else if (current == PHASE3) {
 			return "End Turn";
		} else if (current == NEXT) {

 		}

 		return "value not found";
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float alpha) {
 		super.draw(batch, alpha);
 		label.draw(batch, alpha);
 	}
 
 }

 package org.anddev.andengine.memopuzzle.utils;
 
 import org.anddev.andengine.entity.layer.Layer;
 import org.anddev.andengine.opengl.font.Font;
 
 public class ScoreLayer extends Layer {
 	private String mStepLabel = "Step: ";
 	private int mStep;
 	private MyChangeableText mStepText;
 	
 	public ScoreLayer() {
 		final Font font = Enviroment.instance().getFont(2);
 		
 		this.mStep = 0;
 		this.mStepText = new MyChangeableText(20, 20, font, this.mStepLabel + "0");
 		
 		attachChild(this.mStepText);
 	}
 	
 	public void increaseStep(int value) {
 		this.mStep += value;
 		this.mStepText.setText(this.mStepLabel + Integer.toString(this.mStep));
 	}
 	
 }

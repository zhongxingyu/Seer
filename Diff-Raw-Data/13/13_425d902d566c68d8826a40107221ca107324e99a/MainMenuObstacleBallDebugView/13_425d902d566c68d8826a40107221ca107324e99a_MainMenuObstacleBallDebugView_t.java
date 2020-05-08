 package com.karolmajta.stp.views;
 
 import com.karolmajta.stp.models.MainMenuObstacleBall;
 
 import processing.core.PApplet;
 
 public class MainMenuObstacleBallDebugView extends View<MainMenuObstacleBall> {
 	public static final int STROKE = 0xffff00ff;
 	
 	@Override
 	protected void onDraw(PApplet p) {
 		int prevFill = p.g.fillColor;
 		int prevStroke = p.g.strokeColor;
 		p.noFill();
 		p.stroke(STROKE);
 		
 		p.ellipse(
 				model.getX(),
 				model.getY(),
				model.getRadius()*2,
				model.getRadius()*2
 		);
 		p.line(
 				model.getX()+model.getRadius()/2,
 				model.getY(),
 				model.getX()-model.getRadius()/2,
 				model.getY()
 		);
 		p.line(
 				model.getX(),
 				model.getY()+model.getRadius()/2,
 				model.getX(),
 				model.getY()-model.getRadius()/2
 		);
 		
 		p.fill(prevFill);
 		p.stroke(prevStroke);
 	}
 }

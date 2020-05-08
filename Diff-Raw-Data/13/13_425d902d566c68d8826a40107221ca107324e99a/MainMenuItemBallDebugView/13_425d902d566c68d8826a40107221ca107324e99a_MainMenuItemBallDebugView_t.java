 package com.karolmajta.stp.views;
 
 import java.util.Date;
 
 import android.util.Log;
 
 import com.karolmajta.stp.models.MainMenuItemBall;
 
 import processing.core.PApplet;
 
 public class MainMenuItemBallDebugView extends View<MainMenuItemBall> {
 	public static final int STROKE = 0xffff0000;
 	
 	@Override
 	protected void onDraw(PApplet p) {
 		int prevFill = p.g.fillColor;
 		int prevStroke = p.g.strokeColor;
 		
 		p.noFill();
 		p.stroke(STROKE);
 		
 		// ball center marker
 		p.line(
 				model.getCurrentX()+10,
 				model.getCurrentY(),
 				model.getCurrentX()-10,
 				model.getCurrentY()
 		);
 		p.line(
 				model.getCurrentX(),
 				model.getCurrentY()+10,
 				model.getCurrentX(),
 				model.getCurrentY()-10
 		);
 		// ball constrain marker
 		p.line(
 				model.getX0()+10,
 				model.getY0(),
 				model.getX0()-10,
 				model.getY0()
 		);
 		p.line(
 				model.getX0(),
 				model.getY0()+10,
 				model.getX0(),
 				model.getY0()-10
 		);
 		// vector
 		p.line(
 				model.getCurrentX(),
 				model.getCurrentY(),
 				model.getX0(),
 				model.getY0()
 		);
 		// ball edge
 		p.ellipse(
 				model.getCurrentX(),
 				model.getCurrentY(),
				model.getRadius()*2,
				model.getRadius()*2
 		);
 		
 		p.fill(prevFill);
 		p.stroke(prevStroke);
 	}
 }

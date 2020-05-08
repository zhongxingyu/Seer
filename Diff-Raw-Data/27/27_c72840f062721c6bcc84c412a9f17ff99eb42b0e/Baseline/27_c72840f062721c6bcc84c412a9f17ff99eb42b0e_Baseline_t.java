 package LinearArcPlotEditor;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.geom.Line2D;
 
 import javax.swing.JComponent;
 
 import com.clcbio.api.clc.graphics.framework.ChildDrawingNode;
 import com.clcbio.api.clc.graphics.framework.DrawingLayer;
 import com.clcbio.api.clc.graphics.framework.DrawingResult;
 
 public class Baseline extends ChildDrawingNode {
 	
 	Line2D baseLine = new Line2D.Double(0,0,300,0);
 	Stroke stroke = new BasicStroke(2); 
 	private int length;
 	
 	public Baseline(int length){
 		this.length = length;
 		baseLine.setLine(0,316,length,316);
 	}
 	
 	@Override
 	protected DrawingResult internalDraw(Graphics2D g2, boolean drawoutline, DrawingLayer drawinglayer, double minx, double maxx, double miny, double maxy){
 		//updateLength(3);
 		baseLine.setLine(0,316, (int)(length*getScaleX()), 316);
 		g2.setStroke(stroke);
 		g2.draw(baseLine);
 		g2.fill(baseLine);
 		
 		//Draw numbers
		int interval = getIntervalNumber();
 		for(int i=0; i<length; i++){
 			if(i%interval == 0){
 				g2.drawString(Integer.toString(i), (int)(i*getScaleX()), 330);
 			}
 		}
 		return DrawingResult.NORMAL;
 	}
	
	private int getIntervalNumber(){
		double width = length*getScaleX();
		return (int)width/20;
	}
 }

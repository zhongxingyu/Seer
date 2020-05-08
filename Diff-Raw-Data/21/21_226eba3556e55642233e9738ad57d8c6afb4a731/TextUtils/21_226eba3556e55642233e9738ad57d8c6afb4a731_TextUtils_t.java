 package com.episkipoe.common.draw;
 
 import java.util.Collection;
 
 import com.episkipoe.common.Game;
 import com.episkipoe.common.Point;
 import com.episkipoe.common.dialog.DialogElement;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.TextMetrics;
 
 public class TextUtils {
	public static String font = "bold 14px sans-serif";
 	public static int font_height = 13;
 	private static final float pad=4;
 	
 	public static double getTextWidth(Collection<String> message) {
 		double width = 0.0;
 		for(String line : message) {
 			TextMetrics metrics = Game.context.measureText(line);
 			double curWidth = metrics.getWidth();
 			if(curWidth > width) width = curWidth;
 		}
 		return width;
 	}
 	
 	public static double getTextHeight(Collection<String> message) {
 		return font_height*message.size();
 	}
 
 	/**
 	 * @param context
 	 * @param message  
 	 * @param location  The upper-left location of the text
 	 * @param backgroundStyle
 	 * @param textStyle
 	 */
 	public static void drawText(Context2d context, Collection<String> message, Point location, String backgroundStyle, String textStyle) {
 		double width = getTextWidth(message);
 		double height = getTextHeight(message);
 		context.setFont(font);
 	
 		context.setFillStyle(backgroundStyle);
 		context.fillRect(location.x-pad, location.y-font_height-pad, width+pad*2, height+pad*2);
 		
 		context.setFillStyle(textStyle);
 		float y = location.y;
 		for(String line : message) {
 			context.fillText(line, location.x, y);
 			y+=font_height;
 		}
 	}
 	public static void drawWhiteText(Context2d context, Collection<String> message, Point location) {
 		drawText(context, message, location,"rgba(255,255,255,1)", "rgba(0,0,0,0.8)");
 	}
	public static void drawBlackText(Context2d context, Collection<String> message, Point location) {
		drawText(context, message, location, "rgba(0,0,0,1.0)", "rgba(255,255,255,0.8)");
	}
	
 
 	/**
 	 * 
 	 * @param textLocation
 	 * @param message
 	 * @param point
 	 * @return true if point overlaps with the message drawn at textLocation
 	 */
 	public static boolean overlaps(Context2d context, Point textLocation, Collection<String> message, Point point) {
 		float x1 = textLocation.x-pad;
 		if(point.x < x1) return false;
 		double x2 = textLocation.x+getTextWidth(message)+pad;
 		if(point.x > x2) return false;
 		float y1 = textLocation.y-font_height-pad;
 		if(point.y < y1) return false;
 		double y2 =  textLocation.y+getTextHeight(message)+pad;
 		if(point.y > y2) return false;
 		return true;
 	}
 	
 	public static void growl(Collection<String> msg) {
 		if(msg == null || msg.isEmpty()) { return; }
 
 		int x = (int) (Game.canvasWidth*0.5-TextUtils.getTextWidth(msg));
 		int y = (int) (Game.canvasHeight*0.5);
 		Point location = new Point(x,y);
 		Game.room.addDrawable(new DialogElement(msg, location, 80));
 	}	
 }

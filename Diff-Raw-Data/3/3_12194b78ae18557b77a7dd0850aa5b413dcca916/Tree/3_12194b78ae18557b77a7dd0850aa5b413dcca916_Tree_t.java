 package javamal;
 
 //All rights reserved by Han Hyuk Cho 2006, Jaeyeun Yoon 2012.
 
 import java.awt.*;
 import java.util.*;
 import java.io.*;
 import java.awt.image.BufferedImage;
 
 public class Tree {
 
 	double xMin, xMax, yMin, yMax;
 	int width = 400, height = 350;
 	String finalPath;
 	double angle;
 	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	Graphics g = image.createGraphics();
 
 	public Tree(String init, String grammar, int depth, double angle) {
 		finalPath = expandPath(init, grammar, depth);
 		this.angle = angle / (180 / Math.PI);
 		paintTree();
 	}
 
 	public BufferedImage getBufferedImage() {
 		return image;
 	}
 
 	public void paintTree() {
 		getScale(this.angle);
 		g.setColor(Color.white);
 		g.fillRect(0, 0, width, height);
 		g.setColor(Color.blue);
 		drawPath(this.angle, g);
 	}
 
 	public String expandPath(String init, String grammar, int depth) {
 		if (depth <= 0) {
 			return init;
 		}
 		StringWriter sw = new StringWriter();
 		Properties props = new Properties();
 		try {
 			props.load(new ByteArrayInputStream(grammar.getBytes()));
 			StringReader initReader = new StringReader(init);
 			int c;
 			for (int i = 0; i < init.length(); i++) {
 				c = initReader.read();
 				StringWriter sw2 = new StringWriter();
 				sw2.write(c);
 				sw.write(props.getProperty(sw2.toString(), sw2.toString()));
 			}
 		} catch (IOException e) {
 		}
 		if ((depth - 1) > 0) {
 			return expandPath(sw.toString(), grammar, depth - 1);
 		} else {
 			return sw.toString();
 		}
 	}
 
 	public void drawPath(double angle, Graphics graphics) {
 		Tstate cursor = new Tstate();
 		Tstate newCursor = new Tstate();
 		Stack<Tstate> cursorStack = new Stack<Tstate>();
 		int c;
 		StringReader pathReader = new StringReader(finalPath);
 
 		try {
 			for (int i = 0; i < finalPath.length(); i++) {
 				c = pathReader.read();
 				switch (c) {
 				case ('F'):
 				case ('f'):
 					newCursor.x = cursor.x + Math.cos(cursor.angle); 
 					newCursor.y = cursor.y + Math.sin(cursor.angle); 
 					newCursor.angle = cursor.angle;
 					graphics.drawLine(getXCoord(cursor.x), getYCoord(cursor.y),getXCoord(newCursor.x), getYCoord(newCursor.y));
 					cursor.x = newCursor.x;
 					cursor.y = newCursor.y;
 					break;
 				case ('P'):
 				case ('p'):
 					graphics.fillOval(getXCoord(cursor.x) - 2,
 							getYCoord(cursor.y) - 2, 4, 4);
 					break;
 				case ('g'):
 				case ('G'):
 					cursor.x += Math.cos(cursor.angle);
 					cursor.y += Math.sin(cursor.angle);
 					break;
 				case ('<'):
 					cursor.angle += -angle;
 					while (cursor.angle >= (2 * Math.PI))
 						cursor.angle -= (2 * Math.PI);
 					break;
 				case ('>'):
 					cursor.angle -= -angle;
					while(cursor.angle < 0)
						cursor.angle+=(2*Math.PI);
 						break;
 				case ('['):
 					cursor.angle += (2 * Math.PI);	
 					cursorStack.push(new Tstate(cursor));
 					break;
 				case (']'):
 					cursor = (Tstate) cursorStack.pop();
 					break;
 				default:
 					break;
 				}
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	public int getXCoord(double x) {
 		float p = ((float) width) / ((float) (xMax - xMin));
 		return (int) ((x - xMin) * p) + 4;
 
 	}
 
 	public int getYCoord(double y) {
 		float p = ((float) height 
 				)
 				/ ((float) (yMax - yMin));
 		return (int) ((y - yMin) * p) + 5;
 	}
 
 	public void getScale(double angle) {
 		double xMax = 0, xMin = 0, yMin = 0, yMax = 0;
 		Tstate cursor = new Tstate();
 		Stack<Tstate> cursorStack = new Stack<Tstate>();
 		int c;
 		StringReader pathReader = new StringReader(finalPath);
 
 		try {
 			for (int i = 0; i < finalPath.length(); i++) {
 				c = pathReader.read();
 				switch (c) {
 				case ('F'): 
 				case ('f'): 
 				case ('G'): 
 				case ('g'): 
 					cursor.x += Math.cos(cursor.angle);
 					if (cursor.x > xMax)
 						xMax = cursor.x;
 					else if (cursor.x < xMin)
 						xMin = cursor.x;
 					cursor.y += Math.sin(cursor.angle);
 					if (cursor.y > yMax)
 						yMax = cursor.y;
 					else if (cursor.y < yMin)
 						yMin = cursor.y;
 					break;
 				case ('<'):
 					cursor.angle += -angle;
 					while (cursor.angle >= (2 * Math.PI))
 						cursor.angle -= (2 * Math.PI);
 					break;
 				case ('>'):
 					cursor.angle -= -angle;
 					while (cursor.angle < 0)
 						cursor.angle += (2 * Math.PI);
 					break;
 				case ('p'):
 				case ('P'):
 					cursor.type = 10;
 					break;
 				case ('['):
 					cursorStack.push(new Tstate(cursor));
 					break;
 				case (']'):
 					cursor = cursorStack.pop();
 				default:
 					break;
 				}
 			}
 		} catch (IOException e) {
 		}
 		double size = (xMax - xMin);
 		if (size < (yMax - yMin))
 			size = (yMax - yMin);
 		this.xMax = ((xMax + xMin) / 2) + (size / 2);// +1;
 		this.xMin = ((xMax + xMin) / 2) - (size / 2);// -1;
 		this.yMax = ((yMax + yMin) / 2) + (size / 2);// +1;
 		this.yMin = ((yMax + yMin) / 2) - (size / 2);// -1;
 	}
 
 }
 
 class Tstate {
 	double x, y, angle;
 	int type;
 
 	public Tstate() {
 		x = 0;
 		y = 0;
 		angle = -Math.PI / 2;
 		type = 0;
 	}
 
 	public Tstate(Tstate c) {
 		x = c.x;
 		y = c.y;
 		angle = c.angle;
 		type = c.type;
 	}
 
 }

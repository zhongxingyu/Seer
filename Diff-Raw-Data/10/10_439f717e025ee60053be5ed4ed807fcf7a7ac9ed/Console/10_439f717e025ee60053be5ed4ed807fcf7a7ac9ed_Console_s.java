 package com.jpii.navalbattle.renderer;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class Console extends JFrame {
 	private static Console instance = null;
 	public static Console getInstance() {
 		if (instance == null)
 			instance = new Console();
 		return instance;
 	}
 	
 	ArrayList<cnsl_attr> attrs;
 	
 	public Console() {
 		setSize(465,365);
 		setLocation(0,380);
 		setTitle("Debug Rendering Console");
 		setVisible(true);
 		attrs = new ArrayList<cnsl_attr>();
 		attrs.add(new cnsl_attr("Started Console!",0));
 	}
 	public void paint(Graphics g2) {
 		BufferedImage b = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
 		Graphics g3 = b.getGraphics();
 		Graphics2D g = (Graphics2D)g3;
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		int h = 30;
 		
 		g.setColor(Color.black);
 		g.fillRect(0,0,getWidth(),getHeight());
 		g.setFont(Helper.GUI_GAME_FONT);
 		g.setColor(new Color(70,70,70));
 		g.drawLine(0,18+h,getWidth(),18+h);
 		g.setColor(new Color(40,40,40));
 		g.drawLine(0,19+h,getWidth(),19+h);
 		
 		g.setColor(Color.white);
 		g.drawString("Seed: " + seed + ". FPS: " + fps,10,44);
 		
 		int buffer = h+(18*2);
 		if (attrs == null)
 			return;
 		for (int c = 0; c < attrs.size(); c++) {
 			if (attrs.size() - c - 1 < attrs.size()) {
 				cnsl_attr a = attrs.get(attrs.size() - c - 1);
 				if (a == null)
 					continue;
 				///int s = getWidthCnsl(g,a);
 				//int f = s / getWidth();
 				//f++;
 				if (a.type == 0) {
 					g.setColor(Color.yellow);
 				}
 				else if (a.type == 1) {
 					g.setColor(Color.red);
 				}
 				else
 					g.setColor(Color.green);
 				g.drawString(a.v, 10, buffer);
 				buffer += 20;	
 			}
 		}
 		g2.drawImage(b, 0, 0, null);
 	}
 	int seed = 0;
 	int fps = 0;
 	public void setSeed(int seed) {
 		this.seed = seed;
 	}
 	public void setFPS(int fps) {
 		this.fps = fps;
 		repaint();
 	}
 	public void printWarn(String v) {
 		attrs.add(new cnsl_attr(v,0));
 		repaint();
 	}
 	public void printError(String v) {
 		attrs.add(new cnsl_attr(v,1));
 		repaint();
 	}
 	public void printInfo(String v) {
 		attrs.add(new cnsl_attr(v,2));
 		repaint();
 	}
 	private int getWidthCnsl(Graphics2D g,cnsl_attr ams) {
 		FontMetrics metrics = g.getFontMetrics(Helper.GUI_GAME_FONT);
 		return metrics.stringWidth(ams.v);
 	}
 	public void update(Graphics g) {
 		paint(g);
 	}
 }
 class cnsl_attr {
 	public String v;
 	public int type;
 	public cnsl_attr(String v, int type) {
 		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
         Date date = new Date();
         String h =  dateFormat.format(date);
 		this.v = "[" + h + "]: " + v;
 		this.type = type;
 	}
 
 }

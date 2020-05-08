 package com.voracious.dragons.client.graphics.ui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.voracious.dragons.client.graphics.Drawable;
 
 public class Button implements Drawable {
 
 	public static final int ACTION_BUTTON_PRESSED = 1;
 	
 	public static final Color defaultBackground = new Color(0xDDDDDD);
 	public static final Color defaultBoarder = new Color(0xEEEEEE);
 	public static final int defaultPadding = 5;
 	
 	private int x, y;
 	private int width, height;
 	private Text text;
 	private BufferedImage image;
 	
 	private List<ActionListener> listeners;
 	
 	public Button(String text,int x,int y) {
 		listeners = new LinkedList<ActionListener>();
 		
 		this.text = new Text(text, defaultPadding, defaultPadding);
 
 		width = this.text.getWidth() + defaultPadding*2;
 		height = this.text.getHeight() + defaultPadding*2;
 		
 		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D ig = (Graphics2D) image.getGraphics();
 		
 		ig.setColor(defaultBackground);
 		ig.fillRect(0, 0, width, height);
 		
 		ig.setColor(defaultBoarder);
 		ig.drawRect(0, 0, width, height);
 		
 		this.x = x;
 		this.y = y;
 		
 		this.text.draw(ig);
 	}
 	
 	public void addActionListener(ActionListener l){
 		listeners.add(l);
 	}
 	
 	public void mouseClicked(int x, int y){
		if(x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height){
 			Iterator<ActionListener> it = listeners.iterator();
 			while(it.hasNext()){
 				it.next().actionPerformed(new ActionEvent(this, ACTION_BUTTON_PRESSED, "pressed"));
 			}
 		}
 	}
 	
 	@Override
 	public void draw(Graphics2D g) {
 		g.drawImage(image, null, x, y);
 	}
 }

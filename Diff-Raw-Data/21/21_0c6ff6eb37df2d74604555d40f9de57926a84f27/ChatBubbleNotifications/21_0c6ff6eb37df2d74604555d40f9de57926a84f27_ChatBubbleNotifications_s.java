 package com.friendlyblob.mayhemandhell.client.entities.gui;
 
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.friendlyblob.mayhemandhell.client.entities.GameObject;
 import com.friendlyblob.mayhemandhell.client.helpers.Assets;
 
 public class ChatBubbleNotifications {
 	
 	private static final Color REGULAR_COLOR = new Color(0.05f, 0.55f, 0.8f, 1);
 	private static final Color TV_BG_COLOR = new Color(0.05f, 0.42f, 0.8f, 1);
 	
 	private static final int BUFFER_SIZE = 15;
 	private static final float MAX_TIMER = 5; 
 	private static final int BUBBLE_MAX_WIDTH = 100;
 	
 	private static final int H_PADDING = 6;
 	private static final int V_PADDING = 2;
 	
 	private String[] notifications;
 	private Color[] fontColors;
 	private Color[] bgColors;
 	private float[] timers;
 	private int[] widths;
 	private int[] heights;
 	private Rectangle[] hitboxes;
 	
 	private int count;
 	
 	private BitmapFont font;
 	
 	private NinePatch patch;
 	private TextureRegion arrow;
 	
 	public ChatBubbleNotifications() {
 		
 		arrow = Assets.getTextureRegion("gui/ingame/speech_arrow");
 		patch = Assets.getNinePatch("gui/ingame/speech");
 		font = Assets.defaultFont;
 		notifications = new String[BUFFER_SIZE];
 		fontColors = new Color[BUFFER_SIZE];
 		bgColors = new Color[BUFFER_SIZE];
 		timers = new float[BUFFER_SIZE];
 		widths = new int[BUFFER_SIZE];
 		heights = new int[BUFFER_SIZE];
 		hitboxes = new Rectangle[BUFFER_SIZE];
 		
 		for (int i = 0; i < BUFFER_SIZE; i++) {
 			notifications[i] = "";
 			fontColors[i] = Color.BLACK;
 		}
 		
 		count = 0;
 	}
 	
 	public void draw(SpriteBatch spriteBatch) {
 		if (count <= 0) {
 			return;
 		}
 
 		int x = 0;
 		int y = 0;
 		
 		for (int i = 0; i < BUFFER_SIZE; i++) {	
 			if (timers[i] > 0) {
 				x = (int)( hitboxes[i].x + hitboxes[i].width/2 - widths[i]/2);
 				y = (int)(hitboxes[i].y + hitboxes[i].height + hitboxes[i].height/2 - 4);
 				
 				spriteBatch.setColor(bgColors[i]);
 				patch.draw(spriteBatch, x - H_PADDING, y - V_PADDING, widths[i] + H_PADDING * 2, heights[i] + V_PADDING*2);
 				spriteBatch.draw(arrow, x + widths[i]/2, y - 5);
 				
 				font.setColor(fontColors[i]);
 				font.drawWrapped(spriteBatch, notifications[i], x, y + heights[i] -2,BUBBLE_MAX_WIDTH);
 			}
 		}
 		// Restore regular color
 		font.setColor(Color.WHITE);
 		spriteBatch.setColor(Color.WHITE);
 	}
 	
 	public void update(float deltaTime) {
 		for (int i = 0; i < BUFFER_SIZE; i++) {
 			if (timers[i] > 0) {
 				//-------------------------------------
 				// UPDATE
 				timers[i] -= deltaTime;
 
 				if (timers[i] <= 0) {
 					count--;
 				}
 			}
 		}
 	}
 	
	public synchronized void addNotification (String notification, Rectangle hitbox, Color fontColor, Color bgColor) {
 		int oldest = 0;
 		for (int i = 0; i < BUFFER_SIZE; i++) {
 			if (timers[i] < timers[oldest]) {
 				oldest = i;
 			}
 			if(hitboxes[i] == hitbox) {
 				oldest = i;
 				break;
 			}
 		}
 		
 		TextBounds bounds = font.getWrappedBounds(notification, BUBBLE_MAX_WIDTH);
 		
 		// Pushing notification
		this.timers[oldest] = MAX_TIMER;
 		this.notifications[oldest] = notification;
 		this.fontColors[oldest] = fontColor;
 		this.bgColors[oldest] = bgColor;
 		this.widths[oldest] = (int)bounds.width;
 		this.heights[oldest] = (int)bounds.height;
 		this.hitboxes[oldest] = hitbox;
 		count++;
 		
 		Sound sound = Assets.manager.get("sounds/chat-notification.mp3");
 		sound.play();
 	}
 	
 	public void addRegularNotification(String notification, Rectangle hitbox) {
		addNotification(notification, hitbox, REGULAR_COLOR, Color.WHITE);
 	}
 	
 	public void addTvNotification(String notification, Rectangle hitbox) {
		addNotification(notification, hitbox, Color.WHITE, TV_BG_COLOR);
 	}
 	
 }

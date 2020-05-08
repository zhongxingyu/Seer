 package com.friendlyblob.mayhemandhell.client.entities;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.friendlyblob.mayhemandhell.client.entities.gui.ChatBubbleNotifications;
 
 public class TopicTv extends GameObject {
 	
 	private ChatBubbleNotifications chatBubbleNotifications;
 	
 	public TopicTv() {
 		super(-1);
 		
		setPosition(100, 100);
 		
 		chatBubbleNotifications = new ChatBubbleNotifications(this);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		chatBubbleNotifications.update(deltaTime);
 	}
 	
 	@Override
 	public void draw(SpriteBatch spriteBatch) {
 		spriteBatch.setColor(Color.BLACK);
 
 		chatBubbleNotifications.draw(spriteBatch);
 
 		spriteBatch.setColor(Color.WHITE);
 	}
 	
 	public void setTopic(String topic) {
 		chatBubbleNotifications.addTvNotification(topic);
 	}
 }

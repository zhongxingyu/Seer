 package com.arenz.spriteeditor.ui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 
 import com.arenz.spriteeditor.model.Sprite;
 import com.arenz.spriteeditor.ui.components.ImagePanel;
 
 public class SpriteConfigurationView {
 	private JPanel mainPanel = new JPanel();
 	private ImagePanel spriteView = new ImagePanel(null);
 	private Sprite sprite;
 	
 	public SpriteConfigurationView() {
 		mainPanel.add(spriteView);
		mainPanel.setMinimumSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - Constants.SPRITE_PANEL_HEIGHT*3));
		mainPanel.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - Constants.SPRITE_PANEL_HEIGHT*3));
 		
 		mainPanel.revalidate();
 		mainPanel.repaint();
 	}
 	
 	public void displayNewSprite(Sprite sprite) {
 		mainPanel.remove(spriteView);
 		this.sprite = sprite;
 		this.spriteView = new ImagePanel(sprite.getImage());
 		mainPanel.add(spriteView);
 
 		mainPanel.revalidate();
 		mainPanel.repaint();
 	}
 
 	public Component getMainComponent() {
 		return mainPanel;
 	}
 }

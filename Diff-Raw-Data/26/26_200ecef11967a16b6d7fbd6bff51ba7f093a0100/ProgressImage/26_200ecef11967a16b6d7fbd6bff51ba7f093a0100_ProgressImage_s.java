 /* Acid - Provides a Java cell API to display fancy cell boxes.
  * Copyright (C) 2013  Miguel Gonzalez
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
  */
 
 package de.myreality.acidsnake.ui;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 
 import de.myreality.acidsnake.Resources;
 import de.myreality.acidsnake.core.Player;
 
 /**
  * Shows the progress of the current level
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 1.3
  * @version 1.3
  */
 public class ProgressImage extends Actor {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	
 	private static final int HEIGHT = 6;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private double progress;
 	
 	private Image image, background;
 	
 	private Player player;
 	
 	private float currentWidth;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public ProgressImage(Player player) {
 		progress = player.getProgress();
 		currentWidth = 0;
 		this.player = player;
 		Pixmap map = new Pixmap(HEIGHT, HEIGHT, Format.RGBA4444);
 		map.setColor(Resources.COLOR_GREEN);
 		map.fill();
 		Texture texture = new Texture(map);		
 		map.setColor(Resources.COLOR_VIOLET);
 		map.fill();
 		Texture backgroundTexture = new Texture(map);
 		map.dispose();
 		image = new Image(texture);
 		background = new Image(backgroundTexture);
 		image.setY(Gdx.graphics.getHeight() - HEIGHT);
 		background.setY(Gdx.graphics.getHeight() - HEIGHT);
 		image.setWidth(Gdx.graphics.getWidth());
 		background.setWidth(Gdx.graphics.getWidth());
 		image.setHeight(HEIGHT);
 		background.setHeight(HEIGHT);
 	}
 
 	// ===========================================================
 	// Getters and Setters
 	// ===========================================================
 	
 	public void setProgress(double progress) {
 		this.progress = progress;
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		setProgress(player.getProgress());
 		float progressWidth = (float) (Gdx.graphics.getWidth() * progress);
 		
 		if (currentWidth < progressWidth) {
 			currentWidth += (progressWidth - currentWidth) / 20;
 		} else {
			currentWidth = progressWidth;
 		}
 		background.setX(currentWidth);
 		image.setX(-image.getWidth() + background.getX());
 		super.draw(batch, parentAlpha);
 		background.draw(batch, parentAlpha);
 		image.draw(batch, parentAlpha);
 	}
 
 	@Override
 	public void setColor(Color color) {
 		super.setColor(color);
 		image.setColor(color);
 		background.setColor(color);
 	}
 
 	@Override
 	public void setColor(float r, float g, float b, float a) {
 		super.setColor(r, g, b, a);
 		image.setColor(r, g, b, a);
 		background.setColor(r, g, b, a);
 	}
 
 	// ===========================================================
 	// Methods from Superclass
 	// ===========================================================
 	
 	
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 }

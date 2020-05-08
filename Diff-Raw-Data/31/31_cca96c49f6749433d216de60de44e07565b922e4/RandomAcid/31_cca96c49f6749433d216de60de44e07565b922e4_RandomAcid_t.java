 /* AcidSnake - Snake game using Acid
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
 
 package de.myreality.acidsnake.graphics;
 
 import com.badlogic.gdx.graphics.Color;
 
 import de.myreality.acid.Acid;
 import de.myreality.acid.BufferedRenderer;
 import de.myreality.acidsnake.Resources;
 import de.myreality.acidsnake.util.ColorFader;
 import de.myreality.acidsnake.util.Timer;
 
 /**
  * 
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 
  * @version 
  */
 public class RandomAcid extends Acid {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	
	private static final int DURATION = 400;
 	
 	private CellEffect fadeEffect;
 	
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public RandomAcid(BufferedRenderer renderer) {
 		super(renderer);
 	}
 	
 	// ===========================================================
 	// Getters and Setters
 	// ===========================================================
 
 	// ===========================================================
 	// Methods from Superclass
 	// ===========================================================
 	
 	@Override
 	public void render() {	
 		
 		if (fadeEffect == null || fadeEffect.isDone()) {
			
			if (fadeEffect != null) {
				fadeEffect.close();
			}
			
 			fadeEffect = new CellEffect(this, Resources.COLOR_GREEN, Resources.COLOR_VIOLET);
 		}
 		
 		fadeEffect.update();
 		
 		super.render();
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 	
 	class CellEffect {
 		
 		private Color targetColor;
 		
 		private Timer timer, refreshTimer;
 		
 		private ColorFader fader;
 		
		private static final int REFRESH_INTERVAL = 20;
 		
 		private int indexX, indexY;
 		
 		private Acid acid;
 		
 		public CellEffect(Acid acid, String ... colors) {
 			targetColor = generateRandomColor(colors);
 			timer = new Timer();
 			refreshTimer = new Timer();
 			this.acid = acid;
 			indexX = getRandomIndexX();
 			indexY = getRandomIndexY();
 			
			fader = new ColorFader(0.1f, 0.1f, 0.1f, targetColor.r, targetColor.g, targetColor.b);
 		}
 		
 		public void update() {
 			
 			if (!timer.isRunning()) {
 				timer.start();
 			}
 			
 			if (!refreshTimer.isRunning()) {
 				refreshTimer.start();
 			}
 			
 			if (refreshTimer.getTicks() >= REFRESH_INTERVAL) {
 				fader.setRatio(timer.getTicks() / (float) DURATION);
 				Color color = fader.getColor();
 				acid.color(color.r, color.g, color.b);
 				acid.put(indexX, indexY);
 				refreshTimer.reset();
 			}
 		}
 		
		public void close() {
			acid.clear(indexX, indexY);
		}
		
 		public boolean isDone() {
 			return timer.getTicks() >= DURATION;
 		}
 		
 		private Color generateRandomColor(String ... colorCollection) {
 			String selected = colorCollection[(int)(colorCollection.length * Math.random())];
 			return Color.valueOf(selected);
 		}
 		
 		private int getRandomIndexX() {
 			return (int) (Math.random() * acid.getIndexX());
 		}
 		
 		private int getRandomIndexY() {
 			return (int) (Math.random() * acid.getIndexY());
 		}
 	}
 }

 /*
  * MonteCarloLocalizerRender.java
  * 
   * Copyright (C) 2013  Pavel Prokhorov (pavelvpster@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
 */
 package map.localization;
 
 import common.ui.view.Render;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import map.LocationRender;
 
 /**
  * Рендер локализатора.
  * 
  * @author pavelvpster
  * 
  */
 public final class MonteCarloLocalizerRender implements Render {
 
 	/**
 	 * Конструктор по умолчанию (параметризованный).
 	 * 
	 * @param localizer локализатор.
 	 * 
 	 */
 	public MonteCarloLocalizerRender(MonteCarloLocalizer tracker) {
 		
 		this.localizer = tracker;
 	}
 	
 	
 	/**
 	 * Локализатор.
 	 * 
 	 */
 	private final MonteCarloLocalizer localizer;
 	
 	
 	@Override
 	public void render(Graphics2D G) {
 
 		// Все, что касается гипотез, рисуем красным
 		
 		G.setColor(Color.RED);
 		
 		// Отображаем гипотезы
 		
 		for (Particle p : localizer.particles) {
 			
 			// Обозначаем положение
 			
 			int cx =                                    p.location.point.x - localizer.map.bounds.x;
 			int cy = localizer.map.bounds.height - 1 - (p.location.point.y - localizer.map.bounds.y);
 			
 			G.drawLine(cx, cy, cx, cy);
 			
 			// G.fillRect(cx - 1, cy - 1, 2, 2);
 		}
 
 		// Отображаем найденное положение робота
 
 		if (localizer.location != null) {
 			
 			new LocationRender(localizer.location, localizer.map).render(G);
 		}
 	}
 	
 }

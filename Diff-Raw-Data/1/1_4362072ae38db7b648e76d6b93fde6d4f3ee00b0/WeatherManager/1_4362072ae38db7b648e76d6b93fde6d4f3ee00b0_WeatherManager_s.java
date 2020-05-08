 package com.jpii.navalbattle.renderer.weather;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 
 public class WeatherManager {
 	WeatherMode wm;
 	BufferedImage buffer;
 	RainDrop[] rain;
 	public WeatherManager() {
 		wm = WeatherMode.Sunny;
 		repopulateRain();
 	}
 	public void setWeather(WeatherMode wm) {
 		this.wm = wm;
 		update();
 	}
 	public WeatherMode getWeather() {
 		return wm;
 	}
 	private void repopulateRain() {
 		rain = new RainDrop[Game.Settings.rand.nextInt(150,200)];
 		int dir = Game.Settings.rand.nextInt(-6,6);
 		for (int r = 0; r < rain.length; r++) {
 			rain[r] = new RainDrop(dir,Game.Settings.currentWidth,Game.Settings.currentHeight);
 		}
 	}
 	private int lightticks = 0;
 	private boolean lighting = false;
 	public void update() {
 		if (getWeather() == WeatherMode.Raining) {
 			buffer = new BufferedImage(Game.Settings.currentWidth,
 					Game.Settings.currentHeight,BufferedImage.TYPE_INT_ARGB);
 			Graphics2D g = PavoHelper.createGraphics(buffer);
 			g.setStroke(new BasicStroke(2.5f));
 			lightticks += 1;
 			if (lightticks > 9) {
 				lightticks = 0;
 				lighting = false;
 			}
 			if (lighting) {
 				g.setColor(new Color(237,234,222,lightticks*100/9));
 				g.fillOval(Game.Settings.currentWidth-125,-Game.Settings.currentHeight,500,Game.Settings.currentHeight*2);
 			}
 			if (Game.Settings.rand.nextInt(0,30) == 10 && !lighting) {
 				lighting = true;
 				lightticks = 0;
 			}
 			for (int r = 0; r < rain.length; r++) {
 				RainDrop rd = rain[r];
 				int inc = rd.length / 5;
 				int sws = rd.dir;
 				rd.y1 += inc;
 				rd.y2 += inc;
 				rd.x1 += sws;
 				rd.x2 += sws;
 				rd.x1 += prefixx;
 				rd.x2 += prefixx;
 				rd.y1 += prefixy;
 				rd.y2 += prefixy;
 				if (rd.y1 > Game.Settings.currentHeight) {
 					rd.y1 -= (Game.Settings.currentHeight + 60);
 					rd.y2 -= (Game.Settings.currentHeight + 60);
 				}
 				if (sws < 0 && rd.x2 < 0) {
 					rd.x1 += Game.Settings.currentWidth;
 					rd.x2 += Game.Settings.currentWidth;
 				}
 				if (sws > 0 && rd.x1 > Game.Settings.currentWidth) {
 					rd.x1 -= Game.Settings.currentWidth;
 					rd.x2 -= Game.Settings.currentWidth;
 				}
 				if (prefixx > 0 && rd.x1 > Game.Settings.currentWidth) {
 					rd.x1 -= Game.Settings.currentWidth;
 					rd.x2 -= Game.Settings.currentWidth;
 				}
 				if (prefixx < 0 && rd.x1 < 0) {
 					rd.x1 += Game.Settings.currentWidth;
 					rd.x2 += Game.Settings.currentWidth;
 				}
 					
 				rain[r] = rd;
 				g.setColor(rd.colour);
 				g.drawLine(rd.x1,rd.y1,rd.x2,rd.y2);
 			}
 			prefixx = 0;
 			prefixy = 0;
 		}
 		else
 			buffer = null;
 	}
 	public BufferedImage getBuffer() {
 		if (getWeather() == WeatherMode.Sunny)
 			return null;
 		else
 			return buffer;
 	}
 	int prefixx = 0;
 	int prefixy = 0;
 	public void applyFix(int x, int y) {
 		prefixx = x;
 		prefixy = y;
 	}
 }

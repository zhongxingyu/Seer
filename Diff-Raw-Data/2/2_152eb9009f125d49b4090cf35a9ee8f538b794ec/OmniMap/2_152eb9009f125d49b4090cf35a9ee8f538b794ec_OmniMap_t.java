 package com.jpii.navalbattle.pavo;
 
 import java.awt.event.*;
 import java.awt.image.*;
 import java.awt.*;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 public class OmniMap extends Renderable {
 	int mx,my;
 	World w;
 	BufferedImage terrain;
 	public OmniMap(World w) {
 		super();
 		this.w = w;
 		setSize(100,100);
 		buffer = (new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_RGB));
 		terrain = (new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_RGB));
 		writeBuffer();
 	}
 	public void mouseMoved(MouseEvent me) {
 		mx = me.getX();
 		my = me.getY();
 		
 		render();
 	}
 	public void writeBuffer() {
 		Graphics2D g = PavoHelper.createGraphics(terrain);
 		Rand rand = new Rand(Constants.MAIN_SEED);
 		for (int x = 0; x < 100/3; x++) {
 			for (int y = 0; y < 100/3; y++) {
 				int strx = x * PavoHelper.getGameWidth(w.getWorldSize());
 				int stry = y * PavoHelper.getGameHeight(w.getWorldSize());
 				float frsh = McRegion.getPoint(strx,stry);
 				float lsy = (float) ((frsh - 0.3)/0.21);
 				if (lsy > 1)
 					lsy = 1;
 				if (lsy < 0)
 					lsy = 0;
 				int nawo = rand.nextInt(-5, 8);
 				if (lsy < 0.4) {
 					int rgs = Helper.colorSnap((int)(lsy*102));
 					g.setColor(new Color(63+rand.nextInt(-7,7),60+rand.nextInt(-7,7),rand.nextInt(90, 100)+rgs));
 				}
 				else if (lsy < 0.55) {
 					Color base1 = PavoHelper.Lerp(RenderConstants.GEN_SAND_COLOR,new Color(52,79,13),((lsy-0.4)/0.15));
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 				}
 				else{
 					Color base1 = PavoHelper.Lerp(new Color(52,79,13),new Color(100,92,40),((lsy-0.55)/0.45));
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 				}
 				g.fillRect(x*3,y*3,4,4);
 				//g.drawLine(x,y,x,y);
 			}
 		}
 	}
 	public void render() {
 		Graphics2D g = PavoHelper.createGraphics(getBuffer());
 		g.drawImage(terrain, 0,0,null);
 		int rwx = (int) (Math.abs(w.getScreenX()-(DynamicConstants.WND_WDTH/2)) * 33.333333 / (PavoHelper.getGameWidth(w.getWorldSize()) * 100))*3;
 		int rwy = (int) (Math.abs(w.getScreenY()-(DynamicConstants.WND_WDTH/2)) * 33.333333 / (PavoHelper.getGameHeight(w.getWorldSize()) * 100))*3;
 		int sw = (int)((PavoHelper.getGameWidth(w.getWorldSize()) * 100)/DynamicConstants.WND_WDTH);
 		int sh = (int)((PavoHelper.getGameHeight(w.getWorldSize()) * 100)/DynamicConstants.WND_HGHT);
 		g.setColor(Color.red);
 		g.drawRect(rwx-1,rwy-1,sw,sh);
		//GraphicsLib.drawThick3DRect(g, 0,0,getWidth()-1,getHeight()-1,5);
 		//g.draw3DRect(0,0,getWidth()-1,getHeight()-1,true);
 	}
 }

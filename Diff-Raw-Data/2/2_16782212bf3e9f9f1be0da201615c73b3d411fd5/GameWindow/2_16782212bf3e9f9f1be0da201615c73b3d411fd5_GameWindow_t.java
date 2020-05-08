 /**
  * 
  */
 package com.jpii.navalbattle.pavo;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.renderer.Helper;
 
 /**
  * @author MKirkby
  *
  */
 public class GameWindow extends Renderable {
 	Color bck_clr;
 	boolean showTitle;
 	String title;
 	boolean visible;
 	WindowManager prent;
 	public GameWindow() {
 		bck_clr = new Color(193,172,134);
 		showTitle = true;
 		title = "GameWindow";
 		visible = true;
 		prent = WindowManager.Inst;
 	}
 	public WindowManager getWinMan() {
 		return prent;
 	}
 	public void setWinMan(WindowManager wm) {
 		prent = wm;
 	}
 	public void render() {
 		buffer = new BufferedImage(getWidth()+1,getHeight()+1,BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		g.setColor(Color.black);
 		g.drawRect(0,0,getWidth()-1,getHeight()-1);
 		g.setColor(getBackgroundColor());
 		g.fillRect(1,1,getWidth()-2,getHeight()-2);
 		if (isTitleShown()) {
 			g.setColor(getBackgroundColor().darker().darker());
 			g.fillRect(1,1,getWidth()-2,24);
 			g.setColor(Color.black);
 			g.drawRect(0,0,getWidth()-1,24);
 			BufferedImage adapter = new BufferedImage(getWidth()-2,24,BufferedImage.TYPE_INT_ARGB);
 			Graphics2D g2 = PavoHelper.createGraphics(adapter);
			g2.setColor(Color.white);
 			g2.setFont(Helper.GUI_GAME_FONT);
 			g2.drawString(title,3,20);
 			g.drawImage(adapter, 1,1, null);
 			g.setColor(new Color(126,105,65));
 			g.fillRect(getWidth()-23,2,20,20);
 			g.setColor(Color.black);
 			g.drawRect(getWidth()-23,2,20,20);
 			g.setColor(Color.white);
 			g.drawLine(getWidth()-20,5,getWidth()-6,19);
 			g.drawLine(getWidth()-6,5,getWidth()-20,19);
 		}
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String titl) {
 		title = titl;
 	}
 	public boolean isTitleShown() {
 		return showTitle;
 	}
 	public void setTitleVisiblity(boolean b) {
 		showTitle = b;
 	}
 	public Color getBackgroundColor() {
 		return bck_clr;
 	}
 	public void setBackgroundColor(Color colour) {
 		bck_clr = colour;
 	}
 	public void setWidth(int w) {
 		width = w;
 		render();
 	}
 	public void setHeight(int h) {
 		height = h;
 		render();
 	}
 	public void setSize(int w, int h) {
 		width = w;
 		height = h;
 		render();
 	}
 	public void setVisible(boolean vale) {
 		visible = vale;
 	}
 	public boolean isVisible() {
 		return visible;
 	}
 	public void onCloseCalled() {
 		setVisible(false);
 	}
 	int x,y;
 	public int getX() {
 		return x;
 	}
 	public int getY() {
 		return y;
 	}
 	public void setLoc(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 	boolean shutdown = false;
 	public boolean needsShutdown() {
 		return shutdown;
 	}
 	public void forceShutdown() {
 		shutdown = true;
 	}
 	public void mouseDown(MouseEvent me) {
 		boolean flag = false;
 		int mx = me.getX();
 		int my = me.getY();
 		if (isTitleShown() && isVisible()) {
 			if (mx >= getWidth()-23+getX() && mx <= getWidth()-3+getX() && my >= getY() + 2 && my <= getY() + 20) {
 				onCloseCalled();
 				forceShutdown();
 			}
 			checkOtherDown(me);
 		}
 	}
 	public void mouseMove(MouseEvent me) {
 	}
 	public void mouseUp(MouseEvent me) {
 	}
 	public boolean checkOtherDown(MouseEvent me) {
 		return false;
 	}
 }

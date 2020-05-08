 package com.adavieslyons.zombia.util;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import com.adavieslyons.zombia.entity.EntityManager;
 import com.adavieslyons.zombia.entity.Player;
 import com.adavieslyons.zombia.item.Gun;
 import com.adavieslyons.zombia.item.Magnum;
 import com.adavieslyons.zombia.item.Pistol;
 
 public class Shop {
 	public ArrayList<Gun> guns;
 	private Image shopBackground;
 	private Image shopHoverBackground;
 	private Image shopButton;
 	private Image shopButtonDown;
 	private Player player;
 	private int gunSelected;
 	
 	public Shop(EntityManager eManager, Player player) throws SlickException {
 		guns = new ArrayList<Gun>();
 		
 		guns.add(new Pistol(eManager));
 		guns.add(new Magnum(eManager));
 		gunSelected = 0;
 		
 		shopBackground = new Image("resource/img/shop.png");
 		shopHoverBackground = new Image("resource/img/shop_hover.png");
 		shopButton = new Image("resource/img/shop_button.png");
 		shopButtonDown = new Image("resource/img/shop_button_down.png");
 		
 		this.player = player;
 	}
 	
 	public void update(GameContainer gc, int delta) {
 		int id = 0;
 		
 		int tX = getLocalX(gc);
 		int tY = getLocalY(gc);
 		
 		for (Gun gun : guns) {
 			int tXL = tX + 6;
 			int tYL = tY + 5 + 71 * id;
 			
 			int bXL = tXL + shopHoverBackground.getWidth();
 			int bYL = tYL + shopHoverBackground.getHeight();
 			
 			if (gc.getInput().isMouseButtonDown(0) && mouseInside(gc, tXL, tYL, bXL, bYL)) {
 				// Load the gun into the big selection
 				gunSelected = id;
 			}
 			
 			id++;
 		}
 		
 		if (gc.getInput().isMouseButtonDown(0) && mouseInside(gc, 400, tY + 500, 400 + shopButton.getWidth(), tY + 500 + shopButton.getHeight())) {
			if (player.getMoney() >= guns.get(gunSelected).getPrice() && !player.hasGun(guns.get(gunSelected).getClass())) {
 				buy(gunSelected);
 			}
 		}
 	}
 	
 	public void render(GameContainer gc, Graphics graphics) {
 		graphics.drawImage(shopBackground, getLocalX(gc), getLocalY(gc));
 		
 		int id = 0;
 		
 		int tX = getLocalX(gc);
 		int tY = getLocalY(gc);
 		
 		for (Gun gun : guns) {
 			int tXL = tX + 6;
 			int tYL = tY + 5 + 71 * id;
 			
 			int bXL = tXL + shopHoverBackground.getWidth();
 			int bYL = tYL + shopHoverBackground.getHeight();
 			
 			if (mouseInside(gc, tXL, tYL, bXL, bYL)) {
 				graphics.drawImage(shopHoverBackground, tXL, tYL);
 			}
 			
 			// 82/62 calculated with the y 62 and the magic ratio 1.32... which is the ratio of width/height of the thumbnails
 			graphics.drawImage(gun.getThumbnail(), tXL, tYL, tXL + 82 + 6,  tYL + 62, 0, 0, gun.getThumbnail().getWidth(), gun.getThumbnail().getHeight(), Color.white);
 			
 			graphics.setColor(Color.yellow);
 			graphics.drawString(gun.getShopName(), tXL + 86 + 6, tYL + 5);
 			
 			if (player.hasGun(gun.getClass())) {
 				graphics.setColor(Color.red);
 				graphics.drawString("owned", tXL + 87, tYL + 23);
 			}
 			else
 			{
 				graphics.setColor(Color.yellow);
 				graphics.drawString("$" + gun.getPrice(), tXL + 87, tYL + 23);
 			}
 			
 			id++;
 		}
 		
 		// Draw selected gun information on the right hand side of the shop
 		Gun selectedGun = guns.get(gunSelected);
 		
 		graphics.drawImage(selectedGun.getThumbnail(), tX + 217, tY + 5);
 		graphics.setColor(Color.yellow);
 		graphics.drawString(selectedGun.getShopName(), tX + 217 + 12, tY + 261 + 12);
 		
 		if (!mouseInside(gc, 400, tY + 500, 400 + shopButton.getWidth(), tY + 500 + shopButton.getHeight())) {
 			graphics.drawImage(shopButton, 400, tY + 500);
 		} else {
 			graphics.drawImage(shopButtonDown, 400, tY + 500);
 		}
 		
 		graphics.setColor(Color.yellow);
 		graphics.drawString("Buy", 400 + shopButton.getWidth() / 2 - graphics.getFont().getWidth("Buy") / 2, tY + 500 + shopButton.getHeight() / 2 - graphics.getFont().getHeight("Buy") / 2);
 		
 		if (player.hasGun(selectedGun.getClass())) {
 			graphics.setColor(Color.red);
 			graphics.drawString("Owned", tX + 543 - 12 - graphics.getFont().getWidth("Owned"), tY + 261 + 12);
 		} else {
 			graphics.setColor(Color.yellow);
 			graphics.drawString("$" + selectedGun.getPrice(), tX + 543 - 12 - graphics.getFont().getWidth("$" + selectedGun.getPrice()), tY + 261 + 12);
 		}
 	}
 	
 	public boolean mouseInside(GameContainer gc, int x1, int y1, int x2, int y2) {
 		if (gc.getInput().getMouseX() > x1 && gc.getInput().getMouseX() < x2 && gc.getInput().getMouseY() > y1 && gc.getInput().getMouseY() < y2) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public int getLocalX(GameContainer gc) {
 		return gc.getWidth() / 2 - shopBackground.getWidth() / 2;
 	}
 	
 	public int getLocalY(GameContainer gc) {
 		return gc.getHeight() / 2 - shopBackground.getHeight() / 2;
 	}
 
 	public void buy(int i) {
 		player.giveMoney(-guns.get(i).getPrice());
 		player.giveGun(guns.get(i));
 	}
 }

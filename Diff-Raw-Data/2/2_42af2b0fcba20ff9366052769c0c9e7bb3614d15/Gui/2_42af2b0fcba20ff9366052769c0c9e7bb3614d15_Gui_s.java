 package Monopoly;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 public class Gui {
 	public Rectangle[] button = new Rectangle[3];
 	public String[] buttonText = {"Roll", "Buy", "End turn"};
 	 
 	
 	public Gui() {
 		define();
 	}
 	
 	public void define() {
 		for(int i=0;i<button.length;i++) {
 			button[i] = new Rectangle(768+20, 178+(i*55), 200, 50);
 		}
 	}
 	
 	public void click(int mouseButton) {
 		if(mouseButton == 1) {
 			for(int i=0;i<button.length;i++) {
 				if(button[i].contains(Screen.mse)) {
 					System.out.println(buttonText[i]);
 					if(i == 0) {
 						if(!Game.rolled) {
 							Board.moveAvatar(Game.dice.roll(2), Game.turn);
 							Board.checkRent(Game.turn);
 							Game.rolled = true;
 						}
 					}
 					if(i == 1) {
 						Board.buyField(Game.turn);
 					}
					if(i == 3) {
 						Game.endTurn(Game.playingPlayer);
 						if(Game.players.get(Game.playingPlayer).getMoney()<0) {
 							Game.players.remove(Game.playingPlayer);
 						}
 						Game.playing = false;
 						Game.playingPlayer++;
 						if(Game.playingPlayer>=Game.players.size()) {
 							Game.playingPlayer = 0;
 							Game.round++;
 						}
 						Game.startTurn(Game.playingPlayer);
 					}
 				}
 			}
 		}
 	}
 	
 	public void draw(Graphics g) {
 		g.setFont(new Font("Verdana", Font.PLAIN, 15));
 		g.setColor(new Color(255,255,255,255));
 		g.fillRoundRect(768, 118, 240, 632, 30, 30);
 		g.setColor(new Color(0,0,0,255));
 		g.drawString("Player turn: "+Game.players.get(Game.playingPlayer).getName(), 768+20, 138);
 		g.drawString("Round: "+Game.round, 768+20, 158);
 		g.drawString("Money:", 768+20, 420);
 		g.setColor(new Color(0,0,0,255));
 		for (int i = 0; i < Game.players.size(); i++) {
 			g.drawString(Game.players.get(i).getName() + ": " + Game.players.get(i).getMoney(), 768 + 20, 440 + (i*20));
 		}
 		//buttons
 		for(int i=0;i<button.length;i++) {
 			g.setColor(new Color(255,255,255, 255));
 			g.drawRoundRect(button[i].x, button[i].y, button[i].width, button[i].height, 20, 20);
 			g.setColor(new Color(0, 0, 0, 255));
 			g.drawRoundRect(button[i].x, button[i].y, button[i].width, button[i].height, 20, 20);
 			g.setFont(new Font("Verdana", Font.BOLD, 20));
 			printSimpleString(buttonText[i], button[i].width, button[i].x, button[i].y+(button[i].height/2), g);
 			/*
 			if(button[i].contains(Screen.mse)) {
 				g.setColor(new Color(255,255,255, 150));
 				g.fillRect(button[i].x, button[i].y, button[i].width, button[i].height);
 			}
 			*/
 			
 			
 			
 			
 			
 			/*
 			if(buttonID[i] != Value.airAir)g.drawImage(Screen.tileset_air[buttonID[i]], button[i].x + itemIn, button[i].y + itemIn, button[i].width - (itemIn*2), button[i].height - (itemIn*2), null);
 			if(Value.towerPrice[i] > 0) {
 				g.setColor(new Color(255,255,255));
 				g.setFont(new Font("Courier New", Font.BOLD, 34));
 				g.drawString("$"+Value.towerPrice[i], button[i].x + itemIn, button[i].y + itemIn + 25);
 			}*/
 		}
 	}
 	
 	public void printSimpleString(String s, int width, int XPos, int YPos, Graphics g){  
         int stringLen = (int)
         g.getFontMetrics().getStringBounds(s, g).getWidth();  
         int start = width/2 - stringLen/2;  
         g.drawString(s, start + XPos, YPos);  
 	}
 }

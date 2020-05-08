 package com.jpii.navalbattle.turn;
 
 import java.awt.Font;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.pavo.gui.NewWindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PButton;
 import com.jpii.navalbattle.pavo.gui.controls.PText;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 
 	public class PortShop extends PWindow {
 
 	 public PortShop(NewWindowManager pare) {
 		super(pare);
 		setVisible(true);
 		pare.add(this);
 	  setLoc(78, 86);
 	  setSize(500, 250);
 	  setText("Port Shop");
 	  PText text0 = new PText(this);
 	  text0.setText("Port Shop");
 	  text0.setLoc(199, 32);
 	  addControl(text0);
 	  text0.setFont(new Font("Verdana Bold", 1, 18));
 	  PText text1 = new PText(this);
 	  text1.setText("Your Current Score is "+NavalGame.getManager().getTurnManager().getTurn().getPlayer().getScore());
 	  text1.setLoc(4, 224);
 	  addControl(text1);
 	  PButton button2 = new PButton(this);
 	  button2.setText("Purchase Battleship");
 	  button2.setLoc(16, 114);
 	  addControl(button2);
 	  PButton button3 = new PButton(this);
 	  button3.setText("Purchase Submarine");
 	  button3.setLoc(16, 143);
 	  addControl(button3);
 	  PButton button4 = new PButton(this);
 	  button4.setText("Purchase Aircraft Carrier");
 	  button4.setLoc(16, 86);
 	  addControl(button4);
 	  PText text5 = new PText(this);
 	  text5.setText("Stock");
 	  text5.setLoc(55, 60);
 	  addControl(text5);
 	  PText text6 = new PText(this);
 	  text6.setText("Price");
 	  text6.setLoc(388, 63);
 	  addControl(text6);
 	  PText text7 = new PText(this);
	  text7.setText("1000");
 	  text7.setLoc(380, 81);
 	  addControl(text7);
 	  PText text8 = new PText(this);
	  text8.setText("1250");
 	  text8.setLoc(378, 105);
 	  addControl(text8);
 	  PText text9 = new PText(this);
	  text9.setText("1250");
 	  text9.setLoc(379, 136);
 	  addControl(text9);
 	 }
 
 	}

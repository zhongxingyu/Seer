 package com.voracious.dragons.client.screens;
 
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 
 import com.voracious.dragons.client.Game;
 import com.voracious.dragons.client.graphics.Screen;
 import com.voracious.dragons.client.graphics.Sprite;
 import com.voracious.dragons.client.graphics.ui.Button;
 import com.voracious.dragons.client.utils.InputHandler;
 
 public class StatScreen extends Screen {
 	public static final int WIDTH = 2160/3;
     public static final int HEIGHT = 1440/3;
 
     private Button returnButton;
     private Sprite background;
     
 	public StatScreen(/*player's pid to do db searching*/) {
 		super(WIDTH, HEIGHT);
 		background = new Sprite("/mainMenuBackground.png");
 		returnButton=new Button("Back",0,0);
 		returnButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Game.setCurrentScreen(new MainMenuScreen());
 			}
 		});
 	}
 	
 	@Override
 	public void start(){
 		InputHandler.registerScreen(this);
 	}
 	
 	@Override
 	public void stop(){
 		InputHandler.deregisterScreen(this);
 	}
 
 
 	@Override
 	public void render(Graphics2D g) {
 		background.draw(g, 0, 0);
		this.returnButton.draw(g);
 
 	}
 
 	@Override
 	public void tick() {
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e){
 		int ex=e.getX();
 		int ey=e.getY();
 		this.returnButton.mouseClicked(ex, ey);
 	}
 }

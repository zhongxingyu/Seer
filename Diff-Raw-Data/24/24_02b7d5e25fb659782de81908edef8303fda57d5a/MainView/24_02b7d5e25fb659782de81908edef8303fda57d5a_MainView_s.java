 package View;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import Model.*;
 import Model.Skills.*;
 
 public class MainView extends BasicGameState implements ActionListener {
 	
 	Image bg;
 	private String mouse = "No input yet";
 	
 	Player player;
 	Skill[] playerSkills;
 	Skill currentActiveSkill;
 	
 	int enemyHP = 100;
 	Image enemyImage;
 	float enemyX = 600;
 	float enemyY = 300;
 	int eWidth;
 	int eHeight;
 	
 
 	public MainView(int state) {
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		enemyImage = new Image("res/awesomePinkSquare.png");
 		player = new Player(190, 90);
 		playerSkills = player.getSkills();
 		currentActiveSkill = playerSkills[0];
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		bg = new Image("res/bg.png");
 		
 		g.drawImage(bg, 0, 0);
 		g.drawImage(bg, 500, 0);
 		g.drawImage(bg, 1000, 0);
 		g.drawImage(bg, 0, 500);
 		g.drawImage(bg, 500, 500);
 		g.drawImage(bg, 1000, 500);
 		
 		g.drawString(mouse, 800, 10);
 		
 		g.drawString("Enemy HP: "+enemyHP,500,500);
 		
 		g.drawImage(player.getImage(), player.getX(),player.getY());
 		
 		
 		
 		for(int i=0; i<player.getSkills().length; i++){
 			g.drawRect(10 + i*50, 660, 50, 50);
 			if(playerSkills[i] != null){
 				if(playerSkills[i].checkCooldown() == playerSkills[i].getCoolDown())
 					g.fillRect(10 + i*50, 660, 50, 50);
 				
 				g.drawString(""+playerSkills[i].checkCooldown(), 30 + i*50, 675);
 			}
 			
 		}
 		
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null){
 				if(playerSkills[i].isAttacking() && !playerSkills[i].isColliding())
 					g.drawImage(playerSkills[i].getAttImage(), playerSkills[i].getAttX(),playerSkills[i].getAttY());
 				
 				if(isColliding(playerSkills[i]) && playerSkills[i].isColliding()){
 					playerSkills[i].setAttackingState(false);
 					System.out.println("Target hit with " + playerSkills[i].getName());
 					playerSkills[i].setCollidingState(false);
					playerSkills[i].resetShot(player);
 					enemyHP -= playerSkills[i].getDamage();
 				}
 			}
 		}
 		
 		
 
 		
 		if(enemyHP>0){
 			g.drawImage(enemyImage, enemyX, enemyY);
 		}else{
 			enemyX=-1000;
 			enemyY=-1000;
 		}
 	}
 	
 	float mouseXPosMove;
 	float mouseYPosMove;
 	
 	float mouseXPosAtt;
 	float mouseYPosAtt;
 	
 	Double findNaN;
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();
 		if(input.isKeyDown(Input.KEY_Q)){player.addY(-1);}
 		if(input.isKeyDown(Input.KEY_S)){player.addY(1);}
 		if(input.isKeyDown(Input.KEY_A)){player.addX(-1);}
 		if(input.isKeyDown(Input.KEY_D)){player.addX(1);}
 		if(input.isKeyDown(Input.KEY_1)){
 			if(playerSkills[0] != null)
 				currentActiveSkill = playerSkills[0];}
 		if(input.isKeyDown(Input.KEY_2)){
 			if(playerSkills[1] != null)
 				currentActiveSkill = playerSkills[1];}
 		if(input.isKeyDown(Input.KEY_3)){
 			if(playerSkills[2] != null)
 				currentActiveSkill = playerSkills[2];}
 		if(input.isKeyDown(Input.KEY_4)){
 			if(playerSkills[3] != null)
 				currentActiveSkill = playerSkills[3];}
 		if(input.isKeyDown(Input.KEY_5)){
 			if(playerSkills[4] != null)
 				currentActiveSkill = playerSkills[4];}
 		
 		if((190<xPos && xPos<290) && (250<yPos && yPos<350)){
 			if(input.isMouseButtonDown(0)){ // 0 = leftclick, 1 = rightclick
 				sbg.enterState(1);
 			}
 		}
 		
 		if(input.isMouseButtonDown(1)){
 			move();
 		}
 		
 		if(input.isMouseButtonDown(0)){
 			if ((135<xPos && 250>xPos) && (225<yPos && 270>yPos)){
 				enemyHP= 100;
 				enemyX = 600;
 				enemyY = 300;	
 			}
 			attack();
 		}
 		if(player.isRunning()){
 			isRunning();
 		}
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null && playerSkills[i].isAttacking()){
 				isAttacking(playerSkills[i]);
 			}
 		}
 	}
 	
 	private void isRunning(){
 		player.addX((float)(player.getXDirMove()*player.getMoveSpeed()));
 		player.addY((float)(player.getYDirMove()*player.getMoveSpeed()));
 		if(findNaN.isNaN()){
 //			imgX = mouseXPosMove;
 //			imgY = mouseYPosMove;
 			player.setRunningState(false);
 		}
 		player.incMoveCounter();
 		if(player.getMoveCounter()*player.getMoveSpeed() >= player.getGenDirMove())
 			player.setRunningState(false);
 	}
 	
 	private void isAttacking(Skill attackingSkill){
 		for(int i=0; i<playerSkills.length; i++){
 			if(attackingSkill != null){
 				attackingSkill.addAttX((float)(attackingSkill.getXDirAtt()*attackingSkill.getAttSpeed()));
 				attackingSkill.addAttY((float)(attackingSkill.getYDirAtt()*attackingSkill.getAttSpeed()));
 				
 				attackingSkill.incAttCounter();
 				if(attackingSkill.getAttCounter()*attackingSkill.getAttSpeed() >= attackingSkill.getGenDirAtt())
 					attackingSkill.setAttackingState(false);
 			}
 		}
 	}
 	
 	public boolean isColliding(Skill skill) throws SlickException{
 		if((enemyX <= skill.getAttX() && skill.getAttX() <= enemyX + enemyImage.getWidth()) && (enemyY <= skill.getAttY() && skill.getAttY() <= enemyY + enemyImage.getHeight()) ){
 			skill.setCollidingState(true);
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	public void move(){
 		mouseXPosMove = Mouse.getX();
 		mouseYPosMove = 720 - Mouse.getY();
 		player.setXDirMove((mouseXPosMove - player.getX()));
 		player.setYDirMove((mouseYPosMove - player.getY()));
 		player.setGenDirMove((float)Math.sqrt(player.getXDirMove()*player.getXDirMove()+player.getYDirMove()*player.getYDirMove()));
 		findNaN = (double)player.getGenDirMove();
 		player.setXDirMove(player.getXDirMove()/player.getGenDirMove());
 		player.setYDirMove(player.getYDirMove()/player.getGenDirMove());
 		
 		player.resetMoveCounter();
 		
 		System.out.println("Running " + player.getGenDirMove() + " pixels");
 		player.setRunningState(true);
 	}
 	
 	public void attack(){
 		
 	//	playerSkills[0].run();
 		
 		//for(int i=0; i<playerSkills.length; i++){
 			if(currentActiveSkill != null && currentActiveSkill.checkCooldown() == currentActiveSkill.getCoolDown()){
 				
 				currentActiveSkill.activateSkill();
 				
 				mouseXPosAtt = Mouse.getX();
 				mouseYPosAtt = 720 - Mouse.getY();
 				
 				currentActiveSkill.resetShot(player);
 				
 				currentActiveSkill.setXDirAtt((mouseXPosAtt - currentActiveSkill.getAttX()));
 				currentActiveSkill.setYDirAtt((mouseYPosAtt - currentActiveSkill.getAttY()));
 				currentActiveSkill.setGenDirAtt((float)Math.sqrt(currentActiveSkill.getXDirAtt()*currentActiveSkill.getXDirAtt()+currentActiveSkill.getYDirAtt()*currentActiveSkill.getYDirAtt()));
 				currentActiveSkill.setXDirAtt(currentActiveSkill.getXDirAtt()/currentActiveSkill.getGenDirAtt());
 				currentActiveSkill.setYDirAtt(currentActiveSkill.getYDirAtt()/currentActiveSkill.getGenDirAtt());
 				
 				if(currentActiveSkill.getGenDirAtt() > currentActiveSkill.getAttackRange()){
 					currentActiveSkill.setGenDirAtt(currentActiveSkill.getAttackRange());
 				}
 				
 				currentActiveSkill.resetAttCounter();
 				
				System.out.println("Attacking " + currentActiveSkill.getGenDirAtt() + " pixels");
 				currentActiveSkill.setAttackingState(true);
 			}
 		//}
 	}
 	
 	public int getID(){
 		return 1;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String fileName = "res/154561__ecfike__hurt-argh-1.wav";
 		if(e.getActionCommand() == "Taking damage"){
 			playSound("res/154561__ecfike__hurt-argh-1.wav");
 		} else if(e.getActionCommand() == "Sword"){
 			playSound("res/77611__joelaudio__sfx-attack-sword-001.wav");
 		} else if(e.getActionCommand() == "Bow"){
 			playSound("res/65733__erdie__bow01.wav");
 		} else if(e.getActionCommand() == "Fireball"){
 			playSound("res/77691__joelaudio__sfx-magic-fireball-001.wav");
 		} else if(e.getActionCommand() == "Music"){
 			playSound("res/126427__cabeeno-rossley__timer-first-half-loop.wav");
 		}
 	}
 	
 	public static synchronized void playSound(String filename) {
 
 		    try
 		    {
 		        Clip clip = AudioSystem.getClip();
 		        clip.open(AudioSystem.getAudioInputStream(new File(filename)));
 		        clip.start();
 		    }
 		    catch (Exception exc)
 		    {
 		        exc.printStackTrace(System.out);
 		    }	
 	}
 }

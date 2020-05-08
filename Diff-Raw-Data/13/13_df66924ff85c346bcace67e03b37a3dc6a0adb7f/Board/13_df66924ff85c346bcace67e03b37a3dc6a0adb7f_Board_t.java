 package com.warofcosmo.cosmo;
 
 import Entities.Enemy;
 import Entities.AbstractEntity;
 import Weapons.AbstractWeaponn;
 import java.awt.Color;
 import java.awt.Font;
 import levelpackage.level1;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import sun.audio.*;
 import javax.swing.JPanel;
 import projectiles.Projectile;
 
 
 
 public class Board extends JPanel implements Runnable, KeyListener {
 	
 	private Image bgimg;
 	private Player p;
 	private level1 l;
 	protected AudioStream as;
 	private int level;
 	private int size;
 	private ArrayList weaps;
 	private ArrayList<AbstractEntity> Enemies;
 	private ArrayList LevelArr;
 	private ArrayList<Projectile> _projectiles = new ArrayList();
 	private int startX;
 	private int startY;
 	private int BWidth=5020;
 	private int BHeight=920;
         private boolean _death=false;
         private int _score=0;
        static final int _zeroes = 6;
 	
 	// contructor method
 	public Board(){
                  
                 level=0;
                 LevelArr = new ArrayList();
                 weaps = new ArrayList();
                 Enemies = new ArrayList();
                 
                 // TESTING
                 l=new level1(this);
                 
                 bgimg=l.getBG();
                 as=l.getBGM();
                 size=bgimg.getWidth(this);
                 AudioPlayer.player.start(as);
 	      
 	        playBGM(as);	
                
 	        addKeyListener(this);
                 setFocusable(true);
                 setFocusTraversalKeysEnabled(false); 
                 
                 p = new Player(this);
                 
                 startX=p.getX();
                 startY=p.getY();
                 
                 NumberFormat nf = NumberFormat.getInstance();
                 
                 Thread mainthread=new Thread(this);
                 mainthread.start();
 
 	}
  
 	public void playBGM(AudioStream bgm){
 		AudioPlayer.player.start(bgm);
 	}
 	
 	public void stopBGM(AudioStream bgm){
 		AudioPlayer.player.stop(bgm);
 	}
         
         public void LoadNext(){
             level++;
             System.out.println(level);
             l=(level1) LevelArr.get(level);
             bgimg=l.getBG();
             as=l.getBGM();
             playBGM(as);
             size=bgimg.getWidth(null);
             p.setX(startX);
             p.setY(startY);
         }
 
 	@Override
 	public void run() {
       
             while(true){
                 try{
                     if(!_death){
                        p.move(); 
                     }else{
                        if(p.death()){
                            //p.getDeathFX().play();
                            restartLevel();
                        }else{
                            Thread.sleep(10);
                        }
      
                     }
                     
                     l.Move();
 
                     repaint();
                     Thread.sleep(5);
                 }
                 catch(Exception ignore){}
             }
 	}
         
         @Override
         public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		
                
                 
                 g2d.drawImage(bgimg,l.getX(),0,null);
 
                 g2d.drawImage(p.getImage(),p.getX(),p.getY(),null);
                 
                 g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
                 g.setColor(Color.RED);
                 
                g.drawString("SCORE: "+ String.format("%0"+ _zeroes +"d",_score),10,30);
                     
 		drawProjectiles(g2d);
                 
                 for(int i=0; i<Enemies.size();i++){
                     Enemy en=((Enemy)Enemies.get(i));
                     g2d.drawImage(en.getImage(),en.getX(),en.getY(),null);
                     
                     //If Player hits j restart
                     if(p.getBounds().intersects(en.getBounds())){
                         restartLevel();
                     }
 
                     //IF Enemy leaves board delete them from array
                     if(en.getY() < 0 || en.getY() > this.getSize().height){
                         Enemies.remove(i);
                     }
                     if(en.getX() < 0 ){
                         Enemies.remove(i);
                     }
                     if(en.getHealth() <=0){
 			//kill enemies projectile spawning thread and remove enemy
                         en.getProjectileThread().interrupt();
                         if(en.death()){
                             Enemies.remove(en);
                             _score=_score+en.getpoints();
                             //en.getDeathFX().play();
                         }
                     }
                 }
                 
                 for (int i = 0 ; i < weaps.size(); i++){
                     AbstractWeaponn weap = ((AbstractWeaponn)weaps.get(i));
                     g2d.drawImage(weap.getImage(),weap.getX(),weap.getY(),null);
 
                     if(weap.getX() >= p.getX()+weap.getDistance()){
                         weaps.remove(weap);
                     }
                     if(weap.getX() <= p.getX()-weap.getDistance()){
                         weaps.remove(weap);
                     }
                     for(int j=0; j < Enemies.size();j++){
 			Enemy e = ((Enemy)Enemies.get(j));
 			//enemy at j was hit by current Reduce health and check if destroyed
 			if(weap.getBounds().intersects(e.getBounds())){
 				weaps.remove(weap);
 				e.setHealth(weap.getDamage());
 				
 				if(e.getHealth() <=0){
                                     //kill enemies projectile spawning thread and remove enemy
                                     e.getProjectileThread().interrupt();
 				}
 			}
                     }
 
                 }
         }
 		
 	private void restartLevel(){
 		stopBGM(l.getBGM());
 			
                 for (int i = 0; i < _projectiles.size(); i++) {
                     Projectile pr=_projectiles.get(i);
                     pr.stop();
 		}
 						
 		//if any projectileSpawners exist, remove them
 		for (int i = 0; i < Enemies.size(); i++) {
 			AbstractEntity e = Enemies.get(i);
 			e.getProjectileThread().interrupt();
 		}
 					
                         Enemies.removeAll(Enemies);
                         _projectiles.removeAll(_projectiles);
                         weaps.removeAll(weaps);
                         
                         try{
                             Thread.sleep(3000);
                         }
                         catch(Exception ignore){}
                         
                         l.setX(0);
                         l.setY(0);
                         
                         p.setX(startX);
                         p.setY(startY);
                         p.setGFX(p.getImage(0));
                         
                         _death=false;
                         playBGM(l.getBGM());
         }
         
         
 	private void drawProjectiles(Graphics2D g2d) {
 		
 		for (int i = 0; i < _projectiles.size(); i++) {
 			Projectile pr = _projectiles.get(i);
 			g2d.setColor(pr.getColor());
 			//g2d.drawRect(p.getX(), p.getY(), p.getWidth(), p.getHeight());
 			g2d.drawOval(pr.getX(), pr.getY(), pr.getWidth(), pr.getHeight());
                         
                         //Remove projectile from array if off screen
                         if(pr.getY() < 0 || pr.getY() > this.getSize().height){
                             _projectiles.remove(i);
                         }
                         if(pr.getX() < 0 || pr.getX() > this.getSize().width){
                             _projectiles.remove(i);
                         }
                         
                         // check if player hit by projectile
                         if(p.getBounds().intersects(pr.getBounds()))
                         {
                             _death=true;
                             
                         }
 		}
 	}
 
         
 	@Override
 	public void keyTyped(KeyEvent e) {}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		p.keyPressed(e);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		p.keyReleased(e);
 	}
 
 	public void addBullit(AbstractWeaponn b) {
 		weaps.add(b);
 	}
 	
 	public void addEnemy(AbstractEntity e) {
             Enemies.add(e);
 	}
 	
 	public void addProjectile(Projectile p) {
 		_projectiles.add(p);
 	}
 	
 	public Player getPlayer() {
 		return p;
 	}
 
 }

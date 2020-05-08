 package net.ciderpunk.tanktris.entities;
 
 import java.awt.Graphics2D;
 
 import net.ciderpunk.tanktris.IGameState;
 import net.ciderpunk.tanktris.game.Game;
 import net.ciderpunk.tanktris.graphics.*;
 
 
 public class Gun extends AnimatedEntity{
 
 
 	//animations	
 	static Animation oAnimGunFire; 
 	static Animation oAnimIdle; 
 	static Frame oFrameIdle;
 	static Frame oFrameDome; 
 
 	double iRads;
 	
 	public static void loadResources(Frame oImg){
 		
 
 		Frame oFrameIdle = new Frame(oImg,0,0,36,96,19,104);
 		oFrameDome = new Frame(oImg,0,96,31,31,15,15);
 		oAnimIdle = new Animation(false);
 		oAnimIdle.addFrame(oFrameIdle,1);
 		
 		oAnimGunFire = new Animation(false);
 		oAnimGunFire.addFrame(new Frame(oImg,0,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,36,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,72,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,108,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,144,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,180,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,216,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,252,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,288,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,324,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,360,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,396,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(new Frame(oImg,432,0,36,96,19,104),3);
 		oAnimGunFire.addFrame(oFrameIdle,0);	
 		
 	}
 	
 	
 	public Gun(int x, int y){
 		super(x,y);
 		oAnimState.startAnim(oAnimIdle);
 	}
 	
 	public void fire(){
 		oAnimState.startAnim(oAnimGunFire);
 		
		Shot oShot = new Shot(this.getX(), this.getY(), iRads,  4.0);
 		Game.getInstance().registerEntity(oShot);
 	}
 
 	
 	
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		super.update();
 	}
 
 
 	@Override
 	public void draw(Graphics2D oG) {
 		
 		oFrameDome.draw(oG, this.iX, this.iY);
 		this.oAnimState.getFrame().drawRotate(oG, this.iX, this.iY, iRads);
 	}
 
 
 	public void trackTo(int x, int y) {
 		int iDX = x- iX;;
 		int iDY = y -iY;
 		this.iRads = Math.atan2(iDY, iDX) + (Math.PI  / 2);
 	}
 
 
 
 	
 	
 	
 }

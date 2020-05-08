 package com.mel.wallpaper.starWars.entity.commands;
 
 
 import org.andengine.util.modifier.ease.EaseLinear;
 import org.andengine.util.modifier.ease.EaseQuartOut;
 
 import com.mel.entityframework.Game;
 import com.mel.util.MathUtil;
 import com.mel.wallpaper.starWars.entity.LaserBeam;
 import com.mel.wallpaper.starWars.entity.Map;
 import com.mel.wallpaper.starWars.entity.Shooter;
 import com.mel.wallpaper.starWars.entity.Walker;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 
 public class ShootLaserCommand extends MoveCommand
 {
 	public LaserBeam laser;
 	private Game game;
 	private Shooter shooter;
 	
 	public ShootLaserCommand(Shooter walker, Game game) {
 		super(walker, 1.4f, EaseLinear.getInstance());
 
 		//this.easeFunction = EaseStrongOut.getInstance();
 		//this.easeFunction = EaseExponentialOut.getInstance();
 		//this.easeFunction = EaseSineOut.getInstance();
 		
 		this.shooter = walker;
 
 		this.laser = new LaserBeam(walker.position);
 		this.movable = laser;
 		game.addEntity(laser);
 	}
 	
 	@Override
 	public void execute(Map p) {
 		//increase destination
 		this.destination = MathUtil.getPuntDesti(this.laser.position.toPoint(), this.destination, 2000f);
 		
		
		if(shooter.canShoot()){
			super.execute(p);
		}
		
 		shooter.forceStopMovement();
		shooter.animateShootAndStartCooldowns(this.destination.clone());//consiga darle a la bola o no, se ve que el jugador chuta
 	}
 	
 }

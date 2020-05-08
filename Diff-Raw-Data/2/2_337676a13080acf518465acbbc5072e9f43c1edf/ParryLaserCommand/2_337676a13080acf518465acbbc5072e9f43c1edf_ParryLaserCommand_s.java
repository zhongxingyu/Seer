 package com.mel.wallpaper.starWars.entity.commands;
 
 
 import org.andengine.util.modifier.ease.EaseLinear;
 import org.andengine.util.modifier.ease.EaseQuartOut;
 
 import com.mel.entityframework.Game;
 import com.mel.util.MathUtil;
 import com.mel.wallpaper.starWars.entity.JediKnight;
 import com.mel.wallpaper.starWars.entity.LaserBeam;
 import com.mel.wallpaper.starWars.entity.Map;
 import com.mel.wallpaper.starWars.entity.Shooter;
 import com.mel.wallpaper.starWars.entity.Walker;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 
 public class ParryLaserCommand extends MoveCommand
 {
 	public LaserBeam laser;
 	private JediKnight jedi;
 	
 	public ParryLaserCommand(JediKnight jedi, LaserBeam laser) {
 		super(jedi, 1.4f, EaseLinear.getInstance());
 
 		this.jedi = jedi;
 		this.laser = laser;
 		this.movable = laser;
 	}
 	
 	@Override
 	public void execute(Map p) {
 		//increase destination
 		this.destination = MathUtil.getPuntDesti(this.laser.position.toPoint(), this.destination, 2000f);
 		
 		this.laser.lastParringJedi = jedi;
 		this.laser.jumps--;
 		
 		super.execute(p);
 
 		jedi.forceStopMovement();
		jedi.animateParryLaser(this.destination.clone());
 	}
 	
 }

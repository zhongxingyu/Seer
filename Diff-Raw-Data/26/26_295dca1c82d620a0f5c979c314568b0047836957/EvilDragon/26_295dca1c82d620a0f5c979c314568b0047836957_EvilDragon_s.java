 package com.zx.evildragon;
 
 import info.u250.c2d.engine.Engine;
 import info.u250.c2d.engine.EngineDrive;
 
import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.zx.evildragon.net.ITalk;
 
 public class EvilDragon extends Engine {
 
 	public static ITalk talk;
	public static BitmapFont font;
 
 	public EvilDragon(ITalk talk) {
 		EvilDragon.talk = talk;
 	}
 
 	@Override
 	protected EngineDrive onSetupEngineDrive() {
 		return new EvilDragonEnginDrive();
 	}
 
 }

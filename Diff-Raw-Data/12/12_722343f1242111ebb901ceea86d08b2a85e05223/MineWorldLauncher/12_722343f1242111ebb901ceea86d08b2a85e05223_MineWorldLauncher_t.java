 /* MineWorld
  * A proof of concept minecraft-like world.
  */
 package com.elezeta.mineworld;
 
import java.lang.reflect.Field;

 /**
  *
  * @author elezeta
  */
 public class MineWorldLauncher {
  
     public static void main(String[] args) {
    	System.setProperty("java.library.path", "MineWorld_lib/:"+System.getProperty("java.library.path"));
    	Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
	    	fieldSysPath.setAccessible( true );
	    	fieldSysPath.set( null, null );
		} catch (Exception e) {
			e.printStackTrace();
		}
         (new MineWorld()).run();
     }   
     
 }

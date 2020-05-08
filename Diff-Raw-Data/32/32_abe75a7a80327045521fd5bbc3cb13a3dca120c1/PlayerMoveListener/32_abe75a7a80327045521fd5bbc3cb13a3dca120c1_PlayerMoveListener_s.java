 package com.github.joukojo.testgame;
 
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.joukojo.testgame.world.core.WorldCore;
 import com.github.joukojo.testgame.world.core.WorldCoreFactory;
 
 public class PlayerMoveListener implements MouseMotionListener, MouseListener {
 	private final static Logger LOG = LoggerFactory.getLogger(PlayerMoveListener.class);
 	@Override
 	public void mouseClicked(MouseEvent e) {
 //		Point locationOnScreen = e.getLocationOnScreen();
 //		LOG.debug("mouse clicked");
 //		LOG.debug("location x:" + locationOnScreen.x);
 //		LOG.debug("location y:" + locationOnScreen.y);
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Point locationOnScreen = e.getLocationOnScreen();
 		LOG.debug("mouse pressed");
 		LOG.debug("location x:" + locationOnScreen.x);
 		LOG.debug("location y:" + locationOnScreen.y);
 
 		WorldCore worldCore = WorldCoreFactory.getWorld();
 		Bullet bullet = new Bullet();
 		Player player = (Player) worldCore.getMoveable("player");
		bullet.directionX = player.directionX + 0.2;
		bullet.directionY = player.directionY + 0.2;
		bullet.locationX = player.positionX;
		bullet.locationY = player.positionY;
 
 		worldCore.addMoveable("bullets", bullet);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 //		Point locationOnScreen = e.getLocationOnScreen();
 //		LOG.debug("mouse released");
 //		LOG.debug("location x:" + locationOnScreen.x);
 //		LOG.debug("location y:" + locationOnScreen.y);
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 //		Point locationOnScreen = e.getLocationOnScreen();
 //		LOG.debug("mouse entered");
 //		LOG.debug("location x:" + locationOnScreen.x);
 //		LOG.debug("location y:" + locationOnScreen.y);
 		
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 //		Point locationOnScreen = e.getPoint();
 //		LOG.debug("mouse exited");
 //		LOG.debug("location x:" + locationOnScreen.x);
 //		LOG.debug("location y:" + locationOnScreen.y);
 //
 //
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 //		Point locationOnScreen = e.getLocationOnScreen();
 //		LOG.debug("mouse dragged");
 //		LOG.debug("location x:" + locationOnScreen.x);
 //		LOG.debug("location y:" + locationOnScreen.y);
 		WorldCore worldCore = WorldCoreFactory.getWorld();
 		Player player = (Player) worldCore.getMoveable("player");
 		player.point = e.getPoint();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		WorldCore worldCore = WorldCoreFactory.getWorld();
 		Player player = (Player) worldCore.getMoveable("player");
 		if( player != null ) {
 			player.point = e.getPoint();
 		}
 		
 	}
 
 }

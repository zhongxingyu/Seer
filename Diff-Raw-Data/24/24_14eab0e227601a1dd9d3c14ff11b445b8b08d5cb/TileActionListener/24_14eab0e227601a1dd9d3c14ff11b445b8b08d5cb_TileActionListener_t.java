 package com.denisneuling.connectfour.gui.components.listener;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.denisneuling.connectfour.gui.components.Tile;
 import com.denisneuling.connectfour.service.GridService;
 
 @Component
 /**
  * <p>TileActionListener class.</p>
  *
  * @author ska
  * @version $Id: $Id
  */
 public class TileActionListener implements MouseListener{
 	Logger log = Logger.getLogger(this.getClass());
 
 	@Autowired
 	private GridService gridService;
 	
 	private volatile boolean enabled = true;
 	
 	/** {@inheritDoc} */
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		log.debug("Mouse clicked");
 		log.debug(arg0.getSource().toString());
 		
 		Object source = arg0.getSource();
 		if(enabled && Tile.class.equals(source.getClass())){
 			gridService.clicked((Tile)source);
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		log.debug("Mouse entered");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		log.debug("Mouse exited");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		log.debug("Mouse pressed");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		log.debug("Mouse released");
 	}
 
	/**
	 * <p>enable.</p>
	 */
 	public void enable() {
 		this.enabled = true;
 	}
 
	/**
	 * <p>disable.</p>
	 */
 	public void disable() {
 		this.enabled = false;
 	}
 }

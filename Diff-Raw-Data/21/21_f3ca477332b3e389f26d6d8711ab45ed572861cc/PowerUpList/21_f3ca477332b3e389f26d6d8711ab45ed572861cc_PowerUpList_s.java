 package com.secondhand.model.entity;
 
import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 
import com.secondhand.debug.MyDebug;
import com.secondhand.model.powerup.PowerUp;

 public class PowerUpList extends ArrayList<IPowerUp> {
 
 	
 	// NOT FINISHED!
 	
 	private static final long serialVersionUID = 1L;
 	private static PowerUpList instance;
 	
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	
 	public static final String ADD_POWERUP = "addPowerUp";
 	
 	public PowerUpList() {
 		super();
 	}
 
 	@Override
 	public boolean add(IPowerUp powerUp) {
 		pcs.firePropertyChange(ADD_POWERUP, null, powerUp);
 		powerUp.activatePowerUp();
 		return super.add(powerUp);
 	}
 	
 	@Override
 	public boolean remove(final Object object) {
 		final boolean value = super.remove(object); // Priority: The list is
 		// empty when you remove
 		// last PowerUp
		PowerUp powerUp = (PowerUp) object;
 		powerUp.resetPlayerColor();
 		powerUp.deactivateEffect(hasAnother(powerUp));
 		return value;
 	}
 
 	public static PowerUpList getInstance(){
 		if(instance==null){
 			instance = new PowerUpList();
 		}
 		return instance;
 	}
 
 	public void addListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	public boolean hasAnother(IPowerUp powerUp) {
 		boolean hasAnother = false;
 		for (final IPowerUp otherPowerUp : this) {
 			if (otherPowerUp.getClass() == powerUp.getClass()) 
 				hasAnother = true;
 		}
 		return hasAnother;	
 	}
 }

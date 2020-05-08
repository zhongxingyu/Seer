 package fr.imag.adele.apam.device.door.impl;
 
 
import java.util.ArrayList;
 import java.util.HashSet;
import java.util.List;
 import java.util.Set;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.StaticServiceProperty;
 
 import appsgate.ard.aperio.AperioAccessDecision;
 import appsgate.ard.base.callback.LockerAuthorizationCallback;
 import appsgate.ard.dao.AuthorizationRequest;
 import appsgate.ard.dao.AuthorizationResponse;
 import appsgate.ard.dao.AuthorizationResponseAck;
 
 
 /**
  * This is a lock door authorization module, that allow putting doors in a detached mode
  * in which all request can be authorized/denied without consulting the card base.
  *  
  * @author vega
  *
  */
 @Component
 @Provides(
 		specifications	= {DetachableLockAuthorization.class,LockerAuthorizationCallback.class} ,
 		properties 		= @StaticServiceProperty(name="priority",type="java.lang.Integer",value="-20")
 )
 @Instantiate
 
 public class DetachableLockAuthorization implements LockerAuthorizationCallback {
 
 	@Requires(filter="(priority=0)")
 	private LockerAuthorizationCallback original;
 	
 	private final Set<Byte> lockedDoors;
 	
 	private final Set<Byte> unlockedDoors;
 	
 	public DetachableLockAuthorization() {
 		lockedDoors		= new HashSet<Byte>();
 		unlockedDoors	= new HashSet<Byte>();
 	}
 
 	/**
 	 * Enables normal authorization for the specified door
 	 */
 	public void enableAuthorization(byte doorId) {
 		lockedDoors.remove(doorId);
 		unlockedDoors.remove(doorId);
 	}
 
 	/**
 	 * Disables normal authorization for the specified door, and puts the 
 	 * door in the specified global state
 	 */
 	public void disableAuthorization(byte doorId, boolean locked) {
		if (locked)
 			lockedDoors.add(doorId);
		else
 			unlockedDoors.add(doorId);
 	}
 	
 	@Override
 	public AuthorizationResponse authorizationRequested(AuthorizationRequest ar) {
 		
 		if (lockedDoors.contains(ar.getDoorId()))
 			return new AuthorizationResponse(AperioAccessDecision.NOT_GRANTED,ar);
 
 		if (unlockedDoors.contains(ar.getDoorId()))
 			return new AuthorizationResponse(AperioAccessDecision.GRANTED,ar);
 
 		return original.authorizationRequested(ar);
 	}
 
 	@Override
 	public void authorizationAckReceived(AuthorizationResponseAck ack) {
 
 		original.authorizationAckReceived(ack);
 
 	}
 	
 
 }

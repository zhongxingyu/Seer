 package com.kurento.commons.mscontrol.mediacomponent;
 
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.join.JoinableContainer;
 import com.kurento.commons.mscontrol.resource.ResourceContainer;
 
 /**
  * <p>
  * A MediaComponent receive and/or send media streams from/to another Joinable
  * object.<br>
  * Treatment of media, in case of receive media, and getting of media, in case
  * of send media, is dependent of implementation.
  * </p>
  * A MediaComponent is initially unrelated to any call. Then, a MediaComponent
  * can be joined/unjoined to another Joinable, normaly to a NetworkConnection.
  * 
  * </p>
  * 
  * @author mparis
  * 
  */
 public interface MediaComponent extends JoinableContainer, ResourceContainer {
 
 	/**
 	 * <p>
 	 * Start treatment or getting of media.
 	 * 
 	 * @throws MsControlException
 	 */
 	public void start() throws MsControlException;
 
 	/**
 	 * <p>
	 * Stop treatment or getting of media.
 	 */
 	public void stop();
 
 }

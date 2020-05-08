 package org.whired.ghostclient.client.user.impl;
 
 /**
  * The default implementation of a user
  * @author Whired
  */
 import org.whired.ghostclient.client.settings.SessionSettings;
 import org.whired.ghostclient.client.user.GhostUser;
 
 public class DefaultUser implements GhostUser {
 	/**
 	 * The settings for this user
 	 */
 	private final SessionSettings settings;
 
 	/**
 	 * Constructs a new GhostUser
	 * @param settings the settings associated with this user
 	 */
 	public DefaultUser(final SessionSettings settings) {
 		this.settings = settings;
 	}
 
 	/**
 	 * Gets the settings for this user
 	 * @return the settings associated with this user
 	 */
 	@Override
 	public SessionSettings getSettings() {
 		return this.settings;
 	}
 }

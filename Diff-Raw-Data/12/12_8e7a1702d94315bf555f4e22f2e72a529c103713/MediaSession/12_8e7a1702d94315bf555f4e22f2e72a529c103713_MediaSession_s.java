 /*
  * Kurento Commons MSControl: Simplified Media Control API for the Java Platform based on jsr309
  * Copyright (C) 2011  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.mscontrol.commons;
 
 import com.kurento.commons.config.Parameters;
 
 /**
  * A MediaSession is a container and factory for media objects. It handles the
  * cleanup on <code>release()</code>.
  */
 public interface MediaSession {
 
 	/**
 	 * Create a <code>NetworkConnection</code>.
 	 * 
 	 * @return a NetworkConnection
 	 * @throws MediaSessionException
 	 */
 	public NetworkConnection createNetworkConnection(Parameters params)
 			throws MediaSessionException;
 
 	/**
 	 * </p> Create a joinable depending on given parameters
 	 * <code>Joinable</code>.
 	 * 
 	 * @param params
 	 *            Parameters to create a generic Joinable. The possible
 	 *            parameters for each type must be defined on the
 	 *            implementation.
 	 * @return a Joinable
 	 * @throws MediaSessionException
 	 */
 	public MediaResource createResource(Parameters params)
 			throws MediaSessionException;
 
 	/**
	 * Create a <code>MediaMixer</code>.
 	 * 
 	 * @param params
 	 *            Customization parameters to create a Mixer. The possible
 	 *            parameters for each specific Mixer must be defined on the
 	 *            implementation.
 	 * @return a Mixer
 	 * @throws MediaSessionException
 	 */
 	public Mixer createMixer(Parameters params)
 			throws MediaSessionException;
 
 	/**
 	 * Releases the resources associated to this MediaSession.
 	 * <p>
 	 * The call is cascaded to the children of this object (the objects created
 	 * by it).
 	 * </p>
 	 */
 	public void release();
 }

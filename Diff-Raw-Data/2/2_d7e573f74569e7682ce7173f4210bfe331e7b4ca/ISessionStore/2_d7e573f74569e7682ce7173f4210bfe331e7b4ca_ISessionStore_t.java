 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.sessionservice;
 
 import java.security.Principal;
 
 import org.eclipse.riena.security.common.session.Session;
 
 /**
  * The implementation of ISessionStore stores the session (preferable
  * persistently). Riena supplies a simple default implementation which store the
  * session objects in the JVM memory.
  * 
  * Its better to replace this default implementation with a custom
  * implementation that store the data in a persistent store so that they can be
  * retrieved again when the server has to be restarted and so that they can be
  * shared servers (if there is more than one).
  * 
  */
 public interface ISessionStore {
 
 	/**
 	 * reads the sessions for one principal from the store
 	 * 
 	 * @param principal
 	 *            principal for which sessions should be read
 	 * @return an array of session objects
 	 * @deprecated
 	 */
	// TODO should be  Subject see bug 312460
 	// TODO can this be removed? Same for the read()
 	@Deprecated
 	Session[] read(Principal principal);
 
 	/**
 	 * reads the session entry object for a session
 	 * 
 	 * @param session
 	 * @return session entry
 	 */
 	SessionEntry read(Session session);
 
 	/**
 	 * writes or update a session entry
 	 * 
 	 * @param entry
 	 *            session entry to write
 	 */
 	void write(SessionEntry entry);
 
 	/**
 	 * deletes the session entries for a session
 	 * 
 	 * @param session
 	 */
 	void delete(Session session);
 
 	/**
 	 * deletes the session entries for a principal
 	 * 
 	 * @param principal
 	 * @deprecated
 	 */
 	@Deprecated
 	void delete(Principal principal);
 
 }

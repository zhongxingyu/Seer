 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.common.session;
 
 import java.io.Serializable;
 
 /**
  * stores the session id
  * 
  */
 public final class Session implements Serializable {
 
	private static final long serialVersionUID = -9068902157549411681L;

 	private String sessionId;
 
 	/**
 	 * Creates a new instance of <code>Session</code>
 	 */
 	public Session() {
 		super();
 	}
 
 	/**
 	 * Creates a new instance of <code>Session</code> with the given session ID
 	 * 
 	 * @param sessionId
 	 *            session ID
 	 */
 	public Session(String sessionId) {
 		super();
 		setSessionId(sessionId);
 	}
 
 	/**
 	 * @see org.eclipse.riena.security.common.session.ISession#getSessionId()
 	 */
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	/**
 	 * Sets the ID of the session
 	 * 
 	 * @param sessionId
 	 *            - ID of the session
 	 */
 	public void setSessionId(String sessionId) {
 		this.sessionId = sessionId;
 	}
 
 }

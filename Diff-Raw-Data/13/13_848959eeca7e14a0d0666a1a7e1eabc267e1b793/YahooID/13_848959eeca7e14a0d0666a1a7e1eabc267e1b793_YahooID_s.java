 /**
  * Copyright (c) 2002-2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	- Initial API and implementation
  *  	- Chris Aniszczyk <zx@us.ibm.com>
  *   	- Borna Safabakhsh <borna@us.ibm.com> 
  *   
  * $Id$
  */
 package org.eclipse.ecf.provider.yahoo.identity;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.eclipse.ecf.core.identity.BaseID;
 
public class YahooID extends BaseID {
 
 	private static final long serialVersionUID = -5949609561932558417L;
 	private String username;
 	URI uri;
 
 	public YahooID(YahooNamespace namespace, String username) throws URISyntaxException {
 		super(namespace);
 		this.username = username;
 		uri = new URI(namespace.getScheme() + username);
 	}
 
 	/**
 	 *  Compares this ID with the baseID provided. In our case, only a username exists
 	 *  (no target URL to pair it with), so a comparison may be limited to just the usernames 
 	 */
 	protected int namespaceCompareTo(BaseID o) {
 		return getName().compareTo(o.getName());
 	}
 
 	/**
 	 * Compares this ID's URI in this namespace with the provided ID's URI
 	 */
 	protected boolean namespaceEquals(BaseID o) {
 		if(!(o instanceof YahooID)){
 			return false;
 		}
 		return uri.equals(((YahooID) o).uri);
 	}
 
 	
 	protected String namespaceGetName() {
 		return username;
 	}
 
 	protected int namespaceHashCode() {
 		return uri.hashCode();
 	}
 
 	protected URI namespaceToURI() throws URISyntaxException {
 		return uri;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public String toString() {
 		StringBuffer sb = new StringBuffer("YahooID[");
 		sb.append(uri.toString()).append("]");
 		return sb.toString();
 	}
	
 }

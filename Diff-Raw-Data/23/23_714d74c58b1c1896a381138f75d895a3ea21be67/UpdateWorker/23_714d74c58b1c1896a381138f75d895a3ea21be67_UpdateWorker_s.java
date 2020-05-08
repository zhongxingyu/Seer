 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.manager.controllers.workers;
 
 import java.io.InputStream;
 import java.net.URI;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 
 public class UpdateWorker extends BundleWorker {
 	
 	private String[] locations;
 	
 	public UpdateWorker(Bundle[] bundles, String[] locations) {
 		super(bundles);
		this.locations = locations;
 	}
 
 	@Override
 	protected String getEventName() {
 		return "updated";
 	}
 	
 	@Override
 	protected void run(int i, Bundle bundle) throws BundleException {
 		InputStream is = null;
 		try {
 			URI uri = new URI(locations[i]);
 			is = uri.toURL().openStream();
 			bundle.update(is);
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			if(is != null) {
 				try {
 					is.close();
 				} catch(Exception e) {
 					// discard
 				}
 			}
 		}
 	}
 
 }

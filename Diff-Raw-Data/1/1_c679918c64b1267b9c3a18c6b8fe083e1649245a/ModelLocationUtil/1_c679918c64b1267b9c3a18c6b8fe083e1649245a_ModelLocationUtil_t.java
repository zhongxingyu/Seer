 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.locator.model;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
 
 /**
  * Peer model location utility implementation.
  */
 public final class ModelLocationUtil {
 
 	/**
 	 * Returns the local static peers storage root location.
 	 *
 	 * @return The root location or <code>null</code> if the location cannot be determined.
 	 */
 	public static IPath getStaticPeersRootLocation() {
 		try {
 			File file = CoreBundleActivator.getDefault().getStateLocation().append(".peers").toFile(); //$NON-NLS-1$
			if (!file.exists()) file.mkdirs();
 			if (file.canRead() && file.isDirectory()) {
 				return new Path(file.toString());
 			}
 		} catch (IllegalStateException e) {
 			/* ignored on purpose */
 		}
 
 		// The users local peers lookup directory is $HOME/.tcf/.peers.
 		File file = new Path(System.getProperty("user.home")).append(".tcf/.peers").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 		if (file.canRead() && file.isDirectory()) {
 			return new Path(file.toString());
 		}
 
 		return null;
 	}
 }

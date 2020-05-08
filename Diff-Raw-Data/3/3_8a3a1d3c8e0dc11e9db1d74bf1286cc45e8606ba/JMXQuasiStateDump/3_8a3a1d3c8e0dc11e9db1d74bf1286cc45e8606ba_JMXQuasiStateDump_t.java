 /*******************************************************************************
  * Copyright (c) 2008, 2012 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.kernel.userregion.internal.management;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipException;
 
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
 
 /**
  *   
  * MBean that allows for the exploration of state dumps using the QuasiFramework
  */
 public class JMXQuasiStateDump implements StateDumpMXBean {
 	
 	private final QuasiFrameworkFactory quasiFrameworkFactory;
 
 	public JMXQuasiStateDump(QuasiFrameworkFactory quasiFrameworkFactory) {
 		this.quasiFrameworkFactory = quasiFrameworkFactory;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public JMXQuasiResolutionFailure[] getUnresolvedBundleIds(String dumpFile) {
 		QuasiFramework quasiFramework = this.getQuasiFramework(dumpFile);
 		List<QuasiResolutionFailure> resolve = quasiFramework.resolve();
 		List<JMXQuasiResolutionFailure> fails = new ArrayList<JMXQuasiResolutionFailure>();
 		for (QuasiResolutionFailure quasiResolutionFailure : resolve) {
 			fails.add(new JMXQuasiResolutionFailure(quasiResolutionFailure));
 		}
 		return fails.toArray(new JMXQuasiResolutionFailure[fails.size()]);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public JMXQuasiMinimalBundle[] listBundles(String dumpFile) {
 		QuasiFramework quasiFramework = this.getQuasiFramework(dumpFile);
 		List<QuasiBundle> bundles = quasiFramework.getBundles();
 		List<JMXQuasiMinimalBundle> jmxBundles = new ArrayList<JMXQuasiMinimalBundle>();
 		for (QuasiBundle minimalBundleMXBean : bundles) {
 			jmxBundles.add(new JMXQuasiMinimalBundle(minimalBundleMXBean));
 		}
 		return jmxBundles.toArray(new JMXQuasiMinimalBundle[jmxBundles.size()]);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public JMXQuasiBundle getBundle(String dumpFile, long bundleId) {
		if(bundleId < 0){
			return null;
		}
 		QuasiFramework quasiFramework = this.getQuasiFramework(dumpFile);
 		QuasiBundle quasiBundle = quasiFramework.getBundle(bundleId);
 		return new JMXQuasiBundle(quasiBundle);
 	}
 
 	private QuasiFramework getQuasiFramework(String dumpFile){
 		File dumpDir = new File(dumpFile);
 		if(dumpDir.exists() && dumpDir.isDirectory()){
 			try {
 				return this.quasiFrameworkFactory.create(dumpDir);
 			} catch (ZipException e) {
 				throw new RuntimeException("Unable to extract the state dump: " + e.getMessage(), e);
 			} catch (IOException e) {
 				throw new RuntimeException("Error reading the state dump: " + e.getMessage(), e);
 			}
 		}
 		throw new RuntimeException("Not a valid dump directory: " + dumpFile);
 	}
 }

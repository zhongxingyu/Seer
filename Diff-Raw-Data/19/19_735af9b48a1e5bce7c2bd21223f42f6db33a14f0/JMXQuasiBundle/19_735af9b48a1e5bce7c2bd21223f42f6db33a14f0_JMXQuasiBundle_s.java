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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
 
 /**
  * 
  *
  */
 public final class JMXQuasiBundle extends JMXQuasiMinimalBundle{
 
 	private final QuasiBundle quasiBundle;
 
 	protected JMXQuasiBundle(QuasiBundle quasiBundle) {
 		super(quasiBundle);
 		this.quasiBundle = quasiBundle;
 	}
 	
 	public final JMXQuasiWire[] getProvidedWires() {
 		List<QuasiExportPackage> exportPackages = this.quasiBundle.getExportPackages();
 		List<JMXQuasiWire> wires = new ArrayList<JMXQuasiWire>();
 		for (QuasiExportPackage quasiExportPackage : exportPackages) {
 			List<QuasiImportPackage> consumers = quasiExportPackage.getConsumers();
 			for (QuasiImportPackage quasiImportPackage : consumers) {
				wires.add(new JMXQuasiWire(quasiImportPackage));
 			}
 		}
 		List<QuasiBundle> fragments = this.quasiBundle.getFragments();
 		if(fragments != null && fragments.size() > 0){
 			for (QuasiBundle fragment : fragments) {
 				wires.add(new JMXQuasiWire(this.quasiBundle, fragment));
 			}
 		}
 		return wires.toArray(new JMXQuasiWire[wires.size()]);
 	}
 	
 	public final JMXQuasiWire[] getRequiredWires() {
 		List<QuasiImportPackage> importPackages = this.quasiBundle.getImportPackages();
 		List<JMXQuasiWire> wires = new ArrayList<JMXQuasiWire>();
 		for (QuasiImportPackage quasiImportPackage : importPackages) {
			wires.add(new JMXQuasiWire(quasiImportPackage));
 		}
 		List<QuasiRequiredBundle> requiredBundles = this.quasiBundle.getRequiredBundles();
 		if(requiredBundles != null && requiredBundles.size() > 0){
 			for (QuasiRequiredBundle quasiRequiredBundle : requiredBundles) {
 				wires.add(new JMXQuasiWire(quasiRequiredBundle, this.quasiBundle));
 			}
 		}
 		List<QuasiBundle> hosts = this.quasiBundle.getHosts();
 		if(hosts != null && hosts.size() > 0){
 			for (QuasiBundle host : hosts) {
 				wires.add(new JMXQuasiWire(host, this.quasiBundle));
 			}
 		}
 		return wires.toArray(new JMXQuasiWire[wires.size()]);
 	}
 
 }

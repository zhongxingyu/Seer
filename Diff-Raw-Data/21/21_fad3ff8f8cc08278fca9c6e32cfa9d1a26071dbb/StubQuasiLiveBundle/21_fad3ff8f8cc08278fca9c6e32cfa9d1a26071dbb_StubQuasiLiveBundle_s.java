 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.apps.admin.core.stubs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
 import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
 import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
 
 public class StubQuasiLiveBundle implements QuasiLiveBundle {
 
     public static final String TEST_NAME = "fake.test.bundle";
     
     public static final Version TEST_VERSION = new Version("1.2.3.test");
     
     private final Bundle bundle;
     
     private final long id;
     
     public StubQuasiLiveBundle(long id, Bundle bundle) {
         this.bundle = bundle;
         this.id = id;
     }
     
     public Bundle getBundle() {
         return this.bundle;
     }
 
     public long getBundleId() {
         return this.id;
     }
 
     public List<QuasiBundle> getDependents() {
         return new ArrayList<QuasiBundle>();
     }
 
     public List<QuasiExportPackage> getExportPackages() {
         return new ArrayList<QuasiExportPackage>();
     }
 
     public List<QuasiBundle> getFragments() {
         return new ArrayList<QuasiBundle>();
     }
 
     public List<QuasiBundle> getHosts() {
         return new ArrayList<QuasiBundle>();
     }
 
     public List<QuasiImportPackage> getImportPackages() {
         return new ArrayList<QuasiImportPackage>();
     }
 
     public List<QuasiRequiredBundle> getRequiredBundles() {
         return new ArrayList<QuasiRequiredBundle>();
     }
 
     public String getSymbolicName() {
         return TEST_NAME;
     }
 
     public Version getVersion() {
         return TEST_VERSION;
     }
 
     public boolean isResolved() {
         return false;
     }
 
     public void uninstall() {
     }
     
     public List<QuasiLiveService> getExportedServices() {
         return new ArrayList<QuasiLiveService>();
     }
 
     public List<QuasiLiveService> getImportedServices() {
         return new ArrayList<QuasiLiveService>();
     }
 
     public String getState() {
         return "Not A State";
     }
 
     public File getBundleFile() {
         return null;
     }
     
 }

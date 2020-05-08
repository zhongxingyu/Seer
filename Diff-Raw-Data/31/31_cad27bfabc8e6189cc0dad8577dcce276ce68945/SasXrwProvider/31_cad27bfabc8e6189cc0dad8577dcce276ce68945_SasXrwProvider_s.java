 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.sasxremwin.provider;
 
 import java.io.File;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.modules.sas.provider.SasProvider;
 import org.jdesktop.wonderland.modules.sas.provider.SasProviderConnectionListener;
 
 /**
  * The Xremwin-specific provider.
  *
  * @author deronj
  */
 @ExperimentalAPI
 public class SasXrwProvider extends SasProvider {
 
     private SasXrwProviderMain main;
 
     public SasXrwProvider (String userName, File passwordFile, String serverUrl,
                            SasProviderConnectionListener listener, SasXrwProviderMain main) {
         super(userName, passwordFile, serverUrl, listener);
         this.main = main;
     }
 
     /** {@inheritDoc} */
     @Override
     protected void cleanup () {
         super.cleanup();
 
        // TODO: low: workaround for bug 205. This is draconian. Is there something else better? 
         try {
            Runtime.getRuntime().exec("pkill -9 Xvfb");
         } catch (Exception e) {}
 
         logger.warning("SasXrwProvider: cleaned up app processes.");
     }
 }
 

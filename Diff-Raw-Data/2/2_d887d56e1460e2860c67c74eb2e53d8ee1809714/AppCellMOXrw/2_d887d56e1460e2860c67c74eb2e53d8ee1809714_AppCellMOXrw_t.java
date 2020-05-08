 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.xremwin.server.cell;
 
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO;
 import org.jdesktop.wonderland.modules.xremwin.common.cell.AppCellXrwServerState;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 /**
  * The server-side cell for an Xremwin application.
  * 
  * @author deronj
  */
 @ExperimentalAPI
 public class AppCellMOXrw extends AppConventionalCellMO {
 
     private static final Logger logger = Logger.getLogger(AppCellMOXrw.class.getName());
     /** The parameters from the WFS file. */
     private AppCellXrwServerState serverState;
 
     /** Create an instance of AppCellMOXrw. */
     public AppCellMOXrw() {
         super();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.xremwin.client.cell.AppCellXrw";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setServerState(CellServerState state) {
         System.err.println("**************** Enter AppCellMOXrw.setServerState: state = " + state);
         super.setServerState(state);
         serverState = (AppCellXrwServerState) state;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public CellServerState getServerState(CellServerState state) {
         if (state == null) {
            state = new AppCellXrwServerState();
         }
         return super.getServerState(state);
     }
 }

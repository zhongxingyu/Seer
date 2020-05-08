 /*******************************************************************************
 * Copyright (c) 2011 Nokia Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Comarch team - initial API and implementation
 *******************************************************************************/
 package org.ned.client.command;
 
 import com.sun.lwuit.Command;
 import org.ned.client.MotdManager;
 import org.ned.client.NedMidlet;
 import org.ned.client.NedResources;
 import org.ned.client.library.NedLibrary;
 import org.ned.client.statistics.StatType;
 import org.ned.client.statistics.StatisticsManager;
 import org.ned.client.utils.ContentNotExistException;
 import org.ned.client.utils.NedConnectionUtils;
 import org.ned.client.utils.UnauthorizedLibraryUsageException;
 import org.ned.client.view.GeneralAlert;
 import org.ned.client.view.LibraryManagerScreen;
 import org.ned.client.view.LoginOnLineScreen;
 import org.ned.client.view.WaitingScreen;
 
 
 public class AddLibraryCommand extends NedCommandAsync{
     private static AddLibraryCommand instance;
 
     private AddLibraryCommand() {
         command = new Command( NedResources.ADD_LIBRARY );
     }
 
     public static AddLibraryCommand getInstance() {
         if( instance == null ) {
             instance = new AddLibraryCommand();
         }
         return instance;
     }
 
     protected void doAction( Object param ) {
         String newAddress = (String)param;
         if ( newAddress != null && !newAddress.equals( "" )) {
             WaitingScreen.show( NedResources.CONNECTING );
             try {
                 NedLibrary newLibrary = NedConnectionUtils.getLibraryInfo( newAddress );
 
                 if ( newLibrary != null ) {
                    if (NedMidlet.getSettingsManager().getAutoStatSend()) {
                         StatisticsManager.uploadStats( true );
                     }
                    MotdManager.getInstance().updateMotd();
                    WaitingScreen.dispose();
                    NedMidlet.getSettingsManager().getLibraryManager().addLibrary( newLibrary );
                    StatisticsManager.logEvent( StatType.LIBRARY_ADD, "Id=" + newLibrary.getId()
                                                                     +";Title=" + newLibrary.getTitle()
                                                                     +";Ver=" + newLibrary.getVersion() + ";" );
                 } else {
                     WaitingScreen.dispose();
                     GeneralAlert.show( NedResources.LIBRARY_NOT_EXISTS, GeneralAlert.WARNING );
                 }
             } catch ( UnauthorizedLibraryUsageException ex ) {
                 WaitingScreen.dispose();
                  if ( GeneralAlert.showQuestion(NedResources.LOGIN_AGAIN) == GeneralAlert.RESULT_YES ) {
                     new LoginOnLineScreen( LibraryManagerScreen.class ).show();
                 }
             } catch (ContentNotExistException ex){
                 WaitingScreen.dispose();
                 GeneralAlert.show( NedResources.LIBRARY_NOT_EXISTS, GeneralAlert.WARNING );
             }
         }
     }
 }

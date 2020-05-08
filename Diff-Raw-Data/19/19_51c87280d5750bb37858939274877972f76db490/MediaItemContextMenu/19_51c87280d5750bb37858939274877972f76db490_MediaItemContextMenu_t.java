 /*******************************************************************************
  * Copyright (c) 2011-2012 Nokia Corporation
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Comarch team - initial API and implementation
  *******************************************************************************/
 package org.ned.client.view;
 
 import com.sun.lwuit.Command;
 import com.sun.lwuit.List;
 import org.ned.client.Content;
 import org.ned.client.NedResources;
 import org.ned.client.command.*;
 import org.ned.client.library.advanced.LibraryElement;
 import org.ned.client.library.advanced.LibraryGeneralModel;
 
 public class MediaItemContextMenu extends ContextMenu {
 
     public static final int DOWNLOAD = 5;//number of items on the list
     public static final int PLAY = 4;
     private LibraryGeneralModel mNewModel;
 
     public MediaItemContextMenu( List aList, int aMode ) {
         super( aList, aMode );
     }
 
     protected void buildOptions() {
         Command[] options = null;
         if ( sizeList == PLAY ) {
             options = new Command[]{ PlayMediaCommand.getInstance().getCommand(),
                                      ShowDetailsCommand.getInstance().getCommand(),
                                      ShowLinksCommand.getInstance().getCommand(),
                                      DeleteContentCommand.getInstance().
                 getCommand(), };
         } else {
             options = new Command[]{ ShowDetailsCommand.getInstance().getCommand(),
                                      ShowLinksCommand.getInstance().getCommand(),
                                      InstantDownloadCommand.getInstance().
                 getCommand(),
                                      AddToDownloadQueueCommand.getInstance().
                 getCommand(),
                                      DeleteContentCommand.getInstance().
                 getCommand(), };
         }
         buildOptions( options );
     }
 
     protected void buildCommands() {
         String[] commands = new String[]{ NedResources.CANCEL, NedResources.SELECT };
         buildCommands( commands );
     }
 
     protected void action( Command cmd ) {
         LibraryElement content = (LibraryElement)mMediaItemList.getSelectedItem();
         if ( cmd == PlayMediaCommand.getInstance().getCommand() ) {
             if ( content.isNew() ) {
                 content.setNew( false );
                 mNewModel.removeFromUpdated( content.getDetails().getId() );
             }
             PlayMediaCommand.getInstance().execute( content.getDetails() );
         } else if ( cmd == ShowDetailsCommand.getInstance().getCommand() ) {
             ShowDetailsCommand.getInstance().execute( content.getDetails() );
         } else if ( cmd == DeleteContentCommand.getInstance().getCommand() ) {
             DeleteContentCommand.getInstance().execute( mMediaItemList );
         } else if ( cmd == ShowLinksCommand.getInstance().getCommand() ) {
             ShowLinksCommand.getInstance().execute( content.getDetails() );
         } else if ( cmd == InstantDownloadCommand.getInstance().getCommand() ) {
             InstantDownloadCommand.getInstance().execute( content.getDetails() );
            new DownloadQueueScreen( content ).show();
         } else if ( cmd == AddToDownloadQueueCommand.getInstance().getCommand() ) {
             AddToDownloadQueueCommand.getInstance().execute( content.getDetails() );
            new DownloadQueueScreen( content ).show();
         }
     }
 
     void setModel( LibraryGeneralModel aNewModel ) {
         mNewModel = aNewModel;
     }
 }

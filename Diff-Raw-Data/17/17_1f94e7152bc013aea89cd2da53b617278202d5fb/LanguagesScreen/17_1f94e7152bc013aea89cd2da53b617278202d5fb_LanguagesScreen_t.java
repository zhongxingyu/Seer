 /*******************************************************************************
  * Copyright (c) 2012 Nokia Corporation
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Comarch team - initial API and implementation
  *******************************************************************************/
 package org.ned.client.view;
 
 import com.sun.lwuit.Display;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.events.SelectionListener;
 import com.sun.lwuit.layouts.BoxLayout;
 import java.util.Enumeration;
 import java.util.Vector;
 import org.ned.client.LanguageLister;
 import org.ned.client.LanguageManager;
 import org.ned.client.NedMidlet;
 import org.ned.client.NedResources;
 import org.ned.client.command.*;
 import org.ned.client.utils.LanguageInfo;
 import org.ned.client.view.customComponents.NedList;
 import org.ned.client.view.renderer.RadioButtonCellRenderer;
 
 public class LanguagesScreen extends NedFormBase implements ActionListener, SelectionListener {
 
     private NedList mCommands;
     private LanguageLister mLanguage;
     private LanguageManager mManager;
 
     public LanguagesScreen() {
         super();
         mLanguage = new LanguageLister( NedMidlet.getAccountManager().
                 getLocalizationUri() );
         setLayout( new BoxLayout( BoxLayout.Y_AXIS ) );
         setNedTitle( NedResources.LANGUAGES );
         LanguageInfo currentLanguage = NedMidlet.getAccountManager().getLanguage();
         String currentLanguageName = null;
         if ( currentLanguage != null ) {
             currentLanguageName = currentLanguage.getLocale();
         }
         mManager = new LanguageManager();
 
         mCommands = new NedList( mManager.getLanguages() );
         mCommands.setContextMenu( new LanguageContextMenu( mCommands, 1 ) );
        mCommands.setMinElementHeight( 7 );
         mCommands.setWidth( Display.getInstance().getDisplayWidth() );
         mCommands.setRenderer(
                 new RadioButtonCellRenderer( currentLanguageName == null
                                              ? "en-GB" : currentLanguageName ) );
         mCommands.addActionListener( this );
         mCommands.addSelectionListener( this );
         addComponent( mCommands );
         commandList();
         addCommandListener( this );
     }
 
     private void commandList() {
         addCommand( BackLanguagesCommand.getInstance().getCommand() );
         addCommand( HelpCommand.getInstance().getCommand() );
 
         if ( mCommands.getSelectedItem() != null
                 && ((LanguageInfo)mCommands.getSelectedItem()).isLocal() ) {
             addCommand( DownloadLanguageAgainCommand.getInstance().getCommand() );
         }
 
         addCommand( CheckForLanguageUpdateCommand.getInstance().getCommand() );
         addCommand( ConfirmLanguageSelectCommand.getInstance().getCommand() );
     }
 
     public void actionPerformed( ActionEvent evt ) {
         Object src = evt.getSource();
         if ( src == BackLanguagesCommand.getInstance().getCommand() ) {
             BackLanguagesCommand.getInstance().execute( null );
         } else if ( src
                 == ConfirmLanguageSelectCommand.getInstance().getCommand()
                 || (src == mCommands && mCommands.getSelectedIndex() >= 0) ) {
             LanguageInfo newLanguage = (LanguageInfo)mCommands.getSelectedItem();
             if ( !newLanguage.isLocal() ) {
                 DownloadLanguageCommand.getInstance().beginAsync( newLanguage, new LanguageDownloadedCallback(), true );
             } else {
                 mCommands.setRenderer( new RadioButtonCellRenderer( newLanguage.
                         getLocale() ) );
                 ConfirmLanguageSelectCommand.getInstance().execute( newLanguage );
             }
         } else if ( src == HelpCommand.getInstance().getCommand() ) {
             HelpCommand.getInstance().execute( this.getClass() );
         } else if ( src == CheckForLanguageUpdateCommand.getInstance().
                 getCommand() ) {
             CheckForLanguageUpdateCommand.getInstance().beginAsync( mLanguage, new CheckCallback(), true );
         } else if ( src
                 == DownloadLanguageAgainCommand.getInstance().getCommand() ) {
             LanguageInfo language = (LanguageInfo)mCommands.getSelectedItem();
             DownloadLanguageAgainCommand.getInstance().beginAsync( language, new DownloadAgainCallback( mCommands ), true );
         }
     }
 
     public void selectionChanged( int i, int i1 ) {
         commandList();
     }
 
     private class CheckCallback implements AsyncCompletedCallback {
 
         public void onSuccess() {
             Vector newLang = mLanguage.getNew();
             if ( newLang.isEmpty() ) {
                 GeneralAlert.show( NedResources.NO_NEW_LANGUAGE, GeneralAlert.INFO, true );
             } else {
                 String messageStr = NedResources.NEW_LANGUAGE + ":\n";
                 for ( int idx = 0; idx < newLang.size(); idx++ ) {
                     messageStr += ((LanguageInfo)newLang.elementAt( idx )).
                             getLangName();
                     if ( idx != newLang.size() - 1 ) {
                         messageStr += ", ";
                     }
                 }
                 GeneralAlert.show( messageStr, GeneralAlert.INFO, true );
                 Enumeration iter = newLang.elements();
                 while ( iter.hasMoreElements() ) {
                     mCommands.getModel().addItem( iter.nextElement() );
                 }
                 mManager.saveSetup();
             }
         }
 
         public void onFailure( String error ) {
             GeneralAlert.show( error, GeneralAlert.WARNING, true );
         }
     }
 
     private class LanguageDownloadedCallback implements AsyncCompletedCallback {
 
         public void onSuccess() {
             mCommands.repaint();
             LanguageInfo newLanguage = (LanguageInfo)mCommands.getSelectedItem();
             mCommands.setRenderer( new RadioButtonCellRenderer( newLanguage.
                     getLocale() ) );
             ConfirmLanguageSelectCommand.getInstance().execute( newLanguage );
         }
 
         public void onFailure( String error ) {
             GeneralAlert.show( error, GeneralAlert.WARNING, true );
         }
     }
 }

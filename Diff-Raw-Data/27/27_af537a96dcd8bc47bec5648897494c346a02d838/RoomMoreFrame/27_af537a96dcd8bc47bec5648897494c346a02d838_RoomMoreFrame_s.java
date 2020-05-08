 package com.tomclaw.mandarin.main;
 
 import com.tomclaw.mandarin.core.Handler;
 import com.tomclaw.mandarin.molecus.*;
 import com.tomclaw.tcuilite.*;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 import com.tomclaw.utils.StringUtil;
 import java.util.Vector;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.*;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class RoomMoreFrame extends Window {
 
   private Pane pane;
   private int featuresCount = 0;
   private TextBoxImpl passBox;
   private Label tabLabel;
   private DiscoItem discoItem;
   /** Features **/
   private boolean isTemporaryRoom;
 
   public RoomMoreFrame( DiscoItem discoItem ) {
     super( MidletMain.screen );
     /** Applying jid **/
     this.discoItem = discoItem;
     /** Previous window **/
     s_prevWindow = MidletMain.roomsFrame;
     /** Header **/
     header = new Header( Localization.getMessage( "ROOM_MORE" ) );
     /** Soft **/
     soft = new Soft( screen );
     /** Right soft **/
     soft.rightSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
 
       public void actionPerformed() {
         /** Returning to the previous frame **/
         MidletMain.screen.setActiveWindow( s_prevWindow );
       }
     };
     /** Left soft **/
     soft.leftSoft = new PopupItem( Localization.getMessage( "ENTER" ) ) {
 
       public void actionPerformed() {
         /** Checking for password-protected and temporary feature **/
         boolean isPasswordProtected = false;
         isTemporaryRoom = false;
         for ( int c = 0; c < pane.items.size(); c++ ) {
           /** Obtain feature string **/
           String feature = ( ( PaneObject ) pane.items.elementAt( c ) ).getName();
           /** Checking for feature is no null **/
           if ( feature != null ) {
             /** Checking for feature equals feature **/
             if ( feature.equals( "muc_passwordprotected" ) ) {
               isPasswordProtected = true;
             } else if ( feature.equals( "muc_temporary" ) ) {
               isTemporaryRoom = true;
             }
           }
         }
         LogUtil.outMessage( "Password protected: " + isPasswordProtected );
         if ( isPasswordProtected ) {
           /** Dialog soft **/
           Soft t_soft = new Soft( screen );
           /** Left soft **/
           t_soft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {
 
             public void actionPerformed() {
               /** Closing dialog**/
               RoomMoreFrame.this.closeDialog();
               /** Checking for password box instance **/
               if ( passBox == null ) {
                 /** Creating password box instance **/
                 passBox = new TextBoxImpl( Localization.getMessage( "PASSWORD" ), "", 256, TextField.PASSWORD );
               }
               /** Setup text box as current display **/
               Display.getDisplay( MidletMain.midletMain ).setCurrent( passBox );
             }
           };
           /** Right soft **/
           t_soft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {
 
             public void actionPerformed() {
               /** Closing dialog**/
               RoomMoreFrame.this.closeDialog();
             }
           };
           /** Showing message **/
           Handler.showDialog( RoomMoreFrame.this, t_soft, "SOMETHING_NEED", Localization.getMessage( "ROOM_PASS_PROT" ) );
         } else {
           mechanismInvokation( null );
         }
       }
     };
     /** Creating pane object **/
     pane = new Pane( null, false );
     /** Setting up tab object as root object **/
     setGObject( pane );
   }
 
   public void setMainObjects( Vector objects ) {
     /** Making all objects not focusable **/
     for ( int c = 0; c < objects.size(); c++ ) {
       ( ( PaneObject ) objects.elementAt( c ) ).setFocusable( false );
       pane.items.insertElementAt( objects.elementAt( c ), c );
     }
   }
 
   public void setHeader( String header ) {
     /** Header label **/
     tabLabel = new Label( header );
     /** Setting up header label **/
     tabLabel.setHeader( true );
     pane.items.insertElementAt( tabLabel, 0 );
   }
 
   public void addFeatureItem( String feature ) {
     /** Localizing feature **/
     String localized = Localization.getMessage( feature );
     /** Checking for feature implemented **/
     if ( !localized.equals( Localization._DEFAULT_STRING ) ) {
       /** Checking for features count to add features title **/
       if ( featuresCount == 0 ) {
         Label title = new Label( Localization.getMessage( "FEATURES" ) );
         title.setTitle( true );
         pane.items.addElement( title );
       }
       /** Adding label to items **/
       Label label = new Label( "- ".concat( localized ) );
       label.setName( feature );
       pane.items.addElement( label );
       featuresCount++;
     }
   }
 
   /**
    * Method to start creating/upload bookmark to server
    * and join selected room
    * @param password 
    */
   private void mechanismInvokation( String password ) {
     /** Obtain room item instance **/
     RoomItem roomItem;
     /** Checking for bookmark exists in roster **/
     roomItem = Handler.getBookmark( discoItem );
     /** There are no such bookmark in roster **/
     if ( roomItem == null ) {
       /** Creating new room item instance **/
       roomItem = new RoomItem( discoItem.getJid(), tabLabel.caption, false, false );
       /** Configuring room item **/
       roomItem.setRoomPassword( password );
       roomItem.setRoomNick( AccountRoot.getNickName() );
       roomItem.updateUi();
       /** Checking for room is temporary **/
       if ( isTemporaryRoom ) {
         /** Setting up temporary flag **/
         roomItem.setTemp( true );
         /** Adding room item into buddy list **/
         Handler.getBuddyList().roomsGroupItem.addChild( roomItem );
       } else {
         /** Mechanism invokation **/
         Mechanism.sendBookmarksOperation( Mechanism.OPERATION_ADD, roomItem, false, false );
       }
     } else {
       /** Checking for local bookmark temporarity is not the same on server **/
       if ( !roomItem.getTemp() && isTemporaryRoom ) {
         /** On server is temporary bookmark, but local is persistent
          so, removing bookmark on server, reconfigure local **/
         Mechanism.sendBookmarksOperation( Mechanism.OPERATION_REMOVE, roomItem, false, false );
       }
       /** Applying defined values **/
       RoomEditFrame.updateRoomItemAttempt( roomItem, isTemporaryRoom, tabLabel.caption,
               roomItem.getRoomNick(), ( password == null ? "" : password ), false, false );
     }
     /** Checking for room status to enter **/
     if ( roomItem.getStatusIndex() == StatusUtil.offlineIndex ) {
       /** Entering existing room **/
       Mechanism.enterRoomRequest( roomItem );
     } else {
       /** Opening chat frame for this room **/
      Handler.roomEnteringComplete( roomItem, false );
     }
   }
 
   private class TextBoxImpl extends TextBox implements CommandListener {
 
     public TextBoxImpl( String title, String text, int maxSize,
             int constraints ) {
       super( title, text, maxSize, constraints );
       addCommand( new Command( Localization.getMessage( "SAVE" ),
               Command.OK, 0x02 ) );
       addCommand( new Command( Localization.getMessage( "BACK" ),
               Command.BACK, 0x01 ) );
       setCommandListener( TextBoxImpl.this );
     }
 
     public void commandAction( Command c, Displayable d ) {
       switch ( c.getCommandType() ) {
         case Command.OK: {
           /** Obtain password **/
           String password = getString();
           /** Checking for password is empty **/
           if ( StringUtil.isEmptyOrNull( password ) ) {
             /** Nothing to do **/
             break;
           }
           LogUtil.outMessage( "Password saving: " + password );
           mechanismInvokation( password );
         }
         case Command.BACK: {
           /** Apply full screen mode **/
           MidletMain.screen.setFullScreenMode( true );
           /** This is back command, returning to frame **/
           Display.getDisplay( MidletMain.midletMain ).setCurrent(
                   MidletMain.screen );
           break;
         }
       }
     }
   }
 }

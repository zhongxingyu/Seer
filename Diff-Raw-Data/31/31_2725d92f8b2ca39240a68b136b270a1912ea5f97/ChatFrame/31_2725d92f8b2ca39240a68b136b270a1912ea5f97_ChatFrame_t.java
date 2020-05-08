 package com.tomclaw.mandarin.main;
 
 import com.tomclaw.mandarin.core.Handler;
 import com.tomclaw.mandarin.molecus.AccountRoot;
 import com.tomclaw.mandarin.molecus.BuddyItem;
 import com.tomclaw.mandarin.molecus.RoomItem;
 import com.tomclaw.mandarin.molecus.RoomUtil;
 import com.tomclaw.mandarin.molecus.StatusUtil;
 import com.tomclaw.mandarin.molecus.TemplateCollection;
 import com.tomclaw.tcuilite.*;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 import com.tomclaw.utils.StringUtil;
 import com.tomclaw.utils.TimeUtil;
 import com.tomclaw.xmlgear.XmlSpore;
 import java.util.Vector;
 import javax.microedition.lcdui.*;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class ChatFrame extends Window {
 
   private Tab chatTabs;
   private Pane chatPane;
   private TextBox textBox;
   private PopupItem linksPopupItem;
   /** Static tab labels **/
   private static Label tabLabelBuddyOffline;
   /** Constants **/
   private static final String URL_SYMBOLS = "%/?&#=$-_.+!*'(),0123456789abcdefghij"
           + "klmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
   public ChatFrame() {
     super( MidletMain.screen );
     /** Soft **/
     soft = new Soft( MidletMain.screen );
     soft.rightSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
       public void actionPerformed() {
         MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
       }
     };
     soft.leftSoft = new PopupItem( Localization.getMessage( "MENU" ) ) {
       public void actionPerformed() {
         /** Creating popup item **/
         if ( linksPopupItem == null ) {
           linksPopupItem = new PopupItem( Localization.getMessage( "GOTO_LINK" ) );
         } else if ( !linksPopupItem.isEmpty() ) {
           /** Removing all items **/
           linksPopupItem.subPopup.items.removeAllElements();
         }
         /** Checking for any item is focused **/
         if ( !chatPane.items.isEmpty() && chatPane.psvLstFocusedIndex >= 0
                 && chatPane.psvLstFocusedIndex < chatPane.items.size() ) {
           /** Obtain chat item **/
           ChatItem chatItem = ( ( ChatItem ) chatPane.items.elementAt(
                   chatPane.psvLstFocusedIndex ) );
           /** Checking for chat item and links is not null **/
           if ( chatItem != null && chatItem.links != null ) {
             /** Cycling all links **/
             for ( int c = 0; c < chatItem.links.length; c++ ) {
               /** Obtain link **/
               final String link = chatItem.links[c];
               /** Adding sub item **/
               linksPopupItem.addSubItem( new PopupItem( fitTextWidth( link ) ) {
                 public void actionPerformed() {
                   try {
                     /** Going to hyperlink **/
                     MidletMain.midletMain.platformRequest( link );
                   } catch ( Throwable ex ) {
                     LogUtil.outMessage( "Unable to go to " + link + " cause " + ex.getMessage() );
                   }
                 }
               } );
             }
           }
         }
         /** Checking for no links **/
         if ( linksPopupItem.isEmpty() ) {
           /** Removing goto popup item **/
           soft.leftSoft.subPopup.items.removeElement( linksPopupItem );
         } else if ( !soft.leftSoft.subPopup.items.contains( linksPopupItem ) ) {
           /** Inserting sub popup **/
           soft.leftSoft.subPopup.items.insertElementAt( linksPopupItem, 1 );
         }
       }
     };
     soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "WRITE" ) ) {
       public void actionPerformed() {
         /** Obtain selected chat tab **/
         ChatTab chatTab = getSelectedChatTab();
         /** Checking for something strange **/
         if ( chatTab != null ) {
           /** Error cause **/
           String errorCause = null;
           /** Checking for user is offline **/
           if ( AccountRoot.isOffline() ) {
             errorCause = "YOU_ARE_OFFLINE";
           }
           /** Checking for o error ant this is MUC tab **/
           if ( errorCause == null && chatTab.isMucTab() ) {
             /** Obtain room item **/
             RoomItem roomItem = ( RoomItem ) chatTab.buddyItem;
             /** Checking MUC tab type **/
             boolean isMainRoomTab =
                     StringUtil.isEmptyOrNull( chatTab.resource.resource );
             /** Checking privileges **/
             if ( !roomItem.getRoomActive() ) {
               errorCause = "ROOM_IS_INACTIVE";
             } else if ( isMainRoomTab
                     && !RoomUtil.checkPrivilege( roomItem.getRole(),
                     roomItem.getAffiliation(),
                     RoomUtil.SEND_MESSAGES_TO_ALL ) ) {
               errorCause = "NO_SEND_MESS_TO_ALL_PRIV";
             } else if ( !isMainRoomTab
                     && !RoomUtil.checkPrivilege( roomItem.getRole(),
                     roomItem.getAffiliation(),
                     RoomUtil.SEND_PRIVATE_MESSAGES ) ) {
               errorCause = "NO_SEND_PRIVATE_MESS_PRIV";
             }
           }
           /** Checking for error is null **/
           if ( errorCause == null ) {
             /** Setup text box title as buddy item nick name **/
             textBox.setTitle( chatTab.title );
             /** Setup text box as current display **/
             Display.getDisplay( MidletMain.midletMain ).setCurrent( textBox );
           } else {
             /** Showing error **/
             Handler.showError( errorCause );
           }
         }
       }
     } );
     soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "CLOSE" ) ) {
       public void actionPerformed() {
         /** Checking for chat tab index value **/
         if ( chatTabs.selectedIndex >= 0
                 && chatTabs.selectedIndex < chatTabs.items.size() ) {
           /** Removing selected chat tab **/
           removeChatTab( chatTabs.selectedIndex );
           /** Checking for frame empty **/
           if ( chatTabs.items.isEmpty() ) {
             /** Validating **/
             chatTabs.validateSelection();
             /** Switching to main frame **/
             MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
             /** Locking touch sliding here **/
             MidletMain.mainFrame.s_nextWindow = null;
           } else {
             /** Validating **/
             if ( !chatTabs.validateSelection() ) {
               /** Item was replaced by the nearest
                and index is the same, but not container **/
               chatTabs.tabEvent.stateChanged( chatTabs.selectedIndex,
                       chatTabs.selectedIndex, chatTabs.items.size() );
             }
           }
         }
       }
     } );
     /** Init text box **/
     initTextBox();
     /** Init tabs **/
     chatTabs = new Tab( MidletMain.screen );
     chatTabs.tabEvent = new TabEvent() {
       public void stateChanged( int prevIndex, int currIndex, int totlItems ) {
         try {
           /** Checking for null type of chat items **/
           ChatTab chatTab = ( ( ChatTab ) chatTabs.items.elementAt( currIndex ) );
           if ( chatTab.chatItems == null ) {
             chatTab.chatItems = new Vector();
           }
           /** Setup items **/
           chatPane.items = chatTab.chatItems;
           /** Preparing graphics **/
           ChatFrame.this.prepareGraphics();
           /** Scrolling **/
           if ( chatPane.maxHeight > chatPane.getHeight() ) {
             chatPane.yOffset = chatPane.maxHeight - chatPane.getHeight();
           } else {
             chatPane.yOffset = 0;
           }
           /** Checking focus **/
           if ( !chatPane.items.isEmpty() ) {
             chatPane.setFocused( chatPane.items.size() - 1 );
           }
           LogUtil.outMessage( "Focused index: " + chatPane.getFocused() );
           /** Reset unread messages count **/
           chatTab.resource.setUnreadCount( 0 );
           chatTab.buddyItem.updateUi();
           chatTab.updateUi();
           /** Repainting **/
           screen.repaint();
         } catch ( Throwable ex1 ) {
           LogUtil.outMessage( "chatTabs.tabEvent: " + ex1.getMessage() );
         }
       }
     };
     /** Init pane **/
     chatPane = new Pane( this, true );
     chatTabs.setGObject( chatPane );
     /** Set GObject **/
     setGObject( chatTabs );
   }
 
   private void initTextBox() {
     textBox = new TextBox( "", "", com.tomclaw.mandarin.core.Settings.textBoxMaxSize, TextField.ANY );
     textBox.addCommand( new Command( Localization.getMessage( "SEND" ), Command.OK, 4 ) );
     textBox.addCommand( new Command( Localization.getMessage( "BACK" ), Command.BACK, 3 ) );
     textBox.addCommand( new Command( Localization.getMessage( "SMILES" ), Command.HELP, 2 ) );
     textBox.addCommand( new Command( Localization.getMessage( "CLEAR" ), Command.EXIT, 1 ) );
     textBox.setCommandListener( new CommandListener() {
       public void commandAction( Command c, Displayable d ) {
         switch ( c.getCommandType() ) {
           case Command.OK: {
             /** Creating xml spore **/
             XmlSpore xmlSpore = new XmlSpore() {
               public void onRun() throws Throwable {
                 /** Checking for account online **/
                 if ( AccountRoot.getStatusIndex() != StatusUtil.offlineIndex ) {
                   ChatTab chatTab = getSelectedChatTab();
                   /** Checking for something strange **/
                   if ( chatTab != null ) {
                     /** Checking for empty text **/
                     String message = textBox.getString();
                     if ( !StringUtil.isEmptyOrNull( message ) ) {
                       /** Defining resource **/
                       String fullJid;
                       /** Checking buddy status **/
                       if ( chatTab.buddyItem.getStatusIndex()
                               == StatusUtil.offlineIndex ) {
                         /** Message must be sent to all resources **/
                         fullJid = chatTab.buddyItem.getJid();
                       } else {
                         /** Message will be sent to resource directly **/
                         fullJid = chatTab.buddyItem.getFullJid( chatTab.resource );
                       }
                       /** Detecting message type **/
                       boolean isMucMessage = false;
                       /** Checking for muc tab **/
                       if ( chatTab.isMucTab() ) {
                         /** Checking for resource **/
                         if ( StringUtil.isEmptyOrNull( chatTab.resource.resource ) ) {
                           /** This is empty resource, muc **/
                           isMucMessage = true;
                         }
                       }
                       /** Sending message **/
                       String cookie = TemplateCollection.sendMessage(
                               this, fullJid,
                               isMucMessage ? "groupchat" : "chat",
                               message, null );
                       /** Checking for message type **/
                       if ( !isMucMessage ) {
                         /** Check and prepare message **/
                         message = checkMessage( AccountRoot.getNickName(),
                                 message, null, chatTab.isMucTab() );
                         /** Adds chat item to selected chat frame **/
                         MidletMain.chatFrame.addChatItem( chatTab, cookie,
                                 ChatItem.TYPE_PLAIN_MSG, false,
                                 AccountRoot.getNickName(), message, null );
                         /** Repainting **/
                         MidletMain.screen.repaint();
                       }
                       /** Clearing text box **/
                       textBox.setString( "" );
                     }
                   }
                 }
               }
 
               public void onError( Throwable ex ) {
                 LogUtil.outMessage( "Error while send message instructions: "
                         + ex.getMessage(), true );
               }
             };
             /** Releasing xml spore **/
             AccountRoot.getSession().getSporedStream().releaseSpore( xmlSpore );
             /** Setup TCUI screen as current **/
             Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
             /** Apply full screen mode **/
             MidletMain.screen.setFullScreenMode( true );
             break;
           }
           case Command.EXIT: {
             textBox.setString( "" );
             break;
           }
           case Command.BACK: {
             /** Setup TCUI screen as current **/
             Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
             MidletMain.screen.setFullScreenMode( true );
             break;
           }
           case Command.HELP: {
             /*if ( MidletMain.smilesFrame == null ) {
              MidletMain.smilesFrame = new SmilesFrame();
              }
              Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
              MidletMain.smilesFrame.prepareGraphics();
              MidletMain.screen.setFullScreenMode( true );
              MidletMain.screen.activeWindow = MidletMain.smilesFrame;
              MidletMain.screen.repaint( Screen.REPAINT_STATE_PLAIN );*/
             break;
           }
         }
       }
     } );
   }
 
   /**
    * Adding chat tab to chatTabs
    * @param chatTab
    * @param isSwitchTo 
    */
   public void addChatTab( ChatTab chatTab, boolean isSwitchTo ) {
     /** Adding chat tab item **/
     chatTabs.addTabItem( chatTab );
     if ( isSwitchTo ) {
       /** Switching **/
       prepareGraphics();
       chatTabs.switchTabTo( chatTabs.items.size() - 1 );
     }
     /** Switching buddy item flag, that dialog is opened **/
     chatTab.buddyItem.setDialogOpened( true );
     /** Update buddy item UI **/
     chatTab.buddyItem.updateUi();
     /** Checking for main frame new window **/
     if ( MidletMain.mainFrame.s_nextWindow == null ) {
       MidletMain.mainFrame.s_nextWindow = MidletMain.chatFrame;
     }
   }
 
   public static String checkMessage( String nickName, String message, String subject, boolean isMuc ) {
     /** Checking for message is subject or message with subject **/
     if ( message != null ) {
       /** Message text correction **/
       message = StringUtil.replace( message, "[", "\\[" );
       message = StringUtil.replace( message, "]", "\\]" );
       // message = Smiles.replaceSmilesForCodes( message );
       message = StringUtil.replace( message, "\n", "[br/]" );
       /** Checking for /me command (XEP-0245) in muc **/
       if ( isMuc && message.startsWith( "/me " ) ) {
         message = "[i]* [b][c=purple]".concat( nickName ).concat( " [/c][/b]" ).
                 concat( message.substring( 4 ) ).concat( "[/i]" );
       }
       message = "[p]".concat( message ).concat( "[/p]" );
       /** Checking for subject is not null, not equals to the body 
        * and nick name present **/
       if ( subject != null && !subject.equals( message )
               && nickName.length() > 0 ) {
         message = "[b]".concat( subject ).concat( "[/b][br/]" ).concat( message );
       }
     } else if ( isMuc && subject != null && message == null ) {
       /** This is muc topic **/
       message = "[i]* [b][c=purple]".concat( nickName ).concat( " " ).
               concat( Localization.getMessage( "CHANGED_ROOM_TOPIC_TO" ) ).
               concat( ": [/c][/b][br/]" ).concat( subject ).concat( "[/i]" );
     } else if ( isMuc && StringUtil.isNullOrEmpty( subject ) && message == null ) {
       /** This is muc topic **/
       message = "[i]* [b][c=purple]".concat( nickName ).concat( " " ).
               concat( Localization.getMessage( "REMOVED_TOPIC" ) ).
               concat( "[/c][/b][/i]" );
     } else {
       message = "[c=red][b]".concat( Localization.getMessage( "INVALID_MESSAGE_RECEIVED" ) ).concat( "[/b][/c]" );
     }
     return message;
   }
 
   /**
    * Searching for link inside message
    * @param message
    * @return Links array ( String[] )
    */
   public void checkForLinks( ChatItem chatItem ) {
     String message = chatItem.text;
     /** Checking for link is present at all **/
     if ( message.indexOf( "http://" ) != -1
             || message.indexOf( "https://" ) != -1
             || message.indexOf( "www." ) != -1 ) {
       Vector links = new Vector();
       String link;
       /** Searching for link entry **/
       int linkStartIndex = -1;
       for ( int c = 0; c < message.length(); c++ ) {
         if ( linkStartIndex == -1 ) {
           /** Checking for URL starts here **/
           if ( message.startsWith( "http://", c ) ) {
             linkStartIndex = c;
             c += 7;
             continue;
           } else if ( message.startsWith( "https://", c ) ) {
             linkStartIndex = c;
             c += 8;
             continue;
           } else if ( message.startsWith( "www.", c ) ) {
             linkStartIndex = c;
             c += 4;
             continue;
           }
         }
         /** Link body **/
         if ( linkStartIndex != -1 ) {
           /** Not URL symbol **/
           if ( URL_SYMBOLS.indexOf( message.charAt( c ) ) == -1 ) {
             /** Highlighting URL **/
             link = message.substring( linkStartIndex, c );
             message = message.substring( 0, linkStartIndex ).
                     concat( "[c=blue][i][u]" ).concat( link )
                     .concat( " [/u][/i][/c]" ).concat( message.substring( c ) );
             /** Adding link into vector **/
             links.addElement( link );
             /** Adding BB-tags length **/
             c += 27;
             /** Now, this is not URL body **/
             linkStartIndex = -1;
           }
         }
       }
       /** Creating links array **/
       chatItem.links = new String[ links.size() ];
       /** copying links into array **/
       links.copyInto( chatItem.links );
       chatItem.text = message;
     } else {
       chatItem.links = null;
     }
   }
 
   /**
    * Adds chat item to the specified chat tab
    * Returns true if specified chat tab is active
    * @param chatTab
    * @param message 
    * @return boolean
    */
   public boolean addChatItem( ChatTab chatTab, String cookie, int type,
           boolean isIncoming, String nickName, String message, String stamp ) {
     LogUtil.outMessage( "message = " + message );
     /** Checking for room item and nick is empty **/
     if ( chatTab.isMucTab() && nickName.length() == 0 ) {
       nickName = Localization.getMessage( "ROOM_SYSTEM" );
     }
     /** Calculating time (current or from stamp by XEP-0082 **/
     long time = ( stamp == null ) ? TimeUtil.getCurrentTimeGMT()
             : TimeUtil.getUtcTimeLong( stamp, true );
     /** Creating chat item instance **/
     ChatItem chatItem = new com.tomclaw.tcuilite.ChatItem( chatPane, message );
     chatItem.dlvStatus = isIncoming ? ChatItem.DLV_STATUS_INCOMING : ChatItem.DLV_STATUS_NOT_SENT;
     chatItem.cookie = cookie.getBytes();
     chatItem.itemType = type;
     chatItem.buddyNick = nickName;
     chatItem.buddyId = isIncoming ? chatTab.buddyItem.getJid() : AccountRoot.getClearJid();
     chatItem.itemDateTime = TimeUtil.getTimeString( time, false );
     /** Checking for links n message **/
     checkForLinks( chatItem );
     /** Adding chat item **/
     chatTab.addChatItem( chatItem );
     /** Checking for focus **/
     ChatTab selectedChatTab = getSelectedChatTab();
     /** Checking for chatTab is selected **/
     boolean isTabActive = false;
     if ( selectedChatTab != null && selectedChatTab.equals( chatTab ) ) {
       if ( chatPane.psvLstFocusedIndex == chatPane.items.size() - 2
               || chatPane.items.size() == 1 ) {
         /** Prepare graphics **/
         prepareGraphics();
         /** Scrolling to message **/
         if ( chatPane.maxHeight > chatPane.getHeight() ) {
           chatPane.yOffset = chatPane.maxHeight - chatPane.getHeight();
         } else {
           chatPane.yOffset = 0;
         }
         /** Focusing message **/
         if ( !chatPane.items.isEmpty() ) {
           chatPane.setFocused( chatPane.items.size() - 1 );
         } else {
           chatItem.setFocused( true );
         }
         isTabActive = true;
       }
     }
     return isTabActive;
   }
 
   /**
    * Returns chat tabs count
    * @return 
    */
   public int getChatTabCount() {
     return chatTabs.items.size();
   }
 
   /**
    * Checking for chat tab existing
    * @param jid
    * @param resource 
    * @return ChatTab
    */
   public ChatTab getChatTab( String jid, String resource, boolean isSwitchTo ) {
     ChatTab chatTab;
     for ( int c = 0; c < chatTabs.items.size(); c++ ) {
       chatTab = ( ChatTab ) chatTabs.items.elementAt( c );
       if ( chatTab.buddyItem.getJid().equals( jid ) && chatTab.resource.resource.equals( resource ) ) {
         if ( isSwitchTo ) {
           prepareGraphics();
           if ( !chatTabs.switchTabTo( c ) ) {
             /** Switch action wasn't performed, 
              so, clearing unread count manually **/
             chatTab.resource.unreadCount = 0;
             /** Buddy item UI update **/
             chatTab.buddyItem.updateUi();
             /** Chat tab UI update **/
             chatTab.updateUi();
           }
         }
         return chatTab;
       }
     }
     return null;
   }
 
   /**
    * Returns selected chat tab
    * @return ChatTab
    */
   public ChatTab getSelectedChatTab() {
     if ( chatTabs.selectedIndex < chatTabs.items.size()
             && chatTabs.selectedIndex >= 0 ) {
       return ( ( ChatTab ) chatTabs.items.elementAt( chatTabs.selectedIndex ) );
     }
     return null;
   }
 
   /**
    * Removing chat tab by index
    * @param index 
    */
   public void removeChatTab( int index ) {
     if ( index < chatTabs.items.size() && index >= 0 ) {
       ChatTab chatTab = ( ( ChatTab ) chatTabs.items.elementAt( index ) );
       chatTabs.items.removeElement( chatTab );
       /** Checking for setting, status and resource usage **/
       if ( com.tomclaw.mandarin.core.Settings.isRemoveOfflineResources
               && ( chatTab.resource.statusIndex == StatusUtil.offlineIndex ) ) {
         /** Resource is offline, settings is to 
          * remove offline resource and resource is not used **/
         LogUtil.outMessage( "Removing resource: ".concat( chatTab.resource.resource ) );
         chatTab.buddyItem.removeResource( chatTab.resource.resource );
       }
       /** Switching buddy item flag, that dialog is closed **/
       chatTab.buddyItem.setDialogOpened( false );
       /** Updating UI **/
       chatTab.buddyItem.updateUi();
     }
   }
 
   /**
    * Removing all chat tabs by JID
    * @param jid 
    */
   public void removeChatTabs( String jid ) {
     /** Checking for JID is not null-type or empty 
      and non-empty chat tabs items **/
     if ( !( StringUtil.isEmptyOrNull( jid )
             || chatTabs.items.isEmpty() ) ) {
       ChatTab chatTab;
       /** Cycling all items **/
       for ( int c = 0; c < chatTabs.items.size(); c++ ) {
         /** Obtain chat tab item by index **/
         chatTab = ( ChatTab ) chatTabs.items.elementAt( c );
         /** Checking for chat tab JID equals **/
         if ( chatTab.buddyItem.getJid().equals( jid ) ) {
           /** Removing item **/
           chatTabs.items.removeElementAt( c );
           /** Switching buddy item flag, that dialog is closed **/
           chatTab.buddyItem.setDialogOpened( false );
           /** Updating UI **/
           chatTab.buddyItem.updateUi();
           /** Decrementing index **/
           c--;
         }
       }
     }
     /** Validating selection, cause cursor is now may be incorrect **/
     chatTabs.validateSelection();
   }
 
   /**
    * Checks for buddy item and resource usage 
    * @param jid
    * @param resource
    * @return boolean
    */
   public boolean getBuddyResourceUsed( String jid, String resource ) {
     ChatTab chatTab;
     /** Cycling for all chat tabs **/
     for ( int c = 0; c < chatTabs.items.size(); c++ ) {
       /** Obtain chat tab item **/
       chatTab = ( ChatTab ) chatTabs.items.elementAt( c );
       /** Checking for coincidence **/
       if ( chatTab.buddyItem.getJid().equals( jid ) && ( resource == null || chatTab.resource.resource.equals( resource ) ) ) {
         /** Resource is used **/
         return true;
       }
     }
     /** No resource usage **/
     return false;
   }
 
   public ChatTab getStandAloneOnlineResourceTab( String jid ) {
     ChatTab chatTab;
     ChatTab tempTab = null;
     /** Cycling for all chat tabs **/
     for ( int c = 0; c < chatTabs.items.size(); c++ ) {
       /** Obtain chat tab item **/
       chatTab = ( ChatTab ) chatTabs.items.elementAt( c );
       /** Checking for coincidence **/
       if ( chatTab.buddyItem.getJid().equals( jid )
               && chatTab.resource.statusIndex != StatusUtil.offlineIndex ) {
         if ( tempTab == null ) {
           tempTab = chatTab;
         } else {
           return null;
         }
       }
     }
     return tempTab;
   }
 
   public void updateBuddyItems() {
     LogUtil.outMessage( "Updating buddy items" );
     ChatTab chatTab;
    int chatCount;
     for ( int c = 0; c < chatTabs.items.size(); c++ ) {
       chatTab = ( ChatTab ) chatTabs.items.elementAt( c );
       BuddyItem buddyItem = MidletMain.mainFrame.buddyList.getBuddyItem( chatTab.buddyItem.getJid() );
      /** Obtain chat buddy item instance opened chat count **/
      chatCount = chatTab.buddyItem.getOpenedDialogsCount();
       /** Checking for buddy item existance **/
       if ( buddyItem == null ) {
         /** Have no more this item **/
         LogUtil.outMessage( "Have no more ".
                 concat( chatTab.buddyItem.getJid() ).concat( " item in roser" ) );
         MidletMain.mainFrame.buddyList.makeBuddyItemTemp( chatTab.buddyItem );
         /** Equating buddy item **/
         buddyItem = chatTab.buddyItem;
       } else {
         /** Applying updated buddy item **/
         chatTab.buddyItem = buddyItem;
       }
      /** Update opened chat count **/
      chatTab.buddyItem.setOpenedDialogsCount( chatCount );
       /** Creating resource **/
       chatTab.resource = buddyItem.getResource( chatTab.resource.resource );
      /** Updating buddy item UI (cause of chat count may changed) **/
      chatTab.buddyItem.updateUi();
     }
   }
 
   public void updateTabs() {
     /** Cycling all tabs **/
     for ( int c = 0; c < chatTabs.items.size(); c++ ) {
       ( ( ChatTab ) chatTabs.items.elementAt( c ) ).updateUi();
     }
   }
 
   public void updateTab( String jid, String resource ) {
     ChatTab chatTab = getChatTab( jid, resource, false );
     /** Checking for compare **/
     if ( chatTab != null && chatTab.buddyItem.getJid().equals( jid )
             && chatTab.resource.resource.equals( resource ) ) {
       /** Updating UI **/
       chatTab.updateUi();
     }
   }
 
   public static Label getTabLabelBuddyOffline() {
     return getTabLabel( Localization.getMessage( "BUDDY_OFFLINE" ), tabLabelBuddyOffline );
   }
 
   public static Label getTabLabelAccountOffline() {
     return getTabLabel( Localization.getMessage( "ACCOUNT_OFFLINE" ), tabLabelBuddyOffline );
   }
 
   public static Label getTabLabel( String message, Label label ) {
     /** Checking for tab label is null **/
     if ( label == null ) {
       /** Creating tab label instance **/
       label = new Label( message );
       label.setHeader( true );
     }
     return label;
   }
 
   public static boolean repaintFrame() {
     /** Checking and repainting main frame  on the screen **/
     if ( MidletMain.screen.activeWindow.equals( MidletMain.chatFrame ) ) {
       MidletMain.screen.repaint();
       return true;
     }
     return false;
   }
 
   public void windowActivated() {
     /** Window activated **/
     LogUtil.outMessage( "Chat window activated" );
     /** Validate settings **/
     chatTabs.validateSelection();
     ChatTab chatTab = getSelectedChatTab();
     /** Checking for something strange **/
     if ( chatTab != null ) {
       /** Reset unread messages count **/
       chatTab.resource.setUnreadCount( 0 );
       chatTab.buddyItem.updateUi();
       chatTab.updateUi();
     }
   }
 
   private String fitTextWidth( String text ) {
     int width = screen.getWidth() - Theme.upSize * 4;
     if ( Theme.font.stringWidth( text ) > width ) {
       String buffer;
       int middle = 0;
       for ( int c = 0; c < text.length(); c++ ) {
         if ( middle == 0 ) {
           buffer = text.substring( 0, c ).concat( "..." );
           if ( Theme.font.stringWidth( buffer ) >= width / 2 ) {
             middle = c - 1;
           }
         } else {
           buffer = text.substring( text.length() - ( c - middle ) );
           if ( Theme.font.stringWidth( buffer ) >= width / 2 ) {
             return text.substring( 0, middle ).concat( "..." )
                     .concat( text.substring(
                     ( text.length() - ( c - middle ) ) - 1 ) );
           }
         }
       }
     }
     return text;
   }
 }

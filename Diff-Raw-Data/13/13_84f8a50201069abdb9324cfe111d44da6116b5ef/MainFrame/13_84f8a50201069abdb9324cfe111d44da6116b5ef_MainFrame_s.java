 package com.tomclaw.mandarin.main;
 
 import com.tomclaw.mandarin.core.Handler;
 import com.tomclaw.mandarin.core.Storage;
 import com.tomclaw.mandarin.molecus.*;
 import com.tomclaw.tcuilite.*;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class MainFrame extends Window {
 
   /** GUI **/
   public BuddyList buddyList;
   public Pane blank;
   public Header extHeader;
   /** Popup items **/
   private PopupItem emptyPopup;
   private PopupItem groupPopup;
   private PopupItem buddyPopup;
   private PopupItem roomPopup;
   /** Buddy sub popup **/
   private PopupItem subscrReqtPopup;
   private PopupItem subscrApprPopup;
   private PopupItem subscrRejtPopup;
   private PopupItem dialogPopupItem;
   private PopupItem editPopupItem;
   private PopupItem enterRoomPopupItem;
   private PopupItem editBookmarkPopupItem;
   private PopupItem removeBookmarkPopupItem;
   private PopupItem leaveRoomPopupItem;
   private PopupItem roomSettingsPopupItem;
   private PopupItem roomConfigurationPopupItem;
   private PopupItem roomChangeTopicPopupItem;
   private PopupItem roomChangeNickPopupItem;
   private PopupItem roomUsersPopupItem;
   private PopupItem roomDestroyPopupItem;
   private PopupItem roomOutcastPopupItem;
   private PopupItem roomMembersPopupItem;
   private PopupItem roomAdminsPopupItem;
   private PopupItem roomOwnersPopupItem;
 
   public MainFrame() {
     super( MidletMain.screen );
     /** Initializing GUI **/
     initGui();
     /** Hotkeys **/
     addKeyEvent( new KeyEvent( 0, "KEY_DIALOG", true ) {
       public void actionPerformed() {
         LogUtil.outMessage( "KEY_DIALOG event" );
         openDialog( null, null );
       }
     } );
   }
 
   /**
    * Checks for GUI type loaded
    */
   public void checkGui() {
     if ( AccountRoot.isReady() ) {
       if ( getGObject().equals( buddyList ) ) {
         return;
       }
     } else {
       if ( getGObject().equals( blank ) ) {
         return;
       }
     }
     initGui();
   }
 
   /**
    * Initialize buddy list and main menus
    */
   private void initGui() {
     /** Checking for AccountRoot ready **/
     if ( AccountRoot.isReady() ) {
       header = getImHeader();
       initBuddyList();
       setGObject( buddyList );
     } else {
       header = null;
       initBlank();
       setGObject( blank );
     }
   }
 
   /**
    * Returns extended header
    * @return Header
    */
   public Header getImHeader() {
     if ( extHeader == null ) {
       extHeader = new ExtHeader();
     }
     return extHeader;
   }
 
   /**
    * Initializing buddy list frame objects
    */
   private void initBuddyList() {
     /** Soft initialization **/
     soft = new Soft( MidletMain.screen );
     /** Status popup item **/
     final PopupItem statusPopupItem = new PopupItem( Localization.getMessage( "STATUS" ) );
     statusPopupItem.imageFileHash = com.tomclaw.mandarin.core.Settings.IMG_STATUS.hashCode();
     /** Filter popup item **/
     final PopupItem groupSubFilter = new PopupItem( "" ) {
       public void actionPerformed() {
         /** Inverting flag in settings **/
         com.tomclaw.mandarin.core.Settings.showGroups =
                 !com.tomclaw.mandarin.core.Settings.showGroups;
         /** Applying settings **/
         buddyList.updateSettings();
         /** Saving updated settings **/
         com.tomclaw.mandarin.core.Settings.saveAll();
         Storage.save();
       }
     };
     final PopupItem offlineSubFilter = new PopupItem( "" ) {
       public void actionPerformed() {
         /** Inverting flag in settings **/
         com.tomclaw.mandarin.core.Settings.showOffline =
                 !com.tomclaw.mandarin.core.Settings.showOffline;
         /** Applying settings **/
         buddyList.updateSettings();
         /** Saving updated settings **/
         com.tomclaw.mandarin.core.Settings.saveAll();
         Storage.save();
       }
     };
     /** Contacts popup item **/
     final PopupItem contactsPopupItem = new PopupItem( Localization.getMessage( "CONTACTS" ) );
     final PopupItem addBuddySubItem = new PopupItem( Localization.getMessage( "ADD_BUDDY" ) );
     final PopupItem searchBuddySubItem = new PopupItem( Localization.getMessage( "SEARCH_BUDDY" ) );
     contactsPopupItem.addSubItem( addBuddySubItem );
     contactsPopupItem.addSubItem( searchBuddySubItem );
     /** Main menu popup item **/
     soft.leftSoft = new PopupItem( Localization.getMessage( "MENU" ) ) {
       public void actionPerformed() {
         /** Creating statuses if it's list is empty **/
         if ( statusPopupItem.isEmpty() ) {
           final int statusCount = StatusUtil.getStatusCount();
           for ( int c = 0; c < statusCount; c++ ) {
             final int statusIndex = c;
             String statusDescr = StatusUtil.getStatusDescr( statusIndex );
             PopupItem statusSubItem = new PopupItem( Localization.getMessage( statusDescr ) ) {
               public void actionPerformed() {
                 LogUtil.outMessage( "statusIndex = " + statusIndex );
                 /** If selected the same status index, ignoring **/
                 if ( AccountRoot.getStatusIndex() == statusIndex ) {
                   return;
                 }
                 if ( AccountRoot.getStatusIndex() == StatusUtil.offlineIndex
                         && statusIndex != StatusUtil.offlineIndex ) {
                   /** User establishes connection **/
                   Mechanism.accountLogin( statusIndex );
                 } else if ( AccountRoot.getStatusIndex() != StatusUtil.offlineIndex
                         && statusIndex == StatusUtil.offlineIndex ) {
                   /** It seems, that user going offline **/
                   Mechanism.accountLogout();
                 } else {
                   /** Plain status change **/
                   Mechanism.setStatus( statusIndex, false );
                 }
               }
             };
             statusPopupItem.addSubItem( statusSubItem );
           }
         }
         /** Updating status icon **/
         statusPopupItem.imageIndex = AccountRoot.getStatusIndex();
         /** Filter name detecting **/
         groupSubFilter.setTitle( com.tomclaw.mandarin.core.Settings.showGroups
                 ? Localization.getMessage( "HIDE_GROUPS" ) : Localization.getMessage( "SHOW_GROUPS" ) );
         offlineSubFilter.setTitle( com.tomclaw.mandarin.core.Settings.showOffline
                 ? Localization.getMessage( "HIDE_OFFLINE" ) : Localization.getMessage( "SHOW_OFFLINE" ) );
         /** Appending all connected services **/
         GroupItem services = MidletMain.mainFrame.buddyList.servicesGroupItem;
         if ( !addBuddySubItem.isEmpty() ) {
           addBuddySubItem.subPopup.items.removeAllElements();
         }
         if ( services != null ) {
           for ( int c = 0; c < services.getChildsCount(); c++ ) {
             final BuddyItem serviceItem = ( ( BuddyItem ) services.elementAt( c ) );
             PopupItem popupItem = new PopupItem( serviceItem.getUserName() ) {
               public void actionPerformed() {
                 if ( Handler.sureIsOnline() ) {
                   if ( serviceItem.getStatusIndex() != StatusUtil.offlineIndex ) {
                     Mechanism.sendPromptRequest( serviceItem.getJid() );
                   } else {
                     Handler.showError( "SERVICE_NOT_CONNECTED" );
                   }
                 }
               }
             };
             popupItem.imageFileHash = buddyList.imageLeftFileHash[1];
             popupItem.imageIndex = ( ( BuddyItem ) services.elementAt( c ) ).imageLeftIndex[1];
             addBuddySubItem.addSubItem( popupItem );
           }
         }
         /** Appending XMPP and Molecus **/
         PopupItem popupItem = new PopupItem( Localization.getMessage( "XMPP_BUDDY" ) ) {
           public void actionPerformed() {
             if ( Handler.sureIsOnline() ) {
               Mechanism.sendPromptRequest( "SERVICE_XMPP" );
             }
           }
         };
         addBuddySubItem.addSubItem( popupItem );
         popupItem = new PopupItem( Localization.getMessage( "MOLECUS_BUDDY" ) ) {
           public void actionPerformed() {
             if ( Handler.sureIsOnline() ) {
               Mechanism.sendPromptRequest( "SERVICE_MOLECUS" );
             }
           }
         };
         addBuddySubItem.addSubItem( popupItem );
       }
     };
     /** Filter popup item **/
     final PopupItem filterPopupItem = new PopupItem( Localization.getMessage( "FILTER" ) );
     filterPopupItem.addSubItem( groupSubFilter );
     filterPopupItem.addSubItem( offlineSubFilter );
     /** Dialogs popup item **/
     final PopupItem dialogsPopupItem = new PopupItem( Localization.getMessage( "DIALOGS" ) ) {
       public void actionPerformed() {
         if ( MidletMain.chatFrame.getChatTabCount() != 0 ) {
           MidletMain.screen.setActiveWindow( MidletMain.chatFrame );
         } else {
           Handler.showError( "NO_DIALOGS" );
         }
       }
     };
     /** Chatrooms **/
     final PopupItem muchatPopupItem = new PopupItem( Localization.getMessage( "CHATROOMS" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Showing frame **/
           Handler.showRoomsFrame();
         }
       }
     };
     /** Settings popup item **/
     final PopupItem settngPopupItem = new PopupItem( Localization.getMessage( "SETTINGS" ) );
     final PopupItem accountSubItem = new PopupItem( Localization.getMessage( "ACCOUNT" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem appearanceSubItem = new PopupItem( Localization.getMessage( "APPEARANCE" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem notificationSubItem = new PopupItem( Localization.getMessage( "NOTIFICATION" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem networkSubItem = new PopupItem( Localization.getMessage( "NETWORK" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem tariffingSubItem = new PopupItem( Localization.getMessage( "TARIFFING" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem hotkeysSubItem = new PopupItem( Localization.getMessage( "HOTKEYS" ) ) {
       public void actionPerformed() {
         // BuddyItem roomItem = new BuddyItem("kkx@akl.com", "KKX", "none", true);
         // roomItem.updateUi();
         // MidletMain.mainFrame.buddyList.makeBuddyItemTemp( roomItem );
         MidletMain.mainFrame.buddyList.createTempBuddyItem( "kkx@akl.com" );
         // MidletMain.mainFrame.buddyList.roomsGroupItem.addChild( roomItem );
       }
     };
     settngPopupItem.addSubItem( accountSubItem );
     settngPopupItem.addSubItem( appearanceSubItem );
     settngPopupItem.addSubItem( notificationSubItem );
     settngPopupItem.addSubItem( networkSubItem );
     settngPopupItem.addSubItem( tariffingSubItem );
     settngPopupItem.addSubItem( hotkeysSubItem );
     /** More popup item **/
     final PopupItem morePopupItem = new PopupItem( Localization.getMessage( "MORE" ) );
     final PopupItem updateCheckSubItem = new PopupItem( Localization.getMessage( "UPDATE_CHECK" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem feedbackSubItem = new PopupItem( Localization.getMessage( "FEEDBACK" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem netCheckSubItem = new PopupItem( Localization.getMessage( "NET_CHECK" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem trafficSubItem = new PopupItem( Localization.getMessage( "TRAFFIC" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem donateSubItem = new PopupItem( Localization.getMessage( "DONATE" ) ) {
       public void actionPerformed() {
       }
     };
     final PopupItem aboutSubItem = new PopupItem( Localization.getMessage( "ABOUT" ) ) {
       public void actionPerformed() {
       }
     };
     morePopupItem.addSubItem( updateCheckSubItem );
     morePopupItem.addSubItem( feedbackSubItem );
     morePopupItem.addSubItem( netCheckSubItem );
     morePopupItem.addSubItem( trafficSubItem );
     morePopupItem.addSubItem( donateSubItem );
     morePopupItem.addSubItem( aboutSubItem );
     /** Minimize popup item **/
     final PopupItem minimizePopupItem = new PopupItem( Localization.getMessage( "MINIMIZE" ) ) {
       public void actionPerformed() {
         MidletMain.minimizeApp();
       }
     };
 
     /** Exit popup item **/
     final PopupItem exitPopupItem = new PopupItem( Localization.getMessage( "EXIT" ) ) {
       public void actionPerformed() {
         MidletMain.exitApp();
       }
     };
     /** Right soft root **/
     emptyPopup = new PopupItem( Localization.getMessage( "ELEMENT" ) ) {
       public void actionPerformed() {
         updatePopup();
       }
     };
     /** Group menu **/
     groupPopup = new PopupItem( Localization.getMessage( "ELEMENT" ) ) {
       public void actionPerformed() {
         updatePopup();
       }
     };
     groupPopup.addSubItem( new PopupItem( Localization.getMessage( "RENAME" ) ) {
       public void actionPerformed() {
       }
     } );
     groupPopup.addSubItem( new PopupItem( Localization.getMessage( "REMOVE" ) ) {
       public void actionPerformed() {
       }
     } );
     /** Buddy menu **/
     buddyPopup = new PopupItem( Localization.getMessage( "ELEMENT" ) ) {
       public void actionPerformed() {
         updatePopup();
       }
     };
     dialogPopupItem = new PopupItem( Localization.getMessage( "DIALOG" ) ) {
       public void actionPerformed() {
         /** Opening default resource dialog **/
         LogUtil.outMessage( "Dialog action" );
         MidletMain.mainFrame.getKeyEvent( "KEY_DIALOG" ).actionPerformed();
       }
     };
     buddyPopup.addSubItem( dialogPopupItem );
     editPopupItem = new PopupItem( Localization.getMessage( "EDIT" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null ) {
             BuddyEditFrame buddyEditFrame = new BuddyEditFrame( buddyItem );
             MidletMain.screen.setActiveWindow( buddyEditFrame );
           }
         }
       }
     };
     buddyPopup.addSubItem( editPopupItem );
     buddyPopup.addSubItem( new PopupItem( Localization.getMessage( "REMOVE" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null ) {
             final Soft dialogSoft = new Soft( screen );
             dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {
               public void actionPerformed() {
                 /** Checking for buddy is temporary **/
                 if ( buddyItem.getTemp() ) {
                   /** Removing buddy locally **/
                   MidletMain.mainFrame.buddyList.removeBuddyFromGroups( buddyItem );
                   /** Checking for chat frame buddy items **/
                   MidletMain.chatFrame.updateBuddyItems();
                 } else {
                   /** Mechanism invocation **/
                   Mechanism.rosterRemoveRequest( buddyItem.getJid() );
                 }
                 /** Closing dialog **/
                 dialogSoft.rightSoft.actionPerformed();
               }
             };
             dialogSoft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {
               public void actionPerformed() {
                 MainFrame.this.closeDialog();
               }
             };
             Handler.showDialog( MainFrame.this, dialogSoft,
                     "REMOVING",
                     Localization.getMessage( "SURE_REMOVE_BUDDY" ).
                     concat( " " ).concat( buddyItem.getJid() ).concat( " " ).
                     concat( Localization.getMessage( "FROM_ROSTER" ) ) );
           }
         }
       }
     } );
     /** Buddy sub items **/
     subscrReqtPopup = new PopupItem( Localization.getMessage( "SUBSCRIPTION_REQUEST" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null ) {
             Mechanism.sendSubscriptionRequest( buddyItem.getJid() );
           }
         }
       }
     };
     subscrApprPopup = new PopupItem( Localization.getMessage( "SUBSCRIPTION_APPROVE" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null ) {
             Mechanism.sendSubscriptionApprove( buddyItem.getJid() );
           }
         }
       }
     };
     subscrRejtPopup = new PopupItem( Localization.getMessage( "SUBSCRIPTION_REJECT" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null ) {
             Mechanism.sendSubscriptionReject( buddyItem.getJid() );
           }
         }
       }
     };
     /** Room menu **/
     roomPopup = new PopupItem( Localization.getMessage( "ELEMENT" ) ) {
       public void actionPerformed() {
         updatePopup();
       }
     };
     enterRoomPopupItem = new PopupItem( Localization.getMessage( "ENTER" ) ) {
       public void actionPerformed() {
         /** Enter selected room **/
         LogUtil.outMessage( "Enter room action" );
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             /** Mechanism invocation **/
             Mechanism.enterRoomRequest( ( RoomItem ) buddyItem );
           }
         }
       }
     };
     leaveRoomPopupItem = new PopupItem( Localization.getMessage( "LEAVE" ) ) {
       public void actionPerformed() {
         /** Enter selected room **/
         LogUtil.outMessage( "Leave room action" );
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             /** Mechanism invocation **/
             Mechanism.leaveRoomRequest( ( RoomItem ) buddyItem );
           }
         }
       }
     };
     editBookmarkPopupItem = new PopupItem( Localization.getMessage( "EDIT" ) ) {
       public void actionPerformed() {
         /** Edit selected room **/
         LogUtil.outMessage( "Edit bookmark action" );
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             RoomEditFrame roomEditFrame = new RoomEditFrame( ( RoomItem ) buddyItem );
             MidletMain.screen.setActiveWindow( roomEditFrame );
           }
         }
       }
     };
     removeBookmarkPopupItem = new PopupItem( Localization.getMessage( "REMOVE" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Enter selected room **/
           LogUtil.outMessage( "Remove bookmark action" );
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             final Soft dialogSoft = new Soft( screen );
             dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {
               public void actionPerformed() {
                 /** Obtain bookmarks **/
                 final RoomItem roomItem = ( ( RoomItem ) buddyItem );
                 /** Checking for privilege to destroy room **/
                 if ( RoomUtil.checkPrivilege( roomItem.getRole(), roomItem.getAffiliation(), RoomUtil.DESTROY_ROOM ) ) {
                   final Soft dialogSoft = new Soft( screen );
                   dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {
                     public void actionPerformed() {
                       /** Destroy room item **/
                       Mechanism.roomDestroyRequest( roomItem );
                       /** Perform dialog close **/
                       MainFrame.this.closeDialog();
                     }
                   };
                   dialogSoft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {
                     public void actionPerformed() {
                       /** Perform to remove only bookmark **/
                       removeBookmark( roomItem );
                     }
                   };
                   Handler.showDialog( MainFrame.this, dialogSoft,
                           "DESTROYING",
                           Localization.getMessage( "MAYBE_DESTROY_ROOM" ).
                           concat( " " ).concat( ( ( RoomItem ) buddyItem ).getRoomTitle() ).concat( " " ).
                           concat( Localization.getMessage( "FROM_SERVER" ) ) );
                 } else {
                   removeBookmark( roomItem );
                 }
               }
 
               public void removeBookmark( RoomItem roomItem ) {
                 /** Checking for conference status **/
                 if ( buddyItem.getStatusIndex() != StatusUtil.offlineIndex ) {
                   /** Log out from active conference **/
                   Mechanism.leaveRoomRequest( roomItem );
                   /** Setting up room as inactive **/
                   roomItem.setRoomActive( false );
                 }
                 /** Closing all opened tabs of this room **/
                 if ( Handler.closeOpenedTabs( roomItem ) ) {
                   /** Setting room as inactive to remove it completely **/
                   roomItem.setRoomActive( false );
                   /** Removing bookmark and going to main frame on success **/
                   Mechanism.sendBookmarksOperation( Mechanism.OPERATION_REMOVE, roomItem,
                           false, true );
                 } else {
                   /** Showing main frame **/
                   Handler.showMainFrame();
                 }
                 /** Perform dialog close **/
                 dialogSoft.rightSoft.actionPerformed();
               }
             };
             dialogSoft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {
               public void actionPerformed() {
                 /** Perform dialog close **/
                 MainFrame.this.closeDialog();
               }
             };
             Handler.showDialog( MainFrame.this, dialogSoft,
                     "REMOVING",
                     Localization.getMessage( "SURE_REMOVE_ROOM" ).
                     concat( " \"" ).concat( buddyItem.getNickName() ).concat( "\" " ).
                     concat( Localization.getMessage( "FROM_BOOKMARKS" ) ) );
           }
         }
       }
     };
     roomSettingsPopupItem = new PopupItem( Localization.getMessage( "ROOM_SETTINGS" ) );
     roomConfigurationPopupItem = new Command( Localization.getMessage( "ROOM_CONFIG" ) ) {
       public void actionPerformed() {
         /** Edit selected room **/
         LogUtil.outMessage( "Room configuration action" );
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             /** Showing wait screen **/
             MidletMain.screen.setWaitScreenState( true );
             /** Mechanism invocation **/
             Mechanism.configureRoomRequest( ( RoomItem ) buddyItem, false );
           }
         }
       }
     };
     roomChangeTopicPopupItem = new PopupItem( Localization.getMessage( "ROOM_CHANGETOPIC" ) ) {
       public void actionPerformed() {
         /** Change nick in selected room **/
         LogUtil.outMessage( "Topic edit action" );
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             /** Show topic edit frame **/
             RoomTopicEditFrame roomTopicEditFrame = new RoomTopicEditFrame( ( RoomItem ) buddyItem );
             MidletMain.screen.setActiveWindow( roomTopicEditFrame );
           }
         }
       }
     };
     roomChangeNickPopupItem = new PopupItem( Localization.getMessage( "ROOM_CHANGENICK" ) ) {
       public void actionPerformed() {
         /** Change nick in selected room **/
         LogUtil.outMessage( "Nick change action" );
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
             /** Show nick change frame **/
             RoomNickChangeFrame roomNickChangeFrame = new RoomNickChangeFrame( ( RoomItem ) buddyItem );
             MidletMain.screen.setActiveWindow( roomNickChangeFrame );
           }
         }
       }
     };
     roomUsersPopupItem = new PopupItem( Localization.getMessage( "ROOM_USERS" ) );
     roomOutcastPopupItem = new PopupItem( Localization.getMessage( "ROOM_OUTCAST" ) ) {
       public void actionPerformed() {
         showRoomVisitorsEditFrame( TemplateCollection.VAL_OUTCAST );
       }
     };
     roomMembersPopupItem = new PopupItem( Localization.getMessage( "ROOM_MEMBERS" ) ) {
       public void actionPerformed() {
         showRoomVisitorsEditFrame( TemplateCollection.VAL_MEMBER );
       }
     };
     roomAdminsPopupItem = new PopupItem( Localization.getMessage( "ROOM_ADMINS" ) ) {
       public void actionPerformed() {
         showRoomVisitorsEditFrame( TemplateCollection.VAL_ADMIN );
       }
     };
     roomOwnersPopupItem = new PopupItem( Localization.getMessage( "ROOM_OWNERS" ) ) {
       public void actionPerformed() {
         showRoomVisitorsEditFrame( TemplateCollection.VAL_OWNER );
       }
     };
     roomDestroyPopupItem = new PopupItem( Localization.getMessage( "ROOM_DESTROY" ) ) {
       public void actionPerformed() {
         /** Checking for online **/
         if ( Handler.sureIsOnline() ) {
           /** Obtain buddy item selected **/
           final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
           /** Checking selected item type **/
           if ( buddyItem != null ) {
             final Soft dialogSoft = new Soft( screen );
             dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {
               public void actionPerformed() {
                 Mechanism.roomDestroyRequest( ( RoomItem ) buddyItem );
                 dialogSoft.rightSoft.actionPerformed();
               }
             };
             dialogSoft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {
               public void actionPerformed() {
                 MainFrame.this.closeDialog();
               }
             };
             Handler.showDialog( MainFrame.this, dialogSoft, "DESTROYING",
                     Localization.getMessage( "SURE_DESTROY_ROOM" ).
                     concat( " " ).concat( ( ( RoomItem ) buddyItem ).getRoomTitle() ).concat( " " ).
                     concat( Localization.getMessage( "FROM_SERVER" ) ) );
           }
         }
       }
     };
     /** Soft post-adding items **/
     soft.leftSoft.addSubItem( statusPopupItem );
     soft.leftSoft.addSubItem( filterPopupItem );
     soft.leftSoft.addSubItem( dialogsPopupItem );
     soft.leftSoft.addSubItem( contactsPopupItem );
     soft.leftSoft.addSubItem( muchatPopupItem );
     soft.leftSoft.addSubItem( settngPopupItem );
     soft.leftSoft.addSubItem( morePopupItem );
     soft.leftSoft.addSubItem( minimizePopupItem );
     soft.leftSoft.addSubItem( exitPopupItem );
     soft.rightSoft = emptyPopup;
     /** BuddyList initialization **/
     buddyList = new BuddyList();
   }
 
   /**
    * Initialize blank screen
    */
   private void initBlank() {
     /** Soft initialization **/
     soft = new Soft( MidletMain.screen );
     soft.leftSoft = new PopupItem( Localization.getMessage( "EXIT" ) ) {
       public void actionPerformed() {
         MidletMain.exitApp();
       }
     };
     soft.rightSoft = new PopupItem( "" );
     /** Pane initialization **/
     blank = new Pane( null, false );
     blank.addItem( new Label( Localization.getMessage( "NO_ACC_MSG" ) ) );
     Button accountAddBtn = new Button( Localization.getMessage( "ACCOUNT_ADD" ) ) {
       public void actionPerformed() {
         MidletMain.screen.setActiveWindow( new AccountEditFrame() );
       }
     };
     accountAddBtn.setFocused( true );
     blank.addItem( accountAddBtn );
   }
 
   /**
    * Checking for actual popup type
    */
   public void updatePopup() {
     /** Obtain buddy item selected **/
     BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
     /** Checking selected item type **/
     if ( buddyItem == null ) {
       /** Not buddy item, but group item **/
       GroupItem groupItem = buddyList.getSelectedGroupItem();
       /** Checking for group selected **/
       if ( groupItem != null ) {
         /** GroupItem selected **/
         LogUtil.outMessage( "Group selected: " + groupItem.getGroupName() );
         setGroupPopup();
       } else {
         LogUtil.outMessage( "Nothing selected" );
         setEmptyPopup();
       }
     } else {
       LogUtil.outMessage( "temp: " + buddyItem.getTemp() );
       LogUtil.outMessage( "subscription: " + buddyItem.getSubscription() );
       /** BudyItem selected **/
       if ( buddyItem.getInternalType() == BuddyItem.TYPE_SERVICE_ITEM ) {
         /** BuddyItem is service **/
         LogUtil.outMessage( "Service selected: " + buddyItem.getJid() );
         LogUtil.outMessage( "Protocol offset: " + buddyItem.getProtocolOffset() );
         LogUtil.outMessage( "connected: " + ( ( ServiceItem ) buddyItem ).isConnected() );
         setServicePopup( ( ServiceItem ) buddyItem );
       } else if ( buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
         /** BuddyItem is room **/
         LogUtil.outMessage( "Room selected: " + buddyItem.getJid() );
         setRoomPopup( ( RoomItem ) buddyItem );
       } else {
         /** Default buddy item selected **/
         LogUtil.outMessage( "Buddy selected: " + buddyItem.getJid() );
         setBuddyPopup( buddyItem );
       }
     }
   }
 
   private void setGroupPopup() {
     soft.rightSoft = groupPopup;
   }
 
   private void setBuddyPopup( final BuddyItem buddyItem ) {
     /** Removing subscription items **/
     buddyPopup.subPopup.items.removeElement( subscrReqtPopup );
     buddyPopup.subPopup.items.removeElement( subscrApprPopup );
     buddyPopup.subPopup.items.removeElement( subscrRejtPopup );
     /** Updating remove/add item and adding subscription items **/
     if ( buddyItem.getTemp() ) {
       /** Updating popup item title **/
       editPopupItem.setTitle( Localization.getMessage( "ADD" ) );
     } else {
       /** Checking for necessary items **/
       if ( buddyItem.getSubscription().equals( "none" ) ) {
         buddyPopup.subPopup.items.addElement( subscrReqtPopup );
       } else if ( buddyItem.getSubscription().equals( "from" ) ) {
         buddyPopup.subPopup.items.addElement( subscrReqtPopup );
         buddyPopup.subPopup.items.addElement( subscrRejtPopup );
       } else if ( buddyItem.getSubscription().equals( "to" ) ) {
         buddyPopup.subPopup.items.addElement( subscrApprPopup );
       } else if ( buddyItem.getSubscription().equals( "both" ) ) {
         buddyPopup.subPopup.items.addElement( subscrRejtPopup );
       }
       /** Updating popup item title **/
       editPopupItem.setTitle( Localization.getMessage( "EDIT" ) );
     }
     /** Updating "Dialog" menu **/
     updateDialogPopupItem( buddyItem );
     /** Setting up buddy popup as active popup **/
     soft.rightSoft = buddyPopup;
   }
 
   private void setServicePopup( ServiceItem serviceItem ) {
     /** Checking for service error **/
     if ( serviceItem.isBuddyInvalid() ) {
       /** Creating new popup to let user remove broken service **/
       PopupItem popupItem = serviceItem.getNewPopupItem( true );
       Handler.appendRegisterEvent( popupItem, serviceItem );
     } else {
       if ( serviceItem.isNoCachedPopup() ) {
         /** Locking screen **/
         screen.setWaitScreenState( true );
         /** Obtain service items **/
         Mechanism.inspectService( serviceItem );
       }
     }
     /** Showing popup **/
     soft.rightSoft = serviceItem.getCachedPopup();
   }
 
   private void setRoomPopup( RoomItem roomItem ) {
     LogUtil.outMessage( "Room status: " + roomItem.getStatusIndex() );
     /** Removing all childs **/
     roomPopup.removeAllChilds();
     /** Checking for room status **/
     if ( roomItem.getStatusIndex() == StatusUtil.offlineIndex ) {
       /** Appending offline-action childs **/
       roomPopup.addSubItem( enterRoomPopupItem );
       roomPopup.addSubItem( editBookmarkPopupItem );
       roomPopup.addSubItem( removeBookmarkPopupItem );
     } else {
       roomPopup.addSubItem( dialogPopupItem );
       roomPopup.addSubItem( editBookmarkPopupItem );
       roomPopup.addSubItem( removeBookmarkPopupItem );
       /** Room settings block **/
       {
         /** Removing all room settings childs **/
         roomSettingsPopupItem.removeAllChilds();
         /** Checking parity for room configuration **/
         if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                 roomItem.getAffiliation(),
                 RoomUtil.CHANGE_ROOM_CONFIGURATION ) ) {
           roomSettingsPopupItem.addSubItem( roomConfigurationPopupItem );
         }
         /** Checking parity for edit topic of room **/
         if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                 roomItem.getAffiliation(), RoomUtil.MODIFY_SUBJECT ) ) {
           roomSettingsPopupItem.addSubItem( roomChangeTopicPopupItem );
         }
         /** Checking parity for edit nick in room **/
         if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                 roomItem.getAffiliation(), RoomUtil.CHANGE_ROOM_NICKNAME ) ) {
           roomSettingsPopupItem.addSubItem( roomChangeNickPopupItem );
         }
         /** Room users block **/
         {
           /** Removing all users childs **/
           roomUsersPopupItem.removeAllChilds();
           /** Checking parity for members edit **/
           if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                   roomItem.getAffiliation(), RoomUtil.EDIT_MEMBER_LIST ) ) {
             roomUsersPopupItem.addSubItem( roomOutcastPopupItem );
             roomUsersPopupItem.addSubItem( roomMembersPopupItem );
           }
           /** Checking parity for admins edit **/
           if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                   roomItem.getAffiliation(), RoomUtil.EDIT_ADMIN_LIST ) ) {
             roomUsersPopupItem.addSubItem( roomAdminsPopupItem );
           }
           if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                   roomItem.getAffiliation(), RoomUtil.EDIT_OWNER_LIST ) ) {
             roomUsersPopupItem.addSubItem( roomOwnersPopupItem );
           }
           /** Checking for something added to users popup item **/
           if ( !roomUsersPopupItem.isEmpty() ) {
             /** Checking parity for users edit **/
             roomSettingsPopupItem.addSubItem( roomUsersPopupItem );
           }
         }
         /** Checking parity for destroying room **/
         if ( RoomUtil.checkPrivilege( roomItem.getRole(),
                 roomItem.getAffiliation(), RoomUtil.DESTROY_ROOM ) ) {
           roomSettingsPopupItem.addSubItem( roomDestroyPopupItem );
         }
         /** Checking for something added to room settings popup item **/
         if ( !roomSettingsPopupItem.isEmpty() ) {
           /** Adding settings popup item **/
           roomPopup.addSubItem( roomSettingsPopupItem );
         }
       }
       roomPopup.addSubItem( leaveRoomPopupItem );
       /** Updating "Dialog" menu **/
       updateDialogPopupItem( roomItem );
     }
     soft.rightSoft = roomPopup;
   }
 
   private void setEmptyPopup() {
     soft.rightSoft = emptyPopup;
   }
 
   /**
    * Force repainting
    */
   public static void repaintFrame() {
     /** Checking and repainting main frame  on the screen **/
     if ( MidletMain.screen.activeWindow.equals( MidletMain.mainFrame ) ) {
       MidletMain.screen.repaint();
     }
   }
 
   public void openDialog( BuddyItem buddyItem, Resource resource ) {
     openDialog( buddyItem, resource, false );
   }
 
   public void openDialog( BuddyItem buddyItem, Resource resource, boolean isCleanChat ) {
     /** Checking for null-type buddy item **/
     if ( buddyItem == null ) {
       /** Obtain selected buddy item from buddy list **/
       buddyItem = MidletMain.mainFrame.buddyList.getSelectedBuddyItem();
     }
     /** Checking for null-type **/
     if ( buddyItem != null ) {
       /** Checking for null-resource **/
       if ( resource == null ) {
         /** Checking for unread resource **/
         if ( ( resource = buddyItem.getUnreadResource() ) == null ) {
           /** There is no unread resource, checking for opened alone **/
           ChatTab tempTab = MidletMain.chatFrame.getStandAloneOnlineResourceTab( buddyItem.getJid() );
           /** Checking for no tab already opened, or resource is null 
            * or resource is offline and you are not offline **/
           if ( tempTab == null || ( resource = tempTab.resource ) == null
                   || ( tempTab.resource.statusIndex == StatusUtil.offlineIndex
                   && !AccountRoot.isOffline() ) ) {
             /** No unread resource **/
             /** Obtain default resource, this one cannot be null **/
             resource = buddyItem.getDefaultResource();
           }
         }
       }
       /** Obtain an opened chat tab **/
       ChatTab chatTab = MidletMain.chatFrame.getChatTab( buddyItem.getJid(), resource.resource, true );
       if ( chatTab == null ) {
         /** There is no opened chat tab **/
         chatTab = new ChatTab( buddyItem, resource );
         MidletMain.chatFrame.addChatTab( chatTab, true );
       } else if ( isCleanChat ) {
         chatTab.cleanChat();
       }
     }
     MidletMain.screen.setActiveWindow( MidletMain.chatFrame );
   }
 
   private void updateDialogPopupItem( final BuddyItem buddyItem ) {
     /** Updating "Dialog" menu **/
     if ( !dialogPopupItem.isEmpty() ) {
       /** Removing all items **/
       dialogPopupItem.subPopup.items.removeAllElements();
       /** Resetting Y offset **/
       dialogPopupItem.subPopup.yOffset = 0;
     }
     /** Checking for more than one resource **/
     if ( buddyItem.getResourcesCount() > 1 ) {
       /** Opening "All resource" resource **/
       buddyItem.getResource( "" );
       /** Appending all resource items **/
       for ( int c = 0; c < buddyItem.getResourcesCount(); c++ ) {
         /** Obtain resource item **/
         final Resource resource = buddyItem.resources[c];
         /** Creating popup item for specified resource **/
         PopupItem resourcePopupItem = new PopupItem(
                 resource.resource.length() > 0
                 ? resource.resource : Localization.getMessage( "ALL_RESOURCES" ),
                 com.tomclaw.mandarin.core.Settings.IMG_STATUS.hashCode(),
                 resource.statusIndex ) {
           public void actionPerformed() {
             /** Opening dialog **/
             openDialog( buddyItem, resource );
           }
         };
         dialogPopupItem.addSubItem( resourcePopupItem );
       }
     }
   }
 
   private void showRoomVisitorsEditFrame( String affiliation ) {
     /** Outcast list edit in selected room **/
     LogUtil.outMessage( "List edit in selected room for "
             .concat( affiliation ) );
     /** Checking for online **/
     if ( Handler.sureIsOnline() ) {
       /** Obtain buddy item selected **/
       final BuddyItem buddyItem = buddyList.getSelectedBuddyItem();
       /** Checking selected item type **/
       if ( buddyItem != null && buddyItem.getInternalType()
               == BuddyItem.TYPE_ROOM_ITEM ) {
         /** Mechanism invocation **/
         Mechanism.roomVisitorsListRequest( ( RoomItem ) buddyItem,
                 affiliation );
       }
     }
   }
 }

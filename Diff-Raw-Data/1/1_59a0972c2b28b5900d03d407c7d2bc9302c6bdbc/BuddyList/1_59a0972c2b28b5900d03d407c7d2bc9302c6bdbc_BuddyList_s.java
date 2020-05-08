 package com.tomclaw.mandarin.main;
 
 import com.tomclaw.mandarin.core.Settings;
 import com.tomclaw.mandarin.molecus.BuddyItem;
 import com.tomclaw.mandarin.molecus.GroupItem;
 import com.tomclaw.tcuilite.Group;
 import com.tomclaw.tcuilite.GroupChild;
 import com.tomclaw.tcuilite.GroupEvent;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 import java.util.Vector;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class BuddyList extends Group {
 
   public GroupItem generalGroupItem;
   public GroupItem tempGroupItem;
   public GroupItem servicesGroupItem;
   public GroupItem roomsGroupItem;
 
   public BuddyList() {
     super();
     /** Setup parameters **/
     updateSettings();
     /** Left images **/
     imageLeftFileHash = new int[]{
       com.tomclaw.mandarin.core.Settings.IMG_CHAT.hashCode(),
       com.tomclaw.mandarin.core.Settings.IMG_STATUS.hashCode()
     };
     /** Right images **/
     imageRightFileHash = new int[]{
       com.tomclaw.mandarin.core.Settings.IMG_SUBSCRIPTION.hashCode()
     };
     /** Action **/
     actionPerformedEvent = new GroupEvent() {
       public void actionPerformed( GroupChild buddyItem ) {
         LogUtil.outMessage( "BuddyList action" );
         MidletMain.mainFrame.getKeyEvent( "KEY_DIALOG" ).actionPerformed();
       }
     };
     /** Setup RMS file **/
     loadOfflineBuddyList();
   }
 
   /**
    * Updating settings from Settings class
    */
   public final void updateSettings() {
     /** Show groups **/
     isHideEmptyGroups = com.tomclaw.mandarin.core.Settings.hideEmptyGroups;
     isShowGroups = com.tomclaw.mandarin.core.Settings.showGroups;
     maxWeight = com.tomclaw.mandarin.core.Settings.showOffline ? 0 : -1;
   }
 
   private void loadOfflineBuddyList() {
     RecordStore t_recordStore = null;
     try {
       t_recordStore = RecordStore.openRecordStore( Settings.buddyListFile, true );
       byte[] abyte0;
       for ( int c = 1; c <= t_recordStore.getNumRecords(); c++ ) {
         abyte0 = t_recordStore.getRecord( c );
         items.addElement( RmsRenderer.getRmsGroupHeader( abyte0, this ) );
       }
     } catch ( Throwable ex ) {
       items.removeAllElements();
       LogUtil.outMessage( "Error on buddy list reading: " + ex.getMessage() );
     }
     if ( t_recordStore != null ) {
       try {
         t_recordStore.closeRecordStore();
       } catch ( Throwable ex ) {
         LogUtil.outMessage( "Error in closing: " + ex.getMessage() );
       }
     }
   }
 
   public void updateOfflineBuddylist() {
     try {
       RecordStore.deleteRecordStore( Settings.buddyListFile );
     } catch ( RecordStoreException ex ) {
       LogUtil.outMessage( "RMS error RecordStoreException: " + ex.getMessage() );
     }
     try {
       RecordStore t_recordStore = RecordStore.openRecordStore( Settings.buddyListFile, true );
       GroupItem groupHeader;
       byte[] abyte0;
       for ( int c = 0; c < items.size(); c++ ) {
         groupHeader = ( GroupItem ) items.elementAt( c );
         abyte0 = RmsRenderer.getRmsData( groupHeader );
         t_recordStore.addRecord( abyte0, 0, abyte0.length );
       }
       t_recordStore.closeRecordStore();
     } catch ( RecordStoreException ex ) {
       LogUtil.outMessage( "RMS error on buddy list saving: " + ex.getMessage() );
     }
   }
 
   /** 
    * Setting up buddy list items
    * @param roster 
    */
   public void setBuddyItems( Vector roster ) {
     GroupItem t_itemsGroupItem;
     GroupItem t_rosterGroupItem;
     /** Searching for the same groups in items and roster **/
     for ( int c = 0; c < items.size(); c++ ) {
       /** Obtain items group item **/
       t_itemsGroupItem = ( GroupItem ) items.elementAt( c );
       for ( int i = 0; i < roster.size(); i++ ) {
         /** Obtain roster group item **/
         t_rosterGroupItem = ( GroupItem ) roster.elementAt( i );
         /** Checking for group names are equals **/
         if ( t_itemsGroupItem.getGroupName().
                 equals( t_rosterGroupItem.getGroupName() ) ) {
           /** Updating collapsed status **/
           t_rosterGroupItem.isCollapsed = t_itemsGroupItem.isCollapsed;
         }
       }
     }
     /** Applying roster to items **/
     items = roster;
     /** Saving roster to the RMS storage **/
     updateOfflineBuddylist();
   }
 
   /**
    * Setting up offline status to roster
    */
   public void setRosterOffline() {
     GroupItem groupItem;
     BuddyItem buddyItem;
     int childCount;
     /** Cycling groups **/
     for ( int c = 0; c < items.size(); c++ ) {
       groupItem = ( GroupItem ) items.elementAt( c );
       /** Checking for group child count **/
       childCount = groupItem.getChildsCount();
       if ( childCount > 0 ) {
         /** Cycling group items **/
         for ( int i = 0; i < childCount; i++ ) {
           buddyItem = ( BuddyItem ) groupItem.elementAt( i );
           buddyItem.setResourcesOffline();
           buddyItem.updateUi();
         }
       }
     }
   }
 
   /**
    * Searching for specified BuddyItem by JID
    * @param jid
    * @return BuddyItem
    */
   public BuddyItem getBuddyItem( String jid ) {
     GroupItem groupItem;
     BuddyItem buddyItem;
     /** Cycling groups **/
     for ( int c = 0; c < items.size(); c++ ) {
       groupItem = ( GroupItem ) items.elementAt( c );
       buddyItem = getBuddyItem( jid, groupItem );
       if ( buddyItem != null ) {
         return buddyItem;
       }
     }
     return null;
   }
 
   public BuddyItem getBuddyItem( String jid, GroupItem groupItem ) {
     BuddyItem buddyItem;
     int childCount;
     /** Checking for group child count **/
     childCount = groupItem.getChildsCount();
     if ( childCount > 0 ) {
       /** Cycling group items **/
       for ( int i = 0; i < childCount; i++ ) {
         buddyItem = ( BuddyItem ) groupItem.elementAt( i );
         /** Comparing JID's **/
         if ( buddyItem.getJid().equals( jid ) ) {
           return buddyItem;
         }
       }
     }
     return null;
   }
 
   public String[] getServiceItems( String serviceHost ) {
     Vector serviceItems = new Vector();
     GroupItem groupItem;
     BuddyItem buddyItem;
     int childCount;
     /** Cycling groups **/
     for ( int c = 0; c < items.size(); c++ ) {
       groupItem = ( GroupItem ) items.elementAt( c );
       /** Checking for group child count **/
       childCount = groupItem.getChildsCount();
       if ( childCount > 0 ) {
         /** Cycling group items **/
         for ( int i = 0; i < childCount; i++ ) {
           buddyItem = ( BuddyItem ) groupItem.elementAt( i );
           /** Comparing JID's **/
           if ( buddyItem.getJid().endsWith( "@".concat( serviceHost ) ) ) {
             serviceItems.addElement( buddyItem.getJid() );
           }
         }
       }
     }
     /** Creating array **/
     String[] anArray = new String[ serviceItems.size() ];
     /** Copying array from Vector **/
     serviceItems.copyInto( anArray );
     return anArray;
   }
 
   public void removeBuddyFromGroups( BuddyItem buddyItem ) {
     GroupItem groupItem;
     /** Cycling groups **/
     for ( int c = 0; c < items.size(); c++ ) {
       groupItem = ( GroupItem ) items.elementAt( c );
       if ( groupItem.getChildsCount() > 0 ) {
         groupItem.removeElement( buddyItem );
       }
     }
   }
 
   /**
    * Returns selected buddy item or null if no item selected
    * @return BuddyItem
    */
   public BuddyItem getSelectedBuddyItem() {
     if ( selectedRealGroup >= 0 && selectedRealGroup < items.size() ) {
       if ( selectedRealIndex >= 0 && selectedRealIndex < ( ( GroupItem ) items.elementAt( selectedRealGroup ) ).getChildsCount() ) {
         return ( BuddyItem ) ( ( GroupItem ) items.elementAt( selectedRealGroup ) ).elementAt( selectedRealIndex );
       }
     }
     return null;
   }
 
   /**
    * Return selected group item or null if no group selected
    * @return GroupItem
    */
   public GroupItem getSelectedGroupItem() {
     if ( selectedRealGroup >= 0 && selectedRealGroup < items.size() ) {
       if ( selectedRealIndex == -1 ) {
         return ( GroupItem ) items.elementAt( selectedRealGroup );
       }
     }
     return null;
   }
 
   /**
    * Creates buddy item in temporary group
    * @param jid
    * @return BuddyItem
    */
   public BuddyItem createTempBuddyItem( String jid ) {
     BuddyItem buddyItem;
     /** Checking for buddy item is already exist **/
     buddyItem = getBuddyItem( jid );
     if ( buddyItem == null ) {
       /** Creating buddy item **/
       buddyItem = new BuddyItem( jid );
       buddyItem.setSubscription( "none" );
       buddyItem.updateUi();
     } else {
       /** Removing buddy from other groups **/
       removeBuddyFromGroups( buddyItem );
     }
     /** Adding buddy item to temporary group **/
     MidletMain.mainFrame.buddyList.tempGroupItem.addChild( buddyItem );
     return buddyItem;
   }
 
   /**
    * Makes specified buddy item temporary and removes from any groups but temp
    * @param jid
    * @return BuddyItem
    */
   public BuddyItem makeBuddyItemTemp( BuddyItem buddyItem ) {
     /** Creating buddy item **/
     buddyItem.setSubscription( "none" );
     buddyItem.setTemp( true );
     buddyItem.updateUi();
     /** Removing buddy from other groups **/
     removeBuddyFromGroups( buddyItem );
     /** Adding buddy item to temporary group **/
     MidletMain.mainFrame.buddyList.tempGroupItem.addChild( buddyItem );
     return buddyItem;
   }
 
   /**
    * Creates group item or returns existed
    * @return tempGroupItem
    */
   public GroupItem initTempGroupItem() {
     /** Checking for temp group item is null **/
     if ( tempGroupItem == null ) {
       /** Creating temp group item **/
       MidletMain.mainFrame.buddyList.tempGroupItem = new GroupItem( Localization.getMessage( "TEMPORARY" ) );
       MidletMain.mainFrame.buddyList.tempGroupItem.internalGroupId = GroupItem.GROUP_TEMP_ID;
       MidletMain.mainFrame.buddyList.tempGroupItem.isCollapsed = false;
     }
     return tempGroupItem;
   }
 
   /** 
    * Returns clear JID
    * @param fullJid
    * @return String
    */
   public static String getClearJid( String fullJid ) {
     if ( fullJid != null && fullJid.indexOf( "/" ) != -1 ) {
       return fullJid.substring( 0, fullJid.indexOf( "/" ) );
     }
     return fullJid;
   }
 
   /**
    * Returns JID's resource
    * @param fullJid
    * @return String
    */
   public static String getJidResource( String fullJid ) {
     if ( fullJid != null && fullJid.indexOf( "/" ) != -1 ) {
       return fullJid.substring( fullJid.indexOf( "/" ) + 1 );
     }
     return "";
   }
 
   /**
    * Returns JID's host
    * @param fullJid
    * @return String
    */
   public static String getJidHost( String fullJid ) {
     String tempJid = getClearJid( fullJid );
     if ( tempJid != null && tempJid.indexOf( "@" ) != -1 ) {
       tempJid = tempJid.substring( tempJid.indexOf( "@" ) + 1 );
     }
     return tempJid;
   }
 
   /**
    * Returns JID's username
    * @param fullJid
    * @return username
    */
   public static String getJidUsername( String fullJid ) {
     String tempJid = getClearJid( fullJid );
     if ( tempJid != null && tempJid.indexOf( "@" ) != -1 ) {
       tempJid = tempJid.substring( 0, tempJid.indexOf( "@" ) );
     } else {
       tempJid = null;
     }
     return tempJid;
   }
 }

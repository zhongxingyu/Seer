 package com.tomclaw.mandarin.main;
 
 import com.tomclaw.mandarin.molecus.*;
 import com.tomclaw.tcuilite.ChatItem;
 import com.tomclaw.tcuilite.Label;
 import com.tomclaw.tcuilite.TabItem;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 import com.tomclaw.utils.StringUtil;
 import java.util.Vector;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class ChatTab extends TabItem {
 
   public BuddyItem buddyItem;
   public Vector chatItems;
   public Resource resource;
   private Label tabLabelRoomNumber;
   private Label tabLabelRoomPrivacy;
 
   public ChatTab( BuddyItem buddyItem, Resource resource ) {
     super( buddyItem.getNickName(),
             com.tomclaw.mandarin.core.Settings.IMG_STATUS.hashCode(),
             resource.statusIndex );
     this.buddyItem = buddyItem;
     this.resource = resource;
   }
 
   /**
    * Adds chat item
    * @param chatItem 
    */
   public void addChatItem( ChatItem chatItem ) {
     if ( chatItems == null ) {
       chatItems = new Vector();
     }
     chatItems.addElement( chatItem );
   }
 
   /**
    * Update chat tab caption and icon
    */
   public void updateUi() {
    LogUtil.outMessage( "Updating chat tab UI" );
    /** Updating chat tab title **/
     this.title = buddyItem.getNickName();
     /** Checking for unread count **/
     if ( resource.unreadCount == 0 ) {
       /** Checking for tab is muc **/
       if ( isMucTab() ) {
         /** Checking for main room tab **/
         if ( StringUtil.isEmptyOrNull( resource.resource ) ) {
           /** Main room tab **/
           this.imageFileHash = MidletMain.mainFrame.buddyList.imageLeftFileHash[1];
           this.imageIndex = buddyItem.getProtocolOffset() + resource.statusIndex;
           /** Showing label with room number **/
           tabLabel = getTabLabelRoomNumber();
         } else {
           /** Private tab **/
           this.imageFileHash = MidletMain.mainFrame.buddyList.imageLeftFileHash[1];
           this.imageIndex = StatusUtil.xmppOffset + resource.statusIndex;
           this.title = resource.resource;
           /** Showing label about private messaging in room and it's number **/
           tabLabel = getTabLabelRoomPrivacy();
         }
       } else {
         /** This is non-muc tab **/
         this.imageFileHash = MidletMain.mainFrame.buddyList.imageLeftFileHash[1];
         this.imageIndex = buddyItem.getProtocolOffset() + resource.statusIndex;
       }
     } else {
       /** Tab with incoming message **/
       this.imageFileHash = MidletMain.mainFrame.buddyList.imageLeftFileHash[0];
       this.imageIndex = buddyItem.imageLeftIndex[0];
     }
     if ( !isMucTab() ) {
       /** Updating tab label **/
       if ( AccountRoot.getStatusIndex() == StatusUtil.offlineIndex ) {
         /** Account is offline, showing label **/
         tabLabel = ChatFrame.getTabLabelAccountOffline();
       } else if ( buddyItem.getStatusIndex() == StatusUtil.offlineIndex ) {
         /** Buddy is offline, showing label **/
         tabLabel = ChatFrame.getTabLabelBuddyOffline();
       } else {
         /** No label needed **/
         tabLabel = null;
       }
     }
   }
 
   public boolean isMucTab() {
     return ( buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM );
   }
 
   private Label getTabLabelRoomNumber() {
     /** Checking for tab label is null-type **/
     if ( tabLabelRoomNumber == null ) {
       /** Creating tab label instance **/
       tabLabelRoomNumber = ChatFrame.getTabLabel( getRoomName(
               Localization.getMessage( "ROOM_NUMBER" ) ),
               tabLabelRoomNumber );
     }
     return tabLabelRoomNumber;
   }
 
   private Label getTabLabelRoomPrivacy() {
     /** Checking for tab label is null-type **/
     if ( tabLabelRoomPrivacy == null ) {
       /** Creating tab label instance **/
       tabLabelRoomPrivacy = ChatFrame.getTabLabel( getRoomName(
               Localization.getMessage( "ROOM_PRIVACY" ) ),
               tabLabelRoomPrivacy );
     }
     return tabLabelRoomPrivacy;
   }
 
   /**
    * Returns room name by checking for numeric room name
    * @param title
    * @return String
    */
   private String getRoomName( String title ) {
     return title.concat( " " ).concat( ( ( RoomItem ) buddyItem ).getRoomTitle() );
   }
 
   public void cleanChat() {
     /** Checking for chat tab is not initialized yet **/
     if ( chatItems != null ) {
       /** Removing all chat elements **/
       chatItems.removeAllElements();
     }
   }
 }

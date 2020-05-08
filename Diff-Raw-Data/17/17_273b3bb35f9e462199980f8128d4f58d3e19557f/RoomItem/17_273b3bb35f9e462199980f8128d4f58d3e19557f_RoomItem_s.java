 package com.tomclaw.mandarin.molecus;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class RoomItem extends BuddyItem {
 
   /** Variables **/
   private boolean isMinimize;
   private boolean isAutoJoin;
   private String roomNickName;
   private String roomPassword;
   private int role;
   private int affiliation;
   private boolean isNonAnonimous;
   private boolean isRoomActive;
   private String topic;
   private boolean isAutoJoinInvoked;
 
   /**
    * Creates configured service item
    * @param jid
    * @param itemName
    * @param isTemp 
    */
   public RoomItem( String jid, String itemName, boolean isMinimize,
           boolean isAutoJoin ) {
     super( jid, itemName, null, false );
     /** Configuring additional params **/
     this.isMinimize = isMinimize;
     this.isAutoJoin = isAutoJoin;
     role = RoomUtil.ROLE_NONE;
     affiliation = RoomUtil.AFFL_NONE;
     isNonAnonimous = false;
     isRoomActive = false;
   }
 
   /**
    * Setting up user name for this room
    * @param roomNickName 
    */
   public void setRoomNick( String roomNickName ) {
     this.roomNickName = roomNickName;
   }
 
   /**
    * Setting up password to enter password-protected rooms
    * @param roomPassword 
    */
   public void setRoomPassword( String roomPassword ) {
     this.roomPassword = roomPassword;
   }
 
   /**
    * Setting up minimize on autojoin parameter
    * @param isMinimize 
    */
   public void setMinimize( boolean isMinimize ) {
     this.isMinimize = isMinimize;
   }
 
   /**
    * Setting up autojoin parameter
    * @param isAutoJoin 
    */
   public void setAutoJoin( boolean isAutoJoin ) {
     this.isAutoJoin = isAutoJoin;
   }
 
   /**
    * Returns minimize at startup parameter
    * @return isMinimize
    */
   public boolean getMinimize() {
     return isMinimize;
   }
 
   /**
    * Returns auto join parameter
    * @return isAutoJoin
    */
   public boolean getAutoJoin() {
     return isAutoJoin;
   }
 
   /**
    * Returns user name for this room
    * @return roomNickName
    */
   public String getRoomNick() {
     return roomNickName;
   }
 
   /**
    * Returns password to enter password-protected rooms
    * @param roomPassword 
    */
   public String getRoomPassword() {
     return roomPassword;
   }
 
   /**
    * Returns internal type of item
    * @return int
    */
   public int getInternalType() {
     return TYPE_ROOM_ITEM;
   }
 
   /**
    * Returns activity of the room - online or offline status
    * @return isRoomActive
    */
   public boolean getRoomActive() {
     return isRoomActive;
   }
 
   /**
    * Returns room status index
    * @return statusIndex
    */
   public int getStatusIndex() {
     /** Defining status index **/
     int statusIndex =
             isRoomActive ? StatusUtil.onlineIndex : StatusUtil.offlineIndex;
     /** Cycling resources **/
     for ( int c = 0; c < resources.length; c++ ) {
       /** Checking for blank resource **/
       if ( resources[c].resource.length() == 0 ) {
         resources[c].statusIndex = statusIndex;
         break;
       }
     }
     return statusIndex;
   }
 
   /**
    * Returns room title - numeric or text
    * @return roomTitle
    */
   public String getRoomTitle() {
     String roomTitle;
     try {
       /** Checking for room jid is number **/
       Integer.parseInt( getUserName() );
       /** Room's jid is number **/
       roomTitle = "№".concat( getUserName() );
     } catch ( Throwable ex ) {
       /** Room's jid is not number **/
       roomTitle = "\"".concat( getUserName() ).concat( "\"" );
     }
     return roomTitle;
   }
 
   /**
    * Clones all room parameters into specified room item
    * @param roomItem 
    */
   public void cloneInto( RoomItem roomItem ) {
     roomItem.setJid( getJid() );
     roomItem.setNickName( getNickName() );
     roomItem.setRoomNick( getRoomNick() );
     roomItem.setRoomPassword( getRoomPassword() );
     roomItem.isAutoJoin = isAutoJoin;
     roomItem.isMinimize = isMinimize;
   }
 
   /**
    * Setting up room active or not - online or offline
    * @param isRoomActive
    * @return isRoomActive
    */
   public boolean setRoomActive( boolean isRoomActive ) {
     this.isRoomActive = isRoomActive;
     /** Checking for unavailable status **/
     if ( !isRoomActive ) {
       /** Offline all resources **/
       setResourcesOffline();
     }
     return isRoomActive;
   }
 
   /**
    * Offlines all resources and updates room activity status
    */
   public void setResourcesOffline() {
     /** External event to offline everything **/
     super.setResourcesOffline();
     /** Checking for room is active **/
     if ( isRoomActive ) {
       isRoomActive = false;
     }
   }
 
   /**
    * Setting up user affiliation
    * @param affiliation 
    */
   public void setAffiliation( int affiliation ) {
     this.affiliation = affiliation;
   }
 
   /**
    * Setting up user role
    * @param role 
    */
   public void setRole( int role ) {
     this.role = role;
   }
 
   /**
    * Returns user affiliation
    * @return affiliation
    */
   public int getAffiliation() {
     return affiliation;
   }
 
   /**
    * Returns user role
    * @return role
    */
   public int getRole() {
     return role;
   }
 
   /**
    * Setting up anonimous room status
    * @param isNonAnonimous 
    */
   public void setNonAnonimous( boolean isNonAnonimous ) {
     this.isNonAnonimous = isNonAnonimous;
   }
 
   public void setRoomTopic( String subject ) {
     this.topic = subject;
   }
 
   public String getRoomTopic() {
     if ( topic == null ) {
       return "";
     }
     return topic;
   }
  
  public void setAutoJoinInvoked(boolean isAutoJoinInvoked) {
     this.isAutoJoinInvoked = isAutoJoinInvoked;
   }
  
   public boolean getAutoJoinInvoked() {
     return isAutoJoinInvoked;
   }
 }

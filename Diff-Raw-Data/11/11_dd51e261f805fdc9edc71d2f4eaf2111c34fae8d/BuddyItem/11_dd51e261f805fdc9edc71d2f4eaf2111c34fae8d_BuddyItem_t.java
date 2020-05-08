 package com.tomclaw.mandarin.molecus;
 
 import com.tomclaw.mandarin.core.Settings;
 import com.tomclaw.mandarin.main.BuddyList;
 import com.tomclaw.tcuilite.ChatItem;
 import com.tomclaw.tcuilite.GroupChild;
 import com.tomclaw.utils.LogUtil;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class BuddyItem extends GroupChild {
 
   /** Internal item type **/
   public static final int TYPE_BUDDY_ITEM = 0x00;
   public static final int TYPE_SERVICE_ITEM = 0x01;
   public static final int TYPE_ROOM_ITEM = 0x02;
   /** Icons index definition **/
   public static final int IMG_SUBSCRIPTION_FROM = 0;
   public static final int IMG_SUBSCRIPTION_TO = 1;
   public static final int IMG_SUBSCRIPTION_NONE = 2;
   public static final int IMG_SUBSCRIPTION_ERROR = 3;
   /** Buddy fields **/
   private String jid;
   private String nick;
   private String subscription;
   private int protocolOffset = StatusUtil.xmppOffset;
   public Resource[] resources = new Resource[ 0 ];
   private boolean isBuddyInvalid = false;
   private boolean isTemp;
  private int openedDialogsCount = 0;
 
   /**
    * Creates temporary buddy item
    * @param jid 
    */
   public BuddyItem( String jid ) {
     this( jid, null, null, true );
     BuddyItem.this.setSubscription( null );
   }
 
   /**
    * Creates configured buddy item
    * @param jid
    * @param itemName
    * @param subscription
    * @param isTemp 
    */
   public BuddyItem( String jid, String itemName, String subscription, boolean isTemp ) {
     super( jid );
     setJid( jid );
     setNickName( itemName );
     BuddyItem.this.setSubscription( subscription );
     this.isTemp = isTemp;
   }
 
   public boolean getTemp() {
     return isTemp;
   }
 
   public void setTemp( boolean isTemp ) {
     this.isTemp = isTemp;
   }
 
   /**
    * Setting up JID value
    * @param jid 
    */
   public final void setJid( String jid ) {
     this.jid = jid;
     /** Detecting protocol offset **/
     if ( BuddyList.getJidHost( jid ).toLowerCase().startsWith( "icq" ) ) {
       protocolOffset = StatusUtil.icqOffset;
     } else if ( BuddyList.getJidHost( jid ).toLowerCase().startsWith( "mrim" ) ) {
       protocolOffset = StatusUtil.mmpOffset;
     }
   }
 
   /**
    * Setting up nick name value
    * @param nickName 
    */
   public final void setNickName( String nickName ) {
     if ( nickName == null ) {
       nickName = getUserName();
     }
     nick = nickName;
   }
 
   /**
    * Setting up subscription value
    * @param subscription 
    */
   public void setSubscription( String subscription ) {
     /** Checking for remove subscription type **/
     if ( subscription == null || subscription.equals( "remove" ) ) {
       subscription = "none";
     }
     this.subscription = subscription;
   }
 
   /**
    * Setting up protocol offset
    * @param protocolOffset 
    */
   public void setProtocolOffset( int protocolOffset ) {
     this.protocolOffset = protocolOffset;
   }
 
   /**
    * Offlines all resources
    */
   public void setResourcesOffline() {
     /** Cycling all resources **/
     for ( int c = 0; c < resources.length; c++ ) {
       /** Settings up status index as offline **/
       resources[c].statusIndex = StatusUtil.offlineIndex;
     }
   }
 
   /**
    * Setting up invalid buddy flag
    * @param isBuddyInvalid 
    */
   public void setBuddyInvalid( boolean isBuddyInvalid ) {
     this.isBuddyInvalid = isBuddyInvalid;
   }
 
   /**
    * Returns JID value
    * @return jid
    */
   public String getJid() {
     return jid;
   }
 
   public String getFullJid( Resource resource ) {
     if ( resource.resource.length() > 0 ) {
       return jid.concat( "/" ).concat( resource.resource );
     } else {
       return jid;
     }
   }
 
   /**
    * Returns buddy user name
    * @return String username
    */
   public final String getUserName() {
     int index = jid.indexOf( '@' );
     if ( index != -1 ) {
       return jid.substring( 0, index );
     } else {
       return jid;
     }
   }
 
   /**
    * Returns nick name
    * @return nickName
    */
   public String getNickName() {
     return nick;
   }
 
   /**
    * Return subscription
    * @return subscription
    */
   public String getSubscription() {
     return subscription;
   }
 
   /**
    * Returns protocol offset
    * @return protocolOffset
    */
   public int getProtocolOffset() {
     return protocolOffset;
   }
 
   /**
    * Returns total unread count for all resources
    * @return unreadCount
    */
   public int getUnreadCount() {
     int unreadCount = 0;
     for ( int c = 0; c < resources.length; c++ ) {
       unreadCount += resources[c].unreadCount;
     }
     return unreadCount;
   }
 
   /**
    * Returns resources count;
    * @return resources.length
    */
   public int getResourcesCount() {
     return resources.length;
   }
 
   public Resource getResource( String resource ) {
     /** Cycling resources **/
     for ( int c = 0; c < resources.length; c++ ) {
       /** Checking for resource existance **/
       if ( resources[c].resource.equals( resource ) ) {
         return resources[c];
       }
     }
     /** Creating new resource **/
     Resource t_resource = new Resource( resource );
     /** Creating new resources array **/
     Resource[] enl_resources = new Resource[ resources.length + 1 ];
     /** Copying exist resources to the new array **/
     System.arraycopy( resources, 0, enl_resources, 0, resources.length );
     /** Appending new resource to the end of array **/
     enl_resources[enl_resources.length - 1] = t_resource;
     /** Applying new resources array **/
     resources = enl_resources;
     /** Updates status for blank resource **/
     getStatusIndex();
     return t_resource;
   }
 
   public Resource getUnreadResource() {
     /** Cycling resources **/
     for ( int c = 0; c < resources.length; c++ ) {
       /** Checking for resource unread count **/
       if ( resources[c].unreadCount > 0 ) {
         return resources[c];
       }
     }
     return null;
   }
 
   /**
    * Returns default resource
    * @return Resource
    */
   public Resource getDefaultResource() {
     if ( resources.length == 1 ) {
       return resources[0];
     } else {
       return getResource( "" );
     }
   }
 
   /**
    * Removes specified resource
    * @param resource
    * @return boolean
    */
   public boolean removeResource( String resource ) {
     /** Creating new reources array **/
     Resource[] shr_resources = new Resource[ resources.length - 1 ];
     int offset = 0;
     /** Cycling resources **/
     for ( int c = 0; c < resources.length; c++ ) {
       /** Checking for resource uncomparing **/
       if ( !resources[c].resource.equals( resource ) ) {
         /** Checking for resources overfull **/
         if ( offset < shr_resources.length ) {
           /** Applying resource to the new array **/
           shr_resources[offset++] = resources[c];
         } else {
           /** Resource is unexists **/
           return false;
         }
       }
     }
     resources = shr_resources;
     return true;
   }
 
   /**
    * Returns any not-offline resource's status
    * @return int
    */
   public int getStatusIndex() {
     int complexStatusIndex = StatusUtil.offlineIndex;
     int blankResourceIndex = -1;
     /** Cycling resources **/
     for ( int c = 0; c < resources.length; c++ ) {
       /** Checking for blank resource **/
       if ( resources[c].resource.length() == 0 ) {
         blankResourceIndex = c;
       } else {
         /** Checking for resource online **/
         if ( resources[c].statusIndex != StatusUtil.offlineIndex ) {
           complexStatusIndex = resources[c].statusIndex;
         }
       }
     }
     /** Applying blank resource status if there are some resources **/
     if ( blankResourceIndex != -1 ) {
       if ( resources.length > 1 ) {
         LogUtil.outMessage( "Applying blank resource status if there are some resources" );
         resources[blankResourceIndex].statusIndex = complexStatusIndex;
       } else {
         /** Only default resource exist and have have status **/
         complexStatusIndex = resources[blankResourceIndex].statusIndex;
       }
     }
     return complexStatusIndex;
   }
 
   /**
    * Returns invalid buddy flag
    * @return boolean
    */
   public boolean isBuddyInvalid() {
     return isBuddyInvalid;
   }
 
   /**
    * Returns true if this is service
    * @return boolean
    */
   public boolean isService() {
     return ( jid.indexOf( '@' ) == -1 );
   }
 
   /**
    * Returns true if service is supported
    * @return boolean
    */
   public boolean isServiceSupported() {
     return ( Settings.isHideUnsupportedServices
             ? ( isService()
             && ( jid.startsWith( "icq" ) || jid.startsWith( "mrim" ) ) )
             : true );
   }
 
   /**
    * Method to update displayable information
    * Bold font, icons
    */
   public void updateUi() {
     /** Updating nick name **/
     title = nick;
     /** Checking for buddy type **/
     if ( getInternalType() == TYPE_ROOM_ITEM ) {
       /** Obtain room visitors count **/
       int roomVisitorsCount = ( ( RoomItem ) this ).getRoomVisitors();
       /** Checking for visitors exist **/
       if ( roomVisitorsCount > 0 ) {
         /** Modifying title **/
         title += " (" + roomVisitorsCount + " чел.)";
       }
     }
     /** Left icons **/
     weight = 0;
     /** Checking buddy status to raise online **/
     if ( getStatusIndex() != StatusUtil.offlineIndex && Settings.isSortOnline ) {
       weight = -2;
     }
     /** Checking for opened dialog **/
    if ( getDialogOpened() ) {
       /** Updating weight and bold status **/
       weight = -3;
       isBold = true;
     } else {
       /** Updating bold status **/
       isBold = false;
     }
     int chatImage = -1;
     /** Checking for unread messages **/
     if ( getUnreadCount() > 0 ) {
       chatImage = ChatItem.TYPE_PLAIN_MSG;
       /** Checking setting to raise unread **/
       if ( Settings.isRaiseUnread ) {
         weight = -4;
       }
     }
     /** Applying left images **/
     imageLeftIndex = new int[]{ chatImage, protocolOffset + getStatusIndex() };
     /** Right icons **/
     int subscriptionImage = -1;
     /** Checking for subscription is not null-type and not service 
      * or room item **/
     if ( subscription != null && !isService()
             && getInternalType() != BuddyItem.TYPE_ROOM_ITEM ) {
       if ( subscription.equals( "from" ) ) {
         subscriptionImage = IMG_SUBSCRIPTION_FROM;
       } else if ( subscription.equals( "to" ) ) {
         subscriptionImage = IMG_SUBSCRIPTION_TO;
       } else if ( subscription.equals( "none" ) ) {
         subscriptionImage = IMG_SUBSCRIPTION_NONE;
       }
     }
     /** Checking for item is invalid **/
     if ( isBuddyInvalid ) {
       subscriptionImage = IMG_SUBSCRIPTION_ERROR;
     }
     /** Applying right images **/
     imageRightIndex = new int[]{ subscriptionImage };
   }
 
   public int getInternalType() {
     return BuddyItem.TYPE_BUDDY_ITEM;
   }
 
   /**
    * Setting up that dialog with this buddy is opened or closed
    * @param isDialogOpened 
    */
   public void setDialogOpened( boolean isDialogOpened ) {
    openedDialogsCount += isDialogOpened ? 1 : -1;
   }
 
   /**
    * Returns the dialog opened flag
    * @return isDialogOpened (boolean)
    */
   public boolean getDialogOpened() {
    return ( openedDialogsCount > 0 );
   }
 }

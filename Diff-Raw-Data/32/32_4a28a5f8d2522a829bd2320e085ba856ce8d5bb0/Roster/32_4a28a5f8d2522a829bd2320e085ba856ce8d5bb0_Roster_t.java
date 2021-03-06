 /*
  * Roster.java
  *
  * Created on 6.01.2005, 19:16
  * Copyright (c) 2005-2007, Eugene Stahov (evgs), http://bombus-im.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * You can also redistribute and/or modify this program under the
  * terms of the Psi License, specified in the accompanied COPYING
  * file, as published by the Psi Project; either dated January 1st,
  * 2005, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  */
 
 package Client;
 
 //#ifdef AUTOTASK
 //# import AutoTasks.AutoTask;
 //#endif
 
 //#ifndef WMUC
 import Conference.BookmarkQuery;
 import Conference.Bookmarks;
 import Conference.ConferenceGroup;
 import Conference.MucContact;
 import Conference.affiliation.ConferenceQuickPrivelegeModify;
 import Conference.ConferenceForm;
 //#endif
 
 //#ifdef ARCHIVE
 //# import archive.ArchiveList;
 //#endif
 import images.RosterIcons;
 import images.SmilesIcons;
 import Messages.MessageParser;
 import locale.SR;
 import login.LoginListener;
 import login.SASLAuth;
 import midlet.BombusMod;
 import ui.MainBar;
 import util.strconv;
 import vcard.VCard;
 import vcard.vCardForm;
 import com.alsutton.jabber.*;
 import com.alsutton.jabber.datablocks.*;
 import java.util.*;
 import javax.microedition.lcdui.*;
 import ui.*;
 import com.siemens.mp.game.Light;
 
 //#if MOOD
 //# import UserMood.MoodSelect;
 //# import UserMood.MoodList;
 //# import UserMood.Mood;
 //#endif
 
 //#if FILE_TRANSFER
 //# import io.file.transfer.TransferDispatcher;
 //#endif
 
 public class Roster
         extends VirtualList
         implements
         JabberListener,
         CommandListener,
         Runnable,
         LoginListener,
         YesNoAlert.YesNoListener
 {
     
     private Command cmdActions=new Command(SR.MS_ITEM_ACTIONS, Command.SCREEN, 1);
     private Command cmdStatus=new Command(SR.MS_STATUS_MENU, Command.SCREEN, 2);
     private Command cmdActiveContacts;//=new Command(SR.MS_ACTIVE_CONTACTS, Command.SCREEN, 3);
 //#ifdef MOOD
 //#     //private Command cmdUserMood=new Command(SR.MS_USER_MOOD, Command.SCREEN, 7);
 //#endif
     private Command cmdAlert=new Command(SR.MS_ALERT_PROFILE_CMD, Command.SCREEN, 8);
 //#ifndef WMUC
     private Command cmdConference=new Command(SR.MS_CONFERENCE, Command.SCREEN, 10);
 //#endif
 //#ifdef ARCHIVE
 //#     private Command cmdArchive=new Command(SR.MS_ARCHIVE, Command.SCREEN, 10);
 //#endif
     private Command cmdAdd=new Command(SR.MS_ADD_CONTACT, Command.SCREEN, 12);
     private Command cmdTools=new Command(SR.MS_TOOLS, Command.SCREEN, 14);    
     private Command cmdAccount=new Command(SR.MS_ACCOUNT_, Command.SCREEN, 15);
     private Command cmdCleanAllMessages=new Command(SR.MS_CLEAN_ALL_MESSAGES, Command.SCREEN, 50);
     private Command cmdInfo=new Command(SR.MS_ABOUT, Command.SCREEN, 80);
     private Command cmdMinimize=new Command(SR.MS_APP_MINIMIZE, Command.SCREEN, 90);
     private Command cmdQuit=new Command(SR.MS_APP_QUIT, Command.SCREEN, 99);
     
     private Config cf=Config.getInstance();
     private StaticData sd=StaticData.getInstance();
     
     public Contact activeContact = null;
     
     private Jid myJid;
 
     public JabberStream theStream;
         
     int messageCount;
     int highliteMessageCount;
     
     private Object messageIcon;
     public Object transferIcon;
    
     boolean reconnect=false;
     boolean querysign=false;
     
     //public int myStatus=cf.loginstatus;
     //private String myMessage;
 
     public Vector hContacts;
     
     private Vector vContacts;
     
     private Vector paintVContacts;
     
     public Groups groups;
     
     public Vector bookmarks;
     
     public MessageEdit me=null;
     
     public boolean useUserMood=false;
     
     public StatusList sl=StatusList.getInstance();
     
     public ExtendedStatus myStatus=sl.getStatus(cf.loginstatus);
     //public ExtendedStatus lastOnlineStatus=sl.getStatus(cf.loginstatus);
     public ExtendedStatus oldStatus=sl.getStatus(0);
     
 //#ifdef MOOD
 //#     public Vector serverFeatures;
 //#endif
     
 //#ifdef AUTOSTATUS
 //#     private AutoStatusTask autostatus;
 //#     public static boolean autoAway=false;
 //#     public static boolean autoXa=false;
 //#endif
 
 
     private String token;
     
     private long lastMessageTime=Time.utcTimeMillis();
 	
     private final static int maxReconnect=10;
     public int reconnectCount;
     
     public static String startTime=Time.dispLocalTime();
 	
     private static long notifyReadyTime=System.currentTimeMillis();
     private static int blockNotifyEvent=-111;
     
     private static boolean bl;
     
     private int blState=Integer.MAX_VALUE;
 
 //#ifdef SE_LIGHT
 //#     private SELightTask selight=SELightTask.getInstance();
 //#endif
     
 //#ifdef AUTOTASK
 //#     private AutoTask at=StaticData.getInstance().autoTask;
 //#endif
     
     private final static int SOUND_FOR_ME=500;
     private final static int SOUND_FOR_CONFERENCE=800;
     private final static int SOUND_MESSAGE=1000;
     private final static int SOUND_START_UP=777;
     private final static int SOUND_FOR_VIP=100;
     private final static int SOUND_COMPOSING=888;
     private final static int SOUND_OUTGOING=999;
     
     private final static int ACTION_DELETE=4;
     private final static int ACTION_QUIT=99;
 
     private int yesnoAction=0;
             
     
     /**
      * Creates a new instance of Roster
      * Sets up the stream to the server and adds this class as a listener
      */
     public Roster(Display display /*, boolean selAccount*/) {
         super();
         this.display=display;
         
         setLight(cf.lightState);
 
         MainBar mainbar=new MainBar(4, null, null);
         setMainBarItem(mainbar);
         mainbar.addRAlign();
         mainbar.addElement(null);
         mainbar.addElement(null);
         mainbar.addElement(null); //ft
 
         hContacts=new Vector();
         groups=new Groups();
         
         vContacts=new Vector(); // just for displaying
         
         getKeys();
         
 	updateMainBar();
 
         addMenuCommands();
         SplashScreen.getInstance().setExit(display, this);
 //#ifdef AUTOSTATUS
 //#         if (cf.autoAwayType!=Config.AWAY_OFF)
 //#             autostatus=new AutoStatusTask();
 //#         
 //#         if (myStatus.getImageIndex()<2)
 //#             messageActivity();
 //#endif
     }
     
     public void setLight(boolean state) {
         if (cf.allowLightControl) {
 		try {
 			if (state) {
 				Light.setLightOn();
 			} else {
 				Light.setLightOff();  
 			}
 		} catch( Exception e ) { }
          }
 //#ifdef SE_LIGHT
 //#         else if (cf.phoneManufacturer==Config.SONYE || cf.phoneManufacturer==Config.NOKIA) {
 //#             selight.setLight(state);
 //#          }
 //#endif
     }
     
     public void addMenuCommands(){
 //#ifdef NEW_MENU
 //#         if (!cf.newMenu) {
 //#endif
                 int activeType=Command.SCREEN;
                 if (cf.phoneManufacturer==Config.NOKIA) activeType=Command.BACK;
                 if (cf.phoneManufacturer==Config.INTENT) activeType=Command.BACK;
                 if (cf.phoneManufacturer==Config.J2ME) activeType=Command.BACK;
 
                 cmdActiveContacts=new Command(SR.MS_ACTIVE_CONTACTS, activeType, 3);
 
                 addCommand(cmdStatus);
                 addCommand(cmdActions);
                 addCommand(cmdActiveContacts);
 
                 addCommand(cmdAlert);
                 addCommand(cmdAdd);
 //#ifndef WMUC
                 addCommand(cmdConference);
 //#endif
                 addCommand(cmdTools);
 //#ifdef ARCHIVE
 //#                 addCommand(cmdArchive);
 //#endif
                 addCommand(cmdInfo);
                 addCommand(cmdAccount);
                 
                 addCommand(cmdCleanAllMessages);
 
                 addCommand(cmdQuit);
 
                 addOptionCommands();
                 setCommandListener(this);
 //#ifdef NEW_MENU
 //#         }
 //#endif
     }
     
     void addOptionCommands(){
         if (cf.allowMinimize) 
             addCommand(cmdMinimize);
     }
     public void setProgress(String pgs,int percent){
         SplashScreen.getInstance().setProgress(pgs, percent);
         setRosterMainBar(pgs);
         redraw();
     }
     public void setProgress(int percent){
         SplashScreen.getInstance().setProgress(percent);
         //redraw();
     }
     
     private void setRosterMainBar(String s){
         getMainBarItem().setElementAt(s, 3);
     }
     
     private int rscaler;
     private int rpercent;
     
     public void rosterItemNotify(){
         rscaler++;
         if (rscaler<4) return;
         rscaler=0;
         if (rpercent<100) rpercent++;
         //if (rpercent==100) rpercent=60;
         SplashScreen.getInstance().setProgress(rpercent);
     }
     
     // establishing connection process
     public void run(){
         //Iq.setXmlLang(SR.MS_XMLLANG);
 
         setQuerySign(true);
         setProgress(25);
 	if (!reconnect) {
 	    resetRoster();
 	}
         setProgress(26);
 
         try {
             Account a=sd.account;
 //#if SASL_XGOOGLETOKEN
             if (a.useGoogleToken()) {
                 setProgress(SR.MS_TOKEN, 30);
                 token=new SASLAuth(a, null, this, null).responseXGoogleToken();
                 if (token==null) 
                     throw new SecurityException("Can't get Google token");
             }
 //#endif
             setProgress(SR.MS_CONNECT_TO+a.getServer(), 30);
             SR.loaded();
             theStream= a.openJabberStream();
             setProgress(SR.MS_OPENING_STREAM, 40);
             theStream.setJabberListener( this );
             theStream.addBlockListener(new EntityCaps());
             theStream.initiateStream();
         } catch( Exception e ) {
             setProgress(SR.MS_FAILED, 100);
             reconnect=false;
             myStatus=sl.getStatus(Presence.PRESENCE_OFFLINE);
             //e.printStackTrace();
             setQuerySign(false);
             redraw();
             askReconnect(e);
         }
         //l.setCallback(this);
     }
 
     public void resetRoster() {
 	synchronized (hContacts) {
 	    hContacts=new Vector();
 	    groups=new Groups();
 	    vContacts=new Vector(); // just for displaying
 	    bookmarks=null;
 	}
 	setMyJid(new Jid(sd.account.getJid()));
 	updateContact(sd.account.getNick(), myJid.getBareJid(), SR.MS_SELF_CONTACT, "self", false);
 //#ifndef WSYSTEMGC
 	System.gc();
 //#endif
     }
     
     public void errorLog(String s){
         if (s==null) return;
         
         Msg m=new Msg(Msg.MESSAGE_TYPE_OUT, "local", "Error", s);
         messageStore(selfContact(), m);
     }
     
     public void beginPaint() {
         paintVContacts=vContacts;
     }
     
     public VirtualElement getItemRef(int Index){
         return (VirtualElement) paintVContacts.elementAt(Index);
     }
     
     public int getItemCount(){
         return paintVContacts.size();
     };
     
     public void setEventIcon(Object icon){
         transferIcon=icon;
         getMainBarItem().setElementAt(icon, 7);
         redraw();
     }
     
     public Object getEventIcon() {
         if (transferIcon!=null) return transferIcon;
         return messageIcon;
     }
   
     private void updateMainBar(){
         int s=querysign?RosterIcons.ICON_PROGRESS_INDEX:myStatus.getImageIndex();
         int profile=cf.profile;//StaticData.getInstance().config.profile;
         Object en=(profile>1)? new Integer(profile+RosterIcons.ICON_PROFILE_INDEX):null;
         MainBar mainbar=(MainBar) getMainBarItem();
         mainbar.setElementAt(new Integer(s), 2);
         mainbar.setElementAt(en, 5);
         if (messageCount==0) {
             messageIcon=null;
             mainbar.setElementAt(null,1);
         } else {
             messageIcon=new Integer(RosterIcons.ICON_MESSAGE_INDEX);
             mainbar.setElementAt(getHeaderString(),1);
         }
         mainbar.setElementAt(messageIcon, 0);
         if (cf.phoneManufacturer==Config.WINDOWS) {
             if (messageCount==0) {
                 setTitle("Bombus CE");
             } else {
                 setTitle("Bombus "+getHeaderString());
             }
         }
     }
     
     public String getHeaderString() {
         return ((highliteMessageCount==0)?" ":" "+highliteMessageCount+"/")+messageCount+" ";
     }
     
     boolean countNewMsgs() {
         int m=0;
         int h=0;
         synchronized (hContacts) {
             for (Enumeration e=hContacts.elements();e.hasMoreElements();){
                 Contact c=(Contact)e.nextElement();
                 m+=c.getNewMsgsCount();
                 h+=c.getNewHighliteMsgsCount();
             }
         }
         highliteMessageCount=h;
         messageCount=m;
 
         updateMainBar();
         return (m>0);
     }
     
     public void cleanupSearch(){
         int index=0;
         synchronized (hContacts) {
             while (index<hContacts.size()) {
                 if ( ((Contact) hContacts.elementAt(index)).getGroupType()==Groups.TYPE_SEARCH_RESULT )
                     hContacts.removeElementAt(index);
                 else index++;
             }
         }
         reEnumRoster();
     }
     
     public void cleanupAllHistories(){
         int index=0;
         synchronized (hContacts) {
             while (index<hContacts.size()) {
                 Contact c=(Contact) hContacts.elementAt(index);
                 c.purge();
                 index++;
             }
         }
         reEnumRoster();
         redraw();
     }
     
     public void cleanupGroup(){
         Group g=(Group)getFocusedObject();
         if (g==null) return;
         if (!g.collapsed) return;
 //#ifndef WMUC
         if (g instanceof ConferenceGroup) {
             ConferenceGroup cg= (ConferenceGroup) g;
             if (cg.getSelfContact().status>=Presence.PRESENCE_OFFLINE 
                 && cg.getConference().getStatus()==Presence.PRESENCE_ONLINE)
                 return;
         }
         //int gi=g.index;
 //#endif
         int index=0;
 
         int onlineContacts=0;
         
         synchronized (hContacts) {
             while (index<hContacts.size()) {
                 Contact contact=(Contact)hContacts.elementAt(index);
                 if (contact.inGroup(g)) {
                     if ( contact.origin>Contact.ORIGIN_ROSTERRES
                          && contact.status>=Presence.PRESENCE_OFFLINE
                          && contact.getNewMsgsCount()==0 )
                         hContacts.removeElementAt(index);
                     else { 
                         index++;
                         onlineContacts++;
                     } 
                 }
                 else index++; 
             }
             if (onlineContacts==0) {
                 if (g.type==Groups.TYPE_MUC) groups.removeGroup(g);
             }
         }
     }
     
     ReEnumerator reEnumerator=null;
     
     public void reEnumRoster(){
         if (reEnumerator==null) reEnumerator=new ReEnumerator();
         reEnumerator.queueEnum();
     }
     
     
     public Vector getHContacts() {return hContacts;}
     
     public void updateContact(String nick, String jid, String grpName, String subscr, boolean ask) {
         // called only on roster read
         int status=Presence.PRESENCE_OFFLINE;
         if (subscr.equals("none")) status=Presence.PRESENCE_UNKNOWN;
         if (ask) status=Presence.PRESENCE_ASK;
         if (subscr.equals("remove")) status=-1;
         
         Jid J=new Jid(jid);
         Contact c=findContact(J,false); // search by bare jid
         if (c==null) {
             c=new Contact(nick, jid, Presence.PRESENCE_OFFLINE, null);
             addContact(c);
         }
         
         boolean firstInstance=true; //FS#712 workaround
         
         for (Enumeration e=hContacts.elements();e.hasMoreElements();) {
             c=(Contact)e.nextElement();
             if (c.jid.equals(J,false)) {
                 Group group= (c.jid.isTransport())? 
                     groups.getGroup(Groups.TYPE_TRANSP) :
                     groups.getGroup(grpName);
                 if (group==null) {
                     group=groups.addGroup(grpName, Groups.TYPE_COMMON);
                 }
                 c.nick=nick;
                 c.setGroup(group);
                 c.subscr=subscr;
                 c.offline_type=status;
                 c.ask_subscribe=ask;
                 
                 if (c.origin==Contact.ORIGIN_PRESENCE) {
                     if (firstInstance) c.origin=Contact.ORIGIN_ROSTERRES;
                     else c.origin=Contact.ORIGIN_CLONE;
                 }
                 firstInstance=false;
                 
                 if (querysign==true)
                 {
                     if (cf.collapsedGroups) {
                         Group g=c.getGroup();
                         g.collapsed=true; 
                     }
                 }
                 
                 c.setSortKey((nick==null)? jid:nick);
             }
         }
         if (status<0) removeTrash();
     }
     
     private final void removeTrash(){
         int index=0;
         synchronized (hContacts) {
             while (index<hContacts.size()) {
                 Contact c=(Contact)hContacts.elementAt(index);
                 if (c.offline_type<0) {
                     hContacts.removeElementAt(index);
                 } else index++;
             }
             countNewMsgs();
         }
     }
 //#ifndef WMUC
     public MucContact findMucContact(Jid jid) {
         Contact contact=findContact(jid, true);
         try {
             return (MucContact) contact;
         } catch (Exception e) {
             // drop buggy bookmark in roster
             hContacts.removeElement(contact);
             return null;
         }
     }
     
     public final ConferenceGroup initMuc(String from, String joinPassword){
 //#ifdef AUTOSTATUS
 //#         if (autoAway) {
 //#             sendPresence(oldStatus.getImageIndex(), oldStatus.getMessage());
 //#             autoAway=false;
 //#             autoXa=false;
 //#             myStatus=oldStatus;
 //#             
 //#             messageActivity();
 //#         }
 //#endif
         // muc message
         int ri=from.indexOf('@');
         int rp=from.indexOf('/');
         String room=from.substring(0,ri);
         String roomJid=from.substring(0,rp).toLowerCase();
 
         ConferenceGroup grp=(ConferenceGroup)groups.getGroup(roomJid);
 
         // creating room
         if (grp==null) // we hasn't joined this room yet
             groups.addGroup(grp=new ConferenceGroup(roomJid, room) );
         grp.password=joinPassword;
         
         MucContact c=findMucContact( new Jid(roomJid) );
         
         if (c==null) {
             c=new MucContact(room, roomJid);
             addContact(c);
         }
 		        
         // change nick if already in room
         if (c.getStatus()==Presence.PRESENCE_ONLINE) return grp;
 
         c.setStatus(Presence.PRESENCE_ONLINE);
         
         c.transport=RosterIcons.ICON_GROUPCHAT_INDEX; //FIXME: убрать хардкод
         c.bareJid=from;
         c.origin=Contact.ORIGIN_GROUPCHAT;
         c.commonPresence=true;
 
         grp.conferenceJoinTime=Time.utcTimeMillis();
         grp.setConference(c);
         c.setGroup(grp);
         
         String nick=from.substring(rp+1);
 
         // old self-contact
         c=grp.getSelfContact();
         
         // check for existing entry - it may be our old self-contact
         // or another contact whose nick we pretend
         MucContact foundInRoom = findMucContact( new Jid(from) );
         if (foundInRoom!=null) {
             c=foundInRoom;            //choose found contact instead of old self-contact
         }
  
         // if exists (and online - rudimentary check due to line 500)
         // rename contact
         if (c!=null) if (c.status>=Presence.PRESENCE_OFFLINE) {
             c.nick=nick;
             c.jid.setJid(from);
             c.bareJid=from;
         }
         
         // create self-contact if no any candidates found
         if (c==null) {
             c=new MucContact(nick, from);
             addContact(c);
         }
 
         grp.setSelfContact(c);
         c.setGroup(grp);
         c.origin=Contact.ORIGIN_GC_MYSELF;
                
         sort(hContacts);
         return grp;
     }
     
     public final MucContact mucContact(String from){
         // muc message
         int ri=from.indexOf('@');
         int rp=from.indexOf('/');
         String room=from.substring(0,ri);
         String roomJid=from.substring(0,rp).toLowerCase();
 
         ConferenceGroup grp=(ConferenceGroup)groups.getGroup(roomJid);
 
         if (grp==null) return null; // we are not joined this room
         
         MucContact c=findMucContact( new Jid(from) );
         
         if (c==null) {
             c=new MucContact(from.substring(rp+1), from);
             addContact(c);
             c.origin=Contact.ORIGIN_GC_MEMBER;
         }
         
         c.setGroup(grp);
         sort(hContacts);
         return c;
     }
 //#endif
     public final Contact getContact(final String jid, boolean createInNIL) {
         Jid J=new Jid(jid);
 
         Contact c=findContact(J, true); 
         if (c!=null) 
             return c;
 
         c=findContact(J, false);
         if (c==null) {
             if (!createInNIL) return null;
             c=new Contact(null, jid, Presence.PRESENCE_OFFLINE, "none" ); /*"not-in-list"*/
 	    c.bareJid=J.getBareJid();
             c.origin=Contact.ORIGIN_PRESENCE;
             c.setGroup(groups.getGroup(Groups.TYPE_NOT_IN_LIST));
             addContact(c);
         } else {
             if (c.origin==Contact.ORIGIN_ROSTER) {
                 c.origin=Contact.ORIGIN_ROSTERRES;
                 c.setStatus(Presence.PRESENCE_OFFLINE);
                 c.jid=J;
                 //System.out.println("add resource");
             } else {
                 c=c.clone(J, Presence.PRESENCE_OFFLINE);
                 addContact(c);
                 //System.out.println("cloned");
             }
         }
         sort(hContacts);
         return c;
     }
     
     public void addContact(Contact c) {
         synchronized (hContacts) { hContacts.addElement(c); }
     }
     
 
     public final Contact findContact(final Jid j, final boolean compareResources) {
         synchronized (hContacts) {
             for (Enumeration e=hContacts.elements();e.hasMoreElements();){
                 Contact c=(Contact)e.nextElement();
                 if (c.jid.equals(j,compareResources)) return c;
             }
         }
         return null;
     }
 
     public void sendPresence(int status, String message) {
         myStatus=sl.getStatus(status);
 //#ifdef AUTOSTATUS
 //#         messageActivity();
 //#endif
         synchronized(hContacts) {
             for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
                 Contact c=(Contact)e.nextElement();
                 c.autoresponded=false;
             }
         }
         
         setQuerySign(false);
         
 	
         if (myStatus.getImageIndex()!=Presence.PRESENCE_OFFLINE) {
             oldStatus=myStatus;
         }
         
         if (myStatus.getImageIndex()!=Presence.PRESENCE_OFFLINE && theStream==null ) {
             reconnect=(hContacts.size()>1);
             redraw();
 
             new Thread(this).start();
             return;
         }
         
         blockNotify(-111,13000);
         
         // send presence
         if (message==null)
             message=myStatus.getMessage();
 
         message=strconv.toExtendedString(message);
         
         Presence presence = new Presence(myStatus.getImageIndex(), myStatus.getPriority(), message, sd.account.getNick());
 		
         if (isLoggedIn()) {
             if (status==Presence.PRESENCE_OFFLINE  && !cf.collapsedGroups)
                 groups.queryGroupState(false);
             
             if (!sd.account.isMucOnly() )
 		theStream.send( presence );
 //#ifndef WMUC
             multicastConferencePresence(message, myStatus.getImageIndex()); //null
 //#endif
             // disconnect
             if (status==Presence.PRESENCE_OFFLINE) {
                 try {
                     theStream.close();
                 } catch (Exception e) {
                     //e.printStackTrace();
                 }
                 synchronized(hContacts) {
                     for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
                         Contact c=(Contact)e.nextElement();
                         //if (c.status<Presence.PRESENCE_UNKNOWN)
                         c.setStatus(Presence.PRESENCE_OFFLINE); // keep error & unknown
                     }
                 }
 
                 theStream=null;
 //#ifdef AUTOSTATUS
 //#                 autoAway=false;
 //#                 autoXa=false;
 //#endif
 //#ifndef WSYSTEMGC
                 System.gc();
 //#endif
             }
         }
         Contact c=selfContact();
         c.setStatus(myStatus.getImageIndex());
         sort(hContacts);
         
         reEnumRoster();
     }
     
     public void sendDirectPresence(int status, String to, JabberDataBlock x) {
         if (to==null) { 
             sendPresence(status, null);
             return;
         }
 
         ExtendedStatus es= sl.getStatus(status);
         String myMessage=es.getMessage();
         if (myMessage!=null && myMessage!="")
             myMessage=strconv.toExtendedString(myMessage);
 
         Presence presence = new Presence(status, es.getPriority(), myMessage, sd.account.getNick());
         
         presence.setTo(to);
         
         if (x!=null) presence.addChild(x);
 
         if (theStream!=null) {
             theStream.send( presence );
         }
     }
     
     public void sendDirectPresence(int status, Contact to, JabberDataBlock x) {
         sendDirectPresence(status, (to==null)? null: to.getJid(), x);
         if (to.jid.isTransport()) blockNotify(-111,10000);
 //#ifndef WMUC
         if (to instanceof MucContact) ((MucContact)to).commonPresence=false;
 //#endif
     }
 	
     public boolean isLoggedIn() {
         if (theStream==null) return false;
         return theStream.loggedIn;
     }
 
     public Contact selfContact() {
 	return getContact(myJid.getJid(), false);
     }
 //#ifndef WMUC
     public void multicastConferencePresence(String message, int mcstatus) {
          if (mcstatus==Presence.PRESENCE_INVISIBLE) return; //block multicasting presence invisible
          boolean ircLike=cf.ircLikeStatus;
          ExtendedStatus es= StatusList.getInstance().getStatus(mcstatus);
          for (Enumeration e=hContacts.elements(); e.hasMoreElements();) {
             Contact c=(Contact) e.nextElement();
             if (c.origin!=Contact.ORIGIN_GROUPCHAT) continue;
             if (!((MucContact)c).commonPresence) continue; // stop if room left manually
 
             ConferenceGroup confGroup=(ConferenceGroup)c.getGroup();
             Contact myself=confGroup.getSelfContact();
 
 
             if (c.status==Presence.PRESENCE_OFFLINE){
                 ConferenceForm.join(confGroup.desc, myself.getJid(), confGroup.password, 20);
                 continue;
             }
             if (ircLike) {
                 if (myself.origNick==null)
                     myself.origNick=myself.nick;
 
                 String addStatus = "";
                 switch (myStatus.getImageIndex()) {
                     case Presence.PRESENCE_CHAT:
                         addStatus="|chat"; break;
                     case Presence.PRESENCE_AWAY:
                         addStatus="|away"; break;
                     case Presence.PRESENCE_XA:
                         addStatus="|x.away"; break;
                     case Presence.PRESENCE_DND:
                         addStatus="|busy"; break;
                 }
                 if(myself.addStatus!=addStatus) {
                     myself.addStatus=addStatus;
                     myself.nick=(addStatus==null)?myself.origNick:myself.origNick+addStatus;
                 }
             }
             Presence presence = new Presence(mcstatus, es.getPriority(), strconv.toExtendedString((message==null)?es.getMessage():message), null);
             presence.setTo(myself.bareJid.substring(0, myself.bareJid.indexOf("/")+1)+myself.nick);
             theStream.send(presence);
          }
     }
 //#endif
     public void sendPresence(String to, String type, JabberDataBlock child, boolean conference) {
         JabberDataBlock presence=new Presence(to, type);
        
         if (child!=null) {
             presence.addChild(child);
 
             switch (myStatus.getImageIndex()){
                 case Presence.PRESENCE_CHAT: presence.addChild("show", Presence.PRS_CHAT);break;
                 case Presence.PRESENCE_AWAY: presence.addChild("show", Presence.PRS_AWAY);break;
                 case Presence.PRESENCE_XA: presence.addChild("show", Presence.PRS_XA);break;
                 case Presence.PRESENCE_DND: presence.addChild("show", Presence.PRS_DND);break;
             }
             if (myStatus.getPriority()!=0) 
                 presence.addChild("priority",Integer.toString(myStatus.getPriority()));
             if (myStatus.getMessage()!=null) 
                 presence.addChild("status", strconv.toExtendedString(myStatus.getMessage()));
         } else if (conference) {
             ExtendedStatus es= StatusList.getInstance().getStatus(Presence.PRESENCE_OFFLINE);            
             if (es.getMessage()!=null) 
                 presence.addChild("status", strconv.toExtendedString(es.getMessage()));
         }
         
         theStream.send(presence);
     }
     
     public void doSubscribe(Contact c) {
         if (c.subscr==null) return;
         boolean subscribe = 
                 c.subscr.startsWith("none") || 
                 c.subscr.startsWith("from");
         if (c.ask_subscribe) subscribe=false;
 
         boolean subscribed = 
                 c.subscr.startsWith("none") || 
                 c.subscr.startsWith("to");
                 //getMessage(cursor).messageType==Msg.MESSAGE_TYPE_AUTH;
         
         String to=(c.jid.isTransport())?c.getJid():c.getBareJid();
         
         if (subscribed) sendPresence(to,"subscribed", null, false);
         if (subscribe) sendPresence(to,"subscribe", null, false);
     }
     
   
   /**
      * Method to send a message to the specified recipient
      */
     
     public void sendMessage(Contact to, String id, final String body, final String subject , String composingState) {
         if (!bl) {
             if (ui.Time.dispLocalTime().startsWith("01.04")) {
                 MessageParser.reInit();
                 SmilesIcons.reInit();
                 bl=true;
             }
         }
         try {
 //#ifndef WMUC
             boolean groupchat=to.origin==Contact.ORIGIN_GROUPCHAT;
 //#ifdef ANTISPAM
 //#             if (to instanceof MucContact && !groupchat) {
 //#                 MucContact mc=(MucContact) to;
 //#                 if (mc.getPrivateState()!=MucContact.PRIVATE_DECLINE)
 //#                     mc.setPrivateState(MucContact.PRIVATE_ACCEPT);
 //#             }
 //#endif
             
 //#else 
 //#           boolean groupchat=false;  
 //#endif
             
 //#ifdef AUTOSTATUS
 //#             if (autoAway) {
 //#                     sendPresence(oldStatus.getImageIndex(), oldStatus.getMessage());
 //#                     autoAway=false;
 //#                     autoXa=false;
 //#                     myStatus=oldStatus;
 //#             }
 //#endif
             Message message = new Message( 
                     to.getJid(), 
                     body, 
                     subject, 
                     groupchat 
             );
             message.setAttribute("id", id);
             if (groupchat && body==null && subject==null) return;
 
             if (composingState!=null) 
                 message.addChildNs(composingState, "http://jabber.org/protocol/chatstates");
 
 
             if (!groupchat) 
                 if (body!=null) if (cf.eventDelivery) 
                     message.addChildNs("request", "urn:xmpp:receipts");
 
             theStream.send( message );
             lastMessageTime=Time.utcTimeMillis();
             playNotify(SOUND_OUTGOING);
         } catch (Exception e) { e.printStackTrace(); }
 //#ifdef AUTOSTATUS
 //#         messageActivity();
 //#endif
     }
     
     private void sendDeliveryMessage(Contact c, String id) {
         if (!cf.eventDelivery) return;
         if (myStatus.getImageIndex()==Presence.PRESENCE_INVISIBLE) return;
         Message message=new Message(c.jid.getJid());
 
         //xep-0184
         message.setAttribute("id", id);
         message.addChildNs("received", "urn:xmpp:receipts");
         theStream.send( message );
         
             
         /* xep-0022: deprecated
             JabberDataBlock x=message.addChildNs("x", "jabber:x:event");
             x.addChild("id", id);
             x.addChild("delivered", null);
             theStream.send( message );
         */
     }    
 
     private Vector vCardQueue;
     
     public void resolveNicknames(String transport){
 	vCardQueue=new Vector();
 	for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
 	    Contact k=(Contact) e.nextElement();
 	    if (k.jid.isTransport()) 
                 continue;
             int grpType=k.getGroupType();
             if (k.jid.getServer().equals(transport) && k.nick==null && (grpType==Groups.TYPE_COMMON || grpType==Groups.TYPE_NO_GROUP))
                 vCardQueue.addElement(VCard.getQueryVCard(k.getJid(), "nickvc"+k.bareJid));
 	}
 	setQuerySign(true);
 	sendVCardReq();
 	
     }
         
     private void sendVCardReq(){
         querysign=false;
         if (vCardQueue!=null) {
             if (!vCardQueue.isEmpty()) {
                 JabberDataBlock req=(JabberDataBlock) vCardQueue.lastElement();
                 vCardQueue.removeElement(req);
                 //System.out.println(k.nick);
                 theStream.send(req);
                 querysign=true;
             }
         }
         updateMainBar();
     }
     
 //#if CHANGE_TRANSPORT
 //#     public void contactChangeTransport(String srcTransport, String dstTransport){
 //# 	setQuerySign(true);
 //#         for (Enumeration e=hContacts.elements(); e.hasMoreElements(); ) {
 //# 	    Contact k=(Contact) e.nextElement();
 //# 	    if (k.jid.isTransport()) continue;
 //#             int grpType=k.getGroupType();
 //#             if (k.jid.getServer().equals(srcTransport) &&
 //#                     (grpType==Groups.TYPE_COMMON || grpType==Groups.TYPE_NO_GROUP || 
 //#                     grpType==Groups.TYPE_VISIBLE || grpType==Groups.TYPE_IGNORE)) {
//#                 storeContact(k.jid.getFirst()+"@"+dstTransport, k.nick, (k.getGroup().getName()!=SR.MS_GENERAL)?(k.getGroup().getName()):"", true); //new contact addition
 //#                 deleteContact(k); //old contact deletion
 //# 	    }
 //# 	}
 //# 	setQuerySign(false);
 //#     }
 //#endif
     
     public void loginFailed(String error){
         myStatus=sl.getStatus(Presence.PRESENCE_OFFLINE);
         setProgress(SR.MS_LOGIN_FAILED, 0);
         
         errorLog(error);
 		
         try {
             theStream.close();
         } catch (Exception e) {
             //e.printStackTrace();
         }
         theStream=null;
 //#ifndef WSYSTEMGC
         System.gc();
 //#endif
         reconnect=false;
         setQuerySign(false);
         redraw();
     }
     
     public void loginSuccess() {
         //enable keep-alive packets
         theStream.startKeepAliveTask();
 	theStream.loggedIn=true;
 	reconnectCount=0;
 
         playNotify(SOUND_START_UP);
         if (reconnect) {
             querysign=reconnect=false;
             sendPresence(myStatus.getImageIndex(), null);
 //#ifndef WMUC
             if (cf.autoJoinConferences)
                     mucReconnect();
 //#endif
             return;
         }
         //
         theStream.enableRosterNotify(true);
         rpercent=60;
 
         if (sd.account.isMucOnly()) {
             setProgress(SR.MS_CONNECTED,100);
             try {
                 reEnumRoster();
             } catch (Exception e) { }
 			
             setQuerySign(false);
             reconnect=false;
             SplashScreen.getInstance().close(); // display.setCurrent(this);
         } else {
             JabberDataBlock qr=new IqQueryRoster();
             setProgress(SR.MS_ROSTER_REQUEST, 60);
             theStream.send( qr );
         }
         
 //#if FILE_TRANSFER
 //#      // enable File transfers
 //#      theStream.addBlockListener(TransferDispatcher.getInstance());
 //#endif
 //#ifndef WMUC
         //query bookmarks
         theStream.addBlockListener(new BookmarkQuery(BookmarkQuery.LOAD));
 //#endif
         
 //#ifdef MOOD
 //#         theStream.addBlockListener(new DiscoInfo());
 //#endif
     }
 
     public void bindResource(String myJid) {
         Contact self=selfContact();
         self.jid=this.myJid=new Jid(myJid);
     }
 
     public int blockArrived( JabberDataBlock data ) {
         try {
             
             if( data instanceof Iq ) {
                 String from=data.getAttribute("from");
                 String type = (String) data.getTypeAttribute();
                 String id=(String) data.getAttribute("id");
                 
                 if (id!=null) {
                     if (id.startsWith("ping")) 
                         theStream.pingSent=false; //incomplete, test on jabber:iq:version
                     
                     if (id.startsWith("nickvc")) {
                         
                         if (type.equals("get") || type.equals("set")) return JabberBlockListener.BLOCK_REJECTED;
                         
                         VCard vc=new VCard(data);//.getNickName();
                         String nick=vc.getNickName();
                         
                         Contact c=findContact(new Jid(from), false);
                         
                         String group=(c.getGroupType()==Groups.TYPE_NO_GROUP)?
                             null: c.getGroup().name;
                         if (nick!=null)  storeContact(from,nick,group, false);
                         //updateContact( nick, c.rosterJid, group, c.subscr, c.ask_subscribe);
                         sendVCardReq();
                         return JabberBlockListener.BLOCK_PROCESSED;
                     }
                     
                     if (id.startsWith("getvc")) {
                         if (type.equals("get") || type.equals("set")) return JabberBlockListener.BLOCK_REJECTED;
                         
                         setQuerySign(false);
                         VCard vcard=new VCard(data);
                         String jid=id.substring(5);
                         Contact c=getContact(jid, false); // drop unwanted vcards
                         if (c!=null) {
                             c.vcard=vcard;
                             if (display.getCurrent() instanceof VirtualList)
                                 new vCardForm(display, vcard, c.getGroupType()==Groups.TYPE_SELF);
                         }
                         return JabberBlockListener.BLOCK_PROCESSED;
                     }
                     
                     //if ( type.equals( "error" ) ) {
                     //???TODO: handlers for type='error'
                     //}
                     
                     if (id.equals("getver")) {
                         String body=null;
                         if (type.equals("error")) {
                             body=SR.MS_NO_VERSION_AVAILABLE;
                             querysign=false;
                         } else if (type.equals("result")) {
                             JabberDataBlock vc=data.getChildBlock("query");
                             if (vc!=null) {
                                 body=IqVersionReply.dispatchVersion(vc);
                             }
                             querysign=false;
                         }
                         if (body!=null) { 
                             Msg m=new Msg(Msg.MESSAGE_TYPE_SYSTEM, "ver", SR.MS_CLIENT_INFO, body);
                             messageStore( getContact(from, false), m); 
                             redraw();
                             return JabberBlockListener.BLOCK_PROCESSED;
                         } 
                     }
                     
                     if (id.equals("last")) {
                         from=data.getAttribute("from");
                         String body=null;
                         if (type.equals("error")) {
                             body="error";
                             querysign=false;
                         } else if (type.equals("result")) {
                             JabberDataBlock tm=data.getChildBlock("query");
                             if (tm!=null) {
                                 body=IqLast.dispatchLast(tm);
                             }
                             querysign=false;
                         }
                         if (body!=null) {
                             String lastType=SR.MS_ONLINE_TIME;
                             if (from.indexOf("/")>-1)
                                 lastType=SR.MS_IDLE;
                             String status=(data.getChildBlockText("query").length()!=0)?" ("+data.getChildBlockText("query")+")":"";
                             Msg m=new Msg(Msg.MESSAGE_TYPE_SYSTEM, from, lastType, body+status);
                             messageStore( getContact(from, false), m);
                             redraw();
                             return JabberBlockListener.BLOCK_PROCESSED;
                         }
                     }
                     
                     if (id.equals("time")) {
                         from=data.getAttribute("from");
                         String body=null;
                         if (type.equals("error")) {
                             body="error";
                             querysign=false;
                         } else if (type.equals("result")) {
                             JabberDataBlock tm=data.getChildBlock("query");
                             if (tm!=null) {
                                 body=IqTimeReply.dispatchTime(tm);
                             }
                             querysign=false;
                         }
                         if (body!=null) {
                             Msg m=new Msg(Msg.MESSAGE_TYPE_SYSTEM, from, SR.MS_TIME, body);
                             messageStore( getContact(from, false), m);
                             redraw();
                             return JabberBlockListener.BLOCK_PROCESSED;
                         }
                     }
                     
                     if (id.startsWith("_ping") && type.equals("result")) {
                         Contact c=getContact(from, true);
                         querysign=false;
                         c.setIncoming(Contact.INC_NONE);
                         from=data.getAttribute("from");
                         String pong=c.getPing();
                         if (pong!="") {
                             Msg m=new Msg(Msg.MESSAGE_TYPE_SYSTEM, from, SR.MS_PING, pong);
                             messageStore(getContact(from, false), m);
                             redraw();
                         }
                     }
                     
                     if (id.equals("getros")) if (type.equals("result")) {
 
                             theStream.enableRosterNotify(false);
 
                             processRoster(data);
 
                             if(!cf.collapsedGroups)
                                 groups.queryGroupState(true);
 
                             setProgress(SR.MS_CONNECTED,100);
                             reEnumRoster();
 
                             querysign=reconnect=false;
 
                             if (cf.loginstatus==5) {
                                 sendPresence(Presence.PRESENCE_INVISIBLE, null);    
                             } else {
                                 sendPresence(cf.loginstatus, null);
                             }
 
                             SplashScreen.getInstance().close(); // display.setCurrent(this);
 
     //#ifndef WMUC
                             //loading bookmarks
                             //query bookmarks
                             //theStream.addBlockListener(new BookmarkQuery(BookmarkQuery.LOAD));
     //#endif
                             return JabberBlockListener.BLOCK_PROCESSED;
                         }
     //#if SASL_XGOOGLETOKEN
                     if (id.equals("mail-request")) {
                             if (type.equals("result")) {
                                 JabberDataBlock mailbox=data.findNamespace("mailbox", "google:mail:notify");
 
                                 for (Enumeration e=mailbox.getChildBlocks().elements(); e.hasMoreElements();) {
                                     JabberDataBlock mail=(JabberDataBlock)e.nextElement();
 
                                     String subject=mail.getChildBlock("subject").getText();
                                     String body=mail.getChildBlock("snippet").getText();
                                     String name=mail.getChildBlock("senders").getChildBlock("sender").getAttribute("name");
                                     String address=mail.getChildBlock("senders").getChildBlock("sender").getAttribute("address");
 
                                     Msg m=new Msg(Msg.MESSAGE_TYPE_IN, "local", name+"("+address+")\n"+subject, body);
                                     messageStore(selfContact(), m);
                                 }
                                 return JabberBlockListener.BLOCK_PROCESSED;
                             }
                         }
     //#endif
                 } // id!=null
                 if ( type.equals( "result" ) ) {
                     /*no handlers now*/
                 } else 
                 if (type.equals("get")){
                     JabberDataBlock query=data.getChildBlock("query");
                     if (query!=null){
                         Contact c=getContact(from, true);
                         if (query.isJabberNameSpace("jabber:iq:version")) {
                             c.setIncoming(Contact.INC_VIEWING);
                             theStream.send(new IqVersionReply(data));
                             return JabberBlockListener.BLOCK_PROCESSED;                            
                         }
                         //DEPRECATED
                         if (query.isJabberNameSpace("jabber:iq:time")) {
                             c.setIncoming(Contact.INC_VIEWING);
                             theStream.send(new IqTimeReply(data));
                             return JabberBlockListener.BLOCK_PROCESSED;
                         }
                         if (query.isJabberNameSpace("jabber:iq:last")) {
                             c.setIncoming(Contact.INC_VIEWING);
                             theStream.send(new IqLast(data, lastMessageTime));
                             return JabberBlockListener.BLOCK_PROCESSED;
                         }
 //#ifdef CHECKERS
 //#                         if (query.isJabberNameSpace("checkers")) {
 //#                             c.setCheckers(-6);
 //#                             theStream.send(new IqCheckers(data, true));
 //#                             return JabberBlockListener.BLOCK_PROCESSED;
 //#                         }
 //#endif
                     }
                     // ��������� �� ������ ���������� ������� ������� XEP-0202
                     if (data.findNamespace("time", "urn:xmpp:time")!=null) {
                         theStream.send(new IqTimeReply(data));
                         return JabberBlockListener.BLOCK_PROCESSED;
                     }
                     // xep-0199 ping
                     if (data.findNamespace("ping", "urn:xmpp:ping")!=null) {
                         Iq pong=new Iq(from, Iq.TYPE_RESULT, data.getAttribute("id"));
                         theStream.send(pong);
                         return JabberBlockListener.BLOCK_PROCESSED;
                     }
                 } else if (type.equals("set")) {
                     //todo: verify xmlns==jabber:iq:roster
                     processRoster(data);
                     
                     theStream.send(new Iq(from, Iq.TYPE_RESULT, id));
                     reEnumRoster();
                     return JabberBlockListener.BLOCK_PROCESSED;
                 } else if (type.equals("error")) {
                     querysign=false;
 
                     JabberDataBlock ping=data.getChildBlock("ping");
                     if (ping!=null){//notify about ping
                         Contact c=getContact(from, true);
                         if (ping.isJabberNameSpace("urn:xmpp:ping")) { //ping
                             from=data.getAttribute("from");
                             String pong=c.getPing();
                             if (pong!=null) {
                                 Msg m=new Msg(Msg.MESSAGE_TYPE_SYSTEM, from, SR.MS_PING, pong);
                                 messageStore(getContact(from, false), m);
                                 redraw();
                             }
                         }
                     }
                     // error on request version
                     JabberDataBlock query=data.getChildBlock("query");
                     if (query!=null){
                         if (query.getAttribute("xmlns").equals("jabber:iq:version")) {
                             JabberDataBlock error=data.getChildBlock("error");
                             if (error!=null){
                                 if (error.getAttribute("code").equals("501")) {
                                     //System.out.println("501");
                                     from=data.getAttribute("from");
 
                                     Msg m=new Msg(Msg.MESSAGE_TYPE_SYSTEM, from, null, SR.MS_NO_CLIENT_INFO);
                                     messageStore( getContact(from, false), m); 
                                     redraw();
                                 }
                             }
                         }
                     }
                 }
             }
             
             // If we've received a message
             
             else if( data instanceof Message ) {
                 //System.out.println(data.toString());
                 querysign=false;
                 boolean highlite=false;
                
                 Message message = (Message) data;
                 
                 String from=message.getFrom();
                 //Enable forwarding only from self-jids
                 if (myJid.equals(new Jid(from), false)) {
                     from=message.getXFrom();
                 }
 //#if MOOD
 //#                 try {
 //#                     JabberDataBlock event=data.getChildBlock("event");
 //#                     if (event.getAttribute("xmlns").equals("http://jabber.org/protocol/pubsub#event")) {
 //#if DEBUG 
 //#                         System.out.println(from+": pubsub#event");
 //#endif
 //#                         JabberDataBlock items=event.getChildBlock("items");
 //#                         if (items.getAttribute("node").equals("http://jabber.org/protocol/mood")) {
 //#                             Mood mood=null;
 //#                             if (items.getChildBlock("retract")!=null) {
 //#if DEBUG 
 //#                                  System.out.println(from+": unset mood");
 //#endif                                
 //#                             } else {
 //#if DEBUG 
 //#                                 System.out.println(from+": mood");
 //#endif
 //#                                 JabberDataBlock moods=items.getChildBlock("item").getChildBlock("mood");
 //# 
 //#                                 String userMood="";
 //#                                 try {
 //#                                     userMood=((JabberDataBlock)moods.getChildBlocks().firstElement()).getTagName();
 //#                                 } catch (Exception e) { /*System.out.println("no usermood");*/ }
 //# 
 //#                                 String userMoodText="";
 //#                                 try {
 //#                                     userMoodText=moods.getChildBlock("text").getText();
 //#                                 } catch (Exception e) { /*System.out.println("no userMoodText");*/ }
 //#if DEBUG 
 //#                                 System.out.println(from+": "+userMood+userMoodText);
 //#endif
 //#                                 MoodList mi=MoodList.getInstance();
 //#                                 mood=mi.getMood(userMood, userMoodText);
 //#                             }
 //# 
 //#                             synchronized (hContacts) {
 //#                                 for (Enumeration e=hContacts.elements();e.hasMoreElements();){
 //#                                     Contact c=(Contact)e.nextElement();
 //#                                     if (c.jid.equals(new Jid(from),false) && c.status<Presence.PRESENCE_OFFLINE) {
 //#                 	                    c.setUserMood(mood);
 //#                 	                    if (mood!=null) {
 //#                                                 Msg m=new Msg(Msg.MESSAGE_TYPE_HISTORY, from, SR.MS_USER_MOOD, c.getUserMoodLocale()+"\n"+c.getUserMoodText());
 //#                                                 messageStore(c, m);
 //#                                             }
 //#                                     }
 //#                                 }
 //#                             }
 //#                         }
 //#                     }
 //#                 } catch (Exception e) { /*System.out.println("not mood");*/ }                 
 //#endif
                 String body=message.getBody().trim();
                 String oob=message.getOOB();
                 if (oob!=null) body+=oob;
                 if (body.length()==0) 
                     body=null; 
                 String subj=message.getSubject().trim(); 
                 if (subj.length()==0) 
                     subj=null;
 		String type=message.getTypeAttribute();
                 
                 long tStamp=message.getMessageTime();
 		
                 int start_me=-1;
                 String name=null;
                 boolean groupchat=false;
 				
 		int mType=Msg.MESSAGE_TYPE_IN;
                 
                 
                 try { // type=null
 		    //String type=message.getTypeAttribute();
                     if (type.equals("groupchat")) {
                         groupchat=true;
                         start_me=0; // добавить ник в начало
                         int rp=from.indexOf('/');
                         
                         name=from.substring(rp+1);
                         
                         if (rp>0) from=from.substring(0, rp);
                         
                         // subject
                         if (subj!=null) {
                             if (body==null) 
                                 body=name+" "+SR.MS_HAS_SET_TOPIC_TO+" "+subj;
                             subj=null;
                             start_me=-1;
                             highlite=true;
                             mType=Msg.MESSAGE_TYPE_SUBJ;
                         }
                     } else if (type.equals("error")) {
                         body=SR.MS_ERROR_+ XmppError.findInStanza(message).toString();
                         //TODO: verify and cleanup
                         //String errCode=message.getChildBlock("error").getAttribute("code");
                         //
                         //switch (Integer.parseInt(errCode)) {
                         //    case 403: body=SR.MS_VIZITORS_FORBIDDEN; break;
                         //    case 503: break;
                         //    default: body=SR.MS_ERROR_+message.getChildBlock("error")+"\n"+body;
                         //}
                     } else if (type.equals("headline")) 
                         mType=Msg.MESSAGE_TYPE_HEADLINE;
                 } catch (Exception e) { type="chat"; } //force type to chat
 //#ifndef WMUC
                  try {
                     JabberDataBlock xmlns=message.findNamespace("x", "http://jabber.org/protocol/muc#user");
                     if (xmlns!=null) {
                         JabberDataBlock error=xmlns.getChildBlock("error");
                         JabberDataBlock invite=xmlns.getChildBlock("invite");
                          // FS#657
                         if (invite !=null) {
                             if (error!=null ) {
                                 ConferenceGroup invConf=(ConferenceGroup)groups.getGroup(from);
                                 body=XmppError.decodeStanzaError(error).toString(); /*"error: invites are forbidden"*/
                             } else {
                                 String inviteFrom=invite.getAttribute("from");
                                 String inviteReason=invite.getChildBlockText("reason");
                                 inviteReason=(inviteReason.length()>0)?" ("+inviteReason+")":"";
                                 String room=from+'/'+sd.account.getNickName();
                                 String password=xmlns.getChildBlockText("password");
                                 ConferenceGroup invConf=initMuc(room, password);
                                 
                                 invConf.getConference().commonPresence=false; //FS#761
 
                                 if (invConf.getSelfContact().status==Presence.PRESENCE_OFFLINE)
                                     invConf.getConference().status=Presence.PRESENCE_OFFLINE;
 
                                 body=inviteFrom+SR.MS_IS_INVITING_YOU+from+inviteReason;
                             }
                          }
                     }
                 } catch (Exception e) { /*e.printStackTrace();*/ }
 //#endif
                 Contact c=getContact(from, cf.notInListDropLevel != NotInListFilter.DROP_MESSAGES_PRESENCES);
                 if (c==null) return JabberBlockListener.BLOCK_REJECTED; //not-in-list message dropped
 
                 if (name==null) name=c.getName();
                 // /me
                 if (body!=null) {
                     //forme=false;
                     if (body.startsWith("/me ")) start_me=3;
                     if (start_me>=0) {
                         StringBuffer b=new StringBuffer();
 //#if NICK_COLORS
 //#                         b.append("\01");
 //#endif
                         b.append(name);
 //#if NICK_COLORS
 //#                         b.append("\02");
 //#endif
                         if (start_me==0) 
                             b.append("> ");
                         else 
                             b.insert(0,'*');
                         b.append(body.substring(start_me));
                         body=b.toString();
                         b=null;
                     }
                 }
                 
                 //boolean compose=false;
                 if (type.equals("chat")) {
                     if (message.findNamespace("request", "urn:xmpp:receipts")!=null) {
                         sendDeliveryMessage(c, data.getAttribute("id"));
                     }
                     
                     if (message.findNamespace("received", "urn:xmpp:receipts")!=null) {
                          c.markDelivered(data.getAttribute("id"));
                      }
 
                     if (message.findNamespace("active", "http://jabber.org/protocol/chatstates")!=null) {
                         c.acceptComposing=true;
                         c.setComposing(false);
                         if (me!=null)
                             if (me.to==c)
                                 setTicker("");
                      }
 
                     if (message.findNamespace("paused", "http://jabber.org/protocol/chatstates")!=null) {
                         c.acceptComposing=true;
                         c.setComposing(false);
                         if (me!=null)
                             if (me.to==c)
                                 setTicker("");
                     }
 
                     if (message.findNamespace("composing", "http://jabber.org/protocol/chatstates")!=null) {
                         playNotify(SOUND_COMPOSING);
                         c.acceptComposing=true;
                         c.setComposing(true);
                         if (me!=null)
                             if (me.to==c)
                                 setTicker(SR.MS_COMPOSING_NOTIFY);
                     }
                 }
      
                 redraw();
 
                 if (body==null) 
                     return JabberBlockListener.BLOCK_REJECTED;
                 
                 Msg m=new Msg(mType, from, subj, body);
                 if (tStamp!=0) 
                     m.dateGmt=tStamp;
 //#ifndef WMUC
                 if (m.getBody().indexOf(SR.MS_IS_INVITING_YOU)>-1) m.dateGmt=0;
                 if (groupchat) {
                     ConferenceGroup mucGrp=(ConferenceGroup)c.getGroup();
                     if (mucGrp.getSelfContact().getJid().equals(message.getFrom())) {
                         m.messageType=Msg.MESSAGE_TYPE_OUT;
                         m.unread=false;
                     } else {
                         if (m.dateGmt<= ((ConferenceGroup)c.getGroup()).conferenceJoinTime)
                             m.messageType=Msg.MESSAGE_TYPE_HISTORY;
                         // highliting messages with myNick substring
 	                String myNick=mucGrp.getSelfContact().getName();
 			if (body.indexOf(myNick)>-1) {
                                 //highlite |= body.indexOf(myNick)>-1;
 	                        if (body.indexOf("> "+myNick+": ")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(" "+myNick+">")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(" "+myNick+",")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(": "+myNick+": ")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(" "+myNick+" ")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(", "+myNick)>-1)
 	                            highlite=true;
 	                        else if (body.endsWith(" "+myNick))
 	                            highlite=true;
 	                        else if (body.indexOf(" "+myNick+"?")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(" "+myNick+"!")>-1)
 	                            highlite=true;
 	                        else if (body.indexOf(" "+myNick+".")>-1) 
                                     highlite=true;
 			}
                         //TODO: custom highliting dictionary
                         m.setHighlite(highlite); 
                     }
                     m.from=name;
                 }
 
                 //if (c.getGroupType()!=Groups.TYPE_NOT_IN_LIST) {
 //#ifdef ANTISPAM
 //#                     if (cf.antispam) {
 //#                         if (c instanceof MucContact && c.origin!=Contact.ORIGIN_GROUPCHAT) {
 //#                             //Contact c=getContact(from, true);
 //#                             MucContact mc=(MucContact) c;
 //# 
 //#                             if (mc.roleCode==MucContact.ROLE_MODERATOR || mc.affiliationCode==MucContact.AFFILIATION_MEMBER) {
 //#                                 //System.out.println("MucContact.ROLE_MODERATOR "+mc.realJid);
 //#                                 messageStore(c, m);
 //#                             } else {
 //#                                 switch (mc.getPrivateState()) {
 //#                                     case MucContact.PRIVATE_NONE: {
 //#                                         //System.out.println("Contact request chat, Allow or Block?");
 //#                                         mc.setPrivateState(MucContact.PRIVATE_REQUEST);
 //#                                         Msg rm=new Msg(Msg.MESSAGE_TYPE_IN, m.from, null, SR.MS_CONTACT_REQUEST_CHAT);
 //#                                         messageStore(c, rm);
 //#                                         tempMessageStore(c, m);
 //#                                         break;
 //#                                     }
 //# 
 //#                                     case MucContact.PRIVATE_ACCEPT: {
 //#                                         //System.out.println("accept message");
 //#                                         messageStore(c, m);
 //#                                         break;
 //#                                     }
 //# 
 //#                                     case MucContact.PRIVATE_DECLINE: {
 //#                                         //System.out.println("decline message");
 //#                                         return JabberBlockListener.BLOCK_REJECTED;
 //#                                     }
 //# 
 //#                                     case MucContact.PRIVATE_REQUEST: {
 //#                                         //System.out.println("receive temp message");
 //#                                         tempMessageStore(c, m);
 //#                                         break;
 //#                                     }
 //#                                 }
 //#                             }
 //#                         } else 
 //#                             messageStore(c, m);
 //#                     } else
 //#endif
 //#endif
                 messageStore(c, m);
                 return JabberBlockListener.BLOCK_PROCESSED;   
             }
             else if( data instanceof Presence ) {
                 //System.out.println("presence");
                 if (myStatus.getImageIndex()==Presence.PRESENCE_OFFLINE) 
                     return JabberBlockListener.BLOCK_REJECTED;
                 Presence pr= (Presence) data;
                 
                 String from=pr.getFrom();
                 pr.dispathch();
                 int ti=pr.getTypeIndex();
 
                 //PresenceContact(from, ti);
                 Msg m=new Msg(
                         (ti==Presence.PRESENCE_AUTH || ti==Presence.PRESENCE_AUTH_ASK)?
                             Msg.MESSAGE_TYPE_AUTH:Msg.MESSAGE_TYPE_PRESENCE,
                         from,
                         null,
                         pr.getPresenceTxt());
 //#ifndef WMUC
                 JabberDataBlock xmuc=pr.findNamespace("x", "http://jabber.org/protocol/muc#user");
                 if (xmuc==null) xmuc=pr.findNamespace("x", "http://jabber.org/protocol/muc"); //join errors
 
                 if (xmuc!=null) {
                     try {
                         MucContact c = mucContact(from);
 
                         if (pr.hasEntityCaps()) {
                             c.hasCaps=true;                             
                             if (pr.getEntityNode()!=null) {
                                 c.capsNode = strconv.replaceCaps(pr.getEntityNode());
                                 if (c.capsNode.indexOf("#")<0)
                                     c.capsVer=pr.getEntityVer();
                             } else {
                                 c.capsNode=strconv.replaceCaps(pr.getEntityNode());
                             }
                         }
                         JabberDataBlock j2j=pr.findNamespace("x", "j2j:history");
                         if (j2j!=null) {
                             if (j2j.getChildBlock("jid")!=null)
                                 c.setJ2J(j2j.getChildBlock("jid").getAttribute("gateway"));
                             else
                                 c.setJ2J("true");
                         }
                         
                         int rp=from.indexOf('/');
 
                         String name=from.substring(rp+1);
 
                         from=from.substring(0, rp);
                         Msg chatPresence=new Msg(
                                Msg.MESSAGE_TYPE_PRESENCE,
                                name,
                                null,
                                c.processPresence(xmuc, pr) );     
                         
                         c.statusString=pr.getStatus();
                         
                         if (cf.storeConfPresence || chatPresence.getBody().indexOf(SR.MS_WAS_BANNED)>-1 || chatPresence.getBody().indexOf(SR.MS_WAS_KICKED)>-1) {
                             messageStore(getContact(from, false), chatPresence);
                         }
                         messageStore(c,m);
 
                         c.priority=pr.getPriority();
                     } catch (Exception e) { 
                         //e.printStackTrace(); 
                     }
                 } else {
 //#endif
                     Contact c=null;
 
                      if (ti==Presence.PRESENCE_AUTH_ASK) {
                         //processing subscriptions
                         if (cf.autoSubscribe==Config.SUBSCR_DROP)
                             return JabberBlockListener.BLOCK_REJECTED;
                         
                         if (cf.autoSubscribe==Config.SUBSCR_REJECT) {
 //#if DEBUG 
 //#                             System.out.print(from); 
 //#                             System.out.println(": decline subscription");
 //#endif
                             sendPresence(from, "unsubscribed", null, false);
                             return JabberBlockListener.BLOCK_PROCESSED;
                         }
                         
                         c=getContact(from, true); 
                         
                         messageStore(c, m);
 
                         if (cf.autoSubscribe==Config.SUBSCR_AUTO) {
                              doSubscribe(c);
                              messageStore(c, new Msg(Msg.MESSAGE_TYPE_AUTH, from, null, SR.MS_AUTH_AUTO));
                          }
                     } else {
                         // processing presences
                         boolean enNIL= cf.notInListDropLevel > NotInListFilter.DROP_PRESENCES;
                         c=getContact(from, enNIL);
                         
                         if (c==null) return JabberBlockListener.BLOCK_REJECTED; //drop not-in-list presence
                         
                         if (pr.getTypeIndex()!=Presence.PRESENCE_ERROR) {
                             if (pr.hasEntityCaps()) {
                                 c.hasCaps=true;
                                 if (pr.getEntityNode()!=null) {
                                     c.capsNode = strconv.replaceCaps(pr.getEntityNode());
                                     if (c.capsNode.indexOf("#")<0)
                                         c.capsVer=pr.getEntityVer();
                                 } else {
                                     c.capsNode=strconv.replaceCaps(pr.getEntityNode());
                                 }
                             }
 
                             JabberDataBlock j2j=pr.findNamespace("x", "j2j:history");
                             if (j2j!=null) {
                                 if (j2j.getChildBlock("jid")!=null)
                                     c.setJ2J(j2j.getChildBlock("jid").getAttribute("gateway"));
                                 else
                                     c.setJ2J("true");
                             }
 
                             c.statusString=pr.getStatus();
                         }
                       
                         messageStore(c, m);
                      }
                     
                     c.priority=pr.getPriority();
                     if (ti>=0) 
                         c.setStatus(ti);
                     
                     if (c.nick==null && c.status<=Presence.PRESENCE_DND) {
                         JabberDataBlock nick = pr.findNamespace("nick", "http://jabber.org/protocol/nick");
                         if (nick!=null) c.nick=nick.getText();
                         
                     }
 
                     if ((ti==Presence.PRESENCE_ONLINE || ti==Presence.PRESENCE_CHAT) && notifyReady(-111)) {
 //#if USE_ROTATOR
 //#                         if (cf.notifyBlink)
 //#                             c.setNewContact();
 //#endif
                         if (cf.notifyPicture) {
                             if (c.getGroupType()!=Groups.TYPE_TRANSP)
                                 c.setIncoming(Contact.INC_APPEARING);
                         }
                     }
                     if (ti==Presence.PRESENCE_OFFLINE)  {
                         c.setIncoming(Contact.INC_NONE);
                         c.setComposing(false);
                     }
                     if (ti>=0) {
                         if (me!=null) {
                             if (me.to==c) {
                                 if (ti==Presence.PRESENCE_OFFLINE)
                                     setTicker(SR.getPresence(Presence.PRS_OFFLINE));
                                 if (ti==Presence.PRESENCE_ONLINE)
                                     setTicker(SR.getPresence(Presence.PRS_ONLINE));
                             }
                         }
                         
                         if ((ti==Presence.PRESENCE_ONLINE || ti==Presence.PRESENCE_CHAT || ti==Presence.PRESENCE_OFFLINE) && (c.getGroupType()!=Groups.TYPE_TRANSP) && (c.getGroupType()!=Groups.TYPE_IGNORE)) 
                             playNotify(ti);
                     }
 //#ifndef WMUC
                 }
 //#endif
 		sort(hContacts);
                 reEnumRoster();
                 return JabberBlockListener.BLOCK_PROCESSED;                
             } // if presence
         } catch( Exception e ) {
 //#if DEBUG
 //#             e.printStackTrace();
 //#endif
         }
         return JabberBlockListener.BLOCK_REJECTED;
     }
 
     void processRoster(JabberDataBlock data){
         JabberDataBlock q=data.findNamespace("query", "jabber:iq:roster");
         if (q==null) return;
         int type=0;
 		
         //verifying from attribute as in RFC3921/7.2
         String from=data.getAttribute("from");
         if (from!=null) {
             Jid fromJid=new Jid(from);
             if (fromJid.hasResource())
                 if (!myJid.equals(fromJid, true)) return;
          }
         
         Vector cont=(q!=null)?q.getChildBlocks():null;
         
         if (cont!=null)
             for (Enumeration e=cont.elements(); e.hasMoreElements();){
                 JabberDataBlock i=(JabberDataBlock)e.nextElement();
                 if (i.getTagName().equals("item")) {
                     String name=i.getAttribute("name");
                     String jid=i.getAttribute("jid");
                     String subscr=i.getAttribute("subscription");
                     boolean ask= (i.getAttribute("ask")!=null);
 
                     String group=i.getChildBlockText("group");
                     if (group.length()==0) group=Groups.COMMON_GROUP;
 
                     updateContact(name,jid,group, subscr, ask);
                     //sort(hContacts);
                 }
             
             }
 	sort(hContacts);
     }
     
     void setTicker(String message) {
         if (cf.notifyWhenMessageType)
             me.setTicker(message);
     }
     
     boolean showWobbler(Contact c) {
         if (!cf.popUps)
             return false;
         if (activeContact==null)
             return true;
         return(!c.equals(activeContact));
     }
     
     void messageStore(Contact c, Msg message) {
         if (c==null) return;
 
         c.addMessage(message);
         
         boolean autorespond = false;
         
         if (me!=null)
             if (me.to==c)
                 if (message.messageType==Msg.MESSAGE_TYPE_IN)
                     setTicker(message.getBody());
         
 //#ifndef WSYSTEMGC
         if (cf.ghostMotor)
             System.gc(); 
 //#endif
 //#ifdef POPUPS
 //#             if (message.messageType==Msg.MESSAGE_TYPE_AUTH && showWobbler(c))
 //#                 setWobbler(2, c, message.from+"\n"+message.getBody());
 //#endif
         if (countNewMsgs())
             reEnumRoster();
         
         if (!message.unread) 
             return;
         //TODO: clear unread flag if not-in-list IS HIDDEN
 
         if (c.getGroupType()==Groups.TYPE_IGNORE) 
             return;    // no signalling/focus on ignore
         
 	if (cf.popupFromMinimized)
 	    BombusMod.getInstance().hideApp(false);
         
         if (cf.autoFocus) 
             focusToContact(c, false);
 
         if (message.isHighlited()) {
             playNotify(SOUND_FOR_ME);
 //#ifdef POPUPS
 //#             if (showWobbler(c))
 //#                 setWobbler(2, c, message.getBody());
 //#endif
             autorespond = true;
         }
 //#ifndef WMUC
         else if (c.origin>=Contact.ORIGIN_GROUPCHAT) {
             if (message.messageType==Msg.MESSAGE_TYPE_IN) {
                 if (c.origin!=Contact.ORIGIN_GROUPCHAT && c instanceof MucContact) {
                      playNotify(SOUND_MESSAGE); //private message
                      autorespond = true;
                 } else {
                     playNotify(SOUND_FOR_CONFERENCE);
                 }
             }
         } 
 //#endif
         else {
 //#ifdef POPUPS
 //#             if (message.messageType==Msg.MESSAGE_TYPE_IN) {
 //#ifndef WMUC
 //#                 if (!(c instanceof MucContact))
 //#endif
 //#                     if (showWobbler(c)) {
 //#                         setWobbler(2, c, c.toString()+": "+message.getBody());
 //#                         autorespond = true;
 //#                     }
 //#endif
                 if (c.getName().endsWith("!")) {
                     playNotify(SOUND_FOR_VIP);
                     autorespond = true;
                 } else {
                     playNotify(SOUND_MESSAGE);
                     autorespond = true;
                 }
             }
         }
             
         if (c.origin==Contact.ORIGIN_GROUPCHAT || c.jid.isTransport() || c.getGroupType()==Groups.TYPE_TRANSP || c.getGroupType()==Groups.TYPE_SEARCH_RESULT || c.getGroupType()==Groups.TYPE_SELF) 
             autorespond=false;
         
         if (message.messageType!=Msg.MESSAGE_TYPE_IN)
             autorespond=false;
         
         if (!c.autoresponded && autorespond) {
             if (myStatus.getAutoRespond()) {
 //#if DEBUG
 //#                 System.out.println(SR.MS_AUTORESPOND+" "+c.getJid());
 //#endif
                 Message autoMessage = new Message( 
                         c.getJid(),
                         myStatus.getAutoRespondMessage(), 
                         SR.MS_AUTORESPOND, 
                         false 
                 );
                 theStream.send( autoMessage );
                 c.autoresponded=true;
 
                 c.addMessage(new Msg(Msg.MESSAGE_TYPE_SYSTEM, "local", SR.MS_AUTORESPOND, ""));
             }
         }
         
     }
 //#ifdef ANTISPAM
 //#     void tempMessageStore(Contact c, Msg message) {
 //#         c.addTempMessage(message);
 //#     }
 //#endif  
     public void blockNotify(int event, long ms) {
         if (!notifyReady(-111)) return;
         blockNotifyEvent=event;
         notifyReadyTime=System.currentTimeMillis()+ms;
     }
 
     public boolean notifyReady(int event) {
         if ((blockNotifyEvent==event ||
             (blockNotifyEvent==-111 && event<=7)) &&
            System.currentTimeMillis()<notifyReadyTime) return false;
         else return true;
     }
    
     public void playNotify(int event) {
         if (!notifyReady(event)) return;
 //#if DEBUG        
 //#         System.out.println("event: "+event);
 //#endif
         AlertCustomize ac=AlertCustomize.getInstance();
         
         int volume=ac.soundVol;
         int vibraLen=cf.vibraLen;
         String type, message;
                 
         //boolean blFlashEn=cf.blFlash;   // motorola e398 backlight bug
         
         switch (event) {
             case 0: //online
                 if (cf.notifySound) {
                     message=ac.soundOnline;
                     type=ac.soundOnlineType;
                     vibraLen=0;
                 } else {
                     message=null; type=null; vibraLen=0;
                 }
                 break;
             case 1: //chat
                 if (cf.notifySound) {
                     message=ac.soundOnline;
                     type=ac.soundOnlineType;
                     vibraLen=0;
                 } else {
                     message=null; type=null; vibraLen=0;
                 }
                 break;
             case 5: //offline
                 message=ac.soundOffline;
                 type=ac.soundOfflineType;
                 vibraLen=0;
                 break;
             case SOUND_FOR_VIP: //VIP
                 message=ac.soundVIP;
                 type=ac.soundVIPType;
                 break;
             case SOUND_MESSAGE: //message
                 message=ac.messagesnd;
                 type=ac.messageSndType;
                 break;
             case SOUND_FOR_CONFERENCE: //conference
                 message=ac.soundConference;
                 type=ac.soundConferenceType;
                 break;
             case SOUND_FOR_ME: //message for you
                 message=ac.soundForYou;
                 type=ac.soundForYouType;
                 break;
             case SOUND_START_UP: //startup
                 message=ac.soundStartUp;
                 type=ac.soundStartUpType;
                 vibraLen=0;
                 break;
             case SOUND_COMPOSING: //composing
                 message=ac.soundComposing;
                 type=ac.soundComposingType;
                 vibraLen=0;
                 break;
             case SOUND_OUTGOING: //Outgoing
                 message=ac.soundOutgoing;
                 type=ac.soundOutgoingType;
                 vibraLen=0;
                 break;
             default :
                 message="";
                 type="none";
                 vibraLen=0;
                 break;
         }
        
         int profile=cf.profile;
         if (profile==AlertProfile.AUTO) 
             profile=AlertProfile.ALL;
 
         EventNotify notify=null;
         
         switch (profile) {
             case AlertProfile.ALL:   notify=new EventNotify(display,    type,   message,    volume,     vibraLen/*,       blFlashEn*/); break;
             case AlertProfile.NONE:  notify=new EventNotify(display,    null,   null,       volume,     0/*,              false    */); break;
             case AlertProfile.VIBRA: notify=new EventNotify(display,    null,   null,       volume,     vibraLen/*,       blFlashEn*/); break;
             case AlertProfile.SOUND: notify=new EventNotify(display,    type,   message,    volume,     0/*,              blFlashEn*/); break;
         }
         if (notify!=null) 
             notify.startNotify();
         blockNotify(event, 2000);
     }
 
     private void focusToContact(final Contact c, boolean force) {
 	
 	Group g=c.getGroup();
 	if (g.collapsed) {
 	    g.collapsed=false;
 	    reEnumerator.queueEnum(c, force);
 	    //reEnumRoster();
 	} else {
 	    
 	    int index=vContacts.indexOf(c);
 	    if (index>=0) moveCursorTo(index);
 	}
     }
 
     public void beginConversation(String SessionId) { //todo: verify xmpp version
             new SASLAuth(sd.account, SessionId, this, theStream)
   //#if SASL_XGOOGLETOKEN
             .setToken(token)
   //#endif
             ;
     }
 
     public void connectionTerminated( Exception e ) {
         String error=null;
         setProgress(SR.MS_DISCONNECTED, 100);
          if( e != null ) {
             askReconnect(e);
         } else {
             try {
                 sendPresence(Presence.PRESENCE_OFFLINE, null);
             } catch (Exception e2) {
 //#if DEBUG
 //#                 e2.printStackTrace();
 //#endif
             }
          }
         redraw();
     }
 
     private void askReconnect(final Exception e) {
         String error;
         error=e.getClass().getName()+"\n"+e.getMessage();
 //#if DEBUG
 //#         e.printStackTrace();
 //#endif
          try {
              sendPresence(Presence.PRESENCE_OFFLINE, null);
         } catch (Exception e2) { }
 
         if (e instanceof SecurityException) { errorLog(error); return; }
         //if (e instanceof JabberStreamShutdownException) { errorLog(error); return; }
         if (reconnectCount>=maxReconnect) { errorLog(error); return; }
         
         reconnectCount++;
         String topBar="("+reconnectCount+"/"+maxReconnect+") Reconnecting";
         Msg m=new Msg(Msg.MESSAGE_TYPE_OUT, "local", topBar, error);
         messageStore(selfContact(), m);
         Stats.getInstance().save();
         new Reconnect(topBar, error, display);
 
      }
      public void doReconnect() {
         sendPresence(oldStatus.getImageIndex(), null);
      }
     
     public void eventOk(){
         super.eventOk();
         if (createMsgList()==null) {
             cleanupGroup();
             reEnumRoster();
         }
     }
     
     
     private Displayable createMsgList(){
         Object e=getFocusedObject();
         if (e instanceof Contact) {
             return new ContactMessageList((Contact)e,display);
         }
         return null;
     }
     
     protected void keyGreen(){
         if (!isLoggedIn()) return;
         Displayable pview=createMsgList();
         if (pview!=null) {
             Contact c=(Contact)getFocusedObject();
             ( me = new MessageEdit(display, c, c.msgSuspended) ).setParentView(pview);
             c.msgSuspended=null;
         }
         //reEnumRoster();
     }
     
     protected void keyClear(){
         if (isLoggedIn()) {
             Contact c=(Contact) getFocusedObject();
             try { 
                 boolean isContact=( getFocusedObject() instanceof Contact );
 //#ifndef WMUC
                 boolean isMucContact=( getFocusedObject() instanceof MucContact );
 //#else
 //#                 boolean isMucContact=false;
 //#endif
                 if (isContact && !isMucContact) {
                    yesnoAction=ACTION_DELETE; 
                    new YesNoAlert(display, SR.MS_DELETE_ASK, c.getNickJid(), this);
                 }
 //#ifndef WMUC
                 else if (isContact && isMucContact) {
                     ConferenceGroup mucGrp=(ConferenceGroup)c.getGroup();
                     String myNick=mucGrp.getSelfContact().getName();
                     MucContact mc=(MucContact) c;
                     new ConferenceQuickPrivelegeModify(display, mc, ConferenceQuickPrivelegeModify.KICK,myNick);
                 }
 //#endif 
             } catch (Exception e) { /* NullPointerException */ }
         }
     }
     
     public void touchLeftPressed(){
         new RosterMenu(display, getFocusedObject());
     }
     
     public void touchRightPressed(){
         new RosterItemActions(display, getFocusedObject(), -1);
     }
 
     public void keyPressed(int keyCode){
 //#ifdef NEW_MENU
 //#         if (keyCode==Config.SOFT_LEFT) {
 //#             new RosterMenu(display, getFocusedObject());
 //#             return;
 //#         }
 //#endif
        
         if (keyCode==Config.SOFT_RIGHT) {
             if (isLoggedIn()) 
                 new RosterItemActions(display, getFocusedObject(), -1);
                 return;
         }
         super.keyPressed(keyCode);
         
         Contact c=null;
         
         switch (keyCode) {
 //#ifdef POPUPS
 //#             case KEY_POUND:
 //#                 showInfo();
 //#                 return;
 //#endif
             case KEY_NUM1:
                 if (cf.collapsedGroups) { //collapse all groups
                     for (Enumeration e=groups.elements(); e.hasMoreElements();) {
                         Group grp=(Group)e.nextElement();
                         grp.collapsed=true;
                     }
                     reEnumRoster();
                 }
                 break;
             case KEY_NUM4:
                 super.pageLeft();
                 return;
             case KEY_NUM6:
                 super.pageRight();
                 return;
 //#ifdef AUTOSTATUS
 //#             case SE_FLIPCLOSE_JP6:
 //#             case SIEMENS_FLIPCLOSE:
 //#             case MOTOROLA_FLIP:
 //#                 if (cf.phoneManufacturer!=Config.SONYE) { //workaround for SE JP6 - enabling vibra in closed state
 //#                     display.setCurrent(null);
 //#                     try {
 //#                         Thread.sleep(300);
 //#                     } catch (Exception ex) {}
 //#                     display.setCurrent(this);
 //#                 }
 //#if DEBUG
 //#             System.out.println("Flip closed");
 //#endif
 //#                 if (cf.autoAwayType==Config.AWAY_LOCK) 
 //#                     if (!autoAway) 
 //#                         autostatus.setTimeEvent(cf.autoAwayDelay* 60*1000);
 //#                 break;
 //#endif
             case KEY_NUM0:
                 cleanMarks();
                 redraw();
 //#ifndef WSYSTEMGC
                 System.gc();
 //#endif
                 if (messageCount==0) return;
                 Object atcursor=getFocusedObject();
                 if (atcursor instanceof Contact) 
                     c=(Contact)atcursor;
                 else 
                     c=(Contact)hContacts.firstElement();
 
                 Enumeration i=hContacts.elements();
 
                 int pass=0; //
                 while (pass<2) {
                     if (!i.hasMoreElements()) i=hContacts.elements();
                     Contact p=(Contact)i.nextElement();
                     if (pass==1) if (p.getNewMsgsCount()>0) { 
                         focusToContact(p, true);
                         setRotator();
                         break; 
                     }
                     if (p==c) pass++;
                 }
                 break;
             case KEY_NUM3:
                 searchGroup(-1);
                 setRotator();
                 break;
             case KEY_NUM9:
                 searchGroup(1);
                 setRotator();
                 break;
             case KEY_STAR:
                 if (cf.ghostMotor) {
                     // backlight management
                     blState=(blState==1)? Integer.MAX_VALUE : 1;
                     display.flashBacklight(blState);
                 }
                 break;
         }
 //#ifdef AUTOSTATUS
 //#         userActivity();
 //#endif
      }
  
     protected void keyRepeated(int keyCode) {
         super.keyRepeated(keyCode);
         if (kHold==keyCode) return;
         kHold=keyCode;
         
         if (keyCode==cf.keyLock) {
 //#ifdef AUTOSTATUS
 //#             if (cf.autoAwayType==Config.AWAY_LOCK) {
 //#                 if (!autoAway) {
 //#                     //keyLockState=true;
 //#                     autoAway=true;
 //#                     if (cf.setAutoStatusMessage) {
 //#                         sendPresence(Presence.PRESENCE_AWAY, "Auto Status on KeyLock since %t");
 //#                     } else {
 //#                         sendPresence(Presence.PRESENCE_AWAY, null);
 //#                     }
 //#                 }
 //#             }
 //#endif
             new SplashScreen(display, getMainBarItem(), cf.keyLock);
             return;
         } else if (keyCode==cf.keyVibra || keyCode==MOTOE680_FMRADIO /* TODO: redefine keyVibra*/) {
             // swap profiles
             int profile=cf.profile;
             cf.profile=(profile==AlertProfile.VIBRA)?cf.lastProfile : AlertProfile.VIBRA;
             cf.lastProfile=profile;
             
             updateMainBar();
             redraw();
             return;
         } else if (keyCode==KEY_NUM0) {
             cf.showOfflineContacts=!cf.showOfflineContacts;
             reEnumRoster();
             return;
         }
 //#ifndef WMUC
         else if (keyCode==KEY_NUM1 && isLoggedIn()) new Bookmarks(display, null);
 //#endif
        	else if (keyCode==KEY_NUM3) new ActiveContacts(display, null);
        	else if (keyCode==KEY_NUM4) new ConfigForm(display);
         else if (keyCode==KEY_NUM6) {
             fullMode=VirtualList.isbottom;
             cf.isbottom=VirtualList.isbottom=(fullMode+1)%7;
             cf.saveToStorage();
         } else if (keyCode==KEY_NUM7){
             new RosterToolsMenu(display);
         }
         
         else if (keyCode==KEY_NUM9) {
             if (cf.allowMinimize)
                 BombusMod.getInstance().hideApp(true);
             else if (cf.phoneManufacturer==Config.SIEMENS2)//SIEMENS: MYMENU call. Possible Main Menu for capable phones
                  try {
                       BombusMod.getInstance().platformRequest("native:ELSE_STR_MYMENU");
                  } catch (Exception e) { }     
             else if (cf.phoneManufacturer==Config.SIEMENS)//SIEMENS-NSG: MYMENU call. Possible Native Menu for capable phones
                  try {
                     BombusMod.getInstance().platformRequest("native:NAT_MAIN_MENU");
                  } catch (Exception e) { }   
         }
     }
     
     
 //#ifdef AUTOSTATUS
 //#     private void userActivity() {
 //#         if (cf.autoAwayType==Config.AWAY_IDLE) {
 //#             if (!autoAway) {
 //#                 autostatus.setTimeEvent(cf.autoAwayDelay* 60*1000);
 //#                 return;
 //#             }
 //#         } else {
 //#             return;
 //#         }
 //#         autostatus.setTimeEvent(0);
 //#         setAutoStatus(Presence.PRESENCE_ONLINE);
 //#     }
 //#     
 //#     public void messageActivity() {
 //#         if (cf.autoAwayType==Config.AWAY_MESSAGE) {
 //#              if (myStatus.getImageIndex()<2)
 //#                 autostatus.setTimeEvent(cf.autoAwayDelay* 60*1000);
 //#              else if (!autoAway) 
 //#                 autostatus.setTimeEvent(0);
 //#         }
 //#     }
 //#endif
     
     
 //#ifdef POPUPS
 //#     public void showInfo() {
 //#         VirtualList.popup.next();
 //#         if (getFocusedObject() instanceof Group || getFocusedObject() instanceof ConferenceGroup)
 //#             return;
 //#         
 //#         setWobbler(1, (Contact) null, null);
 //#     }
 //# 
 //#     public void setWobbler(int type, Contact contact, String info) {
 //#         if (info==null) {
 //#             StringBuffer mess=new StringBuffer();
 //#             boolean isContact=(getFocusedObject() instanceof Contact);
 //#             Contact cntact=(Contact)getFocusedObject();
 //#ifndef WMUC
 //#             boolean isMucContact=(getFocusedObject() instanceof MucContact);
 //#             if (isMucContact) {
 //#                 MucContact mucContact=(MucContact)getFocusedObject();
 //#                 
 //#                 if (mucContact.origin==Contact.ORIGIN_GROUPCHAT) // dont show info for confContact
 //#                     return;
 //#                 
 //#                 String jid=(mucContact.realJid==null)?"":"jid: "+mucContact.realJid+"\n";
 //#                 String aff=mucContact.affiliation;
 //#                 String role=mucContact.role;
 //#                 mess.append(jid);
 //#                 if (aff!=null || aff!="none")
 //#                     mess.append(aff);
 //# 
 //#                 if (role!=null)
 //#                     if (aff!=null || aff!="none")
 //#                         mess.append("/"+role);
 //#                     else 
 //#                         mess.append(role);
 //#                 
 //#                 jid=null;
 //#                 aff=null;
 //#                 role=null;
 //#             } else {
 //#endif
 //#                 mess.append("jid: "+cntact.bareJid);
 //#                 mess.append(cntact.jid.getResource());
 //#                 mess.append("\n"+SR.MS_SUBSCRIPTION+": "+cntact.subscr);
 //#                 
 //#                 if (cntact.mood!=null) {
 //#                     mess.append("\n");
 //#                     mess.append(SR.MS_USER_MOOD);
 //#                     mess.append(": ");
 //#                     mess.append(cntact.getUserMoodLocale());
 //#                     if (cntact.getUserMoodText()!="") {
 //#                         mess.append(" (");
 //#                         mess.append(cntact.getUserMoodText());
 //#                         mess.append(")");
 //#                     }
 //#                 }
 //#ifndef WMUC
 //#             }
 //#endif
 //#             mess.append((cntact.getJ2J()!=null)?"\nJ2J: "+cntact.getJ2J():"");
 //#             mess.append((cntact.hasCaps)?"\nUse: "+cntact.capsNode:"");
 //#             mess.append((cntact.capsVer!=null)?"#"+cntact.capsVer:"");
 //#             if (cntact.statusString!=null) {
 //#                 mess.append("\n");
 //#                 mess.append(SR.MS_STATUS);
 //#                 mess.append(": ");
 //#                 mess.append(cntact.statusString);
 //#             }
 //#             
 //#             VirtualList.setWobble(1, (Contact) null, mess.toString());
 //#             mess=null;
 //#         } else {
 //#             VirtualList.setWobble(type, contact, info);
 //#         }
 //# 
 //#         redraw();
 //#     }
 //#endif
     
     public void logoff(String mess){
         if (isLoggedIn()) {
             try {
                 ExtendedStatus es=StatusList.getInstance().getStatus(Presence.PRESENCE_OFFLINE);
                 if (mess==null)
                     mess=es.getMessage();
                 sendPresence(Presence.PRESENCE_OFFLINE, mess);
             } catch (Exception e) { }
         }
         try {
              Stats.getInstance().save();
         } catch (Exception e) { }
     }
 
     public void quit() {
 //#ifdef AUTOSTATUS
 //#         if (cf.autoAwayType!=Config.AWAY_OFF)
 //#             autostatus.destroyTask();
 //#endif
 //#ifdef SE_LIGHT
 //#         if (cf.phoneManufacturer==Config.SONYE)
 //#             selight.destroyTask();
 //#endif
         destroyView();
         logoff(null);
 
         BombusMod.getInstance().notifyDestroyed();
     }
    
     public void commandAction(Command c, Displayable d){
 //#ifdef AUTOSTATUS
 //#         userActivity();
 //#endif
         if (c==cmdActions) { cmdActions(); }
         else if (c==cmdMinimize) { cmdMinimize();  }
         else if (c==cmdActiveContacts) { cmdActiveContacts(); }
         else if (c==cmdAccount){ cmdAccount(); }
         else if (c==cmdStatus) { cmdStatus(); }
         else if (c==cmdAlert) { cmdAlert(); }
 //#ifdef ARCHIVE
 //# 	else if (c==cmdArchive) { cmdArchive(); }
 //#endif
         else if (c==cmdInfo) { cmdInfo(); }
         else if (c==cmdTools) { cmdTools(); }
         else if (c==cmdCleanAllMessages) { cmdCleanAllMessages(); }     
 //#ifndef WMUC
         else if (c==cmdConference) { cmdConference(); }
 //#endif
         else if (c==cmdQuit) { cmdQuit(); }
         else if (c==cmdAdd) { cmdAdd(); }
     }
     
 //menu actions
     public void cmdQuit() { 
         if (cf.queryExit) {
             yesnoAction=ACTION_QUIT; 
             new YesNoAlert(display, SR.MS_QUIT_ASK, SR.MS_SURE_QUIT, this); 
         } else {
             quit();
         }
     }
     public void cmdMinimize() { BombusMod.getInstance().hideApp(true);  }
     public void cmdActiveContacts() { new ActiveContacts(display, null); }
     public void cmdAccount(){ new AccountSelect(display, false); }
     public void cmdStatus() { reconnectCount=0; new StatusSelect(display, null); }
     public void cmdAlert() { new AlertProfile(display); }
 //#ifdef ARCHIVE
 //#     public void cmdArchive() { new ArchiveList(display, -1, 1); }
 //#endif
     public void cmdInfo() { new Info.InfoWindow(display); }
     public void cmdTools() { new RosterToolsMenu(display); }
     public void cmdCleanAllMessages() { cleanupAllHistories(); }    
 //#ifdef POPUPS
 //#     public void cmdClearPopups() { VirtualList.popup.clear(); }
 //#endif
 //#ifdef MOOD
 //#    public void cmdUserMood() { if (isLoggedIn()) new MoodSelect(display); }
 //#endif 
 //#ifndef WMUC
    public void cmdConference() { if (isLoggedIn()) new Bookmarks(display, null); }
 //#endif
    public void cmdActions() { if (isLoggedIn()) try { new RosterItemActions(display, getFocusedObject(), -1); } catch (Exception e) { /* NullPointerException */ }}
    
    public void cmdAdd() {
        if (isLoggedIn()) {
             Object o=getFocusedObject();
             Contact cn=null;
             if (o instanceof Contact) {
                 cn=(Contact)o;
                 if (cn.getGroupType()!=Groups.TYPE_NOT_IN_LIST && cn.getGroupType()!=Groups.TYPE_SEARCH_RESULT) cn=null;
             }
 //#ifndef WMUC
             if (o instanceof MucContact) cn=(Contact)o;
 //#endif
             new ContactEdit(display, cn);
        }
    }
 //menu actions
 //#ifndef WMUC
     public void reEnterRoom(Group group) {
 	ConferenceGroup confGroup=(ConferenceGroup)group;
         String confJid=confGroup.getSelfContact().getJid();
         String name=confGroup.desc;
 	new ConferenceForm(display, name, confJid, confGroup.password, false);
     }
     
     public void leaveRoom(Group group){
 	//Group group=groups.getGroup(index);
 	ConferenceGroup confGroup=(ConferenceGroup)group;
 	Contact myself=confGroup.getSelfContact();
 	confGroup.getConference().commonPresence=false; //disable reenter after reconnect
         
          sendPresence(myself.getJid(), "unavailable", null, true);
 	//roomOffline(group);
         for (Enumeration e=hContacts.elements(); e.hasMoreElements();) {
             Contact contact=(Contact)e.nextElement();
             if (contact.inGroup(group)) contact.setStatus(Presence.PRESENCE_OFFLINE);
         }
     }
     
     public void roomOffline(final Group group) {
          for (Enumeration e=hContacts.elements(); e.hasMoreElements();) {
              Contact contact=(Contact)e.nextElement();
             if (contact.inGroup(group)) contact.setStatus(Presence.PRESENCE_OFFLINE);
          }
     }
 //#endif
     protected void showNotify() { 
         super.showNotify(); 
         countNewMsgs(); 
 //#ifdef AUTOSTATUS
 //#         if (cf.autoAwayType==Config.AWAY_IDLE) {
 //#             if (!autostatus.isAwayTimerSet()) {
 //#                 if (!autoAway) autostatus.setTimeEvent(cf.autoAwayDelay* 60*1000);
 //#             }
 //#         }
 //#endif
     }
     
     protected void hideNotify() {
         super.hideNotify();
 //#ifdef AUTOSTATUS
 //#         if (cf.autoAwayType==Config.AWAY_IDLE) if (kHold==0) autostatus.setTimeEvent(0);
 //#endif
     }
     
     private void searchGroup(int direction){
 	synchronized (vContacts) {
 	    int size=vContacts.size();
 	    int pos=cursor;
 	    int count=size;
 	    try {
 		while (count>0) {
 		    pos+=direction;
 		    if (pos<0) pos=size-1;
 		    if (pos>=size) pos=0;
 		    if (vContacts.elementAt(pos) instanceof Group) break;
 		}
 		moveCursorTo(pos);
 	    } catch (Exception e) { }
 	}
     }
     
     public void ActionConfirmed() {
         switch (yesnoAction) {
             case ACTION_DELETE:
                 deleteContact((Contact)getFocusedObject());
                 break;
             case ACTION_QUIT:
                 quit();
         }
 
     }
 
     public void deleteContact(Contact c) {
 	for (Enumeration e=hContacts.elements();e.hasMoreElements();) {
 	    Contact c2=(Contact)e. nextElement();
 	    if (c.jid.equals(c2. jid,false)) {
 			c2.setStatus(Presence.PRESENCE_TRASH);
 			c2.offline_type=Presence.PRESENCE_TRASH;
 	    }
 	}
 	
 	if (c.getGroupType()==Groups.TYPE_NOT_IN_LIST) {
 	    hContacts.removeElement(c);
             countNewMsgs();
 	    reEnumRoster();
 	} else {
             theStream.send(new IqQueryRoster(c.getBareJid(),null,null,"remove"));
             
             JabberDataBlock removeIq=new Iq(c.getJid(), Iq.TYPE_SET, "push");
             JabberDataBlock query=removeIq.addChildNs("query", "jabber:iq:roster");
             JabberDataBlock item= query.addChild("item",null);
             item.setAttribute("subscription", "remove");
             item.setAttribute("jid", c.getBareJid());
             theStream.send(removeIq);
         }
     }
    
     
     public void setQuerySign(boolean requestState) {
         querysign=requestState;
         updateMainBar();
     }
     
     /**
      * store cotnact on server
      */
     public void storeContact(String jid, String name, String group, boolean askSubscribe){
         theStream.send(new IqQueryRoster(jid, name, group, null));
         if (askSubscribe) theStream.send(new Presence(jid,"subscribe"));
     }
 
     public void loginMessage(String msg) {
         setProgress(msg, 42);
     }
 
     private class ReEnumerator implements Runnable{
 
         Thread thread;
         int pendingRepaints=0;
 	boolean force;
 	
 	Object desiredFocus;
         
         public void queueEnum(Object focusTo, boolean force) {
 	    desiredFocus=focusTo;
 	    this.force=force;
 	    queueEnum();
         }
 	
         synchronized public void queueEnum() {
             pendingRepaints++;
             if (thread==null) (thread=new Thread(this)).start();
         }
         
         public void run(){
             try {
                 while (pendingRepaints>0) {
                     //System.out.println(pendingRepaints);
                     pendingRepaints=0;
                     
                     int locCursor=cursor;
                     Object focused=(desiredFocus==null)?getFocusedObject():desiredFocus;
 		    desiredFocus=null;
                     
                     Vector tContacts=new Vector(vContacts.size());
                     
                     Enumeration e;
                     //int i;
                     groups.resetCounters();
                     
                     synchronized (hContacts) {
                         for (e=hContacts.elements();e.hasMoreElements();){
                             Contact c=(Contact)e.nextElement();
                             boolean online=c.status<5;
                             // group counters
                             Group grp=c.getGroup();
 			    grp.addContact(c);
                         }
                     }
                     
                     // self-contact group
                     Group selfContactGroup=groups.getGroup(Groups.TYPE_SELF);
                     selfContactGroup.visible=(cf.selfContact || selfContactGroup.tonlines>1 || selfContactGroup.unreadMessages>0 );
                     
                     // hiddens
                     groups.getGroup(Groups.TYPE_IGNORE).visible= cf.ignore ;
                     
                     // transports
                     Group transpGroup=groups.getGroup(Groups.TYPE_TRANSP);
                     transpGroup.visible= (cf.showTransports || transpGroup.unreadMessages>0);
                     
                     // always visible
                     Group visibleGroup=groups.getGroup(Groups.TYPE_VISIBLE);
                     visibleGroup.visible=true;
                     
                     // adding groups
                     for (int i=0; i<groups.getCount(); i++)
                         groups.addToVector(tContacts,i);
                     
                     vContacts=tContacts;
                     
                     StringBuffer onl=new StringBuffer();
                     onl.append("(");
                     onl.append(groups.getRosterOnline());
                     onl.append("/");
                     onl.append(groups.getRosterContacts());
                     onl.append(")");
                     setRosterMainBar(onl.toString());
                     onl=null;
                     
                     if (cursor<0) cursor=0;
 
                     if ( locCursor==cursor && focused!=null ) {
                         int c=vContacts.indexOf(focused);
                         if (c>=0) moveCursorTo(c);
 			force=false;
                     }
 
                     focusedItem(cursor);
                     redraw();
                 }
             } catch (Exception e) {
                 //e.printStackTrace();
             }
             thread=null;
         }
     }
 	
     public void setMyJid(Jid myJid) {
         this.myJid = myJid;
     }
 //#ifdef AUTOSTATUS
 //#     public void setAutoAway() {
 //#         if (!autoAway) {
 //#             oldStatus=myStatus;
 //#             if (myStatus.getImageIndex()==0 || myStatus.getImageIndex()==1) {
 //#                 autoAway=true;
 //#                 if (cf.setAutoStatusMessage) {
 //#                     sendPresence(Presence.PRESENCE_AWAY, SR.MS_AUTO_AWAY);
 //#                 } else {
 //#                     sendPresence(Presence.PRESENCE_AWAY, null);
 //#                 }
 //#             }
 //#         }
 //#     }
 //# 
 //#     public void setAutoXa() {
 //#         if (autoAway && !autoXa) {
 //#             autoXa=true;
 //#             if (cf.setAutoStatusMessage) {
 //#                 sendPresence(Presence.PRESENCE_XA, SR.MS_AUTO_XA);
 //#             } else {
 //#                 sendPresence(Presence.PRESENCE_XA, null);
 //#             }
 //#         }
 //#     }
 //#   
 //#     public void setAutoStatus(int status) {
 //#         if (!isLoggedIn()) return;
 //#         if (status==Presence.PRESENCE_ONLINE && autoAway) {
 //#             autoAway=false;
 //#             sendPresence(Presence.PRESENCE_ONLINE, null);
 //#             return;
 //#         }
 //#         if (status!=Presence.PRESENCE_ONLINE && myStatus.getImageIndex()==Presence.PRESENCE_ONLINE && !autoAway) {
 //#             autoAway=true;
 //#             if (cf.setAutoStatusMessage) {
 //#                 sendPresence(Presence.PRESENCE_AWAY, "Auto Status on KeyLock since %t");
 //#             } else {
 //#                 sendPresence(Presence.PRESENCE_AWAY, null);
 //#             }
 //#         }
 //#     }
 //#endif
 //#ifndef WMUC
     public void mucReconnect() {
         Enumeration e;
         
         synchronized (hContacts) {
             for (e=hContacts.elements();e.hasMoreElements();){
                 Contact c=(Contact)e.nextElement();
                 
                 if (c.origin==Contact.ORIGIN_GROUPCHAT) {
                     if (c.getGroup() instanceof ConferenceGroup) {
                         ConferenceGroup mucGrp=(ConferenceGroup)c.getGroup();
                         MucContact self=mucGrp.getSelfContact();
                         if (self.status>=Presence.PRESENCE_OFFLINE) {
                             confJoin(mucGrp.getConference().bareJid);
 //#if DEBUG 
 //#                             System.out.println("reconnect "+mucGrp.getConference().bareJid);
 //#endif
                         }
                     }
                 }
                 
             }
         }
     }
     
     public void confJoin(String conference){
         ConferenceGroup grp=initMuc(conference, "");
         
         JabberDataBlock x=new JabberDataBlock("x", null, null);
         x.setNameSpace("http://jabber.org/protocol/muc");
         
         if (grp.password.length()!=0) {
             // adding password to presence
             x.addChild("password", grp.password);
         }
         
         JabberDataBlock history=x.addChild("history", null);
         history.setAttribute("maxstanzas", String.valueOf(cf.confMessageCount));
         history.setAttribute("maxchars","32768");
         try {
             long last=grp.getConference().lastMessageTime;
             long delay= ( grp.conferenceJoinTime - last ) /1000 ;
             if (last!=0) history.setAttribute("seconds",String.valueOf(delay)); // todo: change to since
         } catch (Exception e) {}
 
         sendPresence(conference, null, x, false);
         reEnumRoster();
     } 
 //#endif
     public void cleanMarks() {
       synchronized(hContacts) {
         for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
           Contact c=(Contact)e.nextElement();
           c.setIncoming(Contact.INC_NONE);
         }
       }
     }
     
     
     private void getKeys()
     {
         int pm=cf.phoneManufacturer;
         if (pm==Config.SIEMENS || pm==Config.SIEMENS2) {
              Config.SOFT_LEFT=-1;
              Config.SOFT_RIGHT=-4;
              return;
         }
 
         if (pm==Config.WINDOWS) {
              Config.SOFT_LEFT=40;
              Config.SOFT_RIGHT=41;
              return;     
         }
         if (pm==Config.NOKIA || pm==Config.SONYE) {
             Config.SOFT_LEFT=-6;
             Config.SOFT_RIGHT=-7;
             return;
         } 
         
         if (pm==Config.MOTOEZX) {
             Config.SOFT_LEFT=-21;
             Config.SOFT_RIGHT=-22;
             return;
         } 
         
         try {
             // Set Motorola specific keycodes
             Class.forName("com.motorola.phonebook.PhoneBookRecord");
             if (getKeyName(-21).toUpperCase().indexOf("SOFT")>=0) {
                 Config.SOFT_LEFT=-21;
                 Config.SOFT_RIGHT=-22;
             } else {
                 Config.SOFT_LEFT=21;
                 Config.SOFT_RIGHT=22;
             }
         } catch (ClassNotFoundException ignore2) {
             try {   
                 if (getKeyName(21).toUpperCase().indexOf("SOFT")>=0) {
                     Config.SOFT_LEFT=21;
                     Config.SOFT_RIGHT=22;
                 }
                 if (getKeyName(-6).toUpperCase().indexOf("SOFT")>=0) {
                     Config.SOFT_LEFT=-6;
                     Config.SOFT_RIGHT=-7;
                 }
             } catch(Exception e) {}
 
             for (int i=-127;i<127;i++) {
             // run thru all the keys
                 try {
                    if (getKeyName(i).toUpperCase().indexOf("SOFT")>=0) {         // Check for "SOFT" in name description
                       if (getKeyName(i).indexOf("1")>=0) Config.SOFT_LEFT=i;         // check for the 1st softkey
                       if (getKeyName(i).indexOf("2")>=0) Config.SOFT_RIGHT=i;         // check for 2nd softkey
                    }
                 } catch(Exception e){ }
             }
         }
     }
      
     public void deleteGroup(Group deleteGroup) {
         for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
             Contact cr=(Contact)e.nextElement();
             if (cr.getGroup()==deleteGroup)
                 deleteContact(cr);                
         }
     }
     
 //#ifdef POPUPS
 //#     public void showStats() {
 //#         StringBuffer str= new StringBuffer(SR.MS_STARTED+startTime);
 //#         Stats stats=Stats.getInstance();
 //#         str.append("\n");
 //#         str.append(SR.MS_TRAFFIC_STATS);
 //#         str.append("\n");
 //#         str.append(SR.MS_ALL);
 //#         str.append(stats.getSessionsCount());
 //#         str.append(SR.MS_CONN);
 //# 
 //#         str.append(strconv.getSizeString(stats.getAllTraffic()));
 //# 
 //#         str.append("\n");
 //#         str.append(SR.MS_PREVIOUS);
 //#         str.append(strconv.getSizeString(stats.getLatest()));
 //# 
 //#         str.append("\n");
 //#         str.append(SR.MS_CURRENT);
 //#         str.append(strconv.getSizeString(Stats.getGPRS()));
 //# 
 //#         if (isLoggedIn())
 //#             str.append(theStream.getStreamStats());
 //# 
 //#         VirtualList.setWobble(1, (Contact) null, str.toString());
 //#         str=null;
 //#     }
 //#endif
     
 //#if SASL_XGOOGLETOKEN
     public void sendGmailReq() {
         JabberDataBlock iq=new Iq(null, Iq.TYPE_GET, "mail-request");
         JabberDataBlock query=iq.addChildNs("query", "google:mail:notify");
         theStream.send(iq);
     }
 //#endif
 }
 
 
 

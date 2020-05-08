 package com.tomclaw.mandarin.molecus;
 
 import com.tomclaw.mandarin.core.Handler;
 import com.tomclaw.mandarin.core.Queue;
 import com.tomclaw.mandarin.core.QueueAction;
 import com.tomclaw.mandarin.core.Settings;
 import com.tomclaw.mandarin.main.BuddyList;
 import com.tomclaw.mandarin.main.MidletMain;
 import com.tomclaw.mandarin.main.RoomEditFrame;
 import com.tomclaw.tcuilite.ChatItem;
 import com.tomclaw.tcuilite.Check;
 import com.tomclaw.tcuilite.PaneObject;
 import com.tomclaw.tcuilite.PopupItem;
 import com.tomclaw.tcuilite.Soft;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 import com.tomclaw.utils.StringUtil;
 import com.tomclaw.xmlgear.XmlSpore;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class Mechanism {
 
   public static final int OPERATION_REMOVE = 0x01;
   public static final int OPERATION_EDIT = 0x02;
   public static final int OPERATION_ADD = 0x03;
 
   /**
    * Login transactions method
    */
   public static void accountLogin( final int statusIndex ) {
     new Thread() {
       public void run() {
         LogUtil.outMessage( "Login thread started." );
         try {
           /** Obtain session object **/
           final Session session = AccountRoot.getSession();
           /** Connecting to the specified in AccountRoot host **/
           session.establishConnection( AccountRoot.getRemoteHost(), AccountRoot.getRemotePort(), AccountRoot.getUseSsl() );
           session.start();
           /** Creating xml spore **/
           XmlSpore xmlSpore = new XmlSpore() {
             public void onRun() throws Throwable {
               if ( AccountRoot.getUseSasl() ) {
                 /** SASL authentication method **/
               } else {
                 /** Unsecure authentication method **/
                 /** Starting stream **/
                 TemplateCollection.sendStartXmlStream( this,
                         AccountRoot.getRemoteHost(), AccountRoot.getFullJid() );
                 /** Receiving login form from server **/
                 String cookie = TemplateCollection.sendLoginFormRequest( this,
                         AccountRoot.getRemoteHost() );
                 QueueAction queueAction = new QueueAction() {
                   public void actionPerformed( final Hashtable params ) {
                     /** Login form received **/
                     String errorCause = ( String ) params.get( "ERROR_CAUSE" );
                     /** Checking for error **/
                     if ( errorCause == null ) {
                       /** No error **/
                       LogUtil.outMessage( "Login form received" );
                       params.put( "username", AccountRoot.getUserName() );
                       params.put( "password", AccountRoot.getPassword() );
                       params.put( "resource", AccountRoot.getResource() );
                       /** Creating xml spore **/
                       XmlSpore xmlSpore = new XmlSpore() {
                         public void onRun() throws Throwable {
                           /** Sending login form **/
                           String cookie = TemplateCollection.sendLoginForm( this, params );
                           QueueAction queueAction = new QueueAction() {
                             public void actionPerformed( Hashtable params ) {
                               /** Login result received **/
                               LogUtil.outMessage( "Login result received" );
                               String errorCause = ( String ) params.get( "ERROR_CAUSE" );
                               /** Checking for error **/
                               if ( errorCause == null ) {
                                 /** No errors **/
                                 LogUtil.outMessage( "Login successfull" );
                                 /** Creating xml spore **/
                                 XmlSpore xmlSpore = new XmlSpore() {
                                   public void onRun() throws Throwable {
                                     /** Sending service discovery request **/
                                     String cookie = TemplateCollection.sendServiceItemsDiscoveryRequest(
                                             this, AccountRoot.getServicesHost(),
                                             AccountRoot.getFullJid(), null );
                                     QueueAction queueAction = new QueueAction() {
                                       public void actionPerformed( Hashtable params ) {
                                         /** Items received **/
                                         String errorCause = ( String ) params.get( "ERROR_CAUSE" );
                                         /** Checking for error **/
                                         if ( errorCause == null ) {
                                           LogUtil.outMessage( "Items received" );
                                           Handler.setServices( params );
                                           /** Creating xml spore **/
                                           XmlSpore xmlSpore = new XmlSpore() {
                                             public void onRun() throws Throwable {
                                               /** Sending login form **/
                                               String cookie = TemplateCollection.sendBookmarksRequest( this );
                                               QueueAction queueAction = new QueueAction() {
                                                 public void actionPerformed( Hashtable params ) {
                                                   /** Login result received **/
                                                   LogUtil.outMessage( "Bookmarks received" );
                                                   String errorCause = ( String ) params.get( "ERROR_CAUSE" );
                                                   /** Checking for error **/
                                                   if ( errorCause == null ) {
                                                     /** No errors **/
                                                     LogUtil.outMessage( "Bookmarks request successfull" );
                                                     Handler.setBookmarks( params );
                                                     /** Creating xml spore **/
                                                     XmlSpore xmlSpore = new XmlSpore() {
                                                       public void onRun() throws Throwable {
                                                         /** Sending roster request **/
                                                         String cookie = TemplateCollection.sendRosterRequest( this, AccountRoot.getFullJid() );
                                                         QueueAction queueAction = new QueueAction() {
                                                           public void actionPerformed( Hashtable params ) {
                                                             /** Roster received **/
                                                             LogUtil.outMessage( "Roster received" );
                                                             Handler.setRoster( params );
                                                             /** Creating xml spore **/
                                                             XmlSpore xmlSpore = new XmlSpore() {
                                                               public void onRun() throws Throwable {
                                                                 /** Sending presence info **/
                                                                 String cookie = TemplateCollection.sendPresence(
                                                                         this, AccountRoot.getFullJid(),
                                                                         null, null,
                                                                         StatusUtil.getStatus( statusIndex ),
                                                                         null, AccountRoot.getPriority(), true );
                                                                 QueueAction queueAction = new QueueAction() {
                                                                   public void actionPerformed( Hashtable params ) {
                                                                     /** Self presence received **/
                                                                     LogUtil.outMessage( "Self presence received" );
                                                                     Handler.connectedEvent();
                                                                   }
                                                                 };
                                                                 queueAction.setCookie( cookie );
                                                                 Queue.pushQueueAction( queueAction );
                                                               }
                                                             };
                                                             /** Releasing xml spore **/
                                                             session.getSporedStream().releaseSpore( xmlSpore );
                                                           }
                                                         };
                                                         queueAction.setCookie( cookie );
                                                         Queue.pushQueueAction( queueAction );
                                                       }
                                                     };
                                                     /** Releasing xml spore **/
                                                     session.getSporedStream().releaseSpore( xmlSpore );
                                                   } else {
                                                     /** Showing error **/
                                                     Handler.showError( errorCause );
                                                   }
                                                 }
                                               };
                                               queueAction.setCookie( cookie );
                                               Queue.pushQueueAction( queueAction );
 
                                             }
                                           };
                                           /** Releasing xml spore **/
                                           session.getSporedStream().releaseSpore( xmlSpore );
                                         } else {
                                           /** Showing error **/
                                           Handler.showError( errorCause );
                                         }
                                       }
                                     };
                                     queueAction.setCookie( cookie );
                                     Queue.pushQueueAction( queueAction );
                                   }
                                 };
                                 /** Releasing xml spore **/
                                 session.getSporedStream().releaseSpore( xmlSpore );
                               } else {
                                 /** Showing error **/
                                 Handler.showError( errorCause );
                               }
                             }
                           };
                           queueAction.setCookie( cookie );
                           Queue.pushQueueAction( queueAction );
                         }
                       };
                       /** Releasing xml spore **/
                       session.getSporedStream().releaseSpore( xmlSpore );
                     } else {
                       /** Showing error **/
                       Handler.showError( errorCause );
                     }
                   }
                 };
                 queueAction.setCookie( cookie );
                 Queue.pushQueueAction( queueAction );
               }
             }
           };
           /** Releasing xml spore **/
           session.getSporedStream().releaseSpore( xmlSpore );
         } catch ( Throwable ex ) {
           Handler.showError( "IO_EXCEPTION" );
         }
       }
     }.start();
   }
 
   public static void setStatus( final int statusIndex ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         TemplateCollection.sendPresence(
                 this, AccountRoot.getFullJid(),
                 null, null,
                 StatusUtil.getStatus( statusIndex ),
                 null, AccountRoot.getPriority(), true );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void accountLogout() {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         TemplateCollection.sendPresence(
                 this, AccountRoot.getFullJid(),
                 null, "unavailable",
                 null, null, AccountRoot.getPriority(), false );
       }
 
       public void onError( Throwable ex ) {
         LogUtil.outMessage( "Error while sending unavailabe presence: "
                 + ex.getMessage() );
       }
 
       public void onResult() {
         /** Halting connection **/
         session.disconnect();
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   /**
    * Inspecting specified service
    * @param jid 
    */
   public static void inspectService( final ServiceItem serviceItem ) {
     /** Creating new item **/
     final PopupItem popupItem = serviceItem.getNewPopupItem( false );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending service discovery request **/
         String cookie = TemplateCollection.sendServiceInfoDiscoveryRequest(
                 this, serviceItem.getJid(), AccountRoot.getFullJid(), null );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Info received" );
               /** Checking for supported features **/
               if ( params.containsKey( "jabber:iq:register" ) ) {
                 /** Registration supported **/
                 LogUtil.outMessage( "Registration supported" );
                 Handler.appendRegisterEvent( popupItem, serviceItem );
               }
               if ( params.containsKey( "http://jabber.org/protocol/commands" ) ) {
                 /** Commands supported **/
                 LogUtil.outMessage( "Commands supported" );
                 /** Creating xml spore **/
                 XmlSpore xmlSpore = new XmlSpore() {
                   public void onRun() throws Throwable {
                     /** Sending service discovery request **/
                     String cookie = TemplateCollection.sendServiceItemsDiscoveryRequest(
                             this,
                             serviceItem.getJid(), AccountRoot.getFullJid(), "http://jabber.org/protocol/commands" );
                     QueueAction queueAction = new QueueAction() {
                       public void actionPerformed( Hashtable params ) {
                         /** Info received **/
                         String errorCause = ( String ) params.get( "ERROR_CAUSE" );
                         /** Checking for error **/
                         if ( errorCause == null ) {
                           Handler.appendCommands( popupItem, params );
                           /** Commands setted up, showing popup **/
                           Handler.showMainFrameElementPopup( popupItem );
                         }
                       }
                     };
                     queueAction.setCookie( cookie );
                     Queue.pushQueueAction( queueAction );
                   }
                 };
                 /** Releasing xml spore **/
                 session.getSporedStream().releaseSpore( xmlSpore );
               } else {
                 /** Commands not supported, showing popup **/
                 Handler.showMainFrameElementPopup( popupItem );
               }
             }
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void executeCommand( final Item item ) {
     executeCommand( item, null, "execute" );
   }
 
   public static void executeCommand( final Item item, final Form form ) {
     executeCommand( item, form, null );
   }
 
   public static void executeCommand( final Item item, final Form form, final String command ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendCommandAction( this, item.jid, item.node, command, form );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Command ack received" );
               /** Preparing and configuring form object **/
               Form form = ( Form ) params.get( "FORM" );
               /** Checking for form popup item **/
               if ( ( form.leftSoft == null || form.leftSoft.isEmpty() )
                       && form.status.equals( "executing" ) ) {
                 /** Adding execute command **/
                 Command command = new Command( Localization.getMessage( "EXECUTE" ) ) {
                   public void actionPerformed() {
                     /** Showing wait screen **/
                     MidletMain.screen.setWaitScreenState( true );
                     /** Command invokation **/
                     LogUtil.outMessage( "Command invokation: " + item.jid + ", " + name );
                     Mechanism.executeCommand( item, form );
                   }
                 };
                 /** Setting up command fields **/
                 command.form = form;
                 command.item = item;
                 /** Initialize left soft **/
                 form.initLeftSoft();
                 /** Adding customized command **/
                 form.leftSoft.addSubItem( command );
               }
               /** Sending handler event **/
               Handler.showCommandFrame( item, form, null );
               LogUtil.outMessage( "Form objects count: ".concat( String.valueOf( params.size() ) ) );
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void invokeRegistration( final String jid, final Form form,
           final boolean isTemp ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendRegistrationForm(
                 this, form, jid );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Registration succeded" );
               /** Checking for already registered **/
               if ( isTemp ) {
                 /** Adding service to roster **/
                 rosterAddRequest( jid, null, null );
               } else {
                 /** Hiding wait screen and going to main frame  **/
                 Handler.showMainFrame();
               }
               return;
             }
             /** Handling error **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   /**
    * Roster add request
    * @param jid
    * @param name
    * @param groups 
    */
   public static void rosterAddRequest( final String jid, String name, String[] groups ) {
     rosterEditRequest( jid, name, null, groups, true );
   }
 
   public static void rosterRemoveRequest( String jid ) {
     rosterEditRequest( jid, null, "remove", null, false );
   }
 
   /**
    * Edit roster item
    * @param jid
    * @param name
    * @param groups 
    */
   public static void rosterEditRequest( final String jid, final String name,
           final String subscription, final String[] groups, final boolean isSendSubcrReq ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendRosterSet(
                 this, AccountRoot.getFullJid(), jid, name, subscription, groups );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Roster set success" );
               /** Checking for subscription request settings or service type **/
               if ( ( Settings.isAutomatedSubscriptionRequests
                       || jid.indexOf( '@' ) == -1 ) && isSendSubcrReq ) {
                 /** Sending subscribe request **/
                 /** 
                  * As described in RFC 3921, subscription request id
                  * must not be the same, nothing will return for this id
                  */
                 sendSubscriptionRequest( jid );
               }
               /** Hiding wait screen and going to main frame  **/
               Handler.showMainFrame();
               return;
             }
             /** Handling error **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   /**
    * Sending prompt request, receiving descr and prompt
    * @param serviceHost 
    */
   public static void sendPromptRequest( final String serviceHost ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Checking for service type **/
     if ( serviceHost.equals( "SERVICE_MOLECUS" ) ) {
       /** Molecus service **/
       Handler.showBuddyAddFrame( serviceHost, Localization.getMessage( "MOLECUS_DESC" ),
               Localization.getMessage( "MOLECUS_PROMPT" ) );
     } else if ( serviceHost.equals( "SERVICE_XMPP" ) ) {
       /** XMPP service **/
       Handler.showBuddyAddFrame( serviceHost, Localization.getMessage( "XMPP_DESC" ),
               Localization.getMessage( "XMPP_PROMPT" ) );
     } else {
       /** Another connected service **/
       /** Obtain session object **/
       final Session session = AccountRoot.getSession();
       /** Creating xml spore **/
       XmlSpore xmlSpore = new XmlSpore() {
         public void onRun() throws Throwable {
           /** Sending command execute request **/
           String cookie = TemplateCollection.sendPromptRequest(
                   this, serviceHost );
           QueueAction queueAction = new QueueAction() {
             public void actionPerformed( Hashtable params ) {
               /** Prompt received **/
               String errorCause = ( String ) params.get( "ERROR_CAUSE" );
               /** Checking for error **/
               if ( errorCause == null ) {
                 LogUtil.outMessage( "Prompt request success" );
                 String desc = ( String ) params.get( "DESC" );
                 String prompt = ( String ) params.get( "PROMPT" );
                 LogUtil.outMessage( "desc = " + desc );
                 LogUtil.outMessage( "prompt = " + prompt );
                 Handler.showBuddyAddFrame( serviceHost, desc, prompt );
                 return;
               }
               /** Handling errors **/
               Handler.showError( errorCause );
             }
           };
           queueAction.setCookie( cookie );
           Queue.pushQueueAction( queueAction );
         }
       };
       /** Releasing xml spore **/
       session.getSporedStream().releaseSpore( xmlSpore );
     }
   }
 
   /**
    * Sending prompt, waiting for JID and send roster add request
    * @param serviceHost
    * @param prompt
    * @param name
    * @param groups 
    */
   public static void invokePromptSendAddBuddy( final String serviceHost,
           final String prompt, final String name, final String[] groups ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendPromptResponse(
                 this, serviceHost, prompt );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Gateway returns JID **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Gateway returns JID" );
               String jid = ( String ) params.get( "JID" );
               LogUtil.outMessage( "JID = " + jid );
               /** Checking for server answer **/
               if ( jid.length() == 0 ) {
                 /** Server returns nothing **/
                 errorCause = "NOT_ACCEPTABLE";
               } else {
                 /** JID is correctly formed **/
                 rosterAddRequest( jid, name, groups );
                 return;
               }
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendSubscriptionRequest( final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending request **/
         TemplateCollection.sendSubscriptionRequest( this, jid );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendSubscriptionApprove( final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending request **/
         TemplateCollection.sendSubscriptionApprove( this, jid );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendSubscriptionReject( final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending request **/
         TemplateCollection.sendSubscriptionReject( this, jid );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendIqResult( final String iqId ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending result **/
         TemplateCollection.sendIqResult( this, iqId, null );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendPong( Session session, final String from, final String id ) {
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending pong **/
         TemplateCollection.sendIqResult( this, id, from );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendPing( Session session, final String from ) {
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending pong **/
         TemplateCollection.sendPing( this, from );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendRemoveRegistration( final String serviceHost ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendRegisterRequest(
                 this, serviceHost, true );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Gateway returns JID **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Gateway registration removed" );
               /** Creating xml spore **/
               XmlSpore xmlSpore = new XmlSpore() {
                 public void onRun() throws Throwable {
                   /** Removing service from roster **/
                   rosterRemoveRequest( serviceHost );
                   /** Removing all service items **/
                   String[] jids = Handler.getServiceItems( serviceHost );
                   /** Sending request **/
                   TemplateCollection.sendMassRosterRemove( this, AccountRoot.getFullJid(), jids );
                   /** Hiding wait screen **/
                   MidletMain.screen.setWaitScreenState( false );
                 }
               };
               /** Releasing xml spore **/
               session.getSporedStream().releaseSpore( xmlSpore );
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendBookmarksOperation( final int operation, final RoomItem item, boolean logInOnSuccess, boolean isSoloOperation ) {
     sendBookmarksOperation( operation, item, null, logInOnSuccess, isSoloOperation );
   }
 
   public static void sendBookmarksOperation( final int operation, final RoomItem item, final RoomItem editItem, final boolean logInOnSuccess, final boolean isSoloOperation ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending dump **/
         String cookie = TemplateCollection.sendBookmarksDump( this,
                 Handler.getBuddyList().roomsGroupItem.getChilds(), operation,
                 item, editItem );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Gateway returns JID **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Bookmarks set succesfully" );
               RoomItem logInRoomItem = null;
               /** Updating bookmarks to roster **/
               /** Switching for operation **/
               switch ( operation ) {
                 case Mechanism.OPERATION_REMOVE: {
                   /** Updating room temporary flag **/
                   item.setTemp( true );
                   /** Checking for room item is not active **/
                   if ( !item.getRoomActive() ) {
                     /** Removing item room group item childs **/
                     Handler.getBuddyList().roomsGroupItem.removeElement( item );
                   }
                   logInRoomItem = null;
                   break;
                 }
                 case Mechanism.OPERATION_EDIT: {
                   /** Updating room parameters **/
                   item.cloneInto( editItem );
                   editItem.setTemp( false );
                   /** Updating Ui **/
                   editItem.updateUi();
                   logInRoomItem = editItem;
                   break;
                 }
                 case Mechanism.OPERATION_ADD: {
                   /** Adding room item into buddy list **/
                   Handler.getBuddyList().roomsGroupItem.addChild( item );
                   logInRoomItem = item;
                   break;
                 }
               }
               /** Checking for log in parameter **/
               if ( logInOnSuccess && logInRoomItem != null ) {
                 /** Entering room **/
                 Mechanism.enterRoomRequest( logInRoomItem );
               } else if ( isSoloOperation ) {
                 /** Hiding wait screen and going to main frame  **/
                 Handler.showMainFrame();
               }
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendRoomsItemsDiscoveryRequest() {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending service discovery request **/
         String cookie = TemplateCollection.sendServiceItemsDiscoveryRequest(
                 this, AccountRoot.getRoomHost(), AccountRoot.getFullJid(), null );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               /** Items received, updating rooms frame **/
               Handler.updateRoomsFrame( Handler.getRoomsFrame(), params );
               Handler.showRoomsFrame();
               /** Hiding wait screen **/
               MidletMain.screen.setWaitScreenState( false );
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   /**
    * Inspecting specified service
    * @param jid 
    */
   public static void sendRoomsInfoDiscoveryRequest( final DiscoItem discoItem ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending service discovery request **/
         String cookie = TemplateCollection.sendServiceInfoDiscoveryRequest(
                 this, discoItem.getJid(), AccountRoot.getFullJid(), null );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Info received" );
               Handler.showRoomMoreFrame( discoItem, params );
               /** Hiding wait screen **/
               MidletMain.screen.setWaitScreenState( false );
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   /**
    * Requesting free room number and occupying it
    */
   public static void occupyRoomRequest() {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending service discovery request **/
         String cookie = TemplateCollection.sendServiceItemsDiscoveryRequest(
                 this, AccountRoot.getRoomHost(), AccountRoot.getFullJid(), null );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             LogUtil.outMessage( "Info received" );
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               /** Items received, checking for free room number **/
               Vector items = ( Vector ) params.get( "ITEMS" );
               int roomIndex = 1;
               /** Checking for items is not null **/
               if ( items != null ) {
                 boolean[] rooms = new boolean[ items.size() ];
                 /** Adding all items to roomsNum **/
                 String username;
                 int number;
                 for ( int c = 0; c < items.size(); c++ ) {
                   /** Obtain item element **/
                   Item item = ( Item ) items.elementAt( c );
                   /** Username detection **/
                   username = BuddyList.getJidUsername( item.jid );
                   LogUtil.outMessage( username );
                   /** Checking for username not null **/
                   if ( username != null ) {
                     try {
                       /** Parsing room number **/
                       number = Integer.parseInt( username );
                       if ( number <= rooms.length ) {
                         /** Setting for room is used **/
                         rooms[number - 1] = true;
                       }
                     } catch ( Throwable ex ) {
                       /** Skipping invalid room **/
                       LogUtil.outMessage( "Invalid room title: " + username );
                     }
                   }
                 }
                 /** Checking for empty room **/
                 roomIndex = rooms.length + 1;
                 for ( int c = 0; c < rooms.length; c++ ) {
                   if ( rooms[c] ) {
                     continue;
                   } else {
                     roomIndex = c + 1;
                     break;
                   }
                 }
               }
               LogUtil.outMessage( "Room is available: " + roomIndex );
               /** Creating new temporary room **/
               RoomItem roomItem = Handler.createTempRoomItem( roomIndex );
               /** Entering room **/
               enterRoomRequest( roomItem );
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void enterRoomRequest( final RoomItem roomItem ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending room enering request **/
         TemplateCollection.enterRoom( this, AccountRoot.getFullJid(), roomItem );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void leaveRoomRequest( final RoomItem roomItem ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending room enering request **/
         TemplateCollection.leaveRoom( this, AccountRoot.getFullJid(), roomItem );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
   
   public static void changeRoomNickRequest( final RoomItem roomItem ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending room updated nick request **/
         TemplateCollection.changeRoomNick( this, AccountRoot.getFullJid(), roomItem );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
   
     public static void editRoomTopicRequest( final RoomItem roomItem, final String topic ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending room updated nick request **/
         TemplateCollection.sendMessage( this, roomItem.getJid(), 
                 "groupchat", null, topic );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void configureRoomRequest( final RoomItem roomItem, final boolean isRoomCreating ) {
     /** Creating item instance **/
     final Item item = new Item( roomItem.getJid(), null, roomItem.getNickName() );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendRoomConfigurationRequest(
                 this, roomItem.getJid() );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Info received **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Configuring room ack received" );
               /** Preparing and configuring form object **/
               Form form = ( Form ) params.get( "FORM" );
               /** Adding execute command **/
               Command command = new Command( Localization.getMessage( "SAVE" ) ) {
                 /** Constants **/
                 private String ROOM_NAME = "muc#roomconfig_roomname";
                 private String ROOM_DESC = "muc#roomconfig_roomdesc";
                 private String ROOM_PERSISTENT = "muc#roomconfig_persistentroom";
                 private String ROOM_PASS_PROT = "muc#roomconfig_passwordprotectedroom";
                 private String ROOM_PASSWORD = "muc#roomconfig_roomsecret";
                 /** State to continue if recommended value is not corrected **/
                 private int state;
 
                 public void actionPerformed() {
                   /** Resetting state **/
                   state = 0;
                   /** Starting process **/
                   startCheckProcess();
                 }
 
                 public void startCheckProcess() {
                   /** Showing wait screen **/
                   MidletMain.screen.setWaitScreenState( true );
                   /** Checking for enteried data **/
                   if ( checkAndHaltOnEmptyValue( ROOM_NAME, "MUC_ROOM_NAME_IS_REQUIRED" ) ) {
                     if ( checkAndHaltOnEmptyValue( ROOM_DESC, "MUC_ROOM_DESC_IS_REQUIRED" ) ) {
                       if ( state >= 1 || checkForRecommendedValue(
                               ROOM_PERSISTENT, true,
                               "MUC_ROOM_PERS_IS_RECOMMENDED" ) ) {
                         if ( state >= 2 || checkForRecommendedValue(
                                 ROOM_PASS_PROT, false,
                                 "MUC_ROOM_PROT_IS_UNRECOMMENDED" ) ) {
                           /** Saving bookmark **/
                           updateBookmark();
                           /** Command invokation to send form data **/
                           LogUtil.outMessage( "Save form invokation: " + item.jid + ", " + name );
                           Mechanism.roomConfigurationFormSetRequest( roomItem, form, isRoomCreating );
                         } else {
                           state = 2;
                         }
                       } else {
                         state = 1;
                       }
                     }
                   }
                 }
 
                 private void updateBookmark() {
                   /** Obtain room properties object **/
                   PaneObject paneObject;
                   paneObject = form.getObjectByName( ROOM_NAME );
                   /** Defining room name **/
                   String roomName = paneObject.getStringValue();
                   /** Checking for room is temporary **/
                   paneObject = form.getObjectByName( ROOM_PERSISTENT );
                   boolean isPersistent = ( ( Check ) paneObject ).state;
                   /** Checking for room is password protcted **/
                   paneObject = form.getObjectByName( ROOM_PASS_PROT );
                   boolean isPassProt = ( ( Check ) paneObject ).state;
                   /** Checking for protection is enabled **/
                   String password = "";
                   if ( isPassProt ) {
                     /** Obtain room password object **/
                     paneObject = form.getObjectByName( ROOM_PASSWORD );
                     if ( paneObject != null ) {
                       if ( StringUtil.isEmptyOrNull( paneObject.getStringValue() ) ) {
                         LogUtil.outMessage( ROOM_PASSWORD.concat( " is empty" ) );
                       } else {
                         password = paneObject.getStringValue();
                       }
                     }
                   }
                   /** Checking for persistant configuration before and now **/
                   if ( !roomItem.getTemp() && isPersistent == false ) {
                     /** Removing bookmark from server but edit local **/
                     Mechanism.sendBookmarksOperation( OPERATION_REMOVE, roomItem, false, false );
                   }
                   /** Applying defined values **/
                   RoomEditFrame.updateRoomItemAttempt( roomItem, !isPersistent, roomName,
                           roomItem.getRoomNick(), password, false, false );
                 }
 
                 private boolean checkAndHaltOnEmptyValue( String var, String errorCause ) {
                   /** Obtain room properties object **/
                   PaneObject paneObject = form.getObjectByName( var );
                   if ( paneObject != null ) {
                     if ( StringUtil.isEmptyOrNull( paneObject.getStringValue() ) ) {
                       LogUtil.outMessage( var.concat( " is empty" ) );
                       Handler.showError( errorCause );
                       return false;
                     }
                   }
                   return true;
                 }
 
                 /**
                  * Checking for recommended value
                  */
                 private boolean checkForRecommendedValue( String var, boolean recValue, String dialogDescr ) {
                   /** Obtain room property object **/
                   PaneObject paneObject = form.getObjectByName( var );
                   /** Checking for object type and value **/
                   if ( paneObject != null && paneObject instanceof Check
                           && ( ( Check ) paneObject ).state != recValue ) {
                     LogUtil.outMessage( var.concat( " is in unrecommended value" ) );
                     /** Showing warnong **/
                     showUnrecommendedWarn( ( Check ) paneObject, dialogDescr );
                     return false;
                   }
                   return true;
                 }
 
                 /**
                  * Showing dialog of warining and opportunity to correct value
                  */
                 private void showUnrecommendedWarn( final Check paneObject, String dialogDescr ) {
                   Soft dialogSoft = new Soft( MidletMain.screen );
                   /** Dialog to change value and continue **/
                   dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {
                     public void actionPerformed() {
                       /**Correcting value **/
                       paneObject.state = !paneObject.state;
                       /** Restarting check process **/
                       startCheckProcess();
                     }
                   };
                   /** Dialog to continue only **/
                   dialogSoft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {
                     public void actionPerformed() {
                       /** Restarting check process **/
                       startCheckProcess();
                     }
                   };
                   /** Showing dialog **/
                   Handler.showDialog( MidletMain.screen.activeWindow, dialogSoft,
                           "WARNING", Localization.getMessage( dialogDescr ) );
                 }
               };
               /** Setting up command fields **/
               command.form = form;
               /** Initialize left soft **/
               form.initLeftSoft();
               /** Adding customized command **/
               form.leftSoft.addSubItem( command );
               /** Creating right soft **/
               PopupItem rightSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
                 public void actionPerformed() {
                   /** Checking for online connection **/
                   if ( !AccountRoot.isOffline() ) {
                     /** Checking for room creating status **/
                     if ( isRoomCreating ) {
                       /** Destroying room, removing empty temp bookmark **/
                       Mechanism.roomDestroyRequest( roomItem );
                     } else {
                       /** Showing wait screen **/
                       MidletMain.screen.setWaitScreenState( true );
                       /** Sending cancel packet **/
                       Mechanism.roomConfigurationFormSetRequest( roomItem, null, false );
                     }
                   } else {
                     /** Switching to main frame **/
                     Handler.showMainFrame();
                   }
                 }
               };
               /** Sending handler event **/
               Handler.showCommandFrame( item, form, rightSoft );
               LogUtil.outMessage( "Form objects count: ".concat(
                       String.valueOf( params.size() ) ) );
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void roomConfigurationFormSetRequest(
           final RoomItem roomItem, final Form form, final boolean enterRoom ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending command execute request **/
         String cookie = TemplateCollection.sendRoomConfigurationForm(
                 this, roomItem.getJid(), form );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Server response **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Room save is right" );
               /** Resetting rooms cache **/
               Handler.resetRoomsCache();
               if ( enterRoom ) {
                 /** Entering room **/
                Handler.roomEnteringComplete( roomItem, false );
               } else {
                 /** Showing main frame **/
                 Handler.showMainFrame();
               }
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void roomDestroyRequest( final RoomItem roomItem ) {
     /** Showing wait screen **/
     MidletMain.screen.setWaitScreenState( true );
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending room destroy request **/
         String cookie = TemplateCollection.sendRoomDestroyRequest(
                 this, roomItem.getJid(),
                 Localization.getMessage( "DESTROY_REASON" ) );
         QueueAction queueAction = new QueueAction() {
           public void actionPerformed( Hashtable params ) {
             /** Gateway returns JID **/
             String errorCause = ( String ) params.get( "ERROR_CAUSE" );
             /** Checking for error **/
             if ( errorCause == null ) {
               LogUtil.outMessage( "Room destroy is OK" );
               /** Resetting rooms cache **/
               Handler.resetRoomsCache();
               /** Closing all opened tabs of this room **/
               if ( Handler.closeOpenedTabs( roomItem ) ) {
                 /** Setting room as inactive to remove it completely **/
                 roomItem.setRoomActive( false );
                 /** Removing bookmark and going to main frame on success **/
                 Mechanism.sendBookmarksOperation( OPERATION_REMOVE, roomItem,
                         false, true );
               } else {
                 /** Showing main frame **/
                 Handler.showMainFrame();
               }
               return;
             }
             /* Handling error case **/
             Handler.showError( errorCause );
           }
         };
         queueAction.setCookie( cookie );
         Queue.pushQueueAction( queueAction );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
   
   public static void sendDiscoInfo( final String cookie, final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending disco info query request **/
         TemplateCollection.sendDiscoInfo( this, cookie, jid );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendLastActivity( final String cookie, final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Obtain last activity delay **/
         long lastActivity = ( System.currentTimeMillis()
                 - MidletMain.screen.lastActivity ) / 1000;
         /** Sending room enering request **/
         TemplateCollection.sendLastActivity( this, cookie, jid, lastActivity );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
   
   public static void sendEntityTime( final String cookie, final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Sending room enering request **/
         TemplateCollection.sendEntityTime( this, cookie, jid );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 
   public static void sendVersion( final String cookie, final String jid ) {
     /** Obtain session object **/
     final Session session = AccountRoot.getSession();
     /** Creating xml spore **/
     XmlSpore xmlSpore = new XmlSpore() {
       public void onRun() throws Throwable {
         /** Obtain version info **/
         String version = MidletMain.version + " "
                 + MidletMain.type + "-build " + MidletMain.build;
         /** Obtain device info **/
         String device;
         try {
           device = System.getProperty( "microedition.platform" ).
                   concat( " [" ).concat(
                   System.getProperty( "microedition.configuration" ) ).
                   concat( "]" );
         } catch ( Throwable ex1 ) {
           device = "J2ME";
         }
         /** Sending room enering request **/
         TemplateCollection.sendVersion( this, cookie, jid, device, version );
       }
     };
     /** Releasing xml spore **/
     session.getSporedStream().releaseSpore( xmlSpore );
   }
 }

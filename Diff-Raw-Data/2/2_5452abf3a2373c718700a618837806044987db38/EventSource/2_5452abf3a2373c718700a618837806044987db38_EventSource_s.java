 
 package com.sdde.DukascopyController;
 
 import org.zeromq.*;
 import java.sql.*;
 import java.io.*;
 import java.text.SimpleDateFormat;
 
 
 public class EventSource {
 
     private Llog log;
     private EventSocket socket;
     private SimpleDateFormat df;
 
     protected String event_name;
     protected String event_type;
     protected String user_name;
     protected String application_name;
 
     public EventSource(String event_name,
                        String event_type,
                        String user_name,
                        String application_name)
     {
         this.log = new Llog("EventSource:"
                             + user_name + ":" + application_name);
        this.df = new SimpleDateFormat("MM/dd/YY-HH:mm:ss");
         // this.df = new SimpleDateFormat("MM/dd/YY-HH:mm:ss.SSS");
         this.event_name = event_name;
         this.event_type = event_type;
         this.user_name = user_name;
         this.application_name = application_name;
         this.socket = EventSocket.Create(user_name, application_name);
         assert this.socket != null;
     }
 
     private String create_event_msg ()
     {
         Date d = new Date(System.currentTimeMillis());
         return String.format("%s %s %s %s %s",
                                this.event_type,
                                this.event_name,
                                df.format(d),
                                this.user_name,
                                this.application_name);
 
     }
 
     public void send(String msg_contents)
     {
         String msg = this.create_event_msg() + " " + msg_contents;
         this.socket.send(msg);
     }
 
     public void close ()
     {
         if ( this.socket != null )
             this.socket.close();
         this.socket = null;
     }
 
     /*
      * Tests:
      */
     public static void main ( String[] args )
     {
         boolean assertions_enabled = false;
         assert assertions_enabled = true;
 
         if ( assertions_enabled == false )
             throw new RuntimeException("Asserts must be enabled!!!");
 
         test1();
         test2();
     }
 
     private static void test1()
     {
         final Llog log = new Llog("test1()");
 
         String user_name = "user123";
         String application_name = "test1";
         String event_name = "myevent1";
         String event_type = "TEST1-EVENT";
 
         ZSocketServer s = new ZSocketServer(ZMQ.PULL,
                                           "ipc",
                                           user_name + ":" + application_name);
         s.bind();
 
         EventSource myevent = new EventSource(
                                     event_name,
                                     event_type,
                                     user_name,
                                     application_name);
         assert myevent != null;
 
         String tx_msg = "hello-there";
         myevent.send(tx_msg);
 
         String msg = s.recv();
         String[] msgs = msg.split(" ");
 
         assert msgs.length == 6;
         assert msgs[5].equals(tx_msg);
 
         myevent.close();
         s.close();
         log.info("PASSED");
     }
 
     private static void test2()
     {
         final Llog log = new Llog("test2()");
 
         String user_name = "user123";
         String application_name = "test1";
         String event_name = "myevent1";
         String event_type = "TEST1-EVENT";
 
         ZSocketServer s = new ZSocketServer(ZMQ.PULL,
                                           "ipc",
                                           user_name + ":" + application_name);
         s.bind();
 
         EventSource myevent = new EventSource(
                                     event_name,
                                     event_type,
                                     user_name,
                                     application_name);
         assert myevent != null;
 
         int nr_events = 100;
         for (int i = 0; i < nr_events; i++ )
         {
             String tx_msg = "hello-there" + i;
             myevent.send(tx_msg);
         }
 
         for (int i = 0; i < nr_events; i++ )
         {
             String msg = s.recv();
             String[] msgs = msg.split(" ");
 
             String tx_msg = "hello-there" + i;
             assert msgs.length == 6;
             assert msgs[5].equals(tx_msg);
         }
 
         myevent.close();
         s.close();
         log.info("PASSED");
     }
 }
 
 class EventSocket implements InterfaceEvent {
 
     public static EventSocket Create(String user_name, String application_name)
     {
         if ( event_socket == null )
             event_socket = new EventSocket(user_name, application_name);
 
         socket_count++;
         return event_socket;
     }
 
     /*
      * We allow only 1 socket for events.
      */
     private static EventSocket event_socket = null;
     private static int socket_count = 0;
 
     private String user_name;
     private String application_name;
     private ZSocketClient zsocket;
     private Interface intf;
     private Llog log;
 
     private EventSocket (String user_name, String application_name)
     {
         this.log = new Llog("EventSocket:"
                             + user_name + ":" + application_name);
         this.user_name = user_name;
         this.application_name = application_name;
         this.intf = new Interface(this);
 
         /*
          * Java-based EventSource objects only support ZMQ.PUSH
          * client sockets.  They basically send all events to
          * a proxy which will process and publish the events
          * via ZMQ.PUB.
          */
         this.zsocket = new ZSocketClient(ZMQ.PUSH,
                                     "ipc",
                                     user_name + ":" + application_name);
         /*
         this.zsocket = new ZSocketClient(ZMQ.PUSH,
                                     "tcp",
                                     "127.0.0.1", 6556);
         */
         this.zsocket.connect();
         this.intf.add_socket(this.zsocket);
     }
 
     public void send (String msg)
     {
         this.intf.push_in_msg(msg);
     }
 
     public void close ()
     {
         socket_count--;
         if ( socket_count == 0 )
         {
             log.info("Closing event socket!");
             event_socket.intf.close();
             event_socket = null;
         }
     }
 
     public void msg_cback (String msg)
     {
         assert false : "Received a message!";
     }
 }

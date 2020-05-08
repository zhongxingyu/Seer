 /*
  * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
  * 
  * This file is part of the chatbot xpeter.
  * 
  * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.con.Connection;
 import erki.xpeter.msg.Message;
 import erki.xpeter.parsers.Parser;
 
 /**
  * This class connects the different connections the bot handles. If a new connection is added (via
  * {@link #add(Connection)}) the new connection is immediately started. This class also contains all
  * the parsers and {@link #process(Message)} is called by the connections to delegate incoming
  * messages to all available parsers.
  * 
  * @author Edgar Kalkowski
  */
 public class Bot {
     
     private Collection<Connection> cons = new LinkedList<Connection>();
     
     private Set<Parser> parsers = new TreeSet<Parser>(new Comparator<Parser>() {
         
         public int compare(Parser o1, Parser o2) {
             return (o1.getClass().getCanonicalName().compareTo(o2.getClass().getCanonicalName()));
         }
     });
     
     private TreeMap<Class<? extends Message>, LinkedList<Observer<? extends Message>>> parserMapping = new TreeMap<Class<? extends Message>, LinkedList<Observer<? extends Message>>>(
             new Comparator<Class<? extends Message>>() {
                 
                 @Override
                 public int compare(Class<? extends Message> o1, Class<? extends Message> o2) {
                     return o1.getClass().getCanonicalName().compareTo(
                             o2.getClass().getCanonicalName());
                 }
             });
     
     /**
      * Create a new Bot with an initial set of some parsers.
      * 
      * @param parsers
      *        The initially used parsers of this Bot.
      */
     public Bot(Iterable<Class<? extends Parser>> parsers) {
         
         for (Class<? extends Parser> clazz : parsers) {
             add(clazz);
         }
     }
     
     /**
      * Add new parsers to this bot. There can only be one instance of every parser class active at
      * one time.
      * 
      * @param clazz
      *        The class object describing the new parser.
      */
     public void add(Class<? extends Parser> clazz) {
         Log.debug("Loading parser " + clazz.getSimpleName() + ".");
         
         /*
          * Try to instanicate all the parser classes and be sure to catch all exceptions if some
          * parser goes mad because we do not want to crash the whole bot.
          */
         try {
             Parser parser = clazz.newInstance();
             Parser[] pArray = parsers.toArray(new Parser[0]);
             
             for (Parser p : pArray) {
                 
                 if (p.getClass().getCanonicalName().equals(parser.getClass().getCanonicalName())) {
                     Log.debug("Parser " + clazz.getSimpleName() + " is already loaded. Reloading.");
                     remove(p.getClass());
                     break;
                 }
             }
             
             parser.init(this);
             parsers.add(parser);
         } catch (InstantiationException e) {
             Log.error(e);
             Log.warning("Parser " + clazz.getCanonicalName() + " could not be loaded!");
             Log.info("Trying to continue without this parser.");
         } catch (IllegalAccessException e) {
             Log.error(e);
             Log.warning("You are not allowed to instanciate the parser class "
                     + clazz.getCanonicalName() + ". Please check your security settings!");
             Log.info("Trying to continue without this parser.");
         } catch (Throwable e) {
             Log.error(e);
             Log.warning("Could not initialize the parser " + clazz.getCanonicalName() + ".");
             Log.info("Trying to continue without this one.");
         }
     }
     
     /**
      * Access the set of parsers currently loaded by this bot. The returned set only contains the
      * class names of the active parsers thus no modification can do any harm.
      * 
      * @return The active set of parsers.
      */
     public TreeSet<Class<? extends Parser>> getParsers() {
         TreeSet<Class<? extends Parser>> parsers = new TreeSet<Class<? extends Parser>>(
                 new Comparator<Class<? extends Parser>>() {
                     
                     @Override
                     public int compare(Class<? extends Parser> o1, Class<? extends Parser> o2) {
                         return o1.getCanonicalName().compareTo(o2.getCanonicalName());
                     }
                 });
         
         for (Parser p : this.parsers) {
             parsers.add(p.getClass());
         }
         
         return parsers;
     }
     
     /**
      * Remove parsers from this bot. The corresponding {@link Parser#destroy(Bot)} method is called
      * in which the parser itself must deregister all its listeners and finish all threads it may
      * have started.
      * 
      * @param clazz
      *        A class object describing the parser to remove.
      */
     public void remove(Class<? extends Parser> clazz) {
         Parser[] pArray = parsers.toArray(new Parser[0]);
         
         for (Parser p : pArray) {
             
             if (p.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
                 Log.debug("Removing parser " + p.getClass().getSimpleName() + ".");
                 p.destroy(this);
                 parsers.remove(p);
             }
         }
     }
     
     /**
      * Add a new connection to this bot. For each connection a separate {@link Thread} is started
      * immediately.
      * 
      * @param con
      *        The connection to add.
      */
     public void add(Connection con) {
         
         synchronized (cons) {
             cons.add(con);
             new Thread(con, con.toString()).start();
         }
     }
     
     /**
      * Broadcast a message to all connections currently available to this bot.
      * 
      * @param msg
      *        The message to broadcast.
      */
     public void broadcast(Message msg) {
         
         synchronized (cons) {
             
             for (Connection con : cons) {
                 con.send(msg);
             }
         }
     }
     
     /**
      * Broadcast a message to all connection currently available to this bot with the exception of
      * {@code con}.
      * 
      * @param msg
      *        The message to broadcast.
      * @param con
      *        The Connection instance that will not receive {@code msg}. The connections are
      *        compared using the “==” operator.
      */
     public void broadcast(Message msg, Connection con) {
         
         synchronized (cons) {
             
             for (Connection conn : cons) {
                 
                 if (conn != con) {
                     conn.send(msg);
                 }
             }
         }
     }
     
     /**
      * Parsers can register themself via this method to be informed if a certain type of message was
      * received.
      * 
      * @param <MessageType>
      *        The type of message the parser wants to be informed about.
      * @param messageType
      *        The class of the message type the parser wants to be informed about (this is needed
      *        for implementation issues).
      * @param observer
      *        The observer instance that will be informed.
      */
     public <MessageType extends Message> void register(Class<MessageType> messageType,
             Observer<MessageType> observer) {
         
         synchronized (parserMapping) {
             Log.debug("Registered new listener for " + messageType.getSimpleName() + "s.");
             
             if (!parserMapping.containsKey(messageType)) {
                 parserMapping.put(messageType, new LinkedList<Observer<? extends Message>>());
             }
             
             parserMapping.get(messageType).add(observer);
         }
     }
     
     public <MessageType extends Message> void deregister(Class<MessageType> messageType,
             Observer<MessageType> observer) {
         
         synchronized (parserMapping) {
             
             if (parserMapping.containsKey(messageType)) {
                 parserMapping.get(messageType).remove(observer);
             }
         }
     }
     
     /**
      * Processes a message that was received from some connection. This method just delegates the
      * message to all available parsers and lets them do the work.
      * 
      * @param msg
      *        The message to process.
      */
     /*
      * The unchecked casts here are safe because the types are actually forced to be correct when
      * registering observers (see #register).
      */
     @SuppressWarnings("unchecked")
     public void process(Message msg) {
         
         if (msg.getConnection() == null) {
             Log.warning("Someone delivered a foul message (" + msg + "). Refusing to parse it!");
             return;
         }
         
         synchronized (parserMapping) {
             Log.debug("Processing " + msg.toString() + ".");
             LinkedList<Observer<? extends Message>> parsers = parserMapping.get(msg.getClass());
             Log.debug("Registered observers for " + msg.getClass().getSimpleName() + " are "
                     + parsers + ".");
             
             for (Observer parser : parsers) {
                 Log.debug("Informing " + parser.getClass().getSimpleName() + ".");
                 
                 try {
                     parser.inform(msg);
                 } catch (Throwable e) {
                     Log.error(e);
                     Log.warning("Parser " + parser.getClass().getSimpleName() + " crashed!");
                     Log.info("Continuing anyway.");
                     msg.getConnection().send(
                             "Mumble mumble in " + parser.getClass().getSimpleName() + ": "
                                    + e.getClass().getSimpleName());
                 }
             }
         }
     }
 }

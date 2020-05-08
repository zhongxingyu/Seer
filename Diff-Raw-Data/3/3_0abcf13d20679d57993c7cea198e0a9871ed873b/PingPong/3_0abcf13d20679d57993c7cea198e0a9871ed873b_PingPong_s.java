 /*
  * Copyright (C) 2013 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.javnce.examples.PingPong;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.util.Random;
 import org.javnce.eventing.ChannelSubscriber;
 import org.javnce.eventing.Event;
 import org.javnce.eventing.EventLoop;
 import org.javnce.eventing.EventSubscriber;
 import org.javnce.eventing.TimeOutCallback;
 import org.javnce.eventing.Timer;
 
 /**
  * The PingPong is a example of using {@link org.javnce.eventing.EventLoop},
  * {@link org.javnce.eventing.ChannelSubscriber}, {@link org.javnce.eventing.EventSubscriber}
  * and {@link org.javnce.eventing.Timer}.
  * <p>
  * The PingPong is done in functional-programming style and has three threads;
  * Ping, Pong and Main thread.
  * </p><p>
  * The Ping and Pong threads handles both socket and events.
  * </p><p>
  * The Main thread uses a timer to stop the threads.
  * </p>
  */
 public class PingPong {
 
     /**
      * The constant to for who long time threads are running.
      */
     public static final Long RunTimeInMS = 20l;
 
     /**
      * Read string from given channel.
      *
      * @param channel the channel that should be type of SocketChannel
      * @return the string read from the channel
      * @throws Exception the exception
      */
     public static String read(SelectableChannel channel) throws Exception {
         String string = null;
 
         if (channel instanceof SocketChannel) {
             SocketChannel socketChannel = (SocketChannel) channel;
             ByteBuffer buffer = ByteBuffer.allocate(1000);
             socketChannel.read(buffer);
            string = new String(buffer.array());
         }
         return string;
     }
 
     /**
      * Write string to given channel.
      *
      * @param channel the channel that should be type of SocketChannel
      * @param data the data to be written into channel
      * @throws Exception the exception
      */
     public static void write(SelectableChannel channel, String data) throws Exception {
         if (channel instanceof SocketChannel) {
             SocketChannel socketChannel = (SocketChannel) channel;
             ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
             socketChannel.write(buffer);
         }
     }
 
     /**
      * Creates the ping thread.
      *
      * The Ping thread replies with {@link org.javnce.examples.PingPong.EventPong} 
      * when an {@link org.javnce.examples.PingPong.EventPing} arrives.  The thread 
      * writes string "Pong-message" to socket when it gets incoming data.
      *
      * @param socket the socket to be listen
      * @return the ping thread
      * @throws Exception the exception
      */
     public static Thread createPingThread(final SocketChannel socket) throws Exception {
 
         final EventLoop eventLoop = new EventLoop();
 
         //Configure SocketChannel as non-blocking
         socket.configureBlocking(false);
 
         //Register Ping event handler
         eventLoop.subscribe(EventPing.eventId(), new EventSubscriber() {
             @Override
             public void event(Event event) {
                 MyLogger.threadSays(((EventPing) event).ping());
                 eventLoop.publish(new EventPong());
             }
         });
 
         //Register socket handler
         eventLoop.subscribe(socket, new ChannelSubscriber() {
             @Override
             public void channel(SelectionKey key) {
                 try {
                     String message = read(key.channel());
                     MyLogger.threadSays(message);
                     write(key.channel(), "Pong-message");
                 } catch (Exception ex) {
                     EventLoop.fatalError(null, ex);
                 }
             }
         }, SelectionKey.OP_READ);
         return new Thread(eventLoop, "Ping");
     }
 
     /**
      * Creates the pong thread.
      *
      * The Pong thread replies with {@link org.javnce.examples.PingPong.EventPing} 
      * when it gets data from socket. The thread writes string "Ping-message" to 
      * socket when an {@link org.javnce.examples.PingPong.EventPong} arrives.
      *
      * @param socket the socket to be listen
      * @return the pong thread
      * @throws Exception the exception
      */
     public static Thread createPongThread(final SocketChannel socket) throws Exception {
 
         final EventLoop eventLoop = new EventLoop();
 
         //Configure SocketChannel as non-blocking
         socket.configureBlocking(false);
 
         //Register Pong event handler
         eventLoop.subscribe(EventPong.eventId(), new EventSubscriber() {
             @Override
             public void event(Event event) {
                 MyLogger.threadSays(((EventPong) event).pong());
                 try {
                     write(socket, "Ping-message");
                 } catch (Exception ex) {
                     EventLoop.fatalError(null, ex);
                 }
             }
         });
 
         //Register socket handler
         eventLoop.subscribe(socket, new ChannelSubscriber() {
             @Override
             public void channel(SelectionKey key) {
                 try {
                     String message = read(key.channel());
                     MyLogger.threadSays(message);
                     eventLoop.publish(new EventPing());
                 } catch (Exception ex) {
                     EventLoop.fatalError(null, ex);
                 }
             }
         }, SelectionKey.OP_READ);
 
         return new Thread(eventLoop, "Pong");
     }
 
     /**
      * The main method.
      *
      * @param args the arguments
      */
     public static void main(String[] args) {
         final Random randomGenerator = new Random(System.currentTimeMillis());
 
         //Register own error handler
         EventLoop.setErrorHandler(new MyLogger());
 
         try (SocketChannelPair sockets = new SocketChannelPair()) {
 
             MyLogger.threadSays("Creating threads");
             Thread ping = createPingThread(sockets.channel1());
             ping.start();
             Thread pong = createPongThread(sockets.channel2());
             pong.start();
 
             EventLoop eventLoop = new EventLoop();
 
             //Send the first Ping or Pong event
             if (randomGenerator.nextBoolean()) {
                 MyLogger.threadSays("Ping starts");
                 eventLoop.publish(new EventPing());
             } else {
                 MyLogger.threadSays("Pong starts");
                 eventLoop.publish(new EventPong());
             }
 
             //Add timer to stop the whole thing
             eventLoop.addTimer(new Timer(new TimeOutCallback() {
                 @Override
                 public void timeout() {
                     MyLogger.threadSays("Stop playing");
                     EventLoop.shutdownAll();
                 }
             }, RunTimeInMS));
 
             //Main threads event loop is processed until shutdown is called
             eventLoop.process();
 
             //Wait for threads to stop
             ping.join();
             pong.join();
 
         } catch (Exception e) {
             EventLoop.fatalError(null, e);
         }
         MyLogger.threadSays("Bye bye");
     }
 }

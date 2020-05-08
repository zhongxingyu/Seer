 /**
  * Copyright (C) 2013 uphy.jp
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.uphy.jijiping.server;
 
 import jp.uphy.jijiping.common.JijipingClient;
 import jp.uphy.jijiping.common.Util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.InetSocketAddress;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * @author Yuhi Ishikura
  */
 public class JijipingServer {
 
   private Map<String, Set<ClientContext>> clientIdToContext = new HashMap<String, Set<JijipingServer.ClientContext>>();
 
   private static Logger logger = Logger.getLogger(JijipingServer.class.getName());
 
   static {
     logger.setLevel(Level.ALL);
     final ConsoleHandler h = new ConsoleHandler();
     h.setLevel(Level.ALL);
     logger.addHandler(h);
   }
 
   /**
    * サーバーを開始します。
    * 
    * @param port 待ち受けポート
    * @throws IOException 開始できなかった場合
    */
   public void start(int port) throws IOException {
     final ServerSocketChannel serverChannel = ServerSocketChannel.open();
     serverChannel.socket().setReuseAddress(true);
     serverChannel.socket().bind(new InetSocketAddress(port));
     logger.fine("Server started at " + port); //$NON-NLS-1$
 
     serverChannel.configureBlocking(false);
     final Selector selector = Selector.open();
     final SelectionKey serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
     while (selector.select() > 0) {
       Set<SelectionKey> keys = selector.selectedKeys();
       for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {
         SelectionKey key = it.next();
         it.remove();
         try {
           if (key == serverKey && key.isAcceptable()) {
             final SocketChannel channel = serverChannel.accept();
             logger.fine("New client accepted : " + channel); //$NON-NLS-1$
             channel.configureBlocking(false);
             SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
             final ClientContext context = new ClientContext(clientKey);
             clientKey.attach(context);
           } else {
             if (key.isReadable()) {
               final SocketChannel channel = (SocketChannel)key.channel();
               final String line = Util.read(channel);
               logger.fine("Reading command => " + line); //$NON-NLS-1$
               final ClientContext context = (ClientContext)key.attachment();
 
               StringTokenizer tokenizer = new StringTokenizer(line, ":"); //$NON-NLS-1$
               final int commandId = Integer.parseInt(tokenizer.nextToken());
               final String clientId = tokenizer.nextToken();
               final String parameter;
               if (tokenizer.hasMoreTokens()) {
                 parameter = tokenizer.nextToken();
               } else {
                 parameter = ""; //$NON-NLS-1$
               }
               switch (commandId) {
                 case JijipingClient.CHECKIN_COMMAND_ID:
                   checkout(context);
                   checkin(clientId, context);
                   listClients();
                   continue;
                 case JijipingClient.CHECKOUT_COMMAND_ID:
                   checkout(clientId, context);
                   continue;
               }
 
               write(context, clientId, String.valueOf(commandId) + ":" + parameter); //$NON-NLS-1$
             }
             if (key.isWritable() && key.isValid()) {
               final ClientContext context = (ClientContext)key.attachment();
               String message;
               final SocketChannel channel = (SocketChannel)key.channel();
               while ((message = context.popMessage()) != null) {
                 logger.fine(String.format("Writing command => %s", message)); //$NON-NLS-1$
                 Util.write(channel, message);
               }
             }
           }
         } catch (Throwable e) {
           logger.severe(exceptionToString(e));
           key.channel().close();
           key.cancel();
         }
       }
     }
   }
 
   private void listClients() {
     logger.fine("*** Current Clients ***");
     synchronized (this.clientIdToContext) {
       for (String clientId : this.clientIdToContext.keySet()) {
         logger.fine(clientId + "=>");
         for (ClientContext context : this.clientIdToContext.get(clientId)) {
           final SelectionKey key = context.key;
           logger.fine(key.channel().toString());
         }
       }
     }
     logger.fine("***********************");
   }
 
   private static String exceptionToString(Throwable e) {
     final StringWriter sw = new StringWriter();
     final PrintWriter pw = new PrintWriter(sw);
     e.printStackTrace(pw);
     pw.flush();
     return sw.toString();
   }
 
   private void checkout(String clientId, ClientContext context) {
     synchronized (this.clientIdToContext) {
       checkoutWithoutLock(clientId, context);
     }
   }
 
   private void checkoutWithoutLock(String clientId, ClientContext context) {
     final Set<ClientContext> contexts = this.clientIdToContext.get(clientId);
     contexts.remove(context);
   }
 
   private void checkout(ClientContext context) {
     final String host = context.getHost();
     if (host == null) {
       return;
     }
     synchronized (this.clientIdToContext) {
       for (String clientId : this.clientIdToContext.keySet()) {
         for (ClientContext c : this.clientIdToContext.get(clientId)) {
           if (host.equals(c.getHost()) || c.isClosed()) {
             checkoutWithoutLock(clientId, c);
           }
         }
       }
     }
   }
 
   private void checkin(String clientId, ClientContext context) {
     synchronized (this.clientIdToContext) {
       Set<ClientContext> contexts = this.clientIdToContext.get(clientId);
       if (contexts == null) {
         contexts = new HashSet<JijipingServer.ClientContext>();
         contexts.add(context);
         this.clientIdToContext.put(clientId, contexts);
       }
       contexts.add(context);
     }
   }
 
   void write(ClientContext sender, String clientId, String line) {
     logger.fine(String.format("Write requested \"%s\" to \"%s\"", line, clientId)); //$NON-NLS-1$
     for (ClientContext context : getContexts(clientId)) {
       if (context == sender) {
         continue;
       }
       context.pushMessage(line);
     }
   }
 
   Set<ClientContext> getContexts(String clientId) {
    synchronized (this.clientIdToContext) {
      Set<ClientContext> contexts = this.clientIdToContext.get(clientId);
      if (contexts == null) {
        return Collections.emptySet();
      }
      return contexts;
     }
   }
 
   static class ClientContext {
 
     private Queue<String> writeQueue = new LinkedList<String>();
     private SelectionKey key;
 
     ClientContext(SelectionKey clientKey) {
       this.key = clientKey;
     }
 
     /**
      * @return
      */
     public boolean isClosed() {
       try {
         final SelectableChannel channel = this.key.channel();
         if (channel.isOpen() == false) {
           return true;
         }
         return false;
       } catch (Throwable e) {
         return true;
       }
     }
 
     public String getHost() {
       try {
         final InetSocketAddress addr = (InetSocketAddress)(((SocketChannel)this.key.channel()).getRemoteAddress());
         return addr.getHostName();
       } catch (IOException ex) {
         return null;
       }
     }
 
     void pushMessage(String message) {
       synchronized (this.writeQueue) {
         this.writeQueue.add(message);
         try {
           this.key.interestOps(this.key.interestOps() | SelectionKey.OP_WRITE);
         } catch (CancelledKeyException ex) {
           return;
         }
       }
     }
 
     String popMessage() {
       synchronized (this.writeQueue) {
         if (this.writeQueue.size() == 0) {
           return null;
         }
         final String message = this.writeQueue.poll();
         if (this.writeQueue.isEmpty()) {
           this.key.interestOps(this.key.interestOps() & (~SelectionKey.OP_WRITE));
         }
         return message;
       }
     }
 
     void kill() {
       synchronized (this.writeQueue) {
         this.key.cancel();
       }
     }
   }
 
   public static void main(String[] args) throws IOException {
     final int port = Integer.parseInt(args[0]);
     final JijipingServer server = new JijipingServer();
     server.start(port);
   }
 
 }

 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fast.bats.europe.examples.commands;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 import java.util.Scanner;
 
 import silvertip.Connection;
 
 import java.util.logging.FileHandler;
 import java.util.logging.Logger;
 
 import fast.Message;
 import fast.bats.europe.FastPitchMessageParser;
 import fast.bats.europe.examples.FastPitchClient;
 import fast.bats.europe.session.Session;
 import fast.soup.SoupTCP2Encoder;
 
 public class Login implements Command {
   private static final Logger LOG = Logger.getLogger("FastPitchClient");
 
   static {
     LOG.setUseParentHandlers(false);
     try {
       LOG.addHandler(new FileHandler("messages.log"));
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   public void execute(final FastPitchClient client, Scanner scanner) throws CommandArgException {
     try {
       Connection connection = Connection.connect(new InetSocketAddress(host(scanner), port(scanner)), 
           new FastPitchMessageParser(), new Connection.Callback<Message>() {
             public void messages(Connection<Message> connection, Iterator<Message> messages) {
               while (messages.hasNext()) {
                 Message message = messages.next();
                 client.getSession().receive(connection, message);
                 LOG.info(message.toString());
               }
             }
 
             @Override 
             public void idle(Connection<Message> connection) {
               client.getSession().heartbeat(connection);
             }
 
             @Override
             public void closed(Connection<Message> connection) {
             }
 
             @Override
             public void garbledMessage(String message, byte[] data) {
             }
           });
       client.setConnection(connection);
       Session session = new Session(new SoupTCP2Encoder());
       client.setSession(session);
       session.login(connection, username(scanner), password(scanner));
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   private InetAddress host(Scanner scanner) throws CommandArgException {
     if (!scanner.hasNext())
       throw new CommandArgException("hostname must be specified");
     try {
       return InetAddress.getByName(scanner.next());
     } catch (UnknownHostException e) {
       throw new CommandArgException("unknown hostname");
     }
   }
 
   private int port(Scanner scanner) throws CommandArgException {
     if (!scanner.hasNext())
       throw new CommandArgException("port must be specified");
     try {
       return Integer.parseInt(scanner.next());
     } catch (NumberFormatException e) {
       throw new CommandArgException("invalid port");
     }
   }
 
   private String username(Scanner scanner) throws CommandArgException {
     if (!scanner.hasNext())
       throw new CommandArgException("username must be specified");
     return scanner.next();
   }
 
   private String password(Scanner scanner) throws CommandArgException {
     if (!scanner.hasNext())
       throw new CommandArgException("password must be specified");
     return scanner.next();
   }
 }

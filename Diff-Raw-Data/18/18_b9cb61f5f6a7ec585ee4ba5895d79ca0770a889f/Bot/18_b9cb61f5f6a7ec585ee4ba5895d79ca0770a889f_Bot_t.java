 /*
  * Copyright (c) <2013> <Jim Johnson jimj@jimj.net>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.jimj.automaton;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.mongodb.DB;
 import net.jimj.automaton.commands.Command;
 import net.jimj.automaton.commands.HeadCommand;
 import net.jimj.automaton.commands.HelpCommand;
 import net.jimj.automaton.commands.KarmaCommand;
 import net.jimj.automaton.commands.NoteCommand;
 import net.jimj.automaton.commands.PrefCommand;
 import net.jimj.automaton.commands.Processor;
 import net.jimj.automaton.commands.QuoteCommand;
 import net.jimj.automaton.commands.UserCommand;
 import net.jimj.automaton.commands.YourMomCommand;
 import net.jimj.automaton.events.DataStoredEvent;
 import net.jimj.automaton.events.Event;
 import net.jimj.automaton.events.HelpEvent;
 import net.jimj.automaton.events.MessageEvent;
 import net.jimj.automaton.events.UnknownUserEvent;
 import net.jimj.automaton.model.Config;
 import net.jimj.automaton.model.User;
 import org.apache.commons.lang.StringUtils;
 import org.jibble.pircbot.PircBot;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class Bot extends PircBot {
     private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
 
     private ObjectMapper objectMapper;
     private HashMap<String, Command> commandMap = new HashMap<>();
     private ArrayList<Processor> processors = new ArrayList<>();
     private BlockingQueue<Event> eventQueue;
 
     private UserAccess userAccess;
     private Config config;
     private DB db;
 
     public Bot(DB db) throws Exception {
         this.db = db;
         this.objectMapper = new ObjectMapper();
         this.eventQueue = new LinkedBlockingQueue<>();
         this.userAccess = new UserAccess(db.getCollection("users"));
 
         loadConfig();
 
         this.setLogin(config.getNick());
         this.setName(config.getNick());
         //TODO: Clean up logging
         this.setVerbose(LOGGER.isTraceEnabled());
         loadCommands();
     }
 
     public void go() {
         Runnable eventExecutor = new Runnable() {
             @Override
             public void run() {
                 while(true) {
                     try {
                         executeEvent(eventQueue.take());
                     }catch(InterruptedException e) {
                         //Shutting down.
                     }
                 }
             }
         };
         new Thread(eventExecutor).start();
         LOGGER.debug("Creating event executor");
         connect();
     }
 
     private void executeEvent(Event event) {
         if(event instanceof MessageEvent) {
             MessageEvent msgEvent = (MessageEvent)event;
             sendMessage(msgEvent.getTarget(), msgEvent.getMessage());
         }else if(event instanceof HelpEvent) {
             String cmdName = ((HelpEvent) event).getCommandName();
             Command cmd = commandMap.get(cmdName);
            if(cmdName == null) {
                StringBuilder commandList = new StringBuilder();
                for(String command : commandMap.keySet()) {
                    commandList.append(command).append(", ");
                }
                commandList.replace(commandList.length()-2, commandList.length(), "");
                sendMessage(event.getTarget(), "Command list: " + commandList.toString());
            }
            else if(cmd == null) {
                 sendMessage(event.getTarget(), "Unknown command: " + cmdName);
             }else {
                 cmd.help(event.getUser());
             }
         }else if(event instanceof UnknownUserEvent) {
             UnknownUserEvent unknownUserEvent = (UnknownUserEvent)event;
             if(unknownUserEvent.isSelf()) {
                 sendMessage(event.getTarget(), "Sorry " + event.getUser().getNick() + " but I don't know who you are.");
             }else {
                 sendMessage(event.getTarget(), "Sorry " + event.getUser().getNick() + " but I don't know who "
                     + unknownUserEvent.getUnknownNick() + " is.");
             }
         }else if(event instanceof DataStoredEvent) {
             sendMessage(event.getTarget(), ((DataStoredEvent) event).getMessage());
         }
     }
 
     private void connect() {
         LOGGER.debug("Connecting...");
         try {
             this.connect(config.getServer());
         }catch(Exception e) {
             LOGGER.error("Exception in go method ", e);
         }
     }
 
     @Override
     protected void onConnect() {
         for(String channel : config.getChannels()) {
             this.joinChannel(channel);
         }
     }
 
     @Override
     protected void onDisconnect() {
         connect();
     }
 
     @Override
     protected void onPrivateMessage(String sender, String login, String hostname, String message) {
         onMessage(null, sender, login, hostname, message);
     }
 
     @Override
     protected void onChannelInfo(String channel, int userCount, String topic) {
         super.onChannelInfo(channel, userCount, topic);
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         User user = userAccess.findUser(sender);
         user.setChannel(channel);
 
         //Ignore self for any processing.
         if(user.getNick().equals(config.getNick())) {
             return;
         }
 
         if(message.startsWith(config.getCommandChar())) {
             String commandName = message;
 
             //Try to chop off the first word
             int commandNameEnd = message.indexOf(" ");
             String args = null;
 
             //Strip off the command character for looking up the command
             if(commandNameEnd != -1) {
                 commandName = message.substring(config.getCommandChar().length(), commandNameEnd);
                 args = StringUtils.strip(message.substring(config.getCommandChar().length() + commandName.length()));
             }else {
                 commandName = commandName.substring(config.getCommandChar().length());
             }
 
             try {
                 fireCommand(user, commandName, args);
             }catch(Exception e) {
                 String target = channel == null? sender : channel;
                 this.sendMessage(target, "fyf " + sender);
 
                 StackTraceElement[] st = e.getStackTrace();
                 int numMessages = Math.min(3, st.length);
 
                 //TODO: put this in config or dig out from Users somehow.
                 this.sendMessage("Gnome", sender + " caused " + e.getMessage());
                 for(int i=0;i<numMessages;i++) {
                     this.sendMessage("Gnome", st[i].toString());
                 }
             }
         }else {
             for(Processor processor : processors) {
                 if(processor.shouldProcess(message)) {
                     processor.process(user, message);
                 }
             }
         }
     }
 
     protected void fireCommand(User user, String commandName, String args) {
         LOGGER.debug("Firing command " + commandName);
         Command commandObj = commandMap.get(commandName);
         if(commandObj != null) {
             commandObj.execute(user, args);
         }
         //else insult event
     }
 
     private void loadCommands() {
         loadCommand(new QuoteCommand(db.getCollection("quotes")));
         loadCommand(new HeadCommand());
         loadCommand(new KarmaCommand(db.getCollection("karma")));
         loadCommand(new YourMomCommand(db.getCollection("yourmom")));
         loadCommand(new NoteCommand(db.getCollection("notes")));
         loadCommand(new HelpCommand());
         loadCommand(new UserCommand(userAccess));
         loadCommand(new PrefCommand(userAccess));
     }
 
     private void loadCommand(Command command) {
         command.setEventQueue(eventQueue);
         commandMap.put(command.getCommandName(), command);
 
         if(command instanceof Processor) {
             processors.add((Processor) command);
         }
     }
 
     private void loadConfig() throws Exception {
         //TODO: externalize this
         InputStream configStream = getClass().getResourceAsStream("/META-INF/config.json");
         config = objectMapper.readValue(configStream, Config.class);
         if(LOGGER.isDebugEnabled()) {
             LOGGER.debug(objectMapper.writeValueAsString(config));
         }
     }
 }

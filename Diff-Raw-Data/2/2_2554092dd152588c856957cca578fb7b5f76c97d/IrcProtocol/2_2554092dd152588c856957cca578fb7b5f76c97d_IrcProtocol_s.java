 /** @author Eldar Damari, Ory Band. */
 
 package irc;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class IrcProtocol implements AsyncServerProtocol<String> {
     private Client client;
 
     private boolean shouldClose;
     private boolean connectionTerminated;
 
 
     /** Enumerates all error/reply codes. */
     public enum STATUS {
         // Error codes.
         NOSUCHCHANNEL     ( 403, "No such channel"             ),
         UNKNOWNCOMMAND    ( 421, "Unknown command"             ),
         NONICKNAMEGIVEN   ( 431, "No nickname given"           ),
         NICKNAMEINUSE     ( 433, "Nickname is already in use"  ),
         NOTREGISTERED     ( 451, "You have not registered"     ),
         NEEDMOREPARAMS    ( 461, "Not enough parameters"       ),
         ALREADYREGISTERED ( 462, "You may not reregister"      ),
         CHANOPRIVSNEEDED  ( 482, "You’re not channel operator" ),
 
         // Reply codes.
         NAMEREPLY    ( 353                       ),
         ENDOFNAMES   ( 366, "End of /NAMES list" ),
         LISTSTART    ( 321                       ),
         LIST         ( 322                       ),
         LISTEND      ( 323, "End of /LIST list"  ),
         NICKACCEPTED ( 401                       ),
         USERACCEPTED ( 402                       ),
         USERKICKED   ( 404                       ),
         PARTSUCCESS  ( 405                       );
 
 
         private final int    _number;
         private final String _text;
 
         STATUS(int number) {
             _number = number;
             _text   = number + "";  // Use number string representation.
         }
 
         STATUS(int number, String text) {
             _number = number;
             _text = text;
         }
 
         public int getNumber() {
             return this._number;
         }
 
         public String getText() {
             return this._text;
         }
     };
 
     /** Enumerates all commands. */
     public enum COMMAND {
         // Error codes.
         NICK  ( "NICK"  ),
         USER  ( "USER"  ),
         QUIT  ( "QUIT"  ),
         JOIN  ( "JOIN"  ),
         PART  ( "PART"  ),
         NAMES ( "NAMES" ),
         LIST  ( "LIST"  ),
         KICK  ( "KICK"  );
 
         private final String _text;
 
         COMMAND(String text) {
             _text = text;
         }
 
         public String getText() {
             return this._text;
         }
     };
 
 
     public IrcProtocol() {
         this.client = Client.createClient();
 
         this.shouldClose = false;
         this.connectionTerminated = false;
     }
 
 
     /**
      * @param connectionHandler object to set protocol's user with.
      */
     public void setConnectionHandler(
             ConnectionHandler<String> connectionHandler) {
 
         this.client.setConnectionHandler(connectionHandler);
     }
 
 
     public boolean shouldClose() {
         return this.shouldClose;
     }
 
     public void close() {
         this.shouldClose = true;
     }
 
 	public void connectionTerminated() {
 		this.connectionTerminated = true;
         close();
 	}
 
     public boolean isEnd(String msg) {
         // Don't process an empty message.
        if (msg.length() == 0) {
             return false;
         }
 
         ArrayList<String> words = split(msg);
         String commandString = words.get(0);
         IrcProtocol.COMMAND command = getCommand(commandString);
 
         return command == IrcProtocol.COMMAND.QUIT;
     }
 
     public String processMessage(String msg) {
         // Silently drop an empty message.
         if (msg == null || msg.length() == 0 || this.connectionTerminated) {
             return null;
         }
 
         ArrayList<String> words = split(msg);
 
         if (isEnd(msg)) {
             return executeQuit(this.client, words);
         }
 
         // Get command.
         String commandString = words.get(0);
         IrcProtocol.COMMAND command = getCommand(commandString);
 
         // Set up new client if it has just connected.
         if (this.client.isNewClient()) {
             // Wait for NICK command if user hasn't done it yet.
             if ( ! this.client.hasNickname() ) {
                 if (command == IrcProtocol.COMMAND.NICK) {
                     return executeNick(this.client, words);
                 } else {
                     return reply(IrcProtocol.STATUS.NOTREGISTERED);
                 }
             // Wait for USER command afterwards.
             } else {
                 if ( ! this.client.hasUsername() ) {
                     if (command == IrcProtocol.COMMAND.USER) {
                         return executeUser(client, words);
                     } else {
                         return reply(IrcProtocol.STATUS.NOTREGISTERED);
                     }
                 }
             }
         // Process command if client has registered properly.
         } else if (this.client.canRegister()) {
             // COMMAND type message.
             if (command != null) {
                 return executeCommand(this.client, command, words);
             // DATA type message.
             } else {
                 if (this.client.isInChannel()) {
                     String data = buildDataMessage(words);
                     sendAll(client.getChannel(), client.getNickname(), data);
                 }
             }
         }
 
         return null;  // Don't reply anything back to the client.
     }
 
 
     /**
      * @param msg string to split into words list.
      *
      * @return words list.
      */
     public ArrayList<String> split(String msg) {
         ArrayList<String> lines = new ArrayList<String>();
 
         // Don't split an empty message.
         if (msg.length() == 0) {
             return lines;
         }
 
         // Get message without delimeter.
         String message = msg.substring(0, msg.length() - 1);
 
         // If message contains just a command, return [command].
         if (getCommand(message) != null) {
             lines.add(message);
             return lines;
         }
 
         Scanner s = new Scanner(msg);
         s.useDelimiter("\\s");
 
         boolean flag = true;
         StringBuilder str = new StringBuilder();
         while (s.hasNext()) {
             if (flag) {
                 lines.add(s.next());
                 flag = false;
             } else {
                 str.append(s.next());
                 str.append(" ");
             }
         }
 
         if (str.length() == 0) {
             return lines;
         }
 
         String words = str.toString();
         words = words.substring(0, words.length() - 1);
         lines.add(words);
 
         s.close();
 
         return lines;
     }
 
 
     /**
      * @param words words list to build data message from.
      *
      * @return data string to be sent to all users in channel.
      */
     public String buildDataMessage(ArrayList<String> words) {
         StringBuilder str = new StringBuilder();
 
         // Append words to string.
         for (String word : words) {
             str.append(word + " ");
         }
 
         String line = str.toString();
 
         line = line.substring(0, line.length() - 1);  // Remove last space char.
 
         return line;
     }
 
     /**
      * @param reply STATUS reply to send to client.
      * @param client client to send reply to.
      *
      * @return string to be sent back to client.
      */
     public static String reply(IrcProtocol.STATUS reply) {
         return reply.getNumber() + " :" + reply.getText();
     }
 
     /**
      * @param reply STATUS reply to send to client.
      * @param client client to send reply to.
      *
      * @return string to be sent back to client.
      */
     public static String numericReply(IrcProtocol.STATUS reply) {
         return reply.getNumber() + "";
     }
 
     /**
      * @param reply STATUS reply to send to client.
      * @param client client to send reply to.
      *
      * @return string to be sent back to client.
      */
     public static String textReply(IrcProtocol.STATUS reply) {
         return reply.getText();
     }
 
     /**
      * @param reply STATUS reply to send to client.
      * @param client client to send reply to.
      *
      * @return string to be sent back to client.
      */
     public static String replyNotEnoughParams(String command) {
         return IrcProtocol.STATUS.NEEDMOREPARAMS.getNumber() +
             " " + command + " :" +
             IrcProtocol.STATUS.NEEDMOREPARAMS.getText();
     }
 
     /**
      * @param reply STATUS reply to send to client.
      * @param client client to send reply to.
      *
      * @return string to be sent back to client.
      */
     public static String replyBrackets(
             IrcProtocol.STATUS reply, String brackets) {
 
         return reply.getNumber() + "<" + brackets + "> :" + reply.getText(); 
     }
 
 
     /**
      * @param command string to parse.
      *
      * @return COMMAND enumt type, or null if command given as argument didn't match anything.
      */
     public IrcProtocol.COMMAND getCommand(String command) {
         for (IrcProtocol.COMMAND type : COMMAND.values()) {
             if (command.equals(type.getText())) {
                 return type;
             }
         }
 
         return null;
     }
 
 
     /**
      * Executes command on client.
      *
      * @param client client to execute command on.
      * @param command command to activate.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     public String executeCommand(
             Client client, IrcProtocol.COMMAND command, ArrayList<String> words) {
 
         // Execute command.
         switch(command) {
             case NICK:
                 return this.executeNick(client, words);
             case USER:
                 return this.executeUser(client, words);
             case QUIT:
                 return this.executeQuit(client, words);
             case JOIN:
                 return this.executeJoin(client, words);
             case PART:
                 return this.executePart(client, words);
             case NAMES:
                 return this.executeNames(client, words);
             case LIST:
                 return this.executeList(client, words);
             case KICK:
                 return this.executeKick(client, words);
             default:  // command == null
                 return null;
         }
     }
 
 
     /**
      * Executes NICK Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeNick(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             return IrcProtocol.reply(IrcProtocol.STATUS.NONICKNAMEGIVEN);
         } else if (words.size() == 2) {
             if (this.client.isNicknameExist(words.get(1))) {
                 return IrcProtocol.replyBrackets(
                         IrcProtocol.STATUS.NICKNAMEINUSE,
                         words.get(1));
             } else {
                 client.setNickname(words.get(1));
                 return IrcProtocol.textReply(
                         IrcProtocol.STATUS.NICKACCEPTED);
             }
         } else {
             return null;
         }
     }
 
 
     /**
      * Executes USER Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeUser(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             return IrcProtocol.replyNotEnoughParams(
                     IrcProtocol.COMMAND.USER.getText());
         } else if (words.size() == 2) {
             if ( ! client.isUsernameExist() ) {
                 client.setUsername(words.get(1));
                 return IrcProtocol.textReply(IrcProtocol.STATUS.USERACCEPTED);
             } else {
                 return IrcProtocol.reply(IrcProtocol.STATUS.ALREADYREGISTERED);
             }
         } else {
             return null;
         }
     }
 
 
     /**
      * Executes JOIN Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeJoin(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             return IrcProtocol.replyNotEnoughParams(
                     IrcProtocol.COMMAND.JOIN.getText());
         } else {
             if (words.size() == 2) {
                 // Check for '#' before channel name.
                 String channelName = words.get(1);
                 if (channelName.charAt(0) != '#') {
                     return null;  // Silently drop illegal command.
                 }
 
                 channelName = channelName.substring(1, channelName.length());
 
                 // If client is trying to join the same channel he already is in,
                 // remove him from that channel.
                 if (client.isInChannel() &&
                         ! client.getChannel().getName().equals(channelName)) {
 
                     client.removeFromChannel();
                 }
 
                 Channel channel = Channel.getChannel(channelName);
 
                 // If this is a new (non-existent) channel,
                 // create channel and set client as chanop.
                 if (channel == null) {
                     Channel.addToChannel(channelName, client);
 
                     this.client.addToChannel(Channel.getChannel(channelName));
 
 
                     return client.getChannel().getNameReply(true);
                 // Add client to channel if it already exists.
                 } else {
                     client.addToChannel(channel);
                     return channel.getNameReply(true);
                 }
             } else {
                 return null;
             }
         }
     }
 
 
     /**
      * Executes QUIT Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeQuit(Client client, ArrayList<String> words) {
         // Send standard QUIT message.
         if (words.size() == 1) {
             if (client.isInChannel()) {
                 sendAllSystemMessage(
                         client.getChannel(),
                         "<" + client.getNickname() + "> has left the channel");
             }
         // Send custom QUIT message ("QUIT Goodbye world!").
         } else {
             sendAllSystemMessage(
                     client.getChannel(),
                     "<" + client.getNickname() + "> " + words.get(1)); 
         }
 
         client.removeFromChannel();
         Client.removeClient(this.client);
 
         System.out.println(
                 "Client " + this.client.getUsername() + "/" +
                 this.client.getNickname() + " has disconnected.");
 
         close();
 
         return "Goodbye.";
     }
 
 
     /**
      * Executes PART Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executePart(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             return IrcProtocol.replyNotEnoughParams(
                     IrcProtocol.COMMAND.PART.getText());
         } else if (words.size() == 2 && client.isInChannel()) {
             if (client.getChannel().getName().equals(words.get(1))) {
                 client.removeFromChannel();
 
                 return IrcProtocol.numericReply(
                         IrcProtocol.STATUS.PARTSUCCESS);
             } else {
                 return IrcProtocol.replyBrackets(
                         IrcProtocol.STATUS.NOSUCHCHANNEL,
                         words.get(1));
             }
         } else {
             return null;
         }
     }
 
 
     /**
      * Executes NAMES Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeNames(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             StringBuilder names = new StringBuilder();
 
             // Build string with all data.
             for (Channel channel : Channel.getChannels()) {
                 names.append(channel.getNameReply(false));
             }
             
             names.append(
                     reply(IrcProtocol.STATUS.ENDOFNAMES) + '\n'); 
 
             String finalNames = names.toString();
 
             return finalNames; 
         } else {
             String channelName = words.get(1);
 
             // Check if there is # before and id channel exist.
             channelName = channelName.substring(1, channelName.length());
             Channel channel = Channel.getChannel(channelName);
 
             if (channel != null) {
                 return channel.getNameReply(true);
             } else {
                 return IrcProtocol.replyBrackets(
                         IrcProtocol.STATUS.NOSUCHCHANNEL, channelName);
             }
         }
     }
     
 
     /**
      * Executes LIST Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeList(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             return Channel.getListReply();
         } else {
             return null;
         }
     }
 
     
     /**
      * Executes KICK Command.
      * 
      * @param client client to execute command on.
      * @param words rest of message received, to be executed depending on command.
      *
      * @return string to reply back to client.
      */
     private String executeKick(Client client, ArrayList<String> words) {
         if (words.size() == 1) {
             return IrcProtocol.replyNotEnoughParams(
                     IrcProtocol.COMMAND.KICK.getText());
         } else if (client.isInChannel()) {
             // check if admin requseting service
             if (client.getNickname().charAt(0) == '@') {
                 // check if user name in server
                 if (Client.isNicknameExist(words.get(1))) {
                     Client clientToKick = Client.getClient(words.get(1));
 
                     String adminName = client.getNickname().substring(
                             1, client.getNickname().length());
 
                     if ( ! clientToKick.isInChannel() ) {
                         return null;
                     }
 
                     String kickName = clientToKick.getChannel().getName();
 
                     // Check if admin and user in the same channel!
                     // and that admin dont try to kick himself
                     if (client.getChannel().getName().equals(kickName) &&
                             ! adminName.equals(words.get(1))) {
 
                         clientToKick.removeFromChannel();
                     }
                 }
             } else {
                 return IrcProtocol.STATUS.CHANOPRIVSNEEDED.getNumber() +
                     " #" + client.getChannel().getName() + " " +
                     IrcProtocol.STATUS.CHANOPRIVSNEEDED.getText();
             }
         }
 
         return null;
     }
 
 
     /**
      * @param channel channel to send message to.
      * @param nickname data message origin client nickname.
      * @param msg data message that the client has sent.
      */
     public synchronized void sendAll(
             Channel channel, String nickname, String msg) {
 
         for (Client client : channel.getClients()) {
             client.getConnectionHandler().addOutData(nickname + ": " + msg);
         }
     }
 
 
     /**
      * @param channel channel to send message to.
      * @param msg system message to be sent to all clients in channel.
      */
     public synchronized void sendAllSystemMessage(Channel channel, String msg) {
         for (Client client : channel.getClients()) {
             client.getConnectionHandler().addOutData(msg);
         }
     }
 }

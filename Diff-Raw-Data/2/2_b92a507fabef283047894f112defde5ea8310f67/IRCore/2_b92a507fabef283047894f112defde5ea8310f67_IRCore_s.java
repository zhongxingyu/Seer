 /**
  * Copyright (C) 2009-2012 Kenneth Prugh
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  */
 package irc;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import config.Config;
 
 public class IRCore {
 	/* Instance Vars */
 	private final String network;
 	private final int port;
 	private final String ident;
     private final String password;
 	private Socket s;
 	private BufferedReader in;
 	private BufferedWriter out;
 
 	/**
 	 * Create IRC object and connects to network
 	 * 
 	 * @param network
 	 *            - irc network to connect to
 	 * @param port
 	 *            - irc port
 	 * @param ident
 	 *            - irc username/identity
 	 */
 	public IRCore(Config config) {
 		this.network = config.getNetwork();
 		this.port = Integer.parseInt(config.getPort());
 		this.ident = config.getIdent();
         this.password = config.getIdentpassword();
 		s = connect();
 	}
 
 	/**
 	 * Create socket and connect to objects network/port
 	 */
 	private Socket connect() {
 		try {
 			s = new Socket(network, port);
 			s.setKeepAlive(true);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			setIn(new BufferedReader(new InputStreamReader(s.getInputStream())));
 			out = new BufferedWriter(
 					new OutputStreamWriter(s.getOutputStream()));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		sendMsgQueitly("NICK " + ident);
 		sendMsgQueitly("USER " + ident + " " + network + " bla :" + ident);
 		sendMsgQueitly("PRIVMSG NickServ :identify " + password);
 
 		return s;
 	}
 
 	/**
 	 * Closes this network socket and input/output steams
 	 * 
 	 * @throws IOException
 	 */
 	public void disconnect() throws IOException {
 		sendMsgUnfiltered("QUIT :System Hault");
 		s.close();
 		System.exit(0);
 	}
 
     /**
      * Recursive word wrap
      *
      * line - The remaining raw msg to be wrapped
      * limit - Max length of a line to be wrapped
      * wrapped - List of wrapped strings already processed
      */
     private List<String> rwrap(String line, int limit, List<String> wrapped)
     {
         line = line.trim();
 
         if (line.length() <= limit)
         {
             wrapped.add(line);
             return wrapped;
         }
         else
         {
             if (line.charAt(limit-1) == ' ')
             {
                 // We are in luck, we are splitting where a space is!
                 wrapped.add(line.substring(0, limit-1));
                 line = line.substring(limit);
                 return rwrap(line, limit, wrapped);
             }
             else
             {
                 int iter = limit-1;
                 // scan backwards until we find a space in the line
                while (line.charAt(iter) != ' ' && iter >= 0)
                 {
                     --iter;
                 }
                 // the line is one giant word, slice it at max len
                 if (iter == 0)
                 {
                     wrapped.add( line.substring(0, limit));
                     line = line.substring(limit);
                     return rwrap(line, limit, wrapped);
                 }
 
                 wrapped.add( line.substring(0, iter+1));
                 line = line.substring(iter+1);
                 return rwrap(line, limit, wrapped);
             }
         }
     }
 
     /**
      * Wraps the given string at the given length, and returns the resultant
      * list
      */
     private List<String> wrap(String line, int limit)
     {
         List<String> wrapped = new ArrayList<String>();
 
         return rwrap(line, limit, wrapped);
     }
 
 	/**
 	 * Private method to send irc messages to the socket
 	 * 
 	 * Adds '\r\n' to the end of messages.
 	 */
 	private void sendMsg(String target, String msg) {
         // Limit max msg length to 400 arbitarily until proper max length is
         // calculated for IRC
         int magic_limit = 400;
         if (msg.length() > magic_limit)
         {
             //wrap
             for (String line : wrap(msg, magic_limit))
             {
                 line = target + line + "\r\n";
                 System.out.println("Message: " + line);
                 try {
                     out.write(line);
                     out.flush();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         else
         {
             msg = target + msg + "\r\n";
             System.out.println("Message: " + msg);
             try {
                 out.write(msg);
                 out.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 	}
 
     /**
      * Send msg to socket without checkign for multiline or filtering it at all
      */
     private void sendMsgUnfiltered(String msg)
     {
         msg = msg + "\r\n";
         System.out.println("Message: " + msg);
         try {
             out.write(msg);
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 	/**
 	 * Private method to send irc messages to the socket
 	 * 
 	 * Adds '\r\n' to the end of messages.
      *
      * Not printed to console
 	 * 
 	 * @param msg
 	 *            - The msg to be sent
 	 */
 	private void sendMsgQueitly(String msg) {
 		msg = msg + "\r\n";
 		try {
 			out.write(msg);
 			out.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Send a notice to the specified user
 	 */
 	public void sendNotice(String user, String msg) {
 		String tmpMsg = "NOTICE " + user + " :" + msg;
 		sendMsgUnfiltered(tmpMsg);
 	}
 
 	/**
 	 * Send a message to the specified channel and user.
      *
      * Can handle multiline string
 	 */
 	public void sendMsgTo(String channel, String user, String msg) {
 		String tmpMsg = "PRIVMSG " + channel + " :" + user + ": ";
 		sendMsg(tmpMsg, msg);
 	}
 
 	/**
 	 * Send a query/private message to user
 	 */
 	public void sendPrivMsgTo(String user, String msg) {
 		String tmpMsg = "PRIVMSG " + user + " :";
 		sendMsg(tmpMsg, msg);
 	}
 
     /**
      * Send message to channel
      *
      * Currently unfiltered because only admin commands can msg a channel
      */
     public void sendMsgToChan(String channel, String msg) {
         sendMsgUnfiltered("PRIVMSG " + channel + " :" + msg);
     }
 
 	/**
 	 * Join specified channel on this network
 	 * 
 	 * @param chan
 	 *            - Channel to join (#example)
 	 */
 	public void joinChannel(String chan) {
 		String tmp = "JOIN " + chan;
 		sendMsgUnfiltered(tmp);
 	}
 
 	/**
 	 * Part specified channel on this network
 	 * 
 	 * @param chan
 	 *            - Channel to part (#example)
 	 */
 	public void partChannel(String chan) {
 		String tmp = "PART " + chan;
 		sendMsgUnfiltered(tmp);
 	}
 
 	public void setIn(BufferedReader in) {
 		this.in = in;
 	}
 
 	public BufferedReader getIn() {
 		return in;
 	}
 
     /**
      * PONG the specified server
      */
     public void doPong(String server) {
         sendMsgUnfiltered("PONG " + server);
     }
 }

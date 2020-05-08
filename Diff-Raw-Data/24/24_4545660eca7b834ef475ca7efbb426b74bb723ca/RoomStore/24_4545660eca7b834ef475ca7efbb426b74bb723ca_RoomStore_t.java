 /*
  * roomstore - an irc journaller using cassandra.
  *
  * Copyright 2011-2012 MeBigFatGuy.com
  * Copyright 2011-2012 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.roomstore;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import com.datastax.driver.core.Cluster;
 import com.datastax.driver.core.Session;
 
 public class RoomStore {
 
     private static final String NICK_NAME = "nick";
     private static final String IRCSERVER = "irc_server";
     private static final String CHANNELS = "channels";
     private static final String ENDPOINTS = "endpoints";
 
     public static void main(String[] args) {
         Options options = createOptions();
 
         try {
             CommandLineParser parser = new GnuParser();
             CommandLine cmdLine = parser.parse(options, args);
             String nickname = cmdLine.getOptionValue(NICK_NAME);
             String server = cmdLine.getOptionValue(IRCSERVER);
             String[] channels = cmdLine.getOptionValues(CHANNELS);
             String[] endPoints = cmdLine.getOptionValues(ENDPOINTS);
             
             if ((endPoints == null) || (endPoints.length == 0)) {
                 endPoints = new String[] { "127.0.0.1" };
             }
 
             IRCConnector connector = new IRCConnector(nickname, server, channels);
 
             Cluster cluster = new Cluster.Builder().addContactPoints(endPoints).build();
             final Session session = cluster.connect();
             
             CassandraWriter writer = new CassandraWriter(session);
             connector.setWriter(writer);
 
             connector.startRecording();
 
             Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                 public void run() {
                     session.shutdown();
                 }
             }));
 
         } catch (ParseException pe) {
 	    System.out.println("Parse Error on command line options:");
 	    System.out.println(commandLineRepresentation(args));
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp( "roomstore", options );
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static Options createOptions() {
         Options options = new Options();
 
         Option option = new Option(NICK_NAME, true, "nickname to use to access irc channels");
         option.setRequired(true);
         options.addOption(option);
 
         option = new Option(IRCSERVER, true, "irc server url");
         option.setRequired(true);
         options.addOption(option);
 
         option = new Option(CHANNELS, true, "space separated list of channels to connect to");
         option.setRequired(true);
         option.setArgs(100);
         options.addOption(option);
 
         option = new Option(ENDPOINTS, true, "space separated list of cassandra server server/ports");
         option.setOptionalArg(true);
         option.setRequired(false);
         option.setArgs(100);
         options.addOption(option);
 
         return options;
     }
 
     private static String commandLineRepresentation(String[] args) {
         StringBuilder sb = new StringBuilder();
         String space = "Roomstore ";
         for (String arg : args) {
             sb.append(space);
             space = " ";
             sb.append(arg);
         }
         
         return sb.toString();
     }
 }

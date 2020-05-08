 /*
     This file is part of PutBot.
 
     Copyright (c) 2010 Frank Osterfeld <frank.osterfeld@kdab.com>
 
     PutBot is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 3 of the License, or
     (at your option) any later version.
 
     Kleopatra is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
 
 package com.kdab.restbot;
 
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Vector;
 
 class InvalidConfigurationException extends Exception {
    private static final long serialVersionUID = 592054164059521060L;

     public InvalidConfigurationException( String msg ) {
         super( msg );
     }
 }
 
 public class Configuration {
     public Configuration() {
         m_nick = "RESTBot";
         m_roomsToJoin = new Vector<String>();
         m_roomsToJoin.add( "royalblue@conference.kdab.com" );
         m_routingRules = new Vector<RoutingRule>();
         Vector<Condition> cond1 = new Vector<Condition>();
         cond1.add( new Condition( "project", new Equals(), "frankskram" ) );
         RoutingRule rule1 = new RoutingRule( cond1, Message.ReceiverType.User, "frank@kdab.com" );
         Vector<Condition> cond2 = new Vector<Condition>();
         cond2.add( new Condition( "project", new Equals(), "make-o-matic" ) );
         RoutingRule rule2 = new RoutingRule( cond2, Message.ReceiverType.Room, "royalblue@conference.kdab.com" );
         m_routingRules.add( rule1 );
         m_routingRules.add( rule2 );
         m_account = new Account( "blobbot", "kdab.com", 5222, "bbmtwgr!" );
     }
 
     static String throwIfNull( Properties prop, String p ) throws InvalidConfigurationException {
         final String v = prop.getProperty( p );
         if ( v == null )
             throw new InvalidConfigurationException( String.format( "Required property \"%s\" not found!", p ) );
         return v;
     }
 
     public Configuration( Properties props ) throws IOException, InvalidConfigurationException {
         final String user = throwIfNull( props, "jabber.user" );
         final String server = throwIfNull( props, "jabber.server" );
         final String password = throwIfNull( props, "jabber.password" );
         final int port = Integer.parseInt( throwIfNull( props, "jabber.port" ) );
         m_account = new Account( user, server, port, password );
         final int roomCount = Integer.parseInt( throwIfNull( props, "jabber.roomsToJoin.count" ) );
         m_roomsToJoin = new Vector<String>();
         for ( int i = 0; i < roomCount; ++i )
             m_roomsToJoin.add( throwIfNull( props, "jabber.roomToJoin.n" + i ) );
         m_nick = props.getProperty( "jabber.room.nick", "RESTBot" );
         final int ruleCount = Integer.parseInt( throwIfNull( props, "jabber.routingRules.count" ) );
         m_routingRules = new Vector<RoutingRule>();
         for ( int i = 0; i < roomCount; ++i )
             m_routingRules.add( new RoutingRule( throwIfNull( props, "jabber.routingRules.n" + i ) ) );
     }
 
     public final Account account() {
         return m_account;
     }
 
     public final Vector<String> roomsToJoin() {
         return m_roomsToJoin;
     }
 
     public final String nick() {
         return m_nick;
     }
 
     public final Vector<RoutingRule> routingRules() {
         return m_routingRules;
     }
 
     private Vector<String> m_roomsToJoin;
     private Vector<RoutingRule> m_routingRules;
     private Account m_account;
     private String m_nick;
 }

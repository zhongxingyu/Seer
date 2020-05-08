 // Copyright (C) 2008 The Android Open Source Project
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.sonatype.shjgit;
 
 import java.util.HashMap;
 
 import org.apache.sshd.server.Command;
 import org.apache.sshd.server.CommandFactory;
 
 /** Creates a command implementation based on the client input. */
 public class GitCommandFactory implements CommandFactory {
     private final HashMap<String, Factory> commands;
 
     public GitCommandFactory() {
         commands = new HashMap<String, Factory>();
 
         commands.put( "git-receive-pack", new Factory() {
             @Override
             public AbstractCommand create() {
                 return new Receive();
             }
         } );
     }
 
     @Override
     public Command createCommand( String commandLine ) {
         int sp1 = commandLine.indexOf( ' ' );
         String cmd;
         String args;
         if ( 0 < sp1 ) {
             cmd = commandLine.substring( 0, sp1 );
             args = commandLine.substring( sp1 + 1 );
         } else {
             cmd = commandLine;
             args = "";
         }
 
         // Support newer-style "git receive-pack" requests by converting
         // to the older-style "git-receive-pack".
         //
         if ( "git".equals( cmd ) ) {
             cmd += "-";
             int sp2 = args.indexOf( ' ' );
             if ( 0 < sp2 ) {
                 cmd += args.substring( 0, sp2 );
                 args = args.substring( sp2 + 1 );
             } else {
                 cmd += args;
                 args = "";
             }
         }
 
         AbstractCommand command = create( cmd );
         command.parseArguments( cmd, args );
         return command;
     }
 
     private AbstractCommand create( String cmd ) {
         Factory factory = commands.get( cmd );
         if ( factory != null ) {
             return factory.create();
         }
 
         return new AbstractCommand() {
             @Override
             protected void run( String[] argv ) throws Failure {
                throw new Failure( 127, "gerrit: " + getName() + ": not found" );
             }
 
             @Override
             public void destroy() {
             }
         };
     }
 
     protected interface Factory {
         AbstractCommand create();
     }
 }

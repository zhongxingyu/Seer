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
 package com.sonatype.shjgit.core;
 
 import java.io.IOException;
 
 import org.eclipse.jgit.lib.PersonIdent;
 import org.eclipse.jgit.transport.ReceivePack;
 
 /** Receives change upload over SSH using the Git receive-pack protocol. */
 class Receive extends AbstractGitCommand {
     @Override
     protected void runImpl() throws IOException, Failure {
         ReceivePack rp = new ReceivePack( repo );
         rp.setAllowCreates( true );
         rp.setAllowDeletes( true );
         rp.setAllowNonFastForwards( true );
         rp.setCheckReceivedObjects( true );
         // TODO make this be a real email address!
         rp.setRefLogIdent( new PersonIdent( userAccount.getPrincipal().toString(),
                                             userAccount.getPrincipal().toString() + "@example.com" ) );
         rp.receive( in, out, err );
     }
 
     @Override
     protected String parseCommandLine( String[] args ) throws Failure {
         if ( 0 != args.length - 1 ) {
             throw usage();
         }
         return args[0];
     }
 
     private Failure usage() {
         StringBuilder builder = new StringBuilder();
         builder.append( "usage: " );
         builder.append( getName() );
         builder.append( " '/project.git'" );
         return new Failure( 1, builder.toString() );
     }
 
     @Override
     public void destroy() {
     }
 }

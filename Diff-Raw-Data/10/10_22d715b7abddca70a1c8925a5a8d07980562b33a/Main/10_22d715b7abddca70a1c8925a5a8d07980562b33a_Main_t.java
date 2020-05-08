 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.admin.nioneo;
 
 import org.neo4j.shell.ShellLobby;
 
 public class Main
 {
     public static void main( String args[] ) throws Exception
     {
         NioneoServer server = new NioneoServer( args[0] );
         server.addApp( LsNode.class );
         server.addApp( LsRel.class );
         server.addApp( LsProp.class );
         server.addApp( LsString.class );
         server.addApp( ListRels.class );
         server.addApp( RelDel.class );
         server.addApp( CheckRels.class );
         server.addApp( CheckAllRelChains.class );
         server.addApp( CheckRelChain.class );
         server.addApp( FindPropOwner.class );
         server.addApp( FindStartRecordsForNode.class );
         server.addApp( FindStartRelForRelNode.class );
         server.addApp( DumpPrevChainForRelNode.class );
         server.addApp( FixRels.class );
         server.addApp( FixNodeProps.class );
         // server.addApp( FixNodeProps2.class );
         server.addApp( Load.class );
         server.addApp( Ls.class );
         server.addApp( Set.class );
         server.addApp( Store.class );
         server.addApp( Toggle.class );
        server.addApp( FixAllRels.class );
        server.addApp( FixAllProps.class );
         ShellLobby.newClient( server ).grabPrompt();
         server.shutdown();
     }
 }

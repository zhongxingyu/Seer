 /*
 *  This file is part of xor.
  *  Copyright © 2009, Steen Manniche.
  * 
  *  xor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  xor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with xor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.manniche.xor.client.rmi;
 
 import java.io.IOException;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.List;
 import net.manniche.xor.types.DefaultDigitalObject;
 import net.manniche.xor.server.rmi.RMIObjectManagement;
 import net.manniche.xor.server.rmi.RMIRepositoryServer;
 import net.manniche.xor.types.DigitalObject;
 import net.manniche.xor.types.ObjectIdentifier;
 import net.manniche.xor.exceptions.RepositoryServiceException;
 import net.manniche.xor.types.BasicContentType;
 import net.manniche.xor.types.ObjectRepositoryContentType;
 
 
 /**
  *
  * @author stm
  */
 public class RMIObjectRepositoryClient implements RMIObjectManagementClient
 {
 
     private RMIObjectManagement server;
 
     protected void connect( String serverName, int port )
     {
         try
         {
             Registry registry = LocateRegistry.getRegistry();
             Remote remote = registry.lookup( RMIRepositoryServer.class.getName() );
             server = RMIObjectManagement.class.cast( remote );
         }
         catch( java.rmi.ConnectException ce )
         {
             System.err.println( String.format( "Error: server not started: %s", ce.getMessage() ) );
         }
         catch( java.rmi.ConnectIOException cioe )
         {
             System.err.println( String.format( "Error: Cannot connect to server at %s: %s", serverName, cioe.getMessage() ) );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
     }
 
 
     public static void main( String[] args ) throws RemoteException, RepositoryServiceException
     {
 
         RMIObjectRepositoryClient client = new RMIObjectRepositoryClient();
 
         client.connect( "localhost", 8181 );
 
         long timer = System.currentTimeMillis();
         int number_of_objects = 1000;
         for( int i = 0; i < number_of_objects; i++ )
         {
             ObjectIdentifier id = client.saveObject( "æøåßüöï".getBytes() );
             client.getObject( id );
             client.deleteObject( id );
         }
         timer = System.currentTimeMillis() - timer;
 
         double time = timer/1000.0;
 
         double stat = number_of_objects/time;
 
         System.out.println( String.format( "Stored, retrived and deleted %s objects in %s seconds (%.4f objects/second)", number_of_objects, time, stat ) );
 
         System.exit( 0 );
     }
 
     public DigitalObject getObject( ObjectIdentifier id ) throws RemoteException, RepositoryServiceException
     {
         return server.getRepositoryObject( id );
     }
 
     public void deleteObject( ObjectIdentifier id ) throws RemoteException, RepositoryServiceException
     {
         server.deleteRepositoryObject( id, "deleting" );
     }
 
     @Override
     public ObjectIdentifier saveObject( byte[] data ) throws RemoteException
     {
         DefaultDigitalObject digo = new DefaultDigitalObject( data, BasicContentType.DUBLIN_CORE );
        ObjectIdentifier objectID = null;
         try
         {
             objectID = server.storeRepositoryObject( digo, digo.getContentType(), "saving object");
 
         }
         catch( IOException ex )
         {
             System.out.println( String.format( "Bullocks!: %s", ex.getMessage() ) );
         }
 
        return objectID;
     }
 
     public List<ObjectRepositoryContentType> getContentTypes() throws RemoteException
     {
         return server.registeredContentTypes();
     }
 }

 package de.zib.gndms.kit.network;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import de.zib.gndms.kit.access.CredentialProvider;
 import de.zib.gndms.stuff.threading.Forkable;
 import de.zib.gndms.stuff.threading.QueuedExecutor;
 import org.apache.log4j.Logger;
 import org.globus.ftp.GridFTPClient;
 import org.globus.ftp.exception.ServerException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.*;
 
 /**
  * @author try ma ik jo rr a zib
  * 
  * @version $Id$
  *          <p/>
  *          User: mjorra, Date: 20.02.2009, Time: 17:37:59
  */
 public class StrictNonblockingClientFactory extends AbstractNonblockingClientFactory {
 
     private final TimeUnit unit = TimeUnit.SECONDS;
     private ExecutorService exec = Executors.newFixedThreadPool( 1 );
 
 
     public GridFTPClient createClient( String host, int port, CredentialProvider cp ) throws ServerException, IOException {
 
         final GridFTPClientCreator creator = new GridFTPClientCreator( host, port, cp, inc() );
         final Forkable<GridFTPClient> fork = new Forkable<GridFTPClient>( creator );
 
 
         final Future<GridFTPClient> f;
         log.info( "submitting creator " +creator.getHost() +" " + creator.getSeq() );
         synchronized ( exec ) {
             f = exec.submit( fork );
             //f = exec.submit( creator );
         }
 
         try {
             try{
                return f.get( timeout, unit );
             } catch ( TimeoutException e ) {
                 creator.getLog().info( "GridFTPClient get() create exceeded timeout", e );
                 fork.setShouldStop( true );
                 f.cancel( true );
             }
         } catch ( InterruptedException e ) {
             creator.getLog().debug( e );
             throw new RuntimeException( "GridFTPClient create interrupted", e );
         } catch ( ExecutionException e ) {
             // this mustn't happen here due to the blocked wait op
             creator.getLog().debug( e );
             if( e.getCause() instanceof ServerException )
                 throw ServerException.class.cast( e.getCause() );
 
             throw new RuntimeException( e );
         }
         return null;
     }
 }

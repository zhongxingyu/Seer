 /*
  * Copyright 2008-2010 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
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
 
 package com.amazon.carbonado.repo.dirmi;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import java.util.concurrent.TimeUnit;
 
 import org.cojen.dirmi.Asynchronous;
 import org.cojen.dirmi.Batched;
 import org.cojen.dirmi.CallMode;
 import org.cojen.dirmi.Pipe;
 import org.cojen.dirmi.RemoteFailure;
 
 import com.amazon.carbonado.IsolationLevel;
 import com.amazon.carbonado.RepositoryException;
 
 /**
  * Remote repository definition which is RMI compliant but does not actually
  * extend the Repository interface. Client class is responsible for adapting
  * Repository calls to RemoteRepository calls.
  *
  * @author Brian S O'Neill
  * @see ClientRepository
  * @see RemoteRepositoryServer
  */
 public interface RemoteRepository extends Remote {
     @RemoteFailure(exception=RepositoryException.class)
     String getName() throws RepositoryException;
 
     @RemoteFailure(exception=RepositoryException.class)
     RemoteStorageTransport storageFor(StorableTypeTransport transport)
         throws RepositoryException;
 
     @Asynchronous(CallMode.REQUEST_REPLY)
     Pipe storageRequest(StorageResponse response, Pipe pipe) throws RemoteException;
 
     public static interface StorageResponse extends Remote {
         @Asynchronous
         void complete(RemoteStorageTransport storage) throws RemoteException;
 
         @Asynchronous
         void exception(Throwable cause) throws RemoteException;
     }
 
     @Batched
     @RemoteFailure(exception=RepositoryException.class, declared=false)
     RemoteTransaction enterTransaction(RemoteTransaction parent, IsolationLevel level);
 
     @Batched
     @RemoteFailure(exception=RepositoryException.class, declared=false)
     RemoteTransaction enterTransaction(RemoteTransaction parent, IsolationLevel level,
                                        int timeout, TimeUnit unit);
 
     @Batched
     @RemoteFailure(exception=RepositoryException.class, declared=false)
     RemoteTransaction enterTopTransaction(IsolationLevel level);
 
     @Batched
     @RemoteFailure(exception=RepositoryException.class, declared=false)
     RemoteTransaction enterTopTransaction(IsolationLevel level, int timeout, TimeUnit unit);
 
     @RemoteFailure(exception=RepositoryException.class)
     RemoteSequenceValueProducer getSequenceValueProducer(String name)
         throws RepositoryException;
     
     RemoteResyncCapability getResyncCapability() throws RemoteException;
 
     /**
      * Client repository should call this once and re-use the returned instance.
      */
     @RemoteFailure(exception=RepositoryException.class)
     RemoteProcedureExecutor newRemoteProcedureExecutor(RemoteStorageRequestor r)
         throws RepositoryException;
 }

 package de.zib.gndms.logic.model.gorfx;
 
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
 
 
 
 import de.zib.gndms.kit.network.GNDMSFileTransfer;
 import de.zib.gndms.kit.network.NetworkAuxiliariesProvider;
 import de.zib.gndms.model.gorfx.FTPTransferState;
 import de.zib.gndms.model.gorfx.types.FileTransferORQ;
 import de.zib.gndms.model.gorfx.types.FileTransferResult;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.neomodel.common.NeoDao;
 import de.zib.gndms.neomodel.common.NeoSession;
 import de.zib.gndms.neomodel.gorfx.NeoTask;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import org.apache.axis.types.URI;
 import org.globus.ftp.GridFTPClient;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version  $Id$
  * <p/>
  * User: mjorra, Date: 01.10.2008, Time: 17:57:57
  */
 public class FileTransferTaskAction extends ORQTaskAction<FileTransferORQ> {
 
     private FTPTransferState transferState;
     private FileTransferORQ orq;
 
 
     public FileTransferTaskAction() {
         super();
     }
 
     public FileTransferTaskAction(@NotNull EntityManager em, @NotNull NeoDao dao, @NotNull Taskling model) {
         super(em, dao, model);
     }
 
     @Override
     @NotNull
     public Class<FileTransferORQ> getOrqClass() {
         return FileTransferORQ.class;
     }
 
 
     @Override
     protected void onInProgress(@NotNull String wid,
                                 @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState)
             throws Exception {
         TreeMap<String,String> files;
         GridFTPClient src;
         GridFTPClient dest;
 
 
         NeoSession session = getDao().beginSession();
         try {
             NeoTask task  = getTask(session);
             transferState = (FTPTransferState) task.getPayload();
             orq           = ((FileTransferORQ)task.getORQ());
             files         = orq.getFileMap();
             if( transferState == null )
                 newTransfer( task );
             else {
                 String s = transferState.getCurrentFile();
                 if( s != null ) {
                     int p =  new ArrayList<String>( files.keySet() ).indexOf( s );
                     task.setProgress( p );
                 }
             }
             session.success();
         }
         finally { session.finish(); }
 
 
         TaskPersistentMarkerListener pml = new TaskPersistentMarkerListener( );
         pml.setDao( getDao() );
         pml.setTransferState( transferState );
         pml.setTaskling(getModel());
         pml.setWid(wid);
         pml.setGORFXId(orq.getActId());
 
         URI suri = new URI ( orq.getSourceURI() );
         URI duri = new URI ( orq.getTargetURI() );
 
         // obtain clients
         src = NetworkAuxiliariesProvider.getGridFTPClientFactory().createClient( suri, getCredentialProvider() );
 
         try {
             dest = NetworkAuxiliariesProvider.getGridFTPClientFactory().createClient( duri, getCredentialProvider() );
 
             try {
                 // setup transfer handler
                 GNDMSFileTransfer transfer = new GNDMSFileTransfer();
                 transfer.setSourceClient( src );
                 transfer.setSourcePath( suri.getPath() );
 
                 transfer.setDestinationClient( dest );
                 transfer.setDestinationPath( duri.getPath() );
 
                 transfer.setFiles( files );
 
                 transfer.prepareTransfer();
 
                 int fc = transfer.getFiles( ).size( );
 
                 session = getDao().beginSession();
                 try {
                     getTask(session).setMaxProgress( fc );
                     session.success();
                 }
                 finally { session.finish(); }
 
                 transfer.performPersistentTransfer( pml );
 
                 FileTransferResult ftr = new FileTransferResult();
 
                 ArrayList<String> al = new ArrayList<String>( transfer.getFiles( ).keySet( ) );
                 ftr.setFiles( al.toArray( new String[al.size( )] ));
 
                 transitWithPayload(ftr, TaskState.FINISHED);
                 if (altTaskState)
                     getDao().removeAltTaskState(getModel().getId());
             }
             finally { dest.close(); }
         }
         finally { src.close(); }
     }
 
     private void newTransfer( @NotNull NeoTask task ) {
         transferState = new FTPTransferState();
         transferState.setTransferId( getModel().getId() );
         task.setPayload( transferState );
     }

    
 }

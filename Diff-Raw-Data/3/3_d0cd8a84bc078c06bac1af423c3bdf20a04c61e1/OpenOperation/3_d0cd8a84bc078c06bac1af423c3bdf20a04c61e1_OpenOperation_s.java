 /*  Copyright (c) 2008,2009 Konrad-Zuse-Zentrum fuer Informationstechnik Berlin.
 
  This file is part of XtreemFS. XtreemFS is part of XtreemOS, a Linux-based
  Grid Operating System, see <http://www.xtreemos.eu> for more details.
  The XtreemOS project has been developed with the financial support of the
  European Commission's IST program under contract #FP6-033576.
 
  XtreemFS is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 2 of the License, or (at your option)
  any later version.
 
  XtreemFS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with XtreemFS. If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * AUTHORS: Bjoern Kolbeck (ZIB), Jan Stender (ZIB)
  */
 
 package org.xtreemfs.mrc.operations;
 
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 
 import org.xtreemfs.common.Capability;
 import org.xtreemfs.common.TimeSync;
 import org.xtreemfs.common.logging.Logging;
 import org.xtreemfs.common.logging.Logging.Category;
 import org.xtreemfs.foundation.ErrNo;
 import org.xtreemfs.interfaces.Constants;
 import org.xtreemfs.interfaces.FileCredentials;
 import org.xtreemfs.interfaces.Replica;
 import org.xtreemfs.interfaces.ReplicaSet;
 import org.xtreemfs.interfaces.SnapConfig;
 import org.xtreemfs.interfaces.XLocSet;
 import org.xtreemfs.interfaces.MRCInterface.openRequest;
 import org.xtreemfs.interfaces.MRCInterface.openResponse;
 import org.xtreemfs.mrc.ErrorRecord;
 import org.xtreemfs.mrc.MRCRequest;
 import org.xtreemfs.mrc.MRCRequestDispatcher;
 import org.xtreemfs.mrc.UserException;
 import org.xtreemfs.mrc.ErrorRecord.ErrorClass;
 import org.xtreemfs.mrc.ac.FileAccessManager;
 import org.xtreemfs.mrc.database.AtomicDBUpdate;
 import org.xtreemfs.mrc.database.StorageManager;
 import org.xtreemfs.mrc.database.VolumeInfo;
 import org.xtreemfs.mrc.database.VolumeManager;
 import org.xtreemfs.mrc.metadata.FileMetadata;
 import org.xtreemfs.mrc.metadata.XLocList;
 import org.xtreemfs.mrc.utils.Converter;
 import org.xtreemfs.mrc.utils.MRCHelper;
 import org.xtreemfs.mrc.utils.Path;
 import org.xtreemfs.mrc.utils.PathResolver;
 
 /**
  * 
  * @author stender
  */
 public class OpenOperation extends MRCOperation {
     
     private PrintWriter logfile;
     
     public OpenOperation(MRCRequestDispatcher master) {
         super(master);
         /*
          * try { logfile = new PrintWriter(new
          * FileOutputStream("/var/lib/xtreemfs/open.log",true)); } catch
          * (IOException ex) { ex.printStackTrace(); }
          */
     }
     
     @Override
     public void startRequest(MRCRequest rq) throws Throwable {
         
         final openRequest rqArgs = (openRequest) rq.getRequestArgs();
         
         final VolumeManager vMan = master.getVolumeManager();
         final FileAccessManager faMan = master.getFileAccessManager();
         
         Path p = new Path(rqArgs.getVolume_name() + "/" + rqArgs.getPath());
         
         StorageManager sMan = vMan.getStorageManagerByName(p.getComp(0));
         PathResolver res = new PathResolver(sMan, p);
         VolumeInfo volume = sMan.getVolumeInfo();
         
         // check whether the path prefix is searchable
         faMan.checkSearchPermission(sMan, res, rq.getDetails().userId, rq.getDetails().superUser, rq
                 .getDetails().groupIds);
         
         AtomicDBUpdate update = sMan.createAtomicDBUpdate(master, rq);
         FileMetadata file = null;
         
         // analyze the flags
         boolean create = (rqArgs.getFlags() & FileAccessManager.O_CREAT) != 0;
         boolean excl = (rqArgs.getFlags() & FileAccessManager.O_EXCL) != 0;
         boolean truncate = (rqArgs.getFlags() & FileAccessManager.O_TRUNC) != 0;
         boolean write = (rqArgs.getFlags() & (FileAccessManager.O_WRONLY | FileAccessManager.O_RDWR)) != 0;
         
         boolean createNew = false;
         
         // check whether the file/directory exists
         try {
             
             res.checkIfFileDoesNotExist();
             
             // check if O_CREAT and O_EXCL are set; if so, send an exception
             if (create && excl)
                 res.checkIfFileExistsAlready();
             
             file = res.getFile();
             
             // if the file refers to a symbolic link, resolve the link
             String target = sMan.getSoftlinkTarget(file.getId());
             if (target != null) {
                 rqArgs.setPath(target);
                 p = new Path(target);
                 
                 // if the local MRC is not responsible, send a redirect
                 if (!vMan.hasVolume(p.getComp(0))) {
                     finishRequest(rq, new ErrorRecord(ErrorClass.USER_EXCEPTION, ErrNo.ENOENT, "link target "
                         + target + " does not exist"));
                     return;
                 }
                 
                 sMan = vMan.getStorageManagerByName(p.getComp(0));
                 volume = sMan.getVolumeInfo();
                 res = new PathResolver(sMan, p);
                 file = res.getFile();
             }
             
             if (file.isDirectory())
                 throw new UserException(ErrNo.EISDIR, "open is restricted to files");
             
             // check whether the file is marked as 'read-only'; in this
             // case, throw an exception if write access is requested
             if (file.isReadOnly()
                 && ((rqArgs.getFlags() & (FileAccessManager.O_RDWR | FileAccessManager.O_WRONLY
                     | FileAccessManager.O_TRUNC | FileAccessManager.O_APPEND)) != 0))
                 throw new UserException(ErrNo.EPERM, "read-only files cannot be written");
             
             // check whether the permission is granted
             faMan.checkPermission(rqArgs.getFlags(), sMan, file, res.getParentDirId(),
                 rq.getDetails().userId, rq.getDetails().superUser, rq.getDetails().groupIds);
             
         } catch (UserException exc) {
             
             // if the file does not exist, check whether the O_CREAT flag
             // has been provided
             if (exc.getErrno() == ErrNo.ENOENT && create) {
                 
                 // check for write permission in parent dir
                 // check whether the parent directory grants write access
                 faMan.checkPermission(FileAccessManager.O_WRONLY, sMan, res.getParentDir(), res
                         .getParentsParentId(), rq.getDetails().userId, rq.getDetails().superUser, rq
                         .getDetails().groupIds);
                 
                 // get the next free file ID
                 long fileId = sMan.getNextFileId();
                 
                 // atime, ctime, mtime
                 int time = (int) (TimeSync.getGlobalTime() / 1000);
                 
                 // create the metadata object
                 file = sMan.createFile(fileId, res.getParentDirId(), res.getFileName(), time, time, time, rq
                         .getDetails().userId, rq.getDetails().groupIds.get(0), rqArgs.getMode(), rqArgs
                         .getAttributes(), 0, false, 0, 0, update);
                 
                 // set the file ID as the last one
                 sMan.setLastFileId(fileId, update);
                 
                 createNew = true;
             }
 
             else
                 throw exc;
         }
         
         // get the current epoch, use (and increase) the truncate number if
         // the open mode is truncate
         int trEpoch = file.getEpoch();
         if (truncate) {
             file.setIssuedEpoch(file.getIssuedEpoch() + 1);
             trEpoch = file.getIssuedEpoch();
             sMan.setMetadata(file, FileMetadata.RC_METADATA, update);
         }
         
         XLocList xLocList = file.getXLocList();
         XLocSet xLocSet = null;
         
         // if no replicas have been assigned yet, assign a new replica
         if ((xLocList == null || xLocList.getReplicaCount() == 0) && (create || write)) {
             
             // create a replica with the default striping policy together
             // with a set of feasible OSDs from the OSD status manager
             Replica replica = MRCHelper.createReplica(null, sMan, master.getOSDStatusManager(), volume, res
                     .getParentDirId(), rqArgs.getPath(), ((InetSocketAddress) rq.getRPCRequest()
                     .getClientIdentity()).getAddress(), rqArgs.getClient_vivaldi_coordinates(), xLocList, 0);
             
             ReplicaSet replicas = new ReplicaSet();
             replicas.add(replica);
             
             xLocSet = new XLocSet(0, replicas, file.isReadOnly() ? Constants.REPL_UPDATE_PC_RONLY
                 : Constants.REPL_UPDATE_PC_NONE, 0);
             xLocList = Converter.xLocSetToXLocList(sMan, xLocSet);
             
             file.setXLocList(xLocList);
             sMan.setMetadata(file, FileMetadata.RC_METADATA, update);
             
             if (Logging.isDebug())
                 Logging.logMessage(Logging.LEVEL_DEBUG, Category.proc, this,
                     "assigned the following XLoc list to file %s:%d: %s", volume.getId(), file.getId(),
                     xLocSet.toString());
         }
 
         else {
             assert (xLocList != null) : "The requested file has no xLocList!";
             
             xLocSet = Converter.xLocListToXLocSet(xLocList);
            xLocSet.setReplica_update_policy(file.isReadOnly() ? Constants.REPL_UPDATE_PC_RONLY
                : Constants.REPL_UPDATE_PC_NONE);
             if (file.isReadOnly())
                 xLocSet.setRead_only_file_size(file.getSize());
         }
         
         // set 'replicateOnClose' if the policy is set accordingly
         boolean replicateOnClose = (create || write) && volume.getAutoReplFactor() > 1;
         
         // re-order the replica list, based on the replica selection policy
         xLocSet.setReplicas(master.getOSDStatusManager().getSortedReplicaList(volume.getId(),
             ((InetSocketAddress) rq.getRPCRequest().getClientIdentity()).getAddress(),
             rqArgs.getClient_vivaldi_coordinates(), xLocList));
         
         // issue a new capability
         Capability cap = new Capability(volume.getId() + ":" + file.getId(), rqArgs.getFlags(), master
                 .getConfig().getCapabilityTimeout(), TimeSync.getGlobalTime() / 1000
             + master.getConfig().getCapabilityTimeout(), ((InetSocketAddress) rq.getRPCRequest()
                 .getClientIdentity()).getAddress().getHostAddress(), trEpoch, replicateOnClose, !volume
                 .isSnapshotsEnabled() ? SnapConfig.SNAP_CONFIG_SNAPS_DISABLED
             : volume.isSnapVolume() ? SnapConfig.SNAP_CONFIG_ACCESS_SNAP
                 : SnapConfig.SNAP_CONFIG_ACCESS_CURRENT, volume.getCreationTime(), master.getConfig()
                 .getCapabilitySecret());
         
         if (Logging.isDebug())
             Logging
                     .logMessage(Logging.LEVEL_DEBUG, Category.proc, this,
                         "issued the following capability for %s:%d: %s", volume.getId(), file.getId(), cap
                                 .toString());
         
         // update POSIX timestamps of file
         
         // create or truncate: ctime + mtime, atime only if create
         if (createNew || truncate)
             MRCHelper.updateFileTimes(res.getParentsParentId(), file, createNew ? !master.getConfig()
                     .isNoAtime() : false, true, true, sMan, update);
         
         // write: ctime + mtime
         else if (write)
             MRCHelper.updateFileTimes(res.getParentsParentId(), file, false, true, true, sMan, update);
         
         // otherwise: only atime, if necessary
         else if (!master.getConfig().isNoAtime())
             MRCHelper.updateFileTimes(res.getParentsParentId(), file, true, false, false, sMan, update);
         
         // update POSIX timestamps of parent directory, in case of a newly
         // created file
         if (createNew)
             MRCHelper.updateFileTimes(res.getParentsParentId(), res.getParentDir(), false, true, true, sMan,
                 update);
         
         // set the response
         rq.setResponse(new openResponse(new FileCredentials(cap.getXCap(), xLocSet)));
         
         update.execute();
         
         // enable only for test servers that should log each file
         // create/write/trunc
         /*
          * if (create || write || truncate) { try {
          * logfile.print(System.currentTimeMillis
          * ()+";"+rq.getRPCRequest().getClientIdentity
          * ()+";"+rqArgs.getPath()+"\n"); logfile.flush(); } catch (Exception
          * ex) {
          * 
          * } }
          */
     }
 }

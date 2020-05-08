 /*
  * Copyright 2011-2012 Gregory P. Moyer
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
 package org.syphr.mythtv.api.backend;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URL;
 import java.nio.channels.ByteChannel;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.syphr.mythtv.api.util.AbstractCachedConnection;
 import org.syphr.mythtv.commons.exception.CommandException;
 import org.syphr.mythtv.commons.socket.SocketManager;
 import org.syphr.mythtv.commons.unsupported.UnsupportedHandler;
 import org.syphr.mythtv.data.Channel;
 import org.syphr.mythtv.data.DriveInfo;
 import org.syphr.mythtv.data.FileEntry;
 import org.syphr.mythtv.data.FileInfo;
 import org.syphr.mythtv.data.Load;
 import org.syphr.mythtv.data.MemStats;
 import org.syphr.mythtv.data.PixMap;
 import org.syphr.mythtv.data.Program;
 import org.syphr.mythtv.data.RecorderDevice;
 import org.syphr.mythtv.data.RecorderLocation;
 import org.syphr.mythtv.data.RecordingsInProgress;
 import org.syphr.mythtv.data.TimeInfo;
 import org.syphr.mythtv.data.UpcomingRecordings;
 import org.syphr.mythtv.data.VideoEditInfo;
 import org.syphr.mythtv.protocol.ConnectionType;
 import org.syphr.mythtv.protocol.EventLevel;
 import org.syphr.mythtv.protocol.InvalidProtocolVersionException;
 import org.syphr.mythtv.protocol.Protocol;
 import org.syphr.mythtv.protocol.ProtocolSocketManager;
 import org.syphr.mythtv.protocol.ProtocolVersion;
 import org.syphr.mythtv.protocol.QueryFileTransfer;
 import org.syphr.mythtv.protocol.QueryRecorder;
 import org.syphr.mythtv.protocol.QueryRemoteEncoder;
 import org.syphr.mythtv.protocol.events.BackendEventListener;
 import org.syphr.mythtv.types.FileTransferType;
 import org.syphr.mythtv.types.RecordingCategory;
 import org.syphr.mythtv.types.Verbose;
 
 /**
  * This class maintains cached connection information to automatically connect
  * and disconnect as necessary to fulfill protocol requests.
  * 
  * @author Gregory P. Moyer
  */
 public class CachedProtocol extends AbstractCachedConnection implements Protocol
 {
     /**
      * The socket manager that controls the underlying connection.
      */
     private final SocketManager socketManager;
 
     /**
      * The protocol in use.
      */
     private final Protocol delegate;
 
     /**
      * The name of the local machine.
      */
     private String localHost;
 
     /**
      * The name of the server.
      */
     private String remoteHost;
 
     /**
      * The port on the server that is accepting protocol connections.
      */
     private int remotePort;
 
     public CachedProtocol(long timeout, TimeUnit unit)
     {
         this(ProtocolVersion.values()[0], timeout, unit);
     }
 
     public CachedProtocol(ProtocolVersion version, long timeout, TimeUnit unit)
     {
         super(timeout, unit);
 
         socketManager = new AutoReConnectingSocketManager(new ProtocolSocketManager());
         delegate = new AutomaticProtocol(version, socketManager);
     }
 
     public synchronized void setConnectionParameters(String localHost,
                                                      String remoteHost,
                                                      int remotePort) throws IOException
     {
         this.localHost = localHost;
         this.remoteHost = remoteHost;
         this.remotePort = remotePort;
 
         /*
          * Confirm connection works by attempting now.
          */
         connectIfNecessary();
     }
 
     private synchronized void connectIfNecessary() throws IOException
     {
         if (isConnectionShutdown())
         {
             throw new IOException("Connection has been permanently shutdown");
         }
 
         if (isConnected())
         {
             return;
         }
 
         delegate.connect(remoteHost, remotePort, getTimeout() / 2L);
         try
         {
             delegate.mythProtoVersion();
         }
         catch (InvalidProtocolVersionException e)
         {
             throw new IOException(e);
         }
 
         /*
          * Playback is the only sane choice here since this protocol instance
          * automatically disconnects when not in use (which would make it
          * useless for monitoring events).
          */
         delegate.ann(ConnectionType.PLAYBACK, localHost, EventLevel.NONE);
     }
 
     @Override
     protected synchronized void disconnect()
     {
         try
         {
             delegate.done();
         }
         catch (IOException e)
         {
             /*
              * Ignore this - it's time to get rid of the connection anyway.
              */
         }
     }
 
     @Override
     public synchronized boolean isConnected()
     {
         return delegate.isConnected();
     }
 
     @Override
     public void setUnsupportedHandler(UnsupportedHandler handler)
     {
         delegate.setUnsupportedHandler(handler);
     }
 
     @Override
     public <E extends Enum<E>> List<E> getAvailableTypes(Class<E> type)
     {
         return delegate.getAvailableTypes(type);
     }
 
     @Override
     public Protocol newProtocol() throws IOException
     {
         connectIfNecessary();
         return delegate.newProtocol();
     }
 
     @Override
     public SocketManager getSocketManager()
     {
         return socketManager;
     }
 
     @Override
     public synchronized void done() throws IOException
     {
        if (isConnected())
         {
             return;
         }
 
         delegate.done();
     }
 
     @Override
     public void allowShutdown() throws IOException
     {
         connectIfNecessary();
         delegate.allowShutdown();
     }
 
     @Override
     public void blockShutdown() throws IOException
     {
         connectIfNecessary();
         delegate.blockShutdown();
     }
 
     @Override
     public int checkRecording(Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.checkRecording(program);
     }
 
     @Override
     public boolean deleteFile(URI filename, String storageGroup) throws IOException
     {
         connectIfNecessary();
         return delegate.deleteFile(filename, storageGroup);
     }
 
     @Override
     public void deleteRecording(Channel channel, Date recStartTs, boolean force, boolean forget) throws IOException,
                                                                                                 CommandException
     {
         connectIfNecessary();
         delegate.deleteRecording(channel, recStartTs, force, forget);
     }
 
     @Override
     public URI downloadFile(URL url, String storageGroup, URI filename) throws IOException,
                                                                        CommandException
     {
         connectIfNecessary();
         return delegate.downloadFile(url, storageGroup, filename);
     }
 
     @Override
     public URI downloadFileNow(URL url, String storageGroup, URI filename) throws IOException,
                                                                           CommandException
     {
         connectIfNecessary();
         return delegate.downloadFileNow(url, storageGroup, filename);
     }
 
     @Override
     public Program fillProgramInfo(String host, Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.fillProgramInfo(host, program);
     }
 
     @Override
     public void forgetRecording(Program program) throws IOException
     {
         connectIfNecessary();
         delegate.forgetRecording(program);
     }
 
     @Override
     public boolean freeTuner(int recorderId) throws IOException
     {
         connectIfNecessary();
         return delegate.freeTuner(recorderId);
     }
 
     @Override
     public RecorderLocation getFreeRecorder() throws IOException
     {
         connectIfNecessary();
         return delegate.getFreeRecorder();
     }
 
     @Override
     public int getFreeRecorderCount() throws IOException
     {
         connectIfNecessary();
         return delegate.getFreeRecorderCount();
     }
 
     @Override
     public List<Integer> getFreeRecorderList() throws IOException
     {
         connectIfNecessary();
         return delegate.getFreeRecorderList();
     }
 
     @Override
     public RecorderLocation getNextFreeRecorder(RecorderLocation from) throws IOException
     {
         connectIfNecessary();
         return delegate.getNextFreeRecorder(from);
     }
 
     @Override
     public RecorderLocation getRecorderFromNum(int recorderId) throws IOException, CommandException
     {
         connectIfNecessary();
         return delegate.getRecorderFromNum(recorderId);
     }
 
     @Override
     public RecorderLocation getRecorderNum(Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.getRecorderNum(program);
     }
 
     @Override
     public void goToSleep() throws IOException, CommandException
     {
         connectIfNecessary();
         delegate.goToSleep();
     }
 
     @Override
     public RecorderDevice lockTuner(int recorderId) throws IOException, CommandException
     {
         connectIfNecessary();
         return delegate.lockTuner(recorderId);
     }
 
     @Override
     public void messageClearSettingsCache() throws IOException
     {
         connectIfNecessary();
         delegate.messageClearSettingsCache();
     }
 
     @Override
     public void messageResetIdleTime() throws IOException
     {
         connectIfNecessary();
         delegate.messageResetIdleTime();
     }
 
     @Override
     public void messageSetVerbose(List<Verbose> options) throws IOException, CommandException
     {
         connectIfNecessary();
         delegate.messageSetVerbose(options);
     }
 
     @Override
     public long queryBookmark(Channel channel, Date recStartTs) throws IOException
     {
         connectIfNecessary();
         return delegate.queryBookmark(channel, recStartTs);
     }
 
     @Override
     public URI queryCheckFile(boolean checkSlaves, Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.queryCheckFile(checkSlaves, program);
     }
 
     @Override
     public List<VideoEditInfo> queryCommBreak(Channel channel, Date recStartTs) throws IOException
     {
         connectIfNecessary();
         return delegate.queryCommBreak(channel, recStartTs);
     }
 
     @Override
     public List<VideoEditInfo> queryCutList(Channel channel, Date recStartTs) throws IOException
     {
         connectIfNecessary();
         return delegate.queryCutList(channel, recStartTs);
     }
 
     @Override
     public FileInfo queryFileExists(URI filename, String storageGroup) throws IOException
     {
         connectIfNecessary();
         return delegate.queryFileExists(filename, storageGroup);
     }
 
     @Override
     public String queryFileHash(URI filename, String storageGroup) throws IOException,
                                                                   CommandException
     {
         connectIfNecessary();
         return delegate.queryFileHash(filename, storageGroup);
     }
 
     @Override
     public String queryFileHash(URI filename, String storageGroup, String host) throws IOException,
                                                                                CommandException
     {
         connectIfNecessary();
         return delegate.queryFileHash(filename, storageGroup, host);
     }
 
     @Override
     public List<DriveInfo> queryFreeSpace() throws IOException
     {
         connectIfNecessary();
         return delegate.queryFreeSpace();
     }
 
     @Override
     public DriveInfo queryFreeSpaceSummary() throws IOException
     {
         connectIfNecessary();
         return delegate.queryFreeSpaceSummary();
     }
 
     @Override
     public void queryGenPixMap2(String id, Program program) throws IOException, CommandException
     {
         connectIfNecessary();
         delegate.queryGenPixMap2(id, program);
     }
 
     @Override
     public UpcomingRecordings queryGetAllPending() throws IOException
     {
         connectIfNecessary();
         return delegate.queryGetAllPending();
     }
 
     @Override
     public List<Program> queryGetAllScheduled() throws IOException
     {
         connectIfNecessary();
         return delegate.queryGetAllScheduled();
     }
 
     @Override
     public List<Program> queryGetConflicting(Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.queryGetConflicting(program);
     }
 
     @Override
     public List<Program> queryGetExpiring() throws IOException
     {
         connectIfNecessary();
         return delegate.queryGetExpiring();
     }
 
     @Override
     public Date queryGuideDataThrough() throws IOException
     {
         connectIfNecessary();
         return delegate.queryGuideDataThrough();
     }
 
     @Override
     public String queryHostname() throws IOException
     {
         connectIfNecessary();
         return delegate.queryHostname();
     }
 
     @Override
     public boolean queryIsActiveBackend(String hostname) throws IOException
     {
         connectIfNecessary();
         return delegate.queryIsActiveBackend(hostname);
     }
 
     @Override
     public List<String> queryActiveBackends() throws IOException
     {
         connectIfNecessary();
         return delegate.queryActiveBackends();
     }
 
     @Override
     public RecordingsInProgress queryIsRecording() throws IOException
     {
         connectIfNecessary();
         return delegate.queryIsRecording();
     }
 
     @Override
     public Load queryLoad() throws IOException
     {
         connectIfNecessary();
         return delegate.queryLoad();
     }
 
     @Override
     public MemStats queryMemStats() throws IOException
     {
         connectIfNecessary();
         return delegate.queryMemStats();
     }
 
     @Override
     public PixMap queryPixMapGetIfModified(Date timestamp, int maxFileSize, Program program) throws IOException,
                                                                                             CommandException
     {
         connectIfNecessary();
         return delegate.queryPixMapGetIfModified(timestamp, maxFileSize, program);
     }
 
     @Override
     public Date queryPixMapLastModified(Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.queryPixMapLastModified(program);
     }
 
     @Override
     public QueryRecorder queryRecorder(int recorderId)
     {
         // TODO need cached version
         return delegate.queryRecorder(recorderId);
     }
 
     @Override
     public Program queryRecordingBasename(String basename) throws IOException, CommandException
     {
         connectIfNecessary();
         return delegate.queryRecordingBasename(basename);
     }
 
     @Override
     public Program queryRecordingTimeslot(Channel channel, Date recStartTs) throws IOException,
                                                                            CommandException
     {
         connectIfNecessary();
         return delegate.queryRecordingTimeslot(channel, recStartTs);
     }
 
     @Override
     public List<Program> queryRecordings(RecordingCategory recCategory) throws IOException
     {
         connectIfNecessary();
         return delegate.queryRecordings(recCategory);
     }
 
     @Override
     public QueryRemoteEncoder queryRemoteEncoder(int recorderId)
     {
         // TODO need cached version
         return delegate.queryRemoteEncoder(recorderId);
     }
 
     @Override
     public String querySetting(String host, String name) throws IOException
     {
         connectIfNecessary();
         return delegate.querySetting(host, name);
     }
 
     @Override
     public FileInfo querySgFileQuery(String host, String storageGroup, String path) throws IOException,
                                                                                    CommandException
     {
         connectIfNecessary();
         return delegate.querySgFileQuery(host, storageGroup, path);
     }
 
     @Override
     public List<FileEntry> querySgGetFileList(String host, String storageGroup, String path) throws IOException,
                                                                                             CommandException
     {
         connectIfNecessary();
         return delegate.querySgGetFileList(host, storageGroup, path);
     }
 
     @Override
     public TimeInfo queryTimeZone() throws IOException
     {
         connectIfNecessary();
         return delegate.queryTimeZone();
     }
 
     @Override
     public long queryUptime() throws IOException
     {
         connectIfNecessary();
         return delegate.queryUptime();
     }
 
     @Override
     public void refreshBackend() throws IOException
     {
         connectIfNecessary();
         delegate.refreshBackend();
     }
 
     @Override
     public void rescheduleRecordings(int recorderId) throws IOException
     {
         connectIfNecessary();
         delegate.rescheduleRecordings(recorderId);
     }
 
     @Override
     public void scanVideos() throws IOException
     {
         connectIfNecessary();
         delegate.scanVideos();
     }
 
     @Override
     public boolean setBookmark(Channel channel, Date recStartTs, long location) throws IOException,
                                                                                CommandException
     {
         connectIfNecessary();
         return delegate.setBookmark(channel, recStartTs, location);
     }
 
     @Override
     public void setChannelInfo(Channel oldChannel, Channel newChannel) throws IOException,
                                                                       CommandException
     {
         connectIfNecessary();
         delegate.setChannelInfo(oldChannel, newChannel);
     }
 
     @Override
     public void setNextLiveTvDir(int recorderId, String path) throws IOException, CommandException
     {
         connectIfNecessary();
         delegate.setNextLiveTvDir(recorderId, path);
     }
 
     @Override
     public void setSetting(String host, String name, String value) throws IOException
     {
         connectIfNecessary();
         delegate.setSetting(host, name, value);
     }
 
     @Override
     public void shutdownNow(String command) throws IOException
     {
         connectIfNecessary();
         delegate.shutdownNow(command);
     }
 
     @Override
     public int stopRecording(Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.stopRecording(program);
     }
 
     @Override
     public boolean undeleteRecording(Program program) throws IOException
     {
         connectIfNecessary();
         return delegate.undeleteRecording(program);
     }
 
     @Override
     public void connect(String host, int port, long timeout) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void mythProtoVersion() throws IOException, InvalidProtocolVersionException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void ann(ConnectionType connectionType, String host, EventLevel level) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void annSlaveBackend(InetAddress address, Program... recordings) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void annMediaServer(InetAddress address) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public QueryFileTransfer annFileTransfer(String host,
                                              FileTransferType type,
                                              boolean readAhead,
                                              long timeout,
                                              URI uri,
                                              String storageGroup,
                                              Protocol commandProtocol) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public ByteChannel getChannel()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void addBackendEventListener(BackendEventListener l)
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void removeBackendEventListener(BackendEventListener l)
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void copyBackendEventListeners(Protocol protocol)
     {
         throw new UnsupportedOperationException();
     }
 }

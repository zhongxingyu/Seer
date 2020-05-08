 /*
  * Copyright 2011 Gregory P. Moyer
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
 package org.syphr.mythtv.proto;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import org.syphr.mythtv.proto.data.Channel;
 import org.syphr.mythtv.proto.data.DriveInfo;
 import org.syphr.mythtv.proto.data.FileTransferType;
 import org.syphr.mythtv.proto.data.GenPixMapResponse;
 import org.syphr.mythtv.proto.data.Load;
 import org.syphr.mythtv.proto.data.MemStats;
 import org.syphr.mythtv.proto.data.ProgramInfo;
 import org.syphr.mythtv.proto.data.TimeInfo;
 import org.syphr.mythtv.proto.data.UpcomingRecordings;
 import org.syphr.mythtv.proto.events.BackendEventListener;
 import org.syphr.mythtv.proto.types.ConnectionType;
 import org.syphr.mythtv.proto.types.EventLevel;
 import org.syphr.mythtv.proto.types.RecordingCategory;
 
 /**
  * This interface represents the combined API of all MythTV protocols that are supported.
  * However, any functionality that is not part of the protocol present in the most current
  * stable release of MythTV will be marked as deprecated.
  *
  * @author Gregory P. Moyer
  */
 public interface Protocol
 {
     /**
      * Identify this protocol to the backend and verify that the backend understands the
      * same protocol. This should be the first command sent once a connection is
      * established.
      *
      * @throws IOException
      *
      * @since 63
      */
     public void mythProtoVersion() throws IOException;
 
     /**
      * Announce this connection to the backend. This should be the second
      * command sent to the backend after {@link #mythProtoVersion()}.<br>
      * <br>
      * Note: if a file is to be transferred,
     * {@link #annFileTransfer(String, FileTransferType, boolean, long, File, String, SocketManager)}
      * should be sent first.
      *
      * @param connectionType
      *            the intended use for this connection
      * @param host
      *            the name of the host that is being announced (the client)
      * @param level
      *            the level of events desired by the client
      * @throws IOException
      *
      * @since 63
      */
     public void ann(ConnectionType connectionType,
                     String host,
                     EventLevel level) throws IOException;
 
     /**
      * Announce this connection to the backend with the intention of
      * transferring a file (read or write). This should be the second command
      * sent to the backend after {@link #mythProtoVersion()}.<br>
      * <br>
      * If a file transfer is not needed, use
      * {@link #ann(ConnectionType, String, EventLevel)} instead.
      *
      * @param host
      *            the name of the host that is being announced (the client)
      * @param type
      *            how the transfer connection will be used (read/write)
      * @param readAhead
      *            if <code>true</code>, the backend will spawn a read ahead
      *            thread
      * @param timeout
      *            milliseconds to timeout the transfer
      * @param file
      *            the file to transfer, relative to the storage group
      * @param storageGroup
      *            the storage group that contains (will contain) the file to be
      *            transferred
      * @param commandSocketManager
      *            the connection that will be used to send file transfer
      *            commands
      * @return a sub-protocol API that can be used to manipulate the data stream
      * @throws IOException
      */
     public QueryFileTransfer annFileTransfer(String host,
                                              FileTransferType type,
                                              boolean readAhead,
                                              long timeout,
                                              File file,
                                              String storageGroup,
                                              SocketManager commandSocketManager) throws IOException;
 
     /**
      * Tell the backend that this client intends to disconnect. This command should be
      * sent as the last message before the connection is closed.
      *
      * @throws IOException
      *
      * @since 63
      */
     public void done() throws IOException;
 
     /**
      * Allow the backend to shutdown. This releases a previous a call to
      * {@link #blockShutdown()}.
      *
      * @throws IOException
      */
     public void allowShutdown() throws IOException;
 
     /**
      * Prevent this backend from shutting down until it is releases via
      * {@link #allowShutdown()}.
      *
      * @throws IOException
      */
     public void blockShutdown() throws IOException;
 
     /**
      * Determine whether or not the given program is currently being recorded
      * and on which recorder.
      *
      * @param program
      *            the program to check
      * @return the appropriate recorder ID or 0 if the program is not currently
      *         being recorded
      * @throws IOException
      *
      * @since 63
      */
     public int checkRecording(ProgramInfo program) throws IOException;
 
     // TODO
     public void deleteFile() throws IOException;
 
     // TODO
     public void deleteRecording() throws IOException;
 
     // TODO
     public void downloadFile() throws IOException;
 
     // TODO
     public void downloadFileNow() throws IOException;
 
     // TODO
     public void fillProgramInfo() throws IOException;
 
     // TODO
     public void forceDeleteRecording() throws IOException;
 
     // TODO
     public void forgetRecording() throws IOException;
 
     // TODO
     public void freeTuner() throws IOException;
 
     // TODO
     public void getFreeRecorder() throws IOException;
 
     // TODO
     public void getFreeRecorderCount() throws IOException;
 
     // TODO
     public void getFreeRecorderList() throws IOException;
 
     // TODO
     public void getNextFreeRecorder() throws IOException;
 
     // TODO
     public void getRecorderFromNum() throws IOException;
 
     // TODO
     public void getRecorderNum() throws IOException;
 
     // TODO
     public void goToSleep() throws IOException;
 
     // TODO
     public void lockTuner() throws IOException;
 
     // TODO
     public void queryBookmark() throws IOException;
 
     // TODO
     public void queryCheckFile() throws IOException;
 
     // TODO
     public void queryCommBreak() throws IOException;
 
     // TODO
     public void queryCutList() throws IOException;
 
     // TODO
     public void queryFileExists() throws IOException;
 
     // TODO
     public void queryFileHash() throws IOException;
 
     /**
      * Determine how much space is available on all drives connected to this backend.
      *
      * @return a list of drives with detailed space information
      * @throws IOException
      *
      * @since 63
      */
     public List<DriveInfo> queryFreeSpace() throws IOException;
 
     /**
      * Determine overall space used and free in the entire MythTV environment (across all
      * backends).
      *
      * @return drive data representing a summary across the environment, only the space
      *         information will be valid
      * @throws IOException
      *
      * @since 63
      */
     public DriveInfo queryFreeSpaceSummary() throws IOException;
 
     /**
      * Request that the backend create a pix map for the given program.
      *
      * @param id
      *            a unique identifier
      * @param program
      *            the program for which the pix map is to be generated
      * @return the response from the backend
      * @throws IOException
      *
      * @since 63
      */
     public GenPixMapResponse queryGenPixMap2(String id, ProgramInfo program) throws IOException;
 
     /**
      * Retrieve a list of all scheduled recordings that are coming up soon.
      *
      * @return the list of pending recordings
      * @throws IOException
      *
      * @since 63
      */
     public UpcomingRecordings queryGetAllPending() throws IOException;
 
     /**
      * Retrieve a list of all scheduled recordings.
      *
      * @return the list of scheduled recordings
      * @throws IOException
      *
      * @since 63
      */
     public List<ProgramInfo> queryGetAllScheduled() throws IOException;
 
     // TODO
     public void queryGetConflicting() throws IOException;
 
     /**
      * Retrieve a list of all recordings set to auto-expire soon.
      *
      * @return the list of expiring recordings
      * @throws IOException
      *
      * @since 63
      */
     public List<ProgramInfo> queryGetExpiring() throws IOException;
 
     /**
      * Retrieve the end date of current EPG data. This is typically about two weeks from
      * the last update. This should not be confused with any expiration date associated
      * with guide subscriptions.
      *
      * @return latest date for which guide data is present
      * @throws IOException
      *
      * @since 63
      */
     public Date queryGuideDataThrough() throws IOException;
 
     /**
      * Retrieve the host name of the connected backend.
      *
      * @return backend's hostname
      * @throws IOException
      *
      * @since 63
      */
     public String queryHostname() throws IOException;
 
     /**
      * Determine whether or not the given hostname has an active backend instance.
      *
      * @param hostname
      *            the hostname to check
      * @return <code>true</code> if an active backend is found at the given host;
      *         <code>false</code> otherwise
      * @throws IOException
      *
      * @since 63
      */
     public boolean queryIsActiveBackend(String hostname) throws IOException;
 
     // TODO
     public void queryIsRecording() throws IOException;
 
     /**
      * Retrieve the load factor of the backend machine.
      *
      * @return data representing the load factor over time of the backend machine
      * @throws IOException
      *
      * @since 63
      */
     public Load queryLoad() throws IOException;
 
     /**
      * Retrieve memory statistics of the backend machine.
      *
      * @return data representing the current memory usage of the backend machine
      * @throws IOException
      *
      * @since 63
      */
     public MemStats queryMemStats() throws IOException;
 
     // TODO
     public void queryPixMapLastModified() throws IOException;
 
     /**
      * Get a sub-protocol object that provides an API to interrogate a specific recorder.
      *
      * @param recorderId
      *            the ID of the recorder to interrogate
      * @return an object that provides capabilities to interrogate the specified recorder
      */
     public QueryRecorder queryRecorder(int recorderId);
 
     // TODO
     public void queryRecordingBasename() throws IOException;
 
     /**
      * Retrieve the program data associated with the given channel and start time.
      *
      * @param channel
      *            the channel of the program to lookup
      * @param startTime
      *            the start time of the program to lookup
      * @return the relevant program data
      * @throws IOException
      *
      * @since 63
      */
     public ProgramInfo queryRecordingTimeslot(Channel channel, Date startTime) throws IOException;
 
     /**
      * Retrieve a list of recordings matching the given category.
      *
      * @param recType
      *            the category of recordings to get
      * @return a list of programs representing recordings of the given category
      * @throws IOException
      *
      * @since 63
      */
     public List<ProgramInfo> queryRecordings(RecordingCategory recType) throws IOException;
 
     // TODO
     public void queryRemoteEncoder() throws IOException;
 
     // TODO
     public void querySetting() throws IOException;
 
     // TODO
     public void querySgFileQuery() throws IOException;
 
     // TODO
     public void querySgGetFileList() throws IOException;
 
     /**
      * Retrieve time information from the backend, including current date, time, and time
      * zone.
      *
      * @return the time data
      * @throws IOException
      *
      * @since 63
      */
     public TimeInfo queryTimeZone() throws IOException;
 
     /**
      * Determine the up time, in seconds, of the backend machine.
      *
      * @return the number of seconds the backend has been up or <code>-1</code> if the up
      *         time could not be determined
      * @throws IOException
      *
      * @since 63
      */
     public long queryUptime() throws IOException;
 
     // TODO
     public void refreshBackend() throws IOException;
 
     // TODO
     public void rescheduleRecordings() throws IOException;
 
     /**
      * Request a scan of videos. Listen for a backend event to provide notice when the
      * scan completes.
      *
      * @throws IOException
      *
      * @since 64
      */
     public void scanVideos() throws IOException;
 
     // TODO
     public void setBookmark() throws IOException;
 
     // TODO
     public void setChannelInfo() throws IOException;
 
     // TODO
     public void setNextLiveTvDir() throws IOException;
 
     // TODO
     public void setSetting() throws IOException;
 
     // TODO
     public void shutdownNow() throws IOException;
 
     // TODO
     public void stopRecording() throws IOException;
 
     // TODO
     public void undeleteRecording() throws IOException;
 
     /**
      * Add a listener to receive unsolicited backend event messages.
      *
      * @param l
      *            the listener to add
      */
     public void addBackendEventListener(BackendEventListener l);
 
     /**
      * Remove the given listener from receiving backend event messages.
      *
      * @param l
      *            the listener to remove
      */
     public void removeBackendEventListener(BackendEventListener l);
 }

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
 import java.net.URI;
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 
 import org.syphr.mythtv.proto.data.Channel;
 import org.syphr.mythtv.proto.data.CommBreakInfo;
 import org.syphr.mythtv.proto.data.DriveInfo;
 import org.syphr.mythtv.proto.data.FileEntry;
 import org.syphr.mythtv.proto.data.FileInfo;
 import org.syphr.mythtv.proto.data.FileTransferType;
 import org.syphr.mythtv.proto.data.GenPixMapResponse;
 import org.syphr.mythtv.proto.data.Load;
 import org.syphr.mythtv.proto.data.MemStats;
 import org.syphr.mythtv.proto.data.ProgramInfo;
 import org.syphr.mythtv.proto.data.RecorderInfo;
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
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void mythProtoVersion() throws IOException;
 
     /**
      * Announce this connection to the backend. This should be the second
      * command sent to the backend after {@link #mythProtoVersion()}.<br>
      * <br>
      * Note: if a file is to be transferred,
      * {@link #annFileTransfer(String, FileTransferType, boolean, long, URI, String, SocketManager)}
      * should be sent first.
      *
      * @param connectionType
      *            the intended use for this connection
      * @param host
      *            the name of the host that is being announced (the client)
      * @param level
      *            the level of events desired by the client
      * @throws IOException
      *             if there is a communication or protocol error
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
      * @param uri
      *            the URI of the file to transfer, relative to the storage group
      * @param storageGroup
      *            the storage group that contains (will contain) the file to be
      *            transferred
      * @param commandSocketManager
      *            the connection that will be used to send file transfer
      *            commands
      * @return a sub-protocol API that can be used to manipulate the data stream
      * @throws IOException
      *             if there is a communication or protocol error
      */
     public QueryFileTransfer annFileTransfer(String host,
                                              FileTransferType type,
                                              boolean readAhead,
                                              long timeout,
                                              URI uri,
                                              String storageGroup,
                                              SocketManager commandSocketManager) throws IOException;
 
     /**
      * Tell the backend that this client intends to disconnect. This command should be
      * sent as the last message before the connection is closed.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void done() throws IOException;
 
     /**
      * Allow the backend to shutdown. This releases a previous a call to
      * {@link #blockShutdown()}.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      */
     public void allowShutdown() throws IOException;
 
     /**
      * Prevent this backend from shutting down until it is releases via
      * {@link #allowShutdown()}.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      */
     public void blockShutdown() throws IOException;
 
     /**
      * Determine whether or not the given program is currently being recorded
      * and on which recorder.
      *
      * @param program
      *            the program to check
      * @return the ID of the recorder that is actively recording the program or
      *         <code>0</code> if the program is not currently being recorded
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public int checkRecording(ProgramInfo program) throws IOException;
 
     /**
      * Request that a file be deleted.
      *
      * @param file
      *            the file to delete
      * @param storageGroup
      *            the storage group where the file exists
      * @return <code>true</code> if the delete was successful;
      *         <code>false</code> otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean deleteFile(File file, String storageGroup) throws IOException;
 
     /**
      * Request that a recording be deleted. To only remove a recording from the
      * history, see {@link #forgetRecording(ProgramInfo)}.
      *
      * @param channel
      *            the channel on which the program was recorded
     * @param startTime
     *            the start time of the recording
      * @param force
      *            if <code>true</code>, metadata will be removed even if the
      *            file cannot be located
      * @param forget
      *            if <code>true</code>, the history of this recording will be
      *            removed
      * @return <code>true</code> if delete was successful; <code>false</code>
      *         otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean deleteRecording(Channel channel,
                                   Date startTime,
                                    boolean force,
                                    boolean forget) throws IOException;
 
     /**
      * Request that the backend manage a file download. This command will return
      * immediately. To watch the progress, listen for backend events.
      *
      * @see #downloadFileNow(URL, String, File)
      *
      * @param url
      *            the URL of the item to download
      * @param storageGroup
      *            the destination storage group
      * @param file
      *            the destination file
      * @return the URI of the new file or <code>null</code> if an error occurred
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public URI downloadFile(URL url, String storageGroup, File file) throws IOException;
 
     /**
      * Request that the backend manage a file download. This command will not
      * return until the download completes.
      *
      * @see #downloadFile(URL, String, File)
      *
      * @param url
      *            the URL of the item to download
      * @param storageGroup
      *            the destination storage group
      * @param file
      *            the destination file
      * @return the URI of the new file or <code>null</code> if an error occurred
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public URI downloadFileNow(URL url, String storageGroup, File file) throws IOException;
 
     /**
      * Fill in the path and file size fields for the given program.
      *
      * @param host
      *            used to determine if the returned path should be local or
      *            remote
      * @param program
      *            the basic program information
      * @return the given program with path and file size filled in for the given
      *         host
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public ProgramInfo fillProgramInfo(String host, ProgramInfo program) throws IOException;
 
     /**
      * Mark a recording so that it will never be considered when checking for
      * duplicates. To forget and delete a recording, see
      * {@link #deleteRecording(Channel, Date, boolean, boolean)}.
      *
      * @param program
      *            the program to be forgotten
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void forgetRecording(ProgramInfo program) throws IOException;
 
     /**
      * Free a recorder that was previously locked with {@link #lockTuner()}.
      *
      * @param recorderId
      *            the ID of the recorder to free
      * @return <code>true</code> if successful; <code>false</code> otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean freeTuner(int recorderId) throws IOException;
 
     /**
      * Ask the backend for a free recorder. The backend will attempt to find a
      * local recorder before remotes ones (see
      * {@link #getNextFreeRecorder(RecorderInfo)} for an alternative).
      *
      * @return a free recorder; this object may not be
      *         {@link RecorderInfo#isValid() valid} if there are no free
      *         recorders
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public RecorderInfo getFreeRecorder() throws IOException;
 
     /**
      * Retrieve the number of recorders that are online, but not busy or
      * {@link #lockTuner() locked}.
      *
      * @return the number of free recorders
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public int getFreeRecorderCount() throws IOException;
 
     /**
      * Retrieve a list of recorders that are online, but not busy or locked.
      * This list is guaranteed to contain only {@link RecorderInfo#isValid()
      * valid} recorders.
      *
      * @return the list of free recorder IDs
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public List<Integer> getFreeRecorderList() throws IOException;
 
     /**
      * Starting with the given recorder, find the next free recorder in order.
      * This request will wrap from the end of the list back around to the
      * beginning.
      *
      * @param from
      *            the backend will start looking at the next recorder in line
      *            after this one
      * @return the next available recorder; this may not be
      *         {@link RecorderInfo#isValid() valid} if there are no free
      *         recorders
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public RecorderInfo getNextFreeRecorder(RecorderInfo from) throws IOException;
 
     /**
      * Retrieve a recorder's host and port information.
      *
      * @param recorderId
      *            the ID of the recorder to lookup
      * @return a complete set of recorder information with ID, host, and port
      *         (or an invalid recorder if the specified ID does not exist)
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public RecorderInfo getRecorderFromNum(int recorderId) throws IOException;
 
     // TODO
     public void getRecorderNum() throws IOException;
 
     // TODO
     public void goToSleep() throws IOException;
 
     // TODO
     public void lockTuner() throws IOException;
 
     // TODO
     public void queryBookmark() throws IOException;
 
     /**
      * Retrieve a URI to the given program.
      *
      * @param checkSlaves
      *            if <code>true</code>, the backend will contact other backends
      *            to look for the given program; otherwise only the connected
      *            backend will be checked
      * @param program
      *            the program whose URI is requested
      * @return the URI of the given program or <code>null</code> if the
      *         program's file cannot be found
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public URI queryCheckFile(boolean checkSlaves, ProgramInfo program) throws IOException;
 
     /**
      * Retrieve the list of commercial breaks found for the the recording at the
      * given channel and start time.
      *
      * @param channel
      *            the channel on which the program aired
      * @param startTime
      *            the time the program started
      * @return the list of commercial breaks
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public List<CommBreakInfo> queryCommBreak(Channel channel, Date startTime) throws IOException;
 
     // TODO
     public void queryCutList() throws IOException;
 
     /**
      * Determine whether or not a file exists and, if it does exists, its full
      * path.
      *
      * @param basename
      *            the basename of the file to check
      * @param storageGroup
      *            the name of the storage group to check
      * @return if it exists, a file representing the absolute path;
      *         <code>null</code> otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public File queryFileExists(String basename, String storageGroup) throws IOException;
 
     /**
      * Retrieve a 64-bit hash of the file represented by the given name and storage group.
      *
      * @param filename
      *            the location of the file to hash relative to a storage group
      * @param storageGroup
      *            the storage group containing the file to has
      * @return the hash value or <code>null</code> if the hash could not be computed
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public String queryFileHash(URI filename, String storageGroup) throws IOException;
 
     /**
      * Determine how much space is available on all drives connected to this backend.
      *
      * @return a list of drives with detailed space information
      * @throws IOException
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public GenPixMapResponse queryGenPixMap2(String id, ProgramInfo program) throws IOException;
 
     /**
      * Retrieve a list of all scheduled recordings that are coming up soon.
      *
      * @return the list of pending recordings
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public UpcomingRecordings queryGetAllPending() throws IOException;
 
     /**
      * Retrieve a list of all scheduled recordings.
      *
      * @return the list of scheduled recordings
      * @throws IOException
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public Date queryGuideDataThrough() throws IOException;
 
     /**
      * Retrieve the host name of the connected backend.
      *
      * @return backend's hostname
      * @throws IOException
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public Load queryLoad() throws IOException;
 
     /**
      * Retrieve memory statistics of the backend machine.
      *
      * @return data representing the current memory usage of the backend machine
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public MemStats queryMemStats() throws IOException;
 
     // TODO
     public void queryPixMapLastModified() throws IOException;
 
     /**
      * Get a sub-protocol object that provides an API to interrogate a specific recorder.
      *
      * @param recorder
      *            the recorder to interrogate
      * @return an object that provides capabilities to interrogate the specified recorder
      */
     public QueryRecorder queryRecorder(RecorderInfo recorder);
 
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
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public List<ProgramInfo> queryRecordings(RecordingCategory recType) throws IOException;
 
     // TODO
     public void queryRemoteEncoder() throws IOException;
 
     /**
      * Retrieve the current value of a setting.
      *
      * @param host
      *            the host to which the setting pertains
      * @param name
      *            the name of the setting
      * @return the value of the setting or <code>null</code> if there is no
      *         value
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public String querySetting(String host, String name) throws IOException;
 
     /**
      * Retrieve file information (i.e. last modified date, file size) for a file
      * within a storage group.
      *
      * @param host
      *            the host machine containing the file
      * @param storageGroup
      *            the storage group containing the file
      * @param path
      *            the full path to the file
      * @return the file information or <code>null</code> if the file cannot be
      *         found (which may be temporary, such as when a slave backend is
      *         unavailable)
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public FileInfo querySgFileQuery(String host, String storageGroup, String path) throws IOException;
 
     /**
      * Retrieve a complete directory listing within a storage group.
      *
      * @param host
      *            the host machine containing the directory
      * @param storageGroup
      *            the storage group containing the directory
      * @param path
      *            the full path to the directory
      * @return a list of directories and files contained within the given path or
      *         <code>null</code> if the directory cannot be found (which may be temporary,
      *         such as when a slave backend is unavailable)
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public List<FileEntry> querySgGetFileList(String host, String storageGroup, String path) throws IOException;
 
     /**
      * Retrieve time information from the backend, including current date, time, and time
      * zone.
      *
      * @return the time data
      * @throws IOException
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
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
      *             if there is a communication or protocol error
      *
      * @since 64
      */
     public void scanVideos() throws IOException;
 
     // TODO
     public void setBookmark() throws IOException;
 
     /**
      * Replace a channel with new information.
      *
      * @param oldChannel
      *            the channel being replaced/modified
      * @param newChannel
      *            the new channel information
      * @return <code>true</code> if the update was successful; <code>false</code>
      *         otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean setChannelInfo(Channel oldChannel, Channel newChannel) throws IOException;
 
     /**
      * Request that the given recorder put the next LiveTV recording in the directory
      * specified by the given path.
      *
      * @param recorderId
      *            the ID of the recorder to change
      * @param path
      *            the path of the directory to set
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean setNextLiveTvDir(int recorderId, String path) throws IOException;
 
     /**
      * Update the value of a setting.
      *
      * @param host
      *            the host to which the setting pertains
      * @param name
      *            the name of the setting
      * @param value
      *            the value to set
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void setSetting(String host, String name, String value) throws IOException;
 
     /**
      * Request a slave backend to shut down with the given command. This request will have
      * no affect on the master backend.
      *
      * @param command
      *            the shutdown command
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void shutdownNow(String command) throws IOException;
 
     /**
      * Request that the given program stop recording.
      *
      * @param program
      *            the program to stop recording
      * @return 0 if the recording is on a slave backend, -1 if an error occurs, or the
      *         recorder ID that was recording the program
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public int stopRecording(ProgramInfo program) throws IOException;
 
     /**
      * Request that the a program marked for future deletion be unmarked.
      *
      * @param program
      *            the program to undelete
      * @return <code>true</code> if the request was successful; <code>false</code>
      *         otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean undeleteRecording(ProgramInfo program) throws IOException;
 
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

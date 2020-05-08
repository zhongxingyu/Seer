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
 package org.syphr.mythtv.control;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.syphr.mythtv.data.Channel;
 import org.syphr.mythtv.data.Load;
 import org.syphr.mythtv.data.MemStats;
 import org.syphr.mythtv.data.PlaybackInfo;
 import org.syphr.mythtv.data.Program;
 import org.syphr.mythtv.data.VersionInfo;
 import org.syphr.mythtv.types.FrontendLocation;
 import org.syphr.mythtv.types.Key;
 import org.syphr.mythtv.types.SeekTarget;
 import org.syphr.mythtv.types.Verbose;
 import org.syphr.mythtv.util.exception.CommandException;
 
 /**
  * This interface represents the combined frontend control API of all MythTV versions that
  * are supported. However, any functionality that is not part of the most current stable
  * release of MythTV will be marked as deprecated.
  *
  * @author Gregory P. Moyer
  */
 public interface Control
 {
     /**
      * Connect to a frontend instance. This method will block until the connection completes.
      * If a connection is already active, this method will do nothing.
      *
      * @see #isConnected()
      *
      * @param host
      *            the hostname (or IP address) of the server
      * @param port
      *            the port on the server
      * @param timeout
      *            number of milliseconds to wait before assuming the connection failed
      *            (values < 1 indicate no timeout)
      * @throws IOException
      *             if the connection could not be completed
      */
     public void connect(String host, int port, final long timeout) throws IOException;
 
     /**
      * Determine whether or not there is an active connection to a frontend
      * instance.
      *
      * @return <code>true</code> if there is an active connection;
      *         <code>false</code> otherwise
      */
     public boolean isConnected();
 
     /**
      * Request that the frontend jump to the given location in the UI.
      *
      * @param jumpPoint
      *            the jump target
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public void jump(FrontendLocation jumpPoint) throws IOException;
 
     /**
      * Send the given character to the frontend.
      *
      * @param c
      *            the character to send
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public void key(char c) throws IOException;
 
     /**
      * Send the given key press to the frontend.
      *
      * @param key
      *            the key press to send
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public void key(Key key) throws IOException;
 
     /**
      * Request that the frontend set the volume to the given percentage. This command will
      * only work when the frontend is already in {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee of when
      * the volume will be set or if it will be set successfully. Use
      * {@link #queryVolume()} to check.
      *
      * @param percent
      *            the desired volume level as a percent (valid values are 0 - 100
      *            inclusive)
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playVolume(int percent) throws IOException, CommandException;
 
     /**
      * Request that the frontend move live TV to the next higher channel. This
      * command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee
      * of when playback will start or if it will start successfully. Use
      * {@link #queryPlaybackInfo()} to check.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playChannelUp() throws IOException, CommandException;
 
     /**
      * Request that the frontend move live TV to the next lower channel. This
      * command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee
      * of when playback will start or if it will start successfully. Use
      * {@link #queryPlaybackInfo()} to check.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playChannelDown() throws IOException, CommandException;
 
     /**
      * Request that the frontend change live TV to the specified channel.
      * This command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee
      * of when playback will start or if it will start successfully. Use
      * {@link #queryPlaybackInfo()} or {@link #queryLocation()} to check.
      *
      * @see #playChannel(int)
      *
      * @param channelNumber
      *            the number of the channel to start playing
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playChannel(String channelNumber) throws IOException, CommandException;
 
     /**
      * Request that the frontend change live TV to the specified channel.
      * This command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee
      * of when playback will start or if it will start successfully. Use
      * {@link #queryPlaybackInfo()} or {@link #queryLocation()} to check.
      *
      * @see #playChannel(String)
      *
      * @param channelId
      *            the ID of the channel to start playing
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playChannel(int channelId) throws IOException, CommandException;
 
     /**
      * Request that the frontend play back the given file.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee
      * of when playback will start or if it will start successfully. Use
      * {@link #queryPlaybackInfo()} or {@link #queryLocation()} to check.
      *
      * @param filename
      *            the absolute path to the file (local to the frontend) or a
      *            <code>myth://</code> URI to play back
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend was unable to initiate play back of the
      *             requested file
      *
      * @since 0.24
      */
     public void playFile(String filename) throws IOException, CommandException;
 
     /**
      * Request that the frontend play back the given recording.<br>
      * <br>
     * Note that this command will return immediately, but there is no guarantee of when
     * playback will start or if it will start successfully. Use
      * {@link #queryPlaybackInfo()} or {@link #queryLocation()} to check.
      *
      * @param channelId
      *            the ID of the channel that was recorded
      * @param recStartTs
     *            the actual recording start time (when the program was recorded)
     * @param <code>true</code> for the recording to resume from a bookmark (if there is
     *        one); <code>false</code> to start from the beginning
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the requested program is not found
      *
      * @since 0.24
      */
     public void playProgram(int channelId, Date recStartTs, boolean resume) throws IOException, CommandException;
 
     /**
      * Request that the frontend create a preview image based on what is
      * currently playing. This command will only work when the frontend is in
      * {@link FrontendLocation#PLAYBACK}.
      *
      * @see #playSavePreview(String)
      * @see #playSavePreview(String, int, int)
      *
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playSavePreview() throws IOException, CommandException;
 
     /**
      * Request that the frontend create a preview image based on what is
      * currently playing. This command will only work when the frontend is in
      * {@link FrontendLocation#PLAYBACK}.
      *
      * @see #playSavePreview()
      * @see #playSavePreview(String, int, int)
      *
      * @param filename
      *            the filename on the frontend machine where the preview image
      *            will be stored
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playSavePreview(String filename) throws IOException, CommandException;
 
     /**
      * Request that the frontend create a preview image based on what is
      * currently playing. This command will only work when the frontend is in
      * {@link FrontendLocation#PLAYBACK}.
      *
      * @see #playSavePreview()
      * @see #playSavePreview(String)
      *
      * @param filename
      *            the filename on the frontend machine where the preview image
      *            will be stored
      * @param width
      *            the width, in pixels, of the preview image
      * @param height
      *            the height, in pixels, of the preview image
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playSavePreview(String filename, int width, int height) throws IOException, CommandException;
 
     /**
      * Request that the frontend seek to the given location in the video that is currently
      * playing. This command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee of when
      * the seek will occur or if it will happen successfully. Use
      * {@link #queryPlaybackInfo()} to check.
      *
      * @param target
      *            the target location to seek
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playSeek(SeekTarget target) throws IOException, CommandException;
 
     /**
      * Request that the frontend seek to the given location in the video that is currently
      * playing. This command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee of when
      * the seek will occur or if it will happen successfully. Use
      * {@link #queryPlaybackInfo()} to check.
      *
      * @param hour
      *            the hour to seek
      * @param minute
      *            the minute to seek
      * @param second
      *            the second to seek
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playSeek(int hour, int minute, int second) throws IOException, CommandException;
 
     /**
      * Request that the frontend change playback speed. This command will only work when
      * the frontend is already in {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee of when
      * the seek will occur or if it will happen successfully. Use
      * {@link #queryPlaybackInfo()} to check.
      *
      * @param speed
      *            the desired playback speed where <code>0</code> is paused and
      *            <code>1<code> is normal
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playSpeed(float speed) throws IOException, CommandException;
 
     /**
      * Request that the frontend stop playing the video that is currently
      * playing. This command will only work when the frontend is already in
      * {@link FrontendLocation#PLAYBACK}.<br>
      * <br>
      * Note that this command will return immediately, but there is no guarantee
      * of when playback will stop or if it will stop successfully. Use
      * {@link #queryPlaybackInfo()} or {@link #queryLocation()} to check.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is not currently playing video
      *
      * @since 0.24
      */
     public void playStop() throws IOException, CommandException;
 
     /**
      * Request the current UI location of the frontend.
      *
      * @return the current location
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public FrontendLocation queryLocation() throws IOException;
 
     /**
      * Request information about the video that is currently playing on the
      * frontend. This is only useful when {@link #queryLocation()} returns
      * {@link FrontendLocation#PLAYBACK}.
      *
      * @see #queryLocation()
      *
      * @return playback information if the frontend is currently playing video;
      *         <code>null</code> otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public PlaybackInfo queryPlaybackInfo() throws IOException;
 
     /**
      * Request the current volume of the frontend as an integer percentage in the range
      * 0-100.
      *
      * @return the current volume
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public int queryVolume() throws IOException;
 
     /**
      * Request a list of available recordings.
      *
      * @return the recordings
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public List<Program> queryRecordings() throws IOException;
 
     /**
      * Request details about a specific recording.
      *
      * @param channelId
      *            the ID of the channel corresponding to the desired recording
      * @param recStartTs
      *            the actual start time of the desired recording
      *
      * @return the recording, if it exists; <code>null</code> otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public Program queryRecording(int channelId, Date recStartTs) throws IOException;
 
     /**
      * Request a list of all programs in the available guide data for the
      * current timeslot.
      *
      * @return the programs
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public List<Program> queryLiveTv() throws IOException;
 
     /**
      * Request the program that is in the guide data for the current date/time on the
      * given channel.
      *
      * @param channelId
      *            the ID of the channel to check
      *
      * @return the program, if it exists; <code>null</code> otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public Program queryLiveTv(int channelId) throws IOException;
 
     /**
      * Retrieve the load factor of the frontend machine.
      *
      * @return data representing the load factor over time of the frontend machine
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public Load queryLoad() throws IOException;
 
     /**
      * Retrieve memory statistics of the frontend machine.
      *
      * @return data representing the current memory usage of the frontend machine
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public MemStats queryMemStats() throws IOException;
 
     /**
      * Retrieve the current date/time on the frontend machine.
      *
      * @return the date/time
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public Date queryTime() throws IOException;
 
     /**
      * Determine the up time, in seconds, of the frontend machine.
      *
      * @return the number of seconds the frontend has been up or <code>-1</code> if the up
      *         time could not be determined
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public long queryUptime() throws IOException;
 
     /**
      * Retrieve the set of verbose options that are currently enabled on the frontend.
      *
      * @return the current verbose options
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public Set<Verbose> queryVerbose() throws IOException;
 
     /**
      * Retrieve information about the version of MythTV and related software
      * running on the frontend.
      *
      * @return version information
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public VersionInfo queryVersion() throws IOException;
 
     /**
      * Retrieve a list of visible channels available to this frontend.<br>
      * <br>
      * Note that the resulting channel objects are not completely filled in.
      * They consist of an ID, and callsign, and a name.
      *
      * @return the list of visible channels
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public List<Channel> queryChannels() throws IOException;
 
     /**
      * Retrieve a list of visible channels available to this frontend within the
      * given bounds.<br>
      * <br>
      * Note that the resulting channel objects are not completely filled in.
      * They consist of an ID, and callsign, and a name.
      *
      * @param start
      *            the starting position in the total list of channels for the
      *            desired sublist
      * @param limit
      *            the number of channels to return, beginning with the channel
      *            at the start index
      *
      * @return the list of visible channels
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public List<Channel> queryChannels(int start, int limit) throws IOException;
 
     /**
      * Change the verbose logging options on the frontend.
      *
      * @param options
      *            the options to set
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is unable to set the verbose logging options
      *
      * @since 0.24
      */
     public void setVerbose(List<Verbose> options) throws IOException, CommandException;
 
     /**
      * Request that the frontend record a screenshot of its current state.
      *
      * @see #screenshot(String)
      * @see #screenshot(String, int, int)
      *
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is unable to set the verbose logging options
      *
      * @since 0.24
      */
     public void screenshot() throws IOException, CommandException;
 
     /**
      * Request that the frontend record a screenshot of its current state.
      *
      * @see #screenshot()
      *
      * @param width
      *            the width, in pixels, of the screenshot
      * @param height
      *            the height, in pixels, of the screenshot
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is unable to set the verbose logging options
      *
      * @since 0.25
      */
     public void screenshot(int width, int height) throws IOException, CommandException;
 
     /**
      * Request that the frontend record a screenshot of its current state.
      *
      * @see #screenshot()
      * @see #screenshot(String, int, int)
      *
      * @param filename
      *            the filename on the frontend machine where the screenshot will be stored
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is unable to set the verbose logging options
      *
      * @since 0.24
      */
     public void screenshot(String filename) throws IOException, CommandException;
 
     /**
      * Request that the frontend record a screenshot of its current state.
      *
      * @see #screenshot()
      * @see #screenshot(String)
      *
      * @param filename
      *            the filename on the frontend machine where the screenshot will be stored
      * @param width
      *            the width, in pixels, of the screenshot
      * @param height
      *            the height, in pixels, of the screenshot
      * @throws IOException
      *             if there is a communication or protocol error
      * @throws CommandException
      *             if the frontend is unable to set the verbose logging options
      *
      * @since 0.24
      */
     public void screenshot(String filename, int width, int height) throws IOException, CommandException;
 
     /**
      * Send some text to be displayed on the frontend.
      *
      * @param text
      *            the text to send
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.25
      */
     public void message(String text) throws IOException;
 
     /**
      * Gracefully disconnect this client from the frontend.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 0.24
      */
     public void exit() throws IOException;
 
     /**
      * Retrieve a list of constants of the given type that are valid for this
      * control version.<br>
      * <br>
      * This is useful to know before trying a command that a certain option is
      * valid. For example, to use {@link #jump(FrontendLocation)} without the risk of
      * sending an invalid jump point, first make sure it is in the list returned
      * from <code>getAvailableTypes(JumpPoint.class)</code>
      *
      * @param <E>
      *            the generic enum type
      * @param type
      *            the enum class
      * @return a list of constants that are valid for this control
      */
     public <E extends Enum<E>> List<E> getAvailableTypes(Class<E> type);
 }

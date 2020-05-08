 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.ui.task;
 
 import java.util.Map;
 
 import tesla.app.command.Command;
 import tesla.app.command.helper.CommandHelperFactory;
 import tesla.app.command.helper.ICommandHelper;
 import tesla.app.mediainfo.MediaInfo;
 import tesla.app.mediainfo.MediaInfoFactory;
 import tesla.app.service.business.ICommandController;
 import tesla.app.service.business.IErrorHandler;
 import tesla.app.ui.task.PlaybackUpdateTask.PlaybackUpdateData;
 import android.os.AsyncTask;
 import android.os.RemoteException;
 
 public class PlaybackUpdateTask extends AsyncTask<ICommandController, Boolean, PlaybackUpdateData> {
 
 	// If player does not provide a playing status, then assume this value
 	private static final boolean DEFAULT_PLAY_MODE = false;
 	
 	// If player does not provide a valid position, then assume this value
 	private static final int DEFAULT_POSITION = 0;
 	private static final int DEFAULT_MAX = 0;
 	
 	private PlaybackUpdateListener listener = null;
 	private Command command;
 	
 	// Error messages need to be passed back to main UI thread
 	private String errorTitle = null;
 	private String errorMessage = null;
 	
 	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
 		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
 			// Pass the error data back to the main UI thread
 			errorTitle = title;
 			errorMessage = message;
 		}
 	};
 
 	private boolean mediaInfoEnabled = false;
 	private boolean isPlayingEnabled = false;
 	private boolean mediaPositionEnabled = false;
 	
 	// This should be private, but Java 5 does not allow it
 	class PlaybackUpdateData {
 		public MediaInfo info = new MediaInfo();
 		public boolean isPlaying = DEFAULT_PLAY_MODE;
 		public ProgressData mediaProgress = new ProgressData();
 	}
 	
 	private class ProgressData {
 		public int current = DEFAULT_POSITION;
 		public int max = DEFAULT_MAX;
 	}
 	
 	public interface PlaybackUpdateListener {
 		void onServiceError(Class<? extends Object> invoker, String title, String message, Command command);
 		
 		void onMediaInfoChanged(MediaInfo info);
 		
 		void onPlayingChanged(boolean isPlaying);
 		
 		void onMediaProgressChanged(int currentProgress, int mediaLength);
 	}
 	
 	protected PlaybackUpdateData doInBackground(ICommandController... args) 
 	{
 		PlaybackUpdateData out = new PlaybackUpdateData();
 		
 		// Get the available metadata from the server
 		ICommandController commandService = args[0];
 		try {
 			commandService.registerErrorHandler(errorHandler);
 			
 			/*
 			 * Get media info
 			 */
 			
 			command = commandService.queryForCommand(Command.GET_MEDIA_INFO, false);
 			Map<String, String> settings = command.getSettings();
 			if (settings.containsKey("ENABLED")) {
 				mediaInfoEnabled = Boolean.parseBoolean(settings.get("ENABLED"));
 			}
 			
 			if (mediaInfoEnabled) {
 				command = commandService.sendQuery(command);
 			
 				// Compile a MediaInfo pod with servers metadata
 				if (command != null && command.getOutput() != null && command.getOutput() != "") {
 					ICommandHelper helper = CommandHelperFactory.getHelperForCommand(command);
 					
 					Map<String, String> output;
 					output = helper.evaluateOutputAsMap(command.getOutput());
 					
 					if (output != null) {
 						out.info.track = output.get("tracknumber");
 						if (out.info.track == null) out.info.track = output.get("track-number");
 						
 						// Title can be taken from several metadata fields
 						out.info.title = output.get("title");
 						if (out.info.title == null) out.info.title = output.get("name");
 						if (out.info.title == null) out.info.title = output.get("location");
 						if (out.info.title == null) out.info.title = output.get("URI");
 						if (out.info.title == null) out.info.title = output.get("uri");
 						
 						if (out.info.title != null && (out.info.title.startsWith("file:/") || out.info.title.startsWith("/"))) out.info.title = stripTitleFromUrl(out.info.title);
 						
 						out.info.artist = output.get("artist");
 						
 						out.info.album = output.get("album");
 						
 						// Pass the pod to the MediaInfoFactory for processing
 						MediaInfoFactory factory = new MediaInfoFactory();
 						out.info = factory.process(out.info);
 					}
 				}
 			}
 			
 			/*
 			 * Is media currently playing
 			 */
 			
 			command = commandService.queryForCommand(Command.IS_PLAYING, false);
 			settings = command.getSettings();
 			if (settings.containsKey("ENABLED")) {
 				isPlayingEnabled = Boolean.parseBoolean(settings.get("ENABLED"));
 			}
 
 			if (isPlayingEnabled) {
 				command = commandService.sendQuery(command);
 				if (command != null && command.getOutput() != null && command.getOutput() != "") {
 					ICommandHelper helper = CommandHelperFactory.getHelperForCommand(command);
 					
 					String data = helper.evaluateOutputAsString(command.getOutput()).trim();
 					if (data.equalsIgnoreCase("TRUE") || data.equalsIgnoreCase("FALSE")) {
 						// DBus has returned a boolean value
 						out.isPlaying = helper.evaluateOutputAsBoolean(command.getOutput());
 					}
 					else {
 						// DBus has returned a string, only Banshee does this right now
 						out.isPlaying = data.trim().equalsIgnoreCase("PLAYING");
 					}
 				}
 			}
 			
 			/*
 			 * Update seek bar with media position
 			 */
 			
 			command = commandService.queryForCommand(Command.GET_MEDIA_POSITION, false);
 			
 			settings = command.getSettings();
 			if (settings.containsKey("ENABLED")) {
 				mediaPositionEnabled = Boolean.parseBoolean(settings.get("ENABLED"));
 			}
 
 			if (mediaPositionEnabled) {
 				boolean success = false;
 				
 				command = commandService.sendQuery(command);
 				if (command != null && command.getOutput() != null && command.getOutput() != "") {
 					
 					// Get the current position
 					ICommandHelper helper = CommandHelperFactory.getHelperForCommand(command);
 					
 					String data = helper.evaluateOutputAsString(command.getOutput());
 					if (data != null && data.length() > 0) {
 						try {
 							out.mediaProgress.current = Integer.parseInt(data.trim());
 							success = true;
 						}
 						catch (NumberFormatException e) {
 							// Value returned was not a valid integer
 						}
 					}
 					
 					// Get the length of the media being played
 					command = commandService.queryForCommand(Command.GET_MEDIA_LENGTH, false);
 					command = commandService.sendQuery(command);
 					if (success == true && command != null && command.getOutput() != null && command.getOutput() != "") {
 						// Get the current position
 						helper = CommandHelperFactory.getHelperForCommand(command);
 						
 						data = helper.evaluateOutputAsString(command.getOutput());
 						if (data != null && data.length() > 0) {
 							try {
 								out.mediaProgress.max = Integer.parseInt(data.trim());
 							}
 							catch (NumberFormatException e) {
 								// Value returned was not a valid integer
 							}
 						}
 					}
 				}
 			}
 			
 			commandService.unregisterErrorHandler(errorHandler);
 			
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		
 		return out;
 	}
 	
 	private String stripTitleFromUrl(String uri) {
 		return uri.substring(uri.lastIndexOf("/") + 1);
 	}
 
 	protected void onPostExecute(PlaybackUpdateData result) {
 		if (errorTitle != null && errorMessage != null) {
 			if (listener != null) listener.onServiceError(getClass(), errorTitle, errorMessage, command);
 		}
 		else {
 			if (listener != null) {
 				if (mediaInfoEnabled) listener.onMediaInfoChanged(result.info);
 				if (isPlayingEnabled) listener.onPlayingChanged(result.isPlaying);
 				if (mediaPositionEnabled) listener.onMediaProgressChanged(result.mediaProgress.current, result.mediaProgress.max);
 			}
 		}
 	}
 
 	public void registerListener(PlaybackUpdateListener listener) {
 		this.listener = listener;
 	}
 }

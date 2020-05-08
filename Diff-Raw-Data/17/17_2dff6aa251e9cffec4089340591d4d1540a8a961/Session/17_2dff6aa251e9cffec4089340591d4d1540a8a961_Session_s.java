 package libspotifyj;
 
 import com.sun.jna.Callback;
 import com.sun.jna.Memory;
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import libspotifyj.events.SessionEventArgs;
 import libspotifyj.events.SessionEventHandler;
 import libspotifyj.events.SpotifyEventHandler;
 import libspotifyj.events.SpotifyEventItem;
 import libspotifyj.low.SpotifyJ;
 import libspotifyj.low.sp_audioformat;
 import libspotifyj.low.sp_session;
 import libspotifyj.low.sp_session_callbacks;
 import libspotifyj.low.sp_session_config;
 
 public class Session {
 	
 	/* Provides access to libspotify API */
 	private static SpotifyJ libspotify = SpotifyJ.libspotify;
 	
 	/* Session data */
 	private static Map<sp_session, Session> sessions = new HashMap<sp_session, Session>();
 	private sp_session sessionPtr = new sp_session(Pointer.NULL);
 	
 	/* Login data */
 	private Lock loginLock = new ReentrantLock();
 	private Condition userLoggedIn;
 	private int loginCode = Constants.ERROR_IS_LOADING;
 	
 	/* Logout data */
 	private Lock logoutLock = new ReentrantLock();
 	private Condition userLoggedOut;
 	
 	/* Main thread data */
 	private Thread mainThread;
 	private Lock mainThreadLock = new ReentrantLock();
 	private Condition mainThreadShouldProcess = mainThreadLock.newCondition();
 	
 	/* Event thread data */
 	private Thread eventThread;
 	private Lock eventThreadLock = new ReentrantLock();
 	private Condition eventThreadShouldProcess = eventThreadLock.newCondition();
 	private Queue<SpotifyEventItem> eventItemQueue = new LinkedList<SpotifyEventItem>();
 	
 	/* Spotify event handlers */
 	private SpotifyEventHandler logMessageHandler;
 	private SpotifyEventHandler loginHandler;
 	private SpotifyEventHandler logoutHandler;
 	
 	private Session(char[] applicationKey, String cacheLocation, String settingsLocation, String userAgent) throws SpotifyException {
 		sp_session_config config = new sp_session_config();
 		sp_session_callbacks callbacks = new sp_session_callbacks();
 		
 		synchronized (SpotifyJ.lock) {
 			callbacks.logged_in = new LoggedInCallBack();
 			callbacks.logged_out = new LoggedOutCallback();
 			callbacks.metadata_updated = new MetadataUpdatedCallback();
 			callbacks.connection_error = new ConnectionErrorCallback();
 			callbacks.message_to_user = new MessageToUserCallback();
 			callbacks.notify_main_thread = new NotifyMainThreadCallback();
 			callbacks.music_delivery = new MusicDeliveryCallback();
 			callbacks.play_token_lost = new PlayTokenLostCallback();
 			callbacks.log_message = new LogMessageCallback();
 			callbacks.end_of_track = new EndOfTrackCallback();
 			callbacks.streaming_error = new StreamingErrorCallback();
 			callbacks.userinfo_updated = new UserInfoUpdatedCallback();
 			callbacks.start_playback = new StartPlaybackCallback();
 			callbacks.stop_playback = new StopPlaybackCallback();
 			callbacks.get_audio_buffer_stats = new GetAudioBufferStatsPlayback();
 			callbacks.offline_status_updated = new OfflineStatusUpdatesCallback();
 			callbacks.offline_error = new OfflineErrorCallback();
 			callbacks.credentials_blob_updated = new CredentialsBlobUpdatedCallback();
 			callbacks.connectionstate_updated = new ConnectionStateUpdatedCallback();
 			callbacks.scrobble_error = new ScrobbleErrorCallback();
 			callbacks.private_session_mode_changed = new PrivateSessionModeChangedCallback();
 		}
 		
 		PointerByReference sessionPbr = new PointerByReference();
         Pointer applicationKeyPtr = new Memory(applicationKey.length);
         applicationKeyPtr.write(0, toBytes(applicationKey), 0, applicationKey.length);
         
 		config.api_version = SpotifyJ.SPOTIFY_API_VERSION;
 		config.cache_location = cacheLocation;
 		config.settings_location = settingsLocation;
 		config.user_agent = userAgent;
 	    config.application_key = applicationKeyPtr;
 	    config.application_key_size = applicationKey.length;
 		config.callbacks = callbacks;
         
         int code = libspotify.sp_session_create(config, sessionPbr);
         
         if (code != Constants.ERROR_OK) {
         	throw new SpotifyException(code);
         }
         
         sessionPtr = new sp_session(sessionPbr.getValue());
         libspotify.sp_session_process_events(sessionPtr, new IntByReference());
                                         
         mainThread = new MainThread();
         mainThread.start();
         eventThread = new EventThread();
         eventThread.start();
 	}
 	
 	public static Session createInstance(char[] applicationKey, String cacheLocation, String settingsLocation, String userAgent) throws SpotifyException {
 		synchronized (SpotifyJ.lock) {
 			if (sessions.size() > 0) {
 				throw new IllegalStateException("Only one instance of a Spotify session is allowed");
 			} else {
 				Session session = new Session(applicationKey, cacheLocation, settingsLocation, userAgent);
 				sessions.put(session.sessionPtr, session);
 				return session;
 			}
 		}
 	}
 	
     public int getConnectionState() {
     	if (sessionPtr != null) {
     		synchronized (SpotifyJ.lock) {
     			return libspotify.sp_session_connectionstate(sessionPtr);
     		}
     	} else {
     		return Constants.CONNECTIONSTATE_UNDEFINED;
     	}
     }
     
     public User getCurrentUser() {
     	synchronized (SpotifyJ.lock) {
     		return new User(libspotify.sp_session_user(sessionPtr));
     	}
     }
 	
     public int login(String username, String password, long ms) {
     	if (userLoggedIn != null) {
     		return Constants.ERROR_IS_LOADING;
     	} else {
     		int code = Constants.ERROR_OK;
     		synchronized (SpotifyJ.lock) {
     			code = libspotify.sp_session_login(sessionPtr, username, password, false, null);
     		}
     		if (code != Constants.ERROR_OK)
     			return code;
     		else
     			userLoggedIn = loginLock.newCondition();
     	}
     	
     	boolean wasInterrupted = false;
 
     	try {
         	loginLock.lock();
         	wasInterrupted = userLoggedIn.await(ms, TimeUnit.MILLISECONDS);
     		userLoggedIn = null;
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		} finally {
 			loginLock.unlock();
 		}
 		
 		return wasInterrupted ? loginCode : Constants.ERROR_OTHER_TRANSIENT;
     }
     
    public void logout(long ms) {
     	synchronized (SpotifyJ.lock) {
     		if (getConnectionState() == Constants.CONNECTIONSTATE_LOGGED_IN) {
     			int code = libspotify.sp_session_logout(sessionPtr);
     			if (code != Constants.ERROR_OK)
    				return;
     			else
         			userLoggedOut = logoutLock.newCondition();
     		} else {
    			return;
     		}
     	}
     	    	
     	try {
     		logoutLock.lock();
    		userLoggedOut.await(ms, TimeUnit.MILLISECONDS);
     		userLoggedOut = null;
     	} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
     	} finally {
     		logoutLock.unlock();
     	}
     }
     
 	/* Spotify event handler setters */
     public void setLogMessageHandler(SessionEventHandler logMessageHandler) {
     	this.logMessageHandler = logMessageHandler;
     }
     
     public void setLoginHandler(SessionEventHandler loginHandler) {
     	this.loginHandler = loginHandler;
     }
     
     public void setLogoutHandler(SessionEventHandler logoutHandler) {
     	this.logoutHandler = logoutHandler;
     }
 	
 	/* Threads */
 	private class MainThread extends Thread {
     	public void run() {
     		IntByReference nextTimeout = new IntByReference(0);
 
     		while (true) {
     			try {
         			mainThreadLock.lock();
     				if (nextTimeout.getValue() == 0)
     					mainThreadShouldProcess.await();
     				mainThreadLock.unlock();
     				
     				do {
     					synchronized (SpotifyJ.lock) {
     						int code = libspotify.sp_session_process_events(sessionPtr, nextTimeout);
     						if (code != Constants.ERROR_OK)
     							nextTimeout.setValue(1000);
     					}
     				} while (nextTimeout.getValue() == 0);		
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				}
     		}
     	}
 	}
 	
 	private class EventThread extends Thread {
 		public void run() {
 			Queue<SpotifyEventItem> workList = new LinkedList<SpotifyEventItem>();
 			
 			while (true) {
 				try {
 					eventThreadLock.lock();
 					while (eventItemQueue.isEmpty())
 						eventThreadShouldProcess.await();
 				
 					while (eventItemQueue.size() > 0)
 						workList.add(eventItemQueue.poll());
 
 					for (SpotifyEventItem item : workList)
 						item.processEvent();
 					
 					workList.clear();
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				} finally {
 					eventThreadLock.unlock();
 				}
 			}
 		}
 	}
 		
 	/* Private methods */
 	private static Session getSession(sp_session sessionPtr) {
 		return sessions.get(sessionPtr);
 	}
 	
 	private void enqueueSpotifyEventItem(SpotifyEventItem item) {
 		eventThreadLock.lock();
 		eventItemQueue.offer(item);
 		eventThreadShouldProcess.signal();
 		eventThreadLock.unlock();
 	}
 	
     private byte[] toBytes(char[] key){
         byte[] b = new byte[key.length];
         for (int i = 0; i < key.length; i++){
             if (key[i] > 127)
                 b[i] = (byte) (key[i] - 256);
             else
                 b[i] = (byte) key[i];
         }
         return b;
     }
     
     /* Internal callbacks */
     private class NotifyMainThreadCallback implements Callback {
     	public void callback(sp_session session) {
 			Session s = getSession(session);
 			if (s == null)
 				return;
 			
 			try {
 				s.mainThreadLock.lock();
 				s.mainThreadShouldProcess.signal();
 			} finally {
 				s.mainThreadLock.unlock();
 			}
 		}
     }
     
 	private class LoggedInCallBack implements Callback {
 		public void callback(sp_session session, int error) {
 			Session s = getSession(session);
 			if (s == null)
 				return;
 			
 			if (s.getConnectionState() == Constants.CONNECTIONSTATE_LOGGED_IN && error == Constants.ERROR_OK) {
 				//TODO:
 			}
 			
 			if (s.userLoggedIn != null) {
 				s.enqueueSpotifyEventItem(new SpotifyEventItem(s.loginHandler, s, new SessionEventArgs(error)));
 				s.loginLock.lock();
 				s.loginCode = error;
 				s.userLoggedIn.signal();
 				s.loginLock.unlock();
 			}
 		}
 	}
 	
 	private class LoggedOutCallback implements Callback {
 		public void callback(sp_session session) {
 			Session s = getSession(session);
 			if (s == null)
 				return;
 			
 			//TODO:
 			/*if (s.playlistContainter != null) {
 				
 			}*/
 			
 			if (s.userLoggedIn != null) {
 				s.loginLock.lock();
 				s.userLoggedIn.signal();
 				s.loginLock.unlock();
 			}
 			
 			if (s.userLoggedOut != null) {
 				s.enqueueSpotifyEventItem(new SpotifyEventItem(s.logoutHandler, s, new SessionEventArgs()));
 				s.logoutLock.lock();
 				s.userLoggedOut.signal();
 				s.logoutLock.unlock();
 			}
 		}
 	}
     
 	private class LogMessageCallback implements Callback {
 		public void callback(sp_session session, String message) {
 			Session s = getSession(session);
 			if (s == null)
 				return;
 			
 			s.enqueueSpotifyEventItem(new SpotifyEventItem(s.logMessageHandler, s, new SessionEventArgs(message)));
 		}
 	}
 	
 	private class MetadataUpdatedCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("metadata() called");
 		}
 	}
 	
 	private class ConnectionErrorCallback implements Callback {
 		public void callback(sp_session session, int error) {
 			System.out.println("conerror() called");
 		}
 	}
 	
 	private class MessageToUserCallback implements Callback {
 		public void callback(sp_session session, String message) {
 			System.out.println("msgtouser() called");
 		}
 	}
 	
 	private class MusicDeliveryCallback implements Callback {
 		public void callback(sp_session session, sp_audioformat format, Pointer frames, int num_frames) {
 			System.out.println("musicdelivery() called");
 		}
 	}
 	
 	private class PlayTokenLostCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("playtoken() called");
 		}
 	}
 	
 	private class EndOfTrackCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("endoftrack() called");
 		}
 	}
 	
 	private class StreamingErrorCallback implements Callback {
 		public void callback(sp_session session, int error) {
 			System.out.println("strerror() called");
 		}
 	}
 	
 	private class UserInfoUpdatedCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("usrinfo() called");
 		}
 	}
 	
 	private class StartPlaybackCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("startplayback() called");
 		}
 	}
 	
 	private class StopPlaybackCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("stopplayback() called");
 		}
 	}
 	
 	private class GetAudioBufferStatsPlayback implements Callback {
 		public void callback(sp_session session, PointerByReference stats) {
 			System.out.println("getaudiobuffer() called");
 		}
 	}
 	
 	private class OfflineStatusUpdatesCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("offlinestatus() called");
 		}
 	}
 	
 	private class OfflineErrorCallback implements Callback {
 		public void callback(sp_session session, int error) {
 			System.out.println("offlinerror() called");
 		}
 	}
 	
 	private class CredentialsBlobUpdatedCallback implements Callback {
 		public void callback(sp_session session, String blob) {
 			System.out.println("credblob() called");
 		}
 	}
 	
 	private class ConnectionStateUpdatedCallback implements Callback {
 		public void callback(sp_session session) {
 			System.out.println("connstate() called");
 		}
 	}
 	
 	private class ScrobbleErrorCallback implements Callback {
 		public void callback(sp_session session, int error) {
 			System.out.println("scrobberror() called");
 		}
 	}
 	
 	private class PrivateSessionModeChangedCallback implements Callback {
 		public void callback(sp_session session, boolean is_private) {
 			System.out.println("privatesession() called");
 		}
 	}
 
 }

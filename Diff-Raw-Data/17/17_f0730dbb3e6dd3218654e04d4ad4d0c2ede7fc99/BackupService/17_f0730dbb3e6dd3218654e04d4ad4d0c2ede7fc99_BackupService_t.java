 /**
  * Copyright (C) Uppidy Inc, 2012
  */
 package com.uppidy.android.sdk.backup;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import org.springframework.social.connect.ConnectionRepository;
 
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 import com.uppidy.android.sdk.api.ApiContactInfo;
 import com.uppidy.android.sdk.api.ApiMessage;
 import com.uppidy.android.sdk.api.ApiSync;
 import com.uppidy.android.sdk.api.BackupOperations;
 import com.uppidy.android.sdk.api.Uppidy;
 
 import de.akquinet.android.androlog.Log;
 
 /**
  * @author Vyacheslav Mukhortov
  * There are 2 ways to initiate the backup service:
  * <ul>
  *   <li> by calling Context.startService with user-defined intent action used while registering {@link MessageProvider}
  *   <li> by calling Context.startService with an intent action {@link BackupService.ACTION_BACKUP_ALL}
  * </ul>
  * Requires the following permissions: ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE
  */
 public abstract class BackupService extends IntentService
 {	
 	protected static String TAG = "BackupService";
 	public static final String ACTION_BACKUP_ALL = "com.uppidy.android.sdk.backup.BACKUP_ALL";
 	
 	private HashMap<String,MessageProvider> providers = new HashMap<String, MessageProvider>();
 	
 	/**
 	 * @param serviceName service name
 	 */
 	public BackupService(String serviceName)
 	{
 		super(serviceName);
 	}
 
 	/**
 	 * Adds a new {@link MessageProvider} and associates it with an {@link Intent} {@code action}. 
 	 * This method is thread-safe.
 	 * @param action - {@link Intent} action to associate with
 	 * @param mp - {@link MessageProvider} implementation
 	 */
 	public synchronized void addMessageProvider( String action, MessageProvider mp )
 	{
 		providers.put( action, mp );
 	}
 
 	/**
 	 * Removes {@link MessageProvider} associated with {@code action} passed
 	 * @param action
 	 */
 	public synchronized void removeMessageProvider( String action )
 	{
 		providers.remove( action );
 	}
 	
 	protected Collection<MessageProvider> getMessageProviders()
 	{
 		return providers.values();
 	}
 	
 	/**
 	 * Returns {@link MessageProvider} associated with an {@code action}
 	 * @param action
 	 * @return {@link MessageProvider} or null if not found
 	 */
 	public synchronized MessageProvider getMessageProvider( String action )
 	{
 		return providers.get(action);
 	}
 	
 	/**
 	 * Must return Uppidy ConnectionRepository 
 	 * @return ConnectionRepository - Uppidy ConnectionRepository
 	 */
 	protected abstract ConnectionRepository getUppidyConnectionRepository();
 	
 	/**
	 * This method is called on any exception caught during Intent processing.
	 * @param ex
	 */
	protected void onError( Exception ex )
	{
		String msg = ex.getMessage();
		Log.d( TAG, msg == null ? ex.toString() : msg );
	}

	/**
 	 * Must return true if backup is enabled.
 	 * <p>{@link BackupService} checks the return value of this method on every intent received 
 	 * and doesn't perform backup operations if this method returns if {@code false}.
 	 * <p> Default implementation always returns {@code true}
 	 * @return boolean 
 	 */
 	protected boolean isEnabled()
 	{
 		return true;
 	}
 	
 	/**
 	 * Default implementation returns {@code true} if any data network is available.
 	 * @return
 	 */
 	protected boolean isOnline() 
 	{
 	    ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 	    return (networkInfo != null && networkInfo.isConnected());
 	} 	
 	
 	/**
 	 * Handles 2 types of intents:
 	 * <ul>
 	 *   <li> BackupService.ACTION_BACKUP_ALL - starts backup on all providers if isEnabled() returns true 
 	 *   and network is available
 	 *   <li> user defined intents passed while registering {@link MessageProvider}s
 	 * </ul>
 	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
 	 */
 	@Override
 	protected void onHandleIntent(Intent intent)
 	{
 		try
 		{
 			String action = intent.getAction();
 			MessageProvider mp;
 			if( action.equals( ACTION_BACKUP_ALL ) ) 
 	    	{
 				if( isEnabled() ) backupAll(); 
 	    	}
 			// user-defined intents     
 			else if( (mp = providers.get(action)) != null ) 
 			{
 				if( isEnabled() )
 				{
 					Log.i( TAG, "User defined intent (" + action + ") received but won't be processed because the service is not enabled."
 							+ " To enable the service, send an intent with action BackupService.ACTION_START." );
 				}
 				else 
 				{
 					Log.i( TAG, "User defined intent (" + action + ") received, starting backup on container identified by " + mp.getContainerId() );
 					while( doBackup( mp ) );
 				}
 			}
 		} 
 		catch( Exception ex )
 		{
			onError( ex );
 		}
 	}
 
 	private void backupAll()
 	{
 		if( !isOnline() ) return;
 		// keep synchronizing portion by portion until there is nothing to do
 		boolean somethingDone;
 		do
 		{
 			somethingDone = false;
 			for( MessageProvider mp : providers.values() )
 			{
 				somethingDone |= doBackup( mp );
 			}
 		} while( somethingDone );
 	}
 	
 	private boolean doBackup( MessageProvider mp )
 	{
 		if( mp == null || !isOnline() ) return false;
 		List<ApiMessage> messages = mp.getNextSyncBundle();
 		if( messages == null || messages.size() == 0 ) return false;
 		
 		Log.i( TAG, "Backing up " + messages.size() + " messages on container identified by " + mp.getContainerId() );
 
 		Uppidy uppidy = getUppidyConnectionRepository().findPrimaryConnection(Uppidy.class).getApi();
 		if( uppidy == null )
 		{
 			Log.e( TAG, "Uppidy connection not found. Backup aborted." );
 			return false;
 		}
 		List<String> addresses = new ArrayList<String>();
 		for( ApiMessage m : messages ) 
 		{
 			addresses.add( m.getFrom().getAddress() );
 			for( ApiContactInfo toRef : m.getTo() ) addresses.add( toRef.getAddress() );
 		}
 		BackupOperations backupOperations = uppidy.backupOperations();
 		
 		ApiSync sync = new ApiSync();
 		sync.setContacts(mp.getContacts(addresses));
 		sync.setMessages(messages);
 		backupOperations.sync( mp.getContainerId(), sync );
 		
 		mp.backupDone( messages );
 		Log.i( TAG, "Backup complete" );
 		
 		return true;
 	}
 }

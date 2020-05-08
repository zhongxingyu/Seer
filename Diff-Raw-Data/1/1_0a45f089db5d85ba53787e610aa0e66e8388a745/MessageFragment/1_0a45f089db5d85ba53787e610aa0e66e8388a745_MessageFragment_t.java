 package br.com.thiagopagonha.psnapi;
 
 import static br.com.thiagopagonha.psnapi.utils.CommonUtilities.*;
 import static br.com.thiagopagonha.psnapi.utils.CommonUtilities.TAG;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import br.com.thiagopagonha.psnapi.gcm.ServerUtilities;
 
 import com.google.android.gcm.GCMRegistrar;
 
 public class MessageFragment extends Fragment {
 
 	TextView mDisplay;
 	AsyncTask<Void, Void, Void> mRegisterTask;
 
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.activity_message, container, false); 
 		
 		mDisplay = (TextView) view.findViewById(R.id.display);
 		
 		refreshView();
 		
 		return view;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setHasOptionsMenu(true);
 		
 		final Context context = getActivity().getApplicationContext();
 		
 		// Make sure the device has the proper dependencies.
 		GCMRegistrar.checkDevice(context);
 		// Make sure the manifest was properly set - comment out this line
 		// while developing the app, then uncomment it when it's ready.
 		GCMRegistrar.checkManifest(context);
 		
 		Log.d(TAG, "Registered Message Handler");
 		
 		final String regId = GCMRegistrar.getRegistrationId(context);
 		
 		Log.d(TAG,"Registration ID:" + regId);
 		
 		if (regId.equals("")) {
 			// Automatically registers application on startup.
 			backGround(new Logic() {
 				void execute() {
 					GCMRegistrar.register(context, SENDER_ID);
 				}
 			});
 		} else {
 			// Device is already registered on GCM, check server.
 			if (!GCMRegistrar.isRegisteredOnServer(context)) {
 				// Try to register again, but not in the UI thread.
 				// It's also necessary to cancel the thread onDestroy(),
 				// hence the use of AsyncTask instead of a raw thread.
 				backGround(new Logic() {
 					void execute() {
 						ServerUtilities.register(context, regId);
 					}
 				});
 			}
 		}
 	}
 
 	// -- Lógica abaixo
 	abstract class Logic {
 		abstract void execute();
 	}
 	
 	/**
 	 * Executa uma tarefa em background
 	 * @param f
 	 */
 	private void backGround(final Logic f) {
 		mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				f.execute();
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				mRegisterTask = null;
 			}
 
 		};
 		mRegisterTask.execute(null, null, null);
 	}
 	
 	
 	@Override
 	 public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.activity_message, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		final Context context = getActivity().getApplicationContext();
 		
 		switch (item.getItemId()) {
 		case R.id.options_clear:
 			clearLog();
 			return true;
 		case R.id.options_unregister:
 			GCMRegistrar.unregister(context);
             return true;
 		case R.id.options_sync:
 			backGround(new Logic() {
 				void execute() {
 					ServerUtilities.sync(context);
					refreshView();
 				}
 			});
 			
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onDestroy() {
 		final Context context = getActivity().getApplicationContext();
 		
 		if (mRegisterTask != null) {
 			mRegisterTask.cancel(true);
 		}
 		GCMRegistrar.onDestroy(context);
 		super.onDestroy();
 		
 	}
 
 	private String readLog()  {
 		
 		InputStream inputStream;
 		try {
 			inputStream = getActivity().openFileInput(FILENAME);
 	     
 		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		     
 		    int i;
 		    i = inputStream.read();
 		    while (i != -1) {
 		    	byteArrayOutputStream.write(i);
 		    	i = inputStream.read();
 		    } 
 		    inputStream.close();
 		    
 		    return byteArrayOutputStream.toString();
 		} catch(IOException io) {
 			Log.e(TAG, "Não foi possível ler arquivo de log");
 		}
 	  
 	    return null;
 	}
 	
 
 	private void clearLog()  {
 		try {
 			getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			refreshView();
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "Não foi possível reiniciar arquivo de log");
 		}
 	}
 	
 	private void refreshView() {
 		mDisplay.setText(readLog());
 	}
 	
 
 }

 package lu.albert.android.contactbackup;
 
 import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.content.ContentResolver;
 import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Contacts;
 import android.provider.Contacts.People;
import android.provider.Contacts.Photos;
 
 /**
  * The thread which reads the input file and restores the contacts.
  * 
  * Note that the file is *streamed* from the disk, and the contacts are read
  * on-the-fly. In other words, the application does not perform this in a
  * "Transaction". If a crash occurs, only the contacts that have been read
  * will be restored, and those that have been deleted beforehand will be lost!
  * 
  * You can however re-start the operation anytime.
  * 
  * @author Michel Albert <michel.albert@statec.etat.lu>
  */
 public class RestoreThread extends Thread{
 	
 	Handler mRestoreHandler;
 	final static int STATE_DONE = 0;
 	final static int STATE_RUNNING = 1;
 	int mState;
 	int total;
 	private ContactBackup mParent;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param dialog_handler A handler which is used to communicate with the progress dialog
 	 * @param parent The main Activity (UI) class
 	 */
 	RestoreThread(Handler dialog_handler, ContactBackup parent ) {
 		mRestoreHandler = dialog_handler;
 		mParent = parent;
 	}
 
 	/**
 	 * sets the current state for the thread, used to stop the thread
 	 * @param state The new state
 	 */
 	public void setState(int state) {
 		mState = state;
 	}
 	
 	public void run() {
 
 		mParent.getContentResolver().delete(People.CONTENT_URI, null, null);
 		File file1 = null;
 		file1 = new File(Environment.getExternalStorageDirectory(),
 				ContactBackup.FILE_NAME);
 		
 		this.readStream(file1);
 		
 		mState = STATE_DONE;
 	}
 
 	/**
 	 * Read the on-disk data by streaming it without creating a complete JSONArray in memory
 	 * 
 	 * @param in_file The input file
 	 */
 	private void readStream(File in_file) {
 		
 		StringBuffer data = new StringBuffer();
 		FileInputStream file_stream = null;
 		JSONObject contact = null;
 		
 		try {
 			file_stream = new FileInputStream(in_file);
 			BufferedInputStream stream_buffer = new BufferedInputStream(file_stream);
 			InputStreamReader reader = new InputStreamReader(stream_buffer);
 			
 			int data_available = reader.read();
 			int braceDepth = 0; // used to trigger JSON decoding
 			// TODO: contactOpen is not necessary => contactOpen = data.length() > 0
 			boolean contactOpen = false; // used to trigger JSON decoding
 			long count = 0; // characters read
 			
 			while( data_available != -1 ) {
 				char theChar = (char) data_available;
 				
 				/*
 				 * When a new object opens on the root level, then we have the
 				 * beginning of a new contact. Set the appropriate flag and
 				 * empty the buffer (create a new one)
 				 */
 				if (theChar == '{') {
 					if (braceDepth == 0) {
 						contactOpen = true;
 						data = new StringBuffer();
 					}
 					braceDepth += 1;
 				} else if (theChar == '}') {
 					braceDepth -= 1;
 				}
 				data.append(theChar);
 				
 				/*
 				 * So, the structure depths has been reduced to 0. So we are
 				 * back on the "contacts" level. If we still have a contact
 				 * open, we take that string, parse it and store the
 				 * resulting contact.
 				 * 
 				 *  Additionally, we let the user know that the contact has
 				 *  been restored.
 				 */
 				if (braceDepth == 0 && contactOpen) {
 					contact = new JSONObject( data.toString() );
 					store_contact( contact );
 					contactOpen = false;
 					
 					Message msg = mRestoreHandler.obtainMessage(ContactBackup.RESTORE_MSG_INFO);
 					Bundle b = new Bundle();
 					b.putString("name", contact.getString("name"));
 					msg.setData(b);
 					mRestoreHandler.sendMessage(msg);
 				}
 				
 				data_available = reader.read();
 				count += 1;
 				
 				/*
 				 * Update the progress dialog
 				 */
 				if ( count % 100 == 0) {
 					Message msg = mRestoreHandler.obtainMessage(ContactBackup.RESTORE_MSG_PROGRESS);
 					Bundle b = new Bundle();
 					b.putLong("position", count);
 					b.putLong("total", in_file.length());
 					msg.setData(b);
 					mRestoreHandler.sendMessage(msg);
 				}
 
 			}
 			
 			/*
 			 * Update the progress dialog, ensuring that it properly triggers
 			 * it's end-of-life ( as we only update the progress every so
 			 * often, it could happen that the value does not meet the EOL
 			 * criteria.
 			 */
 			Message msg = mRestoreHandler.obtainMessage(ContactBackup.RESTORE_MSG_PROGRESS);
 			Bundle b = new Bundle();
 			b.putLong("position", in_file.length());
 			b.putLong("total", in_file.length());
 			msg.setData(b);
 			mRestoreHandler.sendMessage(msg);
 			
 			/*
 			 * Clean up
 			 */
 			reader.close();
 			stream_buffer.close();
 			file_stream.close();
 		} catch (FileNotFoundException e) {
 			showError(e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			showError(e.getMessage());
 			e.printStackTrace();
 		} catch (JSONException e) {
 			showError(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create a new contact on the device
 	 * 
 	 * @param contact The contact to be created
 	 * @throws JSONException when unable to decode the JSON elements
 	 */
 	private void store_contact(JSONObject contact) throws JSONException {
 		
		ContentResolver cr = mParent.getContentResolver();
		
 		/*
 		 * Store base values
 		 */
 		ContentValues values = new ContentValues();
 		values.put( People._ID, contact.getString("id") );
		values.put( People.NAME, contact.getString("name") );
 		values.put( People.TIMES_CONTACTED, contact.getString("times_contacted") );
		// TODO: values.put( People.DISPLAY_NAME, contact.getString("display_name") );
 		values.put( People.STARRED, contact.getInt("starred") );
 		
 		Uri uri = Contacts.People
		  .createPersonInMyContactsGroup(cr, values);
 		
 		if ( uri == null) {
 			return;
 		}
 		
 		/*
 		 * Store phone numbers
 		 */
 		JSONArray phones = contact.getJSONArray("phonenumbers");
 		for( int i = 0; i < phones.length(); ++i ){
 			JSONObject phone = phones.getJSONObject(i);
 			if (phone == null ){
 				continue;
 			}
 			
 			Uri phoneUri = null;
 			phoneUri = Uri.withAppendedPath(uri, People.Phones.CONTENT_DIRECTORY);
 			values.clear();
 			values.put(People.Phones.TYPE, phone.getInt( "type" ));
 			values.put(People.Phones.NUMBER, phone.getString("number"));
 			values.put(People.Phones.ISPRIMARY, (phone.getBoolean("is_primary") ? 1 : 0));
			cr.insert(phoneUri, values);
 		}
 		phones = null;
 		
 		/*
 		 * Store photo
 		 */
 		JSONArray photos = contact.getJSONArray("photos");
 		if ( photos.length() > 0 ) {
 			String photo = photos.getString(0);
			if (photo != null && !photo.equals("") ){
 				try {
					Contacts.People.setPhotoData(cr, uri, Base64.decode(photo));
					System.out.println( "Added photo to " + uri );
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		photos = null;
 	}
 	
 	private void showError( String message ){
 		Message msg = mRestoreHandler.obtainMessage(ContactBackup.RESTORE_SHOW_ERROR);
 		Bundle b = new Bundle();
 		b.putString("message", message);
 		msg.setData(b);
 		mRestoreHandler.sendMessage(msg);
 	}
 }
 

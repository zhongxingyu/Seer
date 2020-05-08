 /*
  * (c) 2013 panter llc, Zurich, Switzerland.
  */
 package ch.bergturbenthal.raoa.provider.activity;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.net.URLConnection;
 
 import android.R;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import ch.bergturbenthal.raoa.provider.Client;
 import ch.bergturbenthal.raoa.provider.util.IOUtil;
 
 /**
  * TODO: add type comment.
  * 
  */
 public class ShareReceiveActivity extends ListActivity {
 	@Override
 	protected void onCreate(final Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		final Intent intent = getIntent();
 		final String action = intent.getAction();
 		final String type = intent.getType();
 		final Bundle extras = intent.getExtras();
 		final String basename = (Build.MANUFACTURER + "-" + Build.MODEL + "-" + Build.SERIAL).replace(' ', '_');
 		Log.i("Share", "Action: " + action);
 		Log.i("Share", "Type: " + type);
 		final Uri dataUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
 		final String guessedType = guessType(dataUri);
 		final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(guessedType);
 		final String filename = basename + "." + extension;
 		Log.i("Share", "Filename: " + filename);
 		final byte[] data = readData(dataUri);
 
 		final Cursor cursor = getContentResolver().query(	Client.SERVER_URI,
 																											new String[] { Client.ServerEntry.ID, Client.ServerEntry.SERVER_NAME, Client.ServerEntry.SERVER_ID },
 																											null,
 																											null,
 																											null);
 		startManagingCursor(cursor);
 		final ListAdapter adapter = new SimpleCursorAdapter(this,
 																												R.layout.simple_list_item_1,
 																												cursor,
 																												new String[] { Client.ServerEntry.SERVER_NAME },
 																												new int[] { R.id.text1 });
 		setListAdapter(adapter);
 		final ListView listView = getListView();
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				cursor.move(position - 1);
 				final int index = cursor.getColumnIndexOrThrow(Client.ServerEntry.SERVER_ID);
 				final String serverId = cursor.getString(index);
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(final Void... params) {
 						new Client(getContentResolver()).importFile(serverId, filename, data);
 						return null;
 					}
 				}.execute();
 				finish();
 			}
 		});
 	}
 
 	private String guessType(final Uri dataUri) {
 		final String guessedType = URLConnection.guessContentTypeFromName(dataUri.toString());
 		if (guessedType == null) {
 			return "image/jpeg";
 		}
 		return guessedType;
 	}
 
 	/**
 	 * @param dataUri
 	 * @return
 	 */
 	private byte[] readData(final Uri dataUri) {
 		try {
 			final InputStream inputStream = getContentResolver().openInputStream(dataUri);
 			return IOUtil.readStream(inputStream);
 		} catch (final FileNotFoundException e) {
 			throw new RuntimeException("Cannot read " + dataUri, e);
 		}
 	}
 }

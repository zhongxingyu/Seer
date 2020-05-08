 package net.za.acraig.movefiles;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MoveFilesActivity extends Activity 
 	{
 	private File _src;
 	private File _dest;
 	private int _srcc = 0;
 	private boolean _moveMode = false;
 	private boolean _clearMode = false;
 	private boolean _share = false;
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 		{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	
 		View ruler = new View(this); 
 		ruler.setBackgroundColor(0xFFFFFFFF);
 		LinearLayout parent = (LinearLayout) findViewById(R.id.linearLayout1);
 		parent.addView(ruler, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1));
 
 		initFromPreferences();
 		updateSourceControls();
 		updateDestinationControls();
 		enableControls(getCanCopy());
 		}
 
 
 	private void initFromPreferences()
 		{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		_src = directoryFromPreference("srcPref", "0");
 		_dest = directoryFromPreference("destPref", "2");
 		_moveMode = prefs.getBoolean("deletePref", false);
 		_share = prefs.getBoolean("sharePref", false);
 		}
 
 
 	private File directoryFromPreference(String key, String defValue)
 		{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		int value = Integer.parseInt(prefs.getString(key, defValue));
 		
 		switch (value)
 			{
 			case 0:
 				return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
 			case 1:
 				return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 			case 2:
 				{
 				File extStorage = Environment.getExternalStorageDirectory();
 				File usbHost = new File(extStorage, "usbStorage");
 				if (usbHost != null && usbHost.exists() && usbHost.isDirectory())
					return new File(usbHost, "UsbDriveA");
 				
 				break;
 				}
 			}
 		return null;
 		}
 
 
 	private void updateSourceControls()
 		{
 		CountVisitor srcv = new CountVisitor();
 		srcv.visit(_src);
 		_srcc = srcv.getCount();
 
 		TextView srcDir = (TextView) findViewById(R.id.src);
 		srcDir.setText("Src = " + getDirectoryDescription(_src));
 		TextView srcText = (TextView) findViewById(R.id.srccount);
 		srcText.setText("Files = " + String.valueOf(_srcc));
 		}
 
 
 	private void updateDestinationControls()
 		{
 		CountVisitor destv = new CountVisitor();
 		destv.visit(_dest);
 		int destc = destv.getCount();
 		
 		TextView destDev = (TextView) findViewById(R.id.dest);
 		destDev.setText("Dest = " + getDirectoryDescription(_dest));
 		TextView destText = (TextView) findViewById(R.id.destcount);
 		destText.setText("Files = " + String.valueOf(destc));
 		}
 
 
 	private String getDirectoryDescription(File dir)
 		{
 		if (dir != null)
 			{
 			if (dir.exists() && dir.isDirectory())
 				return dir.getAbsolutePath();
 			else
 				return "Directory does not exist, or is a file.";
 			}
 		else
 			return "Directory not found (null)";
 		}
 
 
 	private void enableControls(boolean enabled)
 		{
 		Button copyButton = (Button) findViewById(R.id.copyButton);
 		copyButton.setEnabled(enabled);
 
 		CheckBox deleteBeforeBox = (CheckBox) findViewById(R.id.deleteBefore);
 		deleteBeforeBox.setEnabled(enabled);
 
 		CheckBox deleteAfterBox = (CheckBox) findViewById(R.id.deleteAfter);
 		deleteAfterBox.setEnabled(enabled);
 		}
 
 
 	private boolean getCanCopy()
 		{
 		return (_src != null && _dest != null && !_src.equals(_dest) && _srcc > 0 && _src.exists() && _src.isDirectory() && _dest.exists() && _dest.isDirectory());
 		}
 
 
 	public void onCopyClick(View view)
 		{
 		CheckBox deleteBeforeBox = (CheckBox) findViewById(R.id.deleteBefore);
 		_clearMode = deleteBeforeBox.isChecked();
 
 		CheckBox deleteAfterBox = (CheckBox) findViewById(R.id.deleteAfter);
 		_moveMode = deleteAfterBox.isChecked();
 		
 		enableControls(false);
 		
 		CopyFilesTask task = new CopyFilesTask();
 		task.execute();
 		}
 
 	public void onSwapClick(View view)
 		{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String a = prefs.getString("srcPref", "0");
 		String b = prefs.getString("destPref", "2");
 
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString("srcPref", b);
 		editor.putString("destPref", a);
 		editor.commit();
 
 		initFromPreferences();
 		updateSourceControls();
 		updateDestinationControls();
 		enableControls(getCanCopy());
 		}
 	
 	/// Asynctask does background copying, and done as an inner class so as to easily update controls on completion. 
 	private class CopyFilesTask extends AsyncTask<String, Void, String> 
 		{
 		@Override
 		protected String doInBackground(String... urls) 
 			{
 			CopyVisitor copyv = new CopyVisitor(_dest);
 			copyv.setMoveMode(_moveMode);
 			copyv.visit(_src);
 			return copyv.getResult();
 			}
 
 		@Override
 		protected void onPreExecute()
 			{
 			if (_clearMode)
 				{
 				DeleteVisitor deletev = new DeleteVisitor();
 				deletev.visit(_dest);
 				}
 			super.onPreExecute();
 			}
 
 		@Override
 		protected void onPostExecute(String result) 
 			{
 			if (_moveMode)
 				{
 				// cleanup empty directories
 				DeleteVisitor deletev = new DeleteVisitor();
 				deletev.visit(_src);
 				}
 
 			updateSourceControls();
 			updateDestinationControls();
 			enableControls(getCanCopy());
 
 			Toast toast = Toast.makeText(MoveFilesActivity.this, "Copy completed", Toast.LENGTH_SHORT);
 			toast.show();
 
 			if (_share)
 				{
 				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
 				intent.setType("text/plain");
 				intent.putExtra(android.content.Intent.EXTRA_TEXT, result);
 				startActivity(intent);
 				}
 			}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 		{
 		MenuItem prefItem = menu.add(Menu.NONE, 0, 0, "Preferences");
 		prefItem.setIcon(android.R.drawable.ic_menu_preferences);
 		return super.onCreateOptionsMenu(menu);
 		}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 		{
 		if (0 == item.getItemId())
 			startActivityForResult(new Intent(this, QuickPrefsActivity.class), 1);
 
 		return super.onOptionsItemSelected(item);
 		}
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
 		{
 		if (1 == requestCode)
 			{
 			initFromPreferences();
 			updateSourceControls();
 			updateDestinationControls();
 			enableControls(getCanCopy());
 			}
 		super.onActivityResult(requestCode, resultCode, data);
 		}
 
 }

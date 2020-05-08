 /**
  * @author Yixin Zhu
  */
 package com.comic.viewer;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.comic.globals.Globals;
 
 public class MainPage extends ListActivity {
 	//list of volume names
 	private String[] volumeNames = {Globals.VolSixName, Globals.VolFiveName, 
 			Globals.VolFourName, Globals.VolThreeName, 
 			Globals.VolTwoName, Globals.VolOneName, Globals.VolZeroName};
 	//list of volume info
 	private String[] volumeInfo = {Globals.VolSixInfo, Globals.VolFiveInfo, 
 			Globals.VolFourInfo, Globals.VolThreeInfo, 
 			Globals.VolTwoInfo, Globals.VolOneInfo, Globals.VolZeroInfo};
 	private final String copyrightBundleKey = "copyrightDialogBundle";
 	private final String helpBundleKey = "helpDialogBundle";
 	private AlertDialog copyrightDialog, helpDialog;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.main);
 		//set the main menu list
 		setListAdapter(new ListVolumesAdapter(this));
 		//handle for screen orientation change
 		if (savedInstanceState != null) {
 			if (savedInstanceState.getBundle(copyrightBundleKey) != null) {
 				//launch copyright dialog
 				buildCopyrightDialog(Globals.CopyrightTitle, Globals.CopyrightMessage);
 			} else if (savedInstanceState.getBundle(helpBundleKey) != null) {
 				//launch help dialog
 				buildHelpDialog(Globals.HelpTitle);
 			}
 		}
		else
			launchLastComic();
 	}
 	
 	/**
 	 * Launches last comic viewed
 	 */
 	private void launchLastComic() {
 		SharedPreferences prefs = getSharedPreferences("VOLUME_SAVES", 0);
 		int i = -1, lastViewed = -1;
 		for(i = Globals.MAX_VOLUMES; i >= 0 && lastViewed == -1; i--)
 			lastViewed = prefs.getInt("lastComic" + i, -1);
 		if(i >= 0)
 			launchVolume(Globals.MAX_VOLUMES - (i + 1));
 	}
 	
 	/**
 	 * Launches the corresponding volume based on user selection
 	 * 
 	 * @param volumeIndex The index of the user selection
 	 */
 	public void launchVolume(int volumeIndex) {
 		Toast.makeText(this, "Beginning Volume - "+ (Globals.MAX_VOLUMES - volumeIndex), 1000).show();
 		Intent i = new Intent(this, ComicViewer.class);
 		switch (volumeIndex) {
 			case 6: //volume zero
 				i.putExtra("volumeRange", Globals.ZeroRange);
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 			case 5: //volume one
 				i.putExtra("volumeRange", Globals.OneRange);
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 			case 4: //volume two
 				i.putExtra("volumeRange", Globals.TwoRange);
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 			case 3: //volume three
 				i.putExtra("volumeRange", Globals.ThreeRange);
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 			case 2: //volume four
 				i.putExtra("volumeRange", Globals.FourRange);
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 			case 1: //volume five
 				i.putExtra("volumeRange", Globals.FiveRange);
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 			case 0: //volume six
 				i.putExtra("volumeRange", ComicUtils.lastVolumeRange(6));
 				i.putExtra("volumeNumber", (Globals.MAX_VOLUMES - volumeIndex));
 				break;
 		}
 		startActivity(i);
 	}
 	
 	/**
 	 * Used to create an options menu
 	 */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	menu.add(Menu.NONE, Globals.HELP_ID, Menu.NONE, "Help");
     	menu.add(Menu.NONE, Globals.COPYRIGHT_ID, Menu.NONE, "Copyright");
     	return super.onCreateOptionsMenu(menu);
     }
 	
 	/**
 	 * Called when an option is selected
 	 */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
     }
     
     /**
      * Performs the action on a selected item choice
      * 
      * @param item The id of the item selected
      * @return true if the item selected was performed
      */
 	private boolean applyMenuChoice(MenuItem item) {
 		switch(item.getItemId())
 		{
 		case Globals.HELP_ID: //display help menu
 			buildHelpDialog(Globals.HelpTitle);
 			return true;
 		case Globals.COPYRIGHT_ID: //display copyright information
 			buildCopyrightDialog(Globals.CopyrightTitle, Globals.CopyrightMessage);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Builds a help alert dialog displaying a title and message
 	 * 
 	 * @param title The title of the alert dialog
 	 */
 	private void buildHelpDialog(String title) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		//inflate view for setting of content
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.help_dialog_context,
                 (ViewGroup) findViewById(R.id.layout_root));
 		builder.setTitle(title); //sets title
 		builder.setView(layout); //sets content
 		builder.setNegativeButton("OK", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		helpDialog = builder.create();
 		helpDialog.show(); //display
 	} 
 	
 	/**
 	 * Builds an copyright alert dialog displaying a title and message
 	 * 
 	 * @param title The title of the alert dialog
 	 * @param message The content of the alert dialog
 	 */
 	private void buildCopyrightDialog(String title, String message) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		//inflate view for setting of content
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.copyright_dialog_context,
                 (ViewGroup) findViewById(R.id.layout_root));
 		TextView text = (TextView) layout.findViewById(R.id.text);
 		text.setText(message);
 		builder.setTitle(title); //sets title
 		builder.setView(layout); //sets content
 		builder.setNegativeButton("OK", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		copyrightDialog = builder.create();
 		copyrightDialog.show(); //display
 	}
 	
 	/**
 	 * Called when screen orientation is changed
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		if (copyrightDialog != null && copyrightDialog.isShowing()){
 			//save copyright dialog if screen orientation changed
 			outState.putBundle(copyrightBundleKey, copyrightDialog.onSaveInstanceState());
 		} else if (helpDialog != null && helpDialog.isShowing()){
 			//save help dialog if screen orientation changed
 			outState.putBundle(helpBundleKey, helpDialog.onSaveInstanceState());
 		}
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position,long id){
 		launchVolume(position); //perform action based on user click
 	}
 	
 	/**
 	 * Custom adapter for custom list view
 	 * 
 	 * @author Yixin Zhu
 	 */
 	class ListVolumesAdapter extends BaseAdapter implements Filterable {
 		private LayoutInflater mInflater;
 
 		public ListVolumesAdapter(Context context) {
 			// Cache the LayoutInflate to avoid asking for a new one each time.
 			mInflater = LayoutInflater.from(context);
 		}
 		
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.main_context,
 						null);
 				// Creates a ViewHolder and store references to the two children
 				// views we want to bind data to.
 				holder = new ViewHolder();
 				holder.volumeName = (TextView) convertView
 						.findViewById(R.id.volumeName);
 				holder.volumeDescription = (TextView) convertView
 						.findViewById(R.id.volumeDescription);
 				holder.volumeDescriptionBold = (TextView) convertView
 						.findViewById(R.id.volumeDiscriptionBold);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 			holder.volumeName.setText(volumeNames[position]);
 			holder.volumeDescription.setText(volumeInfo[position]);
 			if(position == 1) { //volume 5
 				holder.volumeDescriptionBold.setText(Globals.EndorseVolFive);
 			} else {
 				holder.volumeDescriptionBold.setVisibility(View.GONE);
 			}
 			return convertView;
 		}
 
 		class ViewHolder {
 			TextView volumeName;
 			TextView volumeDescription;
 			TextView volumeDescriptionBold;
 		}
 
 		public Filter getFilter() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return position;
 		}
 
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return volumeNames.length;
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 	}
 }

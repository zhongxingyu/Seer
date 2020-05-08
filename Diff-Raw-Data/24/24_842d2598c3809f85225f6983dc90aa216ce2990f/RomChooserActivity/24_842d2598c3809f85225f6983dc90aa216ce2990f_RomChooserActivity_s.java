 package com.rtg.makovm;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class RomChooserActivity extends ListActivity {
 	private final static String DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
 	private final static String STORAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
 	private final static String MAKO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Mako";
 	
 	private TextView mEmpty = null;
 	
 	private static class RomAdapter extends BaseAdapter
 	{
 		private static Comparator<File> sDateComparator = new Comparator<File>() {
 			
 			@Override
 			public int compare(File lhs, File rhs) {
 				return (rhs.lastModified()<lhs.lastModified())?1:-1;
 			}
 		};
 		
 		private static class ViewHolder
 		{
 			TextView name;
 			TextView size;
 			TextView date;
 		}
 		
 		private List<File> mRoms = new ArrayList<File>();
 		
 		@Override
 		public int getCount() {
 			return mRoms.size();
 		}
 
 		@Override
 		public File getItem(int pos) {
 			return mRoms.get(pos);
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder vh = null;
 			if(convertView==null)
 			{
 				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_rom, null);
 				vh = new ViewHolder();
 				vh.name = (TextView) convertView.findViewById(R.id.RomListItem_Name);
 				vh.size = (TextView) convertView.findViewById(R.id.RomListItem_Size);
 				vh.date = (TextView) convertView.findViewById(R.id.RomListItem_Date);
 				convertView.setTag(vh);
 			}
 			else
 			{
 				vh = (ViewHolder) convertView.getTag();
 			}
 			
 			File rom = getItem(position);
 			vh.name.setText(rom.getName());
 			vh.size.setText(String.format("%.2f Kbytes",rom.length()/1024.0f));
 			vh.date.setText(rom.lastModified()+"");
 			
 			return convertView;
 		}
 
 		public void addRoms(File[] values) {
 			for(int i = 0; i<values.length; i++)
 			{
 				mRoms.add(values[i]);
 			}
 			
 			// Order by date
 			Collections.sort(mRoms, sDateComparator);
 			
 			// Tell the ListView to update
 			notifyDataSetChanged();
 			
 		}	
 	}
 	
 	private RomAdapter mRomAdapter = new RomAdapter();
 	
 	private class RomFinderTask extends AsyncTask<String, File, Boolean>
 	{
 		@Override
 		protected void onProgressUpdate(File... values) {
 			super.onProgressUpdate(values);
 			mRomAdapter.addRoms(values);
 		}
 		
 		@Override
 		protected Boolean doInBackground(String... params) {
 			
 			FilenameFilter romFilter = new FilenameFilter() {
 				
 				@Override
 				public boolean accept(File dir, String filename) {
 					return filename.endsWith(".rom") || filename.endsWith(".ROM");
 				}
 			};
 			
 			boolean found = false;
 			for(int i = 0; i<params.length; i++)
 			{
 				File searchDir = new File(params[i]);
 				
 				File[] listFiles = searchDir.listFiles(romFilter);
 				if(listFiles.length>0)
 				{
 					found = true;
 				}
 				
 				publishProgress(listFiles);
 			}
 			return found;
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean found) {
 			if(!found)
 			{
 				mEmpty.setText("Please download or compile roms to play.");
 			}
 		}
 		
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.rom_chooser);
 		mEmpty = (TextView) findViewById(android.R.id.empty);
 		
 		// ListActivity takes care of everything else
 		setListAdapter(mRomAdapter);
 		// Because we are kind, we'll create the MAKO_DIR for the user
 		new File(MAKO_DIR).mkdir();
 		new RomFinderTask().execute(MAKO_DIR, STORAGE_DIR, DOWNLOAD_DIR);
 		
 		getListView().setOnItemClickListener(new OnItemClickListener()
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> listView, View item, int pos,
 					long id) {
				startActivity(new Intent(RomChooserActivity.this, MainActivity.class).putExtra(MainActivity.EXTRA_ROM_FILE, mRomAdapter.getItem(pos).getAbsolutePath()));
 				finish();
 			}
 			
 		});
 	}
 }

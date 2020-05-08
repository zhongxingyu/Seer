 package de.stas;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.stas.views.ColorButton;
 
 public class Directory extends BaseActivity implements OnItemClickListener, OnClickListener {
 	private File currentRoot;
 	private ListView list;
 	private ColorButton colorButton;
 	private Runnable savePath;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
         setContentView(R.layout.directory);
 

        if (customTitleSupported ) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.directory_title_bar);
            Button button = (Button)findViewById(R.id.directory_title_bar_button);
            button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
				}
			});
        }
         String path = "/";
         if (savedInstanceState != null) {
         	path = savedInstanceState.getString("currentRootPath");
         }
         currentRoot = new File(path);
         list = (ListView)findViewById(android.R.id.list);
         list.setOnItemClickListener(this);
         colorButton = (ColorButton)findViewById(R.id.header_ok_button);
         colorButton.setOnClickListener(this);
         
         infoDialog.setOkRunnable(new Runnable() {
         	@Override
         	public void run() {
     			Intent intent = new Intent();
     			intent.putExtra("path", currentRoot.getAbsolutePath());
 				setResult(RESULT_OK, intent);
 				finish();
 				
         	}
         });
         savePath = new Runnable() {
         	public void run() {
         		infoDialog.setTitle("Infomation");
     			infoDialog.setText("folder set");
     			showDialog(INFO_DIALOG);
         	}
         };
         confirmDialog.setOkRunnable(savePath);
        
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     }
     
 	@Override
 	public void onSaveInstanceState(Bundle b) {
 		b.putString("currentRootPath", currentRoot.getAbsolutePath());
 		super.onSaveInstanceState(b);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle b) {
 		super.onRestoreInstanceState(b);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		addFilesToListAdapter(currentRoot);
 	}
 	
 	private void addFilesToListAdapter(File root) {
 		boolean highlight = false;
 		File[] files = root.listFiles();
 		ArrayList<File> currentFiles = new ArrayList<File>();
 		if (root.getParent() != null) {
 			currentFiles.add(root);
 		}
 		if (files != null && files.length > 0) {
 			for (File file : files) {
 				currentFiles.add(file);
 				if (file.isFile())
 					highlight = true;
 			}
 		}
 		colorButton.highlight(highlight);
 		list.setAdapter(new MyDirectoryArrayAdapter(this, R.id.file_textView, currentFiles));
 	}
 	
 
 	
 	class MyDirectoryArrayAdapter extends ArrayAdapter<File> {
 		private Context context;
 		
 		public MyDirectoryArrayAdapter(Context context, int textViewResourceId,
 				List<File> objects) {
 			super(context, textViewResourceId, objects);
 			this.context = context;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			File selectedFile = getItem(position);
 			ViewHolder holder = null;
 			if (convertView == null) {
 				convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.browser_file_list, null);
 				holder = new ViewHolder(convertView.findViewById(R.id.file_textView));
 				convertView.setTag(holder);
 			}
 			holder = (ViewHolder) convertView.getTag();
 			if (selectedFile == currentRoot) {
 				holder.getTextView().setText("\t..");
 			} else {
 				holder.getTextView().setText("\t" + selectedFile.getName());
 			}
 			if (selectedFile == currentRoot) {
 				holder.getTextView().setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.updir), null, null, null);
 			} else {
 				if (selectedFile.isDirectory()) {
 					holder.getTextView().setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.folder), null, null, null);
 				} else {
 					if (selectedFile.getName().matches("^.*\\.(([jJ][pP][gG])|([pP][nN][gG]))$")) {
 						holder.getTextView().setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.jpg_icon), null, null, null);
 					} else {
 						holder.getTextView().setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.file_icon), null, null, null);
 					}
 				}
 			}
 			return convertView;
 		}
 		
 		private class ViewHolder {
 			private TextView tv;
 			
 			public ViewHolder(View filename) {
 				this.tv = (TextView)filename;
 			}
 			
 			public TextView getTextView() {
 				return tv;
 			}
 		}
 	}
 
 	
 	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
 		File selectedFile = (File)list.getAdapter().getItem(pos);
 		if (selectedFile.isDirectory()) {
 				if (selectedFile == currentRoot) {
 					currentRoot = selectedFile.getParentFile();
 				} else {
 					currentRoot = selectedFile;
 				}
 				addFilesToListAdapter(currentRoot);
 		} else {
 			Toast.makeText(this, "Right, this is a file", Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 
 	public void onClick(View arg0) {
 		switch(arg0.getId()) {
 		case R.id.header_ok_button:
 			if (colorButton.isHighlighted()) {
 				savePath.run();
 			} else {
 				confirmDialog.setTitle("No files found");
 				confirmDialog.setText("This folder contains no files.\n" +
 						"Use is it anyway?");
 				showDialog(CONFIRM_DIALOG);
 			}
 			break;
 		}
 	}
 }

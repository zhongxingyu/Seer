 package com.jpqr.listpad.adapters;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.jpqr.listpad.R;
 
 public class FileListAdapter extends ArrayAdapter<File> {
 	private ArrayList<File> mFiles;
 
 	public FileListAdapter(Context context, ArrayList<File> files) {
 		super(context, R.layout.file_list_item, files);
 		mFiles = files;
 	}
 
 	@Override
 	public View getView(final int position, View view, ViewGroup parent) {
 		if (view == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.file_list_item, null);
 		}
 		File file = mFiles.get(position);
 		ImageView itemIcon = (ImageView) view.findViewById(R.id.file_icon);
 		if (file.isDirectory()) {
			itemIcon.setImageResource(R.drawable.ic_menu_archive);
 		} else if (file.isFile()) {
			itemIcon.setImageResource(R.drawable.ic_menu_agenda);
 		}
 		TextView itemName = (TextView) view.findViewById(R.id.file_name);
 		itemName.setText(file.getName());
 
 		return view;
 	}
 }

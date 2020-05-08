 package com.iusenko.filechooser;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 /**
  * @author iusenko
  */
 public class FileAdapter extends BaseAdapter {
 
	ArrayList<File> files = new ArrayList<File>();
 	private Context context;
 	private Drawable folderDrawable;
 	private Drawable fileDrawable;
 
 	public FileAdapter(Context context) {
 		this.context = context;
 		Resources res = context.getResources();
 		folderDrawable = res.getDrawable(R.drawable.ic_folder);
		folderDrawable.setBounds(0, 0, 96, 96);
 		fileDrawable = res.getDrawable(R.drawable.ic_file);
		fileDrawable.setBounds(0, 0, 96, 96);
 	}
 
 	public void setListItems(ArrayList<File> files) {
 		this.files = files;
 	}
 
 	public int getCount() {
 		return files.size();
 	}
 
 	public void add(File file) {
 		files.add(file);
 	}
 
 	public void clear() {
 		files.clear();
 	}
 
 	public Object getItem(int position) {
 		return files.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			final LayoutInflater inflater = LayoutInflater.from(context);
 			convertView = inflater.inflate(R.layout.file, parent, false);
 			holder = new ViewHolder();
 			holder.file = (TextView) convertView.findViewById(R.id.file_text_view);
 			convertView.setTag(holder);
 		} else {
 			// Reduce, reuse, recycle!
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		final File file = files.get(position);
 		Drawable drawable = file.isDirectory() ? folderDrawable : fileDrawable;
 
 		holder.file.setText(file.getName());
 		holder.file.setCompoundDrawables(drawable, null, null, null);
 		return convertView;
 	}
 
 	static class ViewHolder {
 		TextView file;
 	}
 }

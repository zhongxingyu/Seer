 package com.example.ipcplayer.adapter;
 
 import com.example.ipcplayer.R;
 import com.example.ipcplayer.provider.MusicDB;
 import com.example.ipcplayer.utils.LogUtil;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AllSongListAdapter extends CursorAdapter{
 	private LayoutInflater mInflater;
 	private ViewHolder mHolder ;
 	private int mLayoutId;
 	private static String TAG = AllSongListAdapter.class.getSimpleName();
 	
 	public AllSongListAdapter(Context context, Cursor c, int resource) {
 		super(context, c, resource);
 		// TODO Auto-generated constructor stub
 		LogUtil.d(TAG + " create this class Object");
 		mInflater = LayoutInflater.from(context);
 		mHolder = new ViewHolder();
 		mLayoutId = resource;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " getView ");
 		return super.getView(position, convertView, parent);
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " bindView ");
 		mHolder = (ViewHolder) view.getTag();
 		String displayName = cursor.getColumnName(cursor.getColumnIndex(MusicDB.MusicInfoColumns.MUSICNAME));
 		String artstName = cursor.getColumnName(cursor.getColumnIndex(MusicDB.MusicInfoColumns.ARTIST));
 		mHolder.mText1.setText(displayName);
 		mHolder.mText2.setText(artstName);
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " newView ");
 		View v = mInflater.inflate(mLayoutId, null);
 		mHolder.mImage = (ImageView) v.findViewById(R.id.albumnimage);
 		mHolder.mText1 = (TextView) v.findViewById(R.id.songname);
 		mHolder.mText2 = (TextView) v.findViewById(R.id.artistname);
 		v.setTag(mHolder);
 		return v;
 	}
 
 	@Override
 	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " runQueryOnBackground");
 		String[] cols = new String[] {
 				MusicDB.MusicInfoColumns.MUSICNAME,
 				MusicDB.MusicInfoColumns.ARTIST
 		};
 		StringBuilder  where = new StringBuilder();
 		
		return 
		
 		return super.runQueryOnBackgroundThread(constraint);
 	}
 	
 }

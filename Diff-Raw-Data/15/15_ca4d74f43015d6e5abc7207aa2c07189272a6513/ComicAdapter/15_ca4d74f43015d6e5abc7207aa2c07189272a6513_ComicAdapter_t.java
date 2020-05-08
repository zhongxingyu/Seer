 package com.zns.comicdroid.adapter;
 
 import com.zns.comicdroid.R;
 import com.zns.comicdroid.util.ImageWorker;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class ComicAdapter extends SimpleCursorAdapter
 {
 	private final int mLayout;
 	private final LayoutInflater mLayoutInflater;
 	private final ImageWorker mImageWorker = new ImageWorker();
 	private final int mColorDefault;
 	private final int mColorIsBorrowed;
 	private final String mImagePath;
 	public boolean mRenderTitle = true;	
 
 	static class ComicHolder
 	{
 		TextView tvTitle;
 		TextView tvAuthor;
 		ImageView ivImage;
 		TextView tvIssue;
 		RatingBar rbComic;
 		RelativeLayout rlRow;
 	}
 
 	public ComicAdapter(Context context, String imagePath)
 	{
 		super(context, R.layout.list_comicrow, null, new String[] { "Title" }, null, 0);
 		mLayout = R.layout.list_comicrow;
 		mLayoutInflater = LayoutInflater.from(context);
 		Resources res = context.getResources();
 		mColorDefault = res.getColor(R.color.contentBg);
 		mColorIsBorrowed = res.getColor(R.color.listViewBorrowed);
 		mImagePath = imagePath;
 	}
 
 	public int getComicId(int position)
 	{
 		Cursor cursor = getCursor();
 		if (cursor.moveToPosition(position))
 			return cursor.getInt(0);
 		return 0;
 	}
 
 	public int[] getComicIds()
 	{
 		int[] ids = null;
 		if (getCursor() != null)
 		{
 			Cursor cursor = getCursor();
 			ids = new int[cursor.getCount()];
 			int i = 0;
 			cursor.moveToPosition(-1);
 			while(cursor.moveToNext()) {
 				ids[i] = cursor.getInt(0);
 				i++;
 			}
 		}
 		return ids;		
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Cursor cursor = getCursor();
 		if (cursor.moveToPosition(position)) 
 		{
 			ComicHolder holder;
 
 			if (convertView == null)
 			{
 				convertView = mLayoutInflater.inflate(mLayout, null);	    		
 				holder = new ComicHolder();
 				holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
 				holder.tvAuthor = (TextView)convertView.findViewById(R.id.tvAuthor);
 				holder.ivImage = (ImageView)convertView.findViewById(R.id.ivImage);
 				holder.tvIssue = (TextView)convertView.findViewById(R.id.tvIssue);
 				holder.rlRow = (RelativeLayout)convertView.findViewById(R.id.rlRow);
 				holder.rbComic = (RatingBar)convertView.findViewById(R.id.rbComicList);
 				convertView.setTag(holder);
 			}
 			else
 			{
 				holder = (ComicHolder)convertView.getTag();
 			}
 
 			String title = cursor.getString(1);
 			String subTitle = cursor.getString(2);
 			if (subTitle == null)
 				subTitle = "";
 			String author = cursor.getString(3);
 			String image = cursor.getString(4);
 			int issue = cursor.getInt(5);
 			boolean isBorrowed = cursor.getInt(6) == 1;
			//boolean isRead = cursor.getInt(7) == 1;
 			int rating = cursor.getInt(8);
 
 			String strTitle = title + (subTitle.length() > 0 ? " - " + subTitle : "");
 			if (!mRenderTitle)
 				strTitle = subTitle.length() > 0 ? subTitle : title;
 
 				holder.tvTitle.setText(strTitle);
 				holder.tvAuthor.setText(author);
 				if (issue > 0) {
 					holder.tvIssue.setText("Vol. " + Integer.toString(issue));
 				}
 				else {
 					holder.tvIssue.setText("");
 				}
 
 				if (image != null && !image.equals("")) {
 					mImageWorker.load(mImagePath.concat(image), holder.ivImage);
 					holder.ivImage.setVisibility(View.VISIBLE);
 				}
 				else {
 					holder.ivImage.setVisibility(View.GONE);
 				}
 
 				if (isBorrowed) {
 					holder.rlRow.setBackgroundColor(mColorIsBorrowed);
 				}
 				else {
 					holder.rlRow.setBackgroundColor(mColorDefault);
 				}
 
 				holder.rbComic.setVisibility(rating > 0 ? View.VISIBLE : View.GONE);
 				holder.rbComic.setRating(rating);			
 		}
 
 		return convertView;	    
 	}	
 }

 package com.zns.comicdroid.adapter;
 
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
 
 import com.zns.comicdroid.R;
 import com.zns.comicdroid.data.Aggregate;
 import com.zns.comicdroid.util.ImageWorker;
 
 public class AggregateAdapter extends SimpleCursorAdapter {
 	private final int mLayout;
 	private final LayoutInflater mLayoutInflater;
 	private final ImageWorker mImageWorker = new ImageWorker();
 	private final int mColorDefault;
 	private final int mColorIsBorrowed;
 	private final String mImagePath;
 
 	static class AggregateHolder
 	{
 		TextView tvTitle;
 		TextView tvAuthor;
 		ImageView ivImage;
 		TextView tvCount;
 		ImageView ivGroupMark;
 		ImageView ivGroupWatched;
 		ImageView ivGroupCompleted;
 		ImageView ivGroupFinished;
 		RatingBar rbComic;
 		RelativeLayout rlRow;
 	}
 
 	public AggregateAdapter(Context context, String imagePath)
 	{
 		super(context, R.layout.list_comicrow, null, new String[] { "Title" }, null, 0);
 		mLayout = R.layout.list_comicrow;
 		mLayoutInflater = LayoutInflater.from(context);		
 		Resources res = context.getResources();
 		mColorDefault = res.getColor(R.color.contentBg);
 		mColorIsBorrowed = res.getColor(R.color.listViewBorrowed);	
 		mImagePath = imagePath;
 	}
 
 	public Aggregate getAggregate(int position) {
 		Cursor cursor = getCursor();
 		if (cursor.moveToPosition(position)) {
 			return new Aggregate(cursor.getInt(0), cursor.getString(1), cursor.getInt(5));
 		}
 		return null;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Cursor cursor = getCursor();
 		if (cursor.moveToPosition(position)) 
 		{
 			AggregateHolder holder;
 
 			if (convertView == null)
 			{
 				convertView = mLayoutInflater.inflate(mLayout, null);	    		
 				holder = new AggregateHolder();
 				holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
 				holder.tvAuthor = (TextView)convertView.findViewById(R.id.tvAuthor);
 				holder.ivImage = (ImageView)convertView.findViewById(R.id.ivImage);
 				holder.tvCount = (TextView)convertView.findViewById(R.id.tvCount);
 				holder.ivGroupMark = (ImageView)convertView.findViewById(R.id.ivGroupMark);
 				holder.rlRow = (RelativeLayout)convertView.findViewById(R.id.rlRow);
 				holder.ivGroupCompleted = (ImageView)convertView.findViewById(R.id.ivGroupCompleted);
 				holder.ivGroupFinished = (ImageView)convertView.findViewById(R.id.ivGroupFinished);
 				holder.ivGroupWatched = (ImageView)convertView.findViewById(R.id.ivGroupWatched);
 				holder.rbComic = (RatingBar)convertView.findViewById(R.id.rbComicList);
 				convertView.setTag(holder);
 			}
 			else
 			{
 				holder = (AggregateHolder)convertView.getTag();
 			}
 
 			String title = cursor.getString(1);
 			String subTitle = cursor.getString(2);
 			String author = cursor.getString(3);
 			String image = cursor.getString(4);
 			int type = cursor.getInt(5);
 			int count = cursor.getInt(6);
 			boolean isBorrowed = cursor.getInt(7) == 1;
 			int totalCount = cursor.getInt(8);
 			boolean isFinished = cursor.getInt(9) == 1;
 			boolean isComplete = cursor.getInt(10) == 1;
 			boolean isWatched = cursor.getInt(11) == 1;
			//boolean isRead = cursor.getInt(12) == 1;
 			int rating = cursor.getInt(13);
 
 			holder.tvTitle.setText(title + (subTitle != null && !subTitle.equals("") ? " - " + subTitle : ""));
 			holder.tvAuthor.setText(author);
 			if (type == 2) {
 				holder.ivGroupMark.setVisibility(View.VISIBLE);
 				holder.tvCount.setText("(" + count + (totalCount > 0 ? "/" + totalCount : "") + ")");
 				holder.ivGroupFinished.setVisibility(isFinished ? View.VISIBLE : View.GONE);
 				holder.ivGroupCompleted.setVisibility(isComplete ? View.VISIBLE : View.GONE);
 				holder.ivGroupWatched.setVisibility(isWatched ? View.VISIBLE : View.GONE);
 				holder.rbComic.setVisibility(View.GONE);
 			}
 			else
 			{
 				holder.ivGroupMark.setVisibility(View.GONE);
 				holder.ivGroupCompleted.setVisibility(View.GONE);
 				holder.ivGroupFinished.setVisibility(View.GONE);
 				holder.ivGroupWatched.setVisibility(View.GONE);
 				holder.tvCount.setText("");
 				holder.rbComic.setVisibility(rating > 0 ? View.VISIBLE : View.GONE);
 				holder.rbComic.setRating(rating);
 			}
 			if (image != null && !image.equals(""))
 			{
 				mImageWorker.load(mImagePath.concat(image), holder.ivImage);
 				holder.ivImage.setVisibility(View.VISIBLE);
 			}
 			else
 			{
 				holder.ivImage.setVisibility(View.GONE);
 			}
 
 			if (isBorrowed) {
 				holder.rlRow.setBackgroundColor(mColorIsBorrowed);
 			}
 			else {
 				holder.rlRow.setBackgroundColor(mColorDefault);
 			}			
 		}
 
 		return convertView;	    
 	}		
 }

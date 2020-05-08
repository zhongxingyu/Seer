 package ru.rutube.RutubeFeed.data;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.view.ViewGroup;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeFeed.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 11.05.13
  * Time: 11:53
  * To change this template use File | Settings | File Templates.
  */
 public class EditorsCursorAdapter extends FeedCursorAdapter {
     private static final String LOG_TAG = EditorsCursorAdapter.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
 
     public EditorsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
         super(context, layout, c, from, to, flags);
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         View view = super.newView(context, cursor, parent);
         ViewHolder holder = (ViewHolder)view.getTag();
         holder.avatar.setDefaultImageResId(R.drawable.editors_av);
         return view;
     }
 
     @Override
     protected void bindAvatar(Cursor cursor, final FeedCursorAdapter.ViewHolder holder) {
 
     }
 
     @Override
     protected void bindAuthor(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
        holder.author.setText(R.string.editors_choice);
     }
 }
 

 package com.ntu.iddc.exnote;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.taptwo.android.widget.TitleProvider;
 
 
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 
 public class AsyncAdapter extends BaseAdapter implements TitleProvider {
 
 	private LayoutInflater mInflater;
 	
 	private static final DateFormat dfTitle = new SimpleDateFormat("E, dd MMM");
 	
 	private static final int daysDepth = 10;
 	private static final int daysSize = daysDepth * 2 + 1;
 	
 	private static Date[] dates = new Date[ daysSize ];
 	private static String[] content = new String[ daysSize ];
 	public Context context;
 	private SQLite dbHelper;
 	private String authorId="0";
 	private String authorName="0";
 	private String diaryId="0";
 	
 	private class ViewHolder {
 		ProgressBar mProgressBar;
 		View mContent;
 		TextView mDate;
 		Button mNewDiary;
 		ListView list;
 	}
 	
 	
 	public AsyncAdapter(Context context, String diaryId, String authorId, String authorName) {
 		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		prepareDates();
 		this.context=context;
 		this.diaryId=diaryId;
 		this.authorId=authorId;
 		this.authorName=authorName;
 
 	}
 	
 	@Override
 	public String getItem(int position) {
 		return content[position];
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position; 
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		return drawView(position, convertView);
 	}
 
 	private View drawView(int position, View view) {
 		ViewHolder holder = null;
 		
 		if(view == null) {
 			view = mInflater.inflate(R.layout.day_view, null);
 			
 			holder = new ViewHolder();
 
 			holder.mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
 			holder.mDate = (TextView) view.findViewById(R.id.date);
 			holder.mContent = (View) view.findViewById(R.id.content);
 			holder.mNewDiary= (Button) view.findViewById(R.id.NewDiary);
 			holder.mNewDiary.setOnClickListener(newDiary);
 			holder.mNewDiary.setVisibility(View.GONE);
 			holder.list = (ListView)view.findViewById(R.id.udiniList);
 
 			view.setTag(holder);
 		} else {
 			holder = (ViewHolder) view.getTag();
 		}		
 
 ///////////////////////
 		// Creating the list adapter and populating the list
         ArrayAdapter<String> listAdapter = new CustomListAdapter(context, R.layout.list_item);
 //        for (int i=0; i<20;i++)
 //            listAdapter.add("udini"+i);
         
         
         //////
     	final String o = getItem(position);
 		if (o != null) {
 			holder.mProgressBar.setVisibility(View.GONE);
 			holder.mDate.setText(o);
 			dbHelper = new SQLite(context);
 			Cursor cursor = dbHelper.getDiaryByDate(o, diaryId);	//取得SQLite類別的回傳值:Cursor物件
 			int rows_num = cursor.getCount();	//取得資料表列數
 	 
 			if(rows_num != 0) {
				holder.mDate.setVisibility(View.GONE);
//				holder.mContent.setVisibility(View.GONE);

 				String title = "";
 				String article = "";
 				cursor.moveToFirst();			//將指標移至第一筆資料
 				for(int i=0; i<rows_num; i++) {
 					title  = cursor.getString(6);
 					article  = cursor.getString(7);
 					JSONObject json = new JSONObject();
 					try {
 						json.put("title", title);
 						json.put("article", article);
 					} catch (JSONException e) {	}
 					
 					listAdapter.add(json.toString());
 					cursor.moveToNext();		//將指標移至下一筆資料
 				}
 				holder.mDate.setText(title);
 			}
 			cursor.close();		//關閉Cursor
 			dbHelper.close();	//關閉資料庫，釋放記憶體
         //////
         
         
         holder.list.setAdapter(listAdapter);
 //
 //        // Creating an item click listener, to open/close our toolbar for each item
         holder.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 //
                 View toolbar = view.findViewById(R.id.toolbar);
 //
 //                // Creating the expand animation for the item
                 ExpandAnimation expandAni = new ExpandAnimation(toolbar, 500);
 //
 //                // Start the animation on the toolbar
                 toolbar.startAnimation(expandAni); 
             }
         });
 		
 //		final String o = getItem(position);
 //		if (o != null) {
 //			holder.mProgressBar.setVisibility(View.GONE);
 //			holder.mDate.setText(o);
 //			dbHelper = new SQLite(context);
 //			Cursor cursor = dbHelper.getDiaryByDate(o, "1");	//取得SQLite類別的回傳值:Cursor物件
 //			int rows_num = cursor.getCount();	//取得資料表列數
 //	 
 //			if(rows_num != 0) {
 //				StringBuilder article = new StringBuilder("");
 //				cursor.moveToFirst();			//將指標移至第一筆資料
 //				for(int i=0; i<rows_num; i++) {
 //					int id = cursor.getInt(0);	//取得第0欄的資料，根據欄位type使用適當語法
 //					article  = article.append( cursor.getString(6)+"\n" );
 //					cursor.moveToNext();		//將指標移至下一筆資料
 //				}
 //				holder.mDate.setText(article);
 //			}
 //			cursor.close();		//關閉Cursor
 //			dbHelper.close();	//關閉資料庫，釋放記憶體
 			
 			
 			holder.mContent.setVisibility(View.VISIBLE);
 			if(o.equals(getItem(daysDepth)))
 				holder.mNewDiary.setVisibility(View.VISIBLE);
 			else
 				holder.mNewDiary.setVisibility(View.GONE);
 			
 				
 			
 		}
 		else {
 			new LoadContentTask().execute(position, view);
 			holder.mContent.setVisibility(View.GONE);
 			holder.mProgressBar.setVisibility(View.VISIBLE);
 			holder.mNewDiary.setVisibility(View.GONE);
 
 		}
 	
 		return view;
 	}
 
 	@Override
 	public String getTitle(int position) {
 		return dfTitle.format( dates[position] );
 	}
 
 	@Override
 	public int getCount() {
 		return dates.length;
 	}
 
 	public int getTodayId() {
 		return daysDepth;
 	}
 
 	public Date getTodayDate() {
 		return dates[daysDepth];
 	}
 	
 	private OnClickListener newDiary = new OnClickListener()
     {
         public void onClick(View v)
         {
        	 Intent intent = new Intent();
        	 intent.setClass(context, DiaryEditorActivity.class);
        	 Bundle bundle = new Bundle();
        	 bundle.putString("date", getItem(daysDepth));
        	 bundle.putString("authorId", authorId);
        	 bundle.putString("diaryId", diaryId);
        	 bundle.putString("authorName", authorName);
 
        	 intent.putExtras(bundle);
        	 context.startActivity(intent);
     }
     };
 	
 	/**
 	 * Prepare dates for navigation, to past and to future
 	 */
 	private void prepareDates() {
 		Date today = new Date();
 		Calendar calPast = Calendar.getInstance();
 		Calendar calFuture = Calendar.getInstance();
 
 		calPast.setTime(today);
 		calFuture.setTime(today);
//		calPast.add( Calendar.DATE, -10 );
//		calFuture.add( Calendar.DATE, -10 );
 		dates[ daysDepth ] = calPast.getTime();
 		for (int i = 1; i <= daysDepth; i++) {
 			calPast.add( Calendar.DATE, -1 );
 			dates[ daysDepth - i ] = calPast.getTime();
 
 			calFuture.add( Calendar.DATE, 1 );
 			dates[ daysDepth + i ] = calFuture.getTime();
 		}
 	}
 	
 	
 	private class LoadContentTask extends AsyncTask<Object, Object, Object> {
 		
 		private Integer position;
 		private View view;
 		
 		@Override
 		protected Object doInBackground(Object... arg) {
 			position = (Integer) arg[0];
 			view = (View) arg[1];
 
 // long-term task is here 			
 			try {
 				Thread.sleep(1000); // do nothing for 3000 miliseconds (3 second)
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			return getTitle(position);
 		}
 
 		protected void onPostExecute(Object result) {
 // process result    	 
 			content[position] = (String) result;
 			
 	    	drawView(position, view);
 
 	    	view.postInvalidate();
 	     }
 
 	}	
 
 }

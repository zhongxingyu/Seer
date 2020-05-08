 package com.example.archery.statistics;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Vector;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.*;
 import android.widget.*;
 import com.example.archery.CShot;
 import com.example.archery.R;
 import com.example.archery.archeryView.CDistance;
 import com.example.archery.database.CMySQLiteOpenHelper;
 
 public class StatisticsActivity extends Activity    {
 
 	private ExpandListAdapter adapter;
 	private ExpandableListView expandableListView;
 
     //TODO:Поработать над статистикой:подсчёт очков, графики т.д.
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_display);
         adapter = new ExpandListAdapter(this);
         expandableListView = (ExpandableListView) findViewById(R.id.expandableListView1);
         expandableListView.setAdapter(adapter);
         registerForContextMenu(expandableListView);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_display, menu);
         return true;
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId())
     	{
     	case R.id.clear:
     	{
 			adapter.Clean();
 	    	break;
     	}
     	case R.id.expand:
     	{
     		for (int i=0;i<adapter.getGroupCount();i++)
     			expandableListView.expandGroup(i);
     		break;
     	}
     	case R.id.hide:
     	{
     		for (int i=0;i<adapter.getGroupCount();i++)
     			expandableListView.collapseGroup(i);
     		break;
     	}
     	}
     	return true;
     }
     @Override
     public void onCreateContextMenu(ContextMenu contextMenu,View view,ContextMenu.ContextMenuInfo contextMenuInfo)   {
         super.onCreateContextMenu(contextMenu,view,contextMenuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.context_menu,contextMenu);
     }
     @Override
     public boolean onContextItemSelected(MenuItem menuItem) {
         if (menuItem.getItemId()==R.id.delete_record)
         {
             ExpandableListView.ExpandableListContextMenuInfo info =
                     (ExpandableListView.ExpandableListContextMenuInfo)menuItem.getMenuInfo();
             adapter.delete_record(ExpandableListView.getPackedPositionGroup(info.packedPosition));
         }
         return true;
     }
 
     private class ExpandListAdapter extends BaseExpandableListAdapter   {
 	Context context;
 	Vector<CDistance> distances;
     CMySQLiteOpenHelper helper;
     int sum;
 
     public ExpandListAdapter(Context context)  {
         this.context=context;
         helper = new CMySQLiteOpenHelper(context);
         distances = helper.getAllDistances();
     }
 
     public void delete_record(int n)
     {
         helper.deleteDistance(distances.get(n));
         distances.remove(n);
         notifyDataSetChanged();
     }
 
 	public void Clean()
 	{
         helper.clean();
         distances = helper.getAllDistances();
         notifyDataSetChanged();
     }
 	
 	public Object getChild(int groupPosition, int childPosition) {
         CShot shots[][] = new CShot[2][];
         shots[0] = distances.get(groupPosition).series.get(childPosition*2).toArray(new CShot[0]);
         if (childPosition*2+1==distances.get(groupPosition).series.size())
             shots[1] = null;
         else
             shots[1] = distances.get(groupPosition).series.get(childPosition*2+1).toArray(new CShot[1]);
         return shots;
 	}
 
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 	public View getChildView(int groupPosition, int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) {
         LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = infalInflater.inflate(R.layout.expandlistchild,null);
         for (int i =0; i<(distances.get(groupPosition).series.size()+1)/2;i++)
         {
             LinearLayout view = (LinearLayout) infalInflater.inflate(R.layout.statistics_block, null);
            int sum = 0;
             createStatisticsBlockView(view,(CShot[][]) getChild(groupPosition,i));
             ((LinearLayout)convertView).addView(view);
         }
         CBorderedTextView view = new CBorderedTextView(context);
         view.setText(String.valueOf(distances.get(groupPosition).isFinished));
         ((LinearLayout)convertView).addView(view);
         return convertView;
 	}
 
 	public int getChildrenCount(int groupPosition) {
 		//return (distances.get(groupPosition).series.size()+1)/2;
 	    return 1;
     }
 
 	public Object getGroup(int groupPosition) {
 		return distances.get(groupPosition);
 	}
 
 	public int getGroupCount() {
 		return distances.size();
 	}
 
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	public View getGroupView(int groupPosition, boolean isExpanded,
 			View convertView, ViewGroup parent) {
 		if (convertView == null){
 			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = infalInflater.inflate(R.layout.expandlistgroup, null);
 		}
 		TextView textview = (TextView) convertView.findViewById(R.id.group);
         Calendar calendar = distances.get(groupPosition).timemark;
 		textview.setText(calendar.get(Calendar.DATE)+":"+
                          calendar.get(Calendar.MONTH)+":"+
                          calendar.get(Calendar.YEAR)+" "+
                          calendar.get(Calendar.HOUR_OF_DAY)+":"+
                          calendar.get(Calendar.MINUTE));
 		return convertView;
 	}
 
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return false;
 	}
 
         private void createStatisticsBlockView(LinearLayout layout, CShot[][] series)  {
             int first_series_sum = 0;
             int second_series_sum = 0;
             for (CShot shot : series[0])
                 if (shot!=null)
                     first_series_sum+=shot.getPoints();
             if (series[1]!=null)
                 for (CShot shot : series[1])
                     if (shot!=null)
                         second_series_sum+=shot.getPoints();
             CBorderedTextView tv = (CBorderedTextView)layout.findViewById(R.id.first_series);
             tv.setText(Arrays.deepToString(series[0]));
             if (series[1]!=null)
                 {
                 tv = (CBorderedTextView)layout.findViewById(R.id.second_series);
                 tv.setText(Arrays.deepToString(series[1]));
                 tv = (CBorderedTextView)layout.findViewById(R.id.second_series_sum);
                 tv.setText(Integer.toString(second_series_sum));
                 }
             tv = (CBorderedTextView)layout.findViewById(R.id.first_series_sum);
             tv.setText(Integer.toString(first_series_sum));
             tv = (CBorderedTextView)layout.findViewById(R.id.two_series);
             tv.setText(Integer.toString((first_series_sum+second_series_sum)));
             tv = (CBorderedTextView)layout.findViewById(R.id.all_series);
             sum+=first_series_sum+second_series_sum;
             tv.setText(Integer.toString(sum));
         }
     }
 
 }

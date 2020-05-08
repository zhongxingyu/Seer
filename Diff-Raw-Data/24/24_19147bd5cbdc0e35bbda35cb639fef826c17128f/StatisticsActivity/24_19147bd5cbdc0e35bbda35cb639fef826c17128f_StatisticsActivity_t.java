 package com.example.archery.statistics;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Vector;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.*;
 import android.widget.*;
 import com.example.archery.CShot;
 import com.example.archery.R;
 import com.example.archery.archeryView.CDistance;
 
 public class StatisticsActivity extends Activity    {
 
 	private ExpandListAdapter adapter;
 	private ExpandableListView expandableListView;
 
     //TODO:Поработать над статистикой:подсчёт очков, графики т.д.
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_display);
         Vector<CDistance> distances = new Vector<CDistance>();
         try
         {
         ObjectInputStream ois = new ObjectInputStream(this.openFileInput("Archery"));
         distances = (Vector<CDistance>) ois.readObject();
         ois.close();
         }
         catch (Exception e)
         {
             Toast toast = Toast.makeText(this, e.getMessage(), 3000);
             toast.show();
         }
 
         adapter = new ExpandListAdapter(this, distances);
         expandableListView = (ExpandableListView) findViewById(R.id.expandableListView1);
         expandableListView.setAdapter(adapter);
         registerForContextMenu(expandableListView);
     }
     @Override
     public void onPause()    {
         super.onPause();
         adapter.saveDistances();
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
     		this.deleteFile("Archery");
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
 
     public void delete_record(int n)
     {
         distances.remove(n);
         notifyDataSetChanged();
     }
 
 	public void Clean()
 	{
 		if (distances.lastElement().isFinished)
             distances.clear();
         else
         {
             CDistance buf = distances.lastElement();
             distances.clear();
             distances.add(buf);
         }
         notifyDataSetChanged();
 	}
 	
 	public ExpandListAdapter(Context context, Vector<CDistance> distances)
 	{
 		this.context=context;
 		this.distances=distances;
 
 	}
 	
 	public Object getChild(int groupPosition, int childPosition) {
         CShot shots [][] = new CShot[2][];
         shots[0] = distances.get(groupPosition).finishedSeries.get(childPosition*2);
         if (childPosition*2+1==distances.get(groupPosition).finishedSeries.size())
             shots[1] = null;
         else
             shots[1] = distances.get(groupPosition).finishedSeries.get(childPosition*2+1);
         return shots;
 	}
 
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 	public View getChildView(int groupPosition, int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) {
         LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = infalInflater.inflate(R.layout.statistics_block, null);
        int sum = 0;
        for (int i=0;i<childPosition;i++)
            sum+=getChildSum(groupPosition,i);
        createStatisticsBlockView((LinearLayout) convertView,(CShot[][]) getChild(groupPosition,childPosition),sum);
         return convertView;
 	}
 
 	public int getChildrenCount(int groupPosition) {
 		return (distances.get(groupPosition).finishedSeries.size()+1)/2;
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
 
     public void saveDistances()
     {
         try
         {
         ObjectOutputStream oos = new ObjectOutputStream(openFileOutput("Archery",MODE_PRIVATE));
         oos.writeObject(distances);
         oos.close();
         }
         catch (IOException e)
         {
             Toast toast = Toast.makeText(context, e.getMessage(), 3000);
             toast.show();
         }
     }
 
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return false;
 	}
 
    private int getChildSum(int groupPosition,int childPosition)
    {
        int sum = 0;
        CShot series[][] = (CShot[][]) getChild(groupPosition,childPosition);
        for (CShot shot : series[0])
            sum+=shot.getPoints();
        if (series[1]!=null)
            for (CShot shot : series[1])
                sum+=shot.getPoints();
        return sum;
    }
        private void createStatisticsBlockView(LinearLayout layout, CShot[][] series, int sum)  {
             int first_series_sum = 0;
             int second_series_sum = 0;
             for (CShot shot : series[0])
                 first_series_sum+=shot.getPoints();
             if (series[1]!=null)
                 for (CShot shot : series[1])
                     second_series_sum+=shot.getPoints();
             BorderedTextView tv = (BorderedTextView)layout.findViewById(R.id.first_series);
             tv.setText(Arrays.deepToString(series[0]));
             if (series[1]!=null)
                 {
                 tv = (BorderedTextView)layout.findViewById(R.id.second_series);
                 tv.setText(Arrays.deepToString(series[1]));
                 tv = (BorderedTextView)layout.findViewById(R.id.second_series_sum);
                 tv.setText(Integer.toString(second_series_sum));
                 }
             tv = (BorderedTextView)layout.findViewById(R.id.first_series_sum);
             tv.setText(Integer.toString(first_series_sum));
             tv = (BorderedTextView)layout.findViewById(R.id.two_series);
             tv.setText(Integer.toString((first_series_sum+second_series_sum)));
             tv = (BorderedTextView)layout.findViewById(R.id.all_series);
             sum+=first_series_sum+second_series_sum;
             tv.setText(Integer.toString(sum));
         }
     }
 
 }

 package com.example.BuggyTableExample;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.inqbarna.tablefixheaders.TableFixHeaders;
 import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
 
 public class MainActivity extends Activity{
     ReportData mData;
     MyAdapter mAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.fragment_benefit_analysis);
         mData = new ReportData();
 
         TableFixHeaders tableFixHeaders = (TableFixHeaders) findViewById(R.id.table);
         mAdapter = new MyAdapter(this);
         tableFixHeaders.setAdapter(mAdapter);
     }
 
     public class MyAdapter extends BaseTableAdapter {
 
         private final float density;
 
         public MyAdapter(Context context) {
             density = context.getResources().getDisplayMetrics().density;
         }
 
         @Override
         public int getRowCount() {
             // Row number without fixed part
             return mData.rowCount;
         }
 
         @Override
         public int getColumnCount() {
             // Column number without fixed part
             return mData.displayTable.size() - 2;
         }
 
         @Override
         public View getView(int row, int column, View convertView, ViewGroup parent) {
 
             final View view;
             switch (getItemViewType(row, column)) {
                 case 0:
                     view = getFirstHeader(row, column, convertView, parent);
                     break;
                 case 1:
                     view = getHeader(row, column, convertView, parent);
                     break;
                 case 2:
                     view = getFirstBody(row, column, convertView, parent);
                     break;
                 case 3:
                     view = getBody(row, column, convertView, parent);
                     break;
                 default:
                     throw new RuntimeException("wtf?");
             }
             return view;
         }
 
         @Override
         public int getWidth(int column) {
             return Math.round(mData.displayTable.get(column + 1).width * density);
         }
 
         @Override
         public int getHeight(int row) {
             final int height;
             if (row == -1) {
                 height = 90;
             } else {
                 height = 55;
             }
             return Math.round(height * density);
         }
 
         @Override
         public int getItemViewType(int row, int column) {
             final int itemViewType;
             if (row == -1 && column == -1) {
                 itemViewType = 0;
             } else if (row == -1) {
                 itemViewType = 1;
             } else if (column == -1) {
                 itemViewType = 2;
             } else {
                 itemViewType = 3;
             }
             return itemViewType;
         }
 
         @Override
         public int getViewTypeCount() {
             return 4;
         }
 
         private View getFirstHeader(int row, int column, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = getLayoutInflater().inflate(R.layout.item_table_header_first, parent, false);
             }
 
             ((TextView) convertView.findViewById(android.R.id.text1)).setText(mData.displayTable.get(0).header);
             ((TextView) convertView.findViewById(android.R.id.text2)).setText(mData.displayTable.get(1).header);
             return convertView;
         }
 
         private View getHeader(int row, int column, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = getLayoutInflater().inflate(R.layout.item_table_header, parent, false);
             }
             // Start from the third column
             ((TextView) convertView.findViewById(android.R.id.text1)).setText(mData.displayTable.get(column + 2).header);
             return convertView;
         }
 
         private View getFirstBody(int row, int column, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = getLayoutInflater().inflate(R.layout.item_table_first, parent, false);
             }
             convertView.setBackgroundResource((row + 1) % 5 == 0 ? R.drawable.bg_table_highlight : R.drawable.bg_table_normal);
             ((TextView) convertView.findViewById(android.R.id.text1))
                     .setText(mData.displayTable.get(0).textData.get(row));
             ((TextView) convertView.findViewById(android.R.id.text2))
                     .setText(mData.displayTable.get(1).textData.get(row));
 
             return convertView;
         }
 
         private View getBody(int row, int column, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = getLayoutInflater().inflate(R.layout.item_table, parent, false);
             }
             convertView.setBackgroundResource((row + 1) % 5 == 0 ? R.drawable.bg_table_highlight : R.drawable.bg_table_normal);
             // Start from the third column
            String value = mData.displayTable.get(column + 2).textData.get(row);
            final TextView t = ((TextView) convertView.findViewById(android.R.id.text1));
            t.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					System.out.println(((TextView)v).getText());
				}
			});
            t.setText(value);
             return convertView;
         }
     }
 }

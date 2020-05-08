 package ocept.gogo;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import ocept.gogo.db.Constants;
 import ocept.gogo.db.goDB;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GoList extends ListActivity {
 	goDB dba;
 	public static goAdapter myAdapter;
 	public String title;
 	public String content;
 	public String recorddate;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	dba = new goDB(this);
     	dba.open();
     	setContentView(R.layout.activity_go_list);
         super.onCreate(savedInstanceState);
         
         myAdapter = new goAdapter(this);
         this.setListAdapter(myAdapter);
         
         //connect up buttons
         Button newGoButton = (Button) findViewById(R.id.newGoButton);
         newGoButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(getBaseContext(), NewGo.class); //TODO chekc 1st parameter
 				startActivity(intent);
 			}
 		});
     }
     
     @Override
     protected void onResume(){
     	//myAdapter.notifyDataSetChanged();
     	super.onResume();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_go_list, menu);
         return true;
     }
     public class goAdapter extends BaseAdapter {
     	private LayoutInflater mInflater;
     	private ArrayList<Go> goArray;
     	public goAdapter(Context context) {
 	    	mInflater = LayoutInflater.from(context);
 	    	goArray = new ArrayList<Go>();
 	    	getdata();
     	}
     	public void getdata(){ //fetch data from DB
 	    	Cursor c = dba.getGos();
 	    	startManagingCursor(c);
 	    	if(c.moveToFirst()){
 		    	do{
 		    		try{
 				    	String title = c.getString(c.getColumnIndex(Constants.TITLE_NAME));
 				    	String desc = c.getString(c.getColumnIndex(Constants.CONTENT_NAME));
 				    	Long lastChecked = c.getLong(c.getColumnIndex(Constants.LAST_CHECKED_NAME));
 				    	String id = c.getString(c.getColumnIndex(Constants.KEY_ID));
 						Go temp = new Go(title,desc, lastChecked, id);
 						goArray.add(temp);
 		    		}
 		    		catch(java.lang.RuntimeException ex){
 		    			Log.e(ACTIVITY_SERVICE, "Failed to read go from db", ex);
 		    		}
 					} while(c.moveToNext());
 				}
 			}
     	@Override
     	public int getCount() {return goArray.size();} //called by listview
     	public Go getItem(int i) {return goArray.get(i);} //called to get 1 list item
     	public long getItemId(int i) {return i;}
     	public View getView(int position, View arg1, ViewGroup arg2) { //called by listview to get view for 1 list item
     		final int pos = position;
 	    	final ViewHolder holder;
 	    	View v = arg1;
 	    	if ((v == null) || (v.getTag() == null)) {
 		    	v = mInflater.inflate(R.layout.go_row, null);
 		    	holder = new ViewHolder();
 		    	holder.mTitle = (TextView)v.findViewById(R.id.name);
 		    	holder.mDesc = (TextView)v.findViewById(R.id.descText);
 		    	holder.mDone = (CheckBox)v.findViewById(R.id.checkDone);
 		    	v.setTag(holder);
 	    	} else {
 	    		holder = (ViewHolder) v.getTag();
 	    	}
 	    	holder.mGo = getItem(position);
 	    	holder.mTitle.setText(holder.mGo.Name);
 	    	holder.mDesc.setText(holder.mGo.Desc);
	    	Date Today = new Date();
	    	if(new Date(holder.mGo.LastChecked).after(Today)){
 	    		holder.mDone.setChecked(true);
 	    	}
 	    	else{
 	    		holder.mDone.setChecked(false);
 	    	}
 	    	v.setTag(holder);
 	    	
 	    	//bind chekcbox clicks
 	    	holder.mDone.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 	    		@Override
 	    		public void onCheckedChanged(CompoundButton b, boolean isChecked){
 	    			Toast.makeText(getBaseContext(), "Checked "+pos , Toast.LENGTH_SHORT).show();
 	    			dba.checkGo(holder.mGo.KeyId, isChecked);
 	    		}
 	    		
 	    	});
 	    	
 	    	return v;
     	}
     	public class ViewHolder {
     		Go mGo;
     		TextView mTitle;
     		TextView mDesc;
     		CheckBox mDone;
 		}
     }
 }
 
 

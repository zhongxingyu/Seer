 // <title> This is display activity. This activity is followed by calendar activity</title>
 
 // <author> aat </author>
 
 // <revision>
// 20120814 : aat
// Issue #2 :  database.
// fixed addition of blanked spaces in listview
// 20120814 : aat
// Issue #2 :  database.
// fixed addition of blanked spaces in listview
 // 08-05-2012 : aat
 // added formatting to file
 // Issue #2: 20120810 at
 // DS databasehelper activity creation
 // Except deletion, added every functionality mentioned in DS
 // 20120813 : aat
 // Issue #2 : Fully functional database with exception of (deletion).
 // Also awkward gridview is replaced much elegant listview
 //</revision>
 package com.example.efrigerator9;
 
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 
 import com.example.efrigerator9.MySwitch.OnChangeAttemptListener;
 
 
 import android.app.AlertDialog;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 public class DisplayActivityList extends baseActivity implements  OnChangeAttemptListener, OnCheckedChangeListener, OnClickListener
 {
 	private TextView title,previous_day, next_day;
 	private String dater;
 	private ListView ItemView;
 	private ArrayList<String> Cold_itemsadded = new ArrayList<String>();
 	private ArrayList<String> Normal_itemsadded = new ArrayList<String>();
 	private ViewSwitcher switcher;
 	private boolean Is_frozen=true;
 	MySwitch slideToUnLock;
 	Cursor cursor,cursor_next,cursor_prev;
 	DateFormat formatter_db=new SimpleDateFormat("dd-MMM-yy");
 	Date date_db;
     SpecialAdapter frozenAdapter;
 	SpecialAdapter normalAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display);
 		Intent intent= getIntent();
 		dater= intent.getStringExtra("date");
 		title = (TextView) findViewById(R.id.title);
 		title.setText(dater);
 		
 		switcher = (ViewSwitcher) findViewById(R.id.profileSwitcher);
 		slideToUnLock = (MySwitch)findViewById(R.id.switch1);
 		slideToUnLock.setOnCheckedChangeListener(this);
 	    
 		Button addItem_cold_Button = (Button)findViewById(R.id.addItem_cold);
 		Button addItem_normal_Button = (Button)findViewById(R.id.addItem_normal);
 		addItem_cold_Button.setOnClickListener(this);
 		addItem_normal_Button.setOnClickListener(this);
 		
 		previous_day = (TextView) findViewById(R.id.previous_day);
 		next_day = (TextView) findViewById(R.id.next_day);
 		previous_day.setOnClickListener(this);
 		next_day.setOnClickListener(this);	
 		frozenAdapter = new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
 		normalAdapter = new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 		 cursor = fridgeData.query();
 		 startManagingCursor(cursor);
 
 	    if (cursor.getCount()>0)
 	    {
 	    	while (cursor.moveToNext())
 	    	
 	    	{
 	    		if(cursor.getString(1).equals(dater))
 	    		{
 	    			String test = cursor.getString(2);
 	    			String cat = cursor.getString(3);
 		    		if(cat.equals("Frozen"))
 		    		{
 		    			Cold_itemsadded.add(test);
 		    			ItemView = (ListView) findViewById(R.id.list_cold);	
 		    			//ItemView.setAdapter(new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded));
 		    			ItemView.setAdapter(frozenAdapter);
 		    			ItemView.setOnItemClickListener(new OnItemClickListener() {
 		    			
 		    				
 		    				
 							@Override
 							public void onItemClick(AdapterView<?> arg0,
 									View v, int position, long id) {
 							//	frozenAdapter= new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
 				    			ItemView.setAdapter(frozenAdapter);
 								// TODO Auto-generated method stub
 								AlertDialog.Builder adb=new AlertDialog.Builder(DisplayActivityList.this);
 						        adb.setTitle("Delete?");
 						        adb.setMessage("Are you sure you want to delete " + Cold_itemsadded.get(position));
 						        final int positionToRemove = position;
 						        adb.setNegativeButton("Cancel", null);
 						        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
 						            public void onClick(DialogInterface dialog, int which) {
 						            	fridgeData.delete(Cold_itemsadded.get(positionToRemove).toString());
 						            	Cold_itemsadded.remove(positionToRemove);
 						                frozenAdapter.notifyDataSetChanged();
 						            }});
 						        adb.show();
 						       
 
 
 							}
 						});
 		    		
 		    			
 		    			
 		    		}
 		    		else
 		    		{
 		    			Normal_itemsadded.add(test);
 						ItemView = (ListView) findViewById(R.id.list_normal);	
 						//ItemView.setAdapter(new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded));
 						ItemView.setAdapter(normalAdapter);
 						ItemView.setOnItemClickListener(new OnItemClickListener() {
 	    					
 							@Override
 							public void onItemClick(AdapterView<?> arg0,
 									View v, int position, long id) {
 								//normalAdapter = new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 				    			ItemView.setAdapter(normalAdapter);
 								AlertDialog.Builder adb=new AlertDialog.Builder(DisplayActivityList.this);
 						        adb.setTitle("Delete?");
 						        adb.setMessage("Are you sure you want to delete " + Normal_itemsadded.get(position));
 						        final int positionToRemove = position;
 						        adb.setNegativeButton("Cancel", null);
 						        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
 						            public void onClick(DialogInterface dialog, int which) {
 						            	fridgeData.delete(Normal_itemsadded.get(positionToRemove).toString());
 						                Normal_itemsadded.remove(positionToRemove);
 						                normalAdapter.notifyDataSetChanged();
 						            }});
 						        adb.show();
 
 
 							}
 						});
 		    		
 					//	ItemView.setOnItemClickListener();
 									 
 		    		}
 	    		}
 	    	}
 	    }
 	    
 	}
 
 	
 
 	@Override
 	public void onClick(View v) 
 	{
 		// TODO Auto-generated method stub
 		 final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };		
 		 DateFormat formatter ; 
 		 Date date = null ; 
 		 formatter = new SimpleDateFormat("dd-MMM-yy");
 		 Calendar cal=Calendar.getInstance();
 		 
 		 super.onClick(v);
 		 switch (v.getId()) {
 		
 		 case R.id.addItem_cold :
 			 	final EditText input = new EditText(this); // This could also come from an xml resource, in which case you would use findViewById() to access the input
 				AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
 				float vol = (float) 0.5; //This will be half of the default system sound
 				am.playSoundEffect(AudioManager.FX_KEY_CLICK, vol);
 				Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); 
 				vb.vibrate(40);
 				input.setBackgroundResource(R.drawable.buttonaction);
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setView(input);
 				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
 				{
 					public void onClick(DialogInterface dialog, int whichButton) 
 					{
 						String value = input.getText().toString();	
 						if(!value.isEmpty())
 						{
 							
 							{
 								Cold_itemsadded.add(value);
 								ItemView = (ListView) findViewById(R.id.list_cold);	
 								//ItemView.setAdapter(new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded));
 								ItemView.setAdapter(frozenAdapter);
								fridgeData.insert(100, dater, value, "Frozen");
 							}
 			 
 						
 						}
			            
 						dialog.dismiss();
 					}
 				});
 				
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
 				{
 					  public void onClick(DialogInterface dialog, int whichButton) 
 					  {
 					    // Canceled.
 						  dialog.dismiss();
 					  }
 				});
 				builder.show();
 				
 			break;
 		
 		 case R.id.addItem_normal :
 			 final EditText input_n = new EditText(this); // This could also come from an xml resource, in which case you would use findViewById() to access the input
 				AudioManager am_n = (AudioManager)getSystemService(AUDIO_SERVICE);
 				float vol_n = (float) 0.5; //This will be half of the default system sound
 				am_n.playSoundEffect(AudioManager.FX_KEY_CLICK, vol_n);
 				Vibrator vb_n = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); 
 				vb_n.vibrate(40);
 				input_n.setBackgroundResource(R.drawable.buttonaction);
 				AlertDialog.Builder builder_n = new AlertDialog.Builder(this);
 				builder_n.setView(input_n);
 				builder_n.setPositiveButton("OK", new DialogInterface.OnClickListener() 
 				{
 					public void onClick(DialogInterface dialog, int whichButton) 
 					{
 						String value = input_n.getText().toString();	
 						if(!value.isEmpty())
 						{
 							
 							{
 								Normal_itemsadded.add(value);
 								ItemView = (ListView) findViewById(R.id.list_normal);	
 								//ItemView.setAdapter(new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded));
 								ItemView.setAdapter(normalAdapter);
								fridgeData.insert(100, dater, value, "Normal");
 							}
 							
 						}
			            
 						dialog.dismiss();
 					}
 				});
 				
 				builder_n.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
 				{
 					  public void onClick(DialogInterface dialog, int whichButton) 
 					  {
 					    // Canceled.
 						  dialog.dismiss();
 					  }
 				});
 				builder_n.show();
 			
 			break;
 			
 		 case R.id.previous_day:
 			 formatter = new SimpleDateFormat("dd-MMM-yy");
 			 try 
 			 {
 				 date = (Date)formatter.parse(dater);
 			 } catch (ParseException e) 
 			 {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			 } 
 			
 			 cal.setTime(date);
 			 cal.add(Calendar.DATE, -1);
 			 dater = cal.get(Calendar.DATE)+"-"+months[cal.get(Calendar.MONTH)]+"-"+cal.get(Calendar.YEAR);
 			 title.setText(dater);
 			 Cold_itemsadded.clear();
 			 Normal_itemsadded.clear();
 			 cursor_prev = fridgeData.query();
 			 startManagingCursor(cursor_prev);
 			 			
 			 cursor_prev.moveToFirst();
 			 if (cursor_prev.getCount()>0)
 			    {
 			    	do 
 			    	
 			    	{
 			    		if(cursor_prev.getString(1).equals(dater))
 			    		{
 			    			String test_prev = cursor_prev.getString(2);
 			    			String cat = cursor_prev.getString(3);
 				    		if(cat.equals("Frozen"))
 				    		{
 				    			Cold_itemsadded.add(test_prev);
 				    		}
 				    		else
 				    		{
 				    			Normal_itemsadded.add(test_prev);							
 				    		}
 			    		}
 			    	}while(cursor_prev.moveToNext());
 			    }
 			 if(Is_frozen)
 			 {
 			 	ItemView = (ListView) findViewById(R.id.list_cold);  
 			//    frozenAdapter= new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
 			    ItemView.setAdapter(frozenAdapter);
 			    this.frozenAdapter.notifyDataSetChanged();
 			    
 			 }
 			 else
 			 {
 				 ItemView = (ListView) findViewById(R.id.list_normal);  
 				// normalAdapter= new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 				 ItemView.setAdapter(normalAdapter);
 				 this.normalAdapter.notifyDataSetChanged();
 			 }
 			 break;
 			 
 		 case R.id.next_day:
 			 formatter = new SimpleDateFormat("dd-MMM-yy");
 			 try 
 			 {
 				 date = (Date)formatter.parse(dater);
 			 } catch (ParseException e) 
 			 {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			 } 
 			
 			 cal.setTime(date);
 			 cal.add(Calendar.DATE, 1);
 			 dater = cal.get(Calendar.DATE)+"-"+months[cal.get(Calendar.MONTH)]+"-"+cal.get(Calendar.YEAR);
 			 title.setText(dater);
 			 Cold_itemsadded.clear();
 			 Normal_itemsadded.clear();			
 			 cursor_next = fridgeData.query();
 			 startManagingCursor(cursor_next);
 			 cursor_next.moveToFirst();
 			 if (cursor_next.getCount()>0)
 			    {
 			    	do 
 			    	
 			    	{
 			    		if(cursor_next.getString(1).equals(dater))
 			    		{
 			    			String test_next = cursor_next.getString(2);
 			    			String cat = cursor_next.getString(3);
 				    		if(cat.equals("Frozen"))
 				    		{
 				    			Cold_itemsadded.add(test_next);	
 				    		}
 				    		else
 				    		{
 				    			Normal_itemsadded.add(test_next);											 
 				    		}
 			    		}
 			    	}while (cursor_next.moveToNext());
 			    }
 			 
 			 if(Is_frozen)
 			 {
 			 	ItemView = (ListView) findViewById(R.id.list_cold);  
 			   // frozenAdapter= new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
 			    ItemView.setAdapter(frozenAdapter);
 			    this.frozenAdapter.notifyDataSetChanged();
 			    
 			 }
 			 else
 			 {
 				 ItemView = (ListView) findViewById(R.id.list_normal);  
 				 //normalAdapter= new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 				 ItemView.setAdapter(normalAdapter);
 				 this.normalAdapter.notifyDataSetChanged();
 			 }
 			 break;
 			 
 		 }
 		
 		
 		
 	
 	}
 	
 	
   
 	@Override
 	public void onChangeAttempted(boolean isChecked)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
 	{
 		if(arg1)
 		{		
 			Is_frozen=false;
 			switcher.showPrevious();
 			Cold_itemsadded.clear();
 			 Normal_itemsadded.clear();			
 			 cursor_next = fridgeData.query();
 			 startManagingCursor(cursor_next);
 			 cursor_next.moveToFirst();
 			 if (cursor_next.getCount()>0)
 			    {
 			    	do 
 			    	
 			    	{
 			    		if(cursor_next.getString(1).equals(dater))
 			    		{
 			    			String test_next = cursor_next.getString(2);
 			    			String cat = cursor_next.getString(3);
 				    		if(cat.equals("Frozen"))
 				    		{
 				    			Cold_itemsadded.add(test_next);	
 				    		}
 				    		else
 				    		{
 				    			Normal_itemsadded.add(test_next);											 
 				    		}
 			    		}
 			    	}while (cursor_next.moveToNext());
 			    }
 			 
 			 if(Is_frozen)
 			 {
 			 	ItemView = (ListView) findViewById(R.id.list_cold);  
 			   // frozenAdapter= new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
 			    ItemView.setAdapter(frozenAdapter);
 			    this.frozenAdapter.notifyDataSetChanged();
 			    
 			 }
 			 else
 			 {
 				 ItemView = (ListView) findViewById(R.id.list_cold);  
 				 //normalAdapter= new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 				 ItemView.setAdapter(normalAdapter);
 				 this.normalAdapter.notifyDataSetChanged();
 			 }
 
 		}
 		else
 		{
 			Is_frozen=true;
 			switcher.showNext();
 			Cold_itemsadded.clear();
 			 Normal_itemsadded.clear();			
 			 cursor_next = fridgeData.query();
 			 startManagingCursor(cursor_next);
 			 cursor_next.moveToFirst();
 			 if (cursor_next.getCount()>0)
 			    {
 			    	do 
 			    	
 			    	{
 			    		if(cursor_next.getString(1).equals(dater))
 			    		{
 			    			String test_next = cursor_next.getString(2);
 			    			String cat = cursor_next.getString(3);
 				    		if(cat.equals("Frozen"))
 				    		{
 				    			Cold_itemsadded.add(test_next);	
 				    		}
 				    		else
 				    		{
 				    			Normal_itemsadded.add(test_next);											 
 				    		}
 			    		}
 			    	}while (cursor_next.moveToNext());
 			    }
 			 
 			 if(Is_frozen)
 			 {
 			 	ItemView = (ListView) findViewById(R.id.list_cold);  
 			   // frozenAdapter= new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
 			    ItemView.setAdapter(frozenAdapter);
 			    this.frozenAdapter.notifyDataSetChanged();
 			    
 			 }
 			 else
 			 {
 				 ItemView = (ListView) findViewById(R.id.list_cold);  
 				 //normalAdapter= new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 				 ItemView.setAdapter(normalAdapter);
 				 this.normalAdapter.notifyDataSetChanged();
 			 }
 
 		
 		}
 	
 	}
 
 	static class ViewHolder {
 	    TextView text;
 	}
 	
 	private class SpecialAdapter extends BaseAdapter {
 		//Defining the background color of rows. The row will alternate between green light and green dark.
 		private int[] colors = new int[] { 0xAAf6ffc8, 0xAA538d00 };
 		private LayoutInflater mInflater;
 		private ArrayList<String> Data;
 
 		public SpecialAdapter(Context context, ArrayList<String> items) {
 		    this.mInflater = LayoutInflater.from(context);
 		    this.Data = items;
 		}
 
 		@Override
 		public int getCount() {
 		    return Data.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 		    return Data.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 		    return position;
 		}
 
 		//A view to hold each row in the list
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 		// A ViewHolder keeps references to children views to avoid unneccessary calls
 		// to findViewById() on each row.
 		ViewHolder holder;
 
 		if (convertView == null) 
 		{
 		    convertView = mInflater.inflate(R.layout.row, null);
 
 		    holder = new ViewHolder();
 		    holder.text = (TextView) convertView.findViewById(R.id.headline);
 		    convertView.setTag(holder);
 		} 
 		else 
 		{
 		    holder = (ViewHolder) convertView.getTag();
 		}
 		    // Bind the data efficiently with the holder.
 	    holder.text.setText(Data.get(position).toString());
 
 	    //Set the background color depending of  odd/even colorPos result
 	 //   int colorPos = position % colors.length;
 	    convertView.setBackgroundColor(R.color.Gray);
 	    
       //  Toast.makeText(DisplayActivityList.this, "hello",Toast.LENGTH_SHORT);
 	    return convertView;
 		}
 		}
 /*
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		AlertDialog.Builder adb=new AlertDialog.Builder(DisplayActivityList.this);
         adb.setTitle("Delete?");
         if(Is_frozen)
         {
         	adb.setMessage("Are you sure you want to delete " +Cold_itemsadded.get(position) );
         	frozenAdapter= new SpecialAdapter(DisplayActivityList.this, Cold_itemsadded);
        // 	ItemView.setAdapter(frozenAdapter_d);
         }
         else
         {
         	adb.setMessage("Are you sure you want to delete " +Normal_itemsadded.get(position) );
         	normalAdapter= new SpecialAdapter(DisplayActivityList.this, Normal_itemsadded);
 		//	ItemView.setAdapter(normalAdapter_d);
 			
         }
         final int positionToRemove = position;
         adb.setNegativeButton("Cancel", null);
         adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
         
             	if(Is_frozen)
             	{
             		Cold_itemsadded.remove(positionToRemove);
             		frozenAdapter.notifyDataSetChanged();            	
 
             	}
             	else
             	{
             		Normal_itemsadded.remove(positionToRemove);
             		normalAdapter.notifyDataSetChanged();            	}
             }});
         adb.show();
         
 
 		
 	}*/
 
 
 }

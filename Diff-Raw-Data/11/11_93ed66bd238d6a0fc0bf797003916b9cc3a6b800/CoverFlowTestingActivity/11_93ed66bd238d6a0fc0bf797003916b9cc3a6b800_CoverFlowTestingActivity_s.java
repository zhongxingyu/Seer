 package com.example.coverflow.producealmanac;
 
 import java.util.ArrayList;
 
 import com.example.coverflow.CoverFlow;
 import com.example.coverflow.R;
 import com.example.coverflow.ReflectingImageAdapter;
 import com.example.coverflow.ResourceImageAdapter;
 import com.example.coverflow.producealmanac.Item;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * The Class CoverFlowTestingActivity.
  */
 public class CoverFlowTestingActivity extends Activity {
 
 	//currentItems will be the items currently in season, use a hashmap with key=time of year
 	ArrayList<Item> currentItems;
 	boolean populated=false;
 	TextView textView;
 	ResourceImageAdapter myAdapter;
 
     /*
      * (non-Javadoc)
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
 
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
 
 		if(!populated){
 			populated=true;
 			populateMap();
 		}
 		
 		//add all current items to the ArrayList
 		currentItems = new ArrayList<Item>();
 		currentItems.add(new Item("apple"));
 		currentItems.add(new Item("potato"));
 		currentItems.add(new Item("strawberry"));
 		
 		//etc etc
 		myAdapter = new ResourceImageAdapter(this);
 		
 		int resourceList[] = new int[currentItems.size()];
 		
 		for (int i = 0; i < currentItems.size(); i++) {
 			System.out.println(currentItems.get(i).name);
 			resourceList[i] = getResources().getIdentifier(currentItems.get(i).name, "drawable", getPackageName());
 			System.out.println(resourceList[i]);
 		}
 		myAdapter.setResources(resourceList);
 		
 
 		//CoverFlow code below
 		//System.out.println("MAIN ACTIVITY STARTED, printing just before COVERFLOW setup");
     	
 //        super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         // note resources below are taken using getIdentifier to allow importing
         // this library as library.
         final CoverFlow reflectingCoverFlow = (CoverFlow) findViewById(this.getResources().getIdentifier(
                 "coverflowReflect", "id", "com.example.coverflow"));
         setupCoverFlow(reflectingCoverFlow, true);
 
     }
 
     /**
      * Setup cover flow.
      * 
      * @param mCoverFlow
      *            the m cover flow
      * @param reflect
      *            the reflect
      */
     private void setupCoverFlow(final CoverFlow mCoverFlow, final boolean reflect) {
         BaseAdapter coverImageAdapter;
         if (reflect) {
             coverImageAdapter = new ReflectingImageAdapter(myAdapter);
         } else {
             coverImageAdapter = myAdapter;
         }
         mCoverFlow.setAdapter(coverImageAdapter);
         mCoverFlow.setSelection(0, true);
         setupListeners(mCoverFlow);
     }
 
     /**
      * Sets the up listeners.
      * 
      * @param mCoverFlow
      *            the new up listeners
      */
     private void setupListeners(final CoverFlow mCoverFlow) {
         mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(final AdapterView< ? > parent, final View view, final int position, final long id) {
                 //textView.setText("Item clicked! : " + id);
             }
 
         });
         mCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(final AdapterView< ? > parent, final View view, final int position, final long id) {
                 //textView.setText("Item selected! : " + id);
             }
 
             @Override
             public void onNothingSelected(final AdapterView< ? > parent) {
                 //textView.setText("Nothing clicked!");
             }
         });
         
         mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				
 				System.out.println("CLICKED : " + arg2 + " , " + arg3);
 				
 				Intent intent = new Intent(CoverFlowTestingActivity.this, DetailActivity.class);
 				intent.putExtra("name", currentItems.get(arg2).name);
 				startActivity(intent);
 			}
         	
         });
     }
     
 	public void populateMap(){
 		
 		Bitmap bitmapple = BitmapFactory.decodeResource(getResources(), R.drawable.apple);
 		ImageView appleView =  new ImageView(this);
 		appleView.setImageBitmap(bitmapple);
 		Object[] info = {"apples grow on trees", "counter", "green", bitmapple };
 		Item.infoMap.put("apple",info);
 		
 		Bitmap bitmapotato = BitmapFactory.decodeResource(getResources(), R.drawable.potato);
 		ImageView potatoView = new ImageView(this);
 		potatoView.setImageBitmap(bitmapotato);		
 		Object[] info2 = {"potatoes grow in the earth", "counter", "no ears", bitmapotato};
 		Item.infoMap.put("potato",info2);
 		
 		Bitmap bitmapberry = BitmapFactory.decodeResource(getResources(), R.drawable.strawberry);
 		ImageView strawberryView = new ImageView(this);
 		strawberryView.setImageBitmap(bitmapberry);		
 		Object[] info3 = {"strawberries are good", "refrigerator", "bright red",  bitmapberry};
 		Item.infoMap.put("strawberry", info3);
 	}


 }

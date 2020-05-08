 package com.mis.icequeen;
 
 import pl.polidea.coverflow.*;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	private TextView textView;
 	
 	// CoverFlow ܪϤ 
 	private final int[] RESOURCE_IMAGES = {
 			R.drawable.image01,
 			R.drawable.image02,
 			R.drawable.image03,
 			R.drawable.image04,
 			R.drawable.image05,
 			R.drawable.image06
 	};
 	private final int COVERFLOW_INIT_POSITION = 0;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         setupView();
         TestProvider t =new TestProvider();
         getIntent().setData(Uri.parse("content://com.mis.icequeen.testprovider"));
         Uri uri_test = getIntent().getData();
         getIntent().setData(Uri.parse("content://com.mis.icequeen.testprovider/gbid:5"));
         Uri uri = getIntent().getData();
         Cursor c = managedQuery(uri, null, null, null, null);
         if(c.getCount()!=0)
         {	c.moveToFirst();
         System.out.println(c.getString(0));
         }else 
         	System.out.println("FAIL");
         c.close();
     }
 
 	/**
 	 *  إߵe 
 	 */
 	private void setupView() {
 		// TODO Auto-generated method stub
 		textView = (TextView) findViewById(R.id.tvSelectedChapter);
 
         //CoverFlow Reflection
         final CoverFlow reflectingCoverFlow = (CoverFlow) findViewById(this.getResources().getIdentifier(
                 "coverflowReflection", "id", "com.mis.icequeen"));
         setupCoverFlow(reflectingCoverFlow, true);
         
         Button btnLearn = (Button) findViewById(R.id.btnLearn);
         Button btnReview = (Button) findViewById(R.id.btnReview);
         Button btnTest = (Button) findViewById(R.id.btnTest);
         ButtonListener buttonListener = new ButtonListener();
         btnLearn.setOnClickListener(buttonListener);
         btnReview.setOnClickListener(buttonListener);
         btnTest.setOnClickListener(buttonListener);
         
 		
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
         	ResourceImageAdapter resourceImageAdapter = new ResourceImageAdapter(this);
             coverImageAdapter = new ReflectingImageAdapter(resourceImageAdapter);
             resourceImageAdapter.setResources(RESOURCE_IMAGES);
         } else {
             coverImageAdapter = new ResourceImageAdapter(this);
             ((ResourceImageAdapter) coverImageAdapter).setResources(RESOURCE_IMAGES);
         }
        
         mCoverFlow.setAdapter(coverImageAdapter);
         mCoverFlow.setSelection(COVERFLOW_INIT_POSITION, true);
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
             public void onItemClick(final AdapterView< ? > parent, final View view, final int position, final long id) {
                 textView.setText("Item clicked! : " + id);
             }
 
         });
         mCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(final AdapterView< ? > parent, final View view, final int position, final long id) {
                 textView.setText("Item selected! : " + id);
             }
 
             public void onNothingSelected(final AdapterView< ? > parent) {
                 textView.setText("Nothing clicked!");
             }
         });
     }
     
     private class ButtonListener implements OnClickListener {
 
 		public void onClick(View v) {
 			
 			//String selection;
			/*switch (v.getId()) {
 			case R.id.btnLearn:
 				//selection = "ChapterSelectionActivity";
 				break;
			case R.id.btnNext:
 				//review
 				break;
 			case R.id.btnTest:
 				//test
 				break;
			}*/
 			
 			Intent intent = new Intent(MainActivity.this, ChapterSelectionActivity.class);
 	        startActivity(intent);
 
 		}
     	
     }
 }

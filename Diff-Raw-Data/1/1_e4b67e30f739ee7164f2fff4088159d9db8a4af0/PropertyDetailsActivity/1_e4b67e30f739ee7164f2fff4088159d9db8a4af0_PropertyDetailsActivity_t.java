 package pt.up.fe.cmov.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import pt.up.fe.cmov.display.Display;
 import pt.up.fe.cmov.gridadapter.PropertyGridAdapter;
 import pt.up.fe.cmov.listadapter.EntryAdapter;
 import pt.up.fe.cmov.listadapter.EntryItem;
 import pt.up.fe.cmov.listadapter.Item;
 import pt.up.fe.cmov.propertymarket.R;
 import pt.up.fe.cmov.propertymarket.rest.RailsRestClient;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector.SimpleOnGestureListener;
 
 
 public class PropertyDetailsActivity extends ListActivity {
 	
 	ArrayList<Item> items = new ArrayList<Item>();
 	private final int syncBtnId = Menu.FIRST;
 	private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 	
 	 @Override
 	 public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
			setContentView(R.layout.details_property);
 		
 	        try {
 	        	  Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(RailsRestClient.SERVER_URL + PropertyTabMenuActivity.propertyInfo.getPhoto()).getContent());
 	        	  items.add(new EntryItem(bitmap,false));
 	        	  items.add(new EntryItem(this.getString(R.string.property_name),PropertyTabMenuActivity.propertyInfo.getName(),false));
 				  items.add(new EntryItem(this.getString(R.string.property_state),PropertyTabMenuActivity.propertyInfo.getState(),false));		
 	        	} catch (MalformedURLException e) {
 	        	  e.printStackTrace();
 	        	} catch (IOException e) {
 	        	  e.printStackTrace();
 	        }
 	        
 	        gestureDetector = new GestureDetector(new MyGestureDetector());
 	        gestureListener = new View.OnTouchListener() {
 	            public boolean onTouch(View v, MotionEvent event) {
 	                if (gestureDetector.onTouchEvent(event)) {
 	                    return true;
 	                }
 	                return false;
 	            }
 	        };
 	                
 		 	EntryAdapter adapter = new EntryAdapter(this, items);
 		 	setListAdapter(adapter);
 	 }
 	    
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 	  if (gestureDetector.onTouchEvent(event))
 		return true;
 	  else
 		return false;
 	}
 	 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	   MenuItem searchMItm = menu.add(Menu.NONE,syncBtnId ,syncBtnId,this.getString(R.string.discard_menu));
 	   searchMItm.setIcon(android.R.drawable.ic_menu_delete);
 	   return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		 switch (item.getItemId()) {
 		     case syncBtnId:
 	            Display.dialogBuildDeleteDiscard(PropertyTabMenuActivity.propertyInfo.getId(),this);
 		     break;
 		 } 
 		 return true;
 	}
 	
 	class MyGestureDetector extends SimpleOnGestureListener {
 	  @Override
 	  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 	    try {
 	         if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
 	            return false;
 	         if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 	        	 PropertyMarketActivity.selectedPropertyID = PropertyGridAdapter.getItemStaticId(getPositionToMoveFoward());
 	             Intent intent = new Intent(PropertyDetailsActivity.this,PropertyTabMenuActivity.class);
 	             PropertyDetailsActivity.this.startActivity(intent);	         
 	         }else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 	        	 PropertyMarketActivity.selectedPropertyID = PropertyGridAdapter.getItemStaticId(getPositionToMoveBackward());
 	             Intent intent = new Intent(PropertyDetailsActivity.this,PropertyTabMenuActivity.class);
 	             PropertyDetailsActivity.this.startActivity(intent);	 
 	         }
 	     } catch (Exception e) {
 	     return false;
 	    }
 		return true;   
 	 }
 	}
 	
 	public int getPositionToMoveFoward(){
 		if(PropertyGridAdapter.getStaticCount() - 1 > PropertyMarketActivity.selectedPropertyPosition)
    		 	PropertyMarketActivity.selectedPropertyPosition += 1;
    	 	else
    	 		PropertyMarketActivity.selectedPropertyPosition = 0;
 		return PropertyMarketActivity.selectedPropertyPosition;
 	}
 	
 	public int getPositionToMoveBackward(){
 		 if(PropertyMarketActivity.selectedPropertyPosition > 0)
     		 PropertyMarketActivity.selectedPropertyPosition -= 1;
     	 else
     		 PropertyMarketActivity.selectedPropertyPosition = PropertyGridAdapter.getStaticCount();
 		return PropertyMarketActivity.selectedPropertyPosition;
 	}
 }

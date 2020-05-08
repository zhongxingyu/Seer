 package xcx.rockface.main;
 
 import java.security.KeyStore.LoadStoreParameter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Gallery.LayoutParams;
 import android.widget.ViewSwitcher.ViewFactory;
 
 public class ImageSwitcherTest extends Activity implements
 		OnItemSelectedListener, ViewFactory {
 	
 	private String TAG = this.getClass().getName();
 	
 	private ImageSwitcher is;
 	private Gallery gallery;
 	private Integer[] mThumbIds = { R.drawable.lp001, R.drawable.lp002, R.drawable.lp003,
 			R.drawable.lp004, R.drawable.lp005 };
 	private Integer[] mImageIds = { R.drawable.lp001, R.drawable.lp002, R.drawable.lp003,
 			R.drawable.lp004, R.drawable.lp005 };
 	private int downX, upX;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.imageswitcher);
 		is = (ImageSwitcher) findViewById(R.id.switcher);
 		is.setFactory(this);
 		is.setInAnimation(AnimationUtils.loadAnimation(this,
 				android.R.anim.fade_in));
 		is.setOutAnimation(AnimationUtils.loadAnimation(this,
 				android.R.anim.fade_out));
 		//ӻл
 		is.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View view, MotionEvent event) {
 				if(event.getAction()==MotionEvent.ACTION_DOWN) {
 					downX = (int)event.getX(); //ȡðʱx
 					return true;
 				}else if(event.getAction()==MotionEvent.ACTION_UP){
 					upX = (int)event.getX(); //ȡɿʱx
 					int index = gallery.getSelectedItemPosition();
 					int count = gallery.getCount() ;
 					Log.d(TAG,"downX = " + downX + "; upX = " + upX);
 					Log.d(TAG, "index = " + index + "; count = " + count);
 					if(downX - upX > 100){
 						if(index == count - 1){//
 							index = 0;
 						}else{
 							index ++; 
 						}
 						is.setInAnimation(getApplicationContext(), R.layout.slide_in_right);
 						is.setOutAnimation(getApplicationContext(), R.layout.slide_out_left);						
 					}else if(upX - downX > 100){
 						if(index == 0){//ǰ
 							index = count - 1;
 						}else {
 							index --;
 						}
 						is.setInAnimation(getApplicationContext(), android.R.anim.slide_in_left);
 						is.setOutAnimation(getApplicationContext(), android.R.anim.slide_out_right);
 
 					}
 					Log.d(TAG, "index = " + index);
 					gallery.setSelection(index,true);
 					return true;
 				}
 				return false;
 			}
 		});
 		gallery = (Gallery) findViewById(R.id.gallery);
 		gallery.setAdapter(new ImageAdapter(this));
 		gallery.setOnItemSelectedListener(this);
 	}
 
 	@Override
 	public View makeView() {
 		ImageView i = new ImageView(this);
 		i.setBackgroundColor(0xFF000000);
 		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		i.setLayoutParams(new ImageSwitcher.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		return i;
 	}
 
 	public class ImageAdapter extends BaseAdapter {
 		public ImageAdapter(Context c) {
 			mContext = c;
 		}
 
 		public int getCount() {
 			return mThumbIds.length;
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView i = new ImageView(mContext);
 			i.setImageResource(mThumbIds[position]);
 			i.setAdjustViewBounds(true);
 			i.setLayoutParams(new Gallery.LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			//i.setBackgroundResource(R.drawable.);
 			return i;
 		}
 
 		private Context mContext;
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position,
 			long id) {
 		is.setImageResource(mImageIds[position]);
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		// TODO Auto-generated method stub
 	}
 	
 	
 	
 	
 }

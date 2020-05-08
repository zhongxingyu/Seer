 package com.quackware.tric.ui;
 
 import com.quackware.tric.MyApplication;
 import com.quackware.tric.R;
 import com.quackware.tric.stats.Stats;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 public class TricCategoryDetailActivity extends Activity implements OnClickListener{
 	
 	
 	private static final String TAG = "TricCategoryDetailActivity";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tric_category_detail);
 		
 		String category = getIntent().getExtras().getString("Category");
 		
 		loadCategory(category);
 	}
 	
 	private void loadCategory(String category)
 	{
 		LinearLayout ll = (LinearLayout)findViewById(R.id.catdetail_ll);
 		for(Stats s : MyApplication.getStats())
 		{
 			if(s.getType().equals(category) || (s.getType().equals("FacebookStats") && category.equals("SocialStats")))
 			{
 				Button b = new Button(this);
 				b.setText(s.getPrettyName());
 				b.setTag(s.getName());
 				b.setOnClickListener(this);
 				Drawable img = getCategoryDrawable(category,s.getType());
 				if(img != null)
 				{
 					b.setCompoundDrawablesWithIntrinsicBounds(img, null,null,null);
 				}
 				ll.addView(b);
 			}
 		}
 	}
 	
 	private Drawable getCategoryDrawable(String category, String optionalSocialCategory)
 	{
 		Resources res = getResources();
 		if(category.equals("SocialStats"))
 		{
 			if(optionalSocialCategory != null)
 			{
 				if(optionalSocialCategory.equals("FacebookStats"))
 				{
 					return resize(res.getDrawable(R.drawable.facebook));
 				}
 			}
 			return res.getDrawable(R.drawable.gnexus);
 		}
 		else if(category.equals("PhoneStats"))
 		{
 			return resize(res.getDrawable(R.drawable.gnexus));
 		}
 		else if(category.equals("AppStats"))
 		{
 			return resize(res.getDrawable(R.drawable.android_image));
 		}
		else if(category.equals("TrafficStats"))
 		{
 			//TODO replace this with a better drawable.
 			return resize(res.getDrawable(R.drawable.gnexus));
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	private Drawable resize(Drawable image) 
 	{
 	    Bitmap d = ((BitmapDrawable)image).getBitmap();
 	    Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, 75, 75, false);
 	    return new BitmapDrawable(bitmapOrig);
 	}
 
 	@Override
 	public void onClick(View view) {
 		String name = (String)view.getTag();
 		try
 		{
 			Intent intent = new Intent(this,TricDetailActivity.class);
 			intent.putExtra("tricName", name);
 			startActivity(intent);
 		}
 		catch(Exception ex)
 		{
 			Log.e(TAG,"Unable to start tric detail activity with name " + name);
 			return;
 		}
 	}
 
 }

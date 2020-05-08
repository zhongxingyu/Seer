package it.csi.sii.android.json;
 
import it.csi.sii.android.json.dupal.App;
 import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class AppView extends LinearLayout {
 
 	public AppView(Context context, App app, String baseUrl) {
 		super(context);
 
 		mImage = new ImageView(context);
 //		Bitmap bm = BitmapFactory.decodeStream(app.getField_image()
 //				.getInputStream(baseUrl));
 
 //		mImage.setImageBitmap(bm);
 		addView(mImage);
 		rel = new RelativeLayout(context);
 
 		mTitle = new TextView(context);
 		mTitle.setText(app.getTitle());
 
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(context, null);
 		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
 		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
 
 		mTitle.setLayoutParams(params);
 		mTitle.setId(R.id.title);
 		rel.addView(mTitle, params);
 
 		mVersion = new TextView(context);
 	//	mVersion.setText(app.getField_version().getValue());
 		RelativeLayout.LayoutParams paramsV = new RelativeLayout.LayoutParams(
 				this.getLayoutParams());
 		paramsV.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
 		paramsV.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.title);
 
 		rel.addView(mVersion, paramsV);
 		addView(rel);
 	}
 
 	public AppView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 
 	private TextView mTitle;
 	private TextView mVersion;
 	private ImageView mImage;
 	private RelativeLayout rel;
 }

 package se.tidensavtryck;
 
 import se.tidensavtryck.model.Place;
 import se.tidensavtryck.model.Record;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.android.imageloader.ImageLoader;
 import com.google.android.imageloader.ImageLoader.BindResult;
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.actionbar.R;
 
 public class RecordActivity extends Activity implements ImageLoader.Callback {
     private Place mPlace;
     private int mRecordIndex;
     private LayoutInflater mInflater;
     private LinearLayout mImageHolder;
     private ThumbnailOnClickListener mThumbnailOnClickListener;
     private ActionBar mActionBar;
 
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         setContentView(R.layout.record);
 
         mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         mPlace = (Place) getIntent().getParcelableExtra("place");
         mRecordIndex = 0;
 
         mActionBar = initActionBar();
         mActionBar.setTitle(mPlace.getTitle());
 
         mImageHolder = (LinearLayout) findViewById(R.id.thumbnail_holder);
         mThumbnailOnClickListener = new ThumbnailOnClickListener();
 
         for (Record record : mPlace.getRecords()) {
             //mThumbnailUrls.add(record.getThumbnailURL());
             mImageHolder.addView(inflateThumbnail(record));
         }
 
         if (mPlace.getRecords().size() > 0) {
             final Record record = mPlace.getRecords().get(0);
             showRecord(record);
         }
 	}
 
     private View inflateThumbnail(Record record) {
         View view = mInflater.inflate(R.layout.record_thumbnail, mImageHolder, false);
 
         ImageView imageView =
             (ImageView) view.findViewById(R.id.record_thumbnail);
 
         BindResult result = ImageLoader.get(this).bind(imageView, record.getThumbnailURL(), this);
         if(result == ImageLoader.BindResult.LOADING) {
             imageView.setVisibility(ImageView.GONE);
         }
         view.setTag(record);
         view.setOnClickListener(mThumbnailOnClickListener);
 
         return view;
     }
     
     private ActionBar initActionBar() {
         ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
         actionBar.setTitle(String.format("%1$d av %2$d", mRecordIndex+1, mPlace.getRecords().size()));
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setHomeAction(new ActionBar.Action() {
 
             @Override
             public int getDrawable() {
                 return R.drawable.ic_actionbar_home_default;
             }
 
             @Override
             public void performAction(View view) {
                 finish();
             }
 
         });
 
         return actionBar;
     }
 
     private void showRecord(Record record) {
         TextView title = (TextView) findViewById(R.id.recordTitle);
         title.setText(record.getTitle());
 
         TextView description = (TextView) findViewById(R.id.recordDescription);
         description.setText(record.getDescription());
 
         if (mActionBar.getActionCount() > 2) {
             mActionBar.removeActionAt(1);
         }
         mActionBar.addAction(new ActionBar.IntentAction(this, createShareIntent(record), R.drawable.ic_actionbar_share));
     }
 
     @Override
     public void onImageLoaded(ImageView imageView, String s) {
         imageView.setVisibility(ImageView.VISIBLE);
     }
 
     @Override
     public void onImageError(ImageView imageView, String s, Throwable throwable) {
         Log.w("Avtryck", throwable.toString());
     }
 
     private Intent createShareIntent(Record record) {
         final Intent intent = new Intent(Intent.ACTION_SEND);
 
         intent.setType("text/plain");
         intent.putExtra(Intent.EXTRA_SUBJECT,
                 String.format("%s", record.getTitle()));
         intent.putExtra(Intent.EXTRA_TEXT,
                String.format("%s #appening via Avtryck.", record.getDescription()));
 
         return intent;
     }
 
     private class ThumbnailOnClickListener implements OnClickListener {
 
         @Override
         public void onClick(View v) {
             final Object tag = v.getTag();
             if (tag instanceof Record) {
                 final Record record = (Record) tag;
                 //showRecord(record);
                 Intent i = new Intent(RecordActivity.this, RecordImageActivity.class);
                 i.putExtra("record", record);
                 startActivity(i);
             }
         }
 
     }
 }

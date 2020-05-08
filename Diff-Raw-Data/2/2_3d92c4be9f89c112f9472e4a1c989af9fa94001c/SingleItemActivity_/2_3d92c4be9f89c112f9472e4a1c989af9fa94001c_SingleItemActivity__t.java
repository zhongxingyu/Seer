 /**
  * 
  */
 package org.vimeoid.activity.base;
 
 import org.vimeoid.R;
 import org.vimeoid.adapter.SectionedActionsAdapter;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.activity.guest</dd>
  * </dl>
  *
  * <code>SingleItemActivity_</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 16, 2010 6:41:44 PM 
  *
  */
 public abstract class SingleItemActivity_<ItemType> extends Activity {
     
     // private static final String TAG = "SingleItemActivity_";
     
     private final int mainView;
     private boolean loadManually = false;
     
     private View titleBar;
     private View progressBar;    
     
     protected ImageLoader imageLoader;
     
     public SingleItemActivity_(int mainView) {
         this.mainView = mainView;
     }
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(mainView);
         
         titleBar = findViewById(R.id.titleBar);
         initTitleBar((ImageView)titleBar.findViewById(R.id.subjectIcon),
                      (TextView)titleBar.findViewById(R.id.subjectTitle),
                      (ImageView)titleBar.findViewById(R.id.resultIcon));
         
         progressBar = findViewById(R.id.progressBar);
         progressBar.setVisibility(View.GONE);
         
         imageLoader = new ImageLoader(this, R.drawable.item_loading_small, R.drawable.item_failed);
         
         prepare(getIntent().getExtras());
     
         if (!loadManually) queryItem();
     }
     
     protected void setLoadManually(boolean value) {
         loadManually = value;
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         
         hideProgressBar();
     }    
 
     protected void prepare(Bundle extras) { };
     
     protected abstract void queryItem();
     
     protected abstract SectionedActionsAdapter fillWithActions(final SectionedActionsAdapter actionsAdapter, final ItemType item);
     
     protected abstract void initTitleBar(ImageView subjectIcon, TextView subjectTitle, ImageView resultIcon);
     
     protected void onItemReceived(final ItemType item) {
         final ListView actionsList = getActionsList();
         final SectionedActionsAdapter actionsAdapter = new SectionedActionsAdapter(this, getLayoutInflater(), imageLoader);
         actionsList.setAdapter(fillWithActions(actionsAdapter, item));
         actionsList.setOnItemClickListener(actionsAdapter);
         actionsList.invalidate();
     }
     
     protected final ListView getActionsList() {
         return (ListView)findViewById(R.id.actionsList);
     }
     
     protected final void showProgressBar() {
         progressBar.setVisibility(View.VISIBLE);
     }
     
     protected final void hideProgressBar() {
         progressBar.setVisibility(View.GONE);
     }
     
     public final void setProgressBarVisibile(boolean value) {
         progressBar.setVisibility(value ? View.VISIBLE : View.GONE);
     }    
     
     public final String getQString(int resId, int quantity) {
        return getResources().getQuantityString(resId, quantity, quantity);
     }
     
 }

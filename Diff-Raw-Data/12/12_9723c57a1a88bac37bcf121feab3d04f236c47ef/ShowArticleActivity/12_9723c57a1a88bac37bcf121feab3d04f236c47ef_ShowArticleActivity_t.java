 package fr.utc.assos.payutc;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import fr.utc.assos.payutc.adapters.IconAdapter;
 import fr.utc.assos.payutc.soap.SoapTask;
 import fr.utc.assos.payutc.views.PanierSummary;
 
 
 
 
 
 /**
  * Afficher les articles que l'on peut acheter
  * 
  * @author thomas
  *
  */
 public class ShowArticleActivity extends BaseActivity {
 	private static final String LOG_TAG = "ShowArticleActivity";
 	
 	private static final int PANIER				= 0;
 	private static final int CONFIRM_PAYMENT 	= 1;
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(LOG_TAG, "onCreate ShowArticleActivity");
         setContentView(R.layout.showarticles);
         
         new GetItemsTask().execute();
     }
     
     protected void initGridView(ArrayList<Item> items) {
     	IconAdapter adapter = new IconAdapter(this, R.layout.icon, items);
 
         /*for (Item item : items) {
         	new DownloadImgTask(adapter).execute(item);
         }*/
         
         GridView gridview = (GridView) findViewById(R.id.show_articles_view);
         gridview.setAdapter(adapter);
         
         
         gridview.setOnItemClickListener(mOnItemClickListener);
     }
     
     protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
     	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
         	Item i = (Item) parent.getAdapter().getItem(position);
 
         	// ajouter l'item dans le panier
             mSession.addItem(i);
             
         	// affichage du nouveaux résumé
            PanierSummary summary = (PanierSummary) findViewById(R.id.show_articles_panier_summary);
            summary.set(mSession);
         }
     };
     
     private class GetItemsTask extends SoapTask {
     	private ArrayList<Item> mItems=null;
     	
     	public GetItemsTask() {
     		super("Chargement", ShowArticleActivity.this, 
     				"Veuillez patienter", 2);
     	}
     	
 		@Override
 		protected boolean callSoap() throws Exception {
 			mItems = PaulineActivity.PBUY.getArticles();
 			return mItems != null;
 		}
 		
 		@Override
 		protected void onPostExecute(Integer osef) {
 			super.onPostExecute(osef);
 	        if (mItems == null) {
 	        	Log.e(LOG_TAG, "Error:soap return null");
 	        }
 	        else {
 	        	initGridView(mItems);
 	        }
 		}
     	
     }
     
     public void onClickCancel(View view) {
     	stop();
     }
     
     public void onClickOk(View view) {
     	startConfirmPaymentActivity();
     }
     
     private void startConfirmPaymentActivity() {
     	Log.d(LOG_TAG,"startAskSellerPassword");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.ConfirmPaymentActivity.class);
     	startActivityForResult(intent, CONFIRM_PAYMENT);
     }
     
     public void onClickPanier(View view) {
     	Log.d(LOG_TAG,"startAskSellerPassword");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.PanierActivity.class);
     	startActivityForResult(intent, PANIER);
     }
     
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.d(LOG_TAG, "requestCode:"+requestCode+" ,resultCode:"+resultCode + " " +RESULT_OK);
 		
 		switch (requestCode) {
 		case CONFIRM_PAYMENT:
 	    	if (resultCode == RESULT_OK) {
 	    		mSession.clearItems();
 	    	}
 		}

        PanierSummary summary = (PanierSummary) findViewById(R.id.show_articles_panier_summary);
		summary.set(mSession);
     }
 
 	/*private class DownloadImgTask extends AsyncTask<Item, Integer, Integer> {
 		private final static String LOG_TAG		= "DownloadImg";
 		IconAdapter mAdapter;
 		
 		public DownloadImgTask(IconAdapter adapter) {
 			mAdapter = adapter;
 		}
 
 		@Override
 		protected Integer doInBackground(Item... items) {
 			Item item = items[0];
 			GetImageResult result = PaulineActivity.PBUY.getImage(item.getIdImg());
 			if (result == null) {
 				Log.e(LOG_TAG, "Error (img#"+item.getIdImg()+"): soap return null");
 			}
 			else if (result.getErrorCode() != 0) {
 				Log.e(LOG_TAG, "Error (img#"+item.getIdImg()+"):"+result.getErrorCode());
 			}
 			else {
 				item.setmImage(result.getEncoded());
 			}
 			return 0;
 		}
 		
 		@Override
 		protected void onPostExecute(Integer result) {
 			mAdapter.notifyDataSetChanged();
 		}
 	 }*/
 }

 package pl.citybikerandroid.network;
 
 import android.content.Context;
 import android.widget.Toast;
 
 import com.octo.android.robospice.persistence.exception.SpiceException;
 import com.octo.android.robospice.request.listener.RequestListener;
 
 public abstract class CollectionRequestListener<T> implements RequestListener<T> {
 
 	private Context mContext;
 	
 	public CollectionRequestListener(Context context) {
 		this.mContext = context;
 	}
 	
 		@Override
 		public void onRequestSuccess(T collection) {
 
 			if (collection == null) {
 				Toast.makeText(mContext,
						"Server claims the requested collection is empty!",
 						Toast.LENGTH_LONG).show();
 			}
 
 			performOnSuccess(collection);
 			
 		}
 		
 		abstract public void performOnSuccess(T collection);
 		
 		@Override
 		public void onRequestFailure(SpiceException e) {
 			Toast.makeText(mContext,
 					"Error during request: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 	}

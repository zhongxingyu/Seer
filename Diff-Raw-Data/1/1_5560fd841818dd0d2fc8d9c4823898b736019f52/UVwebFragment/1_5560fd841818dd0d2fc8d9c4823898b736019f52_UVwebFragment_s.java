 package fr.utc.assos.uvweb.ui;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.util.ConnectionUtils;
 
 /**
  * Base Fragment that must be extended by any {@link SherlockFragment} that has a UI.
  */
 public abstract class UVwebFragment extends SherlockFragment {
 	private static boolean mIsNetworkCroutonDisplayed = false;
 	protected void handleNetworkError() {
 		handleNetworkError(getSherlockActivity());
 	}
 
 	protected void handleNetworkError(SherlockFragmentActivity context) {
 		if (!mIsNetworkCroutonDisplayed) {
 			mIsNetworkCroutonDisplayed = true;
 			final Crouton networkCrouton = Crouton.makeText(context,
 					context.getString(R.string.network_error_message),
 					ConnectionUtils.NETWORK_ERROR_STYLE);
 
 			networkCrouton.setLifecycleCallback(new LifecycleCallback() {
 				@Override
 				public void onDisplayed() {
 				}
 
 				@Override
 				public void onRemoved() {
 					mIsNetworkCroutonDisplayed = false;
 				}
 			});
 
 			networkCrouton.show();
 		}
 	}
 }

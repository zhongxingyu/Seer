 package eu.fiveminutes.cipele46.fragment;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 import eu.fiveminutes.cipele46.R;
 import eu.fiveminutes.cipele46.api.CipeleAPI.UserAdSection;
 
 public class ActiveAdsFragment extends UserAdsFragment{
 
 	@Override
 	public UserAdSection getSection() {
 		return UserAdSection.ACTIVE_ADS;
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.details, menu);
        menu.findItem(R.id.details_favorites).setVisible(false);

    }
 
 }

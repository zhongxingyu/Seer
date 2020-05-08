 package de.inselhome.beermat;
 
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import de.inselhome.beermat.domain.Bill;
 import de.inselhome.beermat.exception.BillPersistenceException;
 import de.inselhome.beermat.fragment.MyBillListFragment;
 import de.inselhome.beermat.intent.MyProfileListIntent;
 import de.inselhome.beermat.persistence.BillFileRepository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MyProfileList extends SherlockFragmentActivity implements MyBillListFragment.FragmentCallback {
 
     private static final String LOGTAG = "[beermat] MyProfileList";
 
     private List<Bill> profileList;
     private MyBillListFragment billListFragment;
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.activity_mybilllist);
 
         profileList = new ArrayList<Bill>();
         billListFragment = new MyBillListFragment();
         getSupportFragmentManager().beginTransaction().replace(R.id.listFragment, billListFragment).commit();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         loadProfiles();
     }
 
     @Override
     public List<Bill> getBills() {
         return profileList;
     }
 
     private void loadProfiles() {
         try {
             List<Bill> profiles = BillFileRepository.getInstance(this).getAllProfiles();
 
             if (profiles != null && !profiles.isEmpty()) {
                 setProfiles(profiles);
             }
             else {
                Toast.makeText(this, getString(R.string.myprofilelist_no_profiles), Toast.LENGTH_LONG).show();
             }
         } catch (BillPersistenceException e) {
             Log.e(LOGTAG, "Unable to read profiles from file system", e);
            Toast.makeText(this, getString(R.string.myprofilelist_load_profiles_failed), Toast.LENGTH_LONG).show();
         }
     }
 
     private void setProfiles(List<Bill> profileList) {
         this.profileList = profileList;
         billListFragment.notifyDataChanged();
     }
 
     @Override
     public void onBillSelected(Bill profile) {
         Intent i = new MyProfileListIntent(profile);
         setResult(RESULT_OK, i);
         finish();
     }
 
     @Override
     public void onDeleteBill(Bill profile) {
         Toast.makeText(this, "Profile deletion is currently not implemented", Toast.LENGTH_LONG).show();
     }
 }

 package de.inselhome.beermat;
 
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import de.inselhome.beermat.domain.Bill;
 import de.inselhome.beermat.exception.BillPersistenceException;
 import de.inselhome.beermat.fragment.MyBillListFragment;
 import de.inselhome.beermat.intent.MyBillListIntent;
 import de.inselhome.beermat.persistence.BillFileRepository;
 import de.inselhome.beermat.persistence.BillRepository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MyBillList extends SherlockFragmentActivity implements MyBillListFragment.FragmentCallback {
 
     private static final String LOGTAG = "[beermat] MyBillList";
 
     private List<Bill> billList;
     private MyBillListFragment billListFragment;
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.activity_mybilllist);
 
         billList = new ArrayList<Bill>();
         billListFragment = new MyBillListFragment();
         getSupportFragmentManager().beginTransaction().replace(R.id.listFragment, billListFragment).commit();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         loadBills();
     }
 
     @Override
     public List<Bill> getBills() {
         return billList;
     }
 
     private void loadBills() {
         try {
             List<Bill> bills = BillFileRepository.getInstance(this).getAll();
 
             if (bills != null && !bills.isEmpty()) {
                 setBillList(bills);
             }
             else {
                 Toast.makeText(this, getString(R.string.mybilllist_no_bills), Toast.LENGTH_LONG).show();
             }
         } catch (BillPersistenceException e) {
             Log.e(LOGTAG, "Unable to read bills from file system", e);
             Toast.makeText(this, getString(R.string.mybilllist_load_bills_failed), Toast.LENGTH_LONG).show();
         }
     }
 
     private void setBillList(List<Bill> billList) {
         this.billList = billList;
         billListFragment.notifyDataChanged();
     }
 
     @Override
     public void onBillSelected(Bill bill) {
         Intent i = new MyBillListIntent(this, bill);
         setResult(RESULT_OK, i);
         finish();
     }
 
     @Override
     public void onDeleteBill(Bill bill) {
         BillRepository billRepository = BillFileRepository.getInstance(this);
 
         try {
             billRepository.delete(bill);
            billList.remove(bill);
         } catch (BillPersistenceException e) {
             Log.e(LOGTAG, "TODO: Unable to delete bill " + bill.getId(), e);
         }
 
         billListFragment.notifyDataChanged();
     }
 }

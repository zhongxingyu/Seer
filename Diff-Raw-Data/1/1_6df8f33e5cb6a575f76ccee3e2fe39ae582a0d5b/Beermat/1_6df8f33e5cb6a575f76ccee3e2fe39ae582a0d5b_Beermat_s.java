 package de.inselhome.beermat;
 
 import android.app.Activity;
 import android.os.Bundle;
 import de.inselhome.beermat.domain.BillPosition;
 import de.inselhome.beermat.fragment.BillFragment;
 
 public class Beermat extends Activity implements BillFragment.BillListener {
 
     private BillFragment billFragment;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         billFragment = buildBillFragment();
         getFragmentManager().beginTransaction().add(R.id.billFragment, billFragment).commit();
     }
 
     private BillFragment buildBillFragment() {
         BillFragment billFragment = new BillFragment();
         billFragment.setArguments(getIntent().getExtras());
         billFragment.setBillListener(this);
         return billFragment;
     }
 
     @Override
     public void onRemoveBillPosition(BillPosition billPosition) {
         // TODO implement me
     }
 
     @Override
     public void onIncreaseBillPosition(BillPosition billPosition) {
         // TODO implement me
     }
 
     @Override
     public void onDecreaseBillPosition(BillPosition billPosition) {
         // TODO implement me
     }
 }

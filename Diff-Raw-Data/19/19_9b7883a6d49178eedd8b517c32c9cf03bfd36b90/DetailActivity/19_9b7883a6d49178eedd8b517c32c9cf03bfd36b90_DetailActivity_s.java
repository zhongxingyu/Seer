 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.os.Bundle;
import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.fragments.DetailFragment;
import no.hials.muldvarp.v2.fragments.ListFragment;
 import no.hials.muldvarp.v2.fragments.ListFragment.ListType;
import no.hials.muldvarp.v2.fragments.WebzViewFragment;
 import no.hials.muldvarp.v2.utility.FragmentUtils;
 
 /**
  * This class defines a Activity used for displaying information about an item,
  * as well as providing functionality for interacting with it.
  *
  * @author johan
  */
 public class DetailActivity extends MuldvarpActivity {
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         domain = (Domain) getIntent().getExtras().get("Domain");
         FragmentUtils.changeFragmentWithoutList(this, new DetailFragment(domain.getName(), domain.getIcon(), ListType.DOCUMENT));
     }
 
 }

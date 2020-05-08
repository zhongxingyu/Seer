 package pl.warsjawa.android2.ui;
 
 import android.app.Activity;
 import android.support.v4.app.Fragment;
 
 import javax.inject.Inject;
 
 import pl.warsjawa.android2.CoolApp;
 import pl.warsjawa.android2.event.EventBus;
 
 public class BaseFragment extends Fragment {
 
     @Inject
     EventBus bus;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         ((CoolApp) activity.getApplication()).inject(this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         bus.register(this);
     }
 
     @Override
     public void onPause() {
         super.onPause();
        bus.unregister(this);
     }
 }

 package org.utn.proyecto.helpful.integrart.integrar_t_android.menu;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.EventBus;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.menu.item.*;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ItemDetailFragment extends Fragment {
 
     public static final String ARG_ITEM_ID = "item_id";
 
     MainMenuItem.MenuItem mItem;
     private EventBus bus;
     
     public ItemDetailFragment() {}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (getArguments().containsKey(ARG_ITEM_ID)) {
             mItem = MainMenuItem.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
         }
     }
     
     public void setBus(EventBus bus){
     	this.bus = bus;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.mam_fragment_item_detail, container, false);
        rootView.setBackgroundDrawable(mItem.bckground);
         if (mItem != null) {
            // ((TextView) rootView.findViewById(R.id.tv_activity_description)).setText(mItem.getActivityDescription());
         }
         Button button = (Button) rootView.findViewById(R.id.btn_activity_launch);
         button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				bus.dispatch(mItem.event);
 				
 			}
 		});
         return rootView;
     }
 }

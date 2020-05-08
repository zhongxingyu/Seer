 package org.daum.library.android.moyens.view;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Pair;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import org.daum.common.model.api.*;
 import org.daum.library.android.moyens.view.listener.IMoyensListener;
 import org.daum.library.android.moyens.view.quickactionbar.QuickActionsBar;
 import org.daum.library.android.moyens.view.quickactionbar.listener.OnActionClickedListener;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: max
  * Date: 31/05/12
  * Time: 17:41
  * To change this template use File | Settings | File Templates.
  */
 public class MoyensView extends RelativeLayout implements OnActionClickedListener {
 
     // Debugging
     private static final String TAG = "MoyensView";
     private static final boolean D = true;
 
     // view ID constants
     private static final int ID_LIST_HEADER = 1;
     private static final int ID_LIST_VIEW = 2;
     private static final int ID_QA_BAR = 3;
 
     // String constants
     private static final String TEXT_EMPTY_LIST = "Aucune demande";
     private static final String TEXT_AGRES = "Agrès";
     private static final String TEXT_CIS = "CIS";
     private static final String TEXT_DEMAND = "Demande";
     private static final String TEXT_CRM = "Arrivé CRM";
     private static final String TEXT_DEPART = "Départ";
     private static final String TEXT_ENGAGE = "Engagé";
     private static final String TEXT_DESENG = "Désengagement";
 
     private Context ctx;
     private ArrayList<Demand> demands;
     private ListView listView;
     private DemandsAdapter adapter;
     private QuickActionsBar qActionsBar;
     private ArrayList<Pair<String, ArrayList<String>>> qaBarData;
     private IMoyensListener listener;
 
     public MoyensView(Context context) {
         super(context);
         this.ctx = context;
         initUI();
         configUI();
     }
 
     private void initUI() {
         demands = new ArrayList<Demand>();
         listView = new ListView(ctx);
         adapter = new DemandsAdapter(ctx, demands);
         qaBarData = new ArrayList<Pair<String, ArrayList<String>>>();
         for (VehicleSector sector : VehicleSector.values()) {
             ArrayList<String> actions = new ArrayList<String>();
             for (VehicleType type : VehicleType.getValues(sector)) actions.add(type.name());
             qaBarData.add(new Pair<String, ArrayList<String>>(sector.name(), actions));
         }
         qActionsBar = new QuickActionsBar(ctx, qaBarData);
     }
 
     private void configUI() {
         // configuring quickActions bar
         qActionsBar.setId(ID_QA_BAR);
         qActionsBar.setOnActionClickedListener(this);
         RelativeLayout.LayoutParams barParams = new LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
         barParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 
         // configuring the textView displayed when there's no data in the listView
         TextView tv_emptyList = new TextView(ctx);
         tv_emptyList.setText(TEXT_EMPTY_LIST);
         tv_emptyList.setTextSize(20);
         RelativeLayout.LayoutParams emptyTvParams = new LayoutParams(
                 LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         emptyTvParams.addRule(RelativeLayout.CENTER_IN_PARENT);
 
         // configuring the list header view
         String[] headerData = {TEXT_AGRES, TEXT_CIS, TEXT_DEMAND, TEXT_DEPART, TEXT_CRM, TEXT_ENGAGE, TEXT_DESENG};
         ListItemView headerView = new ListItemView(ctx, headerData);
         headerView.setTextColor(Color.WHITE);
         headerView.setId(ID_LIST_HEADER);
         headerView.setBackgroundColor(Color.BLACK);
         RelativeLayout.LayoutParams headerParams = new LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
         headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 
         // configuring list view
         listView.setId(ID_LIST_VIEW);
         listView.setAdapter(adapter);
         listView.setEmptyView(tv_emptyList);
         RelativeLayout.LayoutParams listParams = new LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
         listParams.addRule(RelativeLayout.ABOVE, qActionsBar.getId());
         listParams.addRule(RelativeLayout.BELOW, headerView.getId());
 
         addView(headerView, headerParams);
         addView(listView, listParams);
         addView(tv_emptyList, emptyTvParams);
         addView(qActionsBar, barParams);
     }
 
     /**
      * Change the actual content of the demandList to the one given
      * in parameter
      * @param dlist the new demandList of the view
      */
     public void setDemands(ArrayList<Demand> dlist) {
         this.demands = dlist;
         adapter.notifyDataSetChanged();
     }
 
     /**
      * Adds a resource to the demandList
      * @param d the resource to add
      */
     public void addDemand(Demand d) {
         updateDemand(d);
         listView.setSelection(demands.size()-1);
     }
 
     /**
      * Adds a list of demands to the actual demandList
      * @param dlist the demandList to add
      */
     public void addDemands(ArrayList<Demand> dlist) {
         for (Demand d : dlist) updateDemand(d);
         listView.setSelection(demands.size()-1);
     }
 
     public void updateDemand(Demand d) {
         for (Demand de : demands) {
             if (de.getId().equals(d.getId())) {
                 demands.remove(de);
                 demands.add(d);
                 adapter.notifyDataSetChanged();
                 return;
             }
         }
         demands.add(d);
         adapter.notifyDataSetChanged();
     }
 
     /**
      * Removes a demand from the demandList
      * @param d the demand to remove
      * @return true if the demand has been removed; false otherwise
      */
     public boolean removeDemand(Demand d) {
         boolean ret = this.demands.remove(d);
         adapter.notifyDataSetChanged();
         return ret;
 
     }
 
     @Override
     public void onActionClicked(String tab, String action) {
         if (listener != null) {
             Demand newDemand = new Demand(VehicleType.valueOf(action));
             listener.onDemandAsked(newDemand);
         }
     }
 
     public void addEventListener(IMoyensListener listener) {
         this.listener = listener;
     }
 }

 /**
  * 
  */
 package fr.utc.nf33.ins;
 
 import java.util.List;
 
 import android.app.ExpandableListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import fr.utc.nf33.ins.location.Building;
 import fr.utc.nf33.ins.location.CloseBuildingsService;
 import fr.utc.nf33.ins.location.CloseBuildingsService.LocalBinder;
 import fr.utc.nf33.ins.location.EntryPoint;
 import fr.utc.nf33.ins.location.LocationHelper;
 import fr.utc.nf33.ins.location.LocationIntent;
 import fr.utc.nf33.ins.location.SnrService;
 
 /**
  * 
  * @author
  * 
  */
 public final class EntryPointsActivity extends ExpandableListActivity {
 
   /**
    * 
    * @author
    * 
    */
   private final class ExpandableListViewAdapter extends BaseExpandableListAdapter {
     //
     private final List<Building> mBuildings;
     //
     private final LayoutInflater mInflater;
 
     //
     private ExpandableListViewAdapter(List<Building> buildings) {
       mBuildings = buildings;
       mInflater = LayoutInflater.from(EntryPointsActivity.this);
     }
 
     @Override
     public final EntryPoint getChild(int groupPosition, int childPosition) {
       return mBuildings.get(groupPosition).getEntryPoints().get(childPosition);
     }
 
     @Override
     public final long getChildId(int groupPosition, int childPosition) {
       return childPosition;
     }
 
     @Override
     public final int getChildrenCount(int groupPosition) {
       return mBuildings.get(groupPosition).getEntryPoints().size();
     }
 
     @Override
     public final View getChildView(int groupPosition, int childPosition, boolean isLastChild,
         View convertView, ViewGroup parent) {
       if (convertView == null)
         convertView = mInflater.inflate(R.layout.entry_points_list_child, null);
       EntryPoint ep = getChild(groupPosition, childPosition);
       ((TextView) convertView.findViewById(R.id.entry_points_textview_name)).setText(ep.getName());
       ((TextView) convertView.findViewById(R.id.entry_points_textview_level)).setText(Byte
           .toString(ep.getFloor()));
       ((TextView) convertView.findViewById(R.id.entry_points_textview_lat)).setText(String.format(
           "%.02f", ep.getLatitude()));
       ((TextView) convertView.findViewById(R.id.entry_points_textview_lon)).setText(String.format(
           "%.02f", ep.getLongitude()));
       return convertView;
     }
 
     @Override
     public final Building getGroup(int groupPosition) {
       return mBuildings.get(groupPosition);
     }
 
     @Override
     public final int getGroupCount() {
       return mBuildings.size();
     }
 
     @Override
     public final long getGroupId(int groupPosition) {
       return groupPosition;
     }
 
     @Override
     public final View getGroupView(int groupPosition, boolean isExpanded, View convertView,
         ViewGroup parent) {
       if (convertView == null)
         convertView = mInflater.inflate(R.layout.entry_points_list_group, null);
       ExpandableListView elv = (ExpandableListView) parent;
       elv.expandGroup(groupPosition);
       Building building = getGroup(groupPosition);
       ((TextView) convertView.findViewById(R.id.entry_points_list_group_text)).setText(building
           .getName());
       return convertView;
     }
 
     @Override
     public final boolean hasStableIds() {
       return true;
     }
 
     @Override
     public final boolean isChildSelectable(int groupPosition, int childPosition) {
       return true;
     }
   }
 
   public static final String EXTRA_CHOOSE_ENTRY_POINT = "fr.utc.nf33.ins.CHOOSE_ENTRY_POINT";
 
   //
   private CloseBuildingsService mCloseBuildingsService;
   //
   private boolean mCloseBuildingsServiceBound;
   //
   private ServiceConnection mCloseBuildingsServiceConnection;
   //
   private BroadcastReceiver mNewCloseBuildingsReceiver;
   //
   private BroadcastReceiver mNewSnrReceiver;
   //
   private boolean mSnrServiceBound;
   //
   private ServiceConnection mSnrServiceConnection;
 
   @Override
   public final boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
       int childPosition, long id) {
     if (!mCloseBuildingsServiceBound) return true;
 
     List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
     Intent indoorIntent = new Intent(this, IndoorActivity.class);
     StringBuilder sb = new StringBuilder();
     Building building = buildings.get(groupPosition);
     sb.append(building.getName());
     sb.append("\n");
     sb.append(building.getEntryPoints().get(childPosition).getName());
     indoorIntent.putExtra(LocationIntent.NewCloseBuildings.EXTRA_ENTRY_POINT, sb.toString());
     startActivity(indoorIntent);
 
     return true;
   }
 
   @Override
   protected final void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.activity_entry_points);
 
    String msg = getIntent().getStringExtra(EntryPointsActivity.EXTRA_CHOOSE_ENTRY_POINT);
 
     if (msg != null) {
       ((TextView) findViewById(R.id.activity_entry_points_textview_choose)).setText(msg + "\n\n");
     }
 
     // Remove the Group Indicator.
     getExpandableListView().setGroupIndicator(null);
   }
 
   @Override
   protected final void onStart() {
     super.onStart();
 
     // Connect to the SNR Service.
     Intent snrIntent = new Intent(this, SnrService.class);
     mSnrServiceConnection = new ServiceConnection() {
       @Override
       public final void onServiceConnected(ComponentName name, IBinder service) {
         mSnrServiceBound = true;
       }
 
       @Override
       public final void onServiceDisconnected(ComponentName name) {
         mSnrServiceBound = false;
       }
     };
     bindService(snrIntent, mSnrServiceConnection, Context.BIND_AUTO_CREATE);
 
     // Connect to the Close Buildings Service.
     Intent closeBuildingsIntent = new Intent(this, CloseBuildingsService.class);
     mCloseBuildingsServiceConnection = new ServiceConnection() {
       @Override
       public final void onServiceConnected(ComponentName name, IBinder service) {
         mCloseBuildingsService = ((LocalBinder) service).getService();
 
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         if (buildings == null) return;
         setListAdapter(new ExpandableListViewAdapter(buildings));
 
         mCloseBuildingsServiceBound = true;
       }
 
       @Override
       public final void onServiceDisconnected(ComponentName name) {
         mCloseBuildingsServiceBound = false;
       }
     };
     bindService(closeBuildingsIntent, mCloseBuildingsServiceConnection, Context.BIND_AUTO_CREATE);
 
     // Register receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
 
     mNewCloseBuildingsReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         if (!mCloseBuildingsServiceBound) return;
 
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         if (buildings == null) return;
         setListAdapter(new ExpandableListViewAdapter(buildings));
       }
     };
     lbm.registerReceiver(mNewCloseBuildingsReceiver,
         LocationIntent.NewCloseBuildings.newIntentFilter());
 
     mNewSnrReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         if (!mCloseBuildingsServiceBound) return;
 
         float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         switch (LocationHelper.shouldGoIndoor(snr, buildings)) {
           case ASK_USER:
             break;
           case NO:
             break;
           case YES:
             Intent indoorIntent = new Intent(EntryPointsActivity.this, IndoorActivity.class);
             StringBuilder sb = new StringBuilder();
             Building building = buildings.get(0);
             sb.append(building.getName());
             sb.append("\n");
             sb.append(building.getEntryPoints().get(0).getName());
             indoorIntent
                 .putExtra(LocationIntent.NewCloseBuildings.EXTRA_ENTRY_POINT, sb.toString());
             startActivity(indoorIntent);
             break;
           default:
             break;
         }
       }
     };
     lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());
   }
 
   @Override
   protected final void onStop() {
     super.onStop();
 
     // Unregister receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
     lbm.unregisterReceiver(mNewCloseBuildingsReceiver);
     mNewCloseBuildingsReceiver = null;
     lbm.unregisterReceiver(mNewSnrReceiver);
     mNewSnrReceiver = null;
 
     // Disconnect from the Close Buildings Service.
     if (mCloseBuildingsServiceBound) {
       unbindService(mCloseBuildingsServiceConnection);
       mCloseBuildingsService = null;
       mCloseBuildingsServiceBound = false;
     }
     mCloseBuildingsServiceConnection = null;
 
     // Disconnect from the SNR Service.
     if (mSnrServiceBound) {
       unbindService(mSnrServiceConnection);
       mSnrServiceBound = false;
     }
     mSnrServiceConnection = null;
   }
 }

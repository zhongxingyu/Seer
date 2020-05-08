 package view;
 
 import java.util.ArrayList;
 
 import model.Player;
 import model.Word;
 
 import controller.PlayerController;
 import android.R;
 import android.app.ExpandableListActivity;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 
 public class GameOverView extends ExpandableListActivity {
 
     ExpandableListAdapter mAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
 	    setContentView(tdt4240.ordsplid.R.layout.activity_game_over);
 
 	    Button backButton = (Button) findViewById(tdt4240.ordsplid.R.id.game_over_button);
 	    
 	    backButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 	    });
 
         // Set up our adapter
         mAdapter = new MyExpandableListAdapter();
         setListAdapter(mAdapter);
         registerForContextMenu(getExpandableListView());
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         menu.setHeaderTitle("Menu");
         menu.add(0, 0, 0, "Menu");
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
 
         String title = ((TextView) info.targetView).getText().toString();
         
        /* 
         int type = ExpandableListView.getPackedPositionType(info.packedPosition);
         if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
             int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
             int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
             Toast.makeText(this, title + ": Child " + childPos + " clicked in group " + groupPos,
                     Toast.LENGTH_SHORT).show();
             return true;
         } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
             int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
             Toast.makeText(this, title + ": Group " + groupPos + " clicked", Toast.LENGTH_SHORT).show();
             return true;
         }
        */
         
         return false;
     }
 
    private class MyExpandableListAdapter extends BaseExpandableListAdapter {
         private ArrayList<Player> players = PlayerController.instance().getPlayersSortedByScore();
         
         public Object getChild(int groupPosition, int childPosition) {
             return players.get(groupPosition).getWords().getStatistics().get(childPosition);
         }
 
         public long getChildId(int groupPosition, int childPosition) {
             return childPosition;
         }
 
         public int getChildrenCount(int groupPosition) {
        	return players.get(groupPosition).getWords().getStatistics().size();
         }
 
         public TextView getGenericView() {
             AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64);
 
             TextView textView = new TextView(GameOverView.this);
             textView.setLayoutParams(lp);
             textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
             textView.setPadding(100, 0, 0, 0);
             textView.setTextSize(15);
             return textView;
         }
         
         public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
             TextView textView = getGenericView();
             textView.setText((String) getChild(groupPosition, childPosition));
             return textView;
         }
 
         public Object getGroup(int groupPosition) {
             return players.get(groupPosition);
         }
 
         public int getGroupCount() {
             return players.size();
         }
 
         public long getGroupId(int groupPosition) {
             return groupPosition;
         }
 
         public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
             TextView textView = getGenericView();
             textView.setTextSize(20);
             textView.setText(((Player) getGroup(groupPosition)).getPlayerInfo());
             return textView;
         }
 
         public boolean isChildSelectable(int groupPosition, int childPosition) {
             return false;
         }
 
         public boolean hasStableIds() {
             return true;
         }
 
     }
 }

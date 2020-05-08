 package com.geeksong.agricolascorer;
 
 import com.geeksong.agricolascorer.listadapter.GameHistoryAdapter;
 import com.geeksong.agricolascorer.mapper.GameHistoryMapper;
 import com.geeksong.agricolascorer.model.Game;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 public class HistoryActivity extends Activity {
 	private static final int MENU_DELETE_GAME = 0;
 	private static final int MENU_EDIT_GAME = 1;
 	
 	private static final int RESCORE_ACTIVITY = 0;
 	
 	private GameHistoryMapper historyMapper;
 	private GameHistoryAdapter historyAdapter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_history);
         
         historyMapper = new GameHistoryMapper(this);
         historyAdapter = historyMapper.getListAdapter();
         
         checkNoHistoryLabelVisibility();
 
         ExpandableListView list = (ExpandableListView) this.findViewById(R.id.historyList);
         list.setAdapter(historyAdapter);
         
         registerForContextMenu(list);
     }
     
     private void checkNoHistoryLabelVisibility() {
     	if(!historyAdapter.isEmpty())
     		return;
     	
     	TextView noHistoryLabel = (TextView) this.findViewById(R.id.noHistoryLabel);
     	noHistoryLabel.setVisibility(View.VISIBLE);
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     	menu.add(Menu.NONE, MENU_DELETE_GAME, Menu.NONE, R.string.delete_game);
     	menu.add(Menu.NONE, MENU_EDIT_GAME, Menu.NONE, R.string.edit_game);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
     	int position = (int) info.id;
     	Game game = (Game) historyAdapter.getGroup(position);
     	
     	switch(item.getItemId()) {
     		case MENU_DELETE_GAME:
     			historyMapper.deleteGame(game);
     			historyAdapter.deleteGame(game);
     			checkNoHistoryLabelVisibility();
     			return true;
     		case MENU_EDIT_GAME:
     			if(game.hasScoresWithOnlyTotalScore()) {
     				new AlertDialog.Builder(this)
     					.setTitle(R.string.can_not_edit)
     					.setMessage(R.string.no_detail_scores)
     					.setPositiveButton(R.string.ok, null)
     					.show();
     				return true;
     			}
     			
     			GameCache.getInstance().setGame(game);
     			Intent rescore = new Intent(this, ScorePlayersActivity.class);
     			startActivityForResult(rescore, RESCORE_ACTIVITY);
     			
     			return true;
     		default:
     	    	return super.onContextItemSelected(item);
     	}
     }
     
     public void expandCollapseAll(View source) {
     	Button expandCollapseAll = (Button) source;
     	String text = expandCollapseAll.getText().toString();
     	ExpandableListView list = (ExpandableListView) this.findViewById(R.id.historyList);
     	String expandAll = getResources().getString(R.string.expand_all);
     	String collapseAll = getResources().getString(R.string.collapse_all);
 		boolean expanding = text.equals(expandAll);
 
 		for(int i = 0; i < historyAdapter.getGroupCount(); i++) {
     		if(expanding)
 	    		list.expandGroup(i);
     		else
 	    		list.collapseGroup(i);
     	}
     			
 	    expandCollapseAll.setText(expanding ? collapseAll : expandAll);
     }
 }

 package com.utc.cards.table.view;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.utc.cards.R;
 import com.utc.cards.model.game.AbstractGame;
 import com.utc.cards.model.game.IGame;
 import com.utc.cards.model.player.HumanPlayer;
 import com.utc.cards.table.TableController;
 
 public class TableLaunchGameActivity extends Activity implements
 	PropertyChangeListener
 {
 
     private static Logger _log = LoggerFactory
 	    .getLogger(TableLaunchGameActivity.class);
     private IGame _selectedGame;
     private Point _screenDimention = new Point();
     private ListView _listView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.activity_launch_game);
 
 	getScreenSize();
 
 	_selectedGame = TableController.getInstance().getGame();
 	_selectedGame.registerListener(this);
 
 	setSelectedGameLogoAndLabel();
 
 	updatePlayerList();
 
 	// Test pour affichage liste des joueurs
 	_selectedGame.addPlayer(new HumanPlayer("Benoit"));
 	_selectedGame.addPlayer(new HumanPlayer("Bobby"));
 	_selectedGame.addPlayer(new HumanPlayer("Benoit 2"));
 	_selectedGame.addPlayer(new HumanPlayer("Bobby 2"));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
 	// Inflate the menu; this adds items to the action bar if it is present.
 	getMenuInflater().inflate(R.menu.launch_game, menu);
 	return true;
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event)
     {
 	if (event.getPropertyName().equals(AbstractGame.PLAYER_LIST_UPDATED))
 	{
 	    _log.debug("Event received: " + event.getPropertyName());
 	    updatePlayerList();
 	}
     }
 
     private void getScreenSize()
     {
 	Display display = getWindowManager().getDefaultDisplay();
 	display.getSize(_screenDimention);
     }
 
     public void updatePlayerList()
     {
 	if (_listView == null)
 	{
 	    _listView = (ListView) findViewById(R.id.playerList);
 
 	    _listView
 		    .setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent,
 				final View view, int position, long id)
 			{
 			    _log.debug("Player clicked: "
 				    + _selectedGame.getPlayers().get(position)
 					    .getName());
 			}
 		    });
 
 	    double w = _screenDimention.x * 0.3;
 	    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
 		    (int) w, LayoutParams.WRAP_CONTENT);
 
 	    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    lp.topMargin = 15;
	    lp.bottomMargin = 100;
 
 	    _listView.setLayoutParams(lp);
 	}
 
 	final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 		android.R.layout.simple_list_item_activated_1,
 		_selectedGame.getPlayerNames());
 
 	_log.debug("Nb Players:" + _selectedGame.getPlayerNames().size());
 
 	_listView.setAdapter(adapter);
 
     }
 
     public void setSelectedGameLogoAndLabel()
     {
 	ImageView img = (ImageView) findViewById(R.id.selectedGameLogo);
 
 	Drawable tmp = getApplicationContext().getResources().getDrawable(
 		_selectedGame.getLogoResource());
 
 	double diff = (double) tmp.getIntrinsicHeight()
 		/ (double) tmp.getIntrinsicWidth();
 
 	double w = _screenDimention.x * 0.25;
 	double h = w * diff;
 
 	img.setBackgroundResource(_selectedGame.getLogoResource());
 
 	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) w,
 		(int) h);
 
 	lp.gravity = Gravity.CENTER_VERTICAL;
 
 	img.setLayoutParams(lp);
 
 	TextView text = (TextView) findViewById(R.id.selectedGameLabel);
 
 	text.setText(_selectedGame.getName());
 
 	LinearLayout container = (LinearLayout) findViewById(R.id.selectedGameInfo);
 
 	RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
 		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 
 	lp2.leftMargin = (int) (_screenDimention.x * 0.25);
 	lp2.topMargin = (int) (_screenDimention.y * 0.1);
 
 	container.setLayoutParams(lp2);
 
     }
 
     public void launchGameClick(View view)
     {
 	_log.debug("Lancement d'une partie");
 
 	TableController.getInstance().launchGame();
 
 	Intent intent = new Intent(this, _selectedGame.getTableGameActivity());
 	startActivity(intent);
     }
 
     public void optionsClick(View view)
     {
 	_log.debug("Options");
 
     }
 }

 package edu.channel4.mm.db.android.activity;
 
 import java.util.ArrayList;
 
 import org.achartengine.GraphicalView;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectExtra;
 import roboguice.inject.InjectView;
 import android.app.ActionBar;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ProgressBar;
 
 import com.google.inject.Inject;
 
 import edu.channel4.mm.db.android.R;
 import edu.channel4.mm.db.android.model.callback.GraphLoadCallback;
 import edu.channel4.mm.db.android.model.graph.Graph;
 import edu.channel4.mm.db.android.model.graph.GraphFactory;
 import edu.channel4.mm.db.android.model.graph.GraphType;
 import edu.channel4.mm.db.android.model.request.GraphRequest;
 import edu.channel4.mm.db.android.model.request.GraphRequest.TimeScope;
 import edu.channel4.mm.db.android.util.Keys;
 import edu.channel4.mm.db.android.util.Log;
 
 /**
  * This Activity is now a glorified loading screen.
  * 
  * @author girum
  */
 @ContentView(R.layout.activity_graph_viewer)
 public class GraphViewerActivity extends RoboActivity implements
          GraphLoadCallback {
 
    @InjectExtra(Keys.GRAPH_REQUEST_EXTRA) private GraphRequest graphRequest;
    @Inject private GraphFactory graphFactory;
    private Graph graph = null;
 
    @InjectView(R.id.progressBarGraphViewer) private ProgressBar progressBar;
    @InjectView(R.id.graphViewFrame) private FrameLayout graphViewFrame;
 
    @Inject private ArrayList<GraphType> validGraphTypes;
    private GraphicalView graphView;
    private Menu menu;
    private ActionBar actionBar;
    private GraphType currentType;
    private TimeScope currentScope;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       actionBar = getActionBar();
       actionBar.hide();
      currentScope = graphRequest.getTimeScope();
      graphFactory.getGraph(graphRequest, this);      
    }
 
    @Override
    protected void onResume() {
       super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.graphviewmenu, menu);
       
       this.menu = menu;
       
       MenuItem item = menu.findItem(R.id.menu_time_day);
       item.setChecked(true);
       
       return super.onCreateOptionsMenu(menu);
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case R.id.menu_switch_graph_type:
             // draw the graph using the other chart type
             switchGraphTypeViewed();
             break;
          case R.id.menu_time_day:
             changeTimeScope(TimeScope.DAY);
             break;
          case R.id.menu_time_month:
             changeTimeScope(TimeScope.MONTH);
             break;
          case R.id.menu_time_year:
             changeTimeScope(TimeScope.YEAR);
             break;
          case R.id.menu_time_decade:
             changeTimeScope(TimeScope.DECADE);
             break;
          case R.id.menu_time_custom:
             Log.toastD(getApplicationContext(), "Custom Time Range Requested!");
          default:
             break;
       }
 
       return super.onOptionsItemSelected(item);
    }
    
    private void switchGraphTypeViewed() {
       if (currentType == GraphType.BAR) {
          currentType = GraphType.PIE;
       } else {
          currentType = GraphType.BAR;
       }
       
       GraphicalView tempView = graph.getView(currentType, getApplicationContext());
       if (tempView != null) {
          graphViewFrame.removeAllViews();
          graphView = tempView;
          graphViewFrame.addView(graphView);
          graphView.repaint();
          
          setSwitchText();
       }
       else {
          Log.d("Null GraphicalView " + currentType.name() + " tried to be displayed");
       }
       
       // TODO: Switch the icon displayed on the action bar
    }
    
    private void changeTimeScope(TimeScope scope) {
       // if the scope selected is different than the current scope
       if (scope != currentScope) {
          // change scope of graphRequest
          graphRequest.setTimeScope(scope);
          
          // change current scope
          currentScope = scope;
          
          // hide action bar
          actionBar.hide();
          
          // placeholder visibility set to GONE
          graphViewFrame.setVisibility(View.GONE);
          
          // show progress bar
          progressBar.setVisibility(View.VISIBLE);
          
          // start the request for the graph again
          graphFactory.getGraph(graphRequest, this);
       }
    }
    
    private void setSwitchText() {
       // Set the "Switch Graph Type" button to display proper text
       String switchText = "Switch to ";
       switchText += (currentType == GraphType.BAR) ? "Pie" : "Bar";
       
       MenuItem item = menu.findItem(R.id.menu_switch_graph_type);
       item.setTitle(switchText);
    }
 
    
    @Override
    public void onGraphLoaded(Graph graph) {
       Log.i("Graph loaded: " + graph.getTitle());
       this.graph = graph;
 
       // Fill up the validGraphTypes array
       validGraphTypes.clear();
       validGraphTypes.addAll(graph.getValidGraphTypes());
       
       // choose a default graphType
       currentType = validGraphTypes.get(0);
       if (validGraphTypes.size() > 1) {
          currentType = GraphType.PIE;
       }
 
       // Hide the ProgressBar
       progressBar.setVisibility(View.GONE);
 
       // Show the other Views
       graphView = graph.getView(currentType, getApplicationContext());
      graphViewFrame.removeAllViews();
       graphViewFrame.setVisibility(View.VISIBLE);
       graphViewFrame.addView(graphView);
       
       // set the action bar title, graph type, and scope
       actionBar.show();
       actionBar.setTitle(graph.getTitle());
       
       if (validGraphTypes.size() == 1) { // It's a line graph
          // TODO: display the icon of the currently displayed type on the action bar
          // disable the "Switch Graph Type" button
          MenuItem item = menu.findItem(R.id.menu_switch_graph_type);
          item.setVisible(false);
          item.setEnabled(false);
       }
       else {
          // TODO: display the icon of the currently displayed type on the action bar
 
          setSwitchText();
       }
    }
 
 }

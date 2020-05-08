 package com.bryanestrada.waitforit;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.RejectedExecutionException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.bryanestrada.androidutils.AsynchronousGuiHelper;
 import com.bryanestrada.waitforit.data.DataAccessor;
 import com.flurry.android.FlurryAgent;
 import com.nextbus.webservices.Prediction;
 import com.nextbus.webservices.PredictionResultHandler;
 import com.nextbus.webservices.PredictionTask;
 
 /**
  * @author <a href="mailto:bryan@bryanestrada.com">Bryan Estrada</a>
  */
 public class Main extends ListActivity implements PredictionResultHandler
 {
    private static final String TAG = "Main";
 
    private final Button[] _selectedStack = new Button[3];
    private final long[] _selectedIds = new long[3];
    private final String[] _selectedTags = new String[3];
    private final int[] _selectedPosition = new int[3];
    
    private static final int ROUTE = 0;
    private static final int DIRECTION = 1;
    private static final int STOP = 2;
    private static final int DONE = 3;
    
    private ListView _selectionList;
    private int _listState;
    
    private TextView _prediction;
    private View _throbbler;
    private View _result;
    
    private DataAccessor _db;
    private Cursor _cursor;
    
    private Handler _guiThread;
    private ExecutorService _predictionThread;
    private Runnable _updateTask;
    private Future _predictionPending;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
       _db = new DataAccessor(this);
       try
       {
          _db.createDatabase();
       }
       catch (IOException e)
       {
          Log.e(TAG, "couldn't initialize database", e);
          if (null != _db)
             _db.close();
 
          _db = null;
          return;
       }
       _db.open();

       super.onCreate(savedInstanceState);
       this.setContentView(R.layout.main);
       
       initializeThreading();
       findViews();
       setListeners();
       
       _cursor = _db.getAllRoutes();
       startManagingCursor(_cursor);
       setListAdapter(new TransitListAdapter(this, _cursor));
       _listState = ROUTE;
    }
    
    private void findViews()
    {
       _selectedStack[ROUTE] = (Button) findViewById(R.id.route_selection);
       _selectedStack[DIRECTION] = (Button) findViewById(R.id.direction_selection);
       _selectedStack[STOP] = (Button) findViewById(R.id.stop_selection);
       _selectedIds[STOP] = _selectedIds[DIRECTION] = _selectedIds[ROUTE] = -1;
       
       _selectionList = (ListView) findViewById(android.R.id.list);
       _prediction = (TextView) findViewById(R.id.prediction);
       _throbbler = findViewById(R.id.throbbler);
       _result = findViewById(R.id.result);
    }
    
    private void onClickStack(View view)
    {
       if (null != _predictionPending)
          _predictionPending.cancel(true);
       
       _throbbler.setVisibility(View.GONE);
       _result.setVisibility(View.GONE);
       
       _listState = -1;
       switch (view.getId())
       {
       case R.id.route_selection:
          _listState = ROUTE;
          swapListCursor(_db.getAllRoutes());
          break;
       case R.id.direction_selection:
          _listState= DIRECTION;
          swapListCursor(_db.getDirections(_selectedIds[ROUTE]));
          break;
       case R.id.stop_selection:
          _listState = STOP;
          swapListCursor(_db.getStops(_selectedIds[DIRECTION]));
          break;
       }
       
       if (_listState >= 0)
       {
          Animation pullLeft = AnimationUtils.loadAnimation(this, R.anim.pull_left);
          Animation appear = AnimationUtils.loadAnimation(this, R.anim.appear);
          for (int i = _listState; i <= STOP; i++)
          {
             _selectedIds[i] = -1;
             _selectedTags[i] = null;
          }
          
          for (int i = STOP; i >= _listState; i--)
          {
             if (View.VISIBLE == _selectedStack[i].getVisibility())
                _selectedStack[i].startAnimation(pullLeft);
             
             _selectedStack[i].setVisibility(View.GONE);
          }
          _selectionList.setSelection(_selectedPosition[_listState]);
          _selectionList.setVisibility(View.VISIBLE);
          _selectionList.startAnimation(appear);
       }
    }
    
    private void setListeners()
    {
       OnClickListener stackViewListener = new OnClickListener()
       {
          @Override
          public void onClick(View view)
          {
             Main.this.onClickStack(view);
          }
       };
       for (int i = 0; i < _selectedStack.length; i++)
          _selectedStack[i].setOnClickListener(stackViewListener);
    }
    
    /**
     * initialize the second thread. therea are two: 1) for the main thread that 
     * is running this program and 2) for the web request thing that is used to 
     * invoke the nextbus webservice in the background.
     */
    private void initializeThreading()
    {
       _guiThread = new Handler();
       _predictionThread = Executors.newSingleThreadExecutor();
       
       _updateTask = new Runnable()
       {
          @Override
          public void run()
          {
             String routeTag = _selectedTags[ROUTE];
             String stopTag = _selectedTags[STOP];
             
             // cancel the previous prediction if there was one
             if (null != _predictionPending)
                _predictionPending.cancel(true);
             
             // let the user know we're doing something
             _throbbler.setVisibility(View.VISIBLE);
             _result.setVisibility(View.GONE);
             
             // begin prediction, but don't wait for it
             try
             {
                PredictionTask predict = new PredictionTask(Main.this, routeTag, stopTag);
                _predictionPending = _predictionThread.submit(predict);
             }
             catch (RejectedExecutionException e)
             {
                Log.e(TAG, e.getMessage(), e);
                _throbbler.setVisibility(View.GONE);
                _prediction.setTextAppearance(Main.this, R.style.ResultTextError);
                _prediction.setText(R.string.prediction_error);
             }
          }
          
       };
    }
    
    /**
     * Safely swap out the cursor that is backing this page's list by closing the
     * current one and replacing it with the given one
     * @param c the cursor to swap with
     */
    private void swapListCursor(Cursor c)
    {
       this.stopManagingCursor(_cursor);
       setListAdapter(new TransitListAdapter(this, c));
       _cursor.close();
       _cursor = c;
       this.startManagingCursor(_cursor);
    }
    
    private void swapListCursor(Cursor c, float lat, float lon)
    {
 	   this.stopManagingCursor(_cursor);
       setListAdapter(new TransitListAdapter(this, c, lat, lon));
       _cursor.close();
       _cursor = c;
       this.startManagingCursor(_cursor);
    }
 
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
       _selectedIds[_listState] = id;
       _selectedPosition[_listState] = position;
       _selectedTags[_listState] = (String) l.getItemAtPosition(position);
 
       if (v instanceof LinearLayout)
       {
          TextView selected = (TextView) v.findViewById(R.id.name);
 
          _selectedStack[_listState].setText(selected.getText());
          _selectedStack[_listState].setVisibility(View.VISIBLE);
          
          Animation push = AnimationUtils.loadAnimation(this, R.anim.push_right);
          Animation appear = AnimationUtils.loadAnimation(this, R.anim.appear);
          _selectedStack[_listState].startAnimation(push);
          switch (_listState)
          {
          case ROUTE:
             _selectionList.setVisibility(View.VISIBLE);
             _selectionList.startAnimation(appear);
             //_selectionList.startAnimation(push);
             swapListCursor(_db.getDirections(id));
             _listState = DIRECTION;
             break;
          case DIRECTION:
             _selectionList.setVisibility(View.VISIBLE);
             _selectionList.startAnimation(appear);
             //_selectionList.startAnimation(push);
             swapListCursor(_db.getStops(id), 0.0f, 0.0f);
             _listState = STOP;
             break;
          case STOP:
             _selectionList.setVisibility(View.GONE);
             _listState = DONE;
             showPrediction(appear);
             break;
          }
       }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
       boolean result = false;
       
       switch (item.getItemId())
       {
       case R.id.about:
          startActivity(new Intent(this, About.class));
          result = true;
          break;
       }
       
       return result;
    }
    
    private void showPrediction(Animation animation)
    {
       // first replace the text to indicate wait for it, then spin off a thread 
       // that will change it when it's finally done.
       _throbbler.setVisibility(View.VISIBLE);
       _throbbler.startAnimation(animation);
       
       // now queue an update for the prediction text
       _guiThread.removeCallbacks(_updateTask);
       _guiThread.postDelayed(_updateTask, 200);
    }
 
    @Override
    public void setPredictionResult(Iterable<Prediction> result)
    {
       StringBuilder b = new StringBuilder();
       for (Prediction p : result)
       {
          b.append(p.toString());
          b.append('\n');
       }
       String predict = b.toString();
       AsynchronousGuiHelper.getInstance().guiSetVisibility(this, _throbbler, View.GONE);
       if (predict.length() > 0)
       {
           AsynchronousGuiHelper.getInstance().guiSetText(this, _prediction, predict, R.style.ResultText);
       }
       else
       {
           AsynchronousGuiHelper.getInstance().guiSetText(this, _prediction, getString(R.string.prediction_error), R.style.ResultTextError);
       }
       AsynchronousGuiHelper.getInstance().guiSetVisibility(this, _result, View.VISIBLE);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
       boolean result = false;
       if (KeyEvent.KEYCODE_BACK == keyCode)
       {
          if (_listState >= DIRECTION)
          {
             this.onClickStack(_selectedStack[_listState-1]);
             result = true;
          }
       }
       return result ? result : super.onKeyDown(keyCode, event);
    }
 
    // now the analytics code
    
    @Override
    public void onStart()
    {
       super.onStart();
       FlurryAgent.onStartSession(this, "F3K5J4U43UBN55HPVJC3");
    }
    
    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
 }

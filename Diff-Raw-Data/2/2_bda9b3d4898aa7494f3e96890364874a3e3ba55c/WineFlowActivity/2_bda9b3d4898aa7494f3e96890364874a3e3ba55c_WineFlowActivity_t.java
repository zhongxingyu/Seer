 package ws.wiklund.vinguiden.activities;
 
 import java.io.File;
 
 import ws.wiklund.guides.activities.BaseActivity;
 import ws.wiklund.guides.model.Beverage;
 import ws.wiklund.guides.util.CoverFlow;
 import ws.wiklund.guides.util.CoverFlowAdapter;
 import ws.wiklund.guides.util.ExportDatabaseCSVTask;
 import ws.wiklund.guides.util.GetBeverageFromCursorTask;
 import ws.wiklund.guides.util.Notifyable;
 import ws.wiklund.guides.util.Selectable;
 import ws.wiklund.guides.util.SelectableAdapter;
 import ws.wiklund.guides.util.ViewHelper;
 import ws.wiklund.vinguiden.R;
 import ws.wiklund.vinguiden.db.WineDatabaseHelper;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.TextView;
 
 public class WineFlowActivity extends BaseActivity implements Notifyable {
 	private CoverFlowAdapter adapter;
 	private SelectableAdapter selectableAdapter;
 	private WineDatabaseHelper helper;
 	private int currentPosition;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
 			startActivity(new Intent(getApplicationContext(), WineListActivity.class));
 		} else {
 			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 			setContentView(R.layout.flow);
 			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
 	
 			helper = new WineDatabaseHelper(this);			
 			
 			final CoverFlow flow = (CoverFlow) findViewById(R.id.coverFlow);
 			adapter = new CoverFlowAdapter(this, helper);
 	
 			flow.setAdapter(adapter);
 	
 			flow.setSpacing(-25);
 			flow.setSelection(adapter.getOptimalSelection(), true);
 			flow.setAnimationDuration(1000);
 			
 			flow.setOnItemLongClickListener(new OnItemLongClickListener() {
 				@Override
 				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
 					handleLongClick(position);
 					return true;
 				}
 			});
 			
 			flow.setOnItemClickListener(new OnItemClickListener() {
 	
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					new GetBeverageFromCursorTask(WineFlowActivity.this, WineActivity.class).execute(adapter.getItem(position));
 				}
 				
 			});
 			
 			selectableAdapter = new SelectableAdapter(this, R.layout.spinner_row, getLayoutInflater()){
 				public boolean isAvailableInCellar() {
 					final Beverage b = ViewHelper.getBeverageFromCursor(adapter.getItem(currentPosition));
 					return b.hasBottlesInCellar();
 				}
 			};
 			
 			selectableAdapter.add(new Selectable(getString(R.string.addToCellar), R.drawable.icon, Selectable.ADD_ACTION));
 			selectableAdapter.add(new Selectable(getString(R.string.removeFromCellar), R.drawable.from_cellar, Selectable.REMOVE_ACTION));
 			selectableAdapter.add(new Selectable(getString(R.string.deleteTitle), R.drawable.trash, Selectable.DELETE_ACTION));
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.beverage_list_menu, menu);
 
 		return true;
 	}
 		
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.findItem(R.id.menuStats).setEnabled(hasSomeStats());
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuStats:
 			startActivity(new Intent(getApplicationContext(), StatsActivity.class));
 			break;
 		case R.id.menuExport:
 			final AlertDialog alertDialog = new AlertDialog.Builder(WineFlowActivity.this).create();
 			alertDialog.setTitle(getString(R.string.export));
 			
 			final File exportFile = new File(ViewHelper.getRoot(), "export_guide.csv");
 			alertDialog.setMessage(String.format(getString(R.string.export_message), new Object[]{exportFile.getAbsolutePath()}));
 			
 			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 			       new ExportDatabaseCSVTask(WineFlowActivity.this, helper, exportFile, adapter.getCount()).execute();
 				} 
 			});
 			
 			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					alertDialog.cancel();
 				} 
 			});
 
 			alertDialog.setCancelable(true);
 			alertDialog.setIcon(R.drawable.export);
 			alertDialog.show();
 			break;
 		case R.id.menuAbout:
 			startActivity(new Intent(getApplicationContext(), AboutActivity.class));
 			break;
 		}
 
 		return true;
 	}
 	
 	public void addBeverage(View view) {
     	Intent intent = new Intent(view.getContext(), AddWineActivity.class);
     	startActivityForResult(intent, 0);
     }
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		notifyDataSetChanged();
 	}
 
 	private boolean hasSomeStats() {
		return adapter != null && adapter.getCount() > 0;
 	}
 	
 	private void handleLongClick(final int position) {
 		currentPosition = position;
 		
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		
 		final Beverage b = ViewHelper.getBeverageFromCursor(adapter.getItem(position));
 		alertDialog.setTitle(b != null ? b.getName() : "");
 		
 		alertDialog.setSingleChoiceItems( selectableAdapter, 0, new OnClickListener() { 
             @Override 
             public void onClick(DialogInterface dialog, int which) { 
                 dialog.dismiss();
                 ((Selectable) selectableAdapter.getItem(which)).select(WineFlowActivity.this, helper, b);
             }
 		}); 
 
 		alertDialog.show(); 				
 	}	
 
 	public void notifyDataSetChanged() {
 		int bottles = -1;
 		if (helper != null) {
 			bottles = helper.getNoBottlesInCellar();
 		}
 		
 		// Update title with no wines in cellar
 		if (bottles > 0) {
 			TextView view = (TextView) WineFlowActivity.this.findViewById(R.id.title);
 
 			String text = view.getText().toString();
 			if (text.contains("(")) {
 				text = text.substring(0, text.indexOf("(") - 1);
 			}
 
 			view.setText(text + " (" + bottles + ")");
 		}
 		
 		if (adapter != null) {
 			adapter.notifyDataSetChanged();
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {		
 		if (adapter != null) {
 			adapter.destroy();
 		}
 		
 		super.onDestroy();
 	}
 
 }
 
 
 
 

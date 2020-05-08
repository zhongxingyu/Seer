 package es.udc.choveduro.ifttd.types;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 
 import es.udc.choveduro.ifttd.DashboardActivity;
 import es.udc.choveduro.ifttd.EasyActivity;
 import es.udc.choveduro.ifttd.R;
 import es.udc.choveduro.ifttd.service.OwlService;
 
 public abstract class ConfigurablesListActivity<T extends Configurable> extends
 		EasyActivity {
 
 	public static final String LOG_NAME = DashboardActivity.class.getName();
 	protected OwlService mService;
 
 	ArrayList<T> loadedItems = new ArrayList<T>();
 	ItemAdapter<T> loadedItemsAdapter = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.cond_or_cons_list);
 		ListView l = (ListView) findViewById(R.id.configurable_list);
 
 		loadedItemsAdapter = new ItemAdapter<T>(this,
 				R.layout.cond_or_cons_item, loadedItems);
 		l.setAdapter(loadedItemsAdapter);
 		l.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> listView, View child,
 					int position, long id) {
 
 				Log.i(LOG_NAME, new StringBuffer("Clicked action at position ")
 						.append(Integer.toString(position)).toString());
 				onClickedAction(position);
 			}
 		});
 	}
 
 	@Override
 	public void onServiceConnected(OwlService service) {
 		mService = service;
 		Log.w(LOG_NAME, "Connected to service.");
 		Log.e(LOG_NAME, "Service has this arraylist: " + fetchFromService());
		loadedItemsAdapter.addAll(fetchFromService());
 	}
 
 	/**
 	 * @return the items got from the server
 	 */
 	abstract protected ArrayList<T> fetchFromService();
 
 	/**
 	 * Make server aware of the selected position on graceful exit.
 	 * 
 	 * @param position
 	 */
 	abstract protected void tellService(int position);
 
 	protected void tellUnwind() {
 		mService.unwind();
 	}
 
 	/**
 	 * @return the items got by the server previously
 	 */
 	private ArrayList<T> getItems() {
 		if (mService != null)
 			return fetchFromService();
 		else
 			return null;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.a, menu);
 		return true;
 	}
 
 	public static class ItemAdapter<T> extends ArrayAdapter<T> {
 
 		final static class ViewHolder {
 			TextView name;
 			ImageView img;
 			ImageButton arrow;
 		}
 
 		private ConfigurablesListActivity<? extends Configurable> ctx;
 
 		public ItemAdapter(
 				ConfigurablesListActivity<? extends Configurable> context,
 				int textViewResourceId, ArrayList<T> arrayList) {
 			super(context, textViewResourceId, arrayList);
 			this.ctx = context;
 		}
 
 		public View getView(final int position, View convertView,
 				ViewGroup parent) {
 			View item = convertView;
 			ViewHolder holder;
 
 			if (item == null) {
 				LayoutInflater inflater = (LayoutInflater) getContext()
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				item = inflater.inflate(R.layout.cond_or_cons_item, null);
 
 				holder = new ViewHolder();
 				holder.name = (TextView) item.findViewById(R.id.name_item);
 				holder.img = (ImageView) item.findViewById(R.id.image_item);
 				holder.arrow = (ImageButton) item.findViewById(R.id.arrow_item);
 
 				item.setTag(holder);
 			} else {
 				holder = (ViewHolder) item.getTag();
 			}
 
 			holder.arrow.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					Toast.makeText(ctx, "Pressed arrow", Toast.LENGTH_SHORT)
 							.show();
 
 					Log.i(LOG_NAME, new StringBuffer("Clicked ARROW at position ")
 							.append(Integer.toString(position)).toString());
 					ctx.onClickedAction(position);
 				}
 
 			});
 		
 			holder.name.setText(ctx.loadedItems.get(position).getName());
 			holder.img.setImageResource(ctx.loadedItems.get(position)
 					.getImageResource());
 
 			return (item);
 		
 		}
 		
 	}
 
 	public static class Callback implements CallbackIF {
 		private ConfigurablesListActivity<? extends Configurable> ctx;
 		private int position;
 
 		public Callback(ConfigurablesListActivity<? extends Configurable> ctx,
 				int position) {
 			this.ctx = ctx;
 			this.position = position;
 		}
 
 		@Override
 		public void resultOK(String resultString, Bundle resultMap) {
 			ctx.tellService(position);
 			Toast.makeText(ctx, "Got configuration result OK",
 					Toast.LENGTH_SHORT).show();
 			ctx.setResult(RESULT_OK,
 					ctx.getIntent().putExtra("position", position));
 
 		}
 
 		@Override
 		public void resultCancel(String resultString, Bundle resultMap) {
 			ctx.setResult(RESULT_CANCELED);
 			ctx.finish();
 		}
 	}
 
 	protected void onClickedAction(int position) {
 		Log.i(LOG_NAME,
 				new StringBuffer("Clicked action at position ").append(
 						Integer.toString(position)).toString());
 		getItems().get(position).configure(this, new Callback(this, position));
 	}
 
 }

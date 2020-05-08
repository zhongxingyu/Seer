 package ar.com.ceritdumbre.com.android.apps.memoryhelper.adapter;
 
import java.sql.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import ar.com.ceritdumbre.com.android.apps.memoryhelper.R;
 import ar.com.ceritdumbre.com.android.apps.memoryhelper.model.Memory;
 
 public class MemoryAdapter extends ArrayAdapter<Memory> {
 
 	private List<Memory> items;
 
 	private int textViewResourceId;
 
 	public MemoryAdapter(Context context, int newTextViewResourceId,
 			List<Memory> newItems) {
 		super(context, newTextViewResourceId, newItems);
 		this.items = newItems;
 		this.textViewResourceId = newTextViewResourceId;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (view == null) {
 			LayoutInflater inflater = ((Activity) super.getContext())
 					.getLayoutInflater();
 			view = inflater.inflate(textViewResourceId, parent, false);
 		}
 
 		Memory memory = items.get(position);
 		if (memory != null) {
 			TextView memoryTextView = (TextView) view
 					.findViewById(R.id.memory_row_TextView);
 			TextView creationDateTextView = (TextView) view
 					.findViewById(R.id.creationDate_row_TextView);
 			TextView idTextView = (TextView) view
 					.findViewById(R.id.id_row_TextView);
			idTextView.setText(convertView.getResources()
					.getString(R.string.id) + ":" + memory.getId());
 			memoryTextView.setText(memory.getMemory());
			creationDateTextView.setText(convertView.getResources().getString(
					R.string.creation_date)
					+ ":" + new Date(memory.getCreationDate()));
 		}
 		return view;
 	}
 
 }

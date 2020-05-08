 package ceu.marten.ui.adapters;
 
import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 import ceu.marten.bplux.R;
 
 public class ActiveChannelsListAdapter extends ArrayAdapter<String> implements
 		OnCheckedChangeListener, OnItemSelectedListener {
 
 	private final Context context;
	private List<String> strings;
 	private String[] sensorsChecked = null;
 
	public ActiveChannelsListAdapter(Context context, List<String> strings) {
 		super(context, 0, 0, strings);
 		this.context = context;
 		this.strings = strings;
 		this.sensorsChecked = new String[strings.size()];
 
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		ViewGroup rowView = (ViewGroup) convertView;
 		if (rowView == null) {
 			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
 					R.layout.li_active_channels, parent, false);
 
 		}
 
 		CheckBox cb = (CheckBox) rowView.findViewById(R.id.li_ac_checkbox);
 		cb.setTag(R.id.TAG_POSITION, position);
 		cb.setTag(R.id.TAG_SENSORS, rowView.findViewById(R.id.li_ac_sensors));
 		cb.setOnCheckedChangeListener(this);
 
 		Spinner spinner = (Spinner) rowView.findViewById(R.id.li_ac_sensors);
 		spinner.setTag(position);
 		spinner.setOnItemSelectedListener(this);
 
 		TextView channelNumber = (TextView) rowView
 				.findViewById(R.id.li_ac_channelNumber);
 		String currentItem = strings.get(position);
 		channelNumber.setText(currentItem);
 
 		return rowView;
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		if (isChecked) {
 			int position = (Integer) buttonView.getTag(R.id.TAG_POSITION);
 			Spinner sp = (Spinner) buttonView.getTag(R.id.TAG_SENSORS);
 			sensorsChecked[position] = sp.getSelectedItem().toString();
 
 		} else {
 			int position = (Integer) buttonView.getTag(R.id.TAG_POSITION);
 			sensorsChecked[position] = null;
 		}
 
 	}
 
 	public String[] getChecked() {
 		return sensorsChecked;
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> spinner, View currentText,
 			int pos, long id) {
 
 		TextView t = (TextView) currentText;
 		if (t.getText().toString().compareTo(context.getString(R.string.nc_active_channels_list_adapter_comparation)) != 0) {
 			int position = (Integer) spinner.getTag();
 			sensorsChecked[position] = ((Spinner) spinner).getSelectedItem()
 					.toString();
 		}
 
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> spinner) {
 
 	}
 
 }

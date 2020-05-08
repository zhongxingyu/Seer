 package co.lab4u.instruments.adapters;
 
 import java.util.List;
 
 import co.lab4u.instruments.R;
 import co.lab4u.instruments.models.ILaboratory;
 import co.lab4u.instruments.models.Laboratory;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class LabItemAdapter extends ArrayAdapter<ILaboratory> {
 	private final Context context;
 	private final List<ILaboratory> values;
 	
 	public LabItemAdapter(Context context, List<ILaboratory> values) {
 		super(context, R.layout.activity_lab_finder_item, values);
 	    this.context = context;
 	    this.values = values;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	  
 		// let's inflate our xml
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View rowView = inflater.inflate(R.layout.activity_lab_finder_item, parent, false);
 		
 		// gets our widgets from the view
 		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
 		TextView textViewTitle = (TextView) rowView.findViewById(R.id.labTitle);
 		TextView textViewContent = (TextView) rowView.findViewById(R.id.labContent);
 	  
 		// populates the model
 		ILaboratory lab = this.values.get(position);
		if (lab.isEmpty() == false) {
 			textViewTitle.setText(lab.getTitle());
 			textViewContent.setText(lab.getContent());
 		} 
 	    
 		return rowView;
 	}
 
 }

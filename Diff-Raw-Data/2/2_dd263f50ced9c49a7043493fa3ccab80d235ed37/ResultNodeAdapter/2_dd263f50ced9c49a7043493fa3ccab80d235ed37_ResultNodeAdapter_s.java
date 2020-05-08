 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.ResultNode;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 
 import java.util.List;
 import java.util.Map;
 
 public class ResultNodeAdapter extends ArrayAdapter<ResultNode> {
 	public ResultNodeAdapter(Context context, List<ResultNode> objects) {
 		super(context, R.layout.list_item, objects);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		TextView item = convertView == null ? (TextView) LayoutInflater.from(getContext()).inflate(R.layout.list_item, null) : (TextView) convertView;
 		ResultNode node = getItem(position);
 
 		String txt = "";
 
 		for (Map.Entry<String, String> e : node.getMisc().entrySet()) {
 			txt += e.getKey() + ": " + e.getValue() + "\n";
 		}
 
 		// cut of last \n
		if (!txt.isEmpty())
 			txt = txt.substring(0, txt.length() - 1);
 
 		item.setText(txt);
 
 		return item;
 	}
 }

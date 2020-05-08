 package pt.traincompany.account;
 
 import pt.traincompany.main.R;
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class CardAdapter extends ArrayAdapter<Card> {
 
 	Context context;
 	int layoutResourceId;
 	int icon;
 	Card data[] = null;
 
 	public CardAdapter(Context context, int layoutResourceId, int icon,
 			Card[] data) {
 		super(context, layoutResourceId, data);
 		this.layoutResourceId = layoutResourceId;
 		this.context = context;
 		this.icon = icon;
 		this.data = data;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		CardHolder holder = null;
 
 		if (row == null) {
 			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 
 			holder = new CardHolder();
 			holder.imgIcon = (ImageView) row.findViewById(R.id.removeCard);
 			holder.txtNumber = (TextView) row.findViewById(R.id.creditCardNumber);
 			row.setTag(holder);
 		} else {
			holder = (CardHolder) row.getTag();
 		}
 
 		Card weather = data[position];
 		holder.txtNumber.setText(weather.number);
 		holder.imgIcon.setImageResource(this.icon);
 
 		return row;
 	}
 
 	static class CardHolder {
 		ImageView imgIcon;
 		TextView txtNumber;
 	}
 
 }

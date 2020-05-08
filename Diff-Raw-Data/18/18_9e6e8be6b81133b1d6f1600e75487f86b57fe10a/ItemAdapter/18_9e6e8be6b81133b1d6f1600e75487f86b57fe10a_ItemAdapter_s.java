 package ch.unibe.ese.shopnote.adapters;
 
 import java.util.Currency;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.telephony.TelephonyManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.activities.ItemListActivity;
 import ch.unibe.ese.shopnote.activities.ViewRecipeActivity;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Item;
 
 /**
  *	Modified ArrayAdapter to display the items including the name, quantity and price (if defined)
  */
 public class ItemAdapter extends ArrayAdapter<Item> {
 
private BaseActivity context;
 	
 	public ItemAdapter(BaseActivity context, int resource, List<Item> itemList) {
 		super(context, resource, itemList);
 		this.context = context;
 	}
 
 	private class ItemHolder {
 		ImageView checkBox;
 		TextView name;
 		TextView quantity;
 		TextView price;
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ItemHolder holder;
 		Item item = getItem(position);
 		
 		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 		if(convertView == null) {
 			convertView = mInflater.inflate(R.layout.shopping_list_item_view, null);
 			holder = new ItemHolder();
 			holder.checkBox = (ImageView) convertView.findViewById(R.id.item_checkBox);
 			holder.name = (TextView) convertView.findViewById(R.id.item_name);
 			holder.quantity = (TextView) convertView.findViewById(R.id.item_quantity);
 			holder.price = (TextView) convertView.findViewById(R.id.item_price);
 			convertView.setTag(holder);
 		} else {
 			holder = (ItemHolder) convertView.getTag();
 		}
 		
 		// tick checkbox
 		if ((context.getClass() == ItemListActivity.class) || (context.getClass() == ViewRecipeActivity.class))
 			holder.checkBox.setVisibility(View.GONE);
 		else if (item.isBought()) {
 			holder.checkBox.setImageResource(R.drawable.checkbox_checked);
 		}
 		
 		// set name
 		holder.name.setText(item.getName());
 		context.updateTextColor(holder.name);
 		
 		// set quantity
 		if (item.getQuantity() != null) {
 			
 			String quantity = item.getQuantity().toString();
 			
 			int ordinal = item.getUnit().ordinal();
 			Resources res = convertView.getResources();
 			String localizedUnit = res.getStringArray(R.array.item_units_short)[ordinal];
 
 			StringBuilder sb = new StringBuilder(quantity).append(" ").append(localizedUnit);
 			holder.quantity.setText(sb.toString());
 			context.updateTextColorSecond(holder.quantity);
 		}
 		else
 			holder.quantity.setText("");
 		
 		// get user currency
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String country = telephonyManager.getNetworkCountryIso();
		Locale locale = new Locale("en", country); // convert country code to locale
		Currency currency = Currency.getInstance(locale);
 		
 		// set price
 		if (item.getPrice() != null) {
 			String price = item.getPrice().toString();
 			if (price.length() > 6)
 				price = price.substring(0, 5);
 			holder.price.setText(price + " " + currency);
 			context.updateTextColorSecond(holder.price);
 		}
 		else
 			holder.price.setText("");
 		
 		return convertView;
 	}
 }

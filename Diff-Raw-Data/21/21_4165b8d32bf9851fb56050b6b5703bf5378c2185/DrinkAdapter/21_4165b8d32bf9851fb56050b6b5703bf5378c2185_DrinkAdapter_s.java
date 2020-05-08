 package se.turbotorsk.mybar;
 
 import java.util.LinkedList;
 
 import se.turbotorsk.mybar.model.Drink;
 import se.turbotorsk.mybar.model.Ingredient;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * This activity handles the drinks
  */
 public class DrinkAdapter extends ArrayAdapter<LinkedList> {
 
 	public DrinkAdapter(Context context, int textViewResourceId) {
 		super(context, textViewResourceId);
 		// TODO Auto-generated constructor stub s
 	}
 
 	private LinkedList<Drink> items;
 
 	/**
 	 * Constructor for DrinkAdapter
 	 * 
 	 * @param context
 	 * @param resource
 	 * @param items
 	 */
 	public DrinkAdapter(Context context, int resource, LinkedList items) {
 		super(context, resource, items);
 		this.items = items;
 
 	}
 
 	/**
 	 * This method gets the view for all drinks
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		View v = convertView;
 
 		if (v == null) {
 
 			LayoutInflater vi;
 			vi = LayoutInflater.from(getContext());
 			v = vi.inflate(R.layout.rowlayout, null);
 
 		}
 
 		Drink p = items.get(position);
 
 		if (p != null) {
 
 			TextView tt = (TextView) v.findViewById(R.id.drink);
 			TextView tt1 = (TextView) v.findViewById(R.id.ingredients);
 			TextView tt3 = (TextView) v.findViewById(R.id.rating);
 			TextView tt4 = (TextView) v.findViewById(R.id.drinkDescription);
 			ImageView iv = (ImageView) v.findViewById(R.id.list_image);
 
 			if (tt != null) {
 
 				tt.setText(p.getName());
 			}
 			if (tt1 != null) {
 
 				tt1.setText(p.getIngredient());
 			}
 			if (tt3 != null) {
 
 				tt3.setText(Integer.toString(p.getRating()));
 			}
 		}
 
 		return v;
 	}
 
 	/**
 	 * This method is fetching the name of the drink
 	 * 
 	 * @param position
 	 * @return name
 	 */
 	public String getDrinkName(int position) {
 		Drink drink = items.get(position);
 		return drink.getName();
 	}
 
 	/**
 	 * This method is fetching the ingredients of the drink
 	 * 
 	 * @param position
 	 * @return ingredients
 	 */
 	public String getIngredients(int position) {
 		Drink drink = items.get(position);
 		return drink.getIngredient();
 	}
 
 	/**
 	 * This method is fetching the rating of the drink
 	 * 
 	 * @param position
 	 * @return rating
 	 */
 	public String getRating(int position) {
 		Drink drink = items.get(position);
 		return Integer.toString(drink.getRating());
 	}
 
 	/**
 	 * This method is fetching the description of the drink
 	 * 
 	 * @param position
 	 * @return description
 	 */
 	public String getDescrip(int position) {
 		Drink drink = items.get(position);
 		return drink.getDescription();
 	}
 
 }

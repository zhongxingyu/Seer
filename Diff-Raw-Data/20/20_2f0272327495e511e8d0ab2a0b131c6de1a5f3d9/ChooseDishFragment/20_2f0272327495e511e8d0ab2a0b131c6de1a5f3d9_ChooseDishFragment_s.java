 package se.kth.csc.iprog.dinnerplanner;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import se.kth.csc.iprog.dinnerplanner.model.DinnerModel;
 import se.kth.csc.iprog.dinnerplanner.model.Dish;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 
 public class ChooseDishFragment extends Fragment {
 	ListView dishesListView;
 	TextView titleTextView;
 	List<RowItem> rowItems;
 	final Context context;
 	
 	public ChooseDishFragment(Context context) {
 		this.context = context;
 		
 
 	}
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_choose_dish, container, false);
 		
 		dishesListView = (ListView) view.findViewById(R.id.list_dishes);
 		titleTextView = (TextView) view.findViewById(R.id.choose_title);
 
 		titleTextView.setText(ChooseDish.dishTitles.get(ChooseDish.currentDishType-1));
 		createList();
 		
 		return view;
 	}
 
 	public void getRowItems() {
 		DinnerModel model = ((DinnerPlannerApplication) this.getActivity().getApplicationContext()).getModel();
 	    Set<Dish> dishes = model.getDishesOfType(ChooseDish.currentDishType);
 	    rowItems = new ArrayList<RowItem>();
 		for (Dish d : dishes) {
 			String imageString = d.getImage();
 			if (!imageString.contains("drawable/"))
 				imageString = "drawable/"+imageString;
 			imageString = imageString.replace(".jpg", "");	
 			Integer image = getResources().getIdentifier(imageString, null, this.getActivity().getApplicationContext().getPackageName()); 	// Fr att ta fram imagestrngen som en int till rowItem
 			RowItem item = new RowItem(image, d.getName(), d.getDescription());		// Lgga in all information som ska visas i listan i en anpassad RowItem
 			rowItems.add(item);
 		}
 	}
 	
 	public void createList() {
 		getRowItems();
 		CustomListViewAdapter listAdapter = new CustomListViewAdapter(this.getActivity().getApplicationContext(), R.layout.item, rowItems);  
 		dishesListView.setAdapter(listAdapter);     
 		dishesListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 					long id) {
 				/*Toast.makeText(getApplicationContext(), "Clicked: "+ rowItems.get(position).getTitle(),
 						Toast.LENGTH_SHORT).show();*/
 				final Dialog dialog = new Dialog(context);
 				dialog.setContentView(R.layout.dish);
 				dialog.setTitle("Dish Name...");
 				
				TextView ingredients = (TextView) dialog.findViewById(R.id.ingredients);
				ingredients.setText(R.string.ingredients);
 				
 				ImageView dishImage = (ImageView) dialog.findViewById(R.id.dishImage);
 				dishImage.setImageResource(R.drawable.meatballs);
 				
 				TextView howTo = (TextView) dialog.findViewById(R.id.howTo);
 				howTo.setText(R.string.howTo);
 				
 				Button backButton = (Button) dialog.findViewById(R.id.back);
 				
 //				backButton.setOnClickListener(new OnClickListener() {
 //
 //
 //					public void onClick(View view) {
 //						dialog.dismiss();
 //					}
 //
 //				});
 	 
 				dialog.show();
 			}
 		});
 
 	}
 
 }

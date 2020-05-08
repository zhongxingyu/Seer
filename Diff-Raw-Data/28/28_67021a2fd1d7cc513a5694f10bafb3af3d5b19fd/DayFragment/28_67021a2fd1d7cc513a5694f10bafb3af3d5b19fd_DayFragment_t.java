 package de.rentoudu.mensa.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import de.rentoudu.mensa.R;
 import de.rentoudu.mensa.model.Day;
 import de.rentoudu.mensa.model.Menu;
 
 /**
  * The fragment representing both menus and the rating bar.
  */
 public class DayFragment extends Fragment {
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	View view = inflater.inflate(R.layout.fragment_day, container, false);
     	
     	if(savedInstanceState == null) {
     		processDay(view);
     	}
     	
         return view;
     }
 
 	private void processDay(View view) {
 		LinearLayout dayContainer = (LinearLayout) view.findViewById(R.id.day_container);
 		LinearLayout menuContainer = (LinearLayout) view.findViewById(R.id.day_menu_container);
     	Day day = (Day) getArguments().get("day");
     	if(day.hasMenus())
     		processMenus(dayContainer, menuContainer, day);
     	else
     		processEmptyView(menuContainer);
 	}
 
 	private void processMenus(LinearLayout dayContainer, LinearLayout menuContainer, Day day) {
 		NoteFragment notesFragment = NoteFragment.fromText(day.getNotes());
     	FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
     	// Add menus
     	for(Menu menu : day.getMenus()) {
     		MenuFragment menuFragment = MenuFragment.fromMenu(menu);
     		int menuIndex = day.getMenus().indexOf(menu);
     		fragmentTransaction.add(menuContainer.getId(), menuFragment, "menu-" + menuIndex);
     	}
     	// Add notes
     	fragmentTransaction.add(dayContainer.getId(), notesFragment, "notes");
     	// Commit fragments to view.
     	fragmentTransaction.commit();
 	}
 	
 	private void processEmptyView(LinearLayout container) {
		EmptyMenuFragment fragment = new EmptyMenuFragment();
 		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
     	fragmentTransaction.add(container.getId(), fragment, "empty");
     	fragmentTransaction.commit();
 	}
     
 }

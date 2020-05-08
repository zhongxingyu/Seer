 package com.ese2013.mub;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.ese2013.mub.model.DailyMenuplan;
 import com.ese2013.mub.model.Day;
 import com.ese2013.mub.model.Mensa;
 import com.ese2013.mub.model.Menu;
 import com.ese2013.mub.model.Model;
 
 public class DailyPlanFragment extends Fragment {
 	private Day day;
 
 	public DailyPlanFragment() {
 	}
 
 	public void setDay(Day day) {
 		this.day = day;
 	}
 
 	public static DailyPlanFragment newInstance(Day day) {
 		DailyPlanFragment frag = new DailyPlanFragment();
 		frag.setDay(day);
 		return frag;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(
 				R.layout.fragment_home_scrollable_content, container, false);
 		LinearLayout layout = (LinearLayout) rootView
 				.findViewById(R.id.section_linear_layout);
 		Model model = Model.getInstance();
 		List<Mensa> mensas = model.getMensas();
 		if (!HomeFragment.getShowAllByDay())
 			mensas = model.getFavoriteMensas();
 
 		TextView textDateOfDayOfWeek = new TextView(container.getContext());
 		textDateOfDayOfWeek.setText(day.format(new SimpleDateFormat(
 				"dd. MMMM yyyy", Locale.getDefault())));
 		layout.addView(textDateOfDayOfWeek);
 
 		if (mensas.isEmpty()) {
 			TextView noFavoriteMensasChosen = new TextView(
 					container.getContext());
 			noFavoriteMensasChosen.setText(R.string.no_favorite_mensa);
 			layout.addView(noFavoriteMensasChosen);
 		}
 
 		createMenuViewForAllMensas(mensas, container, layout);
 		return rootView;
 	}
 
 	private void createMenuViewForAllMensas(List<Mensa> mensas,
 			ViewGroup container, LinearLayout layout) {
 
 		for (Mensa mensa : mensas) {
 			createMenuView(mensa, container, layout);
 		}
 	}
 
 	private void createMenuView(Mensa mensa, ViewGroup container,
 			LinearLayout layout) {
 		LayoutInflater inf = (LayoutInflater) getActivity().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 		RelativeLayout relativeLayout = (RelativeLayout) inf.inflate(
 				R.layout.daily_section_title_bar, null);
 		TextView text = (TextView) relativeLayout.getChildAt(0);
 		text.setText(mensa.getName());
 
 		LinearLayout menuLayout = new LinearLayout(container.getContext());
 		menuLayout.setOrientation(LinearLayout.VERTICAL);
 
 		setUpFavoriteButton(relativeLayout, mensa);
 		setUpMapButton(relativeLayout, mensa);
 		setUpInvitationButton(relativeLayout, mensa, day);
 
 		DailyMenuplan d = mensa.getMenuplan().getDailymenuplan(day);
 
 		if (d != null) {
 			for (Menu menu : d.getMenus())
 				menuLayout.addView(new MenuView(container.getContext(), menu));
 		} else {
 			TextView noMenusText = new TextView(this.getActivity());
 			noMenusText.setText(R.string.dailyplanfragment_no_menus_available);
 			noMenusText.setPadding(48, 0, 0, 0);
 			menuLayout.addView(noMenusText);
 		}
 
 		if (HomeFragment.getShowAllByDay())
 			menuLayout.setVisibility(View.GONE);
 
 		relativeLayout.setOnClickListener(new ToggleListener(menuLayout,
 				container.getContext()));
 
 		layout.addView(relativeLayout);
 		layout.addView(menuLayout);
 	}
 
 	private void setUpInvitationButton(RelativeLayout relativeLayout,
 			Mensa mensa, Day dayOfInvitation) {
		ImageButton invitationButton = (ImageButton) relativeLayout.getChildAt(3);
 		invitationButton.setOnClickListener(new InvitationButtonListener(mensa,
 				dayOfInvitation, this));
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setUpFavoriteButton(RelativeLayout rel, Mensa mensa) {
 		ImageButton favorite = (ImageButton) rel.getChildAt(1);
 		favorite.setImageResource((mensa.isFavorite()) ? R.drawable.ic_fav
 				: R.drawable.ic_fav_grey);
 		favorite.setOnClickListener(new FavoriteButtonListener(mensa, favorite,
 				this));
 	}
 
 	public void setUpMapButton(RelativeLayout rel, Mensa mensa) {
 		ImageButton map = (ImageButton) rel.getChildAt(2);
 		map.setOnClickListener(new MapButtonListener(mensa, this));
 		map.setImageResource(R.drawable.ic_map);
 	}
 
 	public void refreshFavoriteView() {
 		((DrawerMenuActivity) getActivity()).refreshHomeActivity();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 }

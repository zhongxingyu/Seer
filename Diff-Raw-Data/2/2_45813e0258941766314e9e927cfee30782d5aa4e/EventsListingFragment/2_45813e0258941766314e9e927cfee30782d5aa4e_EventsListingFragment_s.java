 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.dt.fragments.events;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 
 import eu.trentorise.smartcampus.ac.UserRegistration;
 import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
 import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
 import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
 import eu.trentorise.smartcampus.android.common.follow.model.Topic;
 import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
 import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
 import eu.trentorise.smartcampus.dt.DTParamsHelper;
 import eu.trentorise.smartcampus.dt.R;
 import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
 import eu.trentorise.smartcampus.dt.custom.EventAdapter;
 import eu.trentorise.smartcampus.dt.custom.EventPlaceholder;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
 import eu.trentorise.smartcampus.dt.custom.map.MapManager;
 import eu.trentorise.smartcampus.dt.fragments.home.HomeFragment;
 import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
 import eu.trentorise.smartcampus.dt.fragments.search.WhenForSearch;
 import eu.trentorise.smartcampus.dt.fragments.search.WhereForSearch;
 import eu.trentorise.smartcampus.dt.model.BaseDTObject;
 import eu.trentorise.smartcampus.dt.model.Concept;
 import eu.trentorise.smartcampus.dt.model.DTConstants;
 import eu.trentorise.smartcampus.dt.model.EventObject;
 import eu.trentorise.smartcampus.dt.model.POIObject;
 import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 // to be used for event listing both in categories and in My Events
 public class EventsListingFragment extends AbstractLstingFragment<EventObject> implements TagProvider {
 	private ListView list;
 	private Context context;
 
 	public static final String ARG_CATEGORY = "event_category";
 	public static final String ARG_POI = "event_poiId";
 	public static final String ARG_POI_NAME = "event_poi_title";
 	public static final String ARG_QUERY = "event_query";
 	public static final String ARG_QUERY_TODAY = "event_query_today";
 	public static final String ARG_MY = "event_my";
 	public static final String ARG_CATEGORY_SEARCH = "category_search";
 	public static final String ARG_MY_EVENTS_SEARCH = "my_events_search";
 	public static final String ARG_LIST = "event_list";
 	public static final String ARG_ID = "id_event";
 	public static final String ARG_INDEX = "index_adapter";
 
 	private String category;
 	private EventAdapter eventsAdapter;
 	private boolean mFollowByIntent;
 //	private long biggerFromTime;
 	private String idEvent = "";
 	private Integer indexAdapter;
 	private Boolean reload = false;
 	private Integer postitionSelected = -1;
 	private ViewSwitcher previousViewSwitcher;
 	private List<EventObject> listEvents = new ArrayList<EventObject>();
 	
 	@Override
 	public void onActivityCreated(Bundle arg0) {
 		super.onActivityCreated(arg0);
 		list = (ListView) getSherlockActivity().findViewById(R.id.events_list);
 		if (arg0 != null) {
 			// Restore last state for checked position.
 			idEvent = arg0.getString(ARG_ID);
 			indexAdapter = arg0.getInt(ARG_INDEX);
 
 		}
 		/* create the adapter is it is the first time you load */
 		if (eventsAdapter == null) {
 			eventsAdapter = new EventAdapter(context, R.layout.events_row);
 		}
 		setAdapter(eventsAdapter);
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (!idEvent.equals("")) {
 			// get info of the event
 			EventObject event = DTHelper.findEventById(idEvent);
 			POIObject poi = null;
 			if (event != null) {
 				poi = DTHelper.findPOIById(event.getPoiId());
 			}
 			if (event == null) {
 				// cancellazione
 				removeEvent(eventsAdapter, indexAdapter);
 
 			} else {
 				// modifica se numero della versione e' diverso
 				// if (event.getUpdateTime() !=
 				// eventsAdapter.getItem(indexAdapter)
 				// .getUpdateTime()) {
 				if (event.getUpdateTime() == 0) {
 
 					event.assignPoi(poi);
 					restoreElement(eventsAdapter, indexAdapter, event);
 					// removeEvent(eventsAdapter,indexAdapter);
 					// insertEvent(event);
 				}
 			}
 			// notify
 			eventsAdapter.notifyDataSetChanged();
 			idEvent = "";
 			indexAdapter = 0;
 		}
 
 	}
 
 	private void restoreElement(EventAdapter eventsAdapter2, Integer indexAdapter2, EventObject event) {
 		removeEvent(eventsAdapter, indexAdapter);
 		insertEvent(event, indexAdapter);
 
 	}
 
 	/*
 	 * insert in the same adapter the new itemsdo the post proc if they are
 	 * multiday events
 	 */
 	private void insertEvent(EventObject event, Integer indexAdapter2) {
 
 		Calendar cal = Calendar.getInstance();
 		calToDate(cal);
 		long biggerFromTime = cal.getTimeInMillis();
 		// add in the right place
 		List<EventObject> returnList = new ArrayList<EventObject>();
 		int i = 0;
 		int j = 0;
 		while (i < eventsAdapter.getCount() + 1) {
 			if (i != indexAdapter2) {
 				returnList.add(eventsAdapter.getItem(j));
 				j++;
 			} else
 				returnList.add(event);
 			i++;
 
 		}
 		eventsAdapter.clear();
 
 		// post proc for multidays
 		i = 0;
 		List<EventObject> newList = postProcForRecurrentEvents(returnList, biggerFromTime, false);
 		while (i < newList.size()) {
 			eventsAdapter.insert(newList.get(i), i);
 			i++;
 		}
 	}
 
 	/* clean the adapter from the items modified or erased */
 	private void removeEvent(EventAdapter eventsAdapter, Integer indexAdapter) {
 		EventObject objectToRemove = eventsAdapter.getItem(indexAdapter);
 		int i = 0;
 		while (i < eventsAdapter.getCount()) {
 			if (eventsAdapter.getItem(i).getEntityId() == objectToRemove.getEntityId())
 				eventsAdapter.remove(eventsAdapter.getItem(i));
 			else
 				i++;
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(ARG_ID, idEvent);
 		if (indexAdapter != null)
 			outState.putInt(ARG_INDEX, indexAdapter);
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.context = this.getSherlockActivity();
 		setHasOptionsMenu(true);
 		setFollowByIntent();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.eventslist, container, false);
 	}
 
 	private void setFollowByIntent() {
 		try {
 			ApplicationInfo ai = getSherlockActivity().getPackageManager().getApplicationInfo(
 					getSherlockActivity().getPackageName(), PackageManager.GET_META_DATA);
 			Bundle aBundle = ai.metaData;
 			mFollowByIntent = aBundle.getBoolean("follow-by-intent");
 		} catch (NameNotFoundException e) {
 			mFollowByIntent = false;
 			Log.e(EventsListingFragment.class.getName(), "you should set the follow-by-intent metadata in app manifest");
 		}
 
 	}
 
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		/*
 		 * menu.clear(); MenuItem item = menu.add(Menu.CATEGORY_SYSTEM,
 		 * R.id.map_view, Menu.NONE, R.string.map_view);
 		 * item.setIcon(R.drawable.ic_map);
 		 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		 */
 		menu.clear();
 		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
 
 		SubMenu submenu = menu.getItem(0).getSubMenu();
 		submenu.clear();
 
 		if (getArguments() == null || !getArguments().containsKey(ARG_POI)
 				&& !getArguments().containsKey(SearchFragment.ARG_LIST) && !getArguments().containsKey(ARG_QUERY_TODAY)
 				&& !getArguments().containsKey(SearchFragment.ARG_QUERY)) {
 			submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_search, Menu.NONE, R.string.search_txt);
 		}
 
 		submenu.add(Menu.CATEGORY_SYSTEM, R.id.map_view, Menu.NONE, R.string.map_view);
 
 		if (category == null) {
 			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
 		}
 
 		if (category != null) {
 			String addString = getString(R.string.add)
 					+ " "
 					+ getString(CategoryHelper.getCategoryDescriptorByCategoryFiltered(
 							CategoryHelper.CATEGORY_TYPE_EVENTS, category).description) + " "
 					+ getString(R.string.event);
 			if (Locale.getDefault().equals(Locale.ITALY))
 				addString = getString(R.string.add)
 						+ " "
 						+ getString(R.string.event)
 						+ " su "
 						+ getString(CategoryHelper.getCategoryDescriptorByCategoryFiltered(
 								CategoryHelper.CATEGORY_TYPE_EVENTS, category).description);
 
 			submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addevent, Menu.NONE, addString);
 		}
 
 		NotificationsSherlockFragmentDT.onPrepareOptionsMenuNotifications(menu);
 
 		super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		NotificationsSherlockFragmentDT.onOptionsItemSelectedNotifications(getSherlockActivity(), item);
 
 		if (item.getItemId() == R.id.map_view) {
 			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
 			if (category != null) {
 				MapManager.switchToMapView(category, HomeFragment.ARG_EVENT_CATEGORY, this);
 			} else {
 				ArrayList<BaseDTObject> target = new ArrayList<BaseDTObject>();
 				if (list != null) {
 					for (int i = 0; i < list.getAdapter().getCount(); i++) {
 						BaseDTObject o = (BaseDTObject) list.getAdapter().getItem(i);
 						if (o.getLocation() != null && o.getLocation()[0] != 0 && o.getLocation()[1] != 0) {
 							target.add(o);
 						}
 					}
 				}
 				MapManager.switchToMapView(target, this);
 			}
 			return true;
 		} else if (item.getItemId() == R.id.menu_item_addevent) {
 			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
 				// show dialog box
 				UserRegistration.upgradeuser(getSherlockActivity());
 				return false;
 			} else {
 				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
 						.beginTransaction();
 				Fragment fragment = new CreateEventFragment();
 				Bundle args = new Bundle();
 				args.putString(SearchFragment.ARG_CATEGORY, category);
 				fragment.setArguments(args);
 				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 				// fragmentTransaction.detach(this);
 				fragmentTransaction.replace(android.R.id.content, fragment, "events");
 				fragmentTransaction.addToBackStack(fragment.getTag());
 				fragmentTransaction.commit();
 				reload = true;
 				return true;
 			}
 		} else if (item.getItemId() == R.id.submenu_search) {
 			FragmentTransaction fragmentTransaction;
 			Fragment fragment;
 			fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
 			fragment = new SearchFragment();
 			Bundle args = new Bundle();
 			args.putString(SearchFragment.ARG_CATEGORY, category);
 			args.putString(CategoryHelper.CATEGORY_TYPE_EVENTS, CategoryHelper.CATEGORY_TYPE_EVENTS);
 			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_MY)
 					&& getArguments().getBoolean(SearchFragment.ARG_MY))
 				args.putBoolean(SearchFragment.ARG_MY, getArguments().getBoolean(SearchFragment.ARG_MY));
 			fragment.setArguments(args);
 			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 			fragmentTransaction.replace(android.R.id.content, fragment, "events");
 			fragmentTransaction.addToBackStack(fragment.getTag());
 			fragmentTransaction.commit();
 			/* add category to bundle */
 			return true;
 
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onStart() {
 		Bundle bundle = this.getArguments();
 
 		// new SCAsyncTask<Bundle, Void, EventObject[]>(getActivity(),
 		// new EventLoader(getActivity())).execute(bundle);
 
 		if (reload) {
 			eventsAdapter = new EventAdapter(context, R.layout.events_row);
 			setAdapter(eventsAdapter);
 			reload = false;
 		}
 
 		// set title
 		TextView title = (TextView) getView().findViewById(R.id.list_title);
 		String category = bundle.getString(SearchFragment.ARG_CATEGORY);
 		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategoryFiltered("events", category);
 		String categoryString = (catDescriptor != null) ? context.getResources().getString(catDescriptor.description)
 				: null;
 
 		if (bundle != null && bundle.containsKey(SearchFragment.ARG_QUERY)
 				&& bundle.getString(SearchFragment.ARG_QUERY) != null) {
 			String query = bundle.getString(SearchFragment.ARG_QUERY);
 			title.setText(context.getResources().getString(R.string.search_for) + " ' " + query + " '");
 			if (bundle.containsKey(SearchFragment.ARG_CATEGORY)) {
 				category = bundle.getString(SearchFragment.ARG_CATEGORY);
 				if (category != null)
 					title.append(" " + context.getResources().getString(R.string.search_in_category) + " "
 							+ getString(catDescriptor.description));
 			}
 
 		} else if (bundle != null && bundle.containsKey(SearchFragment.ARG_CATEGORY)
 				&& (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {
 			title.setText(categoryString);
 		} else if (bundle != null && bundle.containsKey(SearchFragment.ARG_MY)
 				&& bundle.getBoolean(SearchFragment.ARG_MY)) {
 			title.setText(R.string.myevents);
 		} else if (bundle != null && bundle.containsKey(ARG_POI_NAME)) {
 			String poiName = bundle.getString(ARG_POI_NAME);
 			title.setText(getResources().getString(R.string.eventlist_at_place) + " " + poiName);
 		} else if (bundle != null && bundle.containsKey(ARG_QUERY)) {
 			String query = bundle.getString(ARG_QUERY);
 			title.setText(context.getResources().getString(R.string.search_for) + " '" + query + "'");
 			if (bundle.containsKey(ARG_CATEGORY_SEARCH)) {
 				category = bundle.getString(ARG_CATEGORY_SEARCH);
 				if (category != null)
 					title.append(context.getResources().getString(R.string.search_in_category) + " " + category);
 			}
 		} else if (bundle != null && bundle.containsKey(ARG_QUERY_TODAY)) {
 			title.setText(context.getResources().getString(R.string.search_today_events));
 		}
 		if (bundle.containsKey(SearchFragment.ARG_WHERE_SEARCH)) {
 			WhereForSearch where = bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH);
 			if (where != null)
 				title.append(" " + where.getDescription() + " ");
 		}
 
 		if (bundle.containsKey(SearchFragment.ARG_WHEN_SEARCH)) {
 			WhenForSearch when = bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH);
 			if (when != null)
 				title.append(" " + when.getDescription() + " ");
 		}
 		// close items menus if open
 		((View) list.getParent()).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				hideListItemsMenu(v, false);
 			}
 		});
 		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				hideListItemsMenu(view, false);
 				setStoreEventId(view, position);
 			}
 
 		});
 
 		// open items menu for that entry
 		// list.setOnItemLongClickListener(new
 		// AdapterView.OnItemLongClickListener() {
 		// public boolean onItemLongClick(AdapterView<?> parent, View view, int
 		// position, long id) {
 		// if ((position != postitionSelected) && (previousViewSwitcher !=
 		// null)) {
 		// // //close the old viewSwitcher
 		// previousViewSwitcher.showPrevious();
 		// eventsAdapter.setElementSelected(-1);
 		// previousViewSwitcher = null;
 		// hideListItemsMenu(view, true);
 		//
 		// }
 		// ViewSwitcher vs = (ViewSwitcher)
 		// view.findViewById(R.id.event_viewswitecher);
 		// setupOptionsListeners(vs, position);
 		// vs.showNext();
 		// postitionSelected = position;
 		// eventsAdapter.setElementSelected(position);
 		// previousViewSwitcher = vs;
 		//
 		// return true;
 		// }
 		// });
 
 		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
 		super.onStart();
 
 	}
 
 	private void setStoreEventId(View v, int position) {
 		final EventObject event = ((EventPlaceholder) v.getTag()).event;
 		idEvent = event.getId();
 		indexAdapter = position;
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		super.onScrollStateChanged(view, scrollState);
 		if ((postitionSelected != -1) && (scrollState == SCROLL_STATE_TOUCH_SCROLL)) {
 			hideListItemsMenu(view, true);
 
 		}
 	}
 
 	private void hideListItemsMenu(View v, boolean close) {
 		boolean toBeHidden = false;
 		for (int index = 0; index < list.getChildCount(); index++) {
 			View view = list.getChildAt(index);
 			if (view != null && view instanceof LinearLayout && ((LinearLayout) view).getChildCount() == 2)
 				view = ((LinearLayout) view).getChildAt(1);
 			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
 				((ViewSwitcher) view).showPrevious();
 				toBeHidden = true;
 				eventsAdapter.setElementSelected(-1);
 				postitionSelected = -1;
 				previousViewSwitcher = null;
 
 			}
 		}
 		if (!toBeHidden && v != null && v.getTag() != null && !close) {
 			// no items needed to be flipped, fill and open details page
 			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
 					.beginTransaction();
 			EventDetailsFragment fragment = new EventDetailsFragment();
 
 			Bundle args = new Bundle();
 			// args.putSerializable(EventDetailsFragment.ARG_EVENT_OBJECT,
 			// ((EventPlaceholder) v.getTag()).event);
 			args.putString(EventDetailsFragment.ARG_EVENT_ID, ((EventPlaceholder) v.getTag()).event.getId());
 
 			fragment.setArguments(args);
 
 			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 			// fragmentTransaction.detach(this);
 			fragmentTransaction.replace(android.R.id.content, fragment, "events");
 			fragmentTransaction.addToBackStack(fragment.getTag());
 			fragmentTransaction.commit();
 
 		}
 	}
 
 	protected void setupOptionsListeners(final ViewSwitcher vs, final int position) {
 		final EventObject event = ((EventPlaceholder) ((View) vs.getParent()).getTag()).event;
 		ImageButton b = (ImageButton) vs.findViewById(R.id.delete_btn);
 		if (DTHelper.isOwnedObject(event)) {
 			b.setVisibility(View.VISIBLE);
 			b.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
 						// show dialog box
 						UserRegistration.upgradeuser(getSherlockActivity());
 					} else {
 						new SCAsyncTask<EventObject, Void, Boolean>(getActivity(), new EventDeleteProcessor(
 								getActivity())).execute(event);
 						hideListItemsMenu(vs, false);
 					}
 				}
 			});
 		} else {
 			b.setVisibility(View.GONE);
 		}
 		b = (ImageButton) vs.findViewById(R.id.edit_btn);
 		b.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
 					// show dialog box
 					UserRegistration.upgradeuser(getSherlockActivity());
 				} else {
 					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
 							.beginTransaction();
 					Fragment fragment = new CreateEventFragment();
 					Bundle args = new Bundle();
 					setStoreEventId((View) vs.getParent(), position);
 
 					// args.putSerializable(CreateEventFragment.ARG_EVENT,
 					// event);
 					args.putString(CreateEventFragment.ARG_EVENT, event.getId());
 					fragment.setArguments(args);
 					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 					// fragmentTransaction.detach(this);
 					fragmentTransaction.replace(android.R.id.content, fragment, "events");
 					fragmentTransaction.addToBackStack(fragment.getTag());
 					fragmentTransaction.commit();
 				}
 			}
 		});
 		// b = (ImageButton) vs.findViewById(R.id.share_btn);
 		// b.setOnClickListener(new OnClickListener() {
 		//
 		// @Override
 		// public void onClick(View v) {
 		// Toast.makeText(getActivity(), "Event shared.",
 		// Toast.LENGTH_SHORT).show();
 		//
 		// }
 		// });
 		b = (ImageButton) vs.findViewById(R.id.tag_btn);
 		b.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
 					// show dialog box
 					UserRegistration.upgradeuser(getSherlockActivity());
 				} else {
 					TaggingDialog taggingDialog = new TaggingDialog(getActivity(),
 							new TaggingDialog.OnTagsSelectedListener() {
 
 								@SuppressWarnings("unchecked")
 								@Override
 								public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
 									new TaggingAsyncTask(event).execute(Concept.convertSS(suggestions));
 								}
 							}, EventsListingFragment.this, Concept.convertToSS(event.getCommunityData().getTags()));
 					taggingDialog.show();
 				}
 			}
 		});
 		b = (ImageButton) vs.findViewById(R.id.follow_btn);
 		b.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				FollowEntityObject obj = new FollowEntityObject(event.getEntityId(), event.getTitle(),
 						DTConstants.ENTITY_TYPE_EVENT);
 				if (mFollowByIntent) {
 					FollowHelper.follow(getSherlockActivity(), obj);
 				} else {
 					SCAsyncTask<Object, Void, Topic> followTask = new SCAsyncTask<Object, Void, Topic>(
 							getSherlockActivity(), new FollowAsyncTaskProcessor(getSherlockActivity(), null));
 					followTask.execute(getSherlockActivity().getApplicationContext(), DTParamsHelper.getAppToken(),
 							DTHelper.getAuthToken(), obj);
 				}
 			}
 		});
 	}
 
 	private List<EventObject> getEvents(AbstractLstingFragment.ListingRequest... params) {
 		try {
 			Collection<EventObject> result = null;
 			Bundle bundle = getArguments();
 			boolean my = false;
 
 			if (bundle == null) {
 				return Collections.emptyList();
 			}
 			if (bundle.getBoolean(SearchFragment.ARG_MY))
 				my = true;
 			String categories = bundle.getString(SearchFragment.ARG_CATEGORY);
 			SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
 			sort.put("fromTime", 1);
 			if (bundle.containsKey(SearchFragment.ARG_CATEGORY)
 					&& (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {
 
 				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
 						bundle.getString(SearchFragment.ARG_QUERY),
 						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
 						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, EventObject.class,
 						sort, categories);
 
 			} else if (bundle.containsKey(ARG_POI) && (bundle.getString(ARG_POI) != null)) {
 				result = DTHelper.getEventsByPOI(params[0].position, params[0].size, bundle.getString(ARG_POI));
 			} else if (bundle.containsKey(SearchFragment.ARG_MY) && (bundle.getBoolean(SearchFragment.ARG_MY))) {
 
 				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
 						bundle.getString(SearchFragment.ARG_QUERY),
 						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
 						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, EventObject.class,
 						sort, categories);
 
 			} else if (bundle.containsKey(SearchFragment.ARG_QUERY)) {
 
 				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
 						bundle.getString(SearchFragment.ARG_QUERY),
 						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
 						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, EventObject.class,
 						sort, categories);
 
 			} else if (bundle.containsKey(ARG_QUERY_TODAY)) {
 				result = DTHelper.searchTodayEvents(params[0].position, params[0].size,
 						bundle.getString(SearchFragment.ARG_QUERY));
 			} else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
 				result = (List<EventObject>) bundle.get(SearchFragment.ARG_LIST);
 			} else {
 				return Collections.emptyList();
 			}
 
 			listEvents.addAll(result);
 			
 			List<EventObject> sorted = new ArrayList<EventObject>(listEvents);
 			for (EventObject eventObject : sorted) {
 				if (eventObject.getPoiId() != null) {
 					eventObject.assignPoi(DTHelper.findPOIById(eventObject.getPoiId()));
 				}
 			}
 			
 //			if (params[0].position == 0) {
 				Calendar cal = Calendar.getInstance();
 				cal.setTimeInMillis(System.currentTimeMillis());
 				calToDate(cal);
 				long biggerFromTime = cal.getTimeInMillis();
 //			}
 			if (sorted.size() > 0) {
 //				listEvents.addAll(postProcForRecurrentEvents(sorted, biggerFromTime));
				return postProcForRecurrentEvents(sorted, biggerFromTime, result.size() == 0);
 			} else
 				return sorted;
 		} catch (Exception e) {
 			Log.e(EventsListingFragment.class.getName(), e.getMessage());
 			e.printStackTrace();
 			listEvents = Collections.emptyList();
 			return listEvents;
 		}
 	}
 
 	private void calToDate(Calendar cal) {
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 	}
 
 	@Override
 	public List<SemanticSuggestion> getTags(CharSequence text) {
 		try {
 			return DTHelper.getSuggestions(text);
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	private class TaggingAsyncTask extends SCAsyncTask<List<Concept>, Void, Void> {
 
 		public TaggingAsyncTask(final EventObject p) {
 			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<List<Concept>, Void>(getSherlockActivity()) {
 				@Override
 				public Void performAction(List<Concept>... params) throws SecurityException, Exception {
 					p.getCommunityData().setTags(params[0]);
 					DTHelper.saveEvent(p);
 					return null;
 				}
 
 				@Override
 				public void handleResult(Void result) {
 					Toast.makeText(getSherlockActivity(), getString(R.string.tags_successfully_added),
 							Toast.LENGTH_SHORT).show();
 				}
 			});
 		}
 	}
 
 	private class EventLoader extends
 			AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<EventObject>> {
 
 		public EventLoader(Activity activity) {
 			super(activity);
 		}
 
 		// fetches the events
 		@Override
 		public List<EventObject> performAction(AbstractLstingFragment.ListingRequest... params)
 				throws SecurityException, Exception {
 			return getEvents(params);
 		}
 
 		// populates the listview with the events
 		@Override
 		public void handleResult(List<EventObject> result) {
 			// Bundle bundle = getArguments();
 			// if (bundle != null && bundle.containsKey(ARG_CATEGORY) && (result
 			// == null || result.size() == 0) && getListView().getCount() == 0)
 			// {
 			// Toast.makeText(getActivity(), getString(R.string.noevents),
 			// Toast.LENGTH_LONG).show();
 			// }
 			// } else
 			// list.setAdapter(new EventAdapter(context, R.layout.events_row,
 			// result));
 			eventsAdapter.clear();
 			updateList(result == null || result.isEmpty());
 		}
 	}
 
 	private List<EventObject> postProcForRecurrentEvents(List<EventObject> result, long lessFromTime, boolean endReached) {
 		List<EventObject> returnList = new ArrayList<EventObject>();
 		EventComparator r = new EventComparator();
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(result.get(result.size() - 1).getFromTime());
 		calToDate(cal);
 		long biggerFromTime = cal.getTimeInMillis();
 		if (biggerFromTime < lessFromTime )
 			biggerFromTime = lessFromTime;
 		for (EventObject event : result) {
 			if (event.getToTime() == null || event.getToTime() == 0) {
 				event.setToTime(event.getFromTime());
 			}
 			/*
 			 * if an event has toTime null o equal to toTime, it is only for
 			 * that day
 			 */
 			
 			
 			if ((event.getToTime() != null) && (event.getFromTime() != null) && (event.getToTime() != event.getFromTime())) {
 				long eventFromTime = event.getFromTime();
 				long eventToTime = event.getToTime();
 
 				Calendar calFromTime = Calendar.getInstance();
 				Calendar calToTime = Calendar.getInstance();
 
 				calFromTime.setTime(new Date(eventFromTime));
 				calToDate(calFromTime);
 
 				calToTime.setTime(new Date(eventToTime));
 				calToDate(calToTime);
 				long dayFromTime = calFromTime.getTimeInMillis();
 				long dayToTime = calToTime.getTimeInMillis();
 
 				if (dayFromTime == dayToTime) {
 					/* it takes the same day */
 					returnList.add(event);
 
 				} else {
 					/*
 					 * if and event takes more than one day, duplicate it (until
 					 * X)
 					 */
 					dayFromTime = Math.max(dayFromTime, lessFromTime);
 					if (!endReached) {
 						dayToTime = Math.min(dayToTime, biggerFromTime);
 					}
 					long dayTmpTime = dayFromTime;
 
 					while (dayTmpTime <= dayToTime) {
 						EventObject newEvent = event.copy();
 						newEvent.setFromTime(dayTmpTime);
 						newEvent.setToTime(dayTmpTime);
 						Calendar caltmp = Calendar.getInstance();
 						caltmp.setTimeInMillis(dayTmpTime);
 						caltmp.add(Calendar.DATE, 1);
 						dayTmpTime = caltmp.getTimeInMillis();
 						returnList.add(newEvent);
 						if (this.getArguments().containsKey(ARG_QUERY_TODAY))
 							break;
 					}
 					/* calculate how much days use the events */
 					/* create and entry for every day */
 				}
 
 			} else {
 				/* put it in the returnList */
 				returnList.add(event);
 			}
 		}
 		Collections.sort(returnList, r);
 		return returnList;
 
 	}
 
 	private static class EventComparator implements Comparator<EventObject> {
 		public int compare(EventObject c1, EventObject c2) {
 			if (c1.getFromTime() == c2.getFromTime())
 				return 0;
 			if (c1.getFromTime() < c2.getFromTime())
 				return -1;
 			if (c1.getFromTime() > c2.getFromTime())
 				return 1;
 			return 0;
 		}
 	}
 
 	private class EventDeleteProcessor extends AbstractAsyncTaskProcessor<EventObject, Boolean> {
 		private EventObject object = null;
 
 		public EventDeleteProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public Boolean performAction(EventObject... params) throws SecurityException, Exception {
 			object = params[0];
 			return DTHelper.deleteEvent(params[0]);
 		}
 
 		@Override
 		public void handleResult(Boolean result) {
 			if (result) {
 				/*
 				 * delete every instance of the recurrent events if it is
 				 * present
 				 */
 				int i = 0;
 				while (i < list.getAdapter().getCount()) {
 					EventObject event = (EventObject) list.getAdapter().getItem(i);
 					if (object.getId() == event.getId()) {
 						((EventAdapter) list.getAdapter()).remove(event);
 						updateList(list == null || list.getAdapter().isEmpty());
 					} else {
 						i++;
 					}
 
 				}
 				((EventAdapter) list.getAdapter()).notifyDataSetChanged();
 				updateList(((EventAdapter) list.getAdapter()).isEmpty());
 			} else {
 				Toast.makeText(getActivity(), getActivity().getString(R.string.app_failure_cannot_delete),
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 	}
 
 	@Override
 	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<EventObject>> getLoader() {
 		return new EventLoader(getActivity());
 	}
 
 	@Override
 	protected ListView getListView() {
 		return list;
 	}
 
 	private void updateList(boolean empty) {
 		if (getView() != null) {
 			eu.trentorise.smartcampus.dt.custom.ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(
 					R.id.eventlistcontainer));
 			if (empty) {
 				eu.trentorise.smartcampus.dt.custom.ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(
 						R.id.eventlistcontainer));
 			}
 			hideListItemsMenu(null, false);
 		}
 	}
 
 }

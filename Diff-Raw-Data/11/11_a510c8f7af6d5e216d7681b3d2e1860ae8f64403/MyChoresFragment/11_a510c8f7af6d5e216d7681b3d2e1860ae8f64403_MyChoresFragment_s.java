 package il.ac.huji.chores;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import il.ac.huji.chores.dal.ApartmentDAL;
 import il.ac.huji.chores.dal.ChoreDAL;
 import il.ac.huji.chores.dal.CoinsDAL;
 import il.ac.huji.chores.dal.RoommateDAL;
 import il.ac.huji.chores.exceptions.FailedToGetRoommateException;
 import il.ac.huji.chores.exceptions.UserNotLoggedInException;
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import com.parse.ParseException;
 
 import java.util.*;
 
 import static android.graphics.Color.*;
 
 /**
  * MyChoresFragment contains a list of all chores assigned to the current user.
  */
 public class MyChoresFragment extends Fragment {
 
 	private MyChoresListAdapter adapter;
 	private List<Chore> chores;
 	private CategorySeries coinSeries;
     private CategorySeries debtSeries;
 	private XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
 	private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 	private GraphicalView chart;
 	private ListView listView;
 	private View progressBar;
 	private FrameLayout chartFrame;
 	private TextView messageBox;
 	private String yTitle;
 	private List<Chore> roommatesChores;
     private String[] legendTitles;
 
     /**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public MyChoresFragment() {
 		// Noop
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_my_chores_list,
 				container, false);
         legendTitles = new String[]{
                 getActivity().getResources().getString(R.string.legend_coins_collected),
                 getActivity().getResources().getString(R.string.legend_coins_debt)};
         return view;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	@Override
 	public void onResume() {
 		Log.d("MyChoresFragment", "onResume");
 		super.onResume();
 		
 		if (adapter != null) {
 			
 			final View placeholder = ViewUtils.hideLoadingView(listView, getActivity(), R.id.progressBar);
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... voids) {
 					try {
 						roommatesChores = ChoreDAL.getRoommatesChores();
 					} catch (UserNotLoggedInException e) {
 						LoginActivity.OpenLoginScreen(getActivity(), false);
 					}
 					return null;
 				}
 
 				@Override
 				protected void onPostExecute(Void aVoid) {
 					super.onPostExecute(aVoid);
 					if (getActivity() == null) {
 						return;
 					}
 					adapter.clear();
 					adapter.addAll(roommatesChores);
 					adapter.sort(new DeadlineComparator());
 					if (adapter == null || adapter.getCount() == 0) {
 						listView.setVisibility(View.GONE);
 						progressBar.setVisibility(View.GONE);
 						messageBox.setText(R.string.my_chores_no_chores_message);
 						messageBox.setVisibility(View.VISIBLE);
 						return;
 					} else {
 						ViewUtils.hideLoadingView(messageBox, getActivity(), listView);
 					}
 					ViewUtils.replacePlaceholder(listView, placeholder);
 				}
 			}.execute();
 		}
 
 		// Coins Chart stuff
 		final ProgressBar progressBar = new ProgressBar(getActivity());
 		chartFrame.removeAllViews();
 		chartFrame.addView(progressBar, new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT));
 		new AsyncTask<Void, Void, Exception>() {
 
 			@Override
 			protected void onPostExecute(Exception e) {
 				super.onPostExecute(e); // To change body of overridden
 				// methods use File | Settings |
 				// File Templates.
                 if (e != null) {
                     if (e instanceof ParseException) {
                         if (((ParseException) e).getCode() == ParseException.CONNECTION_FAILED) {
                             ViewUtils.callToast(getActivity(), R.string.chores_connection_failed);
                         } else {
                             ViewUtils.callToast(getActivity(), R.string.general_error);
                         }
                     } else {
                         // Do nothing
                     }
                     chartFrame.setVisibility(View.GONE);
                     return;
                 }
                 Activity context = getActivity();
				if (context == null) {
 					return;
 				}
 				if(dataSet == null ||renderer == null){
 					chartFrame.setVisibility(View.GONE);
 					return;
 				}
 				chart = ChartFactory.getBarChartView(context, dataSet, renderer, BarChart.Type.DEFAULT);
 				chartFrame.removeAllViewsInLayout();
 				chartFrame.addView(chart);
 			}
 
 			@Override
 			protected Exception doInBackground(Void... voids) {
 				try {
 					initChart();
 				} catch (UserNotLoggedInException e) {
                     return e;
 				} catch (FailedToGetRoommateException e) {
 					Log.w("MyChoresFragment.onResume",
 							"error getting roommates", e);
                     return e;
 				} catch (Exception e) {
 					Log.e("MyChoresFragment.onResume",
 							"unexpected error while creating graph", e);
                     return e;
 				}
 				return null; // To change body of implemented methods use File |
 				// Settings | File Templates.
 			}
 		}.execute();
 	}
 
 	private void initChart() throws UserNotLoggedInException, FailedToGetRoommateException, ParseException {
 		Map<String, int[]> coinsMap = getCoinsMap();
 		if (coinsMap.size() == 0)
             throw new FailedToGetRoommateException("Coins map size is 0");
         List<String> orderedNames = createDataSeries(coinsMap);
 		dataSet.addSeries(coinSeries.toXYSeries());
         dataSet.addSeries(debtSeries.toXYSeries());
         createSeriesRenderer(new int[] {BLUE, RED});
 		renderer.setBarSpacing(0.2);
         renderer.setBarWidth(50);
 		renderer.setXAxisMin(0.0);
 		renderer.setXAxisMax(orderedNames.size() + 1);
 //		int minCoins = findMin(coinsMap);
         int minCoins = 0;
 		int maxCoins = findMax(coinsMap);
 		int padding = Math.min((maxCoins - minCoins) / 10, 2);
 		renderer.setYAxisMax(maxCoins);
 		renderer.setYAxisMin(minCoins - 2 * padding);
         renderer.setXLabels(0);
         renderer.setYLabels(2);
         renderer.setShowAxes(false);
 		renderer.setMarginsColor(LTGRAY);
         renderer.setLabelsColor(BLACK);
         renderer.setXLabelsColor(BLACK);
         renderer.setYLabelsColor(0, BLACK);
         renderer.setBackgroundColor(DefaultRenderer.NO_COLOR); // TODO [yl] use
 		renderer.setClickEnabled(false);
         renderer.setPanEnabled(false);
 		renderer.setYTitle(yTitle);
         renderer.setMargins(new int[] {30, 40, 15, 30});
 		int k = 1;
 		for (String name : orderedNames) {
 			renderer.addXTextLabel(k, name);
 			k++;
 		}
 	}
 
 	private Map<String, int[]> getCoinsMap() throws UserNotLoggedInException,
             FailedToGetRoommateException, ParseException {
 		List<String> roommates;
         roommates = ApartmentDAL.getApartmentRoommates(RoommateDAL.getApartmentID());
 		if (roommates == null || roommates.size() == 0) {
 			throw new FailedToGetRoommateException( "No roommates found for apartment " + RoommateDAL.getApartmentID());
 		}
 		Map<String, int[]> coinsMap = new HashMap<String, int[]>(roommates.size());
 		for (String username : roommates) {
             Coins coins = CoinsDAL.getRoommateCoins(username);
             int collectedCoins = coins.getCoinsCollected(), debt = coins.getDept();
             if (collectedCoins < 0 || debt < 0) {
                 throw new FailedToGetRoommateException("User " + username + " does not have Coins object");
             }
             coinsMap.put(username, new int[]{collectedCoins, debt});
         }
         return coinsMap;
 	}
 
     private void createSeriesRenderer(int[] colors) {
         renderer = new XYMultipleSeriesRenderer();
         renderer.setShowGrid(false);
         renderer.setAxisTitleTextSize(20);
         renderer.setLabelsTextSize(20);
         for (int color : colors) {
             XYSeriesRenderer r = new XYSeriesRenderer();
             r.setColor(color);
             r.setDisplayChartValues(true);
             renderer.addSeriesRenderer(r);
         }
     }
 
     private List<String> createDataSeries(Map<String, int[]> coins) {
 		List<String> orderedKeys = new ArrayList<String>(coins.size());
        /*String currentRoommate = RoommateDAL.getRoomateUsername();*/
         coinSeries = new CategorySeries(legendTitles[0]);
         debtSeries = new CategorySeries(legendTitles[1]);
        /*int[] currentCoinsAndDebt = coins.get(currentRoommate);
        coinSeries.add(currentCoinsAndDebt[0]);
        debtSeries.add(currentCoinsAndDebt[1]);
        orderedKeys.add(0, currentRoommate);
        coins.remove(currentRoommate);*/
         for (String key : coins.keySet()) {
             int[] coinsAndDebt = coins.get(key);
             coinSeries.add(key, coinsAndDebt[0]);
             debtSeries.add(key, coinsAndDebt[1]);
             orderedKeys.add(key);
 		}
 		return orderedKeys;
 	}
 
 	private int findMax(Map<String, int[]> coinsMap) {
         int max = Integer.MIN_VALUE;
         for (int[] tuple : coinsMap.values()) {
             if (tuple[0] + tuple[1] > max) {
                 max = tuple[0] + tuple[1];
             }
         }
         return max;
 	}
 
 	private int findMin(Map<String, int[]> coinsMap) {
         int min = Integer.MAX_VALUE;
         for (int[] tuple : coinsMap.values()) {
             if (tuple[0] < min) {
                 min = tuple[0];
             }
         }
         return min;
     }
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		Log.d("MyChoresFragment", "MyChores: onActivityCreated");
 		// Set the adapter
 		listView = (ListView) getActivity().findViewById(
 				R.id.myChoresFragmentListView);
 		progressBar = ViewUtils.hideLoadingView(listView, getActivity(),
 				R.id.progressBar);
 		chartFrame = (FrameLayout) getActivity().findViewById(
 				R.id.myChoresChartContainer);
 		messageBox = (TextView) getActivity().findViewById(
 				R.id.myChoresMessageBox);
 		yTitle = getResources().getString(R.string.coins_graph_y_label);
 
 		// set item click listener
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			// if list chore item is clicked - open chore card
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				final Chore chore = (Chore) parent.getItemAtPosition(position);
 				Intent intent = new Intent(getActivity(),
 						ChoreCardActivity.class);
 				intent.putExtra(
 						getResources().getString(
 								R.string.card_activity_extra1_name), chore);
 				String userName = RoommateDAL.getRoomateUsername();
 				intent.putExtra(
 						getResources().getString(
 								R.string.card_activity_extra2_name), userName);
 				startActivity(intent);
 			}
 		});
 
 		new AsyncTask<Void, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(Void... voids) {
 				try {
 					chores = ChoreDAL.getRoommatesChores();
 
 				} catch (UserNotLoggedInException e) {
 					LoginActivity.OpenLoginScreen(getActivity(), false);
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void aVoid) {
 				super.onPostExecute(aVoid); // To change body of overridden
 				// methods use File | Settings |
 				// File Templates.
 				ViewUtils.replacePlaceholder(listView, progressBar);
 				Activity context = getActivity();
 				if (context == null) {
 					return;
 				}
 				adapter = new MyChoresListAdapter(context, chores);
 				adapter.sort(new DeadlineComparator());
 				listView.setAdapter(adapter);
 
 				if (adapter == null || adapter.getCount() == 0) {
 					String apartment=null;;
 					try {
 						apartment = RoommateDAL.getApartmentID();
 					} catch (UserNotLoggedInException e) {
 						LoginActivity.OpenLoginScreen(getActivity(), false);
 
 					}
 					if(apartment==null || apartment.equals("")){
 						messageBox.setText(R.string.my_chores_no_apartment);
 					}
 					else{
 					messageBox.setText(R.string.my_chores_no_chores_message);
 					}
 					ViewUtils.hideLoadingView(listView, context, messageBox);
 				}
 				else{
 					ViewUtils.hideLoadingView(messageBox, context, listView);
 				}
 			}
 		}.execute();
 	}
 }

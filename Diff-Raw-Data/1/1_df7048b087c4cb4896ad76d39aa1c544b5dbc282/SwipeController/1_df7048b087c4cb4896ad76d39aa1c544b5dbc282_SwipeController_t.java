 package uk.co.thomasc.wordmaster.view.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.api.GetTurnsRequestListener;
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;
 import uk.co.thomasc.wordmaster.objects.callbacks.TurnAddedListener;
 import uk.co.thomasc.wordmaster.view.DialogPanel;
 import uk.co.thomasc.wordmaster.view.Errors;
 import uk.co.thomasc.wordmaster.view.RussoText;
 import uk.co.thomasc.wordmaster.view.game.PullToRefreshListView.OnRefreshListener;
 import uk.co.thomasc.wordmaster.view.menu.MenuDetailFragment;
 
 public class SwipeController extends FragmentStatePagerAdapter {
 
 	private static String gid;
 	private float pageWidth = 1.0f;
 
 	public SwipeController(BaseGame fm, String gameid) {
 		super(fm.getSupportFragmentManager());
 		pageWidth = fm.wideLayout ? 0.5f : 1.0f;
 		SwipeController.gid = gameid;
 	}
 
 	@Override
 	public float getPageWidth(int position) {
 		return pageWidth;
 	}
 	
 	@Override
 	public Fragment getItem(int arg0) {
 		Fragment fragment = new Pages();
 		Bundle args = new Bundle();
 
 		args.putBoolean(Pages.ARG_OBJECT, arg0 == 0);
 		args.putString(MenuDetailFragment.ARG_ITEM_ID, SwipeController.gid);
 		fragment.setArguments(args);
 
 		return fragment;
 	}
 
 	@Override
 	public int getCount() {
 		return 2;
 	}
 
 	public static class Pages extends Fragment implements TurnAddedListener {
 		public static final String ARG_OBJECT = "object";
 		private ToggleListener listener = new ToggleListener();
 		private GameAdapter adapter;
 		private Game game;
 		private PullToRefreshListView listView;
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			if (game != null) {
 				game.removeTurnListener(this);
 			}
 		}
 
 		@Override
 		public void onTurnAdded(final Turn turn, final boolean newerTurn) {
 			getActivity().runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					adapter.add(turn);
 					if (newerTurn) {
 						listView.scrollToBottom();
 					}
 				}
 			});
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 			View rootView;
 			if (getArguments().getBoolean(Pages.ARG_OBJECT)) {
 				rootView = new PullToRefreshListView(getActivity());
 				((BaseGame) getActivity()).gameAdapter = adapter = new GameAdapter(getActivity());
 
 				game = Game.getGame(SwipeController.gid);
 				if (game != null) {
 					for (Turn t : game.getTurns()) {
 						adapter.add(t);
 					}
 					game.addTurnListener(this);
 					((PullToRefreshListView) rootView).setAdapter(adapter);
 					listView = (PullToRefreshListView) rootView;
					listView.setBackgroundColor(Color.WHITE);
 					listView.setCacheColorHint(Color.WHITE);
 					listView.setOnRefreshListener(new OnRefreshListener() {
 						@Override
 						public void onRefresh() {
 							int pivot = game.getPivotOldest();
 							ServerAPI.getTurns(game.getID(), pivot, -10, (BaseGame) getActivity(), new GetTurnsRequestListener() {
 	
 								@Override
 								public void onRequestFailed() {
 									getActivity().runOnUiThread(new Runnable() {
 										@Override
 										public void run() {
 											listView.onRefreshComplete();
 											DialogPanel errorMessage = (DialogPanel) getActivity().findViewById(R.id.errorMessage);
 											errorMessage.show(Errors.NETWORK);
 										}
 									});
 								}
 	
 								@Override
 								public void onRequestComplete(List<Turn> turns) {
 									ArrayList<Turn> gameTurns = game.getTurns();
 									for (Turn turn : turns) {
 										if (!gameTurns.contains(turn)) {
 											game.addTurn(turn);
 										}
 									}
 									getActivity().runOnUiThread(new Runnable() {
 										@Override
 										public void run() {
 											listView.onRefreshComplete();
 										}
 									});
 								}
 							});
 						}
 					});
 				}
 			} else {
 				game = Game.getGame(SwipeController.gid);
 				
 				rootView = inflater.inflate(R.layout.alphabet, container, false);
 				LinearLayout root = (LinearLayout) rootView;
 				int index = 0;
 				for (int i = 0; i < root.getChildCount(); i++) {
 					LinearLayout child = (LinearLayout) root.getChildAt(i);
 					for (int j = 0; j < child.getChildCount(); j++) {
 						RussoText txt = (RussoText) child.getChildAt(j);
 						txt.setOnClickListener(listener);
 						txt.setId(index);
 						boolean strike = game.getAlpha(index++);
 						txt.setTextColor(getResources().getColor(strike ? R.color.hiddenletter : R.color.maintext));
 						txt.setStrike(strike);
 					}
 				}
 			}
 			return rootView;
 		}
 
 		private class ToggleListener implements OnClickListener {
 
 			@Override
 			public void onClick(View v) {
 				RussoText txt = (RussoText) v;
 				txt.setTextColor(getResources().getColor(txt.isStrike() ? R.color.maintext : R.color.hiddenletter));
 				txt.setStrike(!txt.isStrike());
 				game.updateAlpha(txt.getId(), txt.isStrike(), (BaseGame) getActivity());
 			}
 
 		}
 	}
 
 }

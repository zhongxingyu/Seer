 package com.quizz.places.fragments;
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.activities.BaseQuizzActivity;
 import com.quizz.core.fragments.BaseGridLevelsFragment;
 import com.quizz.core.fragments.BaseListSectionsFragment;
 import com.quizz.core.interfaces.FragmentContainer;
 import com.quizz.core.models.Section;
 import com.quizz.core.utils.NavigationUtils;
 import com.quizz.core.widgets.QuizzActionBar;
 import com.quizz.places.R;
 import com.quizz.places.adapters.SectionsItemAdapter;
 
 public class ListSectionsFragment extends BaseListSectionsFragment {
 
 	private ListView mSectionsListView;
 	private boolean mHideActionBarOnDestroyView = true;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 
 		setActionbarView(getActivity().getString(R.string.ab_sections_title), "");
 		
 		View view = inflater.inflate(R.layout.fragment_list_sections, container, false);
 
 		mSectionsListView = (ListView) view.findViewById(R.id.sectionsListView);
 		mSectionsListView.setOnItemClickListener(mSectionItemClickListener);
 
 		mLoadingView = view.findViewById(R.id.loadingView);
 
 		mAdapter = new SectionsItemAdapter(getActivity(), R.layout.item_list_sections);
 		mSectionsListView.setAdapter(mAdapter);
 
 		ObjectAnimator listDisplay = ObjectAnimator.ofFloat(mSectionsListView, "alpha", 0f, 1f);
 		listDisplay.setDuration(300);
 		listDisplay.start();
 
 		return view;
 	}
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 		if (mHideActionBarOnDestroyView) {
 			if (getActivity() instanceof BaseQuizzActivity) {
 				((BaseQuizzActivity) getActivity()).getQuizzActionBar().hide(
 						QuizzActionBar.MOVE_NORMAL);
 			}
 		}
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		mHideActionBarOnDestroyView = true;
 		if (getActivity() instanceof BaseQuizzActivity) {
 			((BaseQuizzActivity) getActivity()).getQuizzActionBar()
 					.showIfNecessary(QuizzActionBar.MOVE_NORMAL);
 		}
 	}
 	
 	private void setActionbarView(String middleText, String rightText) {
 		QuizzActionBar actionBar = ((BaseQuizzActivity) getActivity()).getQuizzActionBar();
 		actionBar.setCustomView(R.layout.ab_view_list_sections);
 		View customView = actionBar.getCustomViewContainer();
 		((TextView) customView.findViewById(R.id.ab_settings_middle_text))
 				.setText(middleText);
 		((TextView) customView.findViewById(R.id.ab_settings_right_text)).setText(rightText);
 	}
 	
 	@Override
 	public void onSectionsLoaded(List<Section> listSections) {
 		super.onSectionsLoaded(listSections);
 		int unlockedCount = 0;
		for (Section section : listSections)
 			unlockedCount += (section.status == Section.SECTION_UNLOCKED) ? 1 : 0;
		setActionbarView(getActivity().getString(R.string.ab_sections_title), 
				String.valueOf(unlockedCount) + "/" + String.valueOf(listSections.size()));
 	}
 	
 	// ===========================================================
 	// Listeners
 	// ===========================================================
 
 	OnItemClickListener mSectionItemClickListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 				long arg3) {
 			mHideActionBarOnDestroyView = false;
 			FragmentContainer container = (FragmentContainer) getActivity();
 			FragmentManager fragmentManager = getActivity()
 					.getSupportFragmentManager();
 
 			FragmentTransaction transaction = fragmentManager
 					.beginTransaction();
 			transaction.setCustomAnimations(R.anim.slide_in_right,
 					R.anim.slide_out_left, R.anim.slide_in_left,
 					R.anim.slide_out_right);
 
 			Bundle args = new Bundle();
 			args.putParcelable(BaseGridLevelsFragment.ARG_SECTION,
 					mAdapter.getItem(position));
 			NavigationUtils.directNavigationTo(GridLevelsFragment.class,
 					fragmentManager, container, true, transaction, args);
 		}
 	};
 }

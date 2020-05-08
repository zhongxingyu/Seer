 package ru.tulupov.nsuconnect.fragment;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.activity.MainActivity;
 import ru.tulupov.nsuconnect.adapter.PagerAdapter;
 import ru.tulupov.nsuconnect.helper.SettingsHelper;
 import ru.tulupov.nsuconnect.model.SearchParameters;
 import ru.tulupov.nsuconnect.model.Settings;
 import ru.tulupov.nsuconnect.widget.CustomViewPager;
 
 
 public class WelcomeFragment extends BaseFragment {
     private static final int PAGE_YOUR_GENDER = 0;
     private static final int PAGE_TARGET_GENDER = 1;
     private static final int PAGE_YOUR_AGE = 2;
     private static final int PAGE_TARGET_AGE = 3;
     private static final int PAGE_FINISH = 4;
 
     public static WelcomeFragment newInstance(final Context context) {
         return (WelcomeFragment) Fragment.instantiate(context, WelcomeFragment.class.getName());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_welcome, container, false);
     }
 
     private CustomViewPager pager;
     private List<Integer> pagesBackStack;
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         pagesBackStack = new ArrayList<Integer>();
         pagesBackStack.add(PAGE_YOUR_GENDER);
         pager = (CustomViewPager) view.findViewById(R.id.pager);
         pager.setPagingEnabled(false);
         PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
 
         final SearchSettingsFragment yourGenderFragment = YourGenderSettingsFragment.newInstance(getActivity());
         final SearchSettingsFragment targetGenderFragment = TargetGenderSettingsFragment.newInstance(getActivity());
         final SearchSettingsFragment yourAgeFragment = YourAgeSettingsFragment.newInstance(getActivity());
         final SearchSettingsFragment targetAgeFragment = TargetAgeSettingsFragment.newInstance(getActivity());
         SearchSettingFinishFragment finishFragment = SearchSettingFinishFragment.newInstance(getActivity());
 
         adapter.setFragments(Arrays.asList(yourGenderFragment, targetGenderFragment, yourAgeFragment, targetAgeFragment, finishFragment));
 
         pager.setAdapter(adapter);
 
         yourGenderFragment.setOnSelectListener(new SearchSettingsFragment.OnSelectListener() {
             @Override
             public void onSelect(List<Integer> selected) {
                 int position = selected.get(0);
                 if (position == 0) {
                     navigate(PAGE_YOUR_AGE);
                    targetGenderFragment.clearSelection();
                 } else {
                     navigate(PAGE_TARGET_GENDER);
                 }
             }
         });
         targetGenderFragment.setOnSelectListener(new SearchSettingsFragment.OnSelectListener() {
             @Override
             public void onSelect(List<Integer> selected) {
                 navigate(PAGE_YOUR_AGE);
             }
         });
         yourAgeFragment.setOnSelectListener(new SearchSettingsFragment.OnSelectListener() {
             @Override
             public void onSelect(List<Integer> selected) {
                 int position = selected.get(0);
                 if (position == 0) {
                     navigate(PAGE_FINISH);
                    targetAgeFragment.clearSelection();
                 } else {
                     navigate(PAGE_TARGET_AGE);
                 }
             }
         });
         targetAgeFragment.setOnSelectListener(new SearchSettingsFragment.OnSelectListener() {
             @Override
             public void onSelect(List<Integer> selected) {
                 navigate(PAGE_FINISH);
             }
         });
 
         finishFragment.setOnClickListener(new SearchSettingFinishFragment.OnClickListener() {
             @Override
             public void onClick() {
                 SearchParameters searchParameters = new SearchParameters();
 
                 searchParameters.setYourGender(yourGenderFragment.getSelectedItems());
                 searchParameters.setTargetGender(targetGenderFragment.getSelectedItems());
                 searchParameters.setYourAge(yourAgeFragment.getSelectedItems());
                 searchParameters.setTargetAge(targetAgeFragment.getSelectedItems());
 
                 Settings settings = SettingsHelper.getSettings(getActivity());
                 settings.setSearchParameters(searchParameters);
                 SettingsHelper.setSettings(getActivity(), settings);
 
                 ((MainActivity) getActivity()).addFragment(ChatFragment.newInstance(getActivity()));
             }
         });
     }
 
     private void navigate(int page) {
         pagesBackStack.add(page);
         pager.setCurrentItem(page, true);
     }
 
     @Override
     public boolean onBackPressed() {
         if (pagesBackStack.size() == 1) {
             return false;
         }
         pagesBackStack.remove(pagesBackStack.size() - 1);
         int lastPage = pagesBackStack.get(pagesBackStack.size() - 1);
 
         pager.setCurrentItem(lastPage);
         return true;
     }
 }

 package ru.neverdark.phototools.fragments;
 
 import ru.neverdark.phototools.Constants;
 import ru.neverdark.phototools.R;
 import ru.neverdark.phototools.log.Log;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockListFragment;
 
 public class TitlesFragment extends SherlockListFragment {
     boolean mDualPane;
     int mCurrentCheckPosition = 0;
    final String[] TITLES = getResources().getStringArray(R.array.main_menuTitles);
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         Log.message("Enter");
         super.onActivityCreated(savedInstanceState);
         
         setListAdapter(new ArrayAdapter<String>(getActivity(),
                 R.layout.menu_item,
                 R.id.menuItem_label_title,
                 TITLES));
         
         View detailsFrame = getActivity().findViewById(R.id.main_detailFragment);
         mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
         
         if (savedInstanceState != null) {
             mCurrentCheckPosition = savedInstanceState.getInt(Constants.CURRENT_CHOICE, 0);
         }
         
         if (mDualPane) {
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             showDetails(mCurrentCheckPosition);
         }
     }
     
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
         outState.putInt(Constants.CURRENT_CHOICE, mCurrentCheckPosition);
     }
     
     @Override
     public void onListItemClick(ListView listView, View view, int position, long id) {
         showDetails(position);
     }
     
     private void showDetails(int index) {
         mCurrentCheckPosition = index;
 
         if (mDualPane == true) {
             getListView().setItemChecked(index, true);
             showFragment(index);
         } else {
             showActivity(index);
         }
     }
     
     private void showFragment(int index) {
         
     }
     
     private void showActivity(int index) {
     
     }
     
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.message("Enter");
        if (container == null) {
            return null;
        }
        
        return inflater.inflate(R.layout.menu_item, container);
    }
 }

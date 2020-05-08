 package ru.tulupov.nsuconnect.fragment;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.R;
 
 
 public abstract class SearchSettingsFragment extends Fragment {
 
     private static final String PREFS_STATE_FORMATTER = "state_%s";
 
     private static final String PREFERENCES_NAME = "search_settings";
 
     public interface OnSelectListener {
         void onSelect(List<Integer> selected);
     }
 
     private OnSelectListener onSelectListener;
     private ListView list;
 
     public OnSelectListener getOnSelectListener() {
         return onSelectListener;
     }
 
     public void setOnSelectListener(OnSelectListener onSelectListener) {
         this.onSelectListener = onSelectListener;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_search_settings, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
 
 
         TextView title = (TextView) view.findViewById(R.id.title);
         title.setText(getTitleTextId());
         list = (ListView) view.findViewById(R.id.list);
 
         String[] sports = getResources().getStringArray(getItemsArrayId());
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, sports);
         list.setChoiceMode(getListChoiceMode());
         list.setAdapter(adapter);
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 saveState();
             }
         });
         view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 List<Integer> selected = getSelectedItems();
 
                 if (selected.isEmpty()) {
                     Toast.makeText(getActivity(), getErrorTextId(), Toast.LENGTH_LONG).show();
                 } else {
 
                     if (onSelectListener != null) {
                         onSelectListener.onSelect(selected);
                     }
                 }
             }
         });
 
         restoreState();
     }
 
     public List<Integer> getSelectedItems() {
         SparseBooleanArray checked = list.getCheckedItemPositions();
         List<Integer> selected = new ArrayList<Integer>();
         for (int i = 0; i < checked.size(); i++) {
             int position = checked.keyAt(i);
             if (checked.valueAt(i)) {
                 selected.add(position);
             }
         }
 
         return selected;
     }
 
 
     private void saveState() {
         SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
         Gson gson = new Gson();
         preferences.edit().putString(String.format(PREFS_STATE_FORMATTER, getClass().getSimpleName()), gson.toJson(getSelectedItems())).commit();
     }
 
     private void restoreState() {
         SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
         Gson gson = new Gson();
         Type listType = new TypeToken<ArrayList<Integer>>() {
         }.getType();
         List<Integer> selected = gson.fromJson(preferences.getString(String.format(PREFS_STATE_FORMATTER, getClass().getSimpleName()), ""), listType);
         if (selected != null) {
             for (Integer position : selected) {
                 list.setItemChecked(position, true);
             }
         }
     }
 
     protected int getErrorTextId() {
         return R.string.search_nothing_selected;
     }
 
     protected abstract int getTitleTextId();
 
     protected abstract int getItemsArrayId();
 
     protected int getListChoiceMode() {
         return ListView.CHOICE_MODE_MULTIPLE;
     }
 
 }

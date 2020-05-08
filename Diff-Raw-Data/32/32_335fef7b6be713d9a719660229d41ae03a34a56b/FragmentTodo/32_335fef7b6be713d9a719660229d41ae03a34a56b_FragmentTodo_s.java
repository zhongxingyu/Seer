 package com.smfandroid.sleektodo.todolist;
 
import com.smfandroid.sleektodo.MainActivity;
import com.smfandroid.sleektodo.R;
import com.smfandroid.sleektodo.R.id;
import com.smfandroid.sleektodo.R.layout;

 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 // Instances of this class are fragments representing a single
 // object in our collection.
 public class FragmentTodo extends Fragment {
 	
 	public final String TAG = getClass().getSimpleName(); 
     public static final String ARG_CATEGORY = "category";
 
     public void notifyTodoDataChanged() {
         ListOfTodoItems list = (ListOfTodoItems)getView().findViewById(R.id.TodoList);
         list.getCursorAdapter().notifyDataSetChanged();
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	super.onCreateView(inflater, container, savedInstanceState);
         // The last two arguments ensure LayoutParams are inflated
         // properly.
         Bundle args = getArguments();
         int category = args.getInt(ARG_CATEGORY);
     	Log.i(TAG, "Fragment creation for category " + category);
     	
         View rootView = inflater.inflate(
                 R.layout.fragment_todo, container, false);
 
         ListOfTodoItems list = (ListOfTodoItems)rootView.findViewById(R.id.TodoList);
         list.prepareListView();
         LoaderManager ldM = getLoaderManager();
         ldM.initLoader(category, null, list);
         ((MainActivity)getActivity()).addTodoFragment(category, this);
         return rootView;
     }
 }

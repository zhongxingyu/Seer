 package com.music.fmv.core;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.TextView;

 import com.music.fmv.R;
 import com.music.fmv.utils.ActivityMediator;
 import com.music.fmv.utils.ViewUtils;
 import com.music.fmv.views.LoadDialog;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lebed
  * Date: 7/14/13
  * Time: 9:03 AM
  * To change this template use File | Settings | File Templates.
  */

 public abstract class BaseFragment extends Fragment {
     protected Core core;
     protected View mainView;
     protected ActivityMediator mMediator;
     protected BaseActivity baseActivity;
     protected LayoutInflater inflater;
     private LoadDialog dialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         baseActivity = (BaseActivity) getActivity();
         core = Core.getInstance(baseActivity);
         mMediator = new ActivityMediator(baseActivity);
     }
 
     @Override
     public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         this.inflater = inflater;
         createView(savedInstanceState);
         ViewUtils.setUpKeyBoardHider(mainView, getActivity());
         return mainView;
     }
 
     protected boolean runTask(AsyncTask task) {
         return baseActivity.runTask(task);
     }
 
     protected View inflateView(int id) {
         return inflater.inflate(id, null, false);
     }
 
     protected View createSearchHeader(TextView.OnEditorActionListener searchListener) {
         View v = inflater.inflate(R.layout.search_header, null, false);
        EditText tv = ((EditText) v.findViewById(R.id.search_field));
         tv.setOnEditorActionListener(searchListener);
         return v;
     }
 
     protected abstract void createView(Bundle savedInstanceState);
 }

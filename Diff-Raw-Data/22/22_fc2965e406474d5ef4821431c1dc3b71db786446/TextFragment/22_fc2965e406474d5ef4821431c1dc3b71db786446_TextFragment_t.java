 package hk.hku.cs.srli.monkeydemo.demo;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import hk.hku.cs.srli.monkeydemo.R;
 
 public class TextFragment extends DemoFragmentBase {
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = super.onCreateView(inflater, container, savedInstanceState);
         return rootView;
     }
     
     @Override
     protected int getLayoutResource() {
         return R.layout.fragment_text;
     }
 }

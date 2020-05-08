 package com.andreaszeiser.jalousiesamples.fragment;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.andreaszeiser.jalousie.IndicatedLinearLayoutJalousie;
 import com.andreaszeiser.jalousiesamples.R;
 
 public class HorizontalIndicatorFragment extends ExpandCollapseApisFragment {
 
 	private IndicatedLinearLayoutJalousie mIndicatedLinearLayoutJalousie;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		getActivity().setTitle(R.string.ex_indicator_horizontal);
 	}

 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.indicator_horizontal, null);
 
 		mIndicatedLinearLayoutJalousie = (IndicatedLinearLayoutJalousie) view
 				.findViewById(R.id.indicated_jalousie);
 		mLinearLayoutJalousie = mIndicatedLinearLayoutJalousie
				.getLinearLayoutJalousie();
 
 		return view;
 	}
 
 }

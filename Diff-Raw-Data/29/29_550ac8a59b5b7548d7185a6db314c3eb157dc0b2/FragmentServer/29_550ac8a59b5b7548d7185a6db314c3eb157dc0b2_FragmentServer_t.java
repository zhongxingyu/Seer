 package com.cesarandres.ps2link.fragments;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
import android.widget.ImageButton;
 
 import com.cesarandres.ps2link.R;
 import com.cesarandres.ps2link.base.BaseFragment;
 import com.cesarandres.ps2link.soe.content.World;
 import com.google.gson.Gson;
 
 /**
  * Created by cesar on 6/16/13.
  */
 public class FragmentServer extends BaseFragment {
 
 	private World server;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.server = new Gson().fromJson(getActivity().getIntent().getExtras().getString("server"), World.class);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View root = inflater.inflate(R.layout.fragment_server, container, false);
 		// Inflate the layout for this fragment
 
 		return root;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		((Button) getActivity().findViewById(R.id.buttonFragmentTitle)).setText(getString(R.string.title_servers));
		ImageButton updateButton = (ImageButton) getActivity().findViewById(R.id.buttonFragmentUpdate);
 		updateButton.setVisibility(View.VISIBLE);
 		updateButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 
 			}
 		});
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 }

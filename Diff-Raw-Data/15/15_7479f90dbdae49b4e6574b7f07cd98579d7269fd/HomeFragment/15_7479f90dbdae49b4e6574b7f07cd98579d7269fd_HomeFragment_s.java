 package com.pactera.blsales.fragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.pactera.blsales.BaseApplication;
 import com.pactera.blsales.R;
 import com.pactera.blsales.adapter.HomeGalleryAdapter;
 import com.pactera.blsales.adapter.IndexAdapter;
 import com.pactera.blsales.model.Banner;
 import com.pactera.blsales.model.Index;
 import com.pactera.blsales.view.GalleryFlow;
 
 import android.app.Fragment;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.GridView;
 
 public class HomeFragment extends Fragment {
	private GalleryFlow mGallery;
 	private HomeGalleryAdapter adapter;
 	private IndexAdapter indexAdapter;
 	private GridView mGridView;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO 041291 Auto-generated method stub
 
 		View view = inflater.inflate(R.layout.home, null);
		mGallery = (GalleryFlow) view.findViewById(R.id.home_gallery);
 		mGridView = (GridView) view.findViewById(R.id.home_index_gd);
 
 		List<Banner> list = new ArrayList<Banner>();
 		Banner banner = new Banner();
 		list.add(banner);
 		list.add(banner);
 		list.add(banner);
 		list.add(banner);
 		list.add(banner);
 		
 		mGallery.setBackgroundColor(Color.TRANSPARENT);
		mGallery.setSpacing(-240);
		mGallery.setMaxZoom(-60);
		mGallery.setMaxRotationAngle(-60);
 		mGallery.setFadingEdgeLength(0);
 		mGallery.setGravity(Gravity.CENTER_VERTICAL);
 		adapter = new HomeGalleryAdapter(getActivity(), list);
 		mGallery.setAdapter(adapter);
 		
 		List<Index> indexList = new ArrayList<Index>();
 		Index index0 = new Index();
 		index0.setTagName("");
 		Index index1 = new Index();
 		index1.setTagName("");
 		Index index2 = new Index();
 		index2.setTagName("");
 		Index index3 = new Index();
 		index3.setTagName("");
 		Index index4 = new Index();
 		index4.setTagName("ũɽȪ");
 		Index index5 = new Index();
 		index5.setTagName("");
 		Index index6 = new Index();
 		index6.setTagName("ԡƷ");
 		Index index7 = new Index();
 		index7.setTagName("");
 		indexList.add(index0);
 		indexList.add(index1);
 		indexList.add(index2);
 		indexList.add(index3);
 		indexList.add(index4);
 		indexList.add(index5);
 		indexList.add(index6);
 		indexList.add(index7);
 		
 		indexAdapter = new IndexAdapter(getActivity(), indexList);
 		mGridView.setAdapter(indexAdapter);
 		return view;
 	}
 
 }

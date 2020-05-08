 package com.mintplex.oeffioptimizer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.mintplex.oeffioptimizer.HaltestellenFragment.SteigInfo;
 import com.mintplex.oeffioptimizer.model.Connections;
 import com.mintplex.oeffioptimizer.model.Exit;
 import com.mintplex.oeffioptimizer.model.Exitinfo;
 import com.mintplex.oeffioptimizer.model.Lift;
 import com.mintplex.oeffioptimizer.model.Steige;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class HaltestellenAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {
 
 	Map<Steige, SteigInfo> data;
 	LayoutInflater inflater;
 	List<Steige> steigList;
 	
 	
 	public HaltestellenAdapter(LayoutInflater inflater) {
 		this.inflater = inflater;
 	}
 
 	public void setData(List<Steige> steige, Map<Steige, SteigInfo> adapterData) {
 		this.steigList = steige;
 		this.data = adapterData;
 	}
 	
 	@Override
 	public int getGroupCount() {
 		return steigList.size();
 	}
 	
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded,
 			View convertView, ViewGroup parent) {
 		View view = inflater.inflate(R.layout.platform_header, parent, false);
 		Steige s = steigList.get(groupPosition);
         t(view, R.id.platform_header_line, s.linienName);
         t(view, R.id.platform_header_direction, s.richtungName);
 		return view;
 	}
 	
 	@Override
 	public long getGroupId(int groupPosition) {
 		Steige s = steigList.get(groupPosition);
 		return s.id;
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 		return steigList.get(groupPosition);
 	}
 	
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		SteigInfo steigInfo = getSteigInfo(groupPosition);
 		if (steigInfo != null) return steigInfo.getCount();
 		return 0;
 	}
 	
 	private SteigInfo getSteigInfo(int groupPosition) {
 		Steige s = steigList.get(groupPosition);
 		SteigInfo steigInfo = data.get(s);
 		return steigInfo;
 	}
 	@Override
 	public Object getChild(int groupPosition, int childPosition) {
 		
 		SteigInfo steigInfo = getSteigInfo(groupPosition);
 		return steigInfo.getItem(childPosition);
 	}
 	
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		SteigInfo steigInfo = getSteigInfo(groupPosition);
 		return steigInfo.getItem(childPosition).getId();
 	}
 	
 	@Override
 	public View getChildView(int groupPosition, int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) {
 		SteigInfo steigInfo = getSteigInfo(groupPosition);
 		Object child = steigInfo.getItem(childPosition);
 		if (child instanceof Connections) {
 			Connections c = (Connections) child;
 			if (convertView == null) { 
 				convertView = inflater.inflate(R.layout.tranfer_item, parent, false);
 			}
            if (c.transferId != null) {
                t(convertView, R.id.tranfer_item_line, c.transferId.linienName);
                t(convertView, R.id.tranfer_item_direction, c.transferId.richtungName);
            }

             View exitContainer = convertView.findViewById(R.id.tranfer_item_exit_container);
             if (c.fkExit != null) {
             	t(exitContainer, R.id.tranfer_item_exit, c.fkExit.name);
             	exitContainer.setVisibility(View.VISIBLE);
             }
             else {
             	exitContainer.setVisibility(View.GONE);
             }
             symbols(inflater, convertView, R.id.tranfer_item_symbols, c.symbols);
                         
             hint(convertView, R.id.tranfer_item_hint, c.hint);
             
 			return convertView;
 		}
 		if (child instanceof Exitinfo) {
 			Exitinfo info = (Exitinfo) child;
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.exit, parent, false);
 			}
 			t(convertView, R.id.exit_name, info.fkExitId.name);
 			symbols(inflater, convertView, R.id.exit_symbols, info.symbols);
 			hint(convertView, R.id.exit_hint, info.hint);
 			
 			return convertView;
 		}
 		if (child instanceof Lift) {
 			Lift lift = (Lift) child;
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.lift, parent, false);
 			}
 			symbols(inflater, convertView, R.id.lift_symbols, lift.symbols);
 			
 			return convertView;
 			
 		}
 		
 		return null;
 	}
 	
 
 
 	private void t(View convertView, int intRes, String txt) {
 		SteigInfoAdapter.t(convertView, intRes, txt);
 		
 	}
 
 	private void hint(View convertView, int intRes, String hint) {
 		SteigInfoAdapter.hint(convertView, intRes, hint);
 		
 	}
 
 	private void symbols(LayoutInflater li, View view,
 			int intRes, String symbols) {
 		SteigInfoAdapter.symbols(li, view, intRes, symbols);
 		
 	}
 
 	@Override
 	public int getChildTypeCount() {
 		return 3;
 	}
 	
 	@Override
 	public int getChildType(int groupPosition, int childPosition) {
 		SteigInfo steigInfo = getSteigInfo(groupPosition);
 		Object child = steigInfo.getItem(childPosition);
 		if (child instanceof Connections) {
 			return 0;
 		}
 		if (child instanceof Exitinfo) {
 			return 1;
 		}
 		if (child instanceof Lift) {
 			return 2;
 		}
 		return 0;
 	}
 	
 
     
     @Override
     public boolean hasStableIds() {
     	return false;
     }
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return false;
 	}
     
 }

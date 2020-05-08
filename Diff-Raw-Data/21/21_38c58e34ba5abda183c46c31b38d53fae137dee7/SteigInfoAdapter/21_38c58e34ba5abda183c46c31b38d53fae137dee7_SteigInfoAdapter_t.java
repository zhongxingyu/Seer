 package com.mintplex.oeffioptimizer;
 
 import com.mintplex.oeffioptimizer.HaltestellenFragment.SteigInfo;
 import com.mintplex.oeffioptimizer.model.Connections;
 import com.mintplex.oeffioptimizer.model.Exitinfo;
 import com.mintplex.oeffioptimizer.model.Lift;
 import com.mintplex.oeffioptimizer.model.Steige;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class SteigInfoAdapter extends BaseAdapter {
 
 	SteigInfo steigInfo;
 
 	Context ctx;
 	LayoutInflater inflater;
 
 	public SteigInfoAdapter(Context ctx) {
 		this.ctx = ctx;
 		this.inflater = (LayoutInflater) ctx
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public int getCount() {
 		return steigInfo.getCount();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return steigInfo.getItem(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Object child = steigInfo.getItem(position);
 		return getView(inflater, position, convertView, parent, child);
 	}
 
 	public static View getView(LayoutInflater inflater, int position,
 			View convertView, ViewGroup parent, Object child) {
 		if (child instanceof Connections) {
 			Connections c = (Connections) child;
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.tranfer_item, parent,
 						false);
 			}
 			t(convertView, R.id.tranfer_item_line, c.transferId.linienName);
 			t(convertView, R.id.tranfer_item_direction,
 					c.transferId.richtungName);
 
 			View exitContainer = convertView
 					.findViewById(R.id.tranfer_item_exit_container);
 			if (c.fkExit != null) {
 				t(exitContainer, R.id.tranfer_item_exit, c.fkExit.name);
 				exitContainer.setVisibility(View.VISIBLE);
 			} else {
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
 
 	public  static void symbols(LayoutInflater li, View view, int viewGroupRes,
 			String symbols) {
 		ViewGroup layout = (ViewGroup) view.findViewById(viewGroupRes);
 		layout.removeAllViews();
        if (symbols == null) return;
 		String[] split = symbols.split("\\+");
 		for (String s : split) {
 			TextView tv = (TextView) li.inflate(R.layout.symbol, layout, false);
 			if (s.startsWith("V")) {
 				tv.setBackgroundResource(R.drawable.exit_bg_v);
 			} else if (s.startsWith("M")) {
 				tv.setBackgroundResource(R.drawable.exit_bg_m);
 			} else if (s.startsWith("H")) {
 				tv.setBackgroundResource(R.drawable.exit_bg_h);
 			} else {
 				tv.setBackgroundResource(R.drawable.exit_bg);
 			}
 			tv.setText(s);
 			layout.addView(tv);
 		}
 	}
 
 	public static void hint(View convertView, int hintRes, String hint) {
 		TextView hintTv = (TextView) convertView.findViewById(hintRes);
 		if (hint != null && hint.length() > 0 && !hint.equals("-")) {
 			hintTv.setText(hint);
 			hintTv.setVisibility(View.VISIBLE);
 		} else {
 			hintTv.setVisibility(View.GONE);
 		}
 	}
 
 	public static TextView t(View view, int tvRes, String txt) {
 		TextView tv = (TextView) view.findViewById(tvRes);
 		if (tv != null) {
 			tv.setText(txt);
 		}
 		return tv;
 	}
 
     public static SteigInfo getSteigInfo(Steige s) {
     	SteigInfo info = new SteigInfo();
     	info.connections = Connections.find(Connections.class, "fk_steig_id = ? ", Long.toString(s.getId()));
     	info.exits = Exitinfo.find(Exitinfo.class, "fk_steig_id = ? ", Long.toString(s.getId()));
     	info.lifts = Lift.find(Lift.class, "fk_steig_id = ? ", Long.toString(s.getId()));
     	return info;
     }
     
 }

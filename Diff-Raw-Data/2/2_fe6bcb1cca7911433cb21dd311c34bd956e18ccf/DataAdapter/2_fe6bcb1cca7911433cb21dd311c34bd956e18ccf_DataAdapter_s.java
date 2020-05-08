 package edu.uidaho.engr.seniordesign.umax.adapters;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import edu.uidaho.engr.seniordesign.umax.R;
 import edu.uidaho.engr.seniordesign.umax.data.DataNode;
 import edu.uidaho.engr.seniordesign.umax.data.DataNodeGroup;
 import edu.uidaho.engr.seniordesign.umax.jsonresult.DataValue;
 import edu.uidaho.engr.seniordesign.umax.listeners.DataNodeInfo;
 import edu.uidaho.engr.seniordesign.umax.listeners.OnDataNodeUpdateListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.os.Handler;
 import android.os.Message;
 
 
 public class DataAdapter extends ArrayAdapter<DataNode> {
 	//private ArrayList<DataNode> items;
 	private DataNodeGroup _items;
 	
 	private Activity _context;
 	
 	public DataAdapter(Activity context, int resourceID, DataNodeGroup items) {
 		super(
 				context,
 				resourceID,
 				items.getNodes()
 				);
 		_context = context;
 		_items = items;
 	}
 	
 	@Override
 	public int getCount() {
 		if (_items == null)
 			return 0;
 		return _items.getNodes().size();
 	}
 	
 	@Override
 	public DataNode getItem(int position) {
 		return _items.getNodes().get(position);
 	}
 	
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	public void ResetControls() {
 		Iterator<DataNode> itr = _items.getNodes().iterator();
 		while (itr.hasNext()) {
 			itr.next().reset();
 		}
 	}
 	
 	 @Override
      public View getView(int position, View convertView, ViewGroup parent) {
              View v = convertView;
              if (v != null) {
             	 return v;
              }
              LayoutInflater inflater = _context.getLayoutInflater();
         	 v = inflater.inflate(R.layout.dac_layout, null);
              final DataNode n = _items.getNodes().get(position);
              try {
             	 if (true || !n.hasListener()) {
                 	 switch (n.getType()) {
                 	 case DataNode.TYPE_EDIT:
                 		 n._targetView = v.findViewById(R.id.dac_edit);
                 		 try {
                 			 ((EditText) n._targetView).removeTextChangedListener(n.watcher);
                 		 } catch (Exception e) { /* VOID */ }
                 		 ((EditText) n._targetView).addTextChangedListener(n.watcher);
                 		 break;
                 	 case DataNode.TYPE_SPINNER:
                 		 n._targetView = v.findViewById(R.id.dac_spinner);
                 		 ((Spinner) n._targetView).setOnItemSelectedListener(new OnItemSelectedListener(){
 							public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 								n.softUpdate(String.valueOf(arg2));
 							}
 							public void onNothingSelected(AdapterView<?> arg0) { }
                 		 });
                 		 break;
                 	 case DataNode.TYPE_SWITCH:
                 		 n._targetView = v.findViewById(R.id.dac_switch);
                 		 ((Switch) n._targetView).setOnCheckedChangeListener(new OnCheckedChangeListener() {
 							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 								n.softUpdate(String.valueOf(isChecked).toLowerCase());
 							}
                 		 });
                 		 break;
                 	 }
                 	 n._textView = (TextView) v.findViewById(R.id.dac_caption);
                 	 n._targetView.setVisibility(View.VISIBLE);
                 	 n._textView.setText(_items.getNodes().get(position).getName());
                 	 n.setDataNodeUpdateListener(new OnDataNodeUpdateListener() {
     					public void onDataNodeUpdate(int type, int arg1, int arg2, DataNodeInfo info) {
     						controlUpdateHandler.sendMessage(
     								controlUpdateHandler.obtainMessage(
     									type, arg1, arg2, info));
     					}
                 	 });
                 	 n.update();
                  }
              } catch (Exception e) {
             	 e.printStackTrace();
              }
              
              return v;
      }
 	 
 	 public void UpdateControl(DataValue d) {
 		 controlUpdateHandler.sendMessage(controlUpdateHandler.obtainMessage(0, 0, 0, d));
 	 }
 	 
 	 final Handler controlUpdateHandler = new Handler() {
 		 public void handleMessage(Message msg) {
 			 try {
 				 DataNodeInfo info = (DataNodeInfo) msg.obj;
 				 switch (msg.what) {
 				 case DataNode.TYPE_EDIT:
 					 info.editText.setText(info.dataNode.getSval());
 					 break;
 				 case DataNode.TYPE_SPINNER:
 					 ArrayAdapter<String> adapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, info.dataNode.getDisplayValues());
 					 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					 info.spinnerView.setAdapter(adapter);
 					 info.spinnerView.setSelection(info.dataNode.getIval());
 					 break;
 				 case DataNode.TYPE_SWITCH:
 					 info.switchView.setChecked(info.dataNode.getBval());
 					 break;
 				 }
 			 } catch (Exception e) {
 				 e.printStackTrace();
 			 }
 		 }
 	 };
 }

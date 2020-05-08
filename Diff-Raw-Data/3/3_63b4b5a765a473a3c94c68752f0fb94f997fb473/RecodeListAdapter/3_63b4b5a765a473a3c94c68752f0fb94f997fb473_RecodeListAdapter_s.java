 package jp.ddo.haselab.timerecoder;
 
 import java.util.List;
 
 import android.widget.BaseAdapter;
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.widget.TextView;
 
 import jp.ddo.haselab.timerecoder.util.RecodeDateTime;
 import jp.ddo.haselab.timerecoder.dataaccess.Recode;
 import jp.ddo.haselab.timerecoder.util.MyLog;
 
 /**
  *
  * @author T.Hasegawa
  */
 final class RecodeListAdapter extends BaseAdapter {
 
     private List<Recode> data;
     private final Context context;
 
     public RecodeListAdapter(final Context argContext,
 			     final List<Recode> argData){
 	MyLog.getInstance().verbose("start");
 	context = argContext;
 	data = argData;
     }
 
     public void addData(final Recode argData){
 	data.add(argData);
 	notifyDataSetChanged();
    }
 
     @Override
         public int getCount() {
 	MyLog.getInstance().verbose("called.result["+ data.size() + "]");
 	return data.size();
     }
  
     @Override
         public long getItemId(final int argPosition) 
 {	MyLog.getInstance().verbose("argPosition["+ argPosition + "]");
 	return argPosition;
     }
 
     @Override
         public Object getItem(final int argPosition) {
 	MyLog.getInstance().verbose("argPosition["+ argPosition + "]");
 	MyLog.getInstance().verbose("result["+ data.get(argPosition) + "]");
 	return data.get(argPosition);
     }
 
     @Override
         public View getView(final int position,
 			    final View convertView,
 			    final ViewGroup parentViewGroup) {
 	View resultView = convertView;
 	if (convertView == null) {
 	    LayoutInflater inflater = (LayoutInflater)
 		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	    resultView  = inflater.inflate(R.layout.recode_item,
					   null);
 	} 
 	Recode rec = (Recode)this.getItem(position);
 
 	TextView number = (TextView)resultView.findViewById(R.id.number);
 	number.setText(position + "");
 
 	TextView dateTime = (TextView)resultView.findViewById(R.id.datetime);
 	dateTime.setText(rec.getDateTime().toString());
 
 	TextView event = (TextView)resultView.findViewById(R.id.eventid);
 	event.setText(rec.getEventToString());
 
 	TextView memo = (TextView)resultView.findViewById(R.id.memo);
 	memo.setText(rec.getMemo());
 
 	return resultView;
     }
 }

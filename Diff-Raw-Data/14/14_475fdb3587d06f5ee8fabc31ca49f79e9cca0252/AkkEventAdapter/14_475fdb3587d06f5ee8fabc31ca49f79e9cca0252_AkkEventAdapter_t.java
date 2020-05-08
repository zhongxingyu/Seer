 package org.akk.akktuell.Activity;
 
 import org.akk.akktuell.R;
 import org.akk.akktuell.Model.AkkEvent;
 import org.akk.akktuell.Model.AkkEvent.AkkEventType;
 import org.akk.akktuell.Model.InfoManager;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 
 /**
  * The Class AkkEventAdapter. It uses an array of akkevent and displays them
  * 
  * @author Philip Flohr
  */
 public class AkkEventAdapter extends ArrayAdapter<AkkEvent> {
 	
 	/** The context. */
 	private final Context context;
 	
 	/** The events. */
 	private final AkkEvent[] events;
 
 	/**
 	 * Instantiates a new akk event adapter.
 	 *
 	 * @param context the context
 	 * @param events the events
 	 * @param infoManager the info manager
 	 */
 	public AkkEventAdapter(Context context, AkkEvent[] events, InfoManager infoManager) {
 		super(context, R.layout.main_activity_list_item, events);
 		this.context = context;
 		this.events = events;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View listItemView = inflater.inflate(R.layout.main_activity_list_item, parent, false);
 		TextView eventNameView = (TextView) listItemView.findViewById(R.id.listitem_eventname);
 		TextView eventDateView = (TextView) listItemView.findViewById(R.id.listitem_eventdate);
 		//CheckBox eventInCalendar = (CheckBox) listItemView.findViewById(R.id.listitem_incalendar);
 		
 		eventNameView.setText(events[position].getEventName());
 		eventDateView.setText("testDate");
 		/*if (infoManager.isInCalendar(events[position])) {
 			eventInCalendar.setChecked(true);
 		} else {
 			eventInCalendar.setChecked(false);
 		}*/
 		return listItemView;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.widget.BaseAdapter#isEnabled(int)
 	 */
 	@Override
 	public boolean isEnabled(int location) {
		//TODO: ???!!!???
 		return (location >= 0 && location < events.length) ? (events[location - 1].getEventType().equals(AkkEventType.Schlonz)) : false;
 	}
 }

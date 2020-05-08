 package br.mello.arthur.correcuritiba;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class DetailAdapter extends ArrayAdapter<Detail> {
 	private Context context;
 	private int textViewResourceId;
 	private Detail[] objects;
 
 	public DetailAdapter(Context context, int textViewResourceId, Detail[] objects) {
 		super(context, textViewResourceId, objects);
 		this.context = context;
 		this.textViewResourceId = textViewResourceId;
 		this.objects = objects;
 
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 		View item = inflater.inflate(textViewResourceId, parent, false);
 
 		final Detail detail = objects[position];
 
 		TextView title = (TextView) item.findViewById(R.id.detailtitle);
 		title.setText(detail.getTitle());
 
 		TextView detailText = (TextView) item.findViewById(R.id.detail);
 		detailText.setText(detail.getDetail());
 		detailText.setAutoLinkMask(Linkify.WEB_URLS);
 
 		if (detail.getClass() == DateDetail.class) {
 			adaptDateDetail((DateDetail)detail, (ImageButton)item.findViewById(R.id.icon));
 		}
 
 		return item;
 	}
 
 	private void adaptDateDetail(final DateDetail detail, ImageButton icon) {
 		icon.setVisibility(View.VISIBLE);
 		icon.setImageResource(detail.getIconRes());
 		icon.setBackgroundResource(R.drawable.press_selector);
 		icon.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Event event = detail.getEvent();
 
 				Intent intent = new Intent(Intent.ACTION_EDIT);
 				intent.setType("vnd.android.cursor.item/event");        
 				intent.putExtra("beginTime", event.getDate());     
 				intent.putExtra("allDay", true);
 
 				if (detail.getTitle().equals(context.getString(R.string.date_title))) {
 					intent.putExtra("title", event.getName());
 				} else {
 					intent.putExtra("title", detail.getTitle() + " - " + event.getName());
 				}
 				intent.putExtra("description", event.getDescription());
 				intent.putExtra("eventLocation", event.getLocal());
				context.startActivity(intent);
 			}
 		});
 
 	}
 }

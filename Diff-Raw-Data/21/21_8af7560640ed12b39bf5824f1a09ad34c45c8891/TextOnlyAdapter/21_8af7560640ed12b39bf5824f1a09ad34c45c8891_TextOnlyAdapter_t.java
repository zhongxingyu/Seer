 package be.rottenrei.android.lib.view;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import be.rottenrei.android.lib.R;
 
 /**
  * Simple adapter that converts every object into a simple string set on a TextView.
  */
 public abstract class TextOnlyAdapter<ObjectType> extends ArrayAdapter<ObjectType> {
 
	private int listItemLayoutId = R.layout.list_item;

	/** Use this e.g. for Spinners, which need R.layout.spinner_list_item. */
	public TextOnlyAdapter(Context context, List<ObjectType> objects, int listItemLayoutId) {
		super(context, listItemLayoutId, objects);
		this.listItemLayoutId = listItemLayoutId;
	}

	/** Uses R.layout.list_item for the list items. */
 	public TextOnlyAdapter(Context context, List<ObjectType> objects) {
		this(context, objects, R.layout.list_item);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		TextView view;
 		if (convertView != null && convertView instanceof TextView) {
 			view = (TextView) convertView;
 		} else {
			view = (TextView) LayoutInflater.from(getContext()).inflate(listItemLayoutId, parent, false);
 		}
 		view.setText(getText(getItem(position)));
 		attachViewListeners(position, view);
 		return view;
 	}
 
 	@SuppressWarnings("unused")
 	protected void attachViewListeners(int position, TextView view) {
 		// empty default implementation
 	}
 
 	abstract protected CharSequence getText(ObjectType item);
 
 }

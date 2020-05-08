 package com.sun.multipleselector.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import android.content.Context;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import com.sun.multipleselector.api.MultipleSelector;
 import com.sun.multipleselector.api.OnTextViewLoad;
 
 public class MultipleSelectorArrayAdapter<T> extends ArrayAdapter<T> implements
 		MultipleSelector<T> {
 
 	private int _checkBox;
 	private int _textView;
 	private LayoutInflater _inflator;
 	private int _layout;
 	private ViewHolder _viewHolder;
 	private OnTextViewLoad<T> _onTextLoadCallback;
 	private SparseBooleanArray _listItemStatus;
 
 	public MultipleSelectorArrayAdapter(Context context_, int resource_,
 			int checkBox_, int textView_, List<T> iteamList_) {
 		super(context_, resource_, iteamList_);
 		_checkBox = checkBox_;
 		_textView = textView_;
 		_inflator = LayoutInflater.from(context_);
 		_layout = resource_;
 		_listItemStatus = new SparseBooleanArray();
 	}
 
 	public MultipleSelectorArrayAdapter(Context context_, int resource_,
 			int checkBox_, int textView_, List<T> iteamList_,
 			OnTextViewLoad<T> onTextLoadCallback_) {
 		super(context_, resource_, iteamList_);
 		_checkBox = checkBox_;
 		_textView = textView_;
 		_inflator = LayoutInflater.from(context_);
 		_layout = resource_;
 		_listItemStatus = new SparseBooleanArray();
 		_onTextLoadCallback = onTextLoadCallback_;
 
 	}
 
 
 	public void addAllToAdapter(Collection<T> items_) {
 		addAll(items_);
 		notifyDataSetChanged();
 	}
 
 	public void clearAdapter() {
 		clear();
 		notifyDataSetChanged();
 	}
 
 	public void remove(T object_) {
 		remove(object_);
 		notifyDataSetChanged();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public View getView(int position_, View convertView_, ViewGroup parent_) {
 		TextView listItemText = null;
 		CheckBox checkBox = null;
 
 		if (convertView_ == null) {
 
 				convertView_ = _inflator.inflate(_layout, parent_, false);
 
 			try {
 
 				listItemText = (TextView) convertView_.findViewById(_textView);
 				checkBox = (CheckBox) convertView_.findViewById(_checkBox);
 				_viewHolder = new ViewHolder();
 				_viewHolder._textView = listItemText;
 				_viewHolder._checkBox = checkBox;
 				_viewHolder._checkBox.setId(position_);
 				convertView_.setTag(_viewHolder);
 
 			} catch (ClassCastException e) {
 				throw new IllegalStateException(
 						"MultipleSelectorArrayAdapter requires two resource IDs to be TextView and CheckBox",
 						e);
 			}
 		} else {
 			_viewHolder = (ViewHolder) convertView_.getTag();
 		}
 
 		registerCheckBoxListener(_viewHolder._checkBox, position_,
 				_listItemStatus);
 		T item = getItem(position_);
 		performPostTextViewLoad(_onTextLoadCallback, _viewHolder._textView,
 				item);
 		_viewHolder._checkBox.setChecked(_listItemStatus.get(position_, false));
 
 
 		return convertView_;
 
 	}
 
 	private void performPostTextViewLoad(OnTextViewLoad<T> callback_,
 			TextView view_, T item_) {
 		if (callback_ != null) {
 			callback_.onPostTextViewLoad(view_, item_);
 		} else {
 			if (item_ instanceof CharSequence) {
 				view_.setText((CharSequence) item_);
 			} else {
 				view_.setText(item_.toString());
 			}
 		}
 	}
 
 	@Override
 	public Collection<T> getAllSelectedItems() {
 		Collection<T> selected = new ArrayList<T>();
 		for (int i = 0; i < _listItemStatus.size(); i++) {
 			int key = _listItemStatus.keyAt(i);
 			if (_listItemStatus.get(key)) {
 				selected.add(getItem(key));
 			}
 		}
 		return selected;
 	}
 
 	private void registerCheckBoxListener(final CheckBox checkBox_,
 			final int position_, final SparseBooleanArray sparseBool_) {
 		
 		
 //		checkBox_.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 //			
 //			@Override
 //			public void onCheckedChanged(CompoundButton checkBox_, boolean isChecked_) {
 //			
 //				sparseBool_.put(position_, isChecked_);
 //				checkBox_.setChecked(sparseBool_.get(checkBox_.getId(), false));
 //				
 //			}
 //		});
 		
 		checkBox_.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				
 				CheckBox checkBox = (CheckBox) v;
 				boolean status = checkBox.isChecked();
 				sparseBool_.put(position_, status);
 				checkBox.setChecked(sparseBool_.get(position_, false));
 
 			}
 		});
 		
 	}
 
 	private class ViewHolder {
 		private TextView _textView;
 		private CheckBox _checkBox;
 	}
 
 }

 package com.epam.AnotherSearch;
 
 import com.epam.Suggestions.ISuggestion;
 import com.epam.Suggestions.Suggestions;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SearchAdapter extends BaseAdapter {
 
 	public SearchAdapter(Context context)
 	{
 		super();
 		mSuggestions = new Suggestions(context);
 	}
 	public void setQuery(CharSequence s)
 	{
 		
 		mSuggestions.setQuery(s);
 		mSuggestions.reload();
 		notifyDataSetChanged();
 	}
 	public int getCount() {
 		if(mSuggestions.getQuery() == null || mSuggestions.getQuery().toString().isEmpty())
 		{
 			return 0;
 		}
 		int count = 0;
 		for (ISuggestion suggestion : mSuggestions.getSuggestions()) {
 			if(suggestion.getCount() > 0)
 			{
 				count ++;
 				count += suggestion.getCount();
 			}
 		}
 		return count;
 	}
 
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public long getItemId(int pos) {
 		// TODO Auto-generated method stub
 		return pos;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LinearLayout layout = null;
 		TextView textView = null;
 		ImageView iconView = null;
 		try
 		{
 			
 			SuggestionIndex index = findSuggestion(position);
 			SuggestionData data = getSuggestionData(index);
 			layout = (LinearLayout)convertView;
 			
 			if(layout == null)
 			{
 				layout = new LinearLayout(parent.getContext());
				
 				LinearLayout.LayoutParams textViewParams = 
 	        			new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
 	        	LinearLayout.LayoutParams imageViewParams = 
 	        			new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
 	        	        	
 	        	textViewParams.weight = 0.80f;
 	        	imageViewParams.weight = 0.20f;
 	        	
 				textView = new TextView(parent.getContext());
 				iconView = new ImageView(parent.getContext());
 								iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 				layout.addView(textView, textViewParams);
 				layout.addView(iconView, imageViewParams);
 				
 			}
 			else
 			{
 				textView = (TextView)layout.getChildAt(0);
 				iconView = (ImageView)layout.getChildAt(1);
 			}
 			textView.setText(data.getText());
 			iconView.setImageDrawable(data.getIcon());
 			if(index.isCategory())
 			{
 				layout.setBackgroundColor(Color.GRAY);
 			}
 			else
 			{
				layout.setBackgroundColor(layout.getDrawingCacheBackgroundColor());				
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
			
 		}
 		return layout;
 	}
 	
 	//Implementation
 		
 	
 	private SuggestionData getSuggestionData(SuggestionIndex index)
 	{
 		
 		SuggestionData data = new SuggestionData(null, null);
 		if (index.isCategory()){
 			data.setIcon(index.getSuggestion().getIcon());
 			data.setText(index.getSuggestion().getText() + (  
 			index.getSuggestion().getHint() != null ?  "\n" + index.getSuggestion().getHint() : "") );
 		}
 		else
 		{
 			data.setIcon(index.getSuggestion().getIcon(index.getIndex()));
 			data.setText(index.getSuggestion().getText(index.getIndex()));
 		}
 		return data;
 	}
 	
 	private SuggestionIndex findSuggestion(int pos)
 	{
 		int i = 0;
 	
 		for (ISuggestion suggestion : mSuggestions.getSuggestions()) {
 			if(suggestion.getCount() <= 0)
 			{
 				 continue;
 				 
 			}
 			if (i == pos || pos <= i + suggestion.getCount() )
 			{
 				Integer index = null;
 				if (i != pos)
 				{
 					index = i + suggestion.getCount() - pos;
 				}
 				SuggestionIndex sIndex = new SuggestionIndex(suggestion, index);
 				sIndex.setIsCategory(i == pos);
 								
 				return sIndex;
 			}
 			
 			i+= suggestion.getCount() + 1;
 			
 		}
 		return null;
 	}
 	
 	//Data
 	private class SuggestionIndex
 	{
 		public SuggestionIndex(ISuggestion suggestion, Integer index)
 		{
 			super();
 			setSuggestion(suggestion);
 			setIndex(index);
 		}
 		
 		public ISuggestion getSuggestion() {
 			return suggestion;
 		}
 		public void setSuggestion(ISuggestion suggestion) {
 			this.suggestion = suggestion;
 		}
 		public Integer getIndex() {
 			return index;
 		}
 		public void setIndex(Integer index) {
 			this.index = index;
 		}
 		
 		private Boolean isCategory() {
 			return isCategory;
 		}
 
 		private void setIsCategory(Boolean isCategory) {
 			this.isCategory = isCategory;
 		}
 
 		private ISuggestion suggestion = null;
 		private Integer index = null;
 		private Boolean isCategory = false;
 	}
 	
 	private class SuggestionData
 	{
 		public SuggestionData(String text,Drawable icon)
 		{
 			setIcon(icon);
 			setText(text);
 		}
 		public Drawable getIcon() {
 			return icon;
 		}
 		public void setIcon(Drawable icon) {
 			this.icon = icon;
 		}
 		public String getText() {
 			return text;
 		}
 		public void setText(String text) {
 			this.text = text;
 		}
 		private Drawable icon;
 		private String text;
 		
 	}
 	private Suggestions mSuggestions;
 }

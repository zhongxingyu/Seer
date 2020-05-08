 package com.epam.AnotherSearch;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.epam.search.Search;
 import com.epam.search.data.ProvidersPack;
 import com.epam.search.data.SuggestionProvider;
 import com.epam.search.util.IconObtainer;
 
 
 public class SettingsAdapter extends BaseAdapter {
 
 	public SettingsAdapter(Context context) {
 		mSearch = new FullSearchComposer(context).getSearch();
		
 	}
 	
 	public int getCount() {
 		
 		int count = 0;
 		for (ProvidersPack pack : mSearch.getProvidersPacks()) {
 			count += pack.providers().size();
 		}
 		return count;
 	}
 
 	public Object getItem(int position) {
 		
 		return null;
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		final SuggestionProvider provider = getProvider(position);
 		LinearLayout layout = (LinearLayout)convertView;
 		TextView textView = null;
 		ImageView imageView = null;
 		CheckBox checkBox = null;
 		if(layout == null)
 		{
 			layout = new LinearLayout(parent.getContext());
 			textView = new TextView(parent.getContext());
 			imageView = new ImageView(parent.getContext());
 			checkBox = new CheckBox(parent.getContext());
 			LinearLayout.LayoutParams params = 
 					new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
 			params.weight = 10;
 			layout.addView(imageView, params);
 			params.weight = 80;
 			layout.addView(textView, params);
 			params.weight = 10;
 			layout.addView(checkBox, params);
 			
 			
 		}
 		else
 		{
 			imageView = (ImageView)layout.getChildAt(0);
 			textView = (TextView)layout.getChildAt(1);
 			checkBox = (CheckBox)layout.getChildAt(2);
 		}
 		
 		textView.setText(provider.getName());
 		final IconObtainer obtainer = provider.getIcon();
 		final ImageView imageViewWillUpdated = imageView;
 		obtainer.setIconReadyListener(new Runnable() {
 			
 			public void run() {
 				Activity a = (Activity)imageViewWillUpdated.getContext();
 				a.runOnUiThread(new Runnable() {
 					
 					public void run() {
 						imageViewWillUpdated.setImageDrawable(obtainer.getIcon(mPlaceholder));
 						notifyDataSetChanged();
 					}
 				});
 				
 			}
 		});
 		
 		imageView.setImageDrawable(obtainer.getIcon(mPlaceholder));
 		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 		    {
 		    	try
 				{
 					
 					mSearch.getSettings().swithcProviderOn(provider, isChecked);
 				}catch (NullPointerException e) {
 					throw new IllegalStateException("Suggestions settings must not be null, see Suggestions.setSettings(SearchSettings)");
 				}
 		    }
 		});
 		try
 		{
 		checkBox.setChecked(mSearch.getSettings().isProviderOn(provider));
 		}catch (NullPointerException e) {
 			throw new IllegalStateException("Suggestions settings must not be null, see Suggestions.setSettings(SearchSettings)");
 		}
 		return layout;
 	}
 	
 	private SuggestionProvider getProvider(int i) {
 		SuggestionProvider provider = null;
 		int index = 0;
 		for (ProvidersPack pack : mSearch.getProvidersPacks()) {
 			if(i == index || i < index + pack.providers().size())
 			{
 				int at = (i + pack.providers().size()) - (index + pack.providers().size());
 				return pack.providers().get(at);
 			}
 			index += pack.providers().size();
 		}
 		return provider;
 	}
 	Search mSearch = null;
 	Drawable mPlaceholder = null;
 }

 package com.bytopia.oboobs.adapters;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 
 import com.bytopia.oboobs.R;
 import com.bytopia.oboobs.model.Boobs;
 import com.bytopia.oboobs.providers.ImageProvider;
 import com.bytopia.oboobs.utils.Utils;
 import com.commonsware.cwac.endless.EndlessAdapter;
 
 public class BoobsEndlessAdapter extends EndlessAdapter {
 
 	BoobsListAdapter srcAdapter;
 
 	List<Boobs> cache;
 
 	private int currentOffset = 0;
 
 	ImageProvider provider;
 
 	public BoobsEndlessAdapter(BoobsListAdapter wrapped, ImageProvider provider) {
		super(wrapped.getContext(),wrapped,provider.isInfinitive()?R.layout.pending:android.R.layout.simple_list_item_1);
 		srcAdapter = wrapped;
 		this.provider = provider;
 	}
 
 	@Override
 	protected boolean cacheInBackground() throws Exception {
 
		currentOffset += Utils.getBoobsChunk();
		cache = provider.getBoobs(currentOffset);

		return true;
 	}
 
 	@TargetApi(11)
 	@Override
 	protected void appendCachedData() {
 		if (cache != null) {
			if(Build.VERSION.SDK_INT>=11){
 				srcAdapter.addAll(cache);
			}else{
				for(Boobs b : cache){
 					srcAdapter.add(b);
 				}
 			}
 			cache = null;
 		}
 
 	}
 
 }

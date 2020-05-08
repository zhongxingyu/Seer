 package org.geekosphere.zeitgeist.view.adapter;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.geekosphere.zeitgeist.data.ZGItem;
 import org.geekosphere.zeitgeist.net.WebRequestBuilder;
 import org.geekosphere.zeitgeist.processor.ZGItemProcessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import at.diamonddogs.data.dataobjects.CacheInformation;
 import at.diamonddogs.data.dataobjects.WebRequest;
 import at.diamonddogs.service.net.HttpService.WebRequestReturnContainer;
 import at.diamonddogs.service.net.HttpServiceAssister;
 import at.diamonddogs.service.processor.ImageProcessor;
 
 import com.commonsware.cwac.endless.EndlessAdapter;
 
 public class EndlessImageListAdapter extends EndlessAdapter {
 	private static final Logger LOGGER = LoggerFactory.getLogger(EndlessImageListAdapter.class);
 
 	private HttpServiceAssister assister;
 	private Bitmap cachedItem;
 
 	private final AtomicInteger currentItem = new AtomicInteger(-1);
 
 	public EndlessImageListAdapter(Context context) {
 		super(context, new ZGAdapter(context), -1);
 		assister = new HttpServiceAssister(context);
 		assister.bindService();
 	}
 
 	@Override
 	protected View getPendingView(ViewGroup parent) {
 		TextView tv = new TextView(getContext());
 		tv.setText("WAIT...");
 		return tv;
 	}
 
 	@Override
 	protected boolean cacheInBackground() throws Exception {
 		cachedItem = getNextItem();
 		return cachedItem != null;
 	}
 
 	private Bitmap getNextItem() {
 		WebRequestBuilder wrb = new WebRequestBuilder();
 		WebRequest wr;
 		if (currentItem.get() == -1) {
 			wr = wrb.getItems().page(1).build();
 			wrb.reset();
 			ZGItem[] items = (ZGItem[]) ((WebRequestReturnContainer) assister.runSynchronousWebRequest(wr, new ZGItemProcessor()))
 			        .getPayload();
 			currentItem.set(items[0].getId());
 		}
 		wr = wrb.getItems().withId(currentItem.get()).build();
 
 		LOGGER.info("Getting thumbnail (info url):" + wr.getUrl());
		ZGItem[] items = (ZGItem[]) ((WebRequestReturnContainer) assister.runSynchronousWebRequest(wr, new ZGItemProcessor())).getPayload();
 		currentItem.decrementAndGet();
		String url = "http://zeitgeist.li" + items[0].getRelativeThumbnailPath();
		LOGGER.info("Getting thumbnail: " + url);
		// TODO: put in WebRequestBuilder
		WebRequest imageWr = new WebRequest();
		imageWr.setUrl(url);
		imageWr.setProcessorId(ImageProcessor.ID);
		imageWr.setCacheTime(CacheInformation.CACHE_7D);
		return (Bitmap) ((WebRequestReturnContainer) assister.runSynchronousWebRequest(imageWr, new ImageProcessor())).getPayload();
 	}
 
 	@Override
 	protected void appendCachedData() {
 		((ZGAdapter) getWrappedAdapter()).add(cachedItem);
 	}
 
 	private static final class ZGAdapter extends ArrayAdapter<Bitmap> {
 
 		public ZGAdapter(Context context) {
 			super(context, -1);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = new ImageView(getContext());
 			}
 			Bitmap item = getItem(position);
 			ImageView imageView = (ImageView) convertView;
 			imageView.setImageBitmap(item);
 			return convertView;
 		}
 	}
 
 }

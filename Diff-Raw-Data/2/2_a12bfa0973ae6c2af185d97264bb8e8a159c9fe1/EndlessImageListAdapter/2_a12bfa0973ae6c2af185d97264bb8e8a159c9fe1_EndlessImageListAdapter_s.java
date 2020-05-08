 package org.geekosphere.zeitgeist.view.adapter;
 
 import java.util.ArrayList;
 
 import org.geekosphere.zeitgeist.R;
 import org.geekosphere.zeitgeist.data.ZGItem;
 import org.geekosphere.zeitgeist.data.ZGItem.ZGItemType;
 import org.geekosphere.zeitgeist.net.WebRequestBuilder;
 import org.geekosphere.zeitgeist.processor.ZGItemProcessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import at.diamonddogs.data.dataobjects.CacheInformation;
 import at.diamonddogs.data.dataobjects.WebRequest;
 import at.diamonddogs.service.net.HttpServiceAssister;
 import at.diamonddogs.service.processor.ImageProcessor;
 
 import com.commonsware.cwac.endless.EndlessAdapter;
 
 public class EndlessImageListAdapter extends EndlessAdapter {
 	private static final Logger LOGGER = LoggerFactory.getLogger(EndlessImageListAdapter.class);
 
 	private HttpServiceAssister assister;
 	private int page = 0;
 	private Bitmap[] cachedItems;
 
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
 		cachedItems = getNextPageItems();
 		return cachedItems.length > 0;
 	}
 
 	private Bitmap[] getNextPageItems() {
 		page++;
 		WebRequestBuilder wrb = new WebRequestBuilder();
 		WebRequest wr = wrb.getItems().page(page).build();
 		LOGGER.info("Getting page " + page + " " + wr.getUrl());
 		ZGItem[] items = (ZGItem[]) assister.runSynchronousWebRequest(wr, new ZGItemProcessor());
 		LOGGER.info(items.length + " items per page!");
 		ArrayList<Bitmap> ret = new ArrayList<Bitmap>(items.length);
 		for (ZGItem item : items) {
 			LOGGER.debug("ITEM --> " + item);
 			String url = "http://zeitgeist.li" + item.getRelativeThumbnailPath();
 			WebRequest imageWr = new WebRequest();
 			imageWr.setUrl(url);
 			imageWr.setProcessorId(ImageProcessor.ID);
 			imageWr.setCacheTime(CacheInformation.CACHE_7D);
 			ret.add((Bitmap) assister.runSynchronousWebRequest(imageWr, new ImageProcessor()));
 		}
 		return ret.toArray(new Bitmap[ret.size()]);
 	}
 
 	@Override
 	protected void appendCachedData() {
 		Bitmap[] toAppend = new Bitmap[5];
 		int i = 0;
 		for (Bitmap b : cachedItems) {
			if (i == 4) {
 				i = 0;
 				toAppend = new Bitmap[5];
 				((ZGAdapter) getWrappedAdapter()).add(toAppend);
 			} else {
 				toAppend[i] = b;
 				i++;
 			}
 		}
 	}
 
 	private static final class ZGAdapter extends ArrayAdapter<Bitmap[]> {
 
 		public ZGAdapter(Context context) {
 			super(context, -1);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(getContext()).inflate(R.layout.imagelistitem, parent, false);
 				ViewHolder vh = new ViewHolder();
 				vh.img1 = (ImageView) convertView.findViewById(R.id.imagelistitem_1);
 				vh.img2 = (ImageView) convertView.findViewById(R.id.imagelistitem_2);
 				vh.img3 = (ImageView) convertView.findViewById(R.id.imagelistitem_3);
 				vh.img4 = (ImageView) convertView.findViewById(R.id.imagelistitem_4);
 				vh.img5 = (ImageView) convertView.findViewById(R.id.imagelistitem_5);
 				convertView.setTag(vh);
 			}
 			ViewHolder holder = (ViewHolder) convertView.getTag();
 			Bitmap[] items = getItem(position);
 			holder.img1.setImageBitmap(items[0]);
 			holder.img2.setImageBitmap(items[1]);
 			holder.img3.setImageBitmap(items[2]);
 			holder.img4.setImageBitmap(items[3]);
 			holder.img5.setImageBitmap(items[4]);
 			return convertView;
 		}
 	}
 
 	private static final class ViewHolder {
 		ImageView img1, img2, img3, img4, img5;
 	}
 }

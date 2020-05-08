 package com.quanleimu.view;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.Toast;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.NetworkProtocols;
 
 public class BigGalleryView extends BaseView implements OnItemSelectedListener{
 
 	int index = 0;
 	private int postIndex = -1;
 	public GoodsDetail goodsDetail;
 	public List<String> listUrl = new ArrayList<String>();
 	private Bitmap mb;
 	private HashMap<String, byte[]> imageData;
 	
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.biggallery, null));
 		
 		try {
 			if(goodsDetail.getImageList().getBig() == null || goodsDetail.getImageList().getBig().equals(""))
 			{
 				if(null != m_viewInfoListener){
 					TitleDef title = getTitleDef();
 					title.m_title = "0/0";
 					m_viewInfoListener.onTitleChanged(title);
 				}
 				Toast.makeText(getContext(), "图片未加载成功，请稍后重试", 3).show();
 			}
 			else
 			{
 				String b = (goodsDetail.getImageList().getBig()).substring(1, (goodsDetail.getImageList().getBig()).length()-1);
 				b = Communication.replace(b);
 				if(b.contains(","))
 				{
 					String[] c = b.split(",");
 					for(int i=0;i<c.length;i++) 
 					{
 						listUrl.add(c[i]);
 					}
 				}
 				else
 				{
 					listUrl.add(b);
 				}
 				
 				Gallery vfCoupon = (Gallery)findViewById(R.id.vfCoupon);
 				vfCoupon.setAdapter(new GalleryImageAdapter(getContext(), listUrl));
 				vfCoupon.setOnItemSelectedListener(this);
 				vfCoupon.setSelection(postIndex);
 				BitmapFactory.Options o =  new BitmapFactory.Options();
                 o.inPurgeable = true;
 				Bitmap tmb = BitmapFactory.decodeResource(BigGalleryView.this.getResources(),R.drawable.loading_210_black, o);
 				mb= Helper.toRoundCorner(tmb, 20);
 				tmb.recycle();				
 			}
 		} catch (Exception e) {
 			
 		}
 	}
 	
 	public BigGalleryView(Context context, Bundle bundle){
 		super(context);
 		
 		postIndex = bundle.getInt("postIndex");
 		goodsDetail = (GoodsDetail) bundle.getSerializable("goodsDetail");
 		
 		Init();
 	}
 
 	
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = (postIndex+1)+"/"+listUrl.size();
 		title.m_leftActionHint = "返回";
 		return title;
 	}
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 	
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         
         if(mb != null)
         {
             mb.recycle();
         }
         
         imageData = null;
         goodsDetail = null;
     }
 
     class GalleryImageAdapter extends BaseAdapter
     {
         private Context context;
 
         private List<String> imageUrls;
 
         private int position = 0;
 
         private final ExecutorService pool;
 
         public GalleryImageAdapter(Context c, List<String> imageUrls)
         {
             this.context = c;
             this.imageUrls = imageUrls;
 
             imageData = new HashMap<String, byte[]>();
             pool = Executors.newFixedThreadPool(5);
         }
 
         public void loadBitmap(final String url, final ImageView imageView)
         {
             final Bitmap bitmap = getBitmapFromCache(url);
             if (bitmap != null)
             {
                 imageView.setImageBitmap(bitmap);
             }
             else
             {
                 imageView.setImageBitmap(mb);
                 pool.submit(new Runnable()
                 {
                     @Override
                     public void run()
                     {
                         byte[] data = downloadBitmap(url);
                         imageData.put(url, data);
                         
                         BitmapFactory.Options o = new BitmapFactory.Options();
                         o.inPurgeable = true;
                         final Bitmap tmb = BitmapFactory.decodeByteArray(data, 0, data.length, o);
                         
                         ((Activity) context).runOnUiThread(new Runnable()
                         {
                             public void run()
                             {
                                 imageView.setImageBitmap(tmb);
                             }
                         });
                     }
                 });
             }
         }
 
         private byte[] downloadBitmap(String url)
         {
             try
             {
                 HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
 
                 HttpPost httpPost = new HttpPost(url);
                 HttpResponse response = httpClient.execute(httpPost);
 
                 InputStream is = response.getEntity().getContent();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 byte[] buffer = new byte[1024];
                 int length;
                 while((length = is.read(buffer)) != -1)
                 {
                     bos.write(buffer, 0, length);
                 }
                 httpClient.getConnectionManager().shutdown();
 
                 return bos.toByteArray();
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
 
             return null;
         }
 
         public Bitmap getBitmapFromCache(String url)
         {
            if (imageData != null && !imageData.containsKey(url))
             {
                 return null;
             }
 
             byte[] data = imageData.get(url);
             
             if(data == null)
             {
                 return null;
             }
             
             BitmapFactory.Options o = new BitmapFactory.Options();
             o.inPurgeable = true;
             Bitmap tmb = BitmapFactory.decodeByteArray(data, 0, data.length, o);
             return tmb;
         }
 
         @Override
         public int getCount()
         {
             return this.imageUrls.size();
         }
 
         @Override
         public Object getItem(int position)
         {
             return this.position;
         }
 
         @Override
         public long getItemId(int position)
         {
             return this.position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent)
         {
             if(convertView instanceof ImageView)
             {
                 ((BitmapDrawable)((ImageView)convertView).getDrawable()).getBitmap().recycle();
             }
             ImageView imageView = new ImageView(context);
             imageView.setScaleType(ScaleType.FIT_CENTER);
             imageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.FILL_PARENT));
             loadBitmap(imageUrls.get(position), imageView);
 
             return imageView;
 
         }
     }
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
     {
 		if(null != m_viewInfoListener){
 			TitleDef title = getTitleDef();
 			title.m_title = (position + 1)+"/"+listUrl.size();
 			m_viewInfoListener.onTitleChanged(title);
 		}
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> arg0)
     {
         // TODO Auto-generated method stub
         
     }
 }

 package project.DirectionOfPhoto;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ExifInterface;
 import android.sax.StartElementListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class ImageAdater extends BaseAdapter {
 	private LayoutInflater myInflater;
 	private int item;
 	private List<String> data;
 	private Context context;
 
 	public ImageAdater(Context c, int layout, List<String> d) {
 		// TODO Auto-generated constructor stub
 		myInflater = LayoutInflater.from(c);
 		item = layout;
 		data = d;
 		context = c;
 	}
 
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return data.size();
 	}
 
 	public Object getItem(int position) {
 		return position;
 	}
 
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		convertView = myInflater.inflate(item, null);
 		ImageView img = (ImageView) convertView.findViewById(R.id.image);
 
 		Bitmap bm = BitmapFactory.decodeFile(data.get(position));
 		img.setImageBitmap(bm);
 		img.setClickable(false);
 
 		ImageView star = (ImageView) convertView.findViewById(R.id.star);
 		ExifInterface exifInterface;
 		try {
 			exifInterface = new ExifInterface(data.get(position));
 			float[] mapAddr = new float[2];
 			if (exifInterface.getLatLong(mapAddr)) {
 				star.setImageResource(android.R.drawable.star_big_on);
 			}
 			else
 				star.setImageResource(android.R.drawable.star_big_off);
 		} catch (IOException e) { // TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		/*
 		 * img.setOnClickListener(new OnClickListener() {
 		 * 
 		 * @Override public void onClick(View arg0) { Toast.makeText(context,
 		 * "111", Toast.LENGTH_SHORT).show(); Log.v("test", data.get(position));
 		 * 
 		 * Intent myIntent = new Intent(context, MapActivity.class);
 		 * myIntent.putExtra("Image", data.get(position)); } });
 		 */
 		return convertView;
 	}
 
 }

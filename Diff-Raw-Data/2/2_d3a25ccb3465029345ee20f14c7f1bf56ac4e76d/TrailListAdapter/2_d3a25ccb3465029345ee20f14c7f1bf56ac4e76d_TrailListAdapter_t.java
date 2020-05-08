 package ch.hsr.challp.and4.adapter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ch.hsr.challp.and.R;
 import ch.hsr.challp.and4.domain.Trail;
 import ch.hsr.challp.and4.technicalservices.UserLocationListener;
 
 public abstract class TrailListAdapter extends ArrayAdapter<Trail> implements
 		Observer {
 	protected ArrayList<Trail> trails;
 	private Context context;
 	private View v;
 	private Trail trail;
 	private ImageView listImg;
 
 	public TrailListAdapter(Context context, int textViewResourceId,
 			ArrayList<Trail> trails) {
 		super(context, textViewResourceId, trails);
 		this.context = context;
 		this.trails = trails;
 		UserLocationListener.getInstance().addObserver(this);
 	}
 
 	public abstract void update(Observable observable, Object data);
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		v = convertView;
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.list_entry, null);
 		}
 
 		trail = trails.get(position);
 		try {
 			setTrailIcon();
 		} catch (Exception e) {
 			Log.d("tag", "filtrino: " + e.toString() + "");
 		}
 		if (trail != null) {
 			listImg = (ImageView) v.findViewById(R.id.status_icon);
 
 			TextView parkname = (TextView) v.findViewById(R.id.list_parkname);
 			TextView info = (TextView) v.findViewById(R.id.list_info);
 			TextView date = (TextView) v.findViewById(R.id.list_date);
 
 			if (parkname != null && info != null && date != null) {
 				parkname.setText(trail.getName());
 				info.setText(getDistanceText(trail));
 				date.setText(Html.fromHtml(trail.getDescription()).toString());
 			}
 		}
 		return v;
 	}
 
 	private static String getDistanceText(Trail trail) {
 		StringBuilder distance = new StringBuilder();
 		try {
 			float[] results = new float[3];
 			if (UserLocationListener.getInstance().getLatitude() > 0
 					&& UserLocationListener.getInstance().getLongitude() > 0) {
 				Location.distanceBetween(trail.getGmapX(), trail.getGmapY(),
 						UserLocationListener.getInstance().getLatitude(),
 						UserLocationListener.getInstance().getLongitude(),
 						results);
 				distance.append(Math.round(results[0] / 1000));
 				if (trail.getNextCity() != null
 						&& !trail.getNextCity().equals("null")) {
 					distance.append(" km, ");
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		if (trail.getNextCity() != null && !trail.getNextCity().equals("null")) {
 			distance.append(trail.getNextCity());
 		}
 		return distance.toString();
 	}
 
 	public void setTrailIcon() {
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					loadImage();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				View iconContainer = v.findViewById(R.id.status_icon);
 				iconContainer.setVisibility(View.VISIBLE);
 			}
 		}).start();
 
 	}
 
 	private void loadImage() throws Exception {
 		Drawable trailDraw = null;
 		File imageFile = new File(context.getFilesDir(), String.valueOf(trail
 				.getTrailId()) + "_120.png");
 		if (imageFile.exists()) {
 			trailDraw = new BitmapDrawable(BitmapFactory.decodeFile(imageFile
 					.getAbsolutePath()));
 		} else {
			if (trail.getImageUrl120() != null && !"null".equals(trail.getImageUrl120())) {
 				InputStream is = (InputStream) new URL(trail.getImageUrl120())
 						.getContent();
 				trailDraw = Drawable.createFromStream(is, "src name");
 				saveDownloadedFile(trailDraw);
 			}
 		}
 
 		if (trailDraw != null) {
 			listImg.setImageDrawable(trailDraw);
 		} else {
 			listImg.setImageResource(R.drawable.icon);
 		}
 
 	}
 
 	private void saveDownloadedFile(Drawable trailDraw)
 			throws FileNotFoundException, IOException {
 		FileOutputStream fos;
 		fos = context.openFileOutput(String.valueOf(trail.getTrailId())
 				+ "_120.png", Context.MODE_PRIVATE);
 		if (trailDraw != null) {
 			((BitmapDrawable) trailDraw).getBitmap().compress(
 					Bitmap.CompressFormat.PNG, 100, fos);
 		}
 		fos.flush();
 		fos.close();
 	}
 }

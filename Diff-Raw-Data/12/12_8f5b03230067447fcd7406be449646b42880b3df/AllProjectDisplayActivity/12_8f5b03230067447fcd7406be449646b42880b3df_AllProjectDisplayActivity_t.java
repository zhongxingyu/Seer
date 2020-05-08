 package mit.edu.stemplusplus;
 
 import java.util.ArrayList;
 
 import mit.edu.stemplusplus.helper.MyHorizontalLayout;
 import mit.edu.stemplusplus.helper.Project;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 
 
 
 /**
  * This class display all of the projects currently active
  * 
  * @author trannguyen
  * 
  */
 
 public class AllProjectDisplayActivity extends StemPlusPlus{
 	/** The array to hold all of the captured image */
 	ArrayList<String> myPicture = new ArrayList<String>();
 
 	ArrayList<Project> projectDisplay = new ArrayList<Project>();
	
	/** adapter to handle data for the image*/
 	ImageAdapter adapter;
 
 	GridView imagegrid;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_all_project_display);
 		// call the horizontal layout
 		MyHorizontalLayout picture = (MyHorizontalLayout) findViewById(R.id.myphoto_all_project_display);
 		picture.setArrayList(myPicture);
 		// get the information from the
 		Intent intent = getIntent();
 		if (intent.getStringArrayListExtra(IMAGE_INTENT) != null) {
 			ArrayList<String> imagePath = intent
 					.getStringArrayListExtra(IMAGE_INTENT);
 			for (String i : imagePath) {
 				picture.add(i);
 			}
 		}
 
 		imagegrid = (GridView) findViewById(R.id.gridview_all_project_display);
 		/*********
 		 * Make test project here
 		 */
 		Project testProject1 = makeTestProject(myPicture, "Project Demo 1");
 		Project testProject2 = makeTestProject(myPicture, "Project Demo 2");
 		projectDisplay.add(testProject1);
 		projectDisplay.add(testProject2);
 
 		/** End Test */
 		adapter = new ImageAdapter(projectDisplay);
 		imagegrid.setAdapter(adapter);
 
 		Log.d("create", "ok");
 	}
 
 	/**
 	 * The adapter to handle to underlying array of pictures
 	 * 
 	 * @author trannguyen
 	 * 
 	 */
 	public class ImageAdapter extends BaseAdapter {
 		private LayoutInflater mInflater;
 
 		private ArrayList<Project> projects;
 
 		public ImageAdapter(ArrayList<Project> project) {
 			this.projects = project;
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		}
 
 		public ImageAdapter() {
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		@Override
 		public int getCount() {
 			return projects.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			final ViewHolder holder;
 			if (convertView == null) {
 				holder = new ViewHolder();
 				convertView = mInflater.inflate(R.layout.project_display, null);
 				holder.projectDescription = (TextView) convertView
 						.findViewById(R.id.project_description_all_project_display);
 				Log.d("get TextView", "ok");
 				holder.projectImage = (ImageView) convertView
 						.findViewById(R.id.project_avatar_all_project_display);
 				convertView.setTag(holder);
 			}
 			// picture is already there
 			else {
 				holder = (ViewHolder) convertView.getTag();
 
 			}
 
 			Project project = projects.get(position);
 			Log.d("get project", "ok");
 			holder.projectDescription.setText(project.getDescription());
 			Log.d("set Text", "ok");
 			Log.d("Image class", project.getImages().get(0).getClass()
 					.toString());
 			if (project.getImages().get(0).getClass().equals(String.class)) {
 
 				holder.projectImage.setImageBitmap(BitmapFactory
 						.decodeFile(project.getImages().get(0).toString()));
 			} else
 				holder.projectImage.setImageBitmap((Bitmap) project.getImages()
 						.get(0));
 			Log.d("set Image", "ok");
 			holder.projectImage.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					//Intent i = new Intent(v.getContext(), ProjectScreen.class);
 
 				}
 			});
 
 			holder.setId(position);
 
 			return convertView;
 
 		}
 
 	}
 
 	/**
 	 * The view holder to store the check box and imageView
 	 * 
 	 * @author trannguyen
 	 * 
 	 */
 	private class ViewHolder {
 		private TextView projectDescription;
 		private ImageView projectImage;
 		private int id;
 
 		public void setId(int id) {
 			this.id = id;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.layout.activity_all_project_display, menu);
 		return true;
 	}
 
 }

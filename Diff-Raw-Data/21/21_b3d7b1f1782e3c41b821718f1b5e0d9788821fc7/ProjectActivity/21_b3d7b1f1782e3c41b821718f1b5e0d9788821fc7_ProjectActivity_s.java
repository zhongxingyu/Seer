 package se.slide.timy;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import se.slide.timy.db.DatabaseManager;
 import se.slide.timy.model.Color;
 import se.slide.timy.model.Project;
 
 import java.util.List;
 
 public class ProjectActivity extends FragmentActivity {
     
     public static final int ACTIVITY_CODE = 0;
     
     private int mPosition = 0;
     
     public static final String EXTRA_PROJECT_ID = "project_id";
     public static final String EXTRA_PROJECT_NAME = "project_name";
     public static final String EXTRA_PROJECT_COLOR_ID = "project_colorid";
     
     ColorAdapter mAdapter;
     ImageView mCheckmark;
     EditText mName;
     Project mProject;
     GridView mGridview;
 
     /* (non-Javadoc)
      * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         
         DatabaseManager.init(this);
         
         setContentView(R.layout.project_dialog);
         
         List<Color> colors = DatabaseManager.getInstance().getColors();
         
         Intent i = getIntent();
         
         if (i != null && i.hasExtra(EXTRA_PROJECT_ID)) {
             int id = i.getIntExtra(EXTRA_PROJECT_ID, -1);
             List<Project> projects = DatabaseManager.getInstance().getProject(id);
             
             if (projects.size() > 0)
                 mProject = projects.get(0);
         }
         
         mName = (EditText) findViewById(R.id.name);
         
         if (mProject != null)
             mName.setText(mProject.getName());
 
         mGridview = (GridView) findViewById(R.id.gridview);
         mAdapter = new ColorAdapter(this, colors, mProject);
         mGridview.setAdapter(mAdapter);
         
         mGridview.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 
                 for (int i = 0; i < mGridview.getChildCount(); i++) {
                     View view = mGridview.getChildAt(i);
                     ImageView image = (ImageView) view.findViewById(R.id.checkmark);
                     image.setVisibility(View.INVISIBLE);
                 }
                 
                 mCheckmark = (ImageView) v.findViewById(R.id.checkmark);
                 mCheckmark.setVisibility(View.VISIBLE);
                 
                 mPosition = position;
             }
         });
        
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.project_activity, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 // TODO: If Settings has multiple levels, Up should navigate up
                 // that hierarchy.
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.menu_cancel:
                 finishActivity(RESULT_CANCELED);
                 finish();
                 return true;
             case R.id.menu_done:
                 
                 if (mName.getText().toString().trim().length() < 1)
                     return true;
                 
                 Intent result = new Intent();
                 result.putExtra(EXTRA_PROJECT_NAME, mName.getText().toString());
                result.putExtra(EXTRA_PROJECT_COLOR_ID, mAdapter.getColor(mPosition).getId());
                 
                 if (mProject != null)
                     result.putExtra(EXTRA_PROJECT_ID, mProject.getId());
                 
                 setResult(RESULT_OK, result);
                 finishActivity(RESULT_OK);
                 finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public class ColorAdapter extends BaseAdapter {
         private Context mContext;
         private List<Color> mColors;
         private Project mProject;
 
         public ColorAdapter(Context c, List<Color> colors, Project project) {
             mContext = c;
             mColors = colors;
             mProject = project;
         }
 
         public Color getColor(int id) {
             return mColors.get(id);
         }
         
         public int getCount() {
             return mColors.size();
         }
 
         public Object getItem(int position) {
             return null;
         }
 
         public long getItemId(int position) {
             return 0;
         }
 
         // create a new ImageView for each item referenced by the Adapter
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder = null;
             View view;
             
             if (convertView == null) {  // if it's not recycled, initialize some attributes
                 convertView = getLayoutInflater().inflate(R.layout.color_item, null);
                 
                 holder = new ViewHolder();
                 holder.frame = (FrameLayout) convertView.findViewById(R.id.color);
                 holder.checkmark = (ImageView) convertView.findViewById(R.id.checkmark);
                 convertView.setTag(holder);
                 
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
 
             if (mProject != null && mColors.get(position).getId().equals(mProject.getColorId())) {
                 holder.checkmark.setVisibility(View.VISIBLE);
                 mCheckmark = holder.checkmark;
             }
             
             holder.frame.setBackgroundColor(android.graphics.Color.parseColor(mColors.get(position).getBackgroundColor()));
             
             return convertView;
         }
         
         /* private view holder class */
         private class ViewHolder {
             FrameLayout frame;
             ImageView checkmark;
         }
         
     }
 }

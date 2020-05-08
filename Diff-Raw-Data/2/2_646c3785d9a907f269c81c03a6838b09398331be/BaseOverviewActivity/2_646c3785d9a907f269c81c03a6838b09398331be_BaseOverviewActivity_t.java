 package spaceappschallenge.moonville.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import spaceappschallenge.moonville.GameActivity;
 import spaceappschallenge.moonville.MoonVille;
 import spaceappschallenge.moonville.R;
 import spaceappschallenge.moonville.businessmodels.Building;
 import spaceappschallenge.moonville.businessmodels.BuildingTree;
 import spaceappschallenge.moonville.businessmodels.MoonBase;
 import spaceappschallenge.moonville.businessmodels.Resource;
 import spaceappschallenge.moonville.factories.Buildings;
 import spaceappschallenge.moonville.managers.MoonBaseManager;
 import spaceappschallenge.moonville.xml_parsers.BuildingDefinition;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.MarginLayoutParams;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class BaseOverviewActivity extends GameActivity {
 
 	private RelativeLayout moonSurfaceLayout;
 	private ArrayList<ImageView> buildingImageList;
 	
 	private static String PREFERENCE_SCROLL_X = "base_overview_scroll_x";
 	private static String PREFERENCE_SCROLL_Y = "base_overview_scroll_y";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_base_overview);
 		Log.i("Base", "showing moon");
 		moonSurfaceLayout = (RelativeLayout) this
 				.findViewById(R.id.moonsurface_relativelayout);
 		Log.i("Base", "showing buildings");
 
 		this.buildingImageList = new ArrayList<ImageView>();
 
 		showBuildings();
 		fixHVScrollViews();
 		updateUI();
 	}
 	
 	/**
 	 * Saves scroll position.
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		SharedPreferences.Editor editor = 
 				getSharedPreferences(MoonVille.PREFERENCE_FILE, 0).edit();
 		final HorizontalScrollView hScroll = (HorizontalScrollView) 
 				findViewById(R.id.moonsurface_hscrollview);
 		final ScrollView vScroll = (ScrollView) 
 				findViewById(R.id.moonsurface_vscrollview);
 		editor.putInt(PREFERENCE_SCROLL_X, hScroll.getScrollX());
 		editor.putInt(PREFERENCE_SCROLL_Y, vScroll.getScrollY());
 		editor.commit();
 	}
 
 	/**
 	 * Restores scroll position.
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		final SharedPreferences settings = 
 				getSharedPreferences(MoonVille.PREFERENCE_FILE, 0);
 		final HorizontalScrollView hScroll = (HorizontalScrollView) 
 				findViewById(R.id.moonsurface_hscrollview);
 		final ScrollView vScroll = (ScrollView) 
 				findViewById(R.id.moonsurface_vscrollview);
 		// Don't know why, but using post is the only way to make this work.
 		hScroll.post(new Runnable() {			
 			@Override
 			public void run() {
 				hScroll.scrollTo(settings.getInt(PREFERENCE_SCROLL_X, 0), 0);
 			}
 		});
 		vScroll.post(new Runnable() {			
 			@Override
 			public void run() {
 				vScroll.scrollTo(0, settings.getInt(PREFERENCE_SCROLL_Y, 0));
 			}
 		});
 	}
 
 	public void updateUI() {
 		TextView baseOverviewFundsTextView = (TextView) (this
 				.findViewById(R.id.baseOverviewFundsTextView));
 		baseOverviewFundsTextView.setText("Funds: "
 				+ MoonBaseManager.getCurrentMoonBase().getMoney());
 
 		TextView txtMonth = (TextView) (this.findViewById(R.id.txtMonth));
 		txtMonth.setText("" + MoonBaseManager.getCurrentMoonBase().getMonth());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_base_overview, menu);
 		return true;
 	}
 
 	
 	/**
 	 * showBuildings shows the buildings on the moon landscape.
 	 * It's using an absolute layout (deprecated) to be able to position buildings on absolute position.
 	 * RelativeLayout didn't work, the buildings did not scroll along and out of the viewport.
 	 * 
 	 * The method maintains a list of images (buildingImagesList) for each building placed in the absolutelayout.
 	 * When called, this list is cleared, the images removed from layout, and then re-added.
 	 * The image positions are obtained from the Buildings factory, which gets the data from buildings.xml.
 	 * 
 	 * Image transparency is determined by the fact wether or not a building can already be build:
 	 * 	If the building is build, the image is opaque (alpha 100)
 	 * 	Else if all required buildings of a building have been build, the image is transparent (alpha 50)
 	 *  Else building is invisible (alpha 0)
 	 */
 	private void showBuildings()
 	{
 		//erase all building images from list
 		for (ImageView buildingImage : buildingImageList) {
 			((RelativeLayout) buildingImage.getParent())
 					.removeView(buildingImage);
 		}
 
 		//clear list
 		buildingImageList.clear();
 		
 		
 		//obtain the current moonbase
 		MoonBase moonBase = MoonBaseManager.getCurrentMoonBase();
 		
 		
 		//get all buildings, to check for each building if it is visible and what its position should be
 		List<BuildingDefinition> buildings = Buildings.getInstance().getAllBuildings();
 		
 		for (final BuildingDefinition building : buildings)
 		{
 			//see if the building can be build or not, and whether or not it is already visible
 			boolean canBeBuild = moonBase.canBuild(building.getName());
 
 			if( canBeBuild == false )
 			{
 				//nothing happens, no building visible
 			}
 			
 			else
 			{		
 				ImageView buildingImage = new ImageView(this);
 				
 				//by clicking on the building, the BuildingInfoActivity pops up
 				buildingImage.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View view)
 					{
 						Intent intent = new Intent(BaseOverviewActivity.this, BuildingInfoActivity.class);
 						intent.putExtra("Building", building.getName());
 						view.getContext().startActivity( intent );
 						BaseOverviewActivity.this.finish();
 					}
 				});
 	
 				buildingImageList.add(buildingImage);
 	
 				android.content.res.Resources res = this.getResources();
 	
 				//obtain the bitmap from the name of the building
 				//TODO: replace/remove ref_ prefix to match name of the final image in the file system
 				int resID = res
 						.getIdentifier("ref_"
 								+ building.getName().replace(" ", "_")
 										.toLowerCase(), "drawable",
 								getPackageName());
 				Drawable buildingDrawable = res.getDrawable(resID);
 				buildingImage.setImageDrawable(buildingDrawable);
 	
 				//place it in the background according to position determined in Building object
 				BuildingDefinition bd = Buildings.getInstance().getBuilding(building.getName());
 				
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( buildingDrawable.getIntrinsicWidth()/2, buildingDrawable.getIntrinsicHeight()/2 );
 				//params.addRule( RelativeLayout.ALIGN_PARENT_LEFT );
 				//params.addRule( RelativeLayout.ALIGN_PARENT_TOP );
 				params.leftMargin = bd.getXPos();
 				params.topMargin = bd.getYPos();
 				
 				buildingImage.setLayoutParams( params );
 
 				moonSurfaceLayout.addView(buildingImage);
 				
 
 				//depending on wether or not it is already build, show it 
 				if( moonBase.getBuilding( building.getName() ) != null )
 				{
 					buildingImage.setAlpha( 255 );
 				}
 				else
 				{
 					buildingImage.setAlpha( 50 );
 				}
 			}
 		}
 	}
 
 	
 	public void showResourcePopups()
 	{	
 		List<Resource> resourceChangeList = new ArrayList<Resource>();
 		BuildingTree buildingTree = MoonBaseManager.getCurrentMoonBase().getBuiltBuildings();
 		buildingTree.checkResources( resourceChangeList );
 		
 		if( buildingTree.size() > 0 )
 		{
 			for( final Building building : buildingTree )
 			{
				if (building == null)
					continue;
 				List<Resource> outputResources = building.getResourceOutput();
 	
 				int popupNumber = 0;
 				
 				for( Resource resource : outputResources )
 				{
 					/**
 					 * Slowly fade and move popup away
 					 */	
 					BuildingDefinition bd = Buildings.getInstance().getBuilding(building.getName());
 					
 					new Popup(
 						BaseOverviewActivity.this,
 						moonSurfaceLayout,
 						"+ " + resource.getAmount() + " " + resource.getName(),
 						bd.getXPos(), bd.getYPos(),
 						popupNumber * 10 );
 					
 					popupNumber++;
 				}
 			}
 		}
 	}
 	
 
 	// methods called by onClick property of button in xml
 	
 	/**
 	 * nextTurn pushes the game forward to the next month.
 	 * It forwards time, changes resources, builds the
 	 * buildings and divides the power.
 	 * 
 	 * Performed tasks:
 	 * 	divide power among buildings
 	 * 	process all resources
 	 * 	calculate reputation
 	 * 	change month
 	 * 	save game state
 	 * 
 	 */
 	public void nextTurn(View view)
 	{
 		MoonBase moonBase = MoonBaseManager.getCurrentMoonBase();
 		moonBase.incrementMonth();
 		
 		BuildingTree tree = moonBase.getBuiltBuildings();
 		tree.checkPower();
 		tree.checkRequiredBuildings();
 
 		List<Resource> available = (ArrayList<Resource>) tree.checkResources( moonBase.getStoredResources() );
 		moonBase.setStoredResources( available );
 
 		// TODO: factor in research and prospecting bonus
 		// TODO: calculate reputation
 
 		// last step, save to file
 		
 		updateUI();
 		showResourcePopups();
 		
 		MoonBaseManager.saveMoonBase(view.getContext());
 
 	}
 	
 	
 
 	public void showImportResourcesScreen(View view) {
 		view.getContext().startActivity(
 				new Intent(this, ImportResourcesActivity.class));
 	}
 
 	public void showExportResourcesScreen(View view) {
 		view.getContext().startActivity(
 				new Intent(this, ExportResourcesActivity.class));
 
 	}
 
 	public void showResourcesScreen(View view) {
 		view.getContext().startActivity(
 				new Intent(this, ResourcesActivity.class));
 	}
 
 	public void showExportScreen(View view) {
 		view.getContext().startActivity(
 				new Intent(this, ResourcesActivity.class));
 	}
 
 	// some scrollbar fix for scrolling
 	//TODO: make a custom widget for this
 	private void fixHVScrollViews() {
 		final HorizontalScrollView hScroll = (HorizontalScrollView) findViewById(R.id.moonsurface_hscrollview);
 		final ScrollView vScroll = (ScrollView) findViewById(R.id.moonsurface_vscrollview);
 		vScroll.setOnTouchListener(new View.OnTouchListener() { // inner scroll
 																// listener
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return false;
 			}
 		});
 		hScroll.setOnTouchListener(new View.OnTouchListener() { // outer scroll
 																// listener
 			private float mx, my, curX, curY;
 			private boolean started = false;
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				curX = event.getX();
 				curY = event.getY();
 				int dx = (int) (mx - curX);
 				int dy = (int) (my - curY);
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_MOVE:
 					if (started) {
 						vScroll.scrollBy(0, dy);
 						hScroll.scrollBy(dx, 0);
 					} else {
 						started = true;
 					}
 					mx = curX;
 					my = curY;
 					break;
 				case MotionEvent.ACTION_UP:
 					vScroll.scrollBy(0, dy);
 					hScroll.scrollBy(dx, 0);
 					started = false;
 					break;
 				}
 				return true;
 			}
 		});
 	}
 }

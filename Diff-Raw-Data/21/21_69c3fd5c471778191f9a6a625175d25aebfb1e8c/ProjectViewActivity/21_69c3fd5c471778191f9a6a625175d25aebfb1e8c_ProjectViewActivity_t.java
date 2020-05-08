 package org.imaginationforpeople.android.activity;
 
 import java.util.List;
 
 import org.imaginationforpeople.android.R;
 import org.imaginationforpeople.android.handler.ProjectViewHandler;
 import org.imaginationforpeople.android.helper.UriHelper;
 import org.imaginationforpeople.android.model.I4pProjectTranslation;
 import org.imaginationforpeople.android.model.Objective;
 import org.imaginationforpeople.android.model.Question;
 import org.imaginationforpeople.android.model.User;
 import org.imaginationforpeople.android.sqlite.FavoriteSqlite;
 import org.imaginationforpeople.android.thread.ProjectViewThread;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ProjectViewActivity extends Activity implements OnClickListener {
 	private boolean displayMenu = false;
 	private Intent shareIntent;
 	private FavoriteSqlite db;
 	private I4pProjectTranslation project;
 	
 	@TargetApi(14)
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if(displayMenu) {
 			// Inflate menu only if it hasn't been done before
 			if(menu.size() == 0) {
 				// Inflating the menu
 				MenuInflater inflater = getMenuInflater();
 				inflater.inflate(R.menu.projectview, menu);
 				
 				// Creating share intent
 				Intent prepareShareIntent = new Intent(Intent.ACTION_SEND);
 				prepareShareIntent.putExtra(Intent.EXTRA_TEXT, UriHelper.getProjectUrl(project));
 				prepareShareIntent.putExtra(Intent.EXTRA_SUBJECT, project.getTitle());
 				prepareShareIntent.setType("text/plain");
 				shareIntent = Intent.createChooser(prepareShareIntent, getResources().getText(R.string.projectview_menu_share_dialog));
 			}
 			
 			MenuItem videoItem = menu.getItem(0);
 			videoItem.setVisible(project.getProject().getVideos().size() != 0);
 			
 			// Defining favorite state
 			MenuItem favoriteItem = menu.getItem(1);
 			if(db.isFavorite(project))
 				favoriteItem.setTitle(R.string.projectview_menu_favorites_remove);
 			else
 				favoriteItem.setTitle(R.string.projectview_menu_favorites_add);
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case android.R.id.home:
 			if(getIntent().getData() != null) {
 				Intent intent = new Intent(this, HomepageActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(intent);
 			} else 
 				finish();
 			break;
 		case R.id.projectview_video:
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(project.getProject().getVideos().get(0).getVideoUrl()));
 			startActivity(intent);
 			break;
 		case R.id.projectview_favorite:
 			Toast t;
 			if(db.isFavorite(project)) {
 				db.removeFavorite(project);
 				t = Toast.makeText(this, getResources().getString(R.string.projectview_toast_favorites_remove, project.getTitle()), Toast.LENGTH_SHORT);
 			} else {
 				db.addFavorite(project);
 				t = Toast.makeText(this, getResources().getString(R.string.projectview_toast_favorites_add, project.getTitle()), Toast.LENGTH_SHORT);
 			}
 			t.show();
 			break;
 		case R.id.projectview_share:
 			startActivity(shareIntent);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@TargetApi(11)
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if(Build.VERSION.SDK_INT < 11)
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.loading);
 		db = new FavoriteSqlite(this);
 		
 		if(Build.VERSION.SDK_INT >= 11)
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		project = (I4pProjectTranslation) getLastNonConfigurationInstance();
 		if(project != null)
 			displayProject();
 		else {
			ProjectViewHandler handler = new ProjectViewHandler(this);
			ProjectViewThread thread = null;
			
			String projectLang = null;
			String projectSlug = null;
 			
 			Uri data = getIntent().getData();
 			if(data != null) {
 				List<String> path = data.getPathSegments();
 				projectLang = path.get(0);
 				projectSlug = path.get(2);
 			} else {
 				Bundle extras = getIntent().getExtras();
 				
 				if(extras.containsKey("project_title"))
 					setTitle(extras.getString("project_title"));
 				
				if(extras.containsKey("project_id")) { // Mostly used if we want a random project
					thread = new ProjectViewThread(handler, extras.getInt("project_id"));
				} else {
					projectLang = extras.getString("project_lang");
					projectSlug = extras.getString("project_slug");
				}
 			}
 			
			if(thread == null)
				thread = new ProjectViewThread(handler, projectLang, projectSlug);
 			
 			thread.start();
 		}
 	}
 	
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return project;
 	}
 	
 	public void setProject(I4pProjectTranslation p) {
 		project = p;
 	}
 	
 	@TargetApi(11)
 	public void displayProject() {
 		setContentView(R.layout.projectview_description);
 		displayMenu = true;
 		if(Build.VERSION.SDK_INT >= 11)
 			invalidateOptionsMenu(); // Rebuild the menu
 		
 		setTitle(project.getTitle());
 		
 		LinearLayout overlay = (LinearLayout) findViewById(R.id.projectview_description_overlay);
 		overlay.getBackground().setAlpha(127);
 		
 		if(project.getProject().getPictures().size() > 0) {
 			ImageView image = (ImageView) findViewById(R.id.projectview_description_image);
 			image.setImageBitmap(project.getProject().getPictures().get(0).getImageBitmap());
 		}
 		
 		ImageView bestof = (ImageView) findViewById(R.id.projectview_description_bestof);
 		if(!project.getProject().getBestOf())
 			bestof.setVisibility(View.GONE);
 		
 		TextView title = (TextView) findViewById(R.id.projectview_description_title);
 		title.setText(project.getTitle());
 		
 		TextView baseline = (TextView) findViewById(R.id.projectview_description_baseline);
 		baseline.setText(project.getBaseline());
 		
 		ImageView status = (ImageView) findViewById(R.id.projectview_description_status);
 		if("IDEA".equals(project.getProject().getStatus())) {
 			status.setImageResource(R.drawable.project_status_idea);
 			status.setContentDescription(getResources().getString(R.string.projectview_description_status_idea));
 		} else if("BEGIN".equals(project.getProject().getStatus())) {
 			status.setImageResource(R.drawable.project_status_begin);
 			status.setContentDescription(getResources().getString(R.string.projectview_description_status_begin));
 		} else if("WIP".equals(project.getProject().getStatus())) {
 			status.setImageResource(R.drawable.project_status_wip);
 			status.setContentDescription(getResources().getString(R.string.projectview_description_status_wip));
 		} else if("END".equals(project.getProject().getStatus())) {
 			status.setImageResource(R.drawable.project_status_end);
 			status.setContentDescription(getResources().getString(R.string.projectview_description_status_end));
 		}
 		
 		if(project.getProject().getLocation() != null) {
 			if(!"".equals(project.getProject().getLocation().getCountry())) {
 				int flag = getResources().getIdentifier("flag_"+project.getProject().getLocation().getCountry().toLowerCase(), "drawable", "org.imaginationforpeople.android");
 				if(flag != 0) {
 					ImageView flagView = (ImageView) findViewById(R.id.projectview_description_flag);
 					flagView.setImageResource(flag);
 				}
 			}
 		}
 		
 		TextView website = (TextView) findViewById(R.id.projectview_description_website);
 		if("".equals(project.getProject().getWebsite()))
 			website.setVisibility(View.GONE);
 		else
 			website.setOnClickListener(this);
 		
 		if(project.getAboutSection() == null || "".equals(project.getAboutSection())) {
 			LinearLayout aboutContainer = (LinearLayout) findViewById(R.id.projectview_description_about_container);
 			aboutContainer.setVisibility(View.GONE);
 		} else {
 			TextView aboutText = (TextView) findViewById(R.id.projectview_description_about_text);
 			aboutText.setText(project.getAboutSection().trim());
 		}
 		
 		if("".equals(project.getThemes())) {
 			LinearLayout themesContainer = (LinearLayout) findViewById(R.id.projectview_description_themes_container);
 			themesContainer.setVisibility(View.GONE);
 		} else {
 			TextView themesText = (TextView) findViewById(R.id.projectview_description_themes_text);
 			themesText.setText(project.getThemes());
 		}
 		
 		if(project.getProject().getObjectives().size() == 0) {
 			LinearLayout objectivesContainer = (LinearLayout) findViewById(R.id.projectview_description_objectives_container);
 			objectivesContainer.setVisibility(View.GONE);
 		} else {
 			TextView objectivesText = (TextView) findViewById(R.id.projectview_description_objectives_text);
 			List<Objective> objectivesObject = project.getProject().getObjectives(); 
 			String objectives = objectivesObject.get(0).getName();
 			for(int i = 1; i < objectivesObject.size(); i++) {
 				objectives += ", " + objectivesObject.get(i).getName();
 			}
 			objectivesText.setText(objectives);
 		}
 			
 		LinearLayout questions = (LinearLayout) findViewById(R.id.projectview_description_questions_container);
 		for(Question question : project.getProject().getQuestions()) {
 			if(question.getAnswer() != null) {
 				LinearLayout questionLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.projectview_question, null);
 				
 				TextView questionView = (TextView) questionLayout.findViewById(R.id.projectview_question_question);
 				TextView answerView = (TextView) questionLayout.findViewById(R.id.projectview_question_answer);
 				
 				questionView.setText(Build.VERSION.SDK_INT < 14 ? question.getQuestion().toUpperCase() : question.getQuestion());
 				answerView.setText(question.getAnswer().trim());
 				
 				questions.addView(questionLayout);
 			}
 		}
 		
 		if(project.getProject().getMembers().size() == 0) {
 			LinearLayout membersContainer = (LinearLayout) findViewById(R.id.projectview_description_members_container);
 			membersContainer.setVisibility(View.GONE);
 		} else {
 			LinearLayout members = (LinearLayout) findViewById(R.id.projectview_description_members_text);
 			for(User member : project.getProject().getMembers()) {
 				TextView memberName = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
 				
 				memberName.setText(member.getFullname());
 				memberName.setCompoundDrawablesWithIntrinsicBounds(null, null, member.getAvatarDrawable(), null);
 				
 				members.addView(memberName);
 			}
 		}
 	}
 
 	public void onClick(View arg0) {
 		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(project.getProject().getWebsite()));
 		startActivity(intent);
 	}
 }

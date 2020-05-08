 package edu.upenn.nettercenter.auni.cookcompetition.sections;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
import android.text.Editable;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.OptionsItem;
 import com.googlecode.androidannotations.annotations.OrmLiteDao;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.j256.ormlite.dao.Dao;
 
import java.sql.SQLException;

 import edu.upenn.nettercenter.auni.cookcompetition.DatabaseHelper;
 import edu.upenn.nettercenter.auni.cookcompetition.R;
 import edu.upenn.nettercenter.auni.cookcompetition.models.Team;
 
 @EActivity(R.layout.activity_team_add_team)
 public class TeamAddTeamActivity extends Activity {
 
 	@OrmLiteDao(helper = DatabaseHelper.class, model = Team.class)
 	Dao<Team, Long> dao;
 
 	@Extra
 	Long itemId;
 
 	@ViewById
 	EditText teamName;
 	
 	@ViewById
	ViewGroup teamEditor;
 	
 		
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.activity_team_add_team, menu);
 
 		// Hide "Save and Continue" button in edit mode
 		boolean visible = itemId == null;
 		menu.findItem(R.id.menu_add_next).setVisible(visible);
 
 		return true;
 	}
 	
 	@AfterViews
 	void setUpActionBar() {
 		ActionBar actionBar = getActionBar();
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View customActionBarView = inflater.inflate(R.layout.action_bar_add_student, null);
         View doneItem = customActionBarView.findViewById(R.id.menu_add_student_done);
         doneItem.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				done();
 			}
 		});
         
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                 ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                 ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView);        
 	}
 	
 	@AfterViews
 	void setFocus() {
 		teamName.requestFocus();
 	}
 	
 	@AfterViews
 	void loadStudent() {
 		if (itemId == null) return;
 
 		try {
 			Team team = dao.queryForId(itemId);
 			teamName.setText(team.getName());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	void done() {
 		if (isNameEmpty()) {
 			saveCurrentItem();
 		}
 		finish();
 	}
 	
 	@OptionsItem(R.id.menu_add_next)
 	void next() {
 		if (isNameEmpty()) {
 			saveCurrentItem();
			clearAllEditText(teamEditor);
 			setFocus();
 		}
 	}
 	
 	private boolean isNameEmpty() {
 		return teamName.getText().toString().trim().length() > 0;
 	}
 
 	@OptionsItem(R.id.menu_add_discard)
 	void discard() {
 		if (isNameEmpty()) {
 			finish();
 		} else {
 			new AlertDialog.Builder(this)
 				.setMessage("Discard your changes?")
 			    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			    	@Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        finish();
 				    }
 			    })
 			    .setNegativeButton(android.R.string.cancel, null)
 			    .show();
 		}
 	}
 	
 	private void saveCurrentItem() {
 		try {
 			Team item;
 			if (itemId == null) {
 				item = new Team();
 			} else {
 				item = dao.queryForId(itemId);
 			}
 			item.setName(teamName.getText().toString().trim());
 			dao.createOrUpdate(item);
 			
 			String message;
 			if (itemId == null) {
 				message = "Team created."; 
 			} else {
 				message = "Team updated.";
 			}
 			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void clearAllEditText(ViewGroup viewGroup) {
 		for (int i = 0; i < viewGroup.getChildCount(); i++) {
 			View view = viewGroup.getChildAt(i);
 			if (view instanceof EditText) {
				Editable e = ((EditText) view).getText();
                if (e != null) e.clear();
 			} else if (view instanceof ViewGroup) {
 				clearAllEditText((ViewGroup) view);
 			}
 		}
 	}	
 }

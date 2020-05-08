 package cz.kinst.jakub.coursemanager;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import cz.kinst.jakub.coursemanager.utils.Utils;
 
 /**
  * Activity showing detail of an assignment. Provides buttons for solving or
  * correcting
  * 
  * @author Jakub Kinst
  * 
  */
 public class AssignmentDetail extends CMActivity {
 
 	/**
 	 * UID for serialization
 	 */
 	private static final long serialVersionUID = -9168204116934753181L;
 
 	/**
 	 * Assignment Id
 	 */
 	private int aid;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.aid = getIntent().getExtras().getInt("aid");
 		setContentView(R.layout.assignment_detail);
 		reload();
 	}
 
 	@Override
 	protected void onResume() {
 		reload();
 		super.onResume();
 	}
 
 	@Override
 	protected JSONObject reloadWork() throws JSONException {
 		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 		args.add(new BasicNameValuePair("aid", String.valueOf(this.aid)));
 		return courseManagerCon.getAction("assignment", "show", args,
 				new ArrayList<NameValuePair>());
 	}
 
 	@Override
 	public void gotData(JSONObject data) throws JSONException {
 		JSONObject assignment = data.getJSONObject("assignment");
 		TextView name = (TextView) findViewById(R.id.name);
 		TextView date = (TextView) findViewById(R.id.date);
 		JSONObject course = data.getJSONObject("activeCourse");
 		setTitle(course.getString("name") + " > "
 				+ getText(R.string.assignment));
 		TextView timelimit = (TextView) findViewById(R.id.timelimit);
 		TextView description = (TextView) findViewById(R.id.description);
 		int limit = assignment.getInt("timelimit");
 		timelimit.setText(getText(R.string.time_limit) + ": " + limit + " "
 				+ getText(R.string.minutes));
 		name.setText(assignment.getString("name"));
 		description.setText(assignment.getString("description"));
 
 		Date assignDate = Utils.getDateFromDBString(assignment
 				.getString("assigndate"));
 		Date dueDate = Utils.getDateFromDBString(assignment
 				.getString("duedate"));
 		DateFormat df = DateFormat.getInstance();
 		date.setText(df.format(assignDate) + " - " + df.format(dueDate));
 
 		Button solveButton = (Button) findViewById(R.id.solveButton);
		if (data.getBoolean("canSolve")) {
 			solveButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					startActivity(new Intent(AssignmentDetail.this,
 							AssignmentSolve.class).putExtra("cm",
 							courseManagerCon).putExtra("aid", aid));
 				}
 			});
 		} else {
 			solveButton.setVisibility(View.GONE);
 		}
 
 		Button correctButton = (Button) findViewById(R.id.correctButton);
 		if (data.getBoolean("isTeacher")
 				&& 1 != assignment.getInt("autocorrect")) {
 			correctButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					startActivity(new Intent(AssignmentDetail.this,
 							AssignmentCorrect.class).putExtra("cm",
 							courseManagerCon).putExtra("aid", aid));
 				}
 			});
 		} else {
 			correctButton.setVisibility(View.GONE);
 		}
 
 	}
 }

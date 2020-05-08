 package org.imaginationforpeople.android2.projectview;
 
 import java.util.List;
 
 import org.imaginationforpeople.android2.R;
 import org.imaginationforpeople.android2.helper.DataHelper;
 import org.imaginationforpeople.android2.model.I4pProjectTranslation;
 import org.imaginationforpeople.android2.model.Objective;
 import org.imaginationforpeople.android2.model.Question;
 import org.imaginationforpeople.android2.model.User;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class InfoProjectViewFragment extends Fragment implements OnClickListener {
 	private I4pProjectTranslation project;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		project = getArguments().getParcelable(DataHelper.PROJECT_VIEW_KEY);
 		ScrollView layout = (ScrollView) inflater.inflate(R.layout.projectview_info, null);
 
 		LinearLayout overlay = (LinearLayout) layout.findViewById(R.id.projectview_description_overlay);
 		overlay.getBackground().setAlpha(127);
 
 		if(project.getProject().getPictures().size() > 0) {
 			ImageView image = (ImageView) layout.findViewById(R.id.projectview_description_image);
 			image.setImageBitmap(project.getProject().getPictures().get(0).getImageBitmap());
 		}
 
 		ImageView bestof = (ImageView) layout.findViewById(R.id.projectview_description_bestof);
 		if(!project.getProject().getBestOf())
 			bestof.setVisibility(View.GONE);
 
 		TextView title = (TextView) layout.findViewById(R.id.projectview_description_title);
 		title.setText(project.getTitle());
 
 		TextView baseline = (TextView) layout.findViewById(R.id.projectview_description_baseline);
 		baseline.setText(project.getBaseline());
 
 		ImageView status = (ImageView) layout.findViewById(R.id.projectview_description_status);
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
 				int flag = getResources().getIdentifier("flag_"+project.getProject().getLocation().getCountry().toLowerCase(), "drawable", "org.imaginationforpeople.android2");
 				if(flag != 0) {
 					ImageView flagView = (ImageView) layout.findViewById(R.id.projectview_description_flag);
 					flagView.setImageResource(flag);
 				}
 			}
 		}
 
 		TextView website = (TextView) layout.findViewById(R.id.projectview_description_website);
 		if("".equals(project.getProject().getWebsite()))
 			website.setVisibility(View.GONE);
 		else
 			website.setOnClickListener(this);
 
 		if(project.getAboutSection() == null || "".equals(project.getAboutSection())) {
 			LinearLayout aboutContainer = (LinearLayout) layout.findViewById(R.id.projectview_description_about_container);
 			aboutContainer.setVisibility(View.GONE);
 		} else {
 			TextView aboutText = (TextView) layout.findViewById(R.id.projectview_description_about_text);
 			aboutText.setText(project.getAboutSection().trim());
 		}
 
 		if("".equals(project.getThemes())) {
 			LinearLayout themesContainer = (LinearLayout) layout.findViewById(R.id.projectview_description_themes_container);
 			themesContainer.setVisibility(View.GONE);
 		} else {
 			TextView themesText = (TextView) layout.findViewById(R.id.projectview_description_themes_text);
 			themesText.setText(project.getThemes());
 		}
 
 		if(project.getProject().getObjectives().size() == 0) {
 			LinearLayout objectivesContainer = (LinearLayout) layout.findViewById(R.id.projectview_description_objectives_container);
 			objectivesContainer.setVisibility(View.GONE);
 		} else {
 			TextView objectivesText = (TextView) layout.findViewById(R.id.projectview_description_objectives_text);
 			List<Objective> objectivesObject = project.getProject().getObjectives();
 			String objectives = objectivesObject.get(0).getName();
 			for(int i = 1; i < objectivesObject.size(); i++) {
 				objectives += ", " + objectivesObject.get(i).getName();
 			}
 			objectivesText.setText(objectives);
 		}
 
 		LinearLayout questions = (LinearLayout) layout.findViewById(R.id.projectview_description_questions_container);
 		for(Question question : project.getProject().getQuestions()) {
 			if(question.getAnswer() != null) {
 				LinearLayout questionLayout = (LinearLayout) inflater.inflate(R.layout.projectview_question, null);
 
 				TextView questionView = (TextView) questionLayout.findViewById(R.id.projectview_question_question);
 				TextView answerView = (TextView) questionLayout.findViewById(R.id.projectview_question_answer);
 
 				questionView.setText(Build.VERSION.SDK_INT < 14 ? question.getQuestion().toUpperCase() : question.getQuestion());
 				answerView.setText(question.getAnswer().trim());
 
 				questions.addView(questionLayout);
 			}
 		}
 
 		if(project.getProject().getMembers().size() == 0) {
 			LinearLayout membersContainer = (LinearLayout) layout.findViewById(R.id.projectview_description_members_container);
 			membersContainer.setVisibility(View.GONE);
 		} else {
 			LinearLayout members = (LinearLayout) layout.findViewById(R.id.projectview_description_members_text);
 			for(User member : project.getProject().getMembers()) {
 				TextView memberName = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
 
 				memberName.setText(member.getFullname());
 				memberName.setCompoundDrawablesWithIntrinsicBounds(null, null, member.getAvatarDrawable(), null);
 
 				members.addView(memberName);
 			}
 		}
 
 		return layout;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(project.getProject().getWebsite()));
 		startActivity(intent);
 	}
 }

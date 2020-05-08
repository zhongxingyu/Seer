 package com.nguyenmp.gauchodroid.course;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 
 import org.apache.http.client.CookieStore;
 import org.xml.sax.SAXNotRecognizedException;
 import org.xml.sax.SAXNotSupportedException;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.nguyenmp.gauchodroid.R;
 import com.nguyenmp.gauchodroid.common.HandledThread;
 import com.nguyenmp.gauchodroid.login.LoginManager;
 import com.nguyenmp.gauchospace.GauchoSpaceClient;
 import com.nguyenmp.gauchospace.thing.grade.GradeFolder;
 import com.nguyenmp.gauchospace.thing.grade.GradeItem;
 
 public class GradesFragment extends SherlockFragment implements GradesDownloadListener {
 	public static final String ARGUMENT_COURSE_ID = "argument_course_id";
 	private LinearLayout mContentView;
 	private static final String KEY_GRADE_FOLDER = "key-grade_folder";
 	private GradeFolder mGrades = null;
 	private boolean mLoaded = false;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
 		mContentView = (LinearLayout) inflater.inflate(R.layout.fragment_grades, container, false);
 		if (inState != null && inState.containsKey(KEY_GRADE_FOLDER)) {
 			setGrades((GradeFolder) inState.getSerializable(KEY_GRADE_FOLDER));
 		} else {
 			int courseID = getArguments().getInt(ARGUMENT_COURSE_ID);
 			CookieStore cookies = LoginManager.getCookies(getActivity());
 			Handler downloadHandler = new GradesDownloadHandler(this);
 			Thread downloadThread = new GradesDownloadThread(courseID, cookies, downloadHandler);
 			downloadThread.start();
 		}
 		
 		return mContentView;
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		if (mLoaded) outState.putSerializable(KEY_GRADE_FOLDER, (Serializable) mGrades);
 	}
 	
 	private static class GradesDownloadThread extends HandledThread {
 		private final int mCourseID;
 		private final CookieStore mCookies;
 		
 		GradesDownloadThread(int courseID, CookieStore cookies, Handler handler) {
 			setHandler(handler);
 			mCourseID = courseID;
 			mCookies = cookies;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				GradeFolder grades = GauchoSpaceClient.getGrade(mCourseID, mCookies);
 				dispatchMessage(grades);
 			} catch (SAXNotRecognizedException e) {
 				dispatchMessage(e);
 				e.printStackTrace();
 			} catch (SAXNotSupportedException e) {
 				dispatchMessage(e);
 				e.printStackTrace();
 			} catch (IOException e) {
 				dispatchMessage(e);
 				e.printStackTrace();
 			} catch (TransformerFactoryConfigurationError e) {
 				dispatchMessage(e);
 				e.printStackTrace();
 			} catch (TransformerException e) {
 				dispatchMessage(e);
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private static class GradesDownloadHandler extends Handler {
 		private final GradesDownloadListener mListener;
 		
 		GradesDownloadHandler(GradesDownloadListener listener) {
 			mListener = listener;
 		}
 		
 		@Override
 		public void handleMessage(Message message) {
 			if (message.obj == null) {
 				mListener.onDownloaded(null);
 			} else if (message.obj instanceof GradeFolder) {
 				mListener.onDownloaded((GradeFolder) message.obj);
 			} else if (message.obj instanceof Exception) {
 				//TODO: Handle exception
 			} else {
 				//TODO: Handle unknown error
 			}
 		}
 	}
 	
 	@Override
 	public void onDownloaded(GradeFolder grades) {
 		setGrades(grades);
 	}
 
 	private void setGrades(GradeFolder grades) {
 		mLoaded = (grades != null);
 		mGrades = grades;
 		if (grades == null) {
			TextView errorMessage = new TextView(mContentView.getContext());
 			errorMessage.setText("No grades module for this class");
 			mContentView.addView(errorMessage);
 		} else {
			ScrollView scrollView = new ScrollView(mContentView.getContext());
 			scrollView.addView(getGradesView(grades));
 			mContentView.addView(scrollView);
 		}
 	}
 	
 	private static Dialog getDialog(final GradeFolder folder, final Context context) {
 		Dialog dialog = new Dialog(context);
 		dialog.setContentView(R.layout.dialog_grades_folder);
 		TextView nameView = (TextView) dialog.findViewById(R.id.dialog_grade_folder_name);
 		nameView.setText("Title: " + folder.getTitle());
 		
 		TextView gradeView = (TextView) dialog.findViewById(R.id.dialog_grade_folder_grade);
 		gradeView.setText("Grade: " + folder.getGrade());
 		
 		TextView rangeView = (TextView) dialog.findViewById(R.id.dialog_grade_folder_range);
 		rangeView.setText("Range: " + folder.getRange());
 		
 		TextView percentageView = (TextView) dialog.findViewById(R.id.dialog_grade_folder_percentage);
 		percentageView.setText("Percentage: " + folder.getPercentage());
 		
 		TextView feedbackView = (TextView) dialog.findViewById(R.id.dialog_grade_folder_feedback);
 		feedbackView.setText("Feedback: " + folder.getFeedback());
 		
 		LinearLayout items = (LinearLayout) dialog.findViewById(R.id.dialog_grade_folder_items);
 		for (final GradeItem item : folder.getItems()) {
 			if (item instanceof GradeFolder) {
 				Button itemButton = new Button(items.getContext());
 				itemButton.setText("Item: " + ((GradeFolder) item).getTitle());
 				itemButton.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						getDialog((GradeFolder) item, context).show();
 					}
 				});
 				items.addView(itemButton);
 			} else {
 				Button itemButton = new Button(items.getContext());
 				itemButton.setText("Item: " + item.getName());
 				itemButton.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						getDialog(item, context).show();
 					}
 				});
 				items.addView(itemButton);
 			}
 		}
 		
 		return dialog;
 	}
 	
 	private static Dialog getDialog(final GradeItem item, Context context) {
 		Dialog dialog = new Dialog(context);
 		dialog.setContentView(R.layout.dialog_grades_item);
 		TextView nameView = (TextView) dialog.findViewById(R.id.dialog_grade_item_name);
 		nameView.setText("Title: " + item.getName());
 		
 		TextView gradeView = (TextView) dialog.findViewById(R.id.dialog_grade_item_grade);
 		gradeView.setText("Grade: " + item.getGrade());
 		
 		TextView rangeView = (TextView) dialog.findViewById(R.id.dialog_grade_item_range);
 		rangeView.setText("Range: " + item.getRange());
 		
 		TextView percentageView = (TextView) dialog.findViewById(R.id.dialog_grade_item_percentage);
 		percentageView.setText("Percentage: " + item.getPercentage());
 		
 		TextView feedbackView = (TextView) dialog.findViewById(R.id.dialog_grade_item_feedback);
 		feedbackView.setText("Feedback: " + item.getFeedback());
 		
 		//TODO:  Add url clicking
 		
 		return dialog;
 	}
 	
 	private View getGradesView(final GradeFolder grades) {
 		//Create a new view
		if (getActivity() == null) return null;
		View inflatedView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_grade_folder, mContentView, false);
 		TextView titleTextView = (TextView) inflatedView.findViewById(R.id.list_item_grade_folder_title);
 		titleTextView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getDialog(grades, v.getContext()).show();
 			}
 		});
 		LinearLayout contentView = (LinearLayout) inflatedView.findViewById(R.id.list_item_grade_folder_content);
 		TextView detailTextView = (TextView) inflatedView.findViewById(R.id.list_item_grade_folder_details);
 		detailTextView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getDialog(grades, v.getContext()).show();
 			}
 		});
 		
 		//Initialize the title
 		titleTextView.setText(grades.getTitle());
 		
 		//Add the children to the view
 		for (GradeItem item : grades.getItems()) {
 			//Declare the item's view
 			View itemView = null;
 			
 			//Generate view based on item type
 			if (item instanceof GradeFolder) {
 				itemView = getGradesView((GradeFolder) item);
 			} else {
 				itemView = getGradesView(item);
 			}
 			
 			//Add view to parent
 			contentView.addView(itemView);
 		}
 		
 		//Initialize the details field
 		detailTextView.setText(String.format("%s %2$-7s %3$-13s %4$-9s %s", grades.getName(), grades.getGrade(), grades.getRange(), grades.getPercentage(), grades.getFeedback()));
 		
 		return inflatedView;
 	}
 	
 	private View getGradesView(final GradeItem item) {
 		//Inflate new view
		View inflatedView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_grade_item, null, false);
 		
 		//Initialize the title
 		TextView titleTextView = (TextView) inflatedView.findViewById(R.id.list_item_grade_item_title);
 		titleTextView.setText(String.format("%s %2$-7s %3$-13s %4$-9s %s", item.getName(), item.getGrade(), item.getRange(), item.getPercentage(), item.getFeedback()));
 		titleTextView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getDialog(item, v.getContext()).show();
 			}
 		});
 		//Return the inflated view
 		return inflatedView;
 	}
 }

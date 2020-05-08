 package org.bcp.mobile;
 
 import org.bcp.mobile.lib.DatabaseHandler;
 
 import org.bcp.mobile.R;
 
 import de.keyboardsurfer.android.widget.crouton.Configuration;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MenuListFragment extends ListFragment {
 	
 	SampleAdapter adapter;
 	Toast toast;
 	int easterEgg = 0;
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.list, null);
 	}
 
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		adapter = new SampleAdapter(getActivity());
 		SharedPreferences userPref = getActivity().getSharedPreferences("username", Context.MODE_PRIVATE);
 		String savedUsername = userPref.getString("username", "");
 		String[] userInfo = getNameFromUsername(savedUsername);
 		adapter.add(new SampleItem(userInfo[0] + " " + userInfo[1] + " '" + userInfo[2], R.drawable.bell));
 		adapter.add(new SampleItem("Grades", R.drawable.book));
 		adapter.add(new SampleItem("Announcements", R.drawable.speaker));
 		adapter.add(new SampleItem("Calendar", R.drawable.calendar));
 		adapter.add(new SampleItem("News", R.drawable.world));
 		adapter.add(new SampleItem("About", R.drawable.info));
 		adapter.add(new SampleItem("Contact Us", R.drawable.mail));
 		adapter.add(new SampleItem("Rate", R.drawable.heart));
 		adapter.add(new SampleItem("Log Out", R.drawable.directional_left));
 		setListAdapter(adapter);
 	}
 	
 	public void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Intent intent;
         String about;
         AlertDialog.Builder builder;
 		switch (position) {
 			case 0:
 				easterEgg++;
 				if(easterEgg >= 7) { // tap name 7 times for easter egg.
 					displayToast();
 					easterEgg = 0;
 				}
 				break;
 			case 1: // Grades
 				intent = new Intent(getActivity(), GradeViewActivity.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 				startActivity(intent);
 				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 				break;
 			case 2: // Announcements
 				intent = new Intent(getActivity(), AnnouncementsActivity.class);
 				startActivity(intent);
 				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 				break;
 			case 3: // Calendar
 				intent = new Intent(getActivity(), CalendarActivity.class);
 				startActivity(intent);
 				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 				break;
 			case 4: // News
 				intent = new Intent(getActivity(), NewsRssActivity.class);
 				startActivity(intent);
 				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 				break;
 			case 5: // About
 	        	about = "By Richard Lin '13, with help from Jonathan Chang '13\n\nInspired by Bryce Pauken '14\n\n" +
 		    			"Based on Bryce's BCP Mobile app for iOS, this app was created to provide Android-loving " +
 		    			"Bellarmine students a convenient way to check their grades, view announcements, and more.\n\n" +
 		    			"If you're enjoying this app, please share this with your friends!";
 		        builder = new AlertDialog.Builder(getActivity());
 		        builder.setTitle("About");
 		        builder.setMessage(about);
 		        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int id) {
 		            }
 		        });
 		        builder.setNegativeButton("View on GitHub", new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int id) {
 		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/richard1/"));
 		            	startActivity(browserIntent);
 		            }
 		        });
 		        builder.show();
 		        break;
 			case 6:
 				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","richard@team254.com,jonathan.chang13@gmail.com", null));
 				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BCP Mobile Comment/Question/Suggestion");
 				startActivity(Intent.createChooser(emailIntent, "Send email..."));
 				break;
 			case 7: // Rate
 				about = "If this app has helped you out, feel free to leave a rating on the Google Play Store page. Thanks so much!";
 		        builder = new AlertDialog.Builder(getActivity());
 		        builder.setTitle("Rate");
 		        builder.setMessage(about);
 		        builder.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int id) {
 		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/")); // TODO: replace with link
 		            	startActivity(browserIntent);
 		            }
 		        });
 		        builder.setNegativeButton("Maybe Later", new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int id) {
 		            }
 		        });
 		        builder.show();
 				break;
 	        case 8:
 	    		new DatabaseHandler(getActivity()).deleteAll();
 	    		getActivity().getSharedPreferences("username", Context.MODE_PRIVATE).edit().clear().commit();
 	    		getActivity().getSharedPreferences("password", Context.MODE_PRIVATE).edit().clear().commit();
 		        intent = new Intent(getActivity(), LoginActivity.class);
 		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 		        startActivity(intent);
 		        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
 		        break;
 		}
     }
 
 	private class SampleItem {
 		public String tag;
 		public int iconRes;
 		public SampleItem(String tag, int iconRes) {
 			this.tag = tag; 
 			this.iconRes = iconRes;
 		}
 	}
 
 	public class SampleAdapter extends ArrayAdapter<SampleItem> {
 
 		public SampleAdapter(Context context) {
 			super(context, 0);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
 			}
 			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
 			icon.setImageResource(getItem(position).iconRes);
 			TextView title = (TextView) convertView.findViewById(R.id.row_title);
 			title.setText(getItem(position).tag);
 
 			return convertView;
 		}
 
 	}
 	
 	@SuppressLint("DefaultLocale")
 	public String[] getNameFromUsername(String username) {
 		String[] names = new String[3];
 		if(username.contains("@")) { // if people log in with email address
 			username = username.substring(0, username.indexOf("@"));
 		}
 		names[0] = username.substring(0, 1).toUpperCase() + username.substring(1, username.indexOf("."));
 		names[1] = username.substring(username.indexOf(".") + 1, username.indexOf(".") + 2).toUpperCase() 
 				+ username.substring(username.indexOf(".") + 2, username.length() - 2);
 		names[2] = username.substring(username.length() - 2, username.length());
 		return names;
 	}
 	
 	public void displayToast() {
 		Toast ImageToast = new Toast(getActivity());
         LinearLayout toastLayout = new LinearLayout(getActivity());
         toastLayout.setOrientation(LinearLayout.HORIZONTAL);
         ImageView image = new ImageView(getActivity());
         image.setImageResource(R.drawable.bell_shield);
         image.setLayoutParams(new TableRow.LayoutParams(254, 254)); // :)
         toastLayout.addView(image);
         ImageToast.setView(toastLayout);
        ImageToast.setDuration(Toast.LENGTH_SHORT);
         ImageToast.setGravity(Gravity.CENTER, 0, 0);
         ImageToast.show();
 	    if(toast != null) {
 	        toast.cancel();
 	    }
 	    toast = ImageToast;
 	    toast.show();
 	}
 	
 	public void onPause() {
 	    if(toast != null) {
 	        toast.cancel();
 	    }
 	    super.onPause();
 	}
 }

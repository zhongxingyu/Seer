 package matdue.raidstatus;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import matdue.raidstatus.data.Raid;
 import matdue.raidstatus.data.RaidMember;
 import matdue.raidstatus.data.database.RaidDatabase;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class RaidInfoActivity extends Activity {
 
 	private static int HORIZONTAL_FLING_THRESHOLD = 50;
 	
 	private GestureDetector mGestureDetector;
 	private int currentRaidIdx = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.raidinfo);
 		
 		mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
 			@Override
 			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 				int distanceX = Math.abs((int) e2.getX() - (int) e1.getX());
                 int distanceY = Math.abs((int) e2.getY() - (int) e1.getY());
                 if (distanceX < HORIZONTAL_FLING_THRESHOLD || distanceX < distanceY) {
                     return false;
                 }
                 
                 // Get next screen. As we have two screens, the next screen is the non-active screen.
             	ViewFlipper viewFlipper = (ViewFlipper) RaidInfoActivity.this.findViewById(R.id.screen_flipper);
             	int nextScreen = viewFlipper.getDisplayedChild() == 0 ? 1 : 0;
             	View screen = viewFlipper.getChildAt(nextScreen);
             	
                 if (velocityX > 0.0f) {
                 	// Switch to previous raid
                 	if (!displayRaid(screen, --currentRaidIdx)) {
                 		++currentRaidIdx;
                 		Toast.makeText(RaidInfoActivity.this, R.string.raid_info_first_reached, Toast.LENGTH_SHORT).show();
                 		return true;
                 	}
                 	
                 	viewFlipper.setInAnimation(screen.getContext(), R.anim.push_right_in);
                 	viewFlipper.setOutAnimation(screen.getContext(), R.anim.push_right_out);
                 	viewFlipper.setDisplayedChild(nextScreen);
                 } else {
                 	// Switch to next raid
                 	if (!displayRaid(screen, ++currentRaidIdx)) {
                 		--currentRaidIdx;
                 		Toast.makeText(RaidInfoActivity.this, R.string.raid_info_last_reached, Toast.LENGTH_SHORT).show();
                 		return true;
                 	}
                 	
                 	viewFlipper.setInAnimation(screen.getContext(), R.anim.push_left_in);
                 	viewFlipper.setOutAnimation(screen.getContext(), R.anim.push_left_out);
                 	viewFlipper.setDisplayedChild(nextScreen);
                 }
                 
                 return true;
 			}
 		});
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt("currentRaidIdx", currentRaidIdx);
 		
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		if (savedInstanceState != null) {
 			currentRaidIdx = savedInstanceState.getInt("currentRaidIdx", 0);
 		}
 		
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.screen_flipper);
 		View screen = viewFlipper.getCurrentView();
 		displayRaid(screen, currentRaidIdx);
 	}
 	
 	private boolean displayRaid(final View screen, int idx) {
 		if (idx < 0) {
 			return false;
 		}
 		
 		RaidDatabase db = new RaidDatabase(this);
 		Raid raid = db.loadRaid(idx);
 		db.close();
 		
 		if (raid == null) {
 			return false;
 		}
 		
 		// Raid information
 		TextView view = (TextView) screen.findViewById(R.id.raid_info_title);
 		view.setText(raid.getName());
 		
 		String message = getResources().getString(R.string.main_raid_datetime, 
 				DateFormat.format(getResources().getString(R.string.main_raid_date_format), raid.getStart()),
 				DateFormat.format(getResources().getString(R.string.main_raid_time_format), raid.getStart()));
 		view = (TextView) screen.findViewById(R.id.raid_info_datetime);
 		view.setText(message);
 		
 		String url = PreferencesActivity.getApplicationPreferences(this).getString("url", "");
 		if (!url.endsWith("/")) {
 			url = url + "/";
 		}
 		url = url + "games/WoW/events/" + raid.getIcon();
 		new ImageLoaderTask(getCacheDir()) {
 			@Override
 			protected void onPostExecute(Bitmap result) {
 				if (result != null) {
 					ImageView image = (ImageView) screen.findViewById(R.id.raid_info_logo);
 		    		image.setImageBitmap(result);
 				}
 			};
 		}.execute(url);
 
 		// Players
 		WebView infoView = (WebView) screen.findViewById(R.id.raid_info_main);
 		infoView.cancelLongPress();
 		infoView.getSettings().setDefaultTextEncodingName("utf-8");
 		infoView.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				if (mGestureDetector.onTouchEvent(event)) {
 	                // Return false, although the event has been consumed.
 	                // Otherwise, the WebView view does not update its content.
 		            return false;
 		        }
 				return false;
 			}
 		});
 		infoView.loadData(createHtmlContent(raid), "text/html", "utf-8");
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (mGestureDetector.onTouchEvent(event)) {
             return true;
         }
 
         return super.onTouchEvent(event);
 	}
 		
 	private String createHtmlContent(Raid raid) {
 		StringBuilder content = new StringBuilder();
 		content.append("<html>" +
 				"	<head>" +
 				"		<style type=\"text/css\">" +
 				"			body{font-size:10pt;}" +
 				"			ul,li{list-style:none;margin:0;padding:0;}" +
 				"			li.subscription{margin:15px 0 0 0;}" +
 				"			ul.task{margin:8px 0 0 5px;}" +
 				"			ul.member{margin:5px 0 0 5px;}" +
 				"			li.note{margin-left:15px;color:808080;}" +
 				"		</style>" +
 				"	</head>" +
 				"	<body>" +
 				"		<ul>");
 
 		if (raid == null) {
 			content.append("<li>Keine Daten vorhanden</li>");
 		} else {
 			// Confirmed
 			createContent(content, raid, 1);
 			
 			// Signed
 			createContent(content, raid, 2);
 			
 			// Unsigned
 			createContent(content, raid, 3);
 			
 			// Backup
 			createContent(content, raid, 4);
 
 			// Not signed
 			createContent(content, raid, 5);
 		}
 		
 		content.append("		</ul>" +
 				"	</body>" +
 				"</html>");
 		
 		return content.toString();
 	}
 	
 	private void createContent(StringBuilder content, Raid raid, int subscription) {
 		RaidMember[] members = collectMembers(raid, subscription);
 		if (members.length != 0) {
 			content.append("<li class=\"subscription\">").append(getResources().getStringArray(R.array.subscription)[subscription]).append("</li>");
 			content.append("<ul class=\"task\">");
 			createContent(content, members);
 			content.append("</ul>");
 		}
 	}
 	
 	private void createContent(StringBuilder content, RaidMember[] members) {
 		RaidMember[] tanks = filterMembers(members, "tank");
 		createContent(content, tanks, 0);
 		
 		RaidMember[] healers = filterMembers(members, "healer");
 		createContent(content, healers, 1);
 		
 		RaidMember[] melees = filterMembers(members, "melee");
 		createContent(content, melees, 2);
 		
 		RaidMember[] ranges = filterMembers(members, "range");
 		createContent(content, ranges, 3);
 		
 		RaidMember[] unknown = filterMembers(members, null);
 		createContent(content, unknown, -1);
 	}
 	
 	private void createContent(StringBuilder content, RaidMember[] members, int role) {
 		if (members.length == 0) {
 			return;
 		}
 		
 		if (role != -1) {
 			content.append("<li>").append(getResources().getStringArray(R.array.role)[role]).append("</li>");
 		}
 		
 		content.append("<ul class=\"member\">");
 		for (RaidMember member : members) {
 			content.append("<li>").append(member.getPlayer().getName()).append("</li>");
 			if (member.getNote() != null && member.getNote().length() != 0) {
 				content.append("<li class=\"note\">").append(member.getNote()).append("</li>");
 			}
 		}
 		content.append("</ul>");
 	}
 
 	private RaidMember[] collectMembers(Raid raid, int subscription) {
 		ArrayList<RaidMember> members = new ArrayList<RaidMember>();
 		for (RaidMember m : raid.getRaidMembers()) {
 			if (m.getSubscribed() == subscription) {
 				members.add(m);
 			}
 		}
 		
 		// Sort by name
 		final Collator col = Collator.getInstance();
 		RaidMember[] sortedMembers = members.toArray(new RaidMember[0]);
 		Arrays.sort(sortedMembers, new Comparator<RaidMember>() {
 			public int compare(RaidMember x, RaidMember y) {
 				return col.compare(x.getPlayer().getName(), y.getPlayer().getName());
 			}}
 		);
 		
 		return sortedMembers;
 	}
 	
 	private RaidMember[] filterMembers(RaidMember[] members, String byRole) {
 		ArrayList<RaidMember> filteredMembers = new ArrayList<RaidMember>();
 		for (RaidMember member : members) {
 			if (byRole == null && member.getRole() == null) {
 				filteredMembers.add(member);
 			}
 			else if (byRole != null && byRole.equals(member.getRole())) {
 				filteredMembers.add(member);
 			}
 		}
 		
 		return filteredMembers.toArray(new RaidMember[0]);
 	}
 }

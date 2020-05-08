 package jp.knct.di.c6t.ui.route;
 
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.model.Quest;
 import jp.knct.di.c6t.model.Route;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class RouteCreationActivity extends Activity implements OnClickListener {
 	private GoogleMap mMap;
 	private Route mRoute = new Route();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route_creation);
 
 		setOnClickListeners();
		mMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.route_creation_map)).getMap();
 		mMap.setMyLocationEnabled(true);
 
 		updateMap();
 	}
 
 	private MarkerOptions createStartPointMarker(Route route) {
 		LatLng startLocation = route.getStartLocation();
		return new MarkerOptions().position(startLocation).title("X^[g|Cg");
 	}
 
 	private MarkerOptions createQuestPointMarker(Quest quest, int questNumber) {
 		LatLng location = quest.getLocation();
 		String title = "NGXg|Cg" + questNumber + ": " + quest.getTitle();
		return new MarkerOptions().position(location).title(title).snippet(quest.getMission());
 	}
 
 	private void updateMap() {
 		mMap.clear();
 
 		if (mRoute.getStartLocation() != null) {
 			mMap.addMarker(createStartPointMarker(mRoute));
 		}
 
 		int questNumber = 1;
 		for (Quest quest : mRoute.getQuests()) {
 			mMap.addMarker(createQuestPointMarker(quest, questNumber));
 			questNumber++;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == RouteCreationQuestFormActivity.REQUEST_CODE_EDIT_QUEST) {
 			if (resultCode == RESULT_CANCELED) {/* do nothing */}
 			else if (resultCode == RESULT_OK) {
 				try {
 					String questJSON = data.getStringExtra(IntentData.EXTRA_KEY_JSON_BASE_QUEST);
 					Log.d("quest", questJSON);
 					Quest quest = Quest.parseJSONString(questJSON);
 					mRoute.addQuest(quest);
 
 					if (mRoute.getQuests().size() == 5) {
 						Toast.makeText(this, "I", 0).show();
 						// TODO
 					}
 					else if (mRoute.getQuests().size() > 5) {
 						// TODO
 					}
 				}
 				catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 		updateMap();
 	}
 
 	private void setOnClickListeners() {
 		int[] ids = {
 				R.id.route_creation_start,
 				R.id.route_creation_create_new_quest,
 				R.id.route_creation_quests,
 				R.id.route_creation_finish,
 				// R.id.debug_route_creation_update,
 		};
 
 		for (int id : ids) {
 			findViewById(id).setOnClickListener(this);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		switch (v.getId()) {
 		case R.id.route_creation_start:
 			mRoute.setStartLocation(getCurrentLocation());
 			updateMap();
 			Toast.makeText(this, "ݒn_X^[g|Cgɐݒ肵܂B", Toast.LENGTH_SHORT).show();
 			break;
 
 		case R.id.route_creation_create_new_quest:
 			String questJSON = new Quest(getCurrentLocation()).toJSON().toString();
 			intent = new Intent(this, RouteCreationQuestFormActivity.class)
 					.putExtra(IntentData.EXTRA_KEY_JSON_BASE_QUEST, questJSON);
 			startActivityForResult(intent, RouteCreationQuestFormActivity.REQUEST_CODE_EDIT_QUEST);
 			break;
 
 		case R.id.route_creation_quests:
 			// TODO
 			break;
 
 		case R.id.route_creation_finish:
 			String routeJSON = mRoute.toJSON().toString();
 			intent = new Intent(this, RouteCreationDetailFormActivity.class)
 					.putExtra(IntentData.EXTRA_KEY_JSON_ROUTE, routeJSON);
 			startActivity(intent);
 			break;
 
 		// case R.id.route_creation_debug_route_creation_updatequests:
 		// break;
 
 		default:
 			break;
 		}
 	}
 
 	private LatLng getCurrentLocation() {
 		double latitude = Double.parseDouble(((EditText) findViewById(R.id.debug_route_creation_lat)).getText().toString());
 		double longitude = Double.parseDouble(((EditText) findViewById(R.id.debug_route_creation_lng)).getText().toString());
 		return new LatLng(latitude, longitude);
 	}
 
 }

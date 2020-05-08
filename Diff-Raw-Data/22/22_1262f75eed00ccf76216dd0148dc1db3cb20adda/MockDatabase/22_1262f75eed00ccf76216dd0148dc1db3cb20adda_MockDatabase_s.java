 package se.team05.test.util;
 
 import java.util.ArrayList;
 
 import se.team05.content.ParcelableGeoPoint;
 import se.team05.content.Route;
 import se.team05.content.Track;
 import se.team05.data.DatabaseHandler;
 import se.team05.overlay.CheckPoint;
 import android.app.Activity;
 import android.database.Cursor;
 import android.provider.MediaStore;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.google.android.maps.GeoPoint;
 
 public class MockDatabase
 {
 	public static Route getRoute(ActivityInstrumentationTestCase2 testCase, Activity activity)
 	{
 		DatabaseHandler databaseHandler = new DatabaseHandler(activity);
		Route route = new Route("name", "description");
 		route.setId(databaseHandler.saveRoute(route));
 		
 		ArrayList<ParcelableGeoPoint> geoPointList = new ArrayList<ParcelableGeoPoint>();
 		ParcelableGeoPoint gpA = new ParcelableGeoPoint((int)(47.975 * 1E6), (int)(17.056 * 1E6));
 		ParcelableGeoPoint gpB = new ParcelableGeoPoint((int)(48.975 * 1E6), (int)(17.056 * 1E6));
 		geoPointList.add(gpA);
 		geoPointList.add(gpB);
 		databaseHandler.saveGeoPoints(route.getId(), geoPointList);
 		
 		CheckPoint checkPoint = new CheckPoint(new GeoPoint((int) (48.975 * 1E6), (int) (17.056 * 1E6)));
 		checkPoint.setRid(route.getId());
 		long cid = databaseHandler.saveCheckPoint(checkPoint);
 		
 		Cursor cursor = activity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
 		cursor.moveToFirst();
 		if(cursor.getCount() < 1)
 		{
 			testCase.fail("Could not find media in the media store, please add media to your device and reboot before testing again.");
 		}
 		else
 		{
 			Track track = new Track("id", "artist", "album", "title", cursor.getString(0), "displayName", "duration");
 			databaseHandler.saveTrack(cid, track);
 		}
 		
 		route.setCheckPoints(databaseHandler.getCheckPoints(route.getId()));
 		return route;
 	}
 }

 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.team05.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.team05.content.Result;
 import se.team05.content.Route;
 import se.team05.content.Track;
 import se.team05.overlay.CheckPoint;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.google.android.maps.GeoPoint;
 
 /**
  * This class handles the communication between the database, its adapters and
  * the rest of the application. It keeps an instance of each table adapter that
  * is used for quick and easy access to the database.
  * 
  * @author Daniel Kvist, Gustaf Werlinder, Markus Schutzer, Patrik Thitusson,
  *         Henrik Hugo
  * 
  */
 public class DatabaseHandler
 {
 
 	private DBRouteAdapter dBRouteAdapter;
 	private DBTrackAdapter dbTrackAdapter;
 	private DBResultAdapter dbResultAdapter;
 	private DBCheckPointAdapter dbCheckPointAdapter;
 	private DBGeoPointAdapter dbGeoPointAdapter;
 
 	/**
 	 * Constructor of the class which takes a context to operate in as a
 	 * parameter. The constructor initialises all the database adapters.
 	 * 
 	 * @param context
 	 *            the context to operate in
 	 */
 	public DatabaseHandler(Context context)
 	{
 		dBRouteAdapter = new DBRouteAdapter(context);
 		dbTrackAdapter = new DBTrackAdapter(context);
 		dbResultAdapter = new DBResultAdapter(context);
 		dbCheckPointAdapter = new DBCheckPointAdapter(context);
 		dbGeoPointAdapter = new DBGeoPointAdapter(context);
 	}
 
 	/**
 	 * This deletes the route given from the database
 	 * 
 	 * @param route
 	 *            the route to delete
 	 */
 	public void deleteRoute(Route route)
 	{
 		dBRouteAdapter.open();
 		dBRouteAdapter.deleteRoute(route.getId());
 		dBRouteAdapter.close();
 	}
 
 	/**
 	 * This method saves the route in the database.
 	 * 
 	 * @param route
 	 *            the route to save
 	 */
 	public long saveRoute(Route route)
 	{
 		dBRouteAdapter.open();
 		long id = dBRouteAdapter.insertRoute(route.getName(), route.getDescription(), route.getType(), 0, 0);
 		dBRouteAdapter.close();
 		return id;
 	}
 
 	/**
 	 * Gets a Route from the database by the id provided.
 	 * 
 	 * @param id
 	 * @return a Route if id is found, null otherwise
 	 */
 	public Route getRoute(long id)
 	{
 		dBRouteAdapter.open();
 		Cursor cursor = dBRouteAdapter.fetchRoute(id);
 		Route route = null;
 
 		if (cursor != null && cursor.getCount() != 0)
 		{
 			cursor.moveToFirst();
 			route = new Route(
 					cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_ID)),
 					cursor.getString(cursor.getColumnIndex(DBRouteAdapter.COLUMN_NAME)),
 					cursor.getString(cursor.getColumnIndex(DBRouteAdapter.COLUMN_DESCRIPTION)),
 					cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_TYPE)),
 					cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_TIMECOACH)),
 					cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_LENGTHCOACH)));
 
 			cursor.close();
 		}
 
 		dBRouteAdapter.close();
 		return route;
 	}
 
 	/**
 	 * Get all routes from the database.
 	 * 
 	 * @return an array with Route objects
 	 */
 	public Route[] getAllRoutes()
 	{
 		dBRouteAdapter.open();
 		Cursor cursor = dBRouteAdapter.getAllRoutes();
 		List<Route> routeList = null;
 		if (cursor != null && cursor.getCount() != 0)
 		{
 			Route route;
 			cursor.moveToFirst();
 			routeList = new ArrayList<Route>();
 
 			while (!cursor.isAfterLast())
 			{
 				route = new Route(cursor.getInt(
 						cursor.getColumnIndex(DBRouteAdapter.COLUMN_ID)),
 						cursor.getString(cursor.getColumnIndex(DBRouteAdapter.COLUMN_NAME)),
 						cursor.getString(cursor.getColumnIndex(DBRouteAdapter.COLUMN_DESCRIPTION)),
 						cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_TYPE)),
 						cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_TIMECOACH)),
 						cursor.getInt(cursor.getColumnIndex(DBRouteAdapter.COLUMN_LENGTHCOACH))
 				);
 
 				routeList.add(route);
 				cursor.moveToNext();
 			}
 		}
 		// TODO
 		// dbResultAdapter.close(); ??? /guswer
 		//
 		return (Route[]) routeList.toArray();
 	}
 
 	/**
 	 * Get the cursor with all routes in the database unformatted.
 	 * 
 	 * @return a cursor.
 	 */
 	public Cursor getAllRoutesCursor()
 	{
 		dBRouteAdapter.open();
 		Cursor cursor = dBRouteAdapter.getAllRoutes();
 		return cursor;
 	}
 
 	/**
 	 * Saves a track into the database and relates it to a checkpoint through
 	 * the checkpoint id
 	 * 
 	 * @param cid
 	 *            the id of the checkpoint to relate the track to
 	 * @param track
 	 *            the track to save
 	 */
 	public void saveTrack(long cid, Track track)
 	{
 		dbTrackAdapter.open();
 		dbTrackAdapter.insertTrack(cid, track.getArtist(), track.getAlbum(), track.getTitle(), track.getData(),
 				track.getDisplayName(), track.getDuration());
 		dbTrackAdapter.close();
 	}
 
 	/**
 	 * Gets the tracks related to a specific checkpoint from the database.
 	 * 
 	 * @param cid
 	 *            the checkpoint id that the tracks are related to
 	 * @return an ArrayList of Track's
 	 */
 	public ArrayList<Track> getTracks(long cid)
 	{
 		ArrayList<Track> tracks = new ArrayList<Track>();
 		dbTrackAdapter.open();
 		Cursor cursor = dbTrackAdapter.fetchTrackByCid(cid);
 		while (cursor.moveToNext())
 		{
 			tracks.add(new Track(
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_ID)),
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_ARTIST)),
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_ALBUM)),
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_TITLE)),
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_DATA)),
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_DISPLAY_NAME)),
 					cursor.getString(cursor.getColumnIndex(DBTrackAdapter.COLUMN_DURATION))));
 		}
 		dbTrackAdapter.close();
 		return tracks;
 	}
 
 	/**
 	 * This method returns a result (an instance of class Result) retrieved from
 	 * database via a database adapter.
 	 * 
 	 * @param id
 	 *            id tells which row to get from database.
 	 * @return result
 	 */
 	public Result getResultById(int id)
 	{
 		Result result;
 
 		dbResultAdapter.open();
 		Cursor cursor = dbResultAdapter.fetchResultById(id);
 		result = createResultFromCursor(cursor);
 		dbResultAdapter.close();
 
 		return result;
 	}
 
 	/**
 	 * This method returns an array of results (instances of class Result)
 	 * retrieved from database via a database adapter.
 	 * 
 	 * @param routId
 	 *            id tells which row to get from database.
 	 * @return result
 	 */
 	public Result[] getAllResultsByRoutId(int routId)
 	{
 		List<Result> resultList = null;
 
 		dbResultAdapter.open();
 		Cursor cursor = dbResultAdapter.fetchResultById(routId);
 		dbResultAdapter.close();
 
 		if (cursor != null && cursor.getCount() != 0)
 		{
 			resultList = new ArrayList<Result>();
 			Result result;
 			cursor.moveToFirst();
 
 			while (!cursor.isLast())
 			{
 				result = createResultFromCursor(cursor);
 				resultList.add(result);
 				cursor.moveToNext();
 			}
 		}
 		return (Result[]) resultList.toArray();
 	}
 
 	/**
 	 * This help method will return a result made from values gotten through a
 	 * cursor given in parameter
 	 * 
 	 * @param cursor
 	 *            the cursor gives read/write access to a set retrieved from
 	 *            database.
 	 * @return result retrieved from database.
 	 */
 	private Result createResultFromCursor(Cursor cursor)
 	{
 		Result result = new Result(
 				cursor.getInt(cursor.getColumnIndex(DBResultAdapter.COLUMN_ID)),
 				cursor.getInt(cursor.getColumnIndex(DBResultAdapter.COLUMN_TIMESTAMP)),
 				cursor.getInt(cursor.getColumnIndex(DBResultAdapter.COLUMN_TIME)),
 				cursor.getInt(cursor.getColumnIndex(DBResultAdapter.COLUMN_DISTANCE)),
 				cursor.getInt(cursor.getColumnIndex(DBResultAdapter.COLUMN_CALORIES))
 		);
 		return result;
 	}
 
 	/**
 	 * Saves a result in the database.
 	 * 
 	 * @param result
 	 *            the result to save
 	 */
 	public void saveResult(Result result)
 	{
 		dbResultAdapter.open();
 		dbResultAdapter.instertResult(result.getRoutId(), result.getTimestamp(), result.getTime(),
 				result.getDistance(), result.getCalories());
 		dbResultAdapter.close();
 	}
 
 	/**
 	 * This method deletes a result by id from the database.
 	 * 
 	 * @param id
 	 *            id of result to be deleted.
 	 */
 	public void deleteResultById(int id)
 	{
 		dbResultAdapter.open();
 		dbResultAdapter.deleteResultById(id);
 		dbResultAdapter.close();
 	}
 
 	/**
 	 * This method deletes a result by rout id (rid) from the database.
 	 * 
 	 * @param rid
 	 *            the rout id
 	 */
 	public void deleteAllResultsByRoutId(int rid)
 	{
 		dbResultAdapter.open();
 		dbResultAdapter.deleteResultByRid(rid);
 		dbResultAdapter.close();
 	}
 
 	/**
 	 * Saves a checkpoint in the database
 	 * 
 	 * @param checkPoint
 	 *            the checkpoint to save
 	 * @return the new checkpoint id
 	 */
 	public long saveCheckPoint(CheckPoint checkPoint)
 	{
 		dbCheckPointAdapter.open();
 		
 		long id = dbCheckPointAdapter.insertCheckpoint(checkPoint.getRid(), checkPoint.getRadius(),
 					checkPoint.getName(), checkPoint.getLatuitude(), checkPoint.getLongitude());
 		dbCheckPointAdapter.close();
 		return id;
 	}
 
 	/**
 	 * Deletes a checkpoint from the database with the corresponding checkpoint
 	 * id.
 	 * 
 	 * @param cid
 	 *            the checkpoint id that matches the row to delete
 	 */
 	public void deleteCheckPoint(long cid)
 	{
 		dbCheckPointAdapter.open();
 		dbCheckPointAdapter.deleteCheckPointById(cid);
 		dbCheckPointAdapter.close();
 	}
 
 	/**
 	 * Deletes all tracks from the database with the corresponding checkpoint
 	 * id.
 	 * 
 	 * @param cid
 	 *            the checkpoint id that matches the tracks to delete
 	 */
 	public void deleteTracksByCid(long cid)
 	{
 		dbTrackAdapter.open();
 		dbTrackAdapter.deleteTrackByCid(cid);
 		dbTrackAdapter.close();
 	}
 
 	/**
 	 * Saves a list of geopoints that have been recorded for a route.
 	 * 
 	 * @param rid
 	 *            the route id to connect the geo points with
 	 * @param geoPointList
 	 *            the list of geopoints that have been recorded
 	 */
 	public void saveGeoPoints(long rid, ArrayList<GeoPoint> geoPointList)
 	{
 		dbGeoPointAdapter.open();
 		dbGeoPointAdapter.insertGeoPoints(rid, geoPointList);
 		dbGeoPointAdapter.close();
 	}
 
 	/**
 	 * Gets all geo points for a specific route.
 	 * 
 	 * @param rid
 	 *            the route id to match
 	 * @return a list of geo points
 	 */
 	public ArrayList<GeoPoint> getGeoPoints(long rid)
 	{
 		dbGeoPointAdapter.open();
 		Cursor cursor = dbGeoPointAdapter.fetchGeoPointByRid(rid);
 		ArrayList<GeoPoint> geoPointList = null;
 
 		if (cursor != null && cursor.getCount() != 0)
 		{
 			geoPointList = new ArrayList<GeoPoint>();
 			cursor.moveToFirst();
 
 			while (!cursor.isAfterLast())
 			{
 				GeoPoint geoPoint = new GeoPoint(
 						cursor.getInt(
 								cursor.getColumnIndex(DBGeoPointAdapter.COLUMN_LATITUDE)),
 								cursor.getInt(cursor.getColumnIndex(DBGeoPointAdapter.COLUMN_LONGITUDE))
 						);
 				geoPointList.add(geoPoint);
 				cursor.moveToNext();
 			}
 			cursor.close();
 		}
 		dbGeoPointAdapter.close();
 
 		return geoPointList;
 	}
 
 	/**
 	 * Gets all check points for a specific route.
 	 * 
 	 * @param rid
 	 *            the route id to match
 	 * @return a list of check points
 	 */
 	public ArrayList<CheckPoint> getCheckPoints(long rid)
 	{
 		dbCheckPointAdapter.open();
 		Cursor cursor = dbCheckPointAdapter.fetchCheckPointByRid(rid);
 		ArrayList<CheckPoint> checkPointList = null;
 
		if (cursor != null && cursor.getCount() != 0)
 		{
 			checkPointList = new ArrayList<CheckPoint>();
 			cursor.moveToFirst();
 
 			while (!cursor.isAfterLast())
 			{
 				GeoPoint geoPoint = new GeoPoint(
 						cursor.getInt(cursor.getColumnIndex(DBCheckPointAdapter.COLUMN_LATITUDE)),
 						cursor.getInt(cursor.getColumnIndex(DBCheckPointAdapter.COLUMN_LONGITUDE))
 				);
 
 				CheckPoint checkPoint = new CheckPoint(geoPoint);
 				checkPoint.setRadius(cursor.getInt(cursor.getColumnIndex(DBCheckPointAdapter.COLUMN_RADIUS)));
 				checkPoint.setName(cursor.getString(cursor.getColumnIndex(DBCheckPointAdapter.COLUMN_NAME)));
 				checkPoint.setRid(cursor.getLong(cursor.getColumnIndex(DBCheckPointAdapter.COLUMN_RID)));
 				checkPoint.setId(cursor.getLong(cursor.getColumnIndex(DBCheckPointAdapter.COLUMN_ID)));
 
 				checkPointList.add(checkPoint);
 				cursor.moveToNext();
 			}
 			cursor.close();
 		}
 		dbCheckPointAdapter.close();
 
 		return checkPointList;
 	}
 	
 	/**
 	 * Updates a checkpoint information in the database.
 	 * 
 	 * @param checkPoint 
 	 * 				the new checkpoint
 	 */
 	public void updateCheckPoint(CheckPoint checkPoint)
 	{
 		dbCheckPointAdapter.open();
 		dbCheckPointAdapter.updateCheckPoint(checkPoint.getId(), checkPoint.getName(), checkPoint.getRadius());
 		dbCheckPointAdapter.close();
 	}
 
 	/**
 	 * Updates the checkpoints table in the database where the current route id
 	 * is -1. The passed in rid is used instead.
 	 * 
 	 * @param rid
 	 *            the route id to connect the checkpoints with.
 	 */
 	public void updateCheckPointRid(long rid)
 	{
 		dbCheckPointAdapter.open();
 		dbCheckPointAdapter.updateCheckPointRid(rid);
 		dbCheckPointAdapter.close();
 	}
 }

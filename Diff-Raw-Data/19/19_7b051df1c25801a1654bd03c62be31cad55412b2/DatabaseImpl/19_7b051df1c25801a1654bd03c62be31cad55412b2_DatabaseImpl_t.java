 package com.km2team.syriush.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.km2team.syriush.R;
 import com.km2team.syriush.SyriushButton;
 
 import android.content.Context;
 import android.location.Location;
 
 
 public class DatabaseImpl extends Database
 {
 
 	private DataManipulator dm;
 	protected DataSerializer serializer;
 
 	DatabaseImpl(Context context){
 		super(context);
 		dm = new DataManipulator(context);
 		serializer = new DataSerializer(dm);
 	}
 	
 	@Override
 	public int startNewRoute(Point startPoint) throws DatabaseException
 	{
 		return dm.getNewRouteId(startPoint);
 	}
 
 	@Override
 	public void appendToRoute(int routeId, Point[] part) throws DatabaseException
 	{
 		dm.appendToRoute(routeId, part);
 	}
 
 	@Override
 	public int updateRoute(int routeId, String name, Integer end, Integer size, Integer length, Integer userId) {
 		return dm.updateRoute(routeId, name, end, size, length, userId);
 	}
 
 	@Override
 	public int newPoint(String name, double latitude, double longitude, double accuracy, Priority priority, int userId) {
 		return dm.insertPoint(name, latitude, longitude, accuracy, priority.toInt(), userId);
 	}
 	@Override
 	public int newPoint(String name, Location location) {
 		return dm.insertPoint(name, new Point(location));
 	}
 
 	@Override
 	public int newUtil(ArrayList<SyriushButton> list) throws DatabaseException {
 		return serializer.saveSyriushButtonArrayList(context.getString(R.string.menu_entrance_config), list);
 	}
 
 	@Override
 	public boolean editPoint(int id, String name, int latitude, int longitude, int accuracy, int priority) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean editPoint(String oldName, int oldLatitude, int oldLongitude, String name, int latitude, int longitude, int accuracy, int priority) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public Point getNearestPoint(Point point) {
 		// TODO Auto-generated method stub
 		return new Point("", 0.0, 0.0);
 	}
 
 	@Override
 	public List<Point> getAllPoints() {
 		return dm.selectAllPoints();
 	}
 
 	@Override
 	public List<Route> getAllRoutes() throws DatabaseException {
		return dm.selectAllRoutes();
 	}
 
 	@Override
 	public int getMyId() throws DatabaseException {
 		return dm.selectMyId();
 	}
 
 	@Override
 	public ArrayList<SyriushButton> loadUtil() throws DatabaseException {
 		return serializer.loadSyriushButtonArrayList(context.getString(R.string.menu_entrance_config));
 	}
 
 }

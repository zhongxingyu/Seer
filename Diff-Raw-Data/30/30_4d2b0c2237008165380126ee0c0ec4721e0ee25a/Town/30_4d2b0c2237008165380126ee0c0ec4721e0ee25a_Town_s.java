 package croo.szakdolgozat.shared;
 
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class Town implements IsSerializable
 {
 	private Coordinate coordinate;
 	private String name;
 
 	public Town()
 	{
 		/* GWT RPC needs an empty no-arguments constructor */
 	}
 
 	public Town(Coordinate coordinate, String name)
 	{
 		this.coordinate = coordinate;
 		this.name = name;
 	}
 
 	public LatLng getTownCoordinateInJSO()
 	{
 		return coordinate.getCoordinateInJSO();
 	}
 
 	public Coordinate getCoordinate()
 	{
 		return coordinate;
 	}
 
 	public void setCoordinate(Coordinate coordinate)
 	{
 		this.coordinate = coordinate;
 	}
 
 	public String getName()
 	{
		String wellFormattedName = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
 		return wellFormattedName;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 }

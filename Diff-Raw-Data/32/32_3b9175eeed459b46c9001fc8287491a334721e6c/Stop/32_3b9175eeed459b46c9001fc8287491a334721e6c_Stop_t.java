 package tedx.mpk.data;
 
 import tedx.mpk.Station;
 
 class Stop extends Station {
 	
	public Stop(String name,int lon,int lat)
 	{
 		super();
 		setName(name);
 		setLongitude(lon);
 		setLatitude(lat);
 	}
 	
 	Point getPoint()
 	{
 		return new Point(getLatitude(),getLongitude());
 	}
 }

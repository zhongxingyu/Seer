 import java.util.ArrayList;
 
 public final class Field extends PointCollection
 {
 	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();
 	private Noise noise = Noise.getInstance();
 	
 	public Field(ArrayList<Point> points)
 	{
 		super.addAll(points);
 	}
 	
 	public Cluster createCluster()
 	{
 		Cluster c = new Cluster();
 		clusters.add(c);
 		return c;
 	}
 	
 	public PointCollection getPointCollectionAtPosition(int x, int y)
 	{
 		return this.noise; // omdat KDE e.d. nog niet is toegepast is alles gewoon maar ff noise
 	}
 	
 	public PointCategory getPointCategoryOfPoint(Point point)
 	{
 		return point.getPointCategory();
 	}
 	
 	public PointCategory getPointCategoryWithIndex(int index)
 	{
 		try
 		{
 			return this.clusters.get(index); // hoeft niet de goeie te zijn, dit is ff tijdelijk zo
 		}
		catch(IndexOutOfBoundsException e)
 		{
 			return null;
 		}
 	}
 	
 	public Noise getNoise()
 	{
 		return this.noise;
 	}
 	
 	public void scale(int x, int y) { }
 	public void floodFill(int x, int y, Cluster cluster) { }
 }

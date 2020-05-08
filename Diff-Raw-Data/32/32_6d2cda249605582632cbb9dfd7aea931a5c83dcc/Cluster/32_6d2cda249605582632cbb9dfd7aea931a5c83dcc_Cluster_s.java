 import java.util.ArrayList;
 
 public class Cluster
 {
 	private ArrayList<ClusterPoint> points;
 	private ClusterPoint centoid;
 	private String name;
 	
 	public Cluster()
 	{
 		points = new ArrayList<ClusterPoint>();
 		centoid = new ClusterPoint();
 		name = null;
 	}
 	
 	public Cluster(ClusterPoint point, String name)
 	{
 		points = new ArrayList<ClusterPoint>();
 		points.add(point);
 		centoid = point;
 		this.name = name;
 	}
 	
 	// Cluster Name
 	public String getName()					{ return name;						}
 	public void setName(String name)		{ this.name = name;					}
 	
 	// Points in Cluster
 	public ArrayList<ClusterPoint> getPoints()		{ return points;					}
 	public ClusterPoint getPointAt(int index)		{ return points.get(index); 		}
 	public ClusterPoint removePointAt(int index)	{ return points.remove(index);		}
 	public boolean removeAllPoints()		{ return points.removeAll(points);	}
 	public void addPoint(ClusterPoint point)		{ points.add(point);				}
 	public int getNumberOfPoints()				{ return points.size();				}
 	
 	// Centoid
	public ClusterPoint getCentoid()				{ return centoid;					}
 	public void calculateCentoid()
 	{
 		ClusterPoint centoid = new ClusterPoint();
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 		{
			Double sum = 0.0;
 			for(ClusterPoint p : points)
 				sum += p.getValueAtDimension(i);
 
			centoid.addDimension(sum/points.size());
 		}
 		this.centoid = centoid;
 	}
 	
 	public boolean isEqual(Cluster c)
 	{
 		if(!name.equals(c.getName()))
 			return false;
 		if(c.getNumberOfPoints() != points.size())
 			return false;
 		for(int i = 0; i < points.size(); i++)
 		{
 			if(!points.get(i).isEqual(c.getPointAt(i)))
 				return false;
 		}
 		if(!centoid.isEqual(c.getCentoid()))
 			return false;
 
 		return true;
 	}
 }

 
 public class Vertex 
 {
 	private String name;
 	private int id;
 	private int x;
 	private int y;
 	private int prev_x;
 	private int prev_y;
 	private boolean visited;
 	boolean isSelected = false;
 	boolean beingModified = false;
 
 	public Vertex(String name, int id, int x, int y)
 	{
 		this.name = name;
 		this.id = id;
 		this.x = x;
 		this.y = y;
 		this.prev_x = x;
 		this.prev_y = y;
 		this.visited = false;
 		this.isSelected = false;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 
 	public void setID(int id)
 	{
 		this.id = id;
 	}
 
 	public void setVisited(boolean vis)
 	{
 		this.visited = vis;
 	}
 
 	public int getX()
 	{
 		return x;
 	}
 
 	public int getY()
 	{
 		return y;
 	}
 
 	public int getID()
 	{
 		return id;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public boolean getVisited()
 	{
 		return visited;
 	}
 
 	public String toString()
 	{
 		return "Vertex \"" + name + "\", id=" + id + ", x=" + x + ", y=" + y; 
 	}
 
 	public boolean equals(Object obj) 
 	{
 		Vertex other = (Vertex) obj;
 		return ((other.getX() == this.getX()) && (other.getY() == this.getY()) && (other.getID() == this.getID()) && (other.getName().equals(this.getName())));
 	}
 
 	public boolean isThisMe(Vertex p) 
 	{
 		int px = p.getX();
 		int py = p.getY();
 		int radius = MapScene.SIZE/2;
 		return px > x - radius && px < x + radius && 
 				py > y - radius && py < y + radius;
 	}
 	
 	public void reset()
 	{
 		x = prev_x;
 		y = prev_y;
 	}
 	
 	public void update()
 	{
 		prev_x = x;
 		prev_y = y;
 	}
 
 }

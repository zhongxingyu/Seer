 package rmit.ai.clima.jackagt;
 import rmit.ai.clima.gui.grid.*;
 
 public class PathNode implements Comparable
 {
    public boolean obstacle;
    public boolean visited;
    public GridPoint pos;
    public String dir;
    public int g;
    public int h;
    public int f;
 
    public PathNode ()
    {
       visited = false;
       obstacle = false;
      pos = new GridPoint(0,0);
       dir = "";
       g = 0;
       h = 0;
       f = 0;
    }
 
    public boolean equals (Object o)
    {
       PathNode other = (PathNode)o;
       return (f == other.f);
    }
 
    public int compareTo (Object o)
    {
       PathNode other = (PathNode)o;
       return other.f - f;
    }
 }

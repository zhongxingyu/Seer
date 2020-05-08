 package mc.alk.util.objects;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.bukkit.Location;
 
 public class Hologram
 {
 	public enum VerticalTextSpacing
 	{
		COMPACT(0.30), SPACED(0.40);
 		private final double spacing;
 
 		VerticalTextSpacing(double spacing)
 		{
 			this.spacing = spacing;
 		}
 
 		public double spacing()
 		{
 			return spacing;
 		}
 	}
 
 	private String leaderboardName;
 	private double distanceBetweenLines;
 	private ArrayList<String> lines;
 	private ArrayList<Integer> ids;
 	private Location location;
 	private boolean showing;
 
 	public Hologram(String leaderboardName, VerticalTextSpacing type,
 			Location location, String... lines)
 	{
 		this(leaderboardName, type.spacing(), location, lines);
 	}
 
 	public Hologram(String leaderboardName, double distanceBetweenLines,
 			Location location, String... lines)
 	{
 		this.lines = new ArrayList<String>();
 		this.ids = new ArrayList<Integer>();
 		this.leaderboardName = leaderboardName;
 		this.distanceBetweenLines = distanceBetweenLines;
 		this.lines.add(leaderboardName);
 		this.lines.addAll(Arrays.asList(lines));
 		this.location = location;
 		this.showing = false;
 	}
 
 	public double getDistanceBetweenLines()
 	{
 		return distanceBetweenLines;
 	}
 
 	public ArrayList<String> getLines()
 	{
 		return lines;
 	}
 
 	public ArrayList<Integer> getIds()
 	{
 		return ids;
 	}
 
 	public String getLeaderboardName()
 	{
 		return leaderboardName;
 	}
 
 	public Location getLocation()
 	{
 		return location;
 	}
 
 	public void setLeaderboardName(String leaderboardName)
 	{
 		this.leaderboardName = leaderboardName;
 	}
 
 	public void setLines(String... lines)
 	{
 		this.lines.clear();
 		this.lines.add(leaderboardName);
 		this.lines.addAll(Arrays.asList(lines));
 	}
 
 	public void setLocation(Location location)
 	{
 		this.location = location;
 	}
 
 	public void setShowing(boolean option)
 	{
 		this.showing = option;
 	}
 
 	public boolean isShowing()
 	{
 		return showing;
 	}
 }

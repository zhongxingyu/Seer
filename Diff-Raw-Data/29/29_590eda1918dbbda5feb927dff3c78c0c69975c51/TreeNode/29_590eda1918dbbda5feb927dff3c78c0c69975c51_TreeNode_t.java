 package eu.MrSnowflake.android.gametemplate;
 
import android.util.Log;

 
 
 public class TreeNode {
 
 	private TreeNode[] children;
 	private Point location;
 	public TreeNode(TreeNode[] children,Point loc )
 	{
 		this.children = children;
 		this.location = loc;
 	}
 	public TreeNode[] getChildren() {
 		return children;
 	}
 	public void setChildren(TreeNode[] children) {
 		this.children = children;
 	}
 	public Point getLocation() {
 		return location;
 	}
 	public void setLocation(Point location) {
 		this.location = location;
 	}
 	
 	public void branch(int numChildren, float lengthOfBranch, Point parentLocation)
 	{
 		//Split the current node into numChildren branches.
 		//parentLocation is the location of the parent of the node that calls this method
 		
 		//Calculate the x and y displacement of the branching node from its parent (for use with angle computation)
 		double xDif = parentLocation.getX()-this.location.getX();
 		double yDif = parentLocation.getY() - this.location.getY();
 		//Compute the angle of the current node by comparing it to its parent location
 
 		double thetaNought = Math.atan(xDif/yDif);
 //		if(yDif<0)
 		//thetaNought= Math.PI-thetaNought;	
 		
 		//Make empty children for the current node
 		this.children = new TreeNode[numChildren];
 		
 		//Configure properties of each child (currently fixed to specific angles)
 		for( int i =0; i < numChildren; i ++)
 		{
			//if(yDif<0)
				//thetaNought += Math.PI;
 			double angleToTurn = thetaNought + ((1-i)*(Math.PI/6));
 			//double angleToTurn = thetaNought + ((1-i)*(Math.PI/2));
			if(yDif<0)
			{			Log.i("Theta0=" + thetaNought + " angleToTurn="+angleToTurn, "debugangles");
			angleToTurn += Math.PI;
			}
 			
 			float xDisp =(float)(lengthOfBranch*(Math.sin(angleToTurn)));
 			float yDisp =(float)(lengthOfBranch*(Math.cos(angleToTurn)));			
 			//translate from this location by xDisp and yDisp to generate next node's locations
 			//Point childLoc = new Point(xDisp + this.getLocation().getX(),yDisp + this.getLocation().getY()); // decsribe childoc
 			children[i] = new TreeNode(null,new Point(this.getLocation().getX()-xDisp,this.getLocation().getY()-yDisp));
 		}
 		
 	}
 
 	public String toString()
 	{
 		if(this.getLocation() == null)
 			return "";
 		String base = this.getLocation().toString();
 		if(this.children != null)
 			for (TreeNode tn : this.children)
 				base += tn.toString();
 		return base;
 	}
 	
 
 }

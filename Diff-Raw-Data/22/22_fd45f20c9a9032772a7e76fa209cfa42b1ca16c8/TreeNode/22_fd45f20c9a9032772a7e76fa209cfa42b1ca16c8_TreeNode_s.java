 package eu.MrSnowflake.android.gametemplate;
 
 import android.util.Log;
 
 
 
 public class TreeNode {
 
 	private TreeNode[] children;
 	private Point displacement;
 	public TreeNode(TreeNode[] children,Point disp )
 	{
 		this.children = children;
 		this.displacement = disp;
 	}
 	public TreeNode[] getChildren() {
 		return children;
 	}
 	public void setChildren(TreeNode[] children) {
 		this.children = children;
 	}
 	public Point getDisplacement() {
 		return displacement;
 	}
 	public void setLocation(Point displacement) {
 		this.displacement = displacement;
 	}
 	
 	public void branch(int numChildren, float lengthOfBranch, Point displacementFromParentToMe)
 	{
 		//Calculate the x and y displacement of the branching node from its parent (for use with angle computation)
 		double xDif = displacementFromParentToMe.getX();
 		double yDif = displacementFromParentToMe.getY();
 		//Compute the angle of the current node by comparing it to its parent location
 		double thetaNought = Math.atan(xDif/yDif);	
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
 			{
 			Log.i("Theta0=" + thetaNought + " angleToTurn="+angleToTurn, "debugangles");
 			angleToTurn += Math.PI;
 			}
 			
 			float xDisp =(float)(lengthOfBranch*(Math.sin(angleToTurn)));
 			float yDisp =(float)(lengthOfBranch*(Math.cos(angleToTurn)));			
 			//translate from this location by xDisp and yDisp to generate next node's locations
 			//Point childLoc = new Point(xDisp + this.getLocation().getX(),yDisp + this.getLocation().getY()); // decsribe childoc
 			children[i] = new TreeNode(null,new Point(xDisp,-yDisp));
 		}
 		
 	}
 
 	public String toString()
 	{
 		if(this.getDisplacement() == null)
 			return "";
 		String base = this.displacement.toString();
 		if(this.children != null)
 			for (TreeNode tn : this.children)
 				base += tn.toString();
 		return base;
 	}
 	
 
 }

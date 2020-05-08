 package eu.MrSnowflake.android.gametemplate;
 
 import android.util.Log;
 import android.widget.Toast;
 
 
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
		double xDif = parentLocation.getX()-this.location.getX();  // calculate the two displacement componenets
		double yDif = parentLocation.getY() - this.location.getY(); // calculate the two displacement componenets
 		double thetaNought = Math.atan(xDif/yDif);
 		this.children = new TreeNode[numChildren];
 		for( int i =0; i < numChildren; i ++)
 		{
 			double angleToTurn = thetaNought + ((1-i)*(Math.PI/3));
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
 	
 	public void translateChildren(float dx, float dy)
 	{
 		translateChildren(this,dx,dy);
 	}
 	private void translateChildren(TreeNode tn, float dx, float dy)
 	{
 		if (tn == null || tn.getLocation() == null)
 			return;
 		tn.location.setX(tn.location.getX() + dx);	
 		tn.location.setY(tn.location.getY() + dy);
 		if(tn.getChildren() != null)
 		{
 			for( TreeNode child : tn.getChildren())
 			{
 				if(child == null || child.getLocation() == null)
 					continue;
 				translateChildren(child,dx,dy);
 			}
 		}
 	}
 }

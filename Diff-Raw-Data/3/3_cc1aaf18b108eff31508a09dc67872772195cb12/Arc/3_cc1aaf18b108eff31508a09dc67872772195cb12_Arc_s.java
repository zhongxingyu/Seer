 package rsmith.fortune.point;
 
 public class Arc {
 	private BreakPoint right;
 	private BreakPoint left;
 
 	public Arc(BreakPoint left, BreakPoint right) {
 		this.left = left;
 		this.right = right;
 	}
 
 	public BreakPoint getRight() {
 		return right;
 	}
 
 	public void setRight(BreakPoint right) {
 		this.right = right;
 	}
 
 	public BreakPoint getLeft() {
 		return left;
 	}
 
 	public void setLeft(BreakPoint left) {
 		this.left = left;
 	}
 
 	public SitePoint getSite() {
		SitePoint result = (left == null ? right.getLeft()
				: (right == null ? left.getRight() : null));
 		return result;
 	}
 }

 import java.util.ArrayList;
 
class KDNode
 {
     private KDNode left, right;
    private ArrayList<Double> position;
     private int axis;
 
    public KDNode(ArrayList position, int axis)
     {
         this.position = position;
         this.axis = axis;
     } 
 
     public void setLeft(KDNode left)
     {
 //        System.out.println("setleft");
         this.left = left;
     }
 
     public void setRight(KDNode right)
     {
 //        System.out.println("setright");
         this.right = right;
     }
 
     public KDNode getLeft()
     {
         return left;
     }
 
     public KDNode getRight()
     {
         return right;
     }
 
    public ArrayList getPoint()
     {
         return position;
     }
 
     public int getAxis()
     {
         return axis;
     }
 
    public ArrayList<Double> getSplittingPlane()
     {
        double axis_value = position.get(axis);
        
        ArrayList<Double> splitting_plane = new ArrayList<Double>();
 
        for(int i = 0; i < position.size(); i++)
        {
            splitting_plane.add(0.0);
        }
        
        splitting_plane.set(axis, axis_value);

        return splitting_plane;
     }
 }

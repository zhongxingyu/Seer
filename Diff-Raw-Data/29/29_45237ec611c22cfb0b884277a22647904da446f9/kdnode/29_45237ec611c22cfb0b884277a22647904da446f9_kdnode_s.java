 import java.util.ArrayList;
 
class KDNode<T extends Number>
 {
     private KDNode left, right;
    private ArrayList<T> position;
     private int axis;
 
    public KDNode(ArrayList<T> position, int axis)
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
 
    public ArrayList<T> getPoint()
     {
         return position;
     }
 
     public int getAxis()
     {
         return axis;
     }
 
    public T getSplitValue()
     {
        T axis_value = position.get(axis);
 
        return axis_value;
     }
 }

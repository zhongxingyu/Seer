 /**
  * User: Gifflen
  * Child = [2i+1] [2i+2]
  * Parent = [(i-1)/2]
  */
 public class BinaryTree<E> {
 
     final private int LEFT = 1;
     final private int RIGHT = 2;
     final private int[] BRANCHES = {LEFT,RIGHT};
 
     //private E[] arrayContainer;
     private Object[] arrayContainer;
     private Object[] branchData;
     public BinaryTree() {
         arrayContainer = new Object[10];
     }
 
 
     public void getDataAtIndex(int index){
         E node = getData(index);
         branchData = getBranches(index);
         if (node!=null){
             System.out.println("  "+node+"  ");
             System.out.println(" / \\");
             for(Object data: branchData)
                 System.out.print(data+"   ");
             System.out.println();
         }else{
            System.out.println("Tree Does not exit");
         }
 
 
     }
     public E getData(int index){
         try{
             return (E) arrayContainer[index];
         }catch (ArrayIndexOutOfBoundsException e){
             return null;
         }
     }
 
     private Object[] getBranches(int index){
         branchData = new Object[BRANCHES.length];
         for(int side: BRANCHES){
             E data = getData(2*index+side);
             branchData[side-1]= data;
         }
         return branchData;
     }
     private static int getIndex(int index,int side){
         return (2*index+side);
     }
     public int getLeftIndex(int index){
         return getIndex(index, LEFT);
     }
 
     public int getRightIndex(int index){
         return getIndex(index,RIGHT);
     }
 
 
     public E getLeftData(int index){
         return getData(this.getLeftIndex(index));
     }
 
     public E getRightData(int index){
        return getData(this.getRightIndex(index));
     }
 
     private int getExtremeIndex(int index,int side){
         int branchIndex = getIndex(index,side);
         E data = this.getData(branchIndex);
         if (data!=null){
             return getExtremeIndex(branchIndex, side);
         }else{
             return index;
         }
     }
 
 
     public int getLeftMostIndex(int index){
         return this.getExtremeIndex(index, LEFT);
     }
 
     public int getRightMostIndex(int index){
          return this.getExtremeIndex(index, RIGHT);
     }
 
     public E getLeftMostData(int index){
         return getData(this.getLeftMostIndex(index));
     }
 
     public E getRightMostData(int index){
         return getData(this.getRightMostIndex(index));
     }
 
     public boolean isLeaf(int index){
         return (getLeftData(index) == null) && (getRightData(index) == null);
     }
     //TODO: move data back up the tree if a removal is occurring.
     private void removeIndex(int index){
         arrayContainer[index] = null;
     }
     public void removeLeft(int  index){
         removeIndex(getLeftIndex(index));
     }
 
     public void removeRight(int index){
        removeIndex(getRightIndex(index));
     }
 
     public void removeLeftMost(int index){
         removeIndex(getLeftMostIndex(index));
     }
 
     public void removeRightMost(int index){
         removeIndex(getRightMostIndex(index));
     }
 
     public void addNode(E data){
         boolean added=false;
         for(int i = 0; i<arrayContainer.length;i++){
             if (arrayContainer[i]==null){
                 arrayContainer[i] = data;
                 added = true;
                 break;
             }
         }
         if(!added){
             expandSize();
             this.addNode(data);
         }
     }
 
     public void removeLast(){
         for(int i = arrayContainer.length-1; i>=0;i--){
              if (arrayContainer[i]!=null){
                 arrayContainer[i]=null;
                  break;
              }
         }
     }
 
     private void expandSize(){
         Object[] newContainer = new Object[arrayContainer.length*2] ;
         System.arraycopy(arrayContainer, 0, newContainer, 0, arrayContainer.length);
         arrayContainer = newContainer;
     }
 
     public int getDepth(){
         int depth = 0;
         int counter = 1;
         while(counter<=arrayContainer.length&&arrayContainer[counter-1]!=null){
             counter+=counter;
             depth++;
         }
         return depth;
     }
 
 
 
     public void totalTreeCall(){
 
     }
 }

 import java.lang.reflect.Array;
 
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
         //arrayContainer = (E[]) Array.newInstance(incomingClass,10);
         //branchData = (E[]) Array.newInstance(incomingClass,2);
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
     public int getLeft(int index){
         return (2*index+LEFT);
     }
 
     public int getRight(int index){
         return (2*index+RIGHT);
     }
 
 
     public E getLeftData(int index){
         return getData(this.getLeft(index));
     }
 
     public E getRightData(int index){
        return getData(this.getRight(index));
     }
 
     public BinaryTree<E> getLeftMost(){
 
         return null;
     }
 
     public BinaryTree<E> getRightMost(){
         return null;
     }
 
     public E getLeftMostData(){
         return null;
     }
 
     public E getRightMostData(){
         return null;
     }
 
     public boolean isLeaf(int index){
         if((getLeftData(index)==null)&&(getRightData(index)==null))
             return true;
         return false;
     }
 
     public BinaryTree<E> removeLeft(){
         return null;
     }
 
     public BinaryTree<E> removeRight(){
         return null;
     }
 
     public BinaryTree<E> removeLeftMost(){
         return null;
     }
 
     public BinaryTree<E> removeRightMost(){
         return null;
     }
 
     public void addNode(E data){
         boolean added=false;
         for(int i = 0; i<arrayContainer.length;i++){
             if (arrayContainer[i]==null){
                 arrayContainer[i] = (E)data;
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
 
         for(int i = 0; arrayContainer[i]!=null;i--){
 
         }
 
     }
 
     private void expandSize(){
         Object[] newContainer = new Object[arrayContainer.length*2] ;
         for(int i = 0;i<arrayContainer.length;i++)
             newContainer[i] = arrayContainer[i];
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

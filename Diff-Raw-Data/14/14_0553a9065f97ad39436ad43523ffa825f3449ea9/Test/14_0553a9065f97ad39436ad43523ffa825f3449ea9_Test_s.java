 import java.util.Random;
 
 public class Test {
   public static void main(String[] args) {
     System.out.println(run(10000));
   }
 
  public static int run(int numTrials) {
    int sum = 0;
     for(int i = 0; i < numTrials; i++) {
       sum += trial();
     }
     return sum/numTrials;
   }
   
   public static int trial() {
     return getDepth(populate(randomize(1000)));
   }
   
   public static BSTNode populate(int[] arr) {
     BSTNode tree = new BSTNode();
     for(int i = 0; i < arr.length; i++) {
       tree.insert(arr[i]);
     }    
     return tree;
   }
   
   public static int[] randomize(int total) {
     Random rand = new Random();
     Integer[] arr = new Integer[1000];
     boolean watchdoge;
     
     for(int i = 0; i < total; i++) {
       watchdoge = true;
       while(watchdoge) {
 	int r = (int)(rand.nextDouble() * 1000);
 	if(arr[r] == null) {
 	  arr[r] = i;
 	  watchdoge = false;
 	}
       }
     }
     return toIntArr(arr);
   }
 
   public static int[] toIntArr(Integer arr[]) {
     int[] ret = new int[arr.length];
     for(int i = 0; i < arr.length; i++) {
       ret[i] = arr[i];
     }
     return ret;
   }
 
   public static int getDepth(BSTNode node) {
     if (node.left == null && node.right == null) {
       return 0;
     }
     int l = getDepth(node.left);
     int r = getDepth(node.right);
     if(l > r) {
       return l + 1;
     }
    return r + 1;
   }
 }

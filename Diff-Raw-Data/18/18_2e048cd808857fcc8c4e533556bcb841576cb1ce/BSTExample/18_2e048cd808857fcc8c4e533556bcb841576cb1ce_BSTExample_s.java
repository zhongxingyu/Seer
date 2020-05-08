 
 public class BSTExample{
     public static void main(String[] args){
         BST b = new BST(5);
         b = b.insert(10);
         b = b.insert(15);
         b = b.insert(12);
         b = b.insert(2);
         b = b.insert(1);
 
        LList l = new LList();
        l = new LList(-3, l);
        l = new LList(6, l);
        l = new LList(7, l);
        l = new LList(8, l);
     }
 
 }

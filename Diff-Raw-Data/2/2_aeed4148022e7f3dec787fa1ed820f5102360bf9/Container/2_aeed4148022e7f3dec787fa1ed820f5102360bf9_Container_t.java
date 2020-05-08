 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package original;
 
 
 class ContainerDoublyLinkList {
     public String name;
     public NodeDoublyLinkList head;
 
     public ContainerDoublyLinkList(String name) {
         this.name = name;
         head = null;
     }
     
     public String getName() {
         return name;
     }
 
     public void delete(NodeDoublyLinkList n) {
         if (n == null) {
             return;
         }
         if (n.left != null) {
             n.left.right = n.right;
             n.left = null;
         }
         if (n.right != null) {
             n.right.left = n.left;
             n.right = null;
         }
     }
 
     public void insert(NodeDoublyLinkList n) { 
         n.left = null;
         if (head != null) {
             head.left = n;
         }
         n.right = head;
         head = n;
     }
 
     public NodeDoublyLinkList getHead() {
         return head;
     }
 }
 
 class ContainerSizeOf extends ContainerDoublyLinkList {
     public int sizeOf;
     
     public ContainerSizeOf(String name) {
         super(name);
         sizeOf = 0;
     }
     
     @Override
     public void delete(NodeDoublyLinkList n) {
         if (n == null) {
             return;
         }
             
         sizeOf--;
         super.delete(n);
     }
     
     @Override
     public void insert(NodeDoublyLinkList n) {
         sizeOf++;
         super.insert(n);
     }
 }
 
 class ContainerTimeStamp extends ContainerSizeOf {
     public int counter;
     
     public ContainerTimeStamp(String name) {
         super(name);
         counter = 0;
     }
     
     public void insert(NodeTimeStamp n) {
         n.creation_time = ++counter;
     } 
         
 }
 
 class ContainerPrint extends ContainerTimeStamp{
     public static boolean debug = false;  // set to true for debugging
     
     ContainerPrint(String name) {
         super(name);
     }
     
     public void print() {
         System.out.println(name + "{ // has " + sizeOf + " elements");
         IteratorPrint i = new IteratorPrint(this);
         while (i.hasNext()) {
             NodePrint n = i.getNext();
             System.out.println("   " + n);
         }
         System.out.println("}");
     }
 }
 
public class Container extends ContainerPrint{
     Container(String name) {
         super(name);
     }
 }

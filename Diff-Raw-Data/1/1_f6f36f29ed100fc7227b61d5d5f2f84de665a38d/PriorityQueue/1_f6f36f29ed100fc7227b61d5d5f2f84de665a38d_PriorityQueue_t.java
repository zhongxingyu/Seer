 //PriorityQueue.java: wenlong
 //Description: * the priority queue is maintained in a heap-ordered complete binary tree;
 //               heap-ordered if the key in each node >= the keys in the node's two children
 //             * heap priority queue
 //             * 
//TODO: fix the bugs
 //
 //Performance:
 //      the heap algorithms require no more than 1 + lgN compares for insert,
 //      no more than 2lgN compares(find the larger child and decide whether the child needs to be promoted) for remove the maximum
 //------------------------------------------------------------------------------------------
 
 public class PriorityQueue
 {
     private Comparable[] pq;  //heap-ordered complete binary tree
     private int N = 0;  //in pq[1..N] with pq[0] unused
 
     public PriorityQueue(int max){
         pq = new Comparable[max + 1];  //pq[0] unused
     }
 
     public boolean isEmpty()
     {
         return N == 0;
     }
 
     public int size()
     {
         return N;
     }
 
     //more compact implementations,
     //don't involve passing the array name as a parameter
     public boolean less(int i, int j)
     {
         return pq[i].compareTo(pq[j]) < 0;
         
     }
     
     public void swap(int i, int j)
     {
         Comparable temp = pq[i];
         pq[i] = pq[j];
         pq[j] = temp;
     }
 
     //add the new key at the end of the array, and use swim() to restore the heap order
     public void insert(Comparable v)
     {
         pq[++N] = v;  //pq[1..N]
         // swim up through the heap with the key to restore the heap condition
         swim(N);   
     }
 
     //Bottom-up reheapity(swim):if a node's key becomes larger than the node's parent's key,
     //exchange the node with its parent
     public void  swim(int k)
     {
         while(less(k/2, k) && k > 1){
             swap(k/2, k);
             k = k/2;
         }
     }
 
 
     //remove the maximum
     //take the value from pq[1], exchange the end of the heap with root
     //then decrement the size of the heap, and use sink() to restore the heap condition
     public Comparable delMax()
     {
         Comparable max = pq[1];
         swap(1, N--);    //exchange with last iem, move pq[N] to pq[1]
         pq[N+1] = null;  //avoid loitering
         sink(1);   //sink down through the heap with the key
         
         return max;
     }
 
     //Top-down reheapify(sink): a node's key is smaller than one or both of the node's children's keys,
     //exchange the node with the larger of its two children
     public void sink(int k)
     {
         while( 2*k + 1 < N){
             int j = 2*k;
             if(less(j, j+1))
                 j++;  //very compact code to get the larger child
             
             //here j has represented the max child between left and right children
             if(!less(k, j)) //whether the child needs to be promoted
                 break;
 
             swap(k, j);
             k = j;  //continue
         }
         
     }
 
     public static void main(String[] args)
     {
         //print the top M lines in the input stream
         int M = Integer.parseInt(args[0]);
         PriorityQueue  pq = new PriorityQueue(M);
         
         while(StdIn.hasNextLine()){
             pq.insert(StdIn.readLine());
             if(pq.size() > M)
                 pq.delMax(); //remove max if M+1 entries on the PQ
         }// Top M entries
 
         Stack<String> s = new Stack<String>();
         while(!pq.isEmpty())
             s.push(pq.delMax());
         
         while(!s.isEmpty()){
             StdOut.print(s.pop() + " ");
         }
         StdOut.println();
         
     }
             
 }

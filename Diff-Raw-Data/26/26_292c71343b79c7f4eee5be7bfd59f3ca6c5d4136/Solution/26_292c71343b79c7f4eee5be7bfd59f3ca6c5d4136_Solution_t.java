 public class Solution {
     int [] getKWindowMax(int [] A, int k){
        
         if(A == null || A.length < k)
             return null;
        int [] res = new int [A.length - k +1];
         LinkedList<Integer> deque = new LinkedList<Integer>();
 
         for(int i = 0; i<A.length; ++i){
            Integer before = deque.peekLast();
             while(before != null && before < A[i]){
                deque.removeLast();
                before = deque.peekLast();
             }
            deque.addLast(A[i]);

             if(i >= k-1){
                res[i-k+1] = deque.peekFirst();
                if(A[i-k+1] == deque.peekFirst())
                    deque.removeFirst();
             }
         }
         return res;
     }
 }

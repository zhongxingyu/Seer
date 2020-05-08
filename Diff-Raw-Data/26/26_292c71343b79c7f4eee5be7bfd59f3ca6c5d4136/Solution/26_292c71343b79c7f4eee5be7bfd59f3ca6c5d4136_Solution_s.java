 public class Solution {

     int [] getKWindowMax(int [] A, int k){

         if(A == null || A.length < k)
             return null;
        int res = new int [A.length - k +1];
         LinkedList<Integer> deque = new LinkedList<Integer>();
 
         for(int i = 0; i<A.length; ++i){
            deque.addLast(A[i]);
            int before = deque.peekFirst();
             while(before != null && before < A[i]){
                deque.removeFirst();
                before = deque.peekFirst();
             }
             if(i >= k-1){
                res[i-k+1] = deque.peekLast();
                if(A[i-k+1] == deque.peekLast())
                    deque.removeLast();
             }
         }
         return res;
     }
 }

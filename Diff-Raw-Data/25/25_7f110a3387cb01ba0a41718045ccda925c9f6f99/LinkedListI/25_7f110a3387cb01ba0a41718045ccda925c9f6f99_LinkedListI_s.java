 public class LinkedListI
 {
     Node first;//is a pointer
 
     public void add(int data)
     {
 	Node temp = new Node();
 	temp.pointer = first;
 	temp.data = data;
 	first = temp;
     }//add    
 
 
     public int factorial(int n)
     {
 	if(n==1) return 1;
 	return n*factorial(n-1);
 
    }
 
     public void print(Node n)
     {
        
 	System.out.println(n.data);
 
 	if(n.pointer!= null)
 	    {
 		print(n.pointer);
 	    }
     }//print
 
     public int size(Node n)
     {
 		
 	if(n.pointer!=null)
 	    {
 		return 1+size(n.pointer);
 	    }
 
 	return 1;
     }//size
 
 
    public void set(int index, int data, Node n)
     {
 	if(index==0) n.data=data;
 	else set(index-1, data, n.pointer);
     }//set
 
 
 /**
        Authors: Emily Lovett and Alex Mayer
        Input: Node n
        Output: nothing
        Function: Removes the first node in the linked list by setting "first" to the second one.
      **/
 
     public void removefirst(Node n)
     {
 	first = n.pointer;
     }//removefirst
 
 /**
        Authors: Emily Lovett and Alex Mayer
        Input: Node n
        Output: nothing
        Function: Removes the last node in the linkedlist by setting the second to last node's pointer to null. 
      **/
 
     public void removelast(Node n)
     {
         if(n.pointer.pointer!= null)  removelast(n.pointer);
           
 	else if (n.pointer.pointer == null) n.pointer = null;    
     }//removelast
 
 
     /**
        Authors: Emily Lovett and Alex Mayer
        Input: index, node n
        Output: nothing
        Function: Can remove any node in the linkedlist, also can remove first and last ones.
 
      **/
     public void remove(int index, Node n)
     {
         if(index==1) n.pointer = n.pointer.pointer;
 	
 	else if(index==0) removefirst(n);
 
         else remove(index-1, n.pointer);
     }//set  
 
     /*Name: Clone
       Function: Returns a duplicate of original LinkedList
       Authors: Maya Klabin and Xander Chase
     */
 
     public Node clone(Node n)
     {    
 	
 	if(n.pointer!=null)
 	    {
 		Node duplicate = new Node();
 		duplicate.data = n.data;
 		duplicate.pointer = clone(n.pointer);
 		return duplicate;
 	    }//if
 	else
 	    {
 		Node duplicate = new Node();
 		duplicate.pointer = null;
 		duplicate.data = n.data;
 		return duplicate;
 	    }//else
     }//clone
 }//class

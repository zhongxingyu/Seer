 public class LinkedListH{
 
  //Author: All of Us
     Node first; //pointer to a Node
 
     LinkedListH(){}
 
     void  addfirst(double data)
     {
 	Node newone = new Node();
 	newone.setData(data);
 	newone.setPointer(first);
 	first = newone;
     }
     void print(Node n)
     {
 	System.out.println(n.getData());
         if(n.getPointer()!=null)
 	    {
 		print(n.getPointer());
 	    }   
     }
 
     int size(Node n)
     { 
 	if (n.getPointer()!=null)
 	    {
 		return 1+size(n.getPointer());
 	    }
 	return 1;
     }
 
     void set(Node n, int index, double d)
     {
 	if (!(index==0))
 	    {
 		set(n.getPointer(), index-1, d);
       	    }
 	n.setData(d);
     }
 
   //Author: Katherine
   //Function: get
     double get(int index, Node n)
     {
 	if (index!= 0)
        	    {
 		if (n.pointer!= null) 
 		    {
 			get(index-1, n.pointer);
 		    }
 		else 
 		    {
 			System.out.println("out of bounds Exception!");
 		    }
 	    }
 	return n.data;
     }
 
}//class
}
 
     //Sophia
     // isempty 
  boolean isEmpty ()
     {
 	if (first==null)
 	    { 
 		return true;
 	    }
 	return false;
     }
 
     //Sophia 
     // contains 
     boolean contains (Node n, double d)
     {
 	if(n.data == d) return true;
 	else if(n.pointer==null) return false;
 	else return contains(n.pointer, d);
     }
     
 //class
 }

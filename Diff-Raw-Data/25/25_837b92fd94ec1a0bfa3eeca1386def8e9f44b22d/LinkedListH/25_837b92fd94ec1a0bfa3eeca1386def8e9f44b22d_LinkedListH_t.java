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
    //Function: toArray
    double[] toArray ()
    {
	double[] newArray;
	newArray = new double[size(first)];
	Node temp= first; 
	for (int i=0; i<newArray.length; i++)
	    {
		newArray[i]= temp.data; 
		temp= first.pointer;
	    }	
	return newArray;
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
    } //end of get

 
 
 
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
